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

  val create = CreateConcert(50, 100, new Date)
  val buy = BuyTickets("me", 50)
  val change = ChangePrice(75)
  concertActor ! create
  println(s"------- ConcertActor -----> $create")
  concertActor ! buy
  println(s"------- ConcertActor -----> $buy")
  concertActor ! change
  println(s"------- ConcertActor -----> $change")
  
  // Retrieve and print the SalesRecords from ConcertActor
  implicit val timeout = Timeout(5 seconds)
  val salesRecordsFuture = concertActor ? GetSalesRecords
  println(s"------- ConcertActor -----> $GetSalesRecords")
  val salesRecords = Await.result(salesRecordsFuture, timeout.duration)
  println(s"<------ ConcertActor ------ $salesRecords")
  
  // Since PersistenView polls (eventual consistency) we force it 
  // to sync with the journal for our demo
  concertHistoryActor ! Update(await = true)
  
  // Retrieve and print the ConcertHistory
  val historyFuture = concertHistoryActor ? GetConcertHistory
  println(s"---- ConcertHistoryView ---> GetConcertHistory")
  val history = Await.result(historyFuture, timeout.duration)
  println(s"Answer from ConcertHistoryView: $history")
  println(s"<--- ConcertHistoryView ---- history")
  
  system.shutdown()
}