package zio.internal

import java.util.concurrent.LinkedBlockingQueue

class JucBlockingQueue[A] extends MutableConcurrentQueue[A] {
  override val capacity: Int = Int.MaxValue

  private val jucBlockingQueue = new LinkedBlockingQueue[A](capacity)

  override def size(): Int = jucBlockingQueue.size()

  override def enqueuedCount(): Long = throw new UnsupportedOperationException("enqueuedCount not implemented")

  override def dequeuedCount(): Long = throw new UnsupportedOperationException("dequeuedCount not implemented")

  override def offer(a: A): Boolean = jucBlockingQueue.offer(a)

  override def poll(default: A): A = {
    val res = jucBlockingQueue.poll()
    if (res != null) res else default
  }

  override def isEmpty(): Boolean = jucBlockingQueue.isEmpty

  override def isFull(): Boolean = false
}
