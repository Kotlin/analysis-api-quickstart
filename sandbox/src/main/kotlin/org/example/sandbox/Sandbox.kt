package org.example.sandbox

class User(val name: String, val age: Int)

fun greet(user: User): String = "Hello, ${user.name}!"

fun now(): java.time.Instant = java.time.Instant.now()

internal fun helper() {}
