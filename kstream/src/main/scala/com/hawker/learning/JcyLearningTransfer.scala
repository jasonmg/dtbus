package com.hawker.learning

import java.util.concurrent.TimeUnit
import java.util.{List => JList}

import com.hawker.core.{CanalCommonTransfer, KStreamConfig, KafkaMessage}
import com.hawker.utils.ConfigUtil.{config, _}
import com.hawker.utils.kafka.serializer.CommonSerdes
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._

/**
  * @author mingjiang.ji on 2017/12/8
  */
class JcyLearningTransfer(config: KStreamConfig) extends CanalCommonTransfer[KafkaMessage](config) {

  def tablePredicate(tableName: String) = new Predicate[String, KafkaMessage]() {
    override def test(key: String, value: KafkaMessage) = value.tableName == tableName
  }

  val tableTpStudent = tablePredicate("tp_student")

  val tableExam = tablePredicate("ex_exam")
  val tablePaper = tablePredicate("ex_paper")
  val tableExamStudentSelect = tablePredicate("ex_exam_student_select")
  val tableExamStudentResult = tablePredicate("ex_exam_student_result")
  val tableExamStudentResultDetail = tablePredicate("ex_exam_student_result_detail")
  val tablePaperQuestions = tablePredicate("ex_paper_questions")
  val tableExQuestions = tablePredicate("ex_questions")


  override def streamTransfer(streamBuilder: KStreamBuilder): KStream[String, KafkaMessage] = kafkaMessageStream(config.topic, streamBuilder)

  override def postProcess(ks: KStream[String, KafkaMessage]): Unit = {
    val tablesKs = ks.branch(tableTpStudent, tableExam, tablePaper, tableExamStudentSelect, tableExamStudentResult,
      tableExamStudentResultDetail, tablePaperQuestions, tableExQuestions)

    println(s"===================: ${tablesKs.size}")

    for (tableKs <- tablesKs) {
      transfer(tableKs)
    }
  }


  def transfer(ks: KStream[String, KafkaMessage]): Unit = {
    import com.hawker.core.KafkaMessageUtil

    val keyedKs: KStream[String, KafkaMessage] = ks.selectKey(new KeyValueMapper[String, KafkaMessage, String]() {
      override def apply(key: String, value: KafkaMessage): String = {
        val id = value.afterRow.get("id")
        require(id != null, "invalided row, mysql row must contain 'id' ")

        id
      }
    })

    keyedKs.print()


    val window = TimeWindows.of(TimeUnit.SECONDS.toMillis(10))

    val gks = keyedKs.groupByKey(Serdes.String(), new KafkaMessageSerdes())

    keyedKs.groupByKey()

    val res = gks.windowedBy(window).aggregate(
      new Initializer[Seq[KafkaMessage]]() {
        override def apply() = Seq.empty[KafkaMessage]
      },
      new Aggregator[String, KafkaMessage, Seq[KafkaMessage]]() {
        override def apply(key: String, value: KafkaMessage, aggregate: Seq[KafkaMessage]) = {
          aggregate :+ value
        }
      })


    res.toStream.foreach(new ForeachAction[Windowed[String], Seq[KafkaMessage]] {
      override def apply(key: Windowed[String], value: Seq[KafkaMessage]) = {
        println(value)
        println("========Start=========")
        value.foreach(v => println(KafkaMessageUtil.toSql(v)))
        println("========END=========")
      }
    })

  }

}

class KafkaMessageSerdes extends CommonSerdes[KafkaMessage]

object JcyLearningTransfer extends App {
  val p = config.getConfig("jcyLearning").convert[KStreamConfig]
  val jt = new JcyLearningTransfer(p)
  jt.start()
}

