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
import io.kotest.property.exhaustive.exhaustive
import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.T_BOOLEAN
import org.objectweb.asm.Opcodes.T_BYTE
import org.objectweb.asm.Opcodes.T_CHAR
import org.objectweb.asm.Opcodes.T_DOUBLE
import org.objectweb.asm.Opcodes.T_FLOAT
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.Opcodes.T_LONG
import org.objectweb.asm.Opcodes.T_SHORT
import org.objectweb.asm.tree.InsnNode

class ArrayStoresAndLoads : StringSpec({
    "check store insns" {
        checkAll(arrayStoreInsns, nonNegInts) { storeInsn, index ->
            checkAll(valuesForArrayInsn(storeInsn)) { value ->
                singleInsnTest(
                    insn = InsnNode(storeInsn),
                    stackBefore =
                        OperandStack.from(OperandUtil.operandForArrayInsn(storeInsn), index, value),
                    stackAfter = OperandStack.from()
                )
            }
        }
    }

    "check load insns" {
        checkAll(arrayLoadInsns, nonNegInts) { loadInsn, index ->
            singleInsnTest(
                insn = InsnNode(loadInsn),
                stackBefore = OperandStack.from(OperandUtil.operandForArrayInsn(loadInsn), index),
                stackAfter = OperandStack.from(runtimeValueForArrayInsn(loadInsn))
            )
        }
    }
})

/**
 * All *ALOAD insns.
 */
internal val arrayLoadInsns =
    listOf(IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD).exhaustive()

/**
 * All *ASTORE insns.
 */
internal val arrayStoreInsns =
    listOf(IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE).exhaustive()

/**
 * All array type operands for the NEWARRAY insn.
 */
internal val allArrayTypes =
    listOf(T_BOOLEAN, T_CHAR, T_FLOAT, T_DOUBLE, T_BYTE, T_SHORT, T_INT, T_LONG).exhaustive()
