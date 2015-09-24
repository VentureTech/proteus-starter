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

package com.example.app.ui;

import com.example.app.model.UserProfile;
import com.example.app.model.UserProfileDAO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.validation.CommonValidationText;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.resource.CKEditorConfig;
import net.proteusframework.ui.miwt.util.ComponentTreeIterator;
import net.proteusframework.ui.miwt.validation.RequiredValueValidator;
import net.proteusframework.ui.miwt.validation.ValidURLValidator;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Name;

import static net.proteusframework.ui.miwt.component.Field.InputType;

/**
 * UI Editor for UserProfile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configurable(preConstruction = true)
public class UserProfileEditor extends Container

    // We are implementing the ValueEditor API which provides a
    /// consistent API for users of editors. Additionally, the API
    /// works well when used in conjunction with other ValueEditor
    /// implementations.
    implements ValueEditor<UserProfile>
{

    // This class could also be used as a viewer. It's a choice to make determined by how different
    /// the viewer and editor user interfaces (including metadata) are from each other.
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(UserProfileEditor.class);

    /** DAO instance. */
    @Autowired
    private UserProfileDAO _userProfileDAO;
    /** Profile. */
    private UserProfile _userProfile;

    /** Name Prefix. */
    private TextEditor _namePrefix;
    /** Given Name. */
    private TextEditor _nameGiven;
    /** Family Name. */
    private TextEditor _nameFamily;
    /** Name Suffix. */
    private TextEditor _nameSuffix;

    /** Address. */
    private TextEditor _addressLine1;
    /** Address. */
    private TextEditor _addressLine2;
    /** Address. */
    private TextEditor _city;
    /** Address. */
    private TextEditor _state;
    /** Address. */
    private TextEditor _postalCode;

    /** Phone. */
    private TextEditor _phoneNumber;
    /** Email. */
    private TextEditor _emailAddress;

    /** Social Link. */
    private TextEditor _twitterLink;
    /** Social Link. */
    private TextEditor _facebookLink;
    /** Social Link. */
    private TextEditor _linkedInLink;

    /** About Me. */
    private TextEditor _aboutMeProse;
    /** About Me. */
    private TextEditor _aboutMeVideoLink;
    /** Picture. */
    private PictureEditor _picture;

    /**
     * Create a new editor.
     *
     */
    public UserProfileEditor()
    {
        super();
    }

    @Override
    public void init()
    {
        // Make sure you call super.init() at the top of this method.
        /// See the Javadoc for #init() for more information about what it does.
        super.init();

        // Add HTML element type and class names for presentation use
        withHTMLElement(HTMLElement.section);

        UserProfile value = getValue();
        if (value == null) value = new UserProfile(); // Just so we don't have to check for null

        Name name = value.getName();
        // TextEditors automatically add a "prop" class name.
        _namePrefix = new TextEditor(TextSources.createText("Prefix"), name.getFormOfAddress());
        // Instead of adding a user_entry_required class name we could add the HTML5 required attribute,
        // but that requires a bit more consideration
        //// related to how the violation is handled and presented to the user.
        _nameGiven = new TextEditor(TextSources.createText("First"), name.getFirst());
        _nameGiven.addClassName("required");
        _nameFamily = new TextEditor(TextSources.createText("Last"), name.getLast());
        _nameFamily.addClassName("required");
        _nameSuffix = new TextEditor(TextSources.createText("Suffix"), name.getSuffix());
        _nameGiven.setValueValidator(new RequiredValueValidator()
            .withErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, "First Name"));
        _nameFamily.setValueValidator(new RequiredValueValidator()
            .withErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, "Last Name"));
        // You could use fieldset / legend elements. They are better for accessibility
        /// but they can be difficult to style consistently across different browsers
        /// and browser versions. If you do use a legend make sure the
        /// text content is enclosed in another element like a label or span .e.g. <legend><span>Name</span></legend>.
        /// Adding the enclosing element will help with styling.
        add(of(HTMLElement.section,
            "name",
            new Label(TextSources.createText("Name")).withHTMLElement(HTMLElement.h1),
            _namePrefix,
            _nameGiven,
            _nameFamily,
            _nameSuffix
        ));

        Address address = value.getPostalAddress();
        String line1 = null;
        String line2 = null;
        if (address.getAddressLines().length > 0)
            line1 = address.getAddressLines()[0];
        if (address.getAddressLines().length > 1)
            line2 = address.getAddressLines()[1];
        _addressLine1 = new TextEditor(TextSources.createText("Address Line 1"), line1);
        _addressLine2 = new TextEditor(TextSources.createText("Address Line 2"), line2);
        _city = new TextEditor(TextSources.createText("City"), address.getCity());
        _state = new TextEditor(TextSources.createText("State"), address.getState());
        _postalCode = new TextEditor(TextSources.createText("Postal Code"), address.getPostalCode());
        _emailAddress = new TextEditor(TextSources.createText("Email"), value.getEmailAddress());
        // Non default InputTypes can provide alternate keyboard layouts or UIs on devices that
        // support it like phones and tablets. This can make data entry much simpler
        // so consider this depending on the target audience.
        // Additionally, you can hook into the HTML5 constraint validation API if you choose.
        _emailAddress.setInputType(InputType.email);
        _phoneNumber = new TextEditor(TextSources.createText("Phone"), value.getPhoneNumber());
        _phoneNumber.setInputType(InputType.tel);
        add(of(HTMLElement.section,
            "contact",
            new Label(TextSources.createText("Contact Information")).withHTMLElement(HTMLElement.h1),
            of(HTMLElement.div,
                "prop_group address",
                _addressLine1.addClassName("address-line"),
                _addressLine2.addClassName("address-line"),
                _city.addClassName("city"),
                _state.addClassName("state"),
                _postalCode.addClassName("postal-code")
            ),
            _emailAddress.addClassName("email"),
            _phoneNumber.addClassName("phone")
        ));

        add(of(HTMLElement.section,
            "social",
            new Label(TextSources.createText("Social Links")).withHTMLElement(HTMLElement.h1),
            _twitterLink = (TextEditor) _createURLEditor("Twitter Link", value.getTwitterLink()).addClassName("twitter"),
            _facebookLink = (TextEditor) _createURLEditor("Facebook Link", value.getFacebookLink()).addClassName("facebook"),
            _linkedInLink = (TextEditor) _createURLEditor("LinkedIn Link", value.getLinkedInLink()).addClassName("linkedin")
        ));
        _picture = new PictureEditor();
        _picture.setPreserveFileEntity(true);
        _picture.setValue(value.getPicture());
        _picture.setLabel(TextSources.createText("Picture"));
        _picture.addClassName("picture");

        _aboutMeProse = new TextEditor(TextSources.createText("Professional Information, Hobbies, Interests..."),
            value.getAboutMeProse());
        _aboutMeProse.setDisplayHeight(15);
        _aboutMeProse.setDisplayWidth(45);
        _aboutMeProse.setTextEditorConfig(CKEditorConfig.standard);
        add(of(HTMLElement.section,
            "about_me",
            new Label(TextSources.createText("About Me")).withHTMLElement(HTMLElement.h1),
            _picture,
            _aboutMeProse.addClassName("prose"),
            _aboutMeVideoLink = (TextEditor) _createURLEditor("Video Link", value.getAboutMeVideoLink()).addClassName("video")
        ));
    }

    @Override
    public boolean isEditable()
    {
        return _namePrefix.isEditable();
    }

    @Override
    public void setEditable(final boolean b)
    {
        _forEach(value -> value.setEditable(b));
    }

    /**
     * {@inheritDoc}
     * Must be an attached or initialized value.
     * The value will be evicted from the session.
     */
    @Override
    public void setValue(UserProfile value)
    {
        _userProfile = value;
        if (value != null)
        {
            if (_userProfileDAO.isAttached(value))
            {
                Hibernate.initialize(value);
                Hibernate.initialize(value.getName());
                Hibernate.initialize(value.getPostalAddress());
                _userProfileDAO.evict(value);
            }
        }
        if (isInited())
        {
            _updateUI(value);
        }
    }

    @Override
    public UserProfile commitValue() throws MIWTException
    {
        UserProfile profile = getValue();
        if(profile == null)
            profile = new UserProfile();
        _updateUserProfileFromUI(profile);
        return profile;
    }

    @Override
    public UserProfile getUIValue(Level logErrorLevel)
    {
        final UserProfile toCopy = getValue();
        UserProfile profile = toCopy == null ? new UserProfile() : new UserProfile(toCopy);
        _updateUserProfileFromUI(profile);
        return profile;
    }

    @Override
    public ModificationState getModificationState()
    {
        if (!isInited())
            return ModificationState.UNCHANGED;
        final AtomicReference<ModificationState> state = new AtomicReference<>(ModificationState.UNCHANGED);
        _forEach(value -> {
            if (!state.get().isModified()
                && value.getModificationState().isModified())
                state.set(ModificationState.CHANGED);
        });
        return state.get();
    }

    @Override
    public boolean validateUIValue(final Notifiable notifiable)
    {
        final AtomicBoolean valid = new AtomicBoolean(true);
        _forEach(value -> valid.set(value.validateUIValue(notifiable) && valid.get()));
        return valid.get();
    }

    @Override
    public UserProfile getValue()
    {
        // Do not reattach - that would discard any changes since the last call to commit if they have not been persisted.
        return _userProfile;
    }

    /**
     * Iterate over value editors.
     *
     * @param consumer the consumer.
     */
    private void _forEach(Consumer<ValueEditor<?>> consumer)
    {
        ComponentTreeIterator cti = new ComponentTreeIterator(this, false, false, false);

        while (cti.hasNext())
        {
            Component c = cti.next();
            if (c == this) continue;
            if (c instanceof ValueEditor<?>)
                consumer.accept(ValueEditor.class.cast(c));
        }
    }

    /**
     * Update UserProfile from UI.
     *
     * @param profile the profile to update.
     */
    private void _updateUserProfileFromUI(UserProfile profile)
    {
        profile.getName().setFormOfAddress(_namePrefix.commitValue());
        profile.getName().setFirst(_nameGiven.commitValue());
        profile.getName().setLast(_nameFamily.commitValue());
        profile.getName().setSuffix(_nameSuffix.commitValue());

        List<String> addressLines = new ArrayList<>(2);
        String line1 = _addressLine1.commitValue();
        String line2 = _addressLine2.commitValue();
        if (line1 != null) addressLines.add(line1);
        else if (line2 != null) addressLines.add("");
        if (line2 != null) addressLines.add(line2);
        profile.getPostalAddress().setAddressLines(addressLines.toArray(new String[addressLines.size()]));
        profile.getPostalAddress().setCity(_city.commitValue());
        profile.getPostalAddress().setState(_state.commitValue());
        profile.getPostalAddress().setPostalCode(_postalCode.commitValue());
        profile.setEmailAddress(_emailAddress.commitValue());
        profile.setPhoneNumber(_phoneNumber.commitValue());
        profile.setTwitterLink(_createURL(_twitterLink.commitValue()));
        profile.setFacebookLink(_createURL(_facebookLink.commitValue()));
        profile.setLinkedInLink(_createURL(_linkedInLink.commitValue()));
        profile.setAboutMeProse(_aboutMeProse.commitValue());
        profile.setAboutMeVideoLink(_createURL(_aboutMeVideoLink.commitValue()));
        // NOTE : if we ever allowed someone to clear/remove their profile picture, then we'd need to delete the file.
        profile.setPicture(_picture.commitValue());

    }

    /**
     * Update the UI.
     *
     * @param value the value.
     */
    private void _updateUI(@Nullable UserProfile value)
    {
        if (value == null)
        {
            // There's no default state for editors in this case, so we set all to null
            _forEach(editor -> editor.setValue(null));
        }
        else
        {
            Name name = value.getName();
            _namePrefix.setValue(name.getFormOfAddress());
            _nameGiven.setValue(name.getFirst());
            _nameFamily.setValue(name.getLast());
            _nameSuffix.setValue(name.getSuffix());
            Address address = value.getPostalAddress();
            String line1 = null;
            String line2 = null;
            if (address.getAddressLines().length > 0)
                line1 = address.getAddressLines()[0];
            if (address.getAddressLines().length > 1)
                line2 = address.getAddressLines()[1];
            _addressLine1.setValue(line1);
            _addressLine2.setValue(line2);
            _city.setValue(address.getCity());
            _state.setValue(address.getState());
            _postalCode.setValue(address.getPostalCode());
            _twitterLink.setValue(_userProfileDAO.toString(value.getTwitterLink()));
            _facebookLink.setValue(_userProfileDAO.toString(value.getFacebookLink()));
            _linkedInLink.setValue(_userProfileDAO.toString(value.getLinkedInLink()));
            _aboutMeProse.setValue(value.getAboutMeProse());
            _aboutMeVideoLink.setValue(_userProfileDAO.toString(value.getAboutMeVideoLink()));
            _picture.setValue(value.getPicture());
        }
    }

    /**
     * Create a URL editor.
     *
     * @param label the label.
     * @param value the value.
     * @return the editor.
     */
    private TextEditor _createURLEditor(final String label, final URL value)
    {
        TextEditor editor = new TextEditor(TextSources.createText(label), _userProfileDAO.toString(value));
        editor.setInputType(InputType.url);
        editor.setValueValidator(new ValidURLValidator()
            .withErrorMessage(CommonValidationText.ARG0_IS_NOT_A_VALID_ARG1, label, "URL"));
        // If you wanted to make the field required, you could call "setValueRequired(true)" on the validator.
        return editor;
    }

    /**
     * Create a URL from a value returned by an editor created in {@link #_createURLEditor(String, URL)}.
     *
     * @param value the value.
     * @return the URL or null.
     */
    @Nullable
    private static URL _createURL(@Nullable final String value)
    {
        if (StringFactory.isEmptyString(value)) return null;
        try
        {
            return new URL(value);
        }
        catch (MalformedURLException e)
        {
            _logger.error("Unable to construct URL: " + value, e);
        }
        return null;
    }
}
