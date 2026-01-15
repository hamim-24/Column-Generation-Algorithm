package util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static LocalTime parseTime(String timeStr) {

        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    public static long minutesBetween(LocalTime start, LocalTime end) {

        return ChronoUnit.MINUTES.between(start, end);
    }

   
    // calculate duration in minutes from hour
    public static int hoursToMinutes(double hours) {
        
        return (int) Math.round(hours * 60);
    }
}
