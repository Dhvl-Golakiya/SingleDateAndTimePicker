package com.github.florent37.singledateandtimepicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.florent37.singledateandtimepicker.widget.WheelAmPmPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelDayOfMonthPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelDayPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelHourPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelMinutePicker;
import com.github.florent37.singledateandtimepicker.widget.WheelMonthsPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelPicker;
import com.github.florent37.singledateandtimepicker.widget.WheelYearPicker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class SingleDateAndTimePicker extends LinearLayout {

    public static final boolean IS_CYCLIC_DEFAULT = true;
    public static final boolean IS_CURVED_DEFAULT = false;
    public static final boolean MUST_BE_ON_FUTUR_DEFAULT = false;
    public static final int DELAY_BEFORE_CHECK_PAST = 200;
    private static final int VISIBLE_ITEM_COUNT_DEFAULT = 7;
    private static final int PM_HOUR_ADDITION = 12;

    private static final CharSequence FORMAT_24_HOUR = "EEE d MMM H:mm";
    private static final CharSequence FORMAT_12_HOUR = "EEE d MMM h:mm a";

    private WheelYearPicker yearsPicker;
    private WheelMonthsPicker monthPicker;
    private WheelDayOfMonthPicker dayOfMonthPicker;
    private WheelDayPicker daysPicker;
    private WheelMinutePicker minutesPicker;
    private WheelHourPicker hoursPicker;
    private WheelAmPmPicker amPmPicker;

    private Listener listener;

    private String todayText;
    private int textColor;
    private int selectedTextColor;
    private int textSize;
    private int selectorColor;
    private boolean isCyclic;
    private boolean isCurved;
    private int visibleItemCount;
    private View dtSelector;
    private boolean mustBeOnFuture;

    @Nullable
    private Date minDate;
    @Nullable
    private Date maxDate;
    private Date defaultDate;

    private boolean displayYears = false;
    private boolean displayMonth = false;
    private boolean displayDays = true;
    private boolean displayMinutes = true;
    private boolean displayHours = true;
    private boolean displayDayOfMonths = false;

    private boolean isAmPm;
    private int selectorHeight;
    private int stepMinutes = WheelMinutePicker.STEP_MINUTES_DEFAULT;

    public SingleDateAndTimePicker(Context context) {
        this(context, null);
    }

    public SingleDateAndTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleDateAndTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        inflate(context, R.layout.single_day_picker, this);

        isAmPm = !(DateFormat.is24HourFormat(context));

        yearsPicker = (WheelYearPicker) findViewById(R.id.yearPicker);
        monthPicker = (WheelMonthsPicker) findViewById(R.id.monthPicker);
        dayOfMonthPicker = (WheelDayOfMonthPicker) findViewById(R.id.dayOfMonthPicker);
        daysPicker = (WheelDayPicker) findViewById(R.id.daysPicker);
        minutesPicker = (WheelMinutePicker) findViewById(R.id.minutesPicker);
        hoursPicker = (WheelHourPicker) findViewById(R.id.hoursPicker);
        amPmPicker = (WheelAmPmPicker) findViewById(R.id.amPmPicker);
        dtSelector = findViewById(R.id.dtSelector);

        final ViewGroup.LayoutParams dtSelectorLayoutParams = dtSelector.getLayoutParams();
        dtSelectorLayoutParams.height = selectorHeight;
        dtSelector.setLayoutParams(dtSelectorLayoutParams);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.pickerLayout);
        if (defStyleAttr == 0) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.picker_button_background));
            dtSelector.setBackgroundColor(getResources().getColor(R.color.picker_default_selected_text_color));
        } else {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.picker_black_background));
            dtSelector.setBackgroundColor(getResources().getColor(R.color.picker_black_selector_color));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        monthPicker.setOnMonthSelectedListener(new WheelMonthsPicker.OnMonthSelectedListener() {
            @Override
            public void onMonthSelected(WheelMonthsPicker picker, int position, int months) {
                updateDate();
                updateListener();
                checkMinMaxDate(picker);
            }
        });

        yearsPicker.setOnYearSelectedListener(new WheelYearPicker.OnYearSelectedListener() {
            @Override
            public void onYearSelected(WheelYearPicker picker, int position, int years) {
                updateDate();
                updateListener();
                checkMinMaxDate(picker);
            }
        });

        dayOfMonthPicker.setOnDayOfMonthSelectedListener(new WheelDayOfMonthPicker.OnDayOfMonthSelectedListener() {
            @Override
            public void onDayOfMonthSelected(WheelDayOfMonthPicker picker, int position, int dayOfMonth) {
                updateDate();
                updateListener();
                checkMinMaxDate(picker);
            }
        });

        daysPicker.setOnDaySelectedListener(new WheelDayPicker.OnDaySelectedListener() {
            @Override
            public void onDaySelected(WheelDayPicker picker, int position, String name, Date date) {
                updateListener();
                checkMinMaxDate(picker);
            }
        });

        minutesPicker.setListener(new WheelMinutePicker.Listener() {
            @Override
            public void onSelected(WheelMinutePicker picker, int position, String value) {
                updateListener();
                checkMinMaxDate(picker);
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
                checkMinMaxDate(picker);
            }

            @Override
            public void onCurrentScrolled(WheelHourPicker picker, int position, String value) {

            }

            @Override
            public void onFinishedLoop(WheelHourPicker picker) {
                daysPicker.scrollTo(daysPicker.getCurrentItemPosition() + 1);
            }
        });

        amPmPicker.setListener(new WheelAmPmPicker.Listener() {
            @Override
            public void onSelected(WheelAmPmPicker picker, int position, String value) {
                updateListener();
                checkMinMaxDate(picker);
            }

            @Override
            public void onCurrentScrolled(WheelAmPmPicker picker, int position, String value) {

            }

            @Override
            public void onFinishedLoop(WheelAmPmPicker picker) {

            }
        });

        updatePicker();
        updateViews();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        monthPicker.setEnabled(enabled);
        dayOfMonthPicker.setEnabled(enabled);
        yearsPicker.setEnabled(enabled);
        daysPicker.setEnabled(enabled);
        minutesPicker.setEnabled(enabled);
        hoursPicker.setEnabled(enabled);
        amPmPicker.setEnabled(enabled);
    }

    public void setDisplayDays(boolean displayDays) {
        this.displayDays = displayDays;
        updateViews();
        updatePicker();
    }

    public void setDisplayYears(boolean displayYears) {
        this.displayYears = displayYears;
        updateViews();
        updatePicker();
    }

    public void setDisplayDayOfMonths(boolean displayDayOfMonths) {
        this.displayDayOfMonths = displayDayOfMonths;
        updateViews();
        updatePicker();
    }

    public void setDisplayMonths(boolean displayMonths) {
        this.displayMonth = displayMonth;
        updateViews();
        updatePicker();
    }

    public void setDisplayMinutes(boolean displayMinutes) {
        this.displayMinutes = displayMinutes;
        updateViews();
        updatePicker();
    }

    public void setDisplayHours(boolean displayHours) {
        this.displayHours = displayHours;
        updateViews();
        updatePicker();
    }

    public void setTodayText(String todayText) {
        this.todayText = todayText;
        if (daysPicker != null && todayText != null && !todayText.isEmpty()) {
            daysPicker.setTodayText(todayText);
        }
    }

    public void setCurved(boolean curved) {
        isCurved = curved;
        updatePicker();
    }

    public void setCyclic(boolean cyclic) {
        isCyclic = cyclic;
        updatePicker();
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

    public void setIsAmPm(boolean isAmPm) {
        this.isAmPm = isAmPm;
        updateViews();
        updatePicker();
    }

    public void setDayFormatter(SimpleDateFormat simpleDateFormat) {
        if (simpleDateFormat != null) {
            this.daysPicker.setDayFormatter(simpleDateFormat);
        }
    }

    void updateDate() {
        int day = dayOfMonthPicker.getCurrentDayOfMonth();
        int month = monthPicker.getCurrentMonth();
        int year = yearsPicker.getCurrentYear();
        if (month < 7) {
            if (month == 1) {
                if (day > 28) {
                    if (year % 4 == 0) {
                        dayOfMonthPicker.scrollTo(28);
                    }
                    else {
                        dayOfMonthPicker.scrollTo(27);
                    }
                }
            } else if (month % 2 != 0) {
                if (day > 30) {
                    dayOfMonthPicker.scrollTo(29);
                }
            }
        }
        else {
            if (month % 2 == 0) {
                if (day > 30) {
                    dayOfMonthPicker.scrollTo(29);
                }
            }
        }
    }

    public void showOnlyDatePicker() {
        this.displayDayOfMonths = true;
        this.displayMonth = true;
        this.displayYears = true;
        this.displayDays = false;
        this.displayHours = false;
        this.displayMinutes = false;
        updateViews();
        updatePicker();
    }

    public void showDateAndTimePicker() {
        this.displayDayOfMonths = false;
        this.displayMonth = false;
        this.displayYears = false;
        this.displayDays = true;
        this.displayHours = true;
        this.displayMinutes = true;
        updateViews();
        updatePicker();
    }

    public void showTimePicker() {
        this.displayDayOfMonths = false;
        this.displayMonth = false;
        this.displayYears = false;
        this.displayDays = false;
        this.displayHours = true;
        this.displayMinutes = true;
        updateViews();
        updatePicker();
    }

    public boolean isAmPm() {
        return isAmPm;
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    private void updatePicker() {
        if (daysPicker != null && minutesPicker != null && hoursPicker != null && amPmPicker != null && yearsPicker != null && monthPicker != null && dayOfMonthPicker != null) {
            for (WheelPicker wheelPicker : Arrays.asList(daysPicker, minutesPicker, hoursPicker, amPmPicker, yearsPicker, monthPicker, dayOfMonthPicker)) {
                wheelPicker.setItemTextColor(textColor);
                wheelPicker.setSelectedItemTextColor(selectedTextColor);
                wheelPicker.setItemTextSize(textSize);
                wheelPicker.setVisibleItemCount(visibleItemCount);
                wheelPicker.setCurved(isCurved);
                if (wheelPicker != amPmPicker) {
                    if (wheelPicker == minutesPicker && stepMinutes > 10) {
                        wheelPicker.setCyclic(false);
                    } else {
                        wheelPicker.setCyclic(isCyclic);
                    }
                }
            }
        }

        setTodayText(todayText);
        if (amPmPicker != null) {
            amPmPicker.setVisibility((isAmPm && displayHours) ? VISIBLE : GONE);
        }
        if (hoursPicker != null) {
            hoursPicker.setIsAmPm(isAmPm);

            if ( defaultDate != null ) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(defaultDate);
                if(isAmPm){
                    hoursPicker.setDefault( String.valueOf(calendar.get(Calendar.HOUR)));
                }else{
                    hoursPicker.setDefault( String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
                }
            }
        }

        if (monthPicker != null) {
            monthPicker.setVisibility(displayMonth ? VISIBLE : GONE);
        }

        if (dayOfMonthPicker != null) {
            dayOfMonthPicker.setVisibility(displayDayOfMonths ? VISIBLE : GONE);
        }

        if (yearsPicker != null) {
            yearsPicker.setVisibility(displayYears ? VISIBLE : GONE);
        }

        if (hoursPicker != null) {
            hoursPicker.setVisibility(displayHours ? VISIBLE : GONE);
        }
        if (minutesPicker != null) {
            minutesPicker.setVisibility(displayMinutes ? VISIBLE : GONE);
        }
        if (daysPicker != null) {
            daysPicker.setVisibility(displayDays ? VISIBLE : GONE);
        }
    }

    private void updateViews() {
        dtSelector.setBackgroundColor(selectorColor);
    }

    private void checkMinMaxDate(final WheelPicker picker) {
        checkBeforeMinDate(picker);
        checkAfterMaxDate(picker);
    }

    private void checkBeforeMinDate(final WheelPicker picker) {
        picker.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (minDate != null && isBeforeMinDate(getDate())) {
                    //scroll to Min position
                    if (displayDayOfMonths) {
                        yearsPicker.scrollTo(yearsPicker.findIndexOfDate(minDate));
                        monthPicker.scrollTo(monthPicker.findIndexOfDate(minDate));
                        dayOfMonthPicker.scrollTo(dayOfMonthPicker.findIndexOfDate(minDate));
                    } else {
                        amPmPicker.scrollTo(amPmPicker.findIndexOfDate(minDate));
                        daysPicker.scrollTo(daysPicker.findIndexOfDate(minDate));
                        minutesPicker.scrollTo(minutesPicker.findIndexOfDate(minDate));
                        hoursPicker.scrollTo(hoursPicker.findIndexOfDate(minDate));
                    }
                }
            }
        }, DELAY_BEFORE_CHECK_PAST);
    }

    private void checkAfterMaxDate(final WheelPicker picker) {
        picker.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (maxDate != null && isAfterMaxDate(getDate())) {
                    //scroll to Max position
                    if (displayDayOfMonths) {
                        yearsPicker.scrollTo(yearsPicker.findIndexOfDate(maxDate));
                        monthPicker.scrollTo(monthPicker.findIndexOfDate(maxDate));
                        dayOfMonthPicker.scrollTo(dayOfMonthPicker.findIndexOfDate(maxDate));
                    } else {
                        amPmPicker.scrollTo(amPmPicker.findIndexOfDate(maxDate));
                        daysPicker.scrollTo(daysPicker.findIndexOfDate(maxDate));
                        minutesPicker.scrollTo(minutesPicker.findIndexOfDate(maxDate));
                        hoursPicker.scrollTo(hoursPicker.findIndexOfDate(maxDate));
                    }
                }
            }
        }, DELAY_BEFORE_CHECK_PAST);
    }

    private boolean isBeforeMinDate(Date date) {
        final Calendar minDateCalendar = Calendar.getInstance();
        minDateCalendar.setTime(minDate);
        minDateCalendar.set(Calendar.MILLISECOND, 0);
        minDateCalendar.set(Calendar.SECOND, 0);

        final Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        dateCalendar.set(Calendar.MILLISECOND, 0);
        dateCalendar.set(Calendar.SECOND, 0);

        return dateCalendar.before(minDateCalendar);
    }

    private boolean isAfterMaxDate(Date date) {
        final Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.setTime(maxDate);
        maxDateCalendar.set(Calendar.MILLISECOND, 0);
        maxDateCalendar.set(Calendar.SECOND, 0);

        final Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        dateCalendar.set(Calendar.MILLISECOND, 0);
        dateCalendar.set(Calendar.SECOND, 0);

        return dateCalendar.after(maxDateCalendar);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Date getDate() {
        if (displayDayOfMonths) {
            int dayOfMonth = dayOfMonthPicker.getCurrentDayOfMonth();
            int year = yearsPicker.getCurrentYear();
            int month = monthPicker.getCurrentMonth();

            final Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            final Date time = calendar.getTime();
            return time;
        }
        else {
            int hour = hoursPicker.getCurrentHour();
            if (isAmPm && amPmPicker.isPm()) {
                hour += PM_HOUR_ADDITION;
            }
            final int minute = minutesPicker.getCurrentMinute();

            final Calendar calendar = Calendar.getInstance();
            final Date dayDate = daysPicker.getCurrentDate();
            calendar.setTime(dayDate);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            final Date time = calendar.getTime();
            return time;
        }
    }

    public void setStepMinutes(int minutesStep) {
        minutesPicker.setStepMinutes(minutesStep);
        this.stepMinutes = minutesStep;
        if (minutesStep > 10) {
            minutesPicker.setCyclic(false);
        }
    }

    public void setHoursStep(int hoursStep) {
        hoursPicker.setHoursStep(hoursStep);
    }

    public void setDefaultDate( Date date ) {
        this.defaultDate = date;
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (displayDayOfMonths) {
                    yearsPicker.scrollTo(yearsPicker.findIndexOfDate(defaultDate));
                    monthPicker.scrollTo(monthPicker.findIndexOfDate(defaultDate));
                    dayOfMonthPicker.scrollTo(dayOfMonthPicker.findIndexOfDate(defaultDate));
                } else {
                    amPmPicker.scrollTo(amPmPicker.findIndexOfDate(defaultDate));
                    daysPicker.scrollTo(daysPicker.findIndexOfDate(defaultDate));
                    minutesPicker.scrollTo(minutesPicker.findIndexOfDate(defaultDate));
                    hoursPicker.scrollTo(hoursPicker.findIndexOfDate(defaultDate));
                }
            }
        }, DELAY_BEFORE_CHECK_PAST);
    }

    public void selectDate(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        Date date = calendar.getTime();
        daysPicker.setSelectedItemPosition(daysPicker.findIndexOfDate(date));
        amPmPicker.setSelectedItemPosition(amPmPicker.findIndexOfDate(date));
        hoursPicker.setSelectedItemPosition(hoursPicker.findIndexOfDate(date));
        minutesPicker.setSelectedItemPosition(minutesPicker.findIndexOfDate(date));
        monthPicker.setSelectedItemPosition(monthPicker.findIndexOfDate(date));
        dayOfMonthPicker.setSelectedItemPosition(dayOfMonthPicker.findIndexOfDate(date));
        yearsPicker.setSelectedItemPosition(yearsPicker.findIndexOfDate(date));
    }

    private void updateListener() {
        final Date date = getDate();
        CharSequence format = isAmPm ? FORMAT_12_HOUR : FORMAT_24_HOUR;
        String displayed = DateFormat.format(format, date).toString();
        if (listener != null) {
            listener.onDateChanged(displayed, date);
        }
    }

    public void setMustBeOnFuture(boolean mustBeOnFuture) {
        this.mustBeOnFuture = mustBeOnFuture;
        if (mustBeOnFuture) {
            minDate = Calendar.getInstance().getTime(); //minDate is Today
        }
    }

    public boolean mustBeOnFuture() {
        return mustBeOnFuture;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SingleDateAndTimePicker);

        final Resources resources = getResources();
        todayText = a.getString(R.styleable.SingleDateAndTimePicker_picker_todayText);
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
        mustBeOnFuture = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_mustBeOnFuture, MUST_BE_ON_FUTUR_DEFAULT);
        visibleItemCount = a.getInt(R.styleable.SingleDateAndTimePicker_picker_visibleItemCount, VISIBLE_ITEM_COUNT_DEFAULT);

        displayMonth = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayMonth, displayMonth);
        displayYears = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayYears, displayYears);
        displayDays = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayDays, displayDays);
        displayMinutes = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayMinutes, displayMinutes);
        displayHours = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayHours, displayHours);
        displayDayOfMonths = a.getBoolean(R.styleable.SingleDateAndTimePicker_picker_displayDayOfMonth, displayDayOfMonths);

        a.recycle();
    }

    public interface Listener {
        void onDateChanged(String displayed, Date date);
    }
}
