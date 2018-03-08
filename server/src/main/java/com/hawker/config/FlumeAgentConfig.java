package com.hawker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * flume集中化配置管理器
 * 
 * @author gaojun
 *
 */
@Component
public class FlumeAgentConfig {
	protected final static Logger logger = LoggerFactory.getLogger(FlumeAgentConfig.class);

	public String genAgentConfig(String ip,List<String> fileList,String kafkaServer,String kafkaTopic) {
		Resource resource = new ClassPathResource("taildir2kafka_template.properties");
		String flumeCfgName = "flumeagent.properties";

		Properties prop = new Properties();
		try {
			//读取属性文件a.properties
			//InputStream in = new BufferedInputStream (new FileInputStream("a.properties"));
			InputStream in = new BufferedInputStream(resource.getInputStream());
			prop.load(in);     ///加载属性列表
			Iterator<String> it = prop.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				System.out.println(key + "=" + prop.getProperty(key));
			}
			in.close();

			///保存属性到flumeCfg.properties文件
			FileOutputStream oFile = new FileOutputStream(flumeCfgName, false);//true表示追加打开
			prop.setProperty("flumeagent.sinks.k1.kafka.bootstrap.servers", kafkaServer);

			StringBuilder groups = new StringBuilder();
			for (int i = 1; i <= fileList.size(); i++) {
				String source = fileList.get(i-1);
				groups.append("f").append(i);
				if (i != (fileList.size()))
					groups.append(" ");
				String sourceKey = "flumeagent.sources.r1.filegroups.f" + i;
				prop.setProperty(sourceKey,source);
				String sourceHeaderTopicKey = "flumeagent.sources.r1.headers.f" + i + ".topic";
				if(source.contains("/")){
					source=source.replaceAll("/","_");
				}
				String topic = ip + source;
				prop.setProperty(sourceHeaderTopicKey, topic);
			}
			prop.setProperty("flumeagent.sources.r1.filegroups", groups.toString());

			prop.store(oFile, "The New flume agent properties file");
			oFile.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return  flumeCfgName;
	}

}
