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
import javafx.stage.FileChooser
import javafx.stage.Window
import tornadofx.*
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * Controller for the overall application, in the default scope
 */
class AppController : Controller() {
    val preloadStatus = TaskStatus()
    private val view: AppView by inject()
    private var previousFolder: File? = null

    internal fun openNew(tabs: TabPane, currentWindow: Window?) {
        val chosen = chooseFile("Open KTS story",
                owner = currentWindow,
                mode = FileChooserMode.Single,
                filters = arrayOf(FileChooser.ExtensionFilter("Stories (*.story.kts, *.story.txt)",
                        "*.story.kts", "*.story.txt"))) {
            val prev = previousFolder
            if(prev != null && prev.exists() && prev.isDirectory)
                initialDirectory = prev
        }
        if (chosen.size == 1) {
            previousFolder = chosen[0].parentFile
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

    /**
     * An async function that does some loading (mostly to get the classes in
     * memory and warm everything up)
     */
    internal fun preloadElements() {
        runAsync(preloadStatus) {
            updateProgress(-1, 1)
            updateMessage("Initializing Kotlin DSL library...")
            buildStoryDsl("story {}", StoryEnvironment(DummyEnv))

            updateMessage("Initializing Markdown converter...")
            mdToHtml("*test*")

            updateMessage("Initialiazing web view...")
            Class.forName("javafx.scene.web.WebView")
            updateMessage("Done!")
            updateProgress(1, 1)
        } ui {
            view.fadeOutProgress()
        }
    }
}