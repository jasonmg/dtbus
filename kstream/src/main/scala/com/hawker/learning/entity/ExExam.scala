package com.hawker.learning.entity

import java.sql.Timestamp

/**
  * @author mingjiang.ji on 2017/12/8
  */
case class ExExam(id: java.lang.Long,
                  name: String,
                  creator: String,
                  description: String,
                  startTime: Timestamp,
                  endTime: Timestamp,
                  duration: java.lang.Long,
                  growthValue: java.lang.Long,
                  passScore: java.lang.Long)
