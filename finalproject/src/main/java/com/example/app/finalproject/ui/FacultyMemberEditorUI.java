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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.core.validation.CommonValidationText;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.filesystem.FileSystemEntityCreateMode;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.MIWTApplication;
import net.proteusframework.ui.miwt.WindowManager;
import net.proteusframework.ui.miwt.component.Calendar;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.FileField;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.validation.RequiredValueValidator;
import net.proteusframework.ui.workspace.Workspace;
import net.proteusframework.ui.workspace.WorkspaceAware;

/**
 * Editor UI of facultyMembers
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-6 ??4:15
 */
@I18NFile(symbolPrefix = FacultyMemberEditorUI.RESOURCE_NAME,
    i18n = {
        @I18N(symbol = "title name",l10n = @L10N("FACULTY MEMBER REQUEST")),
        @I18N(symbol = "first name",l10n=@L10N("First Name")),
        @I18N(symbol = "last name",l10n=@L10N("Last Name")),
        @I18N(symbol = "rank",l10n = @L10N("Rank")),
        @I18N(symbol = "researchArea", l10n = @L10N("Research Area")),
        @I18N(symbol = "picture",l10n = @L10N("Picture")),
        @I18N(symbol = "joinDate",l10n = @L10N("Join Date")),
        @I18N(symbol = "sabbatical",l10n = @L10N("Sabbatical")),
        @I18N(symbol = "upload", l10n = @L10N("Upload Image")),
        @I18N(symbol = "uploadError", l10n = @L10N("Please upload < jpeg/gif/bmp/png > File")),
        @I18N(symbol = "saveError", l10n = @L10N("Something is wrong when commit,please contact us!")),
    }
)
@Configurable
public class FacultyMemberEditorUI extends MIWTPageElementModelContainer implements WorkspaceAware
{
    /** The resource_name */
    public final static String RESOURCE_NAME = "com.example.app.finalproject.ui.FacultyMemberEditorUI";
    /** Logger.*/
    private final static Logger _logger = Logger.getLogger(FacultyMemberEditorUI.class);
    /** FacultyMemberDao */
    @Autowired
    private FacultyMemberDao _facultyMemberDao;
    /** FacultyMember profile */
    private FacultyMemberProfile _facultyMemberProfile;
    /** Title */
    private Label _title = new Label(FacultyMemberEditorUILOK.TITLE_NAME());
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
    /** Title flag */
    private Boolean _titleFlag;
    /** MessageContainer  */
    private MessageContainer _msgCon;
    /** Workspace */
    private Workspace _workspace;
    /** Container */
    private Container _imgCon;

   /**
    * Constructor
    */
   public FacultyMemberEditorUI(){}

