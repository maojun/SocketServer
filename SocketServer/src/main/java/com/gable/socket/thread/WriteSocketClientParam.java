package com.gable.socket.thread;

import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.gable.socket.bean.SocketBean;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

public class WriteSocketClientParam implements Runnable {
	Logger log = Logger.getLogger(WriteSocketClientParam.class);
	
	private Integer port;
	
	private SocketBean obj;

	public WriteSocketClientParam(Integer port,SocketBean obj){
		this.port = port;
		this.obj = obj;
	}

	@Override
	public void run() {
		log.info("_____SocketServerServiceImpl1,socket端口:" + port+ ":传递参数：" + JsonUtil.toJsonString(obj));
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
}
