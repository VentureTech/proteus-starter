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

package com.example.app.support;

import com.example.app.profile.model.Profile;
import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.membership.Membership;
import com.example.app.profile.model.user.User;
import com.example.app.resource.model.Resource;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.component.generator.XMLRenderer;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.mail.support.MimeTypeUtility;
import net.proteusframework.core.net.ContentTypes;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.internet.http.resource.FactoryResource;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.HTMLComponent;
import net.proteusframework.ui.miwt.component.ParentComponent;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.ComponentTreeIterator;
import net.proteusframework.users.model.Name;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.Role;
import net.proteusframework.users.model.TokenCredentials;
import net.proteusframework.users.model.dao.PrincipalContactUtil;
import net.proteusframework.users.model.dao.PrincipalDAO;
import net.proteusframework.users.model.dao.RoleDAO;

import static java.util.Optional.ofNullable;
import static net.proteusframework.ui.miwt.component.Container.of;

/**
 * Util class for holding utility methods and instances for use across applicaiton.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/2/15 11:03 AM
 */
@SuppressWarnings("unused")
@org.springframework.stereotype.Component
public class AppUtil implements Serializable
{
    /** UTC. */
    public static final TimeZone UTC = TimeZone.getTimeZone(ZoneOffset.UTC);
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(AppUtil.class);
    private static final long serialVersionUID = -6831853311031034991L;
    private static final String CLIENT_PROP_DIALOGS_ANCESTRY = "lr-app-util-dialog-ancestor-workaround";

    @SuppressWarnings("ConstantConditions")
    private static class Holder
    {
        final static AppUtil INSTANCE;
        static
        {
            INSTANCE = ApplicationContextUtils.getInstance().getContext().getBean(AppUtil.class);
        }
    }

    @Autowired
    private transient ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;
    @Autowired
    private transient RoleDAO _roleDAO;
    @Autowired
    private transient PrincipalDAO _principalDAO;
    @Autowired
    private transient CmsFrontendDAO _cmsFrontendDAO;
    @Value("${frontend-access-role}")
    private String _frontEndRoleProgId;
    @Value("${admin-access-role}")
    private String _adminRoleProgId;
    @Value("${default_site_assignment}")
    private Long _defaultEmailTemplateSite;
    @Value("${system.sender}")
    private String _systemSender;
    @Autowired @Qualifier("localeSource")
    private LocaleSource _localeSource;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static AppUtil getInstance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Gets system sender.
     *
     * @return the system sender
     */
    public String getSystemSender()
    {
        return _systemSender;
    }

    /**
     * Copy localized object key.
     *
     * @param toCopy the LOK to copy.
     *
     * @return the copy as a transient localized object key.
     */
    @Nullable
    @Contract("null->null;!null->!null")
    public TransientLocalizedObjectKey copyLocalizedObjectKey(@Nullable LocalizedObjectKey toCopy)
    {
        if(LocalizedObjectKey.isNull(toCopy) && !(toCopy instanceof TransientLocalizedObjectKey))
            return null;
        try
        {
            TransientLocalizedObjectKey tlok = toCopy instanceof TransientLocalizedObjectKey
                ? (TransientLocalizedObjectKey)toCopy
                : TransientLocalizedObjectKey.getTransientLocalizedObjectKey(_localeSource, toCopy);
            if (tlok != null)
                return new TransientLocalizedObjectKey(tlok.getText());
            else
                return new TransientLocalizedObjectKey(null);
        }
        catch (LocaleSourceException e)
        {
            throw new IllegalStateException("Unable to get LOK data.", e);
        }
    }

    /**
     * Add the given value to the given collection, returning the modified collection
     *
     * @param list the collection
     * @param val the value
     * @param <C> the type of the collection
     * @param <T> the type in the collection
     * @param <V> the type of the value -- must be T or a subclass of T
     *
     * @return the collection
     */
    public static <T, V extends T, C extends Collection<T>> C add(C list, V val)
    {
        list.add(val);
        return list;
    }

    /**
     * Add the given object into the given list only if the object is not already within the list.
     *
     * @param <V> the type of the list
     * @param list the List to add the object to
     * @param object the object to add to the list
     * @param index optional index to add the object at within the list.  If the object is already added in the list, ensures
     * that the object is placed at the given index, if one is specified.
     *
     * @return the list.
     */
    public static <V> List<V> addIfNotContains(List<V> list, V object, @Nullable Integer index)
    {
        if (!list.contains(object))
        {
            list.add(object);
        }
        if (index != null)
        {
            list.remove(object);
            list.add(index, object);
        }
        return list;
    }

