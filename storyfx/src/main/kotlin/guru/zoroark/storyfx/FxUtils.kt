/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx

import guru.zoroark.libstorytree.StoryException
import javafx.application.Platform
import javafx.event.EventTarget
import javafx.scene.control.Alert
import javafx.stage.Modality
import javafx.stage.Window
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CountDownLatch

/**
 * Function that should be ran to display a StoryException with useful
 * diagnostics formatting
 */
fun showBuilderError(headerText: String, ex: StoryException, owner: Window? = null) {
    val alert = Alert(Alert.AlertType.ERROR).apply {
        initOwner(owner)
        initModality(Modality.WINDOW_MODAL)
        isResizable = true
        title = "An error occurred"
        this.headerText = headerText
        dialogPane.content = vbox(8) {
            ex.message?.let {
                val cause = ex.cause
                label(it + if (cause != null) " (${cause.message})" else "")
            }
            textarea(
                    """
                    |${ex.diagnosticsMessage}${generateCauseInformation(ex)}

                    |Relevant locations in your story files:
                    |${generateRelevantST(ex).joinToString("\n").prependIndent("  ") orIfNullOrEmpty "(None)"}

                    |Full JVM stack trace:
                    |${ex.stackTraceToString()}
                    """.trimMargin(marginPrefix = "|")) {
                prefRowCount = 20
                prefColumnCount = 50
            }
        }
    }
    alert.showAndWait()
}

private fun generateCauseInformation(ex: Throwable): String {
    val cause = ex.cause
    if (cause != null) {
        if (cause.message != null) {
            return "\n\nCaused by: ${cause.message} (${cause::class.simpleName})"
        }
        return "\n\nCaused by an issue of type: ${cause::class.simpleName}"
    }
    return ""
}

private infix fun String?.orIfNullOrEmpty(s: String) = if (this == null || isBlank()) s else this

private fun generateRelevantST(ex: Throwable): List<String> =
        ex.stackTrace.filter {
            it.fileName?.endsWith(".kts", ignoreCase = true) ?: false
        }.map {
            "- In ${it.fileName} at line ${it.lineNumber.orNaIfNeg()}, within ${it.methodName} of ${it.className}"
        } + ex.cause.let {
            if (it != null) generateRelevantST(it) else listOf()
        }

private fun Int.orNaIfNeg(): String = if (this < 0) "N/A" else this.toString()

private fun Exception.stackTraceToString(): String {
    val sw = StringWriter()
    PrintWriter(sw).use {
        this.printStackTrace(it)
        it.close()
    }
    return sw.toString()
}

/**
 * Create a FontIcon just like you'd create other stuff using TornadoFX
 */
fun EventTarget.icon(iconName: String, op: FontIcon.() -> Unit = {}) = FontIcon(iconName).attachTo(this, op)

/**
 * A runAndWait function for JavaFX that doesn't suck and returns something
 */
fun <T> runAndWait(function: () -> T): T {
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