package com.hawk.ms.common;

public class Constants {
	/**
	 * sql的注释
	 */
	public static String SqlCommemnt = "-- ";
	/**
	 * 更新的标识符
	 */
	public static String UpdateIdentify = "MergeServerUpdate";
	/**
	 * 插入的标识符
	 */
	public static String InsertIdentify = "MergeServerInsert";  
	/**
	 * 属性分隔符
	 */
	public static String AttributeCutter = ",";
	/**
	 * 行分隔符
	 */
	public static String RowSpliter = ";";
	/**
	 * 列分隔符
	 */
	public static String ColumnSpliter = ",";
	
	
	public static class ObjType {
		// 应用程序
		public static final int MANAGER = 100; 
	}
	
	/**
	 * 系统对象id
	 *
	 * 
	 */
	public static class ObjId {
		public static final int APP = 1; 
	}
}
