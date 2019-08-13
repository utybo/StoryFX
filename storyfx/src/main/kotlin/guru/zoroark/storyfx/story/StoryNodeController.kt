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
import guru.zoroark.libstorytree.dsl.StoryBuilderException
import guru.zoroark.storyfx.icon
import guru.zoroark.storyfx.impl.Base64Resource
import guru.zoroark.storyfx.showBuilderError
import guru.zoroark.storyfx.styles.StoryFxCommonStyles
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Modality
import tornadofx.*
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.CountDownLatch

class StoryNodeController : Controller(), CommonEngine, ResourceEngine {
    private val nodeView: StoryNodeView by inject()
    val currentNode = SimpleObjectProperty<StoryNode?>(null)
    val story: StoryModel by inject()
    override var font: StoryFont = StoryFont.MERRIWEATHER
    private var _imgBg: Base64Resource? = null
    override var imageBackground: Resource?
        get() = _imgBg
        set(value) {
            _imgBg = value as Base64Resource
        }

    private fun showOptions(node: StoryNode) {
        nodeView.showOptions(node.options.filter { it.isVisible() })
    }

    fun handleOptionPressed(no: StoryOption) {
        try {
            val next = no.onSelected()
            switchToNode(next ?: currentNode.value!!)
        } catch (e: StoryBuilderException) {
            showBuilderError("Error on a does call or an option selection", e)
        }
    }

    fun switchToNode(node: StoryNode) {
        try {
            node.onNodeReached()
            currentNode.value = node
            nodeView.showNodeText()
            showOptions(node)
        } catch (e: StoryBuilderException) {
            showBuilderError("Error on a onNodeReached call or while displaying node", e)
        }
    }


    override fun warn(message: String) {
        runAndWait {
            alert(Alert.AlertType.WARNING, "Warning from story", content = message,
                    owner = primaryStage.owner)
        }
    }


    private fun <T> runAndWait(function: () -> T): T {
        var result: T by singleAssign()
        if (Platform.isFxApplicationThread()) {
            result = function()
        } else {
            val latch = CountDownLatch(1)
            Platform.runLater {
                result = function()
                latch.countDown()
            }
            latch.await()
        }
        return result
    }

    override fun error(message: String) {
        runAndWait {
            alert(Alert.AlertType.ERROR, "Error from story", content = message,
                    owner = primaryStage.owner)
        }
    }

    override fun askInput(question: String): String = runAndWait {
        ensureResult {
            val input = Dialog<String>()
            var txtbox: TextField by singleAssign()
            with(input) {
                initOwner(nodeView.currentWindow)
                initModality(Modality.WINDOW_MODAL)
                title = "Input - ${story.title.value orIfNullOrEmpty "<No name>"}"
                dialogPane.content = vbox(10) {
                    label(question)
                    txtbox = textfield {
                        vboxConstraints {
                            vGrow = Priority.ALWAYS
                        }
                        minWidth = 200.0
                        maxWidth = Double.MAX_VALUE
                    }
                }
                dialogPane += ButtonType.OK
                setResultConverter { if (it == null) null else txtbox.text }
            }
            input.showAndWait()
        }
    }

    private fun <T> ensureResult(function: () -> Optional<T>): T {
        while (true) {
            val result = function()
            if (result.isPresent)
                return result.get()
        }
    }

    private val cachedResources = mutableMapOf<String, Base64Resource>()

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

    override fun choice(cancellable: Boolean, icon: Any?, text: String, title: String?, vararg options: ChoiceOption): ChoiceOption? = runAndWait {
        val dialog = Dialog<ChoiceOption>()
        // Initialize the choice dialog
        with(dialog) {
            initOwner(nodeView.currentWindow)
            initModality(Modality.WINDOW_MODAL)
            graphic = when (icon) {
                is Base64Resource -> ImageView(Image(icon.openStream()))
                is String -> icon(icon) {
                    styleClass.add(StoryFxCommonStyles.mediumIcon.name)
                    style += "-fx-icon-size: 48;"
                }
                else -> null
            }
            dialogPane.content = label(text) {
                isWrapText = true
                prefWidth = 320.0
                minHeight = Region.USE_COMPUTED_SIZE
            }
            this.title = title ?: "Choice - ${story.title.value orIfNullOrEmpty  "<No name>"}"
        }
        // The btypeMap is used for remembering which ButtonType is associated to which ChoiceOption
        // (or null if the ButtonType corresponds to the Cancel option)
        val btypeMap = mutableListOf<Pair<ButtonType, ChoiceOption?>>()
        with(dialog.dialogPane) {
            options.forEach {
                // Generate the button types...
                val btype = ButtonType(it.text)
                btypeMap += btype to it
                this += btype
                with(lookupButton(btype)) {
                    // Customize the buttons
                    val c = it.color
                    val whiteText = it.whiteText
                    style {
                        if (c != null)
                            baseColor = c(c)
                        if (whiteText)
                            textFill = Color.WHITE
                    }
                }
            }

            if (cancellable) {
                btypeMap += ButtonType.CANCEL to null
                this += ButtonType.CANCEL
            }
        }
        dialog.setResultConverter { x ->
            btypeMap.first { it.first == x }.second
        }

        dialog.isResizable = true
        dialog.dialogPane.minHeight = Region.USE_PREF_SIZE

        return@runAndWait if (cancellable)
            dialog.showAndWait().orElse(null)
        else
            ensureResult {
                dialog.showAndWait()
            }
    }

    override fun closeStory() {
        if (Platform.isFxApplicationThread())
            find<StoryController>().close()
        else {
            Platform.runLater {
                find<StoryController>().close()
            }
            throw StoryLoadingAbortedException()
        }
    }

    private infix fun String?.orIfNullOrEmpty(s: String) = if (this == null || isBlank()) s else this

    private operator fun DialogPane.plusAssign(btype: ButtonType) {
        buttonTypes.add(btype)
    }
}


