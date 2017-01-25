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

import com.example.app.login.oneall.model.OneAllDAO;
import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.example.app.login.social.ui.SocialLoginElement;
import com.example.app.login.social.ui.SocialLoginMode;
import com.example.app.login.social.ui.SocialLoginParams;
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
import net.proteusframework.internet.dataprovider.JavaScriptInitProvider;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.SSOType;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.login.oneall.service.OneAllLoginService.SERVICE_IDENTIFIER;
import static com.example.app.login.oneall.service.OneAllLoginServiceLOK.*;
import static net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_SECRET;

/**
 * OneAll Login Service
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/20/17
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
            l10n = @L10N("In order to link to a social network, you will have to log in first."))
    }
)
@Service(SERVICE_IDENTIFIER)
public class OneAllLoginService implements SocialLoginService
{
    private static final Logger _logger = LogManager.getLogger(OneAllLoginService.class);
    /** The constant SERVICE_IDENTIFIER. */
    public static final String SERVICE_IDENTIFIER = "oneall";
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
                if(isConfigured())
                {
                    request.getDataProviderContainer().registerDataProvider(new JavaScriptInitProvider(
                        "var oneall_subdomain = '" + _subdomain + "';\n"
                        + "\n"
                        + "/* The library is loaded asynchronously */\n"
                        + "var oa = document.createElement('script');\n"
                        + "oa.type = 'text/javascript'; oa.async = true;\n"
                        + "oa.src = '//' + oneall_subdomain + '.api.oneall.com/socialize/library.js';\n"
                        + "var s = document.getElementsByTagName('script')[0];\n"
                        + "s.parentNode.insertBefore(oa, s);"));
                }
            }

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
                    pw.append("<script type=\"text/javascript\">\n")
                        .append('\n')
                        .append("\t/* Embeds the buttons into the container oneall_social_login */\n")
                        .append("  var _oneall = _oneall || [];\n" + "  _oneall.push(['")
                        .append(modeString)
                        .append("', 'set_providers', ")
                        .append(loginParams.getLoginProvidersString())
                        .append("]);\n")
                        .append("  _oneall.push(['")
                        .append(modeString)
                        .append("', 'set_callback_uri', '")
                        .append(loginParams.getCallbackURI())
                        .append("']);\n");
                    if (loginParams.getMode() == SocialLoginMode.Link)
                    {
                        String userToken = getUserTokenForCurrentUser(request);
                        if (!StringFactory.isEmptyString(userToken))
                        {
                            pw.append("  _oneall.push(['")
                                .append(modeString)
                                .append("', 'set_user_token', '")
                                .append(userToken)
                                .append("']);\n");
                        }
                    }
                    pw
                        .append("  _oneall.push(['")
                        .append(modeString)
                        .append("', 'do_render_ui', 'oneall_social_login']);\n")
                        .append(String.valueOf('\n'));
                    pw.append("</script>");
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

    @Override
    public List<SocialLoginProvider> getSupportedProviders()
    {
        String reqURL = _apiEndpoint + "/providers.json";
        try
        {
            String responseString = sendAPIRequest(reqURL, "GET").get();
            if(!StringFactory.isEmptyString(responseString))
            {
                Gson gson = new GsonBuilder().create();
                APIProvidersResponseWrapper response = gson.fromJson(responseString, APIProvidersResponseWrapper.class);
                if(Objects.equals(response.response.request.status.flag, "success"))
                {
                    _supportedProviders.clear();
                    response.response.result.data.providers.entries.forEach(entry ->
                        _supportedProviders.add(new SocialLoginProvider(entry.name, entry.key)));
                }
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            _logger.error("Error occurred while recieving response from OneAll API:", e);
        }
        return _supportedProviders;
    }

    @Override
    public boolean handleLoginCallback(Request request, Response response, SocialLoginParams loginParams)
    {
        if(isConfigured())
        {
            String connectionToken;
            if (!StringFactory.isEmptyString((connectionToken = request.getParameter(PARAM_CONNECTION_TOKEN))))
            {
                String reqURL = _apiEndpoint + "/connections/" + connectionToken + ".json";
                try
                {
                    String responseString = sendAPIRequest(reqURL, "GET").get();
                    if (!StringFactory.isEmptyString(responseString))
                    {
                        Gson gson = new GsonBuilder().create();
                        APIConnectionsResponseWrapper res = gson.fromJson(responseString, APIConnectionsResponseWrapper.class);
                        if (Objects.equals(res.response.request.status.flag, "success"))
                        {
                            String userToken = res.response.result.data.user.user_token;

                            Principal toLogin = _oneAllDAO.getPrincipalForUserToken(userToken,
                                request.getHostname().getDomain(),
                                request.getHostname().getSite().getDomain());
                            if (loginParams.getMode() == SocialLoginMode.Link)
                                return doLink(loginParams, userToken, toLogin);
                            if (loginParams.getMode() == SocialLoginMode.Login)
                                return doLogin(loginParams, userToken, toLogin);
                        }
                    }
                }
                catch (InterruptedException | ExecutionException e)
                {
                    _logger.error("Error occurred while recieving response from OneAll API:", e);
                }
            }
            else
            {
                loginParams.getMessageAcceptor().accept(Message.error(ERROR_NO_CONNECTION_TOKEN_RECEIVED()));
            }
        }
        return false;
    }

    private boolean doLogin(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin)
        throws ExecutionException, InterruptedException
    {
        if(toLogin != null)
        {
            _principalDAO.authenticatePrincipalProgrammatically(toLogin, SHARED_SECRET,
                toLogin.getSSOCredentials(SSOType.other, "oneall"));
            return true;
        }
        //If no user exists in our system, instruct OneAll to delete the user that it created.
        sendAPIRequest(_apiEndpoint + "/users/" + userToken + ".json?confirm_deletion=true", "DELETE");

        loginParams.getMessageAcceptor().accept(Message.error(
            ERROR_USER_DOES_NOT_EXIST_FOR_USER_TOKEN(), ERROR_DETAILS_USER_DOES_NOT_EXIST_FOR_USER_TOKEN()));
        return false;
    }

    private boolean doLink(SocialLoginParams loginParams, String userToken, @Nullable Principal toLogin)
    {
        Principal current = _principalDAO.getCurrentPrincipal();
        if(loginParams.getMode() == SocialLoginMode.Link
           && current != null
           && (toLogin == null || Objects.equals(toLogin.getId(), current.getId())))
        {
            _oneAllDAO.createOneAllCredential(current, userToken);
            _principalDAO.authenticatePrincipalProgrammatically(current, SHARED_SECRET,
                current.getSSOCredentials(SSOType.other, "oneall"));
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
            return _oneAllDAO.getUserTokenForPrincipal(current,
                request.getHostname().getDomain(), request.getHostname().getSite().getDomain());
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

    public static class APIStatus
    {
        public String flag;
        public Integer code;
        public String info;
    }

    public static class APIRequest
    {
        public String date;
        public String resource;
        public APIStatus status;
    }

    @SuppressWarnings("InstanceVariableNamingConvention")
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
}
