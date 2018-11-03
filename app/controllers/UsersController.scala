package controllers

import javax.inject._
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.Page
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.UserService
import skinny.Pagination

@Singleton
class UsersController @Inject()(val userService: UserService, components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {
      def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
        userService.findAll(Pagination(pageSize = Page.DefaultSize, pageNo = page))
          .map { users =>
            Ok(views.html.users.index(loggedIn, users))
          }
          .recover {
            case e: Exception =>
              Logger.error(s"Occurred Error", e)
              Redirect(routes.UsersController.index())
                .flashing("failure" -> Messages("Internal Error"))
          }
          .getOrElse(InternalServerError(Messages("Internal Error")))
      }

      def show(userId: Long): Action[AnyContent] = StackAction { implicit request =>
        userService
          .findById(userId)
          .map { userOpt =>
            userOpt.map { user =>
              Ok(views.html.users.show(loggedIn, user))
            }.get
          }
          .recover {
            case e: Exception =>
              Logger.error(s"Occurred Error", e)
              Redirect(routes.UsersController.index())
                .flashing("failure" -> Messages("Internal Error"))
          }
          .getOrElse(InternalServerError(Messages("Internal Error")))
      }
    }
