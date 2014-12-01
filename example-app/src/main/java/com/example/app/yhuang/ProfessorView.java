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

import com.google.common.base.Supplier;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;
import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;
import com.i2rd.contentmodel.constraint.Constraint;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
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
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;

/**
 * ProfessorView for showing all the professors in the db
 *
 * @author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/17/14 2:45 AM
 */
@I18NFile(
		symbolPrefix = ProfessorView.RESOURCE_NAME,
		i18n = {
				@I18N(symbol = "Component Name", l10n = @L10N("ProfessorView")),
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
public class ProfessorView extends MIWTPageElementModelContainer implements SearchUIOperationHandler
{
	/** RESOURCE_NAME */
	public final static String RESOURCE_NAME = "com.example.app.yhuang.ProfessorView";
	/** Logger */
	private final static Logger _logger = Logger.getLogger(ProfessorView.class);
	/** SearchUI */
	private SearchUIImpl _searchUI;
	/** the last Container */
	private HistoryContainer _historyContainer = new HistoryContainer();

	/**
	 * constructor method
	 */
	public ProfessorView()
	{
		super.init();
		setName(ProfessorViewLOK.COMPONENT_NAME());
		addCategory(CmsCategory.ClientBackend);
	}

	/**
	 * init method,when server start,it run and run one time
	 */
	@Override
	public void init()
	{
		super.init();
		_setupUI();
	}

	/**
	 * setup UI
	 */
	private void _setupUI()
	{
		SearchModelImpl searchModel=new SearchModelImpl();
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "id")
								.withColumnName(ProfessorViewLOK.PROFESSOR_ID())
				));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "name.first")
								.withColumnName(ProfessorViewLOK.PROFESSOR_FIRSTNAME())
				));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "name.last")
						.withColumnName(ProfessorViewLOK.PROFESSOR_LASTNAME())));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "rank")
						.withColumnName(ProfessorViewLOK.RANK())));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "startDate")
						.withColumnName(ProfessorViewLOK.JOINDATE())));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "researchArea")
						.withColumnName(ProfessorViewLOK.RESEARCHAREA())));
		searchModel.getResultColumns().add(
				new SearchResultColumnImpl().withTableColumn(new PropertyColumn(Professor.class, "sabbatical")
						.withColumnName(ProfessorViewLOK.ISSABBATICAL())));

		searchModel.getConstraints().add(
				new SimpleConstraint().withLabel(ProfessorViewLOK.PROFESSOR_FIRSTNAME()).withProperty("name.first")
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
		_historyContainer.setDefaultComponent(_searchUI);
		add(_historyContainer);
	}

	/**
	 * support the operation,like view,add,edit and delete
	 * @param operation like view,add,edit and delete
	 * @return true or false
	 */
	@Override
	public boolean supportsOperation(SearchUIOperation operation)
	{
		switch (operation)
		{
			case view:
				return true;
			default:
				return false;
		}
	}

	/**
	 * handle the operation like view,add,edit and delete
	 * @param context SearchUIOperationContext object
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
			default:
				break;
		}
	}
}
