package zio.internal

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

/*
 * Main purposes of this set of benchmarks are:
 * 1. Get a feel for perf when JVM has several subclasses in the profile.
 * 2. Get profiled assembly.
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
private[this] class RingBufferMethodDispatchBenchmark {
  type QueueElement = () => Int

  def mkEl(): QueueElement  = () => 1
  val emptyEl: QueueElement = () => -1

  @Param(Array("1"))
  var batchSize: Int = _

  @Param(Array("8"))
  var qCapacity: Int = _

  var q1: MutableConcurrentQueue[QueueElement] = _
  var q2: MutableConcurrentQueue[QueueElement] = _
  var q3: MutableConcurrentQueue[QueueElement] = _

  @Setup(Level.Trial)
  def createQ(): Unit = {
    q1 = RingBufferPow2[QueueElement](qCapacity)
    q2 = RingBufferArb[QueueElement](qCapacity)
    q3 = new LinkedQueue[QueueElement]
  }

  @Benchmark
  @Group("OnlyPow2")
  @GroupThreads(1)
  def onlyPow2Pow2(): Int = {
    doOffer(q1, batchSize)
    doPoll(q1)
  }

  @Benchmark
  @Group("Pow2Unbounded")
  @GroupThreads(1)
  def puPow2(): Int = {
    doOffer(q1, batchSize)
    doPoll(q1)
  }

  @Benchmark
  @Group("Pow2Unbounded")
  @GroupThreads(1)
  def puUnbounded(): Int = {
    doOffer(q3, batchSize)
    doPoll(q3)
  }

  @Benchmark
  @Group("All")
  @GroupThreads(1)
  def allPow2(): Int = {
    doOffer(q1, batchSize)
    doPoll(q1)
  }

  @Benchmark
  @Group("All")
  @GroupThreads(1)
  def allArb(): Int = {
    doOffer(q2, batchSize)
    doPoll(q2)
  }

  @Benchmark
  @Group("All")
  @GroupThreads(1)
  def allUnbounded(): Int = {
    doOffer(q3, batchSize)
    doPoll(q3)
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
