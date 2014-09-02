package net.branchandbound.eventsourcing

import akka.persistence.PersistentView

object ConcertHistoryView {
  case class ConcertHistoryState(priceHistory: Seq[Int] = Nil, ticketsPerPrice: Map[Int, Int] = Map.empty, soldOuts: Int = 0) {
    def updated() {}
  }
}

class ConcertHistoryView(id: String) extends PersistentView {
  
  import ConcertHistoryView._
  
  override def persistenceId: String = "some-persistence-id"
  override def viewId: String = "some-persistence-id-view"
 
  def receive: Receive = {
    case payload if isPersistent =>
    // handle message from journal...
    case payload                 =>
    // handle message from user-land...
  }
  
  
}