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

import org.objectweb.asm.Opcodes

internal object OpcodeUtil {

    val arrayTypes = listOf(
        Opcodes.T_BOOLEAN, Opcodes.T_CHAR, Opcodes.T_FLOAT, Opcodes.T_DOUBLE,
        Opcodes.T_BYTE, Opcodes.T_SHORT, Opcodes.T_INT, Opcodes.T_LONG
    )
}
