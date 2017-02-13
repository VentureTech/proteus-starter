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

package com.example.app.profile.ui.resource;

import com.example.app.profile.model.repository.Repository;
import com.example.app.profile.model.repository.RepositoryDAO;
import com.example.app.profile.model.repository.ResourceRepositoryItem;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.profile.ui.repository.ResourceRepositoryItemValueViewer;
import com.example.app.support.service.ApplicationFunctionPermissionCheck;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.TimeZone;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyViewer;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * {@link PropertyViewer} for {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 3:38 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.resource.AbstractProfileResourcePropertyViewer",
    i18n = {
        @I18N(symbol = "Error Unable To Find Resource FMT", l10n = @L10N("Unable to find {0}.")),
        @I18N(symbol = "Error Message Insufficient Permissions FMT",
            l10n = @L10N("You do not have the correct roles to view this {0}."))
    }
)
public abstract class AbstractProfileResourcePropertyViewer extends MIWTPageElementModelPropertyViewer
{
    private final MessageContainer _messages = new MessageContainer(35_000L);

    @Autowired protected RepositoryDAO _repositoryDAO;
    @Autowired protected UserDAO _userDAO;
    @Autowired protected MembershipOperationProvider _mop;

    private boolean _canEdit;

    /**
     * Instantiate a new instance of AbstractProfileResourcePropertyViewer
     */
    public AbstractProfileResourcePropertyViewer()
    {
        super();

        addClassName("resource-repo-item-viewer");
        setHTMLElement(HTMLElement.section);
    }

    /**
     * Gets management application function.
     *
     * @return the management application function
     */
    protected abstract String getManagementApplicationFunction();

    /**
     * Gets editor application function.
     *
     * @return the editor application function
     */
    protected abstract String getEditorApplicationFunction();

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        NavigationAction editAction = CommonActions.EDIT.navAction();
        editAction.configure().toPage(getEditorApplicationFunction())
            .usingCurrentURLData().withSourceComponent(this);
        editAction.setTarget(this, "close");

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toReturnPath(getManagementApplicationFunction())
            .usingCurrentURLData().withSourceComponent(this);

        if (_canEdit)
        {
            setPersistenceActions(editAction, backAction);
        }
        else
        {
            setPersistenceActions(backAction);
        }

        moveToTop(_messages);
    }

    /**
     * Configure this component
     *
     * @param value the value
     * @param permissionCheck the permission check
     */
    protected void configure(@Nullable ResourceRepositoryItem value, ApplicationFunctionPermissionCheck permissionCheck)
    {
        if(value == null)
            throw new IllegalArgumentException("Unable to determine Resource to view.");
        permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Invalid permissions to view page.");

        final TimeZone timeZone = Event.getRequest().getTimeZone();
        User currentUser = _userDAO.getAssertedCurrentUser();
        Repository repo = _repositoryDAO.getOwnerOfRepositoryItem(value);
        _canEdit = _repositoryDAO.canOperate(currentUser, repo, timeZone, _mop.modifyRepositoryResources());

        setValueViewer(new ResourceRepositoryItemValueViewer(value));
    }
}
