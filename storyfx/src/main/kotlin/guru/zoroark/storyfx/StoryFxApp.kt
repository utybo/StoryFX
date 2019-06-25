/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx

import guru.zoroark.storyfx.app.AppView
import guru.zoroark.storyfx.styles.StoryFxCommonStyles
import guru.zoroark.storyfx.styles.StoryFxStyleBright
import guru.zoroark.storyfx.styles.StoryFxStyleDark
import javafx.scene.text.Font
import javafx.stage.Stage
import tornadofx.*
import java.io.InputStreamReader
import java.util.*

/**
 * JavaFX entry point for StoryFX
 */
class StoryFxApp : App(AppView::class) {
    companion object {
        val darkTheme = StoryFxStyleDark().base64URL.toExternalForm()
        val brightTheme = StoryFxStyleBright().base64URL.toExternalForm()
        val themes = listOf(darkTheme, brightTheme)
        val appVersion: String

        init {
            val props = Properties()
            val stream = StoryFxApp::class.java.getResourceAsStream("/version.properties")
            InputStreamReader(stream).use {
                props.load(it)
            }
            appVersion = props["version"].toString()
        }
    }
    init {
        loadFonts(listOf(
                "Merriweather-Bold.ttf",
                "Merriweather-BoldItalic.ttf",
                "Merriweather-Italic.ttf",
                "Merriweather-Regular.ttf",
                "Muli-Bold.ttf",
                "Muli-BoldItalic.ttf",
                "Muli-Regular.ttf",
                "Muli-Italic.ttf"))
    }

    fun loadFonts(fontNames: List<String>) {
        for (s in fontNames) {
            Font.loadFont(javaClass.getResourceAsStream("/guru/zoroark/storyfx/font/$s"), 12.0)
        }
    }

    override fun start(stage: Stage) {
        stage.width = 800.0
        stage.height = 600.0
        super.start(stage)
    }

    override fun onBeforeShow(view: UIComponent) {
        view.root.stylesheets.add("/guru/zoroark/flattena/flattena.css")
        view.root.stylesheets.add(StoryFxCommonStyles().base64URL.toExternalForm())
        view.root.stylesheets.add(brightTheme)
    }
}