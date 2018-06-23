package net.lab0.foiegras.caze

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeSpec.Builder
import net.lab0.foiegras.CompilationFailed
import net.lab0.foiegras.JavaBenchmarking
import net.lab0.foiegras.caze.iface.BenchmarkCase
import net.lab0.foiegras.getter
import net.lab0.foiegras.log
import net.lab0.foiegras.resolveFrom
import java.nio.file.Path
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class NewObjectAsField(
    val outputFolder: Path
) : BenchmarkCase {

  var fieldsCount: Int = -1

  val packageName
    get() = listOf(
        "base",
        "j",
        "initobjects",
        this::class.simpleName?.toLowerCase(),
        "f$fieldsCount"
    ).joinToString(".")

  val className
    get() = "CInitObjects${fieldsCount}Fields"

  override fun evaluateAt(fieldsCount: Int): Boolean {
    this.fieldsCount = fieldsCount
    return generateAndCompile()
  }

  private fun generateAndCompile(): Boolean {
    return try {
      val sources = generateJavaClasses()
      val files = sources.map {
        it.writeTo(outputFolder)
        return@map it.resolveFrom(outputFolder)
      }
      JavaBenchmarking.compileJava(files)

      true
    }
    catch (e: CompilationFailed) {
      log.finer("Failed: ${e.message}")
      false
    }
  }

  private fun generateJavaClasses(): List<JavaFile> {
    val dataImplFields = createAllFields()
    val dataIface = createDataInterface(dataImplFields)
    val dataImpl = createDataImpl(dataImplFields)
    val dataList = buildDataList()

    return listOf(
        JavaFile.builder(packageName, dataIface.build()).build(),
        JavaFile.builder(packageName, dataImpl.build()).build(),
        JavaFile.builder(packageName, dataList.build()).build()
    )
  }

  private fun buildDataList(): Builder {
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
              initBlock
          ).build()
        }
    )

    return dataList
  }

  private fun createDataImpl(
      dataImplFields: List<DataImplField>
  ): TypeSpec.Builder {
    val dataImpl = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            "DataImpl"
        )
    ).addSuperinterface(
        ClassName.bestGuess("$packageName.Data")
    )

    dataImplFields.fold(dataImpl) { d, f ->
      d.addField(
          FieldSpec.builder(
              f.type,
              f.name,
              *f.modifiers
          ).build()
      )
    }

    dataImplFields.fold(dataImpl) { d, f ->
      d.addMethod(
          MethodSpec
              .methodBuilder(
                  f.name.getter()
              ).addModifiers(
                  PUBLIC
              ).returns(
                  f.type
              ).addCode(
                  CodeBlock.of("return \$L;", f.name)
              ).build()
      )
    }

    dataImpl.addMethod(
        MethodSpec
            .constructorBuilder()
            .addParameters(
                dataImplFields.map {
                  ParameterSpec.builder(
                      it.type,
                      it.name
                  ).build()
                }
            )
            .build()
    )

    return dataImpl
  }

  private fun createDataInterface(
      dataImplFields: List<DataImplField>
  ): TypeSpec.Builder {
    val dataIface = TypeSpec.interfaceBuilder(
        ClassName.get(packageName, "Data")
    )

    dataIface.addMethods(
        dataImplFields.map {
          MethodSpec
              .methodBuilder(
                  it.name.getter()
              ).addModifiers(
                  PUBLIC, ABSTRACT
              ).returns(
                  it.type
              ).build()
        }
    )
    return dataIface
  }

  private fun createAllFields(): List<DataImplField> {
    return listOf(
        DataImplField(
            TypeName.get(String::class.java),
            "name",
            arrayOf(PRIVATE)
        ),
        DataImplField(
            TypeName.get(Object::class.java),
            "reference",
            arrayOf(PRIVATE)
        ),
        DataImplField(
            TypeName.INT,
            "start",
            arrayOf(PRIVATE)
        ),
        DataImplField(
            TypeName.INT,
            "end",
            arrayOf(PRIVATE)
        ),
        DataImplField(
            TypeName.get(Array<Float>::class.java),
            "values",
            arrayOf(PRIVATE)
        )
    )
  }

  override fun verboseString() =
      """
        |Static fields initialized with constructors
      """.trimMargin()
}
