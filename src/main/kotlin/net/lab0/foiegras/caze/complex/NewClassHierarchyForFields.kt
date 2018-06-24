package net.lab0.foiegras.caze.complex

import com.google.common.collect.Lists
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import net.lab0.foiegras.caze.iface.AbstractBenchmarkCase
import java.nio.file.Path
import javax.lang.model.element.Modifier.PUBLIC

/**
 * To infinity and beyond.
 * This model exports all the data in external classes
 * and uses classes hierarchies recursively to store
 * fields every time the amount goes above 5k.
 */
class NewClassHierarchyForFields(
    outputFolder: Path
) : AbstractBenchmarkCase(outputFolder) {

  companion object {
    /**
     * Magoic number taken from my previous tests.
     * These were showing that the maximum is around 6500 fields for a single class.
     * This leaves some margin so there is no need to change that number
     * (until the next crazy code generation...)
     */
    const val PARTITION_SIZE = 5000
  }

  val packageName
    get() = listOf(
        "base",
        "j",
        this::class.simpleName?.toLowerCase(),
        "f$fieldsCount"
    ).joinToString(".")

  val className
    get() = "C${fieldsCount}Fields"


  override fun generateJavaClasses(): List<JavaFile> {

    val dataClassesPackage = "$packageName.data"
    val dataPartPackage = "$packageName.part"
    val dataImplFields = createAllFields()
    val dataIface = createDataInterface(dataImplFields, packageName)
    val dataImpl = createDataImpl(dataImplFields, packageName)
    val dataClasses = createDataClasses(fieldsCount, packageName)
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
        .partition((1..fieldsCount).toList(),
                   PARTITION_SIZE
        )
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

    singletonPattern(dataList, "$packageName.$className")

    return dataList.build()
  }

  override fun verboseString(): String {
    return """
            |Infinity hierarchy chain fields. Failed because ${getCauseString()}
          """.trimMargin()
  }

}
