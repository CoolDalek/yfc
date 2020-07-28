import com.google.inject.AbstractModule
import daos._
import daos.mongo._
import daos.slick._
import monix.execution.Scheduler
import play.modules.reactivemongo.{DefaultReactiveMongoApi, ReactiveMongoApi}
import services._
import services.impl._

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Scheduler]).toInstance(Scheduler.Implicits.global)

    bind(classOf[AuthService]).to(classOf[AuthServiceImpl])
    bind(classOf[MailerService]).to(classOf[MailerServiceImpl])
    bind(classOf[PostService]).to(classOf[PostServiceImpl])

    bind(classOf[PostDAO]).to(classOf[PostDAOImpl])
    bind(classOf[TokenDAO]).to(classOf[TokenDAOImpl])
    bind(classOf[UserDAO]).to(classOf[UserDAOImpl])

  }

}
