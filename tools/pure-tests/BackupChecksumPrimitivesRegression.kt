package com.henrylumis.rvhgrader.core.database

/** Executable JVM regression test for the same pure checksum primitives used by production. */
fun main() {
    check(BackupChecksumPrimitives.canonicalNumber(1) == "1")
    check(BackupChecksumPrimitives.canonicalNumber(1.0) == "1")
    check(BackupChecksumPrimitives.canonicalNumber(1e0) == "1")
    check(BackupChecksumPrimitives.canonicalNumber(-0.0) == "0")
    check(BackupChecksumPrimitives.canonicalNumber(1250.0) == "1.25E+3")
    check(BackupChecksumPrimitives.canonicalNumber(0.000001) == "0.000001")
    check(BackupChecksumPrimitives.canonicalNumber(-12.5) == "-12.5")
    check(BackupChecksumPrimitives.canonicalNumber(java.math.BigDecimal("1e100000000")) == "1E+100000000")
    check(BackupChecksumPrimitives.canonicalNumber(java.math.BigDecimal("1000")) == BackupChecksumPrimitives.canonicalNumber(java.math.BigDecimal("1e3")))
    check(runCatching { BackupChecksumPrimitives.canonicalNumber(Double.NaN) }.isFailure)
    check(runCatching { BackupChecksumPrimitives.canonicalNumber(Double.POSITIVE_INFINITY) }.isFailure)

    // Canonical JSON: object key order is ignored, but array order is meaningful.
    val firstTree = linkedMapOf<String, Any?>("z" to 1.0, "a" to listOf("x\ny", true, null))
    val reorderedTree = linkedMapOf<String, Any?>("a" to listOf("x\ny", true, null), "z" to 1e0)
    check(BackupChecksumPrimitives.canonicalJson(firstTree) == BackupChecksumPrimitives.canonicalJson(reorderedTree))
    check(BackupChecksumPrimitives.canonicalJson(firstTree) == "{\"a\":[\"x\\ny\",true,null],\"z\":1}")
    check(BackupChecksumPrimitives.canonicalJson(listOf(1, 2)) != BackupChecksumPrimitives.canonicalJson(listOf(2, 1)))
    check(BackupChecksumPrimitives.canonicalJson(true) != BackupChecksumPrimitives.canonicalJson(1))
    check(BackupChecksumPrimitives.canonicalJson(null) == "null")
    check(BackupChecksumPrimitives.canonicalJson("quote\" slash\\") == "\"quote\\\" slash\\\\\"")

    // Known SHA-256 vector: proves digest output format and implementation.
    check(BackupChecksumPrimitives.sha256("abc") ==
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    val baseline = BackupChecksumPrimitives.sha256(BackupChecksumPrimitives.canonicalJson(firstTree))
    check(baseline == BackupChecksumPrimitives.sha256(BackupChecksumPrimitives.canonicalJson(reorderedTree)))
    val alteredTree = linkedMapOf<String, Any?>("a" to listOf("x\ny", true, null), "z" to 2)
    check(baseline != BackupChecksumPrimitives.sha256(BackupChecksumPrimitives.canonicalJson(alteredTree)))
    check(baseline == BackupChecksumPrimitives.sha256(BackupChecksumPrimitives.canonicalJson(firstTree)))
    println("Backup checksum runtime tests passed (22 assertions).")
}
