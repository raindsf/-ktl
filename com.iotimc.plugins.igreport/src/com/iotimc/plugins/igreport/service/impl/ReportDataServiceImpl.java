package com.iotimc.plugins.igreport.service.impl;

import static com.jayway.jsonpath.JsonPath.parse;
import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.logger.IotimcLoggerFactory;
import com.iotimc.core.service.AdvancedService;
import com.iotimc.core.util.osgi.OsgiServiceUtils;
import com.iotimc.core.util.thread.ThreadExecutor;
import com.iotimc.core.util.threadpool.ThreadPoolUtils;
import com.iotimc.plugins.igreport.service.ReportDataService;
import com.iotimc.plugins.igreport.util.FileUtils;
import com.iotimc.plugins.igreport.util.KeyUtils;
import com.jayway.jsonpath.Filter;

@Service
public class ReportDataServiceImpl implements ReportDataService, InitializingBean {

	private static final Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(ReportDataServiceImpl.class);
	
	private AdvancedService advancedService;
	
	@Override
	public JSONObject getReportData(String dataname, JSONObject args) {
		if(args == null || args.isEmpty()) {
			return getAllReportData(dataname);
		}
		String dataPath = FileUtils.getIgreportRootPath() + "/data/" + dataname;
		File dataFile = new File(dataPath);
		if(!dataFile.exists()) {
			logger.error("数据集[{}]尚未初始化数据", dataname);
			return null;
		}
		File[] files = dataFile.listFiles(new JsonFileFilter());
		if(files == null || files.length == 0) {
			logger.error("数据集[{}]尚未初始化数据", dataname);
			return null;
		}
		String jsonStr;
		Filter filter = getJsonQueryStr(args);
		if(filter == null) {
			jsonStr = FileUtils.loadFileStream(files[0], false);
			return JSONObject.parseObject(jsonStr);
		}
		List<?> resList;
		for(File f : files) {
			jsonStr = FileUtils.loadFileStream(f, false);
			resList = parse(jsonStr).read("$..*[?]", filter);
			if(!resList.isEmpty()) {
				return JSONObject.parseObject(jsonStr);
			}
		}
		
		return null;
	}
	
	
	
	public JSONObject getAllReportData(String dataname) {
		String dataPath = FileUtils.getIgreportRootPath() + "/data/" + dataname;
		File dataFile = new File(dataPath);
		if(!dataFile.exists()) {
			logger.error("数据集[{}]尚未初始化数据", dataname);
			return null;
		}
		File[] files = dataFile.listFiles(new JsonFileFilter());
		if(files == null || files.length == 0) {
			logger.error("数据集[{}]尚未初始化数据", dataname);
			return null;
		}
		JSONObject result = new JSONObject();
		String name, jsonStr;
		for(File f : files) {
			name = f.getName();
			name = name.substring(0, name.lastIndexOf("."));
			jsonStr = FileUtils.loadFileStream(f, false);
			result.put(name, JSONObject.parseObject(jsonStr));
		}
		return result;
	}

	
	private Filter getJsonQueryStr(JSONObject args) {
		//TODO 生成查询条件
		if(args == null) return null;
	//	filter(where("category").is("fiction").and("price").lte(10D)
	//			);

		return null;
	}
	
	
	private class JsonFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith("json");
		}
		
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		advancedService = OsgiServiceUtils.getService(AdvancedService.class);
		ThreadPoolUtils.scheduleAtFixedRate(ThreadExecutor.createTransactionRunnable(new Runnable() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				List<String> datanameList = (List<String>) advancedService.getListByQueryKey("igreport.ctrl.getReportDatanameList", null);
				String dataPath = FileUtils.getIgreportRootPath() + "/data/";
				File datasFile = new File(dataPath);
				File[] files = datasFile.listFiles(new UnuseDataFileFilter(datanameList));
				if(files == null || files.length == 0) return;
				for(File f : files) {
					FileUtils.deleteFile(f.getAbsolutePath());
					f.delete();
				}
			}
		}), 0, 1, TimeUnit.HOURS);
	}
	
	private class UnuseDataFileFilter implements FileFilter {

		private List<String> useDatanameList;
		
		private long nowDate = Long.parseLong(KeyUtils.getTimestampKey());
		
		private long limit = 1000*60*30;
		
		public UnuseDataFileFilter(List<String> useDatanameList) {
			logger.info("useDatanameList的值是[{}]",useDatanameList);
			this.useDatanameList = useDatanameList;
		}

		@Override
		public boolean accept(File pathname) {
			logger.info("pathname的值是[{}]",pathname.getName());
			if(useDatanameList.contains(pathname.getName())) {
				logger.info("pathname的值在数据库列表中，不需要删除");
				return false;
			}
			try {
				Long fileDate = Long.parseLong(pathname.getName());
				if(nowDate - fileDate >= limit){
					logger.info("pathname需要删除,[{},{},{}]",nowDate,fileDate,(nowDate-fileDate));
					return true;
				}
					
			} catch(Exception e) {
				return true;
			}
			
			return false;
		}
		
	}
	
	
}
