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

package experimental.cms.dsl

import com.google.common.collect.Multimap
import com.google.common.io.ByteSource
import com.google.common.io.Files
import com.i2rd.cms.backend.BackendConfig
import com.i2rd.cms.bean.ScriptingBeanPageElementModelFactory
import com.i2rd.cms.component.miwt.MIWTPageElementModelFactory
import com.i2rd.lib.ILibraryType
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryConfiguration
import net.proteusframework.cms.CmsSite
import net.proteusframework.core.StringFactory.trimSlashes
import net.proteusframework.core.html.Element
import net.proteusframework.core.html.EntityUtil
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.mail.support.MimeTypeUtility
import net.proteusframework.core.xml.*
import net.proteusframework.data.filesystem.FileSystemEntityDataSource
import net.proteusframework.email.EmailTemplate
import net.proteusframework.internet.http.Link
import net.proteusframework.ui.management.link.RegisteredLink
import net.proteusframework.ui.miwt.component.Component
import org.apache.logging.log4j.LogManager
import org.xml.sax.Attributes
import java.io.*

internal fun cleanPath(path: String): String = trimSlashes(path.replace('*', '/')).replace(Regex("""/+"""), "/")

interface PathCapable {
    /**
     * The path like "/admin/user-mgt". Wildcard paths end with a "*" like "/admin/user/edit/&#x2a;"
    */
    var path: String

    /**
     * Test if the path is a wildcard path.
     */
    fun isWildcard(): Boolean = path.endsWith("*")

    /**
     * Get a sanitized path that can be set on a [net.proteusframework.cms.PageElementPath]
     */
    fun getCleanPath(): String = cleanPath(path)
}

open class Identifiable(val id: String) {

