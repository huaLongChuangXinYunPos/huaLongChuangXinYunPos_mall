package com.example.hlcloundposproject.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.os.Build;

/**
 * 封装网络访问   的工具类
 * @author hl_zq
 * zhaoq_hero@163.com
 */
public final class HttpTools {
	
	/**
	 * 私有化构造方法   该类 不可以被实例化
	 */
	private HttpTools(){}
	
	//设置链接超时时间
	private static final int CONNECTION_TIMEOUT= 10000;
	//设置  读取超时时间：
	private static final int READ_TIMEOUT = 10000;
//	//设置请求数据的请求头信息：
	public static final String USER_AGENT = 
			"hlrjCloundPos_1.0.0(" + Build.MODEL + "," + Build.VERSION.SDK_INT + ")";
	
	@SuppressWarnings("unused")
	private static InputStream in = null;

	/**
	 * 使用   doGet方法 访问  网络数据   返回字节数组、
	 * @param url
	 * @return
	 */
	public static byte[] doGet(String urlStr) {
		byte[] ret = null;
	
		if(urlStr!=null){
			HttpURLConnection conn = null;
			try {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				
				//设置超时时间
				conn.setConnectTimeout(CONNECTION_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				
				conn.setRequestMethod("GET");
				//设置Accept头信息    
				conn.setRequestProperty("Accept", "appliction/*,text/*,image/*,*/*");
				//设置内容压缩格式
				conn.setRequestProperty("Accept-Encoding", "gzip");
//				//设置  user-agent
				conn.setRequestProperty("User-Agent", USER_AGENT);
				
				conn.connect();
				
				int code = conn.getResponseCode();
				if(code == 200){
					InputStream fin = conn.getInputStream();
					//进行网络输入流的压缩：    获取头字段 
					String encoding = conn.getHeaderField("Content-Encoding");
					
					if("gzip".equals(encoding)){
						fin = new GZIPInputStream(fin);//使用  gzip 压缩格式
					}
					
					//读取  字节流
					ret = StreamUtils.readStream(fin);
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				StreamUtils.close(conn);
			}
		}
		return ret;
	}
	
	/**
	 * doPost方法：
	 * @throws UnsupportedEncodingException 
	 */
	public static void doPost(String url,HashMap<String,String> map) throws UnsupportedEncodingException{
		
		if(url!=null){
			StringBuilder sb = new StringBuilder();
			
			for(Map.Entry<String,String> en : map.entrySet()){
				sb.append(en.getKey())
						.append("=")
						.append(URLEncoder.encode(en.getValue(),"utf-8"));
			}
			
			HttpURLConnection connection = null;
			
			try {
				URL u = new URL(url);
				
				connection = (HttpURLConnection) u.openConnection();
				
				//基本设置：
				connection.setRequestMethod("POST");
				connection.setConnectTimeout(CONNECTION_TIMEOUT);
				connection.setDoInput(true);
				connection.setDoOutput(true);
				
				//设置 提交的数据的类型：
				connection.setRequestProperty("Content-Type", 
						"application/x-www-form-urlencoded");
				connection.connect();
				//设置提交的数据：
				byte[] b = sb.toString().getBytes();
				
				//提交数据   向服务器  写入数据
				OutputStream outputStream = connection.getOutputStream();
				outputStream.write(b,0,b.length);
				outputStream.close();
				
				in = null;
				
				if(connection.getResponseCode()==200){
					in=connection.getInputStream();
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
