package com.hawk.game.activity.impl.inherit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.hawk.activity.type.impl.inheritNew.BackNewPlayerInfo;
import com.hawk.activity.type.impl.inheritNew.cfg.InheritNewActivityTimeCfg;
import com.hawk.activity.type.impl.inheritNew.cfg.InheritNewKVCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.data.PlatTransferInfo;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.InheritCondType;
import com.hawk.log.LogConst.Platform;

/**
 * 军魂传承活动管理类
 * 
 * @author admin
 *
 */
public class InheritNewService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static InheritNewService instance = null;
	
	/**
	 * 回归玩家信息
	 */
	private Map<String, BackNewPlayerInfo> backPlayers;

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static InheritNewService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public InheritNewService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	public boolean init() {
		backPlayers = new ConcurrentHashMap<>();
		return true;
	}
	
	/**
	 *  玩家登录
	 * @param accountInfo
	 */
	public void onPlayerLogin(Player player, AccountInfo accountInfo) {
		try {
			// 非本服玩家不进行检测
			if (!GlobalData.getInstance().isLocalPlayer(player.getId())) {
				return;
			}
			
			roleRechargeAmountSum(player);
			// 新注册玩家 需要进行军魂承接激活检测
			if (accountInfo.isNewly()) {
				newBackCheck(player, accountInfo);
			} else {
				// 老玩家回归,状态检测
				oldBackCheck(player, accountInfo);
			}
			// Redis读取回归信息,判定玩家是否涉及回归
			BackNewPlayerInfo backInfo = this.getBackPlayerInfoNewFromRedis(player);
			int termId = getInheritTerm();
			if(backInfo != null && (getInheritTerm() == termId || termId == 0)){
				String playerId = player.getId();
				if (playerId.equals(backInfo.getCurNewPlayer()) || playerId.equals(backInfo.getCurOldPlayerId()) || playerId.equals(backInfo.getCurrInheritPlayerId())) {
					backPlayers.put(playerId, backInfo);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取当前需进行军魂触发检测的活动期数
	 * @return
	 */
	private int getInheritTerm() {
		ConfigIterator<InheritNewActivityTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritNewActivityTimeCfg.class);
		long now = HawkTime.getMillisecond();
		int termId = 0;
		for(InheritNewActivityTimeCfg cfg : its){
			// 活动开启且在截止触发之前
			if(now > cfg.getStartTimeValue() && now < cfg.getStopTriggerValue()){
				termId = cfg.getTermId();
			}
		}
		return termId;
	}
	
	/**
	 * 老帐号回归检测
	 * @param player
	 * @param accountInfo
	 */
	private void oldBackCheck(Player player, AccountInfo accountInfo) {
		// 活动未开启
		if (getInheritTerm() == 0) {
			return;
		}
		InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
		long lastOutTime = accountInfo.getLogoutTime();
		// 玩家本服帐号离线时间不满足条件,直接返回
		if (HawkTime.getMillisecond() - lastOutTime < cfg.getOfflineTime()) {
			return;
		}

		// Redis先读取
		BackNewPlayerInfo backInfo = this.getBackPlayerInfoNewFromRedis(player);
		// 存在本期回归信息,并且当前时间在本期当此回归有效期内,则不予处理
		if(backInfo != null && backInfo.getTermId() == getInheritTerm() && HawkTime.getMillisecond() <= backInfo.getCurrValidTime()){
			// 记录下没满足的条件类型 和 条件参数
			LogUtil.logInheritCondResult(player, InheritCondType.VALID_ON, HawkTime.formatTime(backInfo.getCurrValidTime()));
			return;
		}
		// 检测是否符合激活回归条件
		List<AccountRoleInfo> roleList = getPlayerAccountInfos(player);
		// 已被承接帐号列表
		List<String> inheritedList = this.getIngheritedInfoFromRedis(player);
		boolean limitMeet = false;
		StringBuilder sb = new StringBuilder();
		for (AccountRoleInfo info : roleList) {
			// 玩家帐号离线时间不满足条件,直接返回
			if (HawkTime.getMillisecond() - info.getLogoutTime() < cfg.getOfflineTime()) {
				// 记录下没满足的条件类型 和 条件参数 （离线时长）
				LogUtil.logInheritCondResult(player, InheritCondType.LOGOUT_TIME_LIMIT, HawkTime.formatTime(info.getLogoutTime()));
				return;
			}
			
			// 帐号已被承接过
			if (inheritedList.contains(info.getPlayerId())) {
				sb.append(info.getServerId()).append("-").append(info.getPlayerId()).append(",");
				continue;
			}
			
			// 配置条件判断
			if (this.checkConfigCond(info)) {
				limitMeet = true;
			}
		}
		if (!limitMeet) {
			// 记录下没满足的条件类型 和 条件参数 （满足条件的号）
			String param = sb.length() == 0 ? "" : sb.deleteCharAt(sb.length() - 1).toString();
			LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_NONE, param);
			return;
		}
		BackNewPlayerInfo backInfoNew = new BackNewPlayerInfo();
		// 如果是本期内再度回归,累计回归次数
		if (backInfo != null && backInfo.getTermId() == getInheritTerm()) {
			backInfoNew.setReturnCnt(backInfo.getReturnCnt() + 1);
		} else {
			backInfoNew.setReturnCnt(1);
		}
		backInfoNew.setOpenId(player.getOpenId());
		backInfoNew.setTermId(getInheritTerm());
		backInfoNew.setBackTime(HawkTime.getMillisecond());
		backInfoNew.setCurOldServer(accountInfo.getServerId());
		backInfoNew.setCurOldPlayerId(accountInfo.getPlayerId());
		// 本次激活有效时间为回归当日0点+激活有效期 
		backInfoNew.setCurrValidTime(HawkTime.getAM0Date(new Date(backInfoNew.getBackTime())).getTime() + cfg.getValidTime());
		RedisProxy.getInstance().updateBackPlayerInfoNew(backInfoNew);

		// 老服触发成功
		String param = sb.length() == 0 ? "" : sb.deleteCharAt(sb.length() - 1).toString();
		LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_SUCC, param);
		
		HawkLog.logPrintln("InheritNewService oldBackCheck success, playerId: {}, openId: {}, name: {}, platform: {}, returnCnt:{}", player.getId(), player.getOpenId(), player.getName(),
				player.getPlatform(), backInfoNew.getReturnCnt());
	}
	
	/**
	 * 新注册账号回归检测
	 * @param player
	 * @param accountInfo
	 */
	@SuppressWarnings("deprecation")
	private void newBackCheck(Player player, AccountInfo accountInfo) {
		// 活动未开启
		if (getInheritTerm() == 0) {
			return;
		}
		// 检测是否符合激活回归条件
		InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
		// Redis先读取
		BackNewPlayerInfo backInfo = this.getBackPlayerInfoNewFromRedis(player);
		// 如果存在本期的回归记录,且当前处于本次活动的有效激活时间之内
		if (backInfo != null && backInfo.getTermId() == getInheritTerm() && HawkTime.getMillisecond() <= backInfo.getCurrValidTime()) {
			// 如果之前没有进行过新角色注册,则本帐号激活军魂承接,否则不进行处理
			if (HawkOSOperator.isEmptyString(backInfo.getCurNewServer())) {
				backInfo.setCurNewServer(accountInfo.getServerId());
				backInfo.setCurNewPlayer(player.getId());
				backInfo.setRegistTime(player.getEntity().getResetTime());
				// 本次激活有效时间为新服注册当日0点+传承活动持续时间
				backInfo.setCurrValidTime(HawkTime.getAM0Date(new Date(backInfo.getRegistTime())).getTime() + cfg.getLastTime());
				RedisProxy.getInstance().updateBackPlayerInfoNew(backInfo);
				
				// 新服触发成功
				LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_SUCC, "");
				
				HawkLog.logPrintln("InheritNewService newBackCheck success, first back new player, playerId: {}, openId: {}, name: {}, platform: {}, returnCnt:{}", player.getId(),
						player.getOpenId(), player.getName(), player.getPlatform(), backInfo.getReturnCnt());
			} else {
				// 新服触发失败
				LogUtil.logInheritCondResult(player, InheritCondType.NEW_SERVER_ROLE_EXIST, String.valueOf(backInfo.getCurNewServer()));
				return;
			}
		} else {
			List<AccountRoleInfo> roleList = getPlayerAccountInfos(player);
			// 已被承接帐号列表
			List<String> inheritedList = this.getIngheritedInfoFromRedis(player);
			boolean limitMeet = false;
			StringBuilder sb = new StringBuilder();
			for (AccountRoleInfo info : roleList) {
				// 帐号已被承接过
				if (inheritedList.contains(info.getPlayerId())) {
					sb.append(info.getServerId()).append("-").append(info.getPlayerId()).append(",");
					continue;
				}
				// 本帐号跳过
				if (info.getPlayerId().equals(player.getId())) {
					continue;
				}
				// 玩家帐号离线时间不满足条件,直接返回
				if (HawkTime.getMillisecond() - info.getLogoutTime() < cfg.getOfflineTime()) {
					// 新服触发失败
					LogUtil.logInheritCondResult(player, InheritCondType.LOGOUT_TIME_LIMIT, HawkTime.formatTime(info.getLogoutTime()));
					return;
				}
				// 配置条件判断
				if (this.checkConfigCond(info)) {
					limitMeet = true;
				}
			}

			// 没有账号满足限制条件
			if (!limitMeet) {
				// 新服触发失败
				String param = sb.length() == 0 ? "" : sb.deleteCharAt(sb.length() - 1).toString();
				LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_NONE, param);
				return;
			}

			BackNewPlayerInfo backInfoNew = new BackNewPlayerInfo();
			// 如果是本期内再度回归,累计回归次数
			if (backInfo != null && backInfo.getTermId() == getInheritTerm()) {
				backInfoNew.setReturnCnt(backInfo.getReturnCnt() + 1);
			} else {
				backInfoNew.setReturnCnt(1);
			}
			backInfoNew.setOpenId(player.getOpenId());
			backInfoNew.setTermId(getInheritTerm());
			backInfoNew.setRegistTime(player.getEntity().getResetTime());
			backInfoNew.setCurNewServer(player.getServerId());
			backInfoNew.setCurNewPlayer(player.getId());
			// 本次激活有效时间为新服注册当日0点+传承活动持续时间
			backInfoNew.setCurrValidTime(HawkTime.getAM0Date(new Date(backInfoNew.getRegistTime())).getTime() + cfg.getLastTime());

			RedisProxy.getInstance().updateBackPlayerInfoNew(backInfoNew);
			
			// 新服触发成功
			String param = sb.length() == 0 ? "" : sb.deleteCharAt(sb.length() - 1).toString();
			LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_SUCC, param);
			
			HawkLog.logPrintln("InheritNewService newBackCheck success, can inherit player, playerId: {}, openId: {}, name: {}, platform: {}, returnCnt: {}", player.getId(),
					player.getOpenId(), player.getName(), player.getPlatform(), backInfoNew.getBackTime());
		}
	}
	

	/**
	 * 获取玩家所有同平台帐号列表
	 * @param accountInfo
	 * @return
	 */
	public List<AccountRoleInfo> getPlayerAccountInfos(Player player) {
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(player.getOpenId());
		List<AccountRoleInfo> list = new ArrayList<>();
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			String roleServerId = roleInfoObj.getServerId();
			String serverId = GsConfig.getInstance().getServerId();
			if (roleServerId.length() > serverId.length() && roleServerId.startsWith("20")) {
				roleServerId = roleServerId.substring(2);
				roleInfoObj.setServerId(roleServerId);
			}
			list.add(roleInfoObj);
		}
		return list;
	}
	
	/**
	 * 获取玩家充值信息集合
	 * @param openId
	 * @return
	 */
	private HashBasedTable<String, String, List<RechargeInfo>> getRechargInfos(Player player, Map<String, List<RechargeInfo>> roleRechargeMap){
		String openid = player.getOpenId();
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(openid);
		
		HashBasedTable<String, String, List<RechargeInfo>> infoTable = HashBasedTable.create();
		for(RechargeInfo info : rechargeInfos){
			// rechargeInfo中的playerId字段是在2024年3月份才添加的，之前没有playerId信息只能通过serverId+platform来匹配，后面就可以直接通过playerId匹配了
			if (!HawkOSOperator.isEmptyString(info.getPlayerId())) {
				List<RechargeInfo> list = roleRechargeMap.get(info.getPlayerId());
				if(list == null){
					list = new ArrayList<>();
					roleRechargeMap.put(info.getPlayerId(), list);
				}
				list.add(info);
				continue;
			}
			
			String serverId = info.getServer();
			String platform = Platform.valueOf(info.getPlatId()).strLowerCase();
			List<RechargeInfo> list = infoTable.get(serverId, platform);
			if(list == null){
				list = new ArrayList<>();
				infoTable.put(serverId, platform, list);
			}
			list.add(info);
		}
		return infoTable;
	}
	
	/**
	 * 获取适合军魂承接的角色信息
	 * @param playerId
	 * @return
	 */
	public AccountRoleInfo getSuitInheritAccount(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return null;
		}
		// 玩家所有帐号
		List<AccountRoleInfo> accountRoles = getPlayerAccountInfos(player);
		// 已被承接帐号列表
		List<String> inheritedList = this.getIngheritedInfoFromRedis(player);
		// 符合被承接条件角色列表
		List<AccountRoleInfo> enoughList = new ArrayList<>();
		int maxVip = 0;
		StringBuilder sb = new StringBuilder();
		// 检测是否符合激活回归条件
		for (AccountRoleInfo info : accountRoles) {
			// 已被承接
			if(inheritedList.contains(info.getPlayerId())){
				sb.append(info.getServerId()).append("-").append(info.getPlayerId()).append(",");
				continue;
			}
			
			// 角色本身
			if (playerId.equals(info.getPlayerId())) {
				continue;
			}
			// 配置条件判断
			if (this.checkConfigCond(info)) {
				maxVip = Math.max(maxVip, info.getVipLevel());
				enoughList.add(info);
			}
		}

		List<AccountRoleInfo> suitList = new ArrayList<>();
		for (AccountRoleInfo info : enoughList) {
			if (info.getVipLevel() < maxVip) {
				continue;
			}
			suitList.add(info);
		}
		if (suitList.isEmpty()) {
			sb.append("fetchSuitRole");
			LogUtil.logInheritCondResult(player, InheritCondType.COND_MATCH_NONE, sb.toString());
			return null;
		}
		if (suitList.size() == 1) {
			return suitList.get(0);
		}
		
		Map<String, Integer> rechargeTotalMap = sortRoleInfoByRechargeTotal(player, suitList);
		if (rechargeTotalMap.isEmpty()) {
			Map<String, String> rolePlatormMap = getRolePlatformMap(suitList);
			Map<String, List<RechargeInfo>> roleRechargeMap = new HashMap<>();
			sortByRechargeAmount(player, suitList, roleRechargeMap, rolePlatormMap); // 按充值额度排序
		}
		return suitList.get(0);
	}
	
	/**
	 * 获取对应角色的充值总额（军魂传承活动调用）
	 * @param roleInfo
	 * @return
	 */
	public int getAccountRechargeNumAndExp(AccountRoleInfo roleInfo) {
		if (roleInfo == null) {
			return 0;
		}

		String openid = roleInfo.getOpenId();
		Map<String, Integer> rechargeTotalMap = RedisProxy.getInstance().getAllRoleRechargeTotal(openid);
		if (!rechargeTotalMap.isEmpty()) {
			return rechargeTotalMap.getOrDefault(roleInfo.getPlayerId(), 0);
		}
		
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(openid);
		
		int sumGold = 0;
		String tarServer = roleInfo.getServerId();
		//转服
		String sourceServer = GlobalData.getInstance().getImmgrationSource(roleInfo.getPlayerId(), tarServer);
		//转平台
		String platform = PlatTransferInfo.getSourcePlatform(roleInfo.getPlayerId(), roleInfo.getPlatform());
		for(RechargeInfo info : rechargeInfos){
			// rechargeInfo中的playerId字段是在2024年3月份才添加的，之前没有playerId信息只能通过serverId+platform来匹配，后面就可以直接通过playerId匹配了
			if (!HawkOSOperator.isEmptyString(info.getPlayerId()) && info.getPlayerId().equals(roleInfo.getPlayerId())) {
				sumGold += info.getCount();
				continue;
			}
			
			String rechargeServer = info.getServer();
			String rechargePlatform = Platform.valueOf(info.getPlatId()).strLowerCase();
			if (!rechargePlatform.equals(platform)) {
				continue;
			}
			
			if (rechargeServer.equals(tarServer) || rechargeServer.equals(sourceServer)) {
				sumGold += info.getCount();
			}
		}
		
		return sumGold;
	}
	
	
	/**
	 * 活动轮询获取玩家回归信息
	 * @param playerId
	 * @return
	 */
	public BackNewPlayerInfo getBackInfoByPlayerIdForAct(String playerId){
		if(HawkOSOperator.isEmptyString(playerId)){
			return null;
		}
		return backPlayers.get(playerId);
	}
	
	/**
	 * 配置条件判断
	 * @param info
	 * @return
	 */
	private boolean checkConfigCond(AccountRoleInfo info) {
		InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
		return info.getPlayerLevel() >= cfg.getLevel() 
				&& info.getVipLevel() >= cfg.getVipLimit() 
				&& info.getBattlePoint() >= cfg.getBattlePoint()
				&& info.getCityLevel() >= cfg.getBuildlevel();
	}
	
	/**
	 * InheritCondResult打点数据收集
	 * @param player
	 * @param sj1    已被传承过的角色信息
	 * @param sj2  未被传承（充过值）的角色信息
	 */
	public void inheritCondDataCollect(Player player, StringJoiner sj1, StringJoiner sj2) {
		List<AccountRoleInfo> inheritedInfo = new ArrayList<>();
		List<HawkTuple2<AccountRoleInfo, Integer>> notInheritedInfo = new ArrayList<>();
		inheritCondDataCollect(player, inheritedInfo, notInheritedInfo);
		for (AccountRoleInfo info : inheritedInfo) {
			sj1.add(info.getServerId() + "_" + info.getPlayerId());
		}
		
		for(HawkTuple2<AccountRoleInfo, Integer> tuple : notInheritedInfo) {
			AccountRoleInfo roleInfo = tuple.first;
			String info = roleInfo.getServerId() + "_" + roleInfo.getPlayerId() + "_" + roleInfo.getVipLevel() + "_" + tuple.second;
			sj2.add(info);
		}
	}
	
	/**
	 * 数据收集  TODO
	 * @param inheritedInfo：已被传承过的角色信息
	 * @param notInheritedInfo：未被传承（充过值）的角色信息
	 */
	public void inheritCondDataCollect(Player player, List<AccountRoleInfo> inheritedInfo, List<HawkTuple2<AccountRoleInfo, Integer>> notInheritedInfo) {
		try {
			List<AccountRoleInfo> accountRoles = getPlayerAccountInfos(player);
			// 已被承接帐号列表
			List<String> inheritedList = this.getIngheritedInfoFromRedis(player);
			// 符合被承接条件角色列表
			List<AccountRoleInfo> enoughList = new ArrayList<>();
			for (AccountRoleInfo info : accountRoles) {
				// 记录已被承接过的角色信息
				if(inheritedList.contains(info.getPlayerId())){
					inheritedInfo.add(info);
					continue;
				}
				// 配置条件判断
				if (this.checkConfigCond(info)) {
					enoughList.add(info);
				}
			}
			
			if (enoughList.isEmpty()) {
				return;
			}

			Map<String, Integer> rechargeTotalMap = sortRoleInfoByRechargeTotal(player, enoughList);
			if (!rechargeTotalMap.isEmpty()) {
				for (int i = 0; i < enoughList.size(); i++) {
					AccountRoleInfo roleInfo = enoughList.get(i);
					int sum = rechargeTotalMap.getOrDefault(roleInfo.getPlayerId(), 0);
					if (sum > 0) {
						HawkTuple2<AccountRoleInfo, Integer> tuple = new HawkTuple2<>(roleInfo, sum);
						notInheritedInfo.add(tuple);
					}
				}
			} else {
				// 充值额度排序
				Map<String, String> rolePlatormMap = getRolePlatformMap(enoughList);
				Map<String, List<RechargeInfo>> roleRechargeMap = new HashMap<>();
				HashBasedTable<String, String, List<RechargeInfo>> table = sortByRechargeAmount(player, enoughList, roleRechargeMap, rolePlatormMap);
				for (int i = 0; i < enoughList.size(); i++) {
					AccountRoleInfo roleInfo = enoughList.get(i);
					int rechargeTotal = getRechargeTotal(table.get(roleInfo.getServerId(), rolePlatormMap.get(roleInfo.getPlayerId())));
					String sourceServer = GlobalData.getInstance().getImmgrationSource(roleInfo.getPlayerId(), roleInfo.getServerId());
					rechargeTotal += getRechargeTotal(table.get(sourceServer, rolePlatormMap.get(roleInfo.getPlayerId())));
					rechargeTotal += getRechargeTotal(roleRechargeMap.get(roleInfo.getPlayerId()));
					if (rechargeTotal > 0) {
						HawkTuple2<AccountRoleInfo, Integer> tuple = new HawkTuple2<>(roleInfo, rechargeTotal);
						notInheritedInfo.add(tuple);
					}
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 根据角色累计充值额度数据来排序
	 * @param player
	 * @param roleList
	 * @return
	 */
	private Map<String, Integer> sortRoleInfoByRechargeTotal(Player player, List<AccountRoleInfo> roleList) {
		Map<String, Integer> rechargeTotalMap = RedisProxy.getInstance().getAllRoleRechargeTotal(player.getOpenId()); 
		if (rechargeTotalMap.isEmpty()) {
			return rechargeTotalMap;
		}
		
		roleList.sort(new Comparator<AccountRoleInfo>() {
			@Override
			public int compare(AccountRoleInfo role0, AccountRoleInfo role1) {
				int sum0 = rechargeTotalMap.getOrDefault(role0.getPlayerId(), 0);
				int sum1 = rechargeTotalMap.getOrDefault(role1.getPlayerId(), 0);
				if (sum0 != sum1) {
					return sum1 - sum0; // 这里要按倒序排，充值金额最多的排在前面
				} else {
					return role0.getRegisterTime() > role1.getRegisterTime() ? 1 : -1; // 充值金额一样多的情况下，根据注册时间按顺序排，注册时间早的拍在前面
				}
			}
		});
		return rechargeTotalMap;
	}
	
	/**
	 * 按充值额度排序
	 * @param player
	 * @param roleList
	 * @return
	 */
	private HashBasedTable<String, String, List<RechargeInfo>> sortByRechargeAmount(Player player, List<AccountRoleInfo> roleList, 
			Map<String, List<RechargeInfo>> roleRechargeMap, Map<String, String> rolePlatormMap) {
		//table存储的是按平台+区服识别的充值数据，roleRechargeMap存的是按角色id识别的数据
		HashBasedTable<String, String, List<RechargeInfo>> table = getRechargInfos(player, roleRechargeMap);
		roleList.sort(new Comparator<AccountRoleInfo>() {
			@Override
			public int compare(AccountRoleInfo role0, AccountRoleInfo role1) {
				String platform0 = rolePlatormMap.get(role0.getPlayerId());
				int sum0 = getRechargeTotal(table.get(role0.getServerId(), platform0));
				String sourceServer0 = GlobalData.getInstance().getImmgrationSource(role0.getPlayerId(), role0.getServerId());
				sum0 += getRechargeTotal(table.get(sourceServer0, platform0));
				sum0 += getRechargeTotal(roleRechargeMap.get(role0.getPlayerId()));
				
				String platform1 = rolePlatormMap.get(role1.getPlayerId());
				int sum1 = getRechargeTotal(table.get(role1.getServerId(), platform1));
				String sourceServer1 = GlobalData.getInstance().getImmgrationSource(role1.getPlayerId(), role1.getServerId());
				sum1 += getRechargeTotal(table.get(sourceServer1, platform1));
				sum1 += getRechargeTotal(roleRechargeMap.get(role1.getPlayerId()));
				
				if (sum0 != sum1) {
					return sum1 - sum0; // 这里要按倒序排，充值金额最多的排在前面
				} else {
					return role0.getRegisterTime() > role1.getRegisterTime() ? 1 : -1; // 充值金额一样多的情况下，根据注册时间按顺序排，注册时间早的拍在前面
				}
			}
		});
		
		return table;
	}
	
	/**
	 * 获取充值总额度
	 * @param serverId
	 * @param platform
	 * @param table
	 * @return
	 */
	private int getRechargeTotal(List<RechargeInfo> list) {
		int total = 0;
		if (list != null) {
			total += list.stream().mapToInt(r -> r.getCount()).sum();
		}
		return total;
	}
	
	/**
	 * 获取BackNewPlayerInfo信息
	 * @param player
	 * @return
	 */
	public BackNewPlayerInfo getBackPlayerInfoNewFromRedis(Player player) {
		BackNewPlayerInfo backInfo = RedisProxy.getInstance().getBackPlayerInfoNew(player.getOpenId());
		return backInfo;
	}
	
	/**
	 * 获取账号下已被传承过的角色信息
	 * @param player
	 * @return
	 */
	public List<String> getIngheritedInfoFromRedis(Player player) {
		List<String> inheritedList = RedisProxy.getInstance().getIngheritedInfos(player.getOpenId());
		return inheritedList;
	}
	
	/**
	 * 获取角色平台对应信息
	 * @param roleInfoList
	 * @return
	 */
	public Map<String, String> getRolePlatformMap(List<AccountRoleInfo> roleInfoList) {
		Map<String, String> rolePlatormMap = new HashMap<>();
		for (AccountRoleInfo roleInfo : roleInfoList) {
			String platform = PlatTransferInfo.getSourcePlatform(roleInfo.getPlayerId(), roleInfo.getPlatform());
			rolePlatormMap.put(roleInfo.getPlayerId(), platform);
		}
		
		return rolePlatormMap;
	}
	
	/**
	 * 统计一个账号的各个角色的累计充值额度
	 * @param player
	 */
	public void roleRechargeAmountSum(Player player) {
		try {
			long lastRefreshTime = 0;
			CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.RECHARGE_DATA_REF_KEY);
			if (customData == null) {
				customData = player.getData().createCustomDataEntity(GsConst.RECHARGE_DATA_REF_KEY, 0, "");
			} else {
				lastRefreshTime = customData.getValue() * 1000L;
			}
			
			//跨天了，重新刷新一遍数据
			if (!HawkTime.isToday(lastRefreshTime)) {
				customData.setValue(HawkTime.getSeconds());
				refreshRechargeAmountSum(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 充值数据统计刷新
	 * @param player
	 */
	private void refreshRechargeAmountSum(Player player) {
		List<AccountRoleInfo> accountRoles = getPlayerAccountInfos(player);
		Map<String, String> rolePlatormMap = getRolePlatformMap(accountRoles);
		Map<String, List<RechargeInfo>> roleRechargeMap = new HashMap<>();
		Map<String, Integer> rechargeAmountMap = new HashMap<>();
		
		//table存储的是按平台+区服识别的充值数据，roleRechargeMap存的是按角色id识别的数据
		HashBasedTable<String, String, List<RechargeInfo>> table = getRechargInfos(player, roleRechargeMap);
		for (AccountRoleInfo roleInfo : accountRoles) {
			String roleId = roleInfo.getPlayerId();
			if (rechargeAmountMap.getOrDefault(roleId, 0) > 0) {
				continue;
			}
			String platform0 = rolePlatormMap.get(roleId);
			int rechargeTotal = getRechargeTotal(table.get(roleInfo.getServerId(), platform0));
			String sourceServer = GlobalData.getInstance().getImmgrationSource(roleId, roleInfo.getServerId());
			rechargeTotal += getRechargeTotal(table.get(sourceServer, platform0));
			rechargeTotal += getRechargeTotal(roleRechargeMap.get(roleId));
			rechargeAmountMap.put(roleId, rechargeTotal);
		}
		
		Map<String, String> map = new HashMap<>();
		rechargeAmountMap.entrySet().forEach(e -> map.put(e.getKey(), String.valueOf(e.getValue())));
		String key = RedisKey.ROLE_RECHARGE_TOTAL + ":" + player.getOpenId();
		RedisProxy.getInstance().getRedisSession().hmSet(key, map, 0);
	}
	
}
