package zio.internal

import org.openjdk.jmh.annotations._
import zio.internal.BenchUtils._

import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
private[this] class PollBenchmark {
  val Ops: Int = 1 << 16

  def mkEl: AnyRef    = new Object()
  val emptyEl: AnyRef = null.asInstanceOf[AnyRef]

  @volatile var noUnrolling = true

  @Param(Array("65536"))
  var qCapacity: Int = _

  @Param(Array("RingBufferPow2", "JCTools", "LinkedQueue", "JucBlocking", "NotThreadSafe"))
  var qType: String = _

  var q: MutableConcurrentQueue[AnyRef] = _

  @Setup(Level.Trial)
  def createQ(): Unit =
    q = queueByType(qType, qCapacity)

  @Setup(Level.Invocation)
  def fill(): Unit = {
    var i = 0
    while (i < Ops) { val anEl = mkEl; q.offer(anEl); i += 1 }
  }

  @Benchmark
  @OperationsPerInvocation(1 << 16)
  def poll(): Unit = {
    val aQ = q
    var i  = 0

    while (i < Ops && noUnrolling) {
      aQ.poll(emptyEl)
      i += 1
    }
  }
}
