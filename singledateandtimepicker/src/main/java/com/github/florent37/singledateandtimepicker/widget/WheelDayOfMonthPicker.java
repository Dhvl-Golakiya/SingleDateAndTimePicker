package com.github.florent37.singledateandtimepicker.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dhvlsimac on 20/02/18.
 */

public class WheelDayOfMonthPicker extends WheelPicker {
    public static final int DAYS_COUNT = 31;

    private int defaultDayOfMonth;

    private WheelPicker.Adapter adapter;

    private int lastScrollPosition;

    private WheelDayOfMonthPicker.OnDayOfMonthSelectedListener onDayOfMonthSelectedListener;

    public WheelDayOfMonthPicker(Context context) {
        this(context, null);
    }

    public WheelDayOfMonthPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAdapter();
    }

    protected void initAdapter(){

        final List<String> days = new ArrayList<>();


        Calendar instance = Calendar.getInstance();
        for (int min = 1; min <= DAYS_COUNT; min += 1) {
            days.add(getFormattedValue(min));
        }
        adapter = new Adapter(days);
        setAdapter(adapter);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // todo fix.   calendar.setTime(defaultDate);
        defaultDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        updateDefaultDayOfMonth();
    }

    private int findIndexOfDayOfMonth(int minute) {
        final int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; ++i) {
            final String object = adapter.getItemText(i);
            final Integer value = Integer.valueOf(object);
            if (minute < value) {
                return i - 1;
            }
        }
        return 0;
    }

    private void updateDefaultDayOfMonth() {
        setSelectedItemPosition(findIndexOfDayOfMonth(defaultDayOfMonth));
    }

    protected String getFormattedValue(Object value) {
        Object valueItem = value;
        if (value instanceof Date) {
            Calendar instance = Calendar.getInstance();
            instance.setTime((Date) value);
            valueItem = instance.get(Calendar.DAY_OF_MONTH);
        }
        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    public void setDefaultDayOfMonth(int dayOfMonth) {
        this.defaultDayOfMonth = dayOfMonth;
        updateDefaultDayOfMonth();
    }

    public void setOnDayOfMonthSelectedListener(WheelDayOfMonthPicker.OnDayOfMonthSelectedListener onDayOfMonthSelectedListener) {
        this.onDayOfMonthSelectedListener = onDayOfMonthSelectedListener;
    }

    @Override
    protected void onItemSelected(int position, Object item) {
        if (onDayOfMonthSelectedListener != null) {
            onDayOfMonthSelectedListener.onDayOfMonthSelected(this, position, getCurrentItemPosition());
        }
    }

    @Override
    protected void onItemCurrentScroll(int position, Object item) {
    }

    @Override
    public int getDefaultItemPosition() {
        return findIndexOfDayOfMonth(defaultDayOfMonth);
    }

    private int convertItemToDayOfMonth(Object item) {
        return Integer.valueOf(String.valueOf(item));
    }

    public int getCurrentDayOfMonth() {
        return convertItemToDayOfMonth(adapter.getItem(getCurrentItemPosition()));
    }

    public interface OnDayOfMonthSelectedListener {
        void onDayOfMonthSelected(WheelDayOfMonthPicker picker, int position, int dayOfMonth);
    }

}

