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

package com.example.app.support;

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
 * {@link Collector} implementation for collecting a stream of Entity IDs
 *
 * @param <I> the type of the entity Id
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/5/16 3:20 PM
 */
public class EntityIdCollector<I extends Number> implements Collector<I, List<I>, List<I>>
{
    private final Supplier<I> _zeroValueSupplier;

    /**
     * Instantiates a new EntityIdCollector
     *
     * @param zeroValueSupplier Supplier for the zero value of the ID type
     */
    public EntityIdCollector(Supplier<I> zeroValueSupplier)
    {
        _zeroValueSupplier = zeroValueSupplier;
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

    @Override
    public Function<List<I>, List<I>> finisher()
    {
        return (list) -> {
            if (list.isEmpty())
                list.add(_zeroValueSupplier.get());
            return list;
        };
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return Collections.emptySet();
    }
}
