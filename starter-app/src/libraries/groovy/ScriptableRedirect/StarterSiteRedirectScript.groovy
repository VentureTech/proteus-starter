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

package ScriptableRedirect

import com.example.app.profile.model.setup.SiteSetupDAO
import com.example.app.profile.model.user.UserDAO
import com.example.app.profile.ui.ApplicationFunctions
import com.example.app.profile.ui.URLProperties
import com.google.common.collect.ImmutableMap
import com.i2rd.cms.bean.LoginBean
import com.i2rd.cms.scripts.impl.ScriptableRedirect
import com.i2rd.lib.Parameter
import com.i2rd.lib.util.ParameterUtil
import groovy.transform.CompileStatic
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.core.StringFactory
import net.proteusframework.core.script.ScriptAttributes
import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.internet.http.Link
import net.proteusframework.ui.management.ApplicationRegistry
import net.proteusframework.ui.management.URLConfigPropertyConverter
import net.proteusframework.users.model.dao.PrincipalDAO
import org.springframework.beans.factory.annotation.Autowired

import javax.script.ScriptContext

import static net.proteusframework.core.locale.TextSources.createText

/**
 * Redirect to Starter Site Setup if no Companies exist.
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/3/17
 */
@CompileStatic
class StarterSiteRedirect implements ScriptableRedirect
{
    @Autowired PrincipalDAO _principalDAO
    @Autowired UserDAO _userDAO
    @Autowired SiteSetupDAO _starterSiteDAO
    @Autowired ApplicationRegistry _applicationRegistry

    private static final String DEFAULT_REDIRECT_PAGE = "Default Redirect Page"

    def _params

    StarterSiteRedirect(ScriptContext context)
    {
        super()
        _params = ScriptAttributes._params.getAttribute(context)
    }

    @Override
    String getRedirectURL(CmsRequest<? extends PageElement> cmsRequest)
    {
        if(_starterSiteDAO.needsSetup())
        {
            def appFunc = _applicationRegistry.getApplicationFunctionByName(ApplicationFunctions.StarterSite.SETUP)
            return _applicationRegistry.createLink(cmsRequest.getHostname(), appFunc, null,
                ImmutableMap.<String, Object>builder()
                .put(URLProperties.COMPANY, URLConfigPropertyConverter.ENTITY_NEW)
                .build()).getURIAsString()
        }
        else
        {
            final String returnUrl = cmsRequest.getParameter(LoginBean.PARM_RETURN_URL)
            if (!StringFactory.isEmptyString(returnUrl) && returnUrl.trim() != "/")
                return returnUrl
            def defaultLink = _params[DEFAULT_REDIRECT_PAGE] as Link
            return defaultLink.getURIAsString()
        }
    }

    @Override
    boolean doSelfTest(PrintWriter printWriter)
    {
        return false
    }

    @Override
    List<? extends Parameter> getParameters()
    {
        return [
            ParameterUtil.link(DEFAULT_REDIRECT_PAGE, createText(DEFAULT_REDIRECT_PAGE), null, null),
        ] as List
    }
}

scriptable = new StarterSiteRedirect(context)
ApplicationContextUtils.instance.context.autowireCapableBeanFactory.autowireBean(scriptable)