package com.lms.gow

import com.lms.gow.App
import utest._

object AppTest extends TestSuite {

  import App._

  def tests = TestSuite {
    'ScalaJSExample {
      assert(square(0) == 0)
      assert(square(4) == 16)
      assert(square(-5) == 25)
    }
  }
}
