package com.iotimc.plugins.igreport.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 实现获取报表数据的接口
 * 
 * @author llb
 * @since 2017-07-06
 */
public interface ReportDataService {

	/**
	 * 获取某个报表的显示值
	 * 
	 * @param dataname
	 * @param args
	 * @return
	 */
	JSONObject getReportData(String dataname, JSONObject args);
	
	/**
	 * 获取某个数据集的所有数据
	 * 
	 * @param dataname
	 * @return
	 
	JSONObject viewReportData(String dataname);
	*/
}
