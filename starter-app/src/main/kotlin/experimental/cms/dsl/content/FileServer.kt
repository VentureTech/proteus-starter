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

import com.i2rd.cms.bean.FileServer.FileServerModel
import com.i2rd.cms.visibility.VisibilityConditionInstance
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement
import java.util.concurrent.TimeUnit

/**
 * File Server Content.
 * @author Russ Tennant (russ@venturetech.net)
 */
class FileServer(id: String): Identifiable(id), Content {
    /** Directory Path. May be partial as long as it is unique. */
    var directory: String = ""


    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing?: com.i2rd.cms.bean.FileServer()
        val builder = FileServerModel.load(
            contentElement.publishedData[helper.getCmsSite().primaryLocale], false)
        updateBuilder(helper, builder)
        helper.assignToSite(PageElementModelImpl.StandardIdentifier(com.i2rd.cms.bean.FileServer::class.java).toIdentifier())
        return ContentInstance(contentElement, builder.content)
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val builder = FileServerModel.load(
            contentElement.publishedData[helper.getCmsSite().primaryLocale], false)
        updateBuilder(helper, builder)
        return builder.isDirty
    }

    private fun updateBuilder(helper: ContentHelper, builder: FileServerModel) {
        builder.isCORSAllowCredentials = true
        builder.corsAllowOrigin = "*"
        builder.setExpireInterval(TimeUnit.HOURS.toSeconds(2))
        val fileSystemEntity = helper.findWebFileSystemEntity(directory)
        if(fileSystemEntity != null)
            builder.fileSystemEntity = fileSystemEntity
    }

    override fun toString(): String {
        return "FileServer(" +
            "directory='$directory'," +
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