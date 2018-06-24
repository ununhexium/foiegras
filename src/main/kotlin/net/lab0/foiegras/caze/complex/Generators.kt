package net.lab0.foiegras.caze.complex

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeSpec.Builder
import net.lab0.foiegras.caze.DataImplField
import net.lab0.foiegras.getter
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.Modifier.SYNCHRONIZED

fun createAllFields(): List<DataImplField> {
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

fun createDataClasses(
    fieldsCount: Int,
    mainClassPackageName: String
): List<TypeSpec> {

  return (1..fieldsCount).map {
    TypeSpec
        .classBuilder("DataClass$it")
        .superclass(
            ClassName.bestGuess("$mainClassPackageName.DataImpl")
        ).addMethod(
            MethodSpec
                .constructorBuilder()
                .addCode(DefaultInitBlock.singleton)
                .addModifiers(PUBLIC)
                .build()
        ).addModifiers(
            PUBLIC
        ).build()
  }
}

fun createDataInterface(
    dataImplFields: List<DataImplField>,
    mainClassPackageName:String
): TypeSpec {
  val dataIface = TypeSpec
      .interfaceBuilder(
          ClassName.get(mainClassPackageName, "Data")
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


fun createDataImpl(
    dataImplFields: List<DataImplField>,
    mainClassPackageName: String
): TypeSpec {
  val dataImpl = TypeSpec.classBuilder(
      ClassName.get(
          mainClassPackageName,
          "DataImpl"
      )
  ).addSuperinterface(
      ClassName.bestGuess("$mainClassPackageName.Data")
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


object DefaultInitBlock {
  val singleton by lazy {
    CodeBlock.of(
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
  }
}


fun singletonPattern(dataList: Builder, fullyQualifiedClassName:String) {
  val thisClass = ClassName.bestGuess(fullyQualifiedClassName)

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

