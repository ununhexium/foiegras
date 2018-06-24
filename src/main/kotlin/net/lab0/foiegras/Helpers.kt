package net.lab0.foiegras

import com.squareup.javapoet.JavaFile
import java.nio.file.Path
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Java util logging is just madness X(
 */
val log: Logger by lazy {
  Logger.getGlobal().level = Level.OFF
  Logger.getGlobal().handlers.forEach {
    it.level = Level.OFF
  }

  val l = Logger.getLogger("Benchmark")
  l.level = Level.FINE

  val h = ConsoleHandler()
  h.level = Level.ALL

  l.addHandler(h)
  l.useParentHandlers = false

  println("Handlers: " + l.handlers.size)

  return@lazy l // lots of work for a lazy init
}

fun JavaFile.resolveFrom(outputFolder: Path) =
    packageName
        .split(".")
        .fold(outputFolder) { path, s ->
          path.resolve(s)
        }
        .resolve("${this.typeSpec.name}.java")


fun String.getter() = "get" + this.capitalize()

/**
 * can't use null in cartesian product. This is a placeholder for it
 */
val NULL = "NULL"

val random = Random()