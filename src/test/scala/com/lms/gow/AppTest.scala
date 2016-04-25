package com.lms.gow

import utest.framework.TestSuite

object AppTest {

  // Initialize App
  App.main()

  def tests = TestSuite {
    'HelloWorld {
      assert(true == true)
    }
  }

}
