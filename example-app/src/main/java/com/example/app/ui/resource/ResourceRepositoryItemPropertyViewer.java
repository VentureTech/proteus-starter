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

package com.example.app.ui.resource;

import com.example.app.model.repository.Repository;
import com.example.app.model.repository.RepositoryDAO;
import com.example.app.model.repository.ResourceRepositoryItem;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.terminology.ProfileTermProvider;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.URLConfigurations;
import com.example.app.ui.URLProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TimeZone;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyViewer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.UIText.ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS_FMT;
import static com.example.app.ui.resource.PublicResourceListingLOK.COMPONENT_NAME;
import static com.example.app.ui.resource.ResourceRepositoryItemPropertyViewerLOK.ERROR_UNABLE_TO_FIND_RESOURCE_FMT;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.core.notification.NotificationImpl.error;

/**
 * {@link PropertyViewer} for {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 3:38 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.resource.ResourceRepositoryItemPropertyViewer",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Resource Repository Item Viewer")),
        @I18N(symbol = "Error Unable To Find Resource FMT", l10n = @L10N("Unable to find {0}.")),
        @I18N(symbol = "Error Message Insufficient Permissions FMT",
            l10n = @L10N("You do not have the correct roles to view this {0}."))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.ResourceRepositoryItem.VIEW,
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.ResourceRepositoryItem.VIEW,
        properties = {
            @URLProperty(name = URLProperties.REPOSITORY_ITEM, type = ResourceRepositoryItem.class)
        },
        pathInfoPattern = "/{" + URLProperties.REPOSITORY_ITEM + '}'
    )
)
public class ResourceRepositoryItemPropertyViewer extends MIWTPageElementModelPropertyViewer
{
    private final MessageContainer _messages = new MessageContainer(35_000L);
    @Autowired
    private RepositoryDAO _repositoryDAO;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private ProfileTermProvider _terms;
    private boolean _canEdit;

    /**
     * Instantiate a new instance of ResourceRepositoryItemPropertyViewer
     */
    public ResourceRepositoryItemPropertyViewer()
    {
        super();

        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("resource-repo-item-viewer");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        NavigationAction editAction = CommonActions.EDIT.navAction();
        editAction.configure().toPage(ApplicationFunctions.ResourceRepositoryItem.EDIT)
            .usingCurrentURLData().withSourceComponent(this);
        editAction.setTarget(this, "close");

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toReturnPath(ApplicationFunctions.ResourceRepositoryItem.MANAGEMENT)
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

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        ResourceRepositoryItem value = request.getPropertyValue(URLProperties.REPOSITORY_ITEM);

        if (value == null)
            _messages.sendNotification(error(createText(ERROR_UNABLE_TO_FIND_RESOURCE_FMT(), _terms.resource())));
        else
        {
            final TimeZone timeZone = Event.getRequest().getTimeZone();
            User currentUser = _userDAO.getAssertedCurrentUser();
            Repository repo = _repositoryDAO.getOwnerOfRepositoryItem(value);
            if (_repositoryDAO.canOperate(currentUser, repo, timeZone, _mop.viewRepositoryResources()))
            {
                _canEdit = _repositoryDAO.canOperate(currentUser, repo, timeZone, _mop.modifyRepositoryResources());
                setValueViewer(new ResourceRepositoryItemValueViewer(value));
                return;
            }
            else
                _messages.sendNotification(error(createText(ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS_FMT(), _terms.resource())));
        }
        setValueViewer(null);
    }
}
