/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree

import kotlin.reflect.KProperty

/**
 * The environment in which the story exist. Provides a way to share values between stories, and gives access
 * to the engine
 */
class StoryEnvironment(val engine: BaseEngine) {
    private val environmentProperties: MutableMap<String, Any?> = mutableMapOf()

    inner class EnvironmentDelegator<T>(private val default: T) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (environmentProperties.containsKey(property.name)) {
                @Suppress("UNCHECKED_CAST")
                return environmentProperties[property.name] as T
            } else {
                environmentProperties[property.name] = default
                return default
            }
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            // TODO add proper type checking
            environmentProperties[property.name] = value
        }
    }

    fun <T> delegated(defaultValue: T): EnvironmentDelegator<T> = EnvironmentDelegator(defaultValue)

}