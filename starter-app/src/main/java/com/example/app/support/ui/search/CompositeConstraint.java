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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.html.ContextImpl;
import net.proteusframework.core.html.Element;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SearchConstraint;

/**
 * {@link SearchConstraint} implementation that allows for grouping multiple search constraints into the same wrapping container.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class CompositeConstraint implements SearchConstraint
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(CompositeConstraint.class);

    /**
     * Composite Constraint component
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class CompositeConstraintComponent extends Container
    {
        private final Map<SearchConstraint, ConstraintComponentMapping> _constraints = new HashMap<>();

        /**
         * Add the given SearchConstraint into this component
         *
         * @param constraint the SearchConstraint
         */
        public void add(SearchConstraint constraint)
        {
            if (_constraints.containsKey(constraint))
            {
                Container current = getWrappingContainer(constraint);
                if (current != null) remove(current);
            }
            Component com = constraint.getConstraintComponent();
            Container con = null;
            Label label = null;
            if (com != null)
            {
                con = of("composite-constraint-child constraint");
                if (!StringFactory.isEmptyString(constraint.getHTMLClass()))
                    con.addClassName(constraint.getHTMLClass());
                if (constraint.getLabel() != null)
                {
                    con.add(label = new Label(constraint.getLabel()));
                }
                Element element = com.getHTMLElement();
                if (element.isValidParent(ContextImpl.of(element, HTMLElement.span))) con.withHTMLElement(HTMLElement.span);
                con.add(com);
                add(con);
            }
            ConstraintComponentMapping mapping = new ConstraintComponentMapping();
            mapping.setComponent(com);
            mapping.setContainer(con);
            mapping.setLabel(label);
            _constraints.put(constraint, mapping);
            setToSpanIfPossible();
        }

        /**
         * Get the wrapping container for the given SearchConstraint, if one exists
         *
         * @param constraint the SearchConstraint
         *
         * @return wrapping container
         */
        @Nullable
        public Container getWrappingContainer(SearchConstraint constraint)
        {
            return Optional.ofNullable(_constraints.get(constraint)).map(ConstraintComponentMapping::getContainer).orElse(null);
        }

        @Override
        public Container add(Component com)
        {
            Container con = super.add(com);
            setToSpanIfPossible();
            return con;
        }

        private void setToSpanIfPossible()
        {
            AtomicReference<Boolean> canSetToSpan = new AtomicReference<>(true);
            components().forEachRemaining(com -> {
                Element element = com.getHTMLElement();
                canSetToSpan.accumulateAndGet(element.isValidParent(ContextImpl.of(element, HTMLElement.span)),
                    (b1, b2) -> b1 && b2);
            });
            if (canSetToSpan.get())
                setHTMLElement(HTMLElement.span);
            else
                setHTMLElement(HTMLElement.div);
        }

        /**
         * Get the constraint component for the given SearchConstraint, if one exists
         *
         * @param constraint the SearchConstraint
         *
         * @return constraint component
         */
        @Nullable
        public Component getConstraintComponent(SearchConstraint constraint)
        {
            return Optional.ofNullable(_constraints.get(constraint)).map(ConstraintComponentMapping::getComponent).orElse(null);
        }

        /**
         * Get the label component for the given SearchConstraint, if one exists
         *
         * @param constraint the SearchConstraint
         *
         * @return label component
         */
        @Nullable
        public Label getLabel(SearchConstraint constraint)
        {
            return Optional.ofNullable(_constraints.get(constraint)).map(ConstraintComponentMapping::getLabel).orElse(null);
        }

        /**
         * Check if the given SearchConstraint is contained within this CompositeConstraintComponent
         *
         * @param constraint the SearchConstraint
         *
         * @return boolean flag -- true if this CompositeConstraintComponent contains the given SearchConstraint
         */
        public boolean hasConstraint(SearchConstraint constraint)
        {
            return _constraints.containsKey(constraint);
        }
    }

    /**
     * Mapping class to store a constraint component, its wrapping container, and its label.
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class ConstraintComponentMapping
    {
        private Container _con;
        private Label _label;
        private Component _com;

        /**
         * Get the constraint component for this mapping
         *
         * @return component
         */
        @Nullable
        public Component getComponent()
        {
            return _com;
        }

        /**
         * Set the constraint component for this mapping
         *
         * @param com component
         */
        public void setComponent(@Nullable Component com)
        {
            _com = com;
        }

        /**
         * Get the container for this mapping
         *
         * @return container
         */
        @Nullable
        public Container getContainer()
        {
            return _con;
        }

        /**
         * Set the container for this mapping
         *
         * @param con container
         */
        public void setContainer(@Nullable Container con)
        {
            _con = con;
        }

        /**
         * Get the label for this mapping
         *
         * @return label
         */
        @Nullable
        public Label getLabel()
        {
            return _label;
        }

        /**
         * Set the label for this mapping
         *
         * @param label label
         */
        public void setLabel(@Nullable Label label)
        {
            _label = label;
        }
    }
    private final List<SearchConstraint> _constraints = new ArrayList<>();
    private String _name;
    private String _HTMLClass;
    private TextSource _label;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public SearchConstraint clone()
    {
        CompositeConstraint cc = new CompositeConstraint();
        cc.setName(getName());
        cc.setHTMLClass(getHTMLClass());
        getConstraints().forEach(cc::addConstraint);
        return cc;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    /**
     * Set the Name for this search constraint
     *
     * @param name the Name
     */
    public void setName(String name)
    {
        _name = name;
    }

    @Nullable
    @Override
    public byte[] encodeValueForPersistence(Component constraintComponent)
    {
        if (constraintComponent instanceof CompositeConstraintComponent)
        {
            CompositeConstraintComponent ccc = (CompositeConstraintComponent) constraintComponent;
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 final ObjectOutputStream oos = new ObjectOutputStream(baos))
            {
                List<SearchConstraint> constraints = getConstraints().stream()
                    .filter(ccc::hasConstraint).collect(Collectors.toList());
                oos.writeByte(constraints.size());
                for (SearchConstraint constraint : constraints)
                {
                    oos.writeObject(constraint);
                    oos.writeObject(constraint.encodeValueForPersistence(ccc.getConstraintComponent(constraint)));
                }
                return baos.toByteArray();
            }
            catch (Exception e)
            {
                _logger.error("Unable to encode value for CompositeConstraint.", e);
            }
        }
        return null;
    }

    @Override
    public void restoreValueForPersistence(Component constraintComponent, byte[] encodedValue)
    {
        if (constraintComponent instanceof CompositeConstraintComponent)
        {
            CompositeConstraintComponent ccc = (CompositeConstraintComponent) constraintComponent;
            try (final ByteArrayInputStream bais = new ByteArrayInputStream(encodedValue);
                 final ObjectInputStream ois = new ObjectInputStream(bais))
            {
                final byte countConstraints = ois.readByte();
                for (int i = 0; i < countConstraints; i++)
                {
                    final SearchConstraint constraint = (SearchConstraint) ois.readObject();
                    final byte[] constraintByteArray = (byte[]) ois.readObject();
                    addConstraint(constraint);
                    ccc.add(constraint);
                    constraint.restoreValueForPersistence(ccc.getConstraintComponent(constraint), constraintByteArray);
                }
            }
            catch (Exception e)
            {
                _logger.error("Unable to decode value for CompositeConstraint." + Arrays.toString(encodedValue), e);
            }
        }
    }

    @Nullable
    @Override
    public TextSource getLabel()
    {
        return _label;
    }

    /**
     * Set the Label for this composite constraint
     *
     * @param label the Label
     */
    public void setLabel(@Nullable TextSource label)
    {
        _label = label;
    }

    @Nullable
    @Override
    public String getHTMLClass()
    {
        return _HTMLClass;
    }

    /**
     * Set the html class for this constraint
     *
     * @param htmlClass the html class
     */
    public void setHTMLClass(@Nullable String htmlClass)
    {
        _HTMLClass = htmlClass;
    }

    @Nullable
    @Override
    public Component getConstraintComponent()
    {
        CompositeConstraintComponent ccc = new CompositeConstraintComponent();
        getConstraints().forEach(ccc::add);
        return ccc;
    }

    @Override
    public void reset(Component constraintComponent)
    {
        if (constraintComponent instanceof CompositeConstraintComponent)
        {
            CompositeConstraintComponent ccc = (CompositeConstraintComponent) constraintComponent;
            getConstraints().forEach(constraint -> {
                if (ccc.hasConstraint(constraint))
                    constraint.reset(ccc.getConstraintComponent(constraint));
            });
        }
    }

    @Override
    public boolean hasValue(@Nullable Component constraintComponent)
    {
        if (constraintComponent instanceof CompositeConstraintComponent)
        {
            CompositeConstraintComponent ccc = (CompositeConstraintComponent) constraintComponent;
            AtomicReference<Boolean> hasVal = new AtomicReference<>(false);
            getConstraints().forEach(constraint -> {
                if (ccc.hasConstraint(constraint))
                    hasVal.accumulateAndGet(constraint.hasValue(ccc.getConstraintComponent(constraint)), (b1, b2) -> b1 || b2);
            });
            return hasVal.get();
        }
        else return false;
    }

    @Override
    public void addCriteria(QLBuilder builder, @Nullable Component constraintComponent)
    {
        if (constraintComponent instanceof CompositeConstraintComponent)
        {
            CompositeConstraintComponent ccc = (CompositeConstraintComponent) constraintComponent;
            getConstraints().forEach(constraint -> {
                if (ccc.hasConstraint(constraint))
                    constraint.addCriteria(builder, ccc.getConstraintComponent(constraint));
            });
        }
    }

    /**
     * Add the given SearchConstraint to this CompositeConstraint
     *
     * @param constraint the Constraint to add
     *
     * @return this
     */
    public CompositeConstraint addConstraint(SearchConstraint constraint)
    {
        getConstraints().add(constraint);
        return this;
    }

    /**
     * Get the search constraints that make up this composite constraint
     *
     * @return constraints
     */
    public List<SearchConstraint> getConstraints()
    {
        return _constraints;
    }

    /**
     * Set the html class for this constraint
     *
     * @param htmlClass the html class
     *
     * @return this
     */
    public CompositeConstraint withHTMLClass(@Nullable String htmlClass)
    {
        setHTMLClass(htmlClass);
        return this;
    }

    /**
     * Set the Label for this composite constraint
     *
     * @param label the Label
     *
     * @return this
     */
    public CompositeConstraint withLabel(@Nullable TextSource label)
    {
        setLabel(label);
        return this;
    }

    /**
     * Set the Name for this search constraint
     *
     * @param name the name
     *
     * @return this
     */
    public CompositeConstraint withName(String name)
    {
        setName(name);
        return this;
    }
}
