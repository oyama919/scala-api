package services

import models.{FavoriteMicroPost, MicroPost, PagedItems}
import scalikejdbc.{AutoSession, DBSession}
import skinny.Pagination

import scala.util.Try

trait FavoriteMicroPostService {

  def create(favoriteMicroPost: FavoriteMicroPost)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findById(id: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]]

  def findByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]]

  def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]]

  def findMicroPostsByUserId(pagination: Pagination, microPostId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def deleteBy(userId: Long, favoriteMicroPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int]

}
