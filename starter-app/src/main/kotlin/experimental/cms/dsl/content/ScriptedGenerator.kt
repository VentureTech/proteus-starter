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
import com.i2rd.cms.visibility.VisibilityConditionInstance
import com.i2rd.contentmodel.data.ModelDataDAO
import com.i2rd.converter.ConverterContext
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryConfiguration
import com.i2rd.lib.LibraryDAO
import experimental.cms.dsl.*
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.category.CmsCategory
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.internet.http.Link

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
            val scriptParameters = type.getParameters(LibraryConfiguration(library), libraryDAO, type.createContext())
            for(param in scriptParameters) {
                val parameterValues = parameters[param.name] ?: continue
                for(item in parameterValues) {
                    var parameterValue = item
                    when(param.dataDomain.dataType) {
                        Link::class.java, FileEntity::class.java -> {
                            if (parameterValue is String)
                                parameterValue = helper.getInternalLink(parameterValue)
                            }
                    }
                    val convertedValue = param.converter.convert(ConverterContext(parameterValue, String::class.java))!!
                    builder.params.put(param.name, convertedValue)
                }
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

    override fun toString(): String {
        return "ScriptedGenerator(" +
            "script=$script," +
            "parameters=$parameters," +
            "category=$category," +
            "path='$path'," +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }

    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null
}