package models

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

case class Params(contents: String, size: Int, uppercase: Boolean, ecLevel: ErrorCorrectionLevel)
