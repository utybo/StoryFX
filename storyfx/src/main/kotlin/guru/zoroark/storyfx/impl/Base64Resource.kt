/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.storyfx.impl

import guru.zoroark.libstorytree.Resource
import tornadofx.*
import java.io.InputStream
import java.util.*

/**
 * Represents any resource in StoryFX
 */
open class Base64Resource(override val name: String, val base64: String) : Resource {
    private var preKnownBytes: ByteArray? = null

    val bytes: ByteArray by lazy {
        preKnownBytes ?: Base64.getDecoder().decode(base64)
    }

    constructor(name: String, bytes: ByteArray) : this(name, Base64.getEncoder().encodeToString(bytes)) {
        preKnownBytes = bytes
    }

    override fun openStream(): InputStream {
        return bytes.inputStream()
    }
}