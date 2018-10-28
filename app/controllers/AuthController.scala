package controllers

import javax.inject.{Inject, Singleton}

import com.github.t3hnar.bcrypt._
import forms.Login
import jp.t2v.lab.play2.auth.LoginLogout // ログイン・ログアウト用のトレイト
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
  val userService: UserService,
  components: ControllerComponents
)(implicit ec: ExecutionContext)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with LoginLogout {
      private val loginForm: Form[Login] = Form {
        /** mapping => verifying
          * 認証成功: ユーザー認証処理(authenticateメソッド)を呼び出し
          * 認証失敗: Messages("AuthFailed")がフォームのグローバルエラーとして返される
          */
        mapping(
          "email"    -> email,
          "password" -> nonEmptyText
        )(Login.apply)(Login.unapply)
          .verifying("AuthFailed", form => authenticate(form.email, form.password).isDefined)
      }
      // ログインセッション保存用フォーム
      private val rememberMeForm: Form[Boolean] = Form {
        "rememberme" -> boolean
      }
      def index: Action[AnyContent] = Action { implicit request =>
        Ok(
          views.html.auth.login(loginForm, rememberMeForm.fill(request.session.get("rememberme").exists("true" ==)))
        )
      }
      /** play2-auth login
        * ログイン失敗時
        *   Future.successful(Future)
        *   return Future
        * ログイン成功時
        *   markLoggedIn 戻り値 Result => Future[Result]
        *   Playのactionは通常、Result型の戻り値  Request[A] => Result
        *   Action.asyncとすると、Future[Result]型の戻り値となる  Request[A] => Future[Result]
        */
      def login: Action[AnyContent] = {
        Action.async { implicit request =>
          val rememberMe = rememberMeForm.bindFromRequest()
          loginForm.bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(views.html.auth.login(formWithErrors, rememberMe))),
            {
              login =>
                val req = request.addAttr(TypedKey[Boolean]("rememberme"), rememberMe.get)
                markLoggedIn(login.email)(req, ec) {
                  Redirect(routes.HomeController.index())
                    .withSession("rememberme" -> rememberMe.get.toString)
                    .flashing("success" -> Messages("LoggedIn"))
                }
            }
          )
        }
      }
      def logout: Action[AnyContent] = Action.async { implicit request =>
        markLoggedOut()(request, ec) {
          Redirect(routes.HomeController.index())
            .flashing("success" -> Messages("LoggedOut"))
            .removingFromSession("rememberme")
        }
      }
      private def authenticate(email: String, password: String): Option[User] = {
        userService
          .findByEmail(email)
          .map { user =>
            user.flatMap { u =>
              if (password.isBcrypted(u.password))
                user
              else
                None
            }
          }
          .get
      }
    }