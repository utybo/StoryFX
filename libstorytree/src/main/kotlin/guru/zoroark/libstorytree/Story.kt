/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

import java.util.*

/**
 * Root class for stories. Stories are objects made of nodes and additional information, such as author, title,
 * identifier and initial node information.
 *
 * Nodes in the same story must never share the same ID, and stories in the same environment must never share the same ID.
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
    private val nodes: TreeSet<StoryNode> = TreeSet(Comparator { x, y -> x.id.compareTo(y.id) })

    /**
     * The code to execute to get the initial node
     */
    var initialNode: () -> StoryNode = {
        this[1] ?: throw StoryException(
                """
                Initial node (with id 1) does not exist.
                
                If your initial node does not have id 1, use
                    initialNode = { nodeRef(...) }
                in your story block to change it.
                """.trimIndent())
    }

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
     * @throws StoryException if the node could not be found
     */
    fun nodeRef(id: String): StoryNode {
        return this[id] ?: throw StoryException("Node with id $id not found in story ${this.id}")
    }

    /**
     * Get the node which has the given ID. This is similar to using the [get] functions, but will never return null.
     *
     * @param id The ID to look for
     * @return The node which has the id passed in the parameter
     * @throws StoryException if the node could not be found
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
                is StoryNode -> throw StoryException("""
                    Story nodes must be given to a 'does' block, not a 'goesTo' block.
                    Replace the 'goesTo' for this option by a 'does'.
                """.trimIndent())
                else -> throw StoryException("Invalid id type: " + x::class.simpleName)
            }
        }
    }

    /**
     * Specifies that this option goes to the node from the same story with the given ID. Usage:
     *
     * ```
     *     option { "My option" } goesTo 2
     *     option { "My other option" ] goesTo "some node"
     * ```
     *
     * Note that `goesTo x` is strictly equivalent to `goesTo { x }` and `does { nodeRef(x) }`, where
     * `x` is an integer or a string.
     *
     * @param id The id of the node this option should lead to
     */
    infix fun StoryOption.goesTo(id: Any) = goesTo { id }

    /**
     * Run a node block on a node that already exists. This can be used to, for
     * example, customize an imported node.
     */
    fun inNode(id: Int, block: StoryNode.() -> Unit) = inNode(id.toString(), block)

    fun inNode(id: String, block: StoryNode.() -> Unit) {
        block(nodeRef(id))
    }

    /**
     * Run a node block in all of the nodes in this story.
     */
    fun inAllNodes(block: StoryNode.() -> Unit) {
        nodes.forEach {
            block(it)
        }
    }
}

