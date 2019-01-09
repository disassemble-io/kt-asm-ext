import io.disassemble.asm.ext.children
import io.disassemble.asm.ext.instructionTree
import io.disassemble.asm.ext.methodList
import io.disassemble.asm.ext.verbose
import io.disassemble.asm.util.ClassScanner
import org.objectweb.asm.tree.AbstractInsnNode
import java.io.File

fun main(args: Array<String>) {
    val jar = "/media/tyler/2TBDRIVE/RuneScape/packs/oldschool/177.jar"
    val classes = ClassScanner.scanJar(File(jar))

    classes["client"]?.methodList?.find { it.name == "init" }?.let { method ->
        val tree = method.instructionTree
        tree.forEach { printTree(it) }
    }
}

fun printTree(insn: AbstractInsnNode, indent: String = "") {
    println(indent + insn.verbose)
    insn.children.forEach { printTree(it, "$indent  ") }
}