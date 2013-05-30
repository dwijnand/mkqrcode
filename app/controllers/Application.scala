package controllers

import collection.JavaConverters._
import com.google.common.io.BaseEncoding
import com.google.zxing.{EncodeHintType, BarcodeFormat}
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object Application extends Controller {
  val qrCodeWriter = new QRCodeWriter
  val encodingHints = Map(
    EncodeHintType.CHARACTER_SET -> "UTF-8",
    EncodeHintType.MARGIN -> 1
  )

  def index = Action {
    val contents = "https://abc.de/cps?r=1234567890123456789012345678901234567890123456789012345678901234"
    val size = 250
    val errorCorrectionLevel = ErrorCorrectionLevel.H

    val encodingHints = this.encodingHints + (EncodeHintType.ERROR_CORRECTION -> errorCorrectionLevel)
    val bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, size, size, encodingHints.asJava)
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray
    val qrCodeString = BaseEncoding.base64().encode(bytes)
    Ok(views.html.index(qrCodeString))
  }
}
