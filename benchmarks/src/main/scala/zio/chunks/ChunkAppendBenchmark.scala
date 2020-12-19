package zio.chunks

import org.openjdk.jmh.annotations._
import zio.Chunk

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ChunkAppendBenchmarks {

  val chunk: Chunk[Int]   = Chunk(1)
  val vector: Vector[Int] = Vector(1)

  @Param(Array("10000"))
  var size: Int = _

  @Benchmark
  def chunkAppend(): Chunk[Int] = {
    var i       = 0
    var current = chunk

    while (i < size) {
      current = current :+ i
      i += 1
    }

    current
  }

  @Benchmark
  def vectorAppend(): Vector[Int] = {
    var i       = 0
    var current = vector

    while (i < size) {
      current = current :+ i
      i += 1
    }

    current
  }
}
