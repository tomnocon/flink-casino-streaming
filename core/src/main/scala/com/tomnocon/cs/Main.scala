package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.model.MachineEventDeserializer
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.utils.ParameterTool
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

    source
      .print()
    //println(env.getExecutionPlan)
    //sensorStream.print()

    env.execute("Casino Streaming")
  }
}
