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
 * The story engine all implementations must support. It only consists
 * of two methods: warn and error, which are used for reporting information
 * from the story
 */
interface BaseEngine {
    /**
     * Show a warning to the end user, usually appearing in a popup.
     *
     * @param message The message to display
     */
    fun warn(message: String)

    /**
     * Show an error to the end user, usually appearing in a popup.
     *
     * @param message The message to display
     */
    fun error(message: String)
}

/**
 * An engine that supports resources (loading and retrieval). You should not interact
 * with these functions directly. Use the loadResources() and resources\["resourceName"]
 * functions from the StoryBuilder.
 */
interface ResourceEngine : BaseEngine {

    /**
     * Get a [Resource] which has the following name. For example, if the file `myResource.png`
     * is placed in the resources folder, calling `getResource("myResource.png")` will return
     * the resource that corresponds to that file.
     *
     * @param resourceName the path to the resource, relative to the resources folder.
     */
    fun getResource(resourceName: String): Resource

    /**
     * Make the engine load the resources in the resources folder.
     */
    fun loadResources()
}

/**
 * Represents a resource placed in the resources folder
 */
interface Resource {
    /**
     * The name of the resource this object represents
     */
    val name: String
}

/**
 * A more advanced engine that supports:
 *
 * * Input from the user ([askInput])
 * * Image backgrounds ([imageBackground])
 * * Fonts ([font])
 */
interface CommonEngine : BaseEngine {

    /**
     * Ask the user for input (as a string) and returns what they answered,
     * usually in a popup. The user is forced to reply with a non-empty string.
     *
     * @param question The question that is displayed when the popup appears.
     */
    fun askInput(question: String): String

    var imageBackground: Resource?
    var font: StoryFont
}