package com.gable.socket.thread;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.gable.socket.bean.SocketBean;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

/**
 * 筛选客户端正确的结果，以免线程错拿
 * @author mj
 *
 */
public class FetchResult implements Callable<SocketBean> {
	Logger log = Logger.getLogger(FetchResult.class);
	private UUID uid;

	private Long MaxTime;

	public FetchResult(UUID uid, Long MaxTime) {
		this.uid = uid;
		this.MaxTime = MaxTime;
	}

	@Override
	public SocketBean call() throws Exception {
		SocketBean object = null;
		// 轮询开始时间
		long startWaitTime = System.currentTimeMillis();
		// 轮询抓取客户端匹配信息
		log.info("_____FetchResult1开始筛选返回结果,开始时间：" + startWaitTime + ",流水号:"
				+ JsonUtil.toJsonString(uid));
		while (object == null && System.currentTimeMillis() - startWaitTime < MaxTime) {
			object = InitUtil.resultMap.get(uid);
		}
		log.info("_____FetchResult2結束筛选,结束时间：" + System.currentTimeMillis() + ",流水号:"
				+ JsonUtil.toJsonString(uid));
		return object;
	}

}
