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

package com.example.app.login.social.ui;

import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.example.app.support.ui.UIPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.i2rd.cms.bean.LoginBean;
import com.i2rd.cms.generator.LoginRenderer;
import com.i2rd.cms.scripts.impl.ScriptableRedirect;
import com.i2rd.cms.scripts.impl.ScriptableRedirectType;
import com.i2rd.lib.LibraryConfiguration;
import com.i2rd.lib.LibraryDAO;

import net.proteusframework.cms.component.generator.GeneratorImpl;
import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.LinkUtil;
import net.proteusframework.cms.controller.ProcessChain;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.core.notification.HTMLNotificationRenderer;
import net.proteusframework.core.notification.Notification;
import net.proteusframework.internet.http.Link;
import net.proteusframework.internet.http.RequestError;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.internet.http.Scope;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * {@link GeneratorImpl} for {@link SocialLoginElement}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
@Configurable
public class SocialLoginGenerator extends GeneratorImpl<SocialLoginElement>
{
    private static final Logger _logger = LogManager.getLogger(SocialLoginGenerator.class);

    public static enum LoginResult
    {
        /** Standard Success Result.  Instructs the {@link SocialLoginGenerator} to perform the redirect as normal */
        SUCCESS_DO_REDIRECT,
        /** Instructs the {@link SocialLoginGenerator} to perform the redirect using javascript, after the page has loaded.
         * This is done because in some cases, some javascript must be executed against a Social Login service api post-login. */
        SUCCESS_DO_REDIRECT_JAVASCRIPT,
        /** Pretty self-explanatory */
        FAIL
    }

    private static final String PARAM_CALLBACK = "callback";
    private static final String PROP_JS_REDIRECT = "social-login-js-redirect";

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //Autowired In.
    @Autowired private List<SocialLoginService> _loginServices;
    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private LibraryDAO _libraryDAO;
    @Autowired private UIPreferences _uiPreferences;

    private Renderer<SocialLoginElement> _socialLoginRenderer;

    /**
     * Instantiates a new Social login generator.
     */
    public SocialLoginGenerator()
    {
        super();
        setScope(Scope.REQUEST);
    }

    @Override
    public List<NDE> getNDEs()
    {
        if(_socialLoginRenderer != null)
        {
            return _socialLoginRenderer.getNDEs();
        }
        else return Collections.emptyList();
    }

    @Override
    public void preRenderProcess(CmsRequest<SocialLoginElement> request, CmsResponse response, ProcessChain chain)
    {
        super.preRenderProcess(request, response, chain);

        SocialLoginContentBuilder cb = SocialLoginContentBuilder.load(request.getPageElementData(),
            SocialLoginContentBuilder.class, true);

        //Grab the selected Login Service.  If one cannot be found, the renderer will not be set.
        _loginServices.stream().filter(ls -> Objects.equals(ls.getServiceIdentifier(), cb.getLoginServiceIdentifier())).findFirst()
            .ifPresent(selectedService -> {

                //Get selected providers.  If none can be found, the renderer will not be set.
                List<SocialLoginProvider> providers = selectedService.getSupportedProviders().stream()
                    .filter(slp -> cb.getProviderProgrammaticNames().stream()
                        .anyMatch(pid -> Objects.equals(pid, slp.getProgrammaticName())))
                    .collect(Collectors.toList());

                if (!providers.isEmpty())
                {
                    //Construct the callback URI
                    final ResponseURL callbackURL = response.createURL(true);
                    String paramRetUrl;
                    if((paramRetUrl = request.getParameter(LoginBean.PARM_RETURN_URL)) != null
                       && !Objects.equals(paramRetUrl, "/"))
                    {
                        callbackURL.addParameter(LoginBean.PARM_RETURN_URL, paramRetUrl);
                    }
                    callbackURL.addParameter(PARAM_CALLBACK, true);
                    callbackURL.setAbsolute(true);
                    request.getSession(Scope.SESSION).initialize();
                    SocialLoginParams params = new SocialLoginParams(
                        callbackURL,
                        cb.getMode(), selectedService, _uiPreferences::addMessage, cb, providers);

                    //Check if we are in a callback
                    if(request.getParameter(PARAM_CALLBACK) != null)
                    {
                        LoginResult loginResult;
                        if((loginResult = selectedService.handleLoginCallback(request, response, params)) != LoginResult.FAIL)
                        {
                            if(params.getMode() == SocialLoginMode.Login)
                                handleLoginSuccess(params, request, response, loginResult);
                            else if(params.getMode() == SocialLoginMode.Link)
                                handleLinkSuccess(params, request, response, loginResult);
                            if(loginResult == LoginResult.SUCCESS_DO_REDIRECT)
                                return;
                        }
                        else
                        {
                            clearURLParams(params, request, response);
                        }
                    }

                    _socialLoginRenderer = selectedService.createLoginRenderer(params);

                    _socialLoginRenderer.preRenderProcess(request, response, chain);
                }
            });
    }

    private void clearURLParams(SocialLoginParams params, CmsRequest<SocialLoginElement> request, CmsResponse response)
    {
        response.redirect(getClearedURL(params, request, response));
    }

    private ResponseURL getClearedURL(SocialLoginParams params, CmsRequest<SocialLoginElement> request, CmsResponse response)
    {
        ResponseURL redirect = response.createURL();
        request.getParameterMap().keySet().stream()
            .filter(k -> !params.getLoginService().getURLParametersToRemoveAfterCallback().contains(k))
            .filter(k -> !Objects.equals(k, PARAM_CALLBACK))
            .forEach(k -> redirect.addParameter(k, request.getParameter(k)));
        return redirect;
    }

