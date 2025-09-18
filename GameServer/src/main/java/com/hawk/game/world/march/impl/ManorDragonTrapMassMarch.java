package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackKVCfg;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.ManorMarchEnum;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo.Builder;
import com.hawk.game.protocol.Mail.PBGuildDragonAttackDamageMail;
import com.hawk.game.protocol.Mail.PBGuildDragonAttackDamagePlayer;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.ManorMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 集结攻占联盟领地
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorDragonTrapMassMarch extends PlayerMarch implements MassMarch, ManorMarch {

	public ManorDragonTrapMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.DRAGON_ATTACT_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.DRAGON_ATTACT_MASS_JOIN;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch leaderMarch = getMarchEntity();
		// 目标点
		int terminalId = leaderMarch.getTerminalId();
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点为空
		if (point == null) {
			dragonAttckMarchReturn();
			WorldMarchService.logger.info("ManorDragonTrapMassMarch onMarchReach, point null, terminalId:{}", terminalId);
			return;
		}
		// 非野怪点
		if (point.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
			dragonAttckMarchReturn();
			WorldMarchService.logger.error("attackNianMarch reach, point not nian, terminalId:{}", terminalId);
			return;
		}
		//数据检查不过关
		int checkRlt = ManorMarchEnum.DRAGON_ATTACT_MASS.checkMarch(point, player, true);
		if(checkRlt != 0){
			dragonAttckMarchReturn();
			WorldMarchService.logger.error("attackNianMarch reach, point not nian, terminalId:{}", terminalId);
			return;
		}
		
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(player);
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
			atkMarchs.add(massJoinMarch);
		}
		//打架
		BattleOutcome battleOutcome = this.doBattle(point,atkPlayers,atkMarchs);
		if(Objects.isNull(battleOutcome)){
			this.dragonAttckMarchReturn();
			return;
		}
		// 发送战斗结果，用于前端播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, true, battleOutcome.getAftArmyMapAtk().get(player.getId()), Collections.emptyList(), false);
		//处理结果
		this.doBattleResult(battleOutcome,point,atkPlayers);
		//行军返回
		WorldMarchService.getInstance().onMarchReturn(this, battleOutcome.getAftArmyMapAtk().get(this.getMarchEntity().getPlayerId()), 0);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, battleOutcome.getAftArmyMapAtk().get(massJoinMarch.getMarchEntity().getPlayerId()), 0);
		}
	}
	
	
	@Override
	public Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		int terminalId = this.getMarchEntity().getTerminalId();
		int pos[] = GameUtil.splitXAndY(terminalId);
		builder.setPointType(WorldPointType.GUILD_TERRITORY);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		return builder;
	}
	
	
	
	
	public BattleOutcome doBattle(WorldPoint point,List<Player> atkPlayers,List<IWorldMarch> atkMarchs){
		try {
			//获取陷阱
			GuildDragonTrap trap = this.getGuildDragonTrap(point.getGuildId());
			PveBattleIncome battleIncome = BattleService.getInstance().initGuildDragonAttackBattleData(BattleType.ATTACK_GUNDAM_PVE, point.getId(),10000, trap.getTarpArmy(),atkMarchs);
			// 战斗数据输出
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			return battleOutcome;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	
	
	public void doBattleResult(BattleOutcome battleOutcome,WorldPoint point,List<Player> atkPlayers){
		
		String battleId = HawkUUIDGenerator.genUUID();
		GuildDragonTrap trap = this.getGuildDragonTrap(point.getGuildId());
		Map<String,PBGuildDragonAttackDamagePlayer.Builder> dmap = new HashMap<>();
		for (Player player : atkPlayers) {
			// 获取伤害比率
			int killCount = getKillCount(battleOutcome, player);
			// 发邮件:伤害奖励
			PBGuildDragonAttackDamagePlayer.Builder damageBuilder = PBGuildDragonAttackDamagePlayer.newBuilder();
			damageBuilder.setPlayerId(player.getId());
			damageBuilder.setName(player.getName());
			damageBuilder.setIcon(player.getIcon());
			damageBuilder.setPfIcon(player.getPfIcon());
			damageBuilder.setKillScore(killCount);
			dmap.put(player.getId(), damageBuilder);
			//统计伤害
			trap.addDamage(player.getId(), killCount);
			
			LogUtil.logGuildDragonAttackFight(player, trap.getTermId(), trap.getGuildId(), battleId, killCount, player.getCreateTime());
		}
		
		List<PBGuildDragonAttackDamagePlayer.Builder> mlist = new ArrayList<>();
		mlist.addAll(dmap.values());
		Collections.sort(mlist, new Comparator<PBGuildDragonAttackDamagePlayer.Builder>() {
			@Override
			public int compare(com.hawk.game.protocol.Mail.PBGuildDragonAttackDamagePlayer.Builder o1,
					com.hawk.game.protocol.Mail.PBGuildDragonAttackDamagePlayer.Builder o2) {
				if(o1.getKillScore() != o2.getKillScore()){
					return o1.getKillScore() > o2.getKillScore()?-1:1;
				}
				return o1.getPlayerId().compareTo(o2.getPlayerId());
			}
		});
		//排序
		for(int r =0;r<mlist.size();r++){
			PBGuildDragonAttackDamagePlayer.Builder damageBuilder = mlist.get(r);
			damageBuilder.setRank(r+1);
		}
		//发战报
		for (Player player : atkPlayers) {
			// 获取伤害比率
			PBGuildDragonAttackDamageMail.Builder dbuilder = PBGuildDragonAttackDamageMail.newBuilder();
			dbuilder.setX(point.getX());
			dbuilder.setY(point.getY());
			dbuilder.setSelfDamage(dmap.get(player.getId()));
			mlist.forEach(mi->dbuilder.addRankDamages(mi));
			MailParames.Builder mailBuilder= MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.GUILD_DRAGON_ATTCK_PLAYER_FIGHT)
					.addSubTitles(dmap.get(player.getId()).getKillScore())
					.addContents(dbuilder);
			SystemMailService.getInstance().sendMail(mailBuilder.build());
		}
		
	}
	
	public int getKillCount(BattleOutcome battleOutcome, Player player) {
		// 单人击杀玩家数量
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		Map<Integer, Double>  pmap = cfg.getDamageParamMap();
		long playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		
		HawkLog.logPrintln("=getKillCount======================"+player.getName()+"=========start================");
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			BattleSoldierCfg bcfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, playerArmyInfo.getArmyId());
			if(Objects.isNull(bcfg)){
				continue;
			}
			int soldierType  = bcfg.getSoldierType().getNumber();
			double param = pmap.getOrDefault(soldierType, 10000d);
			double armyKill = param * playerArmyInfo.getKillCount() / 10000;
			playerKillCount += armyKill;
			HawkLog.logPrintln("=getKillCount,armyId:{},type:{},param:{},armyKill:{},totalKill:{}", playerArmyInfo.getArmyId(),soldierType,param,armyKill,playerKillCount);
		}
		playerKillCount = (long) Math.pow(playerKillCount, cfg.getDamagePower());
		long rlt = Math.max(playerKillCount, 1); // 总有傻子出一个兵 还来问为什么伤害为0
		HawkLog.logPrintln("=getKillCount,totalKill:{},param:{},rlt:{}", playerKillCount,cfg.getDamagePower(),rlt);
		HawkLog.logPrintln("=getKillCount======================"+player.getName()+"===========over==============");
		return (int) rlt;
	}
	
	
	public int getWoundCount(BattleOutcome battleOutcome, Player player) {
		int woundCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			woundCount += playerArmyInfo.getWoundedCount();
		}
		return woundCount;
	}
	
	
	/**
	 * 获取联盟建筑
	 * @param guildId
	 * @return
	 */
	public GuildDragonTrap getGuildDragonTrap(String guildId){
		List<IGuildBuilding> list = GuildManorService.getInstance().getGuildBuildByType(guildId, TerritoryType.GUILD_DRAGON_TRAP);
		if(list.isEmpty()){
			return null;
		}
		GuildDragonTrap trap = (GuildDragonTrap) list.get(0);
		return trap;
	}

	
	
	
	/**
	 * 集结打怪行军返回
	 */
	public void dragonAttckMarchReturn() {
		// 队长行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());

		// 队员行军返回
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			// 行军返回
			WorldMarchService.getInstance().onMarchReturn(massJoinMarch, massJoinMarch.getMarchEntity().getArmys(), getMarchEntity().getTerminalId());
		}
	}

	
}
