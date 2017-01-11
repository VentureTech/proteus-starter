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

package com.example.app.schedule.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;

import com.example.app.schedule.model.ICal4jSchedule;
import com.example.app.schedule.model.WeekOfMonth;
import com.example.app.support.service.AppUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.ui.miwt.CannotCloseMIWTException;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.BooleanValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CheckboxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.RadioButtonValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.component.event.ItemEventType;
import net.proteusframework.ui.miwt.data.event.ListSelectionEvent;
import net.proteusframework.ui.miwt.data.event.ListSelectionListener;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.event.EventQueue;
import net.proteusframework.ui.miwt.event.EventQueueElement;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.schedule.ui.ICal4jScheduleEditorLOK.*;
import static java.time.DayOfWeek.*;
import static java.util.Arrays.asList;
import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.core.locale.TextSources.EMPTY;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.ui.miwt.util.CommonButtonText.ANY;

/**
 * iCal4j schedule editor.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.example.app.schedule.ui.ICal4jScheduleEditor",
    i18n = {
        @I18N(symbol = "Label Repeat", l10n = @L10N("Repeat")),
        @I18N(symbol = "Label Next", l10n = @L10N("Frequency")),
        @I18N(symbol = "Label Repeat Every", l10n = @L10N("Every")),
        @I18N(symbol = "Label By", l10n = @L10N("By")),
        @I18N(symbol = "Label Month", l10n = @L10N("Month")),
        @I18N(symbol = "By Day Of Month", l10n = @L10N("Day Of Month")),
        @I18N(symbol = "By Day Of Week", l10n = @L10N("Day Of Week")),
        @I18N(symbol = "By Day Of Week And Week No", l10n = @L10N("Day Of Week And Week #")),
        @I18N(symbol = "Label Day Of Month", l10n = @L10N("Day Of Month")),
        @I18N(symbol = "Label On Day", l10n = @L10N("On Day")),
        @I18N(symbol = "Label On Week", l10n = @L10N("On Week")),
        @I18N(symbol = "Last Day Of Month", l10n = @L10N("Last"))
    }
)
public class ICal4jScheduleEditor extends Container implements ValueEditor<ICal4jSchedule>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ICal4jScheduleEditor.class);

    static enum NextInterval
    {
        Day("DAILY"),
        Week("WEEKLY"),
        Month("MONTHLY"),
        Year("YEARLY");

        private final String _recurFrequency;

        /**
         * Return an interval from another representation.
         *
         * @param otherRepresentation the other representation
         *
         * @return the interval.
         */
        public static NextInterval of(String otherRepresentation)
        {
            switch (otherRepresentation)
            {
                case Recur.DAILY:
                    return Day;
                case Recur.WEEKLY:
                    return Week;
                case Recur.MONTHLY:
                    return Month;
                case Recur.YEARLY:
                    return Year;
                default:
                    throw new AssertionError("Invalid representation: " + otherRepresentation);
            }
        }

        NextInterval(String recurFrequency)
        {
            _recurFrequency = recurFrequency;
        }

        /**
         * Return Recur frequency.
         *
         * @return the recur (ical4j) frequency.
         */
        @Contract(pure = true)
        public String toRecur()
        {
            return _recurFrequency;
        }
    }

    private static class DoWWoM extends Container
    {
        private final RadioButtonValueEditor<DayOfWeek> _innerOnDay = new RadioButtonValueEditor<>(LABEL_ON_DAY(),
            EnumSet.allOf(DayOfWeek.class), null);
        private final RadioButtonValueEditor<WeekOfMonth> _innerOnWeek = new RadioButtonValueEditor<>(LABEL_ON_WEEK(),
            EnumSet.allOf(WeekOfMonth.class), null);
        private final List<DoWWoM> _editorList;
        private boolean _showRemoveButton = true;

        /**
         * Instantiates a new editor.
         *
         * @param editorList the editor list.
         */
        public DoWWoM(List<DoWWoM> editorList)
        {
            _editorList = editorList;
        }

        /**
         * Get the on day editor.
         *
         * @return the on day editor.
         */
        public RadioButtonValueEditor<DayOfWeek> getOnDay()
        {
            return _innerOnDay;
        }

        /**
         * Get the on week editor.
         *
         * @return the on week editor.
         */
        public RadioButtonValueEditor<WeekOfMonth> getOnWeek()
        {
            return _innerOnWeek;
        }

        @Override
        public void init()
        {
            super.init();
            addClassName("dow-wom");

            if (_showRemoveButton)
                _editorList.add(this);

            add(_innerOnDay);
            add(_innerOnWeek);

            _innerOnDay.addClassName("dow");
            _innerOnWeek.addClassName("wom");

            if (_showRemoveButton)
            {
                final PushButton removeBtn = CommonActions.REMOVE.push();
                add(of("actions", removeBtn));
                removeBtn.addActionListener(ev -> close());
            }
        }

        @Override
        public void close() throws CannotCloseMIWTException
        {
            super.close();
            _editorList.remove(this);
        }

        /**
         * Set the show remove button flag.
         *
         * @param showRemoveButton if true, a remove button will be visible.
         */
        public void setShowRemoveButton(boolean showRemoveButton)
        {
            _showRemoveButton = showRemoveButton;
        }
    }
    protected final BooleanValueEditor _repeat = new BooleanValueEditor(LABEL_REPEAT(), null);
    protected final ComboBoxValueEditor<Integer> _repeatEvery = new ComboBoxValueEditor<>(LABEL_REPEAT_EVERY(),
        asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30),
        1);
    private final ComboBoxValueEditor<NextInterval> _next = new ComboBoxValueEditor<>(LABEL_NEXT(),
        EnumSet.allOf(NextInterval.class),
        NextInterval.Month);
    private final Label _repeatEveryUnit = new Label();
    private final ComboBoxValueEditor<Month> _month = new ComboBoxValueEditor<>(LABEL_MONTH(), asList(Month.values()), null);
    private final ComboBoxValueEditor<LocalizedObjectKey> _monthBy = new ComboBoxValueEditor<>(LABEL_BY(),
        asList(BY_DAY_OF_MONTH(), BY_DAY_OF_WEEK(), BY_DAY_OF_WEEK_AND_WEEK_NO()), BY_DAY_OF_MONTH());
    private final ComboBoxValueEditor<LocalizedObjectKey> _yearBy = new ComboBoxValueEditor<>(LABEL_BY(),
        asList(BY_DAY_OF_MONTH(), BY_DAY_OF_WEEK_AND_WEEK_NO()), BY_DAY_OF_MONTH());
    private final ComboBoxValueEditor<Object> _dayOfMonth = new ComboBoxValueEditor<>(LABEL_DAY_OF_MONTH(),
        asList(ANY, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            LAST_DAY_OF_MONTH()),
        1);
    private final List<DoWWoM> _dayWeekEditors = new ArrayList<>();
    private final CheckboxValueEditor<DayOfWeek> _onDay = new CheckboxValueEditor<>(LABEL_ON_DAY(),
        EnumSet.allOf(DayOfWeek.class), null);
    private final DoWWoM _yearMonthDoWWoM = new DoWWoM(_dayWeekEditors);
    private final Container _dowWoMWrapper = new Container();
    private ICal4jSchedule _value;
    private boolean _isAlwaysRepeat;

    static WeekDay getWeekDay(DayOfWeek dow)
    {
        switch (dow)
        {
            case MONDAY:
                return WeekDay.MO;
            case TUESDAY:
                return WeekDay.TU;
            case WEDNESDAY:
                return WeekDay.WE;
            case THURSDAY:
                return WeekDay.TH;
            case FRIDAY:
                return WeekDay.FR;
            case SATURDAY:
                return WeekDay.SA;
            case SUNDAY:
                return WeekDay.SU;
            default:
                throw new AssertionError("Invalid DayOfWeek: " + dow);
        }
    }    static DayOfWeek getDayOfWeek(WeekDay wd)
    {
        switch (wd.getDay())
        {
            case "MO":
                return MONDAY;
            case "TU":
                return TUESDAY;
            case "WE":
                return WEDNESDAY;
            case "TH":
                return THURSDAY;
            case "FR":
                return FRIDAY;
            case "SA":
                return SATURDAY;
            case "SU":
                return SUNDAY;
            default:
                throw new AssertionError("Invalid WeekDay: " + wd);
        }
    }

    static void setDayMonthValue(Recur recur, @Nullable Object value)
    {
        if (value == null)
            return;
        if (Objects.equals(LAST_DAY_OF_MONTH(), value))
        {
            recur.getMonthDayList().add(-1);
        }
        else if (value instanceof Number)
        {
            final Number number = (Number) value;
            recur.getMonthDayList().add(number.intValue());
        }
    }

    /**
     * Instantiates a new editor.
     */
    public ICal4jScheduleEditor()
    {
        _yearMonthDoWWoM.setVisible(false);
        _yearMonthDoWWoM.setShowRemoveButton(false);
        _yearMonthDoWWoM.getOnWeek().setValue(WeekOfMonth.FIRST);
        _yearMonthDoWWoM.getOnDay().setValue(MONDAY);
        _yearBy.setVisible(false);

        _month.setVisible(false);
        _onDay.setVisible(false);
        _dowWoMWrapper.setVisible(false);
        _dayOfMonth.setVisible(false);

        addClassName("prop").addClassName("schedule");
    }

    @Override
    public boolean isEditable()
    {
        return true;
    }    /**
     * Get boolean flag.  If true, this schedule editor will not display the "repeat" editor, and it will always be set to repeat
     *
     * @return boolean flag
     */
    public boolean isAlwaysRepeat()
    {
        return _isAlwaysRepeat;
    }

    @Override
    public void setEditable(boolean b)
    {
        throw new UnsupportedOperationException();
    }    /**
     * Set boolean flag.  If true, this schedule editor will not display the "repeat" editor, and it will always be set to repeat
     *
     * @param isAlwaysRepeat boolean flag
     */
    public void setAlwaysRepeat(boolean isAlwaysRepeat)
    {
        _isAlwaysRepeat = isAlwaysRepeat;
    }

    private void addDayOfTheMonthToRecurCommit(Recur recur)
    {
        setDayMonthValue(recur, _dayOfMonth.commitValue());
    }

    private void addDayOfTheMonthToRecurUIValue(Level logErrorLevel, Recur recur)
    {
        setDayMonthValue(recur, _dayOfMonth.getUIValue(logErrorLevel));
    }    @Nullable
    @Override
    public ICal4jSchedule commitValue() throws MIWTException
    {
        final NextInterval next = _next.commitValue();
        assert next != null;
        final Boolean repeat = _repeat.commitValue() || isAlwaysRepeat();
        final Integer repeatEvery = _repeatEvery.commitValue();
        if (repeat)
            assert repeatEvery != null;
        final Recur recur = new Recur();
        recur.setFrequency(next.toRecur());
        if (repeat)
            recur.setInterval(repeatEvery);


        switch (next)
        {
            case Day:
            {
                // Nothing to do for Day.
                break;
            }
            case Week:
            {
                addOnDayToRecurCommit(recur);
                break;
            }
            case Month:
            {
                commitMonth(recur);
                break;
            }
            case Year:
            {
                commitYear(recur);
                break;
            }
            default:
                throw new AssertionError("Missing case for " + next);
        }


        ICal4jSchedule ical4jSchedule = getValue() == null ? new ICal4jSchedule() : getValue();
        ical4jSchedule.setRepeat(repeat);
        ical4jSchedule.setRecurrenceRule(recur.toString());
        _value = ical4jSchedule;
        return ical4jSchedule;
    }

    private void addOnDayToRecurCommit(Recur recur)
    {
        final Collection<DayOfWeek> uiValue = _onDay.commitValue();
        assert uiValue != null;
        final WeekDayList dayList = recur.getDayList();
        uiValue.stream().map(ICal4jScheduleEditor::getWeekDay).forEach(dayList::add);
    }    @Override
    public ModificationState getModificationState()
    {
        return AppUtil.getModificationStateForComponent(this);
    }

    private void addOnDayToRecurForUIValue(Level logErrorLevel, Recur recur)
    {
        final Collection<DayOfWeek> uiValue = _onDay.getUIValue(logErrorLevel);
        assert uiValue != null;
        final WeekDayList dayList = recur.getDayList();
        uiValue.stream().map(ICal4jScheduleEditor::getWeekDay).forEach(dayList::add);
    }    @Nullable
    @Override
    public ICal4jSchedule getUIValue(Level logErrorLevel)
    {
        final NextInterval next = _next.getUIValue(logErrorLevel);
        assert next != null;
        final Boolean repeat = _repeat.getUIValue(logErrorLevel) || isAlwaysRepeat();
        final Integer repeatEvery = _repeatEvery.getUIValue(logErrorLevel);
        if (repeat)
            assert repeatEvery != null;
        final Recur recur = new Recur();
        recur.setFrequency(next.toRecur());
        if (repeat)
            recur.setInterval(repeatEvery);


        switch (next)
        {
            case Day:
            {
                // Nothing to do for Day.
                break;
            }
            case Week:
            {
                addOnDayToRecurForUIValue(logErrorLevel, recur);
                break;
            }
            case Month:
            {
                getUIValueMonth(logErrorLevel, recur);
                break;
            }
            case Year:
            {
                getUIValueYear(logErrorLevel, recur);
                break;
            }
            default:
                throw new AssertionError("Missing case for " + next);
        }


        ICal4jSchedule ical4jSchedule = new ICal4jSchedule();
        ical4jSchedule.setRepeat(repeat);
        ical4jSchedule.setRecurrenceRule(recur.toString());
        return ical4jSchedule;
    }

    private void commitMonth(Recur recur)
    {
        final LocalizedObjectKey uiValue = _monthBy.commitValue();
        assert uiValue != null;
        final String symbolicName = uiValue.getSymbolicName();
        switch (symbolicName)
        {
            case BY_DAY_OF_MONTH_SYMBOL:
            {
                addDayOfTheMonthToRecurCommit(recur);
                break;
            }
            case BY_DAY_OF_WEEK_SYMBOL:
            {
                addOnDayToRecurCommit(recur);
                break;
            }
            case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
            {
                for (DoWWoM tmp : _dayWeekEditors)
                {
                    final DayOfWeek dow = tmp.getOnDay().commitValue();
                    if (dow == null) continue;

                    final WeekDay weekDay = getWeekDay(dow);
                    final WeekOfMonth weekOfMonth = tmp.getOnWeek().commitValue();
                    assert weekOfMonth != null;
                    recur.getDayList().add(new WeekDay(weekDay, weekOfMonth.getOffset()));
                }
                break;
            }
            default:
                _logger.error("Unhandled by: " + symbolicName);
                break;
        }
    }    @Nullable
    @Override
    public ICal4jSchedule getValue()
    {
        return _value;
    }

    private void commitYear(Recur recur)
    {
        final Month month = _month.commitValue();
        if (month != null)
            recur.getMonthList().add(month.getValue());
        final LocalizedObjectKey by = _yearBy.commitValue();
        assert by != null;
        final String symbolicName = by.getSymbolicName();
        switch (symbolicName)
        {
            case BY_DAY_OF_MONTH_SYMBOL:
            {
                addDayOfTheMonthToRecurCommit(recur);
                break;
            }
            case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
            {
                final DayOfWeek dow = _yearMonthDoWWoM.getOnDay().commitValue();
                assert dow != null;
                final WeekDay weekDay = getWeekDay(dow);
                final WeekOfMonth weekOfMonth = _yearMonthDoWWoM.getOnWeek().commitValue();
                assert weekOfMonth != null;
                recur.getDayList().add(new WeekDay(weekDay, weekOfMonth.getOffset()));
                break;
            }
            default:
                _logger.error("Unhandled by: " + symbolicName);
                break;
        }
    }    @SuppressFBWarnings(value = "UC_USELESS_OBJECT_STACK", justification = "Avoid ConcurrentModificationException")
    @Override
    public void setValue(@Nullable ICal4jSchedule value)
    {
        _value = value;
        if (!isInited())
            return;

        new ArrayList<>(_dayWeekEditors).forEach(Component::close);
        _dayOfMonth.setValue(null);
        _onDay.setValue(null);
        if (value == null || isEmptyString(value.getRecurrenceRule()))
        {
            _next.setValue(NextInterval.Month);
            _repeatEvery.setValue(1);
            _monthBy.setValue(BY_DAY_OF_MONTH());
            _repeat.setValue(false);
        }
        else
        {
            _next.setValue(null);
            _monthBy.getValueComponent().clearSelection();
            Recur recur;
            try
            {
                recur = new Recur(value.getRecurrenceRule());
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException("Invalid schedule.", e);
            }
            final NextInterval frequency = NextInterval.of(recur.getFrequency());
            _next.setValue(frequency);
            final int recurInterval = recur.getInterval();
            if (recurInterval != -1)
                _repeatEvery.setValue(recurInterval);
            if (!recur.getDayList().isEmpty())
            {
                final WeekDayList dayList = recur.getDayList();
                final List<DayOfWeek> dayOfWeekList = new ArrayList<>();
                boolean includesOffset = false;
                for (Object o : dayList)
                {
                    WeekDay wd = (WeekDay) o;
                    if (wd.getOffset() != 0)
                    {
                        includesOffset = true;
                        break;
                    }
                    dayOfWeekList.add(getDayOfWeek(wd));
                }
                if (includesOffset)
                {
                    if (frequency == NextInterval.Month)
                        _monthBy.setValue(BY_DAY_OF_WEEK_AND_WEEK_NO());
                    else if (frequency == NextInterval.Year)
                        _yearBy.setValue(BY_DAY_OF_WEEK_AND_WEEK_NO());
                    else
                        _logger.warn("Unsupported frequency for DayOfWeek/WeekOfMonth: " + frequency);
                    if (frequency == NextInterval.Month)
                    {
                        new ArrayList<>(_dayWeekEditors).forEach(Component::close);
                        for (Object o : dayList)
                        {
                            WeekDay wd = (WeekDay) o;
                            final DoWWoM c = new DoWWoM(_dayWeekEditors);
                            _dowWoMWrapper.add(c);
                            assert wd.getOffset() != 0 : "Expecting offset";
                            DayOfWeek dayOfWeek = getDayOfWeek(wd);
                            c.getOnDay().setValue(dayOfWeek);
                            c.getOnWeek().setValue(WeekOfMonth.fromOffset(wd.getOffset()));
                        }

                    }
                    else if (frequency == NextInterval.Year)
                    {
                        if (dayList.size() > 1)
                        {
                            _logger.warn("Year frequency only supports a single DayOfWeek/WeekOfMonth. Discarding remainder.");
                        }
                        WeekDay wd = (WeekDay) dayList.get(0);
                        DayOfWeek dayOfWeek = getDayOfWeek(wd);
                        _yearMonthDoWWoM.getOnDay().setValue(dayOfWeek);
                        _yearMonthDoWWoM.getOnWeek().setValue(WeekOfMonth.fromOffset(wd.getOffset()));
                    }
                }
                else
                {
                    if (frequency != NextInterval.Month)
                        _logger.warn("Unsupported frequency for DayOfWeek: " + frequency);
                    _monthBy.setValue(BY_DAY_OF_WEEK());
                    _onDay.setValue(dayOfWeekList);
                }
            }
            else
            {
                _monthBy.setValue(BY_DAY_OF_MONTH());
                final NumberList monthDayList = recur.getMonthDayList();
                if (!monthDayList.isEmpty())
                {
                    final Integer n = (Integer) monthDayList.get(0);
                    if (Objects.equals(n, -1))
                        _dayOfMonth.setValue(LAST_DAY_OF_MONTH());
                    else
                        _dayOfMonth.setValue(n);
                }

            }
            if (!recur.getMonthList().isEmpty())
            {
                final Integer n = (Integer) recur.getMonthList().get(0);
                _month.setValue(Month.of(n));
            }
            _repeat.setValue(value.isRepeat());
        }
    }

    private void getUIValueMonth(Level logErrorLevel, Recur recur)
    {
        final LocalizedObjectKey uiValue = _monthBy.getUIValue(logErrorLevel);
        assert uiValue != null;
        final String symbolicName = uiValue.getSymbolicName();
        switch (symbolicName)
        {
            case BY_DAY_OF_MONTH_SYMBOL:
            {
                addDayOfTheMonthToRecurUIValue(logErrorLevel, recur);
                break;
            }
            case BY_DAY_OF_WEEK_SYMBOL:
            {
                addOnDayToRecurForUIValue(logErrorLevel, recur);
                break;
            }
            case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
            {
                for (DoWWoM tmp : _dayWeekEditors)
                {
                    final DayOfWeek dow = tmp.getOnDay().getUIValue(logErrorLevel);
                    if (dow == null) continue;

                    final WeekDay weekDay = getWeekDay(dow);
                    final WeekOfMonth weekOfMonth = tmp.getOnWeek().getUIValue(logErrorLevel);
                    assert weekOfMonth != null;
                    recur.getDayList().add(new WeekDay(weekDay, weekOfMonth.getOffset()));
                }
                break;
            }
            default:
                _logger.error("Unhandled by: " + symbolicName);
                break;
        }
    }    @Override
    public void init()
    {
        super.init();

        final ListSelectionListener nextIntervalListener = ev -> {
            final NextInterval uiValue = _next.getUIValue();
            assert uiValue != null;
            _repeatEveryUnit.setText(createText(uiValue.name() + 's'));
            switch (uiValue)
            {
                case Day:
                {
                    _month.setVisible(false);
                    _monthBy.setVisible(false);
                    _yearBy.setVisible(false);
                    _yearMonthDoWWoM.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(false);
                    _onDay.setVisible(false);
                    break;
                }
                case Year:
                {
                    _yearBy.setVisible(true);
                    _yearMonthDoWWoM.setVisible(false);
                    _month.setVisible(true);
                    _monthBy.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(true);
                    _onDay.setVisible(true);
                    // Trigger by listener
                    final LocalizedObjectKey byValue = _yearBy.getValue();
                    _yearBy.setValue(null);
                    _yearBy.setValue(byValue);
                    break;
                }
                case Month:
                {
                    _yearBy.setVisible(false);
                    _yearMonthDoWWoM.setVisible(false);
                    _month.setVisible(false);
                    _monthBy.setVisible(true);
                    _dowWoMWrapper.setVisible(true);
                    _dayOfMonth.setVisible(true);
                    _onDay.setVisible(true);
                    // Trigger by listener
                    final LocalizedObjectKey byValue = _monthBy.getValue();
                    _monthBy.setValue(null);
                    _monthBy.setValue(byValue);
                    break;
                }
                case Week:
                {
                    _yearBy.setVisible(false);
                    _yearMonthDoWWoM.setVisible(false);
                    _month.setVisible(false);
                    _monthBy.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(false);
                    _onDay.setVisible(true);
                    break;
                }
                default:
                    break;
            }
        };
        _next.getValueComponent().watch().getSelectionModel().addListSelectionListener(nextIntervalListener);
        _repeat.setSelected(true);
        _repeat.setVisible(!isAlwaysRepeat());
        final ListSelectionListener monthByListener = ev -> {
            final LocalizedObjectKey by = _monthBy.getUIValue();
            assert by != null;
            final String symbolicName = by.getSymbolicName();
            switch (symbolicName)
            {
                case BY_DAY_OF_MONTH_SYMBOL:
                {
                    _onDay.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(true);
                    break;
                }
                case BY_DAY_OF_WEEK_SYMBOL:
                {
                    _onDay.setVisible(true);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(false);
                    break;
                }
                case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
                {
                    _onDay.setVisible(false);
                    _dowWoMWrapper.setVisible(true);
                    _dayOfMonth.setVisible(false);
                    if (_dayWeekEditors.isEmpty())
                    {
                        _dowWoMWrapper.add(new DoWWoM(_dayWeekEditors));
                    }
                    break;
                }
                default:
                    _logger.error("Unhandled by: " + by);
                    break;
            }
        };
        _monthBy.getValueComponent().watch().getSelectionModel().addListSelectionListener(monthByListener);
        _yearBy.getValueComponent().watch().getSelectionModel().addListSelectionListener(ev -> {
            final LocalizedObjectKey by = _yearBy.getUIValue();
            assert by != null;
            final String symbolicName = by.getSymbolicName();
            switch (symbolicName)
            {
                case BY_DAY_OF_MONTH_SYMBOL:
                {
                    _onDay.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(true);
                    _yearMonthDoWWoM.setVisible(false);
                    break;
                }
                case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
                {
                    _onDay.setVisible(false);
                    _dowWoMWrapper.setVisible(false);
                    _dayOfMonth.setVisible(false);
                    _yearMonthDoWWoM.setVisible(true);
                    break;
                }
                default:
                    _logger.error("Unhandled by: " + by);
                    break;
            }
        });

        add(_repeat);
        add(of(_repeatEvery, _repeatEveryUnit));
        add(_next);
        add(_month);
        add(_monthBy);
        add(_yearBy);
        add(_yearMonthDoWWoM);
        add(_dowWoMWrapper);
        add(_dayOfMonth);
        add(_onDay);

        initDoWWoMWrapper();


        _next.addClassName("frequency");
        _repeat.addClassName("repeat");
        _repeatEvery.getParent().addClassName("repeat-interval-wrapper");
        _repeatEvery.addClassName("repeat-interval");
        _repeatEvery.setHTMLElement(HTMLElement.span);
        _month.addClassName("month");
        _monthBy.addClassName("by");
        _dowWoMWrapper.addClassName("dow-wom-wrapper");
        _dayOfMonth.addClassName("dom");
        _onDay.addClassName("dow");

        _repeat.setHTMLElement(HTMLElement.span);
        _repeatEveryUnit.addClassName("repeat-unit");

        _month.setCellRenderer(new CustomCellRenderer(EMPTY, input -> {
            Month m = (Month) input;
            assert m != null;
            return createText(m.getDisplayName(TextStyle.FULL, getLocaleContext().getLocale()));
        }));

        _repeat.watch().addItemListener(ev -> {
            final boolean selected = ev.getStateChange() == ItemEventType.STATE_SELECTED;
            if (!selected)
            {
                hideNonRepeatComponents();
            }
            if (selected)
            {
                _repeatEvery.getParent().setVisible(true);
                { // Next
                    _next.setVisible(true);
                    final ComboBox cbc = _next.getValueComponent();
                    final int idx = cbc.getSelectedIndex();
                    nextIntervalListener.valueChanged(new ListSelectionEvent(cbc, idx, idx, false));
                }
            }
        });

        setValue(getValue());
        EventQueue.queue(new EventQueueElement()
        {
            @Override
            public void fire()
            {
                if (!_repeat.isSelected())
                {
                    hideNonRepeatComponents();
                }
            }

            @Override
            public int getEventPriority()
            {
                return Event.PRIORITY_SESSION;
            }
        });
    }

    private void getUIValueYear(Level logErrorLevel, Recur recur)
    {
        final Month month = _month.getUIValue(logErrorLevel);
        if (month != null)
            recur.getMonthList().add(month.getValue());
        final LocalizedObjectKey by = _yearBy.getUIValue();
        assert by != null;
        final String symbolicName = by.getSymbolicName();
        switch (symbolicName)
        {
            case BY_DAY_OF_MONTH_SYMBOL:
            {
                addDayOfTheMonthToRecurUIValue(logErrorLevel, recur);
                break;
            }
            case BY_DAY_OF_WEEK_AND_WEEK_NO_SYMBOL:
            {
                final DayOfWeek dow = _yearMonthDoWWoM.getOnDay().getUIValue(logErrorLevel);
                assert dow != null;
                final WeekDay weekDay = getWeekDay(dow);
                final WeekOfMonth weekOfMonth = _yearMonthDoWWoM.getOnWeek().getUIValue(logErrorLevel);
                assert weekOfMonth != null;
                recur.getDayList().add(new WeekDay(weekDay, weekOfMonth.getOffset()));
                break;
            }
            default:
                _logger.error("Unhandled by: " + symbolicName);
                break;
        }
    }    private void hideNonRepeatComponents()
    {
        if (!isAlwaysRepeat())
        {
            Iterator<Component> it = components();
            while (it.hasNext())
            {
                Component next = it.next();
                if (_repeat != next && next.getHTMLElement() != HTMLElement.h1)
                {
                    next.setVisible(false);
                }
            }
        }
    }





    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        return true;
    }

















    private void initDoWWoMWrapper()
    {
        _dowWoMWrapper.addClassName("dow-wom-wrapper");
        final PushButton addDayWeekContainer = CommonActions.ADD.push();
        addDayWeekContainer.setTooltip(
            ConcatTextSource.create(CommonActions.ADD.getName(), BY_DAY_OF_WEEK_AND_WEEK_NO())
                .withSpaceSeparator()
        );
        AppUtil.enableTooltip(addDayWeekContainer);
        _dowWoMWrapper.add(of("actions", addDayWeekContainer).withHTMLElement(HTMLElement.span));
        addDayWeekContainer.addActionListener(ev -> _dowWoMWrapper.add(new DoWWoM(_dayWeekEditors)));
    }

}
