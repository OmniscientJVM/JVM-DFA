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

import io.kotest.core.spec.style.StringSpec
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MultiANewArrayInsnNode

internal class MultiANewArrayTest : StringSpec({
    "[I" {
        singleInsnTest(
            insn = MultiANewArrayInsnNode("[I", 1),
            stackBefore = OperandStack.from(Operand.IntType.RuntimeInt()),
            stackAfter = OperandStack.from(
                Operand.RefType.ArrayRef(ArrayType.Primitive(Opcodes.T_INT))
            )
        )
    }

    "[[I" {
        singleInsnTest(
            insn = MultiANewArrayInsnNode("[[I", 2),
            stackBefore = OperandStack.from(
                Operand.IntType.RuntimeInt(),
                Operand.IntType.RuntimeInt()
            ),
            stackAfter = OperandStack.from(Operand.RefType.ArrayRef(ArrayType.Ref("[I")))
        )
    }
})
