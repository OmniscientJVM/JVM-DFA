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
import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DCONST_0
import org.objectweb.asm.Opcodes.DCONST_1
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP2_X2
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.DUP_X2
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FCONST_0
import org.objectweb.asm.Opcodes.FCONST_2
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ICONST_5
import org.objectweb.asm.Opcodes.ICONST_M1
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LCONST_0
import org.objectweb.asm.Opcodes.LCONST_1
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEWARRAY
import org.objectweb.asm.Opcodes.NOP
import org.objectweb.asm.Opcodes.POP
import org.objectweb.asm.Opcodes.POP2
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.SIPUSH
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * This class is NOT thread-safe.
 */
class Interpreter {

    private val stackMap: MutableMap<AbstractInsnNode, OperandStack> = HashMap()

    /**
     * Determine the state of the stack immediately before the [insn] executes.
     */
    fun stackBefore(insn: AbstractInsnNode) = stackAfter(insn.previous)

    /**
     * Determine the state of the stack immediately after the [insn] executes.
     */
    fun stackAfter(insn: AbstractInsnNode): OperandStack {
        return stackMap.getOrPut(insn) {
            computeStack(insn)
        }
    }

    /**
     * Changes every occurrence of the [old] operand to the [new] operand.
     *
     * @param old The old operand to get rid of.
     * @param new The new operand to replace the [old] with.
     */
    fun updateOperand(old: Operand, new: Operand) {
        val newMap = stackMap.mapValues { (_, stack) ->
            stack.updateOperand(old, new)
        }

        stackMap.clear()
        stackMap.putAll(newMap)
    }

    /**
     * Sets the stack for the first insn. FOR USE IN TESTS ONLY!
     */
    internal fun setStackAfter(insn: AbstractInsnNode, stack: OperandStack) {
        stackMap[insn] = stack
    }

    private fun computeStack(insn: AbstractInsnNode): OperandStack {
        val prevInsn = insn.previous
        val prevStack = if (prevInsn == null) {
            // insn is the first insn in the list
            OperandStack.from()
        } else stackAfter(prevInsn)

        return interpret(insn, prevStack)
    }

    private fun interpret(insn: AbstractInsnNode, stack: OperandStack): OperandStack {
        val operation = stackOperationFor(insn)
        return stack.applyOperation(operation)
    }

