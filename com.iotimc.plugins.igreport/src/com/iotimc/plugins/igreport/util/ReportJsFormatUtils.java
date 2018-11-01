package com.iotimc.plugins.igreport.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iotimc.core.logger.IotimcLoggerFactory;

/**
 * 报表文件操作的工具类<P>
 * 
 * 整个报表目录结构如下：
 * 报表目录结构如下：
 * <pre>
 * virgo:{
 * 	igreport:{
 * 		theme:[	//模板目录
 * 			1.js,
 * 			2.js
 * 		],
 * 		report:[{	//存放所有子报表目录
 * 			xxxxx : [	//具体报表目录
 * 				"xxxx.js",
 * 				"xxxx.less"
 * 			]
 * 		}],
 * 		data:{
 * 			xxxxx:[
 * 				"xxx.ktr",
 * 				"xxx.json",
 * 				"xxx.json"
 * 			]
 * 		}
 * 	}
 * }
 * </pre> 
 * @author llb
 * @since 2017-07-07
 *
 */
public class ReportJsFormatUtils {

	private static final Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(ReportJsFormatUtils.class);
	
	//private static final Logger logger = LoggerFactory.getLogger(ReportJsFormatUtils.class);
	
	
	/**
	 * 将原始参数<code>json</code>按照指定目录格式化，输出到<code>outPath</code>目录
	 * 
	 * @param code	报表名
	 * @param json	需做解析处理的json
	 * @param themePath	模板所在目录
	 * @param outPath	解析后文件输出目录
	 */
	public static void format2Out(JSONObject json, String themePath, String outPath) {
		String code, theme, dataname, js, html, css, less;
		JSONArray includes, packages;
		String originalJS;
		StringBuffer jss = new StringBuffer();
		
		code = json.getString("code");
		dataname = json.getString("dataname");
		theme = json.getString("theme");
		includes = json.getJSONArray("includes");
		packages = json.getJSONArray("packages");
		js = json.getString("js");
		html = json.getString("html");
		css = json.getString("css");
		less = json.getString("less");
		// theme
		if(StringUtils.isBlank(theme)) theme = "1";
		originalJS = FileUtils.loadFileStream(new File(themePath + theme + ".js"), true);
		
		//name
		originalJS = originalJS.replace("{{code}}", code);
		
		// dataname
		originalJS = originalJS.replace("{{dataname}}", dataname);
		
		// js
		if(!StringUtils.isBlank(js)) {
			jss.append(js);
			originalJS = originalJS.replace("{{js}}", jss.toString());
		} else {
			originalJS = originalJS.replace("{{js}}", "");
		}

		// css
		if(!StringUtils.isBlank(css)) {
			originalJS = originalJS.replace("{{css}}", ",'cssloader!plugins/igreport/downfile.do?code=" + code + "&action=main.css'");
		} else {
			originalJS = originalJS.replace("{{css}}", "");
		}
		
		// less
		if(!StringUtils.isBlank(less)) {
			originalJS = originalJS.replace("{{less}}", ",'less!plugins/igreport/downfile.do?code=" + code + "&action=main.less'");
		} else {
			originalJS = originalJS.replace("{{less}}", "");
		}
		
		// html
		if(!StringUtils.isBlank(html)) {
			originalJS = originalJS.replace("{{html}}", "'" + html + "'");			
		} else {
			originalJS = originalJS.replace("{{html}}", "''");
		}
		
		// includes
		if(includes != null && !includes.isEmpty()) {
			StringBuffer ins = new StringBuffer();
			Iterator<?> iter = includes.iterator();
			
			while(iter.hasNext()){
				ins.append(",'");
				ins.append(iter.next());
				ins.append("'");
			}
			originalJS = originalJS.replace("{{includes}}", ins.toString());
		} else {
			originalJS = originalJS.replace("{{includes}}", "");
		}
		
		
		// packages
		if(packages !=null && !packages.isEmpty()) {
			StringBuffer packages_state = new StringBuffer();
			StringBuffer packages_quote = new StringBuffer();
			String packageName = "";
			
			Iterator<?> iter = packages.iterator();
			
			while(iter.hasNext()){
				packageName = (String) iter.next();
				packages_state.append("'");
				packages_state.append(packageName);
				packages_state.append("', ");
				packages_quote.append(", ");
				packages_quote.append(packageName);
			}
			originalJS = originalJS.replace("{{packages_state}}", packages_state.toString());
			originalJS = originalJS.replace("{{packages_quote}}", packages_quote.toString());
		} else {
			originalJS = originalJS.replace("{{packages_state}}", "");
			originalJS = originalJS.replace("{{packages_quote}}", "");
		}
		
		logger.debug("igreport:报表[{}]格式化后的js为[{}]", code, originalJS);
		writeOut(outPath, "main.js", originalJS);
		if(!StringUtils.isBlank(css)) {
			logger.debug("igreport:报表[{}]格式化后的css为[{}]", code, css);
			writeOut(outPath, "main.css", css);
		}
		if(!StringUtils.isBlank(less)) {
			logger.debug("igreport:报表[{}]格式化后的less为[{}]", code, less);
			writeOut(outPath, "main.less", less);			
		}
	}
	
	
	private static void writeOut(String outPath, String name, String data) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outPath + "/" + name)), "UTF-8"));
			writer.write(data);
		} catch(Exception e) {
			System.err.println(e);
		} finally {
			try {
				if(writer != null) {
					writer.flush();
					writer.close();
				}
			} catch(Exception e) {
				System.err.println(e);
			}
		}	
	}
	
	
}
