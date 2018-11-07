package com.tomnocon.cs.function

import java.util.concurrent.TimeUnit

import com.tomnocon.cs.model.Helpers._
import com.tomnocon.cs.model.{MachineEvent, MachineFraudAlert}
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

import scala.concurrent.duration.{Duration, FiniteDuration}

class DepositFraudDetectionFunction(val minimumTimeDifference : Duration) extends ProcessWindowFunction[MachineEvent, MachineFraudAlert, String, GlobalWindow] {

  override def process(key: String, context: Context, elements: Iterable[MachineEvent], out: Collector[MachineFraudAlert]): Unit =
  {
    if(elements.last != elements.head){
      for (pair <- elements.sliding(2)) {
        val difference = FiniteDuration.apply(Math.abs(pair.last.timestamp - pair.head.timestamp), TimeUnit.MILLISECONDS)
        if(difference < minimumTimeDifference){
          out.collect(pair.last.toMachineFraudAlert(s"Minimum time difference between deposits has been exceeded. Difference: $difference", context.currentProcessingTime))
        }
      }
    }
  }
}