/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import org.w3c.dom.Document
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.get

interface CssDeclarationsUriHolder {
    val cssDeclarationsUri: String

    fun addFontsToPage(document: Document = kotlinx.browser.document) {
        val head = document.getElementsByTagName("head")[0]!!
        val link = document.createElement("link") as HTMLLinkElement
        link.rel = "stylesheet"
        link.type = "text/css"
        link.href = cssDeclarationsUri
        head.appendChild(link)
    }
}
