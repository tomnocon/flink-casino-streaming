package com.tomnocon.cs.model

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema

class MachineEventDeserializer extends AbstractDeserializationSchema[MachineEvent] {

  override def deserialize(message: Array[Byte]): MachineEvent = {
    Utils.deserializeJson(message, classOf[MachineEvent])
  }
}
