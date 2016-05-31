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
import com.example.app.model.repository.RepositoryItemStatus;
import com.example.app.model.repository.ResourceRepositoryItem;
import com.example.app.model.resource.Resource;
import com.example.app.service.ResourceCategoryLabelProvider;
import com.example.app.service.ResourceTagsLabelProvider;
import com.example.app.terminology.ProfileTermProvider;
import com.example.app.ui.SelectActionColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.AbstractPropertyConstraint;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;

import static com.example.app.support.AppUtil.nullFirst;
import static com.example.app.ui.UIText.SEARCH_MODEL_NAME_FMT;
import static com.example.app.ui.UIText.SEARCH_SUPPLIER_DESCRIPTION_FMT;
import static com.example.app.ui.UIText.SEARCH_SUPPLIER_NAME_FMT;
import static com.example.app.ui.resource.ResourceSelectorLOK.*;
import static com.example.app.ui.resource.ResourceText.LABEL_AUTHOR;
import static com.example.app.ui.resource.ResourceValueEditorLOK.LABEL_NAME;
import static com.example.app.ui.resource.ResourceValueEditorLOK.LABEL_TYPE;
import static net.proteusframework.core.locale.TextSources.EMPTY;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.ui.search.SearchUIAction.search;

/**
 * Provides a UI for selecting Resources from a given Repository
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/29/15 11:44 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.resource.ResourceSelector",
    i18n = {
        @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} Selector Search")),
        @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} Selector Search")),
        @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} Selector")),
        @I18N(symbol = "Label Name", l10n = @L10N("Name")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label Author", l10n = @L10N("Author")),
        @I18N(symbol = "Label Owner", l10n = @L10N("Owner")),
        @I18N(symbol = "Label Type", l10n = @L10N("Type")),
        @I18N(symbol = "Column Categories", l10n = @L10N("Categories"))
    }
)
@Configurable
public class ResourceSelector extends Container implements SearchUIOperationHandler
{
    @Autowired
    private ResourceCategoryLabelProvider _rtlp;
    @Autowired
    private ResourceTagsLabelProvider _rclp;
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private RepositoryDAO _repositoryDAO;
    @Autowired
    private ProfileTermProvider _terms;

    private final Repository _repository;
    private final List<Resource> _selection = new ArrayList<>();
    private final List<Action> _entityActions = new ArrayList<>();
    private Function<SearchUIOperationContext, Void> _onSelect;

    /**
     *   Instantiate a new instance of ResourceSelector
     *   @param repository the Repository to select Resources from
     *   @param initialSelection the initial list of Resources to have selected
     */
    public ResourceSelector(@Nonnull Repository repository, @Nullable List<Resource> initialSelection)
    {
        _repository = repository;
        if(initialSelection != null)
        {
            _selection.addAll(initialSelection);
        }
        addClassName("resource-selector");
    }

    private Repository getRepository()
    {
        return _er.reattachIfNecessary(_repository);
    }
    
    /**
     *   Get the list of currently selected Resources
     *   @return selected Resources
     */
    public List<Resource> getSelection()
    {
        return _selection;
    }

    /**
     *   Set the list of currently selected Resources
     *   @param selection resources
     */
    public void setSelection(List<Resource> selection)
    {
        _selection.clear();
        _selection.addAll(selection);
    }

    /**
     *   Get the OnSelect listener.  By default, this just adds to the list returned by {@link #getSelection()}
     *   @return the OnSelect listener
     */
    @SuppressWarnings("ConstantConditions")
    public Function<SearchUIOperationContext, Void> getOnSelect()
    {
        if(_onSelect == null)
        {
            return context -> {
                ResourceRepositoryItem rri = context.getData();
                if(rri != null)
                {
                    _selection.add(rri.getResource());
                    context.getSearchUI().doAction(search);
                }
                return null;
            };
        }
        return _onSelect;
    }
    /**
     *   Set the OnSelect listener.  By default, this just adds to the list returned by {@link #getSelection()}
     *   @param onSelect the OnSelect listener
     */
    public void setOnSelect(Function<SearchUIOperationContext, Void> onSelect)
    {
        _onSelect = onSelect;
    }

    /**
     *   Set the Entity Actins on the Search UI. This only works if it is called before init.
     *   @param actions the Actions to set
     */
    public void setEntityActions(Action... actions)
    {
        _entityActions.clear();
        Collections.addAll(_entityActions, actions);
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("Resource Selector");
        options.setSearchOnPageLoad(true);
        options.addSearchSupplier(searchSupplier);
        options.setHistory(new HistoryImpl());
        _entityActions.forEach(options::addEntityAction);

        SearchUIImpl searchUI = new SearchUIImpl(options);

        add(of("search-wrapper resource-selector-search", searchUI));
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch(operation)
        {
            case select:
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        switch (context.getOperation())
        {
            case select:
                getOnSelect().apply(context);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("Duplicates")
    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Resource Selector Search");
        searchModel.setDisplayName(createText(SEARCH_MODEL_NAME_FMT(), _terms.resource()));

        addConstraints(searchModel);

        addResultColumns(searchModel);

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(createText(SEARCH_SUPPLIER_NAME_FMT(), _terms.resource()));
        searchSupplier.setDescription(createText(SEARCH_SUPPLIER_DESCRIPTION_FMT(), _terms.resource()));
        searchSupplier.setSearchModel(searchModel);

        searchSupplier.setBuilderSupplier(() -> {
            QLBuilderImpl builder = new QLBuilderImpl(ResourceRepositoryItem.class, "rriAlias");

            builder.setProjection(builder.getAlias());

            builder.appendCriteria("rriAlias.id in(\n"
                + "select rirel.repositoryItem.id\n"
                + "from RepositoryItemRelation rirel\n"
                + "where rirel.repository.id = :repoId)\n")
                .putParameter("repoId", getRepository().getId());

            List<Integer> selectedResources = getSelection().stream().map(resource -> _repositoryDAO.getRepoItemForResource
                (resource)).filter(Optional::isPresent).map(rri -> rri.get().getId()).collect(Collectors.toList());
            if(selectedResources.isEmpty())
                selectedResources.add(0);
            builder.appendCriteria("rriAlias.id not in(:selectedResourceIds)")
                .putParameter("selectedResourceIds", selectedResources);

            return builder;
        });

        return searchSupplier;
    }

    @SuppressWarnings("Duplicates")
    private void addConstraints(SearchModelImpl searchModel)
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
        AbstractPropertyConstraint categoryConstraint = new ComboBoxConstraint(categories, null, CommonButtonText.ANY){
            @Override
            public void addCriteria(QLBuilder builder, Component constraintComponent)
            {
                ComboBox component = (ComboBox)constraintComponent;
                Label category = (Label)component.getSelectedObject();
                if(category != null && shouldReturnConstraintForValue(category))
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

    private void addResultColumns(SearchModelImpl searchModel)
    {
        ActionColumn actions = new SelectActionColumn();

        searchModel.getResultColumns().add(actions);

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
                ResourceRepositoryItem rri = (ResourceRepositoryItem)input;
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
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                ResourceRepositoryItem rri = (ResourceRepositoryItem)input;
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
}
