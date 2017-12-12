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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.ButtonGroup;
import net.proteusframework.ui.miwt.component.AbstractButton;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.util.ComponentTreeIterator;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SimpleConstraint;

/**
 * {@link SimpleConstraint} implementation that uses a {@link Checkbox}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class CheckboxConstraint extends SimpleConstraint
{
    private Object _expectedValue = true;
    private ButtonGroup _buttonGroup;

    /** Override getLabel and always return null.  Label will be set on the constraint component if it exists. */
    @Override
    public TextSource getLabel()
    {
        return null;
    }

    @Override
    public Component getConstraintComponent()
    {
        Checkbox ch = new Checkbox(getCheckboxLabel());
        ch.addClassName("checkbox-constrsint");
        if (getButtonGroup() != null)
        {
            getButtonGroup().add(ch);
        }
        return ch;
    }

    /**
     * Get the label to be used on the checkbox
     *
     * @return the label
     */
    @Nullable
    public TextSource getCheckboxLabel()
    {
        return super.getLabel();
    }

    /**
     * Get the ButtonGroup that this constraint is attached to
     *
     * @return button group
     */
    public ButtonGroup getButtonGroup()
    {
        return _buttonGroup;
    }

    /**
     * Set the ButtonGroup that this constraint is attached to
     *
     * @param buttonGroup button group
     */
    public void setButtonGroup(ButtonGroup buttonGroup)
    {
        _buttonGroup = buttonGroup;
    }

    @Override
    public Object getValue(Component constraintComponent)
    {
        if (constraintComponent instanceof Checkbox)
        {
            return ((Checkbox) constraintComponent).isSelected();
        }
        else return Optional.ofNullable(findCheckbox(constraintComponent)).map(AbstractButton::isSelected).orElse(false);
    }

    @Override
    public void setValue(Component constraintComponent, Object value)
    {
        Boolean val = value != null && Objects.equals(value, getExpectedValue());
        if (constraintComponent instanceof Checkbox)
            ((Checkbox) constraintComponent).setSelected(val);
        else Optional.ofNullable(findCheckbox(constraintComponent)).ifPresent(ch -> ch.setSelected(val));
    }

    /**
     * Get the expected value for this CheckboxConstraint
     * Defaults to Boolean.TRUE if it is not set.
     *
     * @return expected value
     */
    public Object getExpectedValue()
    {
        return _expectedValue;
    }

    /**
     * Set the expected value for this CheckboxConstraint
     * Defaults to Boolean.TRUE if it is not set.
     *
     * @param expectedValue expected value
     */
    public void setExpectedValue(Object expectedValue)
    {
        _expectedValue = expectedValue;
    }

    /**
     * Find the Checkbox from the given Component by iterating over the component tree
     *
     * @param component the Component
     *
     * @return checkbox or null
     */
    @Nullable
    public static Checkbox findCheckbox(Component component)
    {
        AtomicReference<Checkbox> checkbox = new AtomicReference<>(null);
        ComponentTreeIterator it = new ComponentTreeIterator(component);
        it.forEachRemaining(com -> {
            if (checkbox.get() == null)
            {
                if (com instanceof Checkbox)
                {
                    checkbox.set((Checkbox) com);
                }
            }
        });
        return checkbox.get();
    }

    @Override
    public void addCriteria(QLBuilder builder, Component constraintComponent)
    {
        Object val = getValue(constraintComponent);
        if (shouldReturnConstraintForValue(val))
        {
            Operator operator = getOperator();
            if (operator == null) operator = Operator.eq;
            operator.addCriteria(builder, this, getExpectedValue());
        }
    }

    @Override
    protected boolean shouldReturnConstraintForValue(@Nullable Object value)
    {
        return value instanceof Boolean && (Boolean) value;
    }

    @Override
    public void reset(Component constraintComponent)
    {
        if (constraintComponent instanceof Checkbox)
            ((Checkbox) constraintComponent).setSelected(false);
        else Optional.ofNullable(findCheckbox(constraintComponent)).ifPresent(ch -> ch.setSelected(false));
    }

    /**
     * Set the ButtonGroup that this constraint is attached to
     *
     * @param buttonGroup button group
     *
     * @return this
     */
    public CheckboxConstraint withButtonGroup(ButtonGroup buttonGroup)
    {
        setButtonGroup(buttonGroup);
        return this;
    }

    /**
     * Set the expected value for this CheckboxConstraint
     * Defaults to Boolean.TRUE if it is not set.
     *
     * @param expectedValue expected value
     *
     * @return this
     */
    public CheckboxConstraint withExpectedValue(Object expectedValue)
    {
        setExpectedValue(expectedValue);
        return this;
    }
}
