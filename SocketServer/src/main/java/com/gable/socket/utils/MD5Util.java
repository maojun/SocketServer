package com.gable.socket.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//MD5辅助类
public class MD5Util {
	public final static String MD5_SALT = "gable!@#$%^";
	public final static String md5(String requestTime){
		try {
			String str = MD5_SALT+requestTime;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));  
            byte b[] = md.digest();  
  
            int i;  
  
            StringBuffer buf = new StringBuffer("");  
            for (int offset = 0; offset < b.length; offset++) {  
                i = b[offset];  
                if (i < 0)  
                    i += 256;  
                if (i < 16)  
                    buf.append("0");  
                buf.append(Integer.toHexString(i));  
            }  
            //32位加密  
            return buf.toString();  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
            return null;  
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}  
	}
}