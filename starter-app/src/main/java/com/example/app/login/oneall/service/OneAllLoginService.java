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

package com.example.app.login.oneall.service;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.app.login.oneall.model.OneAllDAO;
import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.example.app.login.social.ui.SocialLoginElement;
import com.example.app.login.social.ui.SocialLoginGenerator.LoginResult;
import com.example.app.login.social.ui.SocialLoginMode;
import com.example.app.login.social.ui.SocialLoginParams;
import com.example.app.login.social.ui.SocialLoginServiceEditor;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.LoginLogoutHelper;
import net.proteusframework.cms.controller.LogoutCallback;
import net.proteusframework.cms.controller.ProcessChain;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.config.ExecutorConfig;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.data.http.URLGenerator;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.internet.http.Scope;
import net.proteusframework.internet.http.ServletSession;
import net.proteusframework.internet.http.resource.ClassPathResourceLibrary;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.internet.http.resource.FactoryResource;
import net.proteusframework.internet.http.resource.FactoryResourceConfiguration;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.AuthenticationDomainList;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.login.oneall.service.OneAllLoginService.SERVICE_IDENTIFIER;
import static com.example.app.login.oneall.service.OneAllLoginServiceLOK.*;
import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_SECRET;

/**
 * OneAll Login Service
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /20/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.login.oneall.service.OneAllLoginService",
    i18n = {
        @I18N(symbol = "Name", l10n = @L10N("OneAll")),
        @I18N(symbol = "Description", l10n = @L10N("OneAll Login Service")),
        @I18N(symbol = "Error No Connection Token Received",
            l10n = @L10N("A connection could not be established to Social Login service. Please contact support.")),
        @I18N(symbol = "Error User Does Not Exist For User Token",
            description = "",
            l10n = @L10N("No User exists in the system for the given social network login.")),
        @I18N(symbol = "Error Details User Does Not Exist For User Token",
            description = "",
            l10n = @L10N("If you have an existing account, please log in and link your social network through Account Management")),
        @I18N(symbol = "Error Different User Exists For User Token",
            l10n = @L10N("A User already exists in the system for the given social network login")),
        @I18N(symbol = "Error Not Logged In",
            description = "",
            l10n = @L10N("In order to link to a social network, you will have to log in first.")),
        @I18N(symbol = "Label SSO Enabled",
            description = "",
            l10n = @L10N("SSO Enabled")),
        @I18N(symbol = "Info Successfully Linked FMT",
            description = "{0:SocialLoginProvider#getDisplayName}",
            l10n = @L10N("Successfully Linked {0}")),
        @I18N(symbol = "Confirm Unlink FMT",
            description = "{0:SocialLoginProvider#getDisplayName}",
            l10n = @L10N("Are you sure you want to unlink {0}?")),
        @I18N(symbol = "Unlink",
            description = "",
            l10n = @L10N("Unlink")),
        @I18N(symbol = "Successfully Unlinked FMT", 
            description = "{0:SocialLoginProvider#getDisplayName}", 
            l10n = @L10N("Successfully Unlinked {0}")),
        @I18N(symbol = "Link Accounts",
            description = "",
            l10n = @L10N("Link Accounts")),
        @I18N(symbol = "Unlink Accounts",
            description = "",
            l10n = @L10N("Unlink Accounts"))
    }
)
@Service(SERVICE_IDENTIFIER)
public class OneAllLoginService implements SocialLoginService
{
    private static final Logger _logger = LogManager.getLogger(OneAllLoginService.class);
    /** The constant SERVICE_IDENTIFIER. */
    public static final String SERVICE_IDENTIFIER = "oneall";
    /** The constant SESSION_KEY_SSO_TOKEN */
    public static final String SESSION_KEY_SSO_TOKEN = "oneall-sso-token";
    /** The constant SESSION_KEY_IDENTITY_TOKEN. */
    public static final String SESSION_KEY_IDENTITY_TOKEN = "oneall-identity-token";
    /** The constant PROP_SSO_ENABLED */
    public static final String PROP_SSO_ENABLED = "sso-enabled";
    /** The constant PARAM_SSO_CALLBACK */
    public static final String PARAM_SSO_CALLBACK = "sso-callback";
    /** The constant PARAM_UNLINK. */
    public static final String PARAM_UNLINK = "oneall-unlink";

    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\n\r]");
    private static final String PARAM_CONNECTION_TOKEN = "connection_token";

    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private ExecutorConfig _executor;
    @Autowired private OneAllDAO _oneAllDAO;
    @Autowired private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;
    @Autowired private URLGenerator _urlGenerator;
    @Autowired private Environment _environment;
    @Value("${oneall.subdomain:#{null}}") String _subdomain;
    @Value("${oneall.public.key:#{null}}") String _publicKey;
    @Value("${oneall.private.key:#{null}}") String _privateKey;
    @Value("${oneall.api.endpoint:#{null}}") String _apiEndpoint;

    private final List<SocialLoginProvider> _supportedProviders = new ArrayList<>();

    @Override
    public Renderer<SocialLoginElement> createLoginRenderer(SocialLoginParams loginParams)
    {
        return new Renderer<SocialLoginElement>()
        {
            @Override
            public void preRenderProcess(CmsRequest<SocialLoginElement> request, CmsResponse response, ProcessChain chain)
            {
                unlinkIfSpecified(request, response, loginParams);
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<NDE> getNDEs()
            {
                return Collections.singletonList(OneAllNDELibrary.ProviderCSS.getNDE());
            }

            @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
            @Override
            public void render(CmsRequest<SocialLoginElement> request, CmsResponse response, RenderChain chain) throws IOException
            {
                if(isConfigured())
                {
                    final FactoryResourceConfiguration providersIconsConfig = new FactoryResourceConfiguration(
                        _classPathResourceLibraryHelper.createResource(
                            "social/login/oneall/oneall-provider-icons.png"));
                    providersIconsConfig.setExpireInterval(30L, ChronoUnit.DAYS);
                    final String providerIconsURL = _urlGenerator.createURL(providersIconsConfig).getLink().getURIAsString();
                    EntityUtilWriter pw = response.getContentWriter();
                    Principal currentPrincipal = _principalDAO.getCurrentPrincipal();
                    List<String> registeredProviderProgs = _oneAllDAO.getRegisteredProviders(currentPrincipal);
                    List<SocialLoginProvider> registeredProviders = loginParams.getLoginProviders().stream()
                        .filter(rp -> registeredProviderProgs.stream()
                            .anyMatch(rpp -> Objects.equals(rpp, rp.getProgrammaticName())))
                        .collect(Collectors.toList());
                    if(loginParams.getMode() == SocialLoginMode.Link && !registeredProviders.isEmpty())
                    {
                        pw.append("<div class=\"oneall-providers-unlink oneall\">");
                        pw.append("<div class=\"oneall-providers-unlink-title\">").append(UNLINK_ACCOUNTS()).append("</div>");
                        pw.append("<div class=\"content\">");
                        pw.append("<div class=\"providers\">");
                        ResponseURL responseURL = response.createURL();
                        registeredProviders.forEach(rp -> {
                            pw.append("<span class=\"provider\">");
                            final String url = responseURL.getURL(true);
                            final String confirmText = CONFIRM_UNLINK_FMT(rp.getDisplayName())
                                .getText(request.getLocaleContext()).toString();
                            final String okText = CommonButtonText.YES.getText(request.getLocaleContext()).toString();
                            final String cancelText = CommonButtonText.NO.getText(request.getLocaleContext()).toString();
                            final String unlinkText = UNLINK().getText(request.getLocaleContext()).toString();
                            pw.append("<form class=\"provider-unlink\" method=\"GET\"").appendEscapedAttribute("action", url)
                                .append("onSubmit=\""
                                        + "if(event.target.hasAttribute('data-confirmed')) {"
                                        + "return true;"
                                        + "}"
                                        + "event.preventDefault();"
                                        + "if(miwt && miwt.confirm) { "
                                        + "miwt.confirm('" + confirmText + "', '" + okText + "', '" + cancelText + "', function(c){"
                                        + "if(c) {"
                                        + "event.target.setAttribute('data-confirmed', c);"
                                        + "event.target.submit();"
                                        + '}'
                                        + "});"
                                        + "} else {"
                                        + "var c = confirm('" + confirmText + "');"
                                        + "if(c) {"
                                        + "event.target.setAttribute('data-confirmed', c);"
                                        + "event.target.submit();"
                                        + '}'
                                        + "} return false;"
                                        + '"')
                                .append('>');
                            pw.append("<input type=\"hidden\" name=\"").append(PARAM_UNLINK).append('"')
                                .appendEscapedAttribute("value", rp.getProgrammaticName()).append('>');
                            pw.append("<button type=\"submit\"")
                                .append(" class=\"button button-" + rp.getProgrammaticName() + '"')
                                .append(" style=\"background-image: url('" + providerIconsURL + "')\"")
                                .append(" title=\"" + unlinkText + ' ' + rp.getDisplayName() + '"')
                                .append("></button>");
                            pw.append("</form>");
                            pw.append("<span class=\"provider-name\">").append(rp.getDisplayName()).append("</span>");
                            pw.append("</span>");
                        });
                        pw.append("</div>");
                        pw.append("</div>");
                        pw.append("</div>");
                    }
                    boolean isLinkAndWillDisplayOptionsToLink = loginParams.getMode() == SocialLoginMode.Link
                                    && !Objects.equals(loginParams.getLoginProvidersString(registeredProviders), "[]");
                    if(isLinkAndWillDisplayOptionsToLink)
                    {
                        pw.append("<div class=\"oneall-providers-link\">");
                        pw.append("<div class=\"oneall-providers-link-title\">").append(LINK_ACCOUNTS()).append("</div>");
                    }
                    pw.append("<div id=\"oneall-providers-login\"'></div>");
                    if(isLinkAndWillDisplayOptionsToLink)
                    {
                        pw.append("</div>");
                    }
                    pw.append("<script async=\"async\" type=\"text/javascript\">\n")
                        //Setup OneAll Library
                        .append("var oneall_subdomain = '").append(_subdomain).append("';\n")
                        .append('\n')
                        .append("var oa = document.createElement('script');\n")
                        .append("oa.type = 'text/javascript';\n")
                        .append("oa.async = true;\n")
                        .append("oa.src = '//' + oneall_subdomain + '.api.oneall.com/socialize/library.js';\n")
                        .append("var s = document.getElementsByTagName('script')[0];\n")
                        .append("s.parentNode.insertBefore(oa, s);\n")
                        //Done Setting Up OneAll Library
                        .append('\n')
                        .append("\t/* Embeds the buttons into the container oneall_social_login */\n")
                        .append("var _oneall = _oneall || [];\n")
                        .append("_oneall.push(['social_login', 'set_callback_uri', '")
                        .append(loginParams.getCallbackURL().getURL(true))
                        .append("']);\n");

                    pw.append("_oneall.push(['social_login', 'set_providers', ");
                    if (loginParams.getMode() == SocialLoginMode.Link)
                    {
                        //Need to add only providers that user has not linked yet.
                        pw.append(loginParams.getLoginProvidersString(registeredProviders));
                    }
                    else
                    {
                        pw.append(loginParams.getLoginProvidersString());
                    }
                    pw
                        .append("]);\n")
                        .append("_oneall.push(['social_login', 'do_render_ui', 'oneall-providers-login']);\n")
                        .append(String.valueOf('\n'));
                    pw.append("</script>");

//                    if(Boolean.valueOf(loginParams.getContentBuilder().getProperty(PROP_SSO_ENABLED, "false")))
//                    {
//
//                        String ssoToken = request.getSession(Scope.SESSION).getString(SESSION_KEY_SSO_TOKEN, null);
//
//                        pw.append("<script async=\"async\" type=\"text/javascript\">\n")
//                            .append('\n')
//                            .append("var _oneall = _oneall || [];\n");
//                        if(!isEmptyString(ssoToken))
//                        {
//                            pw.append("_oneall.push(['single_sign_on', 'do_register_sso_session', '")
//                                .append(ssoToken)
//                                .append("']);\n");
//                        }
//                        else if(loginParams.getMode() == SocialLoginMode.Login
//                            && !Boolean.valueOf(request.getParameter(PARAM_SSO_CALLBACK, "false")))
//                        {
//                            String callbackURL = loginParams.getCallbackURL().addParameter(PARAM_SSO_CALLBACK, true).getURL(true);
//
//                            pw.append("_oneall.push(['single_sign_on', 'set_callback_uri', '")
//                                .append(callbackURL)
//                                .append("']);\n")
//                                .append("_oneall.push(['single_sign_on', 'do_check_for_sso_session']);\n");
//                        }
//                        pw.append("</script>");
//                    }
                }
            }
        };
    }

    private void unlinkIfSpecified(CmsRequest<SocialLoginElement> request, CmsResponse response, SocialLoginParams loginParams)
    {
        String unlinkProvider = request.getParameter(PARAM_UNLINK);
        Principal principal = _principalDAO.getCurrentPrincipal();
        if(isEmptyString(unlinkProvider) || principal == null) return;
        _oneAllDAO.deleteOneAllCredential(principal, unlinkProvider);
        ResponseURL redirect = response.createURL();
        request.getParameterMap().keySet().stream()
            .filter(k -> !Objects.equals(k, PARAM_UNLINK))
            .forEach(k -> redirect.addParameter(k, request.getParameter(k)));
        loginParams.getMessageAcceptor().accept(Message.info(SUCCESSFULLY_UNLINKED_FMT(
            loginParams.getProviderForProgrammaticName(unlinkProvider).getDisplayName())));
        response.redirect(redirect);
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return NAME();
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return DESCRIPTION();
    }

    @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    @Override
    public List<SocialLoginProvider> getSupportedProviders()
    {
        String reqURL = _apiEndpoint + "/providers.json";
        String responseString = safeSendAPIRequest(reqURL, "GET");
        if(!isEmptyString(responseString))
        {
            Gson gson = new GsonBuilder().create();
            APIProvidersResponseWrapper response = gson.fromJson(responseString, APIProvidersResponseWrapper.class);
            if(Objects.equals(response.response.request.status.flag, "success"))
            {
                _supportedProviders.clear();
                response.response.result.data.providers.entries.forEach(entry -> {
                    if(entry.isConfigured())
                    {
                        _supportedProviders.add(new SocialLoginProvider(entry.name, entry.key));
                    }
                });
            }
        }
        return _supportedProviders;
    }

    @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    @Override
    public LoginResult handleLoginCallback(Request request, Response response, SocialLoginParams loginParams)
    {
        if(isConfigured())
        {
            String connectionToken;
            if (!isEmptyString((connectionToken = request.getParameter(PARAM_CONNECTION_TOKEN))))
            {
                String reqURL = _apiEndpoint + "/connections/" + connectionToken + ".json";
                String responseString = safeSendAPIRequest(reqURL, "GET");
                if (!isEmptyString(responseString))
                {
                    Gson gson = new GsonBuilder().create();
                    APIConnectionsResponseWrapper res = gson.fromJson(responseString, APIConnectionsResponseWrapper.class);
                    if (Objects.equals(res.response.request.status.flag, "success"))
                    {
                        String userToken = res.response.result.data.user.user_token;
                        String identityToken = res.response.result.data.user.identity.identity_token;
                        String provider = res.response.result.data.user.identity.provider;

                        Principal toLogin = _oneAllDAO.getPrincipalForUserToken(userToken, provider,
                            AuthenticationDomainList.createDomainList(
                                request.getHostname().getDomain(),
                                request.getHostname().getSite().getDomain()));
                        if (loginParams.getMode() == SocialLoginMode.Link)
                            return doLink(loginParams, userToken, toLogin, request, identityToken, provider);
                        if (loginParams.getMode() == SocialLoginMode.Login)
                            return doLogin(loginParams, userToken, toLogin, request, identityToken, provider);
                    }
                }
            }
            else
            {
                loginParams.getMessageAcceptor().accept(Message.error(ERROR_NO_CONNECTION_TOKEN_RECEIVED()));
            }
        }
        return LoginResult.FAIL;
    }

    @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    private LoginResult doLogin(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin,
        Request request, String identityToken, String provider)
    {
        if(toLogin != null)
        {
            _principalDAO.authenticatePrincipalProgrammatically(toLogin, SHARED_SECRET,
                toLogin.getOpenAuthCredentials(SERVICE_IDENTIFIER, provider));
//            if(Boolean.valueOf(loginParams.getContentBuilder().getProperty(PROP_SSO_ENABLED, "false")))
//            {
//                if(!Boolean.valueOf(request.getParameter(PARAM_SSO_CALLBACK, "false"))
//                   && createSSOSession(request, identityToken))
//                    return LoginResult.SUCCESS_DO_REDIRECT_JAVASCRIPT;
//                if(Boolean.valueOf(request.getParameter(PARAM_SSO_CALLBACK, "false")))
//                {
//                    registerSSOLogoutCallback(request, identityToken);
//                }
//            }
            return LoginResult.SUCCESS_DO_REDIRECT;
        }

        loginParams.getMessageAcceptor().accept(Message.error(
            ERROR_USER_DOES_NOT_EXIST_FOR_USER_TOKEN(), ERROR_DETAILS_USER_DOES_NOT_EXIST_FOR_USER_TOKEN()));
        return LoginResult.FAIL;
    }

    private void registerSSOLogoutCallback(Request request, String identityToken)
    {
        final ServletSession session = request.getSession(Scope.SESSION);
        session.setString(SESSION_KEY_IDENTITY_TOKEN, identityToken);
        session.setObject(LoginLogoutHelper.SESSION_KEY_LOGOUT_CALLBACK, OneAllSSOLogoutCallback.class);
    }

    private boolean createSSOSession(Request request, String identityToken)
    {
        //If SSO Is Enabled for this Social Login component, then we instantiate a new SSO Session
        //And insert the sso token into the session
        String reqURL = _apiEndpoint + "/sso/sessions/identities/" + identityToken + ".json";
        String responseString = safeSendAPIRequest(reqURL, "PUT");
        if(!isEmptyString(responseString))
        {
            Gson gson = new GsonBuilder().create();
            APISSOSessionResponseWrapper res = gson.fromJson(responseString, APISSOSessionResponseWrapper.class);
            if(Objects.equals(res.response.request.status.flag, "created"))
            {
                String ssoToken = res.response.result.data.sso_session.sso_session_token;
                request.getSession(Scope.SESSION).setString(SESSION_KEY_SSO_TOKEN, ssoToken);
                registerSSOLogoutCallback(request, identityToken);
                return true;
            }
        }
        return false;
    }

    private LoginResult doLink(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin,
        Request request, String identityToken, String providerProg)
    {
        SocialLoginProvider provider = loginParams.getLoginProviders().stream()
            .filter(p -> Objects.equals(p.getProgrammaticName(), providerProg)).findFirst()
            .orElseGet(() -> new SocialLoginProvider(providerProg, providerProg));
        Principal current = _principalDAO.getCurrentPrincipal();
        if(current != null && (toLogin == null || Objects.equals(toLogin.getId(), current.getId())))
        {
            _oneAllDAO.createOneAllCredential(current, userToken, provider.getProgrammaticName());
            _principalDAO.authenticatePrincipalProgrammatically(current, SHARED_SECRET,
                current.getOpenAuthCredentials(SERVICE_IDENTIFIER, provider.getProgrammaticName()));
            loginParams.getMessageAcceptor().accept(Message.info(INFO_SUCCESSFULLY_LINKED_FMT(provider.getDisplayName())));
//            if(Boolean.valueOf(loginParams.getContentBuilder().getProperty(PROP_SSO_ENABLED, "false"))
//                && !Boolean.valueOf(request.getParameter(PARAM_SSO_CALLBACK, "false")))
//            {
//                if(createSSOSession(request, identityToken)) return LoginResult.SUCCESS_DO_REDIRECT_JAVASCRIPT;
//            }
            return LoginResult.SUCCESS_DO_REDIRECT;
        }
        if(current == null)
        {
            loginParams.getMessageAcceptor().accept(Message.error(ERROR_NOT_LOGGED_IN()));
        }
        if(toLogin != null && current != null
           && !Objects.equals(toLogin.getId(), current.getId()))
        {
            loginParams.getMessageAcceptor().accept(Message.error(ERROR_DIFFERENT_USER_EXISTS_FOR_USER_TOKEN()));
        }
        return LoginResult.FAIL;
    }

    @Override
    public List<String> getURLParametersToRemoveAfterCallback()
    {
        return ImmutableList.of("connection_token", "oa_social_login_token", "identity_vault_key", "oa_action");
    }

    @Override
    public String getServiceIdentifier()
    {
        return SERVICE_IDENTIFIER;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nonnull
    @Override
    public List<SocialLoginServiceEditor> createEditors()
    {
//        BooleanValueEditor ssoEnabledValueEditor = new BooleanValueEditor(LABEL_SSO_ENABLED(), null);
//        ssoEnabledValueEditor.addClassName(PROP_SSO_ENABLED);
//        SocialLoginServiceEditor sSOEnabledEditor = new SocialLoginServiceEditor(
//            PROP_SSO_ENABLED, ssoEnabledValueEditor, String::valueOf, Boolean::valueOf);
//        return Collections.singletonList(sSOEnabledEditor);
        return Collections.emptyList();
    }

    /**
     * Send an api request to OneAll.  This wraps {@link #sendAPIRequest(String, String)} and attempts to get the response from
     * the returned Future.  As a result, this is blocking, while {@link #sendAPIRequest(String, String)} is not.
     *
     * @param urlString the url string
     * @param method the method
     *
     * @return the string
     */
    @Nullable
    public String safeSendAPIRequest(@Nullable String urlString, @Nullable final String method)
    {
        try
        {
            return sendAPIRequest(urlString, method).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            _logger.error("Error occurred while recieving response from OneAll API:", e);
        }
        return null;
    }

    /**
     * Send an api request to OneAll
     *
     * @param urlString the url string
     * @param method the method
     *
     * @return the future
     */
    public Future<String> sendAPIRequest(@Nullable String urlString, @Nullable final String method)
    {
        return _executor.executorService().submit(() -> {
            String requestMethod = method;
            if(requestMethod == null) requestMethod = "GET";

            if(isConfigured() && !isEmptyString(urlString))
            {
                try
                {
                    String authString = _publicKey + ':' + _privateKey;
                    final String base64RawAuthString = new String(Base64.getEncoder().encode(authString.getBytes()));
                    String encodedAuthString = NEWLINE_PATTERN.matcher(base64RawAuthString).replaceAll("");

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod(requestMethod);
                    conn.setRequestProperty("Authorization", "Basic " + encodedAuthString);
                    if(!Objects.equals(requestMethod.toUpperCase(), "GET"))
                        conn.setFixedLengthStreamingMode(0);
                    conn.setDoOutput(true);
                    conn.setReadTimeout(10000);
                    conn.connect();
                    conn.getInputStream();

                    StringBuilder sb = new StringBuilder();
                    String line;

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())))
                    {
                        while((line = br.readLine()) != null)
                        {
                            sb.append(line);
                        }
                    }
                    return sb.toString();
                }
                catch (Exception e)
                {
                    _logger.error("Error recieving data from OneAll API: ", e);
                }
            }
            return null;
        });
    }

    /**
     * Is configured boolean.
     *
     * @return the boolean
     */
    public boolean isConfigured()
    {
        if(isEmptyString(_privateKey))
        {
            _privateKey = _environment.getProperty("oneall.private.key",
                _environment.getProperty("oneall_private_key"));
        }
        boolean isConfigured = !isEmptyString(_publicKey)
            && !isEmptyString(_privateKey)
            && !isEmptyString(_subdomain)
            && !isEmptyString(_apiEndpoint);
        if(!isConfigured)
        {
            _logger.warn("OneAll is not configured.  Should include the following values within properties file or environment:\n"
                         + "oneall.subdomain\n"
                         + "oneall.public.key\n"
                         + "oneall.private.key\n"
                         + "oneall.api.endpoint");
        }
        return isConfigured;
    }

    public static enum OneAllNDELibrary implements ClassPathResourceLibrary
    {
        ProviderCSS("social/login/oneall/oneall-providers.css", "text/css")
        ;

        @Component
        static class OneAllImageLibraryInjector
        {
            @Autowired private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

            @PostConstruct
            public void postConstruct()
            {
                for(OneAllNDELibrary img : EnumSet.allOf(OneAllNDELibrary.class))
                {
                    img.setLibraryHelper(_classPathResourceLibraryHelper);
                }
            }
        }

        private final String _classPath;
        private final List<ClassPathResourceLibrary> _dependencies;
        private final String _contentType;

        @SuppressWarnings("NonFinalFieldInEnum")
        private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

        @SuppressWarnings("unchecked")
        OneAllNDELibrary(String classPath, String contentType, ClassPathResourceLibrary... dependencies)
        {
            _classPath = classPath;
            _contentType = contentType;
            _dependencies = dependencies != null && dependencies.length > 0
                ? Arrays.asList(dependencies)
                : Collections.emptyList();
        }

        private ClassPathResourceLibraryHelper getLibraryHelper()
        {
            return _classPathResourceLibraryHelper;
        }

        private void setLibraryHelper(ClassPathResourceLibraryHelper libraryHelper)
        {
            _classPathResourceLibraryHelper = libraryHelper;
        }

        @Override
        public FactoryResource getResource()
        {
            return getLibraryHelper().createResource(this);
        }

        @Nullable
        @Override
        public NDE getNDE()
        {
            return getLibraryHelper().createNDE(this, true);
        }

        @Override
        public String getContentType()
        {
            return _contentType;
        }

        @Override
        public String getClassPath()
        {
            return _classPath;
        }

        @Override
        public List<? extends ClassPathResourceLibrary> getDependancies()
        {
            return _dependencies;
        }

        @Nullable
        @Override
        public LocalizedObjectKey getDescription()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return StringFactory.getBasename(_classPath);
        }
    }
    
    /**
     * Logout Callback Implementation for OneAll SSO
     * @author Alan Holt (aholt@venturetech.net)
     */
    @Configurable
    public static class OneAllSSOLogoutCallback implements LogoutCallback
    {
        @Autowired private OneAllLoginService _loginService;
        @Value("${oneall.api.endpoint:#{null}}") String _apiEndpoint;
        
        @Override
        public void doCallback(HttpServletRequest request, HttpServletResponse response)
        {
            if(_loginService.isConfigured())
            {
                HttpSession session = request.getSession(false);
                if(session != null)
                {
                    final Object identityTokenObj = session.getAttribute(SESSION_KEY_IDENTITY_TOKEN);
                    String identityToken = identityTokenObj != null ? String.valueOf(identityTokenObj) : null;
                    if(!isEmptyString(identityToken))
                    {
                        String url = _apiEndpoint + "/sso/sessions/identities/" + identityToken + ".json?confirm_deletion=true";
                        String responseString = _loginService.safeSendAPIRequest(url, "DELETE");
                        if(!isEmptyString(responseString))
                        {
                            Gson gson = new GsonBuilder().create();
                            APISSOSessionDestroyResponseWrapper res = gson.fromJson(
                                responseString, APISSOSessionDestroyResponseWrapper.class);
                            if (!Objects.equals(res.response.request.status.flag, "success"))
                            {
                                _logger.warn("SSO Session Destroy request to OneAll not successful: \n" + responseString);
                            }
                        }
                        else
                        {
                            _logger.warn("SSO Session Destroy request to OneAll failed: no response");
                        }
                    }
                    else
                    {
                        _logger.warn("Unable to perform OneALlSSOLogoutCallback. "
                                     + SESSION_KEY_IDENTITY_TOKEN + " was not specified within the session.");
                    }
                }
            }
        }
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings("unused")
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIStatus
    {
        public String flag;
        public Integer code;
        public String info;
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIRequest
    {
        public String date;
        public String resource;
        public APIStatus status;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIPagination
    {
        public static class APIPaginationOrder
        {
            public String field;
            public String direction;
        }
        public Integer current_page;
        public Integer total_pages;
        public Integer entries_per_page;
        public Integer total_entries;
        public APIPaginationOrder order;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIConnectionsResponseWrapper
    {
        @SuppressWarnings("unused")
        public static class APIConnectionsResponse
        {
            @SuppressWarnings("unused")
            public static class APIConnectionsResult
            {
                public APIStatus status;
                public APIConnectionsData data;
            }

            public APIRequest request;
            public APIConnectionsResult result;
        }

        public APIConnectionsResponse response;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIConnectionsData
    {
        @SuppressWarnings("unused")
        public static class APIConnection
        {
            public String connection_token;
            public String date;
            public String plugin;
        }

        @SuppressWarnings("unused")
        public static class APIConnectionUser
        {
            @SuppressWarnings("unused")
            public static class APIConnectionIdentity
            {
                public String identity_token;
                public String provider;
                public String id;
                public String displayName;
            }

            public String user_token;
            public String date;
            public APIConnectionIdentity identity;
        }

        public APIConnection connection;
        public APIConnectionUser user;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIProvidersResponseWrapper
    {
        @SuppressWarnings("unused")
        public static class APIProvidersResponse
        {
            @SuppressWarnings("unused")
            public static class APIProvidersResult
            {
                @SuppressWarnings("unused")
                public static class APIProvidersData
                {
                    public APIProviders providers;
                }

                public APIProvidersData data;
            }

            public APIRequest request;
            public APIProvidersResult result;
        }

        public APIProvidersResponse response;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIProviders
    {
        @SuppressWarnings("unused")
        public static class APIProvider
        {
            @SuppressWarnings("unused")
            public static class APIProviderConfiguration
            {
                @SuppressWarnings("unused")
                public static class APIProviderConsumerKeys
                {
                    public String consumer_id;
                    public String consumer_key;
                    public String consumer_secret;
                }

                public Boolean is_required;
                public Boolean is_completed;
            }

            public String name;
            public String key;
            public Boolean is_configurable;
            public APIProviderConfiguration configuration;

            /**
             * Is configured boolean.
             *
             * @return the boolean
             */
            public boolean isConfigured()
            {
                return !is_configurable || (!configuration.is_required || configuration.is_completed);
            }
        }

        public Integer count;
        public List<APIProvider> entries;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APISSOSessionResponseWrapper
    {
        @SuppressWarnings("unused")
        public static class APISSOSessionResponse
        {
            @SuppressWarnings("unused")
            public static class APISSOSessionResult
            {
                @SuppressWarnings("unused")
                public static class APISSOSessionData
                {
                    @SuppressWarnings("unused")
                    public static class APISSOSession
                    {
                        public String sso_session_token;
                        public String user_token;
                        public String identity_token;
                    }

                    public APISSOSession sso_session;
                }

                public APISSOSessionData data;
            }

            public APIRequest request;
            public APISSOSessionResult result;
        }

        public APISSOSessionResponse response;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APISSOSessionDestroyResponseWrapper
    {
        public static class APISSOSessionDestroyResponse
        {
            public APIRequest request;
        }

        public APISSOSessionDestroyResponse response;
    }
    //CHECKSTYLE:ON
}
