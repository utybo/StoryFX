/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.app

import guru.zoroark.storyfx.StoryFxApp
import guru.zoroark.storyfx.icon
import guru.zoroark.storyfx.styles.StoryFxCommonStyles
import javafx.animation.FadeTransition
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.TabPane
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class AppView : View() {
    var isDarkMode = SimpleBooleanProperty(false)
    val controller: AppController by inject()
    var appTabPane: TabPane by singleAssign()
    private var progress: Node by singleAssign()

    override val root = borderpane {
        title = "StoryFX"
        center {
            appTabPane = tabpane {
                tab("Home") {
                    isClosable = false
                    borderpane {
                        center = vbox(16, alignment = Pos.CENTER) {
                            hbox(16, alignment = Pos.CENTER) {
                                icon("mdi-book-open-page-variant") {
                                    styleClass.add(StoryFxCommonStyles.massiveIcon.name)
                                }
                                label("StoryFX") {
                                    styleClass.add(StoryFxCommonStyles.massiveText.name)
                                }
                            }
                            button("Open") {
                                graphic = FontIcon("gmi-insert-drive-file")
                                action {
                                    controller.openNew(appTabPane, currentWindow)
                                }
                            }
                        }
                        top = hbox(4, alignment = Pos.TOP_RIGHT) {
                            style {
                                opacity = 0.5
                            }
                            paddingAll = 4
                            button("Light theme") {
                                action { changeThemeTo(StoryFxApp.brightTheme, false) }
                            }
                            button("Dark theme") {
                                action { changeThemeTo(StoryFxApp.darkTheme, true) }
                            }
                        }
                        bottom = borderpane {
                            progress = hbox(8, alignment = Pos.CENTER_RIGHT) {
                                label(controller.preloadStatus.message)
                                progressbar(controller.preloadStatus.progress)
                            }
                            right = progress
                            left = label("Version ${StoryFxApp.appVersion}") {
                                enableWhen { SimpleBooleanProperty(false) }
                            }

                            paddingAll = 8
                        }

                        controller.preloadElements()

                    }
                }
            }
        }
    }

    fun fadeOutProgress() {
        val transition = FadeTransition()
        with(transition) {
            node = progress
            fromValue = 1.0
            toValue = 0.0
            duration = Duration.seconds(2.0)
        }
        transition.play()
    }

    private fun changeThemeTo(themeCss: String, isDark: Boolean) {
        root.stylesheets.removeAll(StoryFxApp.themes)
        isDarkMode.value = isDark
        root.stylesheets.add(themeCss)

    }
}