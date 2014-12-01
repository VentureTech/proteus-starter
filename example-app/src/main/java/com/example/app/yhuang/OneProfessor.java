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

import sun.font.TextLabel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;
import com.i2rd.miwt.richtext.DeprecatedAttributesTransform;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.ClickToEditForm;
import net.proteusframework.ui.miwt.util.RendererEditorState;
import net.proteusframework.ui.miwt.component.Field;

/**
 * OneProfessor shows one professor details
 *
 * @author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/24/14 6:13 AM
 */
@I18NFile(
		symbolPrefix = OneProfessor.RESOURCE_NAME,
		i18n = {
				@I18N(symbol = "Component Name", l10n = @L10N("OneProfessor")),
				@I18N(symbol = "Professor_id", l10n = @L10N("Professor ID")),
				@I18N(symbol = "Professor_firstName", l10n = @L10N("Professor First Name")),
				@I18N(symbol = "Professor_lastName", l10n = @L10N("Professor Last Name")),
				@I18N(symbol = "Rank", l10n = @L10N("Rank")),
				@I18N(symbol = "joinDate", l10n = @L10N("Join Date")),
				@I18N(symbol = "ResearchArea", l10n = @L10N("Research Area")),
				@I18N(symbol = "IsSabbatical", l10n = @L10N("IsSabbatical")),
		}
)
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OneProfessor extends MIWTPageElementModelContainer
{
	/** RESOURCE_NAME */
	public final static String RESOURCE_NAME = "com.example.app.yhuang.OneProfessor";
	/** Logger */
	private final static Logger _logger = Logger.getLogger(OneProfessor.class);
	/**firstName field*/
	private Field _firstName;
	/**lastName field*/
	private Field _lastName;
	/**rank*/
	private Field _rank;
	/**joinDate*/
	private Field _startDate;
	/**researchArea field*/
	private Field _researchArea;
	/**is sabbatical*/
	private Field __isSabbatical;
	/**the professor dao*/
	@Autowired
	private ProfessorDAO _professorDAO=ProfessorDAO.getInstance();

	/**
	 * constructor method
	 */
	public OneProfessor()
	{
		super();
		setName(OneProfessorLOK.COMPONENT_NAME());
		addCategory(CmsCategory.ClientBackend);
	}

	/**
	 * setup UI
	 */
	private void _initComponent()
	{
		_firstName=new Field();
		_lastName=new Field();
		_rank=new Field();
		_startDate=new Field();
		_researchArea=new Field();
		__isSabbatical=new Field();

		_firstName.setEditable(false);
		_lastName.setEditable(false);
		_rank.setEditable(false);
		_startDate.setEditable(false);
		_researchArea.setEditable(false);
		__isSabbatical.setEditable(false);
	}

	/**
	 * init method,it runs one time when the server starts
	 */
	@Override
	public void init()
	{
		super.init();
		_initComponent();
		add(Container.of(new Label(OneProfessorLOK.PROFESSOR_FIRSTNAME()), _firstName));
		add(Container.of(new Label(OneProfessorLOK.PROFESSOR_LASTNAME()),_lastName));
		add(Container.of(new Label(OneProfessorLOK.RANK()),_rank));
		add(Container.of(new Label(OneProfessorLOK.JOINDATE()),_startDate));
		add(Container.of(new Label(OneProfessorLOK.RESEARCHAREA()),_researchArea));
		add(Container.of(new Label(OneProfessorLOK.ISSABBATICAL()),__isSabbatical));
	}

	/**
	 * by this method,can receive the parameter from response
	 * @param request Request instance
	 * @param response Response instance
	 * @param state RendererEditorState<?> instance
	 */
	@Override
	public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
	{
		super.preRenderProcess(request, response, state);
		String sid=request.getParameter("id");
		Long id=Long.parseLong(sid);
		Professor professor=_professorDAO.findById(id);
		_firstName.setText(professor.getName().getFirst());
		_lastName.setText(professor.getName().getLast());
		_rank.setText(professor.getRank().toString());
		_startDate.setText(professor.getStartDate().toString());
		_researchArea.setText(professor.getResearchArea());
		__isSabbatical.setText(professor.isSabbatical()+"");
	}
}
