/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree.dsl

import guru.zoroark.libstorytree.ChoiceOption
import guru.zoroark.libstorytree.CommonEngine
import guru.zoroark.libstorytree.StoryException

/**
 * Main entry point for the
 * [Choices DSL](https://storyfx.zoroark.guru/docs/storykts/choices-dsl.html)
 *
 * Asks the user for a choice using the different properties initialized in the
 * init passed in the lambda-with-receiver. For more details, check the official
 * documentation.
 *
 * This entry point does not return anything and should be used for *choices*
 * that *do* things.
 *
 * @param init A lambda-with-receiver for [ChoicesInitializer], with Unit as the
 * generic type parameter.
 */
fun StoryBuilder.choices(init: ChoicesInitializer<Unit>.() -> Unit) {
    askChoice(init)
}

private fun <T> StoryBuilder.askChoice(init: ChoicesInitializer<T>.() -> Unit): Pair<ChoicesInitializer<T>, ChoiceOption?> {
    val eng = requireEngine<CommonEngine>()
    val initializer = ChoicesInitializer<T>()
    init(initializer)
    val chosen = eng.choice(
            cancellable = initializer.canBeCancelled,
            icon = initializer.icon,
            text = initializer.text,
            title = initializer.title,
            options = *initializer.choices.toTypedArray()
    )
    // The following also works for cancelled values, in which case chosen is null.
    initializer.choicesActions.filter { it.first === chosen }.forEach { it.second() }
    return initializer to chosen
}

fun <T> StoryBuilder.fromChoices(init: ChoicesInitializer<T>.() -> Unit): T {
    val (initializer, chosen) = askChoice(init)
    initializer.choicesActions.filter { it.first === chosen }.forEach { it.second() }
    try {
        return initializer.choicesYielding.first { it.first === chosen }.second()
    } catch (e: NoSuchElementException) {
        throw StoryException(
                if (chosen != null)
                    """
                    The choices DSL did not specify any value for when choosing ${chosen.text}.
                    Make sure you specify a value using 'yields {value}' after declaring the option.
                    """.trimIndent()
                else
                    """
                    The choices DSL did not specify any value for when cancelling.
                    Make sure you specify a value using 'yieldIfCancelled {value}' in the 'choices' block.
                    """.trimIndent(),
                cause = e
        )
    }
}

/**
 * Class part of the Choices DSL, used for defining the values and properties
 * of the choice being asked as well as deal with the choice.
 */
class ChoicesInitializer<T> {
    /**
     * Initializer result for whether this can be cancelled or not
     */
    internal var canBeCancelled = false

    /**
     * Initializer result for what the text (question) should be
     */
    internal var text = ""

    /**
     * Initializer result for the title of the choice dialog box.
     */
    internal var title: String? = null

    /**
     * Initializer result for the icon of the choice dialog box
     */
    internal var icon: Any? = null

    /**
     * (DSL) This indicates that the choice box can be cancelled.
     *
     * Usage:
     *
     * ```
     * choices (or fromChoices) {
     *     cancellable
     *     ...
     * }
     * ```
     */
    val cancellable: Unit
        get() {
            canBeCancelled = true
        }

    /**
     * Initializer result for the choices that were defined
     */
    internal val choices = mutableListOf<ChoiceOption>()

    /**
     * Initializer result for what actions should do.
     *
     * It is possible for choices to have multiple actions defined. A null value
     * in a pair means that the action should be done when the choice is
     * cancelled.
     */
    internal val choicesActions = mutableListOf<Pair<ChoiceOption?, () -> Unit>>()

    /**
     * Initializer result for what values are yielded.
     *
     * While it is possible for choices to have multiple yielded values defined,
     * only the first one in the list must be considered. A null ChoiceOption in
     * the pair means that it is the value that should be returned when the
     * choice is cancelled.
     */
    internal val choicesYielding = mutableListOf<Pair<ChoiceOption?, () -> T>>()

    /**
     * (DSL) Define a choice with the give string as its name.
     */
    fun choice(text: String): ChoiceOption {
        val choice = ChoiceOption(text)
        choices += choice
        return choice
    }

