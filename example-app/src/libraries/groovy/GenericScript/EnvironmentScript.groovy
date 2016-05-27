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

package GenericScript

import com.i2rd.lib.Testable
import net.proteusframework.core.notification.Notification
import net.proteusframework.core.notification.NotificationImpl
import net.proteusframework.core.notification.NotificationType
import net.proteusframework.core.spring.ApplicationContextUtils
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.core.env.CompositePropertySource
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.PropertySource

import static net.proteusframework.core.locale.TextSources.createText
import static net.proteusframework.core.locale.TextSources.createTextForAny

AbstractApplicationContext context = ApplicationContextUtils.instance.context
ConfigurableEnvironment senv = context.getBean(ConfigurableEnvironment.class)

def renderMap(List<Notification> messages, PropertySource ps)
{
    if(ps instanceof EnumerablePropertySource)
    {
        EnumerablePropertySource eps = ps as EnumerablePropertySource;
        eps.propertyNames.each {k ->
            def v = eps.getProperty(k)
            messages.add(new NotificationImpl(NotificationType.INFO, createText("$k = $v")))
        }
    }
    else
    {
        ps.getProperties().each {k, v ->
            messages.add(new NotificationImpl(NotificationType.INFO, createText("$k = $v")))
        }
    }
}

new Testable() {
    @Override
    boolean testSelf(List<Notification> messages)
    {
        senv.getPropertySources().iterator().each {ps ->
            messages.add(new NotificationImpl(NotificationType.SUCCESS, createTextForAny(ps)))
            if(ps instanceof CompositePropertySource)
            {
                def cps = ps as CompositePropertySource
                cps.propertySources.each {PropertySource cpsp ->
                    messages.add(new NotificationImpl(NotificationType.SUCCESS, createTextForAny(cpsp)))
                    renderMap(messages, cpsp)
                }

            }
            else
            {
                renderMap(messages, ps)
            }
        }

        return true
    }
}