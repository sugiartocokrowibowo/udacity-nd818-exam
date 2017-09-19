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
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }
}
