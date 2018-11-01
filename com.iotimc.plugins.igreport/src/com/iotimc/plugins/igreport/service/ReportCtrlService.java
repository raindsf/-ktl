package com.iotimc.plugins.igreport.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.iotimc.plugins.igreport.domain.Igreport;

/**
 * 本接口的功能为对报表的控制性功能
 * 
 * @author llb
 * @since 报表控制接口
 *
 */
public interface ReportCtrlService {

	/**
	 * 获取所有的报表列表<p>
	 * 
	 * 返回结果如下：
	 * <pre>
	 * [{
	 *	"id":"",		//报表ID
	 *	"code":"",		//报表名
	 *	"name":""		//报表中文名
	 *	"img":""		//报表缩略图base64
	 * }]
	 * </pre>
	 * 
	 * @return
	 */
	List<Map<String, Object>> getIgreportList();
	
	/**
	 * 获取报表明细信息<P>
	 * 
	 * 返回结果如下：
	 * <pre>
	 * {
	 *	"id":"",		//报表ID
	 *	"code":"",		//报表名
	 *	"name":""		//报表中文名
	 *	"img":""		//报表缩略图base64
	 *	"jsonval": {	//报表详细内容
	 *	}
	 * 
	 * }
	 * </pre>
	 * 
	 * @param id
	 * @return
	 */
	Map<?,?> getIgreportDetail(String id);
	
	/**
	 * 预览临时报表
	 * 
	 * @param json
	 * @return
	 */
	String buildTempView(JSONObject json);
	
	/**
	 * 保存igreport数据
	 * 
	 * @param id
	 * @param img	缩略图base64
	 * @param json
	 * @return
	 */
	String saveIgReport(String id, String img, JSONObject json);
	
	/**
	 * 删除某个报表
	 * 
	 * @param id
	 */
	void deleteIgReport(String id);
	
	
	/**
	 * 保存kettle脚本，并执行一遍上传的kettle标本
	 * 
	 * @param fileBytes
	 * @return
	 * @throws 如果kettle脚本执行异常，则抛出{@link RuntimeException}
	 */
	String saveKtrScript(byte[] fileBytes);
	
	/**
	 * 对某个报表执行格式化
	 * 
	 * @param igreport
	 */
	void formatIgreportFile(Igreport igreport);
	
	/**
	 * 保存kettle脚本，并执行一遍上传的kettle标本
	 * 
	 * @param fileBytes
	 * @param params
	 * @return
	 * @throws 如果kettle脚本执行异常，则抛出{@link RuntimeException}
	 */
	String saveKtrScript(byte[] fileBytes, String[] params);
	
}
