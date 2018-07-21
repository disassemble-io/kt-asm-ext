package io.disassemble.asm.ext

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */

/**
 * Gets the interface list as a List<String>
 */
val ClassNode.interfaceList: List<String>
    @Suppress("UNCHECKED_CAST")
    get() = this.interfaces as List<String>

/**
 * Gets the field list as a List<FieldNode>
 */
val ClassNode.fieldList: List<FieldNode>
    @Suppress("UNCHECKED_CAST")
    get() = this.fields as List<FieldNode>

/**
 * Gets the method list as a List<MethodNode>
 */
val ClassNode.methodList: List<MethodNode>
    @Suppress("UNCHECKED_CAST")
    get() = this.methods as List<MethodNode>