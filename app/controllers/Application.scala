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
import play.api.mvc._
import utils._

import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.imageio.ImageIO

object Application extends Controller {
  val locale = Locale.ENGLISH
  val qrCodeWriter = new QRCodeWriter
  val encodingHints = Map(EncodeHintType.CHARACTER_SET -> "UTF-8")

  val initialParams = Params("https://www.powatag.com/cps/1234567890", 100, uppercase = true, ErrorCorrectionLevel.L)

  val paramsForm = Form(
    mapping(
      "contents" -> nonEmptyText,
      "size" -> number,
      "uppercase" -> boolean,
      "errorCorrectionLevel" -> enum[ErrorCorrectionLevel]
    )(Params.apply)(Params.unapply)
  )

  def home = Action {
    implicit request =>
      paramsForm.bindFromRequest.fold(
        formWithErrors => Ok(render(initialParams)),
        params => Ok(render(params))
      )
  }

  def post = Action {
    implicit request =>
      paramsForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.home(None, formWithErrors)),
        params => Redirect(routes.Application.home().url, request.body.asFormUrlEncoded.get)
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

    views.html.home(Some((p, qrCodeString)), paramsForm.fill(p))
  }

  val errorCorrectionLevels = ErrorCorrectionLevel.values().map(_ match {
    case ErrorCorrectionLevel.L => ("L", "Low")
    case ErrorCorrectionLevel.M => ("M", "Medium")
    case ErrorCorrectionLevel.Q => ("Q", "Quartile")
    case ErrorCorrectionLevel.H => ("H", "High")
  })
}
