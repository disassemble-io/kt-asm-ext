import io.disassemble.asm.ext.*
import io.disassemble.asm.util.ClassScanner
import io.disassemble.asm.util.query.Queries.method
import io.disassemble.asm.util.query.Queries.num
import io.disassemble.asm.util.query.Queries.sipush
import org.objectweb.asm.tree.AbstractInsnNode
import java.io.File

fun main(args: Array<String>) {
    val jar = "/media/tyler/2TBDRIVE/RuneScape/packs/oldschool/177.jar"
    val classes = ClassScanner.scanJar(File(jar))

    classes["client"]?.methodList?.find { it.name == "init" }?.instructionTree?.query(
            method("^>(III")
                    / num(765)
                    / num(503)
                    / (sipush name "revision")
    )?.forEach { entry ->
        println(entry.key + ":")
        entry.value.forEach {
            println("  " + it.verbose)
        }
    }
}

fun printTree(insn: AbstractInsnNode, indent: String = "") {
    println(indent + insn.verbose)
    insn.children.forEach { printTree(it, "$indent  ") }
}