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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.component.CardContainer;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.DateConstraint;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;

import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.ui.workspace.AbstractUITask;
import net.proteusframework.ui.workspace.EntityWorkspaceEvent;
import net.proteusframework.ui.workspace.ListTaskManager;
import net.proteusframework.ui.workspace.StandardEventType;
import net.proteusframework.ui.workspace.Workspace;
import net.proteusframework.ui.workspace.WorkspaceAware;
import net.proteusframework.ui.workspace.WorkspaceEvent;
import net.proteusframework.ui.workspace.WorkspaceHandler;
import net.proteusframework.ui.workspace.WorkspaceHandlerContext;
import net.proteusframework.ui.workspace.WorkspaceHandlerResult;
import net.proteusframework.ui.workspace.WorkspaceImpl;

/**
 * the professor search UI
 * @author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/17/14 2:45 AM
 */
@I18NFile(
		symbolPrefix = ProfessorSearchUI.RESOURCE_NAME,
		i18n = {
				@I18N(symbol = "Component Name", l10n = @L10N("Professor searchUI")),
				@I18N(symbol = "Professor_firstName", l10n = @L10N("Professor First Name")),
				@I18N(symbol = "Professor_lastName", l10n = @L10N("Professor Last Name")),
				@I18N(symbol = "Rank", l10n = @L10N("Rank")),
				@I18N(symbol = "joinDate", l10n = @L10N("Join Date")),
				@I18N(symbol = "ResearchArea", l10n = @L10N("Research Area")),
		}
)
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProfessorSearchUI extends MIWTPageElementModelContainer implements SearchUIOperationHandler,WorkspaceHandler
{
	/** RESOURCE_NAME */
	public final static String RESOURCE_NAME = "com.example.app.yhuang.ProfessorSearchUI";
	/** Logger */
	private final static Logger _logger = Logger.getLogger(ProfessorSearchUI.class);
	/** SearchUI */
	private SearchUIImpl _searchUI;
	/** the last Container */
	private HistoryContainer _historyContainer = new HistoryContainer();
	/** main Container */
	private Container _mainCon = new Container();
	/** Tasks Container */
	private CardContainer _taskContainer;
	/** Locale Context. */
	private LocaleContext _lc;
	/** Workspace */
	private WorkspaceImpl _workspace;
	/**Professor Dao */
	@Autowired
	private ProfessorDAO _professorDAO=ProfessorDAO.getInstance();

	/**
	 * constructor method
	 */
	public ProfessorSearchUI()
	{
		super.init();
		setName(ProfessorSearchUILOK.COMPONENT_NAME());
		addCategory(CmsCategory.ClientBackend);
	}

	/**
	 * init method
	 */
	@Override
	public void init()
	{
		super.init();
		_setupUI();
	}

	/**
	 * setup the searchUI
	 */
	private void _setupUI()
	{
		_mainCon.removeAllComponents();
		_lc = getLocaleContext();

		final MessageContainer notifiable = new MessageContainer(TimeUnit.SECONDS.toMillis(60));
		_taskContainer = new CardContainer();
		_workspace = new WorkspaceImpl(new ListTaskManager(), notifiable, _taskContainer);

		addClassName(Workspace.CSS_WORKSPACE).addClassName(Workspace.CSS_WORKSPACE_TOP_SEARCH);
		_workspace.registerHandler(this);

		SearchModelImpl searchModel=new SearchModelImpl();
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "name.first")
								.withColumnName(ProfessorSearchUILOK.PROFESSOR_FIRSTNAME().getAnyLocalization(_lc))
				));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "name.last")
						.withColumnName(ProfessorSearchUILOK.PROFESSOR_LASTNAME().getAnyLocalization(_lc))));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "rank")
						.withColumnName(ProfessorSearchUILOK.RANK().getAnyLocalization(_lc))));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "startDate")
						.withColumnName(ProfessorSearchUILOK.JOINDATE().getAnyLocalization(_lc))));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "researchArea")
						.withColumnName(ProfessorSearchUILOK.RESEARCHAREA().getAnyLocalization(_lc))));

		searchModel.getConstraints().add(
				new SimpleConstraint().withLabel(ProfessorSearchUILOK.PROFESSOR_FIRSTNAME()).withProperty("name.first")
						.withOperator(PropertyConstraint.Operator.like));
		searchModel.getConstraints().add(
				new SimpleConstraint().withLabel(ProfessorSearchUILOK.PROFESSOR_LASTNAME()).withProperty("name.last")
						.withOperator(PropertyConstraint.Operator.like));


		List<Rank> rankList=new ArrayList<Rank>();
		rankList.add(null);
		rankList.addAll(Arrays.asList(Rank.values()));
		final ComboBoxConstraint rankConstraint = new ComboBoxConstraint(rankList, CommonButtonText.ANY,CommonButtonText.ANY)
		{
			public void addCriteria(QLBuilder qb, Component constraintComponent)
			{
				Object obj = getValue(constraintComponent);
				if (obj == null)
					return;
				qb.appendCriteria("rank", Operator.eq, obj);
			}
		};
		searchModel.getConstraints().add(rankConstraint.withLabel(TextSources.create(ProfessorSearchUILOK.RANK())).withCoerceValue
				(false));

		DateConstraint startDateConstraint=new DateConstraint();
		startDateConstraint.setLabel(ProfessorSearchUILOK.JOINDATE());
		searchModel.getConstraints().add(startDateConstraint.withProperty("startDate").withOperator(
				PropertyConstraint.Operator.like));

		searchModel.getConstraints().add(
				new SimpleConstraint().withLabel(ProfessorSearchUILOK.RESEARCHAREA()).withProperty("researchArea")
						.withOperator(PropertyConstraint.Operator.like));

		ActionColumn actionColumn = new ActionColumn();
		actionColumn.setIncludeCopy(false);
		searchModel.getResultColumns().add(actionColumn);

		Supplier<QLBuilder> builderSupplier=new Supplier<QLBuilder>()
		{
			@Override
			public QLBuilder get()
			{
				return new QLBuilderImpl(Professor.class,"professor");
			}
		};
		SearchSupplierImpl supplier=new SearchSupplierImpl();
		supplier.setSearchUIOperationHandler(this);
		supplier.setBuilderSupplier(builderSupplier);
		supplier.setSearchModel(searchModel);
		SearchUIImpl.Options options = new SearchUIImpl.Options(getClass().getName());
		options.addSearchSupplier(supplier);
		_searchUI = new SearchUIImpl(options);

		final PushButton addBtn= CommonActions.ADD.push();
		addBtn.setLabel(TextSources.create("add new professor"));
		addBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				ProfessorCmsBean professorCmsBean=new ProfessorCmsBean();
				_historyContainer.navigateBackOnClose(professorCmsBean);
				_historyContainer.getHistory().add(new HistoryElement(professorCmsBean));
			}
		});
		_mainCon.add(Container.of("search-part", _searchUI,addBtn));
		_mainCon.add(_workspace.getUITaskManager().getComponent());
		_mainCon.add(_taskContainer);
		_historyContainer.setDefaultComponent(_mainCon);
		add(_historyContainer);

	}

	/**
	 * support the operation ,like add,view,delete
	 * @param operation like add,view,delete,edit
	 * @return if support some operations,return true
	 */
	@Override
	public boolean supportsOperation(SearchUIOperation operation)
	{
		switch (operation)
		{
			case delete:
			case view:
				return true;
			default:
				return false;
		}
	}

	/**
	 * override the method of Workspace
	 * @param context the SearchUIOperationContext context
	 */
	@Override
	public void handle(SearchUIOperationContext context)
	{
		final Object rowData = EntityRetriever.getInstance().narrowProxyIfPossible(context.getData());
		if (rowData == null || !(rowData instanceof Professor))
			return;

		final Professor professor = (Professor) rowData;
		switch (context.getOperation())
		{
			case view:
				Response response = net.proteusframework.ui.miwt.event.Event.getResponse();
				response.redirect(response.createURL("/details?id="+professor.getId()));
				break;
			case delete:
				_workspace.handle(new EntityWorkspaceEvent<Professor>(this, professor, StandardEventType.deletion));
				break;
			default:
				break;
		}
	}

	/**
	 * override the method of Workspace
	 * @param workspaceevent the WorkspaceEvent object
	 * @return true or false
	 */
	@Override
	public boolean supportsWorkspaceEvent(WorkspaceEvent workspaceevent)
	{
		return (EnumSet.of(StandardEventType.selection, StandardEventType.deletion)
				.contains(workspaceevent.getType()))
				&& (workspaceevent instanceof EntityWorkspaceEvent<?>)
				&& ((EntityWorkspaceEvent<?>) workspaceevent).getEntityClass() == Professor.class;
	}

	/**
	 * override the method of Workspace
	 * @param workspaceevent the WorkspaceEvent object
	 * @param context WorkspaceHandlerContext object
	 * @return WorkspaceHandlerResult instance
	 */
	@Override
	public WorkspaceHandlerResult handle(WorkspaceEvent workspaceevent, WorkspaceHandlerContext context)
	{
		if (workspaceevent instanceof EntityWorkspaceEvent<?> && ((EntityWorkspaceEvent<?>) workspaceevent).getEntityClass() == Professor.class)
		{
			EntityWorkspaceEvent<Professor> ewe = (EntityWorkspaceEvent<Professor>) workspaceevent;
			StandardEventType type = (StandardEventType) ewe.getType();
			final Professor professor = ewe.getEntity();
			WorkspaceAware ui = null;
			switch (type)
			{
				case deletion:
					_professorDAO.deleteProfessor(professor);
					_searchUI.doAction(SearchUIAction.search);
					return new WorkspaceHandlerResult().setEventHandled(true);
				default:
					return new WorkspaceHandlerResult().setEventHandled(false);
			}
		}
		return new WorkspaceHandlerResult().setEventHandled(true);
	}
}