    private void handleLinkSuccess(SocialLoginParams params, CmsRequest<SocialLoginElement> request, CmsResponse response,
        LoginResult loginResult)
    {
        ResponseURL cleared = getClearedURL(params, request, response);
        if(loginResult == LoginResult.SUCCESS_DO_REDIRECT)
            response.redirect(cleared);
        else if(loginResult == LoginResult.SUCCESS_DO_REDIRECT_JAVASCRIPT)
            _uiPreferences.setStoredObject(PROP_JS_REDIRECT, cleared.getURL(true));
    }

    private void handleLoginSuccess(SocialLoginParams params, CmsRequest<SocialLoginElement> request, CmsResponse response,
        LoginResult loginResult)
    {
        ResponseURL redirect;
        //Respect the dynamic return page first
        redirect = getDynamicReturn(request, response);
        //Get the scripted redirect URL if it exists
        redirect = redirect != null ? redirect : getScriptableRedirectURL(params.getContentBuilder(), request, response);
        //Finally, use the specified landing page
        redirect = redirect != null ? redirect : getLandingPage(params.getContentBuilder(), response);

        if(redirect != null && loginResult == LoginResult.SUCCESS_DO_REDIRECT)
            response.redirect(redirect);
        else if(redirect != null && loginResult == LoginResult.SUCCESS_DO_REDIRECT_JAVASCRIPT)
            _uiPreferences.setStoredObject(PROP_JS_REDIRECT, redirect.getURL(true));
        else
            response.sendError(RequestError.FORBIDDEN,
                request.getLocaleContext().getLocalizedText(LoginRenderer.REDIRECT_ERROR).getText());
    }

    @Nullable
    private ResponseURL getScriptableRedirectURLWithoutPermissionCheck(
        long scriptInstanceID, CmsRequest<SocialLoginElement> request, CmsResponse response)
    {
        if (scriptInstanceID > 0)
        {
            final LibraryConfiguration<ScriptableRedirectType> lc = _libraryDAO.getLibraryConfiguration(scriptInstanceID);
            if (lc != null)
            {
                ScriptableRedirect sr = _libraryDAO.createInstance(lc);
                if (sr != null)
                {
                    String url = sr.getRedirectURL(request);
                    if (!isEmptyString(url))
                    {
                        return response.createURL(url);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private ResponseURL getScriptableRedirectURL(
        SocialLoginContentBuilder cb, CmsRequest<SocialLoginElement> request, CmsResponse response)
    {
        try
        {
            final long scriptInstanceID = cb.getScriptedRedirectInstance();
            ResponseURL rUrl = getScriptableRedirectURLWithoutPermissionCheck(scriptInstanceID, request, response);
            if(rUrl != null && hasPermissionToAccess(rUrl.getLink()))
                return rUrl;
        }
        catch(Exception e)
        {
            final SocialLoginElement pe = request.getPageElement();
            _logger.error(
                "Caught error getting ScriptedRedirect URL for " + pe.getClass().getSimpleName() + ": \""
                + pe.getName() + "\", on Site: " + request.getHostname().getName(), e);
        }
        return null;
    }

    @Nullable
    private ResponseURL getDynamicReturn(CmsRequest<SocialLoginElement> request, CmsResponse response)
    {
        String paramRetUrl;
        if((paramRetUrl = request.getParameter(LoginBean.PARM_RETURN_URL)) != null)
        {
            ResponseURL retUrl = response.createURL(paramRetUrl);
            if(hasPermissionToAccess(retUrl.getLink()))
                return retUrl;
        }
        return null;
    }

    @Nullable
    private ResponseURL getLandingPage(SocialLoginContentBuilder cb, CmsResponse response)
    {
        Link landingPage = cb.getLandingPage();
        if(landingPage != null)
        {
            if(hasPermissionToAccess(landingPage))
                return response.createURL(landingPage);
        }
        return null;
    }

    private boolean hasPermissionToAccess(Link link)
    {
        if(LinkUtil.isInternal(link))
        {
            if(LinkUtil.requiresAuthorizationToAccess(link))
            {
                return LinkUtil.principalCanAccess(link, _principalDAO.getCurrentPrincipal());
            }
        }
        return true;
    }

    @Override
    public void render(CmsRequest<SocialLoginElement> request, CmsResponse response, RenderChain chain) throws IOException
    {
        super.render(request, response, chain);

        if(isEmptyString(request.getParameter(PARAM_CALLBACK)))
        {
            List<Notification> notifications = _uiPreferences.consumeMessages();
            if(!notifications.isEmpty())
            {
                new HTMLNotificationRenderer(request, response.getContentWriter())
                    .withRenderXHTML(request.isRequestForXmlContent())
                    .withStandardWrappingElement()
                    .render(notifications);
            }
        }
        if(_socialLoginRenderer != null)
            _socialLoginRenderer.render(request, response, chain);

        _uiPreferences.getStoredObject(PROP_JS_REDIRECT).map(String::valueOf).ifPresent(jsRedirectUrl -> {
            EntityUtilWriter pw = response.getContentWriter();
            pw.append("<script type=\"application/javascript\">");
            //noinspection StringConcatenationInsideStringBufferAppend
            pw.append("window.onload = function() { window.location = '" + jsRedirectUrl + "'; };");
            pw.append("</script>");
            _uiPreferences.setStoredObject(PROP_JS_REDIRECT, null);
        });
    }
}