    /**
     * Use ApplicationContextUtils to get an instance of the given Class
     *
     * @param <C> the Class
     * @param clazz the Class
     *
     * @return the singleton
     */
    public static <C> C autowire(Class<C> clazz)
    {
        return autowire(clazz, null);
    }

    /**
     * Converts the given information into a Bootstrap Card
     *
     * @param htmlClassName a defining classname
     * @param titleComponent the Component used as the card title
     * @param contentComponent the Component used as the card content
     *
     * @return the cardified container
     */
    public static Container cardify(String htmlClassName, Component titleComponent, Component contentComponent)
    {
        return cardify(null, htmlClassName, titleComponent, contentComponent);
    }

    /**
     * Converts the given information into a Bootstrap Card
     *
     * @param container the container to convert into a Card
     * @param htmlClassName a defining classname
     * @param titleComponent the Component used as the card title
     * @param contentComponent the Component used as the card content
     *
     * @return the cardified container
     */
    public static Container cardify(@Nullable Container container, String htmlClassName,
        Component titleComponent, Component contentComponent)
    {
        if (container == null)
        {
            return of("card " + htmlClassName,
                of("card-block  card-header" + htmlClassName + "-header",
                    titleComponent.addClassName("card-title")),
                contentComponent.addClassName("card-block"))
                .withHTMLElement(HTMLElement.section);
        }
        else
        {
            container.removeAllComponents();
            container.addClassName("card");
            container.add(of("card-block  card-header" + htmlClassName + "-header",
                titleComponent.addClassName("card-title")));
            container.add(contentComponent.addClassName("card-block"));
            return container.withHTMLElement(HTMLElement.section);
        }
    }

    /**
     * Convert the given ZonedDateTime to a UTC date for persistence
     *
     * @param dt the ZonedDateTime to convert to UTC
     *
     * @return a Date object that represents the same instant as the ZonedDateTime, but at UTC.
     */
    @Nullable
    public static Date convertForPersistence(@Nullable ZonedDateTime dt)
    {
        if (dt == null) return null;
        ZonedDateTime atUtc = dt.withZoneSameInstant(ZoneOffset.UTC);
        return new Date(atUtc.toInstant().toEpochMilli());
    }

    /**
     * Convert the given Date from UTC to a ZonedDateTime at the given TimeZone
     *
     * @param date the UTC date
     * @param zone the TimeZone to convert the time to
     *
     * @return a ZonedDateTime that represents the same instant as the UTC date, but at the given TimeZone.
     */
    @Nullable
    public static ZonedDateTime convertFromPersisted(@Nullable Date date, @Nullable TimeZone zone)
    {
        if (date == null || zone == null) return null;
        ZonedDateTime from = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        return from.withZoneSameInstant(zone.toZoneId());
    }

    /**
     * Create an HTMLComponent from content that might have links requiring externalization.
     *
     * @param internalMarkup the content
     *
     * @return the component
     */
    public static HTMLComponent createHTMLComponentFromInternalMarkup(String internalMarkup)
    {
        try
        {
            internalMarkup = XMLRenderer.parseWithRoot(internalMarkup, Event.getRequest(), Event.getResponse());
        }
        catch (IOException e)
        {
            _logger.error("Cannot parse XHTML content to externalize links", e);
        }

        return new HTMLComponent(internalMarkup);
    }

    /**
     * Disable tooltip support.
     *
     * @param component the component.
     */
    public static void disableTooltip(Component component)
    {
        component.removeClassName("tooltips");
    }

    /**
     * Check if the two Double values are equal by checking that the absolute value between them is less than 0.01
     *
     * @param d1 first double
     * @param d2 second double
     *
     * @return boolean, if true, the doubles are equal
     */
    public static boolean doubleEquals(Double d1, Double d2)
    {
        return Math.abs(d1 - d2) <= 0.01;
    }

    /**
     * Enable tooltip support.
     *
     * @param component the component.
     */
    public static void enableTooltip(Component component)
    {
        component.addClassName("tooltips");

    }

