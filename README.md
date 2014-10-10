### Example code for 'Event-sourcing with Akka'

You need Scala 2.11 and SBT to build and run this code. The file 'ConcertMain.scala' starts up the persistent actor and view and runs a small scenario.

Since the default LevelDB journal of Akka is used, the events are stored in ```journal``` directory. You can remove the directory to start over with a clean eventstore.

The application can be run using SBT:
```
sbt run
```

