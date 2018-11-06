package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.model.Helpers._
import com.tomnocon.cs.model._
import com.tomnocon.cs.sink.{InfluxDbPoint, InfluxDbSink}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object Main {
  def main(args: Array[String]): Unit = {

    val kafkaProps = new Properties()
    kafkaProps.setProperty("bootstrap.servers", "localhost:9092")

    val parameters = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setGlobalJobParameters(parameters)
    env.enableCheckpointing(1000)
    env.setParallelism(1)

    val kafkaConsumer = new FlinkKafkaConsumer011("topic", new MachineEventDeserializer, kafkaProps)
    kafkaConsumer.setStartFromEarliest()
    kafkaConsumer.assignTimestampsAndWatermarks(new MachineEventWatermarkEmitter)

    val source = env
      .addSource(kafkaConsumer)

    // Map to machine income
    val machineIncomeSource = source
      .filter(machineEvent => Array(MachineEventType.Win, MachineEventType.Bet).contains(machineEvent.`type`))
      .map[MachineIncome]((machineEvent: MachineEvent) => machineEvent.toMachineIncome)

    // Game income pipeline
    machineIncomeSource
      .keyBy(_.gameId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineIncome) => machineProfit.toInfluxDbPoint("game_income"))
      .addSink(new InfluxDbSink)
      .name("Game Income")

    // Site income pipelines
    val siteIncomeSource = machineIncomeSource
      .keyBy(_.siteId)
      .timeWindow(Time.seconds(30))

    siteIncomeSource
      .maxBy("value")
      .map[InfluxDbPoint]((machineProfit: MachineIncome) => machineProfit.toInfluxDbPoint("site_max_income"))
      .addSink(new InfluxDbSink)
      .name("Site Max Income")

    siteIncomeSource
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineIncome) => machineProfit.toInfluxDbPoint("site_income"))
      .addSink(new InfluxDbSink)
      .name("Site Income")

    // Machine income pipeline
    machineIncomeSource
      .keyBy(_.machineId)
      .timeWindow(Time.seconds(30))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[InfluxDbPoint]((machineProfit: MachineIncome) => machineProfit.toInfluxDbPoint("machine_income"))
      .addSink(new InfluxDbSink)
      .name("Machine Income")


    //gameProfit
    //.print()
    //println(env.getExecutionPlan)
    //sensorStream.print()

    env.execute("Casino Streaming")
  }
}
