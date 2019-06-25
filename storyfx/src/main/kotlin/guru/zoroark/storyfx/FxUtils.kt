/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx

import guru.zoroark.libstorytree.dsl.StoryBuilderException
import javafx.event.EventTarget
import javafx.scene.control.Alert
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

fun showBuilderError(ex: StoryBuilderException) {
    val alert = Alert(Alert.AlertType.ERROR).apply {
        isResizable = true
        title = "An error occurred"
        headerText = "Error while loading story"
        dialogPane.content = vbox {
            ex.message?.let { label(it + if (ex.cause != null) " (${ex.cause!!.message})" else "") }
            textarea(ex.diagnosticsMessage) {
                prefRowCount = 20
                prefColumnCount = 50
            }
        }
    }
    alert.showAndWait()
}

fun EventTarget.icon(iconName: String, op: FontIcon.() -> Unit = {}) = FontIcon(iconName).attachTo(this, op)