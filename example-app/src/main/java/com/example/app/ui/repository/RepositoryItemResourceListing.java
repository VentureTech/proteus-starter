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

package com.example.app.ui.repository;


import com.example.app.model.repository.RepositoryDAO;
import com.example.app.model.repository.RepositoryItem;
import com.example.app.model.repository.ResourceRepositoryItem;
import com.example.app.model.resource.Resource;
import com.example.app.model.terminology.ProfileTermProvider;
import com.example.app.support.AppUtil;

import com.example.app.ui.resource.ResourceRepositoryItemValueViewer;
import com.example.app.ui.resource.ResourceSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.search.SearchUIAction;

import static com.example.app.ui.UIText.RESOURCES;
import static com.example.app.ui.repository.RepositoryItemResourceListingLOK.BUTTON_TEXT_SELECT_RESOURCES_FMT;
import static com.example.app.ui.repository.RepositoryItemResourceListingLOK.LABEL_SELECT_RESOURCES_FMT;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Provides a UI for listing Resources owned by a RepositoryItem
 * Implementations of this UI should be {@link Configurable} or a {@link Component}
 *
 * @param <RI> the RepositoryItem subclass whose Resources we are managing
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/30/15 9:13 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.repository.RepositoryItemResourceListing",
    i18n = {
        @I18N(symbol = "Button Text Select Resources FMT", l10n = @L10N("Select {0}")),
        @I18N(symbol = "Label Select Resources FMT", l10n = @L10N("Select {0}")),
        @I18N(symbol = "Button Text Done", l10n = @L10N("Done"))
    }
)
public abstract class RepositoryItemResourceListing<RI extends RepositoryItem> extends Container
{
    private final RI _value;
    private final boolean _canEdit;
    private final Container _listing = of("resource-listing");
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private RepositoryDAO _repositoryDAO;
    @Autowired
    private ProfileTermProvider _terms;

    /**
     * Instantiates a new instance of RepositoryItemResourceListing
     *
     * @param value the RepositoryItem to list the resources for
     * @param canEdit boolean flag -- if true, allows adding or removing resources to the RepositoryItem
     */
    public RepositoryItemResourceListing(@Nonnull RI value, boolean canEdit)
    {
        _value = value;
        _canEdit = canEdit;

        addClassName("resource-mgt search clearfix");
    }

    @Override
    public void init()
    {
        super.init();

        PushButton selectResources = new PushButton(BUTTON_TEXT_SELECT_RESOURCES_FMT(RESOURCES()));
        selectResources.addActionListener(ev -> beginResourceSelection());
        selectResources.addClassName("select-resources");

        populateListing();

        if (_canEdit)
        {
            add(of("actions entity-actions", selectResources));
        }
        add(_listing);
    }

    /**
     * Add the Resource to the given Value
     *
     * @param resource the Resource to add
     * @param value the RepositoryItem to add the Resource to
     *
     * @return the Value with the Resource added to it.
     */
    protected abstract RI addResourceToValue(Resource resource, RI value);

    /**
     * Ensures that properties on the given Value are populated as they should be
     *
     * @param value the Value
     *
     * @return the Value, with any properties populated that should be populated
     */
    protected abstract RI ensureValueState(RI value);

    /**
     * Get the Resources from the given Value
     *
     * @param value the RepositoryItem to retrieve the Resources from
     *
     * @return the Resources for the RepositoryItem
     */
    protected abstract List<Resource> getResourcesFromValue(RI value);

    /**
     * Get the RepositoryItem whose Resources we are managing
     *
     * @return the RepositoryItem
     */
    @Nonnull
    protected RI getValue()
    {
        return ensureValueState(_er.reattachIfNecessary(_value));
    }

    /**
     * Remove the Resource from the given Value
     *
     * @param resource the Resource to remove
     * @param value the RepositoryItem to remove the Resource from
     *
     * @return the Value with the Resource removed from it
     */
    protected abstract RI removeResourceFromValue(Resource resource, RI value);

    /**
     * Set the Resources on the given Value
     *
     * @param resources the Resources to set on the Value
     * @param value the RepositoryItem to set the Resources on
     *
     * @return the Value with the Resources set on it.
     */
    protected abstract RI setResourcesOnValue(List<Resource> resources, RI value);

    @SuppressWarnings("ConstantConditions")
    private void beginResourceSelection()
    {
        final Dialog dlg = new Dialog(getApplication(), LABEL_SELECT_RESOURCES_FMT(RESOURCES()));
        dlg.addClassName("resource-selector-dialog");

        ResourceSelector selector = new ResourceSelector(_repositoryDAO.getOwnerOfRepositoryItem(getValue()),
            getResourcesFromValue(getValue()));

        selector.setOnSelect(context -> {
            ResourceRepositoryItem rri = context.getData();
            if (rri != null)
            {
                RI val = addResourceToValue(rri.getResource(), getValue());
                _repositoryDAO.mergeRepositoryItem(val);
                selector.setSelection(getResourcesFromValue(getValue()));
                populateListing();
                context.getSearchUI().doAction(SearchUIAction.search);
            }
            return null;
        });

        dlg.add(of(HTMLElement.section, "repo-item resource-selector", selector));

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }

    private void populateListing()
    {
        _listing.removeAllComponents();
        getResourcesFromValue(getValue()).forEach(r -> {
            Optional<ResourceRepositoryItem> repoItem = _repositoryDAO.getRepoItemForResource(r);
            if (repoItem.isPresent())
            {
                ResourceRepositoryItemValueViewer viewer = new ResourceRepositoryItemValueViewer(repoItem.get());
                PushButton remove = CommonActions.REMOVE.push();
                AppUtil.enableTooltip(remove);
                remove.addActionListener(ev -> {
                    RI val = removeResourceFromValue(repoItem.get().getResource(), getValue());
                    _repositoryDAO.mergeRepositoryItem(val);
                    populateListing();
                });

                _listing.add(of("resource", of("actions persistence-actions", remove), viewer));
            }
        });
    }
}
