import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.i2rd.cms.CmsSiteCapabilityOperation
import com.i2rd.cms.backend.BackendConfig
import com.i2rd.cms.backend.BackendConfigImpl
import com.i2rd.cms.backend.dnd.DropTargetListPosition
import com.i2rd.cms.backend.permission.PageDataPermissionHelper
import com.i2rd.cms.bean.DelegateElement
import com.i2rd.cms.bean.MIWTRenderingData
import com.i2rd.cms.bean.contentmodel.CmsModelDataSet
import com.i2rd.cms.bean.scripted.FixedScriptedPageElementModel
import com.i2rd.cms.bean.scripted.GroovyScriptedPageElementFactory
import com.i2rd.cms.bean.scripted.ScriptedBase
import com.i2rd.cms.bean.scripted.ScriptedPageElementImpl
import com.i2rd.cms.controller.PreferencesUtil
import com.i2rd.cms.controller.json.ComponentInfo
import com.i2rd.cms.controller.json.ComponentSearch
import com.i2rd.cms.controller.json.Option
import com.i2rd.cms.controller.json.Permissions
import com.i2rd.cms.controller.json.SiteInfo
import com.i2rd.cms.controller.json.WebPageInfo
import com.i2rd.cms.dao.CmsBackendDAO
import com.i2rd.cms.docroot.CmsCssLibrary
import com.i2rd.cms.editor.CmsEditorDAO
import com.i2rd.cms.editor.DefaultEditorUI
import com.i2rd.cms.editor.DelegationHelper
import com.i2rd.cms.editor.EditorUIConfig
import com.i2rd.cms.editor.EditorUIEvent
import com.i2rd.cms.editor.EditorUIEventListener
import com.i2rd.cms.editor.FixedStateWorkFlowTransitionSelector
import com.i2rd.cms.miwt.SiteAwareMIWTApplication
import com.i2rd.cms.page.BeanBoxList
import com.i2rd.cms.workflow.Transition
import com.i2rd.cms.workflow.WorkFlowManager
import com.i2rd.contentmodel.data.ModelDataDAO
import com.i2rd.users.ApplicationContextBean
import com.i2rd.users.PermissionHelperUtil
import com.ibm.icu.util.ULocale
import groovy.transform.CompileStatic
import net.proteusframework.cms.CmsSite
import net.proteusframework.cms.Negotiable
import net.proteusframework.cms.NegotiableImpl
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.PageElementModel
import net.proteusframework.cms.PageElementPath
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.content.ContentBuilder
import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor
import net.proteusframework.cms.component.editor.DelegatePurpose
import net.proteusframework.cms.component.editor.EditorUI
import net.proteusframework.cms.component.generator.Generator
import net.proteusframework.cms.component.generator.GeneratorImpl
import net.proteusframework.cms.component.generator.MIWTRenderer
import net.proteusframework.cms.component.page.Page
import net.proteusframework.cms.component.page.layout.Box
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.CmsSession
import net.proteusframework.cms.controller.NDEHelper
import net.proteusframework.cms.controller.PageElementChain
import net.proteusframework.cms.controller.ParsedRequest
import net.proteusframework.cms.controller.PluginContext
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.cms.controller.URLParser
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.cms.permission.PagePermission
import net.proteusframework.cms.support.HTMLPageElementUtil
import net.proteusframework.core.StringFactory
import net.proteusframework.core.html.Element
import net.proteusframework.core.html.HTMLDoctype
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.io.EntityUtilWriter
import net.proteusframework.core.locale.LocaleContext
import net.proteusframework.core.locale.LocalizedText
import net.proteusframework.core.locale.TextSource
import net.proteusframework.core.locale.TextSources
import net.proteusframework.core.net.LenientContentType
import net.proteusframework.internet.http.Link
import net.proteusframework.internet.http.ParameterView
import net.proteusframework.internet.http.Request
import net.proteusframework.internet.http.RequestError
import net.proteusframework.internet.http.Response
import net.proteusframework.internet.http.ResponseError
import net.proteusframework.internet.http.ResponseStatus
import net.proteusframework.internet.http.Scope
import net.proteusframework.internet.http.resource.html.NDE
import net.proteusframework.internet.http.resource.html.NDEResourceComparator
import net.proteusframework.internet.http.useragent.UserAgent
import net.proteusframework.ui.miwt.HTMLRenderContext
import net.proteusframework.ui.miwt.MIWTApplication
import net.proteusframework.ui.miwt.MIWTSession
import net.proteusframework.ui.miwt.component.ComboBox
import net.proteusframework.ui.miwt.component.ComponentImpl
import net.proteusframework.ui.miwt.component.Container
import net.proteusframework.ui.miwt.component.Frame
import net.proteusframework.ui.miwt.component.Label
import net.proteusframework.ui.miwt.component.event.ComponentAdapter
import net.proteusframework.ui.miwt.component.event.ComponentEvent
import net.proteusframework.ui.miwt.data.SimpleListModel
import net.proteusframework.ui.miwt.event.Event
import net.proteusframework.ui.miwt.event.EventQueue
import net.proteusframework.ui.miwt.event.EventQueueElement
import net.proteusframework.ui.miwt.layout.LayoutConstraint
import net.proteusframework.ui.miwt.util.ComponentTreeIterator
import net.proteusframework.ui.miwt.util.MIWTUtil
import net.proteusframework.ui.miwt.util.RendererEditorState
import net.proteusframework.users.model.CredentialPolicyLevel
import net.proteusframework.users.model.dao.PermissionDAO
import net.proteusframework.users.model.dao.PrincipalDAO
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import javax.mail.internet.ContentType
import javax.script.ScriptContext

import static net.proteusframework.internet.http.ResponseError.NOT_IMPLEMENTED

/**
 * Content builder properties for the Component's model data.
 * @author Russ Tennant (russ@i2rd.com)
 */
enum LEComponentProperty
{
    Mode;
}

/**
 * Modes of operation.
 * @author Russ Tennant (russ@i2rd.com)
 */
enum LEMode
{
    API,
    // ComponentPalette,
    // AddComponent,
    Editor;
}

enum ComponentEditorAction
{
    /** Add a component. */
    add,
    /** Move a component. */
    move,
    /** Edit a component. */
    edit,
    /** Mark a component as deleted. */
    delete,
    /** Detach a component. */
    detach,
    /** Close/cancel current operation i.e. closing an editor. */
    close;

