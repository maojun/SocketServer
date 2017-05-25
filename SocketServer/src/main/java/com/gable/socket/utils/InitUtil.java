package com.gable.socket.utils;

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.gable.socket.bean.SocketBean;

public class InitUtil {
	/**
	 * socket对象MAP,和端口映射
	 */
	public static Map<Integer, Socket> skMap;
	/**
	 * 医院ID——socket端口 映射MAP
	 */
	public static Map<Long,Integer> hospitalIdPortMap;
	
	/**
	 * 无界线程池
	 */
	public static ExecutorService executorService;
	
	/**
	 * 并发结果集
	 */
	public static Map<UUID,SocketBean> resultMap;
}
