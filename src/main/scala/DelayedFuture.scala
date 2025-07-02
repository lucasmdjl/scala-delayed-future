/*
 * Scala Delayed Future - A Scala 3 micro-library for ergonomically creating delayed Futures.
 * Copyright (C) 2025 Lucas de Jong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.lucasmdjl.scala.delayedfuture

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

extension (f: Future.type)

  /** Creates a Future that completes after the specified delay.
    *
    * The operation is executed after the delay has elapsed. If the operation
    * throws an exception, the returned Future will be failed with that
    * exception.
    *
    * @param delay
    *   the duration to wait before executing the operation. Must not be negative
    * @param operation
    *   the computation to execute after the delay
    * @tparam T
    *   the type of the operation's result
    * @return
    *   a Future that will complete with the operation's result after the delay
    *
    * @example
    *   {{{
    * import scala.concurrent.duration._
    *
    * val future = Future.delayed(2.seconds) {
    *   println("This prints after 2 seconds")
    *   42
    * }
    *   }}}
    */
  def delayed[T](delay: FiniteDuration)(operation: => T): Future[T] = {
    require(delay >= 0.nanos, s"delay must not be negative, but was $delay")
    val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(
      1,
      r => {
        val t = Executors.defaultThreadFactory().newThread(r)
        t.setDaemon(true)
        t
      }
    )
    val promise = Promise[T]()
    scheduler.schedule(
      () => {
        promise.complete(Try(operation))
        scheduler.shutdown()
        42 // Help the compiler choose an overload of schedule
      },
      delay.toNanos,
      TimeUnit.NANOSECONDS
    )
    promise.future
  }

  /** Creates a Future that executes another Future after the specified delay.
    *
    * This is useful for delaying the start of an asynchronous operation. The
    * delay applies to when the future is created, not when it completes.
    *
    * @param delay
    *   the duration to wait before starting the future. Must not be negative.
    * @param future
    *   the future computation to execute after the delay (call-by-name)
    * @tparam T
    *   the type of the future's result
    * @return
    *   a Future that will start executing after the delay
    * @example
    *   {{{
    * import scala.concurrent.duration._
    * import scala.concurrent.ExecutionContext.Implicits.global
    *
    * val delayed = Future.after(5.seconds) {
    *   Future {
    *     // This HTTP call starts 5 seconds from now
    *     httpClient.get("https://api.example.com")
    *   }
    * }
    *   }}}
    */
  def after[T](delay: FiniteDuration)(future: => Future[T])(using
      ExecutionContext
  ): Future[T] = delayed(delay)(()).flatMap(_ => future)
