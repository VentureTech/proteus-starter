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

package com.example.app.finalproject.util;

import java.util.HashMap;
import java.util.Map;

import com.i2rd.cms.editor.ValidationState;
import com.i2rd.cms.miwt.LinkSelector;

import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor;
import net.proteusframework.cms.component.editor.EditorUI;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.composite.Message;

/**
 * Editor for PageElements.
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-17 ??8:23
 */
@I18NFile(symbolPrefix = "DetailInfoEditor", i18n = {
    @I18N(symbol = "listFaculty_page", l10n = @L10N("ListFaculty Page")),
    @I18N(symbol = "detailInfo_page", l10n = @L10N("DetailInfo Page")),
    @I18N(symbol = "save_page", l10n = @L10N("Save Page")),
    @I18N(symbol = "cancel_page", l10n = @L10N("Cancel Page")),
    @I18N(symbol = "error_page", l10n = @L10N("Error Page")),
    @I18N(symbol = "pageConfig_first", l10n = @L10N("Please config the page links first.")),
})

public class DetailInfoEditor extends ContentBuilderBasedEditor<DetailInfoContentBuilder>
{
    /** SerialVersionUID. */
    private static final long serialVersionUID = 0;
    /** linkMap */
    private Map<DetailInfoProperties, LinkSelector> _linksMap = new HashMap<DetailInfoProperties, LinkSelector>();
    /** listFaculty_page link selector */
    private final LinkSelector _listFacultyPage = new LinkSelector();
    /** detailInfo_page link selector */
    private final LinkSelector _detailInfoPage = new LinkSelector();
    /** Save Page link selector. */
    private final LinkSelector _savePage  = new LinkSelector();
    /** Cancel Page link selector. */
    private final LinkSelector _cancelPage = new LinkSelector();
    /** Error Page link selector. */
    private final LinkSelector _errorPage = new LinkSelector();
    /**
     * Constructor.
     */
    public DetailInfoEditor()
    {
        super(DetailInfoContentBuilder.class);
    }

    @Override
    public void createUI(EditorUI editorUI)
    {
        super.createUI(editorUI);
        final DetailInfoContentBuilder builder = getBuilder();
        if(builder.getListFacultyPage() != null)
            _listFacultyPage.setSelection(builder.getListFacultyPage());
        if(builder.getDetailInfoPage() != null)
            _detailInfoPage.setSelection(builder.getDetailInfoPage());
        if(builder.getSavePage() != null)
            _savePage.setSelection(builder.getSavePage());
        if(builder.getCancelPage() != null)
            _cancelPage.setSelection(builder.getCancelPage());
        if(builder.getErrorPage() != null)
            _errorPage.setSelection(builder.getErrorPage());
        _listFacultyPage.setExternalLinkOption(false);
        _detailInfoPage.setExternalLinkOption(false);
        _savePage.setExternalLinkOption(false);
        _cancelPage.setExternalLinkOption(false);
        _errorPage.setExternalLinkOption(false);
        editorUI.addComponent(Container.of("prop-listFacultyPage", DetailInfoEditorLOK.LISTFACULTY_PAGE(), _listFacultyPage));
        editorUI.addComponent(Container.of("prop-detailInfoPage", DetailInfoEditorLOK.DETAILINFO_PAGE(), _detailInfoPage));
        editorUI.addComponent(Container.of("prop save_page", DetailInfoEditorLOK.SAVE_PAGE(), _savePage));
        editorUI.addComponent(Container.of("prop cancel_page", DetailInfoEditorLOK.CANCEL_PAGE(), _cancelPage));
        editorUI.addComponent(Container.of("prop-errorPage", DetailInfoEditorLOK.ERROR_PAGE(), _errorPage));
    }

    @Override
    public ValidationState validate(Notifiable errors)
    {
        Link listFacultyPageSelection = _listFacultyPage.getSelection();
        Link detailInfoPageSelection = _detailInfoPage.getSelection();
        Link savePageSelection = _savePage.getSelection();
        Link cancelPageSelection = _cancelPage.getSelection();
        Link errorPageSelection = _errorPage.getSelection();
        if(listFacultyPageSelection == null || detailInfoPageSelection ==
            null||savePageSelection==null||cancelPageSelection==null||errorPageSelection==null)
        {
            errors.sendNotification(Message.error(DetailInfoEditorLOK.PAGECONFIG_FIRST()));
            return ValidationState.invalid_modeldata;
        }
        return ValidationState.valid;
    }

    @Override
    protected void _updateBuilder()
    {
        DetailInfoContentBuilder builder = getBuilder();
        builder.setListFacultyPage(_listFacultyPage.getSelection());
        builder.setDetailInfoPage(_detailInfoPage.getSelection());
        builder.setSavePage(_savePage.getSelection());
        builder.setCancelPage(_cancelPage.getSelection());
        builder.setError_page(_errorPage.getSelection());
    }
}
