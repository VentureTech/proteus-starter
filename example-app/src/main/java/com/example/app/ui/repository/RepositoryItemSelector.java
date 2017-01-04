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

import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.MembershipOperation;
import com.example.app.model.repository.Repository;
import com.example.app.model.repository.RepositoryDAO;
import com.example.app.model.repository.RepositoryItem;
import com.example.app.model.terminology.ProfileTermProvider;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;

import com.example.app.ui.SelectActionColumn;
import com.google.common.base.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.TableDisplay;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

import static com.example.app.ui.UIText.*;
import static com.example.app.ui.repository.RepositoryItemSelector.RepositoryItemSelectorMode.LONG;
import static com.example.app.ui.repository.RepositoryItemSelector.RepositoryItemSelectorMode.SHORT;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * UI for selecting a RepositoryItem.  Implementations of this UI should be
 * {@link Configurable} or a {@link Component}
 *
 * @param <RI> The RepositoryItem subclass
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 2/18/16 11:01 AM
 */
public abstract class RepositoryItemSelector<RI extends RepositoryItem> extends Container implements SearchUIOperationHandler
{
    /** Bound property fired when a repository item is selected */
    public static final String PROP_SELECTED_REPO_ITEM = "selected-repository-item";
    /** Bound property fired when the listing of repository items has been modified */
    public static final String PROP_REPO_ITEM_LIST_MODIFIED = "repository-item-list-modified";

