package com.gable.socket.utils;

import java.net.Socket;
import java.util.Map;

public class InitUtil {
	/**
	 * socket对象MAP,和端口映射
	 */
	public static Map<Integer, Socket> skMap;
	/**
	 * 医院ID——socket端口 映射MAP
	 */
	public static Map<Long,Integer> hospitalIdPortMap;
}
