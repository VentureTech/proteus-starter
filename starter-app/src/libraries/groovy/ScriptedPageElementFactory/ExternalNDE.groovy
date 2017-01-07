import com.i2rd.cms.bean.scripted.GroovyScriptedPageElementFactory
import com.i2rd.cms.bean.scripted.ScriptedBase
import com.i2rd.cms.bean.scripted.ScriptedPageElementImpl
import com.i2rd.cms.bean.scripted.ScriptedPageElementModelImpl
import groovy.transform.CompileStatic
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.component.content.ContentBuilder
import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor
import net.proteusframework.cms.component.editor.EditorUI
import net.proteusframework.cms.component.generator.Generator
import net.proteusframework.cms.component.generator.GeneratorImpl
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.io.EntityUtilWriter
import net.proteusframework.core.locale.TextSource
import net.proteusframework.internet.http.resource.StringResource
import net.proteusframework.internet.http.resource.html.NDEType
import net.proteusframework.internet.http.resource.html.StringNDE
import net.proteusframework.internet.http.resource.html.URINDE
import net.proteusframework.ui.miwt.component.Container
import net.proteusframework.ui.miwt.component.Field
import org.apache.logging.log4j.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import static java.nio.charset.StandardCharsets.UTF_8
import static net.proteusframework.cms.controller.NDEHelper.NDE_ATTRIBUTE_LOCATION
import static net.proteusframework.cms.controller.NDEHelper.NDE_ATTRIBUTE_LOCATION_VALUE_HEAD
import static net.proteusframework.core.locale.TextSources.createText
import static net.proteusframework.internet.http.resource.html.NDEType.JS

@CompileStatic
class ExternalNDEComponent
    extends ScriptedPageElementModelImpl
{
    def getEditor(Class editorType)
    {
        new ExternalNDEEditor()
    }
}

@CompileStatic
enum ExternalNDEProperty {
    ndes
}

@CompileStatic
class ExternalNDEEditor
    extends ContentBuilderBasedEditor<ContentBuilder<ExternalNDEProperty>>
{
    final TextSource LABEL_NDES = createText('NDEs')

    static final DEFAULT_MARKUP = '''\
<ndes>
<!--
\tInclude external CSS or JavaScript as an NDE. Any number of urinde or stringnde
\telements may appear here. Each urinde has a uri attribute, which may be any
\tvalid URI, and a type attribute, which may be either CSS or JS.

\tExamples:

\t<urinde type="CSS" uri="//example.com/style.css" />
\t<urinde type="JS" uri="//example.com/script.js" />

\tThe stringnde element allows you to specify the link content directly.

\tExample:

\t<stringnde>
\t\t<link rel="manifest" href="/icons/manifest.json"/>
\t\t<link rel="mask-icon" href="/icons/safari-pinned-tab.svg" color="#fcc331"/>
\t</stringnde>
-->

</ndes>
'''

    Field ndes = new Field('', 80, 20)

    ExternalNDEEditor()
    {
        super(ContentBuilder.class)
    }

    protected void _updateBuilder()
    {
        builder.setProperty(ExternalNDEProperty.ndes, ndes.text)
    }

    void createUI(final EditorUI editor)
    {
        super.createUI(editor);

        ndes.text = builder.getProperty(ExternalNDEProperty.ndes) ?: DEFAULT_MARKUP
        editor.addComponent Container.of(HTMLElement.div, 'prop text ndes', LABEL_NDES, ndes)
    }
}

@CompileStatic
class ExternalNDEGenerator
    extends GeneratorImpl
{
    final Logger logger = ExternalNDELogger.getLogger(getClass())

    final List NDEs = []

    public void preRenderProcess(final CmsRequest req, final CmsResponse resp, final ProcessChain pc)
    {
        try
        {
            final builder = ContentBuilder.load(req.pageElementData, ContentBuilder, true)
            def ndesContent = builder.getProperty(ExternalNDEProperty.ndes)
            def document = Jsoup.parse(ndesContent)
            Elements ndes = document.select("urinde")
            ndes.each {Element el ->
                final url = resp.createURL()
                url.absolute = true
                final base = new URI(url.getURL(true))
                final uri = base.resolve(el.attr('uri'))
                NDEs << new URINDE(URI: uri, type: NDEType.valueOf(el.attr('type')))
            }
            ndes = document.select("stringnde")
            ndes.each {Element el ->
                final sw = new StringWriter()
                final pw = new EntityUtilWriter(sw)
                pw.setXHTML(req.isRequestForXmlContent())
                pw.setLocale(req.getUserLocale())
                el.children().each {Element nde ->
                    if(nde.tag().isSelfClosing() && nde.tag().isEmpty())
                    {
                        pw.append('<').append(nde.tagName())
                        nde.attributes().each {Attribute attribute ->
                            pw.appendEscapedAttribute(attribute.key, attribute.value)
                        }
                        pw.appendEmptyTagClose().println()
                    }
                    else
                    {
                        pw.append('<').append(nde.tagName())
                        nde.attributes().each {Attribute attribute ->
                            pw.appendEscapedAttribute(attribute.key, attribute.value)
                        }
                        pw.append('>')
                        pw.println(nde.html())
                        pw.append('</').append(nde.tagName()).append('>')
                    }
                }
                final src = sw as String
                final res = new StringResource(src, 'text/javascript', UTF_8.name(), null)
                final nde = new StringNDE(res, JS, true)
                nde.setAttribute(NDE_ATTRIBUTE_LOCATION, NDE_ATTRIBUTE_LOCATION_VALUE_HEAD)
                NDEs << nde
            }
        }
        catch (final e)
        {
            logger.warn "Unexpected ${e.getClass().simpleName} parsing NDEs", e
        }
    }
}

@CompileStatic
class ExternalNDEPageElement
    extends ScriptedPageElementImpl
{
    ExternalNDEPageElement(final ScriptedBase base)
    {
        super(base)
    }

    Generator<? extends PageElement> getGenerator(final CmsRequest req)
    {
        new ExternalNDEGenerator()
    }
}

@CompileStatic
class ExternalNDELogger
{
    static Logger logger

    static Logger getLogger(final Class cls)
    {
        getLogger(cls.simpleName)
    }

    static Logger getLogger(final String name)
    {
        org.apache.logging.log4j.LogManager.getLogger("${logger.name}.${name}")
    }
}

ExternalNDELogger.logger = _logger

factory = new GroovyScriptedPageElementFactory(
    {new ExternalNDEComponent()},
    {new ExternalNDEPageElement(it)},
    context)
