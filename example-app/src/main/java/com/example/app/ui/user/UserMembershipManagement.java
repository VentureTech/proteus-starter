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

import com.example.app.model.profile.Membership;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.support.AppUtil;
import com.example.app.terminology.ProfileTermProvider;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.NamedObjectComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Menu;
import net.proteusframework.ui.miwt.component.MenuItem;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.TableCellRenderer;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

import static com.example.app.ui.UIText.SEARCH_MODEL_NAME_FMT;
import static com.example.app.ui.UIText.SEARCH_SUPPLIER_DESCRIPTION_FMT;
import static com.example.app.ui.UIText.SEARCH_SUPPLIER_NAME_FMT;
import static com.example.app.ui.user.UserMembershipManagementLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Management UI for Memberships between a User and Profile(s)
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/9/15 1:10 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserMembershipManagement",
    i18n = {
        @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} Role Search")),
        @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} Role Search")),
        @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} Roles")),
        @I18N(symbol = "Button Text Modify", l10n = @L10N("Modify Permissions")),
        @I18N(symbol = "Delete Confirm Text FMT", l10n = @L10N("Are you sure you want to remove this {0}?")),
        @I18N(symbol = "Column Role", l10n = @L10N("Role")),
        @I18N(symbol = "Operations Search Model Name FMT", l10n = @L10N("{0} Operations Search")),
        @I18N(symbol = "Operations Search Supplier Name FMT", l10n = @L10N("{0} Operations Search")),
        @I18N(symbol = "Operations Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} Operations")),
        @I18N(symbol = "Column Operation", l10n = @L10N("Operation")),
        @I18N(symbol = "Column Enabled", l10n = @L10N("Enabled"))
    }
)
@Configurable
public class UserMembershipManagement extends HistoryContainer implements SearchUIOperationHandler
{
    private final User _user;
    private final List<Profile> _profiles = new ArrayList<>();
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private ProfileTermProvider _terms;
    private SearchUIImpl _searchUI;

    /**
     * Instantiate a new instance of UserMembershipManagement
     *
     * @param user the User for which this manager is managing the Roles
     * @param profiles the profiles for which this manager is managing the Roles -- must have at least one
     */
    public UserMembershipManagement(@Nonnull User user, Profile... profiles)
    {
        super();
        Preconditions.checkNotNull(user, "User was null, this should not happen.");
        Preconditions.checkArgument(profiles.length > 0, "Need at least one Profile for Role Management.");

        _user = user;
        Collections.addAll(_profiles, profiles);

        addClassName("user-roles");
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("User Role Management");
        options.setSearchOnPageLoad(true);

        options.setSearchActions(Collections.emptyList());
        options.addSearchSupplier(searchSupplier);
        options.setHistory(getHistory());

        _searchUI = new SearchUIImpl(options);

        Menu menu = new Menu(CommonButtonText.ADD);
        menu.setTooltip(ConcatTextSource.create(CommonButtonText.ADD, _terms.membershipType()).withSpaceSeparator());
        AppUtil.enableTooltip(menu);
        menu.addClassName("entity-action");
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        final TimeZone tz = getSession().getTimeZone();
        getProfiles().forEach(profile -> {
            MenuItem subMenu = getProfileMenuItem(profile);
            User currentUser = _userDAO.getAssertedCurrentUser();
            if (_profileDAO.canOperate(currentUser, profile, tz, _mop.modifyUserRoles()))
            {
                counter.set(counter.get() + 1);
                menu.add(subMenu);
            }
        });

        menu.setVisible(counter.get() > 0);

        setDefaultComponent(of("search-wrapper user-role-search",
            of("entity-actions actions", menu), _searchUI));
    }

