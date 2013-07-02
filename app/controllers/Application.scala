package controllers

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import controllers.utils._
import models.{Request, Response}

import java.util.Locale

object Application extends Controller {
  private[this] val locale = Locale.ENGLISH
  private[this] val encodingHints: Map[EncodeHintType, _] = Map(EncodeHintType.CHARACTER_SET -> "UTF-8")

  private[this] val initialRequest =
    Request("https://www.powatag.com/cps/1234567890", 100, uppercase = true, ErrorCorrectionLevel.L, 4)

  private[this] val form = Form(
    mapping(
      "contents" -> nonEmptyText,
      "size" -> number,
      "uppercase" -> boolean,
      "ecLevel" -> enum[ErrorCorrectionLevel],
      "quietZone" -> number
    )(Request.apply)(Request.unapply)
  )

  def home = Action {
    implicit request => {
      request.queryString.size match {
        case 0 => Ok(views.html.Application.home(None, form.fill(initialRequest)))
        case _ => form.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.Application.home(None, formWithErrors)),
          params => Ok(render(params))
        )
      }
    }
  }

  def post = Action(request => Redirect(routes.Application.home().url, request.body.asFormUrlEncoded.get))

  private[this] def render(r: Request) = {
    val encodingHints = this.encodingHints +
      (EncodeHintType.ERROR_CORRECTION -> r.ecLevel) +
      (EncodeHintType.MARGIN -> r.quietZone)
    val data = if (r.uppercase) r.contents.toUpperCase(locale) else r.contents

    val qrCode = QREncoder.encode(data, encodingHints)
    val qrCodeString = QREncoder.render(qrCode, encodingHints, r.size)
    val response = Response(r, qrCode, qrCodeString)

    views.html.Application.home(Some(response), form.fill(r))
  }
}
