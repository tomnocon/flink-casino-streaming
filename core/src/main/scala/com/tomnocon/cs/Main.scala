package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.model.{GameProfit, MachineEvent, MachineEventDeserializer, MachineEventType}
import com.tomnocon.cs.sink.{InfluxDbPoint, InfluxDbSink}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object Main {
  def main(args: Array[String]): Unit = {
    val kafkaProps = new Properties()
    kafkaProps.setProperty("bootstrap.servers", "localhost:9092")

    val parameters = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(parameters)
    env.enableCheckpointing(1000)
    env.setParallelism(1)

    val source = env
      .addSource(new FlinkKafkaConsumer011("topic", new MachineEventDeserializer, kafkaProps))

    // Sum sensors data in window period
    val gameProfit = source
      .filter(machineEvent => Array(MachineEventType.Win, MachineEventType.Bet).contains(machineEvent.`type`))
      .map[GameProfit]((machineEvent: MachineEvent) => {
      if (machineEvent.`type` == MachineEventType.Win) {
        GameProfit(gameId = machineEvent.gameId, value = -machineEvent.value, timestamp = machineEvent.timestamp)
      } else {
        GameProfit(gameId = machineEvent.gameId, value = machineEvent.value, timestamp = machineEvent.timestamp)
      }
    })
      .keyBy(_.gameId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => GameProfit(gameId = p1.gameId, value = p1.value + p2.value, p2.timestamp) }
      .map[InfluxDbPoint]((gameProfit: GameProfit) => {
      InfluxDbPoint(measurement = "game_profit", timestamp = gameProfit.timestamp, tags = Map("gameId" -> gameProfit.gameId), fields = Map("value" -> gameProfit.value.asInstanceOf[AnyRef]))
    })
      .addSink(new InfluxDbSink)
      .name("sum(p1, p2)")

    //gameProfit
    //.print()
    //println(env.getExecutionPlan)
    //sensorStream.print()

    env.execute("Casino Streaming")
  }
}
