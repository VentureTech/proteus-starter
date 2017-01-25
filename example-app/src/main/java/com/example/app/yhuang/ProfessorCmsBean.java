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

package com.example.app.yhuang;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Arrays;
import java.util.EnumSet;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.ButtonGroup;
import net.proteusframework.ui.miwt.component.Calendar;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.RadioButton;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.workspace.Workspace;
import net.proteusframework.ui.workspace.WorkspaceAware;
import net.proteusframework.users.model.Name;


/**
 * this CmsBean is for adding a professor
 * @author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/12/14 4:50 AM
 */
@I18NFile(
		symbolPrefix ="com.example.app.yhuang.ProfessorCmsBean",
		i18n = {
				@I18N(symbol = "Component Name", l10n = @L10N("hyg professor Component"))
		}
)
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProfessorCmsBean extends MIWTPageElementModelContainer implements WorkspaceAware
{
	/**firstName field*/
	private Field _firstName;
	/**lastName field*/
	private Field _lastName;
	/**rank*/
	private ComboBox _rank;
	/**joinDate*/
	private Calendar _joinDate;
	/**researchArea field*/
	private Field _researchArea;
	/**_yes button and _no button for isSabbatical*/
	private RadioButton _yes,_no;
	/**_save button and _cancel button for adding profesor and
	 * cancelling the content of professor information*/
	private PushButton _save,_cancel;
	/**the professor dao*/
	private ProfessorDAO _professorDAO=ProfessorDAO.getInstance();

	/**
	 * constructor method
	 */
	public ProfessorCmsBean()
	{
		super();
		setName(ProfessorCmsBeanLOK.COMPONENT_NAME());
		addCategory(CmsCategory.ClientBackend);
		initComponent();
	}

	/**
	 * constructor method with parameters
	 * @param professor professor object
	 * @param flag control the display of the CmsBean
	 */
	public ProfessorCmsBean(Professor professor,String flag)
	{
		initComponent();
		if("view".equals(flag))
		{
			_firstName.setText(professor.getName().getFirst());
			_firstName.setEditable(false);
			_lastName.setText(professor.getName().getLast());
			_lastName.setEditable(false);
			_rank.setSelectedObject(professor.getRank());
			_rank.setEnabled(false);
			_joinDate.setDate(professor.getStartDate());
			_joinDate.setEnabled(false);
			_researchArea.setText(professor.getResearchArea());
			_researchArea.setEditable(false);
			if(professor.isSabbatical())
			{
				_yes.setSelected(true);
				_no.setEnabled(false);
			}else {
				_no.setSelected(true);
				_yes.setEnabled(false);
			}
			_save.setVisible(false);
			_cancel.setVisible(false);
		}else if("edit".equals(flag))
		{
			_firstName.setText(professor.getName().getFirst());
			_lastName.setText(professor.getName().getLast());
			_rank.setSelectedObject(professor.getRank());
			_joinDate.setDate(professor.getStartDate());
			_researchArea.setText(professor.getResearchArea());
			if(professor.isSabbatical())
			{
				_yes.setSelected(true);
			}else {
				_no.setSelected(true);
			}
		}

	}

	/**
	 * override the method of Workspace
	 * @param workspace workspace instance
	 */
	@Override
	public void initialize(Workspace workspace)
	{
	}

	/**
	 * setup the UI
	 */
	private void initComponent()
	{
		_firstName=new Field();
		_lastName=new Field();
		_rank=new ComboBox(new SimpleListModel<Rank>(Rank.Lecturer,
				Rank.AdjunctProfessor,Rank.AssistantProfessor,
				Rank.AssociateProfessor,Rank.Professor));
		_joinDate=new Calendar();
		_researchArea=new Field();
		ButtonGroup group=new ButtonGroup();
		_yes=new RadioButton(TextSources.create("Yes"),group);
		_no=new RadioButton(TextSources.create("No"),group);
		_save=new PushButton(new LocalizedText("save"));
		_cancel=new PushButton(new LocalizedText("cancel"));

		_save.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				Professor professor=new Professor();
				Name name=new Name();
				name.setFirst(_firstName.getText());
				name.setLast(_lastName.getText());
				professor.setName(name);
				professor.setRank((Rank)_rank.getSelectedObject());
				professor.setStartDate(_joinDate.getDate());
				professor.setResearchArea(_researchArea.getText());
				if(_yes.isSelected())
				{
					professor.setSabbatical(true);
				}else
				{
					professor.setSabbatical(false);
				}
				boolean result=_professorDAO.saveProfessor(professor);
				if(result)
				{
					close();
				}
			}
		});

		_cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				close();
			}
		});

	}

	/**
	 * the init method of CmsBean
	 */
	@Override
	public void init()
	{
		super.init();
		add(Container.of(new Label(new LocalizedText("FirstName")),_firstName));
		add(Container.of(new Label(new LocalizedText("LastName")),_lastName));
		add(Container.of(new Label(new LocalizedText("Rank")),_rank));
		add(Container.of(new Label(new LocalizedText("JoinDate")),_joinDate));
		add(Container.of(new Label(new LocalizedText("ResearchArea")),_researchArea));
		add(Container.of(new Label(new LocalizedText("IsSabbatical")),_yes,_no));
		add(Container.of(_save,_cancel));
	}
}
