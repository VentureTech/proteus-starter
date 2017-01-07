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

package com.example.app.support.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * {@link Collector} implementation for collecting a stream to an array
 *
 * @param <I> the stream type
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/11/16 10:21 AM
 */
public class ArrayCollector<I> implements Collector<I, List<I>, I[]>
{
    private final Class<I> _clazz;

    /**
     * Instantiates a new instance of ArrayCollector
     *
     * @param clazz the Array Type
     */
    public ArrayCollector(Class<I> clazz)
    {
        _clazz = clazz;
    }

    @Override
    public Supplier<List<I>> supplier()
    {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<I>, I> accumulator()
    {
        return List::add;
    }

    @Override
    public BinaryOperator<List<I>> combiner()
    {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Function<List<I>, I[]> finisher()
    {
        return list -> {
            I[] arr = (I[]) Array.newInstance(_clazz, list.size());
            for (int i = 0; i < list.size(); i++)
            {
                arr[i] = list.get(i);
            }
            return arr;
        };
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return Collections.emptySet();
    }
}
