package com.gable.socket.service;

import java.util.UUID;

import com.gable.socket.bean.SocketBean;

/**
 * socket服务端service
 * @author mj
 *
 */
public interface SocketServerService {
	/**
	 * 写入到客户端
	 * @param port
	 * @param obj
	 */
	public void outPutSocketToClient(Integer port,SocketBean obj);
	
	/**
	 * 抓取客户端返回值
	 * @param port
	 * @param id
	 * @return
	 */
	public SocketBean inPutSocketFromClient(Integer port,UUID id);
}
