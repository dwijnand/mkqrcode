package models

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.QRCode

case class Request(contents: String, size: Int, uppercase: Boolean, ecLevel: ErrorCorrectionLevel, quietZone: Int)

case class Response(request: Request, qrCode: QRCode, qrCodeString: String)
