package com.hawker.core


import com.hawker.exception.ExceptionHandler
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream

/**
  * @author mingjiang.ji on 2017/9/7
  */
trait ToHDFS[O] {

  implicit val exceptionHandler: ExceptionHandler

  private[core] def saveToHDFS(dStream: DStream[O], outputPath: String)(implicit ss: SparkSession): Unit = {
    import ss.implicits._

    dStream.foreachRDD(_.handleFailAction("save to HDFS error", rdd => {
      rdd.map(_.toString).toDF.write.mode("append").text(outputPath)
    }))
  }


  private def saveAsTextFileAndMerge[T](hdfsServer: String, fileName: String, rdd: RDD[T]) = {
    val sourceFile = hdfsServer + "/tmp/"
    rdd.saveAsTextFile(sourceFile)
    val dstPath = hdfsServer + "/final/"
    merge(sourceFile, dstPath, fileName)
  }

  private def merge(srcPath: String, dstPath: String, fileName: String): Unit = {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    val destinationPath = new Path(dstPath)
    if (!hdfs.exists(destinationPath)) {
      hdfs.mkdirs(destinationPath)
    }
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath + "/" + fileName), false, hadoopConfig, null)
  }
}


