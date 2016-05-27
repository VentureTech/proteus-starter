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

package ScriptedPageElementFactory.WebService

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.i2rd.cms.backend.files.FileChooser
import com.i2rd.cms.bean.scripted.GroovyScriptedPageElementFactory
import com.i2rd.cms.bean.scripted.ScriptedBase
import com.i2rd.cms.bean.scripted.ScriptedPageElementImpl
import com.i2rd.cms.bean.scripted.ScriptedPageElementModelImpl
import com.i2rd.cms.bean.util.JavaScriptBeanContentBuilder
import com.i2rd.cms.codecompletion.impl.StandardDomains
import com.i2rd.cms.editor.ValidationState
import com.i2rd.cms.miwt.CodeEditor
import com.i2rd.completion.CompletionDomainDefinition
import com.i2rd.completion.HelpCodeCompletionProvider
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.proteusframework.cms.CmsSite
import net.proteusframework.cms.FileSystemDirectory
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.component.content.ContentBuilder
import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor
import net.proteusframework.cms.component.editor.EditorUI
import net.proteusframework.cms.component.generator.ContentWrapper
import net.proteusframework.cms.component.generator.GeneratorImpl
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.cms.support.HTMLPageElementUtil
import net.proteusframework.core.StringFactory
import net.proteusframework.core.html.HTMLDoctype
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.io.EntityUtilWriter
import net.proteusframework.core.locale.TextSources
import net.proteusframework.core.net.ContentTypes
import net.proteusframework.core.net.LenientContentType
import net.proteusframework.core.notification.Notifiable
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.data.filesystem.FileSystemDAO
import net.proteusframework.internet.http.Scope
import net.proteusframework.internet.http.resource.html.NDE
import net.proteusframework.internet.http.resource.html.NDEType
import net.proteusframework.internet.http.resource.html.URINDE
import net.proteusframework.ui.miwt.component.Container
import net.proteusframework.ui.miwt.component.Dialog
import net.proteusframework.ui.miwt.component.Field
import net.proteusframework.ui.miwt.component.Label as LabelComponent
import net.proteusframework.ui.miwt.component.PushButton
import net.proteusframework.ui.miwt.component.TabItemDisplay
import net.proteusframework.ui.miwt.component.composite.Message
import net.proteusframework.ui.miwt.event.ActionEvent
import net.proteusframework.ui.miwt.event.ActionListener
import net.proteusframework.ui.miwt.util.CommonActions
import net.proteusframework.ui.miwt.util.CommonButtonText
import org.apache.logging.log4j.Logger

/**
 * When configured with a service account ID and private key file, uses the GoogleAnalyticsProfile for the
 * current site to get an access token.  
 * Outputs the access token to JavaScript as a global variable "googleAnalyticsToken".  The token can
 * access Google Analytics through the service account for 1 hour.  Outputs the Property ID as a JavaScript
 * global variable "googleAnalyticsPropertyId", and Account ID as "googleAnalyticsAccountId". 
 * This component just authenticates and provides a token to JavaScript.  
 * Requires additional JS to make the Analytics and Chart API calls.  Do not use this on a public site.
 * @author Jonathan Crosmer (jcrosmer@i2rd.com)
 * @author Russ Tennant (russ@i2rd.com)
 */
@CompileStatic
class GoogleAnalyticsComponent extends ScriptedPageElementModelImpl
{
    Logger logger
    def getEditor(Class editorType)
    {
        return new GoogleAnalyticsEditor(logger:logger)
    }
}
@CompileStatic
enum GoogleAnalyticsProperty
{
    privateKeyFileId,
    serviceAccountId,
    ndes,
    javascript
}

@CompileStatic
class GoogleAnalyticsEditor extends ContentBuilderBasedEditor<ContentBuilder<GoogleAnalyticsProperty>>
{
    static final DEFAULT_MARKUP = '''\
        <ndes>
            <!-- Include external CSS or JavaScript as an NDE. Any number of urinde elements may
                 appear here. Each has a uri attribute, which may be any valid URI, and a type
                 attribute, which may be either CSS or JS.

                 Examples:

                 <urinde type="CSS" uri="//example.com/style.css" />
                 <urinde type="JS" uri="//example.com/script.js" />
            -->

        </ndes>
    ''' . stripIndent()

