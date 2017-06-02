package com.gable.socket.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.gable.socket.bean.SocketBean;
import com.gable.socket.bean.SocketObject;

public class InitUtil {
	/**
	 * socket对象MAP,和端口映射
	 */
	public static Map<Integer, List<SocketObject>> skMap = new HashMap<Integer,List<SocketObject>>();;
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
	
	public static List<SocketObject> skList = new ArrayList<SocketObject>();
	
	public static int index = 0;
	
	public static SocketObject getSocketObject(Integer port){
		List<SocketObject> list = skMap.get(port);
		for(SocketObject so : list){
			list.remove(so);
			return so;
		}
		return null;
	}
	
	public static void insertSocketList(Integer port,SocketObject so){
		if(skMap.get(port) == null || skMap.get(port).size() == 0){
			List<SocketObject> list = new ArrayList<SocketObject>();
			list.add(so);
			skMap.put(port, list);
		}else{
			//清空之前连接,否则客户端多次重启。服务端的缓存对象就是无效的
			if(skMap.get(port).size() >= 20){
				skMap.remove(port);
				List<SocketObject> list = new ArrayList<SocketObject>();
				list.add(so);
				skMap.put(port, list);
			}else{
				skMap.get(port).add(so);
			}
		}
	}
	
	public static Integer getIndex(){
		index = index +1;
		return index;
	}
}
