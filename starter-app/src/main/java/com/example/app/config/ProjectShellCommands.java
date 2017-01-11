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

import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.profile.service.ProfileTypeKindLabelProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.i2rd.cms.util.AbstractShellCommands;

import net.proteusframework.cms.label.Label;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.AuthenticationDomainDAO;
import net.proteusframework.users.model.dao.AuthenticationDomainList;

import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.users.model.dao.AuthenticationDomainList.createDomainList;
import static net.proteusframework.users.model.dao.AuthenticationDomainList.emptyDomainList;

/**
 * Shell commands for project
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/23/15 9:54 AM
 */
public class ProjectShellCommands extends AbstractShellCommands
{

    @Autowired
    private JDBCLocaleSource _jdbcLocaleSource;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private CompanyDAO _companyDAO;
    @Autowired
    private ShellCommandsUtil _shellCommandsUtil;
    @Autowired
    private ProfileTypeKindLabelProvider _profileTypeKindLabelProvider;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private AuthenticationDomainDAO _domainDAO;
    @Autowired
    private MembershipOperationProvider _mop;

    /**
     * Create a User for the given Principal username
     *
     * @param username the username to create the User for
     * @param authDomainName the authentication domain to search for the Principal in
     * @param profileId the profile programmatic identifier.
     *
     * @return the created or retrieved User, or null if a Principal could not be found
     */
    @Nullable
    @CliCommand(value = "create user for principal", help = "Create a User for the given Principal username")
    public User createUserForPrincipal(
        @CliOption(key = "username",
            help = "The username of the Principal to create the User for", mandatory = true) String username,
        @CliOption(key = "authdomain",
            help = "The authentication domain name to search in", mandatory = false) String authDomainName,
        @CliOption(key = "profile", help = "The profile entity programmatic identifier", mandatory = true) String profileId)
    {
        Preconditions.checkNotNull(username, "Username was null!");
        AuthenticationDomainList authDomain = emptyDomainList();
        if (!isEmptyString(authDomainName))
        {
            authDomain = createDomainList(_domainDAO.getAuthenticationDomain(authDomainName));
        }
        Company profile = _profileDAO.getProfile(Company.class, Integer.valueOf(profileId));
        if (profile == null)
            throw new IllegalArgumentException("Given programmatic identifier for Profile was not valid");

        Principal principal = principalDAO.getPrincipalByLogin(username, authDomain);
        if (principal != null)
        {

            User user = _userDAO.getUserForPrincipal(principal);
            if (user == null)
            {
                user = new User();
                user.setPrincipal(principal);
                user = _userDAO.mergeUser(user);
                _companyDAO.addUserToCompany(profile, user);
                _shellCommandsUtil.printLine("User saved with ID: " + user.getId());
            }
            else
            {
                _companyDAO.addUserToCompany(profile, user);
                _shellCommandsUtil.printLine("User saved with ID: " + user.getId());
            }
            return user;
        }
        else
            _shellCommandsUtil.printLine("Retrieved principal was null!");
        return null;
    }

    /**
     * Add a ProfileType
     *
     * @param profileTypeData the profile type data, in json
     * <pre>
     *    {
     *      "programmaticIdentifier": "testProfileType",
     *      "name": "testProfileType",
     *      "description": "a test profile type",
     *      "kind": "Company"
     *    }
     *   </pre>
     *
     * @throws LocaleSourceException if creation of name or description localized object keys fails
     * @throws IOException if the shell screws up
     */
    @CliCommand(value = "create or update profiletype",
        help = "Create or modify a ProfileType by providing required data via json string.")
    public void addOrModifyProfileType(
        @CliOption(key = "data", help = "The profile type data, in json.  This can be left blank to use the interactive shell")
            String profileTypeData) throws LocaleSourceException, IOException
    {
        Gson gson = new GsonBuilder().serializeNulls().create();

        ProfileType profileType = !isEmptyString(profileTypeData)
            ? gson.fromJson(profileTypeData, ProfileTypeData.class)
            .toProfileType(_profileDAO, _jdbcLocaleSource, _mop, _shellCommandsUtil, _profileTypeKindLabelProvider)
            : new ProfileTypeData().toProfileType(
                _profileDAO, _jdbcLocaleSource, _mop, _shellCommandsUtil, _profileTypeKindLabelProvider);

        if (profileType.getKind() != null)
        {
            _profileTypeKindLabelProvider.addLabel(profileType.getKind());
        }
        profileType = _profileDAO.mergeProfileType(profileType);
        _shellCommandsUtil.printLine("Profile Type created with ID: " + profileType.getId());
    }

