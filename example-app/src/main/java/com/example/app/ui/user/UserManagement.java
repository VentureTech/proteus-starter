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

package com.example.app.ui.user;

import com.google.common.base.Supplier;
import com.lrlabs.model.profile.Membership;
import com.lrlabs.model.profile.MembershipType;
import com.lrlabs.model.profile.ProfileDAO;
import com.lrlabs.model.user.ContactMethod;
import com.lrlabs.model.user.User;
import com.lrlabs.model.user.UserDAO;
import com.lrlabs.model.user.UserPosition;
import com.lrlabs.terminology.ProfileTermProvider;
import com.lrlabs.util.ContactUtil;
import com.lrlabs.util.LRLabsUtil;
import com.lrsuccess.ldp.model.coached.CoachedEntity;
import com.lrsuccess.ldp.model.coached.CoachedEntityDAO;
import com.lrsuccess.ldp.model.profile.CoachingEntity;
import com.lrsuccess.ldp.model.profile.CoachingEntityDAO;
import com.lrsuccess.ldp.model.profile.MembershipOperationConfiguration;
import com.lrsuccess.ldp.model.profile.PlanDAO;
import com.lrsuccess.ldp.ui.Application;
import com.lrsuccess.ldp.ui.ApplicationFunctions;
import com.lrsuccess.ldp.ui.URLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelHistoryContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.NamedObjectComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.miwt.util.RendererEditorState;
import net.proteusframework.ui.search.AbstractBulkAction;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.PropertyConstraint.Operator;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilder.JoinType;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIContext;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.PrincipalStatus;

import static com.lrsuccess.ldp.ui.user.UserManagementLOK.*;

