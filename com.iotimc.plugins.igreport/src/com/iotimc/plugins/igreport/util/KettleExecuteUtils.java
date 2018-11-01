package com.iotimc.plugins.igreport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;

import com.iotimc.core.logger.IotimcLoggerFactory;

/**
 * kettle执行工具类
 * 
 * @author llb
 * @since 2017-07-31
 *
 */
public class KettleExecuteUtils {

	private final static Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(KettleExecuteUtils.class);
	//private final static Logger logger = LoggerFactory.getLogger(KettleExecuteUtils.class);
	
	@SuppressWarnings("unchecked")
	public static void executeIgreportKtr(String ktrPath) {
		SAXReader reader = new SAXReader();
		File f = new File(ktrPath);
		XMLWriter writer = null;
		try {
			Document document = reader.read(f);
			Element rootElm = document.getRootElement();
			List<Element> steps = rootElm.selectNodes("//step");
			Element typeElm, pathElm;
			String outputPath;
			for(Element step : steps) {
				typeElm = (Element) step.selectSingleNode("type");
				if("JsonOutput".equals(typeElm.getText())) {
					pathElm = (Element) step.selectSingleNode("file/name");
					outputPath = pathElm.getText();
					outputPath = outputPath.replaceAll("\\"+File.separator, "\\/");
					pathElm.setText(f.getParent() + "/" + outputPath.substring(outputPath.lastIndexOf("/") + 1));
				}
			}
			
			writer = new XMLWriter(new FileOutputStream(f));
			writer.write(document);
			executeTransformScript(f.getAbsolutePath());
		} catch(Exception e) {
			logger.error("执行kettle脚步出现异常", e);
			throw new RuntimeException("执行kettle脚步出现异常" , e);
		} finally {
			if(writer != null) { 
				try {
					writer.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void executeIgreportKtr(String ktrPath, String[] params) {
		SAXReader reader = new SAXReader();
		File f = new File(ktrPath);
		XMLWriter writer = null;
		try {
			Document document = reader.read(f);
			Element rootElm = document.getRootElement();
			List<Element> steps = rootElm.selectNodes("//step");
			Element typeElm, pathElm;
			String outputPath;
			for(Element step : steps) {
				typeElm = (Element) step.selectSingleNode("type");
				if("JsonOutput".equals(typeElm.getText())) {
					pathElm = (Element) step.selectSingleNode("file/name");
					outputPath = pathElm.getText();
					outputPath = outputPath.replaceAll("\\"+File.separator, "\\/");
					pathElm.setText(f.getParent() + "/" + outputPath.substring(outputPath.lastIndexOf("/") + 1));
				}
			}
			
			writer = new XMLWriter(new FileOutputStream(f));
			writer.write(document);
			executeTransformScript(params, f.getAbsolutePath());
		} catch(Exception e) {
			logger.error("执行kettle脚步出现异常", e);
			throw new RuntimeException("执行kettle脚步出现异常" , e);
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	
	public static void executeTransformScript(String path) throws Exception {
		Trans trans = null; 
		try {
			// 初始化  
			// 转换元对象  
			KettleEnvironment.init();// 初始化  
			EnvUtil.environmentInit();  
			TransMeta transMeta = new TransMeta(path);  
            // 转换
			trans = new Trans(transMeta);
			// 执行转换
			trans.setLogLevel(LogLevel.BASIC);
			trans.execute(null);
            // 等待转换执行结束  
			trans.waitUntilFinished();  
            // 抛出异常  
			if (trans.getErrors() > 0) {  
				throw new Exception("无法执行转换，传输过程中发生异常");  
            }  
        } catch (Exception e) {  
            throw e;  
        }
	}
	
	public static void executeTransformScript(String[] params,String path) throws Exception {
		Trans trans = null; 
		try {
			// 初始化  
			// 转换元对象  
			KettleEnvironment.init();// 初始化  
			EnvUtil.environmentInit();  
			TransMeta transMeta = new TransMeta(path);  
            // 转换
			trans = new Trans(transMeta);
			// 执行转换
			trans.setLogLevel(LogLevel.BASIC);
			trans.execute(params);
            // 等待转换执行结束  
			trans.waitUntilFinished();  
            // 抛出异常  
			if (trans.getErrors() > 0) {  
				throw new Exception("无法执行转换，传输过程中发生异常");  
            }  
        } catch (Exception e) {  
            throw e;  
        }
	}
	
}
