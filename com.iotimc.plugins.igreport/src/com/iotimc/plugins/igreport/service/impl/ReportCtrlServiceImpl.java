package com.iotimc.plugins.igreport.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.bean.security.UserDetail;
import com.iotimc.core.logger.IotimcLoggerFactory;
import com.iotimc.core.service.AdvancedService;
import com.iotimc.core.util.code.CodeFactory;
import com.iotimc.core.util.init.spring.AbstractInitializingBean;
import com.iotimc.core.util.osgi.OsgiServiceUtils;
import com.iotimc.core.util.thread.ThreadExecutor;
import com.iotimc.core.util.threadpool.ThreadPoolUtils;
import com.iotimc.plugins.igreport.domain.Igreport;
import com.iotimc.plugins.igreport.service.ReportCtrlService;
import com.iotimc.plugins.igreport.util.FileUtils;
import com.iotimc.plugins.igreport.util.KettleExecuteUtils;
import com.iotimc.plugins.igreport.util.KeyUtils;
import com.iotimc.plugins.igreport.util.ReportJsFormatUtils;

/**
 * 
 * @author llb
 *
 */
@Service
public class ReportCtrlServiceImpl extends AbstractInitializingBean implements ReportCtrlService {

	private static final Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(ReportCtrlServiceImpl.class);
	
	private AdvancedService advancedService;
	
	/**
	 * 临时文件保存时间，单位为分钟
	 */
	private final int chean_time_min = 2;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getIgreportList() {
		return (List<Map<String, Object>>) advancedService.getListByQueryKey("igreport.ctrl.getReportList", null);
	}

