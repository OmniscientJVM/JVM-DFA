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
import java.lang.RuntimeException
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode

class DupInsns : StringSpec({
    "dup a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from(value, value)
            )
        }
    }

    "dup a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP),
                stackBefore = OperandStack.from(value)
            )
        }
    }

    "dup_x1 a category 1 value" {
        checkAll(allCategory1Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP_X1),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup_x1 a category 1 value over a category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP_X1),
                stackBefore = OperandStack.from(value2, value1)
            )
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP_X1),
                stackBefore = OperandStack.from(value1, value2)
            )
        }
    }

    "dup_x2 with three category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values) {
            value1, value2, value3 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP_X2),
                stackBefore = OperandStack.from(value3, value2, value1),
                stackAfter = OperandStack.from(value1, value3, value2, value1)
            )
        }
    }

    "dup_x2 with a category 1 value and a category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP_X2),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup_x2 with a category 2 value and a category 1 value" {
        checkAll(allCategory2Values, allCategory1Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP_X2),
                stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value2, value1, value2, value1)
            )
        }
    }

    "dup2 with category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from(value, value)
            )
        }
    }

    "dup2 with category 1 value and category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP2),
                stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2_x1 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values) {
            value1, value2, value3 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X1),
                stackBefore = OperandStack.from(value3, value2, value1),
                stackAfter = OperandStack.from(value2, value1, value3, value2, value1)
            )
        }
    }

    "dup2_x1 with category 2 value over category 1 value" {
        checkAll(allCategory2Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X1),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup2_x1 with category 1 value over category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.DUP2_X1),
                stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2_x2 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values, allCategory1Values) {
            value1, value2, value3, value4 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X2),
                stackBefore = OperandStack.from(value4, value3, value2, value1),
                stackAfter = OperandStack.from(value2, value1, value4, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 2 value over category 1 values" {
        checkAll(allCategory2Values, allCategory1Values, allCategory1Values) {
            value1, value2, value3 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X2),
                stackBefore = OperandStack.from(value3, value2, value1),
                stackAfter = OperandStack.from(value1, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 1 values over category 2 value" {
        checkAll(allCategory1Values, allCategory1Values, allCategory2Values) {
            value1, value2, value3 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X2),
                stackBefore = OperandStack.from(value3, value2, value1),
                stackAfter = OperandStack.from(value2, value1, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 2 value over category 2 value" {
        checkAll(allCategory2Values, allCategory2Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.DUP2_X2),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }
})
