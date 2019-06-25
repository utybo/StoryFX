/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.story

import guru.zoroark.storyfx.app.AppView
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Tab
import tornadofx.*
import java.util.concurrent.Callable

class StoryController : Controller() {
    var tab = SimpleObjectProperty<Tab?>(null)
    val loadingController: StoryLoadingController by inject()
    val nodeController: StoryNodeController by inject()
    val view: StoryView by inject()
    val model: StoryModel by inject()
    val mainView: AppView by inject(FX.defaultScope)

    fun initialize() {
        tab.value?.text = "Loading..."
        loadingController.load()
    }

    @Suppress("ObjectLiteralToLambda")
    fun loadingFinished() {
        tab.value?.textProperty()?.bind(Bindings.createStringBinding(object : Callable<String> {
            override fun call(): String =
                    if (model.title.value.isEmpty())
                        "<No name>"
                    else
                        model.title.value
        }, model.title))

        nodeController.switchToNode(model.item.initialNode.invoke())
        view.switchToNodeView()
    }

    fun close() {
        if (tab.value != null)
            mainView.appTabPane.tabs.remove(tab.value)
    }
}