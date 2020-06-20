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
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.ints
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode

class Consts : StringSpec({
    "iconst" {
        checkAll(Exhaustive.ints(0..5)) { offset ->
            singleInsnTest(
                insn = InsnNode(Opcodes.ICONST_M1 + offset),
                stackAfter = OperandStack.from(Operand.IntType.ConstInt(-1 + offset))
            )
        }
    }

    "lconst" {
        checkAll(Exhaustive.ints(0..1)) { offset ->
            singleInsnTest(
                insn = InsnNode(Opcodes.LCONST_0 + offset),
                stackAfter = OperandStack.from(Operand.LongType.ConstLong(offset.toLong()))
            )
        }
    }

    "fconst" {
        checkAll(Exhaustive.ints(0..2)) { offset ->
            singleInsnTest(
                insn = InsnNode(Opcodes.FCONST_0 + offset),
                stackAfter = OperandStack.from(Operand.FloatType.ConstFloat(offset.toFloat()))
            )
        }
    }

    "dconst" {
        checkAll(Exhaustive.ints(0..1)) { offset ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DCONST_0 + offset),
                stackAfter = OperandStack.from(Operand.DoubleType.ConstDouble(offset.toDouble()))
            )
        }
    }
})