    /**
     * Check if the current user has permission for this action.
     * @param prp the parsed request path.
     * @return true or false.
     */
    boolean hasPermission(ParsedRequest prp)
    {

        PluginContext ctx = new PluginContext(prp, EnumSet.of(Option.permissions))
        try
        {
            ctx.setPermissionHelperUtil(PermissionHelperUtil.getInstance())
            ctx.activate();
            ComponentInfo ci = ComponentInfo.create(ctx, prp.getRenderedElement())
            switch (this)
            {
                case close: return true
                case edit:
                    return ci.getPermissions().get(Permissions.COMPONENT_EDIT) ?: false
                case detach:
                case delete:
                    return ci.getPermissions().get(Permissions.COMPONENT_DELETE) ?: false
                case add:
                case move:
                    return true; // This needs to be checked elsewhere
            }
        }
        finally
        {
            ctx.deactivate();
        }
        return false;
    }
}

class ComponentEditorRequest
{
    PageElement pageElement
    Locale locale
    CmsModelDataSet modelData
    ComponentEditorAction action = ComponentEditorAction.edit

    // Used as key - only compare pageElement and locale
    int hashCode()
    {
        final p = 67;
        int res = this.pageElement.hashCode();
        res = res * p + this.locale.hashCode();
        return res;
    }

    boolean equals(Object other)
    {
        if (!(other instanceof ComponentEditorRequest))
            return false
        ComponentEditorRequest cer = other
        if (!this.pageElement.equals(cer.pageElement))
            return false
        if (!this.locale.equals(cer.locale))
            return false
        return true
    }
}

class JavaScriptComponent extends ComponentImpl
{
    int renderCount = 0;
    int renderLimit = 0;
    boolean alwayInvalidate = false
    String code

    JavaScriptComponent(String code)
    {
        this.code = code;
    }

    JavaScriptComponent withAlwaysInvalidate(boolean value)
    {
        this.alwayInvalidate = value;
        return this
    }

    JavaScriptComponent withRenderLimit(int limit)
    {
        if (limit < 0) limit = 0
        this.renderLimit = limit
        return this
    }

    @Override
    Element getHTMLElement()
    {
        // Fully qualified to workaround groovy bug
        return net.proteusframework.core.html.HTMLElement.script
    }

    @Override
    void preRenderProcess(Request request, Response response, RendererEditorState state)
    {
        super.preRenderProcess(request, response, state);
        if (alwayInvalidate)
        {
            invalidate()
        }
        if (renderLimit > 0 && renderCount >= renderLimit)
        {
            EventQueue.queue(new EventQueueElement() {

                public int getEventPriority()
                {
                    return Event.PRIORITY_ACTION;
                }

                public void fire()
                {
                    JavaScriptComponent.this.close();
                }
            });
        }
    }

    @Override
    public void render(LayoutConstraint constraint, HTMLRenderContext context)
    {
        if (code && (renderLimit == 0 || renderCount < renderLimit))
        {
            renderCount++;
            context.print('<script');
            context.printHtmlAttribute('type', 'text/javascript');
            context.println('>');
            context.print(code)
            context.print('\n</script>');
        }
    }
}

/**
 * Component editor integration application.
 * Responsible for maintaining component editor state.
 * @author Russ Tennant (russ@i2rd.com)
 */
@CompileStatic
class ComponentEditorIntegration extends SiteAwareMIWTApplication
{
    /** Close JavaScript */
    final static String CLOSE_JAVASCRIPT = '''
setTimeout(function(){
    window.parent.postMessage({"action": "editorClosed"}, '*');
}, 10); // let other scripts run first
''';
    /** l10n function. */
    Closure<TextSource> l10n = {Object it -> TextSources.createTextForAny(it)};
    /** Components. ComponentEditorRequest -> Component. */
    final Map<ComponentEditorRequest, ComponentImpl> components = [:];
    /** Current Component */
    ComponentImpl currentComponent;
    /** Default Component */
    final Label defaultComponent = new Label(l10n('Unable to display editor for specified component')).withHTMLElement(HTMLElement.p);
    /** Close listener */
    final ComponentAdapter closeListener = new ComponentAdapter() {
        void componentClosed(ComponentEvent e)
        {
            def req = Event.request
            def resp = Event.response
            def url = resp.createURL(false)
            url.link.setPathInfo(req.pathInfo.split('/')[0..2].join('/') + '/close');
            resp.redirect(url)
            //System.out.println 'Editor close listener'
        }
    };


    /**
     * Constructor.
     * @param session session.
     */
    public ComponentEditorIntegration(final MIWTSession session)
    {
        super(session);
    }

    @Override
    protected void siteChanged(CmsSite site)
    {
        super.siteChanged(site);
        //System.err.println('Site changed to ' + site.id);
        ComponentTreeIterator cti = new ComponentTreeIterator(getFrame(), false, false);
        while (cti.hasNext())
        {
            def c = cti.next();
            if (c instanceof DefaultEditorUI)
            {
                def editor = c as DefaultEditorUI
                editor.dontCheckDirtyBeforeClose();
                editor.close();
            }
        }

    }

    @Override
    protected Frame setupFrame()
    {
        //System.err.println 'App.userStarted'
        MIWTApplication me = this
        Frame frame = new Frame(me) {
            @Override
            public void preRenderProcess(final Request request, final Response response, final RendererEditorState state)
            {
                // Check if we need to edit a ContentElement.
                def cer = request.getSession(Scope.REQUEST).getObject(ComponentEditorRequest.class.name, null)
                if (!(cer instanceof ComponentEditorRequest))
                {
                    // Temp workaround for class cast exceptions during testing due to recompilation
                    //System.err.println 'CLEARING UI'
                    cer = null
                    getSession().destroy()
                    def url = response.createURL(false)
                    url.link.setPathInfo(request.pathInfo);
                    response.redirect(url)
                }
                else if (cer != null)
                {
                    setupEditor((ComponentEditorRequest)cer)
                }
                super.preRenderProcess(request, response, state);
            }
        };
        frame.setComponentName('Frame');
        frame.addClassName('editor_frame');
        frame.setVisible(true);
        frame.add(this.defaultComponent);
        frame.add(new JavaScriptComponent('''
(function(w,d){
    var de = d.documentElement;
    function sizeIframe(dim, oncetype){
        var msg = {
         "action": "sizeIframe",
         "oncetype": oncetype?(""+oncetype):"",
         "data" : {
           "width":dim?dim.width:Math.max(de.scrollWidth, 980),
           "height":dim?dim.height:Math.max(de.scrollHeight, 200)
           }
        };
        w.parent.postMessage(msg, '*');
   }
   // Set initial width so when we measure later, the height isn't too far off due to wrapping.
   //sizeIframe({width:900,height:300}, "init");

   function startSizingCheck(){
     var cnt = 0, same = 0, sizeCheck, last = 0;
     sizeCheck = w.setInterval(function(){
       var sh = de.scrollHeight;
       //w.parent.postMessage({"action":"log","data":sh}, '*');
       if(cnt++ > 8 || same > 2) {
           sizeIframe();
           w.clearInterval(sizeCheck);
           cnt = 11;
       }
       if(last != sh) same = 0;
       else same++;
       //w.parent.postMessage({"action":"log","data":{last:last,sh:sh,same:same,cnt:cnt}}, '*');
       last = sh;
     }, 20);
   }
   if(d.readyState == 'complete') {
       //w.parent.postMessage({"action":"log","data":"ready"}, '*');
       setTimeout(startSizingCheck,10);
   }else{
       //w.parent.postMessage({"action":"log","data":"event listener"}, '*');
       // Wait until CSS is loaded and applied as it will affect the size.
       w.addEventListener('load',startSizingCheck);
   }
})(window,document);
''').withAlwaysInvalidate(true));

        return frame;
    }

