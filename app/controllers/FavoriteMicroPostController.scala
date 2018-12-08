package controllers

import java.time.ZonedDateTime

import javax.inject._
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.{FavoriteMicroPost, Page}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{FavoriteMicroPostService, UserService, MicroPostService}
import skinny.Pagination

@Singleton
class FavoriteMicroPostController @Inject()(
  val favoriteMicroPostService: FavoriteMicroPostService,
  val userService: UserService,
  val microPostService: MicroPostService,
  components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {

      def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
        val userOpt: User = loggedIn
        favoriteMicroPostService.findFavoriteMicroPostsByUserId(
          Pagination(pageSize = Page.DefaultSize, pageNo = page),
          userOpt.id.get
          ).map { favoriteMicroPosts =>
              Ok(views.html.favorite_micro_post.index(Some(userOpt), favoriteMicroPosts))
            }
            .recover {
              case e: Exception =>
                Logger.error(s"Occurred Error", e)
                Redirect(routes.UsersController.index())
                  .flashing("failure" -> Messages("Internal Error"))
            }
            .getOrElse(InternalServerError(Messages("Internal Error")))
        }

      def favorite(microPostId: Long): Action[AnyContent] = StackAction { implicit request =>
        val currentUserId      = loggedIn.id.get
        val now         = ZonedDateTime.now()
        val favoriteMicroPost  = FavoriteMicroPost(None, currentUserId, microPostId, now, now)
        favoriteMicroPostService
          .create(favoriteMicroPost)
          .map { _ =>
            Redirect(routes.HomeController.index())
          }
          .recover {
            case e: Exception =>
              Logger.error("occurred error", e)
              Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
          }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }

      def unFavorite(favoriteMicroPostId: Long): Action[AnyContent] = StackAction { implicit request =>
        val currentUserId = loggedIn.id.get
        favoriteMicroPostService
          .deleteBy(currentUserId, favoriteMicroPostId)
          .map { _ =>
            Redirect(routes.HomeController.index())
          }
          .recover {
            case e: Exception =>
              Logger.error("occurred error", e)
              Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
          }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }
}