    /**
     * Post construction
     */
    @PostConstruct
    public void postConstruct()
    {
        _shellCommandsUtil.setShellLogger(shellLogger);
    }

}

class MembershipTypeData
{
    /** json mapping */
    public String name;
    /** json mapping */
    public String programmaticIdentifier;
    /** json mapping */
    public String profileTypeProgId;
    /** json mapping */
    public String[] defaultOperations;

    /**
     * Convert this MembershipTypeData into a MembershipType.  If a MembershipType with this data's programmatic identifier
     * already exists, the existing MembershipType is simply returned
     *
     * @param profileDAO the profile DAO
     * @param localeSource the locale source used to create LoKs
     * @param mop the MembershipOperationProvider
     * @param cmdUtil the shell commands util
     *
     * @return an instance of MembershipType
     *
     * @throws LocaleSourceException if creation of LoK fails
     * @throws IOException if creation of LoK fails
     */
    public MembershipType toMembershipType(@Nonnull ProfileDAO profileDAO, @Nonnull JDBCLocaleSource localeSource,
        MembershipOperationProvider mop,
        ShellCommandsUtil cmdUtil) throws LocaleSourceException, IOException
    {
        cmdUtil.printLine("Creating / Getting MembershipType...");
        String additionalAskArg = "for this MembershipType";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(programmaticIdentifier, additionalAskArg);
        Optional<MembershipType> membershipType;
        if ((membershipType = profileDAO.getMembershipType(profileTypeProgId, programmaticIdentifier)).isPresent())
        {
            cmdUtil.printLine("MembershipType already exists.  Returning...");
            return membershipType.get();
        }
        membershipType = Optional.of(new MembershipType());
        membershipType.get().setProgrammaticIdentifier(programmaticIdentifier);

        name = cmdUtil.getName(name, additionalAskArg);

        membershipType.get().setName(cmdUtil.createEnglishLOK(name));

        if (defaultOperations == null)
        {
            List<String> operations = new ArrayList<>();
            boolean cont = true;
            while (cont)
            {
                cmdUtil.getShell().printNewline();
                String line = cmdUtil.getShell().readLine("Membership Operation(To stop adding operations, just hit ENTER):");
                if (!isEmptyString(line))
                {
                    operations.add(line);
                }
                else
                {
                    cont = false;
                }
            }
            defaultOperations = operations.toArray(new String[operations.size()]);
        }

        membershipType.get().getDefaultOperations().addAll(Arrays.stream(defaultOperations)
            .map(opProg -> profileDAO.getMembershipOperation(opProg).orElse(null))
            .filter(memOp -> memOp != null)
            .collect(Collectors.toList()));

        return membershipType.get();
    }
}

