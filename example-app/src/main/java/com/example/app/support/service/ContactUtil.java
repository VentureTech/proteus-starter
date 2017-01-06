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

package com.example.app.support.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.geo.CountryInformation;
import net.proteusframework.core.geo.RegionInformation;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.Gender;
import net.proteusframework.users.model.PhoneNumber;

import static java.util.Optional.ofNullable;

/**
 * Class containing utility methods for dealing with {@link Contact}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/1/15 3:43 PM
 */
public class ContactUtil
{
    /**
     * Get an address.
     *
     * @param contact the contact. May be null.
     * @param category categories to search.
     *
     * @return return an optional containing the first address that matches the category.
     */
    @Nonnull
    public static Optional<Address> getAddress(@Nullable Contact contact, @Nonnull ContactDataCategory... category)
    {
        if (contact == null)
            return Optional.empty();
        final Map<ContactDataCategory, Address> m = new EnumMap<>(ContactDataCategory.class);
        contact.getAddresses().stream().filter(a -> !a.getAddressLineList().isEmpty())
            .forEach(a -> m.put(a.getCategory(), a));
        for (ContactDataCategory cdc : category)
            if (m.containsKey(cdc))
                return ofNullable(m.get(cdc));
        return Optional.empty();
    }

    /**
     * Gets contact locale.
     *
     * @param contact the contact
     * @param defaultLocale optional default locale to use if one could not be found
     *
     * @return the recipient locale
     */
    public static Optional<Locale> getContactLocale(@Nullable Contact contact, @Nullable Locale defaultLocale)
    {
        if (contact == null)
            return ofNullable(defaultLocale);
        final Locale preferredLocale = contact.getPreferredLocale();
        return preferredLocale == null
            ? ofNullable(defaultLocale)
            : Optional.of(preferredLocale);
    }

    /**
     * Gets contact timezone.
     *
     * @param contact the contact
     * @param defaultTimeZone optional default TimeZone to use if one could not be found
     *
     * @return the time zone.
     */
    public static Optional<TimeZone> getContactTimeZone(@Nullable Contact contact, @Nullable TimeZone defaultTimeZone)
    {
        if (contact == null)
            return ofNullable(defaultTimeZone);
        final TimeZone preferredTimeZone = contact.getPreferredTimeZone();
        return preferredTimeZone == null
            ? ofNullable(defaultTimeZone)
            : Optional.of(preferredTimeZone);
    }

    /**
     * Get an email address.
     *
     * @param contact the contact. May be null.
     * @param category categories to search.
     *
     * @return return an optional containing the first email address that matches the category.
     */
    @Nonnull
    public static Optional<EmailAddress> getEmailAddress(@Nullable Contact contact, @Nonnull ContactDataCategory... category)
    {
        if (contact == null)
            return Optional.empty();
        final Map<ContactDataCategory, EmailAddress> m = new EnumMap<>(ContactDataCategory.class);
        contact.getEmailAddresses().stream().filter(ea -> !StringFactory.isEmptyString(ea.getEmail()))
            .forEach(ea -> m.put(ea.getCategory(), ea));
        for (ContactDataCategory cdc : category)
            if (m.containsKey(cdc))
                return ofNullable(m.get(cdc));
        return Optional.empty();
    }

    /**
     * Get a phone number.
     *
     * @param contact the contact. May be null.
     * @param category categories to search.
     *
     * @return return an optional containing the first phone number that matches the category.
     */
    @Nonnull
    public static Optional<PhoneNumber> getPhoneNumber(@Nullable Contact contact, @Nonnull ContactDataCategory... category)
    {
        if (contact == null)
            return Optional.empty();
        final Map<ContactDataCategory, PhoneNumber> m = new EnumMap<>(ContactDataCategory.class);
        contact.getPhoneNumbers().stream().filter(pn -> !pn.isEmpty())
            .forEach(pn -> m.put(pn.getCategory(), pn));
        for (ContactDataCategory cdc : category)
            if (m.containsKey(cdc))
                return ofNullable(m.get(cdc));
        return Optional.empty();
    }

    /**
     * Get a list of timezone IDs, excluding those specified
     *
     * @param excludedTimeZoneIDs a list of TimeZone IDs to exclude
     * @param countryCode the country code to use for retrieving the timezones -- defaults to US if null
     *
     * @return a list of TimeZone IDs
     */
    @Nonnull
    public static List<String> getTimeZoneList(@Nullable List<String> excludedTimeZoneIDs, @Nullable String countryCode)
    {
        final List<String> list = new ArrayList<>(
            CountryInformation.getInstance().getTimeZonesByCodeOrCountry(ofNullable(countryCode).orElse("US")));

        if (excludedTimeZoneIDs != null)
            excludedTimeZoneIDs.forEach(list::remove);

        return list;
    }

