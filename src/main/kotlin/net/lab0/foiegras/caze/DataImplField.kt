package net.lab0.foiegras.caze

import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

class DataImplField(
    val type: TypeName,
    val name: String,
    val modifiers: Array<Modifier>
)