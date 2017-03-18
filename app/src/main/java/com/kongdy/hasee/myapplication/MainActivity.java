package com.kongdy.hasee.myapplication;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MyCalendarPager mcp_calendar;
    private TextView tv_current_date;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mcp_calendar = getView(R.id.mcp_calendar);
        tv_current_date = getView(R.id.tv_current_date);

        // 初始化头部时间
        setCalendarHeadTime(MyCalendarPager.MIDDLE_OFFSET_POS);

        mcp_calendar.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setCalendarHeadTime(position);

                /**
                 * 设置2017年2月的1,2,3,4,5号请过假
                 */
                MonthDateView monthDateView = mcp_calendar.getMonthDateView(position);
                Calendar calendar = monthDateView.getMonthCalendar();
                if(calendar.get(Calendar.YEAR) == 2017
                        && calendar.get(Calendar.MONTH) == 1) {
                    monthDateView.setLeaves(1,2,3,4,5);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        /**
         * 通过此方法可以获得当前显示月份所有被选中日期的时间戳
         */
        SparseArray<Long> checkedTime = mcp_calendar.getCurrentDateView().getCheckDayTime();
    }

    private void setCalendarHeadTime(int pos) {
        MonthDateView monthDateView = mcp_calendar.getMonthDateView(pos);
        tv_current_date.setText(simpleDateFormat.format(monthDateView.getMonthCalendar().getTimeInMillis()));
    }

    public void onCalendarHeadClick(View view) {
        switch (view.getId()) {
            case R.id.tv_forward_month:
                mcp_calendar.setCurrentItem(mcp_calendar.getCurrentItem()-1);
                break;
            case R.id.tv_next_month:
                mcp_calendar.setCurrentItem(mcp_calendar.getCurrentItem()+1);
                break;
        }
    }

    public <T extends View> T getView(int resId) {
        return (T)findViewById(resId);
    }
}
