package com.hawk.game.world.march;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;

/**
 * 行军接口
 * 
 * @author zhenyu.shang
 * @since 2017年8月25日
 */
public interface IWorldMarch extends Comparable<IWorldMarch> {

	/**
	 * 初始化
	 */
	public void register();

	/**
	 * 移除
	 */
	public void remove();

	/**
	 * 获取行军ID
	 */
	default String getMarchId(){
		return getMarchEntity().getMarchId();
	}

	/**
	 * 行军心跳
	 */
	public void heartBeats();

	/**
	 * 获取行军类型
	 * 
	 * @return
	 */
	public WorldMarchType getMarchType();

	/**
	 * 是否是被动行军
	 * 
	 * @return
	 */
	public boolean isPassiveMarch();

	/**
	 * 获取行军所属玩家ID
	 * 
	 * @return
	 */
	public String getPlayerId();

	/**
	 * 获取行军实体
	 * 
	 * @return
	 */
	public WorldMarch getMarchEntity();

	// ------------------------------------------------------
	/**
	 * 是否是回程行军
	 * 
	 * @return
	 */
	public boolean isReturnBackMarch();

	/**
	 * 是否是行军过程中的行军
	 * 
	 * @param march
	 * @return
	 */
	public boolean isMarchState();

	/**
	 * 是否是集结行军
	 * 
	 * @return
	 */
	public boolean isMassMarch();

	/**
	 * 是否是集结加入行军
	 * 
	 * @return
	 */
	public boolean isMassJoinMarch();

	/**
	 * 是否是援助类行军
	 * 
	 * @return
	 */
	public boolean isAssistanceMarch();

	/**
	 * 是否是国王战行军
	 * 
	 * @return
	 */
	public boolean isPresidentMarch();

	/**
	 * 是否是国王战箭塔行军
	 * 
	 * @return
	 */
	public boolean isPresidentTowerMarch();

	/**
	 * 是否是超级武器行军
	 * 
	 * @return
	 */
	public boolean isSuperWeaponMarch();

	/**
	 * 是否是联盟舱体守护行军
	 * @return
	 */
	public boolean isGuildSpaceMarch();
	
	/** 小战区行军 */
	public default boolean isXZQMarch(){
		return false;
	}

	/**
	 * 是否是航海要塞行军
	 */
	public boolean isFortressMarch();
	
	/**
	 * 是否是战旗行军
	 * 
	 * @return
	 */
	public boolean isWarFlagMarch();
	
	/**
	 * 是否是联盟领地行军
	 * 
	 * @param worldMarch
	 * @return
	 */
	public boolean isManorMarch();

