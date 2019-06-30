package guru.zoroark.libstorytree

import guru.zoroark.libstorytree.dsl.does
import guru.zoroark.libstorytree.dsl.node
import guru.zoroark.libstorytree.dsl.nodeRef
import guru.zoroark.libstorytree.dsl.option
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private val optionPattern = "\\{(.+?)}\\s*(.+?)\\s*".toRegex()


fun parseStoryText(input: File): Story {
    return BufferedReader(InputStreamReader(input.inputStream())).use {
        parseStoryText(it)
    }
}

fun parseStoryText(input: BufferedReader): Story {
    val story = Story()
    var currentNode: StoryNode? = null
    input.forEachLine {
        val trimmed = it.trim()
        if (currentNode == null) {
            if ('=' in trimmed) {
                val bits = trimmed.split('=', limit = 1)
                val propertyName = bits[0]
                val propertyValue = bits[1]
                story.applyTxtProperty(propertyName, propertyValue)
                return@forEachLine
            }
        }
        if (!trimmed.isEmpty() && trimmed.first() == '[' && trimmed.last() == ']' && trimmed.length >= 3) {
            val nodeId = trimmed.substring(1, trimmed.length - 1)
            if (currentNode != null) {
                val textToPutIn = currentNode!!.text()
                currentNode!!.text = { textToPutIn }
            }
            currentNode = story.node(nodeId)
            return@forEachLine
        }
        if (currentNode != null) {
            val matched = optionPattern.matchEntire(trimmed)
            if (matched != null) {
                currentNode!!.option { matched.groupValues[1] } does { story.nodeRef(matched.groupValues[2]) }
            } else {
                val textToPutIn = currentNode!!.text() + "\n" + trimmed
                currentNode!!.text = { textToPutIn }
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
            nodeRef(value) ?: error("Node defined in intialNode property does not exist")
        }
    }
}
