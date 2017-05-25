package com.gable.socket.thread;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

/**
 * 这是一个错误的案例，在服务端与客户端长连接的数据传输中，
 * 因为写和读都是异步操作
 * 并发请求时可能出现请求1拿到的时请求2的返回结果
 * 在程序  @1 中无法正确的控制，此类作废
 * @author mj
 *
 */
public class ReadSocketClientResult implements Callable<SocketBean> {

	Logger log = Logger.getLogger(ReadSocketClientResult.class);

	private Integer port;

	private UUID uid;

	private Long MaxTime;

	public ReadSocketClientResult(Integer port, UUID uid, Long MaxTime) {
		this.port = port;
		this.uid = uid;
		this.MaxTime = MaxTime;
	}

	@Override
	public SocketBean call() throws Exception {
		Socket socket = InitUtil.skMap.get(port);
		SocketBean object = null;
		if (socket != null) {
			try {
				// 轮询开始时间
				long startWaitTime = System.currentTimeMillis();
				// 轮询抓取客户端匹配信息
				log.info("_____SocketServerServiceImpl3,socket端口:" + port + "开始抓取客户端返回结果,开始时间：" + startWaitTime
						+ ",流水号:" + JsonUtil.toJsonString(uid));
				//在限定时间类抓取到结果，返回
				while (object == null && System.currentTimeMillis() - startWaitTime < MaxTime) {
					InputStream in = socket.getInputStream();
					if (in.available() > 0) {
						ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
						Object obj = ois.readObject();
						//跳过心跳包  "Heartbeat" 为心跳包发送过来的数据
						if (obj != null && !obj.toString().equals("Heartbeat")) {
							String jsonStr = JSON.toJSONString(obj);
							object = JsonUtil.getObject(jsonStr, SocketBean.class);
							//@1    上面已经不满足while条件了，如果这里如果是请求1 拿到的请求2结果
							//两者的uid 肯定匹配不上，也就不会进入下面的if,return obejct
							//而while 也将退出，就会直接返回最后的 "内部超时"
							if (object != null && uid.equals(object.getUid())) {
								log.info("_____SocketServerServiceImpl4,流水号：" + JsonUtil.toJsonString(uid) + "返回结果："
										+ JsonUtil.toJsonString(object));
								return object;
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("_____SocketServerServiceImpl5,流水号：" + JsonUtil.toJsonString(uid) + "抓取客户端返回结果异常："
						+ e.toString());
				InitUtil.skMap.remove(port);
				System.out.println("=====================================服务端关闭socket");
				socket.close();
			}
			log.info("_____SocketServerServiceImpl6,流水号：" + JsonUtil.toJsonString(uid) + "抓取客户端超时,结束时间："
					+ System.currentTimeMillis());
			object = new SocketBean(0, "内部超时！");
		}
		return object;
	}

}
