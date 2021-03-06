package com.github.florent37.singledateandtimepicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.florent37.singledateandtimepicker.widget.WheelDayRangePicker;
import com.github.florent37.singledateandtimepicker.widget.WheelHourPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelMinutePicker;
import com.github.florent37.singledateandtimepicker.widget.WheelPicker;

import java.util.Arrays;

/**
 * Created by dhvlsimac on 22/03/18.
 */

public class TimeRangePicker extends LinearLayout {


    public static final boolean IS_CYCLIC_DEFAULT = true;
    public static final boolean IS_CURVED_DEFAULT = false;
    public static final boolean MUST_BE_ON_FUTUR_DEFAULT = false;
    private static final int VISIBLE_ITEM_COUNT_DEFAULT = 7;


    private WheelMinutePicker minutesPicker;
    private WheelHourPicker hoursPicker;
    private WheelDayRangePicker dayRangePicker;


    private TimeRangePicker.Listener listener;

    private int textColor;
    private int selectedTextColor;
    private int textSize;
    private int selectorColor;
    private boolean isCurved;
    private boolean isCyclic;
    private int visibleItemCount;
    private View dtSelector;

    private int selectorHeight;
    private int stepMinutes = WheelMinutePicker.STEP_MINUTES_DEFAULT;


    public TimeRangePicker(Context context) {
        this(context, null);
    }

    public TimeRangePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeRangePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        inflate(context, R.layout.time_range_picker, this);

        minutesPicker = (WheelMinutePicker) findViewById(R.id.minutesRangePicker);
        hoursPicker = (WheelHourPicker) findViewById(R.id.hoursRangePicker);
        dayRangePicker = (WheelDayRangePicker) findViewById(R.id.dayRangePicker);
        dtSelector = findViewById(R.id.dtRangeSelector);

