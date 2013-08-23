package forkpin.web.eshq

import EventSource._
import forkpin.web.ForkpinServlet

class EventSourceServlet extends ForkpinServlet {

  post("/socket") {
    createSocket(params("channel"))
  }

  get("/test") {
    publish("forkpin", "all-done!")
  }

}
