package com.kongdy.hasee.myapplication;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


public class MonthDateView extends View {

    private static final int NUM_COLUMNS = 7;
    private static int NUM_ROWS = 6;

    private int mDayColor = Color.parseColor("#505052");
    private int mSelectDayColor = Color.parseColor("#ffffff");
    private int mSelectBGColor = Color.parseColor("#1FC2F3");
    //周六、周日的颜色
    private int mWeekendColor = Color.parseColor("#f4906b");
    private int mWeekendDefaultColor = Color.parseColor("#b5b5b5");

    // default line color
    private int lineColor = Color.parseColor("#e3e3e3");
    private int mColumnSize, mRowSize;
    private float mDaySize = 18;
    private int mCircleRadius = 30;
    private DateClick dateClick;

    // paint
    private TextPaint mPaint;
    private Paint dayPaint;
    private Paint linePaint;
    private Paint leavePaint;

    private MonthData monthData;

    // 除去padding的界限
    private float leftGlobalStartX;
    private float topGlobalStartY;

    // 点击游标
    private int tempClickPos = -1;

    private int firstDay = 0;
    private int dayCount = 30;

    // 顶部星期
    private WeekDayView weekDayView;
    // 除去padding的size
    private int mWidth;
    private int mHeight;

    private float leaveCircleRadius;

    private int[] leaves = new int[0];

    public MonthDateView(Context context) {
        this(context, null);
    }

    public MonthDateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MonthDateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        mPaint = new TextPaint();
        dayPaint = new Paint();
        linePaint = new Paint();
        leavePaint = new Paint();

        // paint high effect
        highEffect(mPaint);
        highEffect(dayPaint);
        highEffect(linePaint);
        highEffect(leavePaint);

        mPaint.setTextSize(mDaySize);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(getDimenSize(TypedValue.COMPLEX_UNIT_DIP,1));

        leaveCircleRadius = getDimenSize(TypedValue.COMPLEX_UNIT_DIP,2);

        weekDayView = new WeekDayView(getContext());

