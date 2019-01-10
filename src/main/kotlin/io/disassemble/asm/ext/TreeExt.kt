package io.disassemble.asm.ext

import io.disassemble.asm.util.query.InsnNameMap
import io.disassemble.asm.util.query.InsnQuery
import org.objectweb.asm.tree.AbstractInsnNode

/**
 * Visits this tree recursively
 */
fun List<AbstractInsnNode>.visitTree(visitor: (insn: AbstractInsnNode) -> Unit) {
    this.forEach {
        visitor(it)
        it.children.visitTree(visitor)
    }
}

/**
 * Queries this tree with the given query filters
 */
fun List<AbstractInsnNode>.query(vararg queries: InsnQuery): InsnNameMap {
    val allMatches: InsnNameMap = HashMap()
    this.visitTree { insn ->
        val matches = queries[0].match(insn)
        var matchCount = 0
        if (matches.isNotEmpty()) {
            matchCount++
            val subMap: InsnNameMap = HashMap()
            var matchIdx = 1
            var next = insn.nextInTree
            var travel = 0
            while (next != null && matchIdx < queries.size) {
                val nextQuery = queries[matchIdx]
                val nextMap = nextQuery.match(next)
                if (++travel > nextQuery.dist) {
                    break
                }
                if (nextMap.isNotEmpty()) {
                    subMap.putAll(nextMap)
                    matchCount++
                    matchIdx++
                    travel = 0
                }
                next = next.nextInTree
            }
            if (matchCount == queries.size) {
                matches.putAll(subMap)
            }
        }
        if (matchCount == queries.size) {
            matches.keys.filter {
                it.matches("-?\\d+(\\.\\d+)?".toRegex())
            }.forEach { matches.remove(it) }
            allMatches.putAll(matches)
        }
    }
    return allMatches
}

/**
 * Prints this tree
 */
fun List<AbstractInsnNode>.print() {
    this.forEach { it.printTree() }
}