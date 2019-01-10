package io.disassemble.asm.util.query

import io.disassemble.asm.ext.findChildren
import io.disassemble.asm.ext.hash
import io.disassemble.asm.ext.nextInTree
import io.disassemble.asm.util.StringMatcher
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

typealias InsnFilter = (insn: AbstractInsnNode) -> Boolean
typealias ChildQuery = () -> InsnQuery
typealias InsnNameMap = MutableMap<String, MutableList<AbstractInsnNode>>

class InsnQuery(var filter: InsnFilter, child: ChildQuery? = null) {

    val nearQueries: MutableList<InsnQuery> = ArrayList()
    val childQueries: MutableList<InsnQuery> = ArrayList()
    var name: String? = null
    var dist: Int = 10

    init {
        child?.let { childQueries.add(it()) }
    }

    /**
     * Uses the % operator to set the name of this query
     */
    operator fun rem(name: String) = name(name)

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
    operator fun plus(query: InsnQuery): InsnQuery = near(query)

    /**
     * Adds a query to the nearQueries list
     */
    infix fun near(query: InsnQuery): InsnQuery {
        nearQueries.add(query)
        return this
    }

    /**
     * Uses the / operator to add a query to the childQueries list
     */
    operator fun div(query: InsnQuery): InsnQuery = child(query)

    /**
     * Adds a query to the childQueries list
     */
    infix fun child(query: InsnQuery): InsnQuery {
        childQueries.add(query)
        return this
    }

    /**
     * Sets the maximum travel distance to finding this query
     */
    operator fun rangeTo(dist: Int): InsnQuery = dist(dist)

