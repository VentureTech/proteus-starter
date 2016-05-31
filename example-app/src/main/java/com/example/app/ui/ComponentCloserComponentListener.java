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

import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.event.ComponentAdapter;
import net.proteusframework.ui.miwt.component.event.ComponentEvent;

/**
 * Component adapter that closes the given Component when the component the listener it is attached to closes.
 * You can use this to have to close a component when another, different, component is closed.
 * @author Alan Holt (aholt@venturetech.net)
 */
public class ComponentCloserComponentListener extends ComponentAdapter
{
    private final Component _component;

    /**
     *   Instantiates a new ComponentCloserComponentListener
     * @param component the Component to close
     */
    public ComponentCloserComponentListener(Component component)
    {
        _component = component;
    }

    @Override
    public void componentClosed(ComponentEvent e)
    {
        _component.close();
    }
}