package com.hawk.game.player.platchange;

import java.util.Map;
import java.util.Map.Entry;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.PlatTransferInfo;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.log.LogConst.Platform;
import com.hawk.sdk.msdk.entity.CheckBalanceResult;

/**
 * 转平台（安卓、ios互转）服务类
 * 
 * @author lating
 */
public class PlatChangeService {
	/**
	 * 单例
	 */
	private static PlatChangeService instance = new PlatChangeService();
	
	/**
	 * 获取单例
	 */
	public static PlatChangeService getInstance() {
		return instance;
	}
	
	/**
	 * 转平台
	 */
	public void changePlatform(String openId, String serverId) {
		// 先把两个账号都踢掉
		if (!kickout(serverId, openId, "android")) {
			return;
		}
		
		// 先把两个账号都踢掉
		if (!kickout(serverId, openId, "ios")) {
			return;
		}
		
		// 之前的账号
		AccountInfo androidAccount = getAccountInfo(serverId, openId, "android");
		AccountInfo iosAccount = getAccountInfo(serverId, openId, "ios");
		
		// 之前的玩家
		Player androidPlayer = getPlayer(androidAccount);
		Player iosPlayer = getPlayer(iosAccount);
		
		// 转平台
		boolean succ1 = realChangePlatform(androidPlayer, "ios", openId, serverId);
		boolean succ2 = realChangePlatform(iosPlayer, "android", openId, serverId);
		if (!succ1 && !succ2) {
			return;
		}
		
		//recentServer信息处理
		Map<String, String> recentServerMap = RedisProxy.getInstance().getRecentServer(openId);
		for(Entry<String, String> entry : recentServerMap.entrySet()) {
			if (entry.getKey().equals(serverId + ":ios")) {
				RedisProxy.getInstance().updateRecentServer(serverId, openId, "android", entry.getValue());
			}
			if (entry.getKey().equals(serverId + ":android")) {
				RedisProxy.getInstance().updateRecentServer(serverId, openId, "ios", entry.getValue());
			}
		}
		
		// 之前android为null,证明ios转到了android,转平台后删掉ios的信息
		if (androidPlayer == null) {
			RedisProxy.getInstance().deleRecentServer(serverId, openId, "ios");
			RedisProxy.getInstance().removeAccountRole(openId, serverId, "ios");
			GlobalData.getInstance().removePuidAccountData(openId, "ios", serverId);
		}
		
		if (iosPlayer == null) {
			RedisProxy.getInstance().deleRecentServer(serverId, openId, "android");
			RedisProxy.getInstance().removeAccountRole(openId, serverId, "android");
			GlobalData.getInstance().removePuidAccountData(openId, "android", serverId);
		}
	}
	
