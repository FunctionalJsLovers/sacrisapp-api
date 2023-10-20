package utils

import play.api.libs.json.Reads.of
import play.api.libs.json._

import java.time.LocalDateTime

object ModelJson {
  val text255: Reads[String] = of[String]
    .map(_.strip)
    .filterNot(JsonValidationError("cannot be longer than 255"))(_.length > 255)

  val statusReads: Reads[String] = {
    case JsString(status) if Seq("scheduled", "paid").contains(status) => JsSuccess(status)
    case _ => JsError("must be either \"scheduled\" or \"paid\"")
  }

  implicit class EnhancedReads[T <: Product](reads: Reads[T]) {
    def withAtLeastOneAttribute: Reads[T] =
      reads.filterNot(JsonValidationError("at least one attribute is required"))(productFieldsAllNone)

    private def productFieldsAllNone(product: Product): Boolean =
      product.productIterator.forall {
        case _ :: _ => false
        case Nil => false
        case anotherProduct: Product if anotherProduct.productArity > 0 => productFieldsAllNone(anotherProduct)
        case any => any == None
      }
  }
  implicit class EnhancedStringReads(stringReads: Reads[String]) {
    def nonEmpty: Reads[String] = stringReads.filterNot(JsonValidationError("cannot be blank"))(_.isBlank)
  }

  val NAME: Reads[Option[String]] = (__ \ "name").readNullable[String](text255.nonEmpty)
  val DATE: Reads[Option[LocalDateTime]] = (__ \ "date").readNullable[LocalDateTime]
  val ESTIMATED_TIME: Reads[Option[Int]] = (__ \ "estimated_time").readNullable[Int]
  val STATUS: Reads[Option[String]] = (__ \ "status").readNullable[String](statusReads)
  val PRICE: Reads[Option[Int]] = (__ \ "price").readNullable[Int]
  val DESCRIPTION: Reads[Option[String]] = (__ \ "description").readNullable[String](text255.nonEmpty)
}
