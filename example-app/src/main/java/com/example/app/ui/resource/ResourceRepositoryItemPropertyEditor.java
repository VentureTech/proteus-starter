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

import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.repository.Repository;
import com.example.app.model.repository.RepositoryDAO;
import com.example.app.model.repository.RepositoryItemRelation;
import com.example.app.model.repository.RepositoryItemRelationType;
import com.example.app.model.repository.ResourceRepositoryItem;
import com.example.app.model.resource.ResourceDAO;
import com.example.app.model.resource.ResourceType;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.URLConfigurations;
import com.example.app.ui.URLProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.management.nav.config.CurrentURLPropertyValueResolver;
import net.proteusframework.ui.management.nav.config.PropertyValueResolverParameter;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.resource.ResourceRepositoryItemPropertyEditorLOK.COMPONENT_NAME;

/**
 * {@link PropertyEditor} for {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 11:14 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.resource.ResourceRepositoryItemPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Resource Repository Item Editor")),
        @I18N(symbol = "Error Message Insufficient Permissions FMT",
            l10n = @L10N("You do not have the correct roles to modify this {0}."))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.ResourceRepositoryItem.EDIT,
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.ResourceRepositoryItem.EDIT,
        properties = {
            @URLProperty(name = URLProperties.REPOSITORY_ITEM, type = ResourceRepositoryItem.class),
            @URLProperty(name = URLProperties.RESOURCE_TYPE, type = ResourceType.class,
                converter = ResourceTypeURLConfigPropertyConverter.class),
            @URLProperty(name = URLProperties.REPOSITORY, type = Repository.class),
            @URLProperty(name = URLProperties.REPOSITORY_OWNER, type = Profile.class)
        },
        pathInfoPattern = "/{" + URLProperties.REPOSITORY_ITEM + "}/{" + URLProperties.RESOURCE_TYPE + "}/{"
                          + URLProperties.REPOSITORY + "}/{" + URLProperties.REPOSITORY_OWNER + '}'
    )
)
public class ResourceRepositoryItemPropertyEditor extends MIWTPageElementModelPropertyEditor<ResourceRepositoryItem>
{
    @Autowired
    private RepositoryDAO _repositoryDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private ResourceDAO _resourceDAO;
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private SelectedCompanyTermProvider _terms;

    private boolean _canEdit;
    private ResourceRepositoryItem _saved;
    private RepositoryItemRelation _relation;
    private User _currentUser;

    /**
     * Instantiate a new instance of ResourceRepositoryItemPropertyEditor
     */
    public ResourceRepositoryItemPropertyEditor()
    {
        super(new ResourceRepositoryItemValueEditor());

        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("resource-repo-item-editor");
        setHTMLElement(HTMLElement.section);
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        ResourceRepositoryItem value = request.getPropertyValue(URLProperties.REPOSITORY_ITEM);
        ResourceType resourceType = request.getPropertyValue(URLProperties.RESOURCE_TYPE);
        Repository repo = request.getPropertyValue(URLProperties.REPOSITORY);
        Optional<Profile> owner = Optional.ofNullable(request.getPropertyValue(URLProperties.REPOSITORY_OWNER));

        if (value != null && !_repositoryDAO.isTransient(value))
        {
            resourceType = value.getResource().getResourceType();
            owner = _repositoryDAO.getOwnerOfRepository(_repositoryDAO.getOwnerOfRepositoryItem(value));
        }
        assert resourceType != null : "ResourceType was null.  This should not happen unless the URL params are screwed up.";
        getValueEditor().setResourceType(resourceType);
        repo = owner.map(Profile::getRepository).orElse(repo);
        getValueEditor().setOwner(repo);
        final TimeZone tz = Event.getRequest().getTimeZone();
        _currentUser = _userDAO.getAssertedCurrentUser();
        _canEdit =
            owner.map(profile -> _profileDAO.canOperate(_currentUser, profile, tz, _mop.viewRepositoryResources()))
                .orElse(false)
            && owner.map(profile -> _profileDAO.canOperate(_currentUser, profile, tz, _mop.modifyRepositoryResources()))
                .orElse(false);

        final Repository fRepo = repo;
        _relation = value != null && value.getId() != null && value.getId() > 0
            ? _repositoryDAO.getRelation(repo, value).orElse(null)
            : ((Supplier<RepositoryItemRelation>) () -> {
                RepositoryItemRelation relation = new RepositoryItemRelation();
                relation.setRelationType(RepositoryItemRelationType.owned);
                relation.setRepository(fRepo);
                return relation;
            }).get();

        if (_canEdit && _relation != null)
        {
            getValueEditor().setValue(value);
            setSaved(value);
        }
        else
        {
            throw new IllegalArgumentException("Invalid Permissions To View Page");
        }
    }    /**
     * Set the saved ResourceRepositoryItem to be used for constructing URL properties after saving the ResourceRepositoryItem
     *
     * @param saved the persisted ResourceRepositoryItem
     */
    public void setSaved(@Nullable ResourceRepositoryItem saved)
    {
        _saved = _er.narrowProxyIfPossible(saved);
    }

    @Override
    public ResourceRepositoryItemValueEditor getValueEditor()
    {
        return (ResourceRepositoryItemValueEditor) super.getValueEditor();
    }

    @Override
    public void init()
    {
        super.init();

        NavigationAction saveAction = CommonActions.SAVE.navAction();
        saveAction.onCondition(input ->
            persist(toSave -> {
                assert toSave != null : "ResourceRepositoryItem should not be null if you are persisting!";
                toSave.setResource(_resourceDAO.saveResourceImage(
                    toSave.getResource(), getValueEditor().getPictureEditor().commitValue()));
                _relation.setRepositoryItem(toSave);
                setSaved(_repositoryDAO.mergeRepositoryItemRelation(_relation)
                    .getCastRepositoryItem(ResourceRepositoryItem.class));
                return Boolean.TRUE;
            }));
        saveAction.configure().toReturnPath(ApplicationFunctions.ResourceRepositoryItem.VIEW).withSourceComponent(this);
        saveAction.setPropertyValueResolver(new CurrentURLPropertyValueResolver()
        {
            @Override
            public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
            {
                Map<String, Object> map = super.resolve(parameter);
                map.put(URLProperties.REPOSITORY_ITEM, _saved);
                return map;
            }
        });
        saveAction.setTarget(this, "close");

        NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(ApplicationFunctions.ResourceRepositoryItem.MANAGEMENT)
            .usingCurrentURLData().withSourceComponent(this);
        cancelAction.setTarget(this, "close");

        if (_canEdit)
        {
            setPersistenceActions(saveAction, cancelAction);
        }
        else
        {
            setPersistenceActions(cancelAction);
        }
    }


}
