package com.iotimc.plugins.igreport.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.bean.parameter.request.RequestParameterUtil;
import com.iotimc.core.util.json.JsonUtil;
import com.iotimc.core.util.web.ResponseResultUtils;
import com.iotimc.plugins.igreport.service.ReportCtrlService;
import com.iotimc.plugins.igreport.service.ReportDataService;

/**
 * 报表管理部分接口
 * 
 * @author llb
 * @since 2017-07-07
 *
 */
@Controller
@RequestMapping("/plugins/igreport/reportctrl.do")
public class ReportCtrlController {

	@Autowired
	private ReportCtrlService reportCtrlService;
	
	@Autowired
	private ReportDataService reportDataService;
	
	/**
	 * 获取所有已存数据库的报表列表<P />
	 * 
	 * 请求格式如下：
	 * <pre id="request">
	 * {
	 * 
	 * }
	 * </pre>
	 * 
	 * 响应格式如下：
	 * <pre id="response">
	 * {
	 *	"result":{
	 *		"rows":[{
	 *			"id":"",		//报表ID
	 *			"code":"",		//报表名
	 *			"name":"",		//报表中文名
	 *			"img":"",		//报表缩略图base64
	 *			"cretime":"",	//创建日期的时间戳
	 *			"modtime":""	//修改日期的时间戳	
	 *		}]
	 *	} 
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=getIgreportList")
	public void getIgreportList(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject result = new JSONObject();
			result.put("rows", reportCtrlService.getIgreportList());
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表列表获取成功", token, result));
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 获取报表的明细内容<P />
	 * 
	 * 请求格式如下：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 * 		"id":""		//报表ID
	 * 	}
	 * }
	 * </pre>
	 * 
	 * 响应格式如下：
	 * <pre id="response">
	 * {
	 *	"result":{
	 *		"data":{
	 *			"id":"",		//报表ID
	 *			"code":"",		//报表名
	 *			"name":"",		//报表中文名
	 *			"img":"",		//报表缩略图base64
	 *			"jsonval": {	//报表详细内容
	 *			}
	 *		}
	 *	} 
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=getIgreportDetail")
	public void getIgreportDetail(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			if(dataObject.get("id") == null) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("报表明细数据获取失败，缺少报表id", token, null));
				return;
			}
			
			JSONObject result = new JSONObject();
			result.put("data", reportCtrlService.getIgreportDetail(dataObject.getString("id")));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表明细数据获取成功", token, result));
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 预览临时报表<p />
	 * 请求格式如下：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 *		"name": "罪犯在册趋势分析",
	 *		"code": "jyzcqsfx",
	 *		"dataname":"jyzcqsfx",
	 *		"theme": 1,
	 *		"includes": [],
	 *		"packages": []
	 * 	}
	 * 
	 * }
	 * </pre>
	 * 
	 * 响应json如下：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"code":""	//临时报表名
	 * 	}
	 * }
	 * </pre>
	 * 
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=viewTempReport")
	public void viewTempReport(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			JSONObject result = new JSONObject();
			result.put("code", reportCtrlService.buildTempView(dataObject));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表生成成功", token, result));
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 保存报表<p />
	 * 请求格式如下：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 *		"id": "",
	 *		"img":"",
	 *		"jsonval":{	//报表json
	 *		
	 *		}
	 * 	}
	 * 
	 * }
	 * </pre>
	 * 
	 * 响应json如下：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"id":"",	//报表ID
	 *		"img":"",	//报表缩略图base64
	 *		"jsonval":{	//报表json
	 *		
	 *		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=saveIgReport")
	public void saveIgReport(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			if(dataObject.get("jsonval") == null) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("数据保存失败，缺少报表数据", token, null));
				return;
			}
			
			JSONObject result = new JSONObject();
			String id = reportCtrlService.saveIgReport(dataObject.getString("id"), dataObject.getString("img"), 
					dataObject.getJSONObject("jsonval"));
			result.put("id", id);
			result.putAll(dataObject);
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表保存成功", token, result));
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 删除报表<p />
	 * 请求格式如下：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 *		"id": ""
	 * 	}
	 * 
	 * }
	 * </pre>
	 * 
	 * 响应json如下：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"id":""	//删除报表ID
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=deleteIgReport")
	public void deleteIgReport(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			reportCtrlService.deleteIgReport(dataObject.getString("id"));
			JSONObject result = new JSONObject();
			result.put("id", dataObject.getString("id"));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表删除成功", token, result));
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 上传一个kettle的转换脚本<p />
	 * 
	 * 请求格式：
	 * <pre id="request"  type="file">
	 * {
	 * 	//上传文件流
	 * }
	 * </pre>
	 * 
	 * 响应格式：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"dataname":""	//返回的数据集名
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(params="action=uploadktr")
	public void uploadktr(HttpServletRequest req,HttpServletResponse res) {
		String token = (String)req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE);
		try {
			JSONObject result = new JSONObject();
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("utf-8");
			List<FileItem> fileList = upload.parseRequest(req);
			if(fileList.isEmpty()) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("缺少转换脚本文件", token, result));
				return;
			}
			Iterator<FileItem> it = fileList.iterator();
			FileItem item = it.next();
			
			if(!item.getName().endsWith(".ktr")) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("上传文件格式有误，请上传格式为ktr的转换文件", token, result));
				return;
			}
			
			if(req.getParameter("params")!=null && !req.getParameter("params").equals("")){
				//String[] paramValue = new
				String params =  new String(req.getParameter("params").getBytes("ISO-8859-1"), "UTF-8");
				Map<String,Object> map = JsonUtil.parserToMap(params);
				Set<String> keys = map.keySet();  
				Iterator<String> eit = keys.iterator();
				String[] paramValue = new String[keys.size()];
				int i = 0;
				while(eit.hasNext()){
					paramValue[i++] = (String) map.get(eit.next());
				}
				byte[] fileBytes = item.get();
				result.put("dataname", reportCtrlService.saveKtrScript(fileBytes,paramValue));
			}else{
				byte[] fileBytes = item.get();
				result.put("dataname", reportCtrlService.saveKtrScript(fileBytes));
			}
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("kettle脚本上传成功", token, result));
		} catch(Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 预览某个数据集中的所有json<p />
	 * 请求参数如下：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 * 		"name":""	//数据集名
	 * 	}
	 * }
	 * </pre>
	 * 响应结果：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"data":{
	 * 			"name1":{		//文件1中的json
	 * 
	 * 			},
	 * 			"name2":{		//文件2中的json
	 * 
	 * 			}
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@Deprecated
	@RequestMapping(params="action=viewReportData")
	public void viewReportData(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		
		try {
//			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
//			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			JSONObject result = new JSONObject();
//			String dataname = dataObject.getString("name");
			JSONObject resultData = null;//reportDataService.viewReportData(dataname);
			if(resultData == null || resultData.isEmpty()) {
				result.put("data", new JSONObject());
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("未找到符合条件的数据集", token, result));
			}
//			else {
//				result.put("data", resultData);
//				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("获取数据集成功", token, result));	
//			}
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
		
	}
	
	
	/**
	 * 获取报表数据接口<p />
	 * 该接口会根据查询条件获取报表数据，如果未传查询条件则会获取所有数据<p />
	 * 
	 * <pre id="request">
	 * {
	 * 	"data":{
	 * 		"name":"",	//数据集名
	 * 		"args":{		//查询参数
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 响应结果：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		"data":{ //有传查询条件则对应结果集
	 * 			
	 * 		},
	 * 		"data":{ //如果没传查询条件则会查询所有结果集，并以文件名区分
	 * 			"name1":{		//文件1中的json
	 * 
	 * 			},
	 * 			"name2":{		//文件2中的json
	 * 
	 * 			}
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=getReportData")
	public void getReportData(HttpServletRequest req,HttpServletResponse res) {
		String token = String.valueOf(req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE));
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			JSONObject result = new JSONObject();
			JSONObject args = dataObject.getJSONObject("args");
			String dataname = dataObject.getString("name");
			JSONObject resultData = reportDataService.getReportData(dataname, args);
			if(resultData == null || resultData.isEmpty()) {
				result.put("data", new JSONObject());
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("未找到符合条件的数据集", token, result));
			} else {
				result.put("data", resultData);
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("获取报表数据成功", token, result));	
			}
		} catch (Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
}
