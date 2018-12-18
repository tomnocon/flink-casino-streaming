package com.tomnocon.cs.model

case class MachineIncome(machineId: String, value: Long, gameId: String, siteId: String, timestamp: Long, start: Long, end: Long) {

  override def toString: String = s"MachineIncome{machineId=$machineId, value=$value, gameId=$gameId, siteId=$siteId, timestamp=$timestamp, start=$start, end=$end}"
}
