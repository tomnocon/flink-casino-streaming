package com.tomnocon.cs.model

import com.tomnocon.cs.sink.InfluxDbPoint

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
            timestamp = base.timestamp)
        case MachineEventType.Bet =>
          MachineIncome(
            machineId = base.machineId,
            siteId = base.siteId,
            gameId = base.gameId,
            value = base.value,
            timestamp = base.timestamp)
        case _ => throw new IllegalArgumentException(s"This is not machine income event: $base")
      }
    }

    def toMachineFraudAlert(message: String, timestamp: Long): MachineFraudAlert = {
      MachineFraudAlert(
        message = message,
        machineId = base.machineId,
        siteId = base.siteId,
        timestamp = timestamp
      )
    }
  }

  implicit class RichMachineProfit(base: MachineIncome) {
    def sum(second: MachineIncome): MachineIncome = {
      MachineIncome(
        machineId = base.machineId,
        siteId = base.siteId,
        gameId = base.gameId,
        value = base.value + second.value,
        timestamp = second.timestamp)
    }

    def toInfluxDbPoint(measurement: String): InfluxDbPoint = {
      InfluxDbPoint(
        measurement = measurement,
        timestamp = base.timestamp,
        tags = Map("gameId" -> base.gameId, "siteId" -> base.siteId, "machineId" -> base.machineId),
        fields = Map("value" -> base.value.asInstanceOf[AnyRef]))
    }
  }

}
