package zio.internal

import org.openjdk.jmh.annotations._
import zio.internal.BenchUtils._

import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
private[this] class RoundtripBenchmark {
  type QueueElement = () => Int

  def mkEl(): QueueElement  = () => 1
  val emptyEl: QueueElement = () => -1

  @Param(Array("1", "4"))
  var batchSize: Int = _

  @Param(Array("8"))
  var qCapacity: Int = _

  @Param(Array("RingBufferPow2", "RingBufferArb", "JCTools", "LinkedQueue", "JucBlocking", "NotThreadSafe"))
  var qType: String = _

  var q: MutableConcurrentQueue[QueueElement] = _

  @Setup(Level.Trial)
  def createQ(): Unit =
    q = queueByType(qType, qCapacity)

  @Benchmark
  def offerAndPoll(): Int = {
    doOffer(q, batchSize)
    doPoll(q)
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  def doOffer(q: MutableConcurrentQueue[QueueElement], bSize: Int): Unit = {
    var i = 0
    while (i < bSize) {
      q.offer(mkEl())
      i += 1
    }
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  def doPoll(q: MutableConcurrentQueue[QueueElement]): Int = {
    var i           = 0
    var result: Int = 0
    while (i < batchSize) {
      val delayed = q.poll(emptyEl)
      result += delayed()
      i += 1
    }
    result
  }
}
