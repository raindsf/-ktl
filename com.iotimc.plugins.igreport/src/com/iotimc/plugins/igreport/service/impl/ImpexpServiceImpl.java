package com.iotimc.plugins.igreport.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.logger.IotimcLoggerFactory;
import com.iotimc.core.service.AdvancedService;
import com.iotimc.core.util.code.CodeFactory;
import com.iotimc.core.util.osgi.OsgiServiceUtils;
import com.iotimc.core.util.threadpool.ThreadPoolUtils;
import com.iotimc.plugins.igreport.domain.Igreport;
import com.iotimc.plugins.igreport.service.ImpexpService;
import com.iotimc.plugins.igreport.service.ReportCtrlService;
import com.iotimc.plugins.igreport.util.FileUtils;
import com.iotimc.plugins.igreport.util.KeyUtils;
import com.iotimc.plugins.igreport.util.ZipUtil;

@Service
public class ImpexpServiceImpl implements ImpexpService, InitializingBean{

	private static final Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(ImpexpServiceImpl.class);
	
	private AdvancedService advancedService;
	
	@Autowired
	private ReportCtrlService reportCtrlService;
	
	@Override
	public String exportIgreport(JSONArray ids) {
		String expRootPath = FileUtils.getIgreportRootPath() + "/impexp/exp";
		File file = new File(expRootPath);
		if(!file.exists()) {
			file.mkdirs();
		}
		
		//创建导出文件的存放目录
		/*
		 * 整个导出文件目录就结构如下：
		 * 
		 * "根目录"{
		 * 	"报表code":{
		 * 		"igreport.json"
		 * 		"data":[
		 * 			"transform.ktr",
		 * 			"data1.json",
		 * 			"data2.json"
		 * 		]
		 * 	}
		 * }
		 * 
		 * 
		 * 
		 */
		String keyString = KeyUtils.getTimestampKey();
		File expFile = new File(expRootPath, keyString);
		expFile.mkdirs();
		
		File dataRootFile = new File(FileUtils.getIgreportRootPath() + "/data");
		
		String code;
		Igreport igreport;
		
		File reportFile, srcDataFile, targetDataFile;
		//byte[] bs;
		for(Object o : ids) {
			igreport = advancedService.getEntity(Igreport.class, o.toString());
			logger.debug("igreport报表导出:正在处理报表[{}-{}]", igreport.getName(), igreport.getCode());
			//创建报表根目录
			code = igreport.getCode();
			reportFile = new File(expFile, code);
			reportFile.mkdirs();
			/*
			try {
				bs = igreport.getJsonval().getBytes("UTF-8");
			} catch (Exception e) {
				bs = new byte[0];
			}
			
			for(byte b : bs){
				b = (byte) (b+256);
			}*/
			
			FileUtils.writeFile(igreport.getJsonval(), new File(reportFile, "igreport.js"));
			//FileUtils.writeFile(bs, new File(reportFile, "igreport.js"));
			
			//如果报表存在数据集，则讲报表数据集输出到data目录
			if(!StringUtils.isBlank(igreport.getDataname())) {
				targetDataFile = new File(reportFile, "data");
				targetDataFile.mkdirs();
				srcDataFile = new File(dataRootFile, igreport.getDataname());
				FileUtils.copyFileDirectory(srcDataFile.getAbsolutePath(), targetDataFile.getAbsolutePath());
			}
		}
		
		//将报表输出目录打包至	/igreport/impexp/exp/zip 下
		String zipOutPath = FileUtils.getIgreportRootPath() + "/impexp/exp/zip";
		File zipOutFile = new File(zipOutPath);
		if(!zipOutFile.exists()) {
			zipOutFile.mkdirs();
		}
		String zipName = keyString + ".zip";
		ZipUtil.zip(expFile.getAbsolutePath(), (new File(zipOutPath, zipName)).getAbsolutePath());
		return zipName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		advancedService = OsgiServiceUtils.getService(AdvancedService.class);
		
		ThreadPoolUtils.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				cleanTempFile();
			}
		}, 0, 1, TimeUnit.HOURS);
		
	}

	@Override
	public JSONObject checkImportReport(String fileName, byte[] filebytes) {
		String impRootPath = FileUtils.getIgreportRootPath() + "/impexp/imp";
		File file = new File(impRootPath);	
		if(!file.exists()) {
			file.mkdirs();
		}
		
		String keyString = KeyUtils.getTimestampKey();
		File impFile = new File(impRootPath, keyString);
		impFile.mkdirs();
		
		String impZipPath = impFile.getAbsolutePath() + "/" + fileName; 
		FileUtils.outPutFile(impZipPath, filebytes);
		try {
			ZipUtil.unzip(impZipPath);			
		} catch(Exception e) {
			logger.error("解压缩导入文件出现异常", e);
			throw new RuntimeException("解压缩报表导入文件出现异常", e);
		}
		
		List<File> jsonFileList = FileUtils.searchFile(impFile.getAbsolutePath(), "js");
		if(jsonFileList.isEmpty()) {
			throw new RuntimeException("未找到可解析的报表");
		}
		JSONArray checkList = new JSONArray();
		JSONObject reportJson, checkObject;
		List<?> list;
		for(File f : jsonFileList) {
			reportJson = JSONObject.parseObject(FileUtils.loadFileStream(f, false));
			list = advancedService.getHibernateQueryService().createCriteria(Igreport.class)
					.add(Restrictions.eq("code", reportJson.getString("code"))).list();
			
			checkObject = new JSONObject();
			checkObject.put("code", reportJson.getString("code"));
			checkObject.put("name", reportJson.getString("name"));
			checkObject.put("unique", list.isEmpty());
			checkList.add(checkObject);
		}
		
		JSONObject result  = new JSONObject();
		result.put("impid", keyString);
		result.put("check", checkList);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray importReport(String impid, JSONArray reports) {
		String impRootPath = FileUtils.getIgreportRootPath() + "/impexp/imp/" + impid;
		File impFile = new File(impRootPath);
		if(!impFile.exists()) {
			throw new RuntimeException("不存储目录为[" + impid + "]的报表");
		}
		List<File> jsonFileList = FileUtils.searchFile(impRootPath, "js");
		if(jsonFileList.isEmpty()) {
			throw new RuntimeException("未找到可解析的报表");
		}
		
		JSONObject reportJson;
		String dataname;
		List<Igreport> list;
		Igreport igreport;
		File srcDataFile, destDataFile;
		JSONArray result = new JSONArray();
		JSONObject resultObject;
		//byte[] bs;
		for(File f : jsonFileList) {
		/*	try {
				bs = FileUtils.loadFileStream(f, false).getBytes("UTF-8");
			} catch (Exception e) {
				bs = new byte[0];
			}
			
			for(byte b : bs){
				b = (byte) (b-256);
			}
			reportJson = JSONObject.parseObject(new String(bs));
			*/
			reportJson = JSONObject.parseObject(FileUtils.loadFileStream(f, false));
			if(!reports.contains(reportJson.get("code"))) continue;
			
			list = advancedService.getHibernateQueryService().createCriteria(Igreport.class)
					.add(Restrictions.eq("code", reportJson.getString("code"))).list();
			
			dataname = reportJson.getString("dataname");
			if(list.isEmpty()) {
				igreport = new Igreport();
				igreport.setId((String) CodeFactory.newID(igreport));
				igreport.setCode(reportJson.getString("code"));
				igreport.setName(reportJson.getString("name"));
				igreport.setImg(reportJson.getString("img"));
				igreport.setDataname(reportJson.getString("dataname"));
				igreport.setJsonval(reportJson.toJSONString());
				igreport.setModtime(new Date());
				igreport.setCretime(new Date());
				advancedService.insertEntity(igreport);
			} else {
				igreport = list.get(0);
				igreport.setCode(reportJson.getString("code"));
				igreport.setName(reportJson.getString("name"));
				igreport.setDataname(reportJson.getString("dataname"));
				igreport.setImg(reportJson.getString("img"));
				igreport.setModtime(new Date());
				igreport.setJsonval(reportJson.toJSONString());
				advancedService.updateEntity(igreport);
			}
			
			if(!StringUtils.isBlank(dataname)) {
				srcDataFile = new File(f.getParentFile(), "data");
				destDataFile = new File(FileUtils.getIgreportRootPath() + "/data/" + dataname);
				if(!destDataFile.exists()) {
					destDataFile.mkdirs();
				}
				FileUtils.copyFile(srcDataFile, destDataFile);
			}
			reportCtrlService.formatIgreportFile(igreport);
			
			resultObject = new JSONObject();
			resultObject.put("code", igreport.getCode());
			resultObject.put("name", igreport.getName());
			resultObject.put("id", igreport.getId());
			result.add(resultObject);
		}
		
		return result;
	}
	
	private void cleanTempFile(){
		cheanExportFiles();
		cheanImportFiles();
	}
	
	/**
	 * 清除超时的导出文件
	 */
	private void cheanExportFiles() {
		String expRootPath = FileUtils.getIgreportRootPath() + "/impexp/exp";
		File expRootFile = new File(expRootPath);
		
		long nowDate = Long.parseLong(KeyUtils.getTimestampKey());
		long limit = 1000*60*30;
		long fileDate;
		File[] children = expRootFile.listFiles();
		for(File f : children) {
			if(!f.isDirectory()) continue;
			if(f.getName().equals("zip")) {
				File[] childZips = f.listFiles();
				String name;
				for(File zip : childZips) {
					name = zip.getName();
					name = name.substring(0, name.indexOf("."));
					try {
						fileDate = Long.parseLong(name);
						if(nowDate - fileDate >= limit) {
							zip.delete();
						}						
					} catch(Exception e) {
						continue;
					}
				}
				continue;
			}
			try {
				fileDate = Long.parseLong(f.getName());
				if(nowDate - fileDate >= limit) {
					FileUtils.deleteFile(f.getAbsolutePath());
					f.delete();
				}
			} catch(Exception e) {
				continue;
			}
		}
	}
	
	/**
	 * 清除超时的导入文件
	 */
	private void cheanImportFiles() {
		String impRootPath = FileUtils.getIgreportRootPath() + "/impexp/imp";
		File impRootFile = new File(impRootPath);
		
		long nowDate = Long.parseLong(KeyUtils.getTimestampKey());
		long limit = 1000*60*30;
		
		File[] children = impRootFile.listFiles();
		long fileDate;
		for(File f : children) {
			if(!f.isDirectory()) continue;
			try {
				fileDate = Long.parseLong(f.getName());
				if(nowDate - fileDate >= limit) {
					FileUtils.deleteFile(f.getAbsolutePath());
					f.delete();
				}
			} catch(Exception e) {
				continue;
			}
		}
	}

}
