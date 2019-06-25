/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree.dsl

import guru.zoroark.libstorytree.Story
import guru.zoroark.libstorytree.StoryNode
import guru.zoroark.libstorytree.StoryOption

fun Story.node(id: String, body: () -> String = { "" }, init: StoryNode.() -> Unit = {}): StoryNode {
    val node = StoryNode(id)
    node.text = body
    init(node)
    nodes.add(node)
    return node
}

fun Story.node(id: Int, body: () -> String = { "" }, init: StoryNode.() -> Unit = {}): StoryNode =
        node(id.toString(), body, init)

fun Story.nodeRef(id: String) = this[id]

fun Story.nodeRef(id: Int) = this[id.toString()]

fun Story.initialNode(nodeGetter: () -> StoryNode?) {
    initialNode = { nodeGetter() ?: error("Initial node not found") }
}

fun Story.doesNodeExist(id: Int): Boolean = this.doesNodeExist(id.toString())

fun Story.doesNodeExist(id: String): Boolean {
    return nodes.any { it.id == id }
}

infix fun StoryNode.with(init: StoryNode.() -> Unit) {
    init()
}

infix fun StoryNode.body(body: () -> String): StoryNode {
    text = body
    return this
}

fun StoryNode.option(text: () -> String, isAvailable: () -> Boolean = { true }, isVisible: () -> Boolean = { isAvailable() }, onSelected: () -> StoryNode?) {
    options.add(StoryOption(text, isAvailable, isVisible, onSelected))
}

fun StoryNode.option(text: () -> String): StoryOption {
    val opt = StoryOption(text)
    options.add(opt)
    return opt
}

fun StoryNode.onNodeReached(operation: () -> Unit) {
    onNodeReached = operation
}

infix fun StoryOption.does(what: () -> StoryNode?): StoryOption {
    onSelected = what
    return this
}

infix fun StoryOption.availableIf(condition: () -> Boolean): StoryOption {
    isAvailable = condition
    return this
}

infix fun StoryOption.text(text: () -> String): StoryOption {
    this.text = text
    return this
}
