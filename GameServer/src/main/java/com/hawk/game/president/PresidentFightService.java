package com.hawk.game.president;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.model.President;
import com.hawk.game.president.model.PresidentCrossRateInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.President.PresidentEventType;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.protocol.President.PresidentQuarterInfo;
import com.hawk.game.protocol.President.PresidentQuarterMarch;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.PresidentTowerPointId;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldThreadScheduler;

import io.netty.util.internal.StringUtil;

/**
 * 国王战服务类
 * @author zhenyu.shang
 * @since 2017年12月5日
 */
public class PresidentFightService extends HawkAppObj {
	
	/**
	 * 日志记录器
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 王城实体
	 */
	private PresidentCity presidentCity;
	
	/**
	 * 全局实例对象
	 */
	private static PresidentFightService instance = null;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static PresidentFightService getInstance() {
		return instance;
	}
	

	public PresidentFightService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		presidentCity = new PresidentCity();
		
		if(!presidentCity.init()){
			return false;
		}
		
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(1000) {
			@Override
			public void onPeriodTick() {
				try {
					presidentCity.tick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				try {
					PresidentBuff.getInstance().onTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});
		
		// 国王战各项记录信息
		if (!PresidentRecord.getInstance().init()) {
			return false;
		}

		// 国王战礼包
		if (!PresidentGift.getInstance().init()) {
			return false;
		}

		// 国王战官职
		if (!PresidentOfficier.getInstance().init()) {
			return false;
		}
		
		if (!PresidentBuff.getInstance().init()) {
			return false;
		}
		
		return true;
	}


	public PresidentCity getPresidentCity() {
		return presidentCity;
	}
	
	public PresidentTower getPresidentTower(int pointId){
		return presidentCity.getTower(pointId);
	}
	
	public String getPresidentTowerGuild(int pointId) {
		PresidentTower tower = presidentCity.getTower(pointId);
		if (tower == null) {
			return null;
		}
		return presidentCity.getTower(pointId).getGuildId();
	}
	
	/**
	 * 获取箭塔的占领服
	 * @param pointId
	 * @return
	 */
	public String getPresidentTowerServer(int pointId) {
		String presidentTowerGuild = getPresidentTowerGuild(pointId);
		if (HawkOSOperator.isEmptyString(presidentTowerGuild)) {
			return StringUtil.EMPTY_STRING;
		}
		return GuildService.getInstance().getGuildServerId(presidentTowerGuild);
	}
	
	/**
	 * 攻击王城胜利处理
	 * @param atkLeaderId
	 * @param defLeaderId
	 */
	public void doPresidentAttackWin(String atkLeaderId, String defLeaderId) {
		// 发送国王战邮件
		PresidentFightService.getInstance().sendPresidentWarMail(atkLeaderId, defLeaderId, true);

		// 添加战争记录
		int type = HawkOSOperator.isEmptyString(defLeaderId) ? PresidentEventType.OCCUPY_PALACE_VALUE : PresidentEventType.ATTACK_WIN_VALUE;
		PresidentRecord.getInstance().addPresidentEventRecord(type, atkLeaderId, defLeaderId, null);
		
		// 改变王城占领者
		Player atkLeader = GlobalData.getInstance().makesurePlayer(atkLeaderId);
		changeOccuption(atkLeader.getGuildId(), atkLeader.getName());
	}
	
	/**
	 * 攻击王城失败利处理
	 * @param atkLeaderId
	 * @param defLeaderId
	 */
	public void doPresidentAttackLose(String atkLeaderId, String defLeaderId) {
		PresidentRecord.getInstance().addPresidentEventRecord(PresidentEventType.ATTACK_FAILED_VALUE, atkLeaderId, defLeaderId, null);
	}
	
	/**
	 * 援助王城处理
	 * @param massMarchList
	 */
	public void doPresidentAssistance(IWorldMarch march) {
		// 王城占领者id
		Player leader = WorldMarchService.getInstance().getPresidentLeader();
		if (leader == null) {
			PresidentRecord.getInstance().addPresidentEventRecord(PresidentEventType.OCCUPY_PALACE_VALUE, march.getPlayerId(), null, null);
		} else {
			PresidentRecord.getInstance().addPresidentEventRecord(PresidentEventType.PRESIDENT_ASSISTANCE_VALUE, march.getPlayerId(), leader.getId(), null);
		}
	}
	
	/**
	 * 攻击王城箭塔胜利处理
	 * @param atkLeaderId
	 * @param defLeaderId
	 */
	public void doPresidentTowerAttackWin(String atkLeaderId, String defLeaderId, int pointId) {
		// 添加国王箭塔战争记录
		int type = HawkOSOperator.isEmptyString(defLeaderId) ? PresidentEventType.OCCUPY_PALACE_VALUE : PresidentEventType.ATTACK_WIN_VALUE;
		PresidentRecord.getInstance().addPresidentTowerEventRecord(type, atkLeaderId, defLeaderId, pointId);
		
		// 改变王城占领者
		Player atkLeader = GlobalData.getInstance().makesurePlayer(atkLeaderId);
		changeTowerOccuption(atkLeader.getGuildId(), pointId, atkLeader.getId(), atkLeader.getName());
	}
	
	/**
	 * 攻击王城箭塔失败利处理
	 * @param atkLeaderId
	 * @param defLeaderId
	 */
	public void doPresidentTowerAttackLose(String atkLeaderId, String defLeaderId, int pointId) {
		PresidentRecord.getInstance().addPresidentTowerEventRecord(PresidentEventType.ATTACK_FAILED_VALUE, atkLeaderId, defLeaderId, pointId);
	}
	
	/**
	 * 援助王城箭塔处理
	 * @param massMarchList
	 */
	public void doPresidentTowerAssistance(IWorldMarch march, int pointId) {
		if (march == null || march.getMarchEntity().isInvalid() || !march.isReachAndStopMarch()) {
			return;
		}
		
		Player towerLeader = WorldMarchService.getInstance().getPresidentTowerLeader(pointId);
		if (towerLeader == null) {
			PresidentRecord.getInstance().addPresidentTowerEventRecord(PresidentEventType.OCCUPY_PALACE_VALUE, march.getPlayerId(), null, pointId);
		} else {
			PresidentRecord.getInstance().addPresidentTowerEventRecord(PresidentEventType.PRESIDENT_ASSISTANCE_VALUE, march.getPlayerId(), towerLeader.getId(), pointId);
		}
	}
	
	/**
	 * 占领王城
	 */
	public void changeOccuption(String guildId, String leaderName){
		if(!guildId.equals(getCurrentGuildId())){
			
			// 记录箭塔归属变更日志
			logger.info("president change occuption, guildId:{}", guildId);
			
			this.presidentCity.setOccupyTime(HawkApp.getInstance().getCurrentTime());
			this.presidentCity.setGuildId(guildId);
			
			// 广播国王战状态信息
			presidentCity.broadcastPresidentInfo(null);
			if(leaderName != null) {
				// 发送系统公告
				noticeOccuptionChanged(WorldPointType.KING_PALACE_VALUE, leaderName, GuildService.getInstance().getGuildTag(guildId));
			}
			
			if (CrossActivityService.getInstance().isOpen()) {
				CrossActivityService.getInstance().changeCrossPresidentOccupy(guildId);
			}
		}
	}
	
	/**
	 * 占领箭塔
	 * @param guildId
	 * @param pointId
	 */
	public void changeTowerOccuption(String guildId, int pointId, String leaderId, String leaderName){
		
		// 记录箭塔归属变更日志
		int pos[] = GameUtil.splitXAndY(pointId);
		logger.info("president tower change occuption, x:{}, y:{}, guildId:{}, leaderId:{}, leaderName:{}", pos[0], pos[1], guildId, leaderId, leaderName);
		
		PresidentTower tower = getPresidentTower(pointId);
		
		tower.setGuildId(guildId);
		tower.setLeaderId(leaderId);
		tower.setLeaderName(leaderName);
		
		if (HawkOSOperator.isEmptyString(guildId)) {
			tower.setOccupyTime(0);
		} else if(!guildId.equals(tower.getGuildId())){
			tower.setOccupyTime(HawkTime.getMillisecond());
		}
		
		// 广播国王战箭塔状态信息
		getPresidentTower(pointId).broadcastPresidentTowerInfo(null);
		if(leaderName != null && guildId != null){
			// 发送系统公告
			noticeOccuptionChanged(WorldPointType.CAPITAL_TOWER_VALUE, leaderName, GuildService.getInstance().getGuildTag(guildId));
		}
	}
	
	
	private void noticeOccuptionChanged(int pointType, String leaderName, String guildTag){
		// 国王战开始系统通知 
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
				Const.NoticeCfgId.PRESIDENT_CHANGE, null, guildTag, leaderName, pointType);
	}
	
