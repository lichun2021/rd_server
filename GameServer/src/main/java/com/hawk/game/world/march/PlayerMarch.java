package com.hawk.game.world.march;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;

import com.hawk.game.GsConfig;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.march.MarchPart;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldSnowballService;

/**
 * 玩家行军
 * @author zhenyu.shang
 * @since 2017年8月25日
 */
public abstract class PlayerMarch implements IWorldMarch {
	
	private WorldMarch marchEntity;

	public PlayerMarch(WorldMarch marchEntity) {
		HawkAssert.notNull(marchEntity);
		this.marchEntity = marchEntity;
	}

	@Override
	public void register() {
		// 注册总行军
		WorldMarchService.getInstance().registerMarchs(this);
		// 注册主动行军
		WorldMarchService.getInstance().registerPlayerMarch(this);
		// 更新点信息
		WorldMarchService.getInstance().updatePointMarchInfo(this, false);
		// 添加联盟领地行军
		if (WorldUtil.isGuildBuildPoint(getMarchEntity().getTerminalId()) && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addManorMarchs(getMarchEntity().getTerminalId(), this, true);
		}
		// 国王战总统府行军
		if (this.isPresidentMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addPresidentMarch(this, true);
		}
		// 国王战总统府箭塔行军
		if (this.isPresidentTowerMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addPresidentTowerMarch(getMarchEntity().getTerminalId(), this, true);
		}
		// 超级武器行军
		if (this.isSuperWeaponMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addSuperWeaponMarch(getMarchEntity().getTerminalId(), this, true);
		}
		
		// 超级武器行军
		if (this.isXZQMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addXZQMarch(getMarchEntity().getTerminalId(), this, true);
		}
		// 航海远征行军
		if (this.isFortressMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addFortressMarch(getMarchEntity().getTerminalId(), this, true);
		}
		// 超级战旗行军		
		if (this.isWarFlagMarch() && isReachAndStopMarch()) {
			WorldMarchService.getInstance().addFlagMarchs(getMarchEntity().getTargetId(), this, true);
		}
		// 添加到联盟战争
		if (needShowInGuildWar()) {
			WorldMarchService.getInstance().addGuildMarch(this);
		}
	}

	@Override
	public void remove() {
		
		// 移除行军实体
		marchEntity.delete(true);
		
		// 移除行军表
		WorldMarchService.getInstance().onlyRemoveMarch(this.getMarchId());
		// 移除起点记录
		WorldMarchService.getInstance().removeWorldPointMarch(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY(), this);
		// 移除终点记录
		WorldMarchService.getInstance().removeWorldPointMarch(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY(), this);
		// 清理主动表
		WorldMarchService.getInstance().removePlayerMarch(this);
		// 移除联盟领地行军
		WorldMarchService.getInstance().removeManorMarch(getMarchEntity().getTerminalId(), getMarchEntity().getMarchId());
		// 移除战旗行军
		WorldMarchService.getInstance().removeFlagMarch(getMarchEntity().getTargetId(), getMarchId());
		// 移除国王战行军
		WorldMarchService.getInstance().removePresidentMarch(this);
		// 移除国王战箭塔行军
		WorldMarchService.getInstance().removePresidentTowerMarch(getMarchEntity().getTerminalId(), this.getMarchId());
		// 移除超级武器行军
		WorldMarchService.getInstance().removeSuperWeaponMarch(getMarchEntity().getTerminalId(), this.getMarchId());
		// 移除小战区行军
		WorldMarchService.getInstance().removeXZQMarch(getMarchEntity().getTerminalId(), this.getMarchId());
		// 移除要塞行军
		WorldMarchService.getInstance().removeFortressMarch(getMarchEntity().getTerminalId(), this.getMarchId());
		// 发送尖塔杀伤邮件
		FightMailService.getInstance().sendTowerKillInfoMail(this);

		// 发起自动打野行军消息
		checkAutoMarch();
		
		if (marchEntity.isOffensive()) {
			WorldMarchService.getInstance().updateOffsiveMarchBackTime(this.getPlayerId());
		}
	}
	
