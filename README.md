# scala-delayed-future

A Scala 3 micro-library for ergonomically creating delayed Futures.

## Why?

Scala's `Future` doesn't have built-in support for delayed execution. This library fills that gap with two simple extension methods that let you delay the start of computations or Future chains.

### Why not cats-effect or Akka?

While cats-effect and Akka provide excellent timing utilities, they come with significant dependencies and learning curves. This library is perfect when you just need simple delay functionality without the overhead of a full effect system or actor framework.

### Why not `Future { Thread.sleep(...) }`?

While `Future { Thread.sleep(2000); doSomething() }` works, it blocks a thread from your ExecutionContext for the entire delay period. This library uses scheduled execution instead, freeing up threads for actual work while waiting.

## Installation

Add to your `build.sbt`:

```scala
libraryDependencies += "dev.lucasmdjl" %% "scala-delayed-future" % "0.1.0"
```

## Usage

Import the extension methods:

```scala
import dev.lucasmdjl.scala.delayedfuture.*
import scala.concurrent.duration.*
```

### `Future.delayed`

Execute a computation after a delay:

```scala
val future = Future.delayed(2.seconds) {
  println("This prints after 2 seconds")
  42
}
```

### `Future.after`

Start a Future after a delay:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

val delayed = Future.after(5.seconds) {
  Future {
    httpClient.get("https://api.example.com")
  }
}
```

## Examples

### Retry with backoff

```scala
def retryWithDelay[T](operation: => Future[T], delay: Duration): Future[T] = {
  operation.recoverWith { case _ =>
    Future.after(delay) {
      operation
    }
  }
}
```

### Rate limiting

```scala
def rateLimitedRequests[T](requests: List[() => Future[T]], interval: Duration): Future[List[T]] = {
  requests.zipWithIndex.map { case (request, index) =>
    Future.after(interval * index) {
      request()
    }
  }.foldLeft(Future.successful(List.empty[T])) { (acc, future) =>
    for {
      list <- acc
      result <- future
    } yield list :+ result
  }
}
```

### Timeout simulation

```scala
val computation = Future.delayed(1.minute) {
  "Long running task complete"
}
```

## Features

- **Zero dependencies** - Uses only the JDK's `ScheduledExecutorService`
- **Resource safe** - Automatically cleans up thread pools
- **Exception safe** - Properly propagates exceptions to the returned Future
- **Non-blocking** - Uses daemon threads that won't prevent JVM shutdown

## How it works

Each delayed operation creates a single-thread scheduler that executes the operation and then shuts itself down. This approach prioritizes simplicity and resource safety over performance for high-frequency operations.

## License

AGPL-3.0
