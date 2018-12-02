package models

import java.time.ZonedDateTime

import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class FavoriteMicroPost(
  id: Option[Long],
  userId: Long,
  microPostId: Long,
  createAt: ZonedDateTime = ZonedDateTime.now(),
  updateAt: ZonedDateTime = ZonedDateTime.now(),
  user: Option[User] = None,
  microPost: Option[MicroPost] = None
)

object FavoriteMicroPost extends SkinnyCRUDMapper[FavoriteMicroPost] {

  lazy val user = User.createAlias("user")

  lazy val userRef = belongsToWithAliasAndFkAndJoinCondition[User](
    right = User -> user,
    fk = "userId",
    on = sqls.eq(defaultAlias.userId, user.id), // DB join
    merge = (favoriteMicroPost, getUser) => favoriteMicroPost.copy(user = getUser) // クラスに取得データをコピー
  )

  lazy val microPost = MicroPost.createAlias("microPost")

  lazy val microPostRef = belongsToWithAliasAndFkAndJoinCondition[MicroPost](
    right = MicroPost -> microPost,
    fk = "microPostId",
    on = sqls.eq(defaultAlias.microPostId, microPost.id), // DB join
    merge = (favoriteMicroPost, getMicroPost) => favoriteMicroPost.copy(microPost = getMicroPost) // クラスに取得データをコピー
  )

  lazy val allAssociations: CRUDFeatureWithId[Long, FavoriteMicroPost] = joins(userRef, microPostRef)

  override def tableName = "favorite_micro_posts"

  override def defaultAlias: Alias[FavoriteMicroPost] = createAlias("fm")

  override def extract(rs: WrappedResultSet, n: ResultName[FavoriteMicroPost]): FavoriteMicroPost =
    autoConstruct(rs, n, "user", "microPost")

  def create(favoriteMicroPost: FavoriteMicroPost)(implicit session: DBSession): Long =
    createWithAttributes(toNamedValues(favoriteMicroPost): _*)

  private def toNamedValues(record: FavoriteMicroPost): Seq[(Symbol, Any)] = Seq(
    'userId   -> record.userId,
    'microPostId -> record.microPostId,
    'createAt -> record.createAt,
    'updateAt -> record.updateAt
  )

  def update(favoriteMicroPost: FavoriteMicroPost)(implicit session: DBSession): Int =
    updateById(favoriteMicroPost.id.get).withAttributes(toNamedValues(favoriteMicroPost): _*)
}
