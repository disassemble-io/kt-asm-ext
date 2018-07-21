package io.disassemble.asm.util

import io.disassemble.asm.ext.methodList
import io.disassemble.asm.ext.patch
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.jar.JarFile

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
object ClassScanner {

    /**
     * Scans the classpath for class files
     *
     * @param predicate The filter to use to filter out classes
     * @param consumer The process to execute on each filtered class
     */
    fun scanClassPath(predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        val list = System.getProperty("java.class.path")
        for (path in list.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val file = File(path)
            if (file.isDirectory) {
                scanDirectory(file, predicate, consumer)
            } else if (path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer)
            }
        }
    }

    /**
     * Scans the given directory for class files
     *
     * @param directory The directory to search
     * @param predicate The filter to use to filter out classes
     * @param consumer The process to execute on each filtered class
     */
    fun scanDirectory(directory: File, predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        for (entry in directory.list()!!) {
            val path = "${directory.path}${File.separator}$entry"
            val file = File(path)
            if (file.isDirectory) {
                scanDirectory(file, predicate, consumer)
            } else if (file.isFile && path.endsWith(".class")) {
                scanClassFile(path, predicate, consumer)
            }
        }
    }

    /**
     * Creates a ClassNode to filter upon from the given file path
     *
     * @param path The path to the class file
     * @param predicate The filter to use to filter out classes
     * @param consumer The process to execute on each filtered class
     */
    fun scanClassFile(path: String, predicate: Predicate<ClassNode>, consumer: Consumer<ClassNode>) {
        try {
            FileInputStream(path).use { input -> scanInputStream(input, predicate, consumer) }
        } catch (e: IOException) {
            println("File was not found: $path")
        }
    }

    /**
     * Creates a ClassNode to filter upon from the given InputStream
     *
     * @param inputStream The class InputStream to use
     * @param predicate The filter to use to filter out classes
     * @param consumer The process to execute on each filtered class
     */
    fun scanInputStream(inputStream: InputStream, predicate: Predicate<ClassNode>,
                        consumer: Consumer<ClassNode>) {
        try {
            val node = ClassNode()
            val reader = ClassReader(inputStream)
            reader.accept(node, ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG)
            if (!predicate.test(node)) {
                return
            }
            node.methodList.forEach { it.patch(node) }
            consumer.accept(node)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Creates a class map from the given jar file
     *
     * @param file The jar file to create a class map from
     */
    fun scanJar(file: File): Map<String, ClassNode> {
        val classes = HashMap<String, ClassNode>()
        val predicate = Predicate<ClassNode> { true }
        val consumer = Consumer<ClassNode> { classes[it.name] = it }
        try {
            JarFile(file).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".class")) {
                        scanInputStream(jar.getInputStream(entry), predicate, consumer)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return classes
    }
}