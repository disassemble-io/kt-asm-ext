package io.disassemble.asm.ext

import io.disassemble.asm.util.InstructionListing
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.Printer
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.contains
import kotlin.collections.emptyList
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.set
import kotlin.collections.toMutableList

/**
 * @author Tyler Sedlar
 * @since 6/24/2018
 */

private val CHILD_MAP: MutableMap<Int, MutableList<AbstractInsnNode>> = HashMap()
private val PARENT_MAP: MutableMap<Int, AbstractInsnNode> = HashMap()
private val TREE_NEXT_NEIGHBOR_MAP: MutableMap<Int, AbstractInsnNode> = HashMap()
private val TREE_PREVIOUS_NEIGHBOR_MAP: MutableMap<Int, AbstractInsnNode> = HashMap()

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
val AbstractInsnNode.children: MutableList<AbstractInsnNode>
    get() {
        return if (hash in CHILD_MAP) {
            CHILD_MAP[hash]!!
        } else {
            val children = ArrayList<AbstractInsnNode>()
            CHILD_MAP[hash] = children
            children
        }
    }

/**
 * Gets this instruction's parent, if constructed through a tree.
 */
val AbstractInsnNode.parent: AbstractInsnNode?
    get() {
        return if (hash in PARENT_MAP) {
            PARENT_MAP[hash]
        } else {
            null
        }
    }

/**
 * Gets this instruction's next neighbor in its tree
 */
val AbstractInsnNode.nextInTree: AbstractInsnNode?
    get() {
        return if (hash in TREE_NEXT_NEIGHBOR_MAP) {
            TREE_NEXT_NEIGHBOR_MAP[hash]
        } else {
            null
        }
    }

/**
 * Gets this instruction's previous neighbor in its tree
 */
val AbstractInsnNode.prevInTree: AbstractInsnNode?
    get() {
        return if (hash in TREE_PREVIOUS_NEIGHBOR_MAP) {
            TREE_PREVIOUS_NEIGHBOR_MAP[hash]
        } else {
            null
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
 * Sets this instruction's parent instruction
 */
fun AbstractInsnNode.setParent(parent: AbstractInsnNode) {
    PARENT_MAP[hash] = parent
}

/**
 * Sets this instruction's neighbor data
 */
fun AbstractInsnNode.setNeighborsInTree(next: AbstractInsnNode) {
    TREE_NEXT_NEIGHBOR_MAP[hash] = next
    TREE_PREVIOUS_NEIGHBOR_MAP[next.hash] = this
}

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

/**
 * Gets the next instruction matching the given query within the max distance
 */
fun AbstractInsnNode.nextInTree(query: (AbstractInsnNode) -> Boolean, maxDist: Int = 50): AbstractInsnNode? {
    var next: AbstractInsnNode? = this.nextInTree
    var dist = 1
    while (next != null &&  dist < maxDist) {
        if (query(next)) {
            return next
        }
        next = next.nextInTree
        dist++
    }
    return null
}

/**
 * Prints out this instructions child tree
 */
fun AbstractInsnNode.printTree(indent: String = "") {
    println(indent + this.verbose)
    this.children.forEach { it.printTree("$indent  ") }
}

/**
 * Gets the children of this instruction's tree matching the given opcode
 */
fun AbstractInsnNode.findChildren(query: (AbstractInsnNode) -> Boolean, idx: Int = 0): MutableList<AbstractInsnNode> {
    if (hash in CHILD_MAP) {
        val treeChildren = CHILD_MAP[hash]!!
        val children: MutableList<AbstractInsnNode> = ArrayList()
        for (i in idx until treeChildren.size) {
            val child = treeChildren[i]
            if (query(child)) {
                children.add(child)
            }
        }
        return if (!children.isEmpty()) children else emptyList<AbstractInsnNode>().toMutableList()
    }
    return emptyList<AbstractInsnNode>().toMutableList()
}