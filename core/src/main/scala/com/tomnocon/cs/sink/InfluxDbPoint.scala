package com.tomnocon.cs.sink

case class InfluxDbPoint(measurement: String, timestamp: Long, tags: Map[String, String] = Map.empty, fields: Map[String, AnyRef] = Map.empty) {

  override def toString: String = s"InfluxDbPoint{measurement=$measurement, timestamp=$timestamp, tags=$tags, fields=$fields}"
}
