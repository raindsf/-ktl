package com.iotimc.plugins.igreport.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.iotimc.core.logger.IotimcLoggerFactory;

public class FileUtils {
	
	private static final Logger logger = IotimcLoggerFactory.getConsoleAndFileLogger(FileUtils.class);
	
	private static String igreportRootPath = null;
	
	/**
	 * 获取报表根目录
	 * 
	 * @return
	 */
	public static String getIgreportRootPath() {
		if(igreportRootPath == null) {
			igreportRootPath = (new File(System.getProperty("catalina.base"))).getParent() + "/igreport";
		}
		return igreportRootPath;
	}
	
	/**
	 * 检测是否存在指定目录，如果不存在则创建，存在则返回
	 * 
	 * @param path
	 */
	public static void createFolder(String path) {
		File file = new File(path);
		if(!file.exists())
			file.mkdirs();
	}
	
	/**
	 * 获取某个目录下的某个类型文件的集合
	 * 
	 * @param rootPath
	 * @param fileType
	 * @return
	 */
	public static List<File> searchFile(String rootPath, String fileType) {
		File rootFile = new File(rootPath);
		List<File> list = new ArrayList<File>();
		dirFile(list, rootFile, fileType);
		return list;
	}
	
	private static void dirFile(List<File> list, File file, String fileType) {
		File[] files = file.listFiles();
		if(files == null || files.length == 0) return;
		String fileName, suffix;
		for(File f : files) {
			if(f.isDirectory()) {
				dirFile(list, f, fileType);
			} else {
				fileName = f.getName();
				suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
				if(suffix.equals(fileType)) {
					list.add(f);
				}
			}
		}
	}
	
