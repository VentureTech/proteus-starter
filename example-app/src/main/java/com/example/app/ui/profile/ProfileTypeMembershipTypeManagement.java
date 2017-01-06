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

package com.example.app.ui.profile;

import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.support.AppUtil;
import com.example.app.ui.CommonEditorFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.proteusframework.core.GloballyUniqueStringGenerator;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.NamedObjectComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.AbstractDataColumn;
import net.proteusframework.ui.column.DataColumnTable;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;

import static com.example.app.ui.UIText.*;
import static com.example.app.ui.profile.ProfileTypeMembershipTypeManagementLOK.*;

/**
 * Membership management for a ProfileType.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7 /1/16 9:17 AM
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.profile.ProfileTypeMembershipTypeManagement",
    i18n = {
        @I18N(symbol = "Development Provider Role FMT", l10n = @L10N("{0} {1}")),
        @I18N(symbol = "Modify Permissions", l10n = @L10N("Modify Permissions")),
        @I18N(symbol = "Edit Role", l10n = @L10N("Edit {0}")),
        @I18N(symbol = "Modify Role Permissions", l10n = @L10N("Modify {0} Permissions")),
        @I18N(symbol = "Confirmation Are You Sure Modification FMT",
            l10n = @L10N("Existing {0} with custom permissions, who have this {1} will have their permissions overwritten.  Are "
                         + "you sure you want to modify permissions for this {1}?"))
    }
)
@Configurable()
public class ProfileTypeMembershipTypeManagement extends Container
{
    /** Property Change Event fired when value is set */
    public static final String EVENT_SET_VALUE = "event-value-set";
    /** Property Change Event fired when core types are set */
    public static final String EVENT_SET_CORE_TYPES = "event-core-types-set";
    /** Property Change Event fired when value is modified */
    public static final String EVENT_MODIFIED_VALUE = "event-value-modified";

    @Autowired
    private EntityRetriever _er;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private CompanyDAO _coachingEntityDAO;
    @Autowired
    private SelectedCompanyTermProvider _terms;

    private ProfileType _value;
    private final List<String> _coreTypeProgIds = new ArrayList<>();

    /**
     * Instantiates a new Profile type membership type management.
     *
     * @param value the value
     */
    public ProfileTypeMembershipTypeManagement(@Nullable ProfileType value)
    {
        setValue(value);
    }

    /**
     * Sets values.
     *
     * @param value the value
     */
    public void setValue(@Nullable ProfileType value)
    {
        ProfileType oldVal = _value;
        _value = value;
        firePropertyChange(EVENT_SET_VALUE, _value == null ? oldVal : null, _value);
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    @Nullable
    public ProfileType getValue()
    {
        return _er.reattachIfNecessary(_value);
    }

    /**
     * Sets core type prog ids.
     *
     * @param coreTypeProgIds the core type prog ids
     */
    public void setCoreTypeProgIds(@Nullable List<String> coreTypeProgIds)
    {
        _coreTypeProgIds.clear();
        if(coreTypeProgIds != null)
            _coreTypeProgIds.addAll(coreTypeProgIds);
        firePropertyChange(EVENT_SET_CORE_TYPES, null, new ArrayList<>(_coreTypeProgIds));
    }

    @Override
    public void init()
    {
        super.init();

        AbstractDataColumn actionsCol = new FixedValueColumn();
        AbstractDataColumn roleCol = new FixedValueColumn()
            .withColumnName(DEVELOPMENT_PROVIDER_ROLE_FMT(_terms.company(), MEMBERSHIP()));
        AbstractDataColumn descCol = new FixedValueColumn()
            .withColumnName(CommonColumnText.DEFAULT);

        DataColumnTable<MembershipType> dataTable = new DataColumnTable<>(actionsCol, roleCol, descCol);

        //Create Actions Column Renderer
        PushButton edit = CommonActions.EDIT.push();
        edit.addActionListener(ev -> editMemType(dataTable.getLeadSelection()));
        PushButton modifyPerms = new PushButton(MODIFY_PERMISSIONS());
        modifyPerms.addActionListener(ev -> modifyMemTypePerms(dataTable.getLeadSelection()));
        PushButton delete = CommonActions.DELETE.push();
        delete.addActionListener(ev -> deleteMemType(dataTable.getLeadSelection()));
        Container actionsRenderer = new Container(){
            @Override
            public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
            {
                MembershipType val = (MembershipType)value;
                delete.setVisible(!_coreTypeProgIds.contains(val.getProgrammaticIdentifier())
                                  && !_profileDAO.isMembershipTypeInUse(val));
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        actionsRenderer.addClassName("actions");
        actionsRenderer.add(edit);
        actionsRenderer.add(modifyPerms);
        actionsRenderer.add(delete);

        //Set up Renderers for Columns
        Optional.ofNullable(dataTable.getUIColumn(actionsCol)).ifPresent(uiCol -> uiCol.setTableCellRenderer(actionsRenderer));
        Optional.ofNullable(dataTable.getUIColumn(roleCol)).ifPresent(uiCol -> uiCol.setTableCellRenderer(
            new CustomCellRenderer(TextSources.EMPTY, input -> {
                MembershipType memType = (MembershipType)input;
                return memType.getName();
            })));
        Optional.ofNullable(dataTable.getUIColumn(descCol)).ifPresent(uiCol -> uiCol.setTableCellRenderer(
            new CustomCellRenderer(TextSources.EMPTY, input -> {
                MembershipType memType = (MembershipType)input;
                return Optional.ofNullable(memType.getDescription()).map(val -> (TextSource)val).orElse(TextSources.EMPTY);
            })));

        dataTable.getDefaultModel().setRows(getRowsToSet());
        addPropertyChangeListener(EVENT_SET_VALUE, evt -> dataTable.getDefaultModel().setRows(getRowsToSet()));
        addPropertyChangeListener(EVENT_SET_CORE_TYPES, evt -> dataTable.getDefaultModel().setRows(getRowsToSet()));
        addPropertyChangeListener(EVENT_MODIFIED_VALUE, evt -> dataTable.getDefaultModel().setRows(getRowsToSet()));

        PushButton addButton = CommonActions.ADD.push();
        addButton.addActionListener(ev -> editMemType(new MembershipType()));

        add(of("search-wrapper",
            of("search",
                of("actions entity-actions", addButton),
                of("pager pager-search-results", dataTable))));
    }

    private Collection<MembershipType> getRowsToSet()
    {
        return Optional.ofNullable(getValue())
            .map(ProfileType::getMembershipTypeSet)
            .orElse(Collections.emptySet()).stream()
            .sorted(new NamedObjectComparator(getLocaleContext()))
            .collect(Collectors.toList());
    }

    private void deleteMemType(@Nullable MembershipType memType)
    {
        if(memType == null || getValue() == null || _profileDAO.isMembershipTypeInUse(memType)) return;
        ProfileType pt = getValue();
        pt.getMembershipTypeSet().remove(memType);
        setValue(_profileDAO.mergeProfileType(pt));
        firePropertyChange(EVENT_MODIFIED_VALUE, null, true);
    }

    private void modifyMemTypePerms(@Nullable MembershipType memType)
    {
        if(memType == null || getValue() == null) return;
        Dialog dlg = new Dialog(getApplication(), MODIFY_ROLE_PERMISSIONS(MEMBERSHIP()));

        MembershipTypeOperationsEditorUI editor = new MembershipTypeOperationsEditorUI(memType);
        PushButton save = CommonActions.SAVE.push();
        save.getButtonDisplay().setConfirmText(CONFIRMATION_ARE_YOU_SURE_MODIFICATION_FMT(USERS(), MEMBERSHIP_TYPE()));
        PushButton cancel = CommonActions.CANCEL.push();

        Container ui = of("edit-membership-type", of("actions", save, cancel), editor);
        ActionListener closer = ev -> dlg.close();

        save.addActionListener(ev -> {
            if(((Supplier<Boolean>)() -> {
                final MembershipType membershipType = _er.reattachIfNecessary(memType);
                membershipType.getDefaultOperations().clear();
                membershipType.getDefaultOperations().addAll(editor.getSelectedOperations());
                _profileDAO.mergeMembershipType(membershipType, true);
                return true;
            }).get())
            {
                setValue(getValue());
                closer.actionPerformed(ev);
            }
        });
        cancel.addActionListener(closer);

        dlg.add(ui);

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }

    private void editMemType(@Nullable MembershipType memType)
    {
        if(memType == null || getValue() == null) return;
        Dialog dlg = new Dialog(getApplication(), EDIT_ROLE(MEMBERSHIP()));
        ProfileType pt = getValue();
        AppUtil.initialize(pt);

        PropertyEditor<MembershipType> propEditor = new PropertyEditor<>();
        CompositeValueEditor<MembershipType> editor = new CompositeValueEditor<>(MembershipType.class);

        if(memType.getDescription() != null)
        {
            editor.add(of("prop default", CommonColumnText.DEFAULT, new Label(Optional.ofNullable(memType.getDescription())
                .map(desc -> (TextSource) desc)
                .orElse(TextSources.EMPTY)).withHTMLElement(HTMLElement.div)));
        }
        CommonEditorFields.addNameEditor(editor);

        ReflectiveAction save = CommonActions.SAVE.defaultAction();
        ReflectiveAction cancel = CommonActions.CANCEL.defaultAction();

        save.setActionListener(ev -> {
            if(propEditor.persist(input -> {
                input.setProfileType(pt);
                if(StringFactory.isEmptyString(input.getProgrammaticIdentifier()))
                {
                    input.setProgrammaticIdentifier(GloballyUniqueStringGenerator.getUniqueString());
                }
                MembershipType mt = _profileDAO.mergeMembershipType(input);
                if(pt.getMembershipTypeSet().stream()
                       .map(MembershipType::getId)
                       .filter(i -> Objects.equals(i, mt.getId()))
                       .count() == 0)
                {
                    pt.getMembershipTypeSet().add(mt);
                }
                return Boolean.TRUE;
            }))
            {
                dlg.close();
                setValue(_profileDAO.mergeProfileType(pt));
            }
        });
        cancel.setActionListener(ev -> dlg.close());

        editor.setValue(memType);
        propEditor.setPersistenceActions(save, cancel);
        propEditor.setValueEditor(editor);

        dlg.add(propEditor);

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }
}