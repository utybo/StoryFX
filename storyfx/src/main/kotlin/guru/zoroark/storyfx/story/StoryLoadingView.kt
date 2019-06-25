/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.story

import guru.zoroark.storyfx.styles.StoryFxStyleDark
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.control.ProgressBar
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.util.concurrent.Callable

class StoryLoadingView : View() {
    val controller: StoryLoadingController by inject()
    private var pg: ProgressBar by singleAssign()
    private var pgAnimation: FadeTransition by singleAssign()

    override val root = borderpane {
        center {
            vbox(4.0, alignment = Pos.CENTER) {
                label(controller.loadingInfo.message) {
                    val fi = FontIcon("gmi-sync-problem")
                    fi.styleClass.add(StoryFxStyleDark.errorLabel.name)
                    controller.loadFailed.addListener { _, _, new ->
                        if (new) {
                            graphic = fi
                            pg.styleClass.add(StoryFxStyleDark.errorLabel.name)
                            pgAnimation.playFromStart()
                            this@label.styleClass.add(StoryFxStyleDark.errorLabel.name)
                        } else {
                            graphic = null
                            pg.styleClass.remove(StoryFxStyleDark.errorLabel.name)
                            pgAnimation.stop()
                            this@label.styleClass.remove(StoryFxStyleDark.errorLabel.name)
                        }
                    }
                }
                pg = progressbar(controller.loadingInfo.progress)
                pgAnimation = FadeTransition(Duration.seconds(2.0), pg)
                with(pgAnimation) {
                    interpolator = Interpolator.EASE_BOTH
                    fromValue = 1.0
                    toValue = 0.2
                    cycleCount = Timeline.INDEFINITE
                    isAutoReverse = true
                }
                hbox(8, alignment = Pos.CENTER) {
                    visibleWhen(controller.loadFailedOnce)
                    enableWhen(controller.loadFailed)
                    button("Retry") {
                        graphic = FontIcon("gmi-sync")
                        action {
                            controller.loadFailed.value = false
                            controller.load()
                        }
                    }
                    button("Close") {
                        visibleWhen {
                            Bindings.createBooleanBinding(
                                    Callable<Boolean> { controller.storyController.tab.value != null },
                                    controller.storyController.tab)
                        }
                        graphic = FontIcon("gmi-close")
                        action {
                            controller.storyController.close()
                        }
                    }
                }
                paddingAll = 4.0
            }
        }
    }
}