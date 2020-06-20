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
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.lang.RuntimeException
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode

class PopInsns : StringSpec({
    "pop a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from()
            )
        }
    }

    "pop a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnShouldThrowTest<IllegalStateException>(
                insn = InsnNode(Opcodes.POP),
                stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 two category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value, value),
                stackAfter = OperandStack.from()
            )
        }
    }

    "pop2 a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from()
            )
        }
    }

    "stack before pop2 a category 2 value" {
        checkAll(allCategory2Values, allCategory1Values) { value1, value2 ->
            val insn = InsnNode(Opcodes.POP2)
            val stackBefore = OperandStack.from(value2, value1)
            val stackAfter = OperandStack.from(value2)

            val insnList = InsnList().apply {
                // Add a NOP so we can set the stack for it to stackBefore
                add(InsnNode(Opcodes.NOP))
                add(insn)
            }

            val interpreter = Interpreter()

            // This will set the stack after the NOP to stackBefore
            interpreter.setStackAfter(insnList.first, stackBefore)

            interpreter.stackAfter(insn).shouldBe(stackAfter)
            interpreter.stackBefore(insn).shouldBe(stackBefore)
        }
    }
})
