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
import com.example.app.finalproject.util.ImageResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;
import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.component.editor.Editor;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.search.QLBuilder;


/**
 * Listing of the facultyMembers
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-18 ??12:15
 */
@I18NFile(symbolPrefix = FacultyMemberListUI.RESOURCE_NAME,
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("FacultyMemberListUI")),
        @I18N(symbol = "error_info", l10n = @L10N("There isn't a faculty.")),
        @I18N(symbol = "title", l10n = @L10N("Welcome To FacultyMember Page")),
    })
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FacultyMemberListUI extends MIWTPageElementModelContainer
{
    /** RESOURCE_NAME */
    public final static String RESOURCE_NAME ="com.example.app.finalproject.ui.FacultyMemberListUI";
    /** Logger */
    private final static Logger _logger = Logger.getLogger(FacultyMemberListUI.class);
    /** Slug */
    private String _slug;
    /** Dao */
    @Autowired
    private FacultyMemberDao _facultyMemberDao;
    /** Message container. */
    private MessageContainer _msgCon;
    /** DetailInfoContentBuilder */
    private DetailInfoContentBuilder _contentBuilder;
    /**
     * create an instance
     */
    public FacultyMemberListUI()
    {
        super();
        setName(FacultyMemberListUILOK.COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
    }

    @Override
    public void init()
    {
        super.init();
        removeAllComponents();
        _msgCon = new MessageContainer(TimeUnit.SECONDS.toMillis(60));
        _msgCon.clearMessages();
        QLBuilder facultyQB=_facultyMemberDao.getAllFacultyQB();
        int size = facultyQB.getQueryResolver().list().size();
        if(size<=0)
        {
            _msgCon.sendNotification(new Message(NotificationType.ERROR,getLocaleContext().getLocalizedText(FacultyMemberListUILOK
                .ERROR_INFO())));
            return;
        }
        final Label title = new Label(FacultyMemberListUILOK.TITLE());
        title.setHTMLElement(HTMLElement.h1);

        Container imageCon=new Container();
        add(Container.of("prop-mainCon",
            Container.of("top", title),imageCon));
        List<FacultyMemberProfile> facultyMembers=facultyQB.getQueryResolver().list();
        for(FacultyMemberProfile facultyMember:facultyMembers)
        {
            final PushButton nameLink = new PushButton();
            nameLink.setLabel(new LocalizedText(facultyMember.getSlug()));
            nameLink.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    String slug = nameLink.getLabel().getText(getLocaleContext()).toString();
                    _slug = _facultyMemberDao.getFacultyMemberProfile(slug).getSlug();
                    Response response = Event.getResponse();
                    response.redirect(response.createURL(_contentBuilder.getDetailInfoPage()).addParameter("slug", _slug));

                }
            });

            if(facultyMember.getPicture()!=null)
            {
                Image image= null;
                try
                {
                    image = new Image(ImageResource.getResource(facultyMember.getPicture(), 80, 90));
                }
                catch (IOException e)
                {
                    _logger.error("Something is wrong when get the picture!", e);
                }
                ImageComponent imageCom=new ImageComponent(image);

                imageCon.add(Container.of("prop-image",
                    Container.of("prop-imageCom",imageCom),
                    Container.of("prop-name",nameLink)));
            }
            else
            {
                imageCon.add(Container.of("prop-image",
                    Container.of("prop-nameLink", nameLink)));
            }
        }
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

