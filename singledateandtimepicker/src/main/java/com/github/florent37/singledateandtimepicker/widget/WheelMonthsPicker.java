package com.github.florent37.singledateandtimepicker.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dhvlsimac on 20/02/18.
 */

public class WheelMonthsPicker extends WheelPicker {
    public static final int MONTHS_COUNT = 11;

    private int defaultMonth;

    private WheelPicker.Adapter adapter;

    private SimpleDateFormat simpleDateFormat;

    private int lastScrollPosition;

    private WheelMonthsPicker.OnMonthSelectedListener onMonthSelectedListener;

    public WheelMonthsPicker(Context context) {
        this(context, null);
    }

    public WheelMonthsPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAdapter();
    }

    protected void initAdapter(){
        this.simpleDateFormat = new SimpleDateFormat("MMMM", getCurrentLocale());

        final List<String> months = new ArrayList<>();


        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.MONTH, 0);
        months.add(getFormattedValue(instance.getTime()));
        for (int min = 0; min < MONTHS_COUNT; min += 1) {
            instance.add(Calendar.MONTH, 1);
            months.add(getFormattedValue(instance.getTime()));
        }
        adapter = new Adapter(months);
        setAdapter(adapter);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // todo fix.   calendar.setTime(defaultDate);
        defaultMonth = calendar.get(Calendar.MONTH);

        updateDefaultMonth();
        notifyDatasetChanged();
    }

    protected String getFormattedValue(Object value) {
        return simpleDateFormat.format(value);
    }

    private void updateDefaultMonth() {
        setSelectedItemPosition(defaultMonth);
    }

    public void setDefaultMonth(int month) {
        this.defaultMonth = month;
        updateDefaultMonth();
    }

    public void setOnMonthSelectedListener(WheelMonthsPicker.OnMonthSelectedListener onMonthSelectedListener) {
        this.onMonthSelectedListener = onMonthSelectedListener;
    }

    @Override
    protected void onItemSelected(int position, Object item) {
        if (onMonthSelectedListener != null) {
            onMonthSelectedListener.onMonthSelected(this, position, getCurrentItemPosition());
        }
    }

    @Override
    protected void onItemCurrentScroll(int position, Object item) {
    }

    @Override
    public int getDefaultItemPosition() {
        return defaultMonth;
    }

    private int convertItemToMonth(Object item) {
        return Integer.valueOf(String.valueOf(item));
    }

    public int getCurrentMonth() {
        Date date = null;
        int itemPosition = getCurrentItemPosition();
        if (itemPosition > adapter.getItemCount() - 1) {
            itemPosition -= adapter.getItemCount();
        }
        String itemText = adapter.getItemText(itemPosition);
        final Calendar todayCalendar = Calendar.getInstance();
        try {
            date = simpleDateFormat.parse(itemText);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            //try to know the year
            final Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            return dateCalendar.get(Calendar.MONTH);
        }
        else {
            final Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(new Date());
            return dateCalendar.get(Calendar.MONTH);
        }
    }

    public interface OnMonthSelectedListener {
        void onMonthSelected(WheelMonthsPicker picker, int position, int months);
    }
}