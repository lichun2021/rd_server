package com.hawk.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志类
 * 
 * @author lating
 * 
 */
public class RobotLog {
	/**
	 * 错误码日志配置对象
	 */
	static Logger errorLogger = LoggerFactory.getLogger("Err");
	/**
	 * 城内日志配置对象
	 */
	static Logger cityLogger = LoggerFactory.getLogger("City");
	/**
	 * 世界日志配置对象
	 */
	static Logger worldLogger = LoggerFactory.getLogger("World");
	/**
	 * 联盟日志配置对象
	 */
	static Logger guildLogger = LoggerFactory.getLogger("Guild");
	/**
	 * 活动日志配置对象
	 */
	static Logger activityLogger = LoggerFactory.getLogger("Activity");

	/**
	 * 调试模式输出
	 * 
	 * @param msg
	 */
	public static void cityDebugPrintln(String msg, Object... params) {
		cityLogger.debug(msg, params);
	}

	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public static void cityPrintln(String msg, Object... params) {
		cityLogger.info(msg, params);
	}

	/**
	 * 错误打印
	 * 
	 * @param msg
	 */
	public static void cityErrPrintln(String msg, Object... params) {
		cityLogger.error(msg, params);
	}
	
	/**
	 * 调试模式输出
	 * 
	 * @param msg
	 */
	public static void worldDebugPrintln(String msg, Object... params) {
		worldLogger.debug(msg, params);
	}

	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public static void worldPrintln(String msg, Object... params) {
		worldLogger.info(msg, params);
	}

	/**
	 * 错误打印
	 * 
	 * @param msg
	 */
	public static void worldErrPrintln(String msg, Object... params) {
		worldLogger.error(msg, params);
	}

	/**
	 * 调试模式输出
	 * 
	 * @param msg
	 */
	public static void guildDebugPrintln(String msg, Object... params) {
		guildLogger.debug(msg, params);
	}

	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public static void guildPrintln(String msg, Object... params) {
		guildLogger.info(msg, params);
	}

	/**
	 * 错误打印
	 * 
	 * @param msg
	 */
	public static void guildErrPrintln(String msg, Object... params) {
		guildLogger.error(msg, params);
	}
	
	/**
	 * 调试模式输出
	 * 
	 * @param msg
	 */
	public static void errDebugPrintln(String msg, Object... params) {
		errorLogger.debug(msg, params);
	}

	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public static void errPrintln(String msg, Object... params) {
		errorLogger.info(msg, params);
	}
	
	/**
	 * 调试模式输出
	 * 
	 * @param msg
	 */
	public static void activityDebugPrintln(String msg, Object... params) {
		activityLogger.debug(msg, params);
	}

	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public static void activityPrintln(String msg, Object... params) {
		activityLogger.info(msg, params);
	}

	/**
	 * 错误打印
	 * 
	 * @param msg
	 */
	public static void activityErrPrintln(String msg, Object... params) {
		activityLogger.error(msg, params);
	}
	
}
