package com.tomnocon.cs

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.utils.ParameterTool

object Main {
  def main(args: Array[String]): Unit = {
    val parameters = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(parameters)
    env.enableCheckpointing(1000)
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //println(env.getExecutionPlan)
    //sensorStream.print()

    env.execute("Casino Streaming")
  }
}
