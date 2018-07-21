package io.disassemble.asm.ext

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */

/**
 * Exports a Map<String, ClassNode> to the given jar file
 *
 * @param jar The jar file to export to
 */
fun Map<String, ClassNode>.export(jar: String) {
    val out = JarOutputStream(FileOutputStream(jar))

    values.forEach {
        val entryKey = it.name.replace("\\.", "/") + ".class"
        out.putNextEntry(JarEntry(entryKey))
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        it.accept(writer)
        out.write(writer.toByteArray())
        out.closeEntry()
    }

    out.close()
}

/**
 * Runs the given remapper through the MutableMap<String, ClassNode>
 *
 * @param remapper The remapper to use
 */
fun MutableMap<String, ClassNode>.remap(remapper: SimpleRemapper) {
    this.values.forEach {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        it.accept(ClassRemapper(writer, remapper))
        val rewritten = ClassNode()
        val reader = ClassReader(writer.toByteArray())
        reader.accept(rewritten, ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG)
        this[it.name] = rewritten
    }
}

/**
 * Finds the MethodNode within the Map<String, ClassNode>
 *
 * @param owner The name of the method's owner
 * @param method The name of the method
 * @param desc The descriptor of the method
 */
fun Map<String, ClassNode>.findMethod(owner: String, method: String, desc: String): MethodNode {
    val cn = this[owner] ?: error("Class '$owner' not found")
    return cn.methodList.find { it.name == method && it.desc == desc } ?: error("Method '$method$desc' not found")
}

/**
 * Finds every MethodNode within the Map<String, ClassNode> matching the given arguments
 *
 * @param owner The name of the method's owner
 * @param method The name of the method
 * @param desc The descriptor of the method
 */
fun Map<String, ClassNode>.findMethodTree(owner: String, method: String, desc: String): Set<MethodNode> {
    val calls: MutableSet<MethodNode> = HashSet()
    var ownerClass = this[owner]
    while (ownerClass != null) {
        calls.addAll(ownerClass.methodList.filter {
            it.name == method && it.desc == desc
        })
        ownerClass = this[ownerClass.superName]
    }
    return calls
}

/**
 * Finds every field within the Map<String, ClassNode> matching the given arguments
 *
 * @param owner The name of the field's owner
 * @param field The name of the field
 * @param desc The descriptor of the field
 */
fun Map<String, ClassNode>.findFieldTree(owner: String, field: String, desc: String): Set<String> {
    val fields: MutableSet<String> = HashSet()
    var ownerClass = this[owner]
    while (ownerClass != null) {
        ownerClass.fieldList.find {
            it.name == field && it.desc == desc
        }?.let {
            fields.add("${ownerClass!!.name}.${it.name}")
        }
        ownerClass = this[ownerClass.superName]
    }
    return fields
}

/**
 * Executes the MethodVisitor throughout the entirety of the Map<String, ClassNode>
 *
 * @param visitor The MethodVisitor to execute
 */
fun Map<String, ClassNode>.visitMethods(visitor: MethodVisitor) {
    this.values.forEach {
        it.methodList.forEach {
            it.accept(visitor)
        }
    }
}