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

package com.example.app.finalproject.ui;

import com.example.app.finalproject.model.FacultyMemberDao;
import com.example.app.finalproject.model.FacultyMemberProfile;
import com.example.app.finalproject.model.Rank;
import com.google.common.base.Supplier;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.i2rd.cms.bean.MIWTBeanConfig;
import com.i2rd.cms.bean.MIWTStateEvent;
import com.i2rd.cms.miwt.BlankColumn;
import com.i2rd.cms.miwt.SiteAwareMIWTApplication;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.component.CardContainer;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.DateConstraint;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.ui.workspace.AbstractUITask;
import net.proteusframework.ui.workspace.EntityWorkspaceEvent;
import net.proteusframework.ui.workspace.ListTaskManager;
import net.proteusframework.ui.workspace.StandardEventType;
import net.proteusframework.ui.workspace.Workspace;
import net.proteusframework.ui.workspace.WorkspaceAware;
import net.proteusframework.ui.workspace.WorkspaceEvent;
import net.proteusframework.ui.workspace.WorkspaceHandler;
import net.proteusframework.ui.workspace.WorkspaceHandlerContext;
import net.proteusframework.ui.workspace.WorkspaceHandlerResult;
import net.proteusframework.ui.workspace.WorkspaceImpl;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.Principal;

/**
 * SearchUI for FacultyMemberProfile
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-10 ??12:53
 */
