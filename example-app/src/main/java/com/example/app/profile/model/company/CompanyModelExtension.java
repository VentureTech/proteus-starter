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

package com.example.app.profile.model.company;

import com.example.app.profile.model.location.Location;
import com.example.app.profile.service.DefaultProfileTermProvider;
import com.example.app.support.service.AppUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.i2rd.domainmodel.context.DomainModelContext;
import com.i2rd.domainmodel.extension.impl.AbstractModelExtension;
import com.i2rd.domainmodel.model.DomainData;
import com.i2rd.domainmodel.model.DomainModel;
import com.i2rd.domainmodel.model.DomainModelBuilder;
import com.i2rd.domainmodel.resolver.DataTypeResolver;
import com.i2rd.domainmodel.resolver.DomainModelResolver;
import com.i2rd.domainmodel.resolver.PropertyPathResolver;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.metric.Dimension;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.http.FileEntityImageResourceFactory;
import net.proteusframework.data.filesystem.http.FileSystemEntityResourceFactory;
import net.proteusframework.data.http.URLGenerator;
import net.proteusframework.internet.http.resource.FactoryResourceConfiguration;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.profile.model.company.CompanyModelExtensionLOK.*;
import static net.proteusframework.cms.support.ImageFileUtil.getDimension;
import static net.proteusframework.cms.support.ImageFileUtil.getScaledDimension;