	/**
	 * 根据条件触发自动打野行军
	 */
	private void checkAutoMarch() {
		Player player = GlobalData.getInstance().getActivePlayer(marchEntity.getPlayerId());
		if (player == null) {
			return;
		}
		
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(marchEntity.getPlayerId());
		if (autoMarchParam == null) {
			return;
		}

		int id = marchEntity.getAutoMarchIdentify();
		autoMarchParam.resetAutoMarchStatus(id);
		
		HawkTaskManager.getInstance().postMsg(player.getXid(), AutoSearchMonsterMsg.valueOf());
	}
	
	@Override
	public Set<IWorldMarch> getMassJoinMarchs(boolean needReach) {
		return  WorldMarchService.getInstance().getMassJoinMarchs(this, needReach);
	}
	
	@Override
	public long getMarchNeedTime() {
		if (WorldUtil.isRobotMarch(this.getMarchEntity())) {
			return WorldMarchConstProperty.getInstance().getNewGuidePvpMarchTime() * 1000;
		}
		// 行军速度
		double speed = getMarchBaseSpeed();
		// 出发点
		AlgorithmPoint origion = new AlgorithmPoint(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY());
		if (getMarchEntity().getCallBackTime() > 0) {
			origion = new AlgorithmPoint(getMarchEntity().getCallbackX(), getMarchEntity().getCallbackY());
		}
		// 目标点
		AlgorithmPoint terminal = new AlgorithmPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		// 根据黑土地进行分段
		List<MarchPart> parts = WorldUtil.getMarchParts(origion, terminal);

		double time = 0;
		for (MarchPart part : parts) {
			// 正常行军时间
			double partNormalTime = getPartMarchTime(part.getDistance(), speed, false);
			// 机器人模式下 不进行黑土地减速
			if (part.isSlowDown() && !(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug())) {
				// 黑土地行军时间
				double partSlowDownTime = getPartMarchTime(part.getDistance(), speed, true);
				// 计算黑土地行军作用号效果
				partSlowDownTime = effectToMarchTime(partNormalTime, true, partSlowDownTime);

				time += partSlowDownTime;
				continue;
			}

			time += partNormalTime;
		}

		// 计算作用号效果(向上取整)
		time = Math.ceil(effectToMarchTime(time, false, 0));
		time = time > 1.0 ? time : 1.0;
		
		// 需求：首次攻击2/3级野怪，行军时间特殊处理。
		if (WorldUtil.isAtkMonsterMarch(this) 
				&& !HawkOSOperator.isEmptyString(this.getMarchEntity().getTargetPointField())
				&& WorldMarchConstProperty.getInstance().isMonsterLevelId(Integer.parseInt((this.getMarchEntity().getTargetPointField())))
				&& time > WorldMarchConstProperty.getInstance().getFirstMonsterMarchTime(Integer.parseInt((this.getMarchEntity().getTargetPointField())))) {
			time = WorldMarchConstProperty.getInstance().getFirstMonsterMarchTime(Integer.parseInt((this.getMarchEntity().getTargetPointField())));
		}
		
		// 需求：前三次攻击新版野怪，行军时间特殊处理。
		if (this.getMarchType() == WorldMarchType.NEW_MONSTER && !HawkOSOperator.isEmptyString(this.getMarchEntity().getTargetPointField())) {
			ArrayList<Integer> newMonsterSpecialTimeArr = WorldMapConstProperty.getInstance().getNewMonsterSpecialTimeArr();
			int atkTimes = Integer.parseInt(this.getMarchEntity().getTargetPointField());
			long thisTime = newMonsterSpecialTimeArr.get(atkTimes);
			time = thisTime > time ? time : thisTime;
		}
		
		return (long) (time * 1000);
	}

	/**
	 * 获取基础行军速度
	 * 
	 * @param march
	 * @return
	 */
	public double getMarchBaseSpeed() {
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		armyList.addAll(getMarchEntity().getArmys());
		if (isMassMarch()) {
			Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			for (IWorldMarch tempMarch : joinMarchs) {
				armyList.addAll(tempMarch.getMarchEntity().getArmys());
			}
		}
		return WorldUtil.minSpeedInArmy(this.getPlayer(), armyList);
	}

