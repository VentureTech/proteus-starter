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

import com.i2rd.cms.component.miwt.MIWTPageElementModel
import com.i2rd.cms.visibility.VisibilityConditionInstance
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement

/**
 * MIWT Content for a [MIWTPageElementModel]
 * @author Conner Rocole (crocole@proteus.co)
 */
open class MIWT(id: String, val miwtClass: Class<out MIWTPageElementModel>)
    : Identifiable(id), Content {
    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        if (existing != null) return ContentInstance(existing)
        val pageElementModel = helper.getMIWTPageElementModelFactory().getVirtualComponents(helper.getCmsSite()).first {
            PageElementModelImpl.StandardIdentifier(it.identifier).virtualIdentifier == miwtClass.name
        }
        helper.assignToSite(pageElementModel.identifier)

        return ContentInstance(helper.getMIWTPageElementModelFactory().createInstance(pageElementModel))
    }

    override fun toString(): String {
        return "MIWT(" +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }
}
