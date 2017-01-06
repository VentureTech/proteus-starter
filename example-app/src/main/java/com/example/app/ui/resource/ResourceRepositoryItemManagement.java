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

import com.example.app.profile.model.Profile;
import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.repository.model.Repository;
import com.example.app.repository.model.RepositoryDAO;
import com.example.app.repository.model.RepositoryItemRelation;
import com.example.app.repository.model.RepositoryItemRelationType;
import com.example.app.repository.model.RepositoryItemStatus;
import com.example.app.repository.model.ResourceRepositoryItem;
import com.example.app.resource.model.Resource;
import com.example.app.resource.service.ResourceCategoryLabelProvider;
import com.example.app.resource.service.ResourceTagsLabelProvider;
import com.example.app.resource.service.ResourceTypeService;
import com.example.app.support.AppUtil;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.UIPreferences;
import com.example.app.ui.URLProperties;
import com.google.common.base.Supplier;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelHistoryContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.label.Label;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.LocalizedNamedObjectComparator;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;
import net.proteusframework.ui.management.nav.NavigationDestination;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Menu;
import net.proteusframework.ui.miwt.component.MenuItem;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.TableCellRenderer;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.AbstractPropertyConstraint;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;

import static com.example.app.support.AppUtil.nullFirst;
import static com.example.app.ui.UIText.REPOSITORY;
import static com.example.app.ui.UIText.RESOURCE;
import static com.example.app.ui.resource.ResourceRepositoryItemManagementLOK.*;
import static com.example.app.ui.resource.ResourceText.LABEL_AUTHOR;

