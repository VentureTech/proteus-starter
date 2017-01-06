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

package com.example.app.support.ui.search;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SimpleConstraint;

import static net.proteusframework.core.StringFactory.PATTERN_COMMA;

/**
 * {@link SimpleConstraint} implementation that allows for searching over multiple property values
 * by giving {@link KeywordConstraint#setProperty(String)} a comma-separated string of properties
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class KeywordConstraint extends SimpleConstraint
{
    @Override
    public void addCriteria(QLBuilder builder, Component constraintComponent)
    {
        Object value = getValue(constraintComponent);
        if (shouldReturnConstraintForValue(value))
        {
            String[] properties = PATTERN_COMMA.split(getProperty());
            for (String property : properties)
            {
                SimpleConstraint propConst = new SimpleConstraint(getName() + '-' + property);
                propConst.setProperty(property);
                if (getOperator() == null)
                {
                    setOperator(Operator.like);
                }
                assert getOperator() != null;
                propConst.setOperator(getOperator());
                getOperator().addCriteria(builder, propConst, value);
            }
        }
    }

    /**
     * Set the Properties to search on.
     *
     * @param properties the Properties
     *
     * @return this
     */
    public KeywordConstraint withProperties(String... properties)
    {
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        StringBuilder builder = new StringBuilder();
        Arrays.stream(properties).forEach(property -> {
            builder.append(property);
            if (counter.accumulateAndGet(1, (i1, i2) -> i1 + i2) < properties.length)
            {
                builder.append(',');
            }
        });
        setProperty(builder.toString());
        return this;
    }
}
