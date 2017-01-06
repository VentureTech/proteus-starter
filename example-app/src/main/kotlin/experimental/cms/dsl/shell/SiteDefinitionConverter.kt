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

import experimental.cms.dsl.AppDefinition
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget

/**
 * Shell converter for SiteDefinition
 * @author Russ Tennant (russ@venturtech.net)
 */
open class SiteDefinitionConverter : Converter<AppDefinition> {
    companion object {
        val logger = LogManager.getLogger(SiteDefinitionConverter::class.java)!!
    }
    @Autowired(required = false)
    lateinit var _appDefinitionList: List<AppDefinition>

    override fun supports(type: Class<*>?, optionContext: String?): Boolean = AppDefinition::class.java.isAssignableFrom(type)

    override fun getAllPossibleValues(completions: MutableList<Completion>?, targetType: Class<*>?, existingData: String?,
        optionContext: String?, target: MethodTarget?): Boolean {
        try {
            val list = if(existingData!=null) _appDefinitionList.filter { sd -> sd.definitionName.contains(existingData, true)}
                else _appDefinitionList
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
                val definition = _appDefinitionList.filter { sd -> sd.definitionName == value }.firstOrNull()
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