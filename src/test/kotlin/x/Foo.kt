package x

import com.google.common.collect.Lists
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PROTECTED
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.Modifier.TRANSIENT
import javax.lang.model.element.Modifier.VOLATILE

fun main(args: Array<String>) {
  val none = "none"
  val access = listOf(PUBLIC, PROTECTED, PRIVATE, none)
  val modification = listOf(VOLATILE, FINAL, none)
  val static = listOf(STATIC, none)
  val transient = listOf(TRANSIENT, none)

  val all = Lists.cartesianProduct(access, modification, static, transient)
      .map {
        it.filter { it != none }
      }

  all.forEach { println(it) }
}


