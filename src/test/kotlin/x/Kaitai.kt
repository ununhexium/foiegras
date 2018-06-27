package x

import net.lab0.foiegras.kaitai.JavaClass
import net.lab0.foiegras.kaitai.JavaClass.ClassCpInfo
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.CLASS_TYPE
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.DOUBLE
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.FIELD_REF
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.FLOAT
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.INTEGER
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.INTERFACE_METHOD_REF
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.INVOKE_DYNAMIC
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.LONG
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.METHOD_HANDLE
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.METHOD_REF
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.METHOD_TYPE
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.NAME_AND_TYPE
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.STRING
import net.lab0.foiegras.kaitai.JavaClass.ConstantPoolEntry.TagEnum.UTF8
import net.lab0.foiegras.kaitai.JavaClass.MethodRefCpInfo
import net.lab0.foiegras.kaitai.JavaClass.NameAndTypeCpInfo
import net.lab0.foiegras.kaitai.JavaClass.Utf8CpInfo
import org.junit.jupiter.api.Test

class Kaitai {
    @Test
    fun load() {
        val f = JavaClass.fromFile("/home/ununhexium/dev/kotlin/foiegras/analysis/A.class")
        println(f.constantPoolCount())
        (0..f.constantPoolCount() - 2).forEach {
            val constantPoolEntry = f.constantPool()[it]

            print("Constant entry ${it + 1} is ")
            when (constantPoolEntry.tag()) {
                UTF8 -> show(constantPoolEntry.cpInfo() as JavaClass.Utf8CpInfo)
                INTEGER -> TODO()
                FLOAT -> TODO()
                LONG -> TODO()
                DOUBLE -> TODO()
                CLASS_TYPE -> show(constantPoolEntry.cpInfo() as JavaClass.ClassCpInfo)
                STRING -> TODO()
                FIELD_REF -> TODO()
                METHOD_REF -> show(constantPoolEntry.cpInfo() as JavaClass.MethodRefCpInfo)
                INTERFACE_METHOD_REF -> TODO()
                NAME_AND_TYPE -> show(constantPoolEntry.cpInfo() as JavaClass.NameAndTypeCpInfo)
                METHOD_HANDLE -> TODO()
                METHOD_TYPE -> TODO()
                INVOKE_DYNAMIC -> TODO()
            }
            println()
        }
    }

    private fun show(info: NameAndTypeCpInfo) {
        print("Name and type " + info.nameAsStr() + from(info.nameIndex()))
    }

    private fun show(info: ClassCpInfo) {
        print("Class " + info.nameAsInfo().value() + from(info.nameIndex()))
    }

    private fun show(info: MethodRefCpInfo) {
        print("Method reference " + info.nameAndTypeAsInfo().nameAsInfo().value() + from(info.nameAndTypeIndex()))
    }

    private fun show(info: Utf8CpInfo) {
        print("utf-8 " + info.value().quoted())
    }

    private fun String.quoted() = '"' + this + '"'

    private fun from(index:Int) = " from #$index"
}
