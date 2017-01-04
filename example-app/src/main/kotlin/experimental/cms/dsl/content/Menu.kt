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

import com.i2rd.cms.bean.MenuBean
import com.i2rd.cms.bean.MenuStyle
import com.i2rd.cms.bean.util.MenuBeanContentBuilder
import com.i2rd.cms.bean.util.MenuItem
import com.i2rd.contentmodel.data.ModelData
import com.i2rd.contentmodel.data.ModelDataDAO
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.content.DefaultDataPurpose
import net.proteusframework.core.xml.XMLUtil
import net.proteusframework.internet.http.Link
import java.net.URI


interface MenuBuilder  {
    fun label(label: String, init: MenuItemBuilder.() -> Unit = {})
    fun link(label: String, link: Link, init: MenuItemBuilder.() -> Unit = {})
    fun page(label: String, existingPageId: String, init: MenuItemBuilder.() -> Unit = {})
}

interface MenuItemBuilder : MenuBuilder {
    var htmlClass: String
    var tooltip: String
}

private fun configureProperties(builder: MenuItemBuilder, menuItem: MenuItem) {
    if(builder.htmlClass.isNotBlank())
        menuItem.className = builder.htmlClass
    if(builder.tooltip.isNotBlank())
        menuItem.tooltip = builder.tooltip
}

internal class MenuItemBuilderImpl(val menu: Menu, val item: MenuItem) : MenuItemBuilder {
    override fun label(label: String, init: MenuItemBuilder.() -> Unit) {
        val menuItem = item.addEntry(label)
        val builder = MenuItemBuilderImpl(menu, menuItem).apply(init)
        configureProperties(builder, menuItem)
    }

    override fun link(label: String, link: Link, init: MenuItemBuilder.() -> Unit) {
        val menuItem = item.addEntry(label, link)
        val builder = MenuItemBuilderImpl(menu, menuItem).apply(init)
        configureProperties(builder, menuItem)
    }

    override fun page(label: String, existingPageId: String, init: MenuItemBuilder.() -> Unit) {
        menu.getSite().siteConstructedCallbacks.add({site ->
            val page = site.children.filter { it.id == existingPageId }.first()
            val link = Link(URI.create(page.path))
            val menuItem = item.addEntry(label, link)
            val builder = MenuItemBuilderImpl(menu, menuItem).apply(init)
            configureProperties(builder, menuItem)
        })
    }

    override var htmlClass: String = ""
    override var tooltip: String = ""

}


/**
 * Menu Content.
 * @author Russ Tennant (russ@venturetech.net)
 */
class Menu(id: String): Identifiable(id), Content, MenuBuilder {

    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    private val builder = MenuBeanContentBuilder(MenuStyle.LIST)

    fun style(style: MenuStyle) = {builder.style = style}
    fun markParentLinks(flag: Boolean) = {builder.isMarkActiveParentLinks = flag}
    fun dropdownButtonText(text: String) = {builder.dropdownButtonText = text}

    override fun label(label: String, init: MenuItemBuilder.() -> Unit) {
        val menuItem = builder.addEntry(label)
        val builder = MenuItemBuilderImpl(this, menuItem).apply(init)
        configureProperties(builder, menuItem)
    }

    override fun link(label: String, link: Link, init: MenuItemBuilder.() -> Unit) {
        val menuItem = builder.addEntry(label, link)
        val builder = MenuItemBuilderImpl(this, menuItem).apply(init)
        configureProperties(builder, menuItem)
    }

    override fun page(label: String, existingPageId: String, init: MenuItemBuilder.() -> Unit) {
        getSite().siteConstructedCallbacks.add({site ->
            val page = site.children.filter { it.id == existingPageId }.first()
            val link = Link(URI.create(page.path))
            val menuItem = builder.addEntry(label, link)
            val builder = MenuItemBuilderImpl(this, menuItem).apply(init)
            configureProperties(builder, menuItem)
        })

    }

    @Suppress("UNCHECKED_CAST")
    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = MenuBean()
        val dataSet = builder.getContent(contentElement)
        val dataSetDAO = ModelDataDAO.getInstance(contentElement.contentModelDefinition)
        val data = dataSetDAO.getData(dataSet, DefaultDataPurpose.rendering.name)!! as ModelData<String>
        data.value = helper.convertXHTML(data.value, mutableSetOf("link"))
        return ContentInstance(contentElement, dataSet)
    }

    @Suppress("UNCHECKED_CAST")
    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val dataSetDAO = ModelDataDAO.getInstance(contentElement.contentModelDefinition)
        val existingBuilder = MenuBeanContentBuilder.load(contentElement.publishedData[helper.getCmsSite().primaryLocale], false)
        existingBuilder.style = builder.style
        existingBuilder.dropdownButtonText = builder.dropdownButtonText
        existingBuilder.isMarkActiveParentLinks = builder.isMarkActiveParentLinks
        val oldMenuHTML = dataSetDAO.getData(existingBuilder.getContent(contentElement as MenuBean?),
            DefaultDataPurpose.rendering.name)!! as ModelData<String>
        @Suppress("UNCHECKED_CAST")
        val data = dataSetDAO.getData(builder.getContent(contentElement), DefaultDataPurpose.rendering.name)!!
            as ModelData<String>
        val newMenuHTML = helper.convertXHTML(data.value, mutableSetOf("link"))
        val id1 = XMLUtil.getIdentity(oldMenuHTML.value)
        val id2 = XMLUtil.getIdentity(newMenuHTML)
        return existingBuilder.isDirty || id1 != id2
    }
}