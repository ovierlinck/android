package com.example.danyal.bluetoothhc05;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class ThermoModel extends ViewModel {

    public ThermoModel() {
        super();
        temperature.setValue(-1);
        setpoint.setValue(-1);
        mode.setValue("?");
        command.setValue(false);
    }

    public MutableLiveData<Integer> temperature = new MutableLiveData<>();
    public MutableLiveData<Integer> setpoint = new MutableLiveData<>();
    public MutableLiveData<String> mode = new MutableLiveData<>();
    public MutableLiveData<Boolean> command = new MutableLiveData<>();

    public MutableLiveData<String> dateText = new MutableLiveData<>();
    public MutableLiveData<String> timeText = new MutableLiveData<>();

    private int day = -1;
    private int month = -1;
    private int year = -1;
    private int hour = -1;
    private int minute = -1;
    private int second = -1;

    public void setDay(int day) {
        this.day = day;
        updateDateText();
    }

    public void setMonth(int month) {
        this.month = month;
        updateDateText();
    }

    public void setYear(int year) {
        this.year = year;
        updateDateText();
    }

    public void setHour(int hour) {
        this.hour = hour;
        updateTimeText();
    }

    public void setMinute(int minute) {
        this.minute = minute;
        updateTimeText();
    }

    public void setSecond(int second) {
        this.second = second;
        updateTimeText();
    }

    private String check(int v) {
        return v == -1 ? "?" : ("" + v);
    }

    private void updateDateText()
    {
        dateText.postValue(check(day) + "-" + check(month) + "-" + check(year));
    }
    private void updateTimeText()
    {
        timeText.postValue(check(hour) + ":" + check(minute) + ":" + check(second));
    }

}
