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

interface ResourceCapable {
    /** Internal Use.
     * @see css
     */
    val cssPaths: MutableList<String>
    /** Internal Use.
     * @see javaScript
     */
    val javaScriptPaths: MutableList<String>

    /**
     * Add a css resource to this element.
     *
     * A partial path like "templates/main.min.css" can be specified instead of something like
     * "Stylesheets/ThemeName/templates/main.min.css". The only constraint is that the partial path
     * must result in matching a single file. If it matches multiple files an error is generated.
     *
     * @param path the full or partial path.
     */
    fun css(path: String) {
        cssPaths.add(path)
    }

    /**
     * Add a javaScript resource to this element.
     *
     * A partial path like "templates/main.min.js" can be specified instead of something like
     * "JavaScript/ThemeName/templates/main.min.js". The only constraint is that the partial path
     * must result in matching a single file. If it matches multiple files an error is generated.
     *
     * @param path the full or partial path.
     */
    fun javaScript(path: String) {
        javaScriptPaths.add(path)
    }
}