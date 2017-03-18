package com.kongdy.hasee.myapplication;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kongdy
 *         日历控件
 **/
public class MyCalendarPager extends ViewPager {

    private List<MonthDateView> monthDateViews;

    private long nowTime;

    public static final int MIDDLE_OFFSET_POS = 1073741824;

    private NoEndLessAdapter noEndLessAdapter;

    public MyCalendarPager(Context context) {
        this(context, null);
    }

    public MyCalendarPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPager(attrs);
    }


    private void initPager(AttributeSet attr) {

        nowTime = Calendar.getInstance().getTimeInMillis();

        final MonthDateView monthDateView1 = new MonthDateView(getContext(), attr);
        final MonthDateView monthDateView2 = new MonthDateView(getContext(), attr);
        final MonthDateView monthDateView3 = new MonthDateView(getContext(), attr);

        initParam(monthDateView1);
        initParam(monthDateView2);
        initParam(monthDateView3);

        monthDateViews = new ArrayList<MonthDateView>() {{
            add(0, monthDateView1);
            add(1, monthDateView2);
            add(2, monthDateView3);
        }};

        noEndLessAdapter = new NoEndLessAdapter();

        super.setAdapter(noEndLessAdapter);

        setCurrentItem(MIDDLE_OFFSET_POS);
    }

    /**
     * 设置参数
     */
    private void initParam(MonthDateView monthDateView) {
        monthDateView.setmCircleRadius((int) getResources().getDimension(R.dimen.calendar_day_circle_radius));
        monthDateView.setmDaySize(getResources().getDimension(R.dimen.calendar_day_text_size));
    }

    public MonthDateView getMonthDateView(int pos) {
        return monthDateViews.get(noEndLessAdapter.getCurrentTruthPos(pos));
    }

    public MonthDateView getCurrentDateView() {
        return monthDateViews.get(noEndLessAdapter.getCurrentTruthPos());
    }

    // reload
    @Override
    public void setAdapter(PagerAdapter adapter) {
    }

    class NoEndLessAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        public int getCurrentTruthPos() {
            return getCurrentItem() % monthDateViews.size();
        }

        public int getCurrentTruthPos(int pos) {
            return pos % monthDateViews.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            int offsetPos = position - MIDDLE_OFFSET_POS;

            position = getCurrentTruthPos(position);

            if (position < 0) {
                position = monthDateViews.size() + position;
            }

            MonthDateView monthDateView = monthDateViews.get(position);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(nowTime);
            calendar.add(Calendar.MONTH, offsetPos);

            monthDateView.setTime(calendar);

            ViewParent vp = monthDateView.getParent();

            if (vp != null) {
                ViewGroup vg = (ViewGroup) vp;
                vg.removeView(monthDateView);
            }

            container.addView(monthDateView);

            return monthDateView;
        }
    }
}
