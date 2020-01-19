package com.rallyhealth.weepickle.v1.core

import java.time.Instant
import java.time.format.DateTimeFormatter

/**
  * A [[Visitor]] specialized to work with JSON types. Forwards the
  * not-JSON-related methods to their JSON equivalents.
  */
trait JsVisitor[-T, +J] extends Visitor[T, J]{
  def visitFloat64(d: Double, index: Int): J = {
    val i = d.toLong
    if(i == d) visitFloat64StringParts(i.toString, -1, -1, index)
    else visitFloat64String(d.toString, index)

  }
  def visitFloat32(d: Float, index: Int): J = {
    visitFloat64(d, index)
  }
  def visitInt32(i: Int, index: Int): J = {
    visitInt64(i, index)
  }
  def visitInt64(i: Long, index: Int): J = {
    visitFloat64StringParts(i.toString, -1, -1, index)
  }
  def visitUInt64(i: Long, index: Int): J = {
    visitFloat64StringParts(java.lang.Long.toUnsignedString(i), -1, -1, index)
  }
  def visitFloat64String(s: String, index: Int): J = {
    visitFloat64StringParts(
      s,
      s.indexOf('.'),
      s.indexOf('E') match{
        case -1 => s.indexOf('e')
        case n => n
      },
      -1
    )
  }

  def visitBinary(bytes: Array[Byte], offset: Int, len: Int, index: Int): J = {
    val arr = visitArray(len, index)
    var i = 0
    while (i < len){
      arr.visitValue(arr.subVisitor.visitInt32(bytes(offset + i), index).asInstanceOf[T], index)
      i += 1
    }
    arr.visitEnd(index)
  }

  def visitFloat64StringParts(s: CharSequence, decIndex: Int, expIndex: Int): J = visitFloat64StringParts(s, decIndex, expIndex, -1)

  def visitExt(tag: Byte, bytes: Array[Byte], offset: Int, len: Int, index: Int): J = {
    val arr = visitArray(-1, index)
    arr.visitValue(visitFloat64(tag, index).asInstanceOf[T], -1)
    arr.visitValue(visitBinary(bytes, offset, len, index).asInstanceOf[T], -1)
    arr.visitEnd(-1)
  }

  def visitChar(s: Char, index: Int) = visitString(s.toString, index)

  def visitTimestamp(instant: Instant, index: Int): J = {
    visitString(DateTimeFormatter.ISO_INSTANT.format(instant), index)
  }
}