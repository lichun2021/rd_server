package com.hawk.game.superweapon;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponEvent;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponEventType;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPresident;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;

/**
 * 超级武器相关历史记录
 * 
 * @author golden
 *
 */
public class SuperWeaponRecord {

	/**
	 * 全局实例对象
	 */
	private static SuperWeaponRecord instance = null;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static SuperWeaponRecord getInstance() {
		if (instance == null) {
			instance = new SuperWeaponRecord();
		}
		return instance;
	}
	
	/**
	 * 构造
	 */
	private SuperWeaponRecord() {
		
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public void init() {
		
	}
	
	/**
	 * 添加超级武器事件记录
	 * 
	 */
	public void addSuperWeaponEventRecord(int pointId, SuperWeaponEventType eventType, String playerId, String otherPlayerId) {
		SuperWeaponEvent.Builder builder = SuperWeaponEvent.newBuilder();
		int[] pos = GameUtil.splitXAndY(pointId);
		builder.setPosX(pos[0]);
		builder.setPosY(pos[1]);
		builder.setEventType(eventType);
		builder.setEventTime(GsApp.getInstance().getCurrentTime());
		
		if (!HawkOSOperator.isEmptyString(playerId)) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			builder.setPlayerName(player.getName());
			builder.setGuildTag(player.getGuildTag());
			String guildName = player.getGuildName();
			builder.setGuildName(guildName == null ? "" : guildName);
		}
		
		if (!HawkOSOperator.isEmptyString(otherPlayerId)) {
			Player player = GlobalData.getInstance().makesurePlayer(otherPlayerId);
			builder.setOtherName(player.getName());
			builder.setOtherGuildTag(player.getGuildTag());
			String guildName = player.getGuildName();
			builder.setOtherGuildName(guildName == null ? "" : guildName);
		}
		
		LocalRedis.getInstance().addSuperWeaponDetialEvent(pointId, builder);
		LocalRedis.getInstance().updateSuperWeaponBriefEvent(pointId, builder);
	}
	
	/**
	 * 添加国王当选记录
	 * 
	 */
	public void addSuperWeaponElectedRecord(String playerId, int pointId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		
		int[] pos = GameUtil.splitXAndY(pointId);
		SuperWeaponPresident.Builder builder = SuperWeaponPresident.newBuilder();
		builder.setTurnCount(SuperWeaponService.getInstance().getTurnCount());
		builder.setPlayerId(player.getId());
		builder.setPlayerName(player.getName());
		builder.setPlayerIcon(player.getIcon());
		builder.setGuildId(player.getGuildId());
		builder.setGuildName(player.getGuildName());
		builder.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()));
		builder.setGuildFlag(player.getGuildFlag());
		builder.setTurnTime(HawkTime.getMillisecond());
		builder.setPosX(pos[0]);
		builder.setPosY(pos[1]);
		builder.setPfIcon(player.getPfIcon());
		LocalRedis.getInstance().addElectedSuperWeapon(builder, pointId);
	}
}

