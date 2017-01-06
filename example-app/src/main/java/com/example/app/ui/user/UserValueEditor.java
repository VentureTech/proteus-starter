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

package com.example.app.ui.user;


import com.example.app.config.UserConfig;
import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.membership.Membership;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.profile.model.membership.MembershipTypeProvider;
import com.example.app.profile.model.user.ContactMethod;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.support.AppUtil;
import com.example.app.support.ContactUtil;
import com.example.app.ui.UIPreferences;
import com.example.app.ui.vtcrop.VTCropPictureEditor;
import com.example.app.ui.vtcrop.VTCropPictureEditorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.data.filesystem.http.FileEntityFileItem;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.ListComponent;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.AbstractSimpleValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CheckboxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ListComponentValueEditor;
import net.proteusframework.ui.miwt.data.ListSelectionMode;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.validation.CompositeValidator;
import net.proteusframework.ui.miwt.validation.PassAllValidator;
import net.proteusframework.ui.miwt.validation.RequiredValueValidator;
import net.proteusframework.ui.miwt.validation.Validator;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.Principal;

import static com.example.app.support.AppUtil.nullFirst;
import static com.example.app.ui.user.UserValueViewerLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * {@link CompositeValueEditor} for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserValueEditor",
    i18n = {
        @I18N(symbol = "Label Profile Role FMT", l10n = @L10N("{0} Role")),
    }
)
@Configurable
public class UserValueEditor extends CompositeValueEditor<User>
{
    private final List<AuthenticationDomain> _authDomains = new ArrayList<>();
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private AppUtil _appUtil;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    @Qualifier(UserConfig.PICTURE_EDITOR_CONFIG)
    private VTCropPictureEditorConfig _pictureEditorConfig;
    @Autowired
    private SelectedCompanyTermProvider _terms;
    @Autowired
    private MembershipTypeProvider _mtp;
    @Autowired
    private UIPreferences _uiPreferences;
    private boolean _adminMode = true;
    private VTCropPictureEditor _userPictureEditor;
    private ListComponentValueEditor<MembershipType> _profileEntityMembershipTypeSelector;
    private CheckboxValueEditor<LocalizedObjectKey> _contactMethodSelector;
    private PrincipalValueEditor _principalValueEditor;
    private ComboBoxValueEditor<Link> _loginLandingPage;
    private List<Link> _links;

    /**
     * Instantiate an new instance of UserValueEditor
     */
    public UserValueEditor()
    {
        super(User.class);
    }

    /**
     * Get an Optional possibly containing the Commit Value of the Profile Role Selector, if one was selected.
     *
     * @return an Optional containing the Commit Value of the Profile Role Selector
     */
    @Nonnull
    public List<MembershipType> commitValueCoachingMemType()
    {
        return Optional.ofNullable(_profileEntityMembershipTypeSelector.commitValue()).orElse(Collections.emptyList());
    }

    /**
     * Get the login landing page link.
     *
     * @return the link
     */
    @Nullable
    public Link commitValueLoginLandingPage()
    {
        return _loginLandingPage.commitValue();
    }

    /**
     * Get the excluded contact methods from the UI for the User
     *
     * @return a list of excluded contact methods
     */
    @Nullable
    public ContactMethod commitValuePreferredContactMethod()
    {
        return _contactMethodSelector.commitValue().stream().findFirst().isPresent() ? ContactMethod.PhoneSms : null;
    }

    /**
     * Get the User Image editor.
     *
     * @return the User Image editor
     */
    @Nonnull
    public VTCropPictureEditor getPictureEditor()
    {
        return Optional.ofNullable(_userPictureEditor).orElseThrow(() -> new IllegalStateException(
            "PictureEditor was null.  Do not call getPictureEditor before initialization!"));
    }    /**
     * Get boolean flag.  If true, this viewer is being displayed in admin mode
     * <br>
     * defaults to true.
     *
     * @return boolean flag
     */
    public boolean isAdminMode()
    {
        return _adminMode;
    }

    /**
     * Set boolean flag.  If true, this viewer is being displayed in admin mode
     *
     * @param adminMode boolean flag
     */
    public void setAdminMode(boolean adminMode)
    {
        _adminMode = adminMode;
    }

    /**
     * Get the list of AuthenticationDomains to check for username uniqueness
     *
     * @return a list of AuthenticationDomains
     */
    @Nonnull
    public List<AuthenticationDomain> getAuthDomains()
    {
        return _authDomains;
    }

