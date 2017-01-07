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

package com.example.app.profile.ui.resource;

import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.repository.model.Repository;
import com.example.app.repository.model.RepositoryItem;
import com.example.app.repository.model.RepositoryItemRelation;
import com.example.app.repository.model.RepositoryItemRelationType;
import com.example.app.repository.model.RepositoryItemStatus;
import com.example.app.repository.model.ResourceRepositoryItem;
import com.example.app.repository.ui.ResourceRepositoryItemValueViewer;
import com.example.app.resource.model.Resource;
import com.example.app.resource.model.ResourceVisibility;
import com.example.app.resource.service.ResourceCategoryLabelProvider;
import com.example.app.support.service.AppUtil;
import com.example.app.support.service.EntityIdCollector;
import com.example.app.support.service.InstantDayComparator;
import com.example.app.support.ui.search.KeywordConstraint;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.Pair;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocalizedNamedObjectComparator;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.ComponentTreeIterator;
import net.proteusframework.ui.miwt.util.RendererEditorState;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.SearchConstraint;

import static com.example.app.profile.ui.resource.AbstractProfileResourceListingLOK.*;
import static com.example.app.support.service.AppUtil.nullFirst;
import static java.util.Optional.ofNullable;

/**
 * Provides a UI for listing Resources using ResourceValueViewer, which is much more aesthetically pleasing than a SearchUI.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/14/16 4:36 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.resource.AbstractProfileResourceListing",
    i18n = {
        @I18N(symbol = "Label Sort By", l10n = @L10N("Sort By")),
        @I18N(symbol = "Sort Method Assignment Date ASC", l10n = @L10N("Oldest")),
        @I18N(symbol = "Sort Method Assignment Date DESC", l10n = @L10N("Newest")),
        @I18N(symbol = "Label Keyword Search", l10n = @L10N("Keyword Search (Title, Description)")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category"))
    }
)
@Configurable
public abstract class AbstractProfileResourceListing extends MIWTPageElementModelContainer
{
    /**
     * Resource Listing Sort Methods
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum SortMethod implements NamedObject
    {
        /** Sort Method */
        AssignmentDateASC(SORT_METHOD_ASSIGNMENT_DATE_ASC(), (rirel1, rirel2) ->
            InstantDayComparator.getAscInstance().compare(rirel1.getCreateTime().toInstant(),
                rirel2.getCreateTime().toInstant())),
        /** Sort Method */
        AssignmentDateDESC(SORT_METHOD_ASSIGNMENT_DATE_DESC(), (rirel1, rirel2) ->
            InstantDayComparator.getDescInstance().compare(rirel1.getCreateTime().toInstant(),
                rirel2.getCreateTime().toInstant()));

        private final TextSource _name;
        private final Comparator<RepositoryItemRelation> _relationComparator;

        SortMethod(TextSource name, Comparator<RepositoryItemRelation> relationComparator)
        {
            _name = name;
            _relationComparator = relationComparator;
        }

        /**
         * Get the comparator used for sorting the RepositoryItemRelations
         *
         * @return comparator
         */
        public Comparator<RepositoryItemRelation> getRelationComparator()
        {
            return _relationComparator;
        }

        @Nonnull
        @Override
        public TextSource getName()
        {
            return _name;
        }

        @Nullable
        @Override
        public TextSource getDescription()
        {
            return getName();
        }
    }
    /** Permanent Messages */
    protected final MessageContainer _permanentMessages = new MessageContainer();
    /** Autowired */
    @Autowired
    protected ApplicationRegistry _applicationRegistry;
    /** Autowired */
    @Autowired
    protected EntityRetriever _er;
    /** Autowired */
    @Autowired
    protected UserDAO _userDAO;
    /** Autowired */
    @Autowired
    protected MembershipOperationProvider _mop;
    /** Autowired */
    @Autowired
    protected AppUtil _appUtil;
    /** Autowired */
    @Autowired
    protected ResourceCategoryLabelProvider _rclp;
    private final BiMap<RepositoryItemRelation, Component> _currentResults = HashBiMap.create();
    private final Map<SearchConstraint, Pair<Component, Container>> _constraints = new HashMap<>();
    private final Map<ReflectiveAction, PushButton> _actions = new HashMap<>();
    private final Container _constraintsCon = of("constraints").withHTMLElement(HTMLElement.span);
    private final Container _actionsCon = of("actions search-actions").withHTMLElement(HTMLElement.span);
    private final Container _resultsCon = of("search-results resource-listing");
    private ComboBoxValueEditor<SortMethod> _sortBy;
    private boolean _includePublicOnly;
    private EnumSet<SortMethod> _sortMethods;

    /**
     * Instantiates a new Plan resources.
     */
    public AbstractProfileResourceListing()
    {
        addClassName("search-wrapper");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public void init()
    {
        super.init();

        _sortBy = new ComboBoxValueEditor<>(
            LABEL_SORT_BY(), nullFirst(new ArrayList<>(getSortMethods())), null);
        _sortBy.setCellRenderer(new CustomCellRenderer(CommonButtonText.NONE));
        _sortBy.getValueComponent().addActionListener(ev -> setupResultsUI(getResults()));
        _sortBy.setVisible(!getSortMethods().isEmpty());

        add(_permanentMessages);

        add(of("search", of("search-bar", _constraintsCon, _actionsCon),
            of("search-content", of("actions content-actions", _sortBy), _resultsCon)));

        //Set up Actions
        ReflectiveAction search = CommonActions.SEARCH.defaultAction();
        search.setActionListener(ev -> setupResultsUI(getResults()));

        ReflectiveAction reset = CommonActions.RESET.defaultAction();
        reset.setActionListener(ev -> {
            _constraints.entrySet().forEach(constraintEntry -> constraintEntry.getKey().reset(constraintEntry.getValue().getOne()));
            setupResultsUI(getResults());
        });

        addAction(search);
        addAction(reset);

        //Set up Constraints
        @SuppressWarnings("AnonymousInnerClassMayBeStatic")
        SearchConstraint keywordConstraint = new KeywordConstraint()
        {
            @Override
            public void addCriteria(QLBuilder builder, Component constraintComponent)
            {
                Object val = getValue(constraintComponent);
                if (shouldReturnConstraintForValue(val))
                {
                    QLBuilderImpl rriBuilder = new QLBuilderImpl(ResourceRepositoryItem.class, "rri");
                    rriBuilder.setProjection("distinct rri.id");
                    super.addCriteria(rriBuilder, constraintComponent);
                    List<Integer> rriIds = rriBuilder.getQueryResolver().list();
                    String property = builder.getAlias() + '.' + RepositoryItemRelation.REPOSITORY_ITEM_PROP + ".id";
                    builder.appendCriteria(property + " in (:keywordrriIds)")
                        .putParameter("keywordrriIds", rriIds.stream().collect(new EntityIdCollector<>(() -> 0)));
                }
            }
        }
            .withProperties(
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.NAME_COLUMN_PROP,
                ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.DESCRIPTION_COLUMN_PROP)
            .withLabel(LABEL_KEYWORD_SEARCH())
            .withName("resource-keyword")
            .withHTMLClass("resource-keyword")
            .withOperator(PropertyConstraint.Operator.like);

        ArrayList<net.proteusframework.cms.label.Label> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(_rclp.getEnabledLabels(Optional.empty()).stream().sorted(
            new LocalizedNamedObjectComparator(getLocaleContext())).collect(Collectors.toList()));
        @SuppressWarnings("AnonymousInnerClassMayBeStatic")
        ComboBoxConstraint categoryConstraint = new ComboBoxConstraint(categories, null,
            new CustomCellRenderer(CommonButtonText.ANY))
        {
            @Override
            public void addCriteria(QLBuilder builder, Component constraintComponent)
            {
                Object val = getValue(constraintComponent);
                if (shouldReturnConstraintForValue(val))
                {
                    QLBuilderImpl rriBuilder = new QLBuilderImpl(ResourceRepositoryItem.class, "rri");
                    rriBuilder.setProjection("distinct rri.id");
                    super.addCriteria(rriBuilder, constraintComponent);
                    List<Integer> rriIds = rriBuilder.getQueryResolver().list();
                    String property = builder.getAlias() + '.' + RepositoryItemRelation.REPOSITORY_ITEM_PROP + ".id";
                    builder.appendCriteria(property + " in (:catrriids)")
                        .putParameter("catrriids", rriIds.stream().collect(new EntityIdCollector<>(() -> 0)));
                }
            }
        };
        categoryConstraint.withName("category");
        categoryConstraint.withHTMLClass("category");
        categoryConstraint.withLabel(LABEL_CATEGORY())
            .withProperty(ResourceRepositoryItem.RESOURCE_PROP + '.' + Resource.CATEGORY_PROP);
        categoryConstraint.setCoerceValue(false);

        addConstraint(keywordConstraint);
        addConstraint(categoryConstraint);

        setupResultsUI(getResults());
    }

    private void addAction(ReflectiveAction action)
    {
        PushButton button;
        if ((button = _actions.get(action)) != null)
        {
            _actionsCon.remove(button);
        }
        button = new PushButton(action);
        _actions.put(action, button);
        _actionsCon.add(button);
    }

    private void addConstraint(SearchConstraint constraint)
    {
        Pair<Component, Container> pair;
        if ((pair = _constraints.get(constraint)) != null)
        {
            _constraintsCon.remove(pair.getTwo());
        }
        Component constraintComponent = constraint.getConstraintComponent();
        if (constraintComponent != null)
        {
            pair = new Pair<>(constraintComponent,
                of("constraint " + ofNullable(constraint.getHTMLClass()).orElse(""),
                    new Label(constraint.getLabel()), constraintComponent).withHTMLElement(HTMLElement.span));
            ComponentTreeIterator it = new ComponentTreeIterator(constraintComponent, false, false);
            while (it.hasNext())
            {
                final Component next = it.next();
                if (next instanceof Field)
                {
                    final Field field = (Field) next;
                    field.watchIncremental();
                    field.addPropertyChangeListener(Field.PROP_TEXT,
                        evt -> setupResultsUI(getResults()));
                }
                else if (next instanceof ComboBox)
                {
                    final ComboBox comboBox = (ComboBox) next;
                    comboBox.addActionListener(ev -> setupResultsUI(getResults()));
                }
                else
                    HTMLFeature.watch.add(next);
            }
        }
        else
        {
            pair = new Pair<>(null, null);
        }
        _constraints.put(constraint, pair);
        if (pair.getTwo() != null)
        {
            _constraintsCon.add(pair.getTwo());
        }
    }

    @Override
    public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
    {
        super.preRenderProcess(request, response, state);

        if (!request.isPartial() && isInited())
        {
            setupResultsUI(getResults());
        }
    }

    private void setupResultsUI(List<RepositoryItemRelation> results)
    {
        List<RepositoryItemRelation> relationsToRemove = new ArrayList<>();
        _currentResults.keySet().forEach(currRes -> {
            if (!results.contains(currRes))
            {
                _resultsCon.remove(_currentResults.get(currRes));
                relationsToRemove.add(currRes);
            }
        });
        relationsToRemove.forEach(_currentResults::remove);
        results.forEach(result -> {
            if (!_currentResults.keySet().contains(result))
            {
                ResourceRepositoryItem rri;
                if ((rri = result.getCastRepositoryItem(ResourceRepositoryItem.class)) != null)
                {
                    ResourceRepositoryItemValueViewer vv = new ResourceRepositoryItemValueViewer(rri);
                    Container container = of("resource", vv);
                    _resultsCon.add(container);
                    _currentResults.put(result, container);
                }
            }
        });
        if (!getSortMethods().isEmpty())
        {
            _resultsCon.sort((com1, com2) -> {
                RepositoryItemRelation rirel1 = _currentResults.inverse().get(com1);
                RepositoryItemRelation rirel2 = _currentResults.inverse().get(com2);
                SortMethod sortMethod = _sortBy.commitValue();
                if (sortMethod != null)
                {
                    return sortMethod.getRelationComparator().compare(rirel1, rirel2);
                }
                else return 0;
            });
        }
    }

    private List<RepositoryItemRelation> getResults()
    {
        QLBuilderImpl builder = new QLBuilderImpl(RepositoryItemRelation.class, "rirel");

        List<Integer> repoIds = getRepositories().stream().map(Repository::getId).collect(new EntityIdCollector<>(() -> 0));
        JoinedQLBuilder repoItemBuilder = builder.createJoin(
            QLBuilder.JoinType.INNER, RepositoryItemRelation.REPOSITORY_ITEM_PROP, "ri");
        repoItemBuilder.appendCriteria(RepositoryItem.STATUS_COLUMN_PROP,
            PropertyConstraint.Operator.eq, RepositoryItemStatus.Active);
        repoItemBuilder.appendCriteria("ri.id in (SELECT rri.id FROM ResourceRepositoryItem rri)");

        if (_includePublicOnly)
        {
            builder.startGroup(JunctionOperator.AND);
            repoItemBuilder.appendCriteria("ri.id in (SELECT rri.id FROM ResourceRepositoryItem rri inner join rri.resource res "
                                           + "where res.visibility = :public)")
                .putParameter("public", ResourceVisibility.Public);
            builder.appendCriteria(RepositoryItemRelation.RELATION_TYPE_PROP, PropertyConstraint.Operator.eq,
                RepositoryItemRelationType.owned);
            builder.endGroup();
        }
        else
        {
            builder.appendCriteria(builder.getAlias() + '.' + RepositoryItemRelation.REPOSITORY_PROP + ".id in (:repoIds)")
                .putParameter("repoIds", repoIds);
        }

        _constraints.keySet().forEach(constraint -> constraint.addCriteria(builder, _constraints.get(constraint).getOne()));

        return builder.getQueryResolver().list();
    }

    /**
     * Get the supported Sort Methods
     *
     * @return the sort methods
     */
    public EnumSet<SortMethod> getSortMethods()
    {
        return _sortMethods;
    }

    /**
     * Get the Repositories that this AbstractProfileResourceListing is listing resources from
     *
     * @return repositories
     */
    protected abstract List<Repository> getRepositories();

    /**
     * Set the supported Sort Methods
     *
     * @param sortMethods the sort methods
     */
    public void setSortMethods(EnumSet<SortMethod> sortMethods)
    {
        _sortMethods = sortMethods;
    }

    /**
     * Get the boolean flag determining if public resources should be included within this listing
     *
     * @return include public flag -- if true, public resources will be included
     */
    public boolean isIncludePublicOnly()
    {
        return _includePublicOnly;
    }

    /**
     * Set the boolean flag determining if public resources should be included within this listing
     *
     * @param includePublicOnly if true, public resources will be included
     */
    public void setIncludePublicOnly(boolean includePublicOnly)
    {
        _includePublicOnly = includePublicOnly;
    }
}
