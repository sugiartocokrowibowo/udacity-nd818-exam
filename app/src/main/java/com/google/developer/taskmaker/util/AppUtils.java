package com.google.developer.taskmaker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Useful class for TaskMaker App.
 *
 * Created by falvojr on 18/09/17.
 */
public final class AppUtils {

    private AppUtils() { super(); }

    private static final Locale LOCALE = Locale.getDefault();

    public static final Date DATE_NOW = Calendar.getInstance(LOCALE).getTime();

}
