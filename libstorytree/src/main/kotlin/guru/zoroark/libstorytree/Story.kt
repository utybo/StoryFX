/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

import guru.zoroark.libstorytree.dsl.StoryBuilderException
import java.util.*

/**
 * Root class for stories. Stories are objects made of nodes and additional information, such as author, title,
 * identifier and initial node information.
 */
class Story(
        /**
         * Title of the story
         */
        var title: String = "",
        /**
         * Author of the story
         */
        var author: String = "",
        /**
         * Identifier of the story, which can be used to reference a story
         */
        var id: String = ""
) {
    /**
     * The nodes contained in the story. No two nodes shall have the same ID.
     */
    private val nodes: TreeSet<StoryNode> = TreeSet(Comparator { x, y -> x.id.compareTo(y.id) })

    /**
     * The code to execute to get the initial node
     */
    var initialNode: () -> StoryNode = { this[1] ?: error("Initial node (with id 1) does not exist") }

    /**
     * Gets the node with the given id. The ID is converted to a string and this operator calls the one which has
     * a String parameter.
     *
     * @return The node, or null if nothing was found.
     */
    operator fun get(id: Int): StoryNode? = this[id.toString()]

    /**
     * Gets the node with the given ID.
     *
     * @return The node, or null if nothing was found.
     */
    operator fun get(id: String): StoryNode? = nodes.firstOrNull { it.id == id }

    /**
     * Create a node with the given id, execute the given node block to initialize it, and add it to this story.
     *
     * @param id The id to give to the node. All the nodes of the same story must have different IDs.
     * @param body The node block to execute.
     * @return The [StoryNode] that was created
     */
    fun node(id: String, body: () -> String = { "" }, init: StoryNode.() -> Unit = {}): StoryNode {
        val node = StoryNode(id)
        node.text = body
        init(node)
        nodes.add(node)
        return node
    }

    /**
     * Create a node with the given id, execute the given node block to initialize it, and add it to this story.
     *
     * @param id The id to give to the node. All the nodes of the same story must have different IDs. This ID is
     * converted to a string.
     * @param body The node block to execute.
     * @return The [StoryNode] that was created.
     */
    fun node(id: Int, body: () -> String = { "" }, init: StoryNode.() -> Unit = {}): StoryNode =
            node(id.toString(), body, init)

    /**
     * Get the node which has the given ID.
     *
     * @param id The ID to look for
     * @return The node which has the id passed in the parameter
     * @throws StoryBuilderException if the node could not be found
     */
    fun nodeRef(id: String): StoryNode {
        return this[id] ?: throw StoryBuilderException("Node with id $id not found in story ${this.id}")
    }

    /**
     * Get the node which has the given ID. This is similar to using the [get] functions, but will never return null.
     *
     * @param id The ID to look for
     * @return The node which has the id passed in the parameter
     * @throws StoryBuilderException if the node could not be found
     */
    fun nodeRef(id: Int) = nodeRef(id.toString())

    /**
     * Set the initial node to the given block.
     */
    fun initialNode(nodeGetter: () -> StoryNode) {
        initialNode = nodeGetter
    }

    /**
     * Checks whether the node with the given ID exists or not. The ID is converted to a [String].
     *
     * @return True if there is a node in this story that has the given ID, false otherwise
     */
    fun doesNodeExist(id: Int): Boolean = this.doesNodeExist(id.toString())

    /**
     * Checks whether the node with the given ID exists or not.
     *
     * @return True if there is a node in this story that has the given ID, false otherwise
     */
    fun doesNodeExist(id: String): Boolean {
        return nodes.any { it.id == id }
    }

    /**
     * Specifies that this option goes to the node from the same story with the given ID. Usage:
     *
     * ```
     *     option { "My option" } goesTo { 2 }
     *     option { "My other option" ] goesTo { "some node" }
     * ```
     *
     * Note that `goesTo { x }` is strictly equivalent to `does { nodeRef(x) }`, where `x` is an integer or a string.
     *
     * @param where An expression that returns either a string or an integer.
     */
    infix fun StoryOption.goesTo(where: () -> Any) {
        onSelected = {
            when (val x = where()) {
                is String -> nodeRef(x)
                is Int -> nodeRef(x)
                else -> throw StoryBuilderException("Invalid id type: " + x::class.simpleName)
            }
        }
    }
}

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

/**
 * An option describes one of the possible actions that can be performed from a node.
 *
 * @property text The text getter that should be shown
 * @property isAvailable Whether this option should be available or not. If unavailable, it may or may not remain
 * visible depending on the [isVisible]. An unavailable node is greyed out and cannot be selected. True by default.
 * @property isVisible Whether this option should be visible or not. Calls [isAvailable] by default.
 * @property onSelected The action that is executed when the node is selected, also specifying what node to go to.
 */
class StoryOption(
        var text: () -> String,
        var isAvailable: () -> Boolean = { true },
        var isVisible: () -> Boolean = { isAvailable() },
        var onSelected: () -> StoryNode? = { null }
) {
    /**
     * Define what should be done when the option is selected.
     *
     * @param what What should be done. It returns the node to go to, or null if we should stay on the same node. The
     * node will still be reloaded, meaning that its text will be refreshed and
     * [the node's onNodeReached][StoryNode.onNodeReached] is called.
     *
     * @return this
     */
    infix fun does(what: () -> StoryNode?): StoryOption {
        onSelected = what
        return this
    }

    /**
     * Define the lambda that is called to determine whether the option should be greyed out (false) or available
     * (true).
     *
     * @param condition The lambda to use
     * @return this
     */
    infix fun availableIf(condition: () -> Boolean): StoryOption {
        isAvailable = condition
        return this
    }
}
