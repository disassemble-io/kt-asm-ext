import io.disassemble.asm.ext.*
import io.disassemble.asm.util.ClassScanner
import io.disassemble.asm.util.query.InsnNameMap
import io.disassemble.asm.util.query.Queries.method
import io.disassemble.asm.util.query.Queries.num
import io.disassemble.asm.util.query.Queries.sipush
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val jar = "/media/tyler/2TBDRIVE/RuneScape/packs/oldschool/177.jar"
    val classes = ClassScanner.scanJar(File(jar))

    val trees: MutableMap<MethodNode, List<AbstractInsnNode>> = HashMap()

    var matches: InsnNameMap? = null

    println("Built trees in ${measureTimeMillis {
        classes.values.forEach { cn ->
            cn.methodList.forEach { trees[it] = it.instructionTree }
        }
    }}ms")

    measureTimeMillis {
        classes["client"]?.methodList?.find { it.name == "init" }?.let {
            matches = trees[it]?.query(
                    method("^>(III") {
                        num(765) + num(503) + (sipush % "revision")
                    }
            )
        }
    }.let {
        matches?.forEach { entry ->
            println(entry.key + ":")
            entry.value.forEach {
                println("  " + it.verbose)
            }
        }
        println("Executed in ${it}ms")
    }
}

fun printTree(insn: AbstractInsnNode, indent: String = "") {
    println(indent + insn.verbose)
    insn.children.forEach { printTree(it, "$indent  ") }
}