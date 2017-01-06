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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.i2rd.componentfactory.form.FormData;
import com.i2rd.componentfactory.form.FormProcessUtil;
import com.i2rd.extras.model.ChoiceExtra;
import com.i2rd.extras.model.ChoiceExtraChoice;
import com.i2rd.extras.model.ChoiceExtraValue;
import com.i2rd.extras.model.ChoiceTextValue;
import com.i2rd.extras.model.Extra;
import com.i2rd.extras.model.ExtraValue;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.processes.form.FormProcess;

/**
 * Utility class for creating JSON for the responses in a {@link FormProcess}
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
public final class FormProcessJsonUtil
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(FormProcessJsonUtil.class);

    /**
     * Convert the FormProcess to JSON.
     *
     * @param formProcess the form process
     * @param context Optional context section for the JSON document.
     * @param lc the locale context
     *
     * @return json
     */
    public static JsonObject toJSON(FormProcess formProcess, JsonObject context, LocaleContext lc)
    {
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        JsonObject form = new JsonObject();
        form.addProperty("programmaticIdentifier", formProcess.getFormRevision().getFormDefinition().getProgrammaticIdentifier());
        form.addProperty("status", formProcess.getState().name());
        form.addProperty("submitTime", formProcess.getSubmitTime() == null ? null : sdf.format(formProcess.getSubmitTime()));
        form.addProperty("createTime", sdf.format(formProcess.getCreateTime()));

        JsonObject answers = new JsonObject();
        form.add("answers", answers);

        final EntityRetriever et = EntityRetriever.getInstance();
        final FormData formData = FormProcessUtil.getInstance().getFormData(formProcess, true);
        for (ExtraValue evl : formData.getExtraValueList().getExtraValues())
        {
            evl = et.narrowProxyIfPossible(evl);

            JsonObject answer = new JsonObject();

            Extra e = et.narrowProxyIfPossible(evl.getExtra());

            answers.add(e.getProgrammaticName(), answer);
            if (e.getName() != null)
                answer.addProperty("name", e.getName().getText(lc).toString());
            if (e.getShortName() != null)
                answer.addProperty("shortName", e.getShortName().getText(lc).toString());
            answer.addProperty("type", e.getClass().getSimpleName().replace("Extra", ""));
            answer.addProperty("answer", evl.getAsText(lc).toString());

            if (evl instanceof ChoiceExtraValue)
            {
                ChoiceExtraValue cev = (ChoiceExtraValue) evl;
                ChoiceExtra choiceExtra = (ChoiceExtra) e;

                if (choiceExtra.isAllowMultipleChoice())
                {
                    JsonArray programmatic = new JsonArray();
                    for (ChoiceExtraChoice choice : cev.getChoices())
                    {
                        JsonObject c = new JsonObject();
                        c.addProperty("name", choice.getName().getText(lc).toString());
                        c.addProperty("programmaticName", choice.getProgrammaticName());
                        if (!StringFactory.isEmptyString(choice.getReportValue()))
                            c.addProperty("reportValue", choice.getReportValue());
                        ChoiceTextValue choiceTextValue = cev.getChoiceTextValue(choice);
                        if (choiceTextValue != null)
                            c.addProperty("userText", choiceTextValue.getValue());
                        programmatic.add(c);
                    }
                    answer.add("programmatic", programmatic);
                }
                else
                {
                    JsonObject c = new JsonObject();
                    ChoiceExtraChoice choice = cev.getChoices().isEmpty() ? null : cev.getChoices().get(0);
                    if (choice != null)
                    {
                        c.addProperty("name", choice.getName().getText(lc).toString());
                        c.addProperty("programmaticName", choice.getProgrammaticName());
                        if (!StringFactory.isEmptyString(choice.getReportValue()))
                            c.addProperty("reportValue", choice.getReportValue());
                        ChoiceTextValue choiceTextValue = cev.getChoiceTextValue(choice);
                        if (choiceTextValue != null)
                            c.addProperty("userText", choiceTextValue.getValue());
                    }
                    answer.add("programmatic", c);
                }
            }
            else
            {
                Map<Enum<?>, Object> programmaticValueParts = evl.getProgrammaticValueParts();
                if (!programmaticValueParts.isEmpty())
                {
                    JsonArray programmatic = new JsonArray();

                    for (Map.Entry<Enum<?>, Object> entry : programmaticValueParts.entrySet())
                    {
                        JsonObject c = new JsonObject();
                        programmatic.add(c);

                        c.addProperty("part", entry.getKey().name());
                        c.addProperty("value", entry.getValue() != null ? entry.getValue().toString() : null);
                    }

                    answer.add("programmatic", programmatic);
                }
            }
        }

        JsonObject json = new JsonObject();
        json.add("form", form);
        if (context != null)
            json.add("context", context);

        if (_logger.isTraceEnabled())
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            _logger.trace(gson.toJson(json));
        }

        return json;
    }

}
