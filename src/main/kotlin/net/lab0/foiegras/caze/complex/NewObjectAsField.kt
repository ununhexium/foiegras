package net.lab0.foiegras.caze.complex

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import net.lab0.foiegras.caze.iface.AbstractBenchmarkCase
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import kotlin.math.nextUp

/**
 * Similar to the Flat case, but this generator
 * puts much more data on each field. The data
 * is declared directly inside this class.
 */
class NewObjectAsField(
    outputFolder: Path,
    val variableDeclaration: Boolean
) : AbstractBenchmarkCase(outputFolder) {

  var someFloat = AtomicReference(Float.MIN_VALUE)
  var someInt = Int.MIN_VALUE

  val packageName
    get() = listOf(
        "base",
        "j",
        this::class.simpleName?.toLowerCase(),
        if (variableDeclaration) {
          "variable"
        }
        else {
          "constant"
        },
        "f$fieldsCount"
    ).joinToString(".")

  val className
    get() = "C${fieldsCount}Fields"


  override fun generateJavaClasses(): List<JavaFile> {
    val dataImplFields = createAllFields()
    val dataIface = createDataInterface(dataImplFields, packageName)
    val dataImpl = createDataImpl(dataImplFields, packageName)
    val dataList = buildDataList()

    return listOf(
        JavaFile.builder(packageName, dataIface).build(),
        JavaFile.builder(packageName, dataImpl).build(),
        JavaFile.builder(packageName, dataList).build()
    )
  }

  private fun buildDataList(): TypeSpec {
    val dataList = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            className
        )
    )

    val initBlock = CodeBlock.of(
        "\$L",
        """
          | new DataImpl(
          |   "Hello",
          |   null,
          |   Integer.MIN_VALUE,
          |   Integer.MAX_VALUE,
          |   new Float[] {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f}
          | )
        """.trimMargin()
    )

    dataList.addFields(
        (1..fieldsCount).map {
          FieldSpec.builder(
              ClassName.bestGuess("$packageName.Data"),
              "data$it",
              PUBLIC, STATIC
          ).initializer(
              if (variableDeclaration) {
                genCodeBlock(it)
              }
              else {
                initBlock
              }
          ).build()
        }
    )

    return dataList.build()
  }

  private fun genCodeBlock(index: Int) = CodeBlock.of(
      "\$L",
      """
        | new DataImpl(
        |   "Hello$index",
        |   new String("$index@${someInt++}"),
        |   ${someInt++},
        |   ${someInt++},
        |   new Float[] {
        |   ${(0..9).map { someFloat.getAndNextUp() }.joinToString { it.toString() + "f" }}
        |   }
        | )
      """.trimMargin()
  )

  override fun verboseString(): String {
    val what = if (variableDeclaration) {
      "variable values"
    }
    else {
      "constant values"
    }
    return """
        |Static fields initialized with constructors using $what. Failed because ${getCauseString()}
      """.trimMargin()
  }


  private fun AtomicReference<Float>.getAndNextUp(): Float {
    val next = this.get().nextUp()
    this.set(next)
    return next
  }

}
