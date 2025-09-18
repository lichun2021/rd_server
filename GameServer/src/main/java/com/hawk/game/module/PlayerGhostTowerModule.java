package com.hawk.game.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.GhostTowerCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.invoker.GhostTowerMonsterCreateInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GhostTower.AttackGhostSucessResp;
import com.hawk.game.protocol.GhostTower.CollectTowerProdutionResp;
import com.hawk.game.protocol.GhostTower.GhostTowerChanllageResp;
import com.hawk.game.protocol.GhostTower.GhostTowerInfoResp;
import com.hawk.game.protocol.GhostTower.PBGhostTower;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.PBTowerGhost;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGhostTowerPass;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.GhostInfo;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResTreasurePointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class PlayerGhostTowerModule  extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 生成幽灵间隔
	 */
	private static final long GHOST_CREATE_PERIOD = 2000L;
	

	/**
	 * 上一次幽灵生成时间
	 */
	private long lastGhostCreate;
	
	
	public PlayerGhostTowerModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		this.syncGhostTowerInfo();
		return true;
	}
	
	
	/**
	 * 挑战
	 */
	@ProtocolHandler(code = HP.code.GHOST_TOWER_CHALLANGE_REQ_VALUE)
	public void onChallengeTower(HawkProtocol protocol){
		int stageId = this.player.getPlayerGhostTowerStage();
		int nextStage = stageId +1;
		GhostTowerCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(GhostTowerCfg.class, nextStage);
		if(cfg == null){
			return;
		}
		if(this.player.isCsPlayer()){
			return;
		}
		if(!this.ghostTowerIsBuild()){
			return;
		}
		int ghostPointId = WorldResTreasurePointService.getInstance().getGhostPoint(this.player.getId());
		WorldPoint point = this.getGhost(ghostPointId);
		if(point != null && point.getMonsterId() == cfg.getId()){
			GhostTowerChanllageResp.Builder builder = GhostTowerChanllageResp.newBuilder(); 
			builder.setPosX(point.getX());
			builder.setPoxY(point.getY());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_CHALLANGE_RESP,builder));
			logger.info("onChallengeTower,ghost have, playerId: {},pointId:{}", 
					player.getId(),ghostPointId);
			return;
		}
		//生成过于频繁
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastGhostCreate < GHOST_CREATE_PERIOD){
			sendError(protocol.getType(), Status.Error.GHOST_TOWER_MONSTER_CAN_NOT_SEAT);
			return;
		}
		this.lastGhostCreate = curTime;
		String playerId = this.player.getId();
		WorldPoint playerPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		int pointId = playerPoint.getId();
		WorldTask task = new WorldTask(GsConst.WorldTaskType.GHOST_TOWER_MONSTER_POINT_GENERATE) {
			@Override
			public boolean onInvoke() {
				WorldPoint point = genGhost(playerId, pointId,cfg);
				//生成点失败，发错误码
				if(point == null){
					sendError(protocol.getType(), Status.Error.GHOST_TOWER_MONSTER_CAN_NOT_SEAT);
				}
				return true;
			}
		};
		long worldThreadId = WorldThreadScheduler.getInstance().getThreadState().getIntValue("threadId");
		long currThreadId = HawkOSOperator.getThreadId();
		if (currThreadId != worldThreadId) {
			WorldThreadScheduler.getInstance().postWorldTask(task);
		} else {
			task.onInvoke();
		}
	}
	
	/**
	 * 幽灵详情
	 */
	@ProtocolHandler(code = HP.code.GHOST_TOWER_MONSTER_DETAIL_REQ_VALUE)
	public void onGhostDetail(HawkProtocol protocol){
		int ghostPointId = WorldResTreasurePointService.getInstance().getGhostPoint(this.player.getId());
		WorldPoint point = this.getGhost(ghostPointId);
		if(point == null){
			logger.info("onGhostDetail,ghost point null, playerId: {},pointId:{}", 
					player.getId(),ghostPointId);
			return;
		}
		
		GhostInfo ghost = point.getGhostInfo();
		if(ghost == null){
			logger.info("onGhostDetail,ghost info null, playerId: {},pointId:{}", 
					player.getId(),ghostPointId);
			return;
		}
		//不是自己的怪
		if(!point.getOwnerId().equals(this.player.getId())){
			sendError(protocol.getType(), Status.Error.GHOST_TOWER_MONSTER_NOT_YOURS);
			return;
		}
		if(this.player.isCsPlayer()){
			return;
		}
		PBTowerGhost.Builder builder = PBTowerGhost.newBuilder();
		//层级ID
		builder.setStageId(point.getMonsterId());
		//部队
		List<ArmyInfo> armylist = ghost.getArmyList();
		for (ArmyInfo armyInfo : armylist) {
			builder.addArmyInfo(armyInfo.toArmySoldierPB(ghost.getNpcPlayer()));
		}
		Map<EffType, Integer> effectMap = new HashMap<EffType, Integer>();
		//组装英雄信息
		List<Integer> heroInfoIds = ghost.getHeroIds();
		if(heroInfoIds != null){
			for (PlayerHero hero :ghost.getNpcPlayer().getHeroByCfgId(heroInfoIds)) {
				builder.addHeros(hero.toPBobj());
				Map<EffType, Integer> heroEff = hero.battleEffectMap();
				heroEff.forEach((type,value)-> this.effextPlus(effectMap, type, value));
			}
		}
		//机甲
		if(ghost.getSuperSoldierId() > 0){
			int superSoldierId = ghost.getSuperSoldierId();
			Optional<SuperSoldier> opt= ghost.getNpcPlayer().getSuperSoldierByCfgId(superSoldierId);
			if(opt.isPresent()){
				SuperSoldier ssoldier = opt.get();
				builder.setSsoldier(ssoldier.toPBobj());
				Map<EffType, Integer> ssoldierEff =ssoldier.battleEffect();
				ssoldierEff.forEach((type,value)-> this.effextPlus(effectMap, type, value));
			}
		}
		//装备
		if(ghost.getNpcPlayer().getArmourSuit().size() > 0){
			ArmourBriefInfo armour = ghost.getNpcPlayer().genArmourBriefInfo(ArmourSuitType.ONE);
			builder.setArmourBire(armour);
			List<ArmourEntity> armourList = ghost.getNpcPlayer().getData().getArmourEntityList();
			for(ArmourEntity entity : armourList){
				List<ArmourEffObject> elist = entity.getExtraAttrEff();
				List<ArmourEffObject> slist = entity.getSkillEff();
				elist.forEach(attr->{
					this.effextPlus(effectMap, attr.getType(), attr.getEffectValue());
				});
				slist.forEach(attr->{
					this.effextPlus(effectMap, attr.getType(), attr.getEffectValue());
				});
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_MONSTER_DETAIL_RESP_VALUE,builder));
	}
	
	
	/**
	 * 收集产出
	 */
	@ProtocolHandler(code = HP.code.GHOST_TOWER_COLLECT_REQ_VALUE)
	public void onCollectProdution(HawkProtocol protocol){
		int stageId = this.player.getPlayerGhostTowerStage();
		long lastTime = this.player.getPlayerGhostTowerProductTime();
		GhostTowerCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(GhostTowerCfg.class, stageId);
		if(cfg == null){
			return;
		}
		if(this.player.isCsPlayer()){
			return;
		}
		if(!this.ghostTowerIsBuild()){
			return;
		}
		int ptime = cfg.getProductTime() * 1000;
		long curTime = HawkTime.getMillisecond();
		int pcount = (int) ((curTime - lastTime)/ptime);
		int extTime = (int) ((curTime - lastTime)%ptime);
		long productBegin = curTime;
		if(pcount < cfg.getProductMaxCount()){
			productBegin -= extTime;
		}
		pcount = Math.min(pcount, cfg.getProductMaxCount());
		if(pcount <= 0){
			logger.info("onCollectProdution, pcount<= 0,playerId: {},collectTime:{},lastTime:{}", 
					player.getId(),curTime,lastTime);
			return;
		}
		logger.info("onCollectProdution, playerId: {},count:{},collectTime:{},lastTime:{}", 
				player.getId(),pcount,curTime,lastTime);
		player.setPlayerGhostTowerProductTime(productBegin);
		AwardItems award = AwardItems.valueOf();
		for(int i=0;i<pcount;i++){
			String production = cfg.getProduction();
			int randomProduction = cfg.getRandomProduction();
			AwardCfg awardCfg = HawkConfigManager.getInstance().
					getConfigByKey(AwardCfg.class, randomProduction);
			AwardItems awardItems = AwardItems.valueOf(production);
			AwardItems radnomAwardItems = awardCfg.getRandomAward();
			award.addItemInfos(awardItems.getAwardItems());
			award.addItemInfos(radnomAwardItems.getAwardItems());
		}
		award.rewardTakeAffectAndPush(player, Action.GHOST_TOWER_COLLECT_RES);
		
		long productTime = curTime - lastTime;
		CollectTowerProdutionResp.Builder builder = CollectTowerProdutionResp.newBuilder();
		builder.setProductionTime(productTime);
		for(ItemInfo item : award.getAwardItems()){
			builder.addRewards(item.toRewardItem());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_COLLECT_RESP,builder));
		this.syncGhostTowerInfo();
	}
	
	
	
	/**
	 * 累积作用号
	 * @param effectMap
	 * @param type
	 * @param value
	 */
	public void effextPlus(Map<EffType, Integer> effectMap,EffType type,int value){
		if(effectMap.containsKey(type)){
			value += effectMap.get(type);
		}
		effectMap.put(type, value);
	}
	
	/**
	 * 删除幽灵
	 * @param pointId
	 */
	public void deleteGhostPoint(WorldPoint point,boolean isMail) {
		if(!point.getOwnerId().equals(this.player.getId())){
			return;
		}
		WorldTask task = new WorldTask(GsConst.WorldTaskType.GHOST_TOWER_MONSTER_DEL) {
			@Override
			public boolean onInvoke() {
				int ghostId = point.getMonsterId();
				int pid = point.getId();
				WorldPoint delPoint = WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
				if(delPoint!= null && isMail){
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(player.getId())
							.setMailId(MailId.GHOST_TOWER_MONSTER_ATTACK_FAILED_CHANGED)
							.addContents(ghostId)
							.build());
					logger.info("deleteGhostPoint,email send,playerId:{},pointId:{}", 
							player.getId(),pid);
				}
				return true;
			}
		};
		long worldThreadId = WorldThreadScheduler.getInstance().getThreadState().getIntValue("threadId");
		long currThreadId = HawkOSOperator.getThreadId();
		if (currThreadId != worldThreadId) {
			WorldThreadScheduler.getInstance().postWorldTask(task);
		} else {
			task.onInvoke();
		}
	}
	

	
	
	/**
	 * 获取幽灵点
	 * @param pid
	 * @return
	 */
	public WorldPoint getGhost(int pid){
		WorldPoint point =  WorldPointService.getInstance().getWorldPoint(pid);
		return point;
	}
	
	
	/**
	 * 通知幽灵被击杀
	 */
	public void ghostKilled(int ghostId) {
		GhostTowerCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(GhostTowerCfg.class, ghostId);
		if(cfg == null){
			return;
		}
		//初始化生产时间
		if(this.player.getPlayerGhostTowerProductTime() == 0){
			long curTime = HawkTime.getMillisecond();
			this.player.setPlayerGhostTowerProductTime(curTime);
			logger.error("ghostKilled product time init, playerId: {},productTime:{}", 
					player.getId(),curTime);
		}
		AwardItems awardItems = AwardItems.valueOf(cfg.getKillReward());
		this.player.setPlayerGhostTowerStage(ghostId);
		//发送奖励邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.GHOST_TOWER_CHALLENGE_REWARD)
				.addContents(ghostId)
				.setRewards(awardItems.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		//挑战成功同步客户端
		AttackGhostSucessResp.Builder builder = AttackGhostSucessResp.newBuilder();
		builder.setStageId(ghostId);
		for(ItemInfo info : awardItems.getAwardItems()){
			builder.addRewards(info.toRewardItem());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_ATTACK_SUCESS_RESP_VALUE,builder));
		
		//刷新剧情主线
		MissionManager.getInstance().postMsg(this.player, new EventGhostTowerPass(cfg.getLevel(),cfg.getFloor()));
		//刷新建筑
		this.updateGhostTowerBuilding(cfg);
		//塔信息客户端
		this.syncGhostTowerInfo();
		LogUtil.logGhostAwardSend(player, ghostId, cfg.getKillReward());
		logger.error("ghostKilled stage up, playerId: {},stage:{}", 
				player.getId(),player.getPlayerGhostTowerStage());
	}
	
	public void updateGhostTowerBuilding(GhostTowerCfg cfg){
		int buidingId = cfg.getBuildcfgId();
		List<BuildingBaseEntity> buildingEntitys = player.getData().getBuildingListByType(BuildingType.GHOST_TOWER);
		if(buildingEntitys == null || buildingEntitys.size() <= 0){
			return;
		}
		BuildingBaseEntity buildingEntity = buildingEntitys.get(0);
		BuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buidingId);
		if(currCfg == null){
			return;
		}
		if(buildingEntity.getBuildingCfgId() == buidingId){
			return;
		}
		buildingEntity.setBuildingCfgId(buidingId);
		BuildingService.getInstance().pushBuildingRefresh(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_UPDATE_PUSH_VALUE);
		// 建筑升级操作打点
		LogUtil.logBuildLvUpOperation(player, buildingEntity, currCfg.getLevel(), true);
	}
	
	/**
	 * 生成幽灵
	 */
	public WorldPoint genGhost(String playerId,int pointId,GhostTowerCfg ghostTowerCfg) {
		//生成新的点
		List<Point> areaPoints = WorldPointService.getInstance().
				getRhoAroundPointsFree(pointId, ghostTowerCfg.getAreaRadiusMax());
		Collections.shuffle(areaPoints);
		WorldPoint newPoint = null;
		int[] pos = GameUtil.splitXAndY(pointId);
		int playerX = pos[0];
		int playerY = pos[1];
		for (Point point : areaPoints) {
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			if (!point.canYuriSeat()) {
				continue;
			}
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.YURI_POINT_RADIUS)) {
				continue;
			}
