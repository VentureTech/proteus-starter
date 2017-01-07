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

package ScriptGenerator.Frontend

import com.example.app.profile.service.SelectedCompanyTermProvider
import com.example.app.profile.ui.UIText
import com.example.app.profile.ui.company.CompanyUIPermissionCheck
import com.example.app.profile.ui.user.MyAccountPermissionCheck
import com.example.app.profile.ui.user.UserManagementPermissionCheck
import com.i2rd.cms.generator.MenuBeanGenerator
import groovy.transform.CompileStatic
import net.proteusframework.cms.component.generator.AbstractScriptGenerator
import net.proteusframework.cms.component.generator.ContentWrapper
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.ui.management.ApplicationFunction
import net.proteusframework.ui.management.ApplicationRegistry
import org.springframework.beans.factory.annotation.Autowired
/**
 * Scripted Menu for the Starter Site.
 * <br><br>
 * Intended to be modified for project-specific code, or just used as an example.
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/4/17
 */
@CompileStatic
class StarterSiteMenuGenerator extends AbstractScriptGenerator
{
    @Autowired CompanyUIPermissionCheck _companiesPermissionCheck
    @Autowired UserManagementPermissionCheck _userManagementPermissionCheck
    @Autowired MyAccountPermissionCheck _myAccountPermissionCheck
    @Autowired SelectedCompanyTermProvider _terms
    @Autowired ApplicationRegistry _applicationRegistry

    String _cssID

    StarterSiteMenuGenerator()
    {
        super()
    }

    @Override
    void preRenderProcess(CmsRequest request, CmsResponse response, ProcessChain chain)
    {
        _cssID = MenuBeanGenerator.getMenuCSSID(request)
    }

    @Override
    void render(CmsRequest request, CmsResponse response, RenderChain chain) throws IOException
    {
        def pw = response.getContentWriter()
        ContentWrapper cw = new ContentWrapper(request, response)
        cw.setIdAttribute(_cssID)
        cw.addClassAttribute('starter-menu-wrapper')
        cw.open()

        pw.append('<ul class="nav starter-menu">')
        if(_companiesPermissionCheck.checkPermissionsForCurrent())
        {
            ApplicationFunction func = _applicationRegistry.getApplicationFunctionByName(
                _companiesPermissionCheck.applicationFunctionName)
            def link = _applicationRegistry.createLink(request, response, func, [:])
            def url = response.createURL(link).getURL(false)
            pw.append('<li class="link company-management" data-path="/company">')
            pw.append('<a').appendEscapedAttribute('href', url)
            pw.append(' class="link"><span>')
                .appendEscapedData(_terms.companies())
                .append('</span></a></li>')
        }
        if(_userManagementPermissionCheck.checkPermissionsForCurrent())
        {
            ApplicationFunction func = _applicationRegistry.getApplicationFunctionByName(
                _userManagementPermissionCheck.applicationFunctionName)
            def link = _applicationRegistry.createLink(request, response, func, [:])
            def url = response.createURL(link).getURL(false)
            pw.append('<li class="link user-management" data-path="/user">')
            pw.append('<a').appendEscapedAttribute('href', url)
            pw.append(' class="link"><span>')
                .appendEscapedData(UIText.USERS())
                .append('</span></a></li>')
        }
        pw.append('</ul>')
    }
}

generator = new StarterSiteMenuGenerator()
ApplicationContextUtils.instance.context.autowireCapableBeanFactory.autowireBean(generator)