	/**
	 * 获取一段距离行军时间
	 * @param distance
	 * @param speed
	 * @param isSlowDownPart
	 * 
	 * @return
	 */
	protected double getPartMarchTime(double distance, double speed, boolean isSlowDownPart) {
		// 行军速度倍数因子
		double factor = 1;
		if (isSlowDownPart) {
			factor = WorldMarchConstProperty.getInstance().getWorldMarchCoreRangeTime();
		}
		
		// 行军距离修正参数
		double param1 = WorldMarchConstProperty.getInstance().getDistanceAdjustParam();
		// 部队行军类型行军时间调整参数
		double param2 = 0.0d;

		WorldMarchType marchType = this.getMarchType();
		switch (marchType) {
		case SPY:
		case DRAGON_BOAT_MARCH:
		case RESOURCE_SPREE_BOX_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getReconnoitreTypeAdjustParam();
			speed = 1.0d;
			break;
		case ASSISTANCE_RES:
			param2 = WorldMarchConstProperty.getInstance().getResAidTypeAdjustParam();
			speed = 1.0d;
			break;
		case RANDOM_BOX:
			param2 = WorldMarchConstProperty.getInstance().getBoxTypeAdjustParam();
			speed = 1.0d;
			break;
		case WAREHOUSE_STORE:
		case WAREHOUSE_GET:
			param2 = WorldMarchConstProperty.getInstance().getAllianceStoreAdjustParam();
			speed = 1.0d;
			break;
		case ATTACK_MONSTER:
			param2 = WorldMarchConstProperty.getInstance().getMonsterTypeAdjustParam();
			break;
		case MONSTER_MASS:
		case MONSTER_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getMonsterBossAdjustParam();
			break;
		case FOGGY_FORTRESS_MASS:
		case FOGGY_FORTRESS_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getFoggyAdjustParam();
			break;
		case NEW_MONSTER:
			param2 = WorldMarchConstProperty.getInstance().getNewMonsterAdjustParam();
			break;
		case GUNDAM_SINGLE:
		case GUNDAM_MASS:
		case GUNDAM_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getBossTypeAdjustParam();
			break;
		case NIAN_SINGLE:
		case NIAN_MASS:
		case NIAN_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getNianTypeAdjustParam();
			break;
		case TREASURE_HUNT:
		case OVERLORD_BLESSING_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getReconnoitreTypeAdjustParam();
			speed = 1.0d;
			break;
		case NIAN_BOX_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getNianBoxAdjustParam();
			speed = 1.0d;
			break;
		case CHRISTMAS_MASS:
		case CHRISTMAS_MASS_JOIN:
		case CHRISTMAS_SINGLE:
			param2 = WorldMarchConstProperty.getInstance().getChristmasTypeAdjustParam();
			break;
		case ESPIONAGE_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getEspionageAdjustParam();
			speed = 1.0d;
			break;
		case SNOWBALL_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getSnowballAdjustParam();
			speed = 1.0d;
			break;
		case CAKE_SHARE_MARCH:
			param2 = WorldMarchConstProperty.getInstance().getReconnoitreTypeAdjustParam();
			speed = 1.0d;
			break;
		default:
			param2 = WorldMarchConstProperty.getInstance().getArmyTypeAdjustParam();
			break;
		}

		return Math.pow((distance), param1) * param2 * factor / speed;
	}

	/**
	 * 添加行军速度作用号
	 * 
	 * @param march
	 * @param speed
	 * @return
	 */
	private double effectToMarchTime(double time, boolean slowDown, double slowDownTime) {
		Player player = GlobalData.getInstance().makesurePlayer(getMarchEntity().getPlayerId());

		if (slowDown) {
			// 作用号207： 黑土地行军加速 -> 行军时间 = 非雪地时间 + 雪地时间/（1 + 作用值/10000）
			slowDownTime /= 1 + player.getEffect().getEffVal(EffType.MARCH_SPD_SNOW_LAND, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			return time + slowDownTime;
		}

		int marchType = getMarchEntity().getMarchType();

		int speedUpEffectVal = 0;
		
		// 作用号203： 行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD, getMarchEntity().getEffectParams());

		// 作用号204： 攻击野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_MONSTER, getMarchEntity().getEffectParams());
		}

		// 作用号206/4020： 侦察时行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isSpyMarch(marchType)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_SPY, getMarchEntity().getEffectParams());
			speedUpEffectVal += player.getEffect().getEffVal(EffType.SPY_MARCH_SPEED_ADD, getMarchEntity().getEffectParams());
		}

