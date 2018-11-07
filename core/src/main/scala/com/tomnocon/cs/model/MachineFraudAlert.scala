package com.tomnocon.cs.model

case class MachineFraudAlert(message: String, machineId: String, siteId: String, timestamp: Long){

  override def toString: String = s"MachineFraudAlert{message=$message machineId=$machineId, siteId=$siteId, timestamp=$timestamp}"
}
