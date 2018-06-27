package x

import net.lab0.foiegras.kaitai.JavaClass
import net.lab0.foiegras.kaitai.JavaClass.AttributeInfo.AttrBodyCode
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
import net.lab0.foiegras.kaitai.JavaClass.FieldRefCpInfo
import net.lab0.foiegras.kaitai.JavaClass.FloatCpInfo
import net.lab0.foiegras.kaitai.JavaClass.IntegerCpInfo
import net.lab0.foiegras.kaitai.JavaClass.MethodRefCpInfo
import net.lab0.foiegras.kaitai.JavaClass.NameAndTypeCpInfo
import net.lab0.foiegras.kaitai.JavaClass.StringCpInfo
import net.lab0.foiegras.kaitai.JavaClass.Utf8CpInfo
import org.junit.jupiter.api.Test

class Kaitai {
    @Test
    fun load() {
        val paths = listOf(
//            "/home/ununhexium/dev/kotlin/foiegras/generated/base/j/javaflatcaseimpl/kPublic/init/tbyte/f10240/C10240Fields.class",
//            "/home/ununhexium/dev/kotlin/foiegras/analysis/A.class",
            "/home/ununhexium/dev/kotlin/foiegras/generated/base/j/newobjectasfield/var/f512/C512Fields.class"
        )
        paths.forEach {
            val f = JavaClass.fromFile(it)
            browseConstants(f)
            showLimits(f)
        }
    }

    private fun showLimits(f: JavaClass, threashold: Float = 0.8f) {
        val uint16Threashold = 65536 * threashold
        val uint32Threashold = 65536L * 65536L * threashold

        val int16 = listOf(
            f.constantPoolCount() to "Constant",
            f.methodsCount() to "Methods",
            f.attributesCount() to "Attributes",
            f.fieldsCount() to "Fields",
            f.interfacesCount() to "Interfaces"
        ) + (0 until f.methodsCount()).map {
            f.methods()[it].attributesCount() to "Method #$it attributes"
        }

        val int32 = (0 until f.methodsCount()).flatMap { methodIndex ->
            f.methods()[methodIndex].attributes().mapIndexed { attributeIndex, attributeInfo ->
                when (attributeInfo.info()) {
                    is AttrBodyCode -> (attributeInfo.info() as AttrBodyCode).codeLength() to "Method #$methodIndex attribute #$attributeIndex"
                    else -> TODO("Other attribute body")
                }
            }
        }

        int16.filter { it.first > uint16Threashold }.forEach {
            println(it.second + " count high @ " + it.first)
        }
        int32.filter { it.first > uint32Threashold }.forEach {
            println(it.second + " count high @ " + it.first)
        }
    }

    private fun browseConstants(f: JavaClass) {
        println(f.constantPoolCount())
        (0..f.constantPoolCount() - 2).forEach {
            val constantPoolEntry = f.constantPool()[it]

            print("Constant entry ${it + 1} is ")
            with(constantPoolEntry) {
                when (tag()) {
                    UTF8 -> show(cpInfo() as Utf8CpInfo)
                    INTEGER -> show(cpInfo() as IntegerCpInfo)
                    FLOAT -> show(cpInfo() as FloatCpInfo)
                    LONG -> TODO()
                    DOUBLE -> TODO()
                    CLASS_TYPE -> show(cpInfo() as ClassCpInfo)
                    STRING -> show(cpInfo() as StringCpInfo)
                    FIELD_REF -> show(cpInfo() as FieldRefCpInfo)
                    METHOD_REF -> show(cpInfo() as MethodRefCpInfo)
                    INTERFACE_METHOD_REF -> TODO()
                    NAME_AND_TYPE -> show(cpInfo() as NameAndTypeCpInfo)
                    METHOD_HANDLE -> TODO()
                    METHOD_TYPE -> TODO()
                    INVOKE_DYNAMIC -> TODO()
                    else -> throw RuntimeException("Mhh...")
                }
            }
            println()
        }
    }

    private fun show(info: FloatCpInfo) {
        print(info.value())
    }

    private fun show(info: IntegerCpInfo) {
        print(info.value())
    }

    private fun show(info: StringCpInfo) {
        print(from(info.stringIndex()))
    }

    private fun show(info: FieldRefCpInfo) {
        print(info.classAsInfo().nameAsStr())
    }

    private fun show(info: NameAndTypeCpInfo) {
        print("Name and type " + info.nameAsStr() + from(info.nameIndex()))
    }

    private fun show(info: ClassCpInfo) {
        print("Class " + info.nameAsInfo().value() + from(info.nameIndex()))
    }

    private fun show(info: MethodRefCpInfo) {
        print(
            "Method reference " + info.nameAndTypeAsInfo().nameAsInfo().value() + from(
                info.nameAndTypeIndex()
            )
        )
    }

    private fun show(info: Utf8CpInfo) {
        print("utf-8 " + info.value().quoted())
    }

    private fun String.quoted() = '"' + this + '"'

    private fun from(index: Int) = " from #$index"
}