		// 作用号211： 新版野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (marchType == WorldMarchType.NEW_MONSTER_VALUE  || marchType == WorldMarchType.AGENCY_MARCH_MONSTER_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_NEW_MONSTER, getMarchEntity().getEffectParams());
		}
		
		// 作用号212： 野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this) || marchType == WorldMarchType.NEW_MONSTER_VALUE || WorldUtil.isAtkBossMarch(this)
				 || marchType == WorldMarchType.AGENCY_MARCH_MONSTER_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_ALL_MONSTER, getMarchEntity().getEffectParams());
		}
		
		// 作用号222： 野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this) || marchType == WorldMarchType.NEW_MONSTER_VALUE || WorldUtil.isAtkBossMarch(this)
				 || marchType == WorldMarchType.AGENCY_MARCH_MONSTER_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_ALL_MONSTER_CARD, getMarchEntity().getEffectParams());
		}
		
		// 作用号610： 联盟交易时行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (marchType == WorldMarchType.ASSISTANCE_RES_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.GUILD_TRADE_MARCH_SPD, getMarchEntity().getEffectParams());
		}

		// 作用号614： 联盟援助时，援军部队行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (this.isAssistanceMarch()) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.GUILD_HELP_MARCH_SPD, getMarchEntity().getEffectParams());
		}

		// 作用号221：高达行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkBossMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.MARCH_SPD_GUNDAM, getMarchEntity().getEffectParams());
		}
		
		// 作用号208： 我方开启集结后，其他指挥官加入集结的队伍的行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (this.isMassJoinMarch()) {
			WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
			if (targetPoint != null) {
				Player leader = GlobalData.getInstance().makesurePlayer(targetPoint.getPlayerId());
				if(leader != null){
					speedUpEffectVal += leader.getEffect().getEffVal(Const.EffType.MARCH_SPD_MASS, getMarchEntity().getEffectParams());
				}
			}
		}
		
		// 雪球行军
		if (marchType == WorldMarchType.SNOWBALL_MARCH_VALUE) {
			int snowballSpeed = WorldSnowballService.getInstance().getSpeedUpEffValue(getPlayerId(), getTerminalId());
			speedUpEffectVal += snowballSpeed;
		}
		
		time /= (1 + speedUpEffectVal * GsConst.EFF_PER);

		int slowDownEffectVal = 0;
		
		// 作用号445：玩家基地被侦察或攻击时，敌人行军时间提升XX倍 -> 实际行军时间 = 其他作用号加速后行军时间 *（1 + 作用值/10000）
		WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		if (this.isReturnBackMarch()) {
			terminalPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getOrigionId());
		}
		
		if ((marchType == WorldMarchType.ATTACK_PLAYER_VALUE 
				|| marchType == WorldMarchType.SPY_VALUE 
				|| marchType == WorldMarchType.MASS_VALUE)
				&& terminalPoint != null
				&& WorldUtil.isPlayerPoint(terminalPoint)) {
			Player defPlayer = GlobalData.getInstance().makesurePlayer(getMarchEntity().getTargetId());
			if (defPlayer != null && defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD) > 0) {
				slowDownEffectVal += defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD);
			}
		}
		
		time *= (1 + slowDownEffectVal * GsConst.EFF_PER);
		
		return time;
	}

	public WorldMarch getMarchEntity() {
		return marchEntity;
	}

	public String getPlayerId() {
		return marchEntity.getPlayerId();
	}

	@Override
	public String getMarchId() {
		return marchEntity.getMarchId();
	}

	@Override
	public boolean isPassiveMarch() {
		return false;
	}

	@Override
	public boolean isReturnBackMarch() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE;
	}

	@Override
	public boolean isMarchState() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	@Override
	public boolean isReachAndStopMarch() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_EXPLORE_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE
				|| isManorMarchReachStatus();
	}

	@Override
	public boolean isManorMarchReachStatus() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE
				|| getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		int status = getMarchEntity().getMarchStatus();
		// 出征，收集，回程，集结等待, 探索需要tick
		return status == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_EXPLORE_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_HIDDEN_VALUE;
	}

	@Override
	public String toString() {
		return marchEntity.toString();
	}

	@Override
	public int compareTo(IWorldMarch o) {
		return marchEntity.compareTo(o.getMarchEntity());
	}
	
	@Override
	public boolean doCollectRes(boolean changeSpeed) {
		return true;
	}
}
