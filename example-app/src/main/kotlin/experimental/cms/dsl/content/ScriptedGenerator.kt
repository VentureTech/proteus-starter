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

package experimental.cms.dsl.content

import com.google.common.collect.ArrayListMultimap
import com.i2rd.cms.bean.ScriptingBean
import com.i2rd.cms.bean.util.ScriptingBeanContentBuilder
import com.i2rd.cms.generator.ScriptGeneratorType
import com.i2rd.contentmodel.data.ModelDataDAO
import com.i2rd.converter.ConverterContext
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryDAO
import experimental.cms.dsl.*
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.category.CmsCategory
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.generator.ScriptGenerator
import net.proteusframework.core.script.GroovyScriptContext
import net.proteusframework.core.script.ScriptIdentifier
import net.proteusframework.core.script.ScriptUtility
import javax.script.SimpleScriptContext

/**
 * Scripted Generator.
 * @author Russ Tennant (russ@venturetech.net)
 */
class ScriptedGenerator(id: String) : Identifiable(id), Content {

    private lateinit var script: Script
    private val parameters = ArrayListMultimap.create<String, Any>()
    var category = CmsCategory.CustomCode

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing ?: ScriptingBean()
        val builder = ScriptingBeanContentBuilder()
        @Suppress("UNCHECKED_CAST")
        val library = helper.createLibrary(id, script.file, script.type.modelName) as Library<ScriptGeneratorType>?
        builder.library = library
        if (library != null) {
            val type = library.type
            val libraryDAO = LibraryDAO.getInstance()
            val scriptIdentifier = ScriptIdentifier("0", library.id.toString(), library.file.name, library.file.contentType, false)
            val scriptUtil = ScriptUtility()
            val scriptContext = if(library.file.name.toLowerCase().endsWith("groovy")) GroovyScriptContext() else
                SimpleScriptContext()
            @Suppress("UNCHECKED_CAST")
            val scriptGenerator = scriptUtil.getScriptedInterface(scriptContext, scriptIdentifier,
                libraryDAO.getLibraryCharSource(library), type.scriptingInterface, type.scriptingInterfaceVariable) as
                ScriptGenerator<ScriptingBean>
            for(param in scriptGenerator.parameters) {
                val parameterValue = parameters[param.name] ?: continue
                val convertedValue = param.converter.convert(ConverterContext(parameterValue, String::class.java))!!
                builder.params.put(param.name, convertedValue)
            }
        }
        if(library != null) {
            val factory = helper.getScriptingBeanPageElementModelFactory()
            val standardIdentifier = PageElementModelImpl.StandardIdentifier(factory.pageElementType, library.id.toString())
            val componentIdentifier = standardIdentifier.toIdentifier()
            contentElement.componentIdentifier = componentIdentifier
            helper.assignToSite(componentIdentifier)
            if(library.category != category) {
                library.category = category
                helper.saveLibrary(library)
            }
        }
        return ContentInstance(contentElement = contentElement, dataSet = builder.content)
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val existingDataSet = contentElement.publishedData[helper.getCmsSite().primaryLocale]?: return false
        val (ce, newDataSet) = createInstance(helper, contentElement)
        return !ModelDataDAO.getInstance(contentElement.contentModelDefinition).deepEquals(existingDataSet, newDataSet)
    }

    fun script(file: String) {
        script = Script(ScriptType.ScriptedGenerator, file)
    }

    fun param(parameterName: String, parameterValue: Any) {
        parameters.put(parameterName, parameterValue)
    }

    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null
}