    /**
     * Define the text of this choice box. Usage:
     *
     * ```
     * choices (or fromChoices) {
     *     text {
     *         """
     *         This is what is going to be shown in my choice box!
     *         """
     *     }
     *     ...
     * }
     * ```
     */
    fun text(text: () -> String) {
        this.text = text().trimIndent()
    }


    /**
     * Define the icon for this choice box. This can be a string for a value
     * which the player should recognize (check the full documentation), a
     * [Resource][guru.zoroark.libstorytree.Resource], or null (the default) to
     * indicate that no icon should be displayed. Using anything else
     * will result in a crash.
     *
     * ```
     * choices (or fromChoices) {
     *      icon { "mdi-account" }
     *      // Or
     *      icon { resources["myIcon.png"] }
     *      // Or
     *      icon { null }
     *     ...
     * }
     * ```
     */
    fun icon(icon: () -> Any?) {
        this.icon = icon()
    }

    /**
     * Define the title for this choice box. By default, the title is something
     * similar to "Choices - Story title"
     */
    fun title(title: () -> String?) {
        this.title = title()
    }

    /**
     * Define the background color to use with this choice. The color is a CSS3
     * named color (e.g. "red", "green", "blue", ...) or a color with the
     * regular hexadecimal format (e.g. `#ffffff`). You can chain other
     * choice DSL particles (like [withWhiteText] , [does] or [yields]) before
     * or after this.
     *
     * Usage:
     *
     * ```
     * choice(...) withColor "color"
     * ```
     *
     * @param color The color to use for the background of this option
     */
    infix fun ChoiceOption.withColor(color: String): ChoiceOption {
        this.color = color
        return this
    }

    /**
     * Define whether this choice should have white text over its background
     * color. False by default.
     *
     * Usage:
     *
     * ```
     * choice(...) withWhiteText true
     * ```
     *
     * @param enableWhiteText Whether this choice should have white or black
     * text over it. False by default (= black text by default)
     */
    infix fun ChoiceOption.withWhiteText(enableWhiteText: Boolean): ChoiceOption {
        whiteText = enableWhiteText
        return this
    }

    /**
     * Define an action that should be done when the choice is selected.
     *
     * Usage:
     *
     * ```
     * choice(...) does {
     *     // This is executed when the choice is selected
     * }
     * ```
     */
    infix fun ChoiceOption.does(action: () -> Unit) {
        choicesActions += this to action
    }

    /**
     * Define the value that will be returned when this choice is selected.
     * You **have** to define this when using [fromChoices], otherwise
     * you will get a crash. The value depends on what the [fromChoices] is
     * expected to give back.
     *
     * Only use the lambda form of the function if you have some dynamic element
     * involved (e.g. your value depends on an if). You should use the value
     * directly for clarity.
     *
     * Usage:
     *
     * ```
     * // Recommended
     * choice(...) yields value
     *
     * // Only if you need some sort of dynamic expression (an if, a for...)
     * choice(...) yields { value }
     * ```
     */
    infix fun ChoiceOption.yields(yielder: () -> T) {
        choicesYielding += this to yielder
    }

    infix fun ChoiceOption.yields(yielded: T) {
        choicesYielding += this to { yielded }
    }

    /**
     * Define an action that should be done when the choice is cancelled.
     * Only use this in conjunction with [cancellable].
     *
     * ```
     * choices {
     *     ...
     *     doIfCancelled {
     *          // This will be executed when the action is cancelled
     *     }
     * }
     * ```
     */
    fun doIfCancelled(action: () -> Unit) {
        choicesActions += null to action
    }

    /**
     * Define the value that will be returned when in a [fromChoices] that is
     * [cancellable]. Must be defined if the [fromChoices] is cancellable,
     * otherwise you will get a nasty crash.
     *
     * Contrary to [yields], there is no "lambda-less" form of this.
     *
     * ```
     * fromChoices {
     *     ...
     *     yieldIfCancelled { value }
     * }
     * ```
     */
    fun yieldIfCancelled(yielder: () -> T) {
        choicesYielding += null to yielder
    }
}