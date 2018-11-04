package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.model.Helpers._
import com.tomnocon.cs.model.{MachineEvent, MachineEventDeserializer, MachineEventType, MachineProfit}
import com.tomnocon.cs.sink.{InfluxDbPoint, InfluxDbSink}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
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

    // Map to machine profit
    val machineProfitSource = source
      .filter(machineEvent => Array(MachineEventType.Win, MachineEventType.Bet).contains(machineEvent.`type`))
      .map[MachineProfit]((machineEvent: MachineEvent) => machineEvent.toMachineProfit)

    // Game profit pipeline
    machineProfitSource
      .keyBy(_.gameId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineProfit) => machineProfit.toInfluxDbPoint("game_profit"))
      .addSink(new InfluxDbSink)
      .name("Game Profit")

    // Site profit pipeline
    machineProfitSource
      .keyBy(_.siteId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineProfit) => machineProfit.toInfluxDbPoint("site_profit"))
      .addSink(new InfluxDbSink)
      .name("Site Profit")

    // Machine profit pipeline
    machineProfitSource
      .keyBy(_.machineId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineProfit) => machineProfit.toInfluxDbPoint("machine_profit"))
      .addSink(new InfluxDbSink)
      .name("Machine Profit")


    //gameProfit
    //.print()
    //println(env.getExecutionPlan)
    //sensorStream.print()

    env.execute("Casino Streaming")
  }
}
