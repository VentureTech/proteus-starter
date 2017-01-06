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

package com.example.app.ui.repository;

import com.example.app.repository.model.Repository;
import com.example.app.repository.model.RepositoryItem;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;

/**
 * {@link CompositeValueEditor} implementation for a {@link RepositoryItem} implementation
 *
 * @param <RI> the subclass of RepositoryItem being edited
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/23/15 4:17 PM
 */
public abstract class RepositoryItemValueEditor<RI extends RepositoryItem> extends CompositeValueEditor<RI>
{
    /** Entity Retriever */
    protected EntityRetriever entityRetriever;
    private Repository _owner;

    /**
     * Instantiates a new instance of RepositoryItemValueEditor
     *
     * @param clazz the class of the RepositoryItem for this editor
     */
    public RepositoryItemValueEditor(Class<RI> clazz)
    {
        super(clazz);

        addClassName("repository-item value-editor");
    }

    /**
     * Get the Owner of this RepositoryItem being edited
     *
     * @return the Owner Repository
     */
    @Nonnull
    public Repository getOwner()
    {
        return entityRetriever.reattachIfNecessary(_owner);
    }

    /**
     * Set the Owner of this RepositoryItem being edited
     *
     * @param owner the Owner Repository
     */
    public void setOwner(@Nonnull Repository owner)
    {
        _owner = owner;
    }

    @Nullable
    @Override
    public RI getUIValue(Level logErrorLevel)
    {
        RI result = super.getUIValue(logErrorLevel);
        return Optional.ofNullable(result).orElseThrow(() -> new IllegalStateException("Result RepositoryItem was null."));
    }

    @Nullable
    @Override
    public RI commitValue() throws MIWTException
    {
        RI result = super.commitValue();
        return Optional.ofNullable(result).orElseThrow(() -> new IllegalStateException("Result RepositoryItem was null."));
    }

    /**
     * Set the EntityRetriever for this RepositoryItemValueEditor
     *
     * @param er the EntityRetriever
     */
    @Autowired
    public void setEntityRetriever(EntityRetriever er)
    {
        entityRetriever = er;
    }
}