	/**
	 * 只计算队长的作用号(除了1546的)
	 */
	public default int getLeaderMaxMassJoinSoldierNum(Player leader) {
		int leaderSoldierCnt = leader.getMaxMarchSoldierNum(this.getMarchEntity().getEffectParams(), this.isNationMassMarch());
		// 卫星通讯所集结上限
		BuildingCfg cfg = leader.getData().getBuildingCfgByType(BuildingType.SATELLITE_COMMUNICATIONS);
		int joinMax = cfg == null ? 0 : cfg.getBuildupLimit();
		// 作用号606： 实际集结部队上限 = 基础集结部队上限*（1 + 作用值/10000）
		joinMax *= 1 + leader.getData().getPlayerEffect().getEffVal(EffType.GUILD_TEAM_ARMY_PER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
		// 作用号607： 实际集结部队上限 = 基础集结部队上限 + 作用值
		joinMax += leader.getData().getPlayerEffect().getEffVal(EffType.GUILD_TEAM_ARMY_NUM, getMarchEntity().getEffectParams());
		return leaderSoldierCnt + joinMax;
	}
	
	/**
	 * 获取当前集结上限 -> (队长的 + 队员的 )
	 * 
	 * @return
	 */
	public default int getMaxMassJoinSoldierNum(Player leader){
		return getMaxMassJoinSoldierNum(leader, null);
	}


	/**
	 * 获取驻扎行军id
	 */
	public Set<? extends IWorldMarch> getQuarterMarch();
	
	/**
	 * 获取当前集结上限 -> (队长的 + 队员的 + 预到达的)
	 */
	public default int getMaxMassJoinSoldierNum(Player leader, Player perReachMarchPlayer) {
		
		int leaderMassJoinSoldierNum = getLeaderMaxMassJoinSoldierNum(leader);
		
		// 作用号1546：集结队伍集结上限增加，集结成员的也计算，可以叠加
		int eff1546 = 0;
		
		try {
			Set<IWorldMarch> marchs = new HashSet<>();
			
			// 队长的
			marchs.add(this);
			
			// 驻扎行军的
			marchs.addAll(getQuarterMarch());
			
			// 队员的
			marchs.addAll(getMassJoinMarchs(true));
			
			for (IWorldMarch joinMarch : marchs) {
				Player joinPlayer = joinMarch.getPlayer();
				if (joinPlayer == null) {
					continue;
				}
				eff1546 += joinPlayer.getEffect().getEffVal(EffType.HERO_1546, getMarchEntity().getEffectParams());
			}
			
			
			// 预到达的
			if(Objects.nonNull(perReachMarchPlayer)){
				eff1546 += perReachMarchPlayer.getEffect().getEffVal(EffType.HERO_1546, getMarchEntity().getEffectParams());
			}
			
			int effect1546Power = ConstProperty.getInstance().getEffect1546Power();
			eff1546 = Math.min(eff1546, effect1546Power);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return leaderMassJoinSoldierNum + eff1546;
	}
	
	public default Set<? extends IWorldMarch> getMassJoinMarchs(boolean needReach) {
		return Collections.emptySet();
	}

	/**
	 * 已到达或者已驻扎的行军
	 * 
	 * @param march
	 * @return
	 */
	public boolean isReachAndStopMarch();

	/**
	 * 是否是联盟领地行军
	 * 
	 * @return
	 */
	public boolean isManorMarchReachStatus();

	/**
	 * 是否是需要tick计算的行军
	 * 
	 * @return
	 */
	public boolean isNeedCalcTickMarch();

	/**
	 * 计算行军需要消耗的时间 (单位：毫秒)
	 * 
	 * @return
	 */
	public long getMarchNeedTime();

	/**
	 * 获取基础行军速度
	 * 
	 * @return
	 */
	public double getMarchBaseSpeed();

	/**
	 * 行军开始时调用
	 */
	public void onMarchStart();

	/**
	 * 行军到达
	 */
	public void onMarchReach(Player player);

	/**
	 * 行军返回时调用
	 */
	public void onMarchReturn();

	/**
	 * 行军召回时调用
	 */
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint);

	/**
	 * 采集停留，行军驻留，参与集结着在队长家停留，修改状态通知客户端
	 * 
	 * @param march
	 * @param status
	 * @param armys
	 * @param targetPoint
	 * @return
	 */
	public void onMarchStop(int status, List<ArmyInfo> armys, WorldPoint targetPoint);

	/**
	 * 计算指定坐标点的世界观察者列表
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	default Set<String> calcPointViewers(int viewRadiusX, int viewRadiusY){
		 Set<String> viewerIds = WorldUtil.calcPointViewers(this.getTerminalX(), this.getTerminalY(), 0, 0);
		 return viewerIds;
	}
			
			
	
	/**
	 * 更新行军
	 */
	public void updateMarch();

	/**
	 * 是否需要在联盟战争界面显示
	 * 
	 * @return
	 */
	public boolean needShowInGuildWar();
	
	/**
	 * 是否需要在国家战争界面显示
	 * @return
	 */
	public boolean needShowInNationWar();

	/**
	 * 获取主动方联盟战争界面信息
	 * 
	 * @return
	 */
	public GuildWarTeamInfo.Builder getGuildWarInitiativeInfo();

	/**
	 * 获取被动方联盟战争界面信息
	 */
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo();

	/**
	 * 检查联盟战争显示
	 */
	public boolean checkGuildWarShow();

	/**
	 * 采集资源
	 * 
	 * @return
	 */
	public boolean doCollectRes(boolean changeSpeed);

	/**
	 * 退出联盟行军处理
	 */
	public void doQuitGuild(String guildId);

	/** 获取部队 */
	public default List<ArmyInfo> getArmys() {
		return getMarchEntity().getArmyCopy();
	}

	/** 获取部队总战力 */
	public default float getArmysPowers() {
		List<ArmyInfo> armyInfoList = getMarchEntity().getArmyCopy();
		float power = 0;
		for (ArmyInfo armyInfo : armyInfoList) {
			int armyId = armyInfo.getArmyId();
			int armyCount = armyInfo.getTotalCount();
			BattleSoldierCfg thisCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			power += (thisCfg.getPower() * armyCount);
		}
		return power;
	}

	
	/** 行军玩家 */
	public default Player getPlayer() {
		return GlobalData.getInstance().makesurePlayer(this.getPlayerId());
	}

	/** 出征英雄 */
	public default List<PlayerHero> getHeros() {
		try {

			if (Objects.isNull(getPlayer())) {
				return Collections.emptyList();
			}
			return getPlayer().getHeroByCfgId(getMarchEntity().getHeroIdList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	public default List<Integer> getHeroIdList(){
		return getMarchEntity().getHeroIdList();
	}

	default boolean isGrabResMarch() {
		WorldMarchType marchType = getMarchType();
		return marchType == WorldMarchType.ATTACK_PLAYER ||
				marchType == WorldMarchType.MASS ||
				marchType == WorldMarchType.MASS_JOIN;
	}

	/**
	 * 目标点迁城时, 自身的处理
	 */
	public void targetMoveCityProcess(Player targetPlayer, long currentTime);

	/**
	 * 玩家自己迁城时发出的行军处理
	 */
	public void moveCityProcess(long currentTime);

	/**
	 * 行军删除前在玩家线程对玩家的影响操作 注: 此方法仅是对行军立即删除前做的操作, 正常按时间到达的行军, 不走此方法.(例如: 迁城)
	 */
	public boolean beforeImmediatelyRemoveMarchProcess(Player player);

	default int getTerminalId() {
		return getMarchEntity().getTerminalId();
	}

	default int getMarchStatus() {
		return getMarchEntity().getMarchStatus();
	}

	default int getAlarmPointId() {
		return getMarchEntity().getAlarmPointId();
	}

	default int getTerminalY() {
		return getMarchEntity().getTerminalY();
	}

	default int getTerminalX() {
		return getMarchEntity().getTerminalX();
	}

	default long getStartTime() {
		return getMarchEntity().getStartTime();
	}

	default long getEndTime() {
		return getMarchEntity().getEndTime();
	}

	default long getMassReadyTime() {
		return getMarchEntity().getMassReadyTime();
	}

	default String getAssistantStr() {
		return getMarchEntity().getAssistantStr();
	}

	default int getOrigionX() {
		return getMarchEntity().getOrigionX();
	}

	default int getOrigionY() {
		return getMarchEntity().getOrigionY();
	}

	default int getOrigionId() {
		return getMarchEntity().getOrigionId();
	}

	default int getSuperSoldierId(){
		return getMarchEntity().getSuperSoldierId();
	}
	
	default boolean isExtraSpyMarch() {
		return getMarchEntity().isExtraSpyMarch();
	}
	
	default boolean isNationMassMarch() {
		return false;
	}
	
	public default boolean needShowInNation(Player observer) {
		return false;
	}
}
