package io.disassemble.asm.util.query

import io.disassemble.asm.ext.findChildren
import io.disassemble.asm.ext.hash
import io.disassemble.asm.ext.nextInTree
import io.disassemble.asm.util.StringMatcher
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.MethodInsnNode

typealias InsnFilter = (insn: AbstractInsnNode) -> Boolean
typealias InsnNameMap = MutableMap<String, MutableList<AbstractInsnNode>>

class InsnQuery(var filter: InsnFilter) {

    val nearQueries: MutableList<InsnQuery> = ArrayList()
    val childQueries: MutableList<InsnQuery> = ArrayList()
    var name: String? = null
    var dist: Int = 10

    /**
     * Sets the name of this query
     */
    infix fun name(name: String): InsnQuery {
        this.name = name
        return this
    }

    /**
     * Uses the + operator to add a query to the nearQueries list
     */
    operator fun plus(query: InsnQuery): InsnQuery {
        nearQueries.add(query)
        return this
    }

    /**
     * Uses the / operator to add a query to the childQueries list
     */
    operator fun div(query: InsnQuery): InsnQuery {
        childQueries.add(query)
        return this
    }

    /**
     * Sets the maximum travel distance to finding this query
     */
    operator fun rangeTo(dist: Int): InsnQuery {
        this.dist = dist
        return this
    }

    /**
     * Maps the given instruction to the given map
     */
    private fun map(map: InsnNameMap, insn: AbstractInsnNode, query: InsnQuery) {
        val key = query.name ?: insn.hash.toString()
        map.putIfAbsent(key, ArrayList())
        map[key]!!.add(insn)
    }

    /**
     * Finds and maps the queries neighboring the given instruction
     */
    private fun findNexts(insn: AbstractInsnNode, queries: List<InsnQuery>, map: InsnNameMap) {
        val nexts: MutableList<AbstractInsnNode> = ArrayList()
        var matchCount = 0
        var last = insn
        for (query in queries) {
            val match = last.nextInTree(query.filter, query.dist) ?: break
            nexts.add(match)
            last = match
            matchCount++
        }
        if (matchCount == queries.size) {
            map(map, insn, this)
            for (i in 0 until queries.size) {
                map(map, nexts[i], queries[i])
            }
        }
    }

    /**
     * Finds and maps the children queries in the given instruction
     */
    private fun findChildren(insn: AbstractInsnNode, queries: List<InsnQuery>,
                             map: InsnNameMap = HashMap()): InsnNameMap {
        val children = insn.findChildren(queries[0].filter)
        if (children.isNotEmpty()) {
            for (root in children) {
                val childMap: InsnNameMap = HashMap()
                findNexts(root, queries.subList(1, queries.size), childMap)
                var failedNest = false
                queries.forEach { query ->
                    if (query.childQueries.isNotEmpty()) {
                        val nestedChildren = findChildren(root, query.childQueries)
                        if (nestedChildren.isEmpty()) {
                            failedNest = true
                        } else {
                            childMap.putAll(nestedChildren)
                        }
                    }
                }
                if (!failedNest && childMap.isNotEmpty()) {
                    map(childMap, root, queries[0])
                    map.putAll(childMap)
                }
            }
        }
        return map
    }

    /**
     * Maps this query to the given instruction
     */
    fun match(insn: AbstractInsnNode, map: InsnNameMap = HashMap()): InsnNameMap {
        if (filter(insn)) {
            val nearMap: InsnNameMap = HashMap()
            val childMap: InsnNameMap = HashMap()

            val nearSuccess = if (nearQueries.isNotEmpty()) {
                findNexts(insn, nearQueries, nearMap)
                nearMap.isNotEmpty()
            } else {
                true
            }

            val childSuccess = if (childQueries.isNotEmpty()) {
                findChildren(insn, childQueries, childMap)
                childMap.isNotEmpty()
            } else {
                true
            }

            if (nearSuccess && childSuccess) {
                map(map, insn, this)
                map.putAll(nearMap)
                map.putAll(childMap)
            }

        }
        return map
    }
}

object Queries {

    val any: InsnQuery = InsnQuery { true }
    val num: InsnQuery = InsnQuery { it is IntInsnNode }
    val method: InsnQuery = InsnQuery { it is MethodInsnNode }
    val field: InsnQuery = InsnQuery { it is FieldInsnNode }