	/**
	 * 获取当前王城占领公会
	 * @return
	 */
	public String getCurrentGuildId(){
		return this.presidentCity.getGuildId();
	}

	/**
	 * 获取当前占领服的服务器id
	 * @return
	 */
	public String getCurrentServerId() {
		String guildId = getCurrentGuildId();
		return GuildService.getInstance().getGuildServerId(guildId);
	}
	
	/**
	 * 是否是国王盟
	 * @param guildId
	 * @return
	 */
	public boolean isPresidentGuild(String guildId){
		return !HawkOSOperator.isEmptyString(guildId) && guildId.equals(getCurrentGuildId());
	}
	
	/**
	 * 是否是国王
	 * @param playerId
	 * @return
	 */
	public boolean isPresidentPlayer(String playerId){
		if(presidentCity.getPresident() == null){
			return false;
		}
		return !HawkOSOperator.isEmptyString(playerId) && playerId.equals(presidentCity.getPresident().getPlayerId());
	}
	/**
	 * 是否是临时国王
	 * @param playerId
	 * @return
	 */
	public boolean isProvisionalPresident(String playerId) {
		return PresidentOfficier.getInstance().isProvisionalPresident(playerId);		
	}
	/**
	 * 是否是战争时间
	 * @return
	 */
	public boolean isFightPeriod() {
		if (getPresidentCity().getStatus() == PresidentPeriod.WARFARE_VALUE
				|| getPresidentCity().getStatus() == PresidentPeriod.OVERTIME_VALUE) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取国王战时期阶段(和平or战争)
	 * 
	 * @return
	 */
	public int getPresidentPeriodType() {
		return getPresidentCity().getStatus();
	}
	
	/**
	 * 获取国王Id
	 * @return
	 */
	public String getPresidentPlayerId(){
		return getPresidentCity().getPresidentPlayerId();
	}
	
	/**
	 * 获取国王公会ID
	 * @return
	 */
	public String getPresidentGuildId(){
		President president = getPresidentCity().getPresident();
		if(president == null){
			return null;
		}
		return president.getPlayerGuildId();
	}
	
	/**
	 * 通知国家信息发生改变
	 * 
	 * @param countryName
	 * @param countryIcon
	 */
	public void notifyCountryInfoChanged(String countryName) {
		presidentCity.setCountryName(countryName);
		presidentCity.setCountryModifyTimes(presidentCity.getCountryModifyTimes() + 1);
		// 广播国王信息
		presidentCity.broadcastPresidentInfo(null);
	}
	
	/**
	 * 发送国王战邮件
	 * 
	 * @param attackPlayerId
	 * @param defancePlayerId
	 * @param isAttackWin
	 */
	public void sendPresidentWarMail(String attackPlayerId, String defancePlayerId, boolean isAttackWin) {

		// 攻击方数据
		String attackGuildId = GuildService.getInstance().getPlayerGuildId(attackPlayerId);
		String attackGuildName = GuildService.getInstance().getGuildName(attackGuildId);
		Collection<String> attackPlayerIds = GuildService.getInstance().getGuildMembers(attackGuildId);

		if (HawkOSOperator.isEmptyString(defancePlayerId)) {
			// 发送邮件---国王战攻方邮件，首次占领（攻方联盟所有人）
			for (String playerId : attackPlayerIds) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
	                    .setPlayerId(playerId)
	                    .setMailId(MailId.PRESIDENT_WAR_SUCC_ATTACK)
	                    .build());
			}
			return;
		}

