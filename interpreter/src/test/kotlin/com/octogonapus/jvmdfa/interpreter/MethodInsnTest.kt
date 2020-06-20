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

import com.octogonapus.jvmdfa.testutil.merge
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

internal class MethodInsnTest : StringSpec({

    val valuesToCheck = allValues.merge(
        listOf(Operand.RefType.ArrayRef(ArrayType.Primitive(Opcodes.T_INT))).exhaustive()
    )

    val virtualSpecialAndInterface =
        listOf(Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKEINTERFACE)
            .exhaustive()

    "INVOKE* ()V" {
        checkAll(virtualSpecialAndInterface) { op ->
            singleInsnTest(
                insn = MethodInsnNode(op, "LFoo;", "bar", "()V", false),
                stackBefore = OperandStack.from(Operand.RefType.RuntimeRef("LFoo;")),
                stackAfter = OperandStack.from()
            )
        }
    }

    "INVOKE* (*)V" {
        checkAll(virtualSpecialAndInterface, valuesToCheck) { op, value1 ->
            val desc = OperandUtil.typeDescriptorForOperand(value1)
            singleInsnTest(
                insn = MethodInsnNode(op, "LFoo;", "bar", "($desc)V", false),
                stackBefore = OperandStack.from(Operand.RefType.RuntimeRef("LFoo;"), value1),
                stackAfter = OperandStack.from()
            )
        }
    }

    "INVOKE* (**)V" {
        checkAll(virtualSpecialAndInterface, valuesToCheck, valuesToCheck) { op, value1, value2 ->
            val desc1 = OperandUtil.typeDescriptorForOperand(value1)
            val desc2 = OperandUtil.typeDescriptorForOperand(value2)
            singleInsnTest(
                insn = MethodInsnNode(op, "LFoo;", "bar", "($desc1$desc2)V", false),
                stackBefore = OperandStack.from(
                    Operand.RefType.RuntimeRef("LFoo;"),
                    value1,
                    value2
                ),
                stackAfter = OperandStack.from()
            )
        }
    }

    "INVOKESTATIC ()V" {
        singleInsnTest(
            insn = MethodInsnNode(Opcodes.INVOKESTATIC, "LFoo;", "bar", "()V", false),
            stackBefore = OperandStack.from(),
            stackAfter = OperandStack.from()
        )
    }

    "INVOKESTATIC (*)V" {
        checkAll(valuesToCheck) { value1 ->
            val desc = OperandUtil.typeDescriptorForOperand(value1)
            singleInsnTest(
                insn = MethodInsnNode(Opcodes.INVOKESTATIC, "LFoo;", "bar", "($desc)V", false),
                stackBefore = OperandStack.from(value1),
                stackAfter = OperandStack.from()
            )
        }
    }

    "INVOKESTATIC (**)V" {
        checkAll(valuesToCheck, valuesToCheck) { value1, value2 ->
            val desc1 = OperandUtil.typeDescriptorForOperand(value1)
            val desc2 = OperandUtil.typeDescriptorForOperand(value2)
            singleInsnTest(
                insn = MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "LFoo;",
                    "bar",
                    "($desc1$desc2)V",
                    false
                ),
                stackBefore = OperandStack.from(value1, value2),
                stackAfter = OperandStack.from()
            )
        }
    }
})
