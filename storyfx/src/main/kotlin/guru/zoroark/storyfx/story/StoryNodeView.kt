/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.story

import guru.zoroark.libstorytree.StoryOption
import guru.zoroark.storyfx.app.AppView
import guru.zoroark.storyfx.impl.Base64Resource
import guru.zoroark.storyfx.toNodeHtml
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import tornadofx.*

class StoryNodeView : View() {
    companion object {
        const val optionsPerRow = 2
    }

    val controller: StoryNodeController by inject()
    var options: GridPane by singleAssign()
    var webview: WebView by singleAssign()
    val appView: AppView by inject(FX.defaultScope)

    override val root = borderpane {
        center {
            /*textarea(controller.nodeText) {
                isEditable = false
            }*/
            webview = webview {
            }
        }
        bottom {
            options = gridpane {
                hgrow = Priority.ALWAYS
                hgap = 8.0
                vgap = 8.0
                paddingAll = 8.0
            }
        }
    }

    fun showOptions(nodeOptions: List<StoryOption>) {
        with(options) {
            removeAllRows()
            for ((i, no) in nodeOptions.withIndex()) {
                button(no.text()) {
                    disableProperty().value = !no.isAvailable()
                    useMaxWidth = true
                    gridpaneConstraints {
                        hGrow = Priority.ALWAYS
                        columnRowIndex(i % optionsPerRow, i / optionsPerRow)
                    }
                    action { controller.handleOptionPressed(no) }
                }
            }
        }
    }

    fun showNodeText(isDark: Boolean? = null) {
        webview.engine.loadContent(
                toNodeHtml(
                        controller.currentNode.value!!.text().trimIndent(),
                        controller.font,
                        (controller.imageBackground as? Base64Resource)?.base64,
                        isDark ?: appView.isDarkMode.value)
        )
    }
}