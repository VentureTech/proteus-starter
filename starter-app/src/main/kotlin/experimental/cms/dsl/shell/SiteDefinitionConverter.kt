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

import com.i2rd.hibernate.util.HibernateRunnable
import experimental.cms.dsl.AppDefinition
import org.apache.logging.log4j.LogManager
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget

/**
 * Shell converter for SiteDefinition
 * @author Russ Tennant (russ@venturtech.net)
 */
open class SiteDefinitionConverter : Converter<AppDefinition>, ApplicationContextAware {

    companion object {
        val logger = LogManager.getLogger(SiteDefinitionConverter::class.java)!!
    }

    private val _appDefinitionList = mutableListOf<AppDefinition>()
    private lateinit var _applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        _applicationContext = applicationContext
    }

    private fun getAppDefinitionList(): List<AppDefinition> {
        if(_appDefinitionList.isEmpty()) {
            HibernateRunnable({
                try {
                    _appDefinitionList.addAll(_applicationContext.getBeansOfType(AppDefinition::class.java).values)
                } catch (e: BeansException) {
                    e.printStackTrace()
                }
            }).run()
        }
        return _appDefinitionList
    }

    override fun supports(type: Class<*>?, optionContext: String?): Boolean = AppDefinition::class.java.isAssignableFrom(type)

    override fun getAllPossibleValues(completions: MutableList<Completion>?, targetType: Class<*>?, existingData: String?,
        optionContext: String?, target: MethodTarget?): Boolean {
        try {
            val list = if(existingData!=null) getAppDefinitionList().filter { sd -> sd.definitionName.contains(existingData, true)}
                else getAppDefinitionList()
            list.sortedBy { it.definitionName }.forEach { completions?.add(Completion(it.definitionName)) }
        }
        catch (e: UninitializedPropertyAccessException){
            logger.debug("There are no SiteDefinitions", e)
        }
        return true
    }

    override fun convertFromText(value: String?, targetType: Class<*>?, optionContext: String?): AppDefinition {
        if (!value.isNullOrBlank()) {
            try {
                val definition = getAppDefinitionList().firstOrNull { sd -> sd.definitionName == value }
                if(definition != null)
                    return definition
            }
            catch (e: UninitializedPropertyAccessException){
                logger.debug("There are no SiteDefinitions", e)
            }
        }

        throw RuntimeException("Couldn't convert '$value' to $targetType")
    }

}