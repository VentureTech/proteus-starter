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
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.i2rd.media.ICodec;
import com.i2rd.media.IMediaMetaData;
import com.i2rd.media.IMediaStream;
import com.i2rd.media.IMediaUtility;
import com.i2rd.media.MediaUtilityFactory;

import net.proteusframework.cms.component.generator.XMLRenderer;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.metric.PixelMetric;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.HTMLComponent;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.Media;
import net.proteusframework.ui.miwt.component.URILink;
import net.proteusframework.ui.miwt.data.MediaSource;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Name;

import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * UI view for UserProfile.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Configurable
public class UserProfileViewer extends Container
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(UserProfileViewer.class);
    /** Profile. */
    private final UserProfile _userProfile;

    /** DAO. */
    @Autowired
    private UserProfileDAO _userProfileDAO;

    /**
     * Get the about me.
     *
     * @param videoLink the video link.
     * @param videoLinkURI the video link uRI.
     * @param videoLinkComponent the video link component.
     * @return the about me component or null.
     */
    @Nullable
    static Component getAboutMe(URL videoLink, URI videoLinkURI, URILink videoLinkComponent)
    {
        Component aboutMeVideo = null;
        IMediaUtility util = MediaUtilityFactory.getUtility();
        try
        {
            // Check if we can parse the media and it has a stream we like.
            /// In our made up example, we're only accepting H.264 video. We don't care about the audio in this example.
            IMediaMetaData mmd;
            if (util.isEnabled()
                && videoLinkURI != null
                && (mmd = util.getMetaData(videoLinkURI.toString())).getStreams().length > 0)
            {
                int width = 853, height = 480; // 480p default
                boolean hasVideo = false;
                for (IMediaStream stream : mmd.getStreams())
                {
                    if (stream.getCodec().getType() == ICodec.Type.video
                        && "H264".equals(stream.getCodec().name()))
                    {
                        hasVideo = true;
                        if (stream.getWidth() > 0)
                        {
                            width = stream.getWidth();
                            height = stream.getHeight();
                        }
                        break;
                    }
                }
                if (hasVideo)
                {
                    Media component = new Media();
                    component.setMediaType(Media.MediaType.video);
                    component.addSource(new MediaSource(videoLinkURI));
                    component.setFallbackContent(videoLinkComponent);
                    component.setSize(new PixelMetric(width), new PixelMetric(height));
                    aboutMeVideo = component;
                }
            }
        }
        catch (IllegalArgumentException | RemoteException e)
        {
            _logger.error("Unable to get media information for " + videoLink, e);
        }
        return aboutMeVideo;
    }

    /**
     * Create a new viewer.
     *
     * @param profile the profile.
     */
    public UserProfileViewer(UserProfile profile)
    {
        super();
        Preconditions.checkNotNull(profile);
        _userProfile = profile;
    }

    @Override
    public void init()
    {
        // Make sure you call super.init() at the top of this method.
        /// See the Javadoc for #init() for more information about what it does.
        super.init();

        // Set HTML element type and class names for presentation use on this Container component.
        withHTMLElement(HTMLElement.section);
        addClassName("user-profile-viewer");
        // property_viewer is a standard class name.
        addClassName("property-viewer");
        // Add microdata for programmatic / SEO use
        /// OR use RDFa support
        /// You typically only do this in viewers - not editors.
        setAttribute("itemscope", "");
        setAttribute("itemtype", "http://schema.org/Person");
        // setAttribute allows you to set any attribute as long as it will not interfere with a component's
        /// native HTML. For example, you cannot set the "value" attribute on a Field since
        /// it uses that attribute.

        // It's a good idea to *not* mark variables final that you don't want in the scope of event listeners.
        /// Hibernate/JPA entities are a great example of this pattern. You always need to re-attach
        /// entities before using them, so we should always call getUserProfile() in the context
        /// of handling an event. Note: our getUserProfile() method re-attaches the entity.
        UserProfile userProfile = getUserProfile();

        Name name = userProfile.getName();
        // You can use a Field for displaying non-internationalized content.
        /// It is desirable to do this since you don't need to create a LocalizedText.
        /// However, you cannot change the HTMLElement of a Field at this time,
        /// so some of the following code uses a Label which does allow
        /// specification of the HTMLElement.
        final Field namePrefix = new Field(name.getFormOfAddress(), false);
        final Field nameGiven = new Field(name.getFirst(), false);
        final Field nameFamily = new Field(name.getLast(), false);
        final Field nameSuffix = new Field(name.getSuffix(), false);
        // Sometimes it is easier and less error prone to make a component non-visible
        /// than checking for null on each use. Use this pattern with care. You don't
        /// want to consume a lot of resource unnecessarily.
        if (isEmptyString(namePrefix.getText())) namePrefix.setVisible(false);
        if (isEmptyString(nameSuffix.getText())) nameSuffix.setVisible(false);

        // Address
        Address address = userProfile.getPostalAddress();
        // Address lines are always on their own line so we make sure they are enclosed by a block element like a DIV..
        final Label addressLine1 = new Label();
        addressLine1.withHTMLElement(HTMLElement.div).addClassName("prop").addClassName("address-line");
        final Label addressLine2 = new Label();
        addressLine2.withHTMLElement(HTMLElement.div).addClassName("prop").addClassName("address-line");
        if (address.getAddressLines().length > 0) addressLine1.setText(createText(address.getAddressLines()[0]));
        if (address.getAddressLines().length > 1) addressLine2.setText(createText(address.getAddressLines()[1]));
        final HTMLComponent city = new HTMLComponent();
        // The "prop" class name is part of the standard HTML structure. It is always a good idea to also
        /// add a specific class name like "city" in this example. Please be consistent when using class names.
        /// For example, if everyone else is using "city", please use "city" too. Don't come up with another class name
        /// that means something similar like "town" or "locality". Consistency has a big impact on
        /// the time required to style HTML as well as the ability to reuse CSS.
        city.withHTMLElement(HTMLElement.span).addClassName("prop").addClassName("city");
        if (!isEmptyString(address.getCity()))
        {
            // Our microdata for the city shouldn't include the comma, so this is a bit more complicated than the other examples.
            city.setText(createText("<span itemprop=\"addressLocality\">" + address.getCity()
                + "</span><span class=\"delimiter\">,</span>"));
        }
        else city.setVisible(false);
        final Label state = new Label(createText(address.getState()));
        state.addClassName("prop").addClassName("state");
        final Label postalCode = new Label(createText(address.getPostalCode()));
        postalCode.addClassName("prop").addClassName("postal_code");

        // Other Contact
        final Field phoneNumber = new Field(userProfile.getPhoneNumber(), false);
        final Field emailAddress = new Field(userProfile.getEmailAddress(), false);

        // Social Contact
        final URILink twitterLink = userProfile.getTwitterLink() != null
            ? new URILink(_userProfileDAO.toURI(userProfile.getTwitterLink(), null)) : null;
        final URILink facebookLink = userProfile.getFacebookLink() != null
            ? new URILink(_userProfileDAO.toURI(userProfile.getFacebookLink(), null)) : null;
        final URILink linkedInLink = userProfile.getLinkedInLink() != null
            ? new URILink(_userProfileDAO.toURI(userProfile.getLinkedInLink(), null)) : null;

        // We are going to output HTML received from the outside, so we need to sanitize it first for security reasons.
        /// Sometimes you'll do this sanitation prior to persisting the data. It depends on whether or not you need to
        /// keep the original unsanitized HTML around.
        String processedHTML = userProfile.getAboutMeProse();
        if (!isEmptyString(processedHTML))
        {
            // Process the HTML converting links as necessary (adding JSESSIONID(s)
            /// for URL based session tracking, converting resource links to increase concurrent loading limit,
            /// CMS link externalization, etc).
            /// This is *not* sanitation and should always be done before rendering - never before persisting.
            /// We are doing this before sanitizing the HTML to avoid having to whitelist internal URL protocols, etc.
            try
            {
                processedHTML = XMLRenderer.parseWithRoot(processedHTML, Event.getRequest(), Event.getResponse());
            }
            catch (IOException e)
            {
                _logger.error("Unable to accept HTML: " + processedHTML, e);
            }

            // We don't trust the input, so we sanitize it with a whitelist of allowed HTML.
            Document dirty = Jsoup.parseBodyFragment(processedHTML, "");
            Whitelist whitelist = Whitelist.relaxed();
            // Don't allow users to use our website as a link farm
            whitelist.addEnforcedAttribute("a", "rel", "nofollow");
            Cleaner cleaner = new Cleaner(whitelist);
            Document clean = cleaner.clean(dirty);
            processedHTML = clean.html();
        }
        final HTMLComponent aboutMeProse = new HTMLComponent(processedHTML);
        Component aboutMeVideo = null;
        URL videoLink = userProfile.getAboutMeVideoLink();
        if (videoLink != null)
        {
            // There are several ways to link to media (Youtube video URL, Vimeo video URL, Flickr URL,
            // internally hosted media file, etc).
            /// You can link to it.
            /// You can embed it. See http://oembed.com/ for a common protocol for doing this.
            /// If the link is to the media itself, you can create a player for it.
            /// Below is an example of creating a link to the video as well as a player.
            final URI videoLinkURI = _userProfileDAO.toURI(videoLink, null);
            URILink videoLinkComponent = new URILink(videoLinkURI, createText("My Video"));
            videoLinkComponent.setTarget("_blank");
            aboutMeVideo = getAboutMe(videoLink, videoLinkURI, videoLinkComponent);
            if (aboutMeVideo == null)
            {
                // We could check for oEmbed support in case link was to youtube, vimeo, etc - http://oembed.com/
                // Since this is an example, we'll just output the link.
                aboutMeVideo = videoLinkComponent;
            }
        }
        ImageComponent picture = null;
        final FileEntity userProfilePicture = userProfile.getPicture();
        if (userProfilePicture != null)
        {
            picture = new ImageComponent(new Image(userProfilePicture));
            picture.setImageCaching(userProfilePicture.getLastModifiedTime().before(
                new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60))
            ));
        }

        // Now that we've initialized most of the content, we'll add all the components to this View
        /// using the standard HTML structure for a property viewer.
        add(of(HTMLElement.section,
            "prop-group name",
            new Label(createText("Name")).withHTMLElement(HTMLElement.h1),
            namePrefix.setAttribute("itemprop", "honorificPrefix")
                .addClassName("prop").addClassName("prefix"),
            nameGiven.setAttribute("itemprop", "givenName")
                .addClassName("prop").addClassName("given"),
            nameFamily.setAttribute("itemprop", "familyName")
                .addClassName("prop").addClassName("family"),
            nameSuffix.setAttribute("itemprop", "honorificSuffix")
                .addClassName("prop").addClassName("suffix")
        ));

        // Add wrapping DIV to group address lines if necessary.
        Component streetAddress = (!isEmptyString(addressLine1.getText()) && !isEmptyString(addressLine2.getText())
            ? of(HTMLElement.div, "address-lines", addressLine1, addressLine2)
            : (isEmptyString(addressLine1.getText()) ? addressLine2 : addressLine1).withHTMLElement(HTMLElement.div));
        streetAddress.setAttribute("itemprop", "streetAddress");
        boolean hasAddress = (!isEmptyString(addressLine1.getText())
            || !isEmptyString(addressLine2.getText())
            || !isEmptyString(city.getText())
            || !isEmptyString(state.getText())
            || !isEmptyString(postalCode.getText())
        );
        boolean hasPhone = !isEmptyString(phoneNumber.getText());
        boolean hasEmail = !isEmptyString(emailAddress.getText());
        // We only want to output the enclosing HTML if we have content to display.
        if (hasAddress || hasPhone || hasEmail)
        {
            Container contactContainer = of(HTMLElement.section,
                "contact",
                new Label(createText("Contact Information")).withHTMLElement(HTMLElement.h1)
            );
            add(contactContainer);
            if (hasAddress)
            {
                contactContainer.add(of(HTMLElement.div,
                        "prop-group address",
                        // We are using an H2 here because are immediate ancestor is a DIV. If it was a SECTION,
                        /// then we would use an H1. See the UserProfileViewer for a comparison.
                        new Label(createText("Address")).withHTMLElement(HTMLElement.h2),
                        streetAddress,
                        of(HTMLElement.div, "place",
                            city,
                            state.setAttribute("itemprop", "addressRegion"),
                            postalCode.setAttribute("itemprop", "postalCode"))
                    ).setAttribute("itemprop", "address")
                        .setAttribute("itemscope", "")
                        .setAttribute("itemtype", "http://schema.org/PostalAddress")
                );
            }
            if (hasPhone)
            {
                contactContainer.add(of(HTMLElement.div,
                        "prop phone",
                        new Label(createText("Phone")).withHTMLElement(HTMLElement.h2),
                        phoneNumber.setAttribute("itemprop", "telephone")
                    )
                );
            }
            if (hasEmail)
            {
                contactContainer.add(of(HTMLElement.div,
                        "prop email",
                        new Label(createText("Email")).withHTMLElement(HTMLElement.h2),
                        emailAddress.setAttribute("itemprop", "email")
                    )
                );
            }
        }


        if (twitterLink != null || facebookLink != null || linkedInLink != null)
        {
            Container social = of(
                HTMLElement.section,
                "social",
                new Label(createText("Social Media Links")).withHTMLElement(HTMLElement.h1)
            );
            add(social);
            if (twitterLink != null)
            {
                twitterLink.setTarget("_blank");
                twitterLink.setText(createText("Twitter Link"));
                social.add(of(
                    HTMLElement.div,
                    "prop twitter",
                    createText("Twitter"),
                    twitterLink
                ));
            }
            if (facebookLink != null)
            {
                facebookLink.setTarget("_blank");
                facebookLink.setText(createText("Facebook Link"));
                social.add(of(
                    HTMLElement.div,
                    "prop facebook",
                    createText("Facebook"),
                    facebookLink
                ));
            }
            if (linkedInLink != null)
            {
                linkedInLink.setTarget("_blank");
                linkedInLink.setText(createText("LinkedIn Link"));
                social.add(of(
                    HTMLElement.div,
                    "prop linkedin",
                    createText("LinkedIn"),
                    linkedInLink
                ));
            }
        }

        final boolean hasAboutMeProse = isEmptyString(aboutMeProse.getText());
        if (!hasAboutMeProse || aboutMeVideo != null)
        {
            Container aboutMe = of(
                HTMLElement.section,
                "about-me",
                new Label(createText("About Me")).withHTMLElement(HTMLElement.h1)
            );
            add(aboutMe);
            if (picture != null)
            {
                aboutMe.add(of(
                    HTMLElement.div,
                    "prop picture",
                    createText("Picture"),
                    picture
                ));
            }
            if (hasAboutMeProse)
            {
                aboutMe.add(of(
                    HTMLElement.div,
                    "prop prose",
                    createText("Professional Information, Hobbies, Interests..."),
                    aboutMeProse
                ));
            }
            if (aboutMeVideo != null)
            {
                Label label = new Label(createText("Video")).withHTMLElement(HTMLElement.label);
                label.addClassName("vl");
                aboutMe.add(of(
                    HTMLElement.div,
                    "prop video",
                    label,
                    aboutMeVideo
                ));
            }

        }
    }

    /**
     * Get the user profile attached to the Hibernate Session or EntityManager.
     *
     * @return the attached profile.
     */
    UserProfile getUserProfile()
    {
        // Since we aren't actually persisting anything, this doesn't do anything other than return _userProfile
        /// It's just meant to demonstrate how to do it when you are using entities persisted in data store.
        return EntityRetriever.getInstance().reattachIfNecessary(_userProfile);
    }
}
