package com.gable.socket.application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gable.socket.bean.SocketBean;
import com.gable.socket.bean.SocketObject;
import com.gable.socket.utils.InitUtil;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan("com.gable.socket.*")
@Configuration
public class Application extends SpringBootServletInitializer implements InitializingBean {
	Logger log = Logger.getLogger(Application.class);

	@Value("${PORT}")
	private String port;

	//客户端线程连接数
	@Value("${maxThreadNum}")
	Integer maxThreadNum;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// hospitalId_socket端口map
		String[] portArray = port.split(",");
		InitUtil.hospitalIdPortMap = mappingHospital(portArray);
		// 初始化线程池，JVM自动创建线程，回收线程
		InitUtil.executorService = Executors.newFixedThreadPool(40);

		InitUtil.resultMap = new HashMap<UUID, SocketBean>();

		for (Map.Entry<Long, Integer> hid_port : InitUtil.hospitalIdPortMap.entrySet()) {
			InitUtil.executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						ServerSocket ss = new ServerSocket(hid_port.getValue(), maxThreadNum);
						log.info("_____初始化医院,ID:" + hid_port.getKey() + ",端口：" + hid_port.getValue());
						while (true) {
							Socket socket = ss.accept();
							log.info("_____hi,医院ID:" + hid_port.getKey() + "客户端端接入");
							SocketObject so = new SocketObject(socket,"sockt"+InitUtil.getIndex());
							InitUtil.insertSocketList(hid_port.getValue(), so);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 映射医院ID和对应的socketPort {1=1001,2=1002}
	 * 
	 * @param portArray
	 * @return
	 */
	public Map<Long, Integer> mappingHospital(String[] portArray) {
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		for (int i = 0; i < portArray.length; i++) {
			String ports = portArray[i];
			int indexOf = ports.indexOf("_");
			Long hospitalId = Long.parseLong(ports.substring(0, indexOf));
			Integer port = Integer.parseInt(ports.substring(indexOf + 1, ports.length()));
			map.put(hospitalId, port);
		}
		return map;
	}
}
