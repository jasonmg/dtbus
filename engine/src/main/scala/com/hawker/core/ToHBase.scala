package com.hawker.core

import com.hawker.exception.ExceptionHandler
import com.hawker.utils.{HBaseUtil, Log}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream

import scala.collection.JavaConverters._

/**
  * @author mingjiang.ji on 2017/9/7
  */
trait ToHBase[O] extends Log {

  implicit val exceptionHandler: ExceptionHandler

  /**
    * please rewrite this method if you want save to HBase
    */
  def streamToPut(dStream: DStream[O]): DStream[Put] = ???

  def saveToHBase(cf: HBaseConf, dStream: DStream[O])(implicit ss: SparkSession): Unit = {
    createTable(cf)

    //  利用hadoop mapreduce 
    //    streamToPut(dStream).map(p => (new ImmutableBytesWritable, p))
    //      .foreachRDD(_.handleFailAction("save to HBase error", rdd => {
    //        val conf: Configuration = HBaseConfiguration.create()
    //
    //        conf.set(TableOutputFormat.OUTPUT_TABLE, cf.table)
    //        conf.set("hbase.zookeeper.quorum", cf.zooQuorum.mkString(","))
    //
    //        val jobConf = new Configuration(conf)
    //        //  jobConf.set("mapreduce.job.output.key.class", classOf[Text].getName)
    //        //  jobConf.set("mapreduce.job.output.value.class", classOf[LongWritable].getName)
    //        jobConf.set("mapreduce.outputformat.class", classOf[TableOutputFormat[Text]].getName)
    //
    //        rdd.saveAsNewAPIHadoopDataset(jobConf)
    //      }))

    streamToPut(dStream)
      .foreachRDD(_.handleFailAction("save to HBase error", _.foreachPartition(res =>
        HBaseUtil.save(cf.zooQuorum, cf.table, res.toSeq)
      )))
  }


  def createTable(cf: HBaseConf): Unit = HBaseUtil.createTable(cf.zooQuorum, cf.table, cf.columnFamily)
}
