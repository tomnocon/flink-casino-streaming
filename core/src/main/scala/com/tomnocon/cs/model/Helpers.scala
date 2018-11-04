package com.tomnocon.cs.model

import com.tomnocon.cs.sink.InfluxDbPoint

object Helpers {
  implicit class RichMachineEvent(base: MachineEvent){
    def toMachineProfit : MachineProfit = {
      if (base.`type` == MachineEventType.Win) {
        MachineProfit(
          machineId = base.machineId,
          siteId = base.siteId,
          gameId = base.gameId,
          value = -base.value,
          timestamp = base.timestamp)
      } else {
        MachineProfit(
          machineId = base.machineId,
          siteId = base.siteId,
          gameId = base.gameId,
          value = base.value,
          timestamp = base.timestamp)
      }
    }
  }

  implicit class RichMachineProfit(base: MachineProfit){
    def sum(second: MachineProfit) : MachineProfit = {
      MachineProfit(
        machineId = base.machineId,
        siteId = base.siteId,
        gameId = base.gameId,
        value = base.value + second.value,
        timestamp = second.timestamp)
    }

    def toInfluxDbPoint(measurement: String) : InfluxDbPoint = {
      InfluxDbPoint(
        measurement = measurement,
        timestamp = base.timestamp,
        tags = Map("gameId" -> base.gameId, "siteId" -> base.siteId, "machineId" -> base.machineId),
        fields = Map("value" -> base.value.asInstanceOf[AnyRef]))
    }
  }
}
