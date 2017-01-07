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

package com.example.app.repository.ui;

import com.example.app.repository.model.Repository;
import com.example.app.repository.model.ResourceRepositoryItem;
import com.example.app.resource.model.Resource;
import com.example.app.support.service.ArrayCollector;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.ui.column.DataColumnTable;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.data.Column;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.miwt.validation.ValidatorUtil;

import static com.example.app.profile.ui.UIText.*;
import static com.example.app.repository.ui.ResourceSelectorEditorLOK.ERROR_MESSAGE_AT_LEAST_ONE_RESOURCE_REQUIRED_FMT;

/**
 * {@link ValueEditor} implementation for selecting a list of ResourceRepositoryItems
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/8/16 4:33 PM
 */
@I18NFile(symbolPrefix = "com.example.app.repository.ui.ResourceSelectorEditor",
    i18n = @I18N(symbol = "Error Message At Least One Resource Required FMT",
    l10n = @L10N("You must select at least one {0}.")))
@Configurable
public class ResourceSelectorEditor extends Container implements ValueEditor<List<ResourceRepositoryItem>>
{
    /** Bound property fired when editor editable property changes */
    public static final String PROP_EDITABLE = "editable";
    /** Bound property fired when value of editor changes */
    public static final String PROP_VALUE = "value";

    private final List<Repository> _repositories = new ArrayList<>();
    private final Set<Class<? extends Resource>> _includedResourceTypes = new HashSet<>();
    private final Set<Class<? extends Resource>> _excludedResourceTypes = new HashSet<>();

    @Autowired private EntityRetriever _er;

    private List<ResourceRepositoryItem> _value = new ArrayList<>();
    private TextSource _labelText;
    private ModificationState _modState = ModificationState.UNCHANGED;
    private boolean _editable = true;
    private DataColumnTable<ResourceRepositoryItem> _table;
    private boolean _required;

    @Nullable
    @Override
    public List<ResourceRepositoryItem> getValue()
    {
        return _value;
    }

    @Override
    public void setValue(@Nullable List<ResourceRepositoryItem> value)
    {
        List<ResourceRepositoryItem> oldValue = _value;
        _value = value;
        if (isInited())
        {
            firePropertyChange(PROP_VALUE, value == null ? oldValue : null, value);
        }
    }

    @Override
    public ModificationState getModificationState()
    {
        return _modState;
    }

    @Override
    public List<ResourceRepositoryItem> getUIValue(Level logErrorLevel)
    {
        return _table.getDefaultModel().getRows();
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        boolean valid = ValidatorUtil.getInstance().validateComponentTree(this, notifiable);
        return valid && getValidator().validate(this, notifiable);
    }

