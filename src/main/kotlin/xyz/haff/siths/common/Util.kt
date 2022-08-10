package xyz.haff.siths.common

fun String.escape() = this
    .replace("\"", "\\\"")
    .replace("\n", "\\n")