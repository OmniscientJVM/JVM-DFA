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
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.ints
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.tree.VarInsnNode

class VarInsns : StringSpec({
    "all loads" {
        checkAll(allVarLoads, Exhaustive.ints(0..3)) { load, index ->
            singleInsnTest(
                insn = VarInsnNode(load, index),
                stackAfter = OperandStack.from(OperandUtil.operandForVarInsn(load))
            )
        }
    }

    "all stores" {
        checkAll(allVarStores, Exhaustive.ints(0..3)) { store, index ->
            singleInsnTest(
                insn = VarInsnNode(store, index),
                stackBefore = OperandStack.from(OperandUtil.operandForVarInsn(store)),
                stackAfter = OperandStack.from()
            )
        }
    }
})

internal val allVarLoads = listOf(ILOAD, LLOAD, FLOAD, DLOAD, ALOAD).exhaustive()

internal val allVarStores = listOf(ISTORE, LSTORE, FSTORE, DSTORE, ASTORE).exhaustive()
