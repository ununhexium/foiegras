package net.lab0.foiegras.caze

import com.google.common.collect.Lists
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
import javax.lang.model.element.Modifier.SYNCHRONIZED

/**
 * To infinity and beyond.
 * This model exports all the data in external classes
 * and uses classes hierarchies recursively to store
 * fields everytime the amount goes above 5k.
 */
class NewClassHierarchyForFields(
    val outputFolder: Path
) : BenchmarkCase {

  var fieldsCount: Int = -1

  /**
   * Magoic number taken from my previous tests.
   * These were showing that the maximum is around 6500 fields for a single class.
   * This leaves some margin so there is no need to change that number
   * (until the next crazy code generation...)
   */
  val PARTITION_SIZE = 5000

  val packageName
    get() = listOf(
        "base",
        "j",
        "hiera",
        this::class.simpleName?.toLowerCase(),
        "f$fieldsCount"
    ).joinToString(".")

  val className
    get() = "CHiera${fieldsCount}Fields"

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

    val dataClassesPackage = "$packageName.data"
    val dataPartPackage = "$packageName.part"
    val dataImplFields = createAllFields()
    val dataIface = createDataInterface(dataImplFields)
    val dataImpl = createDataImpl(dataImplFields)
    val dataClasses = createDataClasses()
    val partClasses = createParentClasses(dataPartPackage, dataClassesPackage)
    val dataList = buildDataList(dataPartPackage, partClasses.lastIndex)

    return listOf(
        JavaFile.builder(packageName, dataIface).build(),
        JavaFile.builder(packageName, dataImpl).build(),
        JavaFile.builder(packageName, dataList).build()
    ) + dataClasses.map {
      JavaFile.builder(dataClassesPackage, it).build()
    } + partClasses.map {
      JavaFile.builder(dataPartPackage, it).build()
    }
  }

  private fun createParentClasses(
      dataPartPackage: String,
      dataClassesPackage: String
  ): List<TypeSpec> {
    val dataIface = ClassName.bestGuess("$packageName.Data")

    val partBuilders = Lists
        .partition((1..fieldsCount).toList(), PARTITION_SIZE)
        .mapIndexed { index, partition ->
          TypeSpec
              .classBuilder("Part$index")
              .addModifiers(PUBLIC)
              .addFields(
                  partition.map {
                    FieldSpec.builder(
                        dataIface,
                        "data$it",
                        PUBLIC
                    ).initializer(
                        CodeBlock.of(
                            "new \$T()",
                            ClassName.bestGuess("$dataClassesPackage.DataClass$it")
                        )
                    ).build()
                  }
              )
        }

    // set all the parent classes
    partBuilders
        .drop(1)
        .mapIndexed { index, builder ->
          builder.superclass(ClassName.bestGuess("$dataPartPackage.Part$index"))
        }

    return partBuilders.map { it.build() }
  }

  private fun createDataClasses(): List<TypeSpec> {
    val superBlock = CodeBlock.of(
        "\$L\n",
        """
          | super(
          | "Hello",
          | null,
          | Integer.MIN_VALUE,
          | Integer.MAX_VALUE,
          | new Float[] {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f}
          | );
        """.trimMargin()
    )

    return (1..fieldsCount).map {
      TypeSpec
          .classBuilder("DataClass$it")
          .superclass(
              ClassName.bestGuess("$packageName.DataImpl")
          ).addMethod(
              MethodSpec
                  .constructorBuilder()
                  .addCode(superBlock)
                  .addModifiers(PUBLIC)
                  .build()
          ).addModifiers(
              PUBLIC
          ).build()
    }
  }

  private fun buildDataList(
      dataPartPackage: String,
      topParentIndex: Int
  ): TypeSpec {
    val dataList = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            className
        )
    )

    dataList.superclass(
        ClassName.bestGuess("$dataPartPackage.Part$topParentIndex")
    )

    singletonPattern(dataList)

    return dataList.build()
  }

  private fun singletonPattern(dataList: Builder) {
    val thisClass = ClassName.bestGuess("$packageName.$className")

    dataList.addField(
        FieldSpec
            .builder(
                thisClass,
                "instance"
            ).addModifiers(
                PRIVATE, STATIC
            ).initializer(
                CodeBlock.of("\$L", "null")
            ).build()
    )

    val dollar = "$"

    dataList.addMethod(
        MethodSpec
            .methodBuilder(
                "getInstance"
            ).addModifiers(
                PUBLIC, STATIC, SYNCHRONIZED
            ).returns(
                thisClass
            ).addCode(
                CodeBlock.of(
                    """
                        | if(instance == null) {
                        |   instance = new ${dollar}T();
                        | }
                        | return instance;
                        |
                      """.trimMargin(),
                    thisClass
                )
            ).build()
    )

    dataList.addMethod(
        MethodSpec
            .constructorBuilder()
            .addModifiers(PRIVATE)
            .build()
    )
  }

  private fun createDataImpl(
      dataImplFields: List<DataImplField>
  ): TypeSpec {
    val dataImpl = TypeSpec.classBuilder(
        ClassName.get(
            packageName,
            "DataImpl"
        )
    ).addSuperinterface(
        ClassName.bestGuess("$packageName.Data")
    ).addModifiers(
        PUBLIC
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
            ).addModifiers(
                PUBLIC
            ).build()
    )

    return dataImpl.build()
  }

  private fun createDataInterface(
      dataImplFields: List<DataImplField>
  ): TypeSpec {
    val dataIface = TypeSpec
        .interfaceBuilder(
            ClassName.get(packageName, "Data")
        ).addModifiers(
            PUBLIC
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

    return dataIface.build()
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

  override fun verboseString(): String {
    return """
            |Infinity hierarchy chain fields.
          """.trimMargin()
  }

}
