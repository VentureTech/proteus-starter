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

import java.util.Arrays;
import java.util.Date;

import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.editor.BooleanValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CalendarValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;

import static net.proteusframework.core.locale.TextSources.createText;

/**
 * A property editor for {@link Widget}s
 *
 * @author Conner Rocole (crocole@venturetech.net)
 */
public class WidgetPropertyEditor extends PropertyEditor<Widget>
{
    private final CompositeValueEditor<Widget> _valueEditor;

    /** Constructor */
    public WidgetPropertyEditor()
    {
        super();
        setValueEditor(_valueEditor = new CompositeValueEditor<>(Widget.class));
    }

    @Override
    public void init()
    {
        super.init();

        addClassName("widget");

        ReflectiveAction save = CommonActions.SAVE.defaultAction();
        save.setActionListener(ev -> {
            if(persist(input -> {
                //Normally would save here.
                return true;
            }))
            {
                //Normally you would close the UI.
            }
        });

        ReflectiveAction cancel = CommonActions.CANCEL.defaultAction();
        cancel.setActionListener(ev -> {
            //Normally you would close the UI.
        });

        setPersistenceActions(save, cancel);

        _valueEditor.addEditorForProperty(() -> new TextEditor(CommonColumnText.NAME, null), Widget::getName, Widget::setName);
        _valueEditor.addEditorForProperty(() -> {
            TextEditor editor = new TextEditor(CommonColumnText.DESCRIPTION, null);
            editor.setDisplayHeight(4);
            return editor;
        }, Widget::getDescription, Widget::setDescription);
        _valueEditor.addEditorForProperty(() -> new ComboBoxValueEditor<>(createText("Count"), Arrays.asList(1,2,3,4,5), null),
            Widget::getCount, Widget::setCount);
        _valueEditor.addEditorForProperty(() -> new CalendarValueEditor(createText("Date"), new Date(), null),
            Widget::getDate, Widget::setDate);
        _valueEditor.addEditorForProperty(() -> new BooleanValueEditor(createText("Flag"), false),
            Widget::getFlag, Widget::setFlag);
    }
}