    /**
     * Setup the editor for the specified CMS component.
     * @param cer the component editor request.
     */
    protected void setupEditor(final ComponentEditorRequest cer)
    {
        //System.err.println 'SetupEditor'
        ComponentImpl lookup = this.components[cer]
        if (lookup != null && lookup == this.currentComponent)
        {
            switch (cer.action)
            {
                case ComponentEditorAction.edit:
                    lookup.visible = true;
                    //System.err.println 'SetupEditor: already editing same'
                    return;
                default: break;
            }
        }
        else if (lookup != null)
        {
            //System.out.println('SetupEditor: switching EditorUI. Lookup = ' + lookup + ', CurrentComponent = ' + currentComponent)
        }
        Frame frame = getFrame()
        EventQueue.queue(new EventQueueElement() {

            public int getEventPriority()
            {
                return Event.PRIORITY_COMPONENT_CHANGE;
            }

            public void fire()
            {
                //System.err.println 'SetupEditor.fire'
                if (lookup == null || lookup != currentComponent)
                {
                    //System.err.println 'SetupEditor: Making EditorUIs invisible'
                    ComponentTreeIterator cti = new ComponentTreeIterator(getFrame(), false, false);
                    while (cti.hasNext())
                    {
                        def c = cti.next();
                        if (c instanceof EditorUI)
                        {
                            c.visible = false;
                        }
                    }
                }
                switch (cer.action)
                {
                    case ComponentEditorAction.edit:
                        defaultComponent.visible = false
                        //System.err.println 'SetupEditor - setting up new editor'
                        final BackendConfig bc = BackendConfigImpl.getInstance();
                        final CmsEditorDAO em = bc.getEditorManager();
                        final CmsRequest<? extends PageElement> request = (CmsRequest<? extends PageElement>) Event.getRequest();
                        final Locale localeOverride = cer.locale;
                        final LocaleContext editCtx = localeOverride == null ? em.getPreferredEditingLocaleContext() :
                            net.proteusframework.internet.support.LocaleUtil.getLocaleContext(localeOverride);
                        CmsModelDataSet modelData = cer.modelData;
                        if (modelData == null)
                            modelData = em.getLatestModelData(cer.pageElement, editCtx.getLocale());
                        WorkFlowManager wfm = WorkFlowManager.getInstance()
                        def contentWorkFlow = modelData == null ? cer.pageElement.site.getWorkFlow() : wfm.getWorkFlow(modelData);
                        def uiConfig = new EditorUIConfig(cer.pageElement, editCtx).setModelData(modelData)
                        uiConfig.setWorkFlowTransitionSelector(
                            new FixedStateWorkFlowTransitionSelector(contentWorkFlow.getFinalState()));
                        uiConfig.addEditorUIEventListener({EditorUIEvent e ->
                            if (e.action != EditorUIEvent.Action.cancel && e.actionSuccess)
                            {
                                frame.add(new JavaScriptComponent("""
                                  var msg = {"action": "editorSaved", "data": {"component": "${cer.pageElement.pathId}#${
                                    cer.pageElement.id
                                }"}};
                                  window.parent.postMessage(msg, '*');
                                  """).withRenderLimit(1))
                            }
                        } as EditorUIEventListener)
                        currentComponent = (ComponentImpl) em.createEditor(uiConfig);
                        currentComponent.addComponentListener(closeListener)
                        components[cer] = currentComponent
                        frame.add(currentComponent)
                        break;
                    case ComponentEditorAction.close:
                        defaultComponent.visible = true;
                        ComponentImpl editor = components.remove(cer)
                        if (editor == currentComponent)
                        {
                            currentComponent = null
                            frame.insert(new JavaScriptComponent(CLOSE_JAVASCRIPT).withRenderLimit(1), defaultComponent);
                        }
                        if (editor != null && !editor.isClosed())
                        {
                            editor.removeComponentListener(closeListener)
                            if(editor instanceof DefaultEditorUI)
                                ((DefaultEditorUI)editor).dontCheckDirtyBeforeClose();
                            editor.close();
                        }
                        break;
                }
            }
        });

    }

}

/**
 * LiveEdit API generator.
 * @author Russ Tennant (russ@i2rd.com)
 */
@CompileStatic
class LiveEditComponentEditorGenerator extends MIWTRenderer<ScriptedBase> implements Generator<ScriptedBase>
{
    /**
     * Insert type for a move or add operation.
     * @author Russ Tennant (russ@i2rd.com)
     */
    enum InsertType
    {
        sibling,
        child;
    }
    final Logger _logger = LogManager.getLogger(LiveEditComponentEditorGenerator.class)
    final String ATT_JSON_RESPONSE = 'JSON.RESPONSE'
    ScriptContext scriptContext
    // Automatically set by the GroovyScriptedPageElement (if used)
    public LiveEditComponentEditorGenerator()
    {
        super();
        addNDE(CmsCssLibrary.EDITOR_AND_ADD_COMPONENT.getNDE());
        final MIWTRenderingData mrd = new MIWTRenderingData();
        mrd.setApplicationClass(ComponentEditorIntegration.class);
        mrd.setLazySession(false);
        setRenderingData(mrd);
        setOutputWrappingDIV(false);
    }

    ComponentEditorRequest cer

