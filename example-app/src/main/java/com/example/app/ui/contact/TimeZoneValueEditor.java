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

package com.example.app.ui.contact;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonButtonText;

/**
 * ValueEditor for selecting a TimeZone
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class TimeZoneValueEditor extends ComboBoxValueEditor<TimeZone>
{
    /**
     * The enum Timezone render style.
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum TimezoneRenderStyle
    {
        /** SHORT */
        SHORT(TimeZone.SHORT),
        /** LONG */
        LONG(TimeZone.LONG),
        /** ID */
        ID(2);

        /** render style */
        private final int _renderStyle;

        /**
         * Instantiates a new Timezone render style.
         *
         * @param renderStyle the render style
         */
        TimezoneRenderStyle(int renderStyle)
        {
            _renderStyle = renderStyle;
        }

        /**
         * Gets render style.
         *
         * @return the render style
         */
        public int getRenderStyle()
        {
            return _renderStyle;
        }
    }
    private final TimezoneRenderStyle _renderStyle;
    private final TextSource _nullValueLabel;
    private final TimeZone _defaultValue;

    /**
     * Create a new instance of TimeZoneValueEditor by using TimeZone IDs rather than the TimeZones themselves.
     *
     * @param label the Label for the editor
     * @param timeZones the TimeZones to include in the dropdown
     * @param defaultValue the initial selected TimeZone
     * @param renderStyle the render style of the TimeZones within the dropdown
     * @param nullValueLabel the label to use on a null value within the dropdown.  defaults to "Please Select"
     *
     * @return a new instance of TimeZoneValueEditor
     */
    public static TimeZoneValueEditor create(@Nullable TextSource label, @NotNull List<String> timeZones,
        @Nullable String defaultValue, @NotNull TimezoneRenderStyle renderStyle, @Nullable TextSource nullValueLabel)
    {
        TimeZone tzVal = Optional.ofNullable(defaultValue).map(TimeZone::getTimeZone).orElse(null);
        return create(label, timeZones, tzVal, renderStyle, nullValueLabel);
    }

    /**
     * Create a new instance of TimeZoneValueEditor by using TimeZone IDs rather than the TimeZones themselves.
     *
     * @param label the Label for the editor
     * @param timeZones the TimeZones to include in the dropdown
     * @param defaultValue the initial selected TimeZone
     * @param renderStyle the render style of the TimeZones within the dropdown
     * @param nullValueLabel the label to use on a null value within the dropdown.  defaults to "Please Select"
     *
     * @return a new instance of TimeZoneValueEditor
     */
    public static TimeZoneValueEditor create(@Nullable TextSource label, @NotNull List<String> timeZones,
        @Nullable TimeZone defaultValue, @NotNull TimezoneRenderStyle renderStyle, @Nullable TextSource nullValueLabel)
    {
        List<TimeZone> options = timeZones.stream().map(TimeZone::getTimeZone).collect(Collectors.toList());
        return new TimeZoneValueEditor(label, options, defaultValue, renderStyle, nullValueLabel);
    }

    /**
     * Instantiate a new instance of TimeZoneValueEditor
     *
     * @param label the Label for the editor
     * @param timeZones the TimeZones to include in the dropdown
     * @param defaultValue the initial selected TimeZone
     * @param renderStyle the render style of the TimeZones within the dropdown
     * @param nullValueLabel the label to use on a null value within the dropdown.  defaults to "Please Select"
     */
    public TimeZoneValueEditor(@Nullable TextSource label, @NotNull List<TimeZone> timeZones, @Nullable TimeZone defaultValue,
        @NotNull TimezoneRenderStyle renderStyle, @Nullable TextSource nullValueLabel)
    {
        super(label, timeZones, defaultValue);

        _defaultValue = defaultValue;

        _nullValueLabel = Optional.ofNullable(nullValueLabel).orElse(CommonButtonText.PLEASE_SELECT);

        _renderStyle = renderStyle;
    }

    @Override
    public void init()
    {
        setCellRenderer(new CustomCellRenderer(_nullValueLabel, input -> {
            TimeZone tz = (TimeZone) input;
            if (_renderStyle != TimezoneRenderStyle.ID)
            {
                return tz.getDisplayName(false, _renderStyle.getRenderStyle(), getLocaleContext().getLocale());
            }
            else return tz.getID();
        }));
        super.init();
        final TimeZone defaultTimeZone = Event.getRequest().getHostname().getSite().getDefaultTimeZone();
        setValue(getValue() == null ? defaultTimeZone : getValue());
    }


    @Override
    public void setValue(@Nullable TimeZone value)
    {
        final List<TimeZone> options = getOptions();
        super.setValue(value == null && (options != null && !options.contains(null)) ? _defaultValue : value);
    }

    List<TimeZone> getOptions()
    {
        @SuppressWarnings("unchecked")
        SimpleListModel<TimeZone> model = (SimpleListModel<TimeZone>) getValueComponent().getModel();
        return Collections.unmodifiableList(model.getList());
    }
}
