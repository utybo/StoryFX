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
import javafx.application.Platform
import javafx.event.EventTarget
import javafx.scene.control.Alert
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CountDownLatch

fun showBuilderError(headerText: String, ex: StoryBuilderException) {
    val alert = Alert(Alert.AlertType.ERROR).apply {
        isResizable = true
        title = "An error occurred"
        this.headerText = headerText
        dialogPane.content = vbox {
            ex.message?.let { label(it + if (ex.cause != null) " (${ex.cause!!.message})" else "") }
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

fun generateCauseInformation(ex: Throwable): String {
    val cause = ex.cause
    if (cause != null) {
        if (cause.message != null) {
            return "\n\nCaused by: ${cause.message} (${cause::class.simpleName})"
        }
        return "\n\nCaused by an issue of type: ${cause::class.simpleName}"
    }
    return ""
}

infix fun String?.orIfNullOrEmpty(s: String) = if (this == null || isBlank()) s else this

private fun generateRelevantST(ex: Throwable): List<String> =
        ex.stackTrace.filter { it.fileName?.endsWith(".kts", ignoreCase = true) ?: false }
                .map {
                    "- In ${it.fileName} at line ${it.lineNumber.orNaIfNeg()}, within ${it.methodName} of ${it.className}"
                } +
                if (ex.cause != null) generateRelevantST(ex.cause!!) else listOf()

private fun Int.orNaIfNeg(): String = if (this < 0) "N/A" else this.toString()

private fun Exception.stackTraceToString(): String {
    val sw = StringWriter()
    PrintWriter(sw).use {
        this.printStackTrace(it)
        it.close()
    }
    return sw.toString()
}

fun EventTarget.icon(iconName: String, op: FontIcon.() -> Unit = {}) = FontIcon(iconName).attachTo(this, op)

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