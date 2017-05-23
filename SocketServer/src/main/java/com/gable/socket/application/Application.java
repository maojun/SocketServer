package com.gable.socket.application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gable.socket.utils.InitUtil;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan("com.gable.socket.*")
@Configuration
public class Application extends SpringBootServletInitializer implements InitializingBean {
	Logger log = Logger.getLogger(Application.class);

	@Value("${PORT}")
	private String port;

	@Override
	public void afterPropertiesSet() throws Exception {
		// socket端口_socket对象map
		InitUtil.skMap = new HashMap<Integer, Socket>();

		// hospitalId_socket端口map
		String[] portArray = port.split(",");
		InitUtil.hospitalIdPortMap = mappingHospital(portArray);

		
		for(Map.Entry<Long, Integer> hid_port : InitUtil.hospitalIdPortMap.entrySet()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ServerSocket ss = new ServerSocket(hid_port.getValue(), 5);
						log.info("_____初始化医院,ID:"+hid_port.getKey()+",端口："+hid_port.getValue());
						while (true) {
							Socket socket = ss.accept();
							log.info("_____hi,医院ID:" + hid_port.getKey() + "客户端端接入");
							InitUtil.skMap.put(hid_port.getValue(), socket);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	/**
	 * 映射医院ID和对应的socketPort {1=1001,2=1002}
	 * @param portArray
	 * @return
	 */
	public Map<Long,Integer> mappingHospital(String[] portArray){
		Map<Long,Integer> map = new HashMap<Long,Integer>();
		for (int i = 0; i < portArray.length; i++) {
			String ports = portArray[i];
			int indexOf = ports.indexOf("_");
			Long hospitalId = Long.parseLong(ports.substring(0, indexOf));
			Integer port = Integer.parseInt(ports.substring(indexOf+1, ports.length()));
			map.put(hospitalId, port);
		}
		return map;
	}
}