/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

import guru.zoroark.libstorytree.dsl.StoryBuilderException
import kotlin.reflect.KProperty

/**
 * The environment in which the story exist. Provides a way to share values between stories, and gives access
 * to the engine
 *
 * @param engine The engine this environment uses
 */
class StoryEnvironment(val engine: BaseEngine) {
    private val environmentProperties: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Class used for delegating properties to the environment
     */
    inner class EnvironmentDelegator<T>(private val default: T?) {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (environmentProperties.containsKey(property.name)) {
                @Suppress("UNCHECKED_CAST")
                return environmentProperties[property.name] as T
            } else {
                if (default != null) {
                    environmentProperties[property.name] = default
                    return default
                } else {
                    throw StoryBuilderException("No value set and this delegation does not provide a default")
                }
            }

        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            // TODO add proper type checking
            environmentProperties[property.name] = value
        }
    }

    /**
     * Delegate a property to the environment, making it stored in the environment and shared among all of the stories
     * that are in the same environment.
     */
    fun <T> delegated(defaultValue: T): EnvironmentDelegator<T> = EnvironmentDelegator(defaultValue)

    fun <T> delegated() = EnvironmentDelegator<T>(null)
}