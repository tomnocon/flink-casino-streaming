package com.tomnocon.cs

import java.util.Properties
import java.util.concurrent.{Executors, TimeUnit}

import com.tomnocon.cs.model.MachineEvent
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Random

object CasinoSimulation {

  val random = new Random()

  def main(args: Array[String]): Unit = {
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

    val machines = Array(
      new MachineSimulator(machine = Machine.M0001, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.M0002, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.M0003, site = Site.TheCalypso) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.M0004, site = Site.TheChariot) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.M0005, site = Site.TheChariot) {
        listen(machineListener)
      },
      new MachineSimulator(machine = Machine.M0006, site = Site.TheChariot) {
        listen(machineListener)
      }
    )

    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(machines.length))

    val tasks = for (i <- machines.dropRight(1).indices) yield Future {
      val gate = new Gate
      while (true) {
        gate.await(FiniteDuration.apply(random.nextInt(5) + 1, TimeUnit.SECONDS)) // wait around 5 sec in each step
        val machine = machines.apply(i)
        randomAction(machine) // perform some action on machine
      }
    }

    tasks :+ Future {
      val gate = new Gate
      while (true) {
        val machine = machines.last
        machine.deposit(1000) // fraud simulation - two quick deposits
        gate.await(500.milli)
        machine.deposit(1000)
        gate.await(40.seconds)
      }
    }

    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration.Inf)
  }

  def randomAction(machine: MachineSimulator): Unit = {
    if (machine.credit > 0) {
      val betAmount = machine.credit % (random.nextInt(97) + 1) // random bet [1,97]
      machine.bet(betAmount)
      val probability = random.nextInt(100) + 1
      if (probability > 85) { // 15% chance to win
        machine.win(betAmount * (random.nextInt(5) + 1)) // multiply bet by random number
      }
      if (probability < 10) { // 10% chance to withdraw cash from machine
        machine.withdraw()
      }
      if (probability < 20) { // 20% chance to change game
        machine.runGame(Game.apply(random.nextInt(Game.maxId)))
      }
    } else {
      machine.deposit(random.nextInt(100000) + 1) // if not enough founds, charge machine
    }
  }

}
