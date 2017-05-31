package com.gable.socket.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.bean.SocketObject;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

public class ReadSocketClientResult implements Runnable {

	Logger log = Logger.getLogger(ReadSocketClientResult.class);

	private Integer port;

	private UUID uid;

	private Long MaxTime;
	
	private SocketObject so;

	public ReadSocketClientResult(Integer port, UUID uid, Long MaxTime,SocketObject so) {
		this.port = port;
		this.uid = uid;
		this.MaxTime = MaxTime;
		this.so = so;
	}

	@Override
	public void run() {
		Socket socket = so.getSocket();
		if (socket != null) {
			try {
				insertData(socket);
			} catch (Exception e) {
				log.error("_____SocketServerServiceImpl5,流水号：" + JsonUtil.toJsonString(uid) + "抓取客户端返回结果异常："
						+ e.toString());
				InitUtil.skMap.remove(port);
				log.error("=====================================服务端关闭socket,等待客户端心跳机制重连");
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			log.info("_____SocketServerServiceImpl6,流水号：" + JsonUtil.toJsonString(uid) + "抓取客户端超时,结束时间："
					+ System.currentTimeMillis());
		}
	}

	private void insertData(Socket socket) {
		// 轮询开始时间
		long startWaitTime = System.currentTimeMillis();
		// 轮询抓取客户端匹配信息
		log.info("_____SocketServerServiceImpl3,socket端口:" + port + "开始抓取客户端返回结果,开始时间：" + startWaitTime + ",流水号:"
				+ JsonUtil.toJsonString(uid));
		// 在限定时间类抓取到结果，放入结果集 InitUtil.resultMap
		try {
			SocketBean object = null;
			while (object == null && System.currentTimeMillis() - startWaitTime < MaxTime) {
				ObjectInputStream ois = null;

				InputStream in = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(outputStream);
				if (in.available() > 0) {
					ois = new ObjectInputStream(socket.getInputStream());
					Object obj = ois.readObject();
					// 跳过心跳包 "Heartbeat" 为心跳包发送过来的数据
					if (obj != null && !obj.toString().equals("Heartbeat")) {
						String jsonStr = JSON.toJSONString(obj);
						object = JsonUtil.getObject(jsonStr, SocketBean.class);
						// 这里不再需要流水号判断
						if (object != null) {
							log.info("_____SocketServerServiceImpl4,流水号：" + JsonUtil.toJsonString(uid) + "返回结果："
									+ JsonUtil.toJsonString(object));
							// 将客户端返回的结果统一放到结果集中
							InitUtil.resultMap.put(object.getUid(), object);
						}
					}
				}
			}
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
