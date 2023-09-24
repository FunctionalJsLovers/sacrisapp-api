package util

import play.api.http.Writeable
import play.api.libs.json._
import play.api.libs.json
import play.api.mvc._

import scala.concurrent.ExecutionContext

trait ControllerJson {
  def parse: PlayBodyParsers

  type JsonErrors = collection.Seq[(JsPath, collection.Seq[JsonValidationError])]
  object JsonErrors {
    def apply(path: JsPath, message: String): JsonErrors = Seq(path -> Seq(JsonValidationError(message)))
  }
  implicit val jsonErrorsWrites: OWrites[JsonErrors] = (jsonErrors: JsonErrors) => {
    val errors = jsonErrors.map { case (path, validationErrors) =>
      path.toString -> validationErrors.map(_.message)
    }.toMap
    json.Json.obj("errors" -> errors)
  }
  implicit def jsonWriteable[T](implicit writes: OWrites[T]): Writeable[T] =
    Writeable.writeableOf_JsValue.map[T](Json.toJson(_))

  def parseJson[T](jsValue: JsValue, rootPath: String)(implicit r: Reads[T]): Either[Result, T] =
    jsValue.validate[T]((__ \ rootPath).read[T]).asEither.left.map(Results.BadRequest(_))

  def parseJsonOption[T](jsValue: JsValue, rootPath: String)(implicit r: Reads[T]): Either[Result, Option[T]] =
    jsValue.validate[Option[T]]((__ \ rootPath).readNullable[T]).asEither.left.map(Results.BadRequest(_))

  def jsonParser[T](rootPath: String)(implicit r: Reads[T], ec: ExecutionContext): BodyParser[T] =
    parse.json.validate(parseJson(_, rootPath))

  def jsonParser[T, U](rootPathT: String, rootPathU: String)(implicit rT: Reads[T], rU: Reads[U], ec: ExecutionContext): BodyParser[(T, Option[U])] =
    parse.json.validate { json =>
      parseJson[T](json, rootPathT).flatMap { t =>
        parseJsonOption[U](json, rootPathU).map(u => (t, u))
      }
    }

}
