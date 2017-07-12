package com.gable.socket.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.gable.socket.bean.JsonReturn;
import com.gable.socket.bean.SocketBean;
import com.gable.socket.bean.SocketObject;
import com.gable.socket.thread.FetchResult;
import com.gable.socket.thread.FileUploadThread;
import com.gable.socket.thread.ReadSocketClientResult;
import com.gable.socket.thread.WriteSocketClientParam;
import com.gable.socket.utils.InitUtil;
import com.gable.socket.utils.JsonUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
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

	@Value("${FILEURL}")
	private String FILEURL;

	@Value("${ENDPOINT}")
	private String ENDPOINT;

	@Value("${ACCESSKEYID}")
	private String ACCESSKEYID;

	@Value("${ACCESSKEYSECRET}")
	private String ACCESSKEYSECRET;

	@Value("${BUCKETNAME}")
	private String BUCKETNAME;

	@Value("${LOCALSAVEPATH}")
	private String LOCALSAVEPATH;

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


			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String paraName = (String) parameterNames.nextElement();
				map.put(paraName, request.getParameter(paraName));
			}
			
			// 有文件时需要校验文件地址
			if (haveFile.equals("Y")) {
				String fileAddress = request.getParameter("fileAddress");
				if (StringUtils.isEmpty(fileAddress)) {
					log.error("fileAddress为空");
					return new JsonReturn(0, "fileAddress为空");
				}
				// 将文件替换成中转文件地址
				// http://xxxx/1.jpg,http://xxxx/2.jpg ---->
				// D:/gable/1.jpg,D:/gable/2.jpg
				String localfileAddress = fileAddress.replaceAll(FILEURL, LOCALSAVEPATH);
				map.put("fileAddress", localfileAddress);
				// 有文件时需要将文件从al下载下来写到117本地磁盘中
				InitUtil.executorService.execute(new FileUploadThread(fileAddress, FILEURL, ENDPOINT, ACCESSKEYID,
						ACCESSKEYSECRET, BUCKETNAME, LOCALSAVEPATH));
			}
			
			log.info("_____ScoketController,socketRequest请求参数，body:" + JsonUtil.toJsonString(map));

			// socket传输对象
			sb = new SocketBean(serviceURL, JsonUtil.toJsonString(map));
			// socketClient返回结果对象
			SocketBean resultScoket = null;
			// 组装业务数据，发送给客户端
			// 根据不同的端口，写入对应的socket客户端
			log.info("befor.size:" + InitUtil.skMap.get(port).size());
			SocketObject socketObject = InitUtil.getSocketObject(port);
			log.info("after.size:" + InitUtil.skMap.get(port).size());
			InitUtil.executorService.execute(new WriteSocketClientParam(port, sb, socketObject));
			// 短暂的间隔一下，保证写入客户端的操作在抓取客户端的操作之前
			Thread.sleep(100L);

			InitUtil.executorService.execute(new ReadSocketClientResult(port, sb.getUid(), MaxTime, socketObject));
			// 短暂的间隔一下，保证抓取客户端的结果在筛选返回结果之前
			Thread.sleep(100L);

			// 筛选对应的结果返回
			Future<SocketBean> fetch = InitUtil.executorService.submit(new FetchResult(sb.getUid(), MaxTime));

			// 短暂的间隔一下，保证抓取客户端的结果在筛选返回结果之前
			Thread.sleep(100L);
			// 在限制时间内无法取到结果退出，避免线程阻塞
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
		// if (sb != null)
		log.info("==========服务端缓存结果集：" + JsonUtil.toJsonString(InitUtil.resultMap));
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

	/**
	 * 读取文件，将文件流返回
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getBytes", method = RequestMethod.POST)
	@ResponseBody
	public String getBytes(HttpServletRequest request) {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		try {
			String filePaths = request.getParameter("filePath");
			String[] fileArray = filePaths.split(",");
			for (int i = 0; i < fileArray.length; i++) {
				String filePath = fileArray[i];
				File file = new File(filePath);
				FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
				byte[] b = new byte[1000];
				int n;
				while ((n = fis.read(b)) != -1) {
					bos.write(b, 0, n);
				}
				fis.close();
				bos.close();
				byte[] buffer = bos.toByteArray();
				String isoString = new String(buffer, "ISO-8859-1");
				Map<String, String> map = new HashMap<String, String>();
				map.put(filePath, isoString);
				resultList.add(map);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return JsonUtil.toJsonString(resultList);
	}

	/**
	 * 接受文件流，将文件上传到阿里云服务器
	 * 
	 * @return
	 */
	@RequestMapping(value = "/writeFile", method = RequestMethod.POST)
	@ResponseBody
	public boolean writeFile(HttpServletRequest request) {
		String fileInfo = request.getParameter("fileInfo");
		String key = request.getParameter("fileName");
		if (StringUtils.isEmpty(fileInfo) || StringUtils.isEmpty(key)) {
			return false;
		}
		ByteArrayInputStream byteArrayInputStream = null;
		OSSClient ossClient = null;
		try {
			BASE64Decoder decode = new BASE64Decoder();
			byte[] bytes = decode.decodeBuffer(fileInfo);
			ossClient = new OSSClient(ENDPOINT, ACCESSKEYID, ACCESSKEYSECRET);
			boolean bl = ossClient.doesBucketExist(BUCKETNAME);
			if (!bl) {
				ossClient.createBucket(BUCKETNAME);
			}
			byteArrayInputStream = new ByteArrayInputStream(bytes);
			ossClient.putObject(BUCKETNAME, key, byteArrayInputStream);

			if (ExistObject(key, ossClient)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				byteArrayInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ossClient.shutdown();
		}

		return false;
	}

	/**
	 * 判断当前文件是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean ExistObject(String key, OSSClient ossClient) {
		boolean bl = true;
		bl = ossClient.doesObjectExist(BUCKETNAME, key);
		ossClient.shutdown();
		return bl;
	}

	/**
	 * 根据文件名从ali云上获取文件，转成流返回
	 * 
	 * @return
	 */
	@RequestMapping(value = "/fetchFile", method = RequestMethod.POST)
	@ResponseBody
	public String fetchFile(HttpServletRequest request) {
		String fileName = request.getParameter("fileName");
		if (StringUtils.isEmpty(fileName)) {
			return null;
		}
		OSSClient ossClient = null;
		try {
			// 初始化客戶端
			ossClient = new OSSClient(ENDPOINT, ACCESSKEYID, ACCESSKEYSECRET);
			// 多文件下载，替换文件域名，写入本地磁盘。
			// http://xxxxx/dispatch/639b566d-f6e2-4b0a-8dfb-1fab4465345b.jpg
			// 替换成 dispatch/639b566d-f6e2-4b0a-8dfb-1fab4465345b.jpg
			OSSObject ossObject = ossClient.getObject(BUCKETNAME, fileName);
			log.info("_____文件名称：" + fileName);
			if (ossObject != null) {
				InputStream in = ossObject.getObjectContent();
				byte[] bytes = input2byte(in);
				in.close();
				BASE64Encoder encode = new BASE64Encoder();
				String by = encode.encode(bytes);
				return by;
			}
		} catch (Exception e) {
			log.error("_____文件操作失败：" + e.toString());
		} finally {
			if (ossClient != null) {
				ossClient.shutdown();
			}
		}
		return null;
	}

	public final byte[] input2byte(InputStream inStream) throws IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = inStream.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
}
