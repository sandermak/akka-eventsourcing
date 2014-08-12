package net.branchandbound.eventsourcing

import akka.actor.ActorSystem
import java.util.Date
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object ConcertMain extends App {

  import ConcertActor._

  val system = ActorSystem("concert-es")
  val concertActor = system.actorOf(props("concert1", 50, 100, new Date))

  concertActor ! BuyTickets("me", 50)

  implicit val timeout = Timeout(5 seconds)
  val future = concertActor ? GetSalesRecords
  val result = Await.result(future, timeout.duration).asInstanceOf[Seq[SalesRecord]]
  println(result)
  
  Thread.sleep(1000)
  system.shutdown()
}