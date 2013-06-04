package views.helper

import views.html.helper.FieldConstructor
import views.html.helper.twitterBootstrap2.fieldConstructor

package object twitterBootstrap2 {
  implicit val twitterBootstrap2Field = FieldConstructor(fieldConstructor.f)
}
