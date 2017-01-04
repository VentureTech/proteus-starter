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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;

import com.i2rd.cms.label.miwt.LabelEditor;
import com.i2rd.dynamic.search.SimpleEntityPropertySearch;
import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.cms.label.Label;
import net.proteusframework.cms.label.LabelDAO;
import net.proteusframework.cms.label.LabelDomain;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocalizedObjectKeyComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.ui.column.AbstractDataColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.AbstractAction;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.PropertyConstraint;

/**
 * Provides a UI for managing {@link Label}s on a {@link LabelDomain}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7/13/16 11:28 AM
 */
@SuppressWarnings("deprecation")
@Configurable
public class LabelDomainLabelManagement extends HistoryContainer
{
    @Autowired
    private EntityRetriever _er;

    private final LabelDomain _labelDomain;
    private final TextSource _label;

    /**
     * Instantiates a new Label domain label management.
     *
     * @param labelDomain the label domain
     * @param label the label
     */
    public LabelDomainLabelManagement(LabelDomain labelDomain, @Nullable TextSource label)
    {
        super();
        _labelDomain = labelDomain;
        _label = label;
    }

    LabelDomain getLabelDomain()
    {
        return _er.reattachIfNecessary(_labelDomain);
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    protected TextSource getLabel()
    {
        return _label;
    }

    @Override
    public void init()
    {
        super.init();

        addClassName("label-mgt");
        addClassName(CSSUtil.convertClassName(getLabelDomain().getProgrammaticIdentifier()));

        final MessageContainer messages = new MessageContainer(35_000L);

        final SimpleEntityPropertySearch<Label> labelSearch = new SimpleEntityPropertySearch<Label>(LabelDAO.getInstance()
            .getLabelSearchResultSupplier(getLabelDomain())) {
            @Override
            protected void setupUI()
            {
                super.setupUI();
                getSearchResultsComponent().getModel().setOrderedColumn(0, SortOrder.ASCENDING);
            }
        };
        labelSearch.setTitle(new net.proteusframework.ui.miwt.component.Label(getLabel() == null
                ? getLabelDomain().getName()
                : getLabel())
            .withHTMLElement(HTMLElement.h2));
        labelSearch.scheduleSearch();
        getHistory().addPropertyChangeListener(evt -> labelSearch.scheduleSearch());


        labelSearch.addClassName("labels");
        AbstractAction addLabelAction = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                Label label = new Label();
                label.setLabelDomain(getLabelDomain());
                LabelEditor labelEditor = new LabelEditor(label);
                labelEditor.setClickToEdit(false);
                labelEditor.setShowProgrammaticIdentifier(false);
                navigateBackOnClose(labelEditor);
                getHistory().add(new HistoryElement(labelEditor));
                labelEditor.setTitle(new net.proteusframework.ui.miwt.component.Label(getLabel() == null
                    ? getLabelDomain().getName()
                    : getLabel())
                    .withHTMLElement(HTMLElement.h2));
            }
        };
        addLabelAction.prop(Action.DISPLAY_CLASS, "action-add");
        addLabelAction.prop(Action.NAME, getLocaleContext().getLocalizedText(CommonButtonText.ADD));
        labelSearch.addEntityActions(addLabelAction);

        labelSearch.addFieldConstraint("name", PropertyConstraint.Operator.like, CommonColumnText.NAME);

        LocalizedObjectKeyComparator lokComparator = new LocalizedObjectKeyComparator(getLocaleContext());
        AbstractDataColumn nameCol = new PropertyColumn(Label.class, "name").withColumnName(CommonColumnText.NAME);
        nameCol.setComparator(lokComparator);
        labelSearch.addColumn(nameCol, null);

        AbstractAction editLabelAction = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                Label label = labelSearch.getLeadSelection();
                if(label == null)
                    return;
                LabelEditor labelEditor = new LabelEditor(label);
                labelEditor.setClickToEdit(false);
                labelEditor.setShowProgrammaticIdentifier(false);
                navigateBackOnClose(labelEditor);
                getHistory().add(new HistoryElement(labelEditor));
                labelEditor.setTitle(new net.proteusframework.ui.miwt.component.Label(getLabel() == null
                    ? getLabelDomain().getName()
                    : getLabel())
                    .withHTMLElement(HTMLElement.h2));
            }
        };
        editLabelAction.prop(Action.DISPLAY_CLASS, CSSUtil.CSS_EDIT_BUTTON);
        editLabelAction.prop(Action.SHORT_DESCRIPTION, getLocaleContext().getLocalizedText(CommonButtonText.EDIT_TOOLTIP));
        editLabelAction.prop(Action.NAME, getLocaleContext().getLocalizedText(CommonButtonText.EDIT));
        AbstractAction deleteLabelAction = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                Label label = labelSearch.getLeadSelection();
                if(label == null)
                    return;
                try
                {
                    LabelDAO.getInstance().deleteLabel(label);
                    labelSearch.scheduleSearch();
                }
                catch(Exception e)
                {
                    NotificationImpl error = NotificationImpl.create(e);
                    error.setSource(labelSearch);
                    messages.sendNotification(error);
                }
            }
        };
        deleteLabelAction.prop(Action.DISPLAY_CLASS, CSSUtil.CSS_DELETE_BUTTON);
        deleteLabelAction.prop(Action.NAME, getLocaleContext().getLocalizedText(CommonButtonText.DELETE));
        deleteLabelAction.prop(Action.SHORT_DESCRIPTION, getLocaleContext().getLocalizedText(CommonButtonText.DELETE_TOOLTIP));
        deleteLabelAction.prop(Action.CONFIRMATION, getLocaleContext().getLocalizedText(CommonButtonText.DELETE_CONFIRM));

        labelSearch.addRowActions(editLabelAction, deleteLabelAction);

        setDefaultComponent(of(messages, labelSearch));
    }
}
