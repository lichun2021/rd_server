package com.hawk.game.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.email.HawkEmailService;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

/**
 * 异常信息上报工具类
 * 
 * @author lating
 *
 */
public class ExceptionReportUtil {
	/**
	 * 异常信息时间记录
	 */
	private static Map<String, Long> exceptionInfoMap = new ConcurrentHashMap<>();
	/**
	 * 配置文件提交人报错时间记录
	 */
	private static Map<String, Long> configAuthorExpTimeMap = new ConcurrentHashMap<>();
	/**
	 * 异常信息
	 */
	private static List<String> exceptionMsgInfos = Arrays.asList(
			 "com.hawk.game.global.RedisProxy.getServerIdentify",
	         "com.hawk.game.GsApp.onClosed", 
	         "com.hawk.game.GsApp.onShutdown",
	         "com.hawk.l5.L5Helper.l5Task",
	         "com.hawk.game.service.SysOpService.logGrep",
	         "ERR wrong number of arguments for 'hmset' command",
	         "ERR wrong number of arguments for 'sadd' command",
	         "MySQLNonTransientConnectionException",
	         "Data too long for column 'formationInfo' at row",
	         "Data too long for column 'giftIdTime' at row");
	/**
	 * svn仓库
	 */
	private static SVNRepository repository;
	
