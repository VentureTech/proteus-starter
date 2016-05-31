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

/**
 * Contains classes and file associated with the UI of this application
 */
@I18NFiles({
    @I18NFile(
        file = "UIText",
        classVisibility = I18NFile.Visibility.PUBLIC,
        symbolPrefix = "UIText",
        i18n = {
            @I18N(symbol = "ARG0 Or ARG1", l10n = @L10N("{0} Or {1}")),
            @I18N(symbol = "Label Category", l10n = @L10N("Category")),
            @I18N(symbol = "Label Schedule", l10n = @L10N("Schedule")),
            @I18N(symbol = "Label Recurring Schedule", l10n = @L10N("Repeating Schedule")),
            @I18N(symbol = "Value", l10n = @L10N("Value")),

            @I18N(symbol = "Started", l10n = @L10N("Started")),
            @I18N(symbol = "Start Date", l10n = @L10N("Start Date")),
            @I18N(symbol = "End Date", l10n = @L10N("End Date")),
            @I18N(symbol = "Closed Date", l10n = @L10N("Closed Date")),
            @I18N(symbol = "Assigned To", l10n = @L10N("Assigned To")),
            @I18N(symbol = "Myself", l10n = @L10N("Myself")),
            @I18N(symbol = "Anybody", l10n = @L10N("Anybody")),

            @I18N(symbol = "Added", l10n = @L10N("Added")),
            @I18N(symbol = "Active", l10n = @L10N("Active")),
            @I18N(symbol = "Inactive", l10n = @L10N("Inactive")),
            @I18N(symbol = "Add New FMT", l10n = @L10N("Add New {0}")),
            @I18N(symbol = "Add FMT", l10n = @L10N("Add {0}")),
            @I18N(symbol = "Archive", l10n = @L10N("Archive")),
            @I18N(symbol = "Complete", l10n = @L10N("Complete")),
            @I18N(symbol = "Deleted", l10n = @L10N("Deleted")),
            @I18N(symbol = "Date", l10n = @L10N("Date")),
            @I18N(symbol = "Done", l10n = @L10N("Done")),
            @I18N(symbol = "Edit FMT", l10n = @L10N("Edit {0}")),
            @I18N(symbol = "Set FMT", l10n = @L10N("Set {0}")),
            @I18N(symbol = "End", l10n = @L10N("End")),
            @I18N(symbol = "Error Unable to Determine FMT", l10n = @L10N("Unable to determine {0}.")),
            @I18N(symbol = "Error Message Insufficient Permissions FMT",
                l10n = @L10N("You do not have the correct roles to view this {0}")),
            @I18N(symbol = "Info", l10n = @L10N("Info")),
            @I18N(symbol = "Last Updated", l10n = @L10N("Last Updated")),
            @I18N(symbol = "Location", l10n = @L10N("Location")),
            @I18N(symbol = "Never", l10n = @L10N("Never")),
            @I18N(symbol = "Positions", l10n = @L10N("Positions")),
            @I18N(symbol = "Redo", l10n = @L10N("Redo")),
            @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} Search")),
            @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} Search")),
            @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0}")),
            @I18N(symbol = "Select FMT", l10n = @L10N("Select {0}")),
            @I18N(symbol = "Start", l10n = @L10N("Start")),
            @I18N(symbol = "Time", l10n = @L10N("Time")),
            @I18N(symbol = "End Time", l10n = @L10N("End Time")),
            @I18N(symbol = "Update", l10n = @L10N("Update")),
            @I18N(symbol = "No Emails Were Sent", l10n = @L10N("No Emails Were Sent")),
            @I18N(symbol = "Email With Subject Sent TO",
                l10n = @L10N("Email With Subject, \"{0}\", Sent To {1}")),
            @I18N(symbol = "Label Email Notifications", l10n = @L10N("Email Notifications")),
            @I18N(symbol = "Title Send Email", l10n = @L10N("Send Email")),
            @I18N(symbol = "Action Send Email", l10n = @L10N("Send Email")),
            @I18N(symbol = "Hyphen Symbol", l10n = @L10N("-")),
            @I18N(symbol = "Action Execute", l10n = @L10N("Execute")),
            @I18N(symbol = "Action Take Action", l10n = @L10N("Take Action")),
            @I18N(symbol = "Action Manage", l10n = @L10N("Manage")),
            @I18N(symbol = "Action Deactivate", l10n = @L10N("Deactivate")),
            @I18N(symbol = "Instructions Draft Mode By Default FMT",
                l10n = @L10N("The new {0} will be in Draft mode after it is created.")),
            @I18N(symbol = "Organizer", l10n = @L10N("Organizer")),
            @I18N(symbol = "Attendees", l10n = @L10N("Attendees")),
            @I18N(symbol = "Error Must Be A Whole Number {0}", l10n = @L10N("{0} must be a whole number.")),
        }
    )
})
package com.example.app.ui;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.I18NFiles;
import net.proteusframework.core.locale.annotation.L10N;