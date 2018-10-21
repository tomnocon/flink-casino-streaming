package com.tomnocon.cs.model

import java.io.{ByteArrayOutputStream, IOException}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Utils {

  private val objectMapper = new ObjectMapper
  objectMapper.registerModule(new DefaultScalaModule)

  def serializeToJson(instance: Any): Array[Byte] = {
    val result = new ByteArrayOutputStream
    try {
      objectMapper.writeValue(result, instance)
      result.toByteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  def deserializeJson[T](json: Array[Byte], clazz: Class[T]): T = {
    try {
      objectMapper.readValue(json, clazz)
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }
}
