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

package com.example.app.ui;

import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.validation.Validator;

/**
 * Validator implementation that delegates to a ValueEditor.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class ValueEditorValidator implements Validator
{
    @Override
    public boolean isComponentValidationSupported(Component component)
    {
        return component instanceof ValueEditor<?>;
    }

    @Override
    public boolean validate(Component component, Notifiable notifiable)
    {
        return ((ValueEditor<?>)component).validateUIValue(notifiable);
    }
}
