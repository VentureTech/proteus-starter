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

package com.example.app.model.text;

import org.jetbrains.annotations.Contract;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.io.ObjectStreamException;
import java.io.Serializable;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.spring.ApplicationContextUtils;

/**
 * DAO for Notes
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 2/4/16 9:32 AM
 */
@SuppressWarnings("unused")
@Repository
@Lazy
public class NoteDAO extends DAOHelper implements Serializable
{
    private static final long serialVersionUID = -3749855189704376208L;

    /**
     * Delete the given Note from the database via a DELETE operation
     *
     * @param note the note to delete
     */
    public void deleteNote(Note note)
    {
        doInTransaction(session -> {
            session.delete(note);
        });
    }

    /**
     * Save the given Note into the database via a SAVE_OR_UPDATE operation
     *
     * @param note the note to save
     */
    public void saveNote(Note note)
    {
        doInTransaction(session -> {
            session.saveOrUpdate(note);
        });
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(NoteDAO.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}