    /**
     * Set the list of AuthenticationDomains to check for username uniqueness
     *
     * @param authDomains a list of AuthenticationDomains
     */
    public void setAuthDomains(@Nonnull List<AuthenticationDomain> authDomains)
    {
        _authDomains.clear();
        _authDomains.addAll(authDomains);
        if (isInited())
        {
            _principalValueEditor.setAuthDomains(_authDomains);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init()
    {
        User currentUser = _userDAO.getAssertedCurrentUser();

        _userPictureEditor = new VTCropPictureEditor(_pictureEditorConfig);
        _userPictureEditor.addClassName("user-picture");
        _userPictureEditor.setDefaultResource(_appUtil.getDefaultUserImage());
        _userPictureEditor.setValue(
            Optional.ofNullable(getValue())
                .map(User::getImage)
                .map(FileEntityFileItem::new)
                .orElse(null));

        TimeZone timeZone = getSession().getTimeZone();

        List<MembershipType> initialMemTypes = new ArrayList<>(_profileDAO.getMembershipTypesForProfile(
            _uiPreferences.getSelectedCompany()));
        _profileEntityMembershipTypeSelector = new ListComponentValueEditor<>(
            createText(UserValueEditorLOK.LABEL_PROFILE_ROLE_FMT(_terms.company())),
            initialMemTypes,
            null);
        ((ListComponent) _profileEntityMembershipTypeSelector.getValueComponent())
            .getSelectionModel().setSelectionMode(ListSelectionMode.MULTIPLE_INTERVAL);
        _profileEntityMembershipTypeSelector.getValueComponent().setAttribute("data-placeholder", "None");
        _profileEntityMembershipTypeSelector.setEditable(true);
        _profileEntityMembershipTypeSelector.setVisible(isAdminMode() && (getValue() == null || _userDAO.isTransient(getValue())));

        _contactMethodSelector = new CheckboxValueEditor<>(
            LABEL_CONTACT_PREFERENCES(), Collections.singleton(LABEL_SEND_NOTIFICATION_TO_PHONESMS()), null);
        _contactMethodSelector.setVisible(
            Objects.equals(currentUser.getId(), Optional.ofNullable(getValue()).map(User::getId).orElse(0)));
        AppUtil.walkComponentTree(_contactMethodSelector.getValueComponent(), (comp) -> {
            if (comp instanceof Checkbox)
            {
                Checkbox cb = (Checkbox) comp;
                cb.addActionListener(ev -> {
                    if (cb.isSelected())
                    {
                        _principalValueEditor.getSmsEditor().setRequiredValueValidator();
                    }
                    else
                    {
                        removeRequiredValueValidator(_principalValueEditor.getSmsEditor());
                    }
                });
            }
        });

        // Login Landing Page
        _links = nullFirst(LoginLandingLinks.getAvailableLinks(currentUser, getLocaleContext()));
        _loginLandingPage = new ComboBoxValueEditor<>(
            LABEL_LOGIN_LANDING_PAGE(),
            _links,
            null);
        //This field is available when the current user is the user being edited
        // AND only to the user who has a particular membership under the Profile.
        _loginLandingPage.setVisible(
            Objects.equals(currentUser.getId(), Optional.ofNullable(getValue()).map(User::getId).orElse(0))
            && _profileDAO.getMembershipsForUser(getValue(), null, timeZone).stream()
                .map(Membership::getMembershipType).filter(membershipType1 -> membershipType1 != null)
                .anyMatch(membershipType ->
                    membershipType.equals(_mtp.companyAdmin()))

        );
        _loginLandingPage.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT, input -> {
            Link link = (Link) input;
            return createText(link.getAdditionalAttributes().get("label"));
        }));

        super.init();

        add(_userPictureEditor);

        _principalValueEditor = new PrincipalValueEditor();
        _principalValueEditor.setAdminMode(isAdminMode());
        _principalValueEditor.setAuthDomains(getAuthDomains());
        addEditorForProperty(() -> _principalValueEditor,
            readProp -> {
                Principal p = _er.reattachIfNecessary(readProp.getPrincipal());
                //noinspection ConstantConditions
                if (p == null) return p;
                if (p.getContact() == null)
                    p.setContact(new Contact());
                if (p.getContact().getPhoneNumbers().isEmpty())
                    p.getContact().getPhoneNumbers().add(new PhoneNumber());
                if (readProp.getSmsPhone() != null)
                    p.getContact().getPhoneNumbers().add(readProp.getSmsPhone());
                return p;
            },
            (writeProp, val) -> {
                if (val == null)
                {
                    //noinspection ConstantConditions
                    writeProp.setPrincipal(null);
                    writeProp.setSmsPhone(null);
                }
                else
                {
                    writeProp.setPrincipal(val);
                    if (val.getContact() != null)
                    {
                        Optional<PhoneNumber> phoneNumber =
                            ContactUtil.getPhoneNumber(val.getContact(), ContactDataCategory.PERSONAL);
                        if (phoneNumber.isPresent())
                        {
                            val.getContact().getPhoneNumbers().remove(phoneNumber.get());
                            writeProp.setSmsPhone(phoneNumber.get());
                            return;
                        }
                    }
                    writeProp.setSmsPhone(null);
                }
            });

        add(_profileEntityMembershipTypeSelector);
        add(_contactMethodSelector);
        add(_loginLandingPage);

        setValue(getValue());
        setEditable(isEditable());
    }

