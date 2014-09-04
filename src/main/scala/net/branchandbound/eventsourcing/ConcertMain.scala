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
import akka.persistence.Update

object ConcertMain extends App {

  import ConcertActor._
  import ConcertHistoryView._

  val system = ActorSystem("concert-es")
  val concertActor = system.actorOf(ConcertActor.props("concert1"), "concert1Actor")
  val concertHistoryActor = system.actorOf(ConcertHistoryView.props("concert1"), "concert1HistoryView")

  concertActor ! CreateConcert(50, 100, new Date)
  concertActor ! BuyTickets("me", 50)

  implicit val timeout = Timeout(5 seconds)
  
  val salesRecordsFuture = concertActor ? GetSalesRecords
  val salesRecords = Await.result(salesRecordsFuture, timeout.duration)
  println(s"Answer from ConcertActor: $salesRecords")
  
  concertActor ! ChangePrice(75)
  concertHistoryActor ! Update(await = true)
  val historyFuture = concertHistoryActor ? GetConcertHistory
  val history = Await.result(historyFuture, timeout.duration)
  println(s"Answer from ConcertHistoryView: $history")
  
  system.shutdown()
}