package services

import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class ArtistCategoryService @Inject() (dbService: DBService)(implicit ec: ExecutionContext){
  import dbService._
    import dbService.api._

  def listArtistCategory: Future[Seq[ArtistCategory]] = {
    ArtistCategoryTable.result
      .execute()
      .map(_.map(_.toArtistCategory))
  }

}
