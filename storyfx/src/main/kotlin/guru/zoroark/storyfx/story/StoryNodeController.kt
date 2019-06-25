/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.story

import guru.zoroark.libstorytree.*
import guru.zoroark.storyfx.impl.Base64Resource
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.Alert
import javafx.scene.control.TextInputDialog
import tornadofx.*
import java.io.FileNotFoundException
import java.util.concurrent.CountDownLatch

class StoryNodeController : Controller(), CommonEngine, ResourceEngine {
    val nodeView: StoryNodeView by inject()
    val currentNode = SimpleObjectProperty<StoryNode?>(null)
    val story: StoryModel by inject()
    override var font: StoryFont = StoryFont.MERRIWEATHER
    private var _imgBg: Base64Resource? = null
    override var imageBackground: Resource?
        get() = _imgBg
        set(value) {
            _imgBg = value as Base64Resource
        }

    fun showOptions(node: StoryNode) {
        nodeView.showOptions(node.options.filter { it.isVisible() })
    }

    fun handleOptionPressed(no: StoryOption) {
        val next = no.onSelected()
        switchToNode(next ?: currentNode.value!!)
    }

    fun switchToNode(node: StoryNode) {
        currentNode.value = node
        node.onNodeReached()
        nodeView.showNodeText()
        showOptions(node)
    }


    override fun warn(message: String) {
        val latch = CountDownLatch(1)
        Platform.runLater {
            alert(Alert.AlertType.WARNING, "Warning from story", content = message,
                    owner = primaryStage.owner)
            latch.countDown()
        }
        latch.await()
    }

    override fun error(message: String) {
        Platform.runLater {
            val latch = CountDownLatch(1)
            Platform.runLater {
                alert(Alert.AlertType.ERROR, "Error from story", content = message,
                        owner = primaryStage.owner)
                latch.countDown()
            }
            latch.await()
        }
    }

    override fun askInput(question: String): String {
        val input = TextInputDialog()
        with(input) {
            title = "StoryFX - ${story.title}"
            headerText = question
        }
        val result = input.showAndWait()
        return result.orElse(askInput(question)) // TODO that but cleaner
    }

    val cachedResources = mutableMapOf<String, Base64Resource>()

    override fun getResource(resourceName: String): Base64Resource {
        return cachedResources[resourceName]
                ?: throw FileNotFoundException("Resource was not loaded and/or does not exist: $resourceName")
    }

    override fun loadResources() {
        find<StoryLoadingController>().loadResources(cachedResources)
    }

    private val modeSwitcherChangeListener: ChangeListener<Boolean> = ChangeListener { _, _, n ->
        nodeView.showNodeText(n)
    }
    fun bindModeSwitcherTo(prop: BooleanProperty) {
        prop.addListener(WeakChangeListener(modeSwitcherChangeListener))
    }
}