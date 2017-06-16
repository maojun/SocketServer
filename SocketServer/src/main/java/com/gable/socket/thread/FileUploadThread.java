package com.gable.socket.thread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;

/**
 * 从云服务器上下载数据写入磁盘
 * 
 * @author mj
 *
 */
public class FileUploadThread implements Runnable {

	Logger log = Logger.getLogger(FileUploadThread.class);

	private String filePath;

	private String ENDPOINT;
	private String ACCESSKEYID;
	private String ACCESSKEYSECRET;
	private String BUCKETNAME;
	private String LOCALSAVEPATH;
	
	public FileUploadThread(String filePath,String FILEURL,String ENDPOINT,String ACCESSKEYID,String ACCESSKEYSECRET
			,String BUCKETNAME,String LOCALSAVEPATH) {
		this.filePath = filePath;
		this.ENDPOINT = ENDPOINT;
		this.ACCESSKEYID = ACCESSKEYID;
		this.ACCESSKEYSECRET = ACCESSKEYSECRET;
		this.BUCKETNAME = BUCKETNAME;
		this.LOCALSAVEPATH = LOCALSAVEPATH;
	}

	@Override
	public void run() {
		OutputStream ouputStream = null;
		OSSClient ossClient = null;
		try {
			//初始化客戶端
			ossClient = new OSSClient(ENDPOINT,ACCESSKEYID,ACCESSKEYSECRET);
			//多文件下载，替换文件域名，写入本地磁盘。
			//http://xxxxx/dispatch/639b566d-f6e2-4b0a-8dfb-1fab4465345b.jpg
			//替换成  dispatch/639b566d-f6e2-4b0a-8dfb-1fab4465345b.jpg
			String[] fileArray = filePath.split(",");
			List<String> fileList = replaceFile(fileArray);
			for (int i = 0; i < fileList.size(); i++) {
				String fileName = fileList.get(i);
				OSSObject ossObject = ossClient.getObject(BUCKETNAME, fileName);
				if (ossObject != null) {
					InputStream in = ossObject.getObjectContent();
					byte[] bytes = input2byte(in);
					in.close();
					File file = new File(LOCALSAVEPATH+fileName);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					ouputStream = new FileOutputStream(file);
					ouputStream.write(bytes);
					ouputStream.flush();
				}
			}
			ossClient.shutdown();
		} catch (Exception e) {
			log.error("_____文件操作失败："+e.toString());
		}finally{
			try {
				ouputStream.close();
				ossClient.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
	
	public List<String> replaceFile(String [] fileArray){
		List<String> fileList = new ArrayList<String>();
		String FILEURL="http://gable-hospital.oss-cn-shanghai.aliyuncs.com/";
		for (int i = 0; i < fileArray.length; i++) {
			String file = fileArray[i].replace(FILEURL, "");
			fileList.add(file);
		}
		return fileList;
	}
}
