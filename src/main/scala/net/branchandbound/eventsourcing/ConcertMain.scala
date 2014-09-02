package net.branchandbound.eventsourcing

import java.util.Date

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import ConcertActor.BuyTickets
import ConcertActor.CreateConcert
import ConcertActor.GetSalesRecords
import ConcertActor.SalesRecord
import ConcertActor.props
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object ConcertMain extends App {

  import ConcertActor._

  val system = ActorSystem("concert-es")
  val concertActor = system.actorOf(props("concert1"))

  concertActor ! CreateConcert(50, 100, new Date)
  concertActor ! BuyTickets("me", 50)

  implicit val timeout = Timeout(5 seconds)
  val future = concertActor ? GetSalesRecords
  val result = Await.result(future, timeout.duration).asInstanceOf[Seq[SalesRecord]]
  println(result)
  
  system.shutdown()
}