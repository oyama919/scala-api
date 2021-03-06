package controllers

import javax.inject.{Inject, Singleton}
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.{Page, User}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{FavoriteMicroPostService, MicroPostService, UserFollowService, UserService}
import skinny.Pagination


@Singleton
class UsersController @Inject()(val userService: UserService,
  val microPostService: MicroPostService,
  val userFollowService: UserFollowService,
  val favoriteMicroPostService: FavoriteMicroPostService,
  components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {
      def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
        val userOpt: User = loggedIn
        val favoriteMicroPost = favoriteMicroPostService.findByUserId(userOpt.id.get).get
        userService.findAll(Pagination(pageSize = Page.DefaultSize, pageNo = page))
          .map { users =>
            Ok(views.html.users.index(loggedIn, users, favoriteMicroPost))
          }
          .recover {
            case e: Exception =>
              Logger.error(s"Occurred Error", e)
              Redirect(routes.UsersController.index())
                .flashing("failure" -> Messages("Internal Error"))
          }
          .getOrElse(InternalServerError(Messages("Internal Error")))
        }

      def show(userId: Long, page: Int) = StackAction { implicit request =>
        val triedUserOpt        = userService.findById(userId)
        val triedUserFollows    = userFollowService.findById(loggedIn.id.get)
        val pagination          = Pagination(10, page)
        val triedMicroPosts     = microPostService.findByUserId(pagination, userId)
        val triedFollowingsSize = userFollowService.countByUserId(userId)
        val triedFollowersSize  = userFollowService.countByFollowId(userId)
        val triedFavoriteMicroPosts  = favoriteMicroPostService.findByUserId(loggedIn.id.get)
        (for {
          userOpt        <- triedUserOpt
          userFollows    <- triedUserFollows
          microPosts     <- triedMicroPosts
          followingsSize <- triedFollowingsSize
          followersSize  <- triedFollowersSize
          favoriteMicroPosts  <- triedFavoriteMicroPosts
        } yield {
          userOpt.map { user =>
            Ok(views.html.users.show(loggedIn, user, userFollows, microPosts, followingsSize, followersSize, favoriteMicroPosts))
          }.get
        }).recover {
          case e: Exception =>
            Logger.error(s"occurred error", e)
            Redirect(routes.UsersController.index())
              .flashing("failure" -> Messages("InternalError"))
        }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }

      def getFollowers(userId: Long, page: Int) = StackAction { implicit request =>
        val targetUser           = User.findById(userId).get
        val triedMaybeUserFollow = userFollowService.findById(loggedIn.id.get)
        val pagination           = Pagination(10, page)
        val triedFollowers       = userFollowService.findFollowersByUserId(pagination, userId)
        val triedMicroPostsSize  = microPostService.countBy(userId)
        val triedFollowingsSize  = userFollowService.countByUserId(userId)
        val triedFavoriteMicroPosts   = favoriteMicroPostService.findByUserId(loggedIn.id.get)
        (for {
          userFollows    <- triedMaybeUserFollow
          followers      <- triedFollowers
          microPostSize  <- triedMicroPostsSize
          followingsSize <- triedFollowingsSize
          favoriteMicroPosts <- triedFavoriteMicroPosts
        } yield {
          Ok(
            views.html.users.followings(
              loggedIn,
              targetUser,
              userFollows,
              followers,
              microPostSize,
              followingsSize,
              favoriteMicroPosts
            )
          )
        }).recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.UsersController.index())
              .flashing("failure" -> Messages("InternalError"))
        }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }

      def getFollowings(userId: Long, page: Int) = StackAction { implicit request =>
        val targetUser          = User.findById(userId).get
        val triedUserFollows    = userFollowService.findById(loggedIn.id.get)
        val pagination          = Pagination(10, page)
        val triedFollowings     = userFollowService.findFollowingsByUserId(pagination, userId)
        val triedMicroPostsSize = microPostService.countBy(userId)
        val triedFollowersSize  = userFollowService.countByFollowId(userId)
        val triedFavoriteMicroPosts   = favoriteMicroPostService.findByUserId(loggedIn.id.get)
        (for {
          userFollows    <- triedUserFollows
          followings     <- triedFollowings
          microPostsSize <- triedMicroPostsSize
          followersSize  <- triedFollowersSize
          favoriteMicroPosts <- triedFavoriteMicroPosts
        } yield {
          Ok(
            views.html.users.followings(
              loggedIn,
              targetUser,
              userFollows,
              followings,
              microPostsSize,
              followersSize,
              favoriteMicroPosts
            )
          )
        }).recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.UsersController.index())
              .flashing("failure" -> Messages("InternalError"))
        }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }
    }
