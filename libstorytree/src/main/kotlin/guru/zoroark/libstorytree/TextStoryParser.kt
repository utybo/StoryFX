/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private val optionPattern = "\\{(.+?)}\\s*(.+?)\\s*".toRegex()
private val nodeDefPattern = "\\[.+]".toRegex()

/**
 * Parse a story that is in the story.txt format stored in the given file.
 *
 * @param input The file to read the story.txt story from
 * @return The story that was built
 * @throws
 */
fun parseStoryText(input: File): Story {
    return BufferedReader(InputStreamReader(input.inputStream())).use {
        parseStoryText(it)
    }
}

/**
 * Parse the story.txt formatted story from the given input.
 *
 * @param input The reader from which to read the story.txt formatted data
 * @return The story that was read
 */
fun parseStoryText(input: BufferedReader): Story {
    val story = Story()
    var currentNode: StoryNode? = null
    input.forEachLine {
        val trimmed = it.trim()
        if (trimmed.startsWith("//"))
            return@forEachLine
        if (currentNode == null) {
            if ('=' in trimmed) {
                val bits = trimmed.split('=', limit = 1)
                val propertyName = bits[0].trim()
                val propertyValue = bits[1].trim()
                story.applyTxtProperty(propertyName, propertyValue)
                return@forEachLine
            }
        }
        val nodeDefMatch = nodeDefPattern.matchEntire(trimmed)
        if (nodeDefMatch != null) {
            val nodeId = nodeDefMatch.groupValues[1]
            val toFinalize = currentNode
            if (toFinalize != null) {
                val textToPutIn = toFinalize.text()
                toFinalize.text = { textToPutIn } // Optimization
            }
            currentNode = story.node(nodeId)
        } else currentNode?.run {
            val matched = optionPattern.matchEntire(trimmed)
            if (matched != null) {
                option { matched.groupValues[1] } does { story.nodeRef(matched.groupValues[2]) }
            } else {
                val textToPutIn = text() + "\n$trimmed"
                text = { textToPutIn }
            }

        }
    }
    return story
}

private fun Story.applyTxtProperty(name: String, value: String) {
    when (name) {
        "title" -> title = value
        "author" -> author = value
        "initialNode" -> initialNode = {
            nodeRef(value)
        }
    }
}
