package com.hawk.game.module.autologic.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.hawk.game.config.ConstProperty;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.type.impl.radiationWarTwo.cfg.RadiationWarTwoActivityKVCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.autologic.cfg.AutoMassJoinCfg;
import com.hawk.game.module.autologic.service.GuildAutoMarchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

public enum PlayerAutoMarchEnum {
	//幽灵基地集结
	FOGGY_MASS_JOIN(1) {
		@Override
		public boolean checkWorldMarch(IWorldMarch march) {
			if(march.getMarchType()!= WorldMarchType.FOGGY_FORTRESS_MASS){
				return false;
			}
			if(march.getMarchStatus()!= WorldMarchStatus.MARCH_STATUS_WAITING_VALUE){
				return false;
			}
			AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
			long curTime = HawkTime.getMillisecond();
			if(curTime < march.getMassReadyTime() + cfg.getMassTimeLimit() * 1000){
				return false;
			}
			return true;
		}
		
		@Override
		public boolean checkJoinMarch(IWorldMarch march, Player player) {
			boolean marchCheck = this.checkWorldMarch(march);
			if(!marchCheck){
				return false;
			}
			return this.checkJoinMarchStart(march, player, WorldMarchType.FOGGY_FORTRESS_MASS_JOIN_VALUE);
		}
		
	},
	//BOSS怪-往日危机
	BOSS_MONSTER_OLD(2) {
		@Override
		public boolean checkWorldMarch(IWorldMarch march) {
			if(march.getMarchType()!= WorldMarchType.MONSTER_MASS){
				return false;
			}
			if(march.getMarchStatus()!= WorldMarchStatus.MARCH_STATUS_WAITING_VALUE){
				return false;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
			if (worldPoint == null) {
				return false;
			}
			if(worldPoint.getPointType() !=WorldPointType.MONSTER_VALUE){
				return false;
			}
			AutoMassJoinCfg autoMassJoinCfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
			long curTime = HawkTime.getMillisecond();
			if(curTime < march.getMassReadyTime() + autoMassJoinCfg.getMassTimeLimit() * 1000){
				return false;
			}
			RadiationWarTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RadiationWarTwoActivityKVCfg.class);
			List<Integer> list = cfg.getOldBossItemList();
			for(int itemId: list){
				// 道具
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
				if(Objects.nonNull(itemCfg) && itemCfg.getWorldEnemy() == worldPoint.getMonsterId()){
					return true;
				}
			}
			String bossStr = ConstProperty.getInstance().getBossEnemyIdList184();
			List<Integer> boss184 = SerializeHelper.cfgStr2List(bossStr);
			if (boss184.contains(worldPoint.getMonsterId())) {
				return true;
			}
			return false;
		}

		@Override
		public boolean checkJoinMarch(IWorldMarch march, Player player) {
			boolean marchCheck = this.checkWorldMarch(march);
			if(!marchCheck){
				return false;
			}
			return this.checkJoinMarchStart(march, player, WorldMarchType.MONSTER_MASS_JOIN_VALUE);
		}
	},
	//BOSS怪-当前危机
	BOSS_MONSTER_NEW(3) {
		@Override
		public boolean checkWorldMarch(IWorldMarch march) {
			if(march.getMarchType()!= WorldMarchType.MONSTER_MASS){
				return false;
			}
			if(march.getMarchStatus()!= WorldMarchStatus.MARCH_STATUS_WAITING_VALUE){
				return false;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
			if (worldPoint == null) {
				return false;
			}
			if(worldPoint.getPointType() !=WorldPointType.MONSTER_VALUE){
				return false;
			}
			AutoMassJoinCfg autoMassJoinCfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
			long curTime = HawkTime.getMillisecond();
			if(curTime < march.getMassReadyTime() + autoMassJoinCfg.getMassTimeLimit() * 1000){
				return false;
			}
			RadiationWarTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RadiationWarTwoActivityKVCfg.class);
			List<Integer> list = cfg.getNewBossItemList();
			for(int itemId: list){
				// 道具
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
				if(Objects.nonNull(itemCfg) && itemCfg.getWorldEnemy() == worldPoint.getMonsterId()){
					return true;
				}
			}
			String bossStr = ConstProperty.getInstance().getBossEnemyIdList184();
			List<Integer> boss184 = SerializeHelper.cfgStr2List(bossStr);
			if (boss184.contains(worldPoint.getMonsterId())) {
				return true;
			}
			return false;
		}

		@Override
		public boolean checkJoinMarch(IWorldMarch march, Player player) {
			boolean marchCheck = this.checkWorldMarch(march);
			if(!marchCheck){
				return false;
			}
			return this.checkJoinMarchStart(march, player, WorldMarchType.MONSTER_MASS_JOIN_VALUE);
		}

	};

