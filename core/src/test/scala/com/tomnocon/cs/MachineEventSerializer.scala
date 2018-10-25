package com.tomnocon.cs

import java.util

import com.tomnocon.cs.model.{MachineEvent, Utils}
import org.apache.kafka.common.serialization.Serializer

class MachineEventSerializer extends Serializer[MachineEvent] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: MachineEvent): Array[Byte] = {
    Utils.serializeToJson(data)
  }

  override def close(): Unit = {}
}
