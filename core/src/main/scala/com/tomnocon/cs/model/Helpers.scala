package com.tomnocon.cs.model

import com.tomnocon.cs.sink.ElasticsearchEvent
import org.joda.time.DateTime

object Helpers {

  implicit class RichMachineEvent(base: MachineEvent) {
    def toMachineIncome: MachineIncome = {
      base.`type` match {
        case MachineEventType.Win =>
          MachineIncome(
            machineId = base.machineId,
            siteId = base.siteId,
            gameId = base.gameId,
            value = -base.value,
            timestamp = base.timestamp,
            start = base.timestamp,
            end = base.timestamp)
        case MachineEventType.Bet =>
          MachineIncome(
            machineId = base.machineId,
            siteId = base.siteId,
            gameId = base.gameId,
            value = base.value,
            timestamp = base.timestamp,
            start = base.timestamp,
            end = base.timestamp)
        case _ => throw new IllegalArgumentException(s"This is not machine income event: $base")
      }
    }

    def toMachineFraudAlert(message: String, timestamp: DateTime): MachineFraudAlert = {
      MachineFraudAlert(
        message = message,
        machineId = base.machineId,
        siteId = base.siteId,
        timestamp = timestamp
      )
    }
  }

  implicit class RichMachineIncome(base: MachineIncome) {
    def sum(second: MachineIncome): MachineIncome = {
      MachineIncome(
        machineId = base.machineId,
        siteId = base.siteId,
        gameId = base.gameId,
        value = base.value + second.value,
        timestamp = base.end,
        start = base.start,
        end = second.end)
    }

    def toElasticsearchEvent(index: String = "machine_income"): ElasticsearchEvent = {
      ElasticsearchEvent(id = f"${base.machineId}:${base.end}", index,
        data = Map(
          "value" -> base.value,
          "gameId" -> base.gameId,
          "siteId" -> base.siteId,
          "machineId" -> base.machineId,
          "timestamp" -> new DateTime(base.end),
          "start" -> new DateTime(base.start),
          "end" -> new DateTime(base.end)))
    }
  }

  implicit class RichMachineFraudAlert(base: MachineFraudAlert) {

    def toElasticsearchEvent(index: String = "machine_fraud"): ElasticsearchEvent = {
      ElasticsearchEvent(id = f"${base.machineId}:${base.timestamp}", index,
        data = Map(
          "message" -> base.message,
          "machineId" -> base.machineId,
          "siteId" -> base.siteId,
          "timestamp" -> new DateTime(base.timestamp)))
    }
  }

}