    @Override
    public void preRenderProcess(CmsRequest<ScriptedBase> request, CmsResponse response, ProcessChain chain)
    {
        // Get Component being edited from the URL and setup Object in request attributes.
        String origin = request.getHeaderValue('Origin');
        if (origin)
        {
            ParsedRequest originPRP = URLParser.parseRequest(URI.create(origin), 'GET')
            if (originPRP.getSiteHostname() != null)
            {
                // Must be a CMS site to enable CORS to the backend.
                response.addHeader('Access-Control-Allow-Origin', origin);
                response.addHeader('Access-Control-Allow-Credentials', 'true');
            }
        }
        String url = request.getParameter('url', (String) request.getReferer())
        if (!url)
        {
            response.sendError(RequestError.BAD_REQUEST, "A URL must be specified or a referer must be present");
            return;
        }
        addField('url', url); // For subsequent AJAX posts from MIWT.
        ParsedRequest check = null;
        try
        {
            check = URLParser.parseRequest(Link.createURI(url), request.getMethod());
        }
        catch (java.lang.Exception e)
        {
            response.sendError(ResponseError.APPPLICATION_ERROR, e.getMessage());
            _logger.error('Unable to parse request.', e);
            return;
        }
        if (check.hasError())
        {
            response.sendError(check.getError(), check.getErrorPhrase());
            return;
        }
        if (check.getSiteHostname() == null)
        {
            response.sendError(RequestError.BAD_REQUEST, "Unable to find in CMS");
            return;
        }

        String[] pathInfo = StringFactory.trimSlashes(request.pathInfo ?: '').split('/')
        if (pathInfo.size() != 3)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Bad path')
            return
        }
        //System.out.println pathInfo
        String peStr = pathInfo[0]
        String localeStr = pathInfo[1]
        String actionStr = pathInfo[2]
        if (!localeStr || ULocale.getLanguage(localeStr).isEmpty())
        {
            response.sendError(RequestError.BAD_REQUEST, 'Bad locale')
            return
        }
        def locale = new ULocale(localeStr).toLocale()
        if (!peStr)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Bad page element')
            return
        }
        try
        {
            LiveEditPageElement.updatePRPWithPEPath(check, peStr)
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Bad page element path: ' + peStr + '.\n' + e)
            return
        }
        def action;
        try
        {
            action = ComponentEditorAction.valueOf(actionStr);
        }
        catch (IllegalArgumentException ignore)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Bad action')
            return
        }
        switch (action)
        {
            case ComponentEditorAction.delete:
            case ComponentEditorAction.detach:
            case ComponentEditorAction.add:
            case ComponentEditorAction.move:
                if (request.method != 'POST')
                {
                    response.sendError(RequestError.BAD_REQUEST, 'Bad action method')
                    return
                }
                break;
        }
        def pageElement = check.getRenderedElement();
        if (!pageElement)
        {
            response.sendError(RequestError.NOT_FOUND, 'PageElement does not exist')
            return
        }
        def p = request.getPrincipal()
        if (!CmsFrontendDAO.getInstance().canOperateSite(p, pageElement.getSite(), CmsSiteCapabilityOperation.admin))
        {
            response.sendError(RequestError.FORBIDDEN, "Not authorized for site.");
            return;
        }
        if (!action.hasPermission(check))
        {
            response.sendError(RequestError.FORBIDDEN, "Not authorized to modify data.");
            return;
        }
        final BackendConfig bc = BackendConfigImpl.getInstance();
        final CmsEditorDAO em = bc.getEditorManager();


        int mdsId = request.getParameter('mds', 0)
        ModelDataDAO mdm = ModelDataDAO.getInstance(null)
        CmsModelDataSet mds = (mdsId > 0 ? mdm.getModelDataSet(mdsId) : null) as CmsModelDataSet
        if (mds == null)
        {
            // Latest or Published?
            mds = em.getLatestModelData(pageElement, locale)
        }
        PreferencesUtil.getInstance().setSelectedSite(pageElement.getSite())
        switch (action)
        {
            case ComponentEditorAction.delete:
                response.sendError(NOT_IMPLEMENTED, 'Sorry. This function hasn\'t been implemented yet.')
                break;
            case ComponentEditorAction.detach:
                if (!(pageElement instanceof ContentElement))
                {
                    response.sendError(NOT_IMPLEMENTED, 'LiveEdit deletion of pages / boxes not supported.')
                    return
                }
                def parent = check.getNextToLastPageElementInPath();
                if (pageElement == check.getPageElementRequestPath().getPageElement() || parent == null)
                {
                    response.sendError(NOT_IMPLEMENTED, 'LiveEdit deletion of page level components not supported.')
                    return
                }
                if (parent instanceof Box)
                {
                    Page page = (Page) check.getPageElementRequestPath().getPageElement()
                    BeanBoxList bbl = page.getBeanBoxList(parent);
                    if (bbl != null && !bbl.getElements().contains(pageElement))
                        bbl = page.getPageTemplate().getBeanBoxList(parent)
                    if (bbl == null || !bbl.getElements().contains(pageElement))
                    {
                        response.sendError(RequestError.BAD_REQUEST, 'Cannot find box the component is from.')
                        return
                    }
                    if (!CmsBackendDAO.instance.removeContentElement(bbl, pageElement as ContentElement))
                        CmsBackendDAO.instance.trashContentElement(pageElement as ContentElement)

                }
                else if (pageElement instanceof ContentElement)
                {
                    if (!CmsBackendDAO.instance.removeContentElement(parent as BeanBoxList, pageElement))
                        CmsBackendDAO.instance.trashContentElement(pageElement)
                }
                else
                {
                    response.sendError(RequestError.BAD_REQUEST, 'Unexpected type: ' + pageElement.class.simpleName)
                    return
                }
                response.setStatus(ResponseStatus.NO_CONTENT)
                break;
            case ComponentEditorAction.add:
            case ComponentEditorAction.move:
                insertComponent(request, response, check, url, locale, action)
                break;
            case ComponentEditorAction.close:
            case ComponentEditorAction.edit:
                cer = new ComponentEditorRequest(pageElement: pageElement, locale: locale, modelData: mds, action: action)
                request.getSession(Scope.REQUEST).setObject(ComponentEditorRequest.class.name, cer);
                super.preRenderProcess(request, response, chain);
                break;
        }
    }

    /**
     * Insert a component onto the page targeted at the current path - {@link ParsedRequest}.
     * This supports insertion into a BBL or a delegating ContentElement (as a delegate purpose or as sibling of an existing
     * delegate).
     * @param request the request.
     * @param response the response.
     * @param target the target.
     * @param locale the locale.
     * @param action the action - one of add or move.
     */
    void insertComponent(CmsRequest<ScriptedBase> request, CmsResponse response, ParsedRequest target, String url, Locale locale,
        ComponentEditorAction action)
    {
        BackendConfig bc = BackendConfigImpl.getInstance()
        final ParameterView pv = request.getParameterView(ParameterView.Context.TARGETED_PATH)
        final boolean move = (action == ComponentEditorAction.move)
        DropTargetListPosition position = DropTargetListPosition.after
        try
        {
            if (pv.contains('position')) position = DropTargetListPosition.valueOf(pv.get('position'));
        }
        catch (IllegalArgumentException e)
        {
            _logger.debug('Bad position', e)
            response.sendError(RequestError.BAD_REQUEST, 'Bad position')
            return
        }
        /*
         * Insert the source either as a sibling to the destination or as a child of the destination. 
         */
        PageElement destination = target.getLastPageElementInPath()
        def insertType;
        try
        {
            insertType = InsertType.valueOf(pv.getOrEmpty('insert_type'))
        }
        catch (IllegalArgumentException iae)
        {
            _logger.debug('Bad insert type', iae)
            response.sendError(RequestError.BAD_REQUEST, 'Bad insert type')
            return
        }
        boolean hasCAT = pv.contains('content_area_type')
        PageElement destinationParent = target.getNextToLastPageElementInPath();
        BeanBoxList destinationBBL;
        if (pv.contains('delegate_purpose'))
        {
            destinationParent = destination;
        }
        else if (destinationParent instanceof Box || (hasCAT && destination instanceof Box))
        {
            Page page = (Page) target.getPageElementRequestPath().getPageElement()
            if (hasCAT)
            {
                // The BBL was specified and the 
                destinationBBL = pv.get('content_area_type') == 'page' ?
                    (page.getBeanBoxList().find {BeanBoxList bbl -> bbl.box == destination}) :
                    (page.pageTemplate.getBeanBoxList().find {BeanBoxList bbl -> bbl.box == destination});

            }
            else
            {
                // Find the BBL based on 
                destinationBBL = page.getBeanBoxList().find {BeanBoxList bbl -> bbl.box == destinationParent}
                if (!destinationBBL || !destinationBBL.getElements().contains(destination))
                    destinationBBL = page.pageTemplate.getBeanBoxList(destinationParent as Box)
                if (destinationBBL && !destinationBBL.getElements().contains(destination))
                {
                    _logger.debug('Unable to find destination in BBL')
                    response.sendError(RequestError.BAD_REQUEST, 'Unable to find destination in  BBL')
                    return
                }
            }
            if (!destinationBBL)
            {
                _logger.debug('Unable to find BBL')
                response.sendError(RequestError.BAD_REQUEST, 'Unable to find BBL')
                return
            }
        }

        // Sources - one required
        // Move: pepath
        // Add: pepath | component_identifier | pe
        ParsedRequest sourcePRP;
        PageElement source;
        PageElementModel<? extends PageElement> sourceComponent;
        if (move || pv.contains('pepath'))
        {
            def pepath = pv.get('pepath')
            if (!pepath)
            {
                _logger.debug('Missing pepath')
                response.sendError(RequestError.BAD_REQUEST, 'Missing pepath')
                return
            }
            sourcePRP = URLParser.parseRequest(Link.createURI(url), 'POST')
            try
            {
                LiveEditPageElement.updatePRPWithPEPath(sourcePRP, pepath)
            }
            catch (IllegalArgumentException e)
            {
                _logger.info('Bad pepath', e)
                response.sendError(RequestError.BAD_REQUEST, 'Bad pepath')
                return
            }
            source = sourcePRP.getLastPageElementInPath()
            sourceComponent = bc.getComponent(source)
        }
        else if (pv.contains('component_identifier'))
        {
            def componentIdentifier = pv.get('component_identifier')
            if (!componentIdentifier)
            {
                _logger.debug('Missing component_identifier')
                response.sendError(RequestError.BAD_REQUEST, 'Missing component_identifier')
                return
            }
            sourceComponent = bc.getComponentByIdentifier(target.getSiteHostname().getSite(), componentIdentifier)
            if (!sourceComponent)
            {
                _logger.debug('Missing component for identifier: ' + componentIdentifier)
                response.sendError(RequestError.BAD_REQUEST, 'Missing component for identifier: ' + componentIdentifier)
                return
            }
            source = sourceComponent.getProvider().createInstance(sourceComponent)
        }
        else if (pv.contains('pe'))
        {
            def pe = pv.get('pe')
            if (!pe)
            {
                _logger.debug('Missing pe')
                response.sendError(RequestError.BAD_REQUEST, 'Missing pe')
                return
            }
            try
            {
                def peList = PageElementChain.getPageElementChainPath([pe.replace('#', '')], false)
                source = peList[0]
            }
            catch (IllegalArgumentException e)
            {
                _logger.info('Bad pe', e)
                response.sendError(RequestError.BAD_REQUEST, 'Bad pe')
                return
            }
            sourceComponent = bc.getComponent(source)
        }
        else
        {
            _logger.debug('Missing valid source: pepath | component_identifier | pe')
            response.sendError(RequestError.BAD_REQUEST, 'Missing valid source:  pepath | component_identifier | pe')
            return
        }
        def delegatingContentElement;
        def destinationDelegateTarget;
        DelegatePurpose delegatePurpose;
        if (pv.contains('delegate_purpose'))
        {
            String dp = pv.get('delegate_purpose')
            if (!dp)
            {
                _logger.debug('Missing specified delegate_purpose')
                response.sendError(RequestError.BAD_REQUEST, 'Missing specified delegate_purpose')
                return
            }
            // Check if targeting destination
            DelegationHelper dh = bc.getDelegationHelper(destination);
            if (destination instanceof ContentElement && dh.supportsDelegation())
            {
                ContentElement db = destination as ContentElement
                def supported = dh.getSupportedComponents(db.getDelegates())
                if (supported == null || supported.contains(sourceComponent))
                {
                    def dhPurposes = dh.getSupportedPurposes(sourceComponent, db.getDelegates()) as Set<DelegatePurpose>
                    delegatePurpose = dhPurposes.find({DelegatePurpose it -> it.name() == dp}) as DelegatePurpose
                    delegatingContentElement = destination
                }
                else
                {
                    _logger.info('Destination does not support delegating to source.')
                }
            }
            else
            {
                _logger.info('Destination is not a delegating ContentElement.')
            }

            if (!delegatePurpose)
            {
                _logger.info('Unable to find delegate_purpose')
                response.sendError(RequestError.BAD_REQUEST, 'Unable to find delegate_purpose')
                return
            }
        }
        else if (!destinationBBL)
        {
            // Check if we are targeting a delegate
            DelegationHelper dh = bc.getDelegationHelper(destinationParent);
            if (destinationParent instanceof ContentElement && dh.supportsDelegation())
            {
                ContentElement db = destinationParent as ContentElement
                DelegateElement delegate = db.getDelegates().find {DelegateElement de -> de.getId() == destination.id}
                if (delegate)
                {
                    def supported = dh.getSupportedComponents(db.getDelegates())
                    if (supported == null || supported.contains(sourceComponent))
                    {
                        if (dh.getSupportedPurposes(sourceComponent, db.getDelegates()).contains(delegate.getPurpose()))
                        {
                            destinationDelegateTarget = destination
                            delegatingContentElement = destinationParent
                            delegatePurpose = delegate.getPurpose()
                        }
                        else
                        {
                            _logger.info('Delegating component does not support the source component as a ' + delegate.getPurpose())
                            response.sendErrorCode(470,
                                'Delegating component does not support the source component as a ' + delegate.getPurpose())
                            return
                        }
                    }
                    else
                    {
                        _logger.info('Delegating component does not support the source component')
                        response.sendErrorCode(470, 'Delegating component does not support the source component')
                        return
                    }
                }
                else
                {
                    _logger.info('Could not find delegate for destination (not in a BBL)')
                }
            }
            else
            {
                _logger.info('Not targeting a BBL or Delegating component')
            }
            if (!delegatePurpose || !destinationDelegateTarget)
            {
                _logger.info('Unable to find target of insert (not a BBL or delegating ContentElement)')
                response.
                    sendError(RequestError.BAD_REQUEST, 'Unable to find target of insert (not a BBL or delegating ContentElement)')
                return
            }
        }

        // Check permission
        if (move)
        {
            PluginContext ctx;
            if (insertType == InsertType.sibling)
            {
                ctx = new PluginContext(target, EnumSet.of(Option.permissions))
                try
                {
                    ctx.setPermissionHelperUtil(PermissionHelperUtil.getInstance())
                    ctx.activate();
                    ComponentInfo ci = ComponentInfo.create(ctx, target.getRenderedElement())
                    if (!ci.getPermissions().get(Permissions.COMPONENT_MOVE))
                    {
                        response.sendError(RequestError.FORBIDDEN, 'User does not have permission to move component')
                        return
                    }
                }
                finally
                {
                    ctx.deactivate();
                }
            }

            ctx = new PluginContext(sourcePRP, EnumSet.of(Option.permissions))
            try
            {
                ctx.setPermissionHelperUtil(PermissionHelperUtil.getInstance())
                ctx.activate();
                ComponentInfo ci = ComponentInfo.create(ctx, sourcePRP.getRenderedElement())
                if (!ci.getPermissions().get(Permissions.COMPONENT_MOVE))
                {
                    response.sendError(RequestError.FORBIDDEN, 'User does not have permission to move component')
                    return
                }
            }
            finally
            {
                ctx.deactivate();
            }
        }
        def addExistingPermission = source.id > 0
        if (addExistingPermission)
        {
            if (!BackendConfigImpl.hasComponentAddExistingPermission(null, sourceComponent))
            {
                response.sendError(RequestError.FORBIDDEN, 'User does not have permission to add component')
                return
            }
        }
        else
        {
            if (!BackendConfigImpl.hasComponentAddNewPermission(null, sourceComponent))
            {
                response.sendError(RequestError.FORBIDDEN, 'User does not have permission to add component')
                return
            }
        }
        if (source.getId() == destination.getId())
        {
            // NOOP
            return
        }
        // Insert component at destination (remove from source if Move operation)
        final CmsBackendDAO pem = CmsBackendDAO.getInstance();

        BeanBoxList sourceBBL
        PageElement sourceParent = sourcePRP?.getNextToLastPageElementInPath();
        if (move)
        {

            if (sourceParent instanceof Box)
            {
                // Only pages have boxes
                Page page = sourcePRP.getPageElementRequestPath().getPageElement() as Page
                sourceBBL = page.getBeanBoxList().find {BeanBoxList it -> it.box == sourceParent}
                if (!sourceBBL || !sourceBBL.getElements().contains(source)) sourceBBL =
                    page.pageTemplate.getBeanBoxList(sourceParent as Box)
                if (!sourceBBL || !sourceBBL.getElements().contains(source))
                {
                    _logger.debug('Unable to find BBL')
                    response.sendError(RequestError.BAD_REQUEST, 'Unable to find BBL')
                    return
                }
            }
        }
        boolean success = false;
        // Insert at target
        try
        {
            if (destinationBBL)
            {
                pem.insertContentElement(destinationBBL,
                    source as ContentElement,
                    (insertType == InsertType.sibling ? destination as ContentElement : null),
                    position)
                success = true;
            }
            else if (delegatePurpose)
            {
                pem.insertContentElement(delegatingContentElement as ContentElement,
                    source as ContentElement,
                    delegatePurpose,
                    destinationDelegateTarget as ContentElement,
                    position)
                success = true;
            }
            else
            {
                response.sendError(RequestError.BAD_REQUEST, 'Destination not a BBL or a delegating component with a purpose.')
                return
            }
            request.getSession(Scope.REQUEST).setObject(ATT_JSON_RESPONSE, ['key': "${source.pathId}#${source.id}".toString()])

            // Remove from source
            if (move && destinationParent != sourceParent)
            {
                if (sourceBBL)
                    pem.removeContentElement(sourceBBL, source as ContentElement)
                else
                    pem.removeContentElement(sourceParent as ContentElement, source as ContentElement)
            }
        }
        finally
        {
            if (success)
            {
                if (action == ComponentEditorAction.add && source instanceof ContentElement)
                {
                    ContentElement cb = source as ContentElement;
                    // This is Live Edit - make sure the component has a published revision for the specified locale
                    // Handle fallbacks within locale
                    def hasPublishedData = HTMLPageElementUtil.getFallbackLocales(locale).
                        any {cb.getPublishedData().containsKey(it);}
                    if (!hasPublishedData)
                    {
                        final WorkFlowManager wfm = WorkFlowManager.getInstance();
                        CmsModelDataSet modelData = bc.getEditorManager().getLatestModelData(source, locale);
                        CmsModelDataSet newModelData = null;
                        def contentWorkFlow = modelData == null ? source.site.getWorkFlow() : wfm.getWorkFlow(modelData);
                        if (modelData == null)
                        {
                            newModelData = new CmsModelDataSet();
                            newModelData.setContentElement(cb)
                            def md = cb.getContentModelDefinition()
                            if (md.getId() > 0) newModelData.setConfiguration(md)
                            newModelData.setLocale(locale)
                        }
                        else
                        {
                            newModelData = ModelDataDAO.getInstance(null).createCopy(modelData, false) as CmsModelDataSet;
                        }
                        final Collection<Transition> transitions = wfm.
                            getTransitions(contentWorkFlow, wfm.getCurrentState(modelData), contentWorkFlow.getFinalState());
                        if (transitions.size() > 0)
                        {
                            Transition t = transitions.iterator().next();
                            bc.getEditorManager().mergePageElementChanges(source, t,
                                locale, modelData, newModelData);
                        }
                        else
                            _logger.error('Unable to force the transition to published.');
                    }
                }
            }
        }

    }

    @Override
    public void render(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain) throws IOException
    {
        if (cer == null)
        {
            CmsSession att = request.getSession(Scope.REQUEST)
            def json = att.getObject(ATT_JSON_RESPONSE, null);
            if (json)
            {
                Gson gson = new GsonBuilder()
                    .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                // This does some escaping which is hard to work with.
                // The code creating the objects is responsible for escaping, just like CMS components.
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
                gson.toJson(json, response.getContentWriter())
            }
            return;
        }
        final isXML = request.isRequestForXmlContent()
        def ndeHelper = new NDEHelper()
        def cw = response.contentWriter
        List<NDE> jsInline
        if (!request.isPartial())
        {
            cw.println(HTMLDoctype.html5.getDeclaration(isXML))
            cw.print('<html')
            cw.printHTMLAttribute('lang', MIWTUtil.getInstance().getLangCode(request.getUserLocale()))
            cw.println('>')
            cw.print('<head>')
            cw.print('<title>Component Editor</title>')
            final Set<NDE> processed = new TreeSet<NDE>(new NDEResourceComparator());
            jsInline = ndeHelper.outputNDEs(request, response, processed, isXML)
            cw.print('</head>')
            cw.print('<body>')
        }
        super.render(request, response, chain);

        if (!request.isPartial())
        {
            if (!jsInline.isEmpty())
            {
                for (NDE nde : jsInline)
                {
                    ndeHelper.writeResource(nde.getResource(), cw);
                }
            }
            cw.print('</body>')
            cw.print('</html>')
        }
    }

    @Override
    public String getIdentity(CmsRequest<ScriptedBase> request)
    {
        return request.getPageElement().getLastModified().toString();
    }

    @Override
    public Scope getScope()
    {
        return Scope.REQUEST;
    }

    @Override
    public long getDataSourceTimestamp(CmsRequest<ScriptedBase> request)
    {
        return request.getPageElement().getLastModified().getTime();
    }

    @Override
    public long getExpireTime()
    {
        return 0;
    }
}

