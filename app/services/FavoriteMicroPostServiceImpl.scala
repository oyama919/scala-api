package services

import javax.inject.Singleton

import models.{FavoriteMicroPost, MicroPost, PagedItems}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class FavoriteMicroPostServiceImpl extends FavoriteMicroPostService {

  override def create(favoriteMicroPost: FavoriteMicroPost)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.create(favoriteMicroPost)
  }

  override def findById(id: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]] = Try {
    FavoriteMicroPost.where('id -> id).apply().headOption
  }

  override def findByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]] = Try {
    FavoriteMicroPost.where('userId -> userId).apply()
  }

  override def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]] = Try {
    FavoriteMicroPost.where('microPostId -> microPostId).apply().headOption
  }

  override def findMicroPostsByUserId(pagination: Pagination, microPostId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]] = {
    countByUserId(microPostId).map { size =>
      PagedItems(pagination, size,
        FavoriteMicroPost.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(FavoriteMicroPost.defaultAlias.microPostId, microPostId),
            pagination.limit,
            pagination.offset,
            Seq(FavoriteMicroPost.defaultAlias.id.desc)
          )
          .map(_.microPost.get)
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.allAssociations.countBy(sqls.eq(FavoriteMicroPost.defaultAlias.userId, userId))
  }

  override def deleteBy(userId: Long, microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int] = Try {
    val c     = FavoriteMicroPost.column
    val count = FavoriteMicroPost.countBy(sqls.eq(c.userId, userId).and.eq(c.microPostId, microPostId))
    if (count == 1) {
      FavoriteMicroPost.deleteBy(
        sqls
          .eq(c.userId, userId)
          .and(sqls.eq(c.microPostId, microPostId))
      )
    } else 0
  }

}
