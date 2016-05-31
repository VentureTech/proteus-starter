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

package com.example.app.ui.schedule;

import java.util.Iterator;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.ui.miwt.component.Component;

/**
 * Editor that only allows repeating schedules.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
public class RepeatOnlyICal4jScheduleEditor extends ICal4jScheduleEditor
{
    @Override
    public void init()
    {
        super.init();
        _repeat.setSelected(false);
        moveToTop(_repeatEvery.getParent());
        moveToTop(_repeat);
        Iterator<Component> it = components();
        while(it.hasNext())
        {
            Component next = it.next();
            if(next.getHTMLElement() == HTMLElement.h1)
            {
                moveToTop(next);
                break;
            }
        }
    }
}
