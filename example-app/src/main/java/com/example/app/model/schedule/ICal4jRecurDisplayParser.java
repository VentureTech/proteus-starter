/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package com.example.app.model.schedule;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a way of parsing an ICal4j recurrence rule into human-readable text
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 3/14/16 2:22 PM
 */
public final class ICal4jRecurDisplayParser
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ICal4jRecurDisplayParser.class);

    //Private Constructor
    private ICal4jRecurDisplayParser(){}

    /**
     *   Parse the given recurrence rule into human-readable text
     *   @param recurrenceRule the recurrence rule
     *   @return the human-readable recurrence rule
     */
    public static String parse(String recurrenceRule)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Every ");
            Recur recur = new Recur(recurrenceRule);
            parseFrequency(recur, sb);

            return sb.toString().trim();
        }
        catch(ParseException e)
        {
            _logger.error("An error occurred while parsing recurrence rule: ", e);
            return recurrenceRule;
        }
    }

    private static void parseFrequency(Recur recur, StringBuilder sb) throws ParseException
    {
        switch (recur.getFrequency())
        {
            case Recur.DAILY:
                if (recur.getInterval() <= 1)
                {
                    sb.append("day ");
                }
                else
                {
                    sb.append(recur.getInterval()).append(" days ");
                }
                break;
            case Recur.WEEKLY:
                if (recur.getInterval() <= 1)
                {
                    sb.append("week ");
                }
                else
                {
                    sb.append(recur.getInterval()).append(" weeks ");
                }
                parseWeekly(recur, sb);
                break;
            case Recur.MONTHLY:
                if (recur.getInterval() <= 1)
                {
                    sb.append("month ");
                }
                else
                {
                    sb.append(recur.getInterval()).append(" months ");
                }
                parseMonthly(recur, sb);
                break;
            case Recur.YEARLY:
                if (recur.getInterval() <= 1)
                {
                    sb.append("year ");
                }
                else
                {
                    sb.append(recur.getInterval()).append(" years ");
                }
                parseYearly(recur, sb);
                break;
            default:
                throw new ParseException("Unable to parse Recurrence Rule Frequency: " + recur.getFrequency(), 0);
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseWeekly(Recur recur, StringBuilder sb)
    {
        if(!recur.getDayList().isEmpty())
        {
            sb.append("on ");
            final int totalCountDays = recur.getDayList().size();
            final AtomicReference<Integer> counter = new AtomicReference<>(0);
            recur.getDayList().forEach(day -> {
                WeekDay wd = (WeekDay)day;
                try
                {
                    sb.append(parseWeekDay(wd));
                    counter.set(counter.get() + 1);
                    addSeparator(totalCountDays, counter.get(), sb);
                }
                catch (ParseException e)
                {
                    _logger.error(e);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseMonthly(Recur recur, StringBuilder sb)
    {
        if(!recur.getMonthDayList().isEmpty())
        {
            sb.append("on the ");
            final int totalCountDays = recur.getMonthDayList().size();
            final AtomicReference<Integer> counter = new AtomicReference<>(0);
            recur.getMonthDayList().forEach(day -> {
                try
                {
                    Integer dayNum = (Integer) day;
                    if (dayNum > 0)
                    {
                        sb.append(parseNumberToString(dayNum));
                    }
                    else if (dayNum == 0)
                    {
                        sb.append("any");
                    }
                    else
                    {
                        sb.append("last");
                    }
                    counter.set(counter.get() + 1);
                    addSeparator(totalCountDays, counter.get(), sb);
                }
                catch(ParseException e)
                {
                    _logger.error(e);
                }
            });
            sb.append(" day of the month ");
        }
        else if(!recur.getWeekNoList().isEmpty())
        {
            sb.append("on the ");
            final int totalCountWeeks = recur.getWeekNoList().size();
            final AtomicReference<Integer> counter = new AtomicReference<>(0);
            recur.getWeekNoList().forEach(week -> {
                try
                {
                    Integer weekNo = (Integer) week;
                    if(weekNo > 0)
                    {
                        sb.append(parseNumberToString(weekNo));
                    }
                    else
                    {
                        sb.append("last");
                    }
                    counter.set(counter.get() + 1);
                    addSeparator(totalCountWeeks, counter.get(), sb);
                }
                catch(ParseException e)
                {
                    _logger.error(e);
                }
            });
            sb.append(" week of the month ");
            if(!recur.getDayList().isEmpty())
                sb.append("and ");
        }
        if(!recur.getDayList().isEmpty())
        {
            parseWeekly(recur, sb);
            sb.append(" of the month");
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseYearly(Recur recur, StringBuilder sb)
    {
        if(!recur.getMonthList().isEmpty())
        {
            sb.append("each ");
            final int totalCountMonths = recur.getMonthList().size();
            final AtomicReference<Integer> counter = new AtomicReference<>(0);
            recur.getMonthList().forEach(month -> {
                Integer monthNo = (Integer)month;
                sb.append(new DateFormatSymbols().getMonths()[monthNo - 1]);
                counter.set(counter.get() + 1);
                addSeparator(totalCountMonths, counter.get(), sb);
            });
            parseMonthly(recur, sb);
        }
    }

    private static String parseNumberToString(Integer number) throws ParseException
    {
        String numberString = String.valueOf(number);
        char[] digits = numberString.toCharArray();
        int ones;
        StringBuilder sb = new StringBuilder();
        if(digits.length == 0) throw new ParseException("Unable to parse number to string: " + numberString, 0);
        if(digits.length > 1)
        {
            int tens = Character.getNumericValue(digits[0]);
            ones = Character.getNumericValue(digits[1]);
            switch(tens)
            {
                case 1:
                    if(ones > 0)
                    {
                        return parseTeenNumberToString(numberString, ones);
                    }
                    else
                    {
                        return "10th";
                    }
                case 2:
                    if(ones > 0)
                    {
                        sb.append('2');
                        break;
                    }
                    else
                    {
                        return "20th";
                    }
                case 3:
                    if(ones > 0)
                    {
                        sb.append('3');
                        break;
                    }
                    else
                    {
                        return "30th";
                    }
                default:
                    throw new ParseException("Unable to parse number to string: " + numberString, 0);

            }
        }
        else
        {
            ones = Character.getNumericValue(digits[0]);
        }
        switch(ones)
        {
            case 1:
                sb.append("1st");
                break;
            case 2:
                sb.append("2nd");
                break;
            case 3:
                sb.append("3rd");
                break;
            case 4:
                sb.append("4th");
                break;
            case 5:
                sb.append("5th");
                break;
            case 6:
                sb.append("6th");
                break;
            case 7:
                sb.append("7th");
                break;
            case 8:
                sb.append("8th");
                break;
            case 9:
                sb.append("9th");
                break;
            default:
                throw new ParseException("Unable to parse number to string: " + numberString, 1);
        }
        return sb.toString();
    }

    private static String parseTeenNumberToString(String dayOfMonthString, int ones) throws ParseException
    {
        switch(ones)
        {
            case 1:
                return "11th";
            case 2:
                return "12th";
            case 3:
                return "13th";
            case 4:
                return "14th";
            case 5:
                return "15th";
            case 6:
                return "16th";
            case 7:
                return "17th";
            case 8:
                return "18th";
            case 9:
                return "19th";
            default:
                throw new ParseException("Unable to parse day of month: " + dayOfMonthString, 1);
        }
    }

    private static String parseWeekDay(WeekDay day) throws ParseException
    {
        StringBuilder sb = new StringBuilder();
        if(day.getDay().equals(WeekDay.MO.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Monday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.TU.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Tuesday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.WE.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Wednesday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.TH.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Thursday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.FR.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Friday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.SA.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Saturday");
            return sb.toString();
        }
        if(day.getDay().equals(WeekDay.SU.getDay()))
        {
            if (day.getOffset() > 0)
            {
                sb.append(parseNumberToString(day.getOffset())).append(' ');
            }
            else if (day.getOffset() == -1)
            {
                sb.append("last").append(' ');
            }
            sb.append("Sunday");
            return sb.toString();
        }
        throw new ParseException("Unable to parse WeekDay: " + day.getDay(), 0);
    }

    private static void addSeparator(int totalCount, int currentCount, StringBuilder sb)
    {
        if(currentCount == (totalCount - 1))
        {
            sb.append(", and ");
        }
        else if(currentCount < totalCount)
        {
            sb.append(", ");
        }
        else
        {
            sb.append(' ');
        }
    }
}
