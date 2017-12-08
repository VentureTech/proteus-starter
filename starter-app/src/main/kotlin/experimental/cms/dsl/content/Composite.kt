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

import com.i2rd.cms.bean.CompositeBean
import com.i2rd.cms.visibility.VisibilityConditionInstance
import experimental.cms.dsl.*
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.editor.DelegatePurpose

/**
 * Content that can contain other Content.
 */
class Composite(id: String) : Identifiable(id), Content, DelegateContent {
    override val contentPurpose: MutableMap<Content, DelegatePurpose> = mutableMapOf()

    override fun createInstance(helper: ContentHelper, existing: ContentElement?) = ContentInstance(existing?: CompositeBean())

    override var visibilityCondition: VisibilityConditionInstance? = null
    override val contentList = mutableListOf<Content>()
    override val contentToRemove = mutableListOf<Content>()
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null



}