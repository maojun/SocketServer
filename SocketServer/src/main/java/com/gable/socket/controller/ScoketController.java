package com.gable.socket.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gable.socket.bean.JsonReturn;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.service.SocketServerService;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

/**
 * 
 * @author mj
 *
 */
@Controller
@RequestMapping("/socket")
public class ScoketController {
	Logger log = Logger.getLogger(ScoketController.class);

	@Autowired
	SocketServerService socketServerService;

	@RequestMapping(value = "/socketRequest", produces = "application/json; charset=utf-8")
	@ResponseBody
	public JsonReturn socketRequest(HttpServletRequest request) {
		// 返回结果
		JsonReturn jsonReturn = new JsonReturn();

		try {
			// 获取请求参数
			String hospitalId = request.getHeader("hospitalId"); // 医院ID
			String serviceURL = request.getHeader("serviceURL"); // 请求转发地址
			String haveFile = request.getHeader("haveFile"); // 是否包含文件
			
			// 基本参数校验
			if (StringUtils.isEmpty(hospitalId) || StringUtils.isEmpty(serviceURL) || StringUtils.isEmpty(haveFile)) {
				log.error("hospitalId,serviceURL,haveFile不能为空");
				return new JsonReturn(0, "hospitalId,serviceURL,haveFile不能为空");
			}
			// 校验医院ID 和socket端口的映射关系
			Long hId = Long.parseLong(hospitalId);
			Integer port = InitUtil.hospitalIdPortMap.get(hId);
			if(port == null || port < 1){
				log.error("hospitalId无效,映射socket失败");
				return new JsonReturn(0, "hospitalId无效,映射socket失败");
			}
			
			// 获取所有的业务参数
			Map<String, Object> map = new HashMap<String, Object>();
						
			// 有文件时需要校验文件地址
			if (haveFile.equals("Y")) {
				String fileAddress = request.getParameter("fileAddress");
				if (StringUtils.isEmpty(fileAddress)) {
					log.error("fileAddress为空");
					return new JsonReturn(0, "fileAddress为空");
				}
				map.put("fileAddress", fileAddress);
			}
			
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String paraName = (String) parameterNames.nextElement();
				map.put(paraName, request.getParameter(paraName));
			}
			log.info("_____ScoketController,socketRequest请求参数，body:"+JsonUtil.toJsonString(map));
			
			// socket传输对象
			SocketBean sb = new SocketBean(serviceURL, JsonUtil.toJsonString(map));
			// 组装业务数据，发送给客户端
			// 根据不同的端口，写入对应的socket客户端
			socketServerService.outPutSocketToClient(port, sb);
			Thread.sleep(10L);
			// 抓取客户端返回的结果
			SocketBean resultScoket = socketServerService.inPutSocketFromClient(InitUtil.hospitalIdPortMap.get(hId),
					sb.getUid());
			if (resultScoket != null) {
				Integer code = resultScoket.getCode();
				jsonReturn.setRet(code);
				jsonReturn.setData(resultScoket.getResult());
				jsonReturn.setMsg(resultScoket.getErrorMsg());
			} else {
				jsonReturn.setRet(2);
			}
		} catch (Exception e) {
			log.error("_____ScoketController,socketRequest异常："+e.toString());
			jsonReturn.setRet(0);
			jsonReturn.setMsg(e.getMessage());
		}
		return jsonReturn;
	}

	/**
	 * token 校验失败
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/filterError", produces = "application/json; charset=utf-8")
	@ResponseBody
	public JsonReturn filterError(HttpServletRequest request) {
		JsonReturn json = new JsonReturn();
		String result = request.getAttribute("error").toString();
		json.setRet(0);
		json.setMsg(result);
		return json;
	}
}
