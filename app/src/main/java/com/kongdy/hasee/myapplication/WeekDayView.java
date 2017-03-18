package com.kongdy.hasee.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class WeekDayView extends View {

    private int bgColor = Color.parseColor("#e3e3e3");

	//周一到周五的颜色
	private int mWeedayColor = Color.parseColor("#333333");
	//周六、周日的颜色
	private int mWeekendColor = Color.parseColor("#f4906b");
	//线的宽度
	private int mWeekSize = 15;
	private Paint paint;
    private TextPaint textPaint;
	private String[] weekString = new String[]{"周日","周一","周二","周三","周四","周五","周六"};

    private int topLimit = 0;
    private int selfHeight;

	public WeekDayView(Context context) {
		this(context,null);
	}

	public WeekDayView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public WeekDayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		paint = new Paint();
        textPaint = new TextPaint();

        highEffect(paint);
        highEffect(textPaint);

        textPaint.setTextSize(getDimenSize(TypedValue.COMPLEX_UNIT_SP,mWeekSize));
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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

        int paddingTopAndBottom = (int) getDimenSize(TypedValue.COMPLEX_UNIT_DIP,2);

        topLimit = (int) (fontMetrics.descent-fontMetrics.top)/4+paddingTopAndBottom/2;
        selfHeight = (int) (fontMetrics.descent-fontMetrics.top)+paddingTopAndBottom;

        setMeasuredDimension(widthSize, selfHeight);
	}

	public int getSelfHeight(){
        return selfHeight;
    }

    @Override
	protected void onDraw(Canvas canvas) {
		canvas.saveLayer(0,0,getMeasuredWidth(),getMeasuredHeight(),paint,Canvas.ALL_SAVE_FLAG);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

        paint.setColor(bgColor);
        canvas.drawRect(0,0,width,height,paint);

		int columnWidth = width / 7;
		for(int i=0;i < weekString.length;i++){
			String text = weekString[i];
			int startX = columnWidth * i + columnWidth/2;
			int startY = (height/2+topLimit);
			if(text.indexOf("周日") > -1|| text.indexOf("周六") > -1){
                textPaint.setColor(mWeekendColor);
			} else {
                textPaint.setColor(mWeedayColor);
			}
			canvas.drawText(text, startX, startY, textPaint);
		}
		canvas.restore();
	}

    /**
     * 根据单位获取相对应的像素
     */
    private float getDimenSize(int unit,int value) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit, value, metrics);
    }


	/**
	 * 设置周一-五的颜色
	 * @return
	 */
	public void setmWeedayColor(int mWeedayColor) {
		this.mWeedayColor = mWeedayColor;
	}

	/**
	 * 设置周六、周日的颜色
	 * @param mWeekendColor
	 */
	public void setmWeekendColor(int mWeekendColor) {
		this.mWeekendColor = mWeekendColor;
	}



	/**
	 * 设置字体的大小
	 * @param mWeekSize
	 */
	public void setmWeekSize(int mWeekSize) {
		this.mWeekSize = mWeekSize;
	}


	/**
	 * 设置星期的形式
	 * @param weekString
	 * 默认值	"日","一","二","三","四","五","六"
	 */
	public void setWeekString(String[] weekString) {
		this.weekString = weekString;
	}
}