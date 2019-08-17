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
 * ChoiceOption objects represent a possible choice when using
 * [CommonEngine.choice]. Direct use of the engine's function is not
 * recommended, you should instead consider using the
 * [Choices DSL][guru.zoroark.libstorytree.dsl.ChoicesDsl.choices]
 *
 * @property text The text shown on the button that corresponds to this function
 * @property color The CSS3 background color string that this choice should
 * have, or null to keep the default platform color.
 * @property whiteText True to have the color of the text white, false to have
 * it black. This is useful for when the background color is too dark.
 */
data class ChoiceOption(var text: String, var color: String? = null, var whiteText: Boolean = false)