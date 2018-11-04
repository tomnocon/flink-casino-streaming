package com.tomnocon.cs.sink

import java.util.concurrent.TimeUnit

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}

import scala.collection.JavaConverters.mapAsJavaMapConverter

class InfluxDbSink extends RichSinkFunction[InfluxDbPoint] {

  @transient
  private var influxDB: InfluxDB = _
  private var databaseName: String = _

  override def open(configuration: Configuration): Unit = {
    val parameters = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[ParameterTool]
    databaseName = parameters.get("influx.db", "casino")
    influxDB = InfluxDBFactory.connect(
      parameters.get("influx.url", "http://localhost:8086"),
      parameters.get("influx.user", "admin"),
      parameters.get("influx.password", "admin"))

    influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS)
  }

  override def close(): Unit = {
    influxDB.close()
  }

  override def invoke(dataPoint: InfluxDbPoint): Unit = {
    val point: Point = Point.measurement(dataPoint.measurement)
      .fields(dataPoint.fields.asJava)
      .tag(dataPoint.tags.asJava)
      .time(dataPoint.timestamp, TimeUnit.MILLISECONDS)
      .build()

    influxDB.write(databaseName, "autogen", point)
  }
}