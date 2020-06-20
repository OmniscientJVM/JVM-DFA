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
import io.kotest.property.checkAll
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.IntInsnNode

class IntInsns : StringSpec({
    "bipush" {
        singleInsnTest(
            insn = IntInsnNode(Opcodes.BIPUSH, 7),
            stackAfter = OperandStack.from(Operand.ByteType.ConstByte(7))
        )
    }

    "sipush" {
        singleInsnTest(
            insn = IntInsnNode(Opcodes.SIPUSH, 12345),
            stackAfter = OperandStack.from(Operand.ShortType.ConstShort(12345))
        )
    }

    "newarray" {
        checkAll(allArrayTypes, positiveInts) { arrayType, count ->
            singleInsnTest(
                insn = IntInsnNode(Opcodes.NEWARRAY, arrayType),
                stackBefore = OperandStack.from(count),
                stackAfter = OperandStack.from(
                    Operand.RefType.ArrayRef(ArrayType.Primitive(arrayType))
                )
            )
        }
    }
})
