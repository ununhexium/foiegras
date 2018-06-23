package net.lab0.foiegras

import javax.lang.model.element.Modifier

data class BenchmarkResult(
    val language: Language,
    val fieldsCount: Int,
    val keywordsCount: List<Modifier>,
    val initialized: Boolean
)