/**
 * UI for managing {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 4:19 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.resource.ResourceRepositoryItemManagement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Resource Repository Item Management")),
        @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} {1} Item Search")),
        @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} {1} Item Search")),
        @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} {1} Item")),
        @I18N(symbol = "Label Repository Owner FMT", l10n = @L10N("{0} Owner")),
        @I18N(symbol = "Label Name", l10n = @L10N("Name")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label Owner", l10n = @L10N("Owner")),
        @I18N(symbol = "Label Type", l10n = @L10N("Type")),
        @I18N(symbol = "Delete Confirmation FMT", l10n = @L10N("Are you sure you want to remove this {0}?")),
        @I18N(symbol = "Column Categories", l10n = @L10N("Categories")),
        @I18N(symbol = "Message Resource Deleted FMT", l10n = @L10N("{0} deleted successfully"))
    }
)
@ApplicationFunction(applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.ResourceRepositoryItem.MANAGEMENT,
    description = "UI for managing ResourceRepositoryItems")
public class ResourceRepositoryItemManagement extends MIWTPageElementModelHistoryContainer implements SearchUIOperationHandler
{
    private static class ResourceCategoryComboBoxConstraint extends ComboBoxConstraint
    {
        public ResourceCategoryComboBoxConstraint(ArrayList<Label> tags)
        {
            super(tags, null, CommonButtonText.ANY);
        }

        @Override
        public void addCriteria(QLBuilder builder, Component constraintComponent)
        {
            Object value = getValue(constraintComponent);
            if (!shouldReturnConstraintForValue(value))
                return;
            String paramName = builder.param("category", value);
            JoinedQLBuilder resource = builder.createInnerJoin(ResourceRepositoryItem.RESOURCE_PROP);
            resource.formatCriteria(
                "%s IN (SELECT catRes FROM Resource as catRes INNER JOIN catRes.tags as cat WHERE cat = %s)",
                resource.getAlias(), paramName);
        }
    }

    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private RepositoryDAO _repositoryDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private ResourceTypeService _rts;
    @Autowired
    private ResourceCategoryLabelProvider _rtlp;
    @Autowired
    private ResourceTagsLabelProvider _rclp;
    @Autowired
    private UIPreferences _uiPreferences;

    private User _currentUser;
    private Profile _adminProfile;
    private SearchUIImpl _searchUI;
    private Menu _addMenu;
    /**
     * Instantiate a new instance of ResourceRepositoryItemManagement
     */
    public ResourceRepositoryItemManagement()
    {
        super();

        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("resource-repo-item-mgt");
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("Resouce Repository Item Management");
        options.setSearchOnPageLoad(true);

        _addMenu = new Menu(CommonButtonText.ADD);
        _addMenu.setTooltip(ConcatTextSource.create(CommonButtonText.ADD, RESOURCE()).withSpaceSeparator());
        AppUtil.enableTooltip(_addMenu);
        _addMenu.addClassName("entity-action");
        _rts.getResourceTypes().forEach(resourceType -> {
            MenuItem item = new MenuItem(TextSources.createTextForAny(resourceType));
            item.addClassName(resourceType.getClass().getSimpleName().toLowerCase().replace("resourcetype", ""));
            item.addActionListener(ev -> {
                NavigationDestination addDest = new NavigationDestination(ApplicationFunctions.ResourceRepositoryItem.EDIT);
                HashMap<String, Object> props = new HashMap<>();
                props.put(URLProperties.REPOSITORY_ITEM, URLConfigPropertyConverter.ENTITY_NEW);
                props.put(URLProperties.RESOURCE_TYPE, resourceType);
                props.put(URLProperties.REPOSITORY_OWNER, _adminProfile);
                if (_adminProfile.getRepository() != null)
                {
                    props.put(URLProperties.REPOSITORY, _adminProfile.getRepository());
                }
                addDest.apply(props);
                addDest.actionPerformed(ev);
            });
            _addMenu.add(item);
        });

        options.addSearchSupplier(searchSupplier);
        options.setHistory(getHistory());

        _searchUI = new SearchUIImpl(options);

        setDefaultComponent(of("search-wrapper resource-repo-item-search", of("entity-actions actions", _addMenu), _searchUI));

        setBuilderSupplierAndAddActionAvailability(_currentUser);
    }

    @Nonnull
    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Resource Repository Item Search");
        searchModel.setDisplayName(SEARCH_MODEL_NAME_FMT(RESOURCE(), REPOSITORY()));

        addConstraints(searchModel);

        addResultColumns(searchModel);

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(SEARCH_SUPPLIER_NAME_FMT(RESOURCE(), REPOSITORY()));
        searchSupplier.setDescription(SEARCH_SUPPLIER_DESCRIPTION_FMT(RESOURCE(), REPOSITORY()));
        searchSupplier.setSearchModel(searchModel);

        return searchSupplier;
    }

    private void setBuilderSupplierAndAddActionAvailability(User currentUser)
    {
        assert _searchUI.getSearchSupplier() != null : "Search Supplier was null.  This should not happen.";
        final TimeZone tz = getSession().getTimeZone();
        ((SearchSupplierImpl) _searchUI.getSearchSupplier()).setBuilderSupplier(getBuilderSupplier());
        if (_adminProfile != null)
        {
            if (currentUser == null)
                currentUser = _userDAO.getAssertedCurrentUser();

            _addMenu.setVisible(_profileDAO.canOperate(currentUser, _adminProfile, tz, _mop.viewRepositoryResources())
                                && _profileDAO.canOperate(currentUser, _adminProfile, tz, _mop.modifyRepositoryResources()));
        }
        else _addMenu.setVisible(false);
    }

    private void addConstraints(SearchModelImpl searchModel)
    {
        searchModel.getConstraints().add(new SimpleConstraint("name").withLabel(LABEL_NAME())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        ArrayList<Label> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(_rclp.getEnabledLabels(Optional.empty()).stream().sorted(
            new LocalizedNamedObjectComparator(getLocaleContext())).collect(Collectors.toList()));
        AbstractPropertyConstraint categoryConstraint = new ResourceCategoryComboBoxConstraint(categories)
            .withLabel(_rclp.getLabelDomain().getName())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.TAGS_PROP)
            .withOperator(PropertyConstraint.Operator.in)
            .withValueType(Label.class);
        searchModel.getConstraints().add(categoryConstraint);

        searchModel.getConstraints().add(new SimpleConstraint("author").withLabel(LABEL_AUTHOR())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.AUTHOR_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        ArrayList<Label> types = new ArrayList<>();
        types.add(null);
        types.addAll(_rtlp.getEnabledLabels(Optional.empty()).stream().sorted(
            new LocalizedNamedObjectComparator(getLocaleContext())).collect(Collectors.toList()));
        AbstractPropertyConstraint typeConstraint = new ComboBoxConstraint(types, null, CommonButtonText.ANY)
            .withLabel(_rtlp.getLabelDomain().getName())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP)
            .withOperator(PropertyConstraint.Operator.eq)
            .withCoerceValue(false)
            .withValueType(Label.class);
        searchModel.getConstraints().add(typeConstraint);

        searchModel.getConstraints().add(new ComboBoxConstraint(nullFirst(RepositoryItemStatus.values()), RepositoryItemStatus
            .Active, new CustomCellRenderer(CommonButtonText.ANY))
            .withLabel(CommonColumnText.STATUS)
            .withProperty(ResourceRepositoryItem.STATUS_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.eq));
    }

    private void addResultColumns(SearchModelImpl searchModel)
    {
        final TimeZone tz = getSession().getTimeZone();
        NavigationLinkColumn actions = new NavigationLinkColumn()
        {
            @Override
            public TableCellRenderer getTableCellRenderer(SearchUI searchUI)
            {
                getDeleteAction().setActionListener(ev ->
                    handle(
                        new SearchUIOperationContext(
                            searchUI, SearchUIOperation.delete, SearchUIOperationContext.DataContext.lead_selection)));
                PushButton deleteButton = new PushButton(getDeleteAction());

                Container con = new Container()
                {
                    @Override
                    public Component getTableCellRendererComponent(Table table, Object value,
                        boolean isSelected, boolean hasFocus, int row,
                        int column)
                    {
                        ResourceRepositoryItem rri = (ResourceRepositoryItem) value;
                        boolean v = userCanPerformModification(rri);
                        getEditLink().setVisible(v);
                        getViewLink().setVisible(userCanPerformView(rri));
                        deleteButton.setVisible(v);

                        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                };
                con.add(getViewLink());
                con.add(getEditLink());
                con.add(deleteButton);
                con.addClassName("actions");
                return con;
            }

            private boolean userCanPerformModification(ResourceRepositoryItem rri)
            {
                User currentUser = _userDAO.getAssertedCurrentUser();
                if (rri != null)
                {
                    return _repositoryDAO.canOperate(
                        currentUser, _repositoryDAO.getOwnerOfRepositoryItem(rri), tz, _mop.modifyRepositoryResources());
                }
                else return false;
            }

            private boolean userCanPerformView(ResourceRepositoryItem rri)
            {
                User currentUser = _userDAO.getAssertedCurrentUser();
                if (rri != null)
                {
                    return _repositoryDAO.canOperate(
                        currentUser, _repositoryDAO.getOwnerOfRepositoryItem(rri), tz, _mop.viewRepositoryResources());
                }
                else return false;
            }
        };
        actions.configure()
            .usingDataColumnTableRow(URLProperties.REPOSITORY_ITEM)
            .withSourceComponent(this);
        actions.getEditLink().configure()
            .toPage(ApplicationFunctions.ResourceRepositoryItem.EDIT);
        actions.getViewLink().configure()
            .toPage(ApplicationFunctions.ResourceRepositoryItem.VIEW);
        searchModel.getResultColumns().add(actions);

        SearchResultColumnImpl nameColumn;
        searchModel.getResultColumns().add(nameColumn = new SearchResultColumnImpl()
            .withName("name")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP).withColumnName(LABEL_NAME()))
            .withOrderBy(new QLOrderByImpl("getTextValue("
                                           + "rriAlias." + ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP
                                           + ", '" + getLocaleContext().getLanguage() + "', '', '')")));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("categories")
            .withTableColumn(new FixedValueColumn().withColumnName(_rclp.getLabelDomain().getName()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                ResourceRepositoryItem rri = (ResourceRepositoryItem) input;
                return ConcatTextSource.create(
                    rri.getResource().getTags().stream()
                        .sorted(new LocalizedNamedObjectComparator(getLocaleContext()))
                        .map(TextSources::createTextForAny)
                        .collect(Collectors.toList())).withSeparator(", ");
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
                ResourceRepositoryItem rri = (ResourceRepositoryItem) input;
                return _repositoryDAO.getOwnerOfRepositoryItem(rri).getName();
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("type")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP)
                .withColumnName(_rtlp.getLabelDomain().getName()))
            .withOrderBy(new QLOrderByImpl("getTextValue("
                                           + "rriAlias." + ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP
                                           + ".name"
                                           + ", '" + getLocaleContext().getLanguage() + "', '', '')")));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("status")
            .withTableColumn(new PropertyColumn(ResourceRepositoryItem.class,
                ResourceRepositoryItem.STATUS_COLUMN_PROP)
                .withColumnName(CommonColumnText.STATUS))
            .withOrderBy(new QLOrderByImpl("rriAlias." + ResourceRepositoryItem.STATUS_COLUMN_PROP)));

        searchModel.setDefaultSortColumn(nameColumn);
    }

    private Supplier<QLBuilder> getBuilderSupplier()
    {
        return () -> {
            QLBuilder builder = new QLBuilderImpl(ResourceRepositoryItem.class, "rriAlias");

            builder.appendCriteria("rriAlias.id in(\n"
                                   + "SELECT rirel.repositoryItem.id\n"
                                   + "FROM Profile as profile, RepositoryItemRelation rirel \n"
                                   + "INNER JOIN profile.repository as repo \n"
                                   + "WHERE profile.id = :repoOwnerId\n"
                                   + "AND repo.id = rirel.repository.id\n"
                                   + "AND rirel.relationType = :owned)");
            builder.putParameter("repoOwnerId", _adminProfile.getId());
            builder.putParameter("owned", RepositoryItemRelationType.owned);

            builder.setProjection(builder.getAlias());
            return builder;
        };
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch (operation)
        {
            case add:
            case edit:
            case view:
            case delete:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        switch (context.getOperation())
        {
            //case add: handled by menu
            //case edit: handled by NavigationLinkColumn
            //case view: handled by NavigationLinkColumn
            case delete:
                ResourceRepositoryItem rri = context.getData();
                if (rri != null)
                {
                    Repository repo = Optional.ofNullable(_adminProfile)
                        .map(Profile::getRepository).orElse(null);
                    if (repo != null)
                    {
                        RepositoryItemRelation relation = _repositoryDAO.getRelation(repo, rri).orElse(null);
                        if (relation != null)
                        {
                            _repositoryDAO.deleteRepositoryItemRelation(relation);
                            _searchUI.sendNotification(new NotificationImpl(NotificationType.SUCCESS,
                                MESSAGE_RESOURCE_DELETED_FMT(RESOURCE())));
                        }
                    }
                }
                _searchUI.doAction(SearchUIAction.search);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unused")
        //used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _currentUser = _userDAO.getAssertedCurrentUser();
        _adminProfile = _uiPreferences.getSelectedCompany();

        if (!_profileDAO.canOperate(_currentUser, _adminProfile, AppUtil.UTC, _mop.viewRepositoryResources()))
            throw new IllegalArgumentException("Invalid Permissions To View Page");
    }
}
