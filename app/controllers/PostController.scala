package controllers

import java.util.UUID

import controllers.filters.ControllerUtils
import javax.inject.{Inject, Singleton}
import models.dto.PostDTO
import monix.execution.Scheduler
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PostService

import scala.concurrent.ExecutionContext

@Singleton
class PostController @Inject()(cc: ControllerComponents, postService: PostService)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc) {

  def create: Action[AnyContent] = userActionWithForm(PostDTO.form) { request =>
    postService.create(request.userId, request.parsedBody).map(_ => Ok)
  }

  def getAll: Action[AnyContent] = userAction { request =>
    postService.getAll(request.userId).map {posts =>
      Ok(JsArray(posts.map(_.toJson)))
    }
  }

  def getById(postId: UUID): Action[AnyContent] = userAction {request =>
    postService.getById(postId, request.userId).map(_ => Ok)
  }

  def update(postId: UUID): Action[AnyContent] = userActionWithForm(PostDTO.form) {request =>
    postService.update(postId, request.userId, request.parsedBody).map(_ => Ok)
  }

  def delete(postId: UUID): Action[AnyContent] = userAction {request =>
    postService.delete(postId, request.userId).map(_ => Ok)
  }

}
