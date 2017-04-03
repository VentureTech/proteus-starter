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

package com.example.app.guide;

import java.util.Date;

/**
 * A model used for property editor and viewer examples in {@link HTMLGuide}.
 *
 * @author Conner Rocole (crocole@venturetech.net)
 */
public class Widget
{
    private String _name;
    private String _description;
    private int _count;
    private Date _date;
    private boolean _flag;

    /**
     * Constructor
     */
    public Widget()
    {
    }

    /**
     * Get Name
     *
     * @return Name
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Set Name
     * @param name - Name
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Get Description
     * @return Description
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * Set Description
     * @param description - Description
     */
    public void setDescription(String description)
    {
        _description = description;
    }

    /**
     * Get Count
     * @return Count
     */
    public int getCount()
    {
        return _count;
    }

    /**
     * Set Count
     * @param count - Count
     */
    public void setCount(int count)
    {
        _count = count;
    }

    /**
     * Get Date
     * @return Date
     */
    public Date getDate()
    {
        return _date;
    }

    /**
     * Set Date
     * @param date - Date
     */
    public void setDate(Date date)
    {
        _date = date;
    }

    /**
     * Get Flag
     * @return Flag
     */
    public boolean getFlag()
    {
        return _flag;
    }

    /**
     * Set Flag
     * @param flag - Flag
     */
    public void setFlag(boolean flag)
    {
        _flag = flag;
    }
}
