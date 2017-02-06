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

package com.example.app.profile.ui.location;

import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.model.location.LocationDAO;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.support.service.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.core.GloballyUniqueStringGenerator;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * PropertyEditor for Location
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /25/17
 */
@Configurable
public abstract class AbstractLocationPropertyEditor extends MIWTPageElementModelPropertyEditor<Location>
{
    @Autowired protected AppUtil _appUtil;
    @Autowired protected LocationDAO _locationDAO;
    @Autowired protected SelectedCompanyTermProvider _terms;

    private Location _saved;

    /**
     * Instantiates a new Abstract location property editor.
     */
    public AbstractLocationPropertyEditor()
    {
        super();
        setValueEditor(new LocationValueEditor());
    }

    /**
     * Gets save action.
     *
     * @return the save action
     */
    public abstract NavigationAction getSaveAction();

    /**
     * Gets cancel action.
     *
     * @return the cancel action
     */
    public abstract NavigationAction getCancelAction();

    /**
     * Gets post save.
     *
     * @return the post save
     */
    public abstract UnaryOperator<Location> getPostSave();

    /**
     * Gets page title.
     *
     * @return the page title
     */
    public abstract TextSource getPageTitle();

    /**
     * Gets saved.
     *
     * @return the saved
     */
    public Location getSaved()
    {
        return _saved;
    }

    /**
     * Sets saved.
     *
     * @param saved the saved
     */
    public void setSaved(Location saved)
    {
        _saved = saved;
    }

    /**
     * Gets post persist.
     *
     * @return the post persist
     */
    public abstract BiConsumer<Location, NavigationAction> getPostPersist();

    @Override
    public void init()
    {
        super.init();

        final ReflectiveAction save = CommonActions.SAVE.defaultAction();
        save.setActionListener(ev -> {
            if(persist(location -> {
                assert location != null : "Location should not be null if you are persisting!";

                final LocalizedObjectKey locationName = location.getName();
                location.getRepository().setName(_appUtil.copyLocalizedObjectKey(locationName));
                if(location.getProfileType() == null)
                {
                    location.setProfileType(new ProfileType());
                    location.getProfileType().setName(_appUtil.copyLocalizedObjectKey(locationName));
                    location.getProfileType().setProgrammaticIdentifier(GloballyUniqueStringGenerator.getUniqueString());
                }

                setSaved(_locationDAO.saveLocation(location));
                setSaved(getPostSave().apply(getSaved()));
                return Boolean.TRUE;
            }))
            {
                NavigationAction action = getSaveAction();
                getPostPersist().accept(getSaved(), action);
            }
        });

        final ReflectiveAction cancel = CommonActions.CANCEL.defaultAction();
        cancel.setActionListener(ev -> getCancelAction().actionPerformed(ev));

        setPersistenceActions(save, cancel);

        moveToTop(new Label(getPageTitle()).withHTMLElement(HTMLElement.h1).addClassName("page-header"));
    }
}