/**
 * UI for managing {@link User}s
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/3/15 3:41 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserManagement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("User Management")),
        @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} Search")),
        @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} Search")),
        @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0]")),
        @I18N(symbol = "Constraint Role", l10n = @L10N("Role")),
        @I18N(symbol = "Column Plan Role FMT", l10n = @L10N("{0} Role")),
        @I18N(symbol = "Column Contact Email", l10n = @L10N("Contact Email")),
        @I18N(symbol = "Column Contact Phone", l10n = @L10N("Contact Phone"))
    }
)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ApplicationFunction(applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.User.MANAGEMENT,
    description = "UI for managing Users")
public class UserManagement extends MIWTPageElementModelHistoryContainer implements SearchUIOperationHandler
{
    private static class UserPositionConstraint extends SimpleConstraint
    {
        public UserPositionConstraint()
        {
            super("position");
            setProperty(User.USER_POSITIONS_PROP + "._position");
        }
    }

    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private CoachingEntityDAO _coachingEntityDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private CoachedEntityDAO _coachedEntityDAO;
    @Autowired
    private PlanDAO _planDAO;
    @Autowired
    private MembershipOperationConfiguration _mop;
    @Autowired
    private ProfileTermProvider _terms;

    private SearchUIImpl _searchUI;
    private NavigationAction _addAction;
    private User _currentUser;
    private CoachingEntity _coachingEntity;

    /**
     *   Instantiates a new instance of UserManagement
     */
    public UserManagement()
    {
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("user-mgt");
    }

    @Override
    public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
    {
        super.preRenderProcess(request, response, state);

        _currentUser = _userDAO.getAssertedCurrentUser();
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("User Management");
        options.setSearchOnPageLoad(true);

        _addAction = CommonActions.ADD.navAction();
        _addAction.configure().toPage(ApplicationFunctions.User.EDIT);
        _addAction.setPropertyValueResolver(parameter -> {
            HashMap<String,Object> map = new HashMap<>();
            map.put(URLProperties.USER, URLConfigPropertyConverter.ENTITY_NEW);
            map.put(URLProperties.COACHING_ENTITY, _coachingEntity);
            return map;
        });

        options.getEntityActions().add(_addAction);

        options.addSearchSupplier(searchSupplier);
        options.setHistory(getHistory());

        _searchUI = new SearchUIImpl(options);

        setDefaultComponent(of("search-wrapper user-search", _searchUI));

        setBuilderSupplierAndAddActionAvailability();

    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch(operation)
        {
            case add:
            case view:
                return true;
            case delete:
            case edit:
            case select:
            case copy:
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
//        switch(context.getOperation())
//        {
//            // case add: handled by entity action
//            // case view: handled by NavigationLinkColumn
//            default:
//                break;
//        }
    }

    @Nonnull
    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("User Search");
        searchModel.setDisplayName(createText(SEARCH_MODEL_NAME_FMT(), _terms.user()));

        addConstraints(searchModel);

        addResultColumns(searchModel);

        addBulkActions(searchModel);

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(createText(SEARCH_SUPPLIER_NAME_FMT(), _terms.user()));
        searchSupplier.setDescription(createText(SEARCH_SUPPLIER_DESCRIPTION_FMT(), _terms.user()));
        searchSupplier.setSearchModel(searchModel);

        return searchSupplier;
    }

    private void addResultColumns(SearchModelImpl searchModel)
    {
        final NavigationLinkColumn actionColumn = new NavigationLinkColumn();
        actionColumn.setIncludeCopy(false);
        actionColumn.setIncludeEdit(false);
        actionColumn.setIncludeDelete(false);
        final TimeZone tz = getSession().getTimeZone();
        actionColumn.setIncludeView(_profileDAO.canOperate(_currentUser,
            _coachingEntity, tz, _mop.viewUser()));
        actionColumn.configure()
            .usingDataColumnTableRow(URLProperties.USER)
            .withSourceComponent(this);
        actionColumn.getViewLink().configure()
            .toPage(ApplicationFunctions.User.VIEW);
        searchModel.getResultColumns().add(actionColumn);

        SearchResultColumnImpl idColumn;
        searchModel.getResultColumns().add(idColumn = new SearchResultColumnImpl()
            .withName("id")
            .withTableColumn(new PropertyColumn(User.class, "id")
                .withColumnName(CommonColumnText.ID)));
        final QLOrderByImpl idOrderBy = new QLOrderByImpl("id");
        idOrderBy.setPrefixWithAlias(true);
        idColumn.withOrderBy(idOrderBy);
        idColumn.setDisplayDefault(false);

        SearchResultColumnImpl defaultSortColumn;
        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("first-name")
            .withTableColumn(new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".contact.name.first")
                .withColumnName(CommonColumnText.FIRST_NAME))
            .withOrderBy(new QLOrderByImpl("userAlias." + User.PRINCIPAL_PROP + ".contact.name.first")));

        searchModel.getResultColumns().add(defaultSortColumn = new SearchResultColumnImpl()
            .withName("last-name")
            .withTableColumn(new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".contact.name.last")
                .withColumnName(CommonColumnText.LAST_NAME))
            .withOrderBy(new QLOrderByImpl("userAlias." + User.PRINCIPAL_PROP + ".contact.name.last")));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("title")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.TITLE))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                User user = (User)input;
                return _userDAO.getCurrentUserPosition(user)
                    .map(UserPosition::getPosition)
                    .orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("coached-entity")
            .withTableColumn(new FixedValueColumn().withColumnName(_terms.coachedEntity()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                User user = (User)input;
                List<CoachedEntity> coacheds = _coachedEntityDAO.getCoachedEntitiesForUserAndCoachingEntity
                    (_coachingEntity, user);
                if(coacheds.isEmpty())
                    return TextSources.EMPTY;
                else
                {
                    List<TextSource> coachedTSs = coacheds.stream()
                        .map(TextSources::createTextForAny)
                        .collect(Collectors.toList());
                    return ConcatTextSource.create(coachedTSs).withSeparator(", ");
                }
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("role")
            .withTableColumn(new FixedValueColumn().withColumnName(CONSTRAINT_ROLE()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                User user = (User)input;
                List<MembershipType> roles = _profileDAO.getMemberships(
                    _coachingEntity, user, getSession().getTimeZone())
                    .stream()
                    .filter(mem -> mem.getMembershipType() != null)
                    .map(Membership::getMembershipType)
                    .collect(Collectors.toList());
                if(roles.isEmpty())
                    return TextSources.EMPTY;
                else
                {
                    List<TextSource> memTypeTSs = roles.stream()
                        .map(TextSources::createTextForAny)
                        .collect(Collectors.toList());
                    return ConcatTextSource.create(memTypeTSs).withSeparator(", ");
                }
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("plan-role")
            .withTableColumn(new FixedValueColumn().withColumnName(createText(COLUMN_PLAN_ROLE_FMT(), _terms.plan())))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                User user = (User)input;
                CoachingEntity coaching = _coachingEntity;
                List<MembershipType> memTypes = _planDAO.getPlanMembershipTypesForUserAndCoaching(user, coaching);
                if(memTypes.isEmpty())
                    return TextSources.EMPTY;
                else
                {
                    List<TextSource> memTypeTSs = memTypes.stream()
                        .map(TextSources::createTextForAny)
                        .collect(Collectors.toList());
                    return ConcatTextSource.create(memTypeTSs).withSeparator(", ");
                }
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("contact-email")
            .withTableColumn(new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".username")
                .withColumnName(COLUMN_CONTACT_EMAIL())));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("contact-phone")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_CONTACT_PHONE()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                User user = (User)input;
                return ContactUtil.getPhoneNumber(user.getPrincipal().getContact(), ContactDataCategory.values())
                    .map(PhoneNumber::toExternalForm).orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("status")
            .withTableColumn(new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".status")
                .withColumnName(CommonColumnText.STATUS)));

        searchModel.setDefaultSortColumn(defaultSortColumn);
    }

    private void addConstraints(SearchModelImpl searchModel)
    {
        searchModel.getConstraints().add(
            new ComboBoxConstraint(){

                private ComboBox _constraintComponent;

                @Override
                public net.proteusframework.ui.miwt.component.Component getConstraintComponent()
                {
                    if(_constraintComponent == null || _constraintComponent.isClosed())
                    {
                        List<CoachedEntity> coachedEntities = new ArrayList<>();
                        coachedEntities.add(null);
                        final List<CoachedEntity> coachedEntitiesForCoachingEntity =
                            _coachedEntityDAO.getCoachedEntitiesForCoachingEntity(_coachingEntityDAO
                                .getAssertedCoachingEntityForUser(_currentUser));
                        coachedEntitiesForCoachingEntity.sort(new NamedObjectComparator(getLocaleContext()));
                        coachedEntities.addAll(coachedEntitiesForCoachingEntity);

                        _constraintComponent = new ComboBox(new SimpleListModel<>(coachedEntities));
                        _constraintComponent.setCellRenderer(new CustomCellRenderer(CommonButtonText.ANY));
                    }
                    return _constraintComponent;
                }

                @Override
                public void addCriteria(QLBuilder builder, net.proteusframework.ui.miwt.component.Component constraintComponent)
                {
                    ComboBox combo = (ComboBox)constraintComponent;
                    CoachedEntity coached = (CoachedEntity)combo.getSelectedObject();
                    if(coached != null && shouldReturnConstraintForValue(coached))
                    {
                        QLBuilder membershipBuilder = _profileDAO.getMembershipQLBuilder();
                        membershipBuilder.setProjection(
                            "distinct " + membershipBuilder.getAlias() + '.' + Membership.USER_PROP + ".id");
                        membershipBuilder.appendCriteria(Membership.PROFILE_PROP, getOperator(), coached);
                        List<Integer> userIDs = membershipBuilder.getQueryResolver().list();
                        if(userIDs.isEmpty())
                            userIDs.add(0);
                        builder.appendCriteria(builder.getAlias() + ".id in (:usersOnCoached)")
                            .putParameter("usersOnCoached", userIDs);
                    }
                }
            }
            .withLabel(_terms.coachedEntity())
            .withOperator(Operator.eq));

        searchModel.getConstraints().add(
            new ComboBoxConstraint(){

                private ComboBox _constraintComponent;

                @Override
                public net.proteusframework.ui.miwt.component.Component getConstraintComponent()
                {
                    if(_constraintComponent == null || _constraintComponent.isClosed())
                    {
                        List<MembershipType> memTypes = new ArrayList<>();
                        memTypes.add(null);
                        memTypes.addAll(_profileDAO.getMembershipTypesForProfile(_coachingEntityDAO
                            .getAssertedCoachingEntityForUser(_currentUser)));

                        _constraintComponent = new ComboBox(new SimpleListModel<>(memTypes));
                        _constraintComponent.setCellRenderer(new CustomCellRenderer(CommonButtonText.ANY));
                    }
                    return _constraintComponent;
                }

                @Override
                public void addCriteria(QLBuilder builder, net.proteusframework.ui.miwt.component.Component constraintComponent)
                {
                    ComboBox combo = (ComboBox)constraintComponent;
                    MembershipType memType = (MembershipType)combo.getSelectedObject();
                    if(memType != null && shouldReturnConstraintForValue(memType))
                    {
                        QLBuilder membershipBuilder = _profileDAO.getMembershipQLBuilder();
                        membershipBuilder.setProjection("distinct " + membershipBuilder.getAlias() + '.'
                            + Membership.USER_PROP + ".id");
                        membershipBuilder.appendCriteria(Membership.MEMBERSHIP_TYPE_PROP, getOperator(), memType);
                        membershipBuilder.appendCriteria(Membership.PROFILE_PROP, Operator.eq,
                            _coachingEntity);
                        List<Integer> userIDs = membershipBuilder.getQueryResolver().list();
                        if(userIDs.isEmpty())
                            userIDs.add(0);
                        builder.appendCriteria(builder.getAlias() + ".id in (:usersWithRole)")
                            .putParameter("usersWithRole", userIDs);
                    }
                }
            }
            .withLabel(CONSTRAINT_ROLE())
            .withOperator(Operator.eq));

        searchModel.getConstraints().add(new SimpleConstraint("first-name").withLabel(CommonColumnText.FIRST_NAME)
            .withProperty(User.PRINCIPAL_PROP + ".contact.name.first")
            .withOperator(Operator.like));

        searchModel.getConstraints().add(new SimpleConstraint("last-name").withLabel(CommonColumnText.LAST_NAME)
            .withProperty(User.PRINCIPAL_PROP + ".contact.name.last")
            .withOperator(Operator.like));

        searchModel.getConstraints().add(new SimpleConstraint("username").withLabel(CommonColumnText.EMAIL)
            .withProperty(User.PRINCIPAL_PROP + ".credentials.username")
            .withOperator(Operator.like));

        searchModel.getConstraints().add(new UserPositionConstraint()
            .withLabel(CommonColumnText.TITLE)
            .withOperator(Operator.like));

        List<PrincipalStatus> statuses = new ArrayList<>();
        statuses.add(null);
        Collections.addAll(statuses, PrincipalStatus.values());
        searchModel.getConstraints().add(new ComboBoxConstraint(statuses, PrincipalStatus.active, CommonButtonText.ANY)
            .withLabel(CommonColumnText.STATUS)
            .withProperty(User.PRINCIPAL_PROP + ".status")
            .withOperator(Operator.eq));
    }

    private void addBulkActions(SearchModelImpl searchModel)
    {
        searchModel.getBulkActions().add(new AbstractBulkAction()
        {
            @Override
            public void doAction(SearchUIContext context)
            {
                performPrincipalStatusBulkAction(PrincipalStatus.active);
            }
        }.withName(CommonButtonText.ACTIVATE));

        searchModel.getBulkActions().add(new AbstractBulkAction()
        {
            @Override
            public void doAction(SearchUIContext context)
            {
                performPrincipalStatusBulkAction(PrincipalStatus.closed);
            }
        }.withName(CommonButtonText.CLOSE));

        searchModel.getBulkActions().add(new AbstractBulkAction()
        {
            @Override
            public void doAction(SearchUIContext context)
            {
                performContactUserBulkAction(ContactMethod.PhoneSms);
            }
        }.withName(ContactMethod.PhoneSms.getName()));

        searchModel.getBulkActions().add(new AbstractBulkAction()
        {
            @Override
            public void doAction(SearchUIContext context)
            {
                performContactUserBulkAction(ContactMethod.Email);
            }
        }.withName(ContactMethod.Email.getName()));
    }

    private void performPrincipalStatusBulkAction(@Nonnull PrincipalStatus status)
    {
        @SuppressWarnings("unchecked")
        List<User> selection = (List<User>) _searchUI.getSelection();
        for(User user : selection)
        {
            user = _er.reattachIfNecessary(user);
            user.getPrincipal().setStatus(status);
            user.getPrincipal().setEnabled(status == PrincipalStatus.active);

            _userDAO.mergeUser(user);
        }
        _searchUI.doAction(SearchUIAction.search);
    }

    private void performContactUserBulkAction(@Nonnull ContactMethod contactMethod)
    {
        @SuppressWarnings("unchecked")
        List<User> selection = (List<User>) _searchUI.getSelection();

        if(!selection.isEmpty())
        {
            final Dialog dlg = new Dialog(getApplication(), contactMethod.getName());
            dlg.addClassName("message-ctx-editor-dialog");
            final ActionListener closer = ev -> {
                dlg.close();
                _searchUI.doAction(SearchUIAction.search);
            };
            final MessageContextPropertyEditor editor = new MessageContextPropertyEditor(contactMethod, selection, closer);

            dlg.add(editor);
            getWindowManager().add(dlg);
            dlg.setVisible(true);
        }
    }

    private Supplier<QLBuilder> getBuilderSupplier()
    {
        return () -> {
            final Integer ceId = _coachingEntity.getId();

            QLBuilder builder = _coachingEntityDAO.getCoachingEntityQLBuilder();
            builder.appendCriteria("id", Operator.eq, ceId);
            final JoinedQLBuilder userQB = builder.createJoin(JoinType.INNER, "users", UserDAO.ALIAS);
            builder.setProjection(userQB.getAlias());
            return userQB;
        };
    }

    private void setBuilderSupplierAndAddActionAvailability()
    {
        assert (_searchUI.getSearchSupplier()) != null : "Search Supplier was null.  This should not happen.";
        ((SearchSupplierImpl)_searchUI.getSearchSupplier()).setBuilderSupplier(getBuilderSupplier());
        final TimeZone tz = getSession().getTimeZone();
        _addAction.setEnabled(_profileDAO.canOperate(_currentUser, _coachingEntity, tz, _mop.modifyUser()));
    }

    @SuppressWarnings("unused") //used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _currentUser = _userDAO.getAssertedCurrentUser();
        _coachingEntity = _coachingEntityDAO.getAssertedCoachingEntityForUser(_currentUser);

        if(!_profileDAO.canOperate(_currentUser, _coachingEntity, LRLabsUtil.UTC, _mop.viewUser()))
            throw new IllegalArgumentException("Invalid Permissions To View Page");
    }
}
