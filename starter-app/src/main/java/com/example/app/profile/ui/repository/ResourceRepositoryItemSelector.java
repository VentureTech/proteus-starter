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

package com.example.app.profile.ui.repository;

import com.example.app.profile.model.repository.Repository;
import com.example.app.profile.model.repository.RepositoryItemStatus;
import com.example.app.profile.model.repository.ResourceRepositoryItem;
import com.example.app.profile.model.resource.Resource;
import com.example.app.profile.service.resource.ResourceCategoryLabelProvider;
import com.example.app.profile.service.resource.ResourceTagsLabelProvider;
import com.example.app.support.service.EntityIdCollector;
import com.google.common.base.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.i2rd.hibernate.util.HibernateUtil;

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.AbstractPropertyConstraint;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SimpleConstraint;

import static com.example.app.profile.ui.UIText.RESOURCE;
import static com.example.app.profile.ui.repository.ResourceRepositoryItemSelectorLOK.*;
import static com.example.app.support.service.AppUtil.nullFirst;
import static net.proteusframework.core.locale.TextSources.EMPTY;


/**
 * Provides a UI for selecting a ResourceRepositoryItem
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/8/16 4:36 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.repository.ResourceRepositoryItemSelector",
    i18n = {
        @I18N(symbol = "Label Name", l10n = @L10N("Name")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label AUthor", l10n = @L10N("Author")),
        @I18N(symbol = "Label Type", l10n = @L10N("Type")),
        @I18N(symbol = "Column Categories", l10n = @L10N("Categories")),
        @I18N(symbol = "Label Owner", l10n = @L10N("Owner")),
    }
)
@Configurable
public class ResourceRepositoryItemSelector extends RepositoryItemSelector<ResourceRepositoryItem>
{
    private final List<ResourceRepositoryItem> _selectedResources = new ArrayList<>();
    @Autowired
    private ResourceCategoryLabelProvider _rtlp;
    @Autowired
    private ResourceTagsLabelProvider _rclp;
    private final Set<Class<? extends Resource>> _includedResourceTypes = new HashSet<>();
    private final Set<Class<? extends Resource>> _excludedResourceTypes = new HashSet<>();

    /**
     * Instantiates a new instance of ResourceRepositoryItemSelector
     *
     * @param selectedResources the selected resources
     * @param repos the owning repositories to select from
     */
    public ResourceRepositoryItemSelector(@Nullable List<ResourceRepositoryItem> selectedResources, Repository... repos)
    {
        super(repos);

        if (selectedResources != null)
            _selectedResources.addAll(selectedResources);
    }

    /**
     * Set some resource types to exclude from being selectable, if empty then none are excluded.
     *
     * @param excludeResourceTypes the excluded resource types
     *
     * @return this
     */
    public ResourceRepositoryItemSelector withExcludeResourceTypes(@NotNull Set<Class<? extends Resource>> excludeResourceTypes)
    {
        _excludedResourceTypes.clear();
        _excludedResourceTypes.addAll(excludeResourceTypes);
        return this;
    }

    /**
     * Set some resource types to include in being selectable.  If empty, then all our included.
     *
     * @param includeTypes the included resource types
     *
     * @return this
     */
    public ResourceRepositoryItemSelector withIncludeResourceTypes(@NotNull Set<Class<? extends Resource>> includeTypes)
    {
        _includedResourceTypes.clear();
        _includedResourceTypes.addAll(includeTypes);
        return this;
    }

    /**
     * Set the selected resources
     *
     * @param selectedResources the selected resources
     *
     * @return this
     */
    public ResourceRepositoryItemSelector withSelectedResources(@Nullable List<ResourceRepositoryItem> selectedResources)
    {
        _selectedResources.clear();
        if (selectedResources != null)
            _selectedResources.addAll(selectedResources);
        fireRepoItemListModified();
        return this;
    }

    @Override
    protected TextSource getRepoItemTerm()
    {
        return RESOURCE();
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void addSearchConstraints(SearchModelImpl searchModel)
    {
        searchModel.getConstraints().add(new SimpleConstraint("name").withLabel(LABEL_NAME())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        searchModel.getConstraints().add(new ComboBoxConstraint(nullFirst(RepositoryItemStatus.values()), RepositoryItemStatus
            .Active, new CustomCellRenderer(CommonButtonText.ANY))
            .withLabel(CommonColumnText.STATUS)
            .withProperty(ResourceRepositoryItem.STATUS_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.eq));

        ArrayList<Label> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(_rclp.getEnabledLabels(Optional.empty()));
        AbstractPropertyConstraint categoryConstraint = new ComboBoxConstraint(categories, null, CommonButtonText.ANY)
        {
            @Override
            public void addCriteria(QLBuilder builder, Component constraintComponent)
            {
                ComboBox component = (ComboBox) constraintComponent;
                Label category = (Label) component.getSelectedObject();
                if (category != null && shouldReturnConstraintForValue(category))
                {
                    String categoriesPropPath = builder.getAlias() + '.' + ResourceRepositoryItem.RESOURCE_PROP + '.'
                                                + Resource.TAGS_PROP;
                    builder.appendCriteria(":category in elements(" + categoriesPropPath + ')')
                        .putParameter("category", category);
                }
            }
        }.withLabel(LABEL_CATEGORY());
        searchModel.getConstraints().add(categoryConstraint);

        searchModel.getConstraints().add(new SimpleConstraint("author").withLabel(LABEL_AUTHOR())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.AUTHOR_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        ArrayList<Label> types = new ArrayList<>();
        types.add(null);
        types.addAll(_rtlp.getEnabledLabels(Optional.empty()));
        AbstractPropertyConstraint typeConstraint = new ComboBoxConstraint(types, null, CommonButtonText.ANY)
            .withLabel(LABEL_TYPE())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP)
            .withOperator(PropertyConstraint.Operator.eq)
            .withCoerceValue(false)
            .withValueType(Label.class);
        searchModel.getConstraints().add(typeConstraint);
    }

    /**
     * ResourceRepositoryItemSelector currently only supports LONG mode.
     *
     * @return LONG mode
     */
    @Override
    public RepositoryItemSelectorMode getMode()
    {
        return RepositoryItemSelectorMode.LONG;
    }

    @Override
    protected void addResultColumns(SearchModelImpl searchModel)
    {
        SearchResultColumnImpl nameColumn;
        searchModel.getResultColumns().add(nameColumn = new SearchResultColumnImpl()
            .withName("name")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP).withColumnName(LABEL_NAME()))
            .withOrderBy(new QLOrderByImpl(
                "getTextValue(rriAlias." + ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP + ", '"
                + getLocaleContext().getLanguage() + "', '', '')")));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("categories")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_CATEGORIES()))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                ResourceRepositoryItem rri = (ResourceRepositoryItem) input;
                return ConcatTextSource.create(
                    rri.getResource().getTags().stream().map(TextSources::createTextForAny).collect(Collectors.toList()))
                    .withSeparator(", ");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("author")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.AUTHOR_COLUMN_PROP).withColumnName(LABEL_AUTHOR()))
            .withOrderBy(new QLOrderByImpl(
                "rriAlias." + ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.AUTHOR_COLUMN_PROP)));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("owner")
            .withTableColumn(new FixedValueColumn().withColumnName(LABEL_OWNER()))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                ResourceRepositoryItem rri = (ResourceRepositoryItem) input;
                return _repositoryDAO.getOwnerOfRepositoryItem(rri).getName();
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("type")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP).withColumnName(LABEL_TYPE()))
            .withOrderBy(new QLOrderByImpl(
                "getTextValue(rriAlias." + ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP + ".name, '"
                + getLocaleContext().getLanguage() + "', '', '')")));

        searchModel.setDefaultSortColumn(nameColumn);
    }

    @Override
    protected Function<Object, TextSource> getButtonTextExtractor()
    {
        return obj -> CommonButtonText.SELECT;
    }

    @Override
    protected Supplier<QLBuilder> getBuilderSupplier()
    {
        return () -> {
            QLBuilderImpl builder = new QLBuilderImpl(ResourceRepositoryItem.class, "rriAlias");

            builder.setProjection(builder.getAlias());

            builder.appendCriteria("rriAlias.id in(\n"
                                   + "    select rirel.repositoryItem.id\n"
                                   + "    from RepositoryItemRelation rirel\n"
                                   + "    where rirel.repository.id in (:repoIds))")
                .putParameter("repoIds", getRepositories().stream().map(Repository::getId)
                    .collect(new EntityIdCollector<>(() -> 0)));

            builder.appendCriteria("rriAlias.id not in (:selectedResourceIds)")
                .putParameter("selectedResourceIds", _selectedResources.stream().map(ResourceRepositoryItem::getId)
                    .collect(new EntityIdCollector<>(() -> 0)));


            if (!_includedResourceTypes.isEmpty())
            {
                final HibernateUtil hu = HibernateUtil.getInstance();
                builder.appendCriteria("rriAlias.resource.class in (:includedTypes)")
                    .putParameter("includedTypes",
                        _includedResourceTypes.stream().map(hu::getDiscriminator).collect(Collectors.toSet()));
            }

            if (!_excludedResourceTypes.isEmpty())
            {
                final HibernateUtil hu = HibernateUtil.getInstance();
                builder.appendCriteria("rriAlias.resource.class not in (:excludedTypes)")
                    .putParameter("excludedTypes",
                        _excludedResourceTypes.stream().map(hu::getDiscriminator).collect(Collectors.toSet()));
            }

            return builder;
        };
    }

    @Override
    protected boolean validForSelection(@Nullable ResourceRepositoryItem selected)
    {
        return true;
    }

    @Override
    protected List<Action> getDefaultEntityActions()
    {
        return Collections.emptyList();
    }
}
