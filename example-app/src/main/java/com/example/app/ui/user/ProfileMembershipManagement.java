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

import com.example.app.config.ProjectCacheRegions;
import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.Membership;
import com.example.app.model.profile.MembershipType;
import com.example.app.model.profile.MembershipTypeProvider;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.profile.ProfileType;
import com.example.app.model.terminology.ProfileTermProvider;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.google.common.base.Preconditions;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.proteusframework.core.ToStringComparator;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.lang.CloseableIterator;
import net.proteusframework.core.lang.ComparableComparator;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.NamedObjectComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.core.validation.CommonValidationText;
import net.proteusframework.ui.column.DataColumnTable;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.Menu;
import net.proteusframework.ui.miwt.component.MenuItem;
import net.proteusframework.ui.miwt.component.Pager;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.composite.DateFormatLabel;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.CalendarValueEditor;
import net.proteusframework.ui.miwt.data.Column;
import net.proteusframework.ui.miwt.data.ListSelectionMode;
import net.proteusframework.ui.miwt.data.RelativeOffsetRange;
import net.proteusframework.ui.miwt.data.Row;
import net.proteusframework.ui.miwt.data.RowModelImpl;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.miwt.validation.CompositeValidator;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLResolverOptions;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.PrincipalStatus;

import static com.example.app.support.AppUtil.*;
import static com.example.app.ui.UIText.*;
import static com.example.app.ui.user.ProfileMembershipManagementLOK.*;
import static com.example.app.ui.user.UserMembershipManagementLOK.BUTTON_TEXT_MODIFY;
import static com.example.app.ui.user.UserMembershipManagementLOK.DELETE_CONFIRM_TEXT_FMT;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Management UI for Memberships between a Profile and User(s)
 * This UI doesn't persist any changes to Profile. Caller must do that.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.ProfileRoleManagement",
    i18n = {
        @I18N(symbol = "Insufficient Permissions", l10n = @L10N("Missing required permission to modify {0}s")),
        @I18N(symbol = "Action Done", l10n = @L10N("Done")),
        @I18N(symbol = "Action Edit Dates", l10n = @L10N("Modify Activation Dates")),
        @I18N(symbol = "Tooltip Edit Dates", l10n = @L10N("Change the start or end date of the {0}")),
        @I18N(symbol = "Instruction", l10n = @L10N("Please select the {0}")),
        @I18N(symbol = "Tooltip Deactivate", l10n = @L10N("Deactivate Sets The End Date To The Current Date")),
        @I18N(symbol = "Error Start Date After End Date", l10n = @L10N("Start Date Cannot Be After End Date")),
        @I18N(symbol = "Edit Dates UI Heading Format", l10n = @L10N("Start/End Date for {0} : {1}")),
        @I18N(symbol = "Only One Allowed FMT", l10n = @L10N("Only one {0} is allowed"))
    }
)
@Configurable
public class ProfileMembershipManagement extends HistoryContainer
{
    private final Set<MembershipType> _excludedMembershipTypes = new HashSet<>();
    private final Set<MembershipType> _requiredMembershipTypes = new HashSet<>();
    @Nonnull
    private final Profile _profile;
    private final ComboBox _activeConstraint = new ComboBox(new SimpleListModel<>(Arrays.asList(ACTIVE(), INACTIVE())));
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private SelectedCompanyTermProvider _terms;
    @Autowired
    private MembershipTypeProvider _membershipTypeProvider;
    private DataColumnTable<Membership> _membershipTable;
    private boolean _allowEditActive;

    private static boolean isOverlapped(List<Membership> list)
    {
        for (Membership m1 : list)
        {
            final Date startA = m1.getStartDate();
            final Date endA = m1.getEndDate();

            for (Membership m2 : list)
            {
                if (m1 == m2) continue;
                final Date startB = m2.getStartDate();
                final Date endB = m2.getEndDate();
                if ((startA == null || endB == null || startA.before(endB) || startA.equals(endB))
                    && (endA == null || startB == null || endA.after(startB) || endA.equals(startB)))
                    return true;
            }
        }
        return false;
    }

