package com.gable.socket.thread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.gable.socket.bean.SocketBean;
import com.gable.socket.bean.SocketObject;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

public class WriteSocketClientParam implements Runnable {
	Logger log = Logger.getLogger(WriteSocketClientParam.class);

	private Integer port;

	private SocketBean obj;

	private SocketObject socketObject;

	public WriteSocketClientParam(Integer port, SocketBean obj, SocketObject socketObject) {
		this.port = port;
		this.obj = obj;
		this.socketObject = socketObject;
	}

	@Override
	public void run() {
		log.info("_____SocketServerServiceImpl1,socket端口:" + port + ":传递参数：" + JsonUtil.toJsonString(obj));
		if (socketObject.getSocket() != null) {
			try {
				Socket socket = socketObject.getSocket();
				System.out.println(socketObject.getName()+"开始往客户端些数据！");
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(obj);
				oos.flush();
				InitUtil.insertSocketList(port, socketObject);
			} catch (Exception e) {
				log.error("_____SocketServerServiceImpl2,写入客户端失败：" + e.toString());
				try {
					socketObject.getSocket().close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