/**
 * Extension for Company.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Component
@Lazy
@I18NFile(
    symbolPrefix = "com.example.app.profile.model.company.CompanyModelExtension",
    i18n = {
        @I18N(symbol = "Email Address Usage Instructions", l10n = @L10N("{0}'s Email Address")),
        @I18N(symbol = "Phone Number Usage Instructions", l10n = @L10N("{0}'s Phone Number")),
        @I18N(symbol = "Image Usage Instructions", l10n = @L10N("Image")),
        @I18N(symbol = "Instructions Name", l10n = @L10N("{0} Name")),
        @I18N(symbol = "Linked-In Usage Instructions", l10n = @L10N("LinkedIn URL")),
        @I18N(symbol = "Twitter Usage Instructions", l10n = @L10N("Twitter URL")),
        @I18N(symbol = "Facebook Usage Instructions", l10n = @L10N("Facebook URL")),
        @I18N(symbol = "Google-Plus Usage Instructions", l10n = @L10N("Google Plus URL"))
    }
)
public class CompanyModelExtension extends AbstractModelExtension
{
    private static final Logger _logger = LogManager.getLogger(CompanyModelExtension.class);

    private static final long serialVersionUID = 1288648882847324543L;

    @Autowired
    private DefaultProfileTermProvider _terms;

    private final DataTypeResolver _companyResolver = new DataTypeResolver(Company.class);
    @Autowired
    private AppUtil _appUtil;

    /**
     * Image Parameter Object.
     */
    @Configurable
    public static class ImageParams
    {
        @Autowired
        private AppUtil _appUtil;
        @Autowired
        private FileSystemEntityResourceFactory _fileResourceFactory;
        @Autowired
        private FileEntityImageResourceFactory _imageResourceFactory;
        @Autowired
        private URLGenerator _urlGenerator;

        private final String _domainModelName;
        private final DomainModelBuilder<Object> _dmb;
        private final DomainModelResolver _imageResolver;
        @Nullable
        private final Dimension _dimension;

        /**
         * Instantiates a new Image params.
         *
         * @param dmb the dmb
         * @param domainModelName name.
         * @param imageResolver the image resolver
         * @param dimension the dimension.
         */
        public ImageParams(DomainModelBuilder<Object> dmb, String domainModelName, DomainModelResolver imageResolver,
            @Nullable Dimension dimension)
        {
            _dmb = dmb;
            _domainModelName = domainModelName;
            _imageResolver = imageResolver;
            _dimension = dimension;
        }

        /**
         * Get the app util.
         *
         * @return the app util
         */
        public AppUtil getAppUtil()
        {
            return _appUtil;
        }

        /**
         * Get the dimension.
         *
         * @return the dimension
         */
        @Nullable
        public Dimension getDimension()
        {
            return _dimension;
        }

        /**
         * Get the dmb.
         *
         * @return the dmb
         */
        public DomainModelBuilder<Object> getDmb()
        {
            return _dmb;
        }

        /**
         * Gets domain model name.
         *
         * @return the domain model name
         */
        public String getDomainModelName()
        {
            return _domainModelName;
        }

        /**
         * Get the file resource factory.
         *
         * @return the file resource factory
         */
        public FileSystemEntityResourceFactory getFileResourceFactory()
        {
            return _fileResourceFactory;
        }

        /**
         * Get the image resolver.
         *
         * @return the image resolver
         */
        public DomainModelResolver getImageResolver()
        {
            return _imageResolver;
        }

        /**
         * Get the image resource factory.
         *
         * @return the image resource factory
         */
        public FileEntityImageResourceFactory getImageResourceFactory()
        {
            return _imageResourceFactory;
        }

        /**
         * Get the url generator.
         *
         * @return the url generator
         */
        public URLGenerator getUrlGenerator()
        {
            return _urlGenerator;
        }
    }

    /**
     * Instantiates a new company model extension.
     */
    public CompanyModelExtension()
    {
        super();
        _companyResolver.setCache(true);
        addSupportedExtensionPoint(Company.class);
    }

    @Override
    public List<DomainModel<?>> getDomainModels(DomainModelContext context, DomainModel<?> extendedModel)
    {
        DomainModelBuilder<Object> dmb = new DomainModelBuilder<>();
        dmb.setAutoAddExtensionPoints(true);

        dmb.addChild("name", String.class)
            .setUsageInstructions(INSTRUCTIONS_NAME(_terms.company()))
            .addDataResolver(String.class, new PropertyPathResolver(_companyResolver,
                Company.NAME_COLUMN_PROP + ".getText[lc].toString")
                .addFixedArg("lc", context.getLocaleContext()))
        ;

        dmb.addChild("description", String.class)
            .setUsageInstructions(INSTRUCTIONS_NAME(_terms.company()))
            .addDataResolver(String.class, new PropertyPathResolver(_companyResolver,
                Company.NAME_COLUMN_PROP + ".getText[lc].toString")
                .addFixedArg("lc", context.getLocaleContext()))
        ;

        dmb.addChild("email", String.class)
            .setUsageInstructions(EMAIL_ADDRESS_USAGE_INSTRUCTIONS(_terms.company()))
            .addDataResolver(String.class, (ctx, dm, args) -> _appUtil.getSystemSender())
        ;
        dmb.addChild("phone", String.class)
            .setUsageInstructions(PHONE_NUMBER_USAGE_INSTRUCTIONS(_terms.company()))
            .addDataResolver(String.class, (ctx, dm, args) -> {
                final Company company = getCoachingEntity(ctx, dm, args);
                return Optional.ofNullable(company.getPrimaryLocation())
                    .map(Location::getPhoneNumber)
                    .map(PhoneNumber::toExternalForm).orElse("");
            })
        ;

        addSocialMediaModels(dmb);

        addImage(new ImageParams(dmb,"image",
            new PropertyPathResolver(_companyResolver, Company.IMAGE_PROP), null));
        addImage(new ImageParams(dmb, "email_logo",
            new PropertyPathResolver(_companyResolver, Company.EMAIL_LOGO_PROP), null));

        return dmb.getRootChildren();
    }

    private void addSocialMediaModels(DomainModelBuilder<Object> dmb)
    {
        dmb.addChild("linkedin", String.class)
            .setUsageInstructions(LINKED_IN_USAGE_INSTRUCTIONS())
            .addDataResolver(String.class,
                new PropertyPathResolver(_companyResolver, Company.LINKEDIN_LINK_COLUMN_PROP)
                .setDefault(""))
        ;
        dmb.addChild("twitter", String.class)
            .setUsageInstructions(TWITTER_USAGE_INSTRUCTIONS())
            .addDataResolver(String.class,
                new PropertyPathResolver(_companyResolver, Company.TWITTER_LINK_COLUMN_PROP)
                .setDefault(""))
        ;
        dmb.addChild("facebook", String.class)
            .setUsageInstructions(FACEBOOK_USAGE_INSTRUCTIONS())
            .addDataResolver(String.class,
                new PropertyPathResolver(_companyResolver, Company.FACEBOOK_LINK_COLUMN_PROP)
                .setDefault(""))
        ;
        dmb.addChild("google_plus", String.class)
            .setUsageInstructions(GOOGLE_PLUS_USAGE_INSTRUCTIONS())
            .addDataResolver(String.class,
                new PropertyPathResolver(_companyResolver, Company.GOOGLEPLUS_LINK_COLUMN_PROP)
                .setDefault(""))
        ;
    }

    /**
     * Add image.
     *
     * @param imageParams the image params.
     */
    public static void addImage(ImageParams imageParams)
    {
        EntityRetriever er = EntityRetriever.getInstance();
        imageParams.getDmb().addChild(imageParams.getDomainModelName(), String.class)
            .setUsageInstructions(IMAGE_USAGE_INSTRUCTIONS())
            .addDataResolver(String.class, (domainModelContext, domainModel, arguments) -> {
                final FileEntity image = er.reattachIfNecessary((FileEntity)imageParams.getImageResolver()
                    .resolve(domainModelContext, domainModel, arguments));
                FactoryResourceConfiguration config = null;
                if (image != null && !image.isEntityTrashed())
                {
                    try
                    {
                        if (imageParams.getDimension() != null)
                        {
                            int width = imageParams.getDimension().getWidthMetric().intValue();
                            int height = imageParams.getDimension().getHeightMetric().intValue();
                            final Dimension scaledDimension = getScaledDimension(getDimension(image), width, height);
                            final String resourceId = imageParams.getImageResourceFactory().getPersistentResourceId(image,
                                scaledDimension.getWidthMetric().intValue(),
                                scaledDimension.getHeightMetric().intValue());
                            config =
                                new FactoryResourceConfiguration(imageParams.getImageResourceFactory().getFactoryId(), resourceId);
                        }
                    }
                    catch (IOException ioe)
                    {
                        _logger.debug("Unable to get scaled image.", ioe);
                    }
                    if (config == null)
                    {
                        config = new FactoryResourceConfiguration(
                            imageParams.getFileResourceFactory().getFactoryId(),
                            imageParams.getFileResourceFactory().getPersistentResourceId(image)
                        );
                    }
                    config.setExpireInterval(4, ChronoUnit.HOURS);
                }
                else
                {
                    config = new FactoryResourceConfiguration(imageParams.getAppUtil().getDefaultUserImage());
                    config.setExpireInterval(30, ChronoUnit.DAYS);
                }
                imageParams.getUrlGenerator().setHostname(domainModelContext.getSite().getDefaultHostname());
                return imageParams.getUrlGenerator().createURL(config).getLink().getURIAsString();
            })
        ;
    }

    @Override
    public String getModelName()
    {
        return "coachingModelExtension";
    }

    @Override
    public LocalizedText getName(LocaleContext context)
    {
        return new LocalizedText(_terms.company().getText(context).toString());
    }

    @Nullable
    @Override
    public LocalizedText getDescription(LocaleContext context)
    {
        return getName(context);
    }

    @Nonnull
    private Company getCoachingEntity(DomainModelContext domainModelContext, DomainModel<?> domainModel,
        List<DomainData<?>> arguments)
    {
        final Company company =
            (Company) _companyResolver.resolve(domainModelContext, domainModel, arguments);
        assert company != null;
        return EntityRetriever.getInstance().reattachIfNecessary(company);
    }

}
