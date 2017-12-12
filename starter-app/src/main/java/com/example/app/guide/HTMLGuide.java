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

package com.example.app.guide;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.ui.miwt.component.Calendar;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.LinkComponent;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;

import static com.example.app.guide.HTMLGuideLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.core.notification.Notifications.*;

/**
 * The HTML Guide is an MIWT app that simply displays a collection of commonly used UI elements. This UI has many possible uses:
 *
 *  - A "starter" page for new projects that can be modified to create a unique standard for the particular project.
 *  - A base for frontend developers to begin implementing a design very early in the development process that java developers can
 *      code against.
 *  - A reference for developers to ensure the UIs they create are generating standard HTML structure with commonly used CSS class
 *      names.
 *  - New UIs or Application Functions that a developer creates can be presented to clients for testing with a design mostly in
 *      place.
 *
 * All of these benefits would help move us towards integrating design work throughout the development process to improve user
 * experience. The look and feel of an application plays an important role in its function, and users will be able to provide
 * more valuable feedback when they have a complete UI to use–as opposed to a "functional" UI that will have its design
 * implemented at the end of the project.
 *
 * The HTML Guide is not intended to be used as a functional reference for various Proteus APIs. Although it uses various Proteus
 * APIs, it is targeted at showing how they're used to generate a known HTML structure.
 *
 * @author Conner Rocole (crocole@proteus.co)
 */
@I18NFile(
    symbolPrefix = "com.example.app.guide.HTMLGuide",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("HTML Guide")),
        @I18N(symbol = "Primary", l10n = @L10N("Primary")),
        @I18N(symbol = "Secondary", l10n = @L10N("Secondary")),
        @I18N(symbol = "Success", l10n = @L10N("Success")),
        @I18N(symbol = "Info", l10n = @L10N("Info")),
        @I18N(symbol = "Warning", l10n = @L10N("Warning")),
        @I18N(symbol = "Danger", l10n = @L10N("Danger")),
        @I18N(symbol = "Link", l10n = @L10N("Link")),
        @I18N(symbol = "Default", l10n = @L10N("Default"))
    }
)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(HTMLGuide.RESOURCE_NAME)
public class HTMLGuide extends MIWTPageElementModelContainer
{
    /**
     * Simple informational message block for describing elements of the template.
     */
    private enum Callout
    {
        /** Info */
        INFO("callout-info"),
        /** Warning */
        WARNING("callout-warning"),
        /** Danger */
        DANGER("callout-danger")
        ;

        /** CSS class name */
        final private String _className;

        /**
         * Constructor
         * @param className - CSS class name.
         */
        Callout(String className)
        {
            _className = className;
        }

        /**
         * Get a {@link Container} containing a callout message.
         * @param heading - The title of the callout (Pass null if you don't want one).
         * @param text - The informational text of the callout (Pass null if you don't want one).
         * @return the generated Container.
         */
        Container getContainer(@Nullable String heading, @Nullable String text)
        {
            final Container callout = new Container();
            callout.addClassName("callout");
            callout.addClassName(_className);
            if(heading != null)
            {
                final Label h = new Label(createText(heading));
                h.setHTMLElement(HTMLElement.h4);
                callout.add(h);
            }
            if(text != null)
            {
                final Label t = new Label(createText(text));
                t.setHTMLElement(HTMLElement.p);
                callout.add(t);
            }
            return callout;
        }

        /**
         * Get a {@link Container} containing a callout message.
         * @param text - The informational text of the callout.
         * @return the generated Container.
         */
        Container getContainer(@NotNull String text)
        {
            return getContainer(null, text);
        }
    }

    /** Resource Name. */
    public final static String RESOURCE_NAME = "com.providertrust.exclusioncheck.ui.StarterUI";