    /**
     * Enum defining different versions of the UI.
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum RepositoryItemSelectorMode
    {
        /** Mode */
        LONG,
        /** Mode */
        SHORT
    }

    @Autowired
    protected EntityRetriever _er;
    @Autowired
    protected UserDAO _userDAO;
    @Autowired
    protected RepositoryDAO _repositoryDAO;
    @Autowired
    protected MembershipOperationProvider _mop;
    @Autowired
    protected SelectedCompanyTermProvider _terms;

    private final List<Repository> _repositories = new ArrayList<>();
    private RepositoryItemSelectorMode _mode = LONG;
    private Function<MembershipOperation, Boolean> _canPerformOperationFunction;
    private List<Action> _entityActions;
    private boolean _autoSelectFirst;
    private SearchUIImpl _searchUI;

    /**
     * Instantiates a new instance of RepositoryItemSelector
     *
     * @param repositories the Repository to select the item from
     */
    public RepositoryItemSelector(Repository... repositories)
    {
        super();

        _repositories.addAll(Arrays.asList(repositories));
        addClassName("repo-item-selector");
    }

    /**
     * Add a listener for when a RepositoryItem is selected
     *
     * @param listener the listener
     *
     * @return this
     */
    public RepositoryItemSelector<RI> addRepoItemSelectedListener(PropertyChangeListener listener)
    {
        addPropertyChangeListener(PROP_SELECTED_REPO_ITEM, listener);
        return this;
    }

    /**
     * Fires a property change to notify any listeners that the list of RepositoryItems has been modified
     */
    public void fireRepoItemListModified()
    {
        firePropertyChange(PROP_REPO_ITEM_LIST_MODIFIED, false, true);
    }

    @Override
    public void init()
    {
        super.init();

        setupUI();
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch (operation)
        {
            case select:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        RI data = context.getData();
        switch (context.getOperation())
        {
            case select:
                selectRepoItem(context.getSearchUI(), data);
                break;
            default:
                break;
        }
    }

    /**
     * Set boolean flag -- if true, will auto-select the first value if there is none selected
     *
     * @param autoSelectFirst boolean flag
     *
     * @return this
     */
    public RepositoryItemSelector<RI> withAutoSelectFirst(boolean autoSelectFirst)
    {
        setAutoSelectFirst(autoSelectFirst);
        return this;
    }

    /**
     * Set the CanModify Supplier for this selector
     *
     * @param canPerformOperationFunction the Supplier
     *
     * @return this
     */
    public RepositoryItemSelector<RI> withCanPerformOperationFunction(
        Function<MembershipOperation, Boolean> canPerformOperationFunction)
    {
        _canPerformOperationFunction = canPerformOperationFunction;
        return this;
    }

    /**
     * Set the entity actions on this Selector
     *
     * @param entityActions the entity actions
     *
     * @return this
     */
    public RepositoryItemSelector<RI> withEntityActions(List<Action> entityActions)
    {
        _entityActions = entityActions;
        return this;
    }

    /**
     * Set the UI display mode for this component
     *
     * @param mode the Mode
     *
     * @return this
     */
    public RepositoryItemSelector<RI> withMode(RepositoryItemSelectorMode mode)
    {
        _mode = mode;
        if (isInited())
            setupUI();
        return this;
    }

    /**
     * Set the Repository to use for selecting the RepositoryItem from
     *
     * @param repositories the Repository
     *
     * @return this
     */
    public RepositoryItemSelector<RI> withRepositories(Repository... repositories)
    {
        _repositories.clear();
        _repositories.addAll(Arrays.asList(repositories));
        if (isInited())
            setupUI();
        return this;
    }

    /**
     * Set up the UI
     */
    @SuppressWarnings("unchecked")
    protected void setupUI()
    {
        removeAllComponents();

        final SearchModelImpl searchModel = new SearchModelImpl();
        final SearchSupplierImpl searchSupplier = new SearchSupplierImpl();

        searchModel.setName("Repository Item Selector");
        searchModel.setDisplayName(createText(SEARCH_MODEL_NAME_FMT(), getRepoItemTerm()));

        addSearchConstraints(searchModel);

        SelectActionColumn actions = new SelectActionColumn();
        searchModel.getResultColumns().add(actions);

        if (getMode() == LONG)
        {
            addResultColumns(searchModel);
        }
        else if (getMode() == SHORT)
        {
            actions.withButtonTextExtractor(getButtonTextExtractor());
            actions.withHtmlClassExtractor(getHtmlClassExtractor());
            actions.withIsSelectedChecker(getIsSelectedChecker());
        }
        else
        {
            throw new RuntimeException("Unknown UI Mode.");
        }

        searchSupplier.setName(createText(SEARCH_SUPPLIER_NAME_FMT(), getRepoItemTerm()));
        searchSupplier.setDescription(createText(SEARCH_SUPPLIER_DESCRIPTION_FMT(), getRepoItemTerm()));
        searchSupplier.setSearchModel(searchModel);
        searchSupplier.setSearchUIOperationHandler(this);
        searchSupplier.setBuilderSupplier(() -> {
            QLBuilder builder = getBuilderSupplier().get();
            if (isAutoSelectFirst() && !getIsSelectedChecker().apply(null))
            {
                List<Object> results = builder.getQueryResolver().list();
                if (!results.isEmpty())
                {
                    RI val = (RI) getRowExtractor().apply(results.get(0));
                    selectRepoItem(_searchUI, val);
                }
            }
            return builder;
        });

        SearchUIImpl.Options options = createSearchUIOptions();
        options.getTableDisplay().setVisibleRows(8);
        options.setSearchOnPageLoad(true);
        options.addSearchSupplier(searchSupplier);
        options.setEntityActions(getEntityActions());
        options.setRowExtractor((obj) -> getRowExtractor().apply(obj));
        if (getMode() == SHORT)
        {
            options.setSearchActions(Collections.emptyList());
        }

        _searchUI = new SearchUIImpl(options);
        addRepoItemListModifiedListener(evt -> _searchUI.doAction(SearchUIAction.search));

        add(of("search-wrapper repo-item-search", _searchUI));
    }

    /**
     * Get the term used to describe the Repository Items being selected from
     *
     * @return the term.  This is usually going to be populated from {@link ProfileTermProvider}
     */
    protected abstract TextSource getRepoItemTerm();

    /**
     * Set the search constraints on the given SearchModel
     *
     * @param searchModel the Search Model
     */
    protected abstract void addSearchConstraints(SearchModelImpl searchModel);

    /**
     * Get the UI display mode for this component
     *
     * @return the Mode
     */
    public RepositoryItemSelectorMode getMode()
    {
        return _mode;
    }

    /**
     * Set the search result columns to the given SearchModel.
     * These are only used when the UI is in LONG mode, and actions should not be included here.
     *
     * @param searchModel the Search Model
     */
    protected abstract void addResultColumns(SearchModelImpl searchModel);

    /**
     * Get the button text extractor to use for creating select buttons in SHORT mode for each of the RepositoryItems
     *
     * @return button text extractor
     */
    protected abstract Function<Object, TextSource> getButtonTextExtractor();

    /**
     * Get the html class extractor to use for creating select buttons in SHORT mode for each of the RepositoryItems
     * By default, this returns null.
     *
     * @return html class extractor
     */
    @Nullable
    protected Function<Object, String> getHtmlClassExtractor()
    {
        return null;
    }

    /**
     * Get the isSelectedChecker to use for creating select buttons in SHORT mode for each of the RepositoryItems
     * By default, this returns always returns false.
     * Passing null into the checker should return a boolean flag telling if there is a value selected.
     * see {@link SelectActionColumn#getIsSelectedChecker()}
     *
     * @return is selected checker
     */
    @Nonnull
    protected Function<Object, Boolean> getIsSelectedChecker()
    {
        return (obj) -> false;
    }

    /**
     * Get the builder supplier for the Search Supplier
     *
     * @return the builder supplier
     */
    protected abstract Supplier<QLBuilder> getBuilderSupplier();

    /**
     * Get boolean flag -- if true, will auto-select the first value if there is none selected
     *
     * @return boolean flag
     */
    public boolean isAutoSelectFirst()
    {
        return _autoSelectFirst;
    }

    /**
     * Get the row extractor to be used by this selector
     * By default, no logic is applied in the row extractor
     *
     * @return row extractor
     */
    protected Function<Object, Object> getRowExtractor()
    {
        return (obj) -> obj;
    }

    private void selectRepoItem(SearchUI searchUI, @Nullable RI data)
    {
        if (validForSelection(data))
        {
            fireRepoItemSelected(data);
            if (getIsSelectedSetter() != null)
            {
                getIsSelectedSetter().accept(data);
            }
            searchUI.doAction(SearchUIAction.search);
        }
    }

    /**
     * Create search ui options.
     *
     * @return the search ui options.
     */
    @Nonnull
    protected SearchUIImpl.Options createSearchUIOptions()
    {
        SearchUIImpl.Options options = new SearchUIImpl.Options("Repository Item Selector");
        options.setHistory(new HistoryImpl());
        options.setTableDisplay(new TableDisplay());
        return options;
    }

    /**
     * Get the Entity Actions for this selector
     *
     * @return Entity Actions
     */
    protected List<Action> getEntityActions()
    {
        if (_entityActions != null)
            return _entityActions;
        else return getDefaultEntityActions();
    }

    /**
     * Add a listener for when a the listing of RepositoryItems is modified
     *
     * @param listener the listener
     *
     * @return this
     */
    public RepositoryItemSelector<RI> addRepoItemListModifiedListener(PropertyChangeListener listener)
    {
        addPropertyChangeListener(PROP_REPO_ITEM_LIST_MODIFIED, listener);
        return this;
    }

    /**
     * Checks that the given selected RepositoryItem is valid for selection
     *
     * @param selected the selected RepositoryItem
     *
     * @return boolean flag -- if true, this RepositoryItem is valid for selection
     */
    protected abstract boolean validForSelection(@Nullable RI selected);

    /**
     * Fires a property change to notify any listener that the given RepositoryItem has been selected.
     *
     * @param repoItem the repository item that was selected
     */
    protected void fireRepoItemSelected(@Nullable RI repoItem)
    {
        firePropertyChange(PROP_SELECTED_REPO_ITEM, null, repoItem);
    }

    /**
     * Get the isSelectedSetter to use for setting the currently selected value
     * By default, this does nothing.
     * see {@link SelectActionColumn#getIsSelectedChecker()}
     *
     * @return is selected setter
     */
    @Nonnull
    protected Consumer<Object> getIsSelectedSetter()
    {
        return (obj) -> {
        };
    }

    /**
     * Get the entity actions for this selector UI
     *
     * @return the entity actions
     */
    protected abstract List<Action> getDefaultEntityActions();

    /**
     * Set boolean flag -- if true, will auto-select the first value if there is none selected
     *
     * @param autoSelectFirst boolean flag
     */
    public void setAutoSelectFirst(boolean autoSelectFirst)
    {
        _autoSelectFirst = autoSelectFirst;
    }

    /**
     * Get the can perform operation function
     *
     * @return the can perform operation function
     */
    protected Optional<Function<MembershipOperation, Boolean>> getCanPerformOperationFunction()
    {
        return Optional.ofNullable(_canPerformOperationFunction);
    }

    /**
     * Get the Repository to use for selecting the RepositoryItem from
     *
     * @return the Repository
     */
    protected List<Repository> getRepositories()
    {
        return _repositories.stream().map(_er::reattachIfNecessary).collect(Collectors.toList());
    }
}
