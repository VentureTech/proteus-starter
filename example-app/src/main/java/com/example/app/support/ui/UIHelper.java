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

package com.example.app.support.ui;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;

import static net.proteusframework.ui.miwt.component.Container.of;

/**
 * Ui Helper
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/29/16
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UIHelper
{
    /**
     * Create input instructions label.
     *
     * @param instructionText the instruction text
     *
     * @return the label
     */
    @Nonnull
    public Container createInputInstructions(@Nullable TextSource instructionText)
    {
        return createInputInstructions(instructionText, null);
    }

    /**
     * Create input instructions label.
     *
     * @param instructionText the instruction text
     * @param exampleText the example text
     *
     * @return the label
     */
    @Nonnull
    public Container createInputInstructions(@Nullable TextSource instructionText, @Nullable TextSource exampleText)
    {
        Container wrapper = of("instructions-wrapper");
        if(instructionText != null)
        {
            Label instructions = new Label(instructionText)
                .withHTMLElement(HTMLElement.p);
            instructions.addClassName("input-instructions").addClassName(CSSUtil.CSS_INSTRUCTIONS);
            wrapper.add(instructions);
        }
        if(exampleText != null)
        {
            Label example = new Label(exampleText)
                .withHTMLElement(HTMLElement.p);
            example.addClassName("input-example").addClassName(CSSUtil.CSS_INSTRUCTIONS);
            wrapper.add(example);
        }
        return wrapper;
    }
}
