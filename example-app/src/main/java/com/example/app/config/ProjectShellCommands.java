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

import com.example.app.model.profile.Membership;
import com.example.app.model.profile.MembershipOperation;
import com.example.app.model.profile.MembershipType;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.profile.ProfileType;
import com.example.app.model.repository.Repository;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.service.ProfileService;
import com.example.app.service.ProfileTypeKindLabelProvider;
import com.example.app.service.ResourceCategoryLabelProvider;
import com.example.app.service.ResourceTagsLabelProvider;
import com.example.app.support.LRLabsUtil;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.i2rd.cms.util.AbstractShellCommands;
import com.i2rd.hibernate.task.HibernateSpringShellCommands;

import net.proteusframework.cms.label.Label;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.spring.CoreShellComponent;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.AuthenticationDomainDAO;

import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * Shell commands for ldp project
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/23/15 9:54 AM
 */
@Component
@Lazy
public class ProjectShellCommands extends AbstractShellCommands
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ProjectShellCommands.class);

    @Autowired
    private ProfileService _profileService;
    @Autowired
    private JDBCLocaleSource _jdbcLocaleSource;
    @Autowired
    private ProfileDAO _profileDAO;
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
     *   Post construction
     */
    @PostConstruct
    public void postConstruct()
    {
        _shellCommandsUtil.setShellLogger(shellLogger);
    }

    //Wrote this to test the ProfileData model.  Commented it out for future reference.
