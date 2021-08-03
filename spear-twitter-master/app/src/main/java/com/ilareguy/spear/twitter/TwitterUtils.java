package com.ilareguy.spear.twitter;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import static com.ilareguy.spear.twitter.Config.TWITTER_TIMESTAMP_FORMAT;

public abstract class TwitterUtils{

    public static DateTime twitterTimestampToDate(final String created_at){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(TWITTER_TIMESTAMP_FORMAT);
        return formatter.parseDateTime(created_at);
    }

    public static String dateToShortString(final DateTime past_date){
        final Period diff = new Period(past_date, DateTime.now());


        return (diff.getDays() < 1 && diff.getMonths() == 0 && diff.getYears() == 0)
                ? _elapsedTimeToString(diff)
                : _dateToShortString(past_date);
    }

    public static String fullDateToString(final DateTime date){
        return DateTimeFormat.forPattern("EE, d MMMM yyyy • hh:mm a").print(date);
    }

    private static String _elapsedTimeToString(final Period period){
        final PeriodFormatterBuilder formatter = new PeriodFormatterBuilder();

        if(period.getHours() > 1) // Over an hour old; display hours
            formatter.appendHours().appendSuffix("h");
        else if(period.getMinutes() > 1) // Over a minute old; display minutes
            formatter.appendMinutes().appendSuffix("m");
        else // Display seconds
            formatter.appendSeconds().appendSuffix("s");

        return formatter.toFormatter().print(period);
    }

    private static String _dateToShortString(final DateTime date){
        final DateTime current_date = DateTime.now();
        return DateTimeFormat.forPattern(
                (current_date.getYear() != date.getYear())
                        ? "d MMM yyyy"
                        : "d MMM"
        ).print(date);
    }

}
