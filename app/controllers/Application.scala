package controllers


import com.google.common.io.BaseEncoding
import com.google.zxing.{EncodeHintType, BarcodeFormat}
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import controllers.utils._
import models.Params

import scala.collection.JavaConverters._

import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.imageio.ImageIO

object Application extends Controller {
  private[this] val locale = Locale.ENGLISH
  private[this] val qrCodeWriter = new QRCodeWriter
  private[this] val encodingHints = Map(EncodeHintType.CHARACTER_SET -> "UTF-8")

  private[this] val initialParams = Params("https://www.powatag.com/cps/1234567890", 100, uppercase = true, ErrorCorrectionLevel.L)

  private[this] val paramsForm = Form(
    mapping(
      "contents" -> nonEmptyText,
      "size" -> number,
      "uppercase" -> boolean,
      "errorCorrectionLevel" -> enum[ErrorCorrectionLevel]
    )(Params.apply)(Params.unapply)
  )

  def home = Action {
    implicit request => {
      request.queryString.size match {
        case 0 => Ok(views.html.Application.home(None, paramsForm.fill(initialParams)))
        case _ => paramsForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.Application.home(None, formWithErrors)),
          params => Ok(render(params))
        )
      }
    }
  }

  def post = Action(request => Redirect(routes.Application.home().url, request.body.asFormUrlEncoded.get))

  private[this] def render(p: Params) = {
    val encodingHints = this.encodingHints + (EncodeHintType.ERROR_CORRECTION -> p.errorCorrectionLevel)
    val data = if (p.uppercase) p.contents.toUpperCase(locale) else p.contents

    val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, p.size, p.size, encodingHints.asJava)
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray
    val qrCodeString = BaseEncoding.base64().encode(bytes)

    views.html.Application.home(Some((p, qrCodeString)), paramsForm.fill(p))
  }
}
