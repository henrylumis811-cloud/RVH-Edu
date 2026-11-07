package com.henrylumis.rvhgrader.ui

fun main() {
    var assertions = 0
    fun check(value: Boolean, message: String) { check(value) { message }; assertions++ }

    val first = PinCredential.create("2468")
    check(PinCredential.verify("2468", first), "Correct PIN must verify")
    check(!PinCredential.verify("2469", first), "Incorrect PIN must fail")
    check(first.saltHex.length == 32, "Salt must contain 16 bytes")
    check(first.hashHex.length == 64, "Derived key must contain 32 bytes")

    val second = PinCredential.create("2468")
    check(first.saltHex != second.saltHex, "Each credential must have a unique salt")
    check(first.hashHex != second.hashHex, "Same PIN with a different salt must have a different hash")
    check(PinCredential.verify("2468", second), "Second salted credential must verify")
    check(!PinCredential.verify("0000", second), "Another PIN must not verify")

    check(!PinCredential.verify("2468", PinCredential.Stored("bad", first.hashHex)), "Malformed salt must fail closed")
    check(!PinCredential.verify("2468", PinCredential.Stored(first.saltHex, "xyz")), "Malformed hash must fail closed")
    check(!PinCredential.verify("2468", PinCredential.Stored("00", first.hashHex)), "Wrong salt length must fail closed")
    check(!PinCredential.verify("2468", PinCredential.Stored(first.saltHex, "00")), "Wrong hash length must fail closed")

    println("PinCredentialRegression: $assertions assertions passed")
}
