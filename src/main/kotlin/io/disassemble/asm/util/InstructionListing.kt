package io.disassemble.asm.util

/**
 * @author Tyler Sedlar
 * @since 7/20/2018
 *
 * A stripped down version of the Java Bytecode Instruction Listing on Wikipedia
 */
enum class InstructionListing(
        val mnemonic: String, val hexOp: String, val binOp: String, val stack: String
) {
    AALOAD("aaload", "32", "0011 0010", "arrayref, index → value"),
    AASTORE("aastore", "53", "0101 0011", "arrayref, index, value →"),
    ACONST_NULL("aconst_null", "01", "0000 0001", "→ null"),
    ALOAD("aload", "19", "0001 1001", "→ objectref"),
    ALOAD_0("aload_0", "2a", "0010 1010", "→ objectref"),
    ALOAD_1("aload_1", "2b", "0010 1011", "→ objectref"),
    ALOAD_2("aload_2", "2c", "0010 1100", "→ objectref"),
    ALOAD_3("aload_3", "2d", "0010 1101", "→ objectref"),
    ANEWARRAY("anewarray", "bd", "1011 1101", "count → arrayref"),
    ARETURN("areturn", "b0", "1011 0000", "objectref → [empty]"),
    ARRAYLENGTH("arraylength", "be", "1011 1110", "arrayref → length"),
    ASTORE("astore", "3a", "0011 1010", "objectref →"),
    ASTORE_0("astore_0", "4b", "0100 1011", "objectref →"),
    ASTORE_1("astore_1", "4c", "0100 1100", "objectref →"),
    ASTORE_2("astore_2", "4d", "0100 1101", "objectref →"),
    ASTORE_3("astore_3", "4e", "0100 1110", "objectref →"),
    ATHROW("athrow", "bf", "1011 1111", "objectref → [empty], objectref"),
    BALOAD("baload", "33", "0011 0011", "arrayref, index → value"),
    BASTORE("bastore", "54", "0101 0100", "arrayref, index, value →"),
    BIPUSH("bipush", "10", "0001 0000", "→ value"),
    BREAKPOINT("breakpoint", "ca", "1100 1010", ""),
    CALOAD("caload", "34", "0011 0100", "arrayref, index → value"),
    CASTORE("castore", "55", "0101 0101", "arrayref, index, value →"),
    CHECKCAST("checkcast", "c0", "1100 0000", "objectref → objectref"),
    D2F("d2f", "90", "1001 0000", "value → result"),
    D2I("d2i", "8e", "1000 1110", "value → result"),
    D2L("d2l", "8f", "1000 1111", "value → result"),
    DADD("dadd", "63", "0110 0011", "value1, value2 → result"),
    DALOAD("daload", "31", "0011 0001", "arrayref, index → value"),
    DASTORE("dastore", "52", "0101 0010", "arrayref, index, value →"),
    DCMPG("dcmpg", "98", "1001 1000", "value1, value2 → result"),
    DCMPL("dcmpl", "97", "1001 0111", "value1, value2 → result"),
    DCONST_0("dconst_0", "0e", "0000 1110", "→ 0.0"),
    DCONST_1("dconst_1", "0f", "0000 1111", "→ 1.0"),
    DDIV("ddiv", "6f", "0110 1111", "value1, value2 → result"),
    DLOAD("dload", "18", "0001 1000", "→ value"),
    DLOAD_0("dload_0", "26", "0010 0110", "→ value"),
    DLOAD_1("dload_1", "27", "0010 0111", "→ value"),
    DLOAD_2("dload_2", "28", "0010 1000", "→ value"),
    DLOAD_3("dload_3", "29", "0010 1001", "→ value"),
    DMUL("dmul", "6b", "0110 1011", "value1, value2 → result"),
    DNEG("dneg", "77", "0111 0111", "value → result"),
    DREM("drem", "73", "0111 0011", "value1, value2 → result"),
    DRETURN("dreturn", "af", "1010 1111", "value → [empty]"),
    DSTORE("dstore", "39", "0011 1001", "value →"),
    DSTORE_0("dstore_0", "47", "0100 0111", "value →"),
    DSTORE_1("dstore_1", "48", "0100 1000", "value →"),
    DSTORE_2("dstore_2", "49", "0100 1001", "value →"),
    DSTORE_3("dstore_3", "4a", "0100 1010", "value →"),
    DSUB("dsub", "67", "0110 0111", "value1, value2 → result"),
    DUP("dup", "59", "0101 1001", "value → value, value"),
    DUP_X1("dup_x1", "5a", "0101 1010", "value2, value1 → value1, value2, value1"),
    DUP_X2("dup_x2", "5b", "0101 1011", "value3, value2, value1 → value1, value3, value2, value1"),
    DUP2("dup2", "5c", "0101 1100", "{value2, value1} → {value2, value1}, {value2, value1}"),
    DUP2_X1("dup2_x1", "5d", "0101 1101", "value3, {value2, value1} → {value2, value1}, value3, {value2, value1}"),
    DUP2_X2("dup2_x2", "5e", "0101 1110", "{value4, value3}, {value2, value1} → {value2, value1}, {value4, value3}, {value2, value1}"),
    F2D("f2d", "8d", "1000 1101", "value → result"),
    F2I("f2i", "8b", "1000 1011", "value → result"),
    F2L("f2l", "8c", "1000 1100", "value → result"),
    FADD("fadd", "62", "0110 0010", "value1, value2 → result"),
    FALOAD("faload", "30", "0011 0000", "arrayref, index → value"),
    FASTORE("fastore", "51", "0101 0001", "arrayref, index, value →"),
    FCMPG("fcmpg", "96", "1001 0110", "value1, value2 → result"),
    FCMPL("fcmpl", "95", "1001 0101", "value1, value2 → result"),
    FCONST_0("fconst_0", "0b", "0000 1011", "→ 0.0f"),
    FCONST_1("fconst_1", "0c", "0000 1100", "→ 1.0f"),
    FCONST_2("fconst_2", "0d", "0000 1101", "→ 2.0f"),
    FDIV("fdiv", "6e", "0110 1110", "value1, value2 → result"),
    FLOAD("fload", "17", "0001 0111", "→ value"),
    FLOAD_0("fload_0", "22", "0010 0010", "→ value"),
    FLOAD_1("fload_1", "23", "0010 0011", "→ value"),
    FLOAD_2("fload_2", "24", "0010 0100", "→ value"),
    FLOAD_3("fload_3", "25", "0010 0101", "→ value"),
    FMUL("fmul", "6a", "0110 1010", "value1, value2 → result"),
    FNEG("fneg", "76", "0111 0110", "value → result"),
    FREM("frem", "72", "0111 0010", "value1, value2 → result"),
    FRETURN("freturn", "ae", "1010 1110", "value → [empty]"),
    FSTORE("fstore", "38", "0011 1000", "value →"),
    FSTORE_0("fstore_0", "43", "0100 0011", "value →"),
    FSTORE_1("fstore_1", "44", "0100 0100", "value →"),
    FSTORE_2("fstore_2", "45", "0100 0101", "value →"),
    FSTORE_3("fstore_3", "46", "0100 0110", "value →"),
    FSUB("fsub", "66", "0110 0110", "value1, value2 → result"),
    GETFIELD("getfield", "b4", "1011 0100", "objectref → value"),
    GETSTATIC("getstatic", "b2", "1011 0010", "→ value"),
    GOTO("goto", "a7", "1010 0111", "[no change]"),
    GOTO_W("goto_w", "c8", "1100 1000", "[no change]"),
    I2B("i2b", "91", "1001 0001", "value → result"),
    I2C("i2c", "92", "1001 0010", "value → result"),
    I2D("i2d", "87", "1000 0111", "value → result"),
    I2F("i2f", "86", "1000 0110", "value → result"),
    I2L("i2l", "85", "1000 0101", "value → result"),
    I2S("i2s", "93", "1001 0011", "value → result"),
    IADD("iadd", "60", "0110 0000", "value1, value2 → result"),
    IALOAD("iaload", "2e", "0010 1110", "arrayref, index → value"),
    IAND("iand", "7e", "0111 1110", "value1, value2 → result"),
    IASTORE("iastore", "4f", "0100 1111", "arrayref, index, value →"),
    ICONST_M1("iconst_m1", "02", "0000 0010", "→ -1"),
    ICONST_0("iconst_0", "03", "0000 0011", "→ 0"),
    ICONST_1("iconst_1", "04", "0000 0100", "→ 1"),
    ICONST_2("iconst_2", "05", "0000 0101", "→ 2"),
    ICONST_3("iconst_3", "06", "0000 0110", "→ 3"),
    ICONST_4("iconst_4", "07", "0000 0111", "→ 4"),
    ICONST_5("iconst_5", "08", "0000 1000", "→ 5"),
    IDIV("idiv", "6c", "0110 1100", "value1, value2 → result"),
    IF_ACMPEQ("if_acmpeq", "a5", "1010 0101", "value1, value2 →"),
    IF_ACMPNE("if_acmpne", "a6", "1010 0110", "value1, value2 →"),
    IF_ICMPEQ("if_icmpeq", "9f", "1001 1111", "value1, value2 →"),
    IF_ICMPGE("if_icmpge", "a2", "1010 0010", "value1, value2 →"),
    IF_ICMPGT("if_icmpgt", "a3", "1010 0011", "value1, value2 →"),
    IF_ICMPLE("if_icmple", "a4", "1010 0100", "value1, value2 →"),
    IF_ICMPLT("if_icmplt", "a1", "1010 0001", "value1, value2 →"),
    IF_ICMPNE("if_icmpne", "a0", "1010 0000", "value1, value2 →"),
    IFEQ("ifeq", "99", "1001 1001", "value →"),
    IFGE("ifge", "9c", "1001 1100", "value →"),
    IFGT("ifgt", "9d", "1001 1101", "value →"),
    IFLE("ifle", "9e", "1001 1110", "value →"),
    IFLT("iflt", "9b", "1001 1011", "value →"),
    IFNE("ifne", "9a", "1001 1010", "value →"),
    IFNONNULL("ifnonnull", "c7", "1100 0111", "value →"),
    IFNULL("ifnull", "c6", "1100 0110", "value →"),
    IINC("iinc", "84", "1000 0100", "[No change]"),
    ILOAD("iload", "15", "0001 0101", "→ value"),
    ILOAD_0("iload_0", "1a", "0001 1010", "→ value"),
    ILOAD_1("iload_1", "1b", "0001 1011", "→ value"),
    ILOAD_2("iload_2", "1c", "0001 1100", "→ value"),
    ILOAD_3("iload_3", "1d", "0001 1101", "→ value"),
    IMPDEP1("impdep1", "fe", "1111 1110", ""),
    IMPDEP2("impdep2", "ff", "1111 1111", ""),
    IMUL("imul", "68", "0110 1000", "value1, value2 → result"),
    INEG("ineg", "74", "0111 0100", "value → result"),
    INSTANCEOF("instanceof", "c1", "1100 0001", "objectref → result"),
    INVOKEDYNAMIC("invokedynamic", "ba", "1011 1010", "[arg1, [arg2 ...]] → result"),
    INVOKEINTERFACE("invokeinterface", "b9", "1011 1001", "objectref, [arg1, arg2, ...] → result"),
    INVOKESPECIAL("invokespecial", "b7", "1011 0111", "objectref, [arg1, arg2, ...] → result"),
    INVOKESTATIC("invokestatic", "b8", "1011 1000", "[arg1, arg2, ...] → result"),
    INVOKEVIRTUAL("invokevirtual", "b6", "1011 0110", "objectref, [arg1, arg2, ...] → result"),
    IOR("ior", "80", "1000 0000", "value1, value2 → result"),
    IREM("irem", "70", "0111 0000", "value1, value2 → result"),
    IRETURN("ireturn", "ac", "1010 1100", "value → [empty]"),
    ISHL("ishl", "78", "0111 1000", "value1, value2 → result"),
    ISHR("ishr", "7a", "0111 1010", "value1, value2 → result"),
    ISTORE("istore", "36", "0011 0110", "value →"),
    ISTORE_0("istore_0", "3b", "0011 1011", "value →"),
    ISTORE_1("istore_1", "3c", "0011 1100", "value →"),
    ISTORE_2("istore_2", "3d", "0011 1101", "value →"),
    ISTORE_3("istore_3", "3e", "0011 1110", "value →"),
    ISUB("isub", "64", "0110 0100", "value1, value2 → result"),
    IUSHR("iushr", "7c", "0111 1100", "value1, value2 → result"),
    IXOR("ixor", "82", "1000 0010", "value1, value2 → result"),
    JSR("jsr", "a8", "1010 1000", "→ address"),
    JSR_W("jsr_w", "c9", "1100 1001", "→ address"),
    L2D("l2d", "8a", "1000 1010", "value → result"),
    L2F("l2f", "89", "1000 1001", "value → result"),
    L2I("l2i", "88", "1000 1000", "value → result"),
    LADD("ladd", "61", "0110 0001", "value1, value2 → result"),
    LALOAD("laload", "2f", "0010 1111", "arrayref, index → value"),
    LAND("land", "7f", "0111 1111", "value1, value2 → result"),
    LASTORE("lastore", "50", "0101 0000", "arrayref, index, value →"),
    LCMP("lcmp", "94", "1001 0100", "value1, value2 → result"),
    LCONST_0("lconst_0", "09", "0000 1001", "→ 0L"),
    LCONST_1("lconst_1", "0a", "0000 1010", "→ 1L"),
    LDC("ldc", "12", "0001 0010", "→ value"),
    LDC_W("ldc_w", "13", "0001 0011", "→ value"),
    LDC2_W("ldc2_w", "14", "0001 0100", "→ value"),
    LDIV("ldiv", "6d", "0110 1101", "value1, value2 → result"),
    LLOAD("lload", "16", "0001 0110", "→ value"),
    LLOAD_0("lload_0", "1e", "0001 1110", "→ value"),
    LLOAD_1("lload_1", "1f", "0001 1111", "→ value"),
    LLOAD_2("lload_2", "20", "0010 0000", "→ value"),
    LLOAD_3("lload_3", "21", "0010 0001", "→ value"),
    LMUL("lmul", "69", "0110 1001", "value1, value2 → result"),
    LNEG("lneg", "75", "0111 0101", "value → result"),
    LOOKUPSWITCH("lookupswitch", "ab", "1010 1011", "key →"),
    LOR("lor", "81", "1000 0001", "value1, value2 → result"),
    LREM("lrem", "71", "0111 0001", "value1, value2 → result"),
    LRETURN("lreturn", "ad", "1010 1101", "value → [empty]"),
    LSHL("lshl", "79", "0111 1001", "value1, value2 → result"),
    LSHR("lshr", "7b", "0111 1011", "value1, value2 → result"),
    LSTORE("lstore", "37", "0011 0111", "value →"),
    LSTORE_0("lstore_0", "3f", "0011 1111", "value →"),
    LSTORE_1("lstore_1", "40", "0100 0000", "value →"),
    LSTORE_2("lstore_2", "41", "0100 0001", "value →"),
    LSTORE_3("lstore_3", "42", "0100 0010", "value →"),
    LSUB("lsub", "65", "0110 0101", "value1, value2 → result"),
    LUSHR("lushr", "7d", "0111 1101", "value1, value2 → result"),
    LXOR("lxor", "83", "1000 0011", "value1, value2 → result"),
    MONITORENTER("monitorenter", "c2", "1100 0010", "objectref →"),
    MONITOREXIT("monitorexit", "c3", "1100 0011", "objectref →"),
    MULTIANEWARRAY("multianewarray", "c5", "1100 0101", "count1, [count2,...] → arrayref"),
    NEW("new", "bb", "1011 1011", "→ objectref"),
    NEWARRAY("newarray", "bc", "1011 1100", "count → arrayref"),
    NOP("nop", "00", "0000 0000", "[No change]"),
    POP("pop", "57", "0101 0111", "value →"),
    POP2("pop2", "58", "0101 1000", "{value2, value1} →"),
    PUTFIELD("putfield", "b5", "1011 0101", "objectref, value →"),
    PUTSTATIC("putstatic", "b3", "1011 0011", "value →"),
    RET("ret", "a9", "1010 1001", "[No change]"),
    RETURN("return", "b1", "1011 0001", "→ [empty]"),
    SALOAD("saload", "35", "0011 0101", "arrayref, index → value"),
    SASTORE("sastore", "56", "0101 0110", "arrayref, index, value →"),
    SIPUSH("sipush", "11", "0001 0001", "→ value"),
    SWAP("swap", "5f", "0101 1111", "value2, value1 → value1, value2"),
    TABLESWITCH("tableswitch", "aa", "1010 1010", "index →"),
    WIDE("wide", "c4", "1100 0100", "[same as for corresponding instructions]");

    /**
     * Creates a pair of the pulling and pushing stack sizes
     */
    fun stackSizes(): Pair<Int, Int> {
        var split = stack.split(" → ")
        if (split.size == 1 && stack.contains("→")) {
            split = stack.split(" →")
        }
        var pullCount = 0
        var pushCount = 1
        if (split.size == 2) {
            val before = split[0]
            val after = split[1]
            pullCount = if ("[" !in before) {
                before.split(",").size
            } else {
                -1 // dynamic - invoke*, multianewarray
            }
            pushCount = if ("[" !in after) {
                after.split(",").size
            } else {
                if (after == "[empty]" || after == "[empty], objectref") {
                    1
                } else {
                    error("All instructions have an output")
                }
            }
        } // if mnemonic == "wide", pull may be 1 instead of 0.. need to test.
        return Pair(pullCount, pushCount)
    }

    companion object {

        /**
         * A map of the instruction listings using its mnemonic as a key
         */
        val MAP = InstructionListing.values().map { it.mnemonic to it }.toMap()
    }
}

/**
 * Prints out the enum of the bytecode instruction listing above
 */
//fun main(args: Array<String>) {
//    run { // prints the above enum
//        val url = URL("https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings")
//        val text = url.readText()
//        val tds = text.split("\n").filter { it.startsWith("<td>") }.map { it.substring(4) }
//
//        for (i in 0..(tds.size / 6 - 2)) { // the last opcode is a placeholder
//            var idx = (i * 6)
//            val mnemonic = tds[idx++]
//            val hexOp = tds[idx++]
//            val binOp = tds[idx++]
//            val other = tds[idx++]
//            val stack = tds[idx++]
//            val desc = tds[idx]
//            println("${mnemonic.toUpperCase()}(\"$mnemonic\", \"$hexOp\", \"$binOp\", \"$stack\"),")
//        }
//    }
//}