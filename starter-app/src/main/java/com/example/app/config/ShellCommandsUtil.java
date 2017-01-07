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

package com.example.app.config;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.spring.CoreShellComponent;
import net.proteusframework.users.model.ContactDataCategory;

import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * Shell commands util class.  Used as a utility for shell commands
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class ShellCommandsUtil
{
    @Autowired
    private CoreShellComponent _shell;
    @Autowired
    private JDBCLocaleSource _jdbcLocaleSource;

    private java.util.logging.Logger _shellLogger;

    /**
     * Convert the given category string to a ContactDataCategory
     *
     * @param category the category to convert
     * @param defaultCategory the default ContactDataCategory in case the given category is not recognized
     *
     * @return a ContactDataCategory
     */
    public ContactDataCategory convertCategory(String category, ContactDataCategory defaultCategory)
    {
        switch (category)
        {
            case "business":
            case "BUSINESS":
                return ContactDataCategory.BUSINESS;
            case "personal":
            case "PERSONAL":
                return ContactDataCategory.PERSONAL;
            case "unknown":
            case "UNKNOWN":
                return ContactDataCategory.UNKNOWN;
            default:
                return defaultCategory;
        }
    }

    /**
     * Create a Localized Object Key from the given value
     *
     * @param value the value to create an LoK from
     *
     * @return an LoK
     *
     * @throws LocaleSourceException if creating the LoK failed
     */
    public LocalizedObjectKey createLoK(String value) throws LocaleSourceException
    {
        TransientLocalizedObjectKey valueKey = new TransientLocalizedObjectKey(new HashMap<>());
        valueKey.addLocalization(Locale.ENGLISH, value);

        return valueKey.updateOrStore(_jdbcLocaleSource, null);
    }

    /**
     * Get the category argument from the user (if needed).
     *
     * @param category the current category.  If this is not null or empty, it is simple returned
     * @param additionalAskArg the arg to the question, such as "for this Address" to create a question of: "What is the
     * category for this Address?"
     *
     * @return a user-supplied category, or the current category
     *
     * @throws IOException if the shell screws up
     */
    public String getCategory(String category, String additionalAskArg) throws IOException
    {
        if (isEmptyString(category))
        {
            category = getInteractiveArg(String.format("What is the category %s?", additionalAskArg),
                result -> {
                    switch (result)
                    {
                        case "business":
                        case "BUSINESS":
                        case "personal":
                        case "PERSONAL":
                        case "unknown":
                        case "UNKNOWN":
                            return true;
                        default:
                            return false;
                    }
                });
        }
        return category;
    }

    /**
     * Get a user-provided response of true or false to a question
     *
     * @param ask the question to ask, additional response options will be appended at the end
     *
     * @return boolean true or false
     *
     * @throws IOException if the shell screws up
     */
    public Boolean getConfirmation(String ask) throws IOException
    {
        String confirmS = getInteractiveArg(ask + "(y/n)", response -> {
            if (response != null)
            {
                switch (response.toLowerCase())
                {
                    case "y":
                    case "yes":
                    case "n":
                    case "no":
                        return true;
                    default:
                        return false;
                }
            }
            else return false;
        });
        Boolean confirm;
        switch (confirmS.toLowerCase())
        {
            case "y":
            case "yes":
                confirm = true;
                break;
            case "n":
            case "no":
                confirm = false;
                break;
            default:
                confirm = false;
        }
        return confirm;
    }

    /**
     * Get the description argument from the user (if needed).
     *
     * @param description the current description.  If this is not null or empty, it is simply returned
     * @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
     * description for this ProfileType?"
     *
     * @return a user-supplied description, or the current description
     *
     * @throws IOException if the shell screws up
     */
    public String getDescription(String description, String additionalAskArg) throws IOException
    {
        if (isEmptyString(description))
        {
            description = getInteractiveArg(String.format("What is the description %s?", additionalAskArg), null);
        }
        return description;
    }

    /**
     * Get the name argument from the user (if needed).
     *
     * @param name the current name.  If this is not null or empty, it is simply returned
     * @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
     * name for this ProfileType?"
     *
     * @return a user-supplied name, or the current name
     *
     * @throws IOException if the shell screws up
     */
    public String getName(String name, String additionalAskArg) throws IOException
    {
        if (isEmptyString(name))
        {
            name = getInteractiveArg(String.format("What is the name %s?", additionalAskArg),
                result -> !isEmptyString(result));
        }
        return name;
    }

    /**
     * Get the programmatic identifier argument from the user (if needed).
     *
     * @param programmaticIdentifier the current programmatic identifier.  if this is not null or empty, it is simply returned
     * @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
     * programmatic identifier for this ProfileType?"
     *
     * @return a user-supplied, or the current programmatic identifier
     *
     * @throws IOException if the shell screws up
     */
    public String getProgrammaticIdentifier(String programmaticIdentifier, String additionalAskArg) throws IOException
    {
        if (isEmptyString(programmaticIdentifier))
        {
            programmaticIdentifier = getInteractiveArg(
                String.format("What is the programmatic identifier %s?", additionalAskArg),
                result -> !isEmptyString(result));
        }
        return programmaticIdentifier;
    }

    /**
     * Get an interactive shell argument.
     *
     * @param ask the prompt for the user to supply the argument
     * @param checker a function for checking if the value supplied by the user is valid
     *
     * @return the user supplied value
     *
     * @throws IOException if the shell screws up
     */
    public String getInteractiveArg(String ask, @Nullable Function<String, Boolean> checker)
        throws IOException
    {
        boolean valid = false;
        String result = "";
        while (!valid)
        {
            _shell.printNewline();
            result = _shell.readLine(ask);
            if (checker != null)
            {
                valid = checker.apply(result);
            }
            else
            {
                valid = true;
            }
        }
        return result;
    }

    /**
     * Get the underlying shell
     *
     * @return the shell
     */
    public CoreShellComponent getShell()
    {
        return _shell;
    }

    /**
     * Print a line out to the logger/console
     *
     * @param line the line to print
     */
    public void printLine(String line)
    {
        _shellLogger.info(line);
    }

    /**
     * Set the shell logger for the Shell Commands Util
     *
     * @param logger the logger
     */
    public void setShellLogger(java.util.logging.Logger logger)
    {
        _shellLogger = logger;
    }
}
