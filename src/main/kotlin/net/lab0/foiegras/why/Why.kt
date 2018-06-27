package net.lab0.foiegras.why

import org.objectweb.asm.ClassReader
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val bytes = Files.readAllBytes(Paths.get("generated", "base", "j", "initobjects", "newobjectasfield", "f1", "CInitObjects1constFields.class"))
    val reader = ClassReader(bytes)
    reader.accept(ClassPrinter(), 0)
}
