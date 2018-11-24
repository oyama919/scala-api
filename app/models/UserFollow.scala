package models

import java.time.ZonedDateTime

import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class UserFollow(
  id: Option[Long],
  userId: Long,
  followId: Long,
  createAt: ZonedDateTime = ZonedDateTime.now(),
  updateAt: ZonedDateTime = ZonedDateTime.now(),
  user: Option[User] = None,
  followUser: Option[User] = None)

object UserFollow extends SkinnyCRUDMapper[UserFollow] {

  lazy val user = User.createAlias("user")

  lazy val userRef = belongsToWithAliasAndFkAndJoinCondition[User](
    right = User -> user,
    fk = "userId",
    on = sqls.eq(defaultAlias.userId, user.id), // DB join
    merge = (userFollow, getUser) => userFollow.copy(user = getUser) // クラスに取得データをコピー
  )

  lazy val follower = User.createAlias("follower")

  lazy val followRef = belongsToWithAliasAndFkAndJoinCondition[User](
    right = User -> follower,
    fk = "followId",
    on = sqls.eq(defaultAlias.followId, follower.id),
    merge = (userFollow, getFollower) => userFollow.copy(followUser = getFollower)
  )

  lazy val allAssociations: CRUDFeatureWithId[Long, UserFollow] = joins(userRef, followRef)

  override def tableName = "user_follows"

  override def defaultAlias: Alias[UserFollow] = createAlias("userFollow")

  override def extract(rs: WrappedResultSet, n: ResultName[UserFollow]): UserFollow =
    autoConstruct(rs, n, "user", "followUser")

  def create(userFollow: UserFollow)(implicit session: DBSession): Long =
    createWithAttributes(toNamedValues(userFollow): _*)

  private def toNamedValues(record: UserFollow): Seq[(Symbol, Any)] = Seq(
    'userId   -> record.userId,
    'followId -> record.followId,
    'createAt -> record.createAt,
    'updateAt -> record.updateAt
  )

  def update(userFollow: UserFollow)(implicit session: DBSession): Int =
    updateById(userFollow.id.get).withAttributes(toNamedValues(userFollow): _*)

}