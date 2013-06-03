package controllers

import collection.JavaConverters._
import com.google.common.io.BaseEncoding
import com.google.zxing.{EncodeHintType, BarcodeFormat}
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import models.Params
import play.api.data._
import play.api.data.Forms._
import play.api.data.format._
import play.api.data.format.Formats._
import play.api.mvc._

import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.imageio.ImageIO

object Application extends Controller {
  val locale = Locale.ENGLISH
  val qrCodeWriter = new QRCodeWriter
  val encodingHints = Map(
    EncodeHintType.CHARACTER_SET -> "UTF-8",
    EncodeHintType.MARGIN -> 1
  )

  def enumFormat[T <: Enum[T]](enumType: Class[T]): Formatter[T] = new Formatter[T] {
    def bind(key: String, data: Map[String, String]) = stringFormat.bind(key, data).right.map(s => Enum.valueOf(enumType, s))
    def unbind(key: String, value: T): Map[String, String] = Map(key -> value.name())
  }

  def enum[T <: Enum[T]](enumType: Class[T]) = of(enumFormat(classOf[ErrorCorrectionLevel]))

  val paramsForm = Form(
    mapping(
      "contents" -> nonEmptyText,
      "size" -> number,
      "uppercase" -> boolean,
      "errorCorrectionLevel" -> enum(classOf[ErrorCorrectionLevel])
    )(Params.apply)(Params.unapply)
  )

  def index = Action {
    implicit request =>
      paramsForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.index(None, formWithErrors)),
        params => Ok(render(params))
      )
  }

  def render(p: Params) = {
    val encodingHints = this.encodingHints + (EncodeHintType.ERROR_CORRECTION -> p.errorCorrectionLevel)
    val data = if (p.uppercase) p.contents.toUpperCase(locale) else p.contents

    val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, p.size, p.size, encodingHints.asJava)
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray
    val qrCodeString = BaseEncoding.base64().encode(bytes)

    views.html.index(Some((p, qrCodeString)), paramsForm.fill(p))
  }

  val errorCorrectionLevels = ErrorCorrectionLevel.values().map(_ match {
    case ErrorCorrectionLevel.L => ("L", "Low")
    case ErrorCorrectionLevel.M => ("M", "Medium")
    case ErrorCorrectionLevel.Q => ("Q", "Quartile")
    case ErrorCorrectionLevel.H => ("H", "High")
  })
}
