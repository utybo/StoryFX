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
import java.io.InputStream
import java.util.*

open class Base64Resource(override val name: String, val base64: String) : Resource {

    var bytes: ByteArray? = null

    constructor(name: String, bytes: ByteArray) : this(name, Base64.getEncoder().encodeToString(bytes)) {
        this.bytes = bytes
    }

    override fun openStream(): InputStream {
        if (bytes == null)
            bytes = Base64.getDecoder().decode(base64)
        return bytes!!.inputStream()
    }
}