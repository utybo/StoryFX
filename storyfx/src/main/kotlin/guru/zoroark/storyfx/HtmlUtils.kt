/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx

import guru.zoroark.libstorytree.StoryFont
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

val baseCss =
        """
        <style type="text/css">
            .node-container {
                margin: 4px;
                z-index: 10;
                -webkit-user-select: none;
            }
            
            .overlay-container {
                position: fixed;
                display: block;
                top: 0px;
                left: 0px;
                height: 100%;
                width: 100%;
                z-index: -1;
                -webkit-user-select: none;
            }
        </style>
        """.trimIndent()

fun toNodeHtml(text: String, font: StoryFont, backgroundB64: String? = null, darkMode: Boolean = false): String {
    val sb: StringBuilder = StringBuilder()
    sb.append("<html><head><meta charset=\"utf-8\"/><style type=\"text/css\">body {font-family: '")
    sb.append(font.fontName)
    sb.append("', serif;")
    sb.append("font-size: 14px;}</style>")
    sb.append(baseCss)
    sb.append(additionalStyling(darkMode))
    sb.append(generateBackgroundStyle(backgroundB64, darkMode))
    sb.append("</head><body><div class=\"overlay-container\"></div><div class=\"node-container\">")
    sb.append(mdToHtml(text))
    sb.append("</div></body></html>")
    return sb.toString()
}/*
        <html>
            <head>
                <meta charset="utf-8"/>
                <style type="text/css">
                    body {
                        font-family: "${font.fontName}", serif;
                        font-size: 14px;
                    }
                </style>
                ${generateBackgroundStyle(backgroundB64)}
            </head>
            <body>
                <div class="node-container">
                    ${mdToHtml(text)}
                </div>
            </body>
        </html>*/

fun additionalStyling(darkMode: Boolean): String =
        if (darkMode)
            """
            <style type="text/css">
                body {
                    color: #f0f0f0
                }
            </style>
            """.trimIndent()
        else
            ""

private fun generateBackgroundStyle(bg64: String?, darkMode: Boolean): String {
    if (bg64 == null)
        return if (darkMode) "<style type=\"text/css\">body {background-color: #000000;}</style>" else ""
    val sb = StringBuilder()
    sb.append("<style type=\"text/css\">body {background-image:url('data:image;base64,")
    sb.append(bg64)
    sb.append("');background-size: cover;background-position: center;background-attachment: fixed;}")
    sb.append(".overlay-container {background-color: #")
    sb.append(if (darkMode) "00000088" else "ffffff88")
    sb.append(";</style>")
    return sb.toString()
    /*<style type="text/css">
    body {
        background-image:url('data:image;base64,$bg64');
        background-size: cover;
        background-position: center;
        background-attachment: fixed;
    }

    .bg-container {
        background-color: #ffffff88
    }
    </style>*/
}

fun mdToHtml(text: String): String {
    val parser = Parser.builder().build()
    val document = parser.parse(text)
    val renderer = HtmlRenderer.builder().build()
    return renderer.render(document)
}