	/**
	 * 踢下线
	 */
	private boolean kickout(String serverId, String openId, String platform) {
		// 获取puid
		String puid = GameUtil.getPuidByPlatform(openId, platform);
		// 获取accountInfo
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo == null) {
			HawkLog.errPrintln("change platform, kickout account is null, serverId: {}, openId: {}, platform: {}", serverId, openId, platform);
			return true;
		}
		// 玩家不存在 或者 正在跨服
		Player player = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
		if (player == null || player.isCsPlayer() || CrossService.getInstance().isCrossPlayer(accountInfo.getPlayerId())) {
			HawkLog.errPrintln("change platform, kickout error, serverId: {}, openId: {}, platform: {}", serverId, openId, platform);
			return false;
		}
		// 玩家在线的话,就先给踢掉
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_ACCOUNT_RESET_OFFLINE_VALUE, true, null);
		}
		HawkLog.logPrintln("change platform, kickout success, serverId: {}, openId: {}, platform: {}", serverId, openId, platform);
		return true;
	}
	
	/**
	 * 获取AccountInfo
	 */
	private AccountInfo getAccountInfo(String serverId, String openId, String platform) {
		String puid = GameUtil.getPuidByPlatform(openId, platform);
		return GlobalData.getInstance().getAccountInfo(puid, serverId);
	}
	
	/**
	 * 获取玩家
	 */
	private Player getPlayer(AccountInfo accountInfo) {
		if (accountInfo == null) {
			return null;
		}
		return GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
	}
	
	/**
	 * 转平台
	 * @param player
	 */
	@SuppressWarnings("deprecation")
	public boolean realChangePlatform(Player player, String afterPlatform, String openId, String serverId) {
		if (player == null || player.isCsPlayer() || CrossService.getInstance().isCrossPlayer(player.getId())) {
			HawkLog.errPrintln("change platform player null, afterPlatform: {}, serverId: {}, openid: {}", afterPlatform, serverId, openId);
			return false;
		}
		PlayerEntity entity = player.getEntity();
		String bornPlatform = entity.getPlatform();
		
		// 更新玩家db数据
		String puid = GameUtil.getPuidByPlatform(entity.getOpenid(), afterPlatform);
		entity.setPlatform(afterPlatform);
		entity.setPuid(puid);
		
		//accountRoleInfo信息处理
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
		accountRoleInfo.setPlatform(afterPlatform);
		RedisProxy.getInstance().addAccountRole(accountRoleInfo);
		
		// 更新玩家AccountInfo
		GlobalData.getInstance().updateAccountInfo(entity.getPuid(), entity.getServerId(), entity.getId(), 0, entity.getName());
		
		// redis记录转换信息，作为后面判断的依据
		PlatTransferInfo transferInfo = RedisProxy.getInstance().getPlatTransferInfo(player.getId());
		if (transferInfo == null) {
			transferInfo = new PlatTransferInfo(player.getId(), bornPlatform, afterPlatform, player.getServerId(), HawkTime.getSeconds());
		} else {
			transferInfo.setTransferTo(afterPlatform);
			transferInfo.setTime(HawkTime.getSeconds());
		}
		RedisProxy.getInstance().updatePlatTransferInfo(transferInfo);
		
		HawkLog.logPrintln("change platform success, serverId: {}, openid: {}, playerId: {}, bornPlatform: {}, afterPlatform: {}", 
				entity.getServerId(), entity.getOpenid(), player.getId(), bornPlatform, afterPlatform);
		return true;
	}
	
	/**
	 * 平台转换后首次登录处理
	 * @param result
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean platTransferAfter(Player player, CheckBalanceResult result) {
		int step = 0;
		try {
			PlatTransferInfo transferInfo = RedisProxy.getInstance().getPlatTransferInfo(player.getId());
			// 这里本可以不必要判断saveAmt，为了减少不必要的逻辑判断和执行，还是添加一下saveAmt的判断，saveAmt小于0约等于是新创建的角色
			if (transferInfo == null && player.getPlayerBaseEntity().getSaveAmt() <= 0) {
				// 当前这个角色没有转平台记录，查一下另一个平台的角色是否有转平台记录（如果有，说明这个角色是新创建的角色）
				String otherPlatorm = Platform.IOS.strLowerCase().equals(player.getPlatform()) ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
				AccountRoleInfo otherRoleInfo = RedisProxy.getInstance().getAccountRole(player.getServerId(), otherPlatorm, player.getOpenId());
				if (otherRoleInfo != null && RedisProxy.getInstance().getPlatTransferInfo(otherRoleInfo.getPlayerId()) != null) {
					// 如果另一个平台角色有转平台记录，到这里说明当前这个角色是新创建的角色，也需要生成一条转平台记录，方便对于充值数据的统一处理
					transferInfo = new PlatTransferInfo(player.getId(), player.getPlatform(), player.getPlatform(), player.getServerId(), HawkTime.getSeconds());
					RedisProxy.getInstance().updatePlatTransferInfo(transferInfo);
				}
			}
			
			if (transferInfo == null || transferInfo.getLoginTime() >= transferInfo.getTime()) {
				return false;
			}
			
			int oldDiamonds = player.getPlayerBaseEntity().getDiamonds();
			int newDiamonds = result.getBalance();
			transferInfo.setLoginTime(HawkTime.getSeconds());
			RedisProxy.getInstance().updatePlatTransferInfo(transferInfo);
			step = 1; // 更新时间之后一定要按转换的逻辑处理
			player.getPlayerBaseEntity().setSaveAmt(result.getSave_amt());
			player.getPlayerBaseEntity()._setChargeAmt(result.getSave_amt());
			if (oldDiamonds == newDiamonds) {
				return true;
			}
			
			// 旧的存量大于新的存量，要补米大师存量
			if (oldDiamonds > newDiamonds) {
				player.present(oldDiamonds - newDiamonds, null, "platTransfer", "platTransfer");
			} else { // 要扣米大师存量
				player.pay(newDiamonds - oldDiamonds, "platTransfer", null);
			}
			
			player.getPlayerBaseEntity().setDiamonds(oldDiamonds); // 本地存量原封不动
			player.getPush().syncPlayerDiamonds();
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	
		return step > 0;
	}
	
}
