package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PlanetCollectEvent;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreActivity;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.Mail.CollectResource;
import com.hawk.game.protocol.Mail.CollectType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.PBTreaCollRec;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResTreasurePointService;
import com.hawk.log.Action;

/**
 * 采集资源宝库
 * 
 * @author zhenyu.shang
 * @since 2017年8月25日
 */
public class CollectResTreasureMarch extends PlayerMarch implements BasedMarch {

	public CollectResTreasureMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.COLLECT_RES_TREASURE;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 点为空 || 非宝库点
		if (point == null || point.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			MailId mailId = MailId.COLL_RES_TREASURE_FAIL;
			if (!HawkOSOperator.isEmptyString(march.getExtraInfo())) {
				mailId = MailId.COLL_PLANET_TREASURE_FAIL;
			}
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(mailId)
					.build());
			return;
		}

		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, point.getResourceId());
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		//非基础资源的锅
		if (resTreCfg.getId() == cfg.getRefreshTargetId()) {
			collectItemRes(player, march, point);
			return;
		}
				
		HawkTuple3<Boolean, List<ItemInfo>, Integer> tup3 = getAllResAndIsFirstAndMaxColl(player, point);
		boolean isFirstColl = tup3.first;
		List<ItemInfo> rsAll = tup3.second;
		/** 资源点上限 */
		final int maxCollect = tup3.third;
		List<ItemInfo> firstAwards = new ArrayList<>();
		if (isFirstColl) { // 首次采集奖励
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, resTreCfg.getFirstAward());
			AwardItems awardItems = awardCfg.getRandomAward();
			firstAwards.addAll(awardItems.getAwardItems());
		}
		/** 剩余负重 */
		int leftWeight = WorldUtil.calcTotalWeight(player, march.getArmys(), march.getEffectParams());
		leftWeight = maxCollect <= 0 ? 0 : Math.min(leftWeight, maxCollect);
		final int initCollWeight = leftWeight;
		int cityLv = player.getCityLevel();
		int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
		Set<Integer> resTypeSet = new HashSet<>(); // 可采集类型
		for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
			if (cityLv >= RES_LV[i]) {
				int resType = GsConst.RES_TYPE[i];
				resTypeSet.add(resType);
			}
		}

		Map<PlayerAttr, ItemInfo> recordMap = new HashMap<>();
		int avg = leftWeight / resTypeSet.size(); // 平均负重
		for (ItemInfo res : rsAll) {
			PlayerAttr restype = PlayerAttr.valueOf(res.getItemId());
			if (!resTypeSet.contains(restype.getNumber())) {
				continue;
			}
			int resWeightByType = WorldMarchConstProperty.getInstance().getResWeightByType(restype.getNumber());
			int k = avg / resWeightByType; // 采k个
			long get = Math.min(k, res.getCount());
			if (get > 0) {
				res.setCount(res.getCount() - get);
				recordMap.put(restype, new ItemInfo(res.getType(), res.getItemId(), get));
				leftWeight -= resWeightByType * get;
			}
		}

		for (;;) {
			boolean over = true;
			for (ItemInfo res : rsAll) {
				if (res.getCount() > 0) {
					PlayerAttr restype = PlayerAttr.valueOf(res.getItemId());
					int weight = WorldMarchConstProperty.getInstance().getResWeightByType(restype.getNumber());
					if (leftWeight >= weight) {
						res.setCount(res.getCount() - 1);
						ItemInfo collect = recordMap.getOrDefault(restype, new ItemInfo(res.getType(), res.getItemId(), 0));
						collect.setCount(collect.getCount() + 1);
						leftWeight -= weight;
						over = false;
					}
				}
			}
			if (over) {
				break;
			}
		}

		// 返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		if(firstAwards.isEmpty() && recordMap.isEmpty()){
			return;
		}
		
		// 邮件get
		boolean hasReachMax = initCollWeight - leftWeight >= maxCollect;
		List<ItemInfo> award = new ArrayList<>(recordMap.values());
		CollectMail.Builder builder = createCollectMail(firstAwards, award, hasReachMax, point.getGuildId(),point.getResourceId());
		CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.COLL_RES_TREASURE_OK).addContents(builder).build());

		{// 奖励直接发掉
			AwardItems toSend = AwardItems.valueOf();
			toSend.addItemInfos(firstAwards);
			toSend.addItemInfos(award);
			toSend.rewardTakeAffectAndPush(player, Action.WORLD_PULL_TREASURE);
			
			String awardStr = ItemInfo.toString(toSend.getAwardItems());
			WorldMarchService.logger.info("Res treasure march collect playerId={} arwards={}",player.getId(),awardStr);
		}

		{ // 采集记录
			PBTreaCollRec.Builder record = PBTreaCollRec.newBuilder();
			record.setPlayerId(player.getId());
			record.setAward(ItemInfo.toString(award));
			record.setPlayerName(player.getName());
			record.setIcon(player.getIcon());
			record.setPfIcon(player.getPfIcon());
			if (player.hasGuild()) {
				record.setGuildTag(player.getGuildTag());
			}
			point.addResTreaColRecord(record.build());
		}

		long leftResCount = rsAll.stream().mapToLong(ItemInfo::getCount).sum();
		if (leftResCount <= 100) { // 没资源了, 删除
			WorldResTreasurePointService.getInstance().deletePoint(point);
		}
	}
	
	/**
	 * 星能探索矿点采集
	 * @param player
	 * @param march
	 * @param point
	 */
	private void collectItemRes(Player player, WorldMarch march, WorldPoint point) {
		HawkTuple3<Boolean, List<ItemInfo>, Integer> tup3 = getAllResAndIsFirstAndMaxColl(player, point);
		List<ItemInfo> rsAll = tup3.second;
		/** 资源点上限 */
		final int maxCollect = tup3.third;
		List<ItemInfo> firstAwards = new ArrayList<>();
		
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		/** 剩余负重 */
		int leftWeight = (int) ItemInfo.valueOf(cfg.getGatherOnce()).getCount();
		leftWeight = maxCollect <= 0 ? 0 : Math.min(leftWeight, maxCollect);
		final int initCollWeight = leftWeight;
		
		Map<Integer, ItemInfo> recordMap = new HashMap<>();
		int avg = leftWeight / rsAll.size(); // 平均负重
		for (ItemInfo res : rsAll) {
			int restype = res.getItemId(); 
			int resWeightByType = WorldMarchConstProperty.getInstance().getResWeightByType(restype); //TODO
			int k = avg / resWeightByType; // 采k个
			long get = Math.min(k, res.getCount());
			if (get > 0) {
				res.setCount(res.getCount() - get);
				recordMap.put(restype, new ItemInfo(res.getType(), res.getItemId(), get));
				leftWeight -= resWeightByType * get;
			}
		}

		// 返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		if(firstAwards.isEmpty() && recordMap.isEmpty()){
			return;
		}
		
		// 邮件get
		boolean hasReachMax = initCollWeight - leftWeight >= maxCollect;
		List<ItemInfo> award = new ArrayList<>(recordMap.values());
		CollectMail.Builder builder = createCollectMail(firstAwards, award, hasReachMax, point.getGuildId(),point.getResourceId());
		CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.COLL_RES_TREASURE_OK).addContents(builder).build());

		{// 奖励直接发掉
			AwardItems toSend = AwardItems.valueOf();
			toSend.addItemInfos(firstAwards);
			toSend.addItemInfos(award);
			toSend.rewardTakeAffectAndPush(player, Action.WORLD_PULL_TREASURE);
			String awardStr = ItemInfo.toString(toSend.getAwardItems());
			WorldMarchService.logger.info("planetExplore Res treasure march collect playerId={} arwards={}",player.getId(),awardStr);
		}

		{ // 采集记录
			PBTreaCollRec.Builder record = PBTreaCollRec.newBuilder();
			record.setPlayerId(player.getId());
			record.setAward(ItemInfo.toString(award));
			record.setPlayerName(player.getName());
			record.setIcon(player.getIcon());
			record.setPfIcon(player.getPfIcon());
			if (player.hasGuild()) {
				record.setGuildTag(player.getGuildTag());
			}
			point.addResTreaColRecord(record.build());
			ActivityManager.getInstance().postEvent(new PlanetCollectEvent(player.getId(), HawkTime.getMillisecond(), point.getX(), point.getY(), (int)award.get(0).getCount()));
		}

		long leftResCount = rsAll.stream().mapToLong(ItemInfo::getCount).sum();
		if (leftResCount <= 5) { // 没资源了, 删除
			int posX = point.getX(), posY = point.getY();
			WorldResTreasurePointService.getInstance().deletePoint(point);
			
			//考虑到跨服过来的玩家也可能走到此处，不抛事件了
			//ActivityManager.getInstance().postEvent(new PlanetPointDispearEvent(player.getId(), posX, posY));
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.PLANET_EXPLORE_347_VALUE);
			if (opActivity.isPresent()) {
				PlanetExploreActivity activity = (PlanetExploreActivity)opActivity.get();
				activity.removePoint(player.getId(), posX, posY);
			}
		}
	}

	public HawkTuple3<Boolean, List<ItemInfo>, Integer> getAllResAndIsFirstAndMaxColl(Player player, WorldPoint point) {
		// 野怪配置
		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, point.getResourceId());
		if (resTreCfg == null) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			return HawkTuples.tuple(false, Collections.emptyList(), 0);
		}
		boolean isFirstColl = true;

		int collCount = 0;
		WorldPointPB pointPB = point.toBuilder(WorldPointPB.newBuilder(), "").build();
		List<PBTreaCollRec> caijiJilu = pointPB.getTreaCollrecList();
		// 采集总数
		Map<Integer, Long> countMap = Maps.newHashMapWithExpectedSize(4);
		for (PBTreaCollRec record : caijiJilu) {
			if (Objects.equal(record.getPlayerId(), player.getId())) {
				isFirstColl = false;
			}
			List<ItemInfo> rs = ItemInfo.valueListOf(record.getAward());
			for (ItemInfo item : rs) {
				countMap.merge(item.getItemId(), item.getCount(), (v1, v2) -> v1 + v2);
				if (Objects.equal(record.getPlayerId(), player.getId())) {
					collCount += item.getCount() * WorldMarchConstProperty.getInstance().getResWeightByType(item.getItemId()); //TODO 这里默认是1
				}
			}
		}

		List<ItemInfo> rsAll = ItemInfo.valueListOf(resTreCfg.getTotalRes());
		for (ItemInfo res : rsAll) { // 剩余资源量
			long count = res.getCount() - countMap.getOrDefault(res.getItemId(), 0L);
			res.setCount(count > 0 ? count : 0);
		}
		return HawkTuples.tuple(isFirstColl, rsAll, resTreCfg.getWeight() - collCount);
	}

	public CollectMail.Builder createCollectMail(List<ItemInfo> firstAwards, List<ItemInfo> award, boolean isLimit,String guildId, int resTreasureId) {
		CollectMail.Builder builder = CollectMail.newBuilder();
		WorldMarch march = getMarchEntity();
		builder.setMailId(MailId.COLL_RES_TREASURE_OK_VALUE);
		int[] pos = GameUtil.splitXAndY(march.getOrigionId());
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setType(CollectType.TRRES_TRESURE);
		builder.setResTreasureId(resTreasureId);
		for (ItemInfo item : firstAwards) {
			CollectResource.Builder cBuilder = CollectResource.newBuilder();
			cBuilder.setResCount((int) item.getCount());
			cBuilder.setResId(item.getItemId());
			cBuilder.setResType(item.getType());
			builder.addFirstRes(cBuilder.build());
		}

		for (ItemInfo item : award) {
			CollectResource.Builder cBuilder = CollectResource.newBuilder();
			cBuilder.setResCount((int) item.getCount());
			cBuilder.setResId(item.getItemId());
			cBuilder.setResType(item.getType());
			// // 如果是资源点, 传入资源点Id
			// if (march.getMarchType() == WorldMarchType.COLLECT_RES_TREASURE_VALUE) {
			// cBuilder.setWorldResId(Integer.parseInt(march.getTargetId()));
			// }
			builder.addRes(cBuilder.build());
		}
		if (StringUtils.isNotEmpty(guildId)) {
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			if(StringUtils.isNotEmpty(guildTag)){
				builder.setGuildTag(guildTag);
			}
		}

		// 采集成功
		builder.setIsLimit(isLimit);

		return builder;
	}

	// 如果要到家发的话就在这发
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// WorldMarch march = getMarchEntity();
		// String awardStr = march.getAwardStr();
		// if (!HawkOSOperator.isEmptyString(awardStr)) {
		// AwardItems award = AwardItems.valueOf(awardStr);
		// award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN);
		// }
		//
		// // 进攻部队返回推送
		// String enemyName = GlobalData.getInstance().getPlayerNameById(march.getTargetId());
		// PushService.getInstance().pushMsg(player.getId(), PushMsgType.ACTTACK_ARMY_RETURN_VALUE, enemyName);
		//
		// // 行为日志
		// BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_ATTACK_PLAYER_RETURN,
		// Params.valueOf("march", march), Params.valueOf("awardStr", awardStr));
		return true;
	}

	@Override
	public void onMarchStart() {
	}

	@Override
	public void onMarchReturn() {
	}

	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		// 采集召回
		WorldMarchService.getInstance().onResourceMarchCallBack(this, callbackTime, getMarchEntity().getArmys(), 0);
	}

}
