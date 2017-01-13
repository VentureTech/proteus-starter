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

import com.i2rd.cms.bean.TextBean
import com.i2rd.cms.bean.contentmodel.CmsModelDataSet
import com.i2rd.cms.visibility.VisibilityConditionInstance
import com.i2rd.contentmodel.data.ModelDataDAO
import com.i2rd.contentmodel.data.ModelDataXML
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.content.DefaultDataPurpose
import net.proteusframework.core.xml.XMLUtil

/**
 * Text Content. This generates a [com.i2rd.cms.bean.TextBean].
 */
open class Text(id: String,
    /** The HTML Content. */
    var htmlContent: String = "")
    : Identifiable(id), Content {

    internal open fun createContentElement(): ContentElement = TextBean()

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing?:createContentElement()
        val dataSet = CmsModelDataSet()
        val xhtml = ModelDataXML()
        xhtml.modelField = DefaultDataPurpose.rendering.name
        xhtml.value = helper.convertXHTML(htmlContent)
        dataSet.addModelData(xhtml)
        return ContentInstance(contentElement, dataSet)
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val toCheck = helper.convertXHTML(htmlContent)
        val dataSet = contentElement.publishedData[helper.getCmsSite().primaryLocale]
        val dataSetDAO = ModelDataDAO.getInstance(contentElement.contentModelDefinition)
        if(dataSet == null || dataSet.modelData.isEmpty())
            return true
        val modelData = dataSetDAO.getData(dataSet, DefaultDataPurpose.rendering.name) as ModelDataXML
        return XMLUtil.getIdentity("<div>${modelData.value}</div>") != XMLUtil.getIdentity("<div>${toCheck}</div>")
    }

    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    override fun toString(): String {
        return "${createContentElement().javaClass.simpleName}(" +
            "htmlContent='$htmlContent'," +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }


}