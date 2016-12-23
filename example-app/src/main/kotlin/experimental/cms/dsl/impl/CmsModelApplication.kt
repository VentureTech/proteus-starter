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

package experimental.cms.dsl.impl

import com.i2rd.cms.SiteSSLOption
import com.i2rd.cms.backend.layout.BoxInformation
import com.i2rd.cms.dao.CmsBackendDAO
import com.i2rd.cms.dao.CmsSiteDefinitionDAO
import com.i2rd.cms.workflow.WorkFlowFactory
import com.i2rd.hibernate.util.HibernateRunnable
import experimental.cms.dsl.*
import net.proteusframework.cms.CmsHostname
import net.proteusframework.cms.CmsSite
import net.proteusframework.cms.component.page.PageTemplate
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.core.hibernate.dao.DAOHelper
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment


open class CmsModelApplication(val siteDefinition: SiteDefinition) : DAOHelper() {

    companion object {
        val logger = LogManager.getLogger(CmsModelApplication::class.java)!!
    }

    @Autowired
    lateinit var environment: Environment
    @Autowired
    lateinit var siteDefinitionDAO: CmsSiteDefinitionDAO
    @Autowired
    lateinit var cmsBackendDAO: CmsBackendDAO
    @Autowired
    lateinit var cmsFrontendDAO: CmsFrontendDAO

    private val pageModelToCmsPage = mutableMapOf<Page, net.proteusframework.cms.component.page.Page>()
    private val layoutToBoxInformation = mutableMapOf<Layout, BoxInformation>()

    fun applyDefinition() = HibernateRunnable({
        siteDefinition.getSites().forEach { siteModel ->
            doInTransaction {
                applySiteModel(siteModel)
            }
        }
        cleanup()
    }).run()

    private fun cleanup() {
        pageModelToCmsPage.clear()
        layoutToBoxInformation.clear()
    }

    private fun applySiteModel(siteModel: Site) {
        logger.info("Applying Site Model: ${siteModel.id}")
        if(siteModel.hostnames.isEmpty())
            throw IllegalArgumentException("Invalid SiteModel. No hostnames")

        val site = getOrCreateSite(siteModel)
        // FIXME : finish implementing
    }


    private fun getOrCreateSite(siteModel: Site): CmsSite {
        var cmsSite = siteDefinitionDAO.getSiteByDescription(siteModel.id)
        if(cmsSite == null) {
            cmsSite = CmsSite()
            cmsSite.siteDescription = siteModel.id
            cmsSite.primaryLocale = siteModel.primaryLocale
            cmsSite.defaultTimeZone = siteModel.defaultTimezone
            cmsSite.workFlow = WorkFlowFactory.getStandard3StageWorkFlow()
            cmsBackendDAO.saveSite(cmsSite)
        }
        session.flush()
        return cmsSite
    }

    private fun getOrCreateHostnames(siteModel: Site, site: CmsSite): List<CmsHostname> {
        val list = mutableListOf<CmsHostname>()
        for(hn in siteModel.hostnames) {
            val address = environment.resolveRequiredPlaceholders(hn.address)
            var cmsHostname = cmsFrontendDAO.getSiteHostname(address)
            if(cmsHostname == null) {
                cmsHostname = CmsHostname()
                cmsHostname.site = site
                cmsHostname.name = address
                cmsHostname.sslOption = SiteSSLOption.no_influence
                cmsHostname.welcomePage = getOrCreatePagePass1(site, hn.welcomePage)
                // FIXME : finish implementing
                session.flush()

            } else if(cmsHostname.site != site)
                throw IllegalArgumentException("Hostname, $address, exists on other site.")
            list.add(cmsHostname)
        }
        return list
    }

    private fun getOrCreatePagePass1(site: CmsSite, page: Page): net.proteusframework.cms.component.page.Page {
        val pass1 = pageModelToCmsPage.getOrPut(page, defaultValue = {
            var cmsPage = siteDefinitionDAO.getPageByName(site, page.id)
            if(cmsPage == null) {
                cmsPage = net.proteusframework.cms.component.page.Page()
                cmsPage.site = site
                cmsPage.pageTemplate = getOrCreatePageTemplate(site, page.template)
                // FIXME: finish implementing
                cmsBackendDAO.savePage(cmsPage)
                session.flush()
            }
            return cmsPage
        })
        layoutToBoxInformation.put(page.layout, BoxInformation(pass1.pageTemplate.layout))
        return pass1
    }

    private fun getOrCreatePageTemplate(site: CmsSite, template: Template): PageTemplate {
        var pageTemplate = siteDefinitionDAO.getPageTemplateByName(site, template.id)
        if(pageTemplate == null) {
            pageTemplate = PageTemplate()
            pageTemplate.name = template.id
            pageTemplate.cssName = template.htmlId
            pageTemplate.layout = getOrCreateLayout(site, template.layout)
            // FIXME : finish implementing
            session.flush()
        }
        return pageTemplate
    }

    private fun getOrCreateLayout(site: CmsSite, layout: Layout): net.proteusframework.cms.component.page.layout.Layout {
        var cmsLayout = siteDefinitionDAO.getLayoutByName(site, layout.id)
        if(cmsLayout == null) {
            cmsLayout = net.proteusframework.cms.component.page.layout.Layout()
            cmsLayout.site = site
            cmsLayout.name = layout.id
            populateLayoutBoxes(site, layout, cmsLayout)
            cmsBackendDAO.saveLayout(cmsLayout)
            session.flush()
        }
        return cmsLayout
    }

    private fun populateLayoutBoxes(site: CmsSite, layout: Layout,
        cmsLayout: net.proteusframework.cms.component.page.layout.Layout) {
        for (box in layout.children) {
            val cmsBox = createCmsBox(box, site)
            cmsLayout.boxes.add(cmsBox)
            populateLayoutBoxes(site, box, cmsBox)
        }
    }

    private fun populateLayoutBoxes(site: CmsSite, parent: Box, cmsParent: net.proteusframework.cms.component.page.layout.Box) {
        for (child in parent.children) {
            val cmsBox = createCmsBox(child, site)
            cmsParent.children.add(cmsBox)
            populateLayoutBoxes(site, child, cmsBox)
        }
    }

    private fun createCmsBox(box: Box,
        site: CmsSite): net.proteusframework.cms.component.page.layout.Box {
        val cmsBox = net.proteusframework.cms.component.page.layout.Box()
        cmsBox.site = site
        cmsBox.name = box.id
        cmsBox.boxDescriptor = box.boxType
        cmsBox.defaultContentArea = box.defaultContentArea
        cmsBox.cssName = box.htmlId
        cmsBox.styleClass = box.htmlClass
        return cmsBox
    }

}

