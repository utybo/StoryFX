/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.app

import guru.zoroark.libstorytree.StoryEnvironment
import guru.zoroark.libstorytree.dsl.buildStoryDsl
import guru.zoroark.storyfx.*
import guru.zoroark.storyfx.impl.DummyEnv
import guru.zoroark.storyfx.story.*
import javafx.application.Platform
import javafx.scene.control.TabPane
import javafx.scene.web.WebView
import javafx.stage.Window
import tornadofx.*
import java.util.concurrent.CountDownLatch

class AppController : Controller() {
    val preloadStatus = TaskStatus()
    val view: AppView by inject()

    fun openNew(tabs: TabPane, currentWindow: Window?) {
        val chosen = chooseFile("Open KTS story", owner = currentWindow, mode = FileChooserMode.Single, filters = arrayOf())
        if (chosen.size == 1) {
            // Create a new scope for each story
            val scope = StoryScope(chosen[0])
            val storyModel = StoryModel()
            setInScope(storyModel, scope)
            val tab = tabs.tab("Story") {
                borderpane {
                    center = find(StoryView::class, scope).root
                }
            }
            val storyController: StoryController = find(scope)
            storyController.tab.value = tab
            storyController.initialize()
            val storyNodeController: StoryNodeController = find(scope)
            storyNodeController.bindModeSwitcherTo(view.isDarkMode)
            tabs.selectionModel.select(tab)
        }
    }

    fun preloadElements() {
        runAsync(preloadStatus) {
            updateProgress(-1, 1)
            updateMessage("Initializing Kotlin DSL library...")
            buildStoryDsl("story {}", StoryEnvironment(DummyEnv))

            updateMessage("Initializing Markdown converter...")
            mdToHtml("*test*")

            updateMessage("Initialiazing web view...")
            val cdl = CountDownLatch(1)
            Platform.runLater {
                with(WebView()) {
                    engine.loadContent("<html><body>Hello</body></html>")
                    engine.loadContent(null)
                    cdl.countDown()
                }
            }
            cdl.await()
            updateMessage("Done!")
            updateProgress(1, 1)
        } ui {
            view.fadeOutProgress()
        }
    }
}