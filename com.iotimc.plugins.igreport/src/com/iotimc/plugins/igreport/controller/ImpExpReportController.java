package com.iotimc.plugins.igreport.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.bean.parameter.request.RequestParameterUtil;
import com.iotimc.core.util.web.ResponseResultUtils;
import com.iotimc.plugins.igreport.service.ImpexpService;
import com.iotimc.plugins.igreport.util.ContentTypeUtils;
import com.iotimc.plugins.igreport.util.FileUtils;

/**
 * 报表导入导出接口
 * 
 * @author llb
 * @since 2017-07-17
 *
 */
@Controller
@RequestMapping("/plugins/igreport/impexp.do")
public class ImpExpReportController {
	
	@Autowired
	private ImpexpService impexpService;
	
	/**
	 * 导出指定ID集合的报表文件<p />
	 * 
	 * 请求格式：
	 * <pre id="request">
	 * {
	 * 	data:{
	 * 		"ids":["",""]
	 * 	}
	 * }
	 * </pre>
	 * 
	 * 响应格式：
	 * <pre id="response">
	 * {
	 * 	"result":{
	 * 		data:"文件名"
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=exportReport")
	public void exportReport(HttpServletRequest req,HttpServletResponse res) {
		String token = (String)req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE);
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			if(dataObject.getJSONArray("ids") == null || dataObject.getJSONArray("ids").isEmpty()) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("缺少需导出的报表列表", null, ""));
				return;
			}
			
			JSONObject result = new JSONObject();
			result.put("data", impexpService.exportIgreport(dataObject.getJSONArray("ids")));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表导出成功", token, result));
		} catch(Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 下载导出文件，文件名通过exportReport接口获取，文件名参数通过地址传值，参数名为name<p />
	 * 
	 * 请求格式：
	 * <pre id="request" type="url">
	 * /plugins/igreport/impexp.do?action=downExportFile&amp;name=filename.zip
	 * </pre>
	 * 
	 * 响应格式：
	 * <pre id="response">
	 * {
	 * 	//文件流
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=downExportFile")
	public void downExportFile(HttpServletRequest req,HttpServletResponse res) {
		String name = req.getParameter("name");
		
		File file = new File(FileUtils.getIgreportRootPath()+"/impexp/exp/zip/" + name);
		
		if(!file.exists()) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("下载文件失败，文件不存在", null, ""));
			return;
		}
		
		OutputStream output = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try{
			req.setCharacterEncoding("UTF-8");
			
			
			res.addHeader("Content-Length", file.length()+"");
			res.addHeader("Content-type", ContentTypeUtils.getMimeType(name));
			res.setHeader("Content-disposition", "attachment; filename=" + name);
			output = res.getOutputStream();
			
			bos = new BufferedOutputStream(output);
			
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			byte[] buff = new byte[2048];
			int bytesRead;
			while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff,0,bytesRead);	
			}
		}catch(Exception e){
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, null));
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 上传一个报表包并检测其唯一性<p />
	 * 
	 * 请求格式：
	 * <pre id="request" type="file">
	 * {
	 * 	//上传文件流
	 * }
	 * </pre>
	 * 
	 * 响应格式：
	 * <pre id="response">
	 * {
	 *  "result":{
	 *    "data":{
	 *      "impid":"",			//本次导入任务的ID
	 *      "check":[{
	 *        "code":"报表编号",	//报表编号
	 *        "name":"报表名",		//报表名
	 *        "unique":true		//是否唯一，false表示有重复，ture表示唯一
	 *      }]
	 *    }
	 *  }
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(params="action=checkImportReport")
	public void checkImportReport(HttpServletRequest req,HttpServletResponse res) {
		String token = (String)req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE);
		try {
			JSONObject result = new JSONObject();
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("utf-8");
			List<FileItem> fileList = upload.parseRequest(req);
			if(fileList.isEmpty()) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("缺少上传报表文件", token, result));
				return;
			}
			Iterator<FileItem> it = fileList.iterator();
			FileItem item = it.next();
			
			if(!item.getName().endsWith(".zip")) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("上传文件格式有误，请上传格式为zip的报表包", token, result));
				return;
			}
			
			byte[] fileBytes = item.get();
			result.put("data", impexpService.checkImportReport(item.getName(), fileBytes));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表包上传成功", token, result));
		} catch(Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}
	
	/**
	 * 导入已上传的报表<p />
	 * 
	 * 请求格式：
	 * <pre id="request">
	 * {
	 * 	"data":{
	 *   	"impid":"",	//通过checkImportReport接口获取的id
	 *   	"report":[
	 *   		"",""	//需导入的报表code集合，若有重复则覆盖处理
	 *   	]
	 * 	}
	 * }
	 * </pre>
	 * 
	 * 响应格式：
	 * <pre id="response">
	 * {
	 *  "result":{
	 *    "data":[{	//成功导入的报表集合
	 *      "id":"",	//报表ID
	 *      "code":"",	//报表编号
	 *      "name":""	//报表名
	 *    }]
	 *  }
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping(params="action=importReport")
	public void importReport(HttpServletRequest req,HttpServletResponse res) {
		String token = (String)req.getAttribute(RequestParameterUtil.TOKEN_ATTRIBUTE);
		try {
			JSONObject jsonObject = (JSONObject) req.getAttribute("json");
			JSONObject dataObject = jsonObject==null?new JSONObject():jsonObject.getJSONObject("data");
			
			if(StringUtils.isBlank(dataObject.getString("impid"))) {
				ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getErrorResult("缺少导入任务id标识", null, ""));
				return;
			}
			
			JSONObject result = new JSONObject();
			result.put("data", impexpService.importReport(dataObject.getString("impid"), dataObject.getJSONArray("report")));
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getSuccessResult("报表导入成功", token, result));
		} catch(Exception e) {
			e.printStackTrace();
			ResponseResultUtils.reponseResult(req, res, ResponseResultUtils.getExceptionResult(e, token));
		}
	}

}
