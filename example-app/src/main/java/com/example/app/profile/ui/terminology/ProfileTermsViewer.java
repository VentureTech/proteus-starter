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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.spring.UtilityContext;
import net.proteusframework.ui.column.DataColumnTable;
import net.proteusframework.ui.column.FunctionColumn;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.util.CommonColumnText;

/**
 * ProfileTerms Editor.
 *
 * @author russ (russ@venturetech.net)
 */
@Configurable(preConstruction = true)
public class ProfileTermsViewer extends Container
{
    @Autowired
    private DefaultProfileTermProvider _defaultProfileTermProvider;

    private final LinkedHashMap<ProfileTerm, TransientLocalizedObjectKey> _termMap = new LinkedHashMap<>();
    private final Company _company;

    /**
     * Instantiates a new Profile terms editor.
     * @param company company.
     * @param profileTerms the profile terms to view.
     */
    public ProfileTermsViewer(@Nonnull Company company, @Nonnull ProfileTerms profileTerms)
    {
        _company = company;
        Hibernate.initialize(profileTerms);
        ProfileTerm.populateTermMap(profileTerms, _termMap, UtilityContext.LOCALE_SOURCE.getBean());
    }

    @Override
    public void init()
    {
        super.init();

        final FunctionColumn<ProfileTerm, TextSource> providerCol = new FunctionColumn<>(ProfileTerm.class, TextSource.class,
            this::getTerm);
        providerCol.setColumnName(
            _company.getName()
        );
        final FunctionColumn<ProfileTerm, TextSource> defaultCol = new FunctionColumn<>(ProfileTerm.class, TextSource.class,
            this::getDefaultTerm);
        defaultCol.setColumnName(CommonColumnText.DEFAULT);

        final DataColumnTable<ProfileTerm> table = new DataColumnTable<>(providerCol, defaultCol);
        table.getDefaultModel().setRows(_termMap.keySet());
        table.addClassName("profile-terms");
        add(table);
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
}
