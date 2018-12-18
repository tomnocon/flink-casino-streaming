package com.tomnocon.cs.model

import org.joda.time.DateTime

case class MachineFraudAlert(message: String, machineId: String, siteId: String, timestamp: DateTime){

  override def toString: String = s"MachineFraudAlert{message=$message, machineId=$machineId, siteId=$siteId, timestamp=$timestamp}"
}
