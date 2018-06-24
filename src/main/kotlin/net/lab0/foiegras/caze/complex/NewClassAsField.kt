package net.lab0.foiegras.caze.complex

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import net.lab0.foiegras.caze.iface.AbstractBenchmarkCase
import java.nio.file.Path
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/**
 * Declares all the fields inside this class.
 * Each fields contains the same amount of data
 * as in [NewObjectAsField], but the data itself
 * is exported to a different class.
 */
class NewClassAsField(
    outputFolder: Path,
    val useStatic: Boolean
) : AbstractBenchmarkCase(outputFolder) {

  val packageName
    get() = listOf(
        "base",
        "j",
        this::class.simpleName?.toLowerCase(),
        if (useStatic) {
          "statiq"
        }
        else {
          "instance"
        },
        "f$fieldsCount"
    ).joinToString(".")


  val className
    get() = "C${fieldsCount}Fields"


  override fun generateJavaClasses(): List<JavaFile> {
    val dataImplFields = createAllFields()
    val dataIface = createDataInterface(dataImplFields, packageName)
    val dataImpl = createDataImpl(dataImplFields, packageName)
    val dataClasses = createDataClasses(fieldsCount, packageName)
    val dataList = buildDataList()

    return listOf(
        JavaFile.builder(packageName, dataIface).build(),
        JavaFile.builder(packageName, dataImpl).build(),
        JavaFile.builder(packageName, dataList).build()
    ) + dataClasses.map {
      JavaFile.builder(packageName, it).build()
    }
  }

  private fun buildDataList(): TypeSpec {
    val dataList = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            className
        )
    )

    val modifiers = if (useStatic) {
      arrayOf(PUBLIC, STATIC)
    }
    else {
      arrayOf(PUBLIC)
    }

    dataList.addFields(
        (1..fieldsCount).map {
          FieldSpec.builder(
              ClassName.bestGuess("$packageName.Data"),
              "data$it",
              *modifiers
          ).initializer(
              CodeBlock.of(
                  "new \$T()",
                  ClassName.bestGuess("$packageName.DataClass$it")
              )
          ).build()
        }
    )

    if (!useStatic) {
      singletonPattern(dataList, "$packageName.$className")
    }

    return dataList.build()
  }

  override fun verboseString(): String {
    val what = if (useStatic) {
      "Static"
    }
    else {
      "Instance"
    }

    return """
            |$what fields initialized with 0-arg classes. Failed because ${getCauseString()}
          """.trimMargin()
  }

}
