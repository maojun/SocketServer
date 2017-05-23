package com.gable.socket.service.impl;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.service.SocketServerService;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

@Service
public class SocketServerServiceImpl implements SocketServerService {
	Logger log = Logger.getLogger(SocketServerServiceImpl.class);
	// 最大等待时间,默认两秒
	@Value("${MaxTime:2000}")
	private Long MaxTime;

	@Override
	public void outPutSocketToClient(Integer port, SocketBean obj) {
		log.info("_____SocketServerServiceImpl1,socket端口:" + port+ ":传递参数：" + JsonUtil.toJsonString(obj));
		new Thread(new Runnable() {
			@Override
			public void run() {
				Socket socket = InitUtil.skMap.get(port);
				if (socket != null) {
					try {
						ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
						oos.writeObject(obj);
						oos.flush();
					} catch (Exception e) {
						log.error("_____SocketServerServiceImpl2,写入客户端失败：" + e.toString());
						InitUtil.skMap.remove(port);
					}
				}

			}
		}).start();
	}

	@Override
	public SocketBean inPutSocketFromClient(Integer port, UUID id) {
		Socket socket = InitUtil.skMap.get(port);
		SocketBean object = null;
		if (socket != null) {
			try {
				// 轮询开始时间
				long startWaitTime = System.currentTimeMillis();
				// 轮询抓取客户端匹配信息
				log.info("_____SocketServerServiceImpl3,socket端口:" + port+ "开始抓取客户端返回结果,开始时间："+startWaitTime+",流水号:" + JsonUtil.toJsonString(id));
				while (object == null && System.currentTimeMillis() - startWaitTime < MaxTime) {
					InputStream in = socket.getInputStream();
					if (in.available() > 0) {
						ObjectInputStream ois = new ObjectInputStream(in);
						Object obj = ois.readObject();
						if (obj != null && !obj.toString().equals("Heartbeat")) {
							String jsonStr = JSON.toJSONString(obj);
							object = JsonUtil.getObject(jsonStr, SocketBean.class);
							if (object != null && id.equals(object.getUid())) {
								log.info("_____SocketServerServiceImpl4,流水号："+JsonUtil.toJsonString(id)+ "返回结果："
										+ JsonUtil.toJsonString(object));
								return object;
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("_____SocketServerServiceImpl5,流水号："+JsonUtil.toJsonString(id)+"抓取客户端返回结果异常：" + e.toString());
			}
		}
		log.info("_____SocketServerServiceImpl6,流水号："+JsonUtil.toJsonString(id)+"抓取客户端超时,结束时间："+System.currentTimeMillis());
		object = new SocketBean(0, "内部超时！");
		return object;
	}
}
