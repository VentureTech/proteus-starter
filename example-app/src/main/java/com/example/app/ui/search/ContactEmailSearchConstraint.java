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

package com.example.app.ui.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.EnumSet;

import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;

/**
 * Search Constraint for searching for an {@link EmailAddress} within a property that is a {@link Contact} for an entity.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class ContactEmailSearchConstraint extends SimpleConstraint
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ContactEmailSearchConstraint.class);

    private EnumSet<ContactDataCategory> _contactDataCategories = EnumSet.noneOf(ContactDataCategory.class);

    /**
     *   Instantiate a new instance
     *   @param name the constraint name
     *   @param contactProperty the property name for the Contact
     */
    public ContactEmailSearchConstraint(String name, String contactProperty)
    {
        super(name);
        withProperty(contactProperty);
    }

    /**
     *   Specify one or more ContactDataCategories to search for in conjunction with the email address.
     *   If no contact data categories are specified, it simply searches all of them.
     *   @param contactDataCategories the ContactDataCategories to search
     *   @return this
     */
    public ContactEmailSearchConstraint withContactDataCategories(ContactDataCategory... contactDataCategories)
    {
        _contactDataCategories = EnumSet.copyOf(Arrays.asList(contactDataCategories));
        return this;
    }

    @Override
    public void addCriteria(QLBuilder builder, Component constraintComponent)
    {
        Object val = getValue(constraintComponent);
        if(val != null)
        {
            String value = String.valueOf(val);

            if(shouldReturnConstraintForValue(value) && getOperator() != null)
            {
                JoinedQLBuilder contactJoin = builder.createJoin(
                    QLBuilder.JoinType.LEFT, getProperty(), "contactAlias_" + getName());
                JoinedQLBuilder emailJoin = contactJoin.createJoin(
                    QLBuilder.JoinType.LEFT, "emailAddresses", "contactEmailAlias_" + getName());
                emailJoin.appendCriteria("email", getOperator(), value);
                if(!_contactDataCategories.isEmpty())
                {
                    emailJoin.appendCriteria(emailJoin.getAlias() + ".category in (:categories)")
                        .putParameter("categories", _contactDataCategories);
                }
            }
        }
    }
}
