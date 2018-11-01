package com.iotimc.plugins.igreport.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 键值工具类<P>
 * 可通过该类生成时间戳键值，以及UUID键值
 * 
 * @author llb
 * @since 2017-07-13
 *
 */
public class KeyUtils {

	/**
	 * 获取时间戳的键值
	 * 
	 * @return
	 */
	public synchronized static String getTimestampKey() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String key = dateFormat.format(new Date());
		return key;
	}
	
	/**
	 * 获取UUID键值
	 * 
	 * @return
	 */
	public static String getUUID() {
		return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
	
	
}
