package net.lab0.foiegras.why

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

class ClassPrinter : ClassVisitor(Opcodes.ASM4) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array<String>
    ) {
        System.out.println(name + " extends " + superName + " {");
    }

    override fun visitField(
        access: Int,
        name: String,
        desc: String,
        signature: String,
        value: Any
    ): FieldVisitor? {
        System.out.println("    " + desc + " " + name);
        return null
    }
}