    private <V> void removeRequiredValueValidator(AbstractSimpleValueEditor<V> editor)
    {
        Validator validator = editor.getValueComponent().getValidator();
        if (validator instanceof CompositeValidator)
        {
            CompositeValidator cVl = (CompositeValidator) validator;
            List<Validator> vals = Arrays.stream(cVl.getValidators())
                .filter(v -> !(v instanceof RequiredValueValidator))
                .collect(Collectors.toList());
            cVl.setValidators(vals.toArray(new Validator[vals.size()]));
            editor.getValueComponent().setValidator(cVl);
            editor.removeClassName(CSSUtil.CSS_REQUIRED_FIELD);
        }
        else if (validator instanceof RequiredValueValidator)
        {
            editor.getValueComponent().setValidator(new PassAllValidator());
            editor.removeClassName(CSSUtil.CSS_REQUIRED_FIELD);
        }
    }



    @Override
    public void setValue(@Nullable User value)
    {
        super.setValue(value);

        if (isInited())
        {
            User currentUser = _userDAO.getAssertedCurrentUser();

            _userPictureEditor.setValue(
                Optional.ofNullable(value)
                    .map(User::getImage)
                    .map(FileEntityFileItem::new)
                    .orElse(null));

            if (value != null && value.getPreferredContactMethod() == ContactMethod.PhoneSms)
                _contactMethodSelector.setValue(Collections.singleton(LABEL_SEND_NOTIFICATION_TO_PHONESMS()));

            final Preferences userPref = Preferences.userRoot().node(User.LOGIN_PREF_NODE);
            final String uri = userPref != null ? userPref.get(User.LOGIN_PREF_NODE_LANDING_PAGE, null) : null;
            for (Link l : _links)
            {
                if (l != null && l.getURIAsString().equals(uri))
                {
                    _loginLandingPage.setValue(l);
                    break;
                }
            }
            _contactMethodSelector.setVisible(
                Objects.equals(currentUser.getId(), Optional.ofNullable(getValue()).map(User::getId).orElse(0)));
            //This field is available when the current user is the user being edited
            // AND only to the user who has a particular membership under the Profile.
            _loginLandingPage.setVisible(
                Objects.equals(currentUser.getId(), Optional.ofNullable(getValue()).map(User::getId).orElse(0))
                && _profileDAO.getMembershipsForUser(getValue(), null, getSession().getTimeZone()).stream()
                    .map(Membership::getMembershipType)
                    .anyMatch(membershipType ->
                        membershipType.equals(_mtp.companyAdmin()))
            );
        }
    }

    @Override
    public void setEditable(boolean b)
    {
        super.setEditable(b);

        if (isInited())
        {
            _userPictureEditor.setEditable(b);
            _profileEntityMembershipTypeSelector.setEditable(b);
            _contactMethodSelector.setEditable(b);
            _loginLandingPage.setEditable(b);
        }
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        boolean valid = super.validateUIValue(notifiable);
        valid = _userPictureEditor.validateUIValue(notifiable) && valid;
        valid = _profileEntityMembershipTypeSelector.validateUIValue(notifiable) && valid;
        valid = _loginLandingPage.validateUIValue(notifiable) && valid;
        return _contactMethodSelector.validateUIValue(notifiable) && valid;
    }

    @Override
    public ModificationState getModificationState()
    {
        return AppUtil.getModificationStateForComponent(this);
    }

}
