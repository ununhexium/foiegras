package net.lab0.foiegras.caze

import javax.lang.model.element.Modifier.FINAL

data class JavaBenchmarkCandidate(
    val case: JavaBenchmarkCase,
    val fieldsCount: Int
) : JavaBenchmarkCase by case {
  val packageName
    get() = listOf(
        "base",
        "j",
        "f$fieldsCount",
        "k$keywordsClassNamePart"
    ).joinToString(".")

  private val keywordsClassNamePart
    get() = case.keywords.joinToString(separator = "") {
      it.name.toLowerCase().capitalize()
    }

  val className
    get() = "C${fieldsCount}${if (initialized) "Init" else ""}Fields${keywordsClassNamePart}Class"

  val initialized
    get() = case.init || case.keywords.contains(FINAL)
}

