/*
 * Created on Sep 16, 2004
 */
package edu.umd.cs.marmoset.modelClasses;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 * @author pugh
 *
 */
public class Formats {
	public static final long MILLIS_PER_HOUR = 60L*60L*1000L;
    public static final DateFormat date= new SimpleDateFormat("EEE, MMM d, h:mm a");
    public static final DateFormat shortDate= new SimpleDateFormat("EEE, h a");
	public static final NumberFormat twoDigitInt = new DecimalFormat("00");
    public static final long  MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
}