/**
 * LiveEdit API generator.
 * @author Russ Tennant (russ@i2rd.com)
 */
class LiveEditJSONGenerator extends GeneratorImpl<ScriptedBase>
{
    enum OperationType
    {
        //permissions,
        page,
        site,
        component,
        component_search,
    }

    def scriptContext
    // Automatically set by the GroovyScriptedPageElement (if used)
    def hasPageMgtAccess = false
    ParsedRequest prp;
    //context
    OperationType opType

    public LiveEditJSONGenerator()
    {
        super();
        setScope(Scope.REQUEST)
    }


    @Override
    void preRenderProcess(CmsRequest<ScriptedBase> request, CmsResponse response, ProcessChain chain)
    {
        def origin = request.getHeaderValue('Origin');
        if (origin)
        {
            ParsedRequest originPRP = URLParser.parseRequest(URI.create(origin), 'GET')
            if (originPRP.getSiteHostname() != null)
            {
                // Must be a CMS site to enable CORS to the backend.
                response.addHeader('Access-Control-Allow-Origin', origin);
                response.addHeader('Access-Control-Allow-Credentials', 'true');
            }
        }

        if (!request.isSecure())
        {
            if ('GET'.equalsIgnoreCase(request.getMethod()) && response.redirectForSSLIfNeeded())
                return
            response.sendError(RequestError.BAD_REQUEST, 'Request must be secure')
            return
        }
        def url = request.getParameter('url', (String) request.getReferer())
        if (!url)
        {
            response.sendError(RequestError.BAD_REQUEST, "A URL must be specified or a referer must be present");
            return;
        }
        ParsedRequest check = null;
        try
        {
            check = URLParser.parseRequest(Link.createURI(url), request.getMethod());
        }
        catch (java.lang.Exception e)
        {
            response.sendError(ResponseError.APPPLICATION_ERROR, e.getMessage());
            LogManager.getRootLogger().error('Unable to parse request.', e);
            return;
        }
        if (check.hasError())
        {
            response.sendError(check.getError(), check.getErrorPhrase());
            return;
        }
        if (check.getSiteHostname() == null || check.getRenderedElement() == null)
        {
            response.sendError(RequestError.BAD_REQUEST, "Unable to find page in CMS");
            return;
        }
        def p = request.getPrincipal()
        if (p == null)
        {
            response.sendError(RequestError.UNAUTHORIZED, "Not authenticated.");
            return;
        }
        if (!CmsFrontendDAO.getInstance().canOperateSite(p, check.getSiteHostname().getSite(), CmsSiteCapabilityOperation.admin))
        {
            response.sendError(RequestError.FORBIDDEN, "Not authorized for site.");
            return;
        }
        if (!PermissionHelperUtil.getInstance().currentPrincipalHasPermission(PageDataPermissionHelper.view))
        {
            response.sendError(RequestError.FORBIDDEN, "Not authorized to view page data.");
            return;
        }

        if (request.pathInfo == '/ping') return;

        PermissionDAO app = ApplicationContextBean.DAO_PERMISSION.getBean();
        def pmPerm = app.getPermission(PagePermission.class, 'page_editor_page', CredentialPolicyLevel.MEDIUM, null);
        this.hasPageMgtAccess = pmPerm == null ? false : PrincipalDAO.getInstance().principalHasPermission(p, pmPerm)
        this.prp = check

        String[] parts = StringFactory.trimSlashes(request.pathInfo ?: '').split('/')
        if (parts)
        {
            final siteIdStr = String.valueOf(this.prp.getSiteHostname().getSite().getId())
            switch (parts[0])
            {
                case 'page':
                    opType = OperationType.page;
                    break;
                case 'component_search':
                    opType = OperationType.component_search;
                    break;
                case 'site':
                case siteIdStr:
                    opType = OperationType.site;
                    if (parts.length >= 3)
                    {
                        opType = null;
                        if (parts[1] == 'pe')
                        {
                            try
                            {
                                LiveEditPageElement.updatePRPWithPEPath(prp, parts[2])
                            }
                            catch (IllegalArgumentException iae)
                            {
                                response.sendError(RequestError.BAD_REQUEST, 'Bad path: ' + parts[2] + '. ' + iae)
                                return
                            }
                            opType = OperationType.component
                        }
                    }
                    break;
            }
        }
        if (opType == null)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Unsupported operation')
        }
        else
        {
            PreferencesUtil.getInstance().setSelectedSite(this.prp.getSiteHostname().getSite())
        }
    }


    @Override
    void render(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain) throws IOException
    {
        if (response.isCommitted() || this.prp == null || this.opType == null)
            return;

        renderInfo(request, response, chain)
    }

    void renderInfo(CmsRequest<ScriptedBase> request, CmsResponse response, RenderChain chain) throws IOException
    {
        EntityUtilWriter pw = response.getContentWriter();
        Set<Option> options;
        def pv = request.getParameterView(ParameterView.Context.TARGETED)
        if (pv.get('option') == 'all')
            options = EnumSet.allOf(Option.class)
        else
        {
            options = EnumSet.noneOf(Option.class)
            Option.values().each {Option option -> if (pv.getBoolean(option.name(), false)) options.add(option)}
        }
        PluginContext ctx = new PluginContext(this.prp, options)
        try
        {
            ctx.setPermissionHelperUtil(PermissionHelperUtil.getInstance()) // session should exist
            ctx.activate();
            def wpi = ['status': 500];
            switch (this.opType)
            {
                case OperationType.page:
                    wpi = WebPageInfo.create(ctx)
                    break;
                case OperationType.site:
                    wpi = SiteInfo.create(ctx, prp.getSiteHostname().getSite())
                    break;
                case OperationType.component:
                    wpi = ComponentInfo.create(ctx, prp.getRenderedElement())
                    break;
                case OperationType.component_search:
                    if (!ctx.getOptions().isEmpty())
                    {
                        LogManager.getRootLogger().warn('Options may interfere with component search.');
                    }
                    wpi = ComponentSearch.create(ctx)
                    break;
                default: break;
            }

            Gson gson = new GsonBuilder()
                .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
            // This does some escaping which is hard to work with.
            // The code creating the objects is responsible for escaping, just like CMS components.
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
            gson.toJson(wpi, pw)
        }
        finally
        {
            ctx.deactivate();
        }
    }
}


