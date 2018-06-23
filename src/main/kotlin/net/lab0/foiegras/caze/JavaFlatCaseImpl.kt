package net.lab0.foiegras.caze

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import net.lab0.foiegras.CompilationFailed
import net.lab0.foiegras.JavaBenchmarking
import net.lab0.foiegras.log
import net.lab0.foiegras.resolveFrom
import java.nio.file.Path
import javax.lang.model.element.Modifier

class JavaFlatCaseImpl(
    override val outputFolder: Path,
    override val keywords: List<Modifier>,
    override val init: Boolean
) : JavaFlatCase {

  var fieldsCount: Int = -1

  val packageName
    get() = listOf(
        "base",
        "j",
        this::class.simpleName?.toLowerCase(),
        "f$fieldsCount",
        "k$keywordsClassNamePart"
    ).joinToString(".")

  private val keywordsClassNamePart
    get() = keywords.joinToString(separator = "") {
      it.name.toLowerCase().capitalize()
    }

  val className
    get() = "C$fieldsCount${if (initialized) "Init" else ""}Fields${keywordsClassNamePart}Class"

  override val initialized
    get() = init || keywords.contains(Modifier.FINAL)

  override fun evaluateAt(fieldsCount: Int): Boolean {
    this.fieldsCount = fieldsCount
    return generateAndCompile()
  }


  fun generateAndCompile(): Boolean {
    log.finer(
        """
        |Generating a flat java class with
        |$fieldsCount fields
        |${keywords.size} keywords: ${keywords.joinToString { it.name.toLowerCase() }}
        |${if (initialized) "With initialization" else "Uninitialized"}
        |
      """.trimMargin()
    )

    return try {
      val sources = generateFlatJavaClass()
      val files = sources.map {
        it.writeTo(outputFolder)
        return@map it.resolveFrom(outputFolder)
      }
      JavaBenchmarking.compileJava(files)

      true
    } catch (e: CompilationFailed) {
      log.finer("Failed: ${e.message}")
      false
    }
  }

  fun generateFlatJavaClass(): List<JavaFile> {
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
        |Keywords: ${this.keywords.size} ${this.keywords.joinToString { it.name.toLowerCase() }}
        |Initialisation ${this.initialized}
        |
      """.trimMargin()
}