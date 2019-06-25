/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
module guru.zoroark.libstorytree {
    exports guru.zoroark.libstorytree;
    exports guru.zoroark.libstorytree.dsl;

    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires kotlin.scripting.jvm.host.embeddable;
    requires kotlin.scripting.common;
    requires kotlin.scripting.jvm;
}