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
import com.octogonapus.jvmdfa.interpreter.Operand
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * This class is NOT thread-safe.
 */
class DataFlow(private val interpreter: Interpreter) {

    /**
     * Finds the insn that introduced an array used by an *ASTORE or *ALOAD insn.
     *
     * @param insn The insn that stores into or loads from the array ref to search for.
     * @return The insn that introduced the array ref.
     */
    fun insnIntroducingArray(insn: AbstractInsnNode): AbstractInsnNode? =
        insnIntroducingArrayAndRef(insn).first

    private fun insnIntroducingArrayAndRef(
        insn: AbstractInsnNode
    ): Pair<AbstractInsnNode?, Operand.RefType> {
        val arrayRef = findArrayRef(insn)
        val introducingInsn = findEarliestInsnIntroducingOperand(insn, arrayRef)
        return introducingInsn to arrayRef
    }

    private fun isAStoreInsn(astoreInsn: AbstractInsnNode) =
        astoreInsn is InsnNode && astoreInsn.opcode in listOf(
            Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE,
            Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE
        )

    private fun isALoadInsn(aloadInsn: AbstractInsnNode) =
        aloadInsn is InsnNode && aloadInsn.opcode in listOf(
            Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD,
            Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD
        )

    private fun findArrayRef(insn: AbstractInsnNode): Operand.RefType {
        require(isAStoreInsn(insn) || isALoadInsn(insn))

        val stackAtInsn = interpreter.stackBefore(insn)

        val startingRef = when {
            isAStoreInsn(insn) -> stackAtInsn.peek(2) as Operand.RefType
            isALoadInsn(insn) -> stackAtInsn.peek(1) as Operand.RefType
            else -> error("")
        }

        return startingRef.let {
            when (it) {
                // If we already got an ArrayRef we are done
                is Operand.RefType.ArrayRef -> it

                is Operand.RefType.RuntimeRef -> {
                    // We know the ref `it` is really an array ref, so find its real value. If it
                    // couldn't find the real value, return the ref we started with (i.e., the best
                    // we can do).
                    val arrayRef = getArrayRefFromRefType(insn, it) ?: return startingRef

                    // Tell the interpreter that the `it` (probably a RuntimeRef) is actually
                    // `arrayRef` so that we can search for it later.
                    interpreter.updateOperand(it, arrayRef)

                    arrayRef
                }

                else -> error("Expected an arrayref, got: $it")
            }
        }
    }

    /**
     * Finds the index of the local variable holding the array ref that was stored into or loaded
     * from. If the array ref was not stored into a local before the [insn] runs but will be stored
     * into a local after the [insn] runs, then the index of that local will be returned. Array refs
     * that are part of >=2D arrays typically don't have local variables associated with them. In
     * this case, the local variable storing the outermost array dimension will be returned.
     *
     * @param insn The *ASTORE or *ALOAD insn in question.
     * @return The index of the local variable.
     */
    fun findLocalVariableHoldingArrayRef(insn: AbstractInsnNode): Int? {
        // TODO: Test this
        val (introducingInsn, ref) = insnIntroducingArrayAndRef(insn)
        return findLocalVarFromIntroducingInsn(introducingInsn, ref)
    }

    private fun findLocalVarFromIntroducingInsn(
        introducingInsn: AbstractInsnNode?,
        ref: Operand.RefType
    ): Int? = when {
        introducingInsn == null -> null

        // If it was loaded from a parameter
        introducingInsn is VarInsnNode && introducingInsn.opcode == Opcodes.ALOAD ->
            introducingInsn.`var`

        // If it was created
        introducingInsn.opcode == Opcodes.NEWARRAY ||
            introducingInsn.opcode == Opcodes.ANEWARRAY ||
            introducingInsn.opcode == Opcodes.MULTIANEWARRAY -> {
            val removingInsn = findLatestInsnRemovingOperand(introducingInsn, ref)
            when {
                removingInsn == null -> null

                removingInsn is VarInsnNode && removingInsn.opcode == Opcodes.ASTORE ->
                    removingInsn.`var`

                isAStoreInsn(removingInsn) || removingInsn.opcode == Opcodes.AALOAD -> {
                    // The array ref was introduced by a NEWARRAY and removed by an *ASTORE or
                    // an AALOAD (in the case of a >=2D array).
                    // Either it was leaked or it was stored into and loaded from a local
                    // variable between the introducing and removing insns. Look for an ALOAD
                    // that introduced the array ref between those two.
                    findALOADBetweenInsns(introducingInsn, removingInsn, ref)?.`var`
                }

                else -> error("Unknown removing insn: $removingInsn")
            }
        }

        introducingInsn.opcode == Opcodes.AALOAD -> {
            // An AALOAD obscures the local because inner array dimensions don't typically have
            // local variables. The user expects to see the local for the outermost array dimension
            // instead. So find the array ref the AALOAD loaded from and recurse with it.
            val arrayRef = findArrayRef(introducingInsn)
            val prevArrayInsn = findEarliestInsnIntroducingOperand(introducingInsn, arrayRef)
            findLocalVarFromIntroducingInsn(prevArrayInsn, arrayRef)
        }

        else -> error("Unknown introducing insn: $introducingInsn")
    }