    /**
     * Instantiate a new instance of UserMembershipManagement
     *
     * @param profile the profile for which this manager is managing the Roles.
     */
    public ProfileMembershipManagement(@Nonnull Profile profile)
    {
        super();
        Preconditions.checkNotNull(profile, "Profile was null, this should not happen.");
        _profile = profile;
        initializeProfile(profile);
        addClassName("profile-roles");
    }

    /**
     * Initialize a profile for use with this class.
     * <p>This is called in the constructor, but may be called elsewhere if this class
     * is instantiated with a detached entity.</p>
     *
     * @param profile the profile
     */
    public static void initializeProfile(@Nonnull Profile profile)
    {
        Hibernate.initialize(profile);
        Hibernate.initialize(profile.getMembershipSet());
        profile.getMembershipSet().forEach(membership -> {
            Hibernate.initialize(membership);
            Hibernate.initialize(membership.getOperations());
            Hibernate.initialize(membership.getProfile());
            Hibernate.initialize(membership.getProfile().getProfileType());
            membership.getOperations().forEach(Hibernate::initialize);
            final User user = membership.getUser();
            Hibernate.initialize(user);
            final Principal principal = user.getPrincipal();
            Hibernate.initialize(principal);
            Hibernate.initialize(principal.getContact());
            Hibernate.initialize(principal.getContact().getName());
            Hibernate.initialize(principal.getCredentials());
            principal.getCredentials().forEach(Hibernate::initialize);
        });
        Hibernate.initialize(profile.getProfileType());
        Hibernate.initialize(profile.getProfileType().getMembershipTypeSet());
        profile.getProfileType().getMembershipTypeSet().forEach(Hibernate::initialize);
    }

    /**
     * Add one or more excluded membership types.
     * Excluded membership types will not be shown in the UI.
     *
     * @param types the types.
     */
    public void addExcludedMembershipType(@Nonnull MembershipType... types)
    {
        Collections.addAll(_excludedMembershipTypes, types);
    }

    /**
     * Add one or more required membership types.
     *
     * @param types the types.
     */
    public void addRequiredMembershipType(@Nonnull MembershipType... types)
    {
        Collections.addAll(_requiredMembershipTypes, types);
        _requiredMembershipTypes.removeAll(_excludedMembershipTypes);
    }

    /**
     * Get the membership for the membership type.
     *
     * @param membershipType the membership type.
     *
     * @return list of he membership.
     */
    public List<Membership> getMemberships(MembershipType membershipType)
    {
        return getProfile().getMembershipSet().stream().filter(membership -> {
            final MembershipType type = membership.getMembershipType();
            return Objects.equals(type, membershipType);
        }).collect(Collectors.toList());
    }

