package forkpin.web.eshq

import forkpin.web.ForkpinServlet
import com.github.synesso.eshq.{Channel, Key, Secret, EventSourceClient}
import forkpin.Config
import java.net.URL
import org.scalatra.FutureSupport
import scala.concurrent.ExecutionContext

class EventSourceServlet extends ForkpinServlet with Config with FutureSupport {
  val executor: ExecutionContext = ExecutionContext.global

  private val (key, secret, serviceURL) = (properties("ESHQ_KEY"), properties("ESHQ_SECRET"), properties("ESHQ_URL"))
  private val client = EventSourceClient(Key(key), Secret(secret), new URL(serviceURL))

  post("/socket") {
    client.open(Channel(params("channel")))
  }

  get("/test") {
    client.send(Channel("forkpin"), "make it funky da da da")
  }

}
