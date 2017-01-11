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

package com.example.app.schedule.model;


import javax.annotation.Nonnull;
import java.time.Period;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.schedule.model.ScheduleIntervalLOK.*;


/**
 * Scheduling intervals.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.example.app.schedule.model.ScheduleInterval",
    i18n = {
        @I18N(symbol = "Annual", l10n = @L10N("Annual")),
        @I18N(symbol = "Annual Description", l10n = @L10N("Occurring Yearly")),
        @I18N(symbol = "Annual Sentence Form", l10n = @L10N("Year")),
        @I18N(symbol = "Semi-Annual", l10n = @L10N("Semi-Annual")),
        @I18N(symbol = "Semi-Annual Description", l10n = @L10N("Occurring Twice A Year")),
        @I18N(symbol = "Semi-Annual Sentence Form", l10n = @L10N("6 Months")),
        @I18N(symbol = "Quarter", l10n = @L10N("Quarterly")),
        @I18N(symbol = "Quarter Description", l10n = @L10N("Occurring Every Three Months")),
        @I18N(symbol = "Quarter Sentence Form", l10n = @L10N("3 Months")),
        @I18N(symbol = "Bi-Month", l10n = @L10N("Bi-Monthly")),
        @I18N(symbol = "Bi-Month Description", l10n = @L10N("Occurring Every Two Months")),
        @I18N(symbol = "Bi-Month Sentence Form", l10n = @L10N("2 Months")),
        @I18N(symbol = "Month", l10n = @L10N("Monthly")),
        @I18N(symbol = "Month Description", l10n = @L10N("Occurring Every Month")),
        @I18N(symbol = "Month Sentence Form", l10n = @L10N("Month")),
        @I18N(symbol = "Bi-Week", l10n = @L10N("Bi-Weekly")),
        @I18N(symbol = "Bi-Week Description", l10n = @L10N("Occurring Every Two Weeks")),
        @I18N(symbol = "Bi-Week Sentence Form", l10n = @L10N("2 Weeks")),
        @I18N(symbol = "Week", l10n = @L10N("Weekly")),
        @I18N(symbol = "Week Description", l10n = @L10N("Occurring Every Week")),
        @I18N(symbol = "Week Sentence Form", l10n = @L10N("Week")),
        @I18N(symbol = "Day", l10n = @L10N("Daily")),
        @I18N(symbol = "Day Description", l10n = @L10N("Occurring Every Day")),
        @I18N(symbol = "Day Sentence Form", l10n = @L10N("Day"))
    }
)
public enum ScheduleInterval implements NamedObject
{
    /** Interval. */
    annual(ANNUAL(), ANNUAL_DESCRIPTION(), ANNUAL_SENTENCE_FORM(), Period.ofYears(1)),
    /** Interval. */
    semi_annual(SEMI_ANNUAL(), SEMI_ANNUAL_DESCRIPTION(), SEMI_ANNUAL_SENTENCE_FORM(), Period.ofMonths(6)),
    /** Interval. */
    quarter(QUARTER(), QUARTER_DESCRIPTION(), QUARTER_SENTENCE_FORM(), Period.ofMonths(3)),
    /** Interval. */
    bi_month(BI_MONTH(), BI_MONTH_DESCRIPTION(), BI_MONTH_SENTENCE_FORM(), Period.ofMonths(2)),
    /** Interval. */
    month(MONTH(), MONTH_DESCRIPTION(), MONTH_SENTENCE_FORM(), Period.ofMonths(1)),
    /** Interval. */
    bi_week(BI_WEEK(), BI_WEEK_DESCRIPTION(), BI_WEEK_SENTENCE_FORM(), Period.ofWeeks(2)),
    /** Interval. */
    week(WEEK(), WEEK_DESCRIPTION(), WEEK_SENTENCE_FORM(), Period.ofWeeks(1)),
    /** Interval. */
    day(DAY(), DAY_DESCRIPTION(), DAY_SENTENCE_FORM(), Period.ofDays(1));

    /** Name. */
    @Nonnull
    private final TextSource _name;
    /** Description. */
    @Nonnull
    private final TextSource _description;
    @Nonnull
    private final Period _interval;
    @Nonnull
    private final TextSource _sentenceForm;

    /**
     * Constructor.
     *
     * @param name the interval name.
     * @param description the description.
     */
    ScheduleInterval(@Nonnull TextSource name, @Nonnull TextSource description,
        @Nonnull TextSource sentenceForm, @Nonnull Period interval)
    {
        _name = name;
        _description = description;
        _sentenceForm = sentenceForm;
        _interval = interval;
    }

    /**
     * Get the Interval that this ScheduleInterval uses
     *
     * @return the Interval
     */
    @Nonnull
    public Period getInterval()
    {
        return _interval;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return _name;
    }

    @Nonnull
    @Override
    public TextSource getDescription()
    {
        return _description;
    }

    /**
     * Get the Sentence Form of this Interval
     *
     * @return Sentence Form
     */
    @Nonnull
    public TextSource getSentenceForm()
    {
        return _sentenceForm;
    }
}
