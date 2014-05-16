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

import com.example.app.model.UserProfile;
import com.example.app.model.UserProfileDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.i2rd.cms.bean.MIWTBeanConfig;
import com.i2rd.cms.miwt.TimeAuditableColumn;

import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.ui.mcolumn.FixedValueColumn;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.MIWTSession;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.DateFormatLabel;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplier;
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
import static net.proteusframework.ui.search.SearchUIImpl.Options;

/**
 * Example application to demonstrate how to create and handle
 * UI views.
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
@MIWTBeanConfig(displayName = "User Profile Example Application",
    applicationClass = UserProfileApp.class)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserProfileApp extends SearchUIApp
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(UserProfileApp.class);

    /** Mock Data Access Object. */
    @Autowired
    private UserProfileDAO _userProfileDAO;


    /**
     * Create an instance of the application for spring.
     */
    public UserProfileApp()
    {
        super(null);
    }

    /**
     * Create an instance of the application.
     *
     * @param session the session.
     */
    public UserProfileApp(MIWTSession session)
    {
        super(session);
        // Set the default application name used in toString() and some other circumstances.
        // Since internationalization (i18n) is not a concern for our application, we use the
        /// TextSources.create method as a placeholder in case we change our mind about i18n later.
        /// This will allow you to quickly / programmatically find text needing i18n and l10n (localization).
        this.defAppName = TextSources.create("User Profile Example App");
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
        SearchSupplier searchSupplier = createSearchSupplier();
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
        setupEditor(new UserProfile());
    }

    /**
     * Setup the editor by creating the View and wiring in the necessary
     * controls for persistence with our DAO.
     *
     * @param userProfile the user profile.
     */
    void setupEditor(final UserProfile userProfile)
    {
        // The editor View's responsibility is to provide data capture. It's the
        /// implementer's (this application) responsibility to handle persistence. We'll
        /// setup the editor view by adding a title and buttons to save / cancel changes.

        // To facilitate styling of the content, we use a standard structure for the HTML we output.
        /// Starter stylesheets are provided for the standard HTML structure, so your UI can look good right away.
        /// Refer to https://developer.i2rd.com/standards/property-viewers for detailed information
        /// on HTML structure and the starter CSS.

        // We're going to wrap the property editor in a DIV so our title is outside the property editor.
        /// This is part of the standard HTML structure and Container.of(...) is a common way to create
        /// containers for this structure. The syntax encourages you to specify the HTMLElement
        /// as well as specify a class name.
        final Container editorWrapper = Container.of(HTMLElement.div, "property-wrapper");
        UserProfileEditor editor = new UserProfileEditor(userProfile);
        // CommonActions provides code to create many standard buttons. Its use is recommended
        /// since it takes care of common boilerplate code and allows changes to be made to all
        /// buttons created within a context i.e. an application.
        PushButton saveBtnTop = CommonActions.SAVE.push();
        PushButton cancelBtnTop = CommonActions.CANCEL.push();
        // We're going to create buttons at the top of the property editor and
        /// the bottom of the property editor.
        Container actions = Container.of(HTMLElement.div, "actions top persistence-actions", saveBtnTop, cancelBtnTop);
        // We're adding the actions to the editor *before* it is initialized,
        /// so the "actions" container will appear at the top of the editor.
        editor.add(actions);
        // MessageContainers are used to display messages. We're going to use
        /// it to display validation errors when the user tries to save changes.
        MessageContainer notifiable = new MessageContainer();
        // Layout the content of the editorWrapper: Title, Messages, then the editor.
        editorWrapper.add(new Label(TextSources.create("User Profile")).setHTMLElement(HTMLElement.h1));
        editorWrapper.add(notifiable);
        editorWrapper.add(editor);


        // We are creating the action logic after creating the UI components
        /// so we can reference the component variables in the actions.
        final ActionListener cancelAction = event -> {
            // When the cancelAction is executed, close the editor UI
            /// the switch to the viewer UI.

            // If we want to warn the user about unsaved changes, then
            /// we could check the ModificationState of the editor before
            /// proceeding.

            editorWrapper.close();
            setupViewer(EntityRetriever.getInstance().reattachIfNecessary(userProfile));
        };
        final ActionListener saveAction = event -> {
            // If the user presses one of the save buttons, then
            /// validate that the data captured in the UI is valid
            /// before attempting to save the changes.
            /// Any validation errors will be added to the notifiable.
            notifiable.clearNotifications();
            if (editor.validateUIValue(notifiable))
            {
                try
                {
                    if (editor.getModificationState().isModified())
                    {
                        // Editor indicated data is valid, commit the UI
                        /// data to the UserProfile and attempt to save it.
                        final UserProfile commitValue = editor.commitValue();
                        assert commitValue != null;
                        _userProfileDAO.saveUserProfile(commitValue);
                        // Save was successful. We're executing the cancelAction
                        /// since it switches us to the viewer UI.
                        cancelAction.actionPerformed(event);
                    }
                }
                catch (MIWTException e)
                {
                    _logger.error("Unexpected error committing changes", e);
                    // Let the user know that something bad happened.
                    notifiable.sendNotification(NotificationImpl.create(e, "I'm sorry, we Unable to save"));
                }
            }
        };

        // A Component can only be in the UI in one place at a time.
        /// We need to create new buttons for the action buttons at the
        /// bottom of the editor.
        PushButton saveBtnBottom = CommonActions.SAVE.push();
        PushButton cancelBtnBottom = CommonActions.CANCEL.push();
        actions = Container.of(HTMLElement.div, "actions bottom persistence-actions", saveBtnBottom, cancelBtnBottom);

        // Since the UserProfileEditor was already initialized above <_appFrame.add(editorWrapper)>
        /// we can add the actions and they will appear at the bottom
        /// of the editor. This behavior is dependent on the implementation.
        /// Since we are writing both implementations, it is safe to make
        /// that assumption. If we were using reusable code, we wouldn't
        /// want to make that assumption. It would need to be specified in the
        /// contract of the code for us to do this style of UI building.
        editor.add(actions);

        // Add action listeners to action buttons.
        cancelBtnTop.addActionListener(cancelAction);
        saveBtnTop.addActionListener(saveAction);
        cancelBtnBottom.addActionListener(cancelAction);
        saveBtnBottom.addActionListener(saveAction);

        if (isEmptyString(userProfile.getName().getFirst()))
        {
            // Since the First name is required we can assume this is the initial case and
            // hide the cancel buttons.
            cancelBtnTop.setVisible(false);
            cancelBtnBottom.setVisible(false);
        }
    }

    /**
     * Setup the viewer by wiring in the necessary controls to switch to
     * the editor View.
     *
     * @param userProfile the user profile.
     */
    void setupViewer(final UserProfile userProfile)
    {
        // This is very similar to setupEditor - just a little less complex.
        getActiveSearchUI().getWorkspace().addUITask(new AbstractUITask("up#" + userProfile.getId())
        {
            @Override
            public LocalizedText getLabel(LocaleContext localeContext)
            {
                return new LocalizedText("User Profile - " + _getName(
                    EntityRetriever.getInstance().reattachIfNecessary(userProfile)
                ));
            }

            @Override
            public Component createTaskUI(LocaleContext localeContext)
            {
                final Container viewerWrapper = Container.of(HTMLElement.div, "property-wrapper");
                UserProfileViewer viewer = new UserProfileViewer(userProfile);

                // We are only putting actions at the top of this UI - no bottom actions -
                /// because the viewer UI is much shorter than the editor UI and is likely
                /// to fit on most screens so the edit button should always be visible.
                PushButton editBtn = CommonActions.EDIT.push();
                Container actions = Container.of(HTMLElement.div, "actions top persistence-actions", editBtn);
                viewer.add(actions);

                viewerWrapper.add(new Label(TextSources.create("User Profile")).setHTMLElement(HTMLElement.h1));
                viewerWrapper.add(viewer);

                editBtn.addActionListener(event -> {
                    viewerWrapper.close();
                    setupEditor(EntityRetriever.getInstance().reattachIfNecessary(userProfile));
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
    SearchSupplier createSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("UserProfile Search");
        searchModel.setDisplayNameKey(UserProfileAppLOK.SEARCHSUPPLIER_NAME());
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
                    UserProfile userProfile = (UserProfile) input;
                    if (userProfile == null || userProfile.getName() == null) return null;
                    return _getName(userProfile);
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
                    UserProfile userProfile = (UserProfile) input;
                    if (userProfile == null || userProfile.getEmailAddress() == null) return null;
                    return userProfile.getEmailAddress();
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

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(UserProfileAppLOK.SEARCHSUPPLIER_NAME());
        searchSupplier.setDescription(UserProfileAppLOK.SEARCHSUPPLIER_DESCRIPTION());
        searchSupplier.setBuilderSupplier(() -> new QLBuilderImpl(UserProfile.class, "userProfile"));
        searchSupplier.setSearchModel(searchModel);
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


        return searchSupplier;
    }

    /**
     * Get the name.
     * @param userProfile the user profile.
     * @return  the name or an empty string.
     */
    private static String _getName(UserProfile userProfile)
    {
        final Name name = userProfile.getName();
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
                UserProfile userProfile = context.getData();
                if(userProfile != null)
                    _userProfileDAO.deleteUserProfile(userProfile);
                break;
            case selection:
                final QLBuilder selectionQLBuilder = searchUI.getSelectionQLBuilder();
                if(selectionQLBuilder == null)
                    _userProfileDAO.deleteUserProfiles(searchUI.getSelection());
                else
                    _userProfileDAO.deleteUserProfiles(selectionQLBuilder);
                break;
            case search:
                final QLBuilder currentSearchQLBuilder = searchUI.getCurrentSearchQLBuilder();
                _userProfileDAO.deleteUserProfiles(currentSearchQLBuilder);
                _logger.warn("Not viewing all profiles that match search.");
                break;
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
                UserProfile userProfile = context.getData();
                if(userProfile != null)
                    setupEditor(userProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
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
                UserProfile userProfile = context.getData();
                if(userProfile != null)
                    setupViewer(userProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
        }
    }
}