    Logger logger
    String fileId
    Field serviceAccountIdField
    Field ndes = new Field('', 80, 20)
    CodeEditor javascriptField;

    GoogleAnalyticsEditor()
    {
        super(ContentBuilder.class)
    }

    @Override
    void createUI(EditorUI editorUI)
    {
        super.createUI(editorUI);
        def builder = getBuilder();

        serviceAccountIdField = new Field()
        serviceAccountIdField.setDisplayWidth(120)
        serviceAccountIdField.setText(builder.getProperty(GoogleAnalyticsProperty.serviceAccountId))

        fileId = builder.getProperty(GoogleAnalyticsProperty.privateKeyFileId)
        LabelComponent keyText = new LabelComponent(TextSources.createText('No key file selected'))

        if (fileId)
        {
            try
            {
                FileEntity fe = FileSystemDAO.instance.load(Long.parseLong(fileId), FileEntity.class)
                keyText.setText(TextSources.createText("Key file selected: ${fe.getName()}"))
            }
            catch (Exception ex)
            {
                logger.debug("Error loading file for GoogleAnalyticsComponent: " + fileId, ex)
                fileId = null
            }
        }

        PushButton select = CommonActions.SELECT.push()
        select.addActionListener({
            final FileChooser fileChooser = new FileChooser(
                FileSystemDirectory.getRootDirectory(CmsFrontendDAO.instance.operationalSite))
            final Dialog dialog = new Dialog(editorUI.getApplication(), CommonButtonText.SELECT_FILE)
            dialog.addClassName("file-dialog")
            dialog.add(fileChooser)

            fileChooser.addActionListener({ActionEvent evt ->
                FileEntity fe = evt.getSource() as FileEntity
                fileId = fe.getId()
                keyText.setText(TextSources.createText("Key file selected: ${fe.getName()}"))
                dialog.close()
            } as ActionListener)

            editorUI.getWindowManager().add(dialog)
            dialog.setVisible(true)
        } as ActionListener)

        Container mainCon = Container.of('analytics-con')
        Container ndeCon = Container.of('nde-con')
        Container javascriptCon = Container.of('javascript-con')
        mainCon.add(Container.of('prop service-account',
            new LabelComponent(TextSources.createText('Service Account Email Address')),
            serviceAccountIdField))
        mainCon.add(keyText)
        mainCon.add(Container.of('prop key-file',
            new LabelComponent(TextSources.createText('Private key file for service account')), keyText, select))

        ndes.text = builder.getData(GoogleAnalyticsProperty.ndes) ?: DEFAULT_MARKUP
        ndeCon.add(Container.of('prop ndes', TextSources.createText('JS / CSS'), ndes))

        javascriptField = new CodeEditor(CodeEditor.Language.javascript, 80, 20);
        final List<CompletionDomainDefinition> domains = new ArrayList<>();
        domains.add(HelpCodeCompletionProvider.HELP_DOMAIN_DEFINITION);
        domains.add(StandardDomains.getCSSImageDomain());
        domains.add(StandardDomains.getCSSInfoDomain());
        javascriptField.setDomains(domains);
        javascriptField.addClassName("javascript-content");
        final String data = builder.getData(GoogleAnalyticsProperty.javascript);
        if (data != null)
        {
            javascriptField.setText(data);
        }
        javascriptCon.add(javascriptField)
        javascriptCon.add(new LabelComponent(TextSources.createText(
'Note: <script> tags will be output automatically. You only need to provide the content of the script tag.'))
            .withHTMLElement(HTMLElement.p).addClassName('instructions'))

        editorUI.addTab(new TabItemDisplay(TextSources.createText('Analytics'), 'analytics'), mainCon)
        editorUI.addTab(new TabItemDisplay(TextSources.createText('NDEs'), 'ndes'), ndeCon)
        editorUI.addTab(new TabItemDisplay(TextSources.createText('Javascript'), 'javascript'), javascriptCon)
    }

    @Override
    protected void _updateBuilder()
    {
        builder.setProperty(GoogleAnalyticsProperty.serviceAccountId, serviceAccountIdField.getText())
        builder.setProperty(GoogleAnalyticsProperty.privateKeyFileId, fileId)
        builder.setData(GoogleAnalyticsProperty.ndes, ndes.text, null)
        builder.setData(GoogleAnalyticsProperty.javascript, javascriptField.text, null)
    }

