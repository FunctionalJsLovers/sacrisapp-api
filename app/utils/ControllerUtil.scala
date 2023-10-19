package utils

import play.api.mvc.{Result, Results}
import util.ControllerJson

trait ControllerUtil extends ControllerJson {
  val optionNoContent: Option[_] => Result = {
    case Some(_) => Results.NoContent
    case None => Results.NotFound
  }

}
