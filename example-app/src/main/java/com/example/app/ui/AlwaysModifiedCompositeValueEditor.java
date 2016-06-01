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

import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;

/**
 * Value editor that is always modified..
 *
 * @param <C> class type.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class AlwaysModifiedCompositeValueEditor<C> extends CompositeValueEditor<C>
{
    /**
     * Instantiates a new Always modified composite value editor.
     *
     * @param clazz the clazz.
     */
    public AlwaysModifiedCompositeValueEditor(Class<C> clazz)
    {
        super(clazz);
    }

    @Override
    public ModificationState getModificationState()
    {
        return ModificationState.CHANGED;
    }
}