		// 防御方数据
		String defanceGuildId = GuildService.getInstance().getPlayerGuildId(defancePlayerId);
		String defanceGuildName = GuildService.getInstance().getGuildName(defanceGuildId);
		Collection<String> defancePlayerIds = GuildService.getInstance().getGuildMembers(defanceGuildId);

		MailId attackMailId = isAttackWin ? MailId.PRESIDENT_WAR_SUCC_TO_FROM : MailId.PRESIDENT_WAR_FAILED_TO_FROM;
		MailId defanceMailId = isAttackWin ? MailId.PRESIDENT_WAR_FAILED_TO_TARGET : MailId.PRESIDENT_WAR_SUCC_TO_TARGET;
		// 发送邮件---攻击方邮件
		for (String playerId : attackPlayerIds) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                    .setPlayerId(playerId)
                    .setMailId(attackMailId)
                    .addContents(defanceGuildName)
                    .build());
		}
		// 发送邮件---防御方邮件
		for (String playerId : defancePlayerIds) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                    .setPlayerId(playerId)
                    .setMailId(defanceMailId)
                    .addContents(attackGuildName)
                    .build());
		}
	}
	
	/**
	 * 是否有国王战相关行为
	 * @param guildId
	 * @return
	 */
	public boolean hasPresidentFightAction(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		if (guildId.equals(getCurrentGuildId())) {
			return true;
		}
		for (PresidentTower tower : presidentCity.getTowers()) {
			if (guildId.equals(tower.getGuildId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取王城驻军列表
	 * @param player
	 */
	public void sendPresidentQuarterInfo(Player player) {
		PresidentQuarterInfo.Builder builder = PresidentQuarterInfo.newBuilder();
		
		String currentGuildId = PresidentFightService.getInstance().getCurrentGuildId();
//		if (CrossActivityService.getInstance().isOpen()) {
//			if (HawkOSOperator.isEmptyString(currentGuildId) || !GuildService.getInstance().isSameCamp(player, currentGuildId)) {
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_QUARTER_INFO_S, builder));
//				return;
//			}
//		} else {
//			if (!player.hasGuild() || !player.getGuildId().equals(currentGuildId)) {
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_QUARTER_INFO_S, builder));
//				return;
//			}
//		}
		
		if (!player.hasGuild() || !player.getGuildId().equals(currentGuildId)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_QUARTER_INFO_S, builder));
			return;
		}
		
		BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentMarchs();
		for (String marchId : presidentMarchs) {
			builder.addQuarterMarch(getPresidentQuarterMarch(marchId));
		}
		
		String presidentLeaderMarchId = WorldMarchService.getInstance().getPresidentLeaderMarch();
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(presidentLeaderMarchId);
		if (leaderMarch != null) {
			int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
			builder.setMassSoldierNum(maxMassJoinSoldierNum);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_QUARTER_INFO_S, builder));
	}
	
	/**
	 * 获取王城箭塔驻军列表
	 * @param player
	 */
	public void sendPresidentTowerQuarterInfo(Player player, int pointId) {
		String currentGuildId = PresidentFightService.getInstance().getPresidentTowerGuild(pointId);
		
		PresidentQuarterInfo.Builder builder = PresidentQuarterInfo.newBuilder();
		
//		if (CrossActivityService.getInstance().isOpen()) {
//			if (HawkOSOperator.isEmptyString(currentGuildId) || !GuildService.getInstance().isSameCamp(player, currentGuildId)) {
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_QUARTER_INFO_S, builder));
//				return;
//			}
//		} else {
//			if (!player.hasGuild() || !player.getGuildId().equals(currentGuildId)) {
//				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_QUARTER_INFO_S, builder));
//				return;
//			}
//		}
		if (!player.hasGuild() || !player.getGuildId().equals(currentGuildId)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_QUARTER_INFO_S, builder));
			return;
		}
		
		BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentTowerMarchs(pointId);
		for (String marchId : presidentMarchs) {
			builder.addQuarterMarch(getPresidentQuarterMarch(marchId));
		}
		
		String presidentTowerLeaderMarchId = WorldMarchService.getInstance().getPresidentTowerLeaderMarchId(pointId);
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(presidentTowerLeaderMarchId);
		int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
		builder.setMassSoldierNum(maxMassJoinSoldierNum);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_TOWER_QUARTER_INFO_S, builder));
	}
	
	private PresidentQuarterMarch.Builder getPresidentQuarterMarch(String marchId) {
		PresidentQuarterMarch.Builder builder = PresidentQuarterMarch.newBuilder();
		
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		String playerId = march.getPlayerId();
		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		
		builder.setPlayerId(snapshot.getId());
		builder.setName(snapshot.getName());
		builder.setIcon(snapshot.getIcon());
		builder.setPfIcon(snapshot.getPfIcon());
		builder.setGuildTag(snapshot.getGuildTag());
		builder.setMarchId(marchId);
		
		List<ArmyInfo> armys = march.getMarchEntity().getArmys();
		for (ArmyInfo army : armys) {
			if (army.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(army.toArmySoldierPB(snapshot).build());
		}
		
		List<PlayerHero> heroList = snapshot.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : heroList) {
			builder.addHero(hero.toPBobj());
		}
		SuperSoldier ssoldier = snapshot.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
		if(Objects.nonNull(ssoldier)){
			builder.setSsoldier(ssoldier.toPBobj());
		}
		return builder;
	}
	
	
}
