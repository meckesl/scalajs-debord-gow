package org.scalatra.example.atmosphere

import org.scalatra.test.specs2._

class ChatControllerSpec extends ScalatraSpec { def is = s2"""
  GET / on NotificationsController
    should return status 200 $root
"""

  addServlet(classOf[ChatController], "/*")

  def root = get("/") {
    status must_== 200
  }
}

// TODO Add test code using webSocket
// Scalatra 2.6.x test libraries do not support WebSocket-compatible tests
