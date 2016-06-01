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

package com.example.app.ui.text;

import com.example.app.model.text.Note;
import com.example.app.model.text.NoteDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.beans.PropertyChangeListener;

import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * Property Editor for Note
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 2/4/16 9:40 AM
 */
@Configurable
public class NotePropertyEditor extends PropertyEditor<Note>
{
    /** Bound property fired when the value is successfully saved */
    public static final String PROP_VALUE_SAVED = "value-saved";

    @Autowired
    private NoteDAO _noteDAO;

    /**
     * Instantiates a new instance of NotePropertyEditor
     */
    public NotePropertyEditor()
    {
        super();
        setValueEditor(new NoteValueEditor());
    }

    @Override
    public void init()
    {
        super.init();

        Closer closer = this.new Closer();

        ReflectiveAction save = CommonActions.SAVE.defaultAction();
        save.setActionListener(ev -> {
            if (persist(input -> {
                assert input != null;
                _noteDAO.saveNote(input);
                fireValueSaved(input);
                return Boolean.TRUE;
            }))
            {
                closer.actionPerformed(ev);
            }
        });

        ReflectiveAction cancel = CommonActions.CANCEL.defaultAction();
        cancel.setActionListener(closer);

        setPersistenceActions(save, cancel);
    }

    private void fireValueSaved(Note saved)
    {
        firePropertyChange(PROP_VALUE_SAVED, null, saved);
    }

    /**
     * Add a listener for when the value is successfully saved
     *
     * @param listener the listener
     *
     * @return this
     */
    public NotePropertyEditor withValueSaveListener(PropertyChangeListener listener)
    {
        addPropertyChangeListener(PROP_VALUE_SAVED, listener);
        return this;
    }
}
