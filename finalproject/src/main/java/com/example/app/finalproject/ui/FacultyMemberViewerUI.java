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
import com.example.app.finalproject.model.Rank;
import com.example.app.finalproject.util.ImageResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.constraints.NotNull;

import java.io.IOException;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Calendar;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.workspace.Workspace;
import net.proteusframework.ui.workspace.WorkspaceAware;
/**
 * viewer UI of facultyMembers
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-24 ??11:19
 */
@Configurable
public class FacultyMemberViewerUI extends MIWTPageElementModelContainer implements WorkspaceAware
{
    /** The resource_name */
    public final static String RESOURCE_NAME = "com.example.app.finalproject.ui.FacultyMemberViewerUI";
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(FacultyMemberEditorUI.class);
    /** FacultyMemberDao */
    @Autowired
    private FacultyMemberDao _facultyMemberDao;
    /** FacultyMember profile */
    private FacultyMemberProfile _facultyMemberProfile;
    /** Title */
    private Label title = new Label(FacultyMemberEditorUILOK.TITLE_NAME());
    /** First name field */
    private Field _firstName;
    /** Last name field */
    private Field _lastName;
    /** ComboBox rank */
    private ComboBox _rank;
    /** ResearchArea field */
    private Field _resArea;
    /** FileEntity */
    private FileEntity _img;
    /** Join date */
    private Calendar _joinDate;
    /** ComboBox sabbatical */
    private ComboBox _sabbatical;
    /** Workspace */
    private Workspace _workspace;
    /** MessageContainer  */
    private MessageContainer _msgCon;
    /** Container */
    private Container _imgCon;

    /**
     * Constructor
     */
    public FacultyMemberViewerUI(){}
    /**
     * create a instance
     */
    public FacultyMemberViewerUI(@NotNull FacultyMemberProfile facultyMemberProfile){
        super();
        _facultyMemberProfile = facultyMemberProfile;
    }
    /**
     * Create components
     */
    @Override
    public void init()
    {
        /** Label firLab */
        final Label firLab;
        /** Label lasLab */
        final Label lasLab;
        /** Label rankLab */
        final Label rankLab;
        /** Label resAreaLab */
        final Label resAreaLab;
        /** Label joinDateLab */
        final Label joinDateLab;
        /** Label sabLab */
        final Label sabLab;

        super.init();
        removeAllComponents();
        getLocaleContext();
        _msgCon = new MessageContainer(TimeUnit.SECONDS.toMillis(60));
        _msgCon.clearMessages();

        firLab = new Label(FacultyMemberEditorUILOK.FIRST_NAME());
        _firstName = new Field();
        firLab.addClassName("lab1");
        _firstName.addClassName("val");


        lasLab = new Label(FacultyMemberEditorUILOK.LAST_NAME());
        _lastName = new Field();
        lasLab.addClassName("lab2");
        _lastName.addClassName("val");


        rankLab = new Label(FacultyMemberEditorUILOK.RANK());
        _rank = new ComboBox(new SimpleListModel<>(Rank.values()));
        rankLab.addClassName("lab");
        _rank.addClassName("val");


        resAreaLab = new Label(FacultyMemberEditorUILOK.RESEARCHAREA());
        _resArea = new Field();
        resAreaLab.addClassName("lab");
        _resArea.addClassName("val");


        _imgCon = new Container();
        _img = new FileEntity();

        joinDateLab = new Label(FacultyMemberEditorUILOK.JOINDATE());
        _joinDate = new Calendar();
        _joinDate.setFixedTimeZone(TimeZone.getTimeZone("UTC"));
        joinDateLab.addClassName("lab1");
        _joinDate.addClassName("val");

        sabLab = new Label(FacultyMemberEditorUILOK.SABBATICAL());
        sabLab.addClassName("labs");
        SimpleListModel simpleListModel = new SimpleListModel();
        simpleListModel.add("Please select");
        simpleListModel.add("true");
        simpleListModel.add("false");
        _sabbatical = new ComboBox(simpleListModel);
        _sabbatical.setSelectedIndex(2);

        updateUIValue();

        _firstName.setEditable(false);
        _lastName.setEditable(false);
        _rank.setEnabled(false);
        _resArea.setEditable(false);
        _joinDate.setEnabled(false);
        _sabbatical.setEnabled(false);
        title.setVisible(false);

        add(Container.of("facultyMember-info",
            _msgCon,
            Container.of("property_viewer",
                Container.of("prop-title", title),
                Container.of("prop image", _imgCon),
                Container.of("prop-fName", firLab, _firstName),
                Container.of("prop-lName", lasLab, _lastName),
                Container.of("prop-rank", rankLab, _rank),
                Container.of("prop-res", resAreaLab, _resArea),
                Container.of("prop-jDate", joinDateLab, _joinDate),
                Container.of("prop-sab", sabLab, _sabbatical)
            )
        ));
    }
    /**
     * Update the UI value
     */
    public void updateUIValue()
    {
        FacultyMemberProfile facultyMemberViewer = _facultyMemberDao.getAttachedFacultyMemberProfile(_facultyMemberProfile);
        if(facultyMemberViewer.getPicture()!=null)
        {
            _showImage(facultyMemberViewer.getPicture());
        }
        _firstName.setText(facultyMemberViewer.getFirstName());
        _lastName.setText(facultyMemberViewer.getLastName());
        _rank.setSelectedObject(facultyMemberViewer.getRank());
        _resArea.setText(facultyMemberViewer.getResearchArea());
        _joinDate.setDate(facultyMemberViewer.getJoinDate());
        if(facultyMemberViewer.getSabbatical())
            _sabbatical.setSelectedObject("true");
        else
        {
            _sabbatical.setSelectedObject("false");
        }
    }
    @Override
    /**
     * workspace
     * @param workspace-workspace
     */
    public void initialize(Workspace workspace)
    {
        _workspace = workspace;
    }
    /**
     * Show the image
     * @param fileEntity-the Image file entity
     */
    private void _showImage(FileEntity fileEntity)
    {

        try
        {
            _imgCon.add(new ImageComponent(new Image(ImageResource.getResource(fileEntity, 80, 90))));
        }
        catch (IOException e)
        {
            _logger.error("SetImage throw Io exception", e);
        }
    }
}