	/**
	 * 初始化
	 */
	static {
		// 只有在指定的测试环境下才会启用
		if (GsConfig.getInstance().isDebug() && GsConfig.getInstance().isReportException2Feishu()) {
			String[] authInfo = GsConfig.getInstance().getSvnAuth().split(":");
			if (authInfo.length >= 2) {
				try {
					// 初始化http://和https://协议格式的svn连接所需的环境
					DAVRepositoryFactory.setup();
					// 初始化svn://和svn+xxx://协议格式的svn连接所需的环境
					SVNRepositoryFactoryImpl.setup();
					// 初始化file:///协议格式的svn连接所需的环境
					FSRepositoryFactory.setup();
					
					// 建立同svn仓库的链接
					repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(GsConfig.getInstance().getDebugSvrSvnAddr()));
					@SuppressWarnings("deprecation")
					ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(authInfo[0], authInfo[1]);
					repository.setAuthenticationManager(authManager);
				} catch (Exception e) {
					repository = null;
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 通过飞书上报异常信息
	 * 
	 * @param throwable
	 * @param params
	 */
	public static void reportException2Feishu(String targetUrl, Throwable throwable, Object... params) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}
		
		if (!GsApp.getInstance().isInitOK()) {
			postExceptionMsg2Feishu(targetUrl, throwable, params);
			return;
		} 
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				postExceptionMsg2Feishu(targetUrl, throwable, params);
				return null;
			}
		});
	}
	
	/**
	 * 通过邮件形式上报异常信息
	 * @param throwable
	 * @param params
	 */
	public static void reportException2Mail(Throwable throwable, Object... params) {
		if (!HawkEmailService.getInstance().checkValid()) {
			return;
		}

		String title = String.format("%s_exception", GsConfig.getInstance().getGameId());
		StringBuilder content = new StringBuilder(2048);
		// 游戏名
		content.append("Game: " + GsConfig.getInstance().getGameId());
		// 服务器id
		content.append("\r\nSid: " + GsConfig.getInstance().getServerId());
		// 异常参数
		if (params != null && params.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("\r\nArgs:\r\n");
			for (int i = 0; i < params.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(params[i].toString());
			}
			content.append(sb.toString());
		}
		// 堆栈信息
		content.append("\r\nStack:\r\n" + HawkException.formatStackMsg(throwable));

		HawkEmailService.getInstance().sendEmail(title, content.toString(), GsConfig.getInstance().getAlarmEmails());
	}

	/**
	 * post异常消息
	 * @param targetUrl
	 * @param throwable
	 * @param params
	 */
	private static void postExceptionMsg2Feishu(String targetUrl, Throwable throwable, Object... params) {
		// 异常信息按照错误打印
		String excepMsg = HawkException.formatStackMsg(throwable);
		List<String> filterList = GsConfig.getInstance().getExceptionFilterList();
		if (filterList.stream().filter(e -> excepMsg.indexOf(e) >= 0).findAny().isPresent()) {
			return;
		}
		if (exceptionMsgInfos.stream().filter(e -> excepMsg.indexOf(e) >= 0).findAny().isPresent()) {
			return;
		}
		
		String errorInfo = assembleErrorInfo(excepMsg, throwable, params);
		if (HawkOSOperator.isEmptyString(errorInfo)) {
			return;
		}
		
		String userId = GsConfig.getInstance().getServerOwnerId();
		JSONObject paramJson = new JSONObject();
		paramJson.put("msg_type", "text");
		JSONObject httpContent = new JSONObject();
		if (!HawkOSOperator.isEmptyString(userId)) {
			String ownerName = GsConfig.getInstance().getServerOwnerName();
			httpContent.put("text", "<at user_id=\"" + userId + "\">" + ownerName + "</at> " + errorInfo);
		} else {
			httpContent.put("text", errorInfo);
		}
		
		paramJson.put("content", httpContent);
		//正常通知异常信息
		errorNotify(paramJson, targetUrl);

		String serverId = GsConfig.getInstance().getServerId();
		String cehuaUrl = GsConfig.getInstance().getTargetCehuaUrl();
		// 如果是debug服或milestone服，再给QA同步一份
		if (targetUrl.equals(cehuaUrl) && (serverId.equals("50002") || serverId.equals("50003"))) {
			errorNotify(paramJson, GsConfig.getInstance().getTargetQAUrl());
		}
	}
	
	private static void errorNotify(JSONObject paramJson, String targetUrl) {
		// 服务器启动过程中报错，通过shell命令发送
		if (!GsApp.getInstance().isInitOK()) {
			String exceptionMsg = paramJson.toJSONString();
			StringBuilder sb = new StringBuilder();
	        try {
			    Process ps = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", "curl -X POST -H \"Content-Type: application/json\" -d '" + exceptionMsg + "' " + targetUrl});
			    ps.waitFor();
			    BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			    String line;
			    while ((line = br.readLine()) != null) {
			        sb.append(line).append(HawkScript.HTTP_NEW_LINE);
			    }
			} catch (Exception e) {
				HawkLog.errPrintln("shell report exception to feishu failed, exception: {}, info: {}", exceptionMsg, sb.toString());
			}
		} else {
			JSONObject header = new JSONObject();
			header.put("Content-Type", "application/json");
			paramJson.put("header", header);
			String exceptionMsg = paramJson.toJSONString();
			try {
				HawkHttpUrlService.getInstance().doPost(targetUrl, exceptionMsg, 3000);
			} catch (Exception e) {
				HawkLog.errPrintln("httpuril report exception to feishu failed, exception info: {}", exceptionMsg);
			}
		}
	}
	
	/**
	 * 拼接异常信息
	 * @param throwable
	 * @param args
	 * @param excepMsg
	 * @return
	 */
	private static String assembleErrorInfo(String excepMsg, Throwable throwable, Object... params) {
		// 异常参数
		String args = null;
		if (params != null && params.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < params.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(params[i].toString());
			}
			args = sb.toString();
		}
				
		if (!GsApp.getInstance().isInitOK()) {
			return assembleSpeErrorInfo(excepMsg, args, throwable, params);
		}
		
		StringBuilder keyInfoBuiller = new StringBuilder();
		String mapKey = "";
		try {
			StackTraceElement[] stackArray = throwable.getStackTrace();
			for (int i = 0; stackArray != null && i < stackArray.length; i++) {
				StackTraceElement element = stackArray[i];
				String stackInfo = element.toString(); 
				// com.hawk.game.script.TestOperationHandler.action(TestOperationHandler.java:172)
				if (stackInfo.startsWith("com.hawk.activity") || stackInfo.startsWith("com.hawk.game")) {
					String[] infos = stackInfo.split("\\.");
					String[] methodClassName = infos[infos.length - 2].split("\\(");
					keyInfoBuiller.append("类名：").append(methodClassName[1]);
					String last = infos[infos.length - 1];
					String lineNumber = last.substring(last.indexOf(":") + 1, last.length() - 1);
					keyInfoBuiller.append(", 方法名：").append(methodClassName[0]).append(", 代码行数：").append(lineNumber);
					mapKey = String.format("%s-%s-%s", methodClassName[1], methodClassName[0], lineNumber);
					
					String path = stackInfo.substring(stackInfo.indexOf("com.hawk"), stackInfo.indexOf(methodClassName[0])-1);
					HawkTuple2<String, String> tuple = getSvnCommitInfo(path);
					if (!HawkOSOperator.isEmptyString(tuple.first)) {
						keyInfoBuiller.append(", 文件提交人：").append(tuple.first).append(", 最近提交时间：").append(tuple.second);
					}
					break;
				}
			}
		} catch (Exception e) {
			HawkLog.logPrintln("postExceptionMsg2Feishu error, StackTraceElement loop exception, exception msg: {}", e.getMessage());
		}
		
		long timeNow = 0;
		// 同类异常在60s内不重发
		if (!HawkOSOperator.isEmptyString(mapKey) && (timeNow = HawkTime.getMillisecond()) - exceptionInfoMap.getOrDefault(mapKey, 0L) < 60000L) {
			return null;
		}
		
		exceptionInfoMap.put(mapKey, timeNow);
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(GsConfig.getInstance().getServerId());
		return String.format("%s  ERROR  [serverId-%s-%s]  -  \n关键信息 -> 【%s】 \nxid: null, args: %s\n%s", 
				HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), GsConfig.getInstance().getServerId(), 
				serverInfo == null ? "" : serverInfo.getName(), keyInfoBuiller.toString(), args, excepMsg);
	}
	
	/**
	 * trunk、debug、ms服拼接异常信息
	 * 
	 * @param excepMsg
	 * @param args
	 * @param throwable
	 * @param params
	 * @return
	 */
	private static String assembleSpeErrorInfo(String excepMsg, String args, Throwable throwable, Object... params) {
		String svnAddr = GsConfig.getInstance().getSvnAddr();
		if (!GsConfig.getInstance().isSpecialServer() || HawkOSOperator.isEmptyString(svnAddr)) {
			return String.format("%s  ERROR  [serverId-%s-%s]  -  \nxid: null, args: %s\n%s", 
					HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), GsConfig.getInstance().getServerId(), GsConfig.getInstance().getServerName(), args, excepMsg);
		}
		
		String fileName = "";
		String throwableMsg = throwable.getMessage();
		try {
			StackTraceElement[] stackArray = throwable.getStackTrace();
			for (int i = 0; stackArray != null && i < stackArray.length; i++) {
				String stackInfo = stackArray[i].toString();
				int preIndex = Math.max(0,i-1);
				fileName = checkCondition(stackInfo, stackArray[preIndex], throwableMsg);
				if (!HawkOSOperator.isEmptyString(fileName)) {
					break;
				}
			}
		} catch (Exception e) {
			HawkLog.logPrintln("assembleSpeErrorInfo error, StackTraceElement loop exception, exception msg: {}", e.getMessage());
		}
		
		if (HawkOSOperator.isEmptyString(fileName)) {
			return String.format("%s  ERROR  [serverId-%s-%s]  -  \nxid: null, args: %s\n%s", 
					HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), 
					GsConfig.getInstance().getServerId(), GsConfig.getInstance().getServerName(), 
					args, excepMsg);
		}
		
		String svnFile = svnAddr + fileName;
		String commitAuthor = checkSvnCommitInfo(svnFile);
		String authorName = GsConfig.getInstance().getSvnAuthorName(commitAuthor);
		String authorId = GsConfig.getInstance().getSvnAuthorId(commitAuthor);
		// 没有匹配到对应的svn提交人
		if (HawkOSOperator.isEmptyString(authorId)) {
			return String.format("%s  ERROR  [serverId-%s-%s]  - \nxid: null, args: %s\n%s \n配置文件: %s/%s, 提交人: %s", 
					HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), 
					GsConfig.getInstance().getServerId(), GsConfig.getInstance().getServerName(), 
					args, excepMsg, GsConfig.getInstance().getServerName(), fileName, commitAuthor);
		}
		
		long now = HawkTime.getMillisecond();
		long lastTime = configAuthorExpTimeMap.getOrDefault(authorId, 0L);
		configAuthorExpTimeMap.put(authorId, now);
		// 第一次或距离上次大于10s
		if (now - lastTime > 10000L) {
			return String.format("%s  ERROR  [serverId-%s-%s]  - \nxid: null, args: %s\n%s \n配置文件: %s/%s,  提交人: %s %s", 
					HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), 
					GsConfig.getInstance().getServerId(), GsConfig.getInstance().getServerName(), 
					args, excepMsg, GsConfig.getInstance().getServerName(), fileName, commitAuthor, 
					"<at user_id=\"" + authorId + "\">" + authorName + "</at> ");
		} 
		
		return String.format("%s  ERROR  [serverId-%s-%s]  - \nxid: null, args: %s\n%s \n配置文件: %s/%s, 提交人: %s", 
				HawkTime.formatNowTime(HawkTime.FORMAT_MILLISECOND), 
				GsConfig.getInstance().getServerId(), GsConfig.getInstance().getServerName(), 
				args, excepMsg, GsConfig.getInstance().getServerName(), fileName, commitAuthor);
	}
	
	/**
	 * 条件匹配
	 * @param stackInfo
	 * @param throwableMsg
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private static String checkCondition(String stackInfo, StackTraceElement preStackElement, String throwableMsg) throws ClassNotFoundException {
		// throw new HawkException("config file not exist: " + filePath[0]); -- activity/new_first_recharge/%s/new_first_recharge_cfg.xml
		if (stackInfo.endsWith("loadXmlConfig(HawkConfigStorage.java:175)")) {
			return getConfigFilename(throwableMsg, false);
		}
		
		// throw new HawkException("config class field not exist, file: " + filePath[0] + ", field: " + attrNode.getNodeName().trim());
		if (stackInfo.endsWith("loadXmlConfig(HawkConfigStorage.java:224)")) {
			return getConfigFilename(throwableMsg, true);
		}
		
		// throw new HawkException("config id annotation duplicate: " + filePath[0] + ", nodeName: "+ attrNode.getNodeName());
		if (stackInfo.endsWith("loadXmlConfig(HawkConfigStorage.java:235)")) {
			return getConfigFilename(throwableMsg, true);
		}
		
		// throw new Exception("config assemble failed: " + cfgClass.getName() + ", key: " + cfgKey + ", file: " + filePath[0]);
		if (stackInfo.endsWith("loadXmlConfig(HawkConfigStorage.java:256)")) {
			return getConfigFilename(throwableMsg, false);
		}
		
		// throw new Exception("config key duplicate: " + filePath[0] + ", key: " + cfgKey);
		if (stackInfo.endsWith("loadXmlConfig(HawkConfigStorage.java:269)")) {
			return getConfigFilename(throwableMsg, true);
		}
		
		String[] segArr = stackInfo.split("\\.");
		String segInfo = segArr[segArr.length - 2];
		if (segInfo.startsWith("assemble(") || segInfo.startsWith("checkValid(")) {
			String className = stackInfo.substring(0, stackInfo.indexOf(segInfo) - 1);
			return getConfigFilenameByClassName(className);
		}
		
		if (stackInfo.endsWith("loadKVConfig(HawkConfigStorage.java:333)") && preStackElement != null && preStackElement.toString().endsWith("setAttr(HawkConfigStorage.java:403)")) {
			String className = throwableMsg.substring(throwableMsg.indexOf("com.hawk"));
			return getConfigFilenameByClassName(className);
		}
		
		// java.lang.RuntimeException: com.hawk.game.config.PayGiftCfg
		if (stackInfo.endsWith("checkConfigData(HawkConfigManager.java:447)")) {
			String className = throwableMsg.substring(throwableMsg.indexOf("com.hawk"));
			return getConfigFilenameByClassName(className);
		}
		
		return null;
	}
	
	/**
	 * 获取配置文件的文件名
	 * @param lastLineInfo
	 * @param cut
	 * @return
	 */
	private static String getConfigFilename(String lastLineInfo, boolean cut) {
		String[] segArr = lastLineInfo.split("\\/");
		String lastSeg = segArr[segArr.length - 1];
		String fileName = segArr[segArr.length - 2];
		if (cut) {
			fileName = fileName + "/" + lastSeg.substring(0, lastSeg.indexOf(","));
		} else {
			fileName = fileName + "/" + lastSeg;
		}
		
		if ("activity".equals(segArr[segArr.length - 3]) || "activity".equals(segArr[segArr.length - 4])) {
			fileName = segArr[segArr.length - 3] + "/" + fileName;
			fileName = "activity".equals(segArr[segArr.length - 3]) ? fileName : "activity/" + fileName;
		}
		
		return fileName;
	}
	
	/**
	 * 通过类名获取配置文件的文件名
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static String getConfigFilenameByClassName(String className) throws ClassNotFoundException {
		Class<?> configClass = Class.forName(className);
		if (configClass.getAnnotation(HawkConfigManager.XmlResource.class) != null) {
			HawkConfigManager.XmlResource xmlRes = configClass.getAnnotation(HawkConfigManager.XmlResource.class);
			if (!HawkOSOperator.isEmptyString(xmlRes.file())) {
				return xmlRes.file();
			}
		} else if (configClass.getAnnotation(HawkConfigManager.KVResource.class) != null) {
			HawkConfigManager.KVResource kvRes = configClass.getAnnotation(HawkConfigManager.KVResource.class);
			if (!HawkOSOperator.isEmptyString(kvRes.file())) {
				return kvRes.file();
			}
		}
		
		return "";
	}
	
	/**
	 * 检测svn上配置文件的提交人员
	 * 
	 *	------------------------------------------------------------------------
		r430236 | lijialiang | 2023-10-20 17:22:13 +0800 (五, 2023-10-20) | 1 行
	
		[jenkins] 测试环境异常信息推送至飞书群实现
		------------------------------------------------------------------------
		r430145 | lijialiang | 2023-10-19 16:28:16 +0800 (四, 2023-10-19) | 1 行
	
		[jenkins] 【配置检测失败时抛出异常】 测试环境异常信息推送至飞书群实现
		------------------------------------------------------------------------
		r429749 | lijialiang | 2023-10-17 19:07:16 +0800 (二, 2023-10-17) | 1 行
	 *
	 * @param svnFile
	 * @return
	 */
	@SuppressWarnings("resource")
	private static String checkSvnCommitInfo(String svnFile) {
		InputStream inputStream = null;
		try {
			String[] command = {"svn", "log", svnFile};
			Process process = Runtime.getRuntime().exec(command);
			inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("|") > 0 && line.split("\\|")[0].indexOf("r") >= 0) {
					return line.split("\\|")[1].trim();
				}
				lineNum++;
				if (lineNum > 10) {
					break;
				}
			}
		} catch (Exception e) {
		    //HawkException.catchException(e);
		    HawkLog.errPrintln("postExceptionMsg2Feishu checkSvnCommitInfo exception, svnFile: {}, exception info: {}", svnFile, e.getStackTrace());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//HawkException.catchException(e);
					HawkLog.errPrintln("postExceptionMsg2Feishu checkSvnCommitInfo exception, svnFile: {}, exception info: {}", svnFile, e.getStackTrace());
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 获取java类文件的提交人信息
	 * @param path  格式：com.hawk.game.script.TestOperationHandler
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static HawkTuple2<String, String> getSvnCommitInfo(String path) {
		if (repository == null || HawkOSOperator.isEmptyString(path)) {
			return new HawkTuple2<String, String>(null, null);
		}
		
		if (path.startsWith("com.hawk.activity")) {
			path = "/Code_Server/debug/GameActivity/src/main/java/" + path.replaceAll("\\.", "\\/") + ".java";
		} else if (path.startsWith("com.hawk.game.script")) {
			path = "/Code_Server/debug/GameServer/script/" + path.replaceAll("\\.", "\\/") + ".java";
		} else if (path.startsWith("com.hawk.game.gmscript")) {
			path = "/Code_Server/debug/GameServer/gmscript/" + path.replaceAll("\\.", "\\/") + ".java";
		} else if (path.startsWith("com.hawk.game.idipscript")) {
			path = "/Code_Server/debug/GameServer/idipscript/" + path.replaceAll("\\.", "\\/") + ".java";
		} else {
			path = "/Code_Server/debug/GameServer/src/main/java/" + path.replaceAll("\\.", "\\/") + ".java";
		}
		
		String commitAuthor = "", commitTime = "";
		try {
			int[] revisionGap = GsConfig.getInstance().getSvnRevisionGapArray();
			long endRevision = repository.getLatestRevision();
			long startRevision = endRevision - revisionGap[0];
			final long finalEnd = endRevision;
			// 这里path要求的格式：/Code_Server/debug/GameServer/src/main/java/com/hawk/game/world/WorldPoint.java
			Collection<SVNLogEntry> logEntries = repository.log(new String[] { path }, null, startRevision, endRevision, true, true);

			int i = 0;
			long gap = revisionGap[1];
			// 如果文件提交时间太过久远了，扩大范围重新拉取一次
			while (logEntries.isEmpty() && i < 10 && startRevision > 0) {
				i++;
				gap = gap * i;
				endRevision = startRevision;
				startRevision = endRevision - gap;
				startRevision = Math.max(startRevision, -1);
				logEntries = repository.log(new String[] { path }, null, startRevision, endRevision, true, true);
			}
			
			if (logEntries.isEmpty()) {
				HawkLog.logPrintln("postExceptionMsg2Feishu getSvnCommitInfo result empty, path: {}, final end: {}, repository final end: {}, endRevision: {}, startRevision: {}", 
						path, finalEnd, endRevision, startRevision);
			}
			
			Iterator<SVNLogEntry> entries = logEntries.iterator();
			while (entries.hasNext()) {
				SVNLogEntry logEntry = entries.next();
				commitAuthor = logEntry.getAuthor();
				commitTime = HawkTime.formatTime(logEntry.getDate().getTime());
			}
		} catch (Exception e) {
			//HawkException.catchException(e);
			HawkLog.errPrintln("postExceptionMsg2Feishu getSvnCommitInfo exception, path: {}, exception info: {}", path, e.getStackTrace());
		}
    	
    	return new HawkTuple2<String, String>(commitAuthor, commitTime);
	}
	
}