   /** create an instance
    * @param-facultyMemberProfile
    * @param-editor
    */
   public FacultyMemberEditorUI(@NotNull FacultyMemberProfile facultyMemberProfile,Boolean titleFlag)
    {
        super();
        _titleFlag = titleFlag;
        _facultyMemberProfile = facultyMemberProfile;
        if (titleFlag)
        {
            _title.setVisible(true);
        }
        else
        {
            _title.setVisible(false);
        }
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

        PushButton saveBtn = CommonActions.SAVE.push();
        PushButton cancelBtn = CommonActions.CANCEL.push();
        PushButton uploadBtn = CommonActions.UPLOAD.push();

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

        add(Container.of("facultyMember-info",
            _msgCon,
            Container.of("property_viewer",
                Container.of("prop-title", _title),
                Container.of("prop image", _imgCon),
                Container.of("button btn2", uploadBtn),
                Container.of("prop-fName", firLab, _firstName),
                Container.of("prop-lName", lasLab, _lastName),
                Container.of("prop-rank", rankLab, _rank),
                Container.of("prop-res", resAreaLab, _resArea),
                Container.of("prop-jDate", joinDateLab, _joinDate),
                Container.of("prop-sab", sabLab, _sabbatical),
                Container.of("button btn1", saveBtn, cancelBtn)
            )
        ));

        uploadBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                WindowManager wm = getWindowManager();
                MIWTApplication app = getApplication();
                final Dialog dlg = new Dialog(app, FacultyMemberEditorUILOK.UPLOAD(getLocaleContext()));
                Container upImageContainer = new Container();
                upImageContainer.addClassName("prop-image");
                final MessageContainer msgCon = new MessageContainer();
                msgCon.setExpireInterval(TimeUnit.SECONDS.toMillis(60));
                msgCon.addClassName("message-container");

                final FileField fileValue = new FileField();

                PushButton uploadBtn = CommonActions.UPLOAD.push();
                PushButton cancelBtn = CommonActions.CANCEL.push();
                uploadBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ev)
                    {
                        List<String> acceptImgType = new ArrayList<String>();
                        acceptImgType.add("image/jpeg");
                        acceptImgType.add("image/gif");
                        acceptImgType.add("image/bmp");
                        acceptImgType.add("image/png");
                        if (!acceptImgType.contains(fileValue.getMimeType()))
                        {
                            msgCon.clearMessages();
                            msgCon.add(NotificationType.ERROR, FacultyMemberEditorUILOK.UPLOADERROR(getLocaleContext()));
                            return;
                        }
                        CmsSite site = CmsFrontendDAO.getInstance().getOperationalSite();
                        FileSystemDAO fsp = FileSystemDAO.getInstance();
                        FileEntity fe = new FileEntity();
                        fe.setName(fileValue.getFileName());
                        fe.setContentType(fileValue.getMimeType());
                        try
                        {
                            DirectoryEntity dir = FileSystemDirectory.ProgrammaticData.getSubdirectory(site, "Media");
                            _img = fsp.newFile(dir, fe, FileSystemEntityCreateMode.unique);
                            fsp.setStream(_img, fileValue.getInputStream());
                            _imgCon.removeAllComponents();
                            _showImage(_img);
                            if(_facultyMemberProfile==null)
                                _facultyMemberProfile=new FacultyMemberProfile();
                            dlg.close();
                        }
                        catch (Exception e)
                        {
                            _logger.error("Upload the image error", e);
                        }
                    }
                });
                cancelBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ev)
                    {
                        dlg.close();
                    }
                });
                upImageContainer.add(Container.of("message-con", msgCon));
                upImageContainer.add(Container.of(Container.of(fileValue),
                    Container.of("actions persistence_actions bottom", uploadBtn, cancelBtn)));
                dlg.add(upImageContainer);
                dlg.setVisible(true);
                wm.add(dlg);
            }
        });

        saveBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                if (validateUIValue(_msgCon))
                {
                    String slug = _lastName.getText().toLowerCase()+_firstName.getText();
                    if(_facultyMemberDao.getFlag(slug))
                    {
                        _msgCon.sendNotification(new Message(NotificationType.ERROR,TextSources.create("This Name Exists,Please Change!")));
                    }
                    else
                    {
                        commitValue();
                    }
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                close();
            }
        });
    }

    /**
     * Update the UI value
     */
    public void updateUIValue()
    {
         FacultyMemberProfile facultyMemberEditor = _facultyMemberDao.getAttachedFacultyMemberProfile(_facultyMemberProfile);
         if(facultyMemberEditor.getPicture()!=null)
          {
              _showImage(facultyMemberEditor.getPicture());
          }
         _firstName.setText(facultyMemberEditor.getFirstName());
         _lastName.setText(facultyMemberEditor.getLastName());
         _rank.setSelectedObject(facultyMemberEditor.getRank());
         _resArea.setText(facultyMemberEditor.getResearchArea());
         _joinDate.setDate(facultyMemberEditor.getJoinDate());
         if(facultyMemberEditor.getSabbatical())
              _sabbatical.setSelectedObject("true");
         else
          {
              _sabbatical.setSelectedObject("false");
          }
    }

    /**
     * Pop up the modified value. Before this method, validateUIValue method should be called.
     *
     * @return modified Event entity.
     */
    public FacultyMemberProfile commitValue()
    {
        FacultyMemberProfile facultyMemberEditor = _facultyMemberDao.getAttachedFacultyMemberProfile(_facultyMemberProfile);
        facultyMemberEditor.setFirstName(_firstName.getText());
        facultyMemberEditor.setLastName(_lastName.getText());
        facultyMemberEditor.setRank((Rank) _rank.getSelectedObject());
        facultyMemberEditor.setResearchArea(_resArea.getText());
        facultyMemberEditor.setPicture(_img);
        facultyMemberEditor.setJoinDate(_joinDate.getDate());
        if(_sabbatical.getSelectedIndex()==1)
            facultyMemberEditor.setSabbatical(_sabbatical.getSelectedIndex()==1);
        else
        {
            facultyMemberEditor.setSabbatical(_sabbatical.getSelectedIndex()==2);
        }
        facultyMemberEditor.setSlug(getLastName(_lastName.getText()).toLowerCase()+_firstName.getText());
        facultyMemberEditor.setDeleted(false);
        _facultyMemberDao.saveFacultyMemberProfile(facultyMemberEditor);
        close();
        return facultyMemberEditor;
    }

    /**
     * To get the first letter of the facultyMember's lastName
     * @param lastName
     * @return
     */
    public String getLastName(String lastName)
    {
        String result = "";
        for(int i=0;i<lastName.length();i++)
        {
            char c =lastName.charAt(i);
            int k = (int) c;
            if (k>=65&&k<=90)
            {
               result = result+c;
            }
        }
        return result;
    }

    /**
     * Validate UI value. Before pop the value, this method should be called.
     *
     * @param notifiable usually it is MessageContainer.
     * @return true if the UI value is legal.
     */
    public boolean validateUIValue(MessageContainer notifiable)
    {
        RequiredValueValidator fName = new RequiredValueValidator();
        fName.setErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, FacultyMemberEditorUILOK.FIRST_NAME());
        _firstName.setValidator(fName);

        RequiredValueValidator lName = new RequiredValueValidator();
        lName.setErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, FacultyMemberEditorUILOK.LAST_NAME());
        _lastName.setValidator(lName);

        RequiredValueValidator jDate = new RequiredValueValidator();
        jDate.setErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, FacultyMemberEditorUILOK.JOINDATE());
        _joinDate.setValidator(jDate);

        return notifiable.checkValidators(this);
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
        catch (NullPointerException e)
        {
            _logger.error("Image is null,please upload first", e);
        }
    }
}


