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

package com.example.app.login.social.ui;

import javax.annotation.Nullable;

import net.proteusframework.cms.PageElement;
import net.proteusframework.cms.PageElementModelConfiguration;
import net.proteusframework.cms.category.Categorized;
import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.component.AbstractContentElement;
import net.proteusframework.cms.component.ContentElement;
import net.proteusframework.cms.component.generator.Generator;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.login.social.ui.SocialLoginElementLOK.COMPONENT_NAME_SYMBOL;

/**
 * {@link ContentElement} for displaying a Social Login UI
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.login.social.ui.SocialLoginElement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Social Login"))
    }
)
@PageElementModelConfiguration(name = COMPONENT_NAME_SYMBOL, editor = SocialLoginEditor.class)
@Categorized(category = CmsCategory.UserManagement)
public class SocialLoginElement extends AbstractContentElement
{
    private static final long serialVersionUID = -5001773997078155399L;

    @Nullable
    @Override
    public Generator<? extends PageElement> getGenerator(CmsRequest<? extends PageElement> request)
    {
        return new SocialLoginGenerator();
    }
}
