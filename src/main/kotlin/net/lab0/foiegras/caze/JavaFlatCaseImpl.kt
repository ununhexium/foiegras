package net.lab0.foiegras.caze

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import net.lab0.foiegras.caze.iface.AbstractBenchmarkCase
import java.nio.file.Path
import javax.lang.model.element.Modifier

/**
 * Generates a single Java class with different options.
 *
 * Al the declared fields are in this class.
 */
class JavaFlatCaseImpl(
    outputFolder: Path,
    override val type: TypeName,
    override val keywords: List<Modifier>,
    override val init: Boolean
) : AbstractBenchmarkCase(outputFolder), JavaFlatCase {

  val packageName
    get() = listOf(
        "base",
        "j",
        this::class.simpleName?.toLowerCase(),
        "k$keywordsNamePart",
        if (initialized) "init" else "empty",
        "t${type.keywordHack()}",
        "f$fieldsCount"
    ).joinToString(".")

  private val keywordsNamePart
    get() = keywords.joinToString(separator = "") {
      it.name.toLowerCase().capitalize()
    }

  val className
    get() = "C${fieldsCount}Fields"

  override val initialized
    get() = init || keywords.contains(Modifier.FINAL)

  override fun generateJavaClasses(): List<JavaFile> {
    val typeSpec = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            className
        )
    )

    (1..fieldsCount).forEach {

      val fieldSpec = FieldSpec.builder(
          TypeName.BYTE, "field$it", *keywords.toTypedArray()
      )

      if (initialized) {
        fieldSpec.initializer("\$L", "$it % Byte.MAX_VALUE")
      }

      typeSpec.addField(fieldSpec.build())
    }

    return listOf(
        JavaFile.builder(
            packageName,
            typeSpec.build()
        ).build()
    )
  }

  override fun verboseString() =
      """
        |DataType: ${type.keywordHack()} ; Keywords: ${this.keywords.size} ${this.keywords.joinToString { it.name.toLowerCase() }} ; Initialisation ${this.initialized}. Failed because ${getCauseString()}
        |
      """.trimMargin()


  private fun TypeName.keywordHack(): String {
    return when (this) {
      TypeName.BYTE -> "byte"
      TypeName.SHORT -> "short"
      TypeName.INT -> "int"
      TypeName.LONG -> "long"
      TypeName.OBJECT -> "Object"
      else -> this.toString()
    }
  }
}
