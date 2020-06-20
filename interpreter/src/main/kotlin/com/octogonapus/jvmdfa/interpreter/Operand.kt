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

// It's important that every subclass in this hierarchy has a unique reference because data flow
// analysis uses referential equality.
@Suppress("CanSealedSubClassBeObject")
sealed class Operand {

    /**
     * @return The storage category of this operand.
     */
    abstract fun category(): Category

    /**
     * @return True if this operand can be treated like an int by various opcodes.
     */
    abstract fun isInt(): Boolean

    sealed class IntType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = true

        data class ConstInt(val value: Int) : IntType()
        class RuntimeInt : IntType()
    }

    sealed class LongType : Operand() {

        override fun category() = Category.CategoryTwo
        override fun isInt() = false

        data class ConstLong(val value: Long) : LongType()
        class RuntimeLong : LongType()
    }

    sealed class FloatType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = false

        data class ConstFloat(val value: Float) : FloatType()
        class RuntimeFloat : FloatType()
    }

    sealed class DoubleType : Operand() {

        override fun category() = Category.CategoryTwo
        override fun isInt() = false

        data class ConstDouble(val value: Double) : DoubleType()
        class RuntimeDouble : DoubleType()
    }

    sealed class ByteType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = true

        data class ConstByte(val value: Int) : ByteType()
        class RuntimeByte : ByteType()
    }

    sealed class ShortType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = true

        data class ConstShort(val value: Int) : ShortType()
        class RuntimeShort : ShortType()
    }

    sealed class CharType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = true

        class RuntimeChar : CharType()
    }

    sealed class RefType : Operand() {

        override fun category() = Category.CategoryOne
        override fun isInt() = false

        data class ArrayRef(val type: ArrayType) : RefType()

        /**
         * @param desc The descriptor of the reference type, or null if there is no type
         * information.
         */
        data class RuntimeRef(val desc: String?) : RefType()
        class Null : RefType()
    }
}
