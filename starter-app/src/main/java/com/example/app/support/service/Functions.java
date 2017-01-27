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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import net.proteusframework.core.Pair;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;

/**
 * Class Containing Functional Interfaces that work similarly to {@link Function}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7 /14/16 1:56 PM
 */
public class Functions
{
    /**
     * Functional Interface that works like {@link BiFunction} only it takes an additional argument
     *
     * @param <A> the type parameter
     * @param <B> the type parameter
     * @param <C> the type parameter
     * @param <D> the type parameter
     *
     * @author Alan Holt (aholt@venturetech.net)
     * @since 7 /14/16 1:56 PM
     */
    @FunctionalInterface
    public interface TriFunction<A,B,C,D>
    {
        /**
         * Apply d.
         *
         * @param a the a
         * @param b the b
         * @param c the c
         *
         * @return the d
         */
        D apply(A a, B b, C c);
    }

    /**
     * Functional Interface that works like {@link BiFunction} only it takes two additional arguments
     *
     * @param <A> the type parameter
     * @param <B> the type parameter
     * @param <C> the type parameter
     * @param <D> the type parameter
     * @param <E> the type parameter
     *
     * @author Alan Holt (aholt@venturetech.net)
     * @since 7 /14/16 1:56 PM
     */
    @FunctionalInterface
    public interface QuadFunction<A,B,C,D,E>
    {
        /**
         * Apply e.
         *
         * @param a the a
         * @param b the b
         * @param c the c
         * @param d the d
         *
         * @return the e
         */
        E apply(A a, B b, C c, D d);
    }

    /**
     * Functional Interface that works like {@link Function} but takes any number of arguments.
     * The implementation is responsible for checking and casting the arguments to the right type.
     *
     * @param <A> the return type
     *
     * @author Alan Holt (aholt@venturetech.net)
     * @since 8/11/16 12:33 PM
     */
    @FunctionalInterface
    public interface VarArgFunction<A>
    {
        /**
         * Apply a.
         *
         * @param args the args
         *
         * @return the a
         */
        A apply(Object... args);
    }

    /**
     * Shorthand function for {@link Optional#ofNullable(Object)}
     * <br><br>
     * Reasoning:  {@link Optional#ofNullable(Object)} is a stupid long method name for something as simple as creating an Optional.
     * <br>
     * Also, static importing Optional will cause collisions with
     * {@link Optional#of(Object)} and {@link Container#of(Component...)}, so just static import this method and enjoy having to
     * type less letters to do the same thing!
     *
     * @param <V> the value type
     * @param value the value
     *
     * @return an Optional
     */
    public static <V> Optional<V> opt(@Nullable V value)
    {
        return Optional.ofNullable(value);
    }

    /**
     * Run conditionally.
     *
     * @param condition the condition
     * @param ifTrue the if true
     * @param ifFalse the if false
     *
     * @return the boolean
     */
    public static Boolean runConditionally(Supplier<Boolean> condition, @Nullable Runnable ifTrue, @Nullable Runnable ifFalse)
    {
        if(ifTrue == null) ifTrue = () -> {};
        if(ifFalse == null) ifFalse = () -> {};
        Boolean result;
        if(result = condition.get()) ifTrue.run();
        else ifFalse.run();
        return result;
    }

    /**
     * Run conditionally.
     *
     * @param condition the condition
     * @param ifTrue the if true
     * @param ifFalse the if false
     *
     * @return the boolean
     */
    public static Boolean runConditionally(Boolean condition, @Nullable Runnable ifTrue, @Nullable Runnable ifFalse)
    {
        return runConditionally(() -> condition, ifTrue, ifFalse);
    }

    /**
     * Check if the given Optional has a value present.
     * If value is present, invoke the given Consumer on the value.
     * If value is not present, invoke the given Runnable
     *
     * @param optional the Optional
     * @param ifPresent consumer to invoke if value is present
     * @param orElse runnable to invoke if value is not present
     * @param <A> the optional value type
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <A> void ifPresentOrElse(Optional<A> optional, Consumer<A> ifPresent, Runnable orElse)
    {
        if(optional.isPresent()) optional.ifPresent(ifPresent);
        else orElse.run();
    }

    /**
     * Returns the Enum Value of the given string, or an empty optional.
     * This wraps {@link Enum#valueOf(Class, String)} in a try-catch.
     *
     * @param <T> the enum type
     * @param enumClass the enum class
     * @param value the value
     * @return the value, or empty
     */
    public static <T extends Enum<T>> Optional<T> enumValueOf(@Nonnull Class<T> enumClass, @Nonnull String value)
    {
        try
        {
            return Optional.of(Enum.valueOf(enumClass, value));
        }
        catch(IllegalArgumentException e)
        {
            return Optional.empty();
        }
    }

    /**
     * Checks if the given Optional has a value present.
     * If a value is present, returns the given Optional, otherwise, returns the result of the given Supplier.
     *
     * @param <A> the type parameter
     * @param optional the optional
     * @param orElse the or else
     * @return the optional
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <A> Optional<A> orElseFlatMap(Optional<A> optional, Supplier<Optional<A>> orElse)
    {
        if(optional.isPresent()) return optional;
        else return orElse.get();
    }

    /**
     * Run the given function on the given object, returning the resulting object.
     *
     * @param <A> the type parameter
     * @param obj the obj
     * @param withFunction the with function
     * @return the a
     */
    public static <A> A with(A obj, Function<A, A> withFunction)
    {
        return withFunction.apply(obj);
    }