    /**
     * Gets a localized list of state abbreviations for the United States with a null value at the 0 index.
     *
     * @return the state abbreviations with null value
     */
    public static List<String> getUSStateAbbreviationsWithNullValue()
    {
        List<String> stateAbbrevs = getUSStateAbbreviations();
        stateAbbrevs.add(0, null);
        return stateAbbrevs;
    }

    /**
     * Gets a localized list of state abbreviations for the United States
     *
     * @return the state abbreviations
     */
    public static List<String> getUSStateAbbreviations()
    {
        RegionInformation rInfo = RegionInformation.getInstance();
        CountryInformation cInfo = CountryInformation.getInstance();
        String countryCode = cInfo.getISO2CountryCode(null, "US");
        List<String> stateAbbrevs = rInfo.getSubRegionCodes(
            countryCode);
        List<String> states = stateAbbrevs.stream().filter(sa -> {
            List<String> notIncluded = new ArrayList<>();
            notIncluded.add("AA");
            notIncluded.add("AP");
            notIncluded.add("AE");
            notIncluded.add("VI");
            notIncluded.add("PR");
            notIncluded.add("PW");
            notIncluded.add("MP");
            notIncluded.add("MH");
            notIncluded.add("GU");
            notIncluded.add("FM");
            notIncluded.add("AS");
            return !notIncluded.contains(sa);
        }).collect(Collectors.toList());
        Collections.sort(states);

        return states;
    }

    /**
     * Get the US State Code for the given US State name
     *
     * @param stateName the state name
     * @param localeContext the locale context to use
     *
     * @return the state code
     */
    @Nullable
    public static String getUSStateCode(@Nullable String stateName, LocaleContext localeContext)
    {
        if (stateName == null) return null;
        RegionInformation rInfo = RegionInformation.getInstance();
        CountryInformation cInfo = CountryInformation.getInstance();
        String countryCode = cInfo.getISO2CountryCode(null, "US");

        return ofNullable(rInfo.getRegionCode(localeContext.getLanguage(), countryCode, stateName)).orElse(stateName);
    }

    /**
     * Get the US State Name for the given US State code
     *
     * @param stateCode the state code
     * @param localeContext the locale context to use
     *
     * @return the state name
     */
    public static String getUSStateName(String stateCode, LocaleContext localeContext)
    {
        RegionInformation rInfo = RegionInformation.getInstance();
        CountryInformation cInfo = CountryInformation.getInstance();
        String countryCode = cInfo.getISO2CountryCode(null, "US");

        return rInfo.getRegionName(localeContext.getLanguage(), countryCode, stateCode);
    }

    /**
     * Check if the contact is empty.  Will return true if the contact is null.
     *
     * <br>
     * A contact is considered not empty if any of its fields (excluding those listed below) have been changed from the default.
     * The following fields do not affect empty status:
     * <ul>
     * <li>{@link Contact#getPreferredLocale()}</li>
     * <li>{@link Contact#getPreferredTimeZone()}</li>
     * </ul>
     *
     * @param c the contact, may be null.
     *
     * @return true if the contact is empty or null.  False otherwise.
     */
    public static boolean isContactEmpty(@Nullable final Contact c)
    {
        if (c == null)
            return true;
        if (!c.getEmailAddresses().isEmpty())
            return false;
        if (!c.getAddresses().isEmpty())
            return false;
        if (!c.getPhoneNumbers().isEmpty())
            return false;

        if (c.getName() != null && !c.getName().isEmpty())
            return false;

        if (c.getBirthDate() != null)
            return false;
        if (c.getCompany() != null && c.getCompany().getName() != null && !c.getCompany().getName().isEmpty())
            return false;
        if (c.getDepartment() != null && !c.getDepartment().isEmpty())
            return false;
        if (c.getGender() != Gender.UNSPECIFIED)
            return false;
        if (c.getNotes() != null && !c.getNotes().isEmpty())
            return false;
        if (c.getPosition() != null && !c.getPosition().isEmpty())
            return false;
        return !c.isOptin();
    }

    private ContactUtil()
    {
    }
}
