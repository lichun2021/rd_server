package com.hawk.ms.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.result.Result;

import com.hawk.ms.common.Constants;

public class SqlFileUtil {
	public static Result<String> executeSqlFile(MergeServerSqlSession conn, String fileName) throws IOException, SQLException {
		if (StringUtils.isEmpty(fileName)) {
			throw new RuntimeException("sql file name is empty");
		}
				
		File file = new File(fileName);
		if (!file.exists()) {
			throw new RuntimeException(String.format("file:%s not exist", fileName));
		}
		
		List<String> strList = FileUtils.readLines(file, "UTF-8");
		//过滤掉注释.
		List<String> handledList = strList.stream().filter(str->{
			if (str.trim().indexOf(Constants.SqlCommemnt) >= 0) {
				return false; 
			} else {
				if (str.trim().equals("")) {
					return false;
				}
				return true;
			}
		}).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		boolean flag = false;
		for (String str : handledList) {
			int updateCount = conn.executeUpdateAndGetCount(str, null, 0);
			if (flag) {
				sb.append("\r\n");
			}
			sb.append(String.format("sql : %s updateCount : %s", str, updateCount));
			flag = true;
		}
		
		if (sb.length() > 0) {
			sb.append("\r\n");			
		}
		
		sb.append("count:" + handledList.size());
		
		return Result.success(sb.toString());
	}	
}
