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

package com.example.app.finalproject.ui;


import com.example.app.finalproject.model.FacultyMemberDao;
import com.example.app.finalproject.model.FacultyMemberProfile;
import com.example.app.finalproject.util.DetailInfoContentBuilder;
import com.example.app.finalproject.util.DetailInfoEditor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.config.BeanDefinition;


import javax.annotation.Nullable;

import java.util.List;




import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;
import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;


import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.component.editor.Editor;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.event.EventQueue;
import net.proteusframework.ui.miwt.event.EventQueueElement;
import net.proteusframework.ui.miwt.util.RendererEditorState;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;

/**
 *detailInfo of facultyMember
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-17 ??3:49
 */
@I18NFile(symbolPrefix = FacultyMemberDetail_Info.RESOURCE_NAME,
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("FacultyMemberDetail_Info")),
        @I18N(symbol = "back",l10n = @L10N("Back")),
    })
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public  class FacultyMemberDetail_Info extends MIWTPageElementModelContainer
{
    /** Resource Name. */
    public final static String RESOURCE_NAME = "com.example.app.finalproject.ui.FacultyMemberDetail_Info";
    /** Logger */
    private final static Logger _logger = Logger.getLogger(FacultyMemberDetail_Info.class);
    /** Dao */
    @Autowired
    private FacultyMemberDao _facultyMemberDao;
    /** id */
    private String _id;
    /** response */
    private CmsResponse _response;
    /** DetailInfoContentBuilder */
    private DetailInfoContentBuilder _contentBuilder;

    /**
     * create an instance
     */
    public FacultyMemberDetail_Info()
    {
        super();
        setName(FacultyMemberDetail_InfoLOK.COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
    {
        super.preRenderProcess(request,response,state);
        if (!request.isPartial() && isInited())
            EventQueue.queue(new EventQueueElement()
            {
                @Override
                public void fire()
                {
                    _id=request.getParameter("id");
                    _setupUI();
                }
                @Override
                public int getEventPriority()
                {
                    return 0;
                }
            });

    }
    /**
     * Setup the task search UI
     */
    private void _setupUI()
    {
        removeAllComponents();
        QLBuilder proQB=_facultyMemberDao.getAllFacultyQB();
        proQB.appendCriteria("id", PropertyConstraint.Operator.eq,_id);
        List<FacultyMemberProfile> facultyMemberProfileList=proQB.getQueryResolver().list();
        Container con=new FacultyMemberEditorUI(facultyMemberProfileList.get(0),false);
        PushButton backBtn = new PushButton();
        backBtn.setLabel(TextSources.create("BACK"));
        backBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                Response response = Event.getResponse();
                response.redirect(response.createURL(_contentBuilder.getListFacultyPage()));
            }
        });
        add(backBtn);
        add(con);
    }
    @Nullable
    @Override
    public Editor getEditor()
    {
        return new DetailInfoEditor();
    }

    @Override
    public void configure(@Nullable CmsModelDataSet contentBuilder)
    {
        _contentBuilder = DetailInfoContentBuilder.load(contentBuilder);
    }
}
