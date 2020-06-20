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
package com.octogonapus.jvmdfa.dfa

import com.octogonapus.jvmdfa.interpreter.Interpreter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

internal class DataFlowTest : FunSpec({
    test("find NEWARRAY from *ASTORE with one ALOAD") {
        /*
        int[] i = new int[1];
        i[0] = 6;

        ICONST_1       : 1 ->
        NEWARRAY T_INT : arrayref(int,1) ->
        ASTORE 1       : -> (local 1 now contains arrayref(int,1))
        ALOAD 1        : arrayref(int,1) ->
        ICONST_0       : arrayref(int, 1), 0 ->
        BIPUSH 6       : arrayref(int, 1), 0, 6 ->
        IASTORE        : -> (arrayref(int,1)[0] = 6)
         */

        val newarray = IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT)
        val aload = VarInsnNode(Opcodes.ALOAD, 1)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(InsnNode(Opcodes.ICONST_1))
            add(newarray)
            add(VarInsnNode(Opcodes.ASTORE, 1))
            add(aload)
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(newarray)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(1)
    }

    test("find ANEWARRAY from AASTORE with one ALOAD") {
        /*
        Object[] o = new Object[1];
        o[0] = new Object();

        ICONST_1                                    : 1 ->
        ANEWARRAY Ljava/lang/Object;                : arrayref(Object,1) ->
        ASTORE 1                                    : -> (local 1 now contains arrayref(Object,1))
        ALOAD 1                                     : arrayref(Object,1) ->
        ICONST_0                                    : arrayref(Object, 1), 0 ->
        NEW Ljava/lang/Object;                      : arrayref(Object, 1), 0, Object ->
        DUP                                         : arrayref(Object, 1), 0, Object, Object ->
        INVOKESPECIAL Ljava/lang/Object;.<init> ()V : arrayref(Object, 1), 0, Object ->
        AASTORE                                     : -> (arrayref(Object,1)[0] = Object)
         */

        val anewarray = TypeInsnNode(Opcodes.ANEWARRAY, "Ljava/lang/Object;")
        val aload = VarInsnNode(Opcodes.ALOAD, 1)
        val aastore = InsnNode(Opcodes.AASTORE)
        InsnList().apply {
            add(InsnNode(Opcodes.ICONST_1))
            add(anewarray)
            add(VarInsnNode(Opcodes.ASTORE, 1))
            add(aload)
            add(InsnNode(Opcodes.ICONST_0))
            add(TypeInsnNode(Opcodes.NEW, "Ljava/lang/Object;"))
            add(InsnNode(Opcodes.DUP))
            add(MethodInsnNode(Opcodes.INVOKESPECIAL, "Ljava/lang/Object;", "<init>", "()V", false))
            add(aastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(aastore).shouldBe(anewarray)
        dataFlow.findLocalVariableHoldingArrayRef(aastore).shouldBe(1)
    }

    test("find NEWARRAY from *ASTORE with two ALOADs") {
        /*
        (loosely based on, another ASTORE & ALOAD was added for obfuscation)
        int[] i = new int[1];
        i[0] = 6;

        ICONST_1       : 1 ->
        NEWARRAY T_INT : arrayref(int,1) ->
        ASTORE 1       : -> (local 1 now contains arrayref(int,1))
        ALOAD 1        : arrayref(int,1)
        ASTORE 1       : ->
        ALOAD 1        : arrayref(int,1) ->
        ICONST_0       : arrayref(int, 1), 0 ->
        BIPUSH 6       : arrayref(int, 1), 0, 6 ->
        IASTORE        : -> (arrayref(int,1)[0] = 6)
         */

        val newarray = IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT)
        val aload = VarInsnNode(Opcodes.ALOAD, 1)
        val iastore = InsnNode(Opcodes.IASTORE)
        val aload2 = VarInsnNode(Opcodes.ALOAD, 1)
        InsnList().apply {
            add(InsnNode(Opcodes.ICONST_1))
            add(newarray)
            add(VarInsnNode(Opcodes.ASTORE, 1))
            add(aload)
            add(VarInsnNode(Opcodes.ASTORE, 1))
            add(aload2)
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(newarray)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(1)
    }

    test("Find NEWARRAY from *ASTORE with zero ALOADs") {
        /*
        int[] i = {6};

        ICONST_1       : 1 ->
        NEWARRAY T_INT : arrayref(int,1) ->
        DUP            : arrayref(int,1), arrayref(int,1) ->
        ICONST_0       : arrayref(int,1), arrayref(int,1), 0 ->
        BIPUSH 6       : arrayref(int,1), arrayref(int,1), 0, 6 ->
        IASTORE        : arrayref(int,1) ->
        ASTORE 1       : ->
         */

        val newarray = IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(InsnNode(Opcodes.ICONST_1))
            add(newarray)
            add(InsnNode(Opcodes.DUP))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
            add(VarInsnNode(Opcodes.ASTORE, 1))
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(newarray)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(1)
    }

    test("Find correct NEWARRAY instance from *ASTORE with zero ALOADs") {
        /*
        ICONST_1       : 1 ->
        NEWARRAY T_INT : arrayref(int,1) ->
        DUP            : arrayref(int,1), arrayref(int,1) ->
        ICONST_1       : arrayref(int,1), arrayref(int,1), 1 ->
        NEWARRAY T_INT : arrayref(int,1), arrayref(int,1), arrayref(int,1) ->
        ICONST_0       : arrayref(int,1), arrayref(int,1), arrayref(int,1), 0 ->
        BIPUSH 6       : arrayref(int,1), arrayref(int,1), arrayref(int,1), 0, 6 ->
        IASTORE        : arrayref(int,1), arrayref(int,1) ->
        ICONST_0       : arrayref(int,1), arrayref(int,1), 0 ->
        BIPUSH 6       : arrayref(int,1), arrayref(int,1), 0, 6 ->
        IASTORE        : arrayref(int,1) ->
        ASTORE 1       : ->
         */

        val newarray = IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(InsnNode(Opcodes.ICONST_1))
            add(newarray)
            add(InsnNode(Opcodes.DUP))
            add(InsnNode(Opcodes.ICONST_1))
            add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(InsnNode(Opcodes.IASTORE))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
            add(VarInsnNode(Opcodes.ASTORE, 1))
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(newarray)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(1)
    }

    test("Find ALOAD from array *ASTORE without a NEWARRAY") {
        /*
        thing(new int[1]);
        static void thing(int[] i) {
            i[0] = 6;
        }

        ALOAD 0  : arrayref(int,1) ->
        ICONST_0 : arrayref(int,1), 0 ->
        BIPUSH 6 : arrayref(int,1), 0, 6 ->
        IASTORE  : ->
         */

        val aload = VarInsnNode(Opcodes.ALOAD, 0)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(aload)
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(aload)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(0)
    }

    test("Find ALOAD from array *ASTORE with a useless NEWARRAY and misleading ASTORE") {
        /*
        thing(new int[1]);
        static void thing(int[] i) {
            i[0] = 6;
        }

        ALOAD 0        : arrayref(int,1) ->
        ICONST_1       : arrayref(int,1), 1 ->
        NEWARRAY T_INT : arrayref(int,1), arrayref(int,1) ->
        ASTORE 0       : arrayref(int,1) ->
        ICONST_0       : arrayref(int,1), 0 ->
        BIPUSH 6       : arrayref(int,1), 0, 6 ->
        IASTORE        : ->
         */

        val aload = VarInsnNode(Opcodes.ALOAD, 0)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(aload)
            add(InsnNode(Opcodes.ICONST_1))
            add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT))
            add(VarInsnNode(Opcodes.ASTORE, 0))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(aload)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(0)
    }

    test("Find ALOAD from array *ASTORE with a useless NEWARRAY and a SWAP") {
        /*
        ALOAD 0        : arrayref(int,1)<l0> -> (from a method parameter)
        ICONST_1       : arrayref(int,1)<l0>, 1 ->
        NEWARRAY T_INT : arrayref(int,1)<l0>, arrayref(int,1)<na> ->
        SWAP           : arrayref(int,1)<na>, arrayref(int,1)<l0> ->
        ICONST_0       : arrayref(int,1)<na>, arrayref(int,1)<l0>, 0 ->
        BIPUSH 6       : arrayref(int,1)<na>, arrayref(int,1)<l0>, 0, 6 ->
        IASTORE        : arrayref(int,1)<na> ->
         */

        val aload = VarInsnNode(Opcodes.ALOAD, 0)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(aload)
            add(InsnNode(Opcodes.ICONST_1))
            add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT))
            add(InsnNode(Opcodes.SWAP))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(aload)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(0)
    }

    test("Find variable with a useless ASTORE after the IASTORE") {
        /*
        ALOAD 0        : arrayref(int,1)<l0> ->
        DUP            : arrayref(int,1)<l0>, arrayref(int,1)<l0> ->
        SWAP           : arrayref(int,1)<l0>, arrayref(int,1)<l0> ->
        ICONST_0       : arrayref(int,1)<l0>, arrayref(int,1)<l0>, 0 ->
        BIPUSH 6       : arrayref(int,1)<l0>, arrayref(int,1)<l0>, 0, 6 ->
        IASTORE        : arrayref(int,1)<l0> ->
        ASTORE 1       : ->
         */

        val aload = VarInsnNode(Opcodes.ALOAD, 0)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(aload)
            add(InsnNode(Opcodes.DUP))
            add(InsnNode(Opcodes.SWAP))
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
            add(VarInsnNode(Opcodes.ASTORE, 1))
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(aload)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(0)
    }

    test("Store into leaked array with misleading ASTORE after the IASTORE") {
        /*
        ALOAD 0        : arrayref(int,1)<l0> ->
        ICONST_1       : arrayref(int,1)<l0>, 1 ->
        NEWARRAY T_INT : arrayref(int,1)<l0>, arrayref(int,1)<na> ->
        ICONST_0       : arrayref(int,1)<l0>, arrayref(int,1)<na>, 0 ->
        BIPUSH 6       : arrayref(int,1)<l0>, arrayref(int,1)<na>, 0, 6 ->
        IASTORE        : arrayref(int,1)<l0> ->
        ASTORE 1       : ->
         */

        val aload = VarInsnNode(Opcodes.ALOAD, 0)
        val newarray = IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT)
        val iastore = InsnNode(Opcodes.IASTORE)
        InsnList().apply {
            add(aload)
            add(InsnNode(Opcodes.ICONST_1))
            add(newarray)
            add(InsnNode(Opcodes.ICONST_0))
            add(IntInsnNode(Opcodes.BIPUSH, 6))
            add(iastore)
            add(VarInsnNode(Opcodes.ASTORE, 1))
        }

        val dataFlow = DataFlow(Interpreter())
        dataFlow.insnIntroducingArray(iastore).shouldBe(newarray)
        dataFlow.findLocalVariableHoldingArrayRef(iastore).shouldBe(null)
    }
})