    @Nonnull
    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("User Role Search");
        searchModel.setDisplayName(createText(SEARCH_MODEL_NAME_FMT(), _terms.user()));
        final TimeZone tz = getSession().getTimeZone();
        ActionColumn actions = new ActionColumn()
        {
            @Override
            public TableCellRenderer getTableCellRenderer(SearchUI searchUI)
            {
                final SearchUIOperationHandler handler = searchUI.getSearchSupplier().getSearchUIOperationHandler();
                Container tcr = (Container) super.getTableCellRenderer(searchUI);
                if (tcr == null)
                    tcr = new Container();

                PushButton modifyButton = new PushButton(BUTTON_TEXT_MODIFY())
                {
                    @Override
                    public Component getTableCellRendererComponent(Table table, @Nullable Object value, boolean isSelected,
                        boolean hasFocus,
                        int row, int column)
                    {
                        PushButton btcr = (PushButton) super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);

                        Membership mem = (Membership) value;
                        User currentUser = _userDAO.getAssertedCurrentUser();

                        btcr.setVisible(
                            mem != null && _profileDAO.canOperate(currentUser, mem.getProfile(), tz, _mop.modifyUserRoles()));

                        return btcr;
                    }
                };
                modifyButton.addActionListener(ev -> handler.handle(new SearchUIOperationContext(searchUI, SearchUIOperation.edit,
                    SearchUIOperationContext.DataContext.lead_selection)));

                tcr.add(modifyButton);

                return tcr;
            }
        };
        actions.setIncludeCopy(false);
        actions.setIncludeView(false);
        actions.setIncludeEdit(false);
        actions.getDeleteButton().getButtonDisplay().setConfirmText(createText(DELETE_CONFIRM_TEXT_FMT(), _terms.membership()));
        searchModel.getResultColumns().add(actions);

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("role")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_ROLE()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input

                -> {
                Membership mem = _er.reattachIfNecessary((Membership) input);

                if (mem.getMembershipType() != null)
                    return ConcatTextSource.create(mem.getProfile().getName(), mem.getMembershipType()).withSeparator(" : ");
                else
                    return mem.getProfile().getName();

            })));

        if (getProfiles().size() > 1)
        {
            searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("profile")
                .withTableColumn(new PropertyColumn(Membership.class, Membership.PROFILE_PROP).withColumnName(_terms.profile())));

            searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("profile-type")
                .withTableColumn(new FixedValueColumn().withColumnName(_terms.profileType()))
                .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY, input -> {
                    Membership mem = _er.reattachIfNecessary((Membership) input);
                    return TextSources.createTextForAny(mem.getProfile().getProfileType());
                })));
        }

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(createText(SEARCH_SUPPLIER_NAME_FMT(), _terms.user()));
        searchSupplier.setDescription(createText(SEARCH_SUPPLIER_DESCRIPTION_FMT(), _terms.user()));
        searchSupplier.setSearchModel(searchModel);

        searchSupplier.setBuilderSupplier(() -> {
            QLBuilder builder = _profileDAO.getMembershipQLBuilder();
            builder.appendCriteria(Membership.USER_PROP, PropertyConstraint.Operator.eq, getUser())
                .startGroup(JunctionOperator.OR)
                .appendCriteria(builder.getAlias() + '.' + Membership.PROFILE_PROP + " in (:profiles)")
                .endGroup();
            builder.putParameter("profiles", getProfiles());
            return builder;
        });

        return searchSupplier;
    }

    @Nonnull
    private List<Profile> getProfiles()
    {
        return _profiles.stream()
            .map(prof -> _er.reattachIfNecessary(prof))
            .collect(Collectors.toList());
    }

    private MenuItem getProfileMenuItem(Profile profile)
    {
        final Profile finalProfile = profile;
        TextSource profileText = TextSources.createTextForAny(profile);
        MenuItem subMenu;
        //noinspection ConstantConditions
        if (profile.getProfileType() != null && !profile.getProfileType().getMembershipTypeSet().isEmpty())
        {
            subMenu = new Menu(profileText);
            profile.getProfileType().getMembershipTypeSet().stream().sorted(new NamedObjectComparator(getLocaleContext()))
                .forEach(memType -> {
                    MenuItem memTypeItem = new MenuItem(TextSources.createTextForAny(memType));
                    memTypeItem.addActionListener(ev -> {
                        _profileDAO.saveMembership(_profileDAO.createMembership(
                            finalProfile, getUser(), memType, ZonedDateTime.now(getSession().getTimeZone().toZoneId()), true));
                        _searchUI.doAction(SearchUIAction.search);
                    });
                    ((Menu) subMenu).add(memTypeItem);
                });
        }
        else
        {
            subMenu = new MenuItem(profileText);
            subMenu.addActionListener(ev -> {
                _profileDAO.saveMembership(_profileDAO.createMembership(finalProfile, getUser(), null,
                    ZonedDateTime.now(getSession().getTimeZone().toZoneId()), true));
                _searchUI.doAction(SearchUIAction.search);
            });
        }
        return subMenu;
    }

    @Nonnull
    private User getUser()
    {
        return _er.reattachIfNecessary(_user);
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch (operation)
        {
            case add:
            case edit:
            case delete:
                return true;
            case view:
            case select:
            case copy:
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        Membership mem = context.getData();
        switch (context.getOperation())
        {
            //case add: handled by Menu
            case edit:
                if (mem != null)
                    doEdit(mem);
                break;
            case delete:
                if (mem != null)
                    _profileDAO.deleteMembership(mem);
                _searchUI.doAction(SearchUIAction.search);
                break;
            default:
                break;
        }
    }

    private void doEdit(@Nonnull Membership membership)
    {
        MembershipOperationsEditorUI editor = new MembershipOperationsEditorUI(membership, getHistory());
        PushButton saveButton = CommonActions.SAVE.push();
        PushButton cancelButton = CommonActions.CANCEL.push();

        Container ui = of("edit-membership", of("actions", saveButton, cancelButton), editor);

        ActionListener closer = ev -> getHistory().backOrClear();

        saveButton.addActionListener(ev -> {
            if (((Supplier<Boolean>) () -> {
                final Membership toSave = _er.reattachIfNecessary(membership);
                toSave.getOperations().clear();
                toSave.getOperations().addAll(editor.getSelectedOperations());
                _profileDAO.saveMembership(toSave);
                return true;
            }).get())
            {
                closer.actionPerformed(ev);
            }
        });
        cancelButton.addActionListener(closer);

        getHistory().add(new HistoryElement(ui));
    }
}

