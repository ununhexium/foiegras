package net.lab0.foiegras.caze

import net.lab0.foiegras.Language
import java.nio.file.Path
import javax.lang.model.element.Modifier

class JavaBenchmarkCaseImpl(
    override val language: Language,
    override val outputFolder: Path,
    override val keywords: List<Modifier>,
    override val init: Boolean
) : JavaBenchmarkCase