    override fun toString(): String {
        return "Identifiable(id='$id')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Identifiable) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

open class IdentifiableParent<C>(id: String) : Identifiable(id) {
    internal val children = mutableListOf<C>()
    internal fun add(child: C): C = child.apply { children.add(this) }

    override fun toString(): String {
        return "IdentifiableParent(id='$id', children=$children)"
    }

}

internal class LinkTagConverter(val helper: ContentHelper) : TagListener<TagListenerConfiguration>() {

    val writer: PrintWriter get() = configuration.writer
    val overrideNonVoid = mutableSetOf<String>()

    override fun closeStartElement() {
        writer.append('>')
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes): Boolean {
        val ename = getElementName(localName, qName)
        val convertedAttributes = mutableMapOf<String, String>()
        for (att in ATTS) {
            val value = attributes.getValue(uri, att)
            if (!value.isNullOrBlank()) {
                convertedAttributes.put(att, helper.getInternalLink(value))
                break
            }
        }
        writer.append("<").append(ename)
        for ((key, value) in convertedAttributes.entries) {
            writer.append(" ").append(key).append("=\"").append(value).append("\"")
        }
        XMLUtil.writeAttributes(writer, attributes, convertedAttributes.keys)
        if (isVoidElement(ename))
            writer.append("/>")
        else
            writer.append(">")

        return true
    }

    private fun isVoidElement(ename: String) = HTMLElement.valueOf(ename).kind() == Element.Kind.VOID
        && !overrideNonVoid.contains(ename)


    override fun endElement(uri: String?, localName: String?, qName: String?, empty: Boolean, closedStartElement: Boolean) {
        val ename = getElementName(localName, qName)
        if (isVoidElement(ename))
            return
        writer.append("</").append(ename).append(">")
    }

    override fun getSupportedTags(): Array<out String> = TAGS

    override fun characters(characters: String?, closedStartElement: Boolean): Boolean {
        if (!closedStartElement) writer.append(">")
        writer.append(characters)
        return true
    }

    private fun getElementName(localName: String?, qName: String?): String {
        return (if (localName.isNullOrBlank()) qName!! else localName!!).toLowerCase()
    }

    companion object {
        val TAGS = arrayOf("area", "a", "link", "form", "img", "iframe",
            "video", "source", "audio")
        val ATTS = arrayOf("href", "src", "action")
    }
}

interface PlaceholderHelper {
    fun resolvePlaceholders(template: String): String
}

interface ContentHelper : PlaceholderHelper {
    companion object {
        val logger = LogManager.getLogger(ContentHelper::class.java)!!
        const val PARSER_FAKE_ROOT = "parser_fake_root"
    }

    /**
     * Given a link, return the equivalent internal link.
     * This will convert
     * - CMS Page Paths to CMS Internal Links
     * - File references to Resource Links
     * @param link the link to convert.
     */
    fun getInternalLink(link: String): String

    /**
     * Give a page, return the CMS Link.
     */
    fun getCMSLink(page: Page): Link

    /**
     * Convert XHTML links using #getInternalLink(String)
     * @param html the HTML to convert.
     */
    fun convertXHTML(html: String, nonVoidElements: MutableSet<String> = mutableSetOf()): String {
        val sw = StringWriter(html.length)
        val pw = PrintWriter(sw)
        val htmlParser = GenericParser(pw)
        htmlParser.isHtmlCompatible = true
        val config = TagListenerConfiguration(pw)
        val skipTagListener = SkipTagListener(PARSER_FAKE_ROOT)
        skipTagListener.init(config)
        val linkTagConverter = LinkTagConverter(this)
        linkTagConverter.overrideNonVoid.addAll(nonVoidElements)
        linkTagConverter.init(config)
        htmlParser.setTagListeners(listOf(skipTagListener, linkTagConverter))
        try {
            val parser = XMLUtil.createParserAndWrapException(false, RuntimeException::class.java)
            htmlParser.enableCommentOutput(parser)
            val toParse = StringBuilder(html.length + 40).append('<').append(PARSER_FAKE_ROOT).append('>').append(html)
                .append("</").append(PARSER_FAKE_ROOT).append('>').toString()
            XMLUtil.parseAndWrapException(parser, htmlParser, EntityUtil.escapeForXMLParsing(toParse),
                IOException::class.java)
            return sw.toString()
        } catch (e: IOException) {
            logger.warn("Unable to parse HTML.", e)
        } finally {
            linkTagConverter.destroy()
            skipTagListener.destroy()
        }
        return html
    }

    fun getBackendConfig(): BackendConfig

    fun getMIWTPageElementModelFactory(): MIWTPageElementModelFactory

    fun getScriptingBeanPageElementModelFactory(): ScriptingBeanPageElementModelFactory

    fun getApplicationFunctions(): List<Component>

    fun getCmsSite(): CmsSite

    /**
     * The component identifier.
     * @param componentIdentifier the identifier. Can be looked up using the BackendConfig.
     */
    fun assignToSite(componentIdentifier: String)

    /**
     * Creates a Library, if necessary, and returns the library.
     */
    fun createLibrary(libraryName: String, libraryPath: String, libraryType: String): Library<*>?

    fun saveLibrary(library: Library<*>)
    fun <LT : ILibraryType<LT>?> getLibraryConfiguration(library: Library<LT>): LibraryConfiguration<LT>?
    fun saveLibraryConfiguration(libraryConfiguration: LibraryConfiguration<*>)
    override fun resolvePlaceholders(template: String): String
    fun getEmailTemplate(programmaticName: String): EmailTemplate?
    fun getRegisteredLink(functionName: String, functionContext: String): RegisteredLink?
    fun saveRegisteredLink(registeredLink: RegisteredLink)
    fun <LT:ILibraryType<LT>> setScriptParameters(libraryConfiguration: LibraryConfiguration<LT>, parameters: Multimap<String, Any>)
}


class FileDataSource(val jfile: java.io.File): FileSystemEntityDataSource {
    override fun getLength(): Long = jfile.length()

    override fun getFile(): File? = jfile

    override fun asByteSource(): ByteSource = Files.asByteSource(jfile)

    override fun getName(): String = jfile.name

    override fun getContentType(): String = MimeTypeUtility.getInstance().getContentType(jfile)

    override fun getMetaData(): MutableMap<String, Serializable> = mutableMapOf()

    override fun getOutputStream(): OutputStream = jfile.outputStream()

    override fun getLastModifiedTime(): Long = jfile.lastModified()

    override fun close() {}

    override fun getInputStream(): InputStream = jfile.inputStream()

}

data class AppFunctionPage(val appFunctionName: String, val path: String, val htmlClassName: String)