    @Override
    public void init()
    {
        super.init();
        setValidator(CompositeValidator.of(
            (component, notifiable) -> validateSupporters(notifiable),
            (component, notifiable) -> {
                if (_requiredMembershipTypes.isEmpty())
                    return true;
                final HashSet<MembershipType> toCheck = new HashSet<>(_requiredMembershipTypes);
                getProfile().getMembershipSet().forEach(membership -> toCheck.remove(membership.getMembershipType()));
                toCheck.forEach(mt -> {
                    NotificationImpl notification = new NotificationImpl(NotificationType.ERROR,
                        createText(CommonValidationText.ARG0_IS_REQUIRED, mt.getName()));
                    notification.setSource(this);
                    notifiable.sendNotification(notification);
                });
                return toCheck.isEmpty();
            }
        ));

        User currentUser = _userDAO.getAssertedCurrentUser();
        Hibernate.initialize(currentUser);
        Hibernate.initialize(currentUser.getPrincipal());
        Hibernate.initialize(currentUser.getPrincipal().getContact());
        final Profile adminProfile = getProfile();
        final TimeZone timeZone = getSession().getTimeZone();
        boolean isAdminish = _profileDAO.canOperate(currentUser, adminProfile, timeZone, _mop.modifyCompany());
        if (!_profileDAO.canOperate(currentUser, adminProfile, timeZone, _mop.modifyUserRoles()))
        {
            Label label = new Label(INSUFFICIENT_PERMISSIONS(MEMBERSHIP()))
                .withHTMLElement(HTMLElement.h3);
            setDefaultComponent(label);
            return;
        }
        final SimpleDateFormat dateFormat = getDateFormat(getLocaleContext().getLocale());
        dateFormat.setTimeZone(getSession().getTimeZone());
        final DateFormatLabel dateRenderer = new DateFormatLabel(dateFormat)
        {
            @Override
            public Component getTableCellRendererComponent(Table table, Object cellValue, boolean isSelected, boolean hasFocus,
                int row,
                int column)
            {
                Date value = (Date) cellValue;
                value = toDate(convertFromPersisted(value, getSession().getTimeZone()));
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        final NamedObjectComparator nocComparator = new NamedObjectComparator(getLocaleContext());
        FixedValueColumn actionColumn = new FixedValueColumn();
        actionColumn.setColumnName(CommonColumnText.ACTIONS);
        PropertyColumn userColumn = new PropertyColumn(Membership.class, Membership.USER_PROP);
        userColumn.setColumnName(CommonColumnText.USER);
        userColumn.setComparator(nocComparator);
        PropertyColumn membershipTypeColumn = new PropertyColumn(Membership.class, Membership.MEMBERSHIP_TYPE_PROP);
        membershipTypeColumn.setColumnName(MEMBERSHIP_TYPE());
        membershipTypeColumn.setComparator(nocComparator);
        PropertyColumn membershipDStartColumn = new PropertyColumn(Membership.class, Membership.START_DATE_PROP);
        membershipDStartColumn.setColumnName(START_DATE());
        membershipDStartColumn.setComparator(ComparableComparator.getInstance());
        PropertyColumn membershipDEndColumn = new PropertyColumn(Membership.class, Membership.END_DATE_PROP);
        membershipDEndColumn.setComparator(ComparableComparator.getInstance());
        membershipDEndColumn.setColumnName(END_DATE());

        _membershipTable = isAllowEditActive()
            ? new DataColumnTable<>(actionColumn, userColumn, membershipTypeColumn, membershipDStartColumn, membershipDEndColumn)
            : new DataColumnTable<>(actionColumn, userColumn, membershipTypeColumn);
        _membershipTable.setTableCellRenderer(dateRenderer, Date.class);
        _membershipTable.getDefaultModel().setAutoReattachEntities(false);
        _membershipTable.setRowModel(new RowModelImpl()
        {
            @Override
            public Row getRow(Table table, int row)
            {
                final Row r = super.getRow(table, row);
                final Membership membership = _membershipTable.getDefaultModel().getRow(row);
                if (membership.isActive())
                {
                    r.removeClassName("member-inactive");
                    r.addClassName("member-active");
                }
                else
                {
                    r.addClassName("member-inactive");
                    r.removeClassName("member-active");
                }
                return r;
            }
        });

        QLResolverOptions resolverOptions = new QLResolverOptions();
        resolverOptions.setFetchSize(1);
        resolverOptions.setCacheRegion(ProjectCacheRegions.MEMBER_QUERY);

        PushButton editOperationsBtn = new PushButton(BUTTON_TEXT_MODIFY())
        {
            @Override
            public Component getTableCellRendererComponent(Table table, @Nullable Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column)
            {
                Membership mem = (Membership) value;
                boolean hasOperations = false;
                if (mem != null)
                {
                    final QLBuilderImpl qb = new QLBuilderImpl(ProfileType.class, "ptAlias");
                    qb.setQLResolverOptions(resolverOptions);
                    qb.appendCriteria("id", PropertyConstraint.Operator.eq, mem.getProfile().getProfileType().getId());
                    final JoinedQLBuilder mtQB = qb.createInnerJoin(ProfileType.MEMBERSHIP_TYPES_PROP);
                    final JoinedQLBuilder opQB = mtQB.createInnerJoin(MembershipType.DEFAULT_OPERATIONS_PROP);
                    qb.setProjection("COUNT(DISTINCT %s)", opQB.getAlias());

                    try (CloseableIterator<?> it = qb.getQueryResolver().iterate())
                    {
                        if (it.hasNext())
                        {
                            final Number next = (Number) it.next();
                            hasOperations = next.intValue() > 0;
                        }
                    }
                }
                setVisible(hasOperations && isAdminish);

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        editOperationsBtn.addActionListener(eev -> doOperationEdit(_membershipTable.getLeadSelection()));
        PushButton editActivationDatesBtn = new PushButton(ACTION_EDIT_DATES());
        editActivationDatesBtn.setTooltip(TOOLTIP_EDIT_DATES(MEMBERSHIP()));
        editActivationDatesBtn.addActionListener(eev -> doDatesEdit(_membershipTable.getLeadSelection()));

        PushButton deactivateBtn = new PushButton(ACTION_DEACTIVATE())
        {
            @Override
            public Component getTableCellRendererComponent(Table table, @Nullable Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column)
            {
                Membership m = (Membership) value;
                assert m != null;
                setVisible(m.getEndDate() == null);

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        deactivateBtn.setTooltip(TOOLTIP_DEACTIVATE());
        deactivateBtn.addActionListener(ev -> {
            final Membership membership = _membershipTable.getLeadSelection();
            assert membership != null;
            final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone)
                .minus(1, ChronoUnit.DAYS));
            membership.setEndDate(now);
            showHideConstraints();
        });

        PushButton deleteBtn = CommonActions.DELETE.push();
        deleteBtn.getButtonDisplay().setConfirmText(DELETE_CONFIRM_TEXT_FMT(MEMBERSHIP()));
        deleteBtn.addActionListener(dev -> {
            final Membership membership = _membershipTable.getLeadSelection();
            assert membership != null;
            membership.getProfile().getMembershipSet().remove(membership);
            reloadTableData();
        });
        Container actions = of("actions", editOperationsBtn);
        if (isAllowEditActive())
        {
            actions.add(editActivationDatesBtn);
            actions.add(deactivateBtn);
        }
        if (isAdminish)
            actions.add(deleteBtn);
        final Column uiColumn = _membershipTable.getUIColumn(actionColumn);
        assert uiColumn != null;
        uiColumn.setDisplayClass("action-column");
        _membershipTable.setUICellRenderer(actionColumn, actions);


        Menu menu = new Menu(CommonButtonText.ADD);
        menu.setTooltip(ConcatTextSource.create(CommonButtonText.ADD, MEMBERSHIP_TYPE()).withSpaceSeparator());
        enableTooltip(menu);
        menu.addClassName("entity-action");
        LocaleContext lc = getLocaleContext();
        getProfile().getProfileType().getMembershipTypeSet().stream()
            .filter(membershipType -> !_excludedMembershipTypes.contains(membershipType))
            .sorted(new NamedObjectComparator(lc))
            .forEach(mt -> {
                TextSource menuItemText = mt.getName();
                MenuItem mi = new MenuItem(menuItemText);
                mi.addActionListener(ev -> doSelectUserAndCreateMembership(mt));
                menu.add(mi);
            });

        _activeConstraint.setSelectedObject(ACTIVE());
        _activeConstraint.addActionListener(this::reloadTableData);

        setDefaultComponent(of("search-wrapper profile-role-search",
            of("entity-actions actions", menu),
            of("search-bar", _activeConstraint),
            new Pager(_membershipTable.addClassName("search-results"))));

        reloadTableData();
    }

    /**
     * Test if user is allowed to edit the active properties of
     * membership: {@link Membership#setStartDate(Date)} and {@link Membership#setEndDate(Date)}.
     *
     * @return true or false.
     */
    public boolean isAllowEditActive()
    {
        return _allowEditActive;
    }

    /**
     * Set if user is allowed to edit the active properties of
     * membership: {@link Membership#setStartDate(Date)} and {@link Membership#setEndDate(Date)}.
     *
     * @param allowEditActive the allow edit active
     */
    public void setAllowEditActive(boolean allowEditActive)
    {
        _allowEditActive = allowEditActive;
    }

    /**
     * Validate the supporters.
     *
     * @param notifiable the notifiable.
     *
     * @return true of it's valid.
     */
    public boolean validateSupporters(Notifiable notifiable)
    {
        boolean valid = true;
        // FUTURE : check for singleton membership types
        return valid;
    }

    void doDatesEdit(@Nullable Membership membership)
    {
        if (membership == null)
            return;
        final MembershipType membershipType = membership.getMembershipType();
        assert membershipType != null;
        Label heading = new Label(createText(EDIT_DATES_UI_HEADING_FORMAT(),
            membership.getUser().getName(), membershipType.getName()));
        heading.setHTMLElement(HTMLElement.h3);
        MessageContainer messages = new MessageContainer(35_000L);
        PushButton saveButton = CommonActions.SAVE.push();
        PushButton cancelButton = CommonActions.CANCEL.push();
        RelativeOffsetRange range = new RelativeOffsetRange(5, 2);
        CalendarValueEditor startDateEditor = new CalendarValueEditor(START_DATE(), membership.getStartDate(), range);
        CalendarValueEditor endDateEditor = new CalendarValueEditor(END_DATE(), membership.getEndDate(), range);

        Container ui = of("edit-membership prop-wrapper prop-editor",
            messages,
            heading,
            of("prop-body", startDateEditor, endDateEditor),
            of("actions persistence-actions bottom", saveButton, cancelButton));

        ActionListener closer = ev -> ui.close();

        saveButton.addActionListener(ev -> {
            if (((Supplier<Boolean>) () -> {
                final Date startDate = startDateEditor.commitValue();
                final Date endDate = endDateEditor.commitValue();
                membership.setStartDate(convertForPersistence(toZonedDateTime(startDate, getSession().getTimeZone())));
                ZonedDateTime endDateTime = toZonedDateTime(endDate, getSession().getTimeZone());
                membership.setEndDate(convertForPersistence(endDateTime != null
                    ? endDateTime.minus(1, ChronoUnit.DAYS) : null));
                if (startDate != null && endDate != null && startDate.after(endDate))
                {
                    messages.sendNotification(NotificationImpl.error(
                        ERROR_START_DATE_AFTER_END_DATE()
                    ));
                    return false;
                }
                return true;
            }).get())
            {
                closer.actionPerformed(ev);
                showHideConstraints();
            }
        });
        cancelButton.addActionListener(closer);

        getHistory().add(new HistoryElement(ui));
        navigateBackOnClose(ui);
    }

    void doOperationEdit(@Nullable Membership membership)
    {
        if (membership == null)
            return;
        MembershipOperationsEditorUI editor = new MembershipOperationsEditorUI(membership, getHistory());
        PushButton saveButton = CommonActions.SAVE.push();
        PushButton cancelButton = CommonActions.CANCEL.push();

        Container ui = of("edit-membership prop-wrapper prop-editor",
            editor,
            of("actions persistence-actions bottom", saveButton, cancelButton));

        ActionListener closer = ev -> ui.close();

        saveButton.addActionListener(ev -> {
            if (((Supplier<Boolean>) () -> {
                membership.getOperations().clear();
                membership.getOperations().addAll(editor.getSelectedOperations());
                return true;
            }).get())
            {
                closer.actionPerformed(ev);
            }
        });
        cancelButton.addActionListener(closer);

        getHistory().add(new HistoryElement(ui));
        navigateBackOnClose(ui);
    }

    void doSelectUserAndCreateMembership(MembershipType membershipType)
    {
        final Field name = new Field();
        name.requestFocus();
        name.watchIncremental();
        final Field email = new Field();
        email.watchIncremental();
        final PushButton search = CommonActions.SEARCH.push();

        final FixedValueColumn actionCol = new FixedValueColumn();
        actionCol.setColumnName(CommonColumnText.ACTIONS);
        final PropertyColumn firstNameCol = new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".contact.firstName");
        firstNameCol.setColumnName(CommonColumnText.FIRST_NAME);
        firstNameCol.setComparator(ToStringComparator.CASE_INSENSITIVE);
        final PropertyColumn lastNameCol = new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".contact.lastName");
        lastNameCol.setColumnName(CommonColumnText.LAST_NAME);
        lastNameCol.setComparator(ToStringComparator.CASE_INSENSITIVE);
        final PropertyColumn emailCol = new PropertyColumn(User.class, User.PRINCIPAL_PROP + ".username");
        emailCol.setColumnName(CommonColumnText.LOGIN_NAME);
        emailCol.setComparator(ToStringComparator.CASE_INSENSITIVE);

        final DataColumnTable<User> table = new DataColumnTable<>(actionCol, firstNameCol, lastNameCol, emailCol);
        table.getDisplay().appendDisplayClass("user-table search-results");
        table.getRowSelectionModel().setSelectionMode(ListSelectionMode.SINGLE);

        PushButton selectBtn = CommonActions.SELECT.push();
        final Column actionColumn = table.getUIColumn(actionCol);
        assert actionColumn != null;
        actionColumn.setDisplayClass("action-column");
        actionColumn.setTableCellRenderer(of("actions", selectBtn));
        final ActionListener searchAction = ev -> {
            @SuppressWarnings("ConstantConditions")
            final List<User> exclude = getProfile().getMembershipSet().stream()
                .filter(membership -> membershipType.equals(membership.getMembershipType()))
                .map(Membership::getUser)
                .collect(Collectors.toList());
            final List<User> users = _userDAO.getUsers(name.getText(), name.getText(), email.getText(), exclude);
            table.getDefaultModel().setRows(users.stream().filter(user -> user.getPrincipal().getStatus() == PrincipalStatus
                .active).collect(Collectors.toList()));
        };
        final PropertyChangeListener textChangeListener = evt -> searchAction.actionPerformed(
            new ActionEvent(evt.getSource(), (Component) evt.getSource(), "search"));
        search.addActionListener(searchAction);
        name.addActionListener(searchAction);
        name.addPropertyChangeListener(Field.PROP_TEXT, textChangeListener);
        email.addActionListener(searchAction);
        email.addPropertyChangeListener(Field.PROP_TEXT, textChangeListener);
        final Pager pager = new Pager(table);
        final Container searchBar = of("search-bar",
            of("constraints",
                of("constraint", CommonColumnText.NAME, name).withHTMLElement(HTMLElement.span),
                of("constraint", CommonColumnText.EMAIL, email).withHTMLElement(HTMLElement.span)
            ).withHTMLElement(HTMLElement.span),
            of("actions search-actions", search).withHTMLElement(HTMLElement.span));
        PushButton doneBtn = new PushButton(ACTION_DONE());
        Label instruction = new Label(createText(INSTRUCTION(), membershipType.getName()));
        instruction.addClassName("instrcution");
        Container ui = of("search-wrapper", instruction, searchBar, pager, of("actions bottom", doneBtn));
        doneBtn.addActionListener(ev -> ui.close());
        getHistory().add(new HistoryElement(ui));
        navigateBackOnClose(ui);

        selectBtn.addActionListener(ev -> {
            final User user = table.getLeadSelection();
            assert user != null;
            final Calendar calendar = Calendar.getInstance(getSession().getTimeZone());
            _profileDAO.createMembership(getProfile(), user, membershipType,
                toZonedDateTime(calendar.getTime(), getSession().getTimeZone()), false);
            reloadTableData();
            ui.close();
        });
    }

    void reloadTableData(ActionEvent actionEvent)
    {
        reloadTableData();
    }

    void reloadTableData()
    {
        final Set<Membership> membershipSet = getProfile().getMembershipSet().stream()
            .filter(membership -> !_excludedMembershipTypes.contains(membership.getMembershipType()))
            .filter(membership -> {
                if (ACTIVE().equals(_activeConstraint.getSelectedObject()))
                    return membership.isActive();
                else
                    return !membership.isActive();
            })
            .collect(Collectors.toSet());
        showHideConstraints();
        _membershipTable.getDefaultModel().setRows(membershipSet);
    }

    @Nonnull
    private Profile getProfile()
    {
        return _profile;
    }

    void showHideConstraints()
    {
        final boolean hasInactive = getProfile().getMembershipSet().stream()
            .filter(membership -> !membership.isActive())
            .findAny().isPresent();
        if (!hasInactive)
        {
            _activeConstraint.setSelectedObject(ACTIVE());
            _activeConstraint.setVisible(false);
        }
        else
        {
            _activeConstraint.setVisible(true);
        }
    }
}