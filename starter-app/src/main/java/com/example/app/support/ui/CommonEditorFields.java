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

package com.example.app.support.ui;

import com.example.app.profile.model.repository.RepositoryItem;
import com.example.app.profile.model.repository.RepositoryItemStatus;
import com.example.app.profile.ui.resource.ResourceText;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Supplier;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.LocalizedTextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TemplateCompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;

import static net.proteusframework.ui.miwt.util.CommonColumnText.DESCRIPTION;
import static net.proteusframework.ui.miwt.util.CommonColumnText.NAME;

/**
 * Common editor fields.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@I18NFile(
    classVisibility = I18NFile.Visibility.PUBLIC,
    symbolPrefix = "com.example.app.support.ui.CommonEditorFields",
    i18n = {
        @I18N(symbol = "Label Working Period", l10n = @L10N("Working Period In Days")),
        @I18N(symbol = "Label Auto Complete", l10n = @L10N("Auto-complete")),
        @I18N(symbol = "Label Reschedule Behavior", l10n = @L10N("Reschedule Behavior")),
        @I18N(symbol = "Label Start Date Offset", l10n = @L10N("Start Offset In Days")),
        @I18N(symbol = "Tooltip Start Date Offset",
            l10n = @L10N("How many days to offset the schedule date from a prcoessed start date or the"
                         + " start date of a dependency such as another {0}.")),
        @I18N(symbol = "Label Allow Timeline Change Schedule", l10n = @L10N("Allow Schedule to be modified from the timeline")),
    }
)
public class CommonEditorFields
{

    /**
     * Add a description editor.
     *
     * @param compositeValueEditor the composite editor.
     */
    public static void addDescriptionEditor(CompositeValueEditor<? extends NamedObject> compositeValueEditor)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            LocalizedTextEditor textEditor = new LocalizedTextEditor(DESCRIPTION, null);
            textEditor.setDisplayHeight(5);
            textEditor.setDisplayWidth(40);
            textEditor.setRequiredValueValidator();
            textEditor.addClassName("description");
            textEditor.getValueComponent().setMaxChars(255);
            return textEditor;
        }, "description");
    }

    /**
     * Add a description editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param property the property for the description field
     */
    public static void addDescriptionEditor(CompositeValueEditor<?> compositeValueEditor, String property)
    {
        compositeValueEditor.addEditorForProperty(getDescriptionEditor(), property);
    }

    private static Supplier<ValueEditor<?>> getDescriptionEditor()
    {
        return getDescriptionEditor(null);
    }

    private static Supplier<ValueEditor<?>> getDescriptionEditor(@Nullable TextSource label)
    {
        return getDescriptionEditor(label, 255);
    }

    private static Supplier<ValueEditor<?>> getDescriptionEditor(@Nullable TextSource label, int maxChars)
    {
        return () -> {
            TextEditor textEditor = new TextEditor(label == null ? DESCRIPTION : label, null);
            textEditor.setDisplayHeight(5);
            textEditor.setDisplayWidth(40);
            textEditor.setRequiredValueValidator();
            textEditor.addClassName("description");
            textEditor.getValueComponent().setMaxChars(maxChars);
            if (maxChars > 255)
            {
                textEditor.getValueComponent().setDisplayHeight(10);
            }
            return textEditor;
        };
    }

    /**
     * Add a description editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param reader the property reader to use
     * @param writer the property writer to use
     */
    @SuppressWarnings({"rawtypes"})
    public static void addDescriptionEditor(CompositeValueEditor<?> compositeValueEditor,
        CompositeValueEditor.PropertyReader reader, CompositeValueEditor.PropertyWriter writer)
    {
        addDescriptionEditor(compositeValueEditor, reader, writer, null);
    }

    /**
     * Add a description editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param reader the property reader to use
     * @param writer the property writer to use
     * @param label the label for the description editor
     */
    @SuppressWarnings({"rawtypes"})
    public static void addDescriptionEditor(CompositeValueEditor<?> compositeValueEditor,
        CompositeValueEditor.PropertyReader reader, CompositeValueEditor.PropertyWriter writer, @Nullable TextSource label)
    {
        addDescriptionEditor(compositeValueEditor, reader, writer, label, 255);
    }

    /**
     * Add a description editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param reader the property reader to use
     * @param writer the property writer to use
     * @param label the label for the description editor
     * @param maxChars them ax characters for the editor
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addDescriptionEditor(CompositeValueEditor<?> compositeValueEditor,
        CompositeValueEditor.PropertyReader reader, CompositeValueEditor.PropertyWriter writer,
        @Nullable TextSource label, int maxChars)
    {
        compositeValueEditor.addEditorForProperty(getDescriptionEditor(label, maxChars), reader, writer);
    }

    /**
     * Add a name editor.
     *
     * @param compositeValueEditor the composite editor.
     */
    public static void addNameEditor(CompositeValueEditor<? extends NamedObject> compositeValueEditor)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            LocalizedTextEditor textEditor = new LocalizedTextEditor(NAME, null);
            textEditor.setRequiredValueValidator();
            textEditor.addClassName("name");
            textEditor.getValueComponent().setMaxChars(255);
            return textEditor;
        }, "name");
    }

    /**
     * Add a name editor.
     *
     * @param compositeValueEditor the composite editor.
     */
    public static void addNameEditor(TemplateCompositeValueEditor<? extends NamedObject> compositeValueEditor)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            LocalizedTextEditor textEditor = new LocalizedTextEditor(NAME, null);
            textEditor.setRequiredValueValidator();
            textEditor.addClassName("name");
            textEditor.getValueComponent().setMaxChars(255);
            textEditor.setComponentName("property-name");
            return textEditor;
        }, "name");
    }

    /**
     * Add a name editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param property the property for the name field
     */
    public static void addNameEditor(CompositeValueEditor<?> compositeValueEditor, String property)
    {
        addNameEditor(compositeValueEditor, property, NAME);
    }

    /**
     * Add a name editor.
     *
     * @param compositeValueEditor the composite editor.
     * @param property the property for the name field
     * @param label the label for the field
     */
    public static void addNameEditor(CompositeValueEditor<?> compositeValueEditor, String property, TextSource label)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            TextEditor textEditor = new TextEditor(label, null);
            textEditor.setRequiredValueValidator();
            textEditor.addClassName("name");
            textEditor.getValueComponent().setMaxChars(255);
            return textEditor;
        }, property);
    }

    /**
     * Add a Source Editor for a RepositoryItem editor
     *
     * @param compositeValueEditor the composite value editor.
     */
    public static void addRepositoryItemSourceEditor(CompositeValueEditor<? extends RepositoryItem> compositeValueEditor)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            TextEditor editor = new TextEditor(ResourceText.LABEL_SOURCE(), null);
            editor.setRequiredValueValidator();
            editor.addClassName("source");
            return editor;
        }, RepositoryItem.SOURCE_COLUMN_PROP);
    }

    /**
     * Add a Status Editor for a RepositoryItem editor
     *
     * @param compositeValueEditor the composite value editor
     */
    public static void addRepositoryItemStatusEditor(CompositeValueEditor<? extends RepositoryItem> compositeValueEditor)
    {
        addRepositoryItemStatusEditor(compositeValueEditor, null);
    }

    /**
     * Add a Status Editor for a RepositoryItem editor
     *
     * @param compositeValueEditor the composite value editor
     * @param comparator comparator for
     */
    public static void addRepositoryItemStatusEditor(CompositeValueEditor<? extends RepositoryItem> compositeValueEditor,
        @Nullable Comparator<RepositoryItemStatus> comparator)
    {
        compositeValueEditor.addEditorForProperty(() -> {
            ComboBoxValueEditor<RepositoryItemStatus> editor = new ComboBoxValueEditor<>(
                CommonColumnText.STATUS, RepositoryItemStatus.getListForCombo(
                comparator), null);
            editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
            editor.setRequiredValueValidator();
            editor.addClassName("status");
            return editor;
        }, RepositoryItem.STATUS_COLUMN_PROP);
    }

    private CommonEditorFields()
    {
        // I do nothing!
    }
}
