package testUtils

import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait AsyncUtils {

  implicit class FutureSync[T](f: Future[T]) {
    def get: T = Await.result(f, 5.seconds)
  }

  implicit class TaskSync[T](t: Task[T]) {
    def get: T = t.runToFuture(Scheduler.global).get
  }

}
