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

import java.text.SimpleDateFormat;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.util.CommonActions;

import static net.proteusframework.core.locale.TextSources.createText;

/**
 * A property viewer for viewing {@link Widget}s
 *
 * @author Conner Rocole (crocole@venturetech.net)
 */
public class WidgetPropertyViewer extends PropertyViewer
{
    final Container _valueViewer;
    final Widget _widget;

    /**
     * Constructor
     * @param widget - widget
     */
    public WidgetPropertyViewer(Widget widget)
    {
        super();
        _widget = widget;
        setValueViewer(_valueViewer = new Container());
    }

    @Override
    public void init()
    {
        super.init();

        //You could use NavigationActions if integrating with PUN stack
        ReflectiveAction edit = CommonActions.EDIT.defaultAction();
        edit.setActionListener(ev -> {
            //Take the necessary action to navigate to the edit page or change an existing UI to edit mode.
        });
        setPersistenceActions(edit);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        _valueViewer.addClassName("widget");
        _valueViewer.add(Container.of("prop",
            createText("Name"),
            new Label(createText(_widget.getName()), "val name").withHTMLElement(HTMLElement.div)));
        _valueViewer.add(Container.of("prop",
            createText("Description"),
            new Label(createText(_widget.getDescription()), "val description").withHTMLElement(HTMLElement.div)));
        _valueViewer.add(Container.of("prop",
            createText("Count"),
            new Label(createText(String.valueOf(_widget.getCount())), "val count").withHTMLElement(HTMLElement.div)));
        _valueViewer.add(Container.of("prop",
            createText("Date"),
            new Label(createText(""), "val date").withHTMLElement(HTMLElement.div)));
        _valueViewer.add(Container.of("prop",
            createText("Flag"),
            new Label(createText(String.valueOf(_widget.getFlag())), "val flag").withHTMLElement(HTMLElement.div)));
    }
}
