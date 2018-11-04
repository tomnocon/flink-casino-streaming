package com.tomnocon.cs

import java.util.Properties
import java.util.concurrent.{Executors, TimeUnit}

import com.tomnocon.cs.model.MachineEvent
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Random

object Entrypoint {

  val random = new Random()

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val producer = new KafkaProducer[String, MachineEvent](props, new StringSerializer, new MachineEventSerializer)

    def machineListener(event: MachineEvent): Unit = {
      producer.send(new ProducerRecord[String, MachineEvent]("topic", null, event.timestamp, null, event), new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          System.out.println("Sent message: " + event.toString)
          if (exception != null) {
            exception.printStackTrace()
          }
        }
      })
    }

    val machines = Array(
      new MachineSimulator(machine = Machine.GD0001, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0002, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0003, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0004, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0005, site = Site.TheChariot) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0006, site = Site.TheChariot) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0007, site = Site.TheChariot) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.GD0008, site = Site.TheChariot) {
        listen(machineListener)
      }
    )

    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(machines.length))

    val tasks = for (i <- 0 to machines.length) yield Future {
      val gate = new Gate
      while (true) {
        gate.await(FiniteDuration.apply(random.nextInt(10), TimeUnit.SECONDS)) // wait around 10 sec in each step
        val machine = machines.apply(i)
        action(machine) // perform some action on machine
      }
    }

    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration.Inf)
  }

  def action(machine: MachineSimulator): Unit = {
    if (machine.credit > 0) {
      val betAmount = machine.credit % random.nextInt(97) + 1 // random bet [1,97]
      machine.bet(betAmount)
      val probability = random.nextInt(100)
      if (probability > 84) { // 15% chance to win
        machine.win(betAmount * (random.nextInt(10) + 1)) // multiply bet by random number
      }
      if (probability < 11) { // 10% chance to withdraw cash from machine
        machine.withdraw()
      }
      if (probability < 21) { // 20% chance to change game
        machine.runGame(Game.apply(random.nextInt(Game.maxId)))
      }
    } else {
      machine.deposit(random.nextInt(100000)) // if not enough founds, charge machine
    }
  }

}
