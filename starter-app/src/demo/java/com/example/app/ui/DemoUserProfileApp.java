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

package com.example.app.ui;

import com.example.app.model.DemoUserProfile;
import com.example.app.model.DemoUserProfileDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.i2rd.cms.miwt.TimeAuditableColumn;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.internet.http.SiteLoader;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.miwt.MIWTSession;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.DateFormatLabel;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIApp;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandlerImpl;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.ui.workspace.AbstractUITask;
import net.proteusframework.users.model.Name;

import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.ui.search.SearchUIImpl.Options;

/**
 * Example application to demonstrate how to create and handle
 * UI views using a workspace.
 *
 * You'll want to attach the following files to the page this is on:
 * <ul>
 *     <li>form-elements-buttons.css</li>
 *     <li>tables-trees.css</li>
 *     <li>messages.css</li>
 *     <li>shared-search.css</li>
 *     <li>workspace.css</li>
 *     <li>UserProfileExample1.css *</li>
 *     <li>jquery.Jcrop.css *</li>
 *     <li>img-cropper.css *</li>
 * </ul>
 * The correct version of Jquery comes packaged with Jcrop.
 * <ul>
 *     <li>jquery.min.js *</li>
 *     <li>jquery.color.js *</li>
 *     <li>jquery.Jcrop.min.js *</li>
 *     <li>dnd-file.js *</li>
 *     <li>img-cropper.js *</li>
 * </ul>
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.example.app.UserProfileApp",
    i18n = {
        @I18N(symbol = "SearchSupplier.Name", l10n = @L10N("User Profile")),
        @I18N(symbol = "SearchSupplier.Description", l10n = @L10N("Default User Profile Search"))
    }
)
@Configurable
public class DemoUserProfileApp extends SearchUIApp
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(DemoUserProfileApp.class);

    /** Data Access Object. */
    @Autowired
    private DemoUserProfileDAO _demoUserProfileDAO;
    /** Site Loader. */
    @Autowired
    private SiteLoader _siteLoader;


    /**
     * Create an instance of the application for spring.
     */
    public DemoUserProfileApp()
    {
        super(null);
    }

    /**
     * Create an instance of the application.
     *
     * @param miwtSession the session.
     */
    public DemoUserProfileApp(MIWTSession miwtSession)
    {
        super(miwtSession);
        // Set the default application name used in toString() and some other circumstances.
        // Since internationalization (i18n) is not a concern for our application, we use the
        /// TextSources.createText method as a placeholder in case we change our mind about i18n later.
        /// This will allow you to quickly / programmatically find text needing i18n and l10n (localization).
        defAppName = createText("User Profile Example App");
    }


    @Override
    protected void userStarted()
    {
        // All startup code for an application goes here.
        // You do *not* need to call super.userStarted() if extending MIWTApplication directly - we are not.
        super.userStarted();

        setupSearch();
    }

    /**
     * Setup the search UI.
     */
    public void setupSearch()
    {
        SearchSupplierImpl searchSupplier = createSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(new SearchUIOperationHandlerImpl()
        {
            @Override
            public boolean supportsOperation(SearchUIOperation operation)
            {
                switch (operation)
                {
                    case select:
                    case edit:
                    case delete:
                    case view:
                    case add:
                        return true;
                    case copy:
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
                    case view:
                        viewUserProfiles(context);
                        break;

                    case edit:
                        editUserProfiles(context);
                        break;

                    case delete:
                        deleteUserProfiles(context);
                        break;

                    case add:
                        addUserProfile();
                        break;

                    default:
                        throw new AssertionError("Unhandled operation: " + context.getOperation());
                }
            }
        });
        Options options = new Options("User Profile");
        ReflectiveAction addAction = CommonActions.ADD.defaultAction();
        addAction.setTarget(this, "addUserProfile");
        options.addEntityAction(addAction);
        options.addSearchSupplier(searchSupplier);
        SearchUIImpl searchUI = new SearchUIImpl(options);
        setSearchUI(searchUI);
    }


    /**
     * Add a new user profile.
     */
    void addUserProfile()
    {
        final DemoUserProfile demoUserProfile = new DemoUserProfile();
        demoUserProfile.setSite((CmsSite) _siteLoader.getOperationalSite());
        setupEditor(demoUserProfile);
    }

    /**
     * Setup the editor by creating the View and wiring in the necessary
     * controls for persistence with our DAO.
     *
     * @param demoUserProfile the user profile.
     */
    void setupEditor(final DemoUserProfile demoUserProfile)
    {
        getActiveSearchUI().getWorkspace().addUITask(new AbstractUITask("upe#" + demoUserProfile.getId())
        {
            @Override
            public TextSource getLabel()
            {
                return createText("User Profile - " + _getName(
                    EntityRetriever.getInstance().reattachIfNecessary(demoUserProfile)
                ));
            }

            @Override
            public Component createTaskUI(LocaleContext localeContext)
            {

                ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
                ReflectiveAction cancelAction = CommonActions.CANCEL.defaultAction();
                PropertyEditor<DemoUserProfile> propertyEditor = new PropertyEditor<>();
                propertyEditor.setTitle(new Label(createText("User Profile")).withHTMLElement(HTMLElement.h2));
                propertyEditor.setValueEditor(new DemoUserProfileEditor());
                propertyEditor.setPersistenceActions(saveAction, cancelAction);

                // We are creating the action logic after creating the UI components
                /// so we can reference the component variables in the actions.
                cancelAction.setActionListener(event -> {
                    // When the cancelAction is executed, close the editor UI
                    /// the switch to the viewer UI.

                    // If we want to warn the user about unsaved changes, then
                    /// we could check the ModificationState of the editor before
                    /// proceeding.

                    propertyEditor.close();
                    setupViewer(EntityRetriever.getInstance().reattachIfNecessary(demoUserProfile));
                });

                saveAction.setActionListener(event -> {
                    // If the user presses one of the save buttons, then
                    /// validate that the data captured in the UI is valid
                    /// before attempting to save the changes.
                    /// Any validation errors will be added to the notifiable.
                    if (propertyEditor.validateValue())
                    {
                        boolean success = false;
                        try
                        {
                            if (propertyEditor.getModificationState().isModified())
                            {
                                // Editor indicated data is valid, commit the UI
                                /// data to the UserProfile and attempt to save it.
                                final DemoUserProfile commitValue = propertyEditor.commitValue();
                                assert commitValue != null;
                                _demoUserProfileDAO.saveUserProfile(commitValue);
                                success = true;
                            }
                            else success=true;
                        }
                        catch (Exception e)
                        {
                            _logger.error("Unexpected error committing changes", e);
                            // Let the user know that something bad happened.
                            propertyEditor.getNotifiable()
                                .sendNotification(NotificationImpl.create(e, "I'm sorry, I was unable to save."));
                        }

                        if(success)
                        {
                            // Save was successful. We're executing the cancelAction
                            /// since it switches us to the viewer UI.
                            cancelAction.actionPerformed(event);
                        }
                    }
                });

                return propertyEditor;
            }
        });
    }

    /**
     * Setup the viewer by wiring in the necessary controls to switch to
     * the editor View.
     *
     * @param demoUserProfile the user profile.
     */
    void setupViewer(final DemoUserProfile demoUserProfile)
    {
        // This is very similar to setupEditor - just a little less complex.
        getActiveSearchUI().getWorkspace().addUITask(new AbstractUITask("up#" + demoUserProfile.getId())
        {
            @Override
            public TextSource getLabel()
            {
                return createText("User Profile - " + _getName(
                    EntityRetriever.getInstance().reattachIfNecessary(demoUserProfile)
                ));
            }

            @Override
            public Component createTaskUI(LocaleContext localeContext)
            {
                final Container viewerWrapper = Container.of(HTMLElement.div, "property-wrapper");
                DemoUserProfileViewer viewer = new DemoUserProfileViewer(demoUserProfile);

                // We are only putting actions at the top of this UI - no bottom actions -
                /// because the viewer UI is much shorter than the editor UI and is likely
                /// to fit on most screens so the edit button should always be visible.
                PushButton editBtn = CommonActions.EDIT.push();
                Container actions = Container.of(HTMLElement.div, "actions top persistence-actions", editBtn);
                viewer.add(actions);

                viewerWrapper.add(new Label(createText("User Profile")).withHTMLElement(HTMLElement.h1));
                viewerWrapper.add(viewer);

                editBtn.addActionListener(event -> {
                    viewerWrapper.close();
                    setupEditor(EntityRetriever.getInstance().reattachIfNecessary(demoUserProfile));
                });
                return viewerWrapper;
            }
        });
    }


    /**
     * Create a search supplier for UserProfiles that you can modify.
     * This supplier has the name, description, and
     * @return a new supplier.
     */
    static SearchSupplierImpl createSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("UserProfile Search");
        searchModel.setDisplayName(DemoUserProfileAppLOK.SEARCHSUPPLIER_NAME());
        searchModel.getConstraints().add(new SimpleConstraint("keyword")
            {
                @Override
                public void addCriteria(QLBuilder builder, Component constraintComponent)
                {

                    final Object value = getValue(constraintComponent);
                    if (!shouldReturnConstraintForValue(value) || value == null)
                        return;
                    builder.startGroup(JunctionOperator.OR);
                    final String lowerCaseValue = value.toString().toLowerCase();
                    final String valueParamNameStartsWith = builder.param("valueStart", lowerCaseValue + '%');
                    final String valueParamNameContains = builder.param("valueStart", '%' + lowerCaseValue + '%');
                    final JoinedQLBuilder nameBuilder = builder.createJoin(QLBuilder.JoinType.LEFT, "name", "name");
                    nameBuilder.startGroup(JunctionOperator.OR);
                    nameBuilder.formatCriteria("LOWER(%s.first) LIKE %s", nameBuilder.getAlias(), valueParamNameStartsWith);
                    nameBuilder.formatCriteria("LOWER(%s.last) LIKE %s", nameBuilder.getAlias(), valueParamNameStartsWith);
                    nameBuilder.endGroup();
                    builder.formatCriteria("LOWER(%s.phoneNumber) LIKE %s", builder.getAlias(), valueParamNameStartsWith);
                    builder.formatCriteria("LOWER(%s.emailAddress) LIKE %s", builder.getAlias(), valueParamNameStartsWith);
                    builder.formatCriteria("LOWER(%s.aboutMeProse) LIKE %s", builder.getAlias(), valueParamNameContains);
                    builder.endGroup();
                }
            }
            .withLabel(CommonButtonText.KEYWORD)
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("name")
                .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.NAME))
                .withTableCellRenderer(new CustomCellRenderer("", input -> {
                    DemoUserProfile demoUserProfile = (DemoUserProfile) input;
                    if (demoUserProfile == null || demoUserProfile.getName() == null) return null;
                    return _getName(demoUserProfile);
                }))
                .withOrderBy(new QLOrderByImpl()
                {
                    @Override
                    public void updateOrderBy(QLBuilder builder, SortOrder sortOrder)
                    {
                        final String orderOperator = getOrderOperator(sortOrder);
                        builder.setOrderBy("name.first " + orderOperator + ", name.last " + orderOperator);
                    }
                })
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("emailAddress")
                .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.EMAIL))
                .withTableCellRenderer(new CustomCellRenderer("", input -> {
                    DemoUserProfile demoUserProfile = (DemoUserProfile) input;
                    if (demoUserProfile == null || demoUserProfile.getEmailAddress() == null) return null;
                    return demoUserProfile.getEmailAddress();
                }))
                .withOrderBy(new QLOrderByImpl("emailAddress"))
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("lastModTime")
                .withTableColumn(new TimeAuditableColumn(TimeAuditableColumn.AuditableType.modified_time)
                    .withColumnName(CommonColumnText.MODIFIED).withComparator(null)
                )
                .withTableCellRenderer(DateFormatLabel.createFriendlyInstance(null))
                .withOrderBy(new QLOrderByImpl("lastModTime"))
        );
        final ActionColumn actionColumn = new ActionColumn();
        actionColumn.setIncludeCopy(false);
        searchModel.getResultColumns().add(actionColumn);

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(DemoUserProfileAppLOK.SEARCHSUPPLIER_NAME());
        searchSupplier.setDescription(DemoUserProfileAppLOK.SEARCHSUPPLIER_DESCRIPTION());
        searchSupplier.setBuilderSupplier(() -> new QLBuilderImpl(DemoUserProfile.class, "userProfile"));
        searchSupplier.setSearchModel(searchModel);



        return searchSupplier;
    }

    /**
     * Get the name.
     * @param demoUserProfile the user profile.
     * @return  the name or an empty string.
     */
    private static String _getName(DemoUserProfile demoUserProfile)
    {
        final Name name = demoUserProfile.getName();
        if(name != null)
        {
            final boolean lastNameExists = !isEmptyString(name.getLast());
            final boolean firstNameExists = !isEmptyString(name.getFirst());
            if (firstNameExists)
            {
                if (lastNameExists)
                    return name.getFirst() + ' ' + name.getLast();
                else
                    return name.getFirst();
            }
            else if (lastNameExists)
                return name.getLast();
        }
        return "";
    }

    /**
     * Delete the requested user profiles.
     * @param context the context.
     */
    void deleteUserProfiles(SearchUIOperationContext context)
    {
        final SearchUI searchUI = context.getSearchUI();
        switch (context.getDataContext())
        {
            case new_instance:
            case lead_selection:
                DemoUserProfile demoUserProfile = context.getData();
                if(demoUserProfile != null)
                    _demoUserProfileDAO.deleteUserProfile(demoUserProfile);
                break;
            case selection:
                final QLBuilder selectionQLBuilder = searchUI.getSelectionQLBuilder();
                if(selectionQLBuilder == null)
                    _demoUserProfileDAO.deleteUserProfiles(searchUI.getSelection());
                else
                    _demoUserProfileDAO.deleteUserProfiles(selectionQLBuilder);
                break;
            case search:
                final QLBuilder currentSearchQLBuilder = searchUI.getCurrentSearchQLBuilder();
                _demoUserProfileDAO.deleteUserProfiles(currentSearchQLBuilder);
                _logger.warn("Not viewing all profiles that match search.");
                break;
            default:break;
        }
    }

    /**
     * Edit the requested user profiles.
     * @param context the context.
     */
    void editUserProfiles(SearchUIOperationContext context)
    {
        switch (context.getDataContext())
        {

            case new_instance:
            case lead_selection:
                DemoUserProfile demoUserProfile = context.getData();
                if(demoUserProfile != null)
                    setupEditor(demoUserProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
            default:break;
        }
    }

    /**
     * View the requested user profiles.
     * @param context the context.
     */
    void viewUserProfiles(SearchUIOperationContext context)
    {
        switch (context.getDataContext())
        {
            case new_instance:
            case lead_selection:
                DemoUserProfile demoUserProfile = context.getData();
                if(demoUserProfile != null)
                    setupViewer(demoUserProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
            default:break;
        }
    }
}


