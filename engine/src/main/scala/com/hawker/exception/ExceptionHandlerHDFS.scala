package com.hawker.exception

import com.hawker.utils.AutoClose.using
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * @author mingjiang.ji on 2017/9/6
  */
class ExceptionHandlerHDFS(hdfsPath: String) extends ExceptionHandler {
  private val hdfsPathRex = "(^hdfs://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{2,4})(.+)".r

  private val pair = {
    val res = hdfsPathRex.findAllIn(hdfsPath)
    res.hasNext
    val uri = res.group(1)
    val filePath = res.group(2)
    (uri, filePath)
  }

  override def handler(line: String, ex: Throwable): Unit = {
    val msg = s"Line: ${line.toString} \n Error: ${ex.getMessage}"
    write(pair._1, pair._2, msg.getBytes)
  }

  private def write(uri: String, filePath: String, data: Array[Byte]): Unit = {
    val path = new Path(filePath)
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)

    using(FileSystem.get(conf)) { fs =>
      val os = {
        if (fs.exists(path)) fs.append(path)
        else fs.create(path, false)
      }
      os.write(data)
    }
  }
}
