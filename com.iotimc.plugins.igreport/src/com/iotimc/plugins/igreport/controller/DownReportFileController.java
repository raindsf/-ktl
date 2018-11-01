package com.iotimc.plugins.igreport.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iotimc.core.util.web.ResponseResultUtils;
import com.iotimc.core.web.authentication.RegisterCheckController;
import com.iotimc.plugins.igreport.util.ContentTypeUtils;
import com.iotimc.plugins.igreport.util.FileUtils;

@Controller
@RequestMapping("/plugins/igreport/downfile.do")
public class DownReportFileController implements RegisterCheckController{

	/**
	 * 下载报表文件<p />
	 * 
	 * <pre id="request" type="url">
	 * 	/plugins/igreport/downfile.do?code=code&amp;action=main.js
	 * </pre>
	 * 
	 * 响应内容：
	 * <pre id="response">
	 * {
	 * 	//文件流
	 * }   
	 * </pre>
	 * 
	 * @param req
	 * @param res
	 */
	@RequestMapping
	public void downFile(HttpServletRequest req,HttpServletResponse res){
		String code = req.getParameter("code");
		String fileName = req.getParameter("action");
		String rootPath;
		if(code.startsWith("temp.")) {
			rootPath = FileUtils.getIgreportRootPath() + "/temp/" + code;
		} else {
			rootPath = FileUtils.getIgreportRootPath() + "/report/" + code;	
		}
		
		File fileDir = new File(rootPath);
		if(!fileDir.exists()) fileDir.mkdirs();
		File file = new File(rootPath + "/" + fileName);
		
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
			res.addHeader("Content-type", ContentTypeUtils.getMimeType(fileName));
			res.setHeader("Content-disposition", "attachment; filename=" + fileName);
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
	
	
}
