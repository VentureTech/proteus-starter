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

package com.example.app.ui.company;

import com.example.app.model.company.Company;
import com.example.app.support.AppUtil;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.i2rd.hr.miwt.AddressCellRenderer;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.metric.PixelMetric;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.support.ContactUtil.getAddress;
import static com.example.app.support.ContactUtil.getPhoneNumber;
import static com.example.app.ui.company.CoachingEntityValueEditorLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Value Viewer for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/28/16 10:15 AM
 */
@Configurable
public class CoachingEntityValueViewer extends Container
{
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private AppUtil _labsUtil;

    private Company _coaching;

    /**
     * Instantiates a new company value viewer.
     * <br>
     *     NOTE: Must set Company with {@link #setCoaching(Company)} before initialization.
     */
    public CoachingEntityValueViewer()
    {
        super();
    }

    /**
     * Gets coaching.
     *
     * @return the coaching
     */
    public Company getCoaching()
    {
        Preconditions.checkNotNull(_coaching, "Company has not been set, so it cannot be retrieved.");
        return _er.reattachIfNecessary(_coaching);
    }

    /**
     * Sets coaching.
     *
     * @param coaching the coaching
     */
    public void setCoaching(Company coaching)
    {
        _coaching = coaching;

        if(isInited())
        {
            setupUI();
        }
    }

    @Override
    public void init()
    {
        super.init();

        setupUI();
    }

    private void setupUI()
    {
        removeAllComponents();

        final ImageComponent logoField = new ImageComponent(getCoaching().getImage() != null
            ? new Image(getCoaching().getImage())
            : new Image(_labsUtil.getDefaultResourceImage()));
        logoField.setWidth(new PixelMetric(200));
        final ImageComponent emailLogoField = new ImageComponent(getCoaching().getEmailLogo() != null
            ? new Image(getCoaching().getEmailLogo())
            : new Image(_labsUtil.getDefaultResourceImage()));
        emailLogoField.setWidth(new PixelMetric(200));

        final Container nameCon = of("prop name", CommonColumnText.NAME, new Label(getCoaching().getName()));

        final Address address = getAddress(getCoaching().getContact(), ContactDataCategory.values())
            .orElse(new Address());
        final Container addressCon = of("prop address", CommonColumnText.ADDRESS, new AddressCellRenderer(address));

        final Container phoneCon = of("prop phone", CommonColumnText.PHONE, new Label(createText( getPhoneNumber(
            getCoaching().getContact(), ContactDataCategory.values()).map(PhoneNumber::toExternalForm).orElse(""))));

        final Container websiteCon = of("prop website", LABEL_WEBSITE(), new Label(createText(getCoaching().getWebsiteLink())));

        final Container hostnameCon = of("prop hostname", LABEL_SUB_DOMAIN(),
            new Label(createText(getCoaching().getHostname().getName())));

        final Container linkedInCon = of("prop linkedIn", LABEL_LINKEDIN(), new Label(createText(getCoaching().getLinkedInLink())));

        final Container twitterCon = of("prop twitter", LABEL_TWITTER(), new Label(createText(getCoaching().getTwitterLink())));

        final Container facebookCon = of("prop facebook", LABEL_FACEBOOK(), new Label(createText(getCoaching().getFacebookLink())));

        add(of("logos",
            of("prop", LABEL_WEB_LOGO(), logoField),
            of("prop", LABEL_EMAIL_LOGO(), emailLogoField)));
        add(nameCon);
        add(addressCon);
        add(phoneCon);
        add(websiteCon);
        add(hostnameCon);
        add(linkedInCon);
        add(twitterCon);
        add(facebookCon);
    }
}
