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

import edu.emory.mathcs.backport.java.util.Collections;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.app.login.oneall.model.OneAllDAO;
import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.example.app.login.social.ui.SocialLoginElement;
import com.example.app.login.social.ui.SocialLoginMode;
import com.example.app.login.social.ui.SocialLoginParams;
import com.example.app.login.social.ui.SocialLoginServiceEditor;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.ProcessChain;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.config.ExecutorConfig;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.internet.http.Scope;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.ui.miwt.component.composite.editor.BooleanValueEditor;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.SSOType;
import net.proteusframework.users.model.dao.AuthenticationDomainList;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.login.oneall.service.OneAllLoginService.SERVICE_IDENTIFIER;
import static com.example.app.login.oneall.service.OneAllLoginServiceLOK.*;
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
            l10n = @L10N("SSO Enabled"))
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
    /** The constant PROP_SSO_ENABLED */
    public static final String PROP_SSO_ENABLED = "sso-enabled";
    /** The constant PARAM_SSO_CALLBACK */
    public static final String PARAM_SSO_CALLBACK = "sso-callback";

    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\n\r]");
    private static final String PARAM_CONNECTION_TOKEN = "connection_token";

    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private ExecutorConfig _executor;
    @Autowired private OneAllDAO _oneAllDAO;
    @Value("${oneall.subdomain}") String _subdomain;
    @Value("${oneall.public.key}") String _publicKey;
    @Value("${oneall.private.key}") String _privateKey;
    @Value("${oneall.api.endpoint}") String _apiEndpoint;

    private final List<SocialLoginProvider> _supportedProviders = new ArrayList<>();

    @Override
    public Renderer<SocialLoginElement> createLoginRenderer(SocialLoginParams loginParams)
    {
        return new Renderer<SocialLoginElement>()
        {
            @Override
            public void preRenderProcess(CmsRequest<SocialLoginElement> request, CmsResponse response, ProcessChain chain)
            {
                //Do Nothing.
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<NDE> getNDEs()
            {
                return Collections.emptyList();
            }

            @Override
            public void render(CmsRequest<SocialLoginElement> request, CmsResponse response, RenderChain chain) throws IOException
            {
                if(isConfigured())
                {
                    String modeString = getModeString(loginParams.getMode());
                    EntityUtilWriter pw = response.getContentWriter();
                    pw.append("<div id='oneall_social_login'></div>");
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
                        .append("_oneall.push(['")
                        .append(modeString)
                        .append("', 'set_providers', ")
                        .append(loginParams.getLoginProvidersString())
                        .append("]);\n")
                        .append("_oneall.push(['")
                        .append(modeString)
                        .append("', 'set_callback_uri', '")
                        .append(loginParams.getCallbackURL().getURL(true))
                        .append("']);\n");
                    if (loginParams.getMode() == SocialLoginMode.Link)
                    {
                        String userToken = getUserTokenForCurrentUser(request);
                        if (!StringFactory.isEmptyString(userToken))
                        {
                            pw.append("_oneall.push(['")
                                .append(modeString)
                                .append("', 'set_user_token', '")
                                .append(userToken)
                                .append("']);\n");
                        }
                    }
                    pw
                        .append("_oneall.push(['")
                        .append(modeString)
                        .append("', 'do_render_ui', 'oneall_social_login']);\n")
                        .append(String.valueOf('\n'));
                    pw.append("</script>");

                    if(Boolean.valueOf(loginParams.getContentBuilder().getProperty(PROP_SSO_ENABLED, "false")))
                    {
                        String ssoToken = request.getSession(Scope.SESSION).getString(SESSION_KEY_SSO_TOKEN, null);
                        String callbackURL = loginParams.getCallbackURL().addParameter(PARAM_SSO_CALLBACK, true).getURL(true);

                        pw.append("<script async=\"async\" type=\"text/javascript\">\n")
                            .append('\n')
                            .append("var _oneall = _oneall || [];\n");
                        if(StringFactory.isEmptyString(ssoToken))
                        {
                            pw.append("_oneall.push(['single_sign_on', 'set_callback_uri', '")
                                .append(callbackURL)
                                .append("']);\n")
                                .append("_oneall.push(['single_sign_on', 'do_check_for_sso_session']);\n");
                        }
                        else
                        {
                            pw.append("_oneall.push(['single_sign_on', 'do_register_sso_session', '")
                                .append(ssoToken)
                                .append("']);\n");
                        }
                        pw.append("</script>");
                    }
                }
            }
        };
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
        if(!StringFactory.isEmptyString(responseString))
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
    public boolean handleLoginCallback(Request request, Response response, SocialLoginParams loginParams)
    {
        if(isConfigured())
        {
            String connectionToken;
            if (!StringFactory.isEmptyString((connectionToken = request.getParameter(PARAM_CONNECTION_TOKEN))))
            {
                String reqURL = _apiEndpoint + "/connections/" + connectionToken + ".json";
                String responseString = safeSendAPIRequest(reqURL, "GET");
                if (!StringFactory.isEmptyString(responseString))
                {
                    Gson gson = new GsonBuilder().create();
                    APIConnectionsResponseWrapper res = gson.fromJson(responseString, APIConnectionsResponseWrapper.class);
                    if (Objects.equals(res.response.request.status.flag, "success"))
                    {
                        String userToken = res.response.result.data.user.user_token;
                        String identityToken = res.response.result.data.user.identity.identity_token;

                        Principal toLogin = _oneAllDAO.getPrincipalForUserToken(userToken,
                            AuthenticationDomainList.createDomainList(
                                request.getHostname().getDomain(),
                                request.getHostname().getSite().getDomain()));
                        if (loginParams.getMode() == SocialLoginMode.Link)
                            return doLink(loginParams, userToken, toLogin);
                        if (loginParams.getMode() == SocialLoginMode.Login)
                            return doLogin(loginParams, userToken, toLogin, request, identityToken);
                    }
                }
            }
            else
            {
                loginParams.getMessageAcceptor().accept(Message.error(ERROR_NO_CONNECTION_TOKEN_RECEIVED()));
            }
        }
        return false;
    }

    @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    private boolean doLogin(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin,
        Request request, String identityToken)
    {
        if(toLogin != null)
        {
            _principalDAO.authenticatePrincipalProgrammatically(toLogin, SHARED_SECRET,
                toLogin.getSSOCredentials(SSOType.other, SERVICE_IDENTIFIER));
            if(Boolean.valueOf(loginParams.getContentBuilder().getProperty(PROP_SSO_ENABLED, "false"))
                && !Boolean.valueOf(request.getParameter(PARAM_SSO_CALLBACK, "false")))
            {
                //If SSO Is Enabled for this Social Login component, then we instantiate a new SSO Session
                //And insert the sso token into the session
                String reqURL = _apiEndpoint + "/sessions/identities/" + identityToken + ".json";
                String responseString = safeSendAPIRequest(reqURL, "PUT");
                if(!StringFactory.isEmptyString(responseString))
                {
                    Gson gson = new GsonBuilder().create();
                    APISSOSessionResponseWrapper res = gson.fromJson(responseString, APISSOSessionResponseWrapper.class);
                    if(Objects.equals(res.response.request.status.flag, "success"))
                    {
                        String ssoToken = res.response.result.data.sso_session.sso_session_token;
                        request.getSession(Scope.SESSION).setString(SESSION_KEY_SSO_TOKEN, ssoToken);
                    }
                }
            }
            return true;
        }
        if(_oneAllDAO.getPrincipalForUserToken(userToken, AuthenticationDomainList.emptyDomainList()) == null)
        {
            //If no user exists in our system, instruct OneAll to delete the user that it created.
            sendAPIRequest(_apiEndpoint + "/users/" + userToken + ".json?confirm_deletion=true", "DELETE");
        }

        loginParams.getMessageAcceptor().accept(Message.error(
            ERROR_USER_DOES_NOT_EXIST_FOR_USER_TOKEN(), ERROR_DETAILS_USER_DOES_NOT_EXIST_FOR_USER_TOKEN()));
        return false;
    }

    private boolean doLink(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin)
    {
        Principal current = _principalDAO.getCurrentPrincipal();
        if(current != null && (toLogin == null || Objects.equals(toLogin.getId(), current.getId())))
        {
            _oneAllDAO.createOneAllCredential(current, userToken);
            _principalDAO.authenticatePrincipalProgrammatically(current, SHARED_SECRET,
                current.getSSOCredentials(SSOType.other, SERVICE_IDENTIFIER));
            return true;
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
        return false;
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
        BooleanValueEditor ssoEnabledValueEditor = new BooleanValueEditor(LABEL_SSO_ENABLED(), null);
        ssoEnabledValueEditor.addClassName(PROP_SSO_ENABLED);
        SocialLoginServiceEditor<Boolean> sSOEnabledEditor = new SocialLoginServiceEditor<>(
            PROP_SSO_ENABLED, ssoEnabledValueEditor, String::valueOf, Boolean::valueOf);
        return Collections.singletonList(sSOEnabledEditor);
    }

    @Nullable
    private String safeSendAPIRequest(@Nullable String urlString, @Nullable final String method)
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

    @Nullable
    private Future<String> sendAPIRequest(@Nullable String urlString, @Nullable final String method)
    {
        return _executor.executorService().submit(() -> {
            String requestMethod = method;
            if(requestMethod == null) requestMethod = "GET";

            if(isConfigured() && !StringFactory.isEmptyString(urlString))
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
                    conn.setDoOutput(true);
                    conn.setReadTimeout(10000);
                    conn.connect();
                    conn.getInputStream();

                    StringBuilder sb = new StringBuilder();
                    String line;

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())))
                    {
                        while((line = br.readLine()) != null) {
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

    private boolean isConfigured()
    {
        boolean isConfigured = !StringFactory.isEmptyString(_publicKey)
            && !StringFactory.isEmptyString(_privateKey)
            && !StringFactory.isEmptyString(_subdomain)
            && !StringFactory.isEmptyString(_apiEndpoint);
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

    @Nullable
    private String getUserTokenForCurrentUser(Request request)
    {
        Principal current = _principalDAO.getCurrentPrincipal();
        if(current != null)
        {
            return _oneAllDAO.getUserTokenForPrincipal(current, AuthenticationDomainList.createDomainList(
                request.getHostname().getDomain(), request.getHostname().getSite().getDomain()));
        }
        return null;
    }

    private static String getModeString(SocialLoginMode mode)
    {
        switch(mode)
        {
            case Link:
                return "social_link";
            case Login:
            default:
                return "social_login";
        }
    }

    //CHECKSTYLE:OFF
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

    @SuppressWarnings("InstanceVariableNamingConvention")
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIConnectionsResponseWrapper
    {
        public static class APIConnectionsResponse
        {
            public static class APIConnectionsResult
            {
                public static class APIConnectionsData
                {
                    public static class APIConnection
                    {
                        public String connection_token;
                        public String date;
                        public String plugin;
                    }

                    public static class APIConnectionUser
                    {
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
                public APIStatus status;
                public APIConnectionsData data;
            }

            public APIRequest request;
            public APIConnectionsResult result;
        }

        public APIConnectionsResponse response;
    }

    @SuppressWarnings("InstanceVariableNamingConvention")
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APIProvidersResponseWrapper
    {
        public static class APIProvidersResponse
        {
            public static class APIProvidersResult
            {
                public static class APIProvidersData
                {
                    public static class APIProviders
                    {
                        public static class APIProvider
                        {
                            public static class APIProviderConfiguration
                            {
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

                    public APIProviders providers;
                }

                public APIProvidersData data;
            }

            public APIRequest request;
            public APIProvidersResult result;
        }

        public APIProvidersResponse response;
    }

    @SuppressWarnings("InstanceVariableNamingConvention")
    @SuppressFBWarnings({
        "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
        "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD",
        "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"
    })
    public static class APISSOSessionResponseWrapper
    {
        public static class APISSOSessionResponse
        {
            public static class APISSOSessionResult
            {
                public static class APISSOSessionData
                {
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
    //CHECKSTYLE:ON
}
