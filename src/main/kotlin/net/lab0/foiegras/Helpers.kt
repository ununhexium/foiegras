package net.lab0.foiegras

import com.squareup.javapoet.JavaFile
import java.nio.file.Path


fun JavaFile.resolveFrom(outputFolder: Path) =
    packageName
        .split(".")
        .fold(outputFolder) { path, s ->
          path.resolve(s)
        }
        .resolve("${this.typeSpec.name}.java")


fun String.getter() = "get" + this.capitalize()