    /**
     * Constructor
     */
    public HTMLGuide()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.UserManagement);
    }

    @Override
    public void init()
    {
        super.init();
        _setupUI();
    }

    /**
     * Setup the UI
     */
    private void _setupUI()
    {
        removeAllComponents();

        //Headings
        final Label h1 = new Label(createText("Welcome to the HTML Guide!"));
        h1.setHTMLElement(HTMLElement.h1);
        final Label h2 = new Label(createText("Welcome to the HTML Guide!"));
        h2.setHTMLElement(HTMLElement.h2);
        final Label h3 = new Label(createText("Welcome to the HTML Guide!"));
        h3.setHTMLElement(HTMLElement.h3);
        final Label h4 = new Label(createText("Welcome to the HTML Guide!"));
        h4.setHTMLElement(HTMLElement.h4);
        final Label h5 = new Label(createText("Welcome to the HTML Guide!"));
        h5.setHTMLElement(HTMLElement.h5);
        final Label h6 = new Label(createText("Welcome to the HTML Guide!"));
        h6.setHTMLElement(HTMLElement.h6);

        //Alert Messages
        final MessageContainer notifications = new MessageContainer();
        notifications.sendNotification(successNotification(createText("This is a SUCCESS notification.")));
        notifications.sendNotification(infoNotification(createText("This is an INFO notification.")));
        notifications.sendNotification(importantNotification(createText("This is an IMPORTANT notification.")));
        notifications.sendNotification(errorNotification(createText("This is an ERROR notification.")));

        //Buttons
        final PushButton primaryBtn = new PushButton(PRIMARY());
        primaryBtn.addClassName("btn-primary");
        final PushButton secondaryBtn = new PushButton(SECONDARY());
        secondaryBtn.addClassName("btn-secondary");
        final PushButton successBtn = new PushButton(SUCCESS());
        successBtn.addClassName("btn-success");
        final PushButton infoBtn = new PushButton(INFO());
        infoBtn.addClassName("btn-info");
        final PushButton warningBtn = new PushButton(WARNING());
        warningBtn.addClassName("btn-warning");
        final PushButton dangerBtn = new PushButton(DANGER());
        dangerBtn.addClassName("btn-danger");
        final PushButton linkBtn = new PushButton(LINK());
        linkBtn.addClassName("btn-link");

        final PushButton primaryOutlineBtn = new PushButton(PRIMARY());
        primaryOutlineBtn.addClassName("btn-outline-primary");
        final PushButton secondaryOutlineBtn = new PushButton(SECONDARY());
        secondaryOutlineBtn.addClassName("btn-outline-secondary");
        final PushButton successOutlineBtn = new PushButton(SUCCESS());
        successOutlineBtn.addClassName("btn-outline-success");
        final PushButton infoOutlineBtn = new PushButton(INFO());
        infoOutlineBtn.addClassName("btn-outline-info");
        final PushButton warningOutlineBtn = new PushButton(WARNING());
        warningOutlineBtn.addClassName("btn-outline-warning");
        final PushButton dangerOutlineBtn = new PushButton(DANGER());
        dangerOutlineBtn.addClassName("btn-outline-danger");

        final PushButton searchBigBtn = CommonActions.SEARCH.push();
        searchBigBtn.addClassName("btn-primary btn-lg");
        final PushButton searchRegularBtn = CommonActions.SEARCH.push();
        searchRegularBtn.addClassName("btn-info");
        final PushButton searchSmallBtn = CommonActions.SEARCH.push();
        searchSmallBtn.addClassName("btn-info btn-sm");

        final PushButton viewBtn = CommonActions.VIEW.push();
        viewBtn.addClassName("btn-info");

        //Links
        final ResponseURL url = Event.getResponse().createURL();
        LinkComponent link = new LinkComponent(url.getLink(), createText("Here's what a link will look like!"));

        //Standard property editor built manually
        final MessageContainer propEditorNotifications = new MessageContainer();
        final Field name = new Field();
        final Field description = new Field();
        description.setDisplayHeight(4);
        final SimpleListModel<Integer> numbers = new SimpleListModel<>(1, 2, 3, 4, 5);
        final ComboBox count = new ComboBox(numbers);
        final Calendar date = new Calendar();
        final Checkbox flag = new Checkbox();
        final Checkbox viewerFlag = new Checkbox();
        viewerFlag.setEnabled(false);
        final PushButton topSave = CommonActions.SAVE.push();
        final PushButton topCancel = CommonActions.CANCEL.push();
        final PushButton bottomSave = CommonActions.SAVE.push();
        final PushButton bottomCancel = CommonActions.CANCEL.push();

        //Property editor using PropertyEditor API from Proteus
        final WidgetPropertyEditor widgetPropertyEditor = new WidgetPropertyEditor();

        //Property viewer using PropertyViewer API from Proteus
        final Widget widget = new Widget();
        final WidgetPropertyViewer widgetPropertyViewer = new WidgetPropertyViewer(widget);

        /*
         *  Add everything to the page
         */
        //Headings
        add(Callout.INFO.getContainer("Headings", "The following headings show h1 through h6."));
        add(h1);
        add(h2);
        add(h3);
        add(h4);
        add(h5);
        add(h6);

        //Alert Messages
        add(Callout.INFO.getContainer("Notifications", "These are standard notification messages."));
        add(notifications);

        //Buttons
        add(Callout.INFO.getContainer("Buttons", "The following are standard buttons."));
        add(Container.of("actions", primaryBtn, secondaryBtn, successBtn, infoBtn, warningBtn, dangerBtn, linkBtn));
        add(Container.of("actions", primaryOutlineBtn, secondaryOutlineBtn, successOutlineBtn, infoOutlineBtn, warningOutlineBtn,
            dangerOutlineBtn));
        add(Container.of("actions", searchBigBtn));
        add(Container.of("actions", searchRegularBtn));
        add(Container.of("actions", searchSmallBtn));
        add(Container.of("action-column actions", viewBtn));

        //Links
        add(Callout.INFO.getContainer("Links", "This shows how links are styled."));
        add(link);

        //Standard property editor built manually
        add(Callout.INFO.getContainer("Property Editor", "This is a standard property editor built manually."));
        add(Container.of("prop-wrapper some-class-specific-to-property-set prop-editor",
            Container.of(HTMLElement.header, "prop-header",
                Container.of("prop-header-title",
                    Container.of(HTMLElement.h3, "title", createText("Optional Title of Property Editor"))
                ),
                Container.of("prop-header-actions",
                    Container.of("actions top", topSave, topCancel)
                )
            ),
            propEditorNotifications,
            Container.of("prop-body",
                Container.of("prop name", CommonColumnText.NAME, name.addClassName("val")),
                Container.of("prop description", CommonColumnText.DESCRIPTION, description.addClassName("val")),
                Container.of("prop type", createText("Count"), count.addClassName("val")),
                Container.of("prop check", createText("Date"), date.addClassName("val")),
                Container.of("prop description", createText("Flag"), flag.addClassName("val"))
            ),
            Container.of(HTMLElement.footer, "prop-footer",
                Container.of("prop-footer-actions",
                    Container.of("actions bottom", bottomSave, bottomCancel)
                )
            )
        ));

        //Standard property viewer built manually
        add(Callout.INFO.getContainer("Property Viewer", "This is a standard property viewer built manually."));
        add(Container.of("prop-wrapper some-class-specific-to-property-set prop-viewer",
            Container.of(HTMLElement.header, "prop-header",
                Container.of("prop-header-title",
                    Container.of(HTMLElement.h3, "title", createText("Optional Title of Property Viewer"))
                ),
                Container.of("prop-header-actions",
                    Container.of("actions top", topSave, topCancel)
                )
            ),
            propEditorNotifications,
            Container.of("prop-body",
                Container.of("prop name", CommonColumnText.NAME, new Label(createText(widget.getName()), "val")),
                Container.of("prop description", CommonColumnText.DESCRIPTION, new Label(createText(widget.getDescription()),
                    "val")),
                Container.of("prop type", createText("Count"), new Label(createText(String.valueOf(widget.getCount())), "val")),
                Container.of("prop check", createText("Date"), new Label(createText(""), "val")),
                Container.of("prop description", createText("Flag"), viewerFlag.addClassName("val"))
            ),
            Container.of(HTMLElement.footer, "prop-footer",
                Container.of("prop-footer-actions",
                    Container.of("actions bottom", bottomSave, bottomCancel)
                )
            )
        ));

        //Property editor using PropertyEditor API from Proteus
        add(Callout.INFO.getContainer("Property Editor", "This is a standard property editor built using the PropertyEditor API."));
        add(widgetPropertyEditor);

        //Property viewer using PropertyViewer API from Proteus
        add(Callout.INFO.getContainer("Property Viewer", "This is a standard property viewer built using the PropertyViewer API."));
        add(widgetPropertyViewer);
    }
}