    @Override
    public ValidationState validate(final Notifiable errors)
    {
        if (JavaScriptBeanContentBuilder.getScriptTagCount(javascriptField.getText()) > 1)
        {
            errors.sendNotification(Message.error(TextSources.createText("Cannot have multiple script tags")));
            return ValidationState.invalid_modeldata;
        }
        return ValidationState.valid;
    }

}
@CompileStatic
class GoogleAnalyticsGenerator extends GeneratorImpl<ScriptedBase>
{
    Logger logger

    final List<NDE> ndeList = []
    ContentBuilder contentBuilder

    GoogleAnalyticsGenerator()
    {
        setScope(Scope.REQUEST)
    }

    @Override
    void preRenderProcess(CmsRequest<ScriptedBase> request, CmsResponse response, ProcessChain chain)
    {
        super.preRenderProcess(request, response, chain)
        contentBuilder = ContentBuilder.load(request.pageElementData, ContentBuilder.class, true)

        final url = response.createURL()
        url.absolute = true
        url.partial = true
        url.contentType = ContentTypes.Text.javascript.contentType
        final uri = new URI(url.getURL(true))
        def urinde = new URINDE(URI: uri, type: NDEType.JS)
        ndeList.add(urinde)

        loadURINDEs(request, response)
    }

    void renderJavascriptData(CmsRequest<ScriptedBase> request, CmsResponse response)
    {
        final String javascriptData = contentBuilder.getData(GoogleAnalyticsProperty.javascript)
        if (!StringFactory.isEmptyString(javascriptData))
        {
            EntityUtilWriter contentWriter = response.contentWriter
            contentWriter.append(HTMLPageElementUtil.getOpeningJavascriptScriptTag(request.getNegotiatedContentType()));
            contentWriter.append(javascriptData);
            contentWriter.append(HTMLPageElementUtil.getClosingJavascriptScriptTag(request.getNegotiatedContentType()));
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void loadURINDEs(CmsRequest<ScriptedBase> request, CmsResponse response)
    {
        try
        {
            String ndeData = contentBuilder.getData(GoogleAnalyticsProperty.ndes)
            if (ndeData)
            {
                def list = []
                final parser = new XmlParser()
                final ndes = parser.parseText(ndeData)
                ndes.urinde.each { node ->
                    final url = response.createURL()
                    url.absolute = true
                    final base = new URI(url.getURL(true))
                    final uri = base.resolve(node.@uri)
                    if(logger.isDebugEnabled())
                        logger.debug "$url $base $uri"
                    def urinde = new URINDE(URI: uri, type: NDEType.valueOf(node.@type as String))
                    list.add(urinde)
                }
                ndeList.addAll(list)
            }
        }
        catch (final e)
        {
            logger.warn "Unexpected ${e.getClass().simpleName} parsing NDEs", e
        }
    }

    @Override
    List<NDE> getNDEs()
    {
        return ndeList
    }


    @Override
    void render(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain) throws IOException
    {
        if(request.getNegotiatedContentType().match(ContentTypes.Text.javascript.contentType))
        {
            renderToken(request, response, chain)
        }
        else
        {
            final ContentWrapper wrapper = new ContentWrapper(request, response)
            wrapper.setStandardClassName('google-analytics-component', HTMLDoctype.html4transitional)
            wrapper.open()

            renderJavascriptData(request, response)

            wrapper.close()
        }
    }

    void renderToken(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain)
    {
        String serviceAccountId = contentBuilder.getProperty(GoogleAnalyticsProperty.serviceAccountId)
        String fileId = contentBuilder.getProperty(GoogleAnalyticsProperty.privateKeyFileId)
        com.i2rd.cms.analytics.GoogleAnalyticsProfile profile = CmsFrontendDAO.instance.
            getActiveAnalyticsProfile(CmsFrontendDAO.instance.getOperationalSite())

        EntityUtilWriter contentWriter = response.getContentWriter()
        if (!profile)
        {
            contentWriter.println(
'// <!-- No Google Analytics profile configured for the current site.  Contact support to enable Google Analytics ' +
 'for this site. -->')
            return
        }
        if (!serviceAccountId)
        {
            contentWriter.println('// <!-- No Google service account id configured -->')
            return
        }
        if (!fileId)
        {
            contentWriter.println('// <!-- No Google service account private key file configured -->')
            return
        }

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        String serviceAccountScopes = 'https://www.googleapis.com/auth/analytics.readonly';
        // Note: the thing that GoogleAnalyticsProfile calls a "profile" is actually a "web property ID",
        // where Google uses "profile" to mean a "view" of data from a property.
        String propertyId = profile.getProfileId()
        String accountId = StringFactory.PATTERN_DASH.split(propertyId)[1]
        // Need to parse 123 from format UA-123-1

        CmsSite site = request.getSite()
        FileSystemDAO fsp = FileSystemDAO.instance
        FileEntity fe
        try
        {
            fe = FileSystemDAO.instance.load(Long.parseLong(fileId), FileEntity.class)
        }
        catch (Exception ex)
        {
            def message = "Error loading private key file: FileEntity#${fileId}"
            logger.debug(message, ex)
            contentWriter.println("// <!-- $message -->")
            return
        }

//        InputStream inputStream = fsp.getStream(fe)
//        KeyStore ks = KeyStore.getInstance("PKCS12");
//        try
//        {
//            ks.load(inputStream, null);
//        }
//        catch (Exception ex)
//        {
//            def message = "Error parsing private key file: ${fe.getName()}"
//            logger.debug(message, ex)
//            contentWriter.println("// <!-- $message -->")
//            return
//        }
//        // Alias and password set by Google:
//        PrivateKey key = (PrivateKey) ks.getKey('privatekey', 'notasecret'.toCharArray());
//
        def data = fsp.retrieve(new FileSystemDAO.RetrieveRequest(fe, request))
        def bufferedStream = data.openBufferedStream()
        try
        {

            GoogleCredential credential = GoogleCredential.fromStream(bufferedStream, httpTransport, jsonFactory)
                .createScoped([serviceAccountScopes])

            credential.refreshToken()
            String token = credential.getAccessToken()

            contentWriter.println("""
var googleAnalyticsPropertyId = "${propertyId}";
var googleAnalyticsAccountId = "${accountId}";
var googleAnalyticsToken = "${token}";
""")
        }
        catch(Throwable t)
        {
            logger.info('Unable to get token.', t);
            contentWriter.append('\n<!-- Unable to get token: ').appendEscapedData(t.message).println(' -->')
        }
        finally
        {
            bufferedStream.close();
        }
//        GoogleCredential credential = (new GoogleCredential.Builder()
//            .setTransport(httpTransport)
//            .setJsonFactory(jsonFactory)
//            .setServiceAccountId(serviceAccountId)
//            .setServiceAccountScopes([serviceAccountScopes])
//            .setServiceAccountPrivateKey(key)
//            .build())

//        try
//        {
//            credential.refreshToken()
//            String token = credential.getAccessToken()
//
//            contentWriter.println("""
//var googleAnalyticsPropertyId = "${propertyId}"
//var googleAnalyticsAccountId = "${accountId}"
//var googleAnalyticsToken = "${token}"
//""")
//        }
//        catch(IOException ioe)
//        {
//            logger.info('Unable to get token.', ioe);
//            contentWriter.append('\n<!-- Unable to get token: ').appendEscapedData(ioe.message).println(' -->')
//        }
    }
}

class GoogleAnalyticsPageElement extends ScriptedPageElementImpl
{
    Logger logger
    GoogleAnalyticsPageElement(ScriptedBase basePageElement, Logger logger)
    {
        super(basePageElement)
        this.logger = logger
    }

    @Override
    net.proteusframework.cms.component.generator.Generator<? extends PageElement> getGenerator(
        CmsRequest<? extends PageElement> request)
    {
        LenientContentType contentType = request.getNegotiatedContentType()
        if (!(HTMLPageElementUtil.isWebContentType(contentType)
            || ContentTypes.Text.javascript.contentType.match(contentType)
            || ContentTypes.Application.javascript.contentType.match(contentType))
        )
        {
            return null;
        }
        new GoogleAnalyticsGenerator(logger:logger)
    }

}

factory = new GroovyScriptedPageElementFactory(
    {new GoogleAnalyticsComponent(logger:_logger)},
    {ScriptedBase base -> new GoogleAnalyticsPageElement(base, _logger)},
    context)