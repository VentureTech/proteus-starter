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

import com.example.app.profile.model.ProfileDAO
import com.example.app.profile.model.company.Company
import com.example.app.profile.model.company.CompanyDAO
import com.example.app.profile.model.user.User
import com.example.app.profile.model.user.UserDAO
import com.example.app.support.ui.UIPreferences
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.proteusframework.cms.component.generator.AbstractScriptGenerator
import net.proteusframework.cms.component.generator.CacheableBuilder
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.core.io.EntityUtilWriter
import net.proteusframework.core.locale.NamedObjectComparator
import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.data.http.URLGenerator
import net.proteusframework.internet.http.Scope
import net.proteusframework.users.model.Principal
import net.proteusframework.users.model.dao.PrincipalDAO
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.TimeUnit

import static net.proteusframework.cms.support.HTMLPageElementUtil.getExpireTime

/**
 * Provides a UI for selecting the current user's Company
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/29/16 11:43 AM
 */
@CompileStatic
class CompanySelectorGenerator extends AbstractScriptGenerator
{
    @Autowired PrincipalDAO _principalDAO
    @Autowired UserDAO _userDAO
    @Autowired CompanyDAO _companyDAO
    @Autowired ProfileDAO _profileDAO
    @Autowired URLGenerator _urlGenerator
    @Autowired UIPreferences _uiPreferences

    private User _currentUser
    private Principal _currentPrincipal
    private Company _currentCompany
    private List<Company> _companies
    long expireTime = 0L;


    @Override
    boolean includeWrappingContent(CmsRequest request)
    {
        false
    }

    @Override
    Scope getScope()
    {
        Scope.SESSION
    }

    @Override
    String getIdentity(CmsRequest request)
    {
        CacheableBuilder cb = new CacheableBuilder(request)
        if(_currentUser != null)
            cb.addEntity(_currentUser)
        if(_companies != null)
            cb.addEntities(_companies)
        return cb.makeCacheable().getIdentity(request)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    @Override
    void preRenderProcess(CmsRequest request, CmsResponse response, ProcessChain chain)
    {
        _currentUser = _userDAO.currentUser
        _currentPrincipal = _principalDAO.currentPrincipal
        if (_currentUser == null && _currentPrincipal == null)
            return
        setExpireTime(getExpireTime(request.getPageElement(), TimeUnit.MINUTES, 10))
        _currentCompany = _uiPreferences.getSelectedCompany()
        _companies = (_currentUser != null
            ? _companyDAO.getActiveCompanies(_currentUser)
            : _companyDAO.getActiveCompanies(_currentPrincipal))
        if (!_companies.contains(_currentCompany) && _currentCompany != null)
        {
            _companies.add(0, _currentCompany)
        }
        _companies.sort(new NamedObjectComparator(request.getLocaleContext()))
    }

    @Override
    void render(CmsRequest request, CmsResponse response, RenderChain chain) throws IOException
    {
        if(_currentUser == null && _currentPrincipal == null)
            return

        EntityUtilWriter writer = response.getContentWriter()

        writer.append('<span class="company-selector dropdown">')
        writer.append('<span class="toggler-wrapper" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">')
        if(_currentCompany != null)
        {
            writer.append('<span class="selected-logo"')
            if (_currentCompany.getImage() != null)
            {
                def imageURL = _urlGenerator.createURL(_currentCompany.getImage())
                writer.appendEscapedAttribute('data-background-image', imageURL.getURL(true))
            }
            writer.append('>').appendEscapedData(_currentCompany.getName()).append('</span>')
            if (_companies.size() > 1)
            {
                writer.append('<span class="fa fa-chevron-right"></span>')
            }
        }
        writer.append('</span>')

        if(_companies.size() > 1 || (_companies.size() > 0 && _currentCompany == null))
        {
            writer.append('<span class="dropdown-menu">')
            _companies.forEach({Company company ->
                def hostname = company.getHostname()
                writer.append('<form method="GET"').appendEscapedAttribute('action', "//${hostname.getName()}")
                    .appendEscapedAttribute('target', "_blank")
                    .append('>')
                writer.append('<button type="submit" class="dropdown-item"')
                writer.appendEscapedAttribute('data-popup', "//${hostname.getName()}")
                if (company.getImage() != null)
                {
                    def imageURL = _urlGenerator.createURL(company.getImage())
                    writer.appendEscapedAttribute('data-background-image', imageURL.getURL(true))
                }
                writer.append('>').appendEscapedData(company.getName()).append('</button>')
                writer.append('</form>')
            })
            writer.append('</span>')
        }

        writer.append('</span>')
    }
}

//noinspection GroovyUnusedAssignment
generator = new CompanySelectorGenerator()
ApplicationContextUtils.instance.context.autowireCapableBeanFactory.autowireBean(generator)