package services

import com.google.inject.Inject
import models.Artist.ArtistMap
import models.{Artist, SessionTattoo}
import models.Artist
import models.Category.CategoryMap

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ReportService @Inject() (
    dbService: DBService,
    artistAppointmentService: ArtistAppointmentService,
    artistService: ArtistService,
    artistCategoryService: ArtistCategoryService,
    appointmentService: AppointmentService,
    categoryService: CategoryService
)(implicit
    ec: ExecutionContext
) {

  import dbService._
  import dbService.api._

  def topArtistByNumberOfSessions(startDate: LocalDateTime, endDate:LocalDateTime): Future[ArtistMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithSessions <-
        Future.sequence(artists.map(artist =>
          artistAppointmentService.listSessionsByArtistIdInATimeRange(artist.id, startDate, endDate)
            .map(sessions => (artist, sessions))))
      countSessionByArtist = artistsWithSessions.map(artistWithSession => (artistWithSession._1, artistWithSession._2.size)).toMap
      artistNameSession = countSessionByArtist.map(artistSession => (artistSession._1.name, artistSession._2))
    } yield ArtistMap(artistNameSession)
  }

  def topArtistByWorkedHours(startDate:LocalDateTime, endDate:LocalDateTime): Future[ArtistMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithHours <-
        Future.sequence(artists.map(artist =>
          artistAppointmentService.listSessionsByArtistIdInATimeRange(artist.id, startDate, endDate)
            .map(session => (artist, session.map(_.estimated_time).sum)))
        )
      countArtistHourMap = artistsWithHours.map(artistsWithHours => (artistsWithHours._1.name, artistsWithHours._2)).toMap
    } yield ArtistMap(countArtistHourMap)
  }

  def totalSales(startDate:LocalDateTime, endDate:LocalDateTime): Future[ArtistMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithSales <-
        Future.sequence(
          artists.map { artist =>
            artistAppointmentService.listSessionsByArtistIdInATimeRange(artist.id, startDate, endDate)
              .map { sessions => (artist, sessions.map(_.price).sum)
              }
          }
        )
      artistSalesMap = artistsWithSales.map { case (artist, sales) => (artist.name, sales) }.toMap
    } yield ArtistMap(artistSalesMap)
  }

  /**def topCategoriesByMonth(): Future[CategoryMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithAppointments <-
        Future.sequence(
          artists.map(artist => appointmentService.listAppointmentsByArtist(artist.id).map(appointments => (artist, appointments)))
        )
      categoriesByArtist = artistsWithAppointments.map { case (artist, appointments) => (artist.name, appointments.map(_.category_id)) }.toMap
      categoriesIds = categoriesByArtist.values.flatten
      categoriesGroupedByNumber = categoriesIds.groupBy(uuid => categoriesIds.count(_ == uuid)).map(x => (x._1, x._2.head))

      orderedRepetitionsCountMap = categoriesGroupedByNumber.toSeq.sortBy(-_._1).toMap
      orderedSequence: Seq[(Int, UUID)] = orderedRepetitionsCountMap.toSeq
      sortedSequence: Seq[(Int, UUID)] = orderedSequence.sortBy(-_._1)


      sortedRepetitionsCountMap: Map[Int, UUID] = sortedSequence.toMap

    } yield CategoryMap(sortedRepetitionsCountMap)
  }**/

  def topCategories(): Future[CategoryMap] = {
    for {
      appointments <- appointmentService.listAppointments
      categories <- categoryService.listCategories
      artistCategories <- artistCategoryService.listArtistCategory
      artistCategoriesIds = appointments.map(_.category_id)
      categoriesIds = artistCategoriesIds.map(id => artistCategories.find(_.id == id).get.category_id)
      categoriesCountMap = categories.map(category =>{
        val categoryName = category.name
        val categoryCount = categoriesIds.count(_ == category.id)
        (categoryName, categoryCount)
        }).toMap
    } yield CategoryMap(categoriesCountMap)
  }


}
