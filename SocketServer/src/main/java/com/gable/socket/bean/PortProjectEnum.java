//package com.gable.socket.bean;
///**
// * 端口号映射医院枚举类
// * @author mj
// *
// */
//public enum PortProjectEnum {
//	RedHouse(1001,"红房子医院"),
//	Childrens(1002,"儿童医院");
//	private Integer code;
//	private String name;
//	
//	
//	public Integer getCode() {
//		return code;
//	}
//	public void setCode(Integer code) {
//		this.code = code;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	private PortProjectEnum(Integer code, String name) {
//		this.code = code;
//		this.name = name;
//	}
//	
//	/**
//	 * 
//	 * @param code
//	 * @return
//	 */
//	public static String getNameByCode(Integer code){
//		PortProjectEnum[] values = PortProjectEnum.values();
//		for(PortProjectEnum ppe : values){
//			if(ppe.getCode().intValue() == code){
//				return ppe.getName();
//			}
//		}
//		return null;
//	}
//}
