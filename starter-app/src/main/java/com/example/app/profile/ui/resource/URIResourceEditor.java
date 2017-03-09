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

package com.example.app.profile.ui.resource;


import com.example.app.profile.model.resource.URIResource;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.component.composite.editor.URIEditor;
import net.proteusframework.ui.miwt.component.template.FileSystemTemplateDataSource;

/**
 * {@link ResourceValueEditor} for a {@link URIResource}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.resource.URIResourceEditor",
    i18n = {
        @I18N(symbol = "Link Label", l10n = @L10N("Link"))
    }
)
@Configurable
class URIResourceEditor extends ResourceValueEditor<URIResource>
{
    /**
     * Instantiate a new instance
     *
     * @param value the resource for this editor
     */
    public URIResourceEditor(@Nullable URIResource value)
    {
        super(URIResource.class, value, new FileSystemTemplateDataSource("profile/resource/URIResourceEditor.xml"));
    }

    @Override
    public void init()
    {
        super.init();

        addEditorForProperty(
            () ->{
                URIEditor editor = new URIEditor(URIResourceEditorLOK.LINK_LABEL(), null);
                editor.setComponentName("uri-property");
                return editor;
            },
            URIResource::getUri,
            URIResource::setUri);

        applyTemplate();
    }
}
