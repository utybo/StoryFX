/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.story

import guru.zoroark.libstorytree.StoryEnvironment
import guru.zoroark.libstorytree.StoryException
import guru.zoroark.libstorytree.dsl.buildStoryDsl
import guru.zoroark.libstorytree.parseStoryText
import guru.zoroark.storyfx.impl.Base64Resource
import guru.zoroark.storyfx.impl.FileResource
import guru.zoroark.storyfx.showBuilderError
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*
import java.io.File
import java.io.FileNotFoundException

/**
 * Controller for the story loading panel that is shown while the story is
 * being loaded.
 */
class StoryLoadingController : Controller() {
    val loadFailedOnce = SimpleBooleanProperty(false)
    val storyController: StoryController by inject()
    val nodeController: StoryNodeController by inject()
    val loadFailed = SimpleBooleanProperty(false)
    val loadingInfo: TaskStatus = TaskStatus()
    val loadFrom = (scope as? StoryScope)?.loadFrom ?: kotlin.error("Illegal state")
    lateinit var task: FXTask<*>

    /**
     * Asynchronously load the story. Takes care of updating the task.
     */
    fun load() {
        runAsync(loadingInfo) {
            updateMessage("Loading story...")
            updateProgress(-1, 10)
            try {
                task = this
                val stories = if (loadFrom.extension == "kts")
                    buildStoryDsl(loadFrom, StoryEnvironment(nodeController))
                else
                    listOf(parseStoryText(loadFrom))

                if (stories.isEmpty())
                    throw StoryException("No stories described in file")

                // TODO change this to support loading multiple stories
                storyController.model.rebind { item = stories[0] }
                updateProgress(1, 1)
                updateMessage("Done")
                Platform.runLater {
                    storyController.loadingFinished()
                }
            } catch (e: StoryException) {
                if (e is StoryLoadingAbortedException || e.cause is StoryLoadingAbortedException)
                    return@runAsync // Give up: the loading was explicitly aborted by the user
                updateProgress(1, 1) // the bar is shown in orange here
                updateMessage("Error")
                Platform.runLater {
                    loadFailedOnce.value = true
                    loadFailed.value = true
                    showBuilderError("Error while loading the story", e, find<StoryLoadingView>().currentWindow)
                }
            }
        }
    }

    internal fun loadResources(cachedResources: MutableMap<String, Base64Resource>) {
        val from = (scope as? StoryScope)?.loadFrom ?: kotlin.error("Invalid scope")
        val resourcesFolder = File(from.parentFile, "resources")
        task.updateMessage("Exploring resources...")
        val resourcesFiles = resourcesFolder.getTreeListOfChildren()

        for ((i, pair) in resourcesFiles.withIndex()) {
            val name = pair.first
            task.updateProgress(i.toLong(), resourcesFiles.size.toLong())
            task.updateMessage("Loading resource: $name")
            cachedResources[name] = FileResource(name, pair.second)
        }
        task.updateMessage("Resources loaded, continuing...")
    }

    private fun File.getTreeListOfChildren(): List<Pair<String, File>> {
        val list = mutableListOf<Pair<String, File>>()
        visitFileTree(this) { x, y ->
            list += y to x
        }
        return list
    }

    private fun visitFileTree(root: File, rootName: String = "", action: (File, String) -> Unit) {
        val children = root.listFiles()
                ?: throw FileNotFoundException("Root is a file or doesn't exist")
        for (file in children) {
            if (file.isDirectory)
                visitFileTree(file, "$rootName${file.name}/", action)
            else
                action(file, rootName + file.name)
        }
    }
}