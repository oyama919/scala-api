package services

import javax.inject.Singleton

import models.User
import scalikejdbc.{ AutoSession, DBSession }

import scala.util.Try

@Singleton
class UserServiceImpl extends UserService {

  /** scala.util.Try
    *   success: scala.util.Success
    *   error: scala.util.Failure
    */
  // success: Long AUTO_INCREMENTによるID値  error: Failure
  def create(user: User)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    User.create(user)
  }

  def findByEmail(email: String)(implicit dbSession: DBSession = AutoSession): Try[Option[User]] =
    Try {
      User.where('email -> email).apply().headOption
    }

  override def findAll(implicit dbSession: DBSession): Try[List[User]] = Try {
    User.findAll()
  }

  override def findById(id: Long)(implicit dbSession: DBSession): Try[Option[User]] = Try {
    User.findById(id)
  }

}
