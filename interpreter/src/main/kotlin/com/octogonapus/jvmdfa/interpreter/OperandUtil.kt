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

import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
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
import org.objectweb.asm.Type

internal object OperandUtil {

    internal fun operandForArrayInsn(opcode: Int): Operand.RefType = when (opcode) {
        IALOAD, IASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_INT))
        LALOAD, LASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_LONG))
        FALOAD, FASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_FLOAT))
        DALOAD, DASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_DOUBLE))
        AALOAD, AASTORE -> Operand.RefType.ArrayRef(ArrayType.Ref(null))
        BALOAD, BASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_BYTE))
        CALOAD, CASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_CHAR))
        SALOAD, SASTORE -> Operand.RefType.ArrayRef(ArrayType.Primitive(T_SHORT))
        else -> throw IllegalArgumentException("Cannot get the operand for opcode $opcode")
    }

    internal fun operandForVarInsn(opcode: Int): Operand = when (opcode) {
        ILOAD, ISTORE -> Operand.IntType.RuntimeInt()
        LLOAD, LSTORE -> Operand.LongType.RuntimeLong()
        FLOAD, FSTORE -> Operand.FloatType.RuntimeFloat()
        DLOAD, DSTORE -> Operand.DoubleType.RuntimeDouble()
        ALOAD, ASTORE -> Operand.RefType.RuntimeRef(null)
        else -> throw IllegalArgumentException("Cannot get the operand for opcode $opcode")
    }

    internal fun operandForType(type: Type): Operand = when (type.sort) {
        Type.VOID -> error("Can't get the operand for void.")
        Type.BOOLEAN -> Operand.IntType.RuntimeInt()
        Type.CHAR -> Operand.IntType.RuntimeInt()
        Type.BYTE -> Operand.ByteType.RuntimeByte()
        Type.SHORT -> Operand.ShortType.RuntimeShort()
        Type.INT -> Operand.IntType.RuntimeInt()
        Type.FLOAT -> Operand.FloatType.RuntimeFloat()
        Type.LONG -> Operand.LongType.RuntimeLong()
        Type.DOUBLE -> Operand.DoubleType.RuntimeDouble()
        Type.ARRAY -> {
            if (type.dimensions == 1) {
                Operand.RefType.ArrayRef(arrayTypeForType(type))
            } else {
                val dimPrefix = (1 until type.dimensions).joinToString(separator = "") { "[" }
                Operand.RefType.ArrayRef(ArrayType.Ref("$dimPrefix${type.elementType.descriptor}"))
            }
        }
        Type.OBJECT -> Operand.RefType.RuntimeRef(type.descriptor)
        else -> error("Can't get operand for sort ${type.sort}")
    }

    internal fun typeDescriptorForOperand(operand: Operand): String = when (operand) {
        is Operand.IntType -> "I"
        is Operand.LongType -> "J"
        is Operand.FloatType -> "F"
        is Operand.DoubleType -> "D"
        is Operand.ByteType -> "B"
        is Operand.ShortType -> "S"
        is Operand.CharType -> "C"
        is Operand.RefType -> "Ljava/lang/Object;"
    }

    private fun arrayTypeForType(type: Type): ArrayType = when (type.elementType.sort) {
        Type.BOOLEAN -> ArrayType.Primitive(T_BOOLEAN)
        Type.CHAR -> ArrayType.Primitive(T_CHAR)
        Type.BYTE -> ArrayType.Primitive(T_BYTE)
        Type.SHORT -> ArrayType.Primitive(T_SHORT)
        Type.INT -> ArrayType.Primitive(T_INT)
        Type.FLOAT -> ArrayType.Primitive(T_FLOAT)
        Type.LONG -> ArrayType.Primitive(T_LONG)
        Type.DOUBLE -> ArrayType.Primitive(T_DOUBLE)
        Type.OBJECT -> ArrayType.Ref(type.elementType.descriptor)
        else -> error("Can't get array element type for type ${type.elementType}")
    }
}
