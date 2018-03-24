package com.github.florent37.singledateandtimepicker.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dhvlsimac on 24/03/18.
 */

public class WheelDayRangePicker extends WheelPicker<String> {
    public static final int MIN_DAYS = 0;
    public static final int MAX_DAYS = 6;


    public WheelDayRangePicker(Context context) {
        super(context);
    }

    public WheelDayRangePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initAdapter(){
        final List<String> days = new ArrayList<>();
        for (int min = MIN_DAYS; min <= MAX_DAYS; min += 1) {
            days.add(getFormattedValue(min));
        }
        setAdapter(new Adapter<String>(days));
        setDefault("0");
    }

    private int findIndexOfMinute(int minute) {
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
            valueItem = instance.get(Calendar.MINUTE);
        }
        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    @Override
    public void setDefault(String defaultValue) {
        super.setDefault(getFormattedValue(0));
    }

    private int convertItemToDays(Object item) {
        return Integer.valueOf(String.valueOf(item));
    }

    public int getCurrentDay() {
        return convertItemToDays(adapter.getItem(getCurrentItemPosition()));
    }

    public interface Listener extends WheelPicker.Listener<WheelDayRangePicker, String>{

    }
}