    /**
     * Sets the maximum travel distance to finding this query
     */
    infix fun dist(dist: Int): InsnQuery {
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
    private fun findNexts(insn: AbstractInsnNode, queries: List<InsnQuery>,
                          map: InsnNameMap = HashMap()): InsnNameMap {
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
        return map
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
                    if (query.nearQueries.isNotEmpty()) {
                        val nestedNears = findNexts(root, query.nearQueries)
                        if (nestedNears.isEmpty()) {
                            failedNest = true
                        } else {
                            childMap.putAll(nestedNears)
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

    val any: InsnQuery = InsnQuery({ true })
    val num: InsnQuery = InsnQuery({ it is IntInsnNode })
    val method: InsnQuery = InsnQuery({ it is MethodInsnNode })
    val field: InsnQuery = InsnQuery({ it is FieldInsnNode })
    val lvar: InsnQuery = InsnQuery({ it is VarInsnNode })
    val jump: InsnQuery = InsnQuery({ it is JumpInsnNode })
    var frame: InsnQuery = InsnQuery({ it is FrameNode })
    var label: InsnQuery = InsnQuery({ it is LabelNode })
    var constant: InsnQuery = InsnQuery({ it is LdcInsnNode })
    var line: InsnQuery = InsnQuery({ it is LineNumberNode })
    var tswitch: InsnQuery = InsnQuery({ it is TableSwitchInsnNode })
    var lswitch: InsnQuery = InsnQuery({ it is LookupSwitchInsnNode })
    var mana: InsnQuery = InsnQuery({ it is MultiANewArrayInsnNode })
    var type: InsnQuery = InsnQuery({ it is TypeInsnNode })
    var dynamic: InsnQuery = InsnQuery({ it is InvokeDynamicInsnNode })
    var inc: InsnQuery = InsnQuery({ it is IincInsnNode })

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

    fun op(opcode: Int, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it.opcode == opcode
    }, cq)

    fun filter(filter: InsnFilter, cq: ChildQuery? = null) = InsnQuery(filter, cq)

    fun use(query: InsnQuery, cq: ChildQuery?): InsnQuery {
        val q = InsnQuery(query.filter)
        q.name = query.name
        q.dist = query.dist
        cq?.let { query.childQueries.add(it()) }
        return q
    }

    fun num(operand: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is IntInsnNode && (operand == null || it.operand == operand)
    }, cq)

    fun num(cq: ChildQuery? = null): InsnQuery = num(null, cq)

    fun method(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is MethodInsnNode && (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun method(desc: String?, cq: ChildQuery? = null): InsnQuery = method(null, desc, cq)

    fun method(cq: ChildQuery? = null): InsnQuery = method(null, cq)

    fun membmethod(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (it.opcode == INVOKEVIRTUAL || it.opcode == INVOKESPECIAL) &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun membmethod(desc: String?, cq: ChildQuery? = null): InsnQuery = membmethod(null, desc, cq)

    fun membmethod(cq: ChildQuery? = null): InsnQuery = membmethod(null, cq)

    fun statmethod(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && it.opcode == INVOKESTATIC &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun statmethod(desc: String?, cq: ChildQuery? = null): InsnQuery = statmethod(null, desc, cq)

    fun statmethod(cq: ChildQuery? = null): InsnQuery = statmethod(null, cq)

    fun field(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun field(desc: String?, cq: ChildQuery? = null): InsnQuery = field(null, desc, cq)

    fun field(cq: ChildQuery? = null): InsnQuery = field(null, cq)

    fun membfield(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (it.opcode == PUTFIELD || it.opcode == GETFIELD) &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun membfield(desc: String?, cq: ChildQuery? = null): InsnQuery = membfield(null, desc, cq)

    fun membfield(cq: ChildQuery? = null): InsnQuery = membfield(null, cq)

    fun statfield(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (it.opcode == PUTSTATIC || it.opcode == GETSTATIC) &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun statfield(desc: String?, cq: ChildQuery? = null): InsnQuery = statfield(null, desc, cq)

    fun statfield(cq: ChildQuery? = null): InsnQuery = statfield(null, cq)

    fun getter(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (it.opcode == GETSTATIC || it.opcode == GETFIELD) &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun getter(desc: String?, cq: ChildQuery? = null): InsnQuery = getter(null, desc, cq)

    fun getter(cq: ChildQuery? = null): InsnQuery = getter(null, cq)

    fun putter(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FieldInsnNode && (it.opcode == PUTSTATIC || it.opcode == PUTFIELD) &&
                (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun putter(desc: String?, cq: ChildQuery? = null): InsnQuery = putter(null, desc, cq)

    fun putter(cq: ChildQuery? = null): InsnQuery = putter(null, cq)

    fun lvar(`var`: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is VarInsnNode && (`var` == null || it.`var` == `var`)
    }, cq)

    fun lvar(cq: ChildQuery? = null): InsnQuery = InsnQuery({ it is VarInsnNode }, cq)

    fun jump(opcode: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is JumpInsnNode && (opcode == null || it.opcode == opcode)
    }, cq)

    fun jump(cq: ChildQuery? = null): InsnQuery = jump(null, cq)

    fun frame(type: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is FrameNode && (type == null || it.type == type)
    }, cq)

    fun frame(cq: ChildQuery? = null): InsnQuery = frame(null, cq)

    fun label(cq: ChildQuery? = null): InsnQuery = InsnQuery({ it is LabelNode }, cq)

    fun constant(cst: Any?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is LdcInsnNode && (cst == null || it.cst == cst)
    }, cq)

    fun constant(cq: ChildQuery? = null): InsnQuery = constant(null, cq)

    fun line(line: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is LineNumberNode && (line == null || it.line == line)
    }, cq)

    fun line(cq: ChildQuery? = null): InsnQuery = line(null, cq)

    fun tswitch(min: Int?, max: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is TableSwitchInsnNode && (min == null || it.min == min) && (max == null || it.max == max)
    }, cq)

    fun tswitch(cq: ChildQuery? = null): InsnQuery = tswitch(null, null, cq)

    fun lswitch(keys: Array<Int>?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is LookupSwitchInsnNode && (keys == null || it.keys.containsAll(keys.asList()))
    }, cq)

    fun lswitch(cq: ChildQuery? = null): InsnQuery = lswitch(null, cq)

    fun mana(desc: String?, dims: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is MultiANewArrayInsnNode && (desc == null || StringMatcher.matches(desc, it.desc)) &&
                (dims == null || it.dims == dims)
    }, cq)

    fun mana(desc: String?, cq: ChildQuery? = null): InsnQuery = mana(desc, null, cq)

    fun mana(cq: ChildQuery? = null): InsnQuery = mana(null, cq)

    fun type(desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is TypeInsnNode && (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun dynamic(name: String?, desc: String?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is InvokeDynamicInsnNode && (name == null || StringMatcher.matches(name, it.name)) &&
                (desc == null || StringMatcher.matches(desc, it.desc))
    }, cq)

    fun dynamic(desc: String?, cq: ChildQuery? = null): InsnQuery = dynamic(null, desc, cq)

    fun dynamic(cq: ChildQuery? = null): InsnQuery = dynamic(null, cq)

    fun inc(`var`: Int?, incr: Int?, cq: ChildQuery? = null): InsnQuery = InsnQuery({
        it is IincInsnNode && (`var` == null || it.`var` == `var`) && (incr == null || it.incr == incr)
    }, cq)

    fun inc(incr: Int?, cq: ChildQuery? = null): InsnQuery = inc(null, incr, cq)

    fun inc(cq: ChildQuery? = null): InsnQuery = inc(null, cq)
}