	@Override
	protected void afterBeanInitializing() throws Exception {
		advancedService = OsgiServiceUtils.getService(AdvancedService.class);
		ThreadExecutor.execNewThreadWithTransaction(new Runnable() {
			
			@Override
			public void run() {
			//	initIgreportFile();
			}
		}, "igreportInit");
		
		ThreadPoolUtils.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				cleanTempFile();
			}
			
		}, 1, chean_time_min, TimeUnit.MINUTES);
	}
	
	@SuppressWarnings("unchecked")
	private void initIgreportFile() {
		List<Igreport> igReportList = (List<Igreport>) advancedService.getListByQueryKey("igreport.ctrl.getReportEntityList", null);
		
		for(Igreport entity: igReportList) {
			formatIgreportFile(entity);
		}
	}
	
	/**
	 * 对某个报表执行格式化
	 * 
	 * @param igreport
	 */
	public void formatIgreportFile(Igreport igreport) {
		String code = igreport.getCode();
		logger.debug("igreport:正在初始化报表[{}-{}]", code, igreport.getName());
		
		JSONObject srcJson;
		try {
			srcJson = JSONObject.parseObject(igreport.getJsonval());
		} catch(Exception e) {
			logger.error("igreport:解析报表[" + code + "]原始json[" + 
					igreport.getJsonval() + "]出现异常", e);
			return;
		}
		
		String themePath = FileUtils.getIgreportRootPath() + "/theme/";
		String outPath = FileUtils.getIgreportRootPath() +"/report/"+ code;
		File outFile = new File(outPath);
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
		
		File[] outChildren = outFile.listFiles();
		for(File f : outChildren) {
			f.delete();
		}		
		ReportJsFormatUtils.format2Out(srcJson, themePath, outPath);
	}
	

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public String buildTempView(JSONObject json) {
		String tempId = "temp." + System.currentTimeMillis() + "." +java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
		tempId = tempId.toLowerCase();
		json.put("code", tempId);
		String outPath = FileUtils.getIgreportRootPath() + "/temp/" + tempId;
		File outFile = new File(outPath);
		if(!outFile.exists()) {
			outFile.mkdirs();
		}
		
		ReportJsFormatUtils.format2Out(json, FileUtils.getIgreportRootPath() + "/theme/", outPath);
		return tempId;
	}

	@Override
	public String saveIgReport(String id,String img, JSONObject json) {
		Igreport igreport;
		String code = json.getString("code");
		
		if(StringUtils.isBlank(id)) {
			List<?> list = advancedService.getHibernateQueryService().createCriteria(Igreport.class)
					.add(Restrictions.eq("code", code)).list();
			
			if(!list.isEmpty()) {
				throw new RuntimeException("编号["+ code +"]报表已被使用，无法重复保存");
			}
			
			igreport = new Igreport();
			igreport.setId((String) CodeFactory.newID(igreport));
			igreport.setCode(json.getString("code"));
			igreport.setName(json.getString("name"));
			igreport.setImg(img);
			igreport.setDataname(json.getString("dataname"));
			igreport.setJsonval(json.toJSONString());
			UserDetail.fillUserInfo(igreport, true);
			advancedService.insertEntity(igreport);
		} else {
			igreport = advancedService.getEntity(Igreport.class, id);
			
			if(!code.equals(igreport.getCode())) {
				List<?> list = advancedService.getHibernateQueryService().createCriteria(Igreport.class)
						.add(Restrictions.eq("code", code)).list();
				
				if(!list.isEmpty()) {
					throw new RuntimeException("编号["+ code +"]报表已被使用，无法重复保存");
				}
			}
			
			igreport.setCode(json.getString("code"));
			igreport.setName(json.getString("name"));
			igreport.setDataname(json.getString("dataname"));
			if(!StringUtils.isBlank(img)) {
				igreport.setImg(img);
			}
			igreport.setJsonval(json.toJSONString());
			UserDetail.fillUserInfo(igreport, false);
			advancedService.updateEntity(igreport);
		}
		formatIgreportFile(igreport);
		return igreport.getId();
	}
	
	@Override
	public void deleteIgReport(String id) {
		Igreport igreport = advancedService.getEntity(Igreport.class, id);
		String code = igreport.getCode();
		advancedService.deleteEntity(igreport);
		String reportFilePath = FileUtils.getIgreportRootPath() +"/report/"+ code;
		FileUtils.deleteFile(reportFilePath);
	}

	/**
	 * 清除所有临时目录下的报表文件
	 */
	private void cleanTempFile() {
		try {
			String outPath = FileUtils.getIgreportRootPath() + "/temp/";
			File tempPath = new File(outPath);
			File[] list = tempPath.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().startsWith("temp.");
				}
			});
			long now = System.currentTimeMillis();
			long limit = 1000*60*chean_time_min;
			String t_str;
			for(File f : list) {
				try {
					t_str = f.getName().split("\\.")[1];
					if((now - Long.parseLong(t_str)) >= limit) {
						FileUtils.deleteFile(f.getAbsolutePath());
					}
					f.delete();
				} catch(Exception e) {
					logger.error("igreport:删除临时文件[" + f.getAbsolutePath() + "]出现异常", e);
				}
			}
		} catch(Exception e) {
			logger.error("igreport:删除临时文件出现异常", e);
		}
	}

	@Override
	public Map<?, ?> getIgreportDetail(String id) {
		Igreport report = advancedService.getEntity(Igreport.class, id);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("id", report.getId());
		result.put("code", report.getCode());
		result.put("name", report.getName());
		result.put("img", report.getImg());
		result.put("jsonval", JSONObject.parseObject(report.getJsonval()));
		return result;
	}

	@Override
	public String saveKtrScript(byte[] fileBytes) {
		String rootPath = FileUtils.getIgreportRootPath() + "/data";
		String dataname = KeyUtils.getTimestampKey();
		
		File dataPath = new File(rootPath + "/" + dataname);
		dataPath.mkdirs();
		String filePath = rootPath + "/" + dataname + "/transform.ktr";
		FileUtils.outPutFile(filePath, fileBytes);
		KettleExecuteUtils.executeIgreportKtr(filePath);
		return dataname;
	}

	@Override
	public String saveKtrScript(byte[] fileBytes, String[] params) {
		String rootPath = FileUtils.getIgreportRootPath() + "/data";
		String dataname = KeyUtils.getTimestampKey();
		
		File dataPath = new File(rootPath + "/" + dataname);
		dataPath.mkdirs();
		String filePath = rootPath + "/" + dataname + "/transform.ktr";
		FileUtils.outPutFile(filePath, fileBytes);
		KettleExecuteUtils.executeIgreportKtr(filePath, params);
		return dataname;
	}
	
}
