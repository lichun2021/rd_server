package com.hawk.ms.common;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HawkBinLog {
	private static final Logger logger = LoggerFactory.getLogger("Binlog");
	//binLog的文件.
	public static  String BIN_LOG_PATH = "logs/Binlog.log";
	static final String SPLIT_OPERATOR = "@"; 
	
	public static void info(String str, Object...objects) {
		logger.info(SPLIT_OPERATOR + "" + str, objects);
	}
	
	public static void warn(String str, Object...objects) {
		logger.warn(SPLIT_OPERATOR + "" + str, objects);
	}
	
	public static void error(String str, Object...objects) {
		logger.error(SPLIT_OPERATOR + "" + str, objects);
	}
	
	/**
	 * 把日志从日志文件里面反向解析出来.
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static List<String> convert2StringList(String fileName) throws IOException {
		if (StringUtils.isEmpty(fileName)) {
			throw new RuntimeException("bin log file name is empty"); 
		}
		
		File file = new File(fileName);
		if (!file.exists()) {
			throw new RuntimeException(String.format("bin log :%s file not exist ", fileName));
		}  
		
		List<String> strList = FileUtils.readLines(file, "UTF-8");
		List<String> handledList = strList.stream().map(str->{
			String[] strs = str.split(SPLIT_OPERATOR);
			if (strs.length >= 1) {
				return strs[1];
			} else {
				return "";
			}
		}).filter(str1->{
			return !str1.equals("");
		}).collect(Collectors.toList());
		
		return handledList;
	}
	
	public static void insertTableFail(String dbName, String tableName) {
		info(Constants.InsertIdentify + ":" + dbName + ":" + tableName + ":" + false);
	}
	
	public static void insertTableSuccess(String dbName, String tableName) {
		info(Constants.InsertIdentify + ":" + dbName + ":" +  tableName + ":" + true);
	}
}