class LiveEditEditor extends ContentBuilderBasedEditor<ContentBuilder<LEComponentProperty>>
{
    def l10n = {it -> new LocalizedText(String.valueOf(it))}

    ComboBox modeSelector = new ComboBox(new SimpleListModel<Object>(LEMode.values()))

    LiveEditEditor()
    {
        super(ContentBuilder.class)
    }

    @Override
    void createUI(EditorUI editorUI)
    {
        super.createUI(editorUI);
        def b = getBuilder();
        modeSelector.setSelectedObject(b.getPropertyEnumValue(LEComponentProperty.Mode, LEMode.API))
        editorUI.addComponent(Container.of('',
            new Label(l10n('Mode')),
            modeSelector));
    }

    @Override
    protected void _updateBuilder()
    {
        def b = getBuilder();
        b.setPropertyEnumValue(LEComponentProperty.Mode, modeSelector.getSelectedObject() as LEMode)
    }


}

class LiveEditPageElement extends ScriptedPageElementImpl
{
    def scriptContext

    LiveEditPageElement(ScriptedBase basePageElement)
    {
        super(basePageElement);
    }

    ContentBuilder<LEComponentProperty> getDataModel(CmsRequest<? extends PageElement> request)
    {
        final key = 'LEAPIComponent.datamodel'
        ContentBuilder<LEComponentProperty> scb = request.getSession(Scope.REQUEST).getObject(key, null);
        if (scb == null)
        {
            scb = ContentBuilder.load(request.getPageElementData(), ContentBuilder.class, true);
            request.getSession(Scope.REQUEST).setObject(key, scb);
        }
        return scb;
    }