        monthData = new MonthData(Calendar.getInstance());

    }

    private void highEffect(Paint paint) {
        paint.setDither(true); // 防抖
        paint.setAntiAlias(true); // 抗锯齿
        paint.setFilterBitmap(true); // 滤波
        if (paint instanceof TextPaint) {
            paint.setSubpixelText(true); // 像素自处理，用于文字能产生更好效果，用于非文字绘制会产生不必要的性能浪费
            paint.setTextAlign(Paint.Align.CENTER);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 这里进行初始化，从始至终，只有当界面size变化的时候才会执行一次，不会因为每次绘制而造成的性能不必要的浪费

        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();


        weekDayView.measure(MeasureSpec.makeMeasureSpec(mWidth,MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));


        leftGlobalStartX = (getPaddingLeft() + getPaddingRight()) / 2F;
        topGlobalStartY = (getPaddingTop() + getPaddingBottom()) / 2F;

        reCalcSize();
    }

    private void reCalcSize() {
        leaves = new int[0];
        firstDay = DateUtils.getFirstDayWeek(monthData.calendar.get(Calendar.YEAR)
                , monthData.calendar.get(Calendar.MONTH));
        dayCount = DateUtils.getMonthDays(monthData.calendar.get(Calendar.YEAR)
                , monthData.calendar.get(Calendar.MONTH));

        if(NUM_COLUMNS*6-(firstDay+dayCount) >= 6){
            NUM_ROWS = 5;
        } else {
            NUM_ROWS = 6;
        }

        mColumnSize = mWidth / NUM_COLUMNS;
        mRowSize = (mHeight-weekDayView.getSelfHeight()) / NUM_ROWS;

        float tempStartX;
        float tempStartY = topGlobalStartY + mRowSize / 2F+weekDayView.getSelfHeight();

        monthData.dayViewSparseArray.clear();

        // 填充满日期
        for (int i = 1; i <= NUM_ROWS * NUM_COLUMNS; i++) {
            DayView dayView = new DayView();

            int calcPos = i%7==0?7:i%7;

            tempStartX = leftGlobalStartX + calcPos * mColumnSize - mColumnSize / 2F;

            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            float bottomY = tempStartY + fontMetrics.descent-fontMetrics.top;

            dayView.initParams(tempStartX, tempStartY,bottomY);
            if (i >= firstDay && getDayNum(i) <= dayCount){
                dayView.text = getDayNum(i) + "";

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(monthData.calendar.getTimeInMillis());
                calendar.set(Calendar.DATE,getDayNum(i));
                dayView.time = calendar.getTimeInMillis();
                if(calcPos == 1 || calcPos == 7)
                    dayView.bottomText = "休";
                else
                    dayView.bottomText = "百";

            } else {
                dayView.text = "";
            }

            monthData.dayViewSparseArray.put(i, dayView);

            if (calcPos == 7)
                tempStartY += mRowSize;
        }

    }

    private int getDayNum(int pos) {
       return pos - firstDay + 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 新建图层
        canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), linePaint, Canvas.ALL_SAVE_FLAG);
        for (int i = 1; i <= monthData.dayViewSparseArray.size(); i++) {
            DayView dayView = monthData.dayViewSparseArray.get(i);
            if(null == dayView || dayView.text.equals("")) {
                continue;
            }
            if(isToday(i)) {
                dayPaint.setColor(lineColor);
                drawCircle(dayView.x, dayView.y, canvas);
            }
            for(int j:leaves) {
                if(getDayNum(i) == j) {
                    dayView.isLeave = true;
                    break;
                }
            }
            if (!dayView.isChecked) {
                mPaint.setColor(mDayColor);
                if(i%7 == 0 || i%7 == 1) {
                    dayView.onDayDraw(canvas, mPaint,mWeekendColor);
                } else {
                    dayView.onDayDraw(canvas, mPaint,mWeekendDefaultColor);
                }
            } else {
                dayPaint.setColor(mSelectBGColor);
                drawCircle(dayView.x, dayView.y, canvas);
                mPaint.setColor(mSelectDayColor);
                dayView.onDayDraw(canvas, mPaint,mSelectDayColor);
            }
        };
        drawLine(canvas);
        drawWeekHead(canvas);

        canvas.restore();
    }

    private boolean isToday(int pos) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DATE);
        return (monthData.calendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && monthData.calendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && getDayNum(pos) == day);
    }

    private void drawCircle(float x, float y, Canvas canvas) {
        canvas.drawCircle(x, y, mCircleRadius, dayPaint);
    }

    private void drawLeave(float x,float y,Canvas canvas) {
        canvas.drawCircle(x,y,leaveCircleRadius,leavePaint);
    }

    public void reDraw() {
        if (Build.VERSION.SDK_INT > 15) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void drawWeekHead(Canvas canvas) {
        canvas.translate(leftGlobalStartX,topGlobalStartY);
        weekDayView.draw(canvas);
        canvas.translate(-leftGlobalStartX,-topGlobalStartY);
    }

    private void drawLine(Canvas canvas) {
        int maxLineNum = (int) Math.floor((firstDay+dayCount)/NUM_COLUMNS);
        for (int i = 1;i <= maxLineNum;i++) {
            canvas.drawLine(leftGlobalStartX,i*mRowSize+weekDayView.getSelfHeight(),leftGlobalStartX+mWidth,i*mRowSize+weekDayView.getSelfHeight()
                    ,linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                tempClickPos = getClickPos((int) event.getX(),(int) event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                doClickAction((int) event.getX(),(int) event.getY());
                break;
        }
        return true;
    }


    /**
     * 设置时间
     */
    public void setTime(Calendar calendar) {
        monthData.calendar = calendar;
        reCalcSize();
        reDraw();
    }

    /**
     * 执行点击事件
     */
    private void doClickAction(int x, int y) {
        int dayViewNum = getClickPos(x,y);
        // 当点击不松开，手指滑动到其他的dayView之后，不做点击判断
        if(tempClickPos != dayViewNum)
            return;
        DayView dayView = monthData.dayViewSparseArray.get(dayViewNum);
        if( dayView != null && !TextUtils.isEmpty(dayView.text)) {
            dayView.isChecked = !monthData.dayViewSparseArray.get(dayViewNum).isChecked;
            reDraw();
            //执行activity发送过来的点击处理事件
            if (dateClick != null) {
                dateClick.onClickOnDate();
            }
        }
    }

    private int getClickPos (int x, int y) {
        int row = (int) ((y - topGlobalStartY) / mRowSize);
        int column = (int) ((x - leftGlobalStartX) / mColumnSize);
        return row * NUM_COLUMNS + column+1;
    }

    public Calendar getMonthCalendar() {
        return monthData.calendar;
    }

    /**
     * 设置请假天数
     *
     * @param leaves
     * <h1>
     *     exp:{1,2,3,4,5}
     *     就会显示 当月的1,2,3,4,5号请过假
     * <h1/>
     *
     *
     *
     */
    public void setLeaves(int... leaves) {
        this.leaves = leaves;
        reDraw();
    }

    /**
     * 普通日期的字体颜色，默认黑色
     *
     * @param mDayColor
     */
    public void setmDayColor(int mDayColor) {
        this.mDayColor = mDayColor;
    }

    /**
     * 选择日期的颜色，默认为白色
     *
     * @param mSelectDayColor
     */
    public void setmSelectDayColor(int mSelectDayColor) {
        this.mSelectDayColor = mSelectDayColor;
    }

    /**
     * 选中日期的背景颜色，默认蓝色
     *
     * @param mSelectBGColor
     */
    public void setmSelectBGColor(int mSelectBGColor) {
        this.mSelectBGColor = mSelectBGColor;
        dayPaint.setColor(mSelectBGColor);
        reDraw();
    }

    /**
     * 日期的大小，默认18sp
     *
     * @param mDaySize
     */
    public void setmDaySize(float mDaySize) {
        this.mDaySize = mDaySize;
        mPaint.setTextSize(mDaySize);
        reDraw();
    }

    /***
     * 设置圆圈的半径，默认为6
     * @param mCircleRadius
     */
    public void setmCircleRadius(int mCircleRadius) {
        this.mCircleRadius = mCircleRadius;
    }


    /**
     * 设置日期的点击回调事件
     *
     * @author shiwei.deng
     */
    public interface DateClick {
        void onClickOnDate();
    }

    /**
     * 设置日期点击事件
     *
     * @param dateClick
     */
    public void setDateClick(DateClick dateClick) {
        this.dateClick = dateClick;
    }

    public void setDayViews(SparseArray<DayView> dayViewSparseArray) {
        this.monthData.dayViewSparseArray = dayViewSparseArray;
        reCalcSize();
        reDraw();
    }

    public SparseArray<DayView> getDayViewS() {
        return monthData.dayViewSparseArray;
    }

    public MonthData getMonthData() {
        return monthData;
    }

    public void setMonthData(MonthData monthData) {
        this.monthData = monthData;
        reCalcSize();
        reDraw();
    }

    /**
     * 获得所有被选中天数的时间戳
     * @return
     */
    public SparseArray<Long> getCheckDayTime() {
        SparseArray<Long> longSparseArray = new SparseArray<>();
        int tempCount = 0;
        for(int i = firstDay;i <= firstDay+dayCount+1;i++) {
            DayView dayView = monthData.getDayViewSparseArray().get(i);
            if(null != dayView && dayView.isChecked) {
                longSparseArray.put(tempCount,dayView.time);
            }
        }
        return longSparseArray;
    }

    /**
     * 根据单位获取相对应的像素
     */
    private float getDimenSize(int unit,int value) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit, value, metrics);
    }

    /**
     * 日历每天日期
     */
    public class DayView {
        /**
         * 是否被选中，可以通过手动调节来设定选中状态
         **/
        boolean isChecked = false;
        /**
         * 是否请过假
         */
        boolean isLeave = false;

        /*********** 以下部分会自动匹配，不需要手动计算******************/


        /**
         * 即将被绘制的文字
         **/
        String text = "";
        String bottomText = "";
        long time;
        private float x, y,bottomY;

        public DayView() {
        }

        void initParams(float x, float y,float bottomY) {
            this.x = x;
            this.y = y;
            this.bottomY = bottomY;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        void onDayDraw(Canvas c, Paint paint,int bottomColor) {
            if(!TextUtils.isEmpty(text)) {
                c.drawText(text, x, y, paint);

                paint.setColor(bottomColor);
                c.drawText(bottomText,x,bottomY,paint);

                if(isLeave) {
                    leavePaint.setColor(isChecked?mSelectDayColor:mSelectBGColor);
                    drawLeave(x,y+mCircleRadius-leaveCircleRadius,c);
                }
            }
        }
    }

    /**
     * 当月数据封装
     */
    public class MonthData {
        public Calendar calendar;
        private SparseArray<MonthDateView.DayView>
                dayViewSparseArray = new SparseArray<>();

        public MonthData(Calendar calendar) {
            this.calendar = calendar;
        }

        public SparseArray<DayView> getDayViewSparseArray() {
            return dayViewSparseArray;
        }

        public void setDayViewSparseArray(SparseArray<DayView> dayViewSparseArray) {
            this.dayViewSparseArray = dayViewSparseArray;
        }
    }

}