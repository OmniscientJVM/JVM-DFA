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

// Comment to help with making column selection mode edits
/*
Int
Long
Float
Double
Byte
Short
Ref
 */

sealed class OperandStackOperation {
    object NOP : OperandStackOperation()
    data class PushConstInt(val value: Int) : OperandStackOperation()
    data class PushConstLong(val value: Long) : OperandStackOperation()
    data class PushConstFloat(val value: Float) : OperandStackOperation()
    data class PushConstDouble(val value: Double) : OperandStackOperation()
    data class PushConstByte(val value: Int) : OperandStackOperation()
    data class PushConstShort(val value: Int) : OperandStackOperation()
    data class NewArray(val type: Int) : OperandStackOperation()
    object PushNullRef : OperandStackOperation()
    object LoadIntFromArray : OperandStackOperation()
    object StoreIntoIntArray : OperandStackOperation()
    object LoadLongFromArray : OperandStackOperation()
    object StoreIntoLongArray : OperandStackOperation()
    object LoadFloatFromArray : OperandStackOperation()
    object StoreIntoFloatArray : OperandStackOperation()
    object LoadDoubleFromArray : OperandStackOperation()
    object StoreIntoDoubleArray : OperandStackOperation()
    object LoadRefFromArray : OperandStackOperation()
    object StoreIntoRefArray : OperandStackOperation()
    object LoadByteFromArray : OperandStackOperation()
    object StoreIntoByteArray : OperandStackOperation()
    object LoadCharFromArray : OperandStackOperation()
    object StoreIntoCharArray : OperandStackOperation()
    object LoadShortFromArray : OperandStackOperation()
    object StoreIntoShortArray : OperandStackOperation()
    object Pop : OperandStackOperation()
    object Pop2 : OperandStackOperation()
    object Dup : OperandStackOperation()
    object DupX1 : OperandStackOperation()
    object DupX2 : OperandStackOperation()
    object Dup2 : OperandStackOperation()
    object Dup2X1 : OperandStackOperation()
    object Dup2X2 : OperandStackOperation()
    object Swap : OperandStackOperation()
    data class LoadIntFromLocal(val index: Int) : OperandStackOperation()
    data class StoreIntIntoLocal(val index: Int) : OperandStackOperation()
    data class LoadLongFromLocal(val index: Int) : OperandStackOperation()
    data class StoreLongIntoLocal(val index: Int) : OperandStackOperation()
    data class LoadFloatFromLocal(val index: Int) : OperandStackOperation()
    data class StoreFloatIntoLocal(val index: Int) : OperandStackOperation()
    data class LoadDoubleFromLocal(val index: Int) : OperandStackOperation()
    data class StoreDoubleIntoLocal(val index: Int) : OperandStackOperation()
    data class LoadRefFromLocal(val index: Int) : OperandStackOperation()
    data class StoreRefIntoLocal(val index: Int) : OperandStackOperation()
    data class New(val desc: String) : OperandStackOperation()
    data class ANewArray(val desc: String) : OperandStackOperation()
    data class CheckCast(val desc: String) : OperandStackOperation()
    data class InstanceOf(val desc: String) : OperandStackOperation()
    sealed class Invoke : OperandStackOperation() {
        abstract val owner: String
        abstract val name: String
        abstract val desc: String
        abstract val isInterface: Boolean

        data class Virtual(
            override val owner: String,
            override val name: String,
            override val desc: String,
            override val isInterface: Boolean
        ) : Invoke()
        data class Special(
            override val owner: String,
            override val name: String,
            override val desc: String,
            override val isInterface: Boolean
        ) : Invoke()
        data class Static(
            override val owner: String,
            override val name: String,
            override val desc: String,
            override val isInterface: Boolean
        ) : Invoke()
        data class Interface(
            override val owner: String,
            override val name: String,
            override val desc: String,
            override val isInterface: Boolean
        ) : Invoke()
    }
    data class MultiANewArray(val desc: String, val dims: Int) : OperandStackOperation()
}
