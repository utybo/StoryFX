/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.styles

import javafx.scene.text.FontWeight
import tornadofx.*

class StoryFxCommonStyles : Stylesheet() {
    companion object {
        val massiveIcon by cssclass()
        val massiveText by cssclass()
    }

    val iconSize by cssproperty<Int>("-fx-icon-size")

    init {
        massiveIcon {
            iconSize.value = 64
        }

        massiveText {
            fontSize = 48.px
            fontWeight = FontWeight.BOLD
        }
    }
}