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

package com.example.app.profile.ui.terminology;

import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.terminology.ProfileTerms;
import com.example.app.profile.service.DefaultProfileTermProvider;
import org.apache.logging.log4j.Level;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.spring.UtilityContext;
import net.proteusframework.internet.support.LocaleUtil;
import net.proteusframework.ui.column.DataColumnTable;
import net.proteusframework.ui.column.FunctionColumn;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.data.Column;
import net.proteusframework.ui.miwt.locale.LocalizedObjectKeyFieldComponent;
import net.proteusframework.ui.miwt.util.CommonColumnText;

/**
 * ProfileTerms Editor.
 *
 * @author russ (russ@venturetech.net)
 */
@Configurable(preConstruction = true)
public class ProfileTermsEditor extends Container implements ValueEditor<ProfileTerms>
{
    private ProfileTerms _profileTerms;
    @Autowired
    private DefaultProfileTermProvider _defaultProfileTermProvider;

    private final LinkedHashMap<ProfileTerm, TransientLocalizedObjectKey> _termMap = new LinkedHashMap<>();
    private DataColumnTable<ProfileTerm> _table;
    private ModificationState _modificationState = ModificationState.UNCHANGED;
    private final Company _company;

    /**
     * Instantiates a new Profile terms editor.
     * @param company coaching entity.
     */
    public ProfileTermsEditor(Company company)
    {
        _company = company;
    }

    @Override
    public void init()
    {
        super.init();
        final Locale inputLocale = Locale.ENGLISH;
        final LocaleContext inputLocaleContext = LocaleUtil.getLocaleContext(inputLocale);
        final FunctionColumn<ProfileTerm, TextSource> providerCol = new FunctionColumn<ProfileTerm, TextSource>(
            ProfileTerm.class, TextSource.class, this::getTerm)
        {
            @Override
            public boolean isValueEditable(Object rowData)
            {
                return true;
            }

            @Override
            public void setValue(Object rowData, Object value)
            {
                final ProfileTerm pt = (ProfileTerm) rowData;
                final TransientLocalizedObjectKey tlok = _termMap.get(pt);
                final TextSource defaultTerm = getDefaultTerm(pt);
                String existing = tlok.getText() == null ? null : tlok.getText().get(inputLocale);
                if(!defaultTerm.getText(inputLocaleContext).toString().equals(value))
                {
                    if(!Objects.equals(existing, value))
                    {
                        tlok.addLocalization(inputLocale, value.toString());
                        _modificationState = ModificationState.CHANGED;
                    }
                }
                else
                {
                    if(existing != null && tlok.getText() != null)
                    {
                        tlok.removeLocalization(inputLocale);
                        _modificationState = ModificationState.CHANGED;
                    }
                }
            }
        };
        providerCol.setColumnName(_company.getName());
        final FunctionColumn<ProfileTerm, TextSource> defaultCol = new FunctionColumn<>(ProfileTerm.class, TextSource.class,
            this::getDefaultTerm);
        defaultCol.setColumnName(CommonColumnText.DEFAULT);

        _table = new DataColumnTable<>(providerCol, defaultCol);
        _table.addClassName("profile-terms");

        final LocalizedObjectKeyFieldComponent field = new LocalizedObjectKeyFieldComponent();
        field.setInputLocale(inputLocale);
        field.setInsertLocalizedObjectKeyWhenEmpty(false);
        field.setUpdateLocalizedObjectKeyWhenEmpty(true);
        field.setTrimWhitespace(true);
        final Column providerColumn = _table.getUIColumn(providerCol);
        assert providerColumn != null;
        providerColumn.setTableCellEditor(field);
        add(_table);
        setValue(_profileTerms);
    }

    TextSource getTerm(ProfileTerm term)
    {
        final TransientLocalizedObjectKey tlok = _termMap.get(term);
        if(tlok == null || tlok.getText() == null || tlok.getText().isEmpty())
            return term.getDefaultTerm(_defaultProfileTermProvider);
        return tlok;
    }

    TextSource getDefaultTerm(ProfileTerm term)
    {
        return term.getDefaultTerm(_defaultProfileTermProvider);
    }

    @Nullable
    @Override
    public ProfileTerms getValue()
    {
        return _profileTerms;
    }

    @Override
    public void setValue(@Nullable ProfileTerms profileTerms)
    {
        _profileTerms = profileTerms;
        if(!isInited())
            return;
        if(profileTerms != null)
        {
            Hibernate.initialize(profileTerms);
            _termMap.clear();
            ProfileTerm.populateTermMap(profileTerms, _termMap, UtilityContext.LOCALE_SOURCE.getBean());
            _table.getDefaultModel().setRows(_termMap.keySet());
        }
    }

    @Override
    public ModificationState getModificationState()
    {
        return _modificationState;
    }

    @Nullable
    @Override
    public ProfileTerms getUIValue(Level logErrorLevel)
    {
        final ProfileTerms terms = new ProfileTerms();
        _termMap.entrySet().forEach(entry -> {
            if(entry.getValue() != null)
            {
                final TransientLocalizedObjectKey tlok = entry.getValue();
                if(tlok.hasInMemoryLocalization(true))
                    entry.getKey().setTerm(terms, tlok);
                else
                    entry.getKey().setTerm(terms, null);
            }
        });
        return terms;
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        return true;
    }

    @Nullable
    @Override
    public ProfileTerms commitValue() throws MIWTException
    {
        final ProfileTerms terms = _profileTerms != null ? _profileTerms : new ProfileTerms();
        _termMap.entrySet().forEach(entry -> {
            if(entry.getValue() != null)
            {
                final TransientLocalizedObjectKey tlok = entry.getValue();
                if(tlok.hasInMemoryLocalization(true))
                    entry.getKey().setTerm(terms, tlok);
                else
                    entry.getKey().setTerm(terms, null);
            }
        });
        return terms;
    }

    @Override
    public boolean isEditable()
    {
        return true;
    }

    @Override
    public void setEditable(boolean b)
    {
        if(!b)
            throw new UnsupportedOperationException();

    }
}
