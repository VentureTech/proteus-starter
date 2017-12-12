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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.management.ui.text.TextEditorApplicationFunctions
import net.proteusframework.management.ui.text.TextEditorContentBuilder

/**
 * Editor for static/application text.
 * @author russ (russ@venturetech.net)
 */
class TextEditor(id: String) : ApplicationFunction(id, functionName = TextEditorApplicationFunctions.MANAGEMENT) {
    /** Internal Use. */
    internal var adminRole: String = "proteus_admin"
    /** Internal Use. */
    internal var symbolPrefixList = mutableListOf<String>()

    /**
     * Set the admin role required to edit text.
     * @param programmaticName the role's programmatic name.
     */
    fun adminRole(programmaticName : String) { adminRole = programmaticName }

    /**
     * Add a symbol prefix to limit text editing.
     * @param prefix the prefix. Example: `com.example.app`
     */
    fun symbolPrefix(prefix: String) = symbolPrefixList.add(prefix)

    @SuppressFBWarnings(value = ["NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"],
        justification = "FindBugs is Wrong")
    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentInstance = super.createInstance(helper, existing)
        val builder = TextEditorContentBuilder.load(null, false)
        builder.adminRole = adminRole
        for(prefix in symbolPrefixList) builder.addSymbolPrefix(prefix)
        val content = builder.content
        if(contentInstance.dataSet == null)
            return contentInstance.copy(dataSet = content)
        for(data in content.modelData) contentInstance.dataSet.addModelData(data)
        return contentInstance
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val dataSet = contentElement.publishedData[helper.getCmsSite().primaryLocale]
        val existing = TextEditorContentBuilder.load(dataSet, false)
        existing.adminRole = adminRole
        return existing.isDirty || symbolPrefixList != existing.symbolPrefixList
    }
}