//    /**
//     *   Blah
//     *
//     */
//    @CliCommand(value = "testProfileData")
//    public void testProfileData()
//    {
//        QLBuilder profileBuilder = new QLBuilderImpl(Profile.class, "profile");
//        Profile profile = (Profile)profileBuilder.getQueryResolver().list().get(0);
//        String name = "testing";
//        String category = "test";
//        JsonObject data = new JsonObject();
//        data.add("testBool", new JsonPrimitive(true));
//        data.add("testNum", new JsonPrimitive(1));
//        data.add("testStr", new JsonPrimitive("test-string"));
//        JsonArray testArray = new JsonArray();
//        testArray.add(new JsonPrimitive("test-1"));
//        testArray.add(new JsonPrimitive("test-2"));
//        data.add("testArr", testArray);
//
//        ProfileData pData = new ProfileData();
//        pData.setProfile(profile);
//        pData.setName(name);
//        pData.setCategory(category);
//        pData.setData(data);
//
//        _profileDAO.saveProfileData(pData);
//
//        QLBuilder profileDataBuilder = new QLBuilderImpl(ProfileData.class, "pData");
//        profileDataBuilder.appendCriteria(ProfileData.PROFILE_PROP, PropertyConstraint.Operator.eq, profile);
//        ProfileData retrieved = (ProfileData)profileDataBuilder.getQueryResolver().list().get(0);
//
//        Assert.assertEquals(retrieved.getProfile().getId(), profile.getId());
//        Assert.assertEquals(retrieved.getName(), name);
//        Assert.assertEquals(retrieved.getCategory(), category);
//        Assert.assertEquals(retrieved.getData().get("testBool").getAsBoolean(), true);
//        Assert.assertEquals(retrieved.getData().get("testNum").getAsInt(), 1);
//        Assert.assertEquals(retrieved.getData().get("testStr").getAsString(), "test-string");
//        Assert.assertEquals(retrieved.getData().get("testArr").getAsJsonArray().get(0).getAsString(), "test-1");
//        Assert.assertEquals(retrieved.getData().get("testArr").getAsJsonArray().get(1).getAsString(), "test-2");
//
//        _profileDAO.deleteProfileData(retrieved);
//    }

    /**
     *   Add a ProfileType
     *   @param profileTypeData the profile type data, in json
     *   <pre>
     *    {
     *      "programmaticIdentifier": "testProfileType",
     *      "name": "testProfileType",
     *      "description": "a test profile type",
     *      "kind": "Coaching"
     *    }
     *   </pre>
     *   @throws LocaleSourceException if creation of name or description localized object keys fails
     *   @throws IOException if the shell screws up
     */
    @CliCommand(value = "createOrModifyProfileType",
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

        if(profileType.getKind() != null)
        {
            _profileTypeKindLabelProvider.addLabel(profileType.getKind());
        }
        profileType = _profileDAO.mergeProfileType(profileType);
        _shellCommandsUtil.printLine("Profile Type created with ID: " + profileType.getId());
    }

    /**
     *   Add a Resource Category to the Resource Categories label domain
     *   @param categoryData the category data, in json
     *   @throws LocaleSourceException if creation of Industry localized oject keys fails
     *   @throws IOException if the shell screws up
     */
    @CliCommand(value = "addResourceCategory", help = "Add a Resource Category to the Resource Categories label domain.")
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
     *   Add a Resource Type to the Resource Types label domain
     *   @param typeData the type data, in json
     *   @throws LocaleSourceException if creation of Industry localized oject keys fails
     *   @throws IOException if the shell screws up
     */
    @CliCommand(value = "addResourceType", help = "Add a Resource Type to the Resource Types label domain.")
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
     *   Create a User for the given Principal username
     *   @param username the username to create the User for
     *   @param authDomainName the authentication domain to search for the Principal in
     *   @param profileId the profile programmatic identifier.
     *   @return the created or retrieved User, or null if a Principal could not be found
     */
    @Nullable
    @CliCommand(value = "createUserForPrincipal", help = "Create a User for the given Principal username")
    public User createUserForPrincipal(
        @CliOption(key = "username",
            help = "The username of the Principal to create the User for", mandatory = true) String username,
        @CliOption(key = "authdomain",
            help = "The authentication domain name to search in", mandatory =  false) String authDomainName,
        @CliOption(key = "profile", help = "The profile entity programmatic identifier", mandatory = true) String profileId)
    {
        Preconditions.checkNotNull(username, "Username was null!");
        AuthenticationDomain authDomain = null;
        if(!isEmptyString(authDomainName))
        {
            authDomain = _domainDAO.getAuthenticationDomain(authDomainName);
        }
        Profile profile = _profileService.getProfileByProgrammaticIdentifier(profileId);
        if(profile == null)
            throw new IllegalArgumentException("Given programmatic identifier for Profile was not valid");

        Principal principal = principalDAO.getPrincipalByLogin(username, authDomain);
        if(principal != null)
        {

            User user = _userDAO.getUserForPrincipal(principal);
            if (user == null)
            {
                user = new User();
                user.setPrincipal(principal);
                user = _userDAO.mergeUser(user);
                _profileService.setOwnerProfileForUser(user, profile);
                _shellCommandsUtil.printLine("User saved with ID: " + user.getId());
            }
            else
            {
                _profileService.setOwnerProfileForUser(user, profile);
                _shellCommandsUtil.printLine("User saved with ID: " + user.getId());
            }
            return user;
        }
        else
            _shellCommandsUtil.printLine("Retrieved principal was null!");
        return null;
    }

    /**
     *   Add the given operation to an existing or new membership on the given coaching entity for the given user
     *   @param username the username
     *   @param authDomainName the authentication domain
     *   @param operation the operation
     *   @param profileId the profile programmatic identifier.
     */
    @CliCommand(value = "addOperationForUserOnCoaching", help = "Add the given operation to an existing or new membership on the "
        + "given coaching entity for the given user")
    public void addMembershipOperationForUserOnCoaching(
        @CliOption(key = "username",
            help = "The username of the Principal to search for the User on", mandatory = true) String username,
        @CliOption(key = "authdomain", help = "The authentication domain to search in") String authDomainName,
        @CliOption(key = "operation",
            help = "The programmatic identifier of the membership operation", mandatory = true) String operation,
        @CliOption(key = "profile", help = "The profile entity programmatic identifier", mandatory = true) String profileId)
    {
        MembershipOperation mop = _profileDAO.getMembershipOperation(operation).orElseThrow(
            () -> new IllegalArgumentException("Given programmatic identifier for Membership Operation was not valid"));
        Profile profile = _profileService.getProfileByProgrammaticIdentifier(profileId);
        if(profile == null)
            throw new IllegalArgumentException("Given programmatic identifier for Profile was not valid");

        User user = createUserForPrincipal(username, authDomainName, profileId);

        if(user == null)
            throw new IllegalArgumentException("Principal could not be retrieved for given username/authdomain combination");

        List<Membership> memberships = _profileDAO.getMemberships(profile, user, LRLabsUtil.UTC);
        Membership membership;
        if(memberships.size() > 0)
        {
            membership = memberships.get(0);
        }
        else
        {
            membership = new Membership();
            membership.setUser(user);
            membership.setProfile(profile);
        }
        if(!membership.getOperations().contains(mop))
        {
            membership.getOperations().add(mop);
            _profileDAO.saveMembership(membership);
            _shellCommandsUtil.printLine("Membership saved with associated operation.  ID: " + membership.getId());
        }
        else
            _shellCommandsUtil.printLine("Membership already has operation associated with it.");
    }

    /**
     *   Refreshes the database by dropping the app and audit tables, as well as hibernate_sequence
     *   @throws IOException if the shell screws up
     */
