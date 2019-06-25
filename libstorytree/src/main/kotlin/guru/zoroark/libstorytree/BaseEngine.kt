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
     * Send a warning to the engine. The message should be displayed to the
     * end user.
     */
    fun warn(message: String)

    /**
     * Send an error to the engine. The message should be displayed to the
     * end user.
     */
    fun error(message: String)
}

interface ResourceEngine : BaseEngine {
    fun getResource(resourceName: String): Resource
    fun loadResources()
}

interface Resource {
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
    fun askInput(question: String): String

    var imageBackground: Resource?
    var font: StoryFont
}