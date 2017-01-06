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

package experimental.cms.dsl.shell

import com.i2rd.cms.util.AbstractShellCommands
import experimental.cms.dsl.AppDefinition
import net.proteusframework.core.hibernate.HibernateSessionHelper
import net.proteusframework.core.lang.InternationalizedException
import net.proteusframework.core.text.FriendlyDateFormat
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import java.util.Date
import javax.annotation.PostConstruct

open class CmsDSLShellCommands : AbstractShellCommands() {


    @Autowired(required = false)
    lateinit var _appDefinitionList: List<AppDefinition>
    @Autowired
    lateinit var modelApplication: CmsModelApplication

    @PostConstruct
    fun postConstruct() {
        Database.connect(dataConfig.dataSource())
    }

    @CliCommand(value = "experimental cms dsl list")
    fun list(): Unit {
        val fmt = FriendlyDateFormat()
        val sdMap = mutableMapOf<String, AppDefinition>()
        try {
            _appDefinitionList.forEach { sdMap.put(it.definitionName, it) }
        } catch(e: UninitializedPropertyAccessException) {
            shellLogger.fine("There are no site definitions")
        }
        val list = sdMap.values.toList().sortedBy { it.definitionName }

        transaction {
            //            logger.addLogger(StdOutSqlLogger())
            if (!SiteDefinitionRecord.exists())
                create(SiteDefinitionRecord)
            list.forEach { sd ->
                val result = SiteDefinitionRecord
                    .slice(SiteDefinitionRecord.version, SiteDefinitionRecord.modified)
                    .select(SiteDefinitionRecord.name eq sd.definitionName)
                    .limit(1)
                if (result.empty()) {
                    println("${sd.definitionName}(${sd.version}): Never Applied")
                } else {
                    val row = result.iterator().next()
                    val modified: Date = row[SiteDefinitionRecord.modified].toDate()
                    val version = row[SiteDefinitionRecord.version]
                    println("${sd.definitionName}(${sd.version}): Last Applied Version #$version on ${fmt.format(modified)}")
                }
            }
        }
    }


    @Suppress("IMPLICIT_CAST_TO_ANY")
    @CliCommand(value = "experimental cms dsl apply")
    fun apply(@CliOption(key = arrayOf("definition"), mandatory = true) appDefinition: AppDefinition) {
        if (principalDAO.currentPrincipal == null) {
            shellLogger.warning("Please login first")
            return
        }
        transaction {
            if (!SiteDefinitionRecord.exists())
                create(SiteDefinitionRecord)
        }

        try {
            modelApplication.applyDefinition(appDefinition)
        } catch(e: InternationalizedException) {
            sendNotification(e.createNotification())
            cleanupSession()
            throw e
        }
        catch(e: Throwable) {
            cleanupSession()
            throw e
        }
        cleanupSession()
        transaction {
            val now = DateTime.now()
            val result = SiteDefinitionRecord.select(SiteDefinitionRecord.name eq appDefinition.definitionName).limit(1)
            if (result.empty()) {
                SiteDefinitionRecord.insert {
                    it[name] = appDefinition.definitionName
                    it[version] = appDefinition.version
                    it[created] = now
                    it[modified] = now
                }
            } else {
                SiteDefinitionRecord.update({ SiteDefinitionRecord.id eq result.iterator().next()[SiteDefinitionRecord.id] }) {
                    it[version] = appDefinition.version
                    it[modified] = now
                }
            }
        }
    }

    private fun cleanupSession() {
        try {
            hibernateSessionHelper.session.clear()
            HibernateSessionHelper.flushAndClearSession(hibernateSessionHelper.session)
        } catch (d: Throwable) {
            shellLogger.fine("Could not clear session.")
        }
    }

}