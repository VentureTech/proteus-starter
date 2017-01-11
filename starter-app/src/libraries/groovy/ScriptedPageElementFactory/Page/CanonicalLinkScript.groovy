import com.i2rd.cms.bean.scripted.FixedScriptedPageElementModel
import com.i2rd.cms.bean.scripted.GroovyScriptedPageElementFactory
import com.i2rd.cms.bean.scripted.ScriptedBase
import com.i2rd.cms.bean.scripted.ScriptedPageElementImpl
import groovy.transform.CompileStatic
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.PageElementPath
import net.proteusframework.cms.component.content.ContentBuilder
import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor
import net.proteusframework.cms.component.editor.EditorUI
import net.proteusframework.cms.component.generator.Generator
import net.proteusframework.cms.component.generator.GeneratorImpl
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsRequestContext
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.cms.support.HTMLPageElementUtil
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.locale.LocalizedText
import net.proteusframework.internet.dataprovider.LinkDataProvider
import net.proteusframework.ui.miwt.component.Checkbox
import net.proteusframework.ui.miwt.component.Container
import net.proteusframework.ui.miwt.component.Label
import net.proteusframework.ui.miwt.layout.Anchor

/**
 * Content builder properties for the Component's model data.
 * @author Russ Tennant (russ@i2rd.com)
 */
enum CLComponentProperty
{
    canonical_for_page_alias('page_alias', 'Canonical Link For Page Alias?', false),
    canonical_for_wildcard_page('page_wildcard', 'Canonical Link For Wildcard Page?', false),
    canonical_for_path_ending_slash('path_slash', 'Canonical Link For Path Ending With \'/\'?', true),
    
    def className
    LocalizedText label
    def defaultValue
    
    CLComponentProperty(def cn, def label, def defaultValue)
    {
        this.className = cn
        this.label = new LocalizedText(label)
        this.defaultValue = defaultValue
    }
}

/**
* Test implementation of automatic output of canonical links.
* @author Russ Tennant (russ@i2rd.com)
*/
class CanonicalLinkGenerator extends GeneratorImpl<ScriptedBase>
{
    def scriptContext // Automatically set by the GroovyScriptedPageElement (if used)
    ContentBuilder<CLComponentProperty> builder;

    @Override
    void preRenderProcess(CmsRequest<ScriptedBase> request, CmsResponse response, ProcessChain chain)
    {
        chain.processChildElements()
        
        if(CmsRequestContext.isRequestInError()) 
            return // Don't do error pages

        builder = ContentBuilder.<CLComponentProperty>load(request.pageElementData, ContentBuilder.class, true)

        boolean handlePageWildcard = builder.getPropertyBooleanValue(CLComponentProperty.canonical_for_wildcard_page,
            CLComponentProperty.canonical_for_wildcard_page.defaultValue)
        //System.out.println("Handle Wildcard? ${handlePageWildcard} - Page Wildcard? ${request.getPageElementPath().isWildcard()}")
        if(request.getPageElementPath().isWildcard() && !handlePageWildcard)
            return // Not handling wildcard
        PageElementPath prp = request.getPageElementPath().getPageElement().getPrimaryPageElementPath();
        boolean isPrimaryPath = request.getPageElementPath().equals(prp)
        boolean handlePageAlias = builder.getPropertyBooleanValue(CLComponentProperty.canonical_for_page_alias,
            CLComponentProperty.canonical_for_page_alias.defaultValue)
        //System.out.println("Handle Page Alias? ${handlePageAlias} - Is PRP? ${isPrimaryPath} - PRP is null? ${prp == null}");
        if(!handlePageAlias && !isPrimaryPath)
            return // Not handling page alias

        boolean handlePathSlash = builder.getPropertyBooleanValue(CLComponentProperty.canonical_for_path_ending_slash,
            CLComponentProperty.canonical_for_path_ending_slash.defaultValue)

        def uri = response.createURL(CmsRequestContext.getOriginalRequestURL()).getLink().getURI()
        
        def linkString;
        if(handlePageAlias && prp != null && !isPrimaryPath)
        {
            linkString = response.createURL(prp).getURL(false)
        }
        else
        {
            linkString = uri.getPath()?:'';
        }
        if(handlePathSlash)
        {
            if(linkString.endsWith('/') && linkString.matches('.*[^/]+.*'))
                linkString = linkString.substring(0, linkString.length()-1)
        }
        if(uri.getQuery()) 
            linkString += '?' + uri.getQuery()
        if(linkString.contains(';jsessionid=')) 
            linkString = HTMLPageElementUtil.stripJSessionID(linkString)
        try
        {
            def link = response.createURL(linkString).getLink()
            link.getRelationShips().add('canonical')
            request.getDataProviderContainer().registerDataProvider(new LinkDataProvider(link))
        }
        catch(java.lang.IllegalArgumentException e)
        {
            org.apache.logging.log4j.LogManager.getLogger(CanonicalLinkGenerator.class)
                .error('Unable to create link: ' + linkString, e);
        }
    }
    
