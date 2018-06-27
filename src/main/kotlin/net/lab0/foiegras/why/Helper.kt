package net.lab0.foiegras.why

import java.nio.ByteBuffer

fun ByteArray.take4() = this.copyOfRange(0, 4)
fun ByteArray.drop(i:Int) = this.copyOfRange(i, this.size)

fun Long.toByteArray() =
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(this).array()!!