/**
 * Json Mapping class for ProfileType
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class ProfileTypeData
{
    /** json mapping */
    public String programmaticIdentifier;
    /** json mapping */
    public String name;
    /** json mapping */
    public String description;
    /** json mapping */
    public MembershipTypeData[] membershipTypes;
    /** json mapping */
    public LabelData kind;

    /**
     * Convert this ProfileTypeData into a ProfileType. If a ProfileType with this data's programmatic identifier already
     * exists, the existing ProfileType is simply returned
     *
     * @param profileDAO the profile DAO
     * @param localeSource the locale source used to create localized object keys
     * @param mop the MembershipOperationProvider
     * @param cmdUtil the shell commands util
     * @param profileTypeKindLabelProvider the profile type kind label provider
     *
     * @return an instance of ProfileType
     */
    @Nonnull
    public ProfileType toProfileType(
        @Nonnull ProfileDAO profileDAO, @Nonnull JDBCLocaleSource localeSource,
        @Nonnull MembershipOperationProvider mop, ShellCommandsUtil cmdUtil,
        @Nonnull ProfileTypeKindLabelProvider profileTypeKindLabelProvider)
        throws LocaleSourceException, IOException
    {
        cmdUtil.printLine("Creating / Getting ProfileType...");
        String additionalAskArg = "for this ProfileType";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(
            programmaticIdentifier, additionalAskArg);
        Optional<ProfileType> profileType;
        if ((profileType = profileDAO.getProfileType(programmaticIdentifier)).isPresent())
        {
            cmdUtil.printLine("ProfileType already exists. Returning...");
            addMembershipTypes(profileType.get(), profileDAO, localeSource, mop, cmdUtil);
            setKind(profileType.get(), localeSource, profileTypeKindLabelProvider, cmdUtil);
            return profileType.get();
        }
        profileType = Optional.of(new ProfileType());
        profileType.get().setProgrammaticIdentifier(programmaticIdentifier);

        name = cmdUtil.getName(name, additionalAskArg);
        description = cmdUtil.getDescription(description, additionalAskArg);

        profileType.get().setName(cmdUtil.createEnglishLOK(name));

        if (!isEmptyString(description))
        {
            profileType.get().setDescription(cmdUtil.createEnglishLOK(description));
        }

        addMembershipTypes(profileType.get(), profileDAO, localeSource, mop, cmdUtil);

        setKind(profileType.get(), localeSource, profileTypeKindLabelProvider, cmdUtil);

        return profileType.get();
    }

    private void addMembershipTypes(ProfileType profType, ProfileDAO profileDAO,
        JDBCLocaleSource localeSource, MembershipOperationProvider mop, ShellCommandsUtil cmdUtil)
        throws LocaleSourceException, IOException
    {
        if (membershipTypes == null)
        {
            boolean cont = true;
            while (cont)
            {
                cmdUtil.getShell().printNewline();
                cont = cmdUtil.getConfirmation("Add a MembershipType?");
                if (cont)
                {
                    MembershipTypeData data = new MembershipTypeData();
                    data.profileTypeProgId = programmaticIdentifier;
                    MembershipType type = data.toMembershipType(profileDAO, localeSource, mop, cmdUtil);
                    type.setProfileType(profType);
                    profType.getMembershipTypeSet().add(type);
                }
            }
        }
        else
        {
            for (MembershipTypeData memTypeData : membershipTypes)
            {
                memTypeData.profileTypeProgId = programmaticIdentifier;
                MembershipType type = memTypeData.toMembershipType(profileDAO, localeSource, mop, cmdUtil);
                type.setProfileType(profType);
                profType.getMembershipTypeSet().add(type);
            }
        }
    }

    private void setKind(ProfileType profType, JDBCLocaleSource localeSource, ProfileTypeKindLabelProvider
        profileTypeKindLabelProvider, ShellCommandsUtil cmdUtil)
        throws LocaleSourceException, IOException
    {
        if (kind == null)
        {
            kind = new LabelData();
        }
        profType.setKind(kind.toLabel(profileTypeKindLabelProvider, cmdUtil));
    }
}


/**
 * {@link Label} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class LabelData
{
    /** json mapping */
    public String programmaticIdentifier;
    /** json mapping */
    public String name;
    /** json mapping */
    public String description;

    /**
     * Convert the LabelData into a Label
     *
     * @param lps the {@link LabelDomainProvider} to use for creating the label
     * @param cmdUtil the shell commands util
     *
     * @return a Label
     *
     * @throws IOException if the shell screws up
     * @throws LocaleSourceException if creating the localized object keys fails
     */
    public Label toLabel(@Nonnull LabelDomainProvider lps, @Nonnull ShellCommandsUtil cmdUtil)
        throws IOException, LocaleSourceException
    {
        cmdUtil.printLine("Creating/Getting Label...");
        String additionalAskArg = "for this Label";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(programmaticIdentifier, additionalAskArg);
        Label label = lps.getLabelOrNew(programmaticIdentifier);
        if (label.getId() != null && label.getId() != 0L)
        {
            String cont = cmdUtil.getInteractiveArg("A label with the given programmatic identifier exists, do you wish to update"
                                                    + " it? (y/n)",
                result -> !isEmptyString(result) && ("n".equals(result.toLowerCase()) || "y".equals
                    (result.toLowerCase())));
            switch (cont)
            {
                case "n":
                case "N":
                    return label;
                default:
                    break;
            }
        }
        name = cmdUtil.getName(name, additionalAskArg);
        description = cmdUtil.getDescription(description, additionalAskArg);

        label.setName(cmdUtil.createEnglishLOK(name));

        if (!isEmptyString(description))
        {
            label.setDescription(cmdUtil.createEnglishLOK(description));
        }
        return label;
    }
}
