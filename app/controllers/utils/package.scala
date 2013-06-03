package controllers

import play.api.data.Forms._
import play.api.data.format._
import play.api.data.format.Formats._

import reflect.ClassTag

package object utils {
  def enumFormat[T <: Enum[T]](implicit m: ClassTag[T]) = new Formatter[T] {
    val enumType = m.runtimeClass.asInstanceOf[Class[T]]
    def bind(key: String, data: Map[String, String]) = stringFormat.bind(key, data).right.map(s => Enum.valueOf(enumType, s))
    def unbind(key: String, value: T): Map[String, String] = Map(key -> value.name())
  }

  def enum[T <: Enum[T]](implicit m: ClassTag[T]) = of(enumFormat[T])
}
