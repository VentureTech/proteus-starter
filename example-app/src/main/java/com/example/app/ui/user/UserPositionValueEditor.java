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

package com.example.app.ui.user;

import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserPosition;
import com.example.app.support.AppUtil;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.composite.editor.BooleanValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CalendarValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.data.RelativeOffsetRange;

import static com.example.app.ui.user.UserPositionValueEditorLOK.*;

/**
 * {@link CompositeValueEditor} for {@link UserPosition}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/10/15 2:49 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserPositionValueEditor",
    i18n = {
        @I18N(symbol = "Label Title", l10n = @L10N("Title")),
        @I18N(symbol = "Label Start Date", l10n = @L10N("Start Date")),
        @I18N(symbol = "Label End Date", l10n = @L10N("End Date")),
        @I18N(symbol = "Label Current", l10n = @L10N("Set as primary position?"))
    }
)
@Configurable
public class UserPositionValueEditor extends CompositeValueEditor<UserPosition>
{
    private final User _user;
    @Autowired
    private EntityRetriever _er;

    /**
     * Instantiate a new instance of UserPositionValueEditor
     *
     * @param user the User for which this UserPosition is.
     */
    public UserPositionValueEditor(@Nonnull User user)
    {
        super(UserPosition.class);

        Preconditions.checkNotNull(user, "Given User was null, this should not happen.");

        _user = user;
    }

    @Nullable
    @Override
    public UserPosition getUIValue(Level logErrorLevel)
    {
        UserPosition result = Optional.ofNullable(super.getUIValue(logErrorLevel))
            .orElseThrow(() -> new IllegalStateException("UserPosition was null.  This should not happen."));
        result.setUser(getUser());
        return result;
    }

    @Nonnull
    private User getUser()
    {
        return _er.reattachIfNecessary(_user);
    }

    @Nullable
    @Override
    public UserPosition commitValue() throws MIWTException
    {
        UserPosition result = Optional.ofNullable(super.commitValue())
            .orElseThrow(() -> new IllegalStateException("UserPosition was null.  This should not happen."));
        result.setUser(getUser());
        return result;
    }

    @Override
    public void init()
    {
        super.init();

        addEditorForProperty(() -> {
            TextEditor editor = new TextEditor(LABEL_TITLE(), null);
            editor.setRequiredValueValidator();
            return editor;
        }, UserPosition.POSITION_COLUMN_PROP);

        addEditorForProperty(() -> {
            final CalendarValueEditor editor = new CalendarValueEditor(LABEL_START_DATE(), null, new RelativeOffsetRange(70));
            editor.getValueComponent().setFixedTimeZone(AppUtil.UTC);
            editor.getValueComponent().setIncludeTime(false);
            return editor;
        }, UserPosition.START_DATE_COLUMN_PROP);

        addEditorForProperty(() -> {
            final CalendarValueEditor editor = new CalendarValueEditor(LABEL_END_DATE(), null, new RelativeOffsetRange(70));
            editor.getValueComponent().setFixedTimeZone(AppUtil.UTC);
            editor.getValueComponent().setIncludeTime(false);
            return editor;
        }, UserPosition.END_DATE_COLUMN_PROP);

        addEditorForProperty(() -> {
            return new BooleanValueEditor(LABEL_CURRENT(), null);
        }, UserPosition.CURRENT_COLUMN_PROP);
    }
}
