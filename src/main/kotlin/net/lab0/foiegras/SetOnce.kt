package net.lab0.foiegras

import kotlin.reflect.KProperty

class SetOnce<T> {
  var store: T? = null
}

operator fun <T> SetOnce<T>.getValue(thisRef: Any?, property: KProperty<*>): T =
    store ?: throw IllegalStateException("Access SetOnce before it's been set")

operator fun <T> SetOnce<T>.setValue(thisRef: Any?, property: KProperty<*>, new: T) =
    if (store == null) {
      store = new
    } else {
      // do nothing
    }