    /**
     * Get the content type for a {@link FileItem} correcting it based on the file name if the browser didn't provide a
     * content type.
     *
     * @param item the item
     *
     * @return the content type
     */
    @Nonnull
    public static String getContentType(@Nonnull FileItem item)
    {
        String ct = item.getContentType();

        if (ct == null || "application/octet-stream".equals(ct))
            ct = MimeTypeUtility.getInstance().getContentType(item.getName());

        // In case MimeTypeUtility doesn't do what we wish, it has no API contract.
        if (ct == null)
            ct = "application/octet-stream";

        return ct;
    }

    /**
     * Get the date format fixed to UTC.
     *
     * @param locale the locale.
     *
     * @return the date format.
     */
    public static SimpleDateFormat getDateFormat(Locale locale)
    {
        return getDateFormat(locale, null);
    }

    /**
     * Get the date format fixed to UTC.
     *
     * @param locale the locale.
     * @param pattern the date format pattern
     *
     * @return the date format.
     */
    public static SimpleDateFormat getDateFormat(Locale locale, @Nullable String pattern)
    {
        final SimpleDateFormat format = new SimpleDateFormat(ofNullable(pattern).orElse("MMM d, yyyy"), locale);
        format.setTimeZone(UTC);
        return format;
    }

    /**
     * Get a SimpleDateFormat to use for rendering date information within a time tag's datetime attribute
     *
     * @return SimpleDateFormat
     */
    @Contract(" -> !null")
    public static SimpleDateFormat getDateTimeAttributeDateFormat()
    {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * Get a DateTimeFormatter to use for rendering time information within a time tag's datetime attribute
     *
     * @return a DateTimeFormatter
     */
    @Nonnull
    public static DateTimeFormatter getDateTimeAttributeTimeFormat()
    {
        return DateTimeFormatter.ofPattern("hh:mm a");
    }

    /**
     * Get the file extension for the given {@link FileItem}.
     *
     * @param file the file to retrieve the file extension for
     *
     * @return the file extension (example: ".jpg")
     */
    @Nonnull
    public static String getExtensionWithDot(FileItem file)
    {
        String ext = getExtension(file);
        return '.' + ext;
    }

    /**
     * Get the file extension for the given {@link FileItem}.
     *
     * @param file the file to retrieve the file extension for
     *
     * @return the file extension (example: "jpg")
     */
    @Nonnull
    public static String getExtension(FileItem file)
    {
        return _getExtensionWithFallback(file.getName(), file.getContentType());
    }

    @NotNull
    private static String _getExtensionWithFallback(String fileName, String contentType)
    {
        String ext = StringFactory.getExtension(fileName);
        if (ext.isEmpty() && !ContentTypes.Application.octet_stream.toString().equals(contentType))
        {
            try
            {
                ext = new ContentType(contentType).getSubType().toLowerCase();
                switch (ext)
                {
                    case "jpeg":
                        ext = "jpg";
                        break;
                    case "tiff":
                        ext = "tif";
                        break;
                    case "svg+xml":
                        ext = "svg";
                        break;
                    case "x-portable-anymap":
                        ext = "pnm";
                        break;
                    case "x-portable-bitmap":
                        ext = "pbm";
                        break;
                    case "x-portable-graymap":
                        ext = "pgm";
                        break;
                    case "x-portable-pixmap":
                        ext = "ppm";
                        break;
                    default:
                        break;
                }
            }
            catch (ParseException e)
            {
                _logger.error("Unable to parse content type: " + contentType, e);
            }
        }
        return ext;
    }

    /**
     * Get the file extension for the given {@link FileEntity}.
     *
     * @param file the file to retrieve the file extension for
     *
     * @return the file extension (example: ".jpg")
     */
    @Nonnull
    public static String getExtensionWithDot(FileEntity file)
    {
        String ext = getExtension(file);
        return '.' + ext;
    }

    /**
     * Get the file extension for the given {@link FileEntity}.
     *
     * @param file the file to retrieve the file extension for
     *
     * @return the file extension (example: "jpg")
     */
    @Nonnull
    public static String getExtension(FileEntity file)
    {
        return _getExtensionWithFallback(file.getName(), file.getContentType());
    }

    /**
     * Get the text/html content type
     *
     * @return content type
     */
    public static ContentType getHtmlContentType()
    {
        try
        {
            return new ContentType("text/html");
        }
        catch (ParseException e)
        {
            _logger.error("Unable to create html content type", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the Modification State for the given component by utilizing a {@link ComponentTreeIterator}
     *
     * @param component the component that will serve as the root of the {@link ComponentTreeIterator}
     *
     * @return the modification state
     */
    public static ValueEditor.ModificationState getModificationStateForComponent(Component component)
    {
        ComponentTreeIterator treeIt = new ComponentTreeIterator(component, true, true, false);
        while (treeIt.hasNext())
        {
            final Component next = treeIt.next();
            if (next == component) continue;
            if (next instanceof ValueEditor<?>)
            {
                final ValueEditor<?> editor = (ValueEditor<?>) next;
                if (editor.getModificationState().isModified())
                    return ValueEditor.ModificationState.CHANGED;

            }
        }
        return ValueEditor.ModificationState.UNCHANGED;
    }

    /**
     * Get a valid source component from the component path.  This is similar to
     * {@link ApplicationRegistry#getValidSourceComponent(Component)} but is able to find the source component even
     * when a pesky dialog is in the in way as long as the dialog had {@link #recordDialogsAncestorComponent(Dialog, Component)}
     * call on it.
     *
     * @param component the component to start the search on
     *
     * @return a valid source component that has an ApplicationFunction annotation.
     *
     * @throws IllegalArgumentException if a valid source component could not be found.
     */
    @Nonnull
    public static Component getValidSourceComponentAcrossDialogs(Component component)
    {
        LinkedList<Component> path = new LinkedList<>();
        do
        {
            path.add(component);

            if (component.getClientProperty(CLIENT_PROP_DIALOGS_ANCESTRY) instanceof Component)
                component = (Component) component.getClientProperty(CLIENT_PROP_DIALOGS_ANCESTRY);
            else
                component = component.getParent();

        }
        while (component != null);

        final ListIterator<Component> listIterator = path.listIterator(path.size());
        while (listIterator.hasPrevious())
        {
            final Component previous = listIterator.previous();
            if (previous.getClass().isAnnotationPresent(ApplicationFunction.class))
                return previous;
        }
        throw new IllegalArgumentException("Component path does not contain an application function.");
    }

    /**
     * Get a ZonedDateTime for comparison on membership dates
     *
     * @param zone the TimeZone
     *
     * @return the ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTimeForComparison(TimeZone zone)
    {
        ZonedDateTime dt = ZonedDateTime.now(zone.toZoneId());
        dt = dt.plus(1L, ChronoUnit.HOURS);
        dt = dt.truncatedTo(ChronoUnit.HOURS);
        return dt;
    }

    /**
     * Initialize hibernate entity. Useful for ValueEditors.
     *
     * @param value the value.
     */
    public static void initialize(@Nullable Membership value)
    {
        if (value == null) return;
        Hibernate.initialize(value);
        Hibernate.initialize(value.getMembershipType());
        initialize(value.getUser());
    }

    /**
     * Initialize hibernate entity. Useful for ValueEditors.
     *
     * @param value the value.
     */
    public static void initialize(User value)
    {
        Hibernate.initialize(value);
        EntityRetriever er = autowire(EntityRetriever.class, EntityRetriever.RESOURCE_NAME);
        Hibernate.initialize(er.reattachIfNecessary(value.getPrincipal()));
    }

    /**
     * Initialize.
     *
     * @param value the value
     */
    public static void initialize(Profile value)
    {
        Hibernate.initialize(value);
        Hibernate.initialize(value.getRepository());
        initialize(value.getProfileType());
    }

    /**
     * Initialize.
     *
     * @param value the value
     */
    public static void initialize(Company value)
    {
        initialize((Profile)value);
        Hibernate.initialize(value.getEmailLogo());
        Hibernate.initialize(value.getHostname());
        Hibernate.initialize(value.getImage());
        initialize(value.getPrimaryLocation());
        Hibernate.initialize(value.getProfileTerms());
        value.getLocations().forEach(AppUtil::initialize);
    }

    /**
     * Use ApplicationContextUtils to get an instance of the given Class with the given Resource Name
     *
     * @param <C> the Class
     * @param clazz the Class
     * @param resourceName the resource name
     *
     * @return the singleton
     */
    @SuppressWarnings("ConstantConditions")
    public static <C> C autowire(Class<C> clazz, @Nullable String resourceName)
    {
        if (StringFactory.isEmptyString(resourceName))
            return ApplicationContextUtils.getInstance().getContext().getBean(clazz);
        else
            return ApplicationContextUtils.getInstance().getContext().getBean(resourceName, clazz);
    }

    /**
     * Initialize hibernate entity. Useful for ValueEditors.
     *
     * @param value the value.
     */
    public static void initialize(@Nullable ProfileType value)
    {
        if(value == null) return;
        Hibernate.initialize(value);
        Hibernate.initialize(value.getMembershipTypeSet());
        value.getMembershipTypeSet().forEach(Hibernate::initialize);
    }

    /**
     * Check if the provided HTML content has anything visible to present to a user.
     *
     * @param markup the markup
     *
     * @return true if there is something to show
     */
    public static boolean isEmptyMarkup(String markup)
    {
        final Document document = Jsoup.parse(markup);
        return document.text().trim().isEmpty();
    }

    /**
     * Add a null element as the first element of the list.
     *
     * @param <T> the type.
     * @param list the list.
     *
     * @return the list.
     */
    public static <T> List<T> nullFirst(Collection<T> list)
    {
        ArrayList<T> tList = new ArrayList<>(list);
        return nullInIndex(0, tList);
    }

    /**
     * Add a null element as the element in the given index of the list.
     *
     * @param <T> the type.
     * @param index the index to add the null element in at
     * @param list the list
     *
     * @return the list
     */
    public static <T> List<T> nullInIndex(int index, List<T> list)
    {
        if (list.isEmpty() || list.get(0) != null)
            list.add(index, null);
        return list;
    }

    /**
     * Add a null element as the first element of the list.
     *
     * @param <T> the type.
     * @param values the values that will make up the list
     *
     * @return the list.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> nullFirst(@Nonnull T... values)
    {
        return nullInIndex(0, values);
    }

    /**
     * Add a null element as the element in the given index of the list.
     *
     * @param <T> the type.
     * @param values the values that will make up the list
     * @param index the index to add the null element in at
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> nullInIndex(int index, @Nonnull T... values)
    {
        return nullInIndex(index, new ArrayList<>(Arrays.asList(values)));
    }

    /**
     * Record which component is the ancestor (effectively) for a Dialog.
     * See {@link #getValidSourceComponentAcrossDialogs(Component)} for why you might want to record this.
     *
     * @param dlg the dialog
     * @param ancestor the ancestor
     */
    public static void recordDialogsAncestorComponent(Dialog dlg, Component ancestor)
    {
        dlg.putClientProperty(CLIENT_PROP_DIALOGS_ANCESTRY, ancestor);
    }

    /**
     * Render a user's info in a way that identifies their user account and who the person is
     *
     * @param u the user
     *
     * @return the rendered info
     */
    public static String renderUser(@Nonnull User u)
    {
        Name n = PrincipalContactUtil.getName(u.getPrincipal());
        if (n == null) return "User#" + u.getId();
        return n.getFirst() + ' ' + n.getLast() + " (" + u.getPrincipal().getPasswordCredentials().getUsername() + ')';
    }

    /**
     * Get the default TimeZone statically.
     * Uses application context utils to get LRLabsUtil and call {@link AppUtil#getDefaultTimeZone()}
     *
     * @return the default time zone.
     */
    public static TimeZone staticGetDefaultTimeZone()
    {
        @SuppressWarnings("ConstantConditions")
        AppUtil util = ApplicationContextUtils.getInstance().getContext().getBean(AppUtil.class);
        return util.getDefaultTimeZone();
    }

    /**
     * Get the default time zone.
     *
     * @return the default time zone.
     */
    public TimeZone getDefaultTimeZone()
    {
        return getSite().getDefaultTimeZone();
    }

    /**
     * Get the site.
     *
     * @return the site.
     */
    public CmsSite getSite()
    {
        final CmsSite site = _cmsFrontendDAO.getSite(_defaultEmailTemplateSite);
        assert site != null;
        return site;
    }

    /**
     * Convert the given ZonedDateTime to a Date
     *
     * @param dt the ZonedDateTime
     *
     * @return a Date object that represents the same instant as the ZonedDateTime, at the ZonedDateTime's timezone.
     */
    @Nullable
    public static Date toDate(@Nullable ZonedDateTime dt)
    {
        if (dt == null) return null;
        return new Date(dt.toInstant().toEpochMilli());
    }

    /**
     * Convert the given Date to a ZonedDateTime at the given TimeZone
     *
     * @param date the Date
     * @param zone the TimeZone to convert to
     *
     * @return a ZonedDateTime that represents the same instant as the Date, at the TimeZone specified.
     */
    @Nullable
    public static ZonedDateTime toZonedDateTime(@Nullable Date date, @Nullable TimeZone zone)
    {
        if (date == null || zone == null) return null;
        return ZonedDateTime.ofInstant(date.toInstant(), zone.toZoneId());
    }

    /**
     * Walk component tree.
     *
     * @param parent the parent
     * @param actionOnComponent the action to be performed on each component within the component tree
     */
    public static void walkComponentTree(ParentComponent parent, Consumer<Component> actionOnComponent)
    {
        parent.components().forEachRemaining(component -> {
            if (component instanceof ParentComponent)
            {
                actionOnComponent.accept(component);
                walkComponentTree((ParentComponent) component, actionOnComponent);
            }
            else
            {
                actionOnComponent.accept(component);
            }
        });
    }

    /**
     * Get the default image for the {@link Resource} editor.
     *
     * @return default image for the Resource Editor
     */
    public FactoryResource getDefaultResourceImage()
    {
        return _classPathResourceLibraryHelper.createResource("no-image-placeholder.png");
    }

    /**
     * Get the default User image for the {@link User} model and UIs.
     *
     * @return default User image Resource
     */
    public FactoryResource getDefaultUserImage()
    {
        return _classPathResourceLibraryHelper.createResource("default-profile-picture.png");
    }

    /**
     * Get the Role for Front End Access to the application
     *
     * @return the front end access role
     */
    public Role getFrontEndAccessRole()
    {
        return ofNullable(_roleDAO.getRoleByProgrammaticName(_frontEndRoleProgId))
            .orElseThrow(() -> new IllegalStateException(
                "Front End Role could not be foundfor programmatic id: " + _frontEndRoleProgId));
    }

    /**
     * Get the Role for Admin Access to the application
     *
     * @return the admin access role
     */
    public Role getAdminAccessRole()
    {
        return ofNullable(_roleDAO.getRoleByProgrammaticName(_adminRoleProgId))
            .orElseThrow(() -> new IllegalStateException(
                "Admin Role could not be found for programmatic id: " + _adminRoleProgId));
    }

    /**
     * User has admin role boolean.
     *
     * @param user the user
     *
     * @return the boolean
     */
    public static boolean userHasAdminRole(User user)
    {
        PrincipalDAO principalDAO = AppUtil.autowire(PrincipalDAO.class);
        AppUtil appUtil = AppUtil.autowire(AppUtil.class);
        EntityRetriever er = AppUtil.autowire(EntityRetriever.class, EntityRetriever.RESOURCE_NAME);

        return principalDAO.getAllRoles(er.reattachIfNecessary(user).getPrincipal()).contains(appUtil.getAdminAccessRole());
    }

    /**
     * User has admin role boolean.
     *
     * @param user the user
     *
     * @return the boolean
     */
    public static boolean userHasAdminRole(Principal user)
    {
        PrincipalDAO principalDAO = AppUtil.autowire(PrincipalDAO.class);
        AppUtil appUtil = AppUtil.autowire(AppUtil.class);
        EntityRetriever er = AppUtil.autowire(EntityRetriever.class, EntityRetriever.RESOURCE_NAME);

        return principalDAO.getAllRoles(er.reattachIfNecessary(user)).contains(appUtil.getAdminAccessRole());
    }

    /**
     * Get a new or existing Token for the given Principal
     *
     * @param principal the Principal
     * @param suffix the token suffix
     *
     * @return token
     */
    public TokenCredentials getTokenForPrincipal(Principal principal, String suffix)
    {
        final TokenCredentials token = _principalDAO.getTokenCredentials(principal, suffix);
        if (token.getExpireDate() == null)
        {
            token.setExpireDate(AppUtil.getNewTokenExpireTime());
            _principalDAO.saveCredentials(token);
        }
        return token;
    }

    /**
     * Get an expire time for a new token based off the current date
     *
     * @return token expire time
     */
    public static Date getNewTokenExpireTime()
    {
        return new Date(Instant.now().plus(Duration.ofHours(80)).toEpochMilli());
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(AppUtil.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}
