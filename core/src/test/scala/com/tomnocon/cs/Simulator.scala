package com.tomnocon.cs

import java.util.Properties

import com.tomnocon.cs.model.{MachineEvent, MachineEventType}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import scala.concurrent.duration._

object Simulator {

  private val gate = new Gate()

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val producer = new KafkaProducer[String, MachineEvent](props, new StringSerializer, new MachineEventSerializer)
    producer.send(new ProducerRecord[String, MachineEvent]("topic", MachineEvent(`type` = MachineEventType.Bet, credit = 4000, value = 22, timestamp = now, gameId = Games.BookOfRa.toString, machineId = "GD0001", siteId = Sites.TheCalypso.toString)),  new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if(exception != null) {
          exception.printStackTrace()
        }
      }
    })

    gate.await(100.seconds)

  }

  private def now = System.currentTimeMillis
}
