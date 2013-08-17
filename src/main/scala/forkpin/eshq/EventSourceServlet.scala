package forkpin.eshq

import forkpin.ForkpinServlet
import EventSource._

class EventSourceServlet extends ForkpinServlet {

  post("/socket") {
    createSocket(params("channel"))
  }

  get("/test") {
    publish("forkpin", "all-done!")
  }

}
