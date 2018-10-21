package com.tomnocon.cs

import scala.concurrent.duration._

class Gate {

  private var _isOpen = false

  def isOpen: Boolean ={
    _isOpen
  }

  def open(): Unit = synchronized {
    _isOpen = true
    notifyAll()
  }

  def await(timeout: Duration = Duration.Inf) : Boolean = synchronized {
    val start = now
    var remaining = timeout
    var isBraked = false
    while (!_isOpen && !isBraked) {
      if(remaining == Duration.Zero){
        isBraked = true
      } else if(remaining > Duration.Zero && remaining.isFinite()){
        wait(remaining.toMillis)
        remaining = timeout - now - start
        if(remaining <= Duration.Zero){
          isBraked = true
        }
      } else {
        wait()
      }
    }
    _isOpen
  }

  private def now = System.currentTimeMillis.millis
}