    @Nullable
    @Override
    public List<ResourceRepositoryItem> commitValue() throws MIWTException
    {
        _modState = ModificationState.UNCHANGED;
        setValue(getUIValue());
        return _value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init()
    {
        super.init();

        addClassName("prop");
        addClassName("search");

        final PushButton addButton = CommonActions.ADD.push();
        addButton.addActionListener(ev -> {
            Dialog dlg = new Dialog(getApplication(), SELECT_FMT(RESOURCES()));
            dlg.addClassName("select-resources-dialog");

            PushButton done = new PushButton(DONE());
            done.addClassName("done-action");
            done.addActionListener(ev1 -> dlg.close());

            ResourceRepositoryItemSelector selector = new ResourceRepositoryItemSelector(getUIValue(),
                getRepositories().stream().collect(new ArrayCollector<>(Repository.class)));
            selector.withExcludeResourceTypes(_excludedResourceTypes);
            selector.withIncludeResourceTypes(_includedResourceTypes);
            selector.addRepoItemSelectedListener(evt -> {
                ResourceRepositoryItem rri = _er.reattachIfNecessary((ResourceRepositoryItem) evt.getNewValue());
                _modState = ModificationState.CHANGED;
                _table.getDefaultModel().addRow(rri);
                selector.withSelectedResources(getUIValue());
            });

            dlg.add(of("resource-selector-wrapper", selector, of("actions nav-actions", done)));

            getWindowManager().add(dlg);
            dlg.setVisible(true);
        });

        FixedValueColumn actionColumn = new FixedValueColumn();
        actionColumn.setColumnName(CommonColumnText.ACTIONS);
        FixedValueColumn nameColumn = new FixedValueColumn();
        nameColumn.setColumnName(CommonColumnText.NAME);
        _table = new DataColumnTable<>(actionColumn, nameColumn);
        _table.addClassName("resources");

        PushButton removeButton = CommonActions.REMOVE.push();
        removeButton.addActionListener(ev -> {
            _table.getDefaultModel().removeRow(_table.getLeadSelection());
            _modState = ModificationState.CHANGED;
        });
        Container rowActions = of("actions", removeButton);
        final Column actionUICol = _table.getUIColumn(actionColumn);
        assert actionUICol != null;
        actionUICol.setDisplayClass("action-column");
        _table.setUICellRenderer(actionColumn, rowActions);
        _table.setUICellRenderer(nameColumn, new CustomCellRenderer(TextSources.EMPTY,
            rri -> ((ResourceRepositoryItem) rri).getName()));
        _table.setCaption(new Label(getLabelText()));
        add(of("entity-actions actions", addButton));
        add(_table);

        addPropertyChangeListener(PROP_VALUE, evt ->
            _table.getDefaultModel().setRows((List<ResourceRepositoryItem>) evt.getNewValue()));
        addPropertyChangeListener(PROP_EDITABLE, evt -> {
            addButton.setVisible(_editable);
            removeButton.setVisible(_editable);
            actionUICol.setVisible(_editable);
        });

        if (_required)
        {
            _table.addClassName(CSSUtil.CSS_REQUIRED_FIELD);
            addValidator((component, notifiable) -> {
                List<ResourceRepositoryItem> uiVal = getUIValue();
                if (uiVal == null || uiVal.isEmpty())
                {
                    NotificationImpl error = new NotificationImpl(NotificationType.ERROR,
                        ERROR_MESSAGE_AT_LEAST_ONE_RESOURCE_REQUIRED_FMT(RESOURCES()));
                    error.setSource(this);
                    notifiable.sendNotification(error);
                    return false;
                }
                return true;
            });
        }
    }

    private List<Repository> getRepositories()
    {
        return _repositories.stream().peek(_er::reattachIfNecessary).collect(Collectors.toList());
    }

    /**
     * Get the Label Text for this ValueEditor
     *
     * @return the Label Text
     */
    public TextSource getLabelText()
    {
        return _labelText != null ? _labelText : TextSources.EMPTY;
    }

    @Override
    public boolean isEditable()
    {
        return _editable;
    }

    @Override
    public void setEditable(boolean b)
    {
        _editable = b;
        if (isInited())
        {
            firePropertyChange(PROP_EDITABLE, null, _editable);
        }
    }

    /**
     * Set some resource types to exclude from being selectable, if empty then none are excluded.
     *
     * @param excludeResourceTypes the excluded resource types
     *
     * @return this
     */
    public ResourceSelectorEditor withExcludeResourceTypes(@NotNull Set<Class<? extends Resource>> excludeResourceTypes)
    {
        _excludedResourceTypes.clear();
        _excludedResourceTypes.addAll(excludeResourceTypes);
        return this;
    }

    /**
     * Set some resource types to include in being selectable.  If empty, then all our included.
     *
     * @param includeTypes the included resource types
     *
     * @return this
     */
    public ResourceSelectorEditor withIncludeResourceTypes(@NotNull Set<Class<? extends Resource>> includeTypes)
    {
        _includedResourceTypes.clear();
        _includedResourceTypes.addAll(includeTypes);
        return this;
    }

    /**
     * Set the Label Text for this Value Editor
     *
     * @param labelText the Label Text
     *
     * @return this
     */
    public ResourceSelectorEditor withLabelText(TextSource labelText)
    {
        _labelText = labelText;
        return this;
    }

    /**
     * Set the Repositories to use for selecting Resources
     *
     * @param repositories the Repositories
     *
     * @return this
     */
    public ResourceSelectorEditor withRepositories(List<Repository> repositories)
    {
        _repositories.clear();

        if (repositories != null)
            _repositories.addAll(repositories);

        return this;
    }

    /**
     * Set that there should at least be one resource selected for validation
     *
     * @return this
     */
    public ResourceSelectorEditor withRequiredValueValidator()
    {
        _required = true;
        return this;
    }

    /**
     * Set the value on this ValueEditor
     *
     * @param value the value
     *
     * @return this
     */
    public ResourceSelectorEditor withValue(List<ResourceRepositoryItem> value)
    {
        setValue(value);
        return this;
    }
}
