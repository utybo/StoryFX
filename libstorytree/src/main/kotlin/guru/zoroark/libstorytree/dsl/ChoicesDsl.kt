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

fun StoryBuilder.choices(init: ChoicesInitializer<Unit>.() -> Unit): ChoiceOption {
    return choices(false, init)!!
}

private fun StoryBuilder.choices(cancellable: Boolean, init: ChoicesInitializer<Unit>.() -> Unit): ChoiceOption? {
    val eng = requireEngine<CommonEngine>()
    val initializer = ChoicesInitializer<Unit>()
    init(initializer)
    val chosen = eng.choice(cancellable, initializer.text, *initializer.choices.toTypedArray())
    // The following also works for cancelled values
    initializer.choicesActions.filter { it.first === chosen }.forEach { it.second() }
    return chosen
}

fun <T> StoryBuilder.fromChoices(init: ChoicesInitializer<T>.() -> Unit): T {
    val eng = requireEngine<CommonEngine>()
    val initializer = ChoicesInitializer<T>()
    init(initializer)
    val chosen = eng.choice(initializer.canBeCancelled, initializer.text, *initializer.choices.toTypedArray())
    initializer.choicesActions.filter { it.first === chosen }.forEach { it.second() }
    try {
        return initializer.choicesYielding.first { it.first === chosen }.second()
    } catch (e: NoSuchElementException) {
        throw StoryBuilderException(
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

class ChoicesInitializer<T> {
    internal var canBeCancelled = false
    internal var text = ""

    val cancellable: Unit
        get() {
            canBeCancelled = true
        }

    val choices = mutableListOf<ChoiceOption>()
    val choicesActions = mutableListOf<Pair<ChoiceOption?, () -> Unit>>()
    val choicesYielding = mutableListOf<Pair<ChoiceOption?, () -> T>>()

    fun choice(text: String): ChoiceOption {
        val choice = ChoiceOption(text)
        choices += choice
        return choice
    }

    fun text(text: () -> String) {
        this.text = text().trimIndent()
    }

    infix fun ChoiceOption.withColor(color: String): ChoiceOption {
        this.color = color
        return this
    }

    infix fun ChoiceOption.withWhiteText(enableWhiteText: Boolean): ChoiceOption {
        whiteText = enableWhiteText
        return this
    }

    infix fun ChoiceOption.does(action: () -> Unit) {
        choicesActions += this to action
    }

    infix fun ChoiceOption.yields(yielder: () -> T) {
        choicesYielding += this to yielder
    }

    infix fun doIfCancelled(action: () -> Unit) {
        choicesActions += null to action
    }

    infix fun yieldIfCancelled(yielder: () -> T) {
        choicesYielding += null to yielder
    }
}