	/**
	 * 通过inputStream将文件内容读取为字符串（使用utf-8字符集读取文件）
	 * 
	 * @param file
	 * @param useLineBreak 使用换行符
	 * @return
	 */
	public static String loadFileStream(File file, boolean useLineBreak) {
		BufferedReader bufferedReader = null;
		StringBuilder builder = new StringBuilder();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String str;
			while((str=bufferedReader.readLine()) != null) {
				builder.append(str);
				if(useLineBreak) builder.append("\n");
			}
		} catch(Exception e) {
			logger.error("读取文件["+ file.toString() + "]异常",e );
		} finally {
			if(bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error("读取文件["+ file.toString() + "]异常",e );
				}
		}
		return builder.toString();
	}
	
	/**
	 * 通过FileReader将文件内容读取为字符串
	 * 
	 * @param file
	 * @param useLineBreak
	 * @return
	 */
	public static String loadFileFromReader(File file, boolean useLineBreak) {
		BufferedReader bufferedReader = null;
		StringBuilder builder = new StringBuilder();
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String str;
			while((str=bufferedReader.readLine()) != null) {
				builder.append(str);
				if(useLineBreak)builder.append("\n");
			}
		} catch(Exception e) {
			logger.error("读取文件["+ file.toString() + "]异常",e );
		} finally {
			if(bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error("读取文件["+ file.toString() + "]异常",e );
				}
		}
		return builder.toString();
	}
	
	/**
	 * 从流中读取成字符串
	 * 
	 * @param in
	 * @return
	 */
	public static String loadFile(InputStream in) {
		BufferedReader bufferedReader = null;
		StringBuilder builder = new StringBuilder();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(in));
			String str;
			while((str=bufferedReader.readLine()) != null) {
				builder.append(str);
				builder.append("\n");
			}
		} catch(Exception e) {
			logger.error("读取文件流异常",e );
		} finally {
			if(bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error("读取文件流异常",e );
				}
		}
		return builder.toString();
	}
	
	
	/**
	 * 将content内如写入文件中
	 * 
	 * @param content
	 * @param filePath
	 */
	public static void writeFile(byte[] bs, File file) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(bs);
		} catch (Exception e) {
			logger.error("写入文件[" + file + "]异常",e );
		} finally {
			if (fileOutputStream != null)
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					logger.error("写入文件[" + file + "]异常",e );
				}
		}
	}
	
	/**
	 * 将content内如写入文件中
	 * 
	 * @param content
	 * @param filePath
	 */
	public static void writeFile(String content, File file) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(content.getBytes("UTF-8"));
		} catch (Exception e) {
			logger.error("写入文件[" + file + "]异常",e );
		} finally {
			if (fileOutputStream != null)
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					logger.error("写入文件[" + file + "]异常",e );
				}
		}
	}
	
	
	/**
	 * 删除某个目录下的所有文件
	 * 
	 * @param strPath
	 */
	public static void deleteFile(String strPath) {
		deleteFile(strPath, null);
	}
	
	/**
	 * 删除某个目录下的所有文件，并且可剔除某些目录
	 * 
	 * @param strPath
	 * @param exceptDirectory
	 */
	public static void deleteFile(String strPath, String[] exceptDirectory) {
		File dir = new File(strPath);        
		File[] files = dir.listFiles();               
		if (files == null)
		{
			logger.debug("该目录下没有任何一个文件！");
			return;
		}
		boolean isIgnore;
		String strFileName;
		for (int i = 0; i < files.length; i++) {            
			if (files[i].isDirectory()) {
				isIgnore = false;
				if(exceptDirectory != null) {
					for(String expDir : exceptDirectory){
						if(expDir.equals(files[i].getName())) {
							isIgnore = true;
							break;
						}
					}					
				}
				if(isIgnore) continue;
				deleteFile(files[i].getAbsolutePath(), exceptDirectory);
				logger.debug("正在删除[{}]", files[i].getAbsolutePath());
				files[i].delete();
			}else {
				
				strFileName = files[i].getAbsolutePath().toLowerCase();
				logger.debug("正在删除[{}]", strFileName);
				files[i].delete();
			}        
		}
		dir.delete();
	}
	
	/**
	 * 复制一个目录及其子目录、文件到另外一个目录
	 * @param src
	 * @param dest
	 */
	public static void copyFileDirectory(String src, String dest){
		File srcFile = new File(src);
		File destFile = new File(dest);
		copyFileDirectory(srcFile, destFile, null, null);
	}
	
	/**
	 * 复制一个目录及其子目录、文件到另外一个目录
	 * @param src
	 * @param dest
	 * @param expectType 无需拷贝的文件类型,格式为后缀名  .java 或者 .MF之类的
	 * @param includeType 指定需拷贝的文件类型
	 */
	public static void copyFileDirectory(String src, String dest, String[] expectType, String[] includeType){
		File srcFile = new File(src);
		File destFile = new File(dest);
		copyFileDirectory(srcFile, destFile, expectType, includeType);
	}
	
	/**
	 * 复制一个目录及其子目录、文件到另外一个目录
	 * @param src
	 * @param dest
	 * @param expectType 无需拷贝的文件类型,格式为后缀名  .java 或者 .MF之类的
	 * @param includeType 指定需拷贝的文件类型
	 */
	public static void copyFileDirectory(File src, File dest, String[] expectType, String[] includeType){
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// 递归复制
				copyFileDirectory(srcFile, destFile, expectType, includeType);
			}
		} else {
			String fileName = src.getName();
			if(expectType != null) {
				for(String str : expectType) {
					if(fileName.endsWith(str)) return;
				}
			}
			
			if(includeType != null) {
				boolean isinclude = false;
				for(String str : includeType) {
					if(fileName.endsWith(str)) { 
						isinclude = true;
						break;
					}
				}
				if(!isinclude) return;
			}
			
			
			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(src);
				out = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				
				int length;
				
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			} catch(Exception e) {
				
			} finally {
				try {
					if(in != null)
						in.close();
				} catch (IOException e) {
					
				}
				try {
					if(out != null)
						out.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
	/**
	 * 将指定文件拷贝到目标目录下
	 * 
	 * @param srcFile
	 * @param destDirectory
	 */
	public static void copyFileToDirectory(String srcFile, String destDirectory){
		copyFileToDirectory(new File(srcFile), new File(destDirectory));
	}
	
	/**
	 * 将文件复制为指定文件
	 * 
	 * @param srcFile
	 * @param destFile
	 */
	public static void copyFile(String srcFile, String destFile) {
		copyFile(new File(srcFile), new File(destFile));
	}
	
	/**
	 * 将文件复制为指定文件
	 * 
	 * @param srcFile
	 * @param destFile
	 */
	public static void copyFile(File srcFile, File destFile) {
		FileInputStream inputStream = null;
		try {
			if (srcFile == null || !srcFile.exists()) {
				logger.error("文件拷贝错误，源文件[{}]为空", srcFile);
				return;
			}
			inputStream = new FileInputStream(srcFile);
			outPutFile(destFile.getAbsolutePath(), inputStream);
		} catch(Exception e) {
			if(inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	
	/**
	 * 将指定文件拷贝到指定目录
	 * 
	 * @param srcFile
	 * @param dest
	 */
	public static void copyFileToDirectory(File srcFile, File destDirectory){
		if (!destDirectory.isDirectory()) {
			logger.error("拷贝文件[{}]到指定目录[{}]出错，[{}]不是目录", srcFile, destDirectory, destDirectory);
			return;
		}
		if(!destDirectory.exists())
			destDirectory.mkdirs();
		
		File dest = new File(destDirectory, srcFile.getName());
		InputStream in = null;
		OutputStream out = null;
		try {
			if(dest.exists()) {
				dest.delete();
			} else {
				dest.createNewFile();
			}
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			
			int length;
			
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		} catch(Exception e) {
			
		} finally {
			try {
				if(in != null)
					in.close();
			} catch (IOException e) {
				
			}
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
			}
		}
		
	}
	
	/**
	 * 将字节数组中的值输出成文件
	 * 
	 * @param filePath
	 * @param bs
	 * @return
	 */
	public static boolean outPutFile(String filePath, byte[] bs) {
		File file = new File(filePath);
		OutputStream out = null;
		try {
			file.createNewFile();
			out = new FileOutputStream(file);
			out.write(bs);
		} catch(Exception e) {
			logger.error("将字节数组内容输;出为指定文件[" + filePath + "]异常",e );
			return false;
		} finally {
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					
				}
		}
		return true;
	}
	
	/**
	 * 将输入流的内容输出为指定文件
	 * 
	 * @param file
	 * @param in
	 */
	public static void outPutFile(String file, InputStream in) {
		int fileSeparatorIndex = file.lastIndexOf("/");
		if(fileSeparatorIndex == -1)
			fileSeparatorIndex = file.lastIndexOf("\\");
		String path = file.substring(0, fileSeparatorIndex);
		File destPath = new File(path);
		if (!destPath.exists()) {
			destPath.mkdirs();
		}
		File destFile = new File(file);
		if(!destFile.exists())
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				logger.error("将输入流的内容输出为指定文件[" + file + "]异常",e );
			}
		
		
		OutputStream out = null;
		try {
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			
			int length;
			
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		} catch(Exception e) {
			
		} finally {
			try {
				if(in != null)
					in.close();
			} catch (IOException e) {
				
			}
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
			}
		}
		
		
	}
	
}
