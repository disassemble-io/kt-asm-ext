package io.disassemble.asm.ext

import io.disassemble.asm.util.InstructionListing
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.Printer

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */

private val CHILD_MAP: MutableMap<Int, MutableSet<AbstractInsnNode>> = HashMap()

/**
 * Provides a unique hash for this instruction
 */
val AbstractInsnNode.hash: Int
    get() {
        return System.identityHashCode(this)
    }

/**
 * The children involved with the instruction tree of this instruction
 */
val AbstractInsnNode.children: MutableSet<AbstractInsnNode>
    get() {
        return if (hash in CHILD_MAP) {
            CHILD_MAP[hash]!!
        } else {
            val children = HashSet<AbstractInsnNode>()
            CHILD_MAP[hash] = children
            children
        }
    }

/**
 * The string label for this instruction's opcode
 */
val AbstractInsnNode.opname: String
    get() = try {
        Printer.OPCODES[this.opcode]
    } catch (e: Exception) {
        this.javaClass.simpleName
    }

/**
 * A string displaying the contents of this instruction
 */
val AbstractInsnNode.verbose: String
    get() {
        var verbose = opname
        when {
            this is IntInsnNode -> verbose += " ${this.operand}"
            this is MethodInsnNode -> verbose += " ${this.owner}.${this.name}${this.desc}"
            this is VarInsnNode -> verbose += " ${this.`var`}"
        }
        return verbose
    }

/**
 * A pair of this instruction's stack sizes: the first being the pull size, the second being the push size.
 */
val AbstractInsnNode.stackSizes: Pair<Int, Int>
    get() {
        val on = opname.toLowerCase()
        if (on in InstructionListing.MAP) {
            val listing = InstructionListing.MAP[on]
            var sizes = listing!!.stackSizes()
            if (sizes.first == -1) {
                sizes = when {
                    this is MethodInsnNode -> {
                        var size = Type.getArgumentTypes(this.desc).size
                        if (this.opcode != INVOKESTATIC) {
                            size++
                        }
                        return Pair(size, sizes.second)
                    }
                    this is MultiANewArrayInsnNode -> Pair(this.dims, sizes.second)
                    else -> error("This should not be anything other than an invoke* or multianewarray instruction")
                }
            }
            return sizes
        } else {
            return Pair(0, 0) // likely a LabelNode or similar injected instruction
        }
    }

/**
 * The pull size of this instruction - how many instructions are previously required to use this instruction
 */
val AbstractInsnNode.pullSize: Int
    get() = stackSizes.first

/**
 * The push size of this instruction - how many instructions are put on the stack after this instruction
 */
val AbstractInsnNode.pushSize: Int
    get() = stackSizes.second

/**
 * Checks if this instruction has n instructions following it
 *
 * @param amount The amount of instructions following this instruction
 */
fun AbstractInsnNode.hasNext(amount: Int): Boolean {
    var insn: AbstractInsnNode? = this
    for (i in 0..amount) {
        insn = insn!!.next
        if (insn == null) {
            return false
        }
    }
    return true
}

/**
 * Retrieves the pattern after this instruction if it exists
 *
 * @param opcodes The opcode pattern to look for
 */
fun AbstractInsnNode.nextPattern(vararg opcodes: Int): List<AbstractInsnNode>? {
    if (hasNext(opcodes.size)) {
        val insns: MutableList<AbstractInsnNode> = ArrayList()
        var insn = this
        for (i in 0..opcodes.size) {
            insn = insn.next
            if (insn.opcode != opcodes[i]) {
                return null
            }
            insns.add(insn)
        }
        return insns
    }
    return null
}

/**
 * Gets the next valid instruction if it exists
 */
fun AbstractInsnNode.nextValid(): AbstractInsnNode? {
    var next: AbstractInsnNode? = this.next
    while (next != null && next is LabelNode) {
        next = next.next
    }
    return next
}

/**
 * Gets the next valid pattern if it exists
 *
 * @param opcodes The opcode pattern to look for
 */
fun AbstractInsnNode.nextValidPattern(vararg opcodes: Int): List<AbstractInsnNode>? {
    val results: MutableList<AbstractInsnNode> = ArrayList()
    var current = this
    for (i in opcodes.indices) {
        current = current.nextValid() ?: return null
        results.add(current)
    }
    return results
}