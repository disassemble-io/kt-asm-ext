package io.disassemble.asm.ext

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
private val METHOD_OWNERS = HashMap<Int, ClassNode>()

/**
 * Creates a unique hash for this MethodNode
 */
val MethodNode.hash: Int
    get() = System.identityHashCode(this)

/**
 * The amount of parameters that this method has
 */
val MethodNode.paramSize: Int
    get() = Type.getArgumentTypes(this.desc).size

/**
 * The parent class of this method
 */
val MethodNode.owner: ClassNode
    get() = METHOD_OWNERS[this.hash] ?: error("method has not been patched")

/**
 * The key of this method in the format of <owner>.<name><desc>
 */
val MethodNode.key: String
    get() = "${owner.name}.${this.name}${this.desc}"

/**
 * The tryCatchBlockList as a MutableList<TryCatchBlockNode>
 */
val MethodNode.tryCatchBlockList: MutableList<TryCatchBlockNode>
    @Suppress("UNCHECKED_CAST")
    get() = this.tryCatchBlocks as MutableList<TryCatchBlockNode>

/**
 * The instruction tree for this method
 */
val MethodNode.instructionTree: List<AbstractInsnNode>
    get() {
        val nodes: MutableList<AbstractInsnNode> = ArrayList()
        val insns = this.instructions.toArray().reversedArray()
        val idx = AtomicInteger(0)
        while (idx.get() < insns.size) {
            val insn = insns[idx.get()]
            visitTree(insns, insn, idx, AtomicInteger(0))
            nodes.add(insn)
        }
        return nodes.reversed()
    }

/**
 * Checks whether this method is local (non-static) or not
 */
fun MethodNode.isLocal() = (this.access and Opcodes.ACC_STATIC) == 0

/**
 * Applies a monkey-patch during the ClassScanner reading process
 */
fun MethodNode.patch(owner: ClassNode) {
    METHOD_OWNERS[this.hash] = owner
}

/**
 * Visits every MethodNode that matches this method
 *
 * @param classes The class map to traverse
 */
fun MethodNode.visitCalls(classes: Map<String, ClassNode>): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    this.accept(object : MethodVisitor(Opcodes.ASM6) {
        override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
            if (owner in classes) {
                calls.addAll(classes.findMethodTree(owner, name, desc))
            }
        }
    })
    return calls
}

/**
 * Visits every method in this method's class' superclasses that match this method
 *
 * @param classes The class map to traverse
 */
fun MethodNode.visitSuperCalls(classes: Map<String, ClassNode>): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    calls.addAll(classes.findMethodTree(owner.name, name, desc))
    calls.remove(this)
    return calls
}

/**
 * Traverses the given instruction's tree based on its stack size
 *
 * @param insns The list of instructions in this method
 * @param parent The parent instruction to use
 * @param idx The current index being visited
 * @param ctr The child counter
 */
private fun visitTree(insns: Array<AbstractInsnNode>, parent: AbstractInsnNode, idx: AtomicInteger, ctr: AtomicInteger) {
    val sizes = parent.stackSizes

    ctr.addAndGet(sizes.second) // the pushCount
    idx.incrementAndGet()

    val expected = sizes.first // the pullCount
    val childCtr = AtomicInteger(0)

    while (childCtr.get() < expected) {
        val next = insns[idx.get()]
        parent.children.add(next)
        visitTree(insns, next, idx, childCtr)
    }

    parent.children.reverse()
}