    @Override
    void render(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain) throws IOException
    {
        chain.renderChildElements()
    }
}



class CanonicalLinkEditor extends ContentBuilderBasedEditor<ContentBuilder<CLComponentProperty>>
{
    def l10n = {it -> new LocalizedText(String.valueOf(it))}
    def pageAliasField
    def wildcardPageField
    def pathEndingInSlashField
    
    CanonicalLinkEditor()
    {
        super(ContentBuilder.class)
    }
   
    def checkBox(CLComponentProperty cp)
    {
        // It'd be nice if we could set the CBC to be a DIV.
        def cb = new Checkbox(cp.label)
        cb.setLabelAnchor(Anchor.WEST)
        cb.selected = getBuilder().getPropertyBooleanValue(cp, cp.defaultValue)
        return cb
    }
    
    @Override
    void createUI(EditorUI editorUI)
    {
        super.createUI(editorUI); 
        editorUI.addComponent(Container.of("prop ${CLComponentProperty.canonical_for_page_alias.className}", 
            pageAliasField = checkBox(CLComponentProperty.canonical_for_page_alias)))
        editorUI.addComponent(Container.of("prop ${CLComponentProperty.canonical_for_wildcard_page.className}",
            wildcardPageField = checkBox(CLComponentProperty.canonical_for_wildcard_page)))
        editorUI.addComponent(Container.of("prop ${CLComponentProperty.canonical_for_path_ending_slash.className}",
            pathEndingInSlashField = checkBox(CLComponentProperty.canonical_for_path_ending_slash)))
        editorUI.addComponent(new Label(l10n('The footer of the page / template is the preferred location for this component.'))
                .withHTMLElement(HTMLElement.div)
                .addClassName('instructions'))
    }
    
    @Override
    protected void _updateBuilder()
    {
        def b = getBuilder();
        if(pageAliasField.selected == CLComponentProperty.canonical_for_page_alias.defaultValue)
            b.setProperty(CLComponentProperty.canonical_for_page_alias, null)
        else
            b.setPropertyBooleanValue(CLComponentProperty.canonical_for_page_alias, pageAliasField.selected)
            
        if(wildcardPageField.selected == CLComponentProperty.canonical_for_wildcard_page.defaultValue)
            b.setProperty(CLComponentProperty.canonical_for_wildcard_page, null)
        else
            b.setPropertyBooleanValue(CLComponentProperty.canonical_for_wildcard_page, wildcardPageField.selected)
            
        if(pathEndingInSlashField.selected == CLComponentProperty.canonical_for_path_ending_slash.defaultValue)
            b.setProperty(CLComponentProperty.canonical_for_path_ending_slash, null)
        else
            b.setPropertyBooleanValue(CLComponentProperty.canonical_for_path_ending_slash, pathEndingInSlashField.selected)
    }

    
}
@CompileStatic
class CanonicalLinkPageElement extends ScriptedPageElementImpl
{
    def scriptContext
    CanonicalLinkPageElement(ScriptedBase basePageElement)
    {
        super(basePageElement);
    }
    
    Generator getGenerator(CmsRequest<ScriptedBase> request) 
    {
        switch(request.getNegotiatedContentType())
        {
            case HTMLPageElementUtil.TEXT_HTML:
            case HTMLPageElementUtil.APPLICATION_XHTML:
                return new CanonicalLinkGenerator(scriptContext:scriptContext)
            default: return null;
        }
  }

    @Override
    List<? extends PageElement> getChildPageElements(CmsRequest<? extends PageElement> context)
    {
        // If you want to use the builtin delegation provided by ProcessChain and RenderChain, override this method.
        getBasePageElement().getDelegates()
        // alternate syntax -> return context.getPageElement().getDelegates()
    }
}


factory = new GroovyScriptedPageElementFactory(
    {new FixedScriptedPageElementModel(CanonicalLinkEditor.class, /*AnyComponentDelegationHelper.class*/)},
    {new CanonicalLinkPageElement(it)},
    context) 

