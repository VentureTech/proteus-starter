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

package experimental.cms.dsl

import com.i2rd.cms.bean.TextBean
import com.i2rd.cms.bean.contentmodel.CmsModelDataSet
import com.i2rd.cms.component.miwt.MIWTPageElementModel
import com.i2rd.contentmodel.data.ModelDataDAO
import com.i2rd.contentmodel.data.ModelDataXML
import net.proteusframework.cms.PageElementModelImpl.StandardIdentifier
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.content.DefaultDataPurpose
import net.proteusframework.core.xml.XMLUtil

internal fun createContentIdPredicate(existingId: String): (Content) -> Boolean = { it.id == existingId }

interface ContentContainer {
    val contentList: List<Content>
    val contentToRemove: MutableList<Content>

    fun Content.remove() = contentToRemove.add(this)
}

interface Content : HTMLIdentifier, HTMLClass, ResourceCapable, PathCapable {
    val id: String
    var parent: Any?
    fun createInstance(helper: ContentHelper): ContentElement
    fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean = false
}

class Text(id: String, var htmlContent: String = "")
    : Identifiable(id), Content {

    override fun createInstance(helper: ContentHelper): ContentElement {
        val textBean = TextBean()
        val dataSet = CmsModelDataSet()
        dataSet.contentElement = textBean
        val xhtml = ModelDataXML()
        xhtml.modelField = DefaultDataPurpose.rendering.name
        xhtml.value = helper.convertXHTML(htmlContent)
        dataSet.addModelData(xhtml)
        textBean.dataVersions.add(dataSet)
        return textBean
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val toCheck = helper.convertXHTML(htmlContent)
        val dataSet = contentElement.publishedData[helper.getCmsSite().primaryLocale]
        val dataSetDAO = ModelDataDAO.getInstance(TextBean().contentModelDefinition)
        if(dataSet == null || dataSet.modelData.isEmpty())
            return true
        val modelData = dataSetDAO.getData(dataSet, DefaultDataPurpose.rendering.name) as ModelDataXML
        return XMLUtil.getIdentity("<div>${modelData.value}</div>") != XMLUtil.getIdentity("<div>${toCheck}</div>")
    }

    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    override fun toString(): String {
        return "Text(" +
            "htmlContent='$htmlContent'," +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }


}

class ApplicationFunction(id: String)
    : Identifiable(id), Content {
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    var registerLink: Boolean = true

    override fun createInstance(helper: ContentHelper): ContentElement {
        val match = helper.getApplicationFunctions().filter {
            val annotation = it.javaClass.getAnnotation(net.proteusframework.ui.management.ApplicationFunction::class.java)
            annotation.name == this@ApplicationFunction.id
        }.first() as MIWTPageElementModel
        val pageElementModel = helper.getMIWTPageElementModelFactory().getVirtualComponents(helper.getCmsSite()).filter {
            StandardIdentifier(it.identifier).virtualIdentifier == match.javaClass.name
        }.first()
        helper.assignToSite(pageElementModel.identifier)
        return helper.getMIWTPageElementModelFactory().createInstance(pageElementModel)
    }

    override fun toString(): String {
        return "ApplicationFunction(" +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "registerLink=$registerLink," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }
}
