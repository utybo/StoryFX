/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

/**
 * A node in the story. A node is a "part" of the story the user stops at. Nodes have an ID text and options (which define
 * which nodes the user can go to from the current node).
 *
 * @property id The identifier of the node. IDs must be unique in each story: no nodes must share the same ID within the
 * same story.
 */
class StoryNode(var id: String) {
    /**
     * A lambda for retrieving the text of this node, shown to the user when the node is reached.
     */
    var text: () -> String = { "" }
    /**
     * The options available at this node.
     */
    val options: MutableList<StoryOption> = mutableListOf()

    /**
     * A lambda that is executed whenever the node is reached. Does nothing by default
     */
    var onNodeReached: () -> Unit = {}

    /**
     * Set the [text getter][text] of this node to the given lambda.
     */
    infix fun body(body: () -> String): StoryNode {
        text = body
        return this
    }

    fun bind(vararg bindings: Pair<String, () -> String>) {
        val oldBody = text
        body {
            var s = oldBody()
            for((k, v) in bindings) {
                val replaceBy = v()
                s = s.replace(k, replaceBy)
            }
            return@body s
        }
    }

    /**
     * Create an option with the given text and add it to this node.
     *
     * @param text The option's text
     * @return The option that was created
     */
    fun option(text: () -> String): StoryOption {
        val opt = StoryOption(text)
        options.add(opt)
        return opt
    }

    /**
     * Set what should be done when the node is reached.
     */
    fun onNodeReached(operation: () -> Unit) {
        onNodeReached = operation
    }
}