//    @CliCommand(value = "refreshDatabase", help = "Drops the app and audit schemas, and clears out the automation log so that all"
//        + " data conversions for the project can be re-ran, then re-runs all project data conversions.")
    public void refreshDatabase() throws IOException
    {
        Boolean confirm = _shellCommandsUtil.getConfirmation(
            "Are you sure you want to drop all application data, and all audit data?");
        if(confirm)
        {
            confirm = _shellCommandsUtil.getConfirmation("Are you REALLY sure?  This cannot be undone.");
            if(confirm)
            {
                hibernateSessionHelper.beginTransaction();
                boolean success = false;
                try
                {
                    String schemaDrop = "drop schema %s cascade";
                    hibernateSessionHelper.getSession().createSQLQuery(
                        String.format(schemaDrop, ProjectConfig.PROJECT_SCHEMA))
                        .executeUpdate();
                    hibernateSessionHelper.getSession().createSQLQuery(
                        String.format(schemaDrop, ProjectConfig.ENVERS_SCHEMA))
                        .executeUpdate();
                    hibernateSessionHelper.getSession().createSQLQuery(
                        "DROP SEQUENCE hibernate_sequence")
                        .executeUpdate();
                    hibernateSessionHelper.getSession().createSQLQuery(
                        String.format("delete from automation_task_log where identifier='%s'", ProjectConfig.DC_IDENTIFIER))
                        .executeUpdate();
                    success = true;
                }
                finally
                {
                    if (success)
                    {
                        hibernateSessionHelper.commitTransaction();
                        HibernateSpringShellCommands hssc = new HibernateSpringShellCommands();
                        try
                        {
                            hssc.runDataConversions(false, ProjectConfig.DC_IDENTIFIER, -1, "ldp-shell", null, false);
                        }
                        catch (SQLException e)
                        {
                            _logger.error("An error occurred automatically re-running data conversions.", e);
                        }
                    }
                    else hibernateSessionHelper.recoverableRollbackTransaction();
                }
            }
        }
    }

    /**
     * Shell commands util class.  Used as a utility for shell commands
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    @Component
    public static class ShellCommandsUtil
    {
        @Autowired
        private CoreShellComponent _shell;
        @Autowired
        private JDBCLocaleSource _jdbcLocaleSource;

        private java.util.logging.Logger _shellLogger;

        /**
         *   Set the shell logger for the Shell Commands Util
         *   @param logger the logger
         */
        public void setShellLogger(java.util.logging.Logger logger)
        {
            _shellLogger = logger;
        }

        /**
         *   Get the underlying shell
         *   @return the shell
         */
        public CoreShellComponent getShell()
        {
            return _shell;
        }

        /**
         *   Print a line out to the logger/console
         *   @param line the line to print
         */
        public void printLine(String line)
        {
            _shellLogger.info(line);
        }

        /**
         *   Get an interactive shell argument.
         *   @param ask the prompt for the user to supply the argument
         *   @param checker a function for checking if the value supplied by the user is valid
         *   @return the user supplied value
         *   @throws IOException if the shell screws up
         */
        public String getInteractiveArg(String ask, @Nullable Function<String, Boolean> checker)
            throws IOException
        {
            boolean valid = false;
            String result = "";
            while(!valid)
            {
                _shell.printNewline();
                result = _shell.readLine(ask);
                if(checker != null)
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
         *   Get the programmatic identifier argument from the user (if needed).
         *   @param programmaticIdentifier the current programmatic identifier.  if this is not null or empty, it is simply returned
         *   @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
         *   programmatic identifier for this ProfileType?"
         *   @return a user-supplied, or the current programmatic identifier
         *   @throws IOException if the shell screws up
         */
        public String getProgrammaticIdentifier(String programmaticIdentifier, String additionalAskArg) throws IOException
        {
            if(isEmptyString(programmaticIdentifier))
            {
                programmaticIdentifier = getInteractiveArg(
                    String.format("What is the programmatic identifier %s?", additionalAskArg),
                    result -> !isEmptyString(result));
            }
            return programmaticIdentifier;
        }

        /**
         *   Get the name argument from the user (if needed).
         *   @param name the current name.  If this is not null or empty, it is simply returned
         *   @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
         *   name for this ProfileType?"
         *   @return a user-supplied name, or the current name
         *   @throws IOException if the shell screws up
         */
        public String getName(String name, String additionalAskArg) throws IOException
        {
            if(isEmptyString(name))
            {
                name = getInteractiveArg(String.format("What is the name %s?", additionalAskArg),
                    result -> !isEmptyString(result));
            }
            return name;
        }

        /**
         *   Get the description argument from the user (if needed).
         *   @param description the current description.  If this is not null or empty, it is simply returned
         *   @param additionalAskArg the arg to the question, such as "for this ProfileType" to create a question of:  "What is the
         *   description for this ProfileType?"
         *   @return a user-supplied description, or the current description
         *   @throws IOException if the shell screws up
         */
        public String getDescription(String description, String additionalAskArg) throws IOException
        {
            if(isEmptyString(description))
            {
                description = getInteractiveArg(String.format("What is the description %s?", additionalAskArg), null);
            }
            return description;
        }

        /**
         *   Get the category argument from the user (if needed).
         *   @param category the current category.  If this is not null or empty, it is simple returned
         *   @param additionalAskArg the arg to the question, such as "for this Address" to create a question of: "What is the
         *   category for this Address?"
         *   @return a user-supplied category, or the current category
         *   @throws IOException if the shell screws up
         */
        public String getCategory(String category, String additionalAskArg) throws IOException
        {
            if(isEmptyString(category))
            {
                category = getInteractiveArg(String.format("What is the category %s?", additionalAskArg),
                    result -> {
                        switch(result)
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
         *   Convert the given category string to a ContactDataCategory
         *   @param category the category to convert
         *   @param defaultCategory the default ContactDataCategory in case the given category is not recognized
         *   @return a ContactDataCategory
         */
        public ContactDataCategory convertCategory(String category, ContactDataCategory defaultCategory)
        {
            switch(category)
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
         *   Create a Localized Object Key from the given value
         *   @param value the value to create an LoK from
         *   @return an LoK
         *   @throws LocaleSourceException if creating the LoK failed
         */
        public LocalizedObjectKey createLoK(String value) throws LocaleSourceException
        {
            TransientLocalizedObjectKey valueKey = new TransientLocalizedObjectKey(new HashMap<>());
            valueKey.addLocalization(Locale.ENGLISH, value);

            return valueKey.updateOrStore(_jdbcLocaleSource, null);
        }

        /**
         *   Get a user-provided response of true or false to a question
         *   @param ask the question to ask, additional response options will be appended at the end
         *   @return boolean true or false
         *   @throws IOException if the shell screws up
         */
        public Boolean getConfirmation(String ask) throws IOException
        {
            String confirmS = getInteractiveArg(ask + "(y/n)", response -> {
                if(response != null)
                {
                    switch(response.toLowerCase())
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
            switch(confirmS.toLowerCase())
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
     *   Convert this MembershipTypeData into a MembershipType.  If a MembershipType with this data's programmatic identifier
     *   already exists, the existing MembershipType is simply returned
     *   @param profileDAO the profile DAO
     *   @param localeSource the locale source used to create LoKs
     *   @param mop the MembershipOperationProvider
     *   @param cmdUtil the shell commands util
     *   @return an instance of MembershipType
     *   @throws LocaleSourceException if creation of LoK fails
     *   @throws IOException if creation of LoK fails
     */
    public MembershipType toMembershipType(@Nonnull ProfileDAO profileDAO, @Nonnull JDBCLocaleSource localeSource,
        MembershipOperationProvider mop,
        ProjectShellCommands.ShellCommandsUtil cmdUtil) throws LocaleSourceException, IOException
    {
        cmdUtil.printLine("Creating / Getting MembershipType...");
        String additionalAskArg = "for this MembershipType";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(programmaticIdentifier, additionalAskArg);
        Optional<MembershipType> membershipType;
        if((membershipType = profileDAO.getMembershipType(profileTypeProgId, programmaticIdentifier)).isPresent())
        {
            cmdUtil.printLine("MembershipType already exists.  Returning...");
            return membershipType.get();
        }
        membershipType = Optional.of(new MembershipType());
        membershipType.get().setProgrammaticIdentifier(programmaticIdentifier);

        name = cmdUtil.getName(name, additionalAskArg);

        membershipType.get().setName(cmdUtil.createLoK(name));

        if(defaultOperations == null)
        {
            List<String> operations = new ArrayList<>();
            boolean cont = true;
            while(cont)
            {
                cmdUtil.getShell().printNewline();
                String line = cmdUtil.getShell().readLine("Membership Operation(To stop adding operations, just hit ENTER):");
                if(!isEmptyString(line))
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
     *   Convert this ProfileTypeData into a ProfileType. If a ProfileType with this data's programmatic identifier already
     *   exists, the existing ProfileType is simply returned
     *   @param profileDAO the profile DAO
     *   @param localeSource the locale source used to create localized object keys
     *   @param mop the MembershipOperationProvider
     *   @param cmdUtil the shell commands util
     *   @param profileTypeKindLabelProvider the profile type kind label provider
     *   @return an instance of ProfileType
     */
    @Nonnull
    public ProfileType toProfileType(
        @Nonnull ProfileDAO profileDAO, @Nonnull JDBCLocaleSource localeSource,
        @Nonnull MembershipOperationProvider mop, ProjectShellCommands.ShellCommandsUtil cmdUtil,
        @Nonnull ProfileTypeKindLabelProvider profileTypeKindLabelProvider)
        throws LocaleSourceException, IOException
    {
        cmdUtil.printLine("Creating / Getting ProfileType...");
        String additionalAskArg = "for this ProfileType";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(
            programmaticIdentifier, additionalAskArg);
        Optional<ProfileType> profileType;
        if((profileType = profileDAO.getProfileType(programmaticIdentifier)).isPresent())
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

        if(!isEmptyString(description))
        {
            profileType.get().setDescription(cmdUtil.createLoK(description));
        }

        addMembershipTypes(profileType.get(), profileDAO, localeSource, mop, cmdUtil);

        setKind(profileType.get(), localeSource, profileTypeKindLabelProvider, cmdUtil);

        return profileType.get();
    }

    private void setKind(ProfileType profType, JDBCLocaleSource localeSource, ProfileTypeKindLabelProvider
        profileTypeKindLabelProvider, ProjectShellCommands.ShellCommandsUtil cmdUtil)
        throws LocaleSourceException, IOException
    {
        if(kind == null)
        {
            kind = new LabelData();
        }
        profType.setKind(kind.toLabel(profileTypeKindLabelProvider, cmdUtil));
    }

    private void addMembershipTypes(ProfileType profType, ProfileDAO profileDAO,
        JDBCLocaleSource localeSource, MembershipOperationProvider mop, ProjectShellCommands.ShellCommandsUtil cmdUtil)
        throws LocaleSourceException, IOException
    {
        if(membershipTypes == null)
        {
            boolean cont = true;
            while(cont)
            {
                cmdUtil.getShell().printNewline();
                cont = cmdUtil.getConfirmation("Add a MembershipType?");
                if(cont)
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
            for(MembershipTypeData memTypeData : membershipTypes)
            {
                memTypeData.profileTypeProgId = programmaticIdentifier;
                MembershipType type = memTypeData.toMembershipType(profileDAO, localeSource, mop, cmdUtil);
                type.setProfileType(profType);
                profType.getMembershipTypeSet().add(type);
            }
        }
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
     *   Convert the ContactData into a Contact
     *   @param cmdUtil the shell commands util
     *   @return a Contact
     *   @throws IOException if the shell screws up
     */
    @Nonnull
    public Contact toContact(@Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Contact...");
        Contact contact = new Contact();

        if(address == null)
            address = new AddressData();
        if(phone == null)
            phone = new PhoneData();
        if(email == null)
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
     *   Convert the AddressData into an Address
     *   @param cmdUtil the shell commands util
     *   @return an Address
     *   @throws IOException if the shell screws up
     */
    @Nonnull
    public Address toAddress(@Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Address...");
        Address address = new Address();

        category = cmdUtil.getCategory(category, "for this Address");
        address.setCategory(cmdUtil.convertCategory(category, ContactDataCategory.BUSINESS));

        if(addressLines == null)
        {
            List<String> addressLineList = new ArrayList<>();
            boolean cont = true;
            while(cont)
            {
                cmdUtil.getShell().printNewline();
                String line = cmdUtil.getShell().readLine("Address Line(To stop adding address lines, just hit ENTER):");
                if(!isEmptyString(line))
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

        if(isEmptyString(city))
        {
            city = cmdUtil.getInteractiveArg("What is the city for this Address?",
                result -> !isEmptyString(result));
        }

        if(isEmptyString(state))
        {
            state = cmdUtil.getInteractiveArg("What is the state for this Address?",
                result -> !isEmptyString(result));
        }

        if(isEmptyString(zip))
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
     *   Convert the PhoneNumberData into a PhoneNumber
     *   @param cmdUtil the shell commands util
     *   @return a PhoneNumber
     *   @throws IOException if the shell screws up
     */
    @Nonnull
    public PhoneNumber toPhoneNumber(@Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Phone Number...");
        category = cmdUtil.getCategory(category, "for this Phone Number");
        if(isEmptyString(phone))
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
     *   Convert the EmailData into an EmailAddress
     *   @param cmdUtil the shell commands util
     *   @return an EmailAddress
     *   @throws IOException if the shell screws up
     */
    @Nonnull
    public EmailAddress toEmailAddress(@Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil) throws IOException
    {
        cmdUtil.printLine("Creating Email Address...");
        category = cmdUtil.getCategory(category, "for this Email Address");
        if(isEmptyString(email))
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
     *   Convert the RepositoryData into a Repository
     *   @param cmdUtil the shell commands util
     *   @return a Repository
     *   @throws IOException if the shell screws up
     *   @throws LocaleSourceException if creating the localized object keys fails
     */
    public Repository toRepository(@Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil)
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
     *   Convert the LabelData into a Label
     *   @param lps the {@link LabelDomainProvider} to use for creating the label
     *   @param cmdUtil the shell commands util
     *   @return a Label
     *   @throws IOException if the shell screws up
     *   @throws LocaleSourceException if creating the localized object keys fails
     */
    public Label toLabel(@Nonnull LabelDomainProvider lps, @Nonnull ProjectShellCommands.ShellCommandsUtil cmdUtil)
    throws IOException, LocaleSourceException
    {
        cmdUtil.printLine("Creating/Getting Label...");
        String additionalAskArg = "for this Label";
        programmaticIdentifier = cmdUtil.getProgrammaticIdentifier(programmaticIdentifier, additionalAskArg);
        Label label = lps.getLabelOrNew(programmaticIdentifier);
        if(label.getId() != null && label.getId() != 0L)
        {
            String cont = cmdUtil.getInteractiveArg("A label with the given programmatic identifier exists, do you wish to update"
                + " it? (y/n)", result -> !isEmptyString(result) && ("n".equals(result.toLowerCase()) || "y".equals
                (result.toLowerCase())));
            switch(cont)
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

        if(!isEmptyString(description))
        {
            label.setDescription(cmdUtil.createLoK(description));
        }
        return label;
    }
}