	int val;
	private PlayerAutoMarchEnum(int val) {
		this.val = val;

	}

	public int getVal() {
		return val;
	}


	public abstract boolean checkWorldMarch(IWorldMarch march);
	public abstract boolean checkJoinMarch(IWorldMarch march,Player player);



	/**
	 * 取兵和英雄
	 * @param player
	 * @param armyList
	 * @param heroIdList
	 * @return
	 */
	public boolean checkAutoJoinMassArmy(Player player, List<ArmyInfo> armyList, List<Integer> heroIdList,int soldierCntLimit) {
		PlayerAutoMarchParam param = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		int free = 0;
		long totalCnt = 0;
		List<ArmyEntity> list = player.getData().getArmyEntities();
        List<ArmyInfo> freeList = new ArrayList<>();
		for (ArmyEntity entity : list) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			// 箭塔不参与结算
			if(cfg.getType() == SoldierType.BARTIZAN_100_VALUE){
				continue;
			}
			if(cfg.getType() == SoldierType.WEAPON_LANDMINE_101_VALUE){
				continue;
			}
			if(cfg.getType() == SoldierType.WEAPON_ACKACK_102_VALUE){
				continue;
			}
			if(cfg.getType() == SoldierType.WEAPON_ANTI_TANK_103_VALUE){
				continue;
			}

			totalCnt += entity.getFree();
			totalCnt += entity.getMarch();
			free += entity.getFree();
			if(entity.getFree() > 0){
				freeList.add(new ArmyInfo(entity.getArmyId(), entity.getFree()));
			}
		}
		//可托管士兵
		long autoSoldierCnt =  totalCnt * param.getMarchSoldierPer() / 100 ;
		//已经在自动集结行军中使用的士兵数量
		int autoMarchSoldierCnt = 0;
		int massJoinCnt = 0;
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
        for (IWorldMarch march : marchs) {
            int idx = march.getMarchEntity().getAutoMassJoinIdentify();
            if (idx <= 0) {
            	continue;
            }
            massJoinCnt ++;
            for (ArmyInfo army : march.getArmys()) {
            	autoMarchSoldierCnt += army.getTotalCount();
    		}

        }
		if(autoMarchSoldierCnt >= autoSoldierCnt){
			return false;
		}
		if(massJoinCnt >= param.getMarchCount()){
			return false;
		}
		//玩家队列可带兵数量
		int maxMarchSoldierNum = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		//可使用的托管数量
		int canMarchSoldierCnt = (int) (autoSoldierCnt - autoMarchSoldierCnt);
		canMarchSoldierCnt = Math.min(canMarchSoldierCnt, free);
		//剩余自动集结队列数量
		int canJoinCnt = param.getMarchCount() - massJoinCnt;
		//最少出一个兵   不能超过 队列带兵上限
		int useSoldierCnt =  canMarchSoldierCnt / canJoinCnt;
		useSoldierCnt = Math.max(1, useSoldierCnt);
		useSoldierCnt = Math.min(useSoldierCnt, maxMarchSoldierNum);
		useSoldierCnt = Math.min(useSoldierCnt, soldierCntLimit);

		AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
        //点兵
		int marchSoldierCnt = useSoldierCnt;
		Collections.sort(freeList);

