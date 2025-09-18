package com.hawk.robot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.GuildManager.ApplyGuildReq;
import com.hawk.game.protocol.GuildManager.CreateGuildReq;
import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.GuildManor.GuildManorStat;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;

/**
 *
 * @author zhenyu.shang
 * @since 2017年8月10日
 */
public class GuildUtil {
	
	
	/**
	 * 创建联盟
	 * @param gameRobotEntity
	 */
	public static void createGuild(GameRobotEntity gameRobotEntity){
		String robotNum_s = gameRobotEntity.getPuid().replaceAll("robot_puid_", "");
		CreateGuildReq.Builder builder = CreateGuildReq.newBuilder();
		String guildName = "r_g_"+robotNum_s;
		builder.setName(guildName);
		//随机简称
		builder.setTag(HawkOSOperator.randomString(3,"abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		builder.setLanguage("zh_CN");
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_CREATE_C_VALUE, builder));
		RobotLog.guildPrintln("Robot {} create Guild Name {}", gameRobotEntity.getPuid(), guildName);
	}
	
	/**
	 * 查询加入联盟
	 * @param gameRobotEntity
	 */
	public static void searchOrJoinGuild(GameRobotEntity gameRobotEntity){
		List<String> guildIds = WorldDataManager.getInstance().getGuildIds();
		if(guildIds.size() == 0){
			return;
		}
		Collections.shuffle(guildIds);
		ApplyGuildReq.Builder builder = ApplyGuildReq.newBuilder();
		String guildId = guildIds.get(0);
		builder.setGuildId(guildId);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_APPLY_C_VALUE,builder));
		RobotLog.guildPrintln("Robot {} join Guild Id {}", gameRobotEntity.getPuid(), guildId);
	}

	
	public static boolean isManorComplete(GuildManorStat stat){
		return stat == GuildManorStat.BREAKING_M || stat == GuildManorStat.DAMAGED_M || stat == GuildManorStat.GARRISON_M
				|| stat == GuildManorStat.REPAIRING_M || stat == GuildManorStat.UNGARRISON_M;
	}
	
	
	public static GuildManorBase randomManor(GameRobotEntity robot){
		Map<String, BlockingQueue<GuildManorBase>> allManors = WorldDataManager.getInstance().getGuildManorInfos();
		if(allManors.isEmpty()){
			return null;
		}
		List<GuildManorBase> list = new ArrayList<GuildManorBase>();
		
		for (Entry<String, BlockingQueue<GuildManorBase>> entry : allManors.entrySet()) {
			String guildId = entry.getKey();
			//同公会不打
			if(guildId.equals(robot.getGuildId())){
				continue;
			}
			BlockingQueue<GuildManorBase> manors = entry.getValue();
			if(manors == null || manors.isEmpty()){
				continue;
			}
			//状态不对不打
			for (GuildManorBase guildManorBase : manors) {
				if(GuildUtil.isManorComplete(guildManorBase.getStat())){
					list.add(guildManorBase);
				}
			}
		}
		if(list.isEmpty()){
			return null;
		}
		return HawkRand.randomObject(list);
	}
	
	
	public static boolean isManorMarch(WorldMarchType marchType){
		return marchType == WorldMarchType.MANOR_ASSISTANCE || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN
				|| marchType == WorldMarchType.MANOR_BUILD || marchType == WorldMarchType.MANOR_COLLECT || marchType == WorldMarchType.MANOR_MASS
				|| marchType == WorldMarchType.MANOR_MASS_JOIN || marchType == WorldMarchType.MANOR_REPAIR || marchType == WorldMarchType.MANOR_SINGLE;
	}
}
