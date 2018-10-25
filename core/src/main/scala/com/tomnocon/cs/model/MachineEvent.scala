package com.tomnocon.cs.model

import com.tomnocon.cs.model.MachineEventType.MachineEventType

case class MachineEvent (`type`: MachineEventType, credit: Long, value: Long, gameId: String, machineId: String, siteId: String, timestamp: Long) {

  override def toString: String = s"MachineEvent{type=${`type`}, credit=$credit, value=$value, gameId=$gameId, machineId=$machineId, siteId=$siteId, timestamp=$timestamp}"
}