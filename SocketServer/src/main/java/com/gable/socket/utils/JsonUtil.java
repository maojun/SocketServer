package com.gable.socket.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gable.socket.bean.SocketBean;

/**
 * json辅助类
 * 
 * @author mj
 *
 */
public class JsonUtil {
	/**
	 * 对象转json字符串
	 * @param obj
	 * @return
	 */
	public static String toJsonString(Object obj) {
		return JSON.toJSONString(obj);
	}
	/**
	 * json字符串转对象
	 * @param jsonStr
	 * @param clazz
	 * @return
	 */
	public static <T> T getObject(String jsonStr, Class<T> clazz) {
		T parseObject = JSONObject.parseObject(jsonStr, clazz);
		return parseObject;
	}
	
	/**
	 * json字符串转jsonObject
	 * @param jsonStr
	 * @return
	 */
	public static JSONObject toJsonObject(String jsonStr){
		return JSONObject.parseObject(jsonStr);
	}
	
	/**
	 * json字符串转json数组
	 * @param jsonStr
	 * @return
	 */
	public static JSONArray toJsonArray(String jsonStr){
		return JSONArray.parseArray(jsonStr);
	}
	
	public static void main(String[] args) {
		SocketBean sb1 = new SocketBean(1, "1111");
		SocketBean sb2 = new SocketBean(2, "2222");
		List<SocketBean> list2 = new ArrayList<SocketBean>();
		list2.add(sb1);
		list2.add(sb2);
		String json = toJsonString(list2);
		System.out.println(json);
		JSONArray jsonArray = toJsonArray(json);
		for(int i =0;i<jsonArray.size();i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			SocketBean sb = JsonUtil.getObject(jsonObject.toJSONString(), SocketBean.class);
			System.out.println(sb.getErrorMsg());
		}
	}
}
