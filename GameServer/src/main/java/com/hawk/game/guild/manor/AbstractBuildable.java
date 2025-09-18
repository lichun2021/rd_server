package com.hawk.game.guild.manor;

import java.util.List;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfo;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfoList;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 可建造抽象类
 * @author zhenyu.shang
 * @since 2017年7月11日
 */
public abstract class AbstractBuildable {
	
	private double lastBuildSpeed;
	
	public double getLastBuildSpeed() {
		return lastBuildSpeed;
	}

	public void setLastBuildSpeed(double lastBuildSpeed) {
		this.lastBuildSpeed = lastBuildSpeed;
	}

	public WorldPoint getPoint() {
		return WorldPointService.getInstance().getWorldPoint(getPositionId());
	}
	
	public IWorldMarch getMarchLeader(){
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		if(marchs != null && !marchs.isEmpty()){
			return marchs.get(0);
		}
		return null;
	}
	
	public void makeUIProtocol(ManorPlayerInfoList.Builder builder){
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		if(marchs == null || marchs.isEmpty()){
			return;
		}
		IWorldMarch leaderMarch = marchs.get(0);
		if(leaderMarch == null){
			return;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		if(leader != null){
			int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);
			builder.setMaxArmyCount(maxMassSoldierNum);
			builder.setMaxMarchCount(0);
		}
		for (IWorldMarch march : marchs) {
			ManorPlayerInfo.Builder infoBuilder = ManorPlayerInfo.newBuilder();
			// 获取玩家对象
			Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			infoBuilder.setPlayerId(player.getId());
			infoBuilder.setName(player.getName());
			infoBuilder.setPfIcon(player.getPfIcon());
			infoBuilder.setIcon(player.getIcon());
			infoBuilder.setGuildTag(player.getGuildTag());
			List<ArmyInfo> armyInfos = march.getMarchEntity().getArmys();
			for (ArmyInfo armyInfo : armyInfos) {
				KeyValuePairInt.Builder kb = KeyValuePairInt.newBuilder();
				kb.setKey(armyInfo.getArmyId());
				kb.setVal(armyInfo.getFreeCnt());
				kb.setSoldierStar(player.getSoldierStar(armyInfo.getArmyId()));
				kb.setSoldierPlantStep(player.getSoldierStep(armyInfo.getArmyId()));
				kb.setPlantSkillLevel(player.getSoldierPlantSkillLevel(armyInfo.getArmyId()));
				kb.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(armyInfo.getArmyId()));
				infoBuilder.addArmy(kb.build());
			}
			List<PlayerHero> hero = march.getHeros();
			for (PlayerHero playerHero : hero) {
				infoBuilder.addHeros(playerHero.toPBobj());
			}
			Optional<SuperSoldier> sups = player.getSuperSoldierByCfgId(march.getSuperSoldierId());
			if(sups.isPresent()){
				infoBuilder.setSsoldier(sups.get().toPBobj());
			}
			int maxMassSoldierNum = march.getMaxMassJoinSoldierNum(player);
			infoBuilder.setMaxArmyCount(maxMassSoldierNum);
			builder.addInfos(infoBuilder.build());
		}
	}
	
	/**
	 * 获取当前建筑的 建设/摧毁 速度
	 * @return
	 */
	public double getCurrentSpeed(boolean isBreak, int max){
		int maxSpeed = GuildConstProperty.getInstance().getDefaultBuildSpeed();
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		if(marchs == null || marchs.isEmpty()){
			return 0;
		}
		//此处算兵力数量待定, 战斗力算法不一定
		float p1 = GuildConstProperty.getInstance().getBuildSpeedparameter1();
		float p2 = GuildConstProperty.getInstance().getBuildSpeedparameter2();
		float d1 = GuildConstProperty.getInstance().getDestroySpeedparameter();
		//速度值
		double totalSpeed = 0;
		for (IWorldMarch worldMarch : marchs) {
			String playerId = worldMarch.getMarchEntity().getPlayerId();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			int effValue = player.getEffect().getEffVal(Const.EffType.GUILD_BUILD_SPEED_UP, worldMarch.getMarchEntity().getEffectParams());
			
			//判断队列状态是否已经正确
			for (ArmyInfo armyInfo : worldMarch.getMarchEntity().getArmys()) {
				int cnt = armyInfo.getFreeCnt();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
				float unitPower = cfg.getPower();
				//速度=（战斗力^p1）* p2
				double speed = Math.pow(unitPower, p1) * p2 * (1 + effValue * GsConst.EFF_PER);
				totalSpeed += cnt * speed;
			}
		}
		if(isBreak){
			totalSpeed *= d1;
		}
		return Math.min(max, Math.min(totalSpeed, maxSpeed));
	}
	
	/**
	 * 广播行军更新
	 */
	public void broadcastPointMarchUpate(){
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		if(marchs == null || marchs.isEmpty()){
			return;
		}
		long overTime = getOverTime() * 1000L;
		for (IWorldMarch worldMarch : marchs) {
			//重新计算所有行军摧毁时间
			worldMarch.getMarchEntity().setStartTime(HawkTime.getMillisecond());//设置当前时间为摧毁开始时间
			worldMarch.getMarchEntity().setMarchJourneyTime(overTime);
			worldMarch.getMarchEntity().setEndTime(worldMarch.getMarchEntity().getStartTime() + overTime);
			worldMarch.updateMarch();
		}
	}
	
	/**
	 * 是否能进入状态
	 * @return
	 */
	public abstract boolean canEnterState(int stat);
	
	/**
	 * 尝试进入状态
	 * @return
	 */
	public abstract boolean tryEnterState(int stat);
	
	/**
	 * 判断当前建筑值是否已经满了
	 * @return
	 */
	public abstract boolean isFullLife();
	
	/**
	 * 获取联盟ID
	 * @return
	 */
	public abstract String getGuildId();
	
	
	/**
	 * 获取当前剩余时间
	 * @return
	 */
	public abstract int getOverTime();
	
	/**
	 * 是否能进入建造状态
	 * @return
	 */
	public abstract boolean canEnterBuildState();
	
	/**
	 * 尝试进入建造状态
	 * @return
	 */
	public abstract boolean tryEnterBuildState();
	
	/**
	 * 获取坐标点ID
	 * @return
	 */
	public abstract int getPositionId();
	
	/**
	 * 获取建筑值
	 * @return
	 */
	public abstract double getbuildLife();
	
	/**
	 * 获取坐标点ID
	 * @return
	 */
	public abstract int getbuildStat();
	
	/**
	 * 获取建筑类型
	 * @return
	 */
	public abstract TerritoryType getBuildType();
	
	/**
	 * 获取建筑类型
	 * @return
	 */
	public abstract int getLevel();
	
	
	public abstract int getbuildLimtUp();
	
	/**
	 * 行军遣返
	 * @param playerId
	 * @param targetPlayerId
	 * @return
	 */
	public boolean repatriateMarch(Player player, String targetPlayerId) {
		// 驻军队长行军
		IWorldMarch leanderMarch = getMarchLeader();
		if (leanderMarch == null) {
			return false;
		}
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leanderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		// 不是本盟的没有权限操作
		if (!player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(getPositionId());
		for (IWorldMarch iWorldMarch : marchs) {
			if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
				continue;
			}
			WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, iWorldMarch.getMarchId());
			WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
		}
		return true;
	}
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId) {
		// 驻军队长行军
		IWorldMarch leanderMarch = getMarchLeader();
		if (leanderMarch == null) {
			return false;
		}
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leanderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		// 不是本盟的没有权限操作
		if (!player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changeManorMarchLeader(getPositionId(), targetPlayerId);
				return true;
			}
		});
		return true;
	}
}

