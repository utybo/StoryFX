/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree


class Story(
        var title: String = "",
        var author: String = "",
        var id: String = ""
) {
    val nodes: MutableList<StoryNode> = mutableListOf()
    var initialNode: () -> StoryNode = { this[1] ?: error("Initial node does not exist") }

    operator fun get(id: Int): StoryNode? = this[id.toString()]

    operator fun get(id: String): StoryNode? = nodes.firstOrNull { it.id == id }
}

class StoryNode(var id: String) {
    var text: () -> String = { "" }
    val options: MutableList<StoryOption> = mutableListOf()
    var onNodeReached: () -> Unit = {}
}

class StoryOption(
        var text: () -> String,
        var isAvailable: () -> Boolean = { true },
        var isVisible: () -> Boolean = { isAvailable() },
        var onSelected: () -> StoryNode? = { null }
)
