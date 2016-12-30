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

import com.example.app.model.client.Location;
import com.example.app.model.company.Company;
import com.example.app.support.AppUtil;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Optional;

import com.i2rd.hr.miwt.AddressCellRenderer;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.metric.PixelMetric;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.ui.company.CompanyValueEditorLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Value Viewer for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/28/16 10:15 AM
 */
@Configurable
public class CompanyValueViewer extends Container
{
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private AppUtil _labsUtil;

    private Company _company;

    /**
     * Instantiates a new company value viewer.
     * <br>
     *     NOTE: Must set Company with {@link #setCompany(Company)} before initialization.
     */
    public CompanyValueViewer()
    {
        super();
    }

    /**
     * Gets company.
     *
     * @return the company
     */
    public Company getCompany()
    {
        Preconditions.checkNotNull(_company, "Company has not been set, so it cannot be retrieved.");
        return _er.reattachIfNecessary(_company);
    }

    /**
     * Sets company.
     *
     * @param company the company
     */
    public void setCompany(Company company)
    {
        _company = company;

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

        final ImageComponent logoField = new ImageComponent(getCompany().getImage() != null
            ? new Image(getCompany().getImage())
            : new Image(_labsUtil.getDefaultResourceImage()));
        logoField.setWidth(new PixelMetric(200));
        final ImageComponent emailLogoField = new ImageComponent(getCompany().getEmailLogo() != null
            ? new Image(getCompany().getEmailLogo())
            : new Image(_labsUtil.getDefaultResourceImage()));
        emailLogoField.setWidth(new PixelMetric(200));

        final Container nameCon = of("prop name", CommonColumnText.NAME, new Label(getCompany().getName()));

        final Address address = Optional.ofNullable(getCompany().getPrimaryLocation())
            .map(Location::getAddress)
            .orElse(new Address());
        final Container addressCon = of("prop address", CommonColumnText.ADDRESS, new AddressCellRenderer(address));

        final String phoneNumber = Optional.ofNullable(getCompany().getPrimaryLocation())
            .map(Location::getPhoneNumber)
            .map(PhoneNumber::toExternalForm).orElse("");
        final Container phoneCon = of("prop phone", CommonColumnText.PHONE, new Label(createText(phoneNumber)));

        final Container websiteCon = of("prop website", LABEL_WEBSITE(), new Label(createText(getCompany().getWebsiteLink())));

        final Container hostnameCon = of("prop hostname", LABEL_SUB_DOMAIN(),
            new Label(createText(getCompany().getHostname().getName())));

        final Container linkedInCon = of("prop linkedIn", LABEL_LINKEDIN(), new Label(createText(getCompany().getLinkedInLink())));

        final Container twitterCon = of("prop twitter", LABEL_TWITTER(), new Label(createText(getCompany().getTwitterLink())));

        final Container facebookCon = of("prop facebook", LABEL_FACEBOOK(), new Label(createText(getCompany().getFacebookLink())));

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
