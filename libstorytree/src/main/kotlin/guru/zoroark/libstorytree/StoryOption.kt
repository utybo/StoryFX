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