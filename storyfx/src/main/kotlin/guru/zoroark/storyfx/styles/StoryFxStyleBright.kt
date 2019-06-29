/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.styles

import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Paint
import tornadofx.*

class StoryFxStyleBright : Stylesheet() {
    companion object {
        val errorLabel by cssclass()
    }

    val iconColor by cssproperty<Paint>("-fx-icon-color")

    init {
        root {
            accentColor = c("#4db6ac")
            focusColor = c("#80cbc4")
        }

        errorLabel {
            textFill = Color.RED
            iconColor.value = Color.RED
        }

        errorLabel and progressBar child bar {
            backgroundColor += Color.RED
        }



        StoryFxCommonStyles.massiveIcon {
            iconColor.value = LinearGradient.valueOf("to top, #265c57, #4db6ac")
        }

        StoryFxCommonStyles.massiveText {
            textFill = LinearGradient.valueOf("to top, #265c57, #4db6ac")
        }
    }
}


