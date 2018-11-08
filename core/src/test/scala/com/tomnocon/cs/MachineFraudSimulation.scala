package com.tomnocon.cs

import java.util.Properties
import java.util.concurrent.Executors

import com.tomnocon.cs.model.MachineEvent
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object MachineFraudSimulation {

  def main(args: Array[String]): Unit = {

    val gate = new Gate

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val producer = new KafkaProducer[String, MachineEvent](props, new StringSerializer, new MachineEventSerializer)

    def machineListener(event: MachineEvent): Unit = {
      producer.send(new ProducerRecord[String, MachineEvent]("machine", event), new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          System.out.println(s"Sent message: $event")
          if (exception != null) {
            exception.printStackTrace()
          }
        }
      })
    }

    val machine = new MachineSimulator(machine = Machine.GD0001, site = Site.TheCalypso) {
      listen(machineListener)
    }
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    Future.apply(machine.deposit(1000))
    gate.await(500.milli)
    Future.apply(machine.deposit(1000))
  }
}