//			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
//				continue;
//			}
			if(Math.abs(playerX -point.getX()) < ghostTowerCfg.getAreaRadiusMin()){
				continue;
			}
			if(Math.abs(playerY -point.getY()) < ghostTowerCfg.getAreaRadiusMin()){
				continue;
			}
			newPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), 
					point.getZoneId(), WorldPointType.GHOST_TOWER_MONSTER_VALUE);
			newPoint.setMonsterId(ghostTowerCfg.getId());
			newPoint.setOwnerId(playerId);
			newPoint.setLifeStartTime(HawkTime.getMillisecond());
			newPoint.setPersistable(false);
			GhostInfo ghostInfo = new GhostInfo();
			ghostInfo.setTrapInfo(ghostTowerCfg.getRandTrapInfo());
			ghostInfo.setSoliderInfo(ghostTowerCfg.getRandSoldierInfo());
			ghostInfo.setHeroIds(ghostTowerCfg.getHeroIds());
			ghostInfo.setSuperSoldierId(ghostTowerCfg.getSuperSoldierId());
			ghostInfo.setSuperSoldierStar(ghostTowerCfg.getSuperSoldierLevel());
			ghostInfo.setArmours(ghostTowerCfg.getArmourIds());
			ghostInfo.setEffectmap(ghostTowerCfg.getEffectmap());
			ghostInfo.setChallenger(playerId);
			ghostInfo.init();
			newPoint.setGhostInfo(ghostInfo);
			WorldPointService.getInstance().addPoint(newPoint);
			break;
		}
		if(newPoint != null){
			player.dealMsg(MsgId.GHOST_TOWER_GEN_MONSTER, 
					new GhostTowerMonsterCreateInvoker(player, newPoint.getId(), newPoint.getX(), newPoint.getY()));
		}
		return newPoint;
	}
	
	/**
	 * 幽灵工厂是否被建造
	 * @return
	 */
	public boolean ghostTowerIsBuild(){
		List<BuildingBaseEntity> list = this.player.getData().
				getBuildingListByType(BuildingType.GHOST_TOWER);
		if(list.size() > 0){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 同步塔信息
	 */
	public void syncGhostTowerInfo(){
		GhostTowerInfoResp.Builder builder = GhostTowerInfoResp.newBuilder();
		PBGhostTower.Builder tbuilder = PBGhostTower.newBuilder();
		tbuilder.setStageId(player.getPlayerGhostTowerStage());
		tbuilder.setProductTime(player.getPlayerGhostTowerProductTime());
		builder.setTowerInfo(tbuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_INFO_RESP_VALUE,builder));
		
	}
	

}
