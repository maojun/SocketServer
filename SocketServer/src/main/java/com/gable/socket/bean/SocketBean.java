package com.gable.socket.bean;

import java.io.Serializable;
import java.util.UUID;

/**
 * socket数据传输对象
 * @author mj
 *
 */
public class SocketBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//数据唯一标志
	private UUID uid;
	//APP请求地址，便于内部直接转发
	private String serviceURL;
	//参数，json字符串
	private String param;
	//返回结果，json字符串
	private String result;
	//返回码，1成功，0失败
	private Integer code;
	//错误信息，code为1时可以为空
	private String errorMsg;

	public UUID getUid() {
		return uid;
	}
	public void setUid(UUID uid){
		this.uid = uid;
	}
	public String getServiceURL() {
		return serviceURL;
	}
	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public SocketBean(UUID uid,String serviceURL, String param, String result, Integer code, String errorMsg) {
		this.uid = uid;
		this.serviceURL = serviceURL;
		this.param = param;
		this.result = result;
		this.code = code;
		this.errorMsg = errorMsg;
	}
	public SocketBean(String serviceURL, String param) {
		this.uid = UUID.randomUUID();
		this.serviceURL = serviceURL; 
		this.param = param;
	}
	public SocketBean(Integer code, String errorMsg) {
		this.code = code;
		this.errorMsg = errorMsg;
	}
	private SocketBean() {
	}
}
