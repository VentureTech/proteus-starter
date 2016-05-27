import com.i2rd.cms.CmsSiteCapabilityOperation
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.generator.AbstractScriptGenerator
import net.proteusframework.cms.component.generator.ContentWrapper
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.cms.controller.CmsResponse
import net.proteusframework.cms.controller.ProcessChain
import net.proteusframework.cms.controller.RenderChain
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.core.html.HTMLUtil
import net.proteusframework.core.io.EntityUtilWriter
import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.users.capability.CapabilityDAO
import net.proteusframework.users.model.Principal
import net.proteusframework.users.model.dao.PrincipalContactUtil
import org.apache.commons.lang.StringUtils

import javax.mail.internet.ContentType
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class RequestStats extends AbstractScriptGenerator {

  final KEY_SLOW_REQUESTS = 'sr'
  final KEY_FILTER_BOTS = 'filter_bots'
  final KEY_CURRENT_SITE = 'current_site'
  final KEY_SEARCH = 's'
  final KEY_LAST_REQUEST_TIME = 'lrt'
  
  def cssID
  // Default to only show requests from within the past 20 minutes
  def lastRequestTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(20)
  def requests
  
  boolean includeWrappingContent(CmsRequest request) {false}
  
  void preRenderProcess(CmsRequest request, CmsResponse response, ProcessChain chain) {
    ContentElement cb = request.getPageElement()
    cssID = cb.getStyleClass()
    if(!cssID) cssID = 'request_stats'
    else cssID = HTMLUtil.normalizeID(cssID)
    this.lastRequestTime = request.getParameter(KEY_LAST_REQUEST_TIME, lastRequestTime)
    final Principal user = request.getPrincipal()
    final CapabilityDAO capproc = ApplicationContextUtils.instance.getContext().getBean(CapabilityDAO.class)
    final boolean hasAllSites = (capproc.hasAllDataCapability(user, CmsSiteCapabilityOperation.admin))
    def params = ['lastTime':new java.util.Date(lastRequestTime)]
    def currentSiteOnly = request.getParameter(KEY_CURRENT_SITE, false)
    def hql = "SELECT rs FROM RequestStatistic as rs WHERE rs.requestTime >= :lastTime" 
    if(currentSiteOnly)
      hql += " AND rs.site.id = ${CmsFrontendDAO.getInstance().getOperationalSite().getId()}"
    else if(!hasAllSites) {
      hql += " AND rs.site IN (:sites)"
      params['sites']=CmsFrontendDAO.getInstance().getSitesUserAdministers(user)
    }
    if(request.getParameter(KEY_FILTER_BOTS, true)) {
      hql += " AND LOWER(rs.userAgent) NOT LIKE '%bot%' AND LOWER(rs.userAgent) NOT LIKE '%nagios%' AND rs.userAgent NOT LIKE '%http://%' AND LOWER(rs.userAgent) NOT LIKE '%spider%' AND rs.userAgent NOT LIKE '%libwww-perl%' AND LOWER(rs.userAgent) NOT LIKE '%crawler%' AND LOWER(rs.userAgent) NOT LIKE '%wget%'  AND LOWER(rs.userAgent) NOT LIKE '%Nutch%'"
    }
    if(request.getParameter(KEY_SLOW_REQUESTS, false)) {
      hql += " AND rs.serviceTime > 850"
    }
    hql += " AND (rs.page.id <> ${request.getPageElementPath().getPageElement().getId()} OR rs.page IS NULL)"
    hql += ' ORDER BY rs.requestTime DESC'
    
    def query = capproc.getSession().createQuery(hql)
    query.setMaxResults(75)
    params.each {k,v ->
      if(v instanceof Collection)
        query.setParameterList(k, v)
      else
        query.setParameter(k, v)
    }
    this.requests = query.list()
  }
  
  void render(CmsRequest request, CmsResponse response, RenderChain chain) throws IOException {
    def user = request.getPrincipal()
    def eet = request.getVoidElementClosing()
    def pw = response.getContentWriter()
    def cw = new ContentWrapper(request, response)
    cw.setIdAttribute(cssID)
    cw.open()
    def formUrl = response.createURL()
    formUrl.outputOpeningFormTag()
    pw.append('<div class="search">')
    pw.append('<div class="search_bar">')
    pw.append('<span class="constraints">')
    yesNo(pw, request, 'Filter Bots? ', KEY_FILTER_BOTS)
    yesNo(pw, request, 'Only Current Site? ', KEY_CURRENT_SITE, 'false')
    yesNo(pw, request, 'Only Slow Requests? ', KEY_SLOW_REQUESTS, 'false')
    pw.append('</span>')
    pw.append('<span class="actions search_actions">')
    pw.append("<input type=\"submit\" name=\"${KEY_SEARCH}\" value=\"Search\" ${eet}")
    pw.append('</span>')
      pw.append('</div>') // .search_bar
    
    pw.append """
<table class="miwt_highlight search_results">
<caption>Recent Requests Hitting Origin Server(s)</caption>
<thead>
<tr>
<th class="first method">Method</th>
<th class="time">Request Time</th>
<th class="url">URL</th>
<th class="status">Status</th>
<th class="partial">Partial</th>
<th class="content_type" title="Content Type">Type</th>
<th class="process_time" title="Process Time">PT</th>
<th class="render_time" title="Render Time">RT</th>
<th class="service_time" title="Service Time">ST</th>
<th class="ip" title="IP Address">IP</th>
<th class="last">User</th>
</tr>
</thead>
<tbody>
"""
    def sdf = new SimpleDateFormat('hh:mm:ss a', request.getLocaleContext().getLocale())
    sdf.setTimeZone(request.getTimeZone())

    this.requests.eachWithIndex {rs, i ->
      def ct = new ContentType(rs.contentType?:'application/octet-stream')
      def speedClass = 'very_fast'
      if(rs.serviceTime > 25 && rs.serviceTime < 250) speedClass = 'fast'
      else if(rs.serviceTime > 250 && rs.serviceTime < 850) speedClass = 'good'
      else if(rs.serviceTime > 850 && rs.serviceTime < 5000) speedClass = 'slow'
      else if(rs.serviceTime > 5000) speedClass = 'very_slow'
      def ua = rs.userAgent
      if(rs.proxyAgent) ua += " via ${rs.proxyAgent}"
      def name = PrincipalContactUtil.getFirstName(rs.userPrincipal) + ' ' + PrincipalContactUtil.getLastName(rs.userPrincipal)
      if(name && rs.sessionId) {
        if(!StringUtils.isBlank(name)) name += '. '
        name += "Session: ${rs.sessionId}"
      }
      def username = rs.userPrincipal?.username
      if(!username && rs.sessionId) username = 'session'
      
      pw.append """
<tr class="${i%2!=0?'even':'odd'} ${speedClass}">
<td title="${ua}">${rs.requestMethod}</td>
<td title="${rs.requestTime}">${sdf.format(rs.requestTime)}</td>
<td title="http://${rs.hostName}/${rs.pagePath?:''} ${rs.contentElement?.name ? ('. Targeting ' + rs.contentElement.name): '' }">
      <a target="_blank" href="http://${rs.hostName}/${rs.pagePath}">${rs.hostName.replace('www.', '').replace('.com','')}/${rs.pagePath?:''}</a></td>
<td title="${rs.status >=400 ? rs.errorPhrase?:'' + '.':''} referer: ${rs.referer?:''}">${rs.status}</td>
<td class="partial" title="">${rs.partial?'[X]':'[ ]'}</td>
<td title="${rs.contentType} served as ${rs.userLocale?.displayName}">${ct.subType}</td>
<td title="${rs.processTime} Milliseconds">${rs.processTime}</td>
<td title="${rs.renderTime} Milliseconds">${rs.renderTime}</td>
<td title="${rs.serviceTime} Milliseconds - ${speedClass.replace('_', ' ').capitalize()}">${rs.serviceTime}</td>
<td title=""><a target="_blank" href="http://www.tcpiputils.com/browse/ip-address/${rs.userIPAddress}">${rs.userIPAddress}</a></td>
<td title="${name}">${username?:''}</td>
</tr>
"""
    }
    pw.append """
</tbody>
</table>
"""  
    pw.append('</div>') // .search
    formUrl.outputClosingFormTag()
    cw.close()
  }
  
  void yesNo(EntityUtilWriter pw, CmsRequest request, def label, def key, def dv='true')
  {
    def val = request.getParameter(key, dv)
    pw.append """
<span class="constraint">
<label>${label}
 <select name="${key}">
 <option value="true" ${val=='true'?'selected="selected"':''}>Yes</option>
 <option value="false" ${val=='false'?'selected="selected"':''}>No</option>
 </select>
</label>
</span>
"""
  }
}

generator = new RequestStats()
