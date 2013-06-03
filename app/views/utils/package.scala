package views

import play.api.i18n.Messages

import reflect.ClassTag

package object utils {
  def enumOptions[T <: Enum[T]](implicit m: ClassTag[T]): Seq[(String, String)] = {
    val enumType = m.runtimeClass.asInstanceOf[Class[T]]
    enumType.getEnumConstants.map(ec => (ec.name, Messages(enumType.getName + '.' + ec.name)))
  }
}
