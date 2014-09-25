
package net.branchandbound.eventsourcing

import akka.persistence.PersistentActor
import akka.persistence.SnapshotOffer
import akka.persistence.PersistentView

case object Increment
case object Incremented

class CounterActor extends PersistentActor {

  def persistenceId = "counter"

  var state = 0

  val receiveCommand: Receive = {
    case Increment => persist(Incremented) { evt =>
      state += 1
      println("incremented")
    }
  }

  val receiveRecover: Receive = {
    case Incremented => state += 1
  }
}

class SnapshottingCounterActor extends PersistentActor {
  def persistenceId = "snapshotting-counter"

  var state = 0

  val receiveCommand: Receive = {
    case Increment => persist(Incremented) { evt =>
      state += 1
      println("incremented")
    }
    case "takesnapshot" => saveSnapshot(state)
  }

  val receiveRecover: Receive = {
    case Incremented => state += 1
    case SnapshotOffer(_, snapshotState: Int) => state = snapshotState
  }
}

case object ComplexQuery

class CounterView extends PersistentView {
  override def persistenceId: String = "counter"
  override def viewId: String = "counter-view"
 
  var queryState = 0
  
  def receive: Receive = {
    case Incremented if isPersistent => {
      queryState = someVeryComplicatedCalculation(queryState)
      // Or update a document/graph/relational database
    }
    case ComplexQuery                => {
      sender() ! queryState;
      // Or perform specialized query on datastore
    }
  }
  
  def someVeryComplicatedCalculation(state: Int) = 42
}