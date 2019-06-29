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

class StoryFxStyleDark : Stylesheet() {
    companion object {
        val errorLabel by cssclass()
    }

    val controlInnerBackground by cssproperty<Color>("-fx-control-inner-background")
    val defaultButton by cssproperty<Color>("-fx-default-button")
    val outerBorder by cssproperty<Color>("-fx-outer-border")
    val iconColor by cssproperty<Paint>("-fx-icon-color")

    init {
        root {
            baseColor = c("#1D1D1D")
            backgroundColor += c("#121212")
            controlInnerBackground.value = c("#262626")

            accentColor = c("#4db6ac")
            focusColor = c("#80cbc4")
            defaultButton.value = c("#80cbc4")
            faintFocusColor = c("#80cbc455")

            outerBorder.value = c("#121212")
        }

        tabPane child tabHeaderArea child tabHeaderBackground {
            backgroundColor += c("#262626")
        }

        errorLabel {
            textFill = Color.ORANGE
            iconColor.value = Color.ORANGE
        }

        errorLabel and progressBar child bar {
            backgroundColor += Color.ORANGE
        }

        StoryFxCommonStyles.massiveIcon {
            //iconColor.value = LinearGradient.valueOf("to top, #265c57, #4db6ac")
            iconColor.value = LinearGradient.valueOf("to top, #4db6ac, #95d3cd")
        }

        StoryFxCommonStyles.massiveText {
            //textFill = LinearGradient.valueOf("to top, #265c57, #4db6ac")
            textFill = LinearGradient.valueOf("to top, #4db6ac, #95d3cd")
        }
    }
}