package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.function.DepositFraudDetectionFunction
import com.tomnocon.cs.model.Helpers._
import com.tomnocon.cs.model._
import com.tomnocon.cs.sink.{CustomElasticsearchSinkFunction, ElasticsearchEvent}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.http.HttpHost

import scala.concurrent.duration._

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

    val kafkaConsumer = new FlinkKafkaConsumer011("machine", new MachineEventDeserializer, kafkaProps)
    kafkaConsumer.assignTimestampsAndWatermarks(new MachineEventWatermarkEmitter)

    val elasticsearchSink = createElasticsearchSink

    val source = env
      .addSource(kafkaConsumer)

    // Map to machine income
    source
      .filter(machineEvent => Array(MachineEventType.Win, MachineEventType.Bet).contains(machineEvent.`type`))
      .map[MachineIncome]((machineEvent: MachineEvent) => machineEvent.toMachineIncome)
      .keyBy(_.machineId)
      .timeWindow(Time.seconds(10))
      .reduce { (p1, p2) => p1.sum(p2) }
      .map[ElasticsearchEvent]((machineIncome: MachineIncome) => machineIncome.toElasticsearchEvent())
      .addSink(elasticsearchSink)
      .name("Machine Income")

    // Fraud detection pipeline
    source
      .filter(machineEvent => machineEvent.`type` == MachineEventType.Deposit)
      .keyBy(_.machineId)
      .countWindow(2, 1)
      .process(new DepositFraudDetectionFunction(1.second))
      .map[ElasticsearchEvent]((fraudAlert: MachineFraudAlert) => fraudAlert.toElasticsearchEvent())
      .addSink(elasticsearchSink)
      .name("Fraud Detection")

    env.execute("Casino Streaming")
  }

  def createElasticsearchSink: ElasticsearchSink[ElasticsearchEvent] = {
    val httpHosts = new java.util.ArrayList[HttpHost]
    httpHosts.add(new HttpHost("localhost", 9200, "http"))

    val esSinkBuilder = new ElasticsearchSink.Builder[ElasticsearchEvent](httpHosts, new CustomElasticsearchSinkFunction)
    esSinkBuilder.setBulkFlushInterval(1000)
    esSinkBuilder.build()
  }
}
