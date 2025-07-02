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

import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class CustomException extends RuntimeException

class DelayedFutureSpec extends AnyFlatSpec {

  "Future.delayed" should "return the operation result" in {
    val future = Future.delayed(1.milli) {
      42
    }
    assertResult(42)(Await.result(future, 1.second))
  }

  it should "propagate exceptions" in {
    val future = Future.delayed(1.milli) {
      throw CustomException()
    }
    assertThrows[CustomException](Await.result(future, 1.second))
  }

  it should "delay execution by at least the specified delay" in {
    val before = System.currentTimeMillis()
    val future = Future.delayed(100.millis) {
      System.currentTimeMillis()
    }
    val after = Await.result(future, 1.second)
    assert(after >= before + 100)
  }

  it should "accept 0 delay" in {
    Future.delayed(0.milli) {
      42
    }
  }

  it should "throw IllegalArgument exception if delay is negative" in {
    assertThrows[IllegalArgumentException](Future.delayed(-10.milli) {
      42
    })
  }

  "Future.after" should "complete with the future result if successful" in {
    val future = Future.after(100.millis) {
      Future.successful(42)
    }(using ExecutionContext.global)
    assertResult(42)(Await.result(future, 1.second))
  }

  it should "complete with the future result if failed" in {
    val future = Future.after(100.millis) {
      Future.failed(CustomException())
    }(using ExecutionContext.global)
    assertThrows[CustomException](Await.result(future, 1.second))
  }

  it should "delay execution by at least the specified delay" in {
    val before = System.currentTimeMillis()
    val future = Future.after(100.millis) {
      Future.successful(System.currentTimeMillis())
    }(using ExecutionContext.global)
    val after = Await.result(future, 1.second)
    assert(after >= before + 100)
  }

  it should "accept 0 delay" in {
    Future.after(0.milli) {
      Future.successful(42)
    }(using ExecutionContext.global)
  }

  it should "throw IllegalArgument exception if delay is negative" in {
    assertThrows[IllegalArgumentException](
      Future.after(-10.milli) {
        Future.successful(42)
      }(using ExecutionContext.global)
    )
  }

}
