package com.google.developer.taskmaker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Useful class for TaskMaker App.
 * <p>
 * Created by falvojr on 18/09/17.
 */
public final class AppUtils {

    private AppUtils() {
        super();
    }

    public static long getTaskStandardTimeInMillis(int day, int month, int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
