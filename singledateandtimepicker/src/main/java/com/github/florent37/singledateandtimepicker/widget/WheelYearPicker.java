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

public class WheelYearPicker extends WheelPicker {
    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2020;

    private int defaultYear;

    private WheelPicker.Adapter adapter;

    private int lastScrollPosition;

    private WheelYearPicker.OnYearSelectedListener onYearSelectedListener;


    public WheelYearPicker(Context context) {
        this(context, null);
    }

    public WheelYearPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAdapter();
    }

    protected void initAdapter(){
        final List<String> years = new ArrayList<>();
        for (int min = MIN_YEAR; min <= MAX_YEAR; min += 1) {
            years.add(getFormattedValue(min));
        }
        adapter = new Adapter(years);
        setAdapter(adapter);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // todo fix.   calendar.setTime(defaultDate);
        defaultYear = calendar.get(Calendar.YEAR);

        updateDefaultYear();
    }

    private int findIndexOfYear(int minute) {
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

    protected String getFormattedValue(Object value) {
        Object valueItem = value;
        if (value instanceof Date) {
            Calendar instance = Calendar.getInstance();
            instance.setTime((Date) value);
            valueItem = instance.get(Calendar.YEAR);
        }
        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    private void updateDefaultYear() {
        setSelectedItemPosition(findIndexOfYear(defaultYear));
    }

    public void setDefaultYear(int minutes) {
        this.defaultYear = minutes;
        updateDefaultYear();
    }

    public void setOnYearSelectedListener(WheelYearPicker.OnYearSelectedListener onYearSelectedListener) {
        this.onYearSelectedListener = onYearSelectedListener;
    }

    @Override
    protected void onItemSelected(int position, Object item) {
        if (onYearSelectedListener != null) {
            onYearSelectedListener.onYearSelected(this, position, convertItemToYear(item));
        }
    }

    @Override
    protected void onItemCurrentScroll(int position, Object item) {
    }


    @Override
    public int getDefaultItemPosition() {
        return findIndexOfYear(defaultYear);
    }

    private int convertItemToYear(Object item) {
        return Integer.valueOf(String.valueOf(item));
    }

    public int getCurrentYear() {
        return convertItemToYear(adapter.getItem(getCurrentItemPosition()));
    }

    public interface OnYearSelectedListener {
        void onYearSelected(WheelYearPicker picker, int position, int years);
    }
}

