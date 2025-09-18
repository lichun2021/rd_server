package com.hawk.game.activity.impl.inherit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.hawk.activity.type.impl.inherit.BackPlayerInfo;
import com.hawk.activity.type.impl.inherit.cfg.InheritActivityTimeCfg;
import com.hawk.activity.type.impl.inherit.cfg.InheritKVCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.log.LogConst.Platform;


/**
 * 尤里复仇活动管理类
 * 
 * @author admin
 *
 */
public class InheritService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static InheritService instance = null;
	
	/**
	 * 回归玩家信息
	 */
	private Map<String, BackPlayerInfo> backPlayers;

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static InheritService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public InheritService(HawkXID xid) {
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
			// 新注册玩家 需要进行军魂承接激活检测
			if (accountInfo.isNewly()) {
				newBackCheck(player, accountInfo);
			}
			
			// 老玩家回归,状态检测
			else {
				oldBackCheck(player, accountInfo);
			}
			// Redis读取回归信息,判定玩家是否涉及回归
			BackPlayerInfo backInfo = RedisProxy.getInstance().getBackPlayerInfo(player.getOpenId());
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
	 * 获取当前军魂承接活动期数
	 * @return
	 */
	private int getInheritTerm() {
		ConfigIterator<InheritActivityTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritActivityTimeCfg.class);
		long now = HawkTime.getMillisecond();
		int termId = 0;
		for(InheritActivityTimeCfg cfg : its){
			if(now > cfg.getStartTimeValue() && now < cfg.getEndTimeValue()){
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
		InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
		long lastOutTime = accountInfo.getLogoutTime();
		// 玩家本服帐号离线时间不满足条件,直接返回
		if (HawkTime.getMillisecond() - lastOutTime < cfg.getOfflineTime()) {
			return;
		}

		// Redis先读取
		BackPlayerInfo backInfo = RedisProxy.getInstance().getBackPlayerInfo(player.getOpenId());
		// 如果存在本期的活动记录,则不进行处理
		if (backInfo != null && (backInfo.getTermId() == getInheritTerm() || getInheritTerm() == 0)) {
			return;
		} else {
			// 检测是否符合激活回归条件
			List<AccountRoleInfo> roleList = getPlayerAccountInfos(player.getOpenId());
			// 已被承接帐号列表
			List<String> inheritedList = RedisProxy.getInstance().getIngheritedInfos(player.getOpenId());
			boolean limitMeet = false;
			for (AccountRoleInfo info : roleList) {
				// 帐号已被承接过
				if (inheritedList.contains(info.getPlayerId())) {
					continue;
				}

				// 玩家帐号离线时间不满足条件,直接返回
				if (HawkTime.getMillisecond() - info.getLogoutTime() < cfg.getOfflineTime()) {
					return;
				}
				// VIP等级/指挥官等级/大本等级/建筑等级满足
				if (info.getPlayerLevel() >= cfg.getLevel() && info.getVipLevel() >= cfg.getVipLimit() && info.getBattlePoint() >= cfg.getBattlePoint()
						&& info.getCityLevel() >= cfg.getBuildlevel()) {
					limitMeet = true;
				}
			}
			if (!limitMeet) {
				return;
			}
			backInfo = new BackPlayerInfo();
			backInfo.setOpenId(player.getOpenId());
			backInfo.setTermId(getInheritTerm());
			backInfo.setBackTime(HawkTime.getMillisecond());
			backInfo.setCurOldServer(accountInfo.getServerId());
			backInfo.setCurOldPlayerId(accountInfo.getPlayerId());
			RedisProxy.getInstance().updateBackPlayerInfo(backInfo);
			HawkLog.logPrintln("InheritService oldBackCheck success, playerId: {}, openId: {}, name: {}, platform: {}", player.getId(), player.getOpenId(), player.getName(),
					player.getPlatform());
		}
	}
	
	/**
	 * 新注册账号回归检测
	 * @param player
	 * @param accountInfo
	 */
	private void newBackCheck(Player player, AccountInfo accountInfo) {
		// Redis先读取
		BackPlayerInfo backInfo = RedisProxy.getInstance().getBackPlayerInfo(player.getOpenId());
		// 如果存在本期的回归记录,且回归活动处于开启状态
		if (backInfo != null && (backInfo.getTermId() == getInheritTerm() && getInheritTerm() != 0)) {
			// 如果之前没有进行过新角色注册,则本帐号激活军魂承接,否则不进行处理
			if (HawkOSOperator.isEmptyString(backInfo.getCurNewServer())) {
				backInfo.setCurNewServer(accountInfo.getServerId());
				backInfo.setCurNewPlayer(player.getId());
				backInfo.setRegistTime(player.getEntity().getResetTime());
				RedisProxy.getInstance().updateBackPlayerInfo(backInfo);
				HawkLog.logPrintln("InheritService newBackCheck success, first back new player, playerId: {}, openId: {}, name: {}, platform: {}", player.getId(), player.getOpenId(), player.getName(),
						player.getPlatform());
			} else {
				return;
			}
		} else {
			// 活动未开启
			if (getInheritTerm() == 0) {
				return;
			}
			// 检测是否符合激活回归条件
			InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
			List<AccountRoleInfo> roleList = getPlayerAccountInfos(player.getOpenId());

			// 已被承接帐号列表
			List<String> inheritedList = RedisProxy.getInstance().getIngheritedInfos(player.getOpenId());
			boolean limitMeet = false;
			for (AccountRoleInfo info : roleList) {
				// 帐号已被承接过
				if (inheritedList.contains(info.getPlayerId())) {
					continue;
				}
				// 本帐号跳过
				if (info.getPlayerId().equals(player.getId())) {
					continue;
				}
				// 玩家帐号离线时间不满足条件,直接返回
				if (HawkTime.getMillisecond() - info.getLogoutTime() < cfg.getOfflineTime()) {
					return;
				}
				// VIP等级/指挥官等级/大本等级/建筑等级满足
				if (info.getPlayerLevel() >= cfg.getLevel() && info.getVipLevel() >= cfg.getVipLimit() && info.getBattlePoint() >= cfg.getBattlePoint()
						&& info.getCityLevel() >= cfg.getBuildlevel()) {
					limitMeet = true;
				}
			}
			
			// 没有账号满足限制条件
			if (!limitMeet) {
				return;
			}
			backInfo = new BackPlayerInfo();
			backInfo.setOpenId(player.getOpenId());
			backInfo.setTermId(getInheritTerm());
			backInfo.setRegistTime(player.getEntity().getResetTime());
			backInfo.setCurNewServer(player.getServerId());
			backInfo.setCurNewPlayer(player.getId());
			RedisProxy.getInstance().updateBackPlayerInfo(backInfo);
			HawkLog.logPrintln("InheritService newBackCheck success, can inherit player, playerId: {}, openId: {}, name: {}, platform: {}", player.getId(), player.getOpenId(), player.getName(),
					player.getPlatform());
		}
	}
	

	/**
	 * 获取玩家所有同平台帐号列表
	 * @param accountInfo
	 * @return
	 */
	public List<AccountRoleInfo> getPlayerAccountInfos(String openId) {
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(openId);

		List<AccountRoleInfo> list = new ArrayList<>();
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			list.add(roleInfoObj);
		}
		return list;
	}
	
	/**
	 * 获取玩家充值信息集合
	 * @param openId
	 * @return
	 */
	public HashBasedTable<String, String, List<RechargeInfo>> getRechargInfos(String openId){
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(openId);
		HashBasedTable<String, String, List<RechargeInfo>> infoTable = HashBasedTable.create();
		for(RechargeInfo info : rechargeInfos){
			String serverId = info.getServer();
			String platForm = Platform.valueOf(info.getPlatId()).strLowerCase();
			List<RechargeInfo> list = infoTable.get(serverId, platForm);
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(info);
			infoTable.put(info.getServer(), platForm, list);
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
		String openId = player.getOpenId();
		// 玩家所有帐号
		List<AccountRoleInfo> accountRoles = getPlayerAccountInfos(openId);
		// 已被承接帐号列表
		List<String> inheritedList = RedisProxy.getInstance().getIngheritedInfos(openId);
		// 符合被承接条件角色列表
		List<AccountRoleInfo> enoughList = new ArrayList<>();
		int maxVip = 0;
		// 检测是否符合激活回归条件
		InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
		for (AccountRoleInfo info : accountRoles) {
			// 已被承接
			if(inheritedList.contains(info.getPlayerId())){
				continue;
			}
			
			// 角色本身
			if (playerId.equals(info.getPlayerId())) {
				continue;
			}
			
			// VIP等级/指挥官等级/大本等级/建筑等级不符
			if (info.getPlayerLevel() < cfg.getLevel() || info.getVipLevel() < cfg.getVipLimit() || info.getBattlePoint() < cfg.getBattlePoint()
					|| info.getCityLevel() < cfg.getBuildlevel()) {
				continue;
			}
			
			maxVip = Math.max(maxVip, info.getVipLevel());
			enoughList.add(info);
		}

		List<AccountRoleInfo> suitList = new ArrayList<>();
		for (AccountRoleInfo info : enoughList) {
			if (info.getVipLevel() < maxVip) {
				continue;
			}
			suitList.add(info);
		}
		if (suitList.isEmpty()) {
			return null;
		}
		if (suitList.size() == 1) {
			return suitList.get(0);
		}
		// 充值额度排序
		HashBasedTable<String, String, List<RechargeInfo>> table = getRechargInfos(openId);
		suitList.sort(new Comparator<AccountRoleInfo>() {
			@Override
			public int compare(AccountRoleInfo arg0, AccountRoleInfo arg1) {
				int sum0 = 0;
				List<RechargeInfo> list0 = table.get(arg0.getServerId(), arg0.getPlatform());
				if (list0 != null) {
					sum0 = list0.stream().mapToInt(r -> r.getCount()).sum();
				}
				int sum1 = 0;
				List<RechargeInfo> list1 = table.get(arg1.getServerId(), arg1.getPlatform());
				if (list1 != null) {
					sum1 = list1.stream().mapToInt(r -> r.getCount()).sum();
				}
				if (sum0 != sum1) {
					return sum0 - sum1;
				} else {
					return arg0.getRegisterTime() > arg1.getRegisterTime() ? 1 : -1;
				}
			}

		});
		return suitList.get(0);
	}
	
	/**
	 * 获取对应角色的充值总额
	 * @param roleInfo
	 * @return
	 */
	public int getAccountRechargeNumAndExp(AccountRoleInfo roleInfo) {
		if (roleInfo == null) {
			return 0;
		}
		
		HashBasedTable<String, String, List<RechargeInfo>> table = getRechargInfos(roleInfo.getOpenId());
		List<RechargeInfo> info = table.get(roleInfo.getServerId(), roleInfo.getPlatform());
		int sumGold = 0;
		if (info != null) {
			sumGold = info.stream().mapToInt(r -> r.getCount()).sum();

		}
		return sumGold;
	}
	
	/**
	 * 活动轮询获取玩家回归信息
	 * @param playerId
	 * @return
	 */
	public BackPlayerInfo getBackInfoByPlayerIdForAct(String playerId){
		if(HawkOSOperator.isEmptyString(playerId)){
			return null;
		}
		return backPlayers.get(playerId);
	}
	
}
