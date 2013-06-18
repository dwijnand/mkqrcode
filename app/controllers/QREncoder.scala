package controllers

import com.google.common.io.BaseEncoding
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.{Encoder, QRCode}

import scala.collection.JavaConverters._

import java.awt.image.BufferedImage
import java.awt.{Color, Image}
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object QREncoder {

  def encode(contents: String, hints: Map[EncodeHintType, _] = Map()) = {
    require(contents.length != 0, "Found empty contents")

    val ecLevelAny = hints.getOrElse(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
    val ecLevel = ecLevelAny.asInstanceOf[ErrorCorrectionLevel]

    Encoder.encode(contents, ecLevel, hints.asInstanceOf[Map[EncodeHintType, Any]].asJava)
  }

  def render(qrCode: QRCode, hints: Map[EncodeHintType, _], size: Int): String = {
    val input = qrCode.getMatrix
    require(input != null)

    val quietZoneAny = hints.getOrElse(EncodeHintType.MARGIN, 4)
    val quietZone = quietZoneAny.asInstanceOf[Int]

    val bitMatrix = toBitMatrix(input, outputWidth = input.getWidth, outputHeight = input.getHeight,
      topPadding = 0, leftPadding = 0, pixelWidth = 1, pixelHeight = 1)

    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val scaledImage = bufferedImage.getScaledInstance(size, size, Image.SCALE_FAST)

    val width = scaledImage.getWidth(null) + quietZone * 2
    val height = scaledImage.getHeight(null) + quietZone * 2
    val bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)

    val g = bi.createGraphics()
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, width, height)
    g.setColor(Color.BLACK)
    g.drawImage(scaledImage, quietZone, quietZone, null)
    g.dispose()

    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bi, "png", byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray

    BaseEncoding.base64().encode(bytes)
  }

  private[this] def toBitMatrix(input: ByteMatrix,
                                outputWidth: Int, outputHeight: Int,
                                topPadding: Int, leftPadding: Int,
                                pixelWidth: Int, pixelHeight: Int) = {
    val output = new BitMatrix(outputWidth, outputHeight)
    val inputHeight = input.getHeight
    val inputWidth = input.getWidth

    var inputY = 0
    var outputY = topPadding
    while (inputY < inputHeight) {
      var inputX = 0
      var outputX = leftPadding
      while (inputX < inputWidth) {
        if (input.get(inputX, inputY) == 1) {
          output.setRegion(outputX, outputY, pixelWidth, pixelHeight)
        }
        inputX += 1
        outputX += pixelWidth
      }
      inputY += 1
      outputY += pixelHeight
    }

    output
  }
}
