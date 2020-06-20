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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.merge
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
import org.objectweb.asm.Opcodes.NOP
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode

/**
 * All positive int constants + runtime int.
 */
internal val positiveInts = listOf(
    Operand.IntType.ConstInt(1),
    Operand.IntType.ConstInt(2),
    Operand.IntType.ConstInt(3),
    Operand.IntType.ConstInt(4),
    Operand.IntType.ConstInt(5),
    Operand.IntType.RuntimeInt()
).exhaustive()

/**
 * All non-negative int constants + runtime int.
 */
internal val nonNegInts = listOf(
    Operand.IntType.ConstInt(0)
).exhaustive<Operand.IntType>().merge(positiveInts)

/**
 * All int constants + runtime int.
 */
internal val allInts = listOf(
    Operand.IntType.ConstInt(-1)
).exhaustive<Operand.IntType>().merge(nonNegInts)

/**
 * All long constants + runtime long.
 */
internal val allLongs = listOf(
    Operand.LongType.ConstLong(0),
    Operand.LongType.ConstLong(1),
    Operand.LongType.RuntimeLong()
).exhaustive()

/**
 * All float constants + runtime float.
 */
internal val allFloats = listOf(
    Operand.FloatType.ConstFloat(0.0f),
    Operand.FloatType.ConstFloat(1.0f),
    Operand.FloatType.RuntimeFloat()
).exhaustive()

/**
 * All long constants + runtime long.
 */
internal val allDoubles = listOf(
    Operand.DoubleType.ConstDouble(0.0),
    Operand.DoubleType.ConstDouble(1.0),
    Operand.DoubleType.RuntimeDouble()
).exhaustive()

/**
 * All ref constants + runtime ref.
 */
internal val allRefs = listOf(
    Operand.RefType.ArrayRef(ArrayType.Ref(null)),
    Operand.RefType.ArrayRef(ArrayType.Ref("Ljava/lang/Object;")),
    Operand.RefType.ArrayRef(ArrayType.Primitive(T_INT)),
    Operand.RefType.Null(),
    Operand.RefType.RuntimeRef(null),
    Operand.RefType.RuntimeRef("Ljava/lang/Object;")
).exhaustive()

/**
 * All byte constants + runtime byte.
 */
internal val allBytes = listOf(
    Operand.ByteType.ConstByte(0),
    Operand.ByteType.ConstByte(1),
    Operand.ByteType.RuntimeByte()
).exhaustive()

/**
 * All char constants + runtime char.
 */
internal val allChars = listOf(
    Operand.CharType.RuntimeChar()
).exhaustive<Operand.CharType>()

/**
 * All short constants + runtime short.
 */
internal val allShorts = listOf(
    Operand.ShortType.ConstShort(0),
    Operand.ShortType.ConstShort(1),
    Operand.ShortType.RuntimeShort()
).exhaustive()

/**
 * All category 1 values (their constants + runtime values).
 */
internal val allCategory1Values = allInts.merge(allFloats).merge(allRefs).merge(allBytes)
    .merge(allChars).merge(allShorts)

/**
 * All category 2 values (their constants + runtime values)
 */
internal val allCategory2Values = allLongs.merge(allDoubles)

/**
 * All category 1 and 2 values.
 */
internal val allValues = allCategory1Values.merge(allCategory2Values)

/**
 * @return The values you could store in an array of the type corresponding to the [opcode].
 */
internal fun valuesForArrayInsn(opcode: Int) = when (opcode) {
    IALOAD, IASTORE -> allInts
    LALOAD, LASTORE -> allLongs
    FALOAD, FASTORE -> allFloats
    DALOAD, DASTORE -> allDoubles
    AALOAD, AASTORE -> allRefs
    BALOAD, BASTORE -> allBytes
    CALOAD, CASTORE -> allChars
    SALOAD, SASTORE -> allShorts
    else -> error("")
}

/**
 * @return The Runtime* values you could get from an array of the type corresponding to the
 * [opcode].
 */
internal fun runtimeValueForArrayInsn(opcode: Int) = when (opcode) {
    IALOAD -> Operand.IntType.RuntimeInt()
    LALOAD -> Operand.LongType.RuntimeLong()
    FALOAD -> Operand.FloatType.RuntimeFloat()
    DALOAD -> Operand.DoubleType.RuntimeDouble()
    AALOAD -> Operand.RefType.RuntimeRef(null)
    BALOAD -> Operand.ByteType.RuntimeByte()
    CALOAD -> Operand.CharType.RuntimeChar()
    SALOAD -> Operand.ShortType.RuntimeShort()
    else -> error("")
}

internal fun OperandStack.operandTypesShouldEqual(other: OperandStack) {
    val classes = stack.map { it::class }
    val otherClasses = other.stack.map { it::class }

    classes.zip(otherClasses).forEachIndexed { index, (left, right) ->
        left.shouldBe(right)
        if (left == Operand.RefType.ArrayRef::class) {
            val type = (stack[index] as Operand.RefType.ArrayRef).type
            val otherType = (other.stack[index] as Operand.RefType.ArrayRef).type
            type.shouldBe(otherType)
        }
    }
}

/**
 * Tests the mutations a single instruction does to the stack.
 *
 * @param insn The instruction to execute.
 * @param stackAfter The expected state of the stack after the instruction runs.
 * @param stackBefore The given initial state of the stack before the instruction runs. Pass null
 * for an empty stack.
 */
internal fun singleInsnTest(
    insn: AbstractInsnNode,
    stackAfter: OperandStack,
    stackBefore: OperandStack? = null
) {
    val insnList = if (stackBefore == null) {
        InsnList().apply {
            add(insn)
        }
    } else {
        InsnList().apply {
            add(InsnNode(NOP)) // Add a NOP so we can set the stack for it to stackBefore
            add(insn)
        }
    }

    val interpreter = Interpreter()

    // This will set the stack after the NOP to stackBefore
    if (stackBefore != null) interpreter.setStackAfter(insnList.first, stackBefore)

    interpreter.stackAfter(insn).operandTypesShouldEqual(stackAfter)
}

/**
 * Tests the mutations a single instruction does to the stack.
 *
 * @param insn The instruction to execute.
 * @param stackBefore The given initial state of the stack before the instruction runs. Pass null
 * for an empty stack.
 */
internal inline fun <reified T : Throwable> singleInsnShouldThrowTest(
    insn: AbstractInsnNode,
    stackBefore: OperandStack? = null
) {
    val insnList = if (stackBefore == null) {
        InsnList().apply {
            add(insn)
        }
    } else {
        InsnList().apply {
            add(InsnNode(NOP)) // Add a NOP so we can set the stack for it to stackBefore
            add(insn)
        }
    }

    val interpreter = Interpreter()

    // This will set the stack after the NOP to stackBefore
    if (stackBefore != null) interpreter.setStackAfter(insnList.first, stackBefore)

    shouldThrow<T> { interpreter.stackAfter(insn) }
}
