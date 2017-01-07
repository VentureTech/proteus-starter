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

package com.example.app.profile.ui.user;


import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.model.user.UserPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * {@link PropertyEditor} implementation for {@link UserPosition}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/15/15 4:34 PM
 */
@Configurable
public class UserPositionPropertyEditor extends PropertyEditor<UserPosition>
{
    private final ActionListener _closeActionListener;
    @Autowired
    private UserDAO _userDAO;

    /**
     * Instantiate a new instance of UserPositionPropertyEditor
     *
     * @param user the User for which the edited UserPosition will belong
     * @param closeActionListener actionListener responsible for closing the UI for this PropertyEditor
     */
    public UserPositionPropertyEditor(@Nonnull User user, @Nonnull ActionListener closeActionListener)
    {
        super();
        setValueEditor(new UserPositionValueEditor(user));
        _closeActionListener = closeActionListener;
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public void init()
    {
        super.init();

        ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
        saveAction.setActionListener(ev -> {
            if (persist(input -> {
                if (input != null)
                {
                    return _userDAO.mergeUserPosition(input) != null;
                }
                return false;
            }))
            {
                _closeActionListener.actionPerformed(ev);
            }
        });

        ReflectiveAction cancelAction = CommonActions.CANCEL.defaultAction();
        cancelAction.setActionListener(_closeActionListener);

        setPersistenceActions(saveAction, cancelAction);
    }

    /**
     * Set the value on the value editor
     *
     * @param value the UserPosition to use as the value
     */
    public void setValue(UserPosition value)
    {
        getValueEditor().setValue(value);
    }
}
