package com.hawker.http.embed

import spray.json.{DeserializationException, JsArray, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

/**
  * @author mingjiang.ji on 2017/11/23
  */
class JcyEmbedJsonFormat extends RootJsonFormat[JcyEmbed] {


  override def read(value: JsValue) =
    value match {
      case JsArray(Vector(JsString(sourceApp), JsString(source), JsString(channel), JsString(versionCode),
      JsString(sysVersion), JsString(serverVersion), JsString(cpu), JsString(resolution),
      JsString(language), JsString(imei), JsString(deviceName), JsString(netType),
      JsString(clientIp), JsString(cityId), JsString(wifiMac), JsString(uuid),
      JsString(userId), JsString(eventId), JsString(eventType), JsString(logTime), JsString(params))) =>

        new JcyEmbed(sourceApp, source, channel, versionCode, sysVersion,
          serverVersion, cpu, resolution, language, imei, deviceName,
          netType, clientIp, cityId, wifiMac, uuid, userId,
          eventId, eventType, logTime, params)


      case JsObject(m) =>
        val m1 = m.mapValues(_.toString()).withDefaultValue("")

        new JcyEmbed(m1("sourceApp"), m1("source"), m1("channel"), m1("versionCode"), m1("sysVersion"),
          m1("serverVersion"), m1("CPU"), m1("resolution"), m1("language"), m1("imei"), m1("deviceName"),
          m1("netType"), m1("clientIp"), m1("cityId"), m1("wifiMac"), m1("UUID"), m1("userId"),
          m1("eventId"), m1("eventType"), m1("logTime"), m1("params"))

      case _ => throw new DeserializationException("JcyEmbed expected")
    }

  override def write(o: JcyEmbed) =
    JsArray(JsString(o.getSourceApp),
      JsString(o.getSource),
      JsString(o.getChannel),
      JsString(o.getVersionCode),
      JsString(o.getSysVersion),
      JsString(o.getCPU),
      JsString(o.getResolution),
      JsString(o.getLanguage),
      JsString(o.getImei),
      JsString(o.getDeviceName),
      JsString(o.getNetType),
      JsString(o.getClientIp),
      JsString(o.getCityId),
      JsString(o.getWifiMac),
      JsString(o.getUUID),
      JsString(o.getUserId),
      JsString(o.getEventId),
      JsString(o.getEventType),
      JsString(o.getLogTime),
      JsString(o.getParams))

}