    private fun findALOADBetweenInsns(
        introducingInsn: AbstractInsnNode,
        removingInsn: AbstractInsnNode?,
        ref: Operand.RefType
    ): VarInsnNode? = if (introducingInsn === removingInsn || removingInsn == null) {
        // Either the search range was reduced to 0 or the lower bound fell off the edge.
        null
    } else if (removingInsn is VarInsnNode &&
        removingInsn.opcode == Opcodes.ALOAD &&
        (
            interpreter.stackAfter(removingInsn) -
                interpreter.stackBefore(removingInsn)
            ).contains(ref)
    ) {
        // If we raised the lower bound to an ALOAD insn that put the ref on the stack, we found it.
        removingInsn
    } else {
        findALOADBetweenInsns(introducingInsn, removingInsn.previous, ref)
    }

    /**
     * Finds the [Operand.RefType.ArrayRef] from the [ref] by looking at what was stored into it. If
     * the [ref] originally comes from a method parameter, then this method can't recover any more
     * information, so it will return [ref].
     *
     * @param insn The insn to start searching from.
     * @param ref The operand to search for.
     * @return The original value of the [ref], or null if none was found.
     */
    private fun getArrayRefFromRefType(
        insn: AbstractInsnNode,
        ref: Operand.RefType.RuntimeRef
    ): Operand.RefType? {
        val introducingInsn = findEarliestInsnIntroducingOperand(insn, ref) ?: return null

        return when (introducingInsn.opcode) {
            Opcodes.ALOAD -> {
                check(introducingInsn is VarInsnNode)
                // Find the store that would have stored into the local this ALOAD will load from.
                // If none is found, then assume the ALOAD is loading from a method parameter.
                val storeInsn = findMostRecentStoreIntoIndex(
                    introducingInsn,
                    introducingInsn.`var`
                ) ?: return ref

                when (val storedRef = interpreter.stackBefore(storeInsn).peek()) {
                    // If we found the ArrayRef we are done
                    is Operand.RefType.ArrayRef -> storedRef

                    // If we found another RuntimeRef then keep looking
                    is Operand.RefType.RuntimeRef -> getArrayRefFromRefType(storeInsn, storedRef)

                    else -> error("Unexpected operand type: $storedRef")
                }
            }

            Opcodes.AALOAD -> {
                check(introducingInsn is InsnNode)
                null
            }

            else -> error("Unknown introducing insn: $introducingInsn")
        }
    }

    /**
     * Finds the first possible *STORE insn that stores into the [index].
     *
     * @param insn The insn to start searching from.
     * @param index The index to search for.
     * @return The first matching *STORE insn, or null if none was found.
     */
    private fun findMostRecentStoreIntoIndex(insn: AbstractInsnNode?, index: Int): VarInsnNode? =
        if (insn == null) {
            null
        } else if (insn is VarInsnNode && insn.`var` == index && insn.opcode in listOf(
            Opcodes.ISTORE,
            Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE
        )
        ) {
            insn
        } else {
            findMostRecentStoreIntoIndex(insn.previous, index)
        }

    /**
     * Finds the earliest possible (highest in the bytecode) insn that introduced the operand.
     *
     * @param insn The insn to start searching from.
     * @param operand The operand to search for.
     * @return The earliest matching insn, or null if none was found.
     */
    private fun findEarliestInsnIntroducingOperand(
        insn: AbstractInsnNode?,
        operand: Operand
    ): AbstractInsnNode? = when {
        insn == null -> null

        // Even if we find an introducing insn, keep looking for an earlier one
        didInsnIntroduceOperand(insn, operand) ->
            findEarliestInsnIntroducingOperand(insn.previous, operand) ?: insn

        else -> findEarliestInsnIntroducingOperand(insn.previous, operand)
    }

    /**
     * Finds the latest possible (lowest in the bytecode) insn that introduced the operand.
     *
     * @param insn The insn to start searching from.
     * @param operand The operand to search for.
     * @return The latest matching insn, or null if none was found.
     */
    private fun findLatestInsnRemovingOperand(
        insn: AbstractInsnNode?,
        operand: Operand
    ): AbstractInsnNode? = when {
        insn == null -> null

        // Even if we find a removing insn, keep looking for an earlier one
        didInsnRemoveOperand(insn, operand) ->
            findLatestInsnRemovingOperand(insn.next, operand) ?: insn

        else -> findLatestInsnRemovingOperand(insn.next, operand)
    }

    /**
     * Computes whether an insn introduced an operand by pushing it on the stack.
     *
     * @param insn The insn to consider.
     * @param operand The operand to check for.
     * @return True if the insn introduced the operand.
     */
    private fun didInsnIntroduceOperand(
        insn: AbstractInsnNode?,
        operand: Operand
    ) = insn?.let {
        // Check if this is the first insn. The stack before is empty so just check the stack after.
        if (insn.previous == null) return operand in interpreter.stackAfter(insn)

        operand !in interpreter.stackBefore(insn) && operand in interpreter.stackAfter(insn)
    } ?: false

    /**
     * Computes whether an insn removed an operand by popping it off the stack.
     *
     * @param insn The insn to consider.
     * @param operand The operand to check for.
     * @return True if the insn removed the operand.
     */
    private fun didInsnRemoveOperand(
        insn: AbstractInsnNode?,
        operand: Operand
    ) = insn?.let {
        // Check if this is the first insn. Nothing could have been removed so return false.
        if (insn.previous == null) return false

        operand in interpreter.stackBefore(insn) && operand !in interpreter.stackAfter(insn)
    } ?: false
}
