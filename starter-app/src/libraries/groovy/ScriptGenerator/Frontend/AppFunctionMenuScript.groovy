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
import com.example.app.profile.ui.client.ClientManagementPermissionCheck
import com.example.app.profile.ui.company.CompanyUIPermissionCheck
import com.example.app.profile.ui.company.resource.CompanyResourcePermissionCheck
import com.example.app.profile.ui.user.MyAccountPermissionCheck
import com.example.app.profile.ui.user.UserManagementPermissionCheck
import com.example.app.support.service.ApplicationFunctionPermissionCheck
import com.i2rd.cms.generator.MenuBeanGenerator
import groovy.transform.CompileStatic
import net.proteusframework.cms.component.generator.AbstractScriptGenerator
import net.proteusframework.cms.component.generator.ContentWrapper
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.core.locale.TextSource
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
class AppFunctionMenuGenerator extends AbstractScriptGenerator
{
    @Autowired CompanyUIPermissionCheck _companiesPermissionCheck
    @Autowired UserManagementPermissionCheck _userManagementPermissionCheck
    @Autowired MyAccountPermissionCheck _myAccountPermissionCheck
    @Autowired CompanyResourcePermissionCheck _companyResourcePermissionCheck
    @Autowired ClientManagementPermissionCheck _clientManagementPermissionCheck
    @Autowired SelectedCompanyTermProvider _terms
    @Autowired ApplicationRegistry _applicationRegistry

    String _cssID

    AppFunctionMenuGenerator()
    {
        super()
    }

    @Override
    void preRenderProcess(CmsRequest request, CmsResponse response, ProcessChain chain)
    {
        _cssID = MenuBeanGenerator.getMenuCSSID(request)
    }

    @Override
    boolean includeWrappingContent(CmsRequest request)
    {
        return false
    }

    @Override
    void render(CmsRequest request, CmsResponse response, RenderChain chain) throws IOException
    {
        def pw = response.getContentWriter()
        ContentWrapper cw = new ContentWrapper(request, response)
        cw.setIdAttribute(_cssID)
        cw.addClassAttribute('starter-menu-wrapper menu')
        cw.open()

        pw.append('<ul class="nav starter-menu menubeanh menu">')

        appendLink(request, response, _companiesPermissionCheck, "company-management", _terms.companies())
        appendLink(request, response, _clientManagementPermissionCheck, "client-management", _terms.clients())
        appendLink(request, response, _userManagementPermissionCheck, "user-management", UIText.USERS())
        appendLink(request, response, _companyResourcePermissionCheck, "resource-management", UIText.RESOURCES())

        pw.append('</ul>')

        cw.close()
    }

    private void appendLink(CmsRequest request, CmsResponse response,
        ApplicationFunctionPermissionCheck permissionCheck, String classname, TextSource displayText)
    {
        def pw = response.getContentWriter()
        if(permissionCheck.checkPermissionsForCurrent(request))
        {
            ApplicationFunction func = _applicationRegistry.getApplicationFunctionByName(
                permissionCheck.applicationFunctionName)
            def link = _applicationRegistry.createLink(request, response, func, [:])
            def url = response.createURL(link).getURL(false)
            pw.append('<li class="link ' + classname + '" data-path="/user">')
            pw.append('<a').appendEscapedAttribute('href', url)
            pw.append(' class="link menuitemlabel"><span>')
                .appendEscapedData(displayText)
                .append('</span></a></li>')
        }
    }
}

generator = new AppFunctionMenuGenerator()
ApplicationContextUtils.instance.context.autowireCapableBeanFactory.autowireBean(generator)