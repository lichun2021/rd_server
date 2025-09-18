package com.hawk.activity.configupdate;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityConfigTablePB;
import com.hawk.game.protocol.Activity.ActivityConfigsResp;
import com.hawk.game.protocol.Activity.ConfigCheckCodeList.Builder;
import com.hawk.game.protocol.Activity.ConfigCheckCodePB;
import com.hawk.game.protocol.HP;

/**
 * 活动配置文件管理
 * @author PhilChen
 *
 */
public class ActivityConfigUpdateManager {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 客户端配置文件*/
	private Map<String, byte[]> clientCfgMap;
	
	/** 客户端配置文件crc值*/
	private Map<String, Integer> clientCfgCrcMap;
	
	/** 客户端配置文件md5值*/
	private Map<String, String> clientCfgMd5Map;
	
	private static ActivityConfigUpdateManager instance;
	
	private static final String CLIENT_CFG_PATH = "activity/client/";
	
	private ActivityConfigUpdateManager() {
		clientCfgMap = new HashMap<>();
		clientCfgCrcMap = new HashMap<>();
		clientCfgMd5Map = new HashMap<>();
	}

	public static ActivityConfigUpdateManager getInstance() {
		if (instance == null) {
			instance = new ActivityConfigUpdateManager();
		}
		return instance;
	}
	
	public void init() {
		// 构建文件校验码
		loadClientCfg(false, new ArrayList<>());
	}
	
	/**
	 * 更新配置
	 * @param isPush
	 */
	public void updateConfig(boolean isPush, List<String> updateList) {
		loadClientCfg(isPush, updateList);
	}
	
	/**
	 *  加载客户端配置文件
	 * @param isPush
	 */
	private void loadClientCfg(boolean isPush, List<String> updateList) {
		// 当前目录下配置文件的md5,cfc和配置文件内容的集合
		Map<String, String> md5Map = new HashMap<>();
		Map<String, Integer> crcMap = new HashMap<>();
		Map<String, byte[]> cfgMap = new HashMap<>();

		// 配置根目录
		String configRoot = HawkConfigManager.getInstance().getConfigRoot();

		// 客户端配置文件夹路径
		String folderPath = HawkOSOperator.getWorkPath() + CLIENT_CFG_PATH;
		File forderFile = new File(folderPath);
		File[] files = forderFile.listFiles();
		if (files != null) {
			for (File file : files) {
				String cfgName = file.getName();

				// 获取实际生效文件路径
				String effecFilePath = HawkOSOperator.getWorkPath() + configRoot + CLIENT_CFG_PATH + cfgName;
				if (!HawkOSOperator.existFile(effecFilePath)) {
					effecFilePath = HawkOSOperator.getWorkPath() + CLIENT_CFG_PATH + cfgName;
				}

				File effecFile = new File(effecFilePath);
				String cfgMd5 = HawkMd5.makeMD5(effecFile);
				Integer crc;
				byte[] cfgContent;
				// 如果该配置文件存在且内容未发生变化
				if (clientCfgMd5Map.containsKey(cfgName) && clientCfgMd5Map.get(cfgName).equals(cfgMd5)) {
					cfgContent = clientCfgMap.get(cfgName);
					crc = clientCfgCrcMap.get(cfgName);
				} else {
					cfgContent = this.readFile(effecFile);
					crc = HawkOSOperator.calcCrc(cfgContent);
				}
				md5Map.put(cfgName, cfgMd5);
				crcMap.put(cfgName, crc);
				cfgMap.put(cfgName, cfgContent);
				HawkLog.logPrintln("load client config, filePath: {}, crc: {}", effecFile.getAbsolutePath(), crc);
			}
		}

		List<String> changeCfgNames = new ArrayList<>();

		for (Entry<String, Integer> entry : crcMap.entrySet()) {
			String cfgName = entry.getKey();
			if (!this.clientCfgCrcMap.containsKey(cfgName)) {
				changeCfgNames.add(cfgName);
				continue;
			}
			int oldCrc = this.clientCfgCrcMap.get(cfgName);
			int newCrc = entry.getValue();
			if (oldCrc != newCrc) {
				changeCfgNames.add(cfgName);
				updateList.add(cfgName);
			}
		}
		this.clientCfgMd5Map = md5Map;
		this.clientCfgCrcMap = crcMap;
		this.clientCfgMap = cfgMap;

		if (isPush) {
			Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				pushClientCfg(playerId, changeCfgNames);
			}
		}
		logger.debug("update activity lua config finish.");
	}
	
	
	private byte[] readFile(File file) {
		FileInputStream fr = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			fr = new FileInputStream(file);
			bis = new BufferedInputStream(fr);
			int len;
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}

	
	/**
	 * 向玩家推送配置文件校验码
	 * @param player
	 */
	public void pushCheckCodeToPlayer(String playerId) {
		Builder builder = Activity.ConfigCheckCodeList.newBuilder();
		for (Entry<String, Integer> entry : clientCfgCrcMap.entrySet()) {
			ConfigCheckCodePB.Builder checkCodePB = ConfigCheckCodePB.newBuilder();
			checkCodePB.setConfigName(entry.getKey());
			checkCodePB.setCheckCode(entry.getValue());
			builder.addCheckCodes(checkCodePB);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACTIVITY_CONFIG_CHECK_CODE_S_VALUE, builder));
		logger.debug("push config check code. {}", clientCfgCrcMap);
	}
	
	/**
	 * 向客户端推送lua配置
	 * @param player
	 * @param cfgNameList
	 */
	public void pushClientCfg(String playerId, List<String> cfgNameList) {
		if (cfgNameList == null || cfgNameList.isEmpty()) {
			return;
		}
		boolean hasupdate = false;
		List<ActivityConfigsResp.Builder> builderList = new ArrayList<>();
		ActivityConfigsResp.Builder builder = ActivityConfigsResp.newBuilder();
		builderList.add(builder);
		int totalSize = 0;
		// 分包大小限制
		int limitSize = ActivityConfig.getInstance().getClientCfgLimitSize();
		for (String configName : cfgNameList) {
			if (!this.clientCfgCrcMap.containsKey(configName)) {
				continue;
			}
			if (totalSize >= limitSize) {
				builder = ActivityConfigsResp.newBuilder();
				builderList.add(builder);
				totalSize = 0;
			}
			int crc = getClientConfigCrc(configName);
			byte[] cfgContent = getClientConfigContent(configName);
			ByteString byteString = ByteString.copyFrom(cfgContent);
			logger.debug("push activity configs, configName: {}, size: {}", configName, byteString.size());
			ActivityConfigTablePB.Builder table = ActivityConfigTablePB.newBuilder();
			table.setCheckCode(crc);
			table.setConfigName(configName);
			table.setCfgDataBytes(byteString);
			totalSize += byteString.size();
			builder.addTables(table);
			hasupdate = true;
		}
		if (hasupdate) {
			logger.debug("push activity configs builder, builderSize: {}", builderList.size());
			for (ActivityConfigsResp.Builder builderInfo : builderList) {
				PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PULL_ACTIVITY_CONFIG_S, builderInfo));
			}
		}
	}
	
	/**
	 * 获取客户端配置文件内容
	 * @param configName
	 * @return
	 */
	private byte[] getClientConfigContent(String configName) {
		byte[] content = clientCfgMap.get(configName);
		return content;
	}
	
	/**
	 * 获取客户端配置文件crc校验码
	 * @param configName
	 * @return
	 */
	private int getClientConfigCrc(String configName) {
		Integer crc = clientCfgCrcMap.get(configName);
		if (crc == null) {
			return 0;
		}
		return crc;
	}

}