    @Override
    Generator getGenerator(CmsRequest<? extends PageElement> request)
    {
        ContentBuilder<LEComponentProperty> scb = getDataModel(request);
        def mode = scb.getPropertyEnumValue(LEComponentProperty.Mode, LEMode.API)
        switch (mode)
        {
            case LEMode.API:
                return new LiveEditJSONGenerator(scriptContext: scriptContext)
            case LEMode.Editor:
                return new LiveEditComponentEditorGenerator(scriptContext: scriptContext)
            default:
                return null;
        }
    }

    @Override
    public Negotiable getNegotiable(PageElementPath requestPath)
    {
        return new NegotiableImpl() {
            @Override
            public ContentType negotiateContentType(CmsRequest<? extends PageElement> request, List<LenientContentType> accept,
                UserAgent ua)
            {
                ContentBuilder<LEComponentProperty> scb = getDataModel(request);
                def mode = scb.getPropertyEnumValue(LEComponentProperty.Mode, LEMode.API)
                switch (mode)
                {
                    case LEMode.API:
                        return HTMLPageElementUtil.APPLICATION_JSON;
                    default:
                        return super.negotiateContentType(request, accept, ua);
                }
            }
        };
    }

    @Override
    List<? extends PageElement> getChildPageElements(CmsRequest<? extends PageElement> context)
    {
        // If you want to use the builtin delegation provided by ProcessChain and RenderChain, override this method.
        getBasePageElement().getDelegates()
        // alternate syntax -> return context.getPageElement().getDelegates()
    }

    /**
     * Update the PageElement[Id]Path of the PRP with the specified path string.
     * @param prp the PRP.
     * @param pePath the path string.
     * @throws IllegalArgumentException if the pePath is invalid.
     */
    static void updatePRPWithPEPath(ParsedRequest prp, String pePath)
        throws IllegalArgumentException
    {
        def pathIds = pePath.split(',') as List<String>;
        def pageElementPath = PageElementChain.getPageElementChainPath(pathIds, false)
        if (pageElementPath.size() == 1 && pageElementPath.first().equals(prp.getPageElementRequestPath().getPageElement()))
        {
            // Checking root
            prp.setPageElementIdChainPath([])
            prp.setPageElementChainPath([])
            prp.setRenderedElement(pageElementPath.first());
        }
        else
        {
            prp.setRenderedElement(null);
            prp.setPartial(true)
            prp.setPageElementIdChainPath(pathIds)
            prp.setPageElementChainPath(pageElementPath)
        }
    }
}


factory = new GroovyScriptedPageElementFactory(
    {new FixedScriptedPageElementModel(LiveEditEditor.class)},
    {new LiveEditPageElement(it)},
    context)

