/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.impl

import guru.zoroark.libstorytree.BaseEngine

object DummyEnv : BaseEngine {
    override fun closeStory() {
    }
    override fun warn(message: String) = kotlin.error("Don't do that")
    override fun error(message: String) = kotlin.error("Don't do that")
}