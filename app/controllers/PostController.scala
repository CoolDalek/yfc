package controllers

import java.util.UUID

import controllers.filters.ControllerUtils
import javax.inject.{Inject, Singleton}
import models.dto.PostDTO
import monix.eval.Task
import monix.execution.Scheduler
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.PostService

import scala.concurrent.ExecutionContext

@Singleton
class PostController @Inject()(cc: ControllerComponents, postService: PostService)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc) {

  def create: Action[AnyContent] = userActionWithForm(PostDTO.form) { request =>
    postService.create(request.userId, request.parsedBody).map(post => Ok(post.toJson))
  }

  def getAll: Action[AnyContent] = userAction { request =>
    postService.getAll(request.userId).map {posts =>
      Ok(JsArray(posts.map(_.toJson)))
    }
  }

  def getById(postId: String): Action[AnyContent] = userAction {request =>
    parseId(postId){ parsedId =>
      postService.getById(parsedId, request.userId).map(post => Ok(post.toJson))
    }
  }

  def update(postId: String): Action[AnyContent] = userActionWithForm(PostDTO.form) {request =>
    parseId(postId){ parsedId =>
      postService.update(parsedId, request.userId, request.parsedBody).map(post => Ok(post.toJson))
    }
  }

  def delete(postId: String): Action[AnyContent] = userAction {request =>
    parseId(postId){ parsedId =>
      postService.delete(parsedId, request.userId).map(_ => Ok)
    }
  }

  def parseId(postId: String)(block: UUID => Task[Result]): Task[Result] = {
    try {
      val parsedId = UUID.fromString(postId)
      block(parsedId)
    } catch {
      case e: IllegalArgumentException => Task.raiseError(e)
    }
  }

}
