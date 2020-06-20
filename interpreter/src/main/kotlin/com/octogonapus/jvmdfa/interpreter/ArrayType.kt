/*
 * This file is part of JVM-DFA.
 *
 * JVM-DFA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVM-DFA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JVM-DFA.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.octogonapus.jvmdfa.interpreter

sealed class ArrayType {

    /**
     * An array of primitives.
     */
    data class Primitive(val type: Int) : ArrayType() {
        init {
            require(type in OpcodeUtil.arrayTypes)
        }
    }

    /**
     * An array of references.
     *
     * @param desc The descriptor of the reference type, or null if there is no type
     * information.
     */
    data class Ref(val desc: String?) : ArrayType()
}