    @Suppress("ThrowsCount", "ComplexMethod")
    private fun stackOperationFor(insn: AbstractInsnNode): OperandStackOperation {
        return when (insn) {
            is FieldInsnNode -> TODO()

            is FrameNode -> TODO()

            is IincInsnNode -> TODO()

            is InsnNode -> when (insn.opcode) {
                /*
                Remaining:
                IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
                FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR,
                LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I,
                D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
                DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, or MONITOREXIT
                 */
                NOP -> OperandStackOperation.NOP
                ACONST_NULL -> OperandStackOperation.PushNullRef
                in ICONST_M1..ICONST_5 ->
                    OperandStackOperation.PushConstInt((insn.opcode - ICONST_M1) - 1)
                in LCONST_0..LCONST_1 ->
                    OperandStackOperation.PushConstLong(insn.opcode - LCONST_0.toLong())
                in FCONST_0..FCONST_2 ->
                    OperandStackOperation.PushConstFloat(insn.opcode - FCONST_0.toFloat())
                in DCONST_0..DCONST_1 ->
                    OperandStackOperation.PushConstDouble(insn.opcode - DCONST_0.toDouble())
                IALOAD -> OperandStackOperation.LoadIntFromArray
                IASTORE -> OperandStackOperation.StoreIntoIntArray
                LALOAD -> OperandStackOperation.LoadLongFromArray
                LASTORE -> OperandStackOperation.StoreIntoLongArray
                FALOAD -> OperandStackOperation.LoadFloatFromArray
                FASTORE -> OperandStackOperation.StoreIntoFloatArray
                DALOAD -> OperandStackOperation.LoadDoubleFromArray
                DASTORE -> OperandStackOperation.StoreIntoDoubleArray
                AALOAD -> OperandStackOperation.LoadRefFromArray
                AASTORE -> OperandStackOperation.StoreIntoRefArray
                BALOAD -> OperandStackOperation.LoadByteFromArray
                BASTORE -> OperandStackOperation.StoreIntoByteArray
                CALOAD -> OperandStackOperation.LoadCharFromArray
                CASTORE -> OperandStackOperation.StoreIntoCharArray
                SALOAD -> OperandStackOperation.LoadShortFromArray
                SASTORE -> OperandStackOperation.StoreIntoShortArray
                POP -> OperandStackOperation.Pop
                POP2 -> OperandStackOperation.Pop2
                DUP -> OperandStackOperation.Dup
                DUP_X1 -> OperandStackOperation.DupX1
                DUP_X2 -> OperandStackOperation.DupX2
                DUP2 -> OperandStackOperation.Dup2
                DUP2_X1 -> OperandStackOperation.Dup2X1
                DUP2_X2 -> OperandStackOperation.Dup2X2
                Opcodes.SWAP -> OperandStackOperation.Swap
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is IntInsnNode -> when (insn.opcode) {
                BIPUSH -> OperandStackOperation.PushConstByte(insn.operand)
                SIPUSH -> OperandStackOperation.PushConstShort(insn.operand)
                NEWARRAY -> OperandStackOperation.NewArray(insn.operand)
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is InvokeDynamicInsnNode -> TODO()

            is JumpInsnNode -> TODO()

            is LabelNode -> TODO()

            is LdcInsnNode -> TODO()

            is LineNumberNode -> OperandStackOperation.NOP

            is LookupSwitchInsnNode -> TODO()

            is MethodInsnNode -> when (insn.opcode) {
                Opcodes.INVOKEVIRTUAL -> OperandStackOperation.Invoke.Virtual(
                    insn.owner,
                    insn.name,
                    insn.desc,
                    insn.itf
                )
                Opcodes.INVOKESPECIAL -> OperandStackOperation.Invoke.Special(
                    insn.owner,
                    insn.name,
                    insn.desc,
                    insn.itf
                )
                Opcodes.INVOKESTATIC -> OperandStackOperation.Invoke.Static(
                    insn.owner,
                    insn.name,
                    insn.desc,
                    insn.itf
                )
                Opcodes.INVOKEINTERFACE -> OperandStackOperation.Invoke.Interface(
                    insn.owner,
                    insn.name,
                    insn.desc,
                    insn.itf
                )
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is MultiANewArrayInsnNode -> OperandStackOperation.MultiANewArray(insn.desc, insn.dims)

            is TableSwitchInsnNode -> TODO()

            is TypeInsnNode -> when (insn.opcode) {
                Opcodes.NEW -> OperandStackOperation.New(insn.desc)
                Opcodes.ANEWARRAY -> OperandStackOperation.ANewArray(insn.desc)
                Opcodes.CHECKCAST -> OperandStackOperation.CheckCast(insn.desc)
                Opcodes.INSTANCEOF -> OperandStackOperation.InstanceOf(insn.desc)
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is VarInsnNode -> when (insn.opcode) {
                ILOAD -> OperandStackOperation.LoadIntFromLocal(insn.`var`)
                LLOAD -> OperandStackOperation.LoadLongFromLocal(insn.`var`)
                FLOAD -> OperandStackOperation.LoadFloatFromLocal(insn.`var`)
                DLOAD -> OperandStackOperation.LoadDoubleFromLocal(insn.`var`)
                ALOAD -> OperandStackOperation.LoadRefFromLocal(insn.`var`)
                ISTORE -> OperandStackOperation.StoreIntIntoLocal(insn.`var`)
                LSTORE -> OperandStackOperation.StoreLongIntoLocal(insn.`var`)
                FSTORE -> OperandStackOperation.StoreFloatIntoLocal(insn.`var`)
                DSTORE -> OperandStackOperation.StoreDoubleIntoLocal(insn.`var`)
                ASTORE -> OperandStackOperation.StoreRefIntoLocal(insn.`var`)
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            else -> throw UnsupportedOperationException("Unknown insn: $insn")
        }
    }
}