@I18NFile(
    symbolPrefix = FacultyMemberSearchUI.RESOURCE_NAME,
    i18n = {
        @I18N(symbol = "first name",l10n=@L10N("Member First Name")),
        @I18N(symbol = "last name",l10n=@L10N("Member Last Name")),
        @I18N(symbol = "rank",l10n=@L10N("Member Rank")),
        @I18N(symbol = "user name",l10n=@L10N("CreateUser Name")),
        @I18N(symbol = "user email",l10n=@L10N("CreateUser Email")),
        @I18N(symbol = "sabbatical",l10n = @L10N("Member Sabbatical")),
        @I18N(symbol = "joinDate",l10n = @L10N("Join Date")),
    }
)
@MIWTBeanConfig(value = "FacultyMember-SearchUI", displayName = "FacultyMember-SearchUI",
    applicationClass = SiteAwareMIWTApplication.class, stateEvents = {
    @MIWTStateEvent(eventName = SiteAwareMIWTApplication.SAMA_ADD_COMPONENT, eventValue = "FacultyMember-SearchUI"),
    @MIWTStateEvent(eventName = SiteAwareMIWTApplication.SAMA_RECREATE_ON_SITE_CHANGE, eventValue = "true")})
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FacultyMemberSearchUI extends HistoryContainer implements SearchUIOperationHandler,WorkspaceHandler
{
    /** RESOURCE_NAME */
    public final static String RESOURCE_NAME = "com.example.app.finalproject.ui.FacultyMemberSearchUI";
    /** Logger */
    private final static Logger _logger = Logger.getLogger(FacultyMemberSearchUI.class);
    /** SearchUI */
    private SearchUIImpl _searchUI;
    /** The principal */
    private Principal _createUser;
    /** Main container */
    private Container _mainCon = new Container();
    /** Tasks Container */
    private CardContainer _taskContainer;
    /** Locale Context. */
    private LocaleContext _lc;
    /** Workspace */
    private WorkspaceImpl _workspace;
    /** New a instance*/
    @Autowired
    private FacultyMemberDao _facultyMemberDao;

    /**
     *  Constructor
     */
    public FacultyMemberSearchUI(){}
    /**
     * Create components
     */
    public void init()
    {
        super.init();
        _setupUI();
    }
    /**
     * Set up the SearchUI
     */
    public void _setupUI()
    {
        _mainCon.removeAllComponents();
        _lc = getLocaleContext();

        final MessageContainer notifiable = new MessageContainer(TimeUnit.SECONDS.toMillis(60));
        _taskContainer = new CardContainer();
        _workspace = new WorkspaceImpl(new ListTaskManager(), notifiable, _taskContainer);

        addClassName(Workspace.CSS_WORKSPACE).addClassName(Workspace.CSS_WORKSPACE_TOP_SEARCH);
        _workspace.registerHandler(this);

        final SearchModelImpl searchModel = new SearchModelImpl();

        final PropertyColumn firstNameProp = new PropertyColumn(FacultyMemberProfile.class, "firstName");
        firstNameProp.setDisplayClass("firstName");
        firstNameProp.setColumnName(FacultyMemberSearchUILOK.FIRST_NAME());
        final SearchResultColumnImpl firstColumn = new SearchResultColumnImpl();
        firstColumn.setTableColumn(firstNameProp);
        searchModel.getResultColumns().add(firstColumn);

        final PropertyColumn lastNameProp = new PropertyColumn(FacultyMemberProfile.class, "lastName");
        lastNameProp.setDisplayClass("lastName");
        lastNameProp.setColumnName(FacultyMemberSearchUILOK.LAST_NAME());
        final SearchResultColumnImpl lastColumn = new SearchResultColumnImpl();
        lastColumn.setTableColumn(lastNameProp);
        searchModel.getResultColumns().add(lastColumn);

        final PropertyColumn rankCol = new PropertyColumn(FacultyMemberProfile.class, "rank");
        rankCol.setDisplayClass("rank");
        rankCol.setColumnName(FacultyMemberSearchUILOK.RANK());
        final SearchResultColumnImpl rankColumn = new SearchResultColumnImpl();
        rankColumn.setTableColumn(rankCol);
        searchModel.getResultColumns().add(rankColumn);

        final PropertyColumn userNameProp = new PropertyColumn(FacultyMemberProfile.class, "createUser.username");
        userNameProp.setDisplayClass("createUser-userName");
        userNameProp.setColumnName(FacultyMemberSearchUILOK.USER_NAME());
        final SearchResultColumnImpl userColumn = new SearchResultColumnImpl();
        userColumn.setTableColumn(userNameProp);
        searchModel.getResultColumns().add(userColumn);

        final BlankColumn emailProp = new BlankColumn();
        emailProp.setDisplayClass("email");
        emailProp.setColumnName(FacultyMemberSearchUILOK.USER_EMAIL());
        final SearchResultColumnImpl emailSRCI = new SearchResultColumnImpl();
        emailSRCI.setTableColumn(emailProp);
        searchModel.getResultColumns().add(emailSRCI);
        emailSRCI.setTableCellRenderer(new Label()
        {
            @Nonnull
            @Override
            protected TextSource getCellValueAsText(@Nullable Object value)
            {
                if (value instanceof FacultyMemberProfile)
                {
                    FacultyMemberProfile facultyMember = (FacultyMemberProfile) value;
                    _createUser = facultyMember.getCreateUser();
                    Contact contact = _createUser.getContact();
                    if (contact!=null)
                    {
                        try
                        {
                            contact.getEmailAddresses();
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            _logger.error("Get emailAddress error.", e);
                        }
                        return TextSources.create(contact.getEmailAddresses().get(0).getEmail());
                    }
                }
                return TextSources.EMPTY;
            }
        });

        final PropertyColumn joinDateProp = new PropertyColumn(FacultyMemberProfile.class,"joinDate");
        joinDateProp.setDisplayClass("joinDate");
        joinDateProp.setColumnName(FacultyMemberSearchUILOK.JOINDATE());
        final SearchResultColumnImpl joinDateColumn = new SearchResultColumnImpl();
        joinDateColumn.setTableColumn(joinDateProp);
        searchModel.getResultColumns().add(joinDateColumn);
        joinDateColumn.setTableCellRenderer(new Label()
        {

            protected String getCellValueAsString(Object value)
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                if (value != null)
                {
                    FacultyMemberProfile pro = (FacultyMemberProfile) value;
                    Date date = pro.getJoinDate();
                    if (date != null) return dateFormat.format(date);
                }
                return getCellValueAsString(value);
            }
        });
        joinDateColumn.setOrderBy(new QLOrderByImpl("joinDate"));

        ActionColumn actionColumn = new ActionColumn();
        actionColumn.setIncludeCopy(false);
        searchModel.getResultColumns().add(actionColumn);

        final SimpleConstraint firstNamePro = new SimpleConstraint();
        firstNamePro.setHTMLClass("firstNameCons");
        firstNamePro.setLabel(FacultyMemberSearchUILOK.FIRST_NAME());
        firstNamePro.setOperator(PropertyConstraint.Operator.like);
        firstNamePro.setProperty("firstName");
        searchModel.getConstraints().add(firstNamePro);

        final SimpleConstraint lastNamePro = new SimpleConstraint();
        lastNamePro.setHTMLClass("lastNameCons");
        lastNamePro.setLabel(FacultyMemberSearchUILOK.LAST_NAME());
        lastNamePro.setOperator(PropertyConstraint.Operator.like);
        lastNamePro.setProperty("lastName");
        searchModel.getConstraints().add(lastNamePro);

        final CustomCellRenderer anyRenderer = new CustomCellRenderer(CommonButtonText.ANY);
        final ComboBoxConstraint rankCst = new ComboBoxConstraint()
        {
            @Override
            public void addCriteria(QLBuilder builder, Component constraintComponent)
            {
                final Object value = getValue(constraintComponent);
                if (value != null)
                {
                    builder.appendCriteria("rank", Operator.eq, value);
                }
            }
        };
        rankCst.setLabel(FacultyMemberSearchUILOK.RANK());
        final List<Rank> rankList = new ArrayList<>(Arrays.asList(Rank.values()));
        rankList.add(0, null);
        rankCst.setOptions(rankList);
        rankCst.setCoerceValue(false);
        rankCst.setRenderer(anyRenderer);
        searchModel.getConstraints().add(rankCst);

        final SimpleConstraint userNamePro = new SimpleConstraint();
        userNamePro.setHTMLClass("usernameCons");
        userNamePro.setLabel(FacultyMemberSearchUILOK.USER_NAME());
        userNamePro.setOperator(PropertyConstraint.Operator.like);
        userNamePro.setProperty("createUser.credentials.username");
        searchModel.getConstraints().add(userNamePro);

        final SimpleConstraint emailPro = new SimpleConstraint();
        emailPro.setHTMLClass("emailCons");
        emailPro.setLabel(FacultyMemberSearchUILOK.USER_EMAIL());
        emailPro.setOperator(PropertyConstraint.Operator.like);
        emailPro.setProperty("createUser.contact.emailAddresses.email");
        searchModel.getConstraints().add(emailPro);

        final DateConstraint joinDatePro = new DateConstraint();
        joinDatePro.setHTMLClass("joinDateCons");
        joinDatePro.setLabel(FacultyMemberSearchUILOK.JOINDATE());
        joinDatePro.setOperator(PropertyConstraint.Operator.eq);
        joinDatePro.setProperty("joinDate");
        searchModel.getConstraints().add(joinDatePro);

        final Supplier<QLBuilder> builderSupplier = new Supplier<QLBuilder>()
        {
            @Override
            public QLBuilder get()
            {
                return _facultyMemberDao.getAllFacultyQB();
            }
        };

        SearchSupplierImpl supplier = new SearchSupplierImpl();
        supplier.setBuilderSupplier(builderSupplier);
        supplier.setSearchModel(searchModel);
        supplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options(getClass().getName());
        options.addSearchSupplier(supplier);
        _searchUI = new SearchUIImpl(options);
        final PushButton addBtn= CommonActions.ADD.push();
        addBtn.setLabel(TextSources.create("Add"));
        addBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                FacultyMemberEditorUI addCon = new FacultyMemberEditorUI(null, true);
                navigateBackOnClose(addCon);
                getHistory().add(new HistoryElement(addCon));
            }
        });
        addBtn.addClassName("Add");
        _mainCon.add(Container.of("search-part",addBtn,_searchUI ));
        _mainCon.add(_workspace.getUITaskManager().getComponent());
        _mainCon.add(_taskContainer);
        setDefaultComponent(_mainCon);

    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch (operation)
        {
            case view :
                return true;
            case edit :
                return true;
            case delete:
                return true;
               default:
            return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        final Object rowData = EntityRetriever.getInstance().narrowProxyIfPossible(context.getData());
        if (rowData == null || !(rowData instanceof FacultyMemberProfile))
            return;

        final FacultyMemberProfile facultyMemberProfile = (FacultyMemberProfile) rowData;

        switch (context.getOperation())
        {
            case edit:
                _workspace.handle(new EntityWorkspaceEvent<FacultyMemberProfile>(this, facultyMemberProfile, StandardEventType.modification));
                break;
            case delete:
                _workspace.handle(new EntityWorkspaceEvent<FacultyMemberProfile>(this, facultyMemberProfile, StandardEventType.deletion));
                break;
            case view:
                _workspace.handle(new EntityWorkspaceEvent<FacultyMemberProfile>(this, facultyMemberProfile, StandardEventType.selection));
                break;
            default:
                break;
        }

    }

    @Override
    public boolean supportsWorkspaceEvent(WorkspaceEvent event)
    {
        return (EnumSet.of(StandardEventType.selection,StandardEventType.modification,StandardEventType.deletion)
        .contains(event.getType()))
        && (event instanceof EntityWorkspaceEvent)
        && ((EntityWorkspaceEvent<?>) event).getEntityClass() == FacultyMemberProfile.class;

    }

    @Override
    public WorkspaceHandlerResult handle(WorkspaceEvent event, WorkspaceHandlerContext context)
    {
        if (event instanceof EntityWorkspaceEvent && ((EntityWorkspaceEvent<?>) event).getEntityClass() == FacultyMemberProfile.class)
        {
            EntityWorkspaceEvent<FacultyMemberProfile> ewe = (EntityWorkspaceEvent<FacultyMemberProfile>) event;
            StandardEventType type = (StandardEventType) ewe.getType();
            final FacultyMemberProfile facultyMemberProfile = ewe.getEntity();
            WorkspaceAware ui = null;
            switch (type)
            {
                case deletion:
                    facultyMemberProfile.setDeleted(true);
                    _facultyMemberDao.save(facultyMemberProfile);
                    _searchUI.doAction(SearchUIAction.search);
                    return new WorkspaceHandlerResult().setEventHandled(true);
                case modification:
                    ui = new FacultyMemberEditorUI(facultyMemberProfile,true);
                    break;
                case selection:
                    ui = new FacultyMemberEditorUI(facultyMemberProfile,false);
                    break;
                default:
                    return new WorkspaceHandlerResult().setEventHandled(false);
            }
            if (ui != null)
            {
                context.getWorkspace().register(ui);
                final Component thisIsDumb = (Component) ui;
                context.getWorkspace().addUITask(new AbstractUITask(String.valueOf(facultyMemberProfile.getId()))
                {
                    /**
                     * @return LocalizedText
                     * */
                    @Override
                    public LocalizedText getLabel()
                    {
                        return null;
                    }

                    @Override
                    public Component createTaskUI(LocaleContext localeContext)
                    {
                        return thisIsDumb;
                    }
                });
            }
        }
        return new WorkspaceHandlerResult().setEventHandled(true);
    }
}