    val nop: InsnQuery = op(NOP)
    val aconst_null: InsnQuery = op(ACONST_NULL)
    val iconst_m1: InsnQuery = op(ICONST_M1)
    val iconst_0: InsnQuery = op(ICONST_0)
    val iconst_1: InsnQuery = op(ICONST_1)
    val iconst_2: InsnQuery = op(ICONST_2)
    val iconst_3: InsnQuery = op(ICONST_3)
    val iconst_4: InsnQuery = op(ICONST_4)
    val iconst_5: InsnQuery = op(ICONST_5)
    val lconst_0: InsnQuery = op(LCONST_0)
    val lconst_1: InsnQuery = op(LCONST_1)
    val fconst_0: InsnQuery = op(FCONST_0)
    val fconst_1: InsnQuery = op(FCONST_1)
    val fconst_2: InsnQuery = op(FCONST_2)
    val dconst_0: InsnQuery = op(DCONST_0)
    val dconst_1: InsnQuery = op(DCONST_1)
    val bipush: InsnQuery = op(BIPUSH)
    val sipush: InsnQuery = op(SIPUSH)
    val ldc: InsnQuery = op(LDC)
    val iload: InsnQuery = op(ILOAD)
    val lload: InsnQuery = op(LLOAD)
    val fload: InsnQuery = op(FLOAD)
    val dload: InsnQuery = op(DLOAD)
    val aload: InsnQuery = op(ALOAD)
    val iaload: InsnQuery = op(IALOAD)
    val laload: InsnQuery = op(LALOAD)
    val faload: InsnQuery = op(FALOAD)
    val daload: InsnQuery = op(DALOAD)
    val aaload: InsnQuery = op(AALOAD)
    val baload: InsnQuery = op(BALOAD)
    val caload: InsnQuery = op(CALOAD)
    val saload: InsnQuery = op(SALOAD)
    val istore: InsnQuery = op(ISTORE)
    val lstore: InsnQuery = op(LSTORE)
    val fstore: InsnQuery = op(FSTORE)
    val dstore: InsnQuery = op(DSTORE)
    val astore: InsnQuery = op(ASTORE)
    val iastore: InsnQuery = op(IASTORE)
    val lastore: InsnQuery = op(LASTORE)
    val fastore: InsnQuery = op(FASTORE)
    val dastore: InsnQuery = op(DASTORE)
    val aastore: InsnQuery = op(AASTORE)
    val bastore: InsnQuery = op(BASTORE)
    val castore: InsnQuery = op(CASTORE)
    val sastore: InsnQuery = op(SASTORE)
    val pop: InsnQuery = op(POP)
    val pop2: InsnQuery = op(POP2)
    val dup: InsnQuery = op(DUP)
    val dup_x1: InsnQuery = op(DUP_X1)
    val dup_x2: InsnQuery = op(DUP_X2)
    val dup2: InsnQuery = op(DUP2)
    val dup2_x1: InsnQuery = op(DUP2_X1)
    val dup2_x2: InsnQuery = op(DUP2_X2)
    val swap: InsnQuery = op(SWAP)
    val iadd: InsnQuery = op(IADD)
    val ladd: InsnQuery = op(LADD)
    val fadd: InsnQuery = op(FADD)
    val dadd: InsnQuery = op(DADD)
    val isub: InsnQuery = op(ISUB)
    val lsub: InsnQuery = op(LSUB)
    val fsub: InsnQuery = op(FSUB)
    val dsub: InsnQuery = op(DSUB)
    val imul: InsnQuery = op(IMUL)
    val lmul: InsnQuery = op(LMUL)
    val fmul: InsnQuery = op(FMUL)
    val dmul: InsnQuery = op(DMUL)
    val idiv: InsnQuery = op(IDIV)
    val ldiv: InsnQuery = op(LDIV)
    val fdiv: InsnQuery = op(FDIV)
    val ddiv: InsnQuery = op(DDIV)
    val irem: InsnQuery = op(IREM)
    val lrem: InsnQuery = op(LREM)
    val frem: InsnQuery = op(FREM)
    val drem: InsnQuery = op(DREM)
    val ineg: InsnQuery = op(INEG)
    val lneg: InsnQuery = op(LNEG)
    val fneg: InsnQuery = op(FNEG)
    val dneg: InsnQuery = op(DNEG)
    val ishl: InsnQuery = op(ISHL)
    val lshl: InsnQuery = op(LSHL)
    val ishr: InsnQuery = op(ISHR)
    val lshr: InsnQuery = op(LSHR)
    val iushr: InsnQuery = op(IUSHR)
    val lushr: InsnQuery = op(LUSHR)
    val iand: InsnQuery = op(IAND)
    val land: InsnQuery = op(LAND)
    val ior: InsnQuery = op(IOR)
    val lor: InsnQuery = op(LOR)
    val ixor: InsnQuery = op(IXOR)
    val lxor: InsnQuery = op(LXOR)
    val iinc: InsnQuery = op(IINC)
    val i2l: InsnQuery = op(I2L)
    val i2f: InsnQuery = op(I2F)
    val i2d: InsnQuery = op(I2D)
    val l2i: InsnQuery = op(L2I)
    val l2f: InsnQuery = op(L2F)
    val l2d: InsnQuery = op(L2D)
    val f2i: InsnQuery = op(F2I)
    val f2l: InsnQuery = op(F2L)
    val f2d: InsnQuery = op(F2D)
    val d2i: InsnQuery = op(D2I)
    val d2l: InsnQuery = op(D2L)
    val d2f: InsnQuery = op(D2F)
    val i2b: InsnQuery = op(I2B)
    val i2c: InsnQuery = op(I2C)
    val i2s: InsnQuery = op(I2S)
    val lcmp: InsnQuery = op(LCMP)
    val fcmpl: InsnQuery = op(FCMPL)
    val fcmpg: InsnQuery = op(FCMPG)
    val dcmpl: InsnQuery = op(DCMPL)
    val dcmpg: InsnQuery = op(DCMPG)
    val ifeq: InsnQuery = op(IFEQ)
    val ifne: InsnQuery = op(IFNE)
    val iflt: InsnQuery = op(IFLT)
    val ifge: InsnQuery = op(IFGE)
    val ifgt: InsnQuery = op(IFGT)
    val ifle: InsnQuery = op(IFLE)
    val if_icmpeq: InsnQuery = op(IF_ICMPEQ)
    val if_icmpne: InsnQuery = op(IF_ICMPNE)
    val if_icmplt: InsnQuery = op(IF_ICMPLT)
    val if_icmpge: InsnQuery = op(IF_ICMPGE)
    val if_icmpgt: InsnQuery = op(IF_ICMPGT)
    val if_icmple: InsnQuery = op(IF_ICMPLE)
    val if_acmpeq: InsnQuery = op(IF_ACMPEQ)
    val if_acmpne: InsnQuery = op(IF_ACMPNE)
    val goto: InsnQuery = op(GOTO)
    val jsr: InsnQuery = op(JSR)
    val ret: InsnQuery = op(RET)
    val tableswitch: InsnQuery = op(TABLESWITCH)
    val lookupswitch: InsnQuery = op(LOOKUPSWITCH)
    val ireturn: InsnQuery = op(IRETURN)
    val lreturn: InsnQuery = op(LRETURN)
    val freturn: InsnQuery = op(FRETURN)
    val dreturn: InsnQuery = op(DRETURN)
    val areturn: InsnQuery = op(ARETURN)
    val returnOp: InsnQuery = op(RETURN)
    val getstatic: InsnQuery = op(GETSTATIC)
    val putstatic: InsnQuery = op(PUTSTATIC)
    val getfield: InsnQuery = op(GETFIELD)
    val putfield: InsnQuery = op(PUTFIELD)
    val invokevirtual: InsnQuery = op(INVOKEVIRTUAL)
    val invokespecial: InsnQuery = op(INVOKESPECIAL)
    val invokestatic: InsnQuery = op(INVOKESTATIC)
    val invokeinterface: InsnQuery = op(INVOKEINTERFACE)
    val invokedynamic: InsnQuery = op(INVOKEDYNAMIC)
    val newOp: InsnQuery = op(NEW)
    val newarray: InsnQuery = op(NEWARRAY)
    val anewarray: InsnQuery = op(ANEWARRAY)
    val arraylength: InsnQuery = op(ARRAYLENGTH)
    val athrow: InsnQuery = op(ATHROW)
    val checkcast: InsnQuery = op(CHECKCAST)
    val instanceof: InsnQuery = op(INSTANCEOF)
    val monitorenter: InsnQuery = op(MONITORENTER)
    val monitorexit: InsnQuery = op(MONITOREXIT)
    val multianewarray: InsnQuery = op(MULTIANEWARRAY)
    val ifnull: InsnQuery = op(IFNULL)
    val ifnonnull: InsnQuery = op(IFNONNULL)

    fun op(opcode: Int): InsnQuery = InsnQuery {
        it.opcode == opcode
    }

    fun num(operand: Int): InsnQuery = InsnQuery {
        it is IntInsnNode && it.operand == operand
    }

    fun method(desc: String?): InsnQuery = InsnQuery {
        it is MethodInsnNode && (desc == null || StringMatcher.matches(desc, it.desc))
    }

    fun field(desc: String?): InsnQuery = InsnQuery {
        it is FieldInsnNode && (desc == null || StringMatcher.matches(desc, it.desc))
    }
}