		Map<Integer,ArmyInfo> armyMap = new HashMap<>();
		//最多循环3次  每个兵种都要添加总数的40% 所以最多循环3次即可
		for(int time=1; time<=3;time++){
			for(int i=freeList.size()-1;i >= 0; i--){
				ArmyInfo armyEntity = freeList.get(i);
				if(marchSoldierCnt <= 0){
					break;
				}
				int freeCnt = armyEntity.getTotalCount();
				if(freeCnt <= 0){
					continue;
				}
				//需要出征数量
				long outCnt = useSoldierCnt * 40 / 100;
				outCnt = Math.max(1, outCnt);
				outCnt = Math.min(outCnt, freeCnt);
				outCnt = Math.min(outCnt, marchSoldierCnt);
				marchSoldierCnt -= outCnt;
				armyEntity.setTotalCount((int) (freeCnt - outCnt));
				//组建出征兵
				ArmyInfo armyInfo = armyMap.get(armyEntity.getArmyId());
				if(Objects.isNull(armyInfo)){
					armyInfo = new ArmyInfo(armyEntity.getArmyId(),(int)outCnt);
					armyMap.put(armyEntity.getArmyId(), armyInfo);
				}else{
					int curCnt = armyInfo.getTotalCount() + (int)outCnt;
					armyInfo.setTotalCount(curCnt);
				}
			}
		}
		if(armyMap.size() > 0){
			armyList.addAll(armyMap.values());
		}
		//点将
		List<Integer> hid = new ArrayList<>();
		List<HeroEntity> heros = player.getData().getHeroEntityList();
		for(HeroEntity heroEntity : heros){
			if(heroEntity.getHeroObj().getConfig().getQualityColor() == cfg.getHeroQualityColor() &&
					heroEntity.getHeroObj().getState() == PBHeroState.HERO_STATE_FREE){
				hid.add(heroEntity.getHeroId());
			}
		}
		Collections.shuffle(hid);
		if(hid.size() > 0){
			heroIdList.add(hid.remove(0));
		}
		if(hid.size() > 0){
			heroIdList.add(hid.remove(0));
		}
		return true;
	}

	/**
	 * 加入
	 * @param march
	 * @param player
	 * @param marchType
	 * @return
	 */
	public boolean checkJoinMarchStart(IWorldMarch march, Player player,int marchType) {
		AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
		if (!player.hasGuild()) {
			return false;
		}
		Player leader = march.getPlayer();
		if(Objects.isNull(leader)){
			return false;
		}
		if (!march.isMassMarch()) {
			return false;
		}
		// 检查是否同盟
		if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), leader.getId())) {
			return false;
		}
		//不加入 自己的集结
		if(player.getId().equals(march.getPlayerId())){
			return false;
		}
		//检查是否超员
		Set<IWorldMarch> massMarchList = WorldMarchService.getInstance().getMassJoinMarchs(march, false);
		int count = massMarchList != null ? massMarchList.size() : 0;
		if (count >= (leader.getMaxMassJoinMarchNum(march) + march.getMarchEntity().getBuyItemTimes())) {
			return false;
		}
		// 检查集结士兵数是否超上限
		int maxMassSoldierNum = march.getMaxMassJoinSoldierNum(leader, player);
		// 队长现在带的兵力人口
		int curPopulationCnt = WorldUtil.calcSoldierCnt(march.getMarchEntity().getArmys());
		if (massMarchList != null && massMarchList.size() > 0) {
			for (IWorldMarch joinMarch : massMarchList) {
				//已经有行军在加入集结
				if (joinMarch.getPlayerId().equals(player.getId())) {
					return false;
				}
				if (!joinMarch.getPlayerId().equals(march.getPlayerId())) {
					curPopulationCnt += WorldUtil.calcSoldierCnt(joinMarch.getMarchEntity().getArmys());
				}
			}
		}
		//集结剩余数量
		int massSoldierCnt = maxMassSoldierNum - curPopulationCnt;
		// 士兵人口数已达上限
		if (massSoldierCnt <= 0) {
			return false;
		}
		MassMarch massMarch = (MassMarch) march;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			return false;
		}
		ConsumeItems items = null;
		// 判断体力够不够
		if (marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE ) {
			items = ConsumeItems.valueOf(PlayerAttr.VIT, massMarch.getMarchEntity().getVitCost());
			if (!items.checkConsume(player)) {
				return false;
			}
		}

		PlayerAutoMarchParam param = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		//是否已经超过队伍数量
		BlockingQueue<IWorldMarch> selfMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if(selfMarchs.size() >= player.getMaxMarchNum()){
			return false;
		}
		//自动集结的数量
		int autoCnt = 0;
        for (IWorldMarch selfMarch : selfMarchs) {
            int idx = selfMarch.getMarchEntity().getAutoMassJoinIdentify();
            if (idx > 0) {
            	autoCnt ++;
            }
        }
        //自动集结的数量不足
        if(autoCnt >= param.getMarchCount()){
        	return false;
        }
		int startX = march.getOrigionX();
		int startY = march.getOrigionY();
		int pos = player.getPlayerPos();
		int[] posXY = GameUtil.splitXAndY(pos);
		if(posXY[0] <= 0 && posXY[1] <= 0){
			return false;
		}
		double dis = WorldUtil.distance(startX, startY, posXY[0], posXY[1]);
		if(dis > cfg.getDistanceLimit()){
			return false;
		}
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(startX, startY);
		if (point == null) {
			return false;
		}
		if(!player.isActiveOnline()){
			GuildAutoMarchService.getInstance().addMissMail(player, 2, leader.getName(),
					String.valueOf(this.getVal()),String.valueOf(2));
			return false;
		}
		//点兵点将
		List<ArmyInfo> marchArmy = new ArrayList<>();
		List<Integer> marchHero = new ArrayList<>();
		this.checkAutoJoinMassArmy(player, marchArmy, marchHero,massSoldierCnt);
		//英雄是否可以出征
		if (!ArmyService.getInstance().heroCanMarch(player, marchHero)) {
			return false;
		}
		//如果没有士兵 添加一个虚拟兵
		if(marchArmy.isEmpty()){
			return false;
		}
		//正常出征
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(marchHero);
		effParams.setArmys(marchArmy);
		//临时行军对象
		IWorldMarch marchTemp = WorldMarchService.getInstance().genMarch(player, marchType, pos, point.getId(), march.getMarchId(), point, 0, null, false, effParams);

		if(marchTemp.getMarchNeedTime() > cfg.getMarchTimeLimit() * 1000){
			GuildAutoMarchService.getInstance().addMissMail(player, 1, leader.getName(),
					String.valueOf(this.getVal()),String.valueOf(1));
			return false;
		}
		//赶不上了
		if(marchTemp.getEndTime() >  march.getStartTime()){
			GuildAutoMarchService.getInstance().addMissMail(player,1, leader.getName(),
					String.valueOf(this.getVal()),String.valueOf(1));
			return false;
		}

		// 扣兵 和 英雄
		if (!marchArmy.isEmpty() && !ArmyService.getInstance().checkArmyAndMarch(player, marchArmy, marchHero, 0)) {
			return false;
		}

		// 出发
		IWorldMarch joinMarch = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), march.getMarchId(), null, 0, effParams);
		joinMarch.getMarchEntity().setAutoMassJoinIdentify(1);
		//增加体力消耗
		if (joinMarch != null && marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE) {
			joinMarch.getMarchEntity().setVitCost(massMarch.getMarchEntity().getVitCost());
			//扣体力消耗
			if(Objects.nonNull(items)){
				items.consumeAndPush(player, Action.FIGHT_MONSTER);
			}
		}
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_MASS_JOIN_AUTO,
				Params.valueOf("marchData", joinMarch));
		//日志
		int curOrder = GuildAutoMarchService.getInstance().getGuildAutoQueueOrder(player);
		String targetId = massMarch.getMarchEntity().getTargetId();
		if(Objects.isNull(targetId)){
			targetId = "";
		}
		LogUtil.logAutoMassJionStart(player, joinMarch.getMarchId(), joinMarchType, autoCnt, param.getMarchCount()-autoCnt ,
				curOrder, massMarch.getMarchId(), massMarch.getMarchType().getNumber(),targetId , (int)dis);
		return true;
	}




	public static PlayerAutoMarchEnum valueOf(int val){
		switch (val) {
		case 1:return FOGGY_MASS_JOIN;
		case 2:return BOSS_MONSTER_OLD;
		case 3:return BOSS_MONSTER_NEW;
		default: return null;
		}
	}
}
