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
 * This class represents story fonts that can be used for the nodes' text. Standard fonts that are always available are
 * defined in the companion object.
 *
 * @property fontName The name of the font represented by this StoryFont object
 */
data class StoryFont(val fontName: String) {
    companion object {
        // Some standard fonts

        /**
         * The [Muli](https://github.com/vernnobile/MuliFont) font, a sans serif font that looks clean and modern
         */
        val MULI = StoryFont("Muli")

        /**
         * The [Merriweather](https://github.com/SorkinType/Merriweather) font, a serif font that looks serious and
         * great for storytelling.
         */
        val MERRIWEATHER = StoryFont("Merriweather")
    }
}