package com.hawk.game.script;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;

import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

/**
 * QA更新服务器(配置)的脚本
 *
 * localhost:8080/script/serverupdate?filename=&cmdstr=
 *
 * filename: 文件名
 * cmdstr：执行命令
 *
 * @author lating
 *
 */

public class ServerUpdateHandler extends HawkScript {
	// 脚本支持的命令
	static final List<String> CMDS = Arrays.asList("xmlreload", "reload", "hotfix", "restart", "dbclear");
	static final String DEFAULT_FILE = "default_937687519.zip";
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "server not support auto maintain by script");
		}
		
		String filename = params.get("filename");
        String cmdstr = params.get("cmdstr");
        final String finalFileName = filename;
        final String finalCmdstr = cmdstr;
        
        if ("dbclear".equals(cmdstr) && HawkOSOperator.isEmptyString(filename)) {
        	filename = DEFAULT_FILE;
        }
        
        if (HawkOSOperator.isEmptyString(filename) || HawkOSOperator.isEmptyString(cmdstr)) {
             return HawkScript.failedResponse(SCRIPT_ERROR, "filename, cmdstr param need");
        }
        
        if (!filename.endsWith(".zip") || !CMDS.contains(cmdstr)) {
        	return HawkScript.failedResponse(SCRIPT_ERROR, "param format error");
        }

        if (!cmdstr.equals("restart") && !cmdstr.equals("dbclear")) {
        	return updateServer(filename, cmdstr, true);
        }
        
        if ("dbclear".equals(cmdstr) && !DEFAULT_FILE.equals(filename)) {
        	cmdstr = "updatedbclear";
        }
        
        String result = RedisProxy.getInstance().getRedisSession().getString("server:restart");
        if (!HawkOSOperator.isEmptyString(result)) {
        	return HawkScript.successResponse("server latest restart at " + HawkTime.formatTime(Long.parseLong(result)) + ", please restart after 10 minutes from this time");
        }
        
        String lastFilename = RedisProxy.getInstance().getRedisSession().getString("server:restartfile");
        if (filename.equals(lastFilename)) {
        	return HawkScript.successResponse(filename + " has already used when last restart");
        }
        
        if (!downloadCheck(filename)) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "download file failed, maybe the file not exist");
		}
        
        String execute = params.get("execute");
        if (execute != null && "true".equals(execute)) {
        	// 10分钟内不允许重复restart，主要是防止前端浏览器重复请求
        	RedisProxy.getInstance().getRedisSession().setString("server:restart", String.valueOf(HawkTime.getMillisecond()), 600);
        	if (!DEFAULT_FILE.equals(filename)) {
        		RedisProxy.getInstance().getRedisSession().setString("server:restartfile", filename);
        	}
        	HawkLog.logPrintln("actually execute, filename: {}, cmdstr: {}", filename, cmdstr);
        	return updateServer(filename, cmdstr, false);
        }
        
        HawkTaskManager.getInstance().postTask(new HawkDelayTask(1000, 1000, 1) {
        	@Override
        	public Object run() {
        		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(GsConfig.getInstance().getServerId());
        		if (serverInfo == null) {
        			return new HawkTuple2<Integer, String>(-1, null);
        		} 
        		
        		// 获取并修正游戏服务器的http访问地址
        		String webHost = serverInfo.getWebHost();
        		if (!webHost.startsWith("http://") && !webHost.startsWith("HTTP://")) {
        			webHost = "http://" + webHost;
        		}
        		if (!webHost.endsWith("/")) {
        			webHost += "/";
        		}
        		
        		String file = finalFileName == null ? "" : finalFileName;
        		String url = webHost + "script/serverupdate?filename=" + file + "&cmdstr=" + finalCmdstr + "&execute=true";
        		try {
        			HawkHttpUrlService.getInstance().doGet(url, 1000);
        		} catch (Exception e) {
        			HawkException.catchException(e);
        		}
        		return null;
        	}
        });
        
        return HawkScript.successResponse("server going to restart, please login game after 2 minutes");
	}
	
	private String updateServer(String filename, String cmdstr, boolean download) {
		if (download && !downloadCheck(filename)) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "download file failed, maybe the file not exist");
		}
		
		StringBuilder sb = new StringBuilder();
        try {
		    String shpath="/data/ftp/server_update.sh";
		    Process ps = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", shpath + " " + filename + " " + cmdstr});
		    ps.waitFor();
		    BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		    String line;
		    while ((line = br.readLine()) != null) {
		        sb.append(line).append(HawkScript.HTTP_NEW_LINE);
		    }
		} catch (Exception e) {
	        HawkException.catchException(e);
	        return HawkScript.failedResponse(SCRIPT_ERROR, "server error");
		}
        
        return HawkScript.successResponse(sb.toString());
	}
	
	private boolean downloadCheck(String filename) {
		if (DEFAULT_FILE.equals(filename)) {
			return true;
		}
		
		StringBuilder sb = new StringBuilder();
        try {
		    String shpath="/data/ftp/download_check.sh";
		    Process ps = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", shpath + " " + filename});
		    ps.waitFor();
		    BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		    String line;
		    while ((line = br.readLine()) != null) {
		        sb.append(line).append(HawkScript.HTTP_NEW_LINE);
		    }
		    
		    if (sb.toString().indexOf("failed") >= 0) {
		    	return false;
		    }
		} catch (Exception e) {
	        HawkException.catchException(e);
	        return false;
		}
        
		return true;
	}
	
}
