package com.bancoblanco

/**
 * A TimeSerivce trait.
 */
trait TimeService {
  def now: Long
}

object SimpleTimeService {
  implicit val timeService = new SimpleTimeService
}

class SimpleTimeService extends TimeService {
  def now = compat.Platform.currentTime
}
