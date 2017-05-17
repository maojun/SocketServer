package com.gable.socket.bean;

public class JsonReturn {
	private Integer ret;	//1 为成功，0为失败
	private String msg;	//描述
	private String data;	//结果
	
	public Integer getRet() {
		return ret;
	}
	public void setRet(Integer ret) {
		this.ret = ret;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	public JsonReturn(Integer ret, String msg, String data) {
		this.ret = ret;
		this.msg = msg;
		this.data = data;
	}
	
	public JsonReturn(Integer ret, String msg) {
		this.ret = ret;
		this.msg = msg;
	}
	
	public JsonReturn() {
	}
	
}
