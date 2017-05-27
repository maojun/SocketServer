package com.gable.socket.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gable.socket.bean.JsonReturn;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.thread.FetchResult;
import com.gable.socket.thread.ReadSocketClientResult2;
import com.gable.socket.thread.WriteSocketClientParam;
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

	// 最大等待时间,默认四秒
	@Value("${MaxTime:4000}")
	private Long MaxTime;

	@RequestMapping(value = "/socketRequest", produces = "application/json; charset=utf-8")
	@ResponseBody
	public JsonReturn socketRequest(HttpServletRequest request) {
		// 返回结果
		JsonReturn jsonReturn = new JsonReturn();
		// socket传输对象
		SocketBean sb = null;
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
			if (port == null || port < 1) {
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
			log.info("_____ScoketController,socketRequest请求参数，body:" + JsonUtil.toJsonString(map));

			// socket传输对象
			sb = new SocketBean(serviceURL, JsonUtil.toJsonString(map));
			// socketClient返回结果对象
			SocketBean resultScoket = null;
			// 组装业务数据，发送给客户端
			// 根据不同的端口，写入对应的socket客户端
			InitUtil.executorService.execute(new WriteSocketClientParam(port, sb));
			// 短暂的间隔一下，保证写入客户端的操作在抓取客户端的操作之前
			Thread.sleep(100L);

			InitUtil.executorService.execute(new ReadSocketClientResult2(port, sb.getUid(), MaxTime));
			// 短暂的间隔一下，保证抓取客户端的结果在筛选返回结果之前
			Thread.sleep(100L);

			// 筛选对应的结果返回
			Future<SocketBean> fetch = InitUtil.executorService.submit(new FetchResult(sb.getUid(), MaxTime));

			// 短暂的间隔一下，保证抓取客户端的结果在筛选返回结果之前
			Thread.sleep(100L);
			//在限制时间内无法取到结果退出，避免线程阻塞
			resultScoket = fetch.get(MaxTime, TimeUnit.MILLISECONDS);
			// 抓取客户端返回的结果
			if (resultScoket != null) {
				Integer code = resultScoket.getCode();
				jsonReturn.setRet(code);
				jsonReturn.setData(resultScoket.getResult());
				jsonReturn.setMsg(resultScoket.getErrorMsg());
				InitUtil.resultMap.remove(resultScoket.getUid());
			} else {
				jsonReturn.setRet(0);
				jsonReturn.setMsg("网络异常,请稍后再试");
			}
		} catch (Exception e) {
			log.error("_____ScoketController,socketRequest异常：" + e.toString());
			jsonReturn.setRet(0);
			jsonReturn.setMsg("网络异常,请稍后再试!");
		}
		// 移除结果
//		if (sb != null)
			log.info("==========服务端缓存结果集："+JsonUtil.toJsonString(InitUtil.resultMap));
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
