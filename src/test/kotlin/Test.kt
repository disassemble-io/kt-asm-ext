import io.disassemble.asm.ext.*
import io.disassemble.asm.util.ClassScanner
import org.objectweb.asm.tree.AbstractInsnNode
import java.io.File
import java.util.function.Consumer
import java.util.function.Predicate

fun main(args: Array<String>) {
    ClassScanner.scanDirectory(File("out/test/classes"), Predicate { true }, Consumer {
        if (it.name == "BytecodeTestKt") {
            it.methodList.forEach { method ->
                println(method.key)
//                method.instructions.toArray().forEach {
//                    println("  ${it.verbose} | ${it.stackSizes}")
//                }
                method.instructionTree.forEach {
                    visitRecursive(it) { d, i ->
                        println(" ".repeat(d) + i.verbose + " | " + i.stackSizes)
                    }
                }
                println()
            }
        }
    })
}

fun visitRecursive(insn: AbstractInsnNode, depth: Int, consumer: (depth: Int, insn: AbstractInsnNode) -> Unit) {
    consumer(depth, insn)
    for (child in insn.children) {
        visitRecursive(child, depth + 1, consumer)
    }
}

fun visitRecursive(insn: AbstractInsnNode, consumer: (depth: Int, insn: AbstractInsnNode) -> Unit) {
    visitRecursive(insn, 0, consumer)
}