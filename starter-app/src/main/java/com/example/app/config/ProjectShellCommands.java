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
import com.example.app.profile.model.membership.Membership;
import com.example.app.profile.model.membership.MembershipOperation;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.profile.service.ProfileTypeKindLabelProvider;
import com.example.app.repository.model.Repository;
import com.example.app.resource.service.ResourceCategoryLabelProvider;
import com.example.app.resource.service.ResourceTagsLabelProvider;
import com.example.app.support.service.AppUtil;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.i2rd.cms.util.AbstractShellCommands;

import net.proteusframework.cms.label.Label;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;
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
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ProjectShellCommands.class);

    @Autowired
    private JDBCLocaleSource _jdbcLocaleSource;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private CompanyDAO _companyDAO;
    @Autowired
    private ShellCommandsUtil _shellCommandsUtil;
    @Autowired
    private ResourceTagsLabelProvider _resourceTagsLabelProvider;
    @Autowired
    private ResourceCategoryLabelProvider _resourceCategoryLabelProvider;
    @Autowired
    private ProfileTypeKindLabelProvider _profileTypeKindLabelProvider;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private AuthenticationDomainDAO _domainDAO;
    @Autowired
    private MembershipOperationProvider _mop;

    /**
     * Add the given operation to an existing or new membership on the given company for the given user
     *
     * @param username the username
     * @param authDomainName the authentication domain
     * @param operation the operation
     * @param profileId the profile programmatic identifier.
     */
    @CliCommand(value = "add operation for company user",
        help = "Add the given operation to an existing or new membership on the given company for the given user")
    public void addMembershipOperationForCompanyUser(
        @CliOption(key = "username",
            help = "The username of the Principal to search for the User on", mandatory = true) String username,
        @CliOption(key = "authdomain", help = "The authentication domain to search in") String authDomainName,
        @CliOption(key = "operation",
            help = "The programmatic identifier of the membership operation", mandatory = true) String operation,
        @CliOption(key = "profile", help = "The profile entity programmatic identifier", mandatory = true) String profileId)
    {
        MembershipOperation mop = _profileDAO.getMembershipOperation(operation).orElseThrow(
            () -> new IllegalArgumentException("Given programmatic identifier for Membership Operation was not valid"));
        Company profile = _profileDAO.getProfile(Company.class, Integer.valueOf(profileId));
        if (profile == null)
            throw new IllegalArgumentException("Given programmatic identifier for Profile was not valid");

        User user = createUserForPrincipal(username, authDomainName, profileId);

        if (user == null)
            throw new IllegalArgumentException("Principal could not be retrieved for given username/authdomain combination");

        List<Membership> memberships = _profileDAO.getMemberships(profile, user, AppUtil.UTC);
        Membership membership;
        if (memberships.size() > 0)
        {
            membership = memberships.get(0);
        }
        else
        {
            membership = new Membership();
            membership.setUser(user);
            membership.setProfile(profile);
        }
        if (!membership.getOperations().contains(mop))
        {
            membership.getOperations().add(mop);
            _profileDAO.saveMembership(membership);
            _shellCommandsUtil.printLine("Membership saved with associated operation.  ID: " + membership.getId());
        }
        else
            _shellCommandsUtil.printLine("Membership already has operation associated with it.");
    }

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
    public void addModifyProfileType(
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
     * Add a Resource Category to the Resource Categories label domain
     *
     * @param categoryData the category data, in json
     *
     * @throws LocaleSourceException if creation of Industry localized oject keys fails
     * @throws IOException if the shell screws up
     */
    @CliCommand(value = "add resource category", help = "Add a Resource Category to the Resource Categories label domain.")
    public void addResourceCategory(
        @CliOption(key = "data", help = "The category data, in json. This can be left blank to use the interactive shell")
            String categoryData) throws IOException, LocaleSourceException
    {
        Gson gson = new GsonBuilder().serializeNulls().create();

        Label label = !isEmptyString(categoryData)
            ? gson.fromJson(categoryData, LabelData.class)
            .toLabel(_resourceTagsLabelProvider, _shellCommandsUtil)
            : new LabelData().toLabel(_resourceTagsLabelProvider, _shellCommandsUtil);
        _resourceTagsLabelProvider.addLabel(label);
        _shellCommandsUtil.printLine("Category successfully added with ID: " + label.getId());
    }

    /**
     * Add a Resource Type to the Resource Types label domain
     *
     * @param typeData the type data, in json
     *
     * @throws LocaleSourceException if creation of Industry localized oject keys fails
     * @throws IOException if the shell screws up
     */
    @CliCommand(value = "add resource type", help = "Add a Resource Type to the Resource Types label domain.")
    public void addResourceType(
        @CliOption(key = "data", help = "The type data, in json. This can be left blank to use the interactive shell")
            String typeData) throws IOException, LocaleSourceException
    {
        Gson gson = new GsonBuilder().serializeNulls().create();

        Label label = !isEmptyString(typeData)
            ? gson.fromJson(typeData, LabelData.class)
            .toLabel(_resourceCategoryLabelProvider, _shellCommandsUtil)
            : new LabelData().toLabel(_resourceCategoryLabelProvider, _shellCommandsUtil);
        _resourceCategoryLabelProvider.addLabel(label);
        _shellCommandsUtil.printLine("Type successfully added with ID: " + label.getId());
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

        membershipType.get().setName(cmdUtil.createLoK(name));

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

        profileType.get().setName(cmdUtil.createLoK(name));

        if (!isEmptyString(description))
        {
            profileType.get().setDescription(cmdUtil.createLoK(description));
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
 * {@link Contact} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class ContactData
{
    /** json mapping */
    public AddressData address;
    /** json mapping */
    public PhoneData phone;
    /** json mapping */
    public EmailData email;

    /**
     * Convert the ContactData into a Contact
     *
     * @param cmdUtil the shell commands util
     *
     * @return a Contact
     *
     * @throws IOException if the shell screws up
     */
    @Nonnull
    public Contact toContact(@Nonnull ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Contact...");
        Contact contact = new Contact();

        if (address == null)
            address = new AddressData();
        if (phone == null)
            phone = new PhoneData();
        if (email == null)
            email = new EmailData();

        contact.getAddresses().add(address.toAddress(cmdUtil));
        contact.getPhoneNumbers().add(phone.toPhoneNumber(cmdUtil));
        contact.getEmailAddresses().add(email.toEmailAddress(cmdUtil));
        return contact;
    }
}

/**
 * {@link Address} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class AddressData
{
    /** json mapping */
    public String[] addressLines;
    /** json mapping */
    public String city;
    /** json mapping */
    public String state;
    /** json mapping */
    public String zip;
    /** json mapping */
    public String category;

    /**
     * Convert the AddressData into an Address
     *
     * @param cmdUtil the shell commands util
     *
     * @return an Address
     *
     * @throws IOException if the shell screws up
     */
    @Nonnull
    public Address toAddress(@Nonnull ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Address...");
        Address address = new Address();

        category = cmdUtil.getCategory(category, "for this Address");
        address.setCategory(cmdUtil.convertCategory(category, ContactDataCategory.BUSINESS));

        if (addressLines == null)
        {
            List<String> addressLineList = new ArrayList<>();
            boolean cont = true;
            while (cont)
            {
                cmdUtil.getShell().printNewline();
                String line = cmdUtil.getShell().readLine("Address Line(To stop adding address lines, just hit ENTER):");
                if (!isEmptyString(line))
                {
                    addressLineList.add(line);
                }
                else
                {
                    cont = false;
                }
            }
            addressLines = addressLineList.toArray(new String[addressLineList.size()]);
        }

        Collections.addAll(address.getAddressLineList(), addressLines);

        if (isEmptyString(city))
        {
            city = cmdUtil.getInteractiveArg("What is the city for this Address?",
                result -> !isEmptyString(result));
        }

        if (isEmptyString(state))
        {
            state = cmdUtil.getInteractiveArg("What is the state for this Address?",
                result -> !isEmptyString(result));
        }

        if (isEmptyString(zip))
        {
            zip = cmdUtil.getInteractiveArg("What is the postal code for this Address?",
                result -> !isEmptyString(result));
        }

        address.setCity(city);
        address.setState(state);
        address.setPostalCode(zip);
        return address;
    }
}

/**
 * {@link PhoneNumber} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class PhoneData
{
    /** json mapping */
    public String phone;
    /** json mapping */
    public String category;

    /**
     * Convert the PhoneNumberData into a PhoneNumber
     *
     * @param cmdUtil the shell commands util
     *
     * @return a PhoneNumber
     *
     * @throws IOException if the shell screws up
     */
    @Nonnull
    public PhoneNumber toPhoneNumber(@Nonnull ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Phone Number...");
        category = cmdUtil.getCategory(category, "for this Phone Number");
        if (isEmptyString(phone))
        {
            phone = cmdUtil.getInteractiveArg("What is the number for this Phone Number?",
                result -> !PhoneNumber.valueOf(result).isEmpty());
        }
        PhoneNumber phoneNumber = PhoneNumber.valueOf(phone);
        phoneNumber.setCategory(cmdUtil.convertCategory(category, ContactDataCategory.BUSINESS));

        return phoneNumber;
    }
}

/**
 * {@link EmailAddress} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class EmailData
{
    /** json mapping */
    public String email;
    /** json mapping */
    public String category;

    /**
     * Convert the EmailData into an EmailAddress
     *
     * @param cmdUtil the shell commands util
     *
     * @return an EmailAddress
     *
     * @throws IOException if the shell screws up
     */
    @Nonnull
    public EmailAddress toEmailAddress(@Nonnull ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Email Address...");
        category = cmdUtil.getCategory(category, "for this Email Address");
        if (isEmptyString(email))
        {
            email = cmdUtil.getInteractiveArg("What is the address for this Email Address?",
                result -> !isEmptyString(result));
        }
        EmailAddress emailAddress = new EmailAddress(email);
        emailAddress.setCategory(cmdUtil.convertCategory(category, ContactDataCategory.BUSINESS));
        return emailAddress;
    }
}

/**
 * {@link Repository} json mapping class
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
class RepositoryData
{
    /** json mapping */
    public String name;
    /** json mapping */
    public String description;

    /**
     * Convert the RepositoryData into a Repository
     *
     * @param cmdUtil the shell commands util
     *
     * @return a Repository
     *
     * @throws IOException if the shell screws up
     * @throws LocaleSourceException if creating the localized object keys fails
     */
    public Repository toRepository(@Nonnull ShellCommandsUtil cmdUtil)
        throws IOException, LocaleSourceException
    {
        cmdUtil.printLine("Creating Repository...");
        String additionalAskArg = "for this Repository";
        name = cmdUtil.getName(name, additionalAskArg);
        description = cmdUtil.getDescription(description, additionalAskArg);

        Repository repository = new Repository();
        repository.setName(cmdUtil.createLoK(name));
        repository.setDescription(cmdUtil.createLoK(description));
        return repository;
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

        label.setName(cmdUtil.createLoK(name));

        if (!isEmptyString(description))
        {
            label.setDescription(cmdUtil.createLoK(description));
        }
        return label;
    }
}
