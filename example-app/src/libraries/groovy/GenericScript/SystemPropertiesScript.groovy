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

import static net.proteusframework.core.locale.TextSources.createText

new Testable() {
    @Override
    boolean testSelf(List<Notification> messages)
    {

        System.getProperties().each {k,v ->
            NotificationImpl ni = new NotificationImpl(NotificationType.INFO, createText("$k = $v"))
            messages.add(ni)
        }

        return true
    }
}