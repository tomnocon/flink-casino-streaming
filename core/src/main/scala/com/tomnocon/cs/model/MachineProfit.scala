package com.tomnocon.cs.model

// machine income
case class MachineProfit(machineId: String, value: Long, gameId: String, siteId: String, timestamp: Long) {

  override def toString: String = s"MachineProfit{machineId=$machineId, value=$value, gameId=$gameId, siteId=$siteId, timestamp=$timestamp}"
}