        final ViewGroup.LayoutParams dtSelectorLayoutParams = dtSelector.getLayoutParams();
        dtSelectorLayoutParams.height = selectorHeight;
        dtSelector.setLayoutParams(dtSelectorLayoutParams);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.pickerLayout);
        TextView daysTextView = (TextView) findViewById(R.id.daysTextView);
        TextView hoursTextView = (TextView) findViewById(R.id.hoursTextView);
        TextView minutesTextView = (TextView) findViewById(R.id.minutesTextView);

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "sf_pro_text_medium.otf");
        daysTextView.setTypeface(typeface);
        hoursTextView.setTypeface(typeface);
        minutesTextView.setTypeface(typeface);

        if (defStyleAttr == 0) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.picker_button_background));
            dtSelector.setBackgroundColor(getResources().getColor(R.color.picker_default_selected_text_color));
            daysTextView.setTextColor(getResources().getColor(R.color.picker_default_text_color));
            hoursTextView.setTextColor(getResources().getColor(R.color.picker_default_text_color));
            minutesTextView.setTextColor(getResources().getColor(R.color.picker_default_text_color));
        } else {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.picker_black_background));
            dtSelector.setBackgroundColor(getResources().getColor(R.color.picker_black_selector_color));
            daysTextView.setTextColor(getResources().getColor(R.color.picker_black_text_color));
            hoursTextView.setTextColor(getResources().getColor(R.color.picker_black_text_color));
            minutesTextView.setTextColor(getResources().getColor(R.color.picker_black_text_color));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        dayRangePicker.setListener(new WheelDayRangePicker.Listener() {
            @Override
            public void onSelected(WheelDayRangePicker picker, int position, String value) {
                updateListener();
            }

            @Override
            public void onCurrentScrolled(WheelDayRangePicker picker, int position, String value) {

            }

            @Override
            public void onFinishedLoop(WheelDayRangePicker picker) {

            }
        });

        minutesPicker.setListener(new WheelMinutePicker.Listener() {
            @Override
            public void onSelected(WheelMinutePicker picker, int position, String value) {
                updateListener();
            }

            @Override
            public void onCurrentScrolled(WheelMinutePicker picker, int position, String value) {

            }

            @Override
            public void onFinishedLoop(WheelMinutePicker picker) {
                if (stepMinutes > 10) {
                    return;
                }
                hoursPicker.scrollTo(hoursPicker.getCurrentItemPosition() + 1);
            }
        });

        hoursPicker.setListener(new WheelHourPicker.Listener() {
            @Override
            public void onSelected(WheelHourPicker picker, int position, String value) {
                updateListener();
            }

            @Override
            public void onCurrentScrolled(WheelHourPicker picker, int position, String value) {

            }

            @Override
            public void onFinishedLoop(WheelHourPicker picker) {
            }
        });

        updatePicker();
        updateViews();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        minutesPicker.setEnabled(enabled);
        hoursPicker.setEnabled(enabled);
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        updatePicker();
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        updatePicker();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        updatePicker();
    }

    public void setSelectorColor(int selectorColor) {
        this.selectorColor = selectorColor;
        updateViews();
    }

    public void setVisibleItemCount(int visibleItemCount) {
        this.visibleItemCount = visibleItemCount;
        updatePicker();
    }

    public void resetTime() {
        hoursPicker.resetHour();
        minutesPicker.resetMinute();
    }

    private void updatePicker() {
        if ( minutesPicker != null && hoursPicker != null) {
            for (WheelPicker wheelPicker : Arrays.asList(minutesPicker, hoursPicker, dayRangePicker)) {
                wheelPicker.setItemTextColor(textColor);
                wheelPicker.setSelectedItemTextColor(selectedTextColor);
                wheelPicker.setItemTextSize(textSize);
                wheelPicker.setVisibleItemCount(visibleItemCount);
                wheelPicker.setCurved(isCurved);
            }
        }

        if (hoursPicker != null) {
            hoursPicker.setIsAmPm(false);
            hoursPicker.setIsForRangePicker(true);
            hoursPicker.setHoursStep(1);
        }

        if (minutesPicker != null) {
            minutesPicker.setIsForRangePicker(true);
            minutesPicker.setStepMinutes(stepMinutes);
        }

    }

    public void setStepMinutes(int minutesStep) {
        minutesPicker.setStepMinutes(minutesStep);
        this.stepMinutes = minutesStep;
        if (minutesStep > 10) {
            minutesPicker.setCyclic(false);
        }
    }


    private void updateViews() {
        dtSelector.setBackgroundColor(selectorColor);
    }

    public void setListener(TimeRangePicker.Listener listener) {
        this.listener = listener;
    }

    private void updateListener() {
        if (listener != null) {
            listener.onTimeChanged(String.valueOf(dayRangePicker.getCurrentDay()), String.valueOf(hoursPicker.getCurrentHour()), String.valueOf(minutesPicker.getCurrentMinute()));
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SingleDateAndTimePicker);

        final Resources resources = getResources();
        if (defStyleAttr == 0) {
            textColor = a.getColor(R.styleable.SingleDateAndTimePicker_picker_textColor,
                    ContextCompat.getColor(context, R.color.picker_default_text_color));
            selectedTextColor = a.getColor(R.styleable.SingleDateAndTimePicker_picker_selectedTextColor,
                    ContextCompat.getColor(context, R.color.picker_default_selected_text_color));
        } else {
            textColor = a.getColor(R.styleable.SingleDateAndTimePicker_picker_textColor,
                    ContextCompat.getColor(context, R.color.picker_black_text_color));
            selectedTextColor = a.getColor(R.styleable.SingleDateAndTimePicker_picker_selectedTextColor,
                    ContextCompat.getColor(context, R.color.picker_black_selected_text_color));
        }
        selectorColor = a.getColor(R.styleable.SingleDateAndTimePicker_picker_selectorColor,
                ContextCompat.getColor(context, R.color.picker_default_selector_color));
        selectorHeight = a.getDimensionPixelSize(R.styleable.SingleDateAndTimePicker_picker_selectorHeight, resources.getDimensionPixelSize(R.dimen.wheelSelectorHeight));
        textSize = a.getDimensionPixelSize(R.styleable.SingleDateAndTimePicker_picker_textSize,
                resources.getDimensionPixelSize(R.dimen.WheelItemTextSize));
        isCurved = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_curved, IS_CURVED_DEFAULT);
        isCyclic = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_cyclic, IS_CYCLIC_DEFAULT);
        visibleItemCount = a.getInt(R.styleable.SingleDateAndTimePicker_picker_visibleItemCount, VISIBLE_ITEM_COUNT_DEFAULT);
        a.recycle();
    }

    public interface Listener {
        void onTimeChanged(String days, String hours, String minutes);
    }
}

