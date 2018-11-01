package com.iotimc.plugins.igreport.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 报表导入导出接口，导入导出文件结构如下<p>
 * <pre>
 * {
 * 	"包名":{
 * 		"报表名" : [
 * 			"jsonval.json",
 * 			"data":[
 * 				"transform.ktr",
 * 				"data.json"
 * 			]
 * 		]
 * 	}
 * }
 * </pre>
 * 
 * 
 * @author llb
 * @since 2017-07-17
 *
 */
public interface ImpexpService {
	
	/**
	 * 导出指定id的报表集合
	 * 
	 * @param ids
	 * @return
	 */
	String exportIgreport(JSONArray ids);
	

	/**
	 * 检测某个导入报表的唯一性<p>
	 * 返回检测结果如下：
	 * <pre>
	 * {
	 *   "impid":"",			//本次导入任务的ID
	 *   "check":[{
	 * 	  "code":"报表编号",	//报表编号
	 * 	  "name":"报表名",		//报表名
	 * 	  "unique":true		//是否唯一，false表示有重复，ture表示唯一
	 *   }]
	 * }
	 * </pre>
	 * 
	 * @param filebytes 导入文件的字节数组
	 * @param fileName 文件名
	 * @return 返回保存成功的报表ID
	 */
	JSONObject checkImportReport(String fileName, byte[] filebytes);
	
	
	/**
	 * 导入临时目录中名称为impid的reports中的报表集合<p>
	 * 返回成功导入的报表数据集，返回格式如下：
	 * <pre>
	 * [{	//成功导入的报表集合
	 *      "id":"",	//报表ID
	 *      "code":"",	//报表编号
	 *      "name":""	//报表名
	 * }]
	 * </pre>
	 * 
	 * @param impid
	 * @param reports
	 * @return
	 */
	JSONArray importReport(String impid, JSONArray reports);
	
}
