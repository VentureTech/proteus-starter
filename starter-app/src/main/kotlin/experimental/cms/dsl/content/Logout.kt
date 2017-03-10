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

package experimental.cms.dsl.content

import com.i2rd.cms.bean.LogoutBean
import com.i2rd.cms.bean.util.LogoutBeanContentBuilder
import com.i2rd.cms.visibility.VisibilityConditionInstance
import com.i2rd.expression.IExpression
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.component.ContentElement
import java.util.regex.Pattern

class Logout(id: String) : Identifiable(id), Content {
    companion object {
        val PAT_CMSLINK = Pattern.compile("getCMSLink\\(['\"](.+)['\"]\\)")!!
        const val DEFAULT_EXPRESSION = """
<ul class="menu menu-t1 menubeanh user">
  <li class="mi mi-inactive mi-parent">
      <div class="menuitemlabel"><i class="fa fa-user-circle-o" aria-hidden="true"></i></div>
      <ul class="menu menu-t2 menubeanh">
        <li class="mi mi-inactive current-user">
          <div class="menuitemlabel">
            <span class="mil">${"$"}{users.current.name}</span>
          </div>
        </li>
        <li class="mi mi-inactive my-profile">
          <a href="${"$"}{links.getCMSLink("/account/my-profile").url}" class="menuitemlabel" title="My Profile">
            <span class="mil">My Profile</span></a>
        </li>
        <li class="mi mi-inactive logout-url">
          <a href="/csarf" class="menuitemlabel" title="Logout">
            <span class="mil">Logout</span></a>
        </li>
      </ul>
  </li>
</ul>
"""
    }
    var logoutExpression = DEFAULT_EXPRESSION

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing?:LogoutBean()
        val builder = LogoutBeanContentBuilder()
        updateBuilder(helper, builder)
        return ContentInstance(contentElement, builder.content)
    }


    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val builder = LogoutBeanContentBuilder.load(contentElement.publishedData[helper.getCmsSite().primaryLocale],
            LogoutBeanContentBuilder::class.java, false)
        updateBuilder(helper, builder)
        return builder.isDirty
    }

    private fun updateBuilder(helper: ContentHelper, builder: LogoutBeanContentBuilder) {
        var expression = helper.convertXHTML(logoutExpression)
        expression = expression.replace(PAT_CMSLINK.toRegex(),
            { "getCMSLink(\"${helper.getInternalLink(it.groupValues[1])}\")" })
        builder.logoutOption = LogoutBean.LogoutOption.expression
        builder.logoutExpressionType = IExpression.Type.FreeMarker
        builder.logoutExpression = expression
    }

    override fun toString(): String {
        return "Logout(" +
            "logoutExpression='$logoutExpression'," +
            "path='$path'," +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }

    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null


}
