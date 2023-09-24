package util

import cats.data.EitherT
import cats.implicits._
import play.api.mvc.Result

import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}

object EitherF {

  type EitherF[T] = EitherT[Future, Result, T]

  def apply[T](either: Either[Result, T])(implicit ec: ExecutionContext): EitherF[T] = EitherT.fromEither(either)

  def apply[T](either: Future[Either[Result, T]])(implicit @unused ec: ExecutionContext): EitherF[T] = EitherT(either)

  def when[T](cond: Boolean)(right: => EitherF[T])(implicit ec: ExecutionContext): EitherF[Unit] = {
    if (cond) { right.map(_ => ()) }
    else { EitherF.right(()) }
  }

  def getOrElse[T](f: Future[Option[T]], result: => Result)(implicit ec: ExecutionContext): EitherF[T] = EitherT.fromOptionF(f, result)

  def getOrElse[T](o: Option[T], result: => Result): EitherF[T] = o match {
    case Some(v) => EitherF.right(v)
    case None => left(result)
  }

  def right[T](f: Future[T])(implicit ec: ExecutionContext): EitherF[T] = EitherT.liftF(f)

  def right[T](v: T): EitherF[T] = EitherT(Future.successful(Either.right(v)))

  def left[T](r: Result): EitherF[T] = EitherT(Future.successful(Either.left(r)))

  def require(condition: Future[Boolean], r: => Result)(implicit ec: ExecutionContext): EitherF[Unit] =
    right(condition).flatMap {
      case true => right(())
      case _ => left(r)
    }

  def require(condition: Boolean, r: => Result): EitherF[Unit] =
    if (condition) EitherT(Future.successful(Either.right(()))) else EitherT(Future.successful(Either.left(r)))

  def recover[T](f: Future[T])(pf: PartialFunction[Throwable, Result])(implicit ec: ExecutionContext): EitherF[T] =
    f.attemptT.leftFlatMap {
      case pf(result) => left(result)
      case error => right(Future.failed(error))
    }

  def recover[T](f: Future[T], result: => Result)(implicit ec: ExecutionContext): EitherF[T] =
    recover(f)(_ => result)

  def response(e: EitherF[Result])(implicit ec: ExecutionContext): Future[Result] =
    e.value.map {
      case Left(res) => res
      case Right(res) => res
    }
}
