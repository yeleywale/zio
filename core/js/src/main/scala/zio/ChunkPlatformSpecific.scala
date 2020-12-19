package zio

import scala.reflect.{ClassTag, classTag}

private[zio] trait ChunkPlatformSpecific {

  private[zio] object Tags {
    def fromValue[A](a: A): ClassTag[A] = {
      val _ = a
      classTag[AnyRef].asInstanceOf[ClassTag[A]]
    }
  }
}