    /**
     * Get the lesser of the two Comparable objects,
     * using {@link Comparator#nullsLast(Comparator)} and {@link Comparator#naturalOrder()}.
     *
     * @param <A> the type parameter
     * @param a the first value to compare
     * @param b the second value to compare
     *
     * @return the lesser, or null if both values are null
     */
    @Nullable
    public static <A extends Comparable<A>> A getLesser(@Nullable A a, @Nullable A b)
    {
        if(Objects.compare(a, b, Comparator.nullsLast(Comparator.naturalOrder())) <= 0)
            return a;
        else return b;
    }

    /**
     * Get the greater of the two Comparable objects,
     * using {@link Comparator#nullsLast(Comparator)} and {@link Comparator#naturalOrder()}.
     *
     * @param <A> the type parameter
     * @param a the first value to compare
     * @param b the second value to compare
     *
     * @return the greater, or null if both values are null
     */
    @Nullable
    public static <A extends Comparable<A>> A getGreater(@Nullable A a, @Nullable A b)
    {
        if(Objects.compare(a, b, Comparator.nullsLast(Comparator.naturalOrder())) >= 0)
            return a;
        else return b;
    }

    /**
     * Get the lesser of the two Comparable objects.
     * <br><br>
     * See {@link #getLesser(Comparable, Comparable)}
     *
     * @param <A> the type parameter
     * @param a the first value to compare
     * @param b the second value to compare
     *
     * @return the lesser, or null if both values are null
     */
    @Nullable
    public static <A extends Comparable<A>> A getLesser(@Nonnull AtomicReference<A> a, @Nullable A b)
    {
        return getLesser(a.get(), b);
    }

    /**
     * Get the greater of the two Comparable objects.
     * <br><br>
     * See {@link #getGreater(Comparable, Comparable)}
     *
     * @param <A> the type parameter
     * @param a the first value to compare
     * @param b the second value to compare
     *
     * @return the greater, or null if both values are null
     */
    @Nullable
    public static <A extends Comparable<A>> A getGreater(@Nonnull AtomicReference<A> a, @Nullable A b)
    {
        return getGreater(a.get(), b);
    }

    /**
     * Sets the lesser of the two objects between the value's internal value, and the value to compare into the AtomicReference.
     * <br><br>
     * See {@link #getLesser(AtomicReference, Comparable)}
     *
     * @param <A> the type parameter
     * @param value the value
     * @param toCompare the to compare
     */
    public static <A extends Comparable<A>> void setLesser(@Nonnull AtomicReference<A> value, @Nullable A toCompare)
    {
        value.set(getLesser(value, toCompare));
    }

    /**
     * Sets the greater of the two objects between the value's internal value, and the value to compare into the AtomicReference.
     * <br><br>
     * See {@link #getGreater(AtomicReference, Comparable)}
     *
     * @param <A> the type parameter
     * @param value the value
     * @param toCompare the to compare
     */
    public static <A extends Comparable<A>> void setGreater(@Nonnull AtomicReference<A> value, @Nullable A toCompare)
    {
        value.set(getGreater(value, toCompare));
    }

    /**
     * {@link Collector} implementation for converting a stream of {@link Pair} into a {@link Multimap}
     *
     * @param <I> The Key Type for the Multimap
     * @param <V> The Value Type for the Multimap
     * @author Alan Holt (aholt@venturetech.net)
     * @since 6/22/16 8:51 AM
     */
    public static class MultiMapCollector<I, V> implements Collector<Pair<I, V>, Multimap<I, V>, Multimap<I, V>>
    {
        private final Supplier<Multimap<I,V>> _supplier;

        /**
         * Instantiates a new Multi map collector.
         */
        public MultiMapCollector()
        {
            this(HashMultimap::create);
        }

        /**
         * Instantiates a new Multi map collector.
         *
         * @param supplier the supplier
         */
        public MultiMapCollector(Supplier<Multimap<I,V>> supplier)
        {
            _supplier = supplier;
        }

        @Override
        public Supplier<Multimap<I, V>> supplier()
        {
            return _supplier;
        }

        @Override
        public BiConsumer<Multimap<I, V>, Pair<I, V>> accumulator()
        {
            return (map, pair) -> map.put(pair.getOne(), pair.getTwo());
        }

        @Override
        public BinaryOperator<Multimap<I, V>> combiner()
        {
            return (left, right) -> { left.putAll(right); return left; };
        }

        @Override
        public Function<Multimap<I, V>, Multimap<I, V>> finisher()
        {
            return (mm) -> mm;
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return Collections.emptySet();
        }

        /**
         * Gets pair creator function to be used within a stream.flatMap method call.
         *
         * @param <I> the type of the original value
         * @param <K> the type of the Pair's first value
         * @param <V> the type of the Pair's second value
         * @param keyFunction the key function
         * @param valueFunction the value function
         *
         * @return the pair creator
         */
        public static <I, K, V> Function<I, Stream<Pair<K, V>>> getPairCreator(
            Function<I, K> keyFunction, Function<I, List<V>> valueFunction)
        {
            return (i) -> valueFunction.apply(i).stream()
                .map(v -> new Pair<>(keyFunction.apply(i), v));
        }
    }

    /**
     * {@link Collector} implementation for collecting a stream of Entity IDs
     *
     * @param <I> the type of the entity Id
     * @author Alan Holt (aholt@venturetech.net)
     * @since 4/5/16 3:20 PM
     */
    public static class EntityIdCollector<I extends Number> implements Collector<I, List<I>, List<I>>
    {
        private final Supplier<I> _zeroValueSupplier;

        /**
         *   Instantiates a new EntityIdCollector
         *   @param zeroValueSupplier Supplier for the zero value of the ID type
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
            return (left, right) -> { left.addAll(right); return left; };
        }

        @Override
        public Function<List<I>, List<I>> finisher()
        {
            return (list) -> {
                if(list.isEmpty())
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

}


