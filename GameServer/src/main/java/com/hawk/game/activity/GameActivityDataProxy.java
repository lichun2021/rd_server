package com.hawk.game.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.config.*;
import com.hawk.game.entity.*;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.*;
import com.hawk.game.service.*;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Source;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.serializer.HawkSerializer;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Table;
import com.google.protobuf.ByteString;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.impl.HeavenBlessingActiveEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.extend.TiberiumSeasonTimeAbstract;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivity;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyRankScoreCfg;
import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyRankScoreCfg;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionKVCfg;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostActivity;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostKVCfg;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonTrapData;
import com.hawk.activity.type.impl.heavenBlessing.cfg.HeavenBlessingKVCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireRankCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireTargetCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeRankCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeTargetCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoRankCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoTargetCfg;
import com.hawk.activity.type.impl.inherit.BackPlayerInfo;
import com.hawk.activity.type.impl.inheritNew.BackNewPlayerInfo;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossKVCfg;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.activity.impl.inherit.InheritNewService;
import com.hawk.game.activity.impl.inherit.InheritService;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.heroTrial.HeroTrialCheckerFactory;
import com.hawk.game.heroTrial.mission.IHeroTrialChecker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.PlayerImmgrationModule;
import com.hawk.game.module.PlayerTravelShopModule;
import com.hawk.game.module.PlayerXQHXModule;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonBattleInfo;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonPlayerData;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.MachineAwakeState;
import com.hawk.game.protocol.Activity.PBDamageRank;
import com.hawk.game.protocol.Activity.PBEmptyModel10Info;
import com.hawk.game.protocol.Activity.PBEmptyModel8Info;
import com.hawk.game.protocol.Activity.PBEmptyModel9Info;
import com.hawk.game.protocol.Activity.PBSpreadBindRoleInfo;
import com.hawk.game.protocol.Activity.SpaceMachineGuardActivityInfoPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Cross.PBCrossPostActivityEvent;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.World.PBCakeShare;
import com.hawk.game.protocol.World.PBDragonBoat;
import com.hawk.game.protocol.World.PBTreaCollRec;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CWPlayerData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.TimerEventEnum;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResTreasurePointService;
import com.hawk.gamelib.player.PowerData;
import com.hawk.l5.L5Helper;
import com.hawk.l5.L5Task;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ActivityClickType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.Platform;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

/**
 * 游戏数据获取器
 * 
 * @author PhilChen
 *
 */
public class GameActivityDataProxy implements ActivityDataProxy {
	
	public GameActivityDataProxy() {
	}

	private Player getPlayer(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		return player;
	}
	
	private PlayerData getPlayerData(String playerId){
		return GlobalData.getInstance().getPlayerData(playerId, true);
	}
	
	@Override
	public boolean checkPlayerExist(String playerId) {
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		return accountInfo != null;
	}
	
	@Override
	public String getServerId() {
		return GsConfig.getInstance().getServerId();
	}
	
	@Override
	public int getServerType(){
		return GsConfig.getInstance().getServerType();
	}
	
	@Override
	public String getLocalIdentify() {
		return LocalRedis.getInstance().getLocalIdentify();
	}
	
	@Override
	public boolean isGsInitFinish() {
		return GsApp.getInstance().isInitOK();
	}

	@Override
	public int getCrossDayHour() {
		return TimerEventEnum.ZERO_CLOCK.getClock();
	}
	
	@Override
	public int getConstructionFactoryLevel(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getConstructionFactoryLevel();
	}

	@Override
	public int getConstructionFactoryCfgId(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getConstructionFactory().getBuildingCfgId();
	}

	@Override
	public long getPlayerCreateTime(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getPlayerEntity().getCreateTime();
	}
	
	@Override
	public long getPlayerCreateAM0Date(String playerId) {
		return HawkTime.getAM0Date(new Date(getPlayerCreateTime(playerId))).getTime();
	}

	@Override
	public Set<String> getOnlinePlayers() {
		return GlobalData.getInstance().getOnlinePlayerIds();
	}

	@Override
	public boolean isOnlinePlayer(String playerId) {
		return GlobalData.getInstance().isOnline(playerId);
	}

	@Override
	public boolean consumeItems(String playerId, List<RewardItem.Builder> itemList, int protocolType, Action action) {
		Player player = getPlayer(playerId);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		for (RewardItem.Builder rewardItem : itemList) {
			consumeItems.addConsumeInfo(new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(), (int) rewardItem.getItemCount()), false);
		}

		if (!consumeItems.checkConsume(player, protocolType)) {
			return false;
		}
		consumeItems.consumeAndPush(player, action);
		return true;
	}

	@Override
	public int getBuildMaxLevel(String playerId, int buildType) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		BuildingType type = BuildingType.valueOf(buildType);
		List<BuildingBaseEntity> buildList = playerData.getBuildingListByType(type);
		int maxLevel = 0;
		for (BuildingBaseEntity entity : buildList) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
			if (maxLevel == 0) {
				maxLevel = buildingCfg.getLevel();
				continue;
			}
			if (buildingCfg.getLevel() > maxLevel) {
				maxLevel = buildingCfg.getLevel();
			}
		}
		return maxLevel;
	}
	

	@Override
	public String getPlayerVersion(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return "";
		}
		String version = playerData.getPlayerEntity().getVersion();
		if(version != null && version.indexOf("_") > 0) {
			version = version.split("_")[1];
		}
		return version;
	}

	@Override
	public String getPlayerChannelId(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return "";
		}
		return playerData.getPlayerEntity().getChannelId();
	}
	
	@Override
	public String getPlayerChannel(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return "";
		}
		return playerData.getPlayerEntity().getChannel();
	}

	@Override
	public String getAreaId() {
		return GsConfig.getInstance().getAreaId();
	}

	@Override
	public int getLoginDays(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		StatisticsEntity statisticsEntity = playerData.getStatisticsEntity();
		return statisticsEntity.getLoginDay();
	}

	@Override
	public int getPlayerLevel(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getPlayerBaseEntity().getLevel();
	}

	@Override
	public int getVipLevel(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getPlayerEntity().getVipLevel();
	}
	
	@Override
	public int getBuildingNum(String playerId, int buildType) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		BuildingType type = BuildingType.valueOf(buildType);
		List<BuildingBaseEntity> buildings = playerData.getBuildingListByType(type);
		return buildings.size();
	}

	@Override
	public long getResourceOutputRate(String playerId, int resType) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getResourceOutputRate(resType);
	}

	@Override
	public int getSoldierHaveNum(String playerId, int armyId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return GameUtil.getSoldierHaveNum(playerData, armyId);
	}

	@Override
	public long getServerOpenDate() {
		return GameUtil.getServerOpenTime();
	}
	
	@Override
	public long getServerOpenAM0Date() {
		return HawkTime.getAM0Date(new Date(getServerOpenDate())).getTime();
	}

	@Override
	public void takeReward(String playerId, int rewardId, int num, Action action, boolean popup) {
		Player player = this.getPlayer(playerId);
		AwardService.getInstance().takeAward(player, rewardId, num, action, popup, RewardOrginType.ACTIVITY_REWARD_VALUE, rewardId);
	}

	public void takeReward(String playerId, int rewardId, int num, Action action,
					RewardOrginType rewardType, boolean isPop){
		Player player = this.getPlayer(playerId);
		AwardService.getInstance().takeAward(player, rewardId, num, action,
				isPop, rewardType.getNumber(), rewardId);
	}

	@Override
	public void takeRewardWithFixItem(String playerId, String fixItem, int rewardId, int num, Action action,
							   RewardOrginType rewardType, boolean isPop){
		Player player = this.getPlayer(playerId);
		AwardService.getInstance().takeRewardWithFixItem(player, fixItem, rewardId, num, action,
				isPop, rewardType.getNumber(), rewardId);
	}

	@Override
	public List<RewardItem.Builder> takeRewardReturnItemlist(String playerId, int rewardId, int num,
															 Action action, boolean popup) {
		Player player = this.getPlayer(playerId);
		AwardItems items = AwardService.getInstance().takeAward(player, rewardId, num, action, popup,
				RewardOrginType.ACTIVITY_REWARD_VALUE, rewardId);
		List<ItemInfo> list = items.getAwardItems();
		List<RewardItem.Builder> rewardbuilders = new LinkedList<>();
		for(ItemInfo itemInfo : list){
			RewardItem.Builder itemBuilder = RewardItem.newBuilder();
			itemBuilder.setItemType(itemInfo.getType());
			itemBuilder.setItemId(itemInfo.getItemId());
			itemBuilder.setItemCount(itemInfo.getCount());
			rewardbuilders.add(itemBuilder);
		}
		return rewardbuilders;
	}

	@Override
	public void takeReward(String playerId, int rewardId, int num, Action action,
			int mailid, String content, String activityName) {
		AwardItems awardItems = AwardService.getInstance().takeAward(rewardId, num);
		if(awardItems.getAwardItems() != null && !awardItems.getAwardItems().isEmpty()){
			MailId mailId = MailId.valueOf(mailid);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(mailId)
			        .addTitles(activityName)
			        .addSubTitles(activityName)
			        .addContents(activityName, content)
			        .setRewards(awardItems.getAwardItems())
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .build());
		}		
		
	}
	
	@Override
	public void takeReward(String playerId, int rewardId, int num, Action action, int mailid, Object[] title, Object[] subTitle, Object[] content) {
		AwardItems awardItems = AwardService.getInstance().takeAward(rewardId, num);
		if(awardItems.getAwardItems() != null && !awardItems.getAwardItems().isEmpty()){
			MailId mailId = MailId.valueOf(mailid);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(mailId)
			        .addTitles(title)
			        .addSubTitles(subTitle)
			        .addContents(content)
			        .setRewards(awardItems.getAwardItems())
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .build());
		}		
		
	}
	
	@Override
	public void takeReward(String playerId, int rewardId, int num, Action action,
			int mailid, String content, String activityName, boolean isGet, Map<Integer, Integer> map) {
		AwardItems awardItems = AwardService.getInstance().takeAward(rewardId, num);
		if(awardItems.getAwardItems() != null && !awardItems.getAwardItems().isEmpty()){
			MailId mailId = MailId.valueOf(mailid);
			MailRewardStatus status = isGet ? MailRewardStatus.GET : MailRewardStatus.NOT_GET;  
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(mailId)
			        .addTitles(activityName)
			        .addSubTitles(activityName)
			        .addContents(activityName, content)
			        .setRewards(awardItems.getAwardItems())
			        .setAwardStatus(status)
			        .build());
			if (map != null) {
				for (ItemInfo itemInfo : awardItems.getAwardItems()) {
					Integer oldNum = map.get(itemInfo.getItemId());
					if (oldNum == null) {
						map.put(itemInfo.getItemId(), (int)itemInfo.getCount());
					} else {
						map.put(itemInfo.getItemId(), (int)itemInfo.getCount() + oldNum);
					}
				}
			}
		}		
		
	}
	
	@Override
	public void takeReward(String playerId, List<RewardItem.Builder> itemList, int multi, Action action, boolean isPop, RewardOrginType... originType){
		Player player =  this.getPlayer(playerId);
		AwardItems awardItem = AwardItems.valueOf();
		for (RewardItem.Builder itemInfo : itemList) {
			awardItem.addItem(itemInfo.getItemType(), itemInfo.getItemId(), (int) itemInfo.getItemCount() * multi);
		}
		
		if(isPop){
			awardItem.rewardTakeAffectAndPush(player, action, true, originType.length > 0 ? originType[0] : RewardOrginType.ACTIVITY_REWARD);
		}else{
			awardItem.rewardTakeAffectAndPush(player, action);
		}
	}
	
	@Override
	public void takeReward(String playerId, List<com.hawk.game.protocol.Reward.RewardItem.Builder> itemList,
			Action action, boolean isPop) {		
		takeReward(playerId, itemList, 1, action, isPop, RewardOrginType.ACTIVITY_REWARD);
	}

	@Override
	public void takeRewardAuto(String playerId, RewardItem.Builder itemInfo, int multi, Action action, boolean isPop, RewardOrginType... originType) {
		Player player =  this.getPlayer(playerId);
		int itemId = itemInfo.getItemId();
		ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if(cfg.getItemType() != Const.ToolType.REWARD_VALUE){
			return;
		}
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, cfg.getRewardId());
		if(awardCfg == null){
			return;
		}
		AwardItems awardItem = awardCfg.getRandomAward();
		if(isPop){
			awardItem.rewardTakeAffectAndPush(player, action, true, originType.length > 0 ? originType[0] : RewardOrginType.ACTIVITY_REWARD);
		}else{
			awardItem.rewardTakeAffectAndPush(player, action);
		}
	}

	@Override
	public int getItemNum(String playerId, int itemId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		return playerData.getItemNumByItemId(itemId);
	}

	@Override
	public boolean cost(String playerId, List<com.hawk.game.protocol.Reward.RewardItem.Builder> itemList, int multi, 
			Action action, boolean isGold) {
		Player player = this.getPlayer(playerId);
		ConsumeItems consumteItems = ConsumeItems.valueOf();
		for(RewardItem.Builder rewardItem : itemList){
			consumteItems.addConsumeInfo(new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(), (int)(rewardItem.getItemCount() * multi)), false);
		}
		
		if(!consumteItems.checkConsume(player)){
			return false;
		}
		
		return consumteItems.consumeAndPush(player, action).hasAwardItem();
	}
		
	@Override
	public boolean cost(String playerId, List<com.hawk.game.protocol.Reward.RewardItem.Builder> itemList,
			Action action) {
		return cost(playerId, itemList, 1, action, false);
	}

	@Override
	public String getPlayerName(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null || playerData.getPlayerEntity() == null) {
			return "指挥官已移民";
		}
		return playerData.getPlayerEntity().getName();
	}
	
	public List<Integer> getPersonalProtectVals(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return Collections.emptyList();
		}
		return playerData.getPersonalProtectListVals();
	}

	@Override
	public String getGuildNameByByPlayerId(String playerId) {
		Player player = getPlayer(playerId);
		if (player == null) {
			return "";
		}
		return player.getGuildName();
	}
	
	@Override
	public String getGuildTagByPlayerId(String playerId) {
		Player player = getPlayer(playerId);
		if (player == null) {
			return "";
		}
		return player.getGuildTag();
	}
	
	@Override
	public String getGuildId(String playerId) {
		Player player = getPlayer(playerId);
		if (player != null) {
			return player.getGuildId();
		}
		
		return null;
	}

	
	@Override
	public long getJoinGuildTime(String playerId) {
		long joinTm = GuildService.getInstance().getJoinGuildTime(playerId);
		return joinTm;
	}

	@Override
	public Collection<String> getGuildMemberIds(String guildId) {
		return GuildService.getInstance().getGuildMembers(guildId);
	}
	
	

	@Override
	public List<String> getOnlineGuildMemberIds(String guildId) {
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		List<String> onlineMembers = new ArrayList<>();
		for (String memberId : memberIds) {
			if (GlobalData.getInstance().isOnline(memberId)) {
				onlineMembers.add(memberId);
			}
		}
		return onlineMembers;
	}

	public long getGuildNoArmyPower(String guildId) {
		return GuildService.getInstance().getGuildNoArmyPower(guildId);
	}
	
	@Override
	public int invitePlayerJoinGuild(String playerId, String targetPlayerId) {
		Player player = getPlayer(playerId);
		int result = GuildService.getInstance().invitePlayer(player, targetPlayerId);
		return result;
	}

	@Override
	public PowerData getPowerData(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return null;
		}
		return playerData.getPowerElectric().getPowerData();
	}

	@Override
	public String getGuildName(String guildId) {
		return GuildService.getInstance().getGuildName(guildId);
	}

	@Override
	public List<String> getGuildIds() {
		List<String> guildList = GuildService.getInstance().getGuildIds();
		return guildList;
	}


	@Override
	public String getGuildTag(String guildId) {
		return GuildService.getInstance().getGuildTag(guildId);
	}
	
	@Override
	public String getGuildLeaderName(String guildId) {
		return GuildService.getInstance().getGuildLeaderName(guildId);
	} 
	
	@Override
	public String getGuildLeaderId(String guildId) {
		return GuildService.getInstance().getGuildLeaderId(guildId);
	}
	
	@Override
	public int getGuildFlag(String guildId) {
		return GuildService.getInstance().getGuildFlag(guildId);
	}

	@Override
	public boolean isExistItemId(int id) {
		return ItemCfg.isExistItemId(id);
	}

	@Override
	public boolean isExistAwardId(int id) {
		return AwardCfg.isExistAwardId(id);
	}
	
	@Override
	public boolean sendProtocol(String playerId, HawkProtocol protocol) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			player.sendProtocol(protocol);
			return true;
		}
		return false;
	}

	@Override
	public boolean sendMail(String playerId ,MailId mailId, Object[] title, Object[] subTitle, Object[] content, List<RewardItem.Builder> aitems, boolean isGetReward) {
		List<ItemInfo> items = new ArrayList<>();
		if (aitems != null) {
			for (RewardItem.Builder activityItemInfo : aitems) {
				items.add(new ItemInfo(activityItemInfo.getItemType(), activityItemInfo.getItemId(), (int) activityItemInfo.getItemCount()));
			}
		}
		MailParames.Builder builder = MailParames.newBuilder().setPlayerId(playerId).setMailId(mailId).setRewards(items);
		if (title != null) {
			builder.addTitles(title);
		}
		
		if (subTitle != null) {
			builder.addSubTitles(subTitle);
		}

		if (content != null) {
			builder.addContents(content);
		}
		
		if (isGetReward) {
			builder.setAwardStatus(MailRewardStatus.GET);
		} else {
			builder.setAwardStatus(MailRewardStatus.NOT_GET);
		}
		SystemMailService.getInstance().sendMail(builder.build());
		return true;
	}

	@Override
	public boolean isGmClose(ActivityType activityType) {
		if (activityType == null) {
			return SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK);
		}
		
		return SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ACTIVITY, activityType.intValue());
	}

	@Override
	public boolean isTavernActivity(int achieveId) {
		return TavernService.getInstance().getAchieveCfg(achieveId) != null;
	}

	@Override
	public void recordActivityRewardClick(String playerId, ActivityBtns btn, ActivityType activityType, int cellItemId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("sendActivityRewardClickMsg failed, player null, playerId: {}", playerId);
			return;
		}
		
		LogUtil.logActivityClickFlow(player, ActivityClickType.REWARD_CLICK, 
				String.valueOf(btn.getNumber()), String.valueOf(activityType.intValue()), String.valueOf(cellItemId));
	}

	@Override
	public boolean hasAlreadyFirstRecharge(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return true;
		}
		return playerData.getPlayerBaseEntity().getSaveAmtTotal() > 0;
	}

	@Override
	public void addBuff(String playerId, int buffId, long endTime) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("activity add buff failed, player null, playerId: {}", playerId);
			return;
		}
		
		StatusDataEntity entity = player.addStatusBuff(buffId, endTime);
		if (entity != null) {
			player.getPush().syncPlayerStatusInfo(false, entity);
		}
	}

	@Override

	public Table<Integer, Integer, List<ActivityHellFireTargetCfg>> getHellFireTargetCfgTable() {
		return AssembleDataManager.getInstance().getHellFireTargetTable();
	}

	@Override
	public Map<Integer, List<ActivityHellFireRankCfg>> getHellFireRankMap() {
		return AssembleDataManager.getInstance().getHellFireRankMap();
	}

	@Override
	public PlayerData4Activity getPlayerData4Activity(String playerId) {
		PlayerData4Activity playerData4Activity = this.getPlayer(playerId).getData().getPlayerData4Activity();
		if (playerData4Activity == null) {
			playerData4Activity = new PlayerData4Activity();
			playerData4Activity.setPlayerId(playerId);
			this.getPlayer(playerId).getData().setPlayerData4Activity(playerData4Activity);
		}
		
		this.fillPlayerData4Activity(playerData4Activity);
		return playerData4Activity;
	}
	
	private void fillPlayerData4Activity(PlayerData4Activity playerData4Activity) {
		Player player = this.getPlayer(playerData4Activity.getPlayerId());
		PlayerData playerData = player.getData();
		if (playerData.getPowerElectric().getBuildBattlePoint() <= 0 || playerData.getPowerElectric().getPlayerBattlePoint() <= 0) {
			playerData.getPowerElectric().refreshPowerElectric(player, false, PowerChangeReason.OTHER);
		}
		playerData4Activity.setBuildingBattlePoint(playerData.getPowerElectric().getBuildBattlePoint());
		playerData4Activity.setTechBattlePoint(playerData.getPowerElectric().getTechBattlePoint());
		playerData4Activity.setPlantScienceBattlePoint(player.getPlantScience().getTechPower());
	}


	public void buyMonthCardRecord(String playerId, int cardId, boolean renew, long validEndTime) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("buy monthCard record failed, player null, playerId: {}", playerId);
			return;
		}
		
		LogUtil.logBuyMonthCardFlow(player, cardId, renew, validEndTime);
	}
	
	@Override
	public void buyFundRecord(String playerId, ActivityType type){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("buy fund record failed, player null, playerId: {}", playerId);
			return;
		}
		
		LogUtil.logBuyFundFlow(player, type);
	}
	
	@Override
	public void strongestLeaderScoreRecord(String playerId, ActivityRankType rankType, int stageId, long score) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("strongest leader score record failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logStrongestLeaderScoreFlow(player, rankType, stageId, score);
		
	}
	
	

	@Override
	public void strongestGuildScoreRecord(String playerId, int rankType, int termId, int stageId, long score) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("strongest guild score record failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logStrongestGuildPersonScoreFlow(player, rankType, termId, stageId, score);
		
	}

	@Override
	public void lotteryDrawRecord(String playerId, int type, int lucky, boolean isMulti, boolean causeMulti, String results, String multis) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("lottery draw record failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logLotteryDrawFlow(player, type, lucky, isMulti, causeMulti, results, multis);
		
	}

	@Override
	public Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> getHellTwoTargetCfgTable() {
		return AssembleDataManager.getInstance().getHellFireTwoTargetTable();
	}

	@Override
	public Map<Integer, List<ActivityHellFireTwoRankCfg>> getHellFireTwoRankMap() {
		return AssembleDataManager.getInstance().getHellFireTwoRankMap();
	}

	@Override
	public Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> getHellThreeTargetCfgTable() {
		return AssembleDataManager.getInstance().getHellFireThreeTargetTable();
	}

	@Override
	public Map<Integer, List<ActivityHellFireThreeRankCfg>> getHellFireThreeRankMap() {
		return AssembleDataManager.getInstance().getHellFireThreeRankMap();
	}
	
	@Override
	public void refreshPower(String playerId, PowerChangeReason reason) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("refresh monthCard power failed, player null, playerId: {}", playerId);
			return;
		}
		
		player.refreshPowerElectric(reason);
	}
	
	@Override
	public int getHeroNumByCondition(String playerId, int level, int quality, int star) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		List<PlayerHero> heros = playerData.getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.collect(Collectors.toList());
		long cnt = heros.stream()
			.filter(e -> e.getConfig().getQualityColor() >= quality && e.getLevel() >= level && e.getStar() >= star)
			.count();
		return (int) cnt;
	}
	
	@Override
	public int getSharedHeroNum(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		List<PlayerHero> heros = playerData.getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.collect(Collectors.toList());
		long cnt = heros.stream()
			.filter(e -> e.getShareCount() > 0)
			.count();
		return (int) cnt;
	}

	@Override
	public int getEquipNumByCondition(String playerId, int level, int quality) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return 0;
		}
		List<EquipEntity> equipList = playerData.getEquipEntities();
		int count = 0;
		for (EquipEntity equipEntity : equipList) {
			EquipmentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equipEntity.getCfgId());
			if (cfg.getLevel() >= level && cfg.getQuality() >= quality) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public int getSoldierLevel(Integer soldierId) {
		BattleSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, soldierId);
		if (config == null) {
			return -1;
		}
		return config.getLevel();
	}
	
	

	@Override
	public Map<Integer, Float> getArmyPowerMap() {
		Map<Integer, Float> map = new HashMap<>();
		ConfigIterator<BattleSoldierCfg> its = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		for (BattleSoldierCfg cfg : its) {
			map.put(cfg.getId(), cfg.getPower());
		}
		return map;
	}

	@Override
	public PBHeroInfo getHeroInfo(String playerId, int heroId) {
		Player player = getPlayer(playerId);
		if (player == null) {
			return null;
		}
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if(!heroOp.isPresent()){
			return null;
		}
		PlayerHero hero = heroOp.get();
		PBHeroInfo info =hero.toPBobj();
		HeroCfg config = hero.getConfig();
		PBHeroInfo.Builder builder = info.toBuilder()
				.setUnlockPieces(config.getUnlockPieces())
				.setQualityColor(config.getQualityColor());
		return builder.build();
	}

	@Override
	public int getRechargeCount(String playerId) {
		Player player = getPlayer(playerId);
		if (player == null) {
			return 0;
		}
		return player.getPlayerBaseEntity().getSaveAmtTotal();
	}

	@Override
	public boolean isNewly(String playerId) {
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		if (accountInfo != null) {
			return accountInfo.isNewly();
		} else {
			return false;
		}
	}

	@Override
	public void logPandoraLottery(String playerId, int num) {
		//由玩家主动触发的操作player不可能为空.
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logPandoraLotter(player, num);		
	}

	@Override
	public void logPandoraExchange(String playerId, int cfgId, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logPandoraExchange(player, cfgId, num);
	}

	@Override
	public int getGiftBuyCnt(String giftId) {
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if(giftCfg == null){
			return -1;
		}
		return giftCfg.getPayCount();
	}

	@Override
	public String getItemAward(int awardId) {
		AwardItems awardItems = AwardService.getInstance().takeAward(awardId, 1);
		List<ItemInfo> list = awardItems.getAwardItems();
		return ItemInfo.toString(list);
	}

	@Override
	public void logLuckyStarLottery(String playerId, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logLuckyStarLotter(player, num);
	}

	@Override
	public String getPlatform(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return "";
		}
		return playerData.getPlayerEntity().getPlatform();
	}
	
	@Override
	public int getPlatId(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		return player.getPlatId();
	}	

	@Override
	public HawkTuple2<MachineAwakeState, Long> getMachineAwakeInfo(ActivityType activityType) {
		MachineAwakeState state = MachineAwakeState.SLEEP;
		List<Integer> hourList = new ArrayList<Integer>();
		WorldMapConstProperty cfg = WorldMapConstProperty.getInstance();
		switch (activityType) {
		case MACHINE_AWAKE_ACTIVITY:
			state = WorldGundamService.getInstance().hasGundamInWorld() ? MachineAwakeState.AWAKED : MachineAwakeState.SLEEP;
			hourList.addAll(cfg.getGoundamRefreshTimeArr());
			break;
		case MACHINE_AWAKE_TWO_ACTIVITY:
			state = WorldNianService.getInstance().hasNianInWorld() ? MachineAwakeState.AWAKED : MachineAwakeState.SLEEP;
			hourList.addAll(cfg.getNianRefreshTimeArr());
			break;
		default:
			break;
		}
		Collections.sort(hourList);
		if (hourList.isEmpty()) {
			return new HawkTuple2<Activity.MachineAwakeState, Long>(state, Long.MAX_VALUE);
		}
		int minHour = hourList.get(0);
		int maxHour = hourList.get(hourList.size() - 1);
		Calendar currCalendar = Calendar.getInstance();
		int currHour = currCalendar.get(Calendar.HOUR_OF_DAY);
		Calendar nestCalendar = Calendar.getInstance();
		nestCalendar.setTimeInMillis(HawkTime.getMillisecond());
		nestCalendar.set(Calendar.MINUTE, 0);
		nestCalendar.set(Calendar.SECOND, 0);
		nestCalendar.set(Calendar.MILLISECOND, 0);
		// 今天还没刷新
		if(currHour < maxHour){
			int refreshHour = 0;
			for(int hour : hourList){
				if(hour > currHour){
					refreshHour = hour;
					break;
				}
			}
			nestCalendar.set(Calendar.HOUR_OF_DAY, refreshHour);
		}
		// 今天已刷新完
		else{
			nestCalendar.set(Calendar.HOUR_OF_DAY, minHour);
			nestCalendar.add(Calendar.DATE, 1);
		}

		return new HawkTuple2<Activity.MachineAwakeState, Long>(state, nestCalendar.getTimeInMillis());
	}

	@Override
	public String sendAwardFromAwardCfg(int awardId, int count, String playerId, boolean isPop, Action action, RewardOrginType... originType) {
		AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(cfg == null){
			HawkLog.errPrintln("sendAward error, awardId:{}, count:{}, playerId:{}", awardId, count, playerId);
			return "";
		}
		
		if(count <= 0){
			HawkLog.errPrintln("sendAward error, awardId:{}, count:{}, playerId:{}", awardId, count, playerId);
			return "";
		}
		
		Player player =  this.getPlayer(playerId);
		AwardItems awardItem = AwardItems.valueOf();
		for(int i = 0 ; i < count ; i ++){
			awardItem.addAward(awardId);
		}
		
		List<ItemInfo> awardItems = awardItem.getAwardItems().stream().map(e -> e.clone()).collect(Collectors.toList());
		
		if (isPop) {
			awardItem.rewardTakeAffectAndPush(player, action, true, originType.length > 0 ? originType[0] : RewardOrginType.ACTIVITY_REWARD);
		} else {
			awardItem.rewardTakeAffectAndPush(player, action);
		}
		
		return ItemInfo.toString(awardItems);
	}

	@Override
	public List<String> getAwardFromAwardCfg(int awardId) {
		List<String> list = new ArrayList<>();
		AwardItems awardItem = AwardItems.valueOf();
		//添加随机奖励
		awardItem.addAward(awardId);
		List<ItemInfo> awardItems = awardItem.getAwardItems().stream().map(e -> e.clone()).collect(Collectors.toList());
		for (ItemInfo itemInfo : awardItems) {
			list.add(itemInfo.toString());
		}
		return list;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void sendBroadcat(NoticeCfgId key, String playerId, Object... parms) {
		Player player = null;
		if (!HawkOSOperator.isEmptyString(playerId)) {
			player = GlobalData.getInstance().makesurePlayer(playerId);
		}
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, key, player, parms);
	}

	@Override
	public int getGuildFlat(String guildId) {
		return GuildService.getInstance().getGuildFlag(guildId);
	}
	
	@Override
	public boolean isCrossPlayer(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(Objects.isNull(player)){
			return CrossService.getInstance().isCrossPlayer(playerId);
		}
		return player.isCsPlayer();
	}
	
	public boolean isPlayerCrossIngorePlayerObj(String playerId) {
		return CrossService.getInstance().isCrossPlayer(playerId);
	}
	
	@Override
	public boolean isPlayerExist(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		return player != null;
	}

	@Override
	public boolean isInDungeonState(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
//			if(player.getYQZZState() != null){ // 在月球中不算 
//				return false;
//			}
			return player.isInDungeonMap();
		}
		return false;
	}


	@Override
	public boolean isNpcPlayer(String playerId) {
		return GameUtil.isNpcPlayer(playerId);
	}

	@Override
	public boolean isGuildExist(String guildId) {
		return GuildService.getInstance().isGuildExist(guildId);
	}

	@Override
	public boolean isGuildLocalExist(String guildId) {
		return GuildService.getInstance().getGuildInfoObject(guildId) != null;
	}

	@Override
	public void sendSystemRedEnvelope(String playerId, int awardId, RecieveCallBack callback) {
		AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(cfg == null){
			HawkLog.errPrintln("sendSystemRedEnvelope error, awardId:{}, playerId:{}", awardId, playerId);
			return;
		}
		Player player =  this.getPlayer(playerId);
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addAward(awardId);
		awardItem.rewardTakeAffectAndPush(player, Action.RED_ENVELOPE_SYSTEM, false, RewardOrginType.ACTIVITY_REWARD);
		List<String> list = new ArrayList<>();
		for(ItemInfo info : awardItem.getAwardItems()){
			list.add(info.toString());
		}
		//发放奖励
		callback.call(0,list);
	}

	@Override
	public String getPfIcon(String playerId) {
		PlayerData playerData = getPlayerData(playerId);
		if (playerData == null) {
			return "";
		}
		return playerData.getPfIcon();
	}

	@Override
	public int getIcon(String playerId) {
		Player player = getPlayer(playerId);
		if (player == null) {
			return GameConstCfg.getInstance().getDefaultIcon();
		}
		return player.getIcon();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void addWorldBroadcastMsg(ChatType chatType, NoticeCfgId key, String playerId, Object... parms) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		ChatService.getInstance().addWorldBroadcastMsg(chatType, key, player, parms);
	}

	@Override
	public void addWorldBroadcastMsg(ChatType chatType, String guildId, NoticeCfgId key, String playerId, Object... parms) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(chatType).setKey(key).setPlayer(player).setGuildId(guildId).addParms(parms).build());
	}
	
	@Override
	public String getAccountRoleInfoKey(String openId) {
		return RedisProxy.getInstance().getAccountRoleInfoKey(openId);
	}

	@Override
	public String getOpenId(String playerId) {
		Player player = getPlayer(playerId);
		if(player != null){
			return player.getOpenId();
		}
		return null;
	}

	@Override
	public void logRecieveComeBackGreatReward(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logRecieveComeBackGreatReward(player);		
	}

	@Override
	public void logComeBackAchieve(String playerId, int achieveId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logComeBackAchieve(player, achieveId);	
	}

	@Override
	public void logComeBackExchange(String playerId, int id, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logComeBackExchange(player, id, num);	
	}

	@Override
	public void logComeBackBuy(String playerId, int id, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logComeBackBuy(player, id, num);
	}
	
	public JSONObject fetchPlatformFriendList(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			if (GsConfig.getInstance().isDebug() && GameUtil.isWin32Platform(player)) {
				return RedisProxy.getInstance().getWin32Friend(player.getOpenId());
			} else {
				return RelationService.getInstance().fetchPlatformFriendList(player);
			}			
		}
		
		return null;
	}
	
	/**
	 * 获取玩家的serverId
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String getPlayerServerId(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			return player.getServerId(); 
		} else {
			return null;
		}
	}
	@Override
	public String getPlayerMainServerId(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			return player.getMainServerId();
		} else {
			return null;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public long getServerOpenTime(String playerId) {
		Player player = this.getPlayer(playerId);
		if(player != null){
			return GlobalData.getInstance().getServerOpenTime(player.getServerId());
		}else {
			return GameUtil.getServerOpenTime();
		}
	}

	/**
	 * 调用http服务
	 * @param serverId
	 * @param script
	 * @param formatArgs
	 * @param timeout
	 * @return
	 */
	@Override
	public  HawkTuple2<Integer, String> proxyCall(String serverId, String script, String formatArgs, int timeout) {
		return GmProxyHelper.proxyCall(serverId, script, formatArgs, timeout);
	}
	
	@Override
	public String doHttpRequest(String url, int timeout) {		
		return GmProxyHelper.doHttpRequest(url, timeout);
	}
	
	@Override
	public void saveRoleInfoAfterOldServerComeBack(ActivityAccountRoleInfo info) {
		RedisProxy.getInstance().saveRoleInfoAfterOldServerComeBack(info);
	}

	@Override
	public ActivityAccountRoleInfo getRoleInfoInheritIdentifyAndRemove(String openId) {
		return RedisProxy.getInstance().getRoleInfoInheritIdentifyAndRemove(openId);
	}

	
	@Override
	public BackPlayerInfo getBackPlayerInfoById(String playerId) {
		return InheritService.getInstance().getBackInfoByPlayerIdForAct(playerId);
	}

	@Override
	public AccountRoleInfo getSuitInheritAccount(String playerId) {
		return InheritService.getInstance().getSuitInheritAccount(playerId);
	}

	@Override
	public int getAccountRechargeNumAndExp(AccountRoleInfo roleInfo) {
		return InheritService.getInstance().getAccountRechargeNumAndExp(roleInfo);
	}
	
	
	@Override
	public Long getServerMergeTime() {
		String serverId = GsConfig.getInstance().getServerId();
		return AssembleDataManager.getInstance().getServerMergeTime(serverId);
	}

	@Override
	public void updateBackPlayerInfo(BackPlayerInfo info) {
		RedisProxy.getInstance().updateBackPlayerInfo(info);
	}

	@Override
	public void addInheritedInfo(AccountRoleInfo info, String playerId, int rebetGold) {
		JSONObject json = new JSONObject();
		json.put("oldServer", info.getServerId());
		json.put("oldRoleId", info.getPlayerId());
		json.put("newServer", GsConfig.getInstance().getServerId());
		json.put("newRoleId", playerId);
		json.put("rebetGold", rebetGold);
		json.put("time", HawkTime.getMillisecond());
		RedisProxy.getInstance().addInheritedInfo(info, json);
	}

	@Override
	public int getVipMaxExp() {
		return VipCfg.getMaxVipExp();
	}
	
	/**********************************************/
	@Override
	public BackNewPlayerInfo getBackPlayerInfoByIdNew(String playerId) {
		return InheritNewService.getInstance().getBackInfoByPlayerIdForAct(playerId);
	}

	@Override
	public List<AccountRoleInfo> getPlayerAccountInfosNew(String playerId) {
		Player player = getPlayer(playerId);
		if(player == null){
			return Collections.emptyList();
		}
		return InheritNewService.getInstance().getPlayerAccountInfos(player);
	}

	@Override
	public AccountRoleInfo getSuitInheritAccountNew(String playerId) {
		return InheritNewService.getInstance().getSuitInheritAccount(playerId);
	}

	@Override
	public int getAccountRechargeNumAndExpNew(AccountRoleInfo roleInfo) {
		return InheritNewService.getInstance().getAccountRechargeNumAndExp(roleInfo);
	}
	
	@Override
	public void updateBackPlayerInfoNew(String playerId, BackNewPlayerInfo info) {
		RedisProxy.getInstance().updateBackPlayerInfoNew(info);
	}

	@Override
	public void inheritDataCollect(String playerId, List<AccountRoleInfo> inheritedInfo, List<HawkTuple2<AccountRoleInfo, Integer>> notInheritedInfo) {
		Player player = getPlayer(playerId);
		if(player == null){
			return;
		}
		InheritNewService.getInstance().inheritCondDataCollect(player, inheritedInfo, notInheritedInfo);
	}

	/**********************************************/

	@Override
	public int getOwnerFlagCount(String guildId) {
		return FlagCollection.getInstance().getOwnCompFlags(guildId, false, true, false).size();
	}
	
	@Override
	public List<Integer> getCreatedWarFlagPoints(String guildId) {
		List<IFlag> entityList = FlagCollection.getInstance().getOwnCompFlags(guildId, true, true, true);
		return getPointList(entityList, guildId);
	}
	
	@Override
	public List<Integer> getCanFightCenterFlags(String guildId) {
		List<IFlag> entityList = FlagCollection.getInstance().getCenterFlag(guildId);
		return getPointList(entityList, guildId);
	}
	
	@Override
	public List<Integer> getOccupyWarFlagPoints(String guildId) {
		List<IFlag> entityList = FlagCollection.getInstance().getOccupyFlags(guildId);
		return getPointList(entityList, guildId);
	}

	@Override
	public List<Integer> getLoseWarFlagPoints(String guildId) {
		List<IFlag> entityList = FlagCollection.getInstance().getBeLootFlags(guildId);
		return getPointList(entityList, guildId);
	}
	
	private List<Integer> getPointList(List<IFlag> entityList, String guildId) {
		List<Integer> pointList = new ArrayList<Integer>();
		for (IFlag entity : entityList) {
			if (WarFlagService.getInstance().canFlagFight(entity, guildId)) {
				int[] coord = GameUtil.splitXAndY(entity.getPointId());
				int point = coord[0] * 10000 + coord[1];
				pointList.add(point);
			} else {
//				pointList.add(0);
			}
		}
		return pointList;
	}

	@Override
	public int getMaxCreateWarFlagCount(String guildId) {
		return FlagCollection.getInstance().getOwnerFlagCount(guildId);
	}

	@Override
	public void yuriRevengeSendRewardByMergeServer(int termId) {
		YuriRevengeService.getInstance().onActivityEnd(termId);		
	}

	@Override
	public boolean consumeItemsIsGold(String playerId, List<Builder> itemList, int protocolType, Action action) {
		Player player = getPlayer(playerId);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		for (RewardItem.Builder rewardItem : itemList) {
			consumeItems.addConsumeInfo(new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(), (int) rewardItem.getItemCount()), true);
		}

		if (!consumeItems.checkConsume(player, protocolType)) {
			return false;
		}
		consumeItems.consumeAndPush(player, action);
		return true;
	}

	@Override
	public boolean consumeGold(String playerId, int goldCount, int protocolType, Action action) {
		Player player = getPlayer(playerId);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, goldCount);

		if (!consume.checkConsume(player, protocolType)) {
			return false;
		}
		consume.consumeAndPush(player, action);
		return true;
	}
	
	@Override
	public int getGuildWarFlagCount(String guildId) {
		return FlagCollection.getInstance().getGuildCompFlagCount(guildId);
	}

	@Override
	public void logBuyOrderExp(String playerId, int termId, int cycle, int expId, int exp) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logBuyOrderExp(player, termId, cycle, expId, exp);
	}

	@Override
	public void logBuyOrderAuth(String playerId, int termId, int cycle, int authId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logBuyOrderAuth(player, termId, cycle, authId);
	}

	@Override
	public void logOrderExpChange(String playerId, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logOrderExpChange(player, termId, cycle, expAdd, exp, level, reason, reasonId);
	}

	@Override
	public void logOrderFinishId(String playerId, int termId, int cycle, int orderId, int addTimes, int finishTimes) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logOrderFinishId(player, termId, cycle, orderId, addTimes, finishTimes);
	}

	@Override
	public String getPfIconFromRedis(String openId, String platform) {
		String puid = GameUtil.getPuidByPlatform(openId, platform);
		return RedisProxy.getInstance().getPfIcon(puid);
	}

	@Override
	public void dailySignRewardRecord(String playerId, int type, int termId, int dayIndex, String cost) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logDailySignReward(player, type, termId, dayIndex, cost);		
		}
	}

	@Override
	public boolean isServerPlayer(String playerId) {
		return GlobalData.getInstance().getAccountRoleInfo(playerId) == null ? false : true;
	}

	@Override
	public void logPlanActivityLottery(String playerId, int termId, int lotteryType, int score) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logPlanActivityLottery(player, termId, lotteryType, score) ;
		}
	}

	public void logBountyHunterHit(String playerId, int termId, String boss, int bossHp, String costStr, String rewStr, boolean free, int rewardMutil, int lefState,
			String bigGift) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logBountyHunterHit(player, termId, boss, bossHp, costStr, rewStr, free, rewardMutil, lefState, bigGift);
	}
	
	@Override
	public void setPlayerSpreadInfo(String playerId, String openId, String serverId) {
		RedisProxy.getInstance().setPlayerSpreadInfo(playerId, openId, serverId);
	}

	@Override
	public PBSpreadBindRoleInfo getPlayerSpreadInfo(String playerId) {
		return RedisProxy.getInstance().getPlayerSpreadInfo(playerId);
	}

	@Override
	public void setSpreadOpenidBindFlag(String openId) {
		RedisProxy.getInstance().setSpreadOpenidBindFlag(openId);
	}

	@Override
	public boolean getIsSpreadOpenidBindFlag(String openId) {
		return RedisProxy.getInstance().getIsSpreadOpenidBindFlag(openId);
	}


	/**
	 * 推广员活动玩家打点
	 * @param playerId
	 * @param openid 绑定的openid
	 * @param code  绑定的code
	 * @param charge 玩家充值数量
	 * @param exchange 字段为0 表示是登录数据，其他表示兑换的id
	 * @param count 兑换的数量
	 */
	@Override
	public void logPlayerSpreadLogin(String playerId, String openid, String code, int charge, int exchange, int count) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logPlayerSpreadLogin(player, openid, code, charge, exchange, count) ;
		}
	}

	@Override
	public int caculateTimeGold(long time, SpeedUpTimeWeightType type) {
		return GameUtil.caculateTimeGold(time / 1000, type);
	}

	@Override
	public List<String> getMergeServerList() {
		String serverId = GsConfig.getInstance().getServerId();
		return AssembleDataManager.getInstance().getMergedServerList(serverId);
	}
	
	@Override
	public List<String> getSlaveServerList() {
		return GlobalData.getInstance().getSlaveServerList();
	}

	@Override
	public void logInvest(String playerId, int productId, int investAmount, boolean addCustomer, boolean investCancel) {
		Player player = getPlayer(playerId);
		if (player != null) {
			LogUtil.logInvest(player, productId, investAmount, addCustomer, investCancel);
		}
	}
	

	/**
	 * 幸运折扣活动刷新奖池打点
	 * @param playerId
	 * @param refreshType 1 免费次数刷新, 2 使用道具刷新
	 * @param poolId 刷新到的奖池id
	 * @param discount 刷新到的折扣
	 */
	@Override
	public void logLuckyDiscountDraw(String playerId, int refreshType, int poolId, String discount){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logLuckyDiscountDraw(player, refreshType, poolId, discount) ;
		}		
	}
	
	/**
	 * 幸运折扣活动购买商品打点
	 * @param playerId
	 * @param goods 购买商品
	 * @param price 购买的单价
	 * @param num  购买的数量
	 * @param discount 购买的折扣
	 * @param goodsId 购买商品的id
	 * 
	 */
	
	@Override
	public void logLuckyDiscountBuy(String playerId, String goods, String price, int num, String discount, int goodsId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logLuckyDiscountBuy(player, goods, price, num, discount, goodsId) ;
		}		
	}
	
	/**
	 * 活动模板8 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	@Override
	public void setPlayerUrlModelEightActivityInfo(String playerId, boolean isShared, boolean isReward){
		 RedisProxy.getInstance().setPlayerUrlModelEightActivityInfo(playerId, isShared, isReward);
	}
	
	/**
	 * 活动模板8 腾讯url活动分享 
	 * @return
	 */
	@Override
	public PBEmptyModel8Info getPlayerUrlModelEightActivityInfo(String playerId){
		return RedisProxy.getInstance().getPlayerUrlModelEightActivityInfo(playerId);
	}
	
	/**
	 * 活动模板9 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	@Override
	public void setPlayerUrlModelNineActivityInfo(String playerId, boolean isShared, boolean isReward){
		 RedisProxy.getInstance().setPlayerUrlModelNineActivityInfo(playerId, isShared, isReward);
	}
	
	/**
	 * 活动模板9 腾讯url活动分享 
	 * @return
	 */
	@Override
	public PBEmptyModel9Info getPlayerUrlModelNineActivityInfo(String playerId){
		return RedisProxy.getInstance().getPlayerUrlModelNineActivityInfo(playerId);
	}
	
	/**
	 * 活动模板10 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	@Override
	public void setPlayerUrlModelTenActivityInfo(String playerId, boolean isShared, boolean isReward){
		 RedisProxy.getInstance().setPlayerUrlModelTenActivityInfo(playerId, isShared, isReward);
	}
	
	/**
	 * 活动模板10 腾讯url活动分享 
	 * @return
	 */
	@Override
	public PBEmptyModel10Info getPlayerUrlModelTenActivityInfo(String playerId){
		return RedisProxy.getInstance().getPlayerUrlModelTenActivityInfo(playerId);
	}

	/**
	 * 是否触发任务
	 */
	@Override
	public boolean touchHeroTrial(String playerId, List<Integer> heroIds, List<Integer> condition) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		if (condition.size() <= 0) {
			return false;
		}
		if (heroIds.size() <= 0) {
			return false;
		}
		int type = condition.get(0);
		IHeroTrialChecker checker = HeroTrialCheckerFactory.getInstance().getChecker(type);
		if (checker == null) {
			return false;
		}
		return checker.touchMission(playerId, heroIds, condition);
	}
	
	@Override
	public void logHeroTrialReceive(String playerId, int missionId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHeroTrialReceive(player, missionId);
		}
	}
	
	@Override
	public void logHeroTrialRefreshMission(String playerId, int missionId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHeroTrialRefreshMission(player, missionId);
		}
	}
	
	@Override
	public void logHeroTrialComplete(String playerId, int missionId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHeroTrialComplete(player, missionId);
		}
	}
	
	@Override
	public void logHeroTrialCostRefresh(String playerId, String cost) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHeroTrialCostRefresh(player, cost);
		}
	}
	
	@Override
	public void logHeroTrialCostSpeed(String playerId, int cost) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHeroTrialCostSpeed(player, cost);
		}
	}

	@Override
	public void logHeroBackExchange(String playerId, int activityId, int exchangeId, String costItem, String gainItem, int count) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player != null) {
			ItemInfo itemInfo = ItemInfo.valueOf(costItem);
			itemInfo.setCount(itemInfo.getCount() * count);
			
			ItemInfo awardItem = ItemInfo.valueOf(gainItem);
			awardItem.setCount(awardItem.getCount() * count);
			LogUtil.logHeroBackExchange(player, activityId, exchangeId, itemInfo.toString(), awardItem.toString(), count);
		}
	}

	@Override
	public void logHeroBackBuyChest(String playerId, int activityId, int chestId, int count, String costItem, String gainItem) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player != null) {
			ItemInfo itemInfo = ItemInfo.valueOf(costItem);
			itemInfo.setCount(itemInfo.getCount() * count);
			LogUtil.logHeroBackBuyChest(player, activityId, chestId, count, itemInfo.toString(), gainItem);
		}
	}

	@Override
	public void logBlackTechDraw(String playerId, long cost, int buffId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logBlackTechDraw(player, cost, buffId) ;
		}	
	}

	@Override
	public void logBlackTechActive(String playerId, int buffId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logBlackTechActive(player, buffId) ;
		}	
	}

	@Override
	public void logBlackTechBuy(String playerId, int packageId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logBlackTechBuy(player, packageId) ;
		}	
	}

	@Override
	public void logFullArmedSearch(String playerId, int searchId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logFullArmedSearch(player, searchId) ;
		}		
	}

	@Override
	public void logFullArmedBuy(String playerId, int cfgId, int count) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logFullArmedBuy(player, cfgId, count) ;
		}	
	}

	@Override
	public void logPioneerGiftBuy(String playerId, int termId, int type, int giftId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logPioneerGiftBuy(player, termId, type, giftId);
		}
	}

	@Override
	public void logRouletteActivityLottery(String playerId, int termId, int count, int buyCount, String itemSet) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logRouletteActivityLottery(player, termId, count, buyCount, itemSet);
		}
	}

	@Override
	public void logRouletteActivityRewardBox(String playerId, int termId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logRouletteActivityRewardBox(player, termId);
		}
	}

	@Override
	public void logSkinPlan(String playerId, int addScore, int afterScore, int beforeScore) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logSkinPlan(player, addScore, afterScore, beforeScore);
		}
	}
	
	@Override
	public void logDailyRecharge(String playerId, int id, boolean buyGift) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logDailyRechargeInfo(player, id, buyGift);
		}
	}
	
	@Override
	public void logDailyRechargeNew(String playerId, int giftId, String rewardIds) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logDailyRechargeNewInfo(player, giftId, rewardIds);
		}
	}

	@Override
	public void logMachineAwakePersonDamage(String playerId, int activityId, int termId, int addScore, long totalScore) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logMachineAwakePersonDamage(player, activityId, termId, addScore, totalScore);		
		}
	}

	@Override
	public void logMidAutumnGift(String playerId, int giftId, String items) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logMidAutumnGift(player, giftId, items);
		}
		
	}
	
	@Override
	public boolean checkGuildAuthority(String playerId, AuthId authId) {
		return GuildService.getInstance().checkGuildAuthority(playerId, authId);
	}

	@Override
	public void logACMissionReceive(String playerId, int achieveId, int guildMemberCount) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logACMissionReceive(player, achieveId, guildMemberCount);
		}
	}

	@Override
	public void logACMissionFinish(String playerId, int addExp, int guildMemberCount) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logACMissionFinish(player, addExp, guildMemberCount);
		}
	}

	@Override
	public void logACMissionAbandon(String playerId, int achieveId, boolean outData) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logACMissionAbandon(player, achieveId, outData);
		}
	}

	@Override
	public void logACMBuyTimes(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logACMBuyTimes(player);
		}
	}

	@Override
	public void logMedalTreasureLottery(String playerId, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logMedalTreasureLottery(player, num);
		}
	}

	@Override
	public void logTimieLimit(String playerId, int dropNum) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logTimeLimitDrop(player, dropNum);
		}
		
	}

	@Override
	public void logHellFire(String playerId, int type, int score) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHellFire(player, type, score);
		}
	}

	@Override
	public void travelShopAssistRefresh(String playerId, boolean clear) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			PlayerTravelShopModule travelShopModel = player.getModule(GsConst.ModuleType.TRAVEL_SHOP);
			travelShopModel.onTravelShopAssistActivityChange(clear);
		}
	}
	
	@Override
	public void logTravelShopAssistAchieveFinish(String playerId,int achieveId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logTravelShopAssistAchieveFinish(player, achieveId);
		}
	}

	@Override
	public void logDivideGoldOpenRedEnvelope(String playerId, int goldNum) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logOpenRedEnvelope(player, goldNum);
		}
		
	}

	@Override
	public void redkoiAward(String playerId, int awardId,String termId, String turnId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.redkoiAward(player, awardId,termId, turnId);
		}
		
	}

	
	
	@Override
	public void redkoiPlayerCost(String playerId, int awardId, int costType,long cost, 
			int wishPoint, String termId,String turnId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.redkoiPlayerCost(player, awardId, costType, cost,wishPoint, termId, turnId);
		}
		
	}

	
	@Override
	public boolean isLocalServer(String serverId){
		return GlobalData.getInstance().isLocalServer(serverId);
	}

	
	@Override
	public void logEvolutionExchange(String playerId, int level, int exchangeId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logEvolutionExchange(player, level, exchangeId);
		}
	}
	
	@Override
	public void logEvolutionExpChange(String playerId, int exp, boolean add, int resourceId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logEvolutionExpChange(player, exp, add, resourceId);
		}
	}


	@Override
	public void logEvolutionTask(String playerId, int taskId, int times) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logEvolutionTask(player, taskId, times);
		}
	}

	@Override
	public void logFlightPlanExchange(String playerId, int cfgId, int itemId, int itemNum, int costNum, int exchangeTimes) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logFlightPlanExchange(player, cfgId, itemId, itemNum, costNum, exchangeTimes);
		}
	}

	@Override
	public TiberiumSeasonTimeAbstract getTiberiumSeasonTimeCfg() {
		TiberiumSeasonTimeCfg timeCfg = TiberiumLeagueWarService.getInstance().getCurrTimeCfg();
		return timeCfg;
	}

	@Override
	public TLWGetMatchInfoResp.Builder getTblyMatchInfo(String playerId, int termId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
//			TLWGetMatchInfoResp.Builder builder = TiberiumLeagueWarService.getInstance().getTLWMatchInfo(0, termId, player);
//			return builder;
		}
		return null;
	}

	@Override
	public HawkTuple3<Long, Integer, String> getTWGuildInfo(String guildId) {
		return TiberiumLeagueWarService.getInstance().getTWGuildData(guildId);
	}

	@Override
	public boolean isTblySameRoom(String guildIdA, String guildIdB) {
		return TiberiumLeagueWarService.getInstance().isSameRoom(guildIdA, guildIdB);
	}

	@Override
	public TiberiumSeasonTimeAbstract getTiberiumSeasonTimeCfgByTermId(int season, int termId) {
		TiberiumSeasonTimeCfg timeCfg = TiberiumLeagueWarService.getInstance().getTimeCfgBySeasonAndTermId(season, termId);
		return timeCfg;
	}

	/**
	 * 活动赛事对应开始和结束时间
	 * @param matchType 活动类型
	 * @param termID 活动期数
	 * @return 赛事开始和结束时间 first是开始时间，second是结束时间
	 */
	@Override
	public HawkTuple2<Long, Long> getSeasonActivityMatchInfo(Activity.SeasonMatchType matchType, int termID) {
		//赛事时间
		HawkTuple2<Long, Long> tuple2;
		switch (matchType){
			//泰伯利亚
			case S_TBLY:{
				long startTime = 0L;
				long endTime = 0L;
				//遍历泰伯活动时间配置
				ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance()
						.getConfigIterator(TiberiumSeasonTimeCfg.class);
				for(TiberiumSeasonTimeCfg cfg : its){
					//只处理配置的对应赛季
					if(cfg.getSeason() == termID){
						//获得开始时间
						if(cfg.getSeasonStartTimeValue() != 0){
							startTime = cfg.getSeasonStartTimeValue();
						}
						//获得结束时间
						if(cfg.getSeasonEndTimeValue() != 0){
							endTime = cfg.getSeasonEndTimeValue();
						}
					}
				}
				tuple2 = new HawkTuple2<>(startTime, endTime);
			}
			break;
			//赛博
			case S_CYBORG:{
				//获得配置期数对应的赛博时间配置
				CyborgSeasonTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CyborgSeasonTimeCfg.class, termID);
				if(cfg == null){
					//如果没找到配置返回0
					tuple2 = new HawkTuple2<>(0L, 0L);
				}else {
					tuple2 = new HawkTuple2<>(cfg.getShowTimeValue(), cfg.getEndTimeValue());
				}
			}
			break;
			//陨晶，打压之战
			case S_DYZZ:{
				//获得配置期数对应的陨晶，打压之战时间配置
				DYZZSeasonTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DYZZSeasonTimeCfg.class, termID);
				if(cfg == null){
					//如果没找到配置返回0
					tuple2 = new HawkTuple2<>(0L, 0L);
				}else {
					tuple2 = new HawkTuple2<>(cfg.getShowTimeValue(), cfg.getEndTimeValue());
				}
			}
			break;
			//统帅，大帝战，星球大战
			case S_SW:{
				StarWarsTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsTimeCfg.class, termID);
				if(cfg == null){
					//如果没找到配置返回0
					tuple2 = new HawkTuple2<>(0L, 0L);
				}else {
					tuple2 = new HawkTuple2<>(cfg.getSignStartTimeValue(), cfg.getEndTimeValue());
				}
			}
			break;
			//月球之巅
			case S_YQZZ:{
				long startTime = 0L;
				long endTime = 0L;
				ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
				for(YQZZTimeCfg cfg : iterator){
					//只处理配置的对应赛季
					if(cfg.getSeason() == termID){
						//获得开始时间
						if(cfg.getSeasonStartTimeValue() != 0){
							startTime = cfg.getSeasonStartTimeValue();
						}
						//获得结束时间
						if(cfg.getSeasonEndTimeValue() != 0){
							endTime = cfg.getSeasonEndTimeValue();
						}
					}
				}
				tuple2 = new HawkTuple2<>(startTime, endTime);
			}
			break;
			//星海激战
			case S_XHJZ:{
				XHJZSeasonTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XHJZSeasonTimeCfg.class, termID);
				if(cfg == null){
					//如果没找到配置返回0
					tuple2 = new HawkTuple2<>(0L, 0L);
				}else {
					tuple2 = new HawkTuple2<>(cfg.getSeasonStartTimeValue(), cfg.getSeasonEndTimeValue());
				}
			}
			break;
			//航海赛季
			case S_CROSS:{
				CrossSeasonTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossSeasonTimeCfg.class, termID);
				if(cfg == null){
					//如果没找到配置返回0
					tuple2 = new HawkTuple2<>(0L, 0L);
				}else {
					tuple2 = new HawkTuple2<>(cfg.getShowTimeValue(), cfg.getEndTimeValue());
				}
			}
			break;
			default:{
				//未知类型返回0
				tuple2 = new HawkTuple2<>(0L, 0L);
			}
		}
		//返回赛事时间
		return tuple2;
	}

	@Override
	public int getEnergyCount(String playerId) {
		LaboratoryKVCfg kvcfg = HawkConfigManager.
				getInstance().getKVInstance(LaboratoryKVCfg.class);
		return this.getItemNum(playerId, kvcfg.getLockItemId());
	}
	
	
	@Override
	public List<CommandAcademyRankScoreCfg> getCommandAcademyRankScoreCfg(int rank) {
		return AssembleDataManager.getInstance().getAcademyRankScoreCfg(rank);
	}
	
	@Override
	public List<CommandAcademySimplifyRankScoreCfg> getCommandAcademySimplifyRankScoreCfg(int rank) {
		return AssembleDataManager.getInstance().getAcademyRankSimplifyScoreCfg(rank);
	}

	@Override
	public boolean dealWithBaseBuild(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			//TODO 记录失败信息
			return false;
		}
		BuildingService.getInstance().dealBaseBuild(player);
		return false;
	}

	@Override
	public boolean dealWithBackToNewFlyBuild(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			//TODO 记录失败信息
			return false;
		}
		BuildingService.getInstance().dealBackToNewFlyBuild(player);
		return false;
	}
	
	
	

	@Override
	public  void logCommandAcademyGiftBuy(String playerId, int termId, int stageId,int giftId){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logCommandAcademyGiftBuy failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logCommandAcademyGiftBuy(player, termId, stageId, giftId);
	}
	
	@Override
	public  void logCommandAcademyRank(String playerId, int termId, int stageId,int rankIndex){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logCommandAcademyRank failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logCommandAcademyRank(player, termId, stageId, rankIndex);
	}
	
	
	@Override
	public  void logCommandAcademyBuyCount(int termId,int stageId,int buyCount,int assistCount){
		LogUtil.logCommandAcademyBuyCount(termId, stageId, buyCount, assistCount);
		
	}
	
	@Override
	public void logCommandAcademySimplifyGiftBuy(String playerId, int termId, int stageId, int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logCommandAcademyGiftBuy failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logCommandAcademySimplifyGiftBuy(player, termId, stageId, giftId);
	}

	@Override
	public void logCommandAcademySimplifyRank(String playerId, int termId, int stageId, int rankIndex) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logCommandAcademyRank failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logCommandAcademySimplifyRank(player, termId, stageId, rankIndex);
	}

	@Override
	public void logCommandAcademySimplifyBuyCount(int termId, int stageId, int buyCount, int assistCount) {
		LogUtil.logCommandAcademySimplifyBuyCount(termId, stageId, buyCount, assistCount);
	}
	
	
	@Override
	public void sendAllianceGift(String playerId,int allianceGift){
		// 联盟成员发放礼物
		String guildId = this.getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		if(allianceGift > 0){
			GuildService.getInstance().bigGift(guildId).addSmailGift(allianceGift, false);
		}
	}
	@Override
	public void logEquipBlackMarketRefine(String playerId,int termId,int refineId,int count){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logEquipBlackMarketRefine failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logEquipBlackMarketRefine(player, termId, refineId, count);
	}
	@Override
	public int getChristmasBossRefreshLimit() {
		return WorldMapConstProperty.getInstance().getChristmasRefreshLimit();
	}
	
	@Override
	public void logChristmasTask(int termId, int taskId, int num) {
		LogUtil.logChristmasTask(termId, taskId, num);
	}
	
	@Override
	public void logChristmasTaskReceive(String playerId, int termId, int taskId) {
		Player player = this.getPlayer(playerId);
		LogUtil.logChristmasTaskReceive(player, termId, taskId);
	}
	
	@Override
	public int getChristmasBossNum() {
		return WorldChristmasWarService.getInstance().getBoss().size();
	}
	@Override
	public boolean isInTheSameGuild(String... playerIds) {
		return GuildService.getInstance().isInTheSameGuild(playerIds);
	}

	@Override
	public boolean isDailyOffLine(String playerId) {
		Player player = this.getPlayer(playerId);
		if(player == null){
			return false;
		}
		if(player.isActiveOnline()){
			return false;
		}
		// 玩家离线时间早于今天0点
		if(player.getLogoutTime() < HawkTime.getAM0Date().getTime()){
			return true;
		}
		return false;
	}

	@Override
	public boolean isDailyFirstLogin(String playerId) {
		Player player = this.getPlayer(playerId);
		if(player == null){
			return false;
		}
		// 玩家离线时间早于今天0点
		if(player.getLoginTime() > HawkTime.getAM0Date().getTime() &&player.getLogoutTime() < HawkTime.getAM0Date().getTime()){
			return true;
		}
		return false;
	}
	
	@Override
	public long getPlayerLogoutTime(String playerId) {
		Player player = this.getPlayer(playerId);
		if(player != null) {
			return player.getLogoutTime();
		}
		
		return 0;
	}
	
	public String genRobotName() {
		return WorldRobotNameCfg.randmName();
	}
	
	/**
	 * 资源保卫战 建造资源站
	 * @param player
	 * @param resType 资源站类型
	 */
	public void logResourceDefenseBuild(String playerId, int resType) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logResourceDefenseBuild(player, resType);
	}
	
	/**
	 * 资源保卫战偷取
	 * @param player
	 * @param targetId 目标玩家id
	 */
	public void logResourceDefenseSteal(String playerId, String targetId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logResourceDefenseSteal(player, targetId);
	}
	
	/**
	 * 资源保卫战获取经验
	 * @param player
	 * @param addExp 增加经验
	 * @param afterExp 增加过后玩家总经验
	 * @param afterLevel 增加经验过后玩家等级
	 */
	public void logResourceDefenseExp(String playerId, int addExp, int afterExp, int afterLevel) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logResourceDefenseExp(player, addExp, afterExp, afterLevel);
	}


	@Override
	public void logChronoGiftTaskFinish(String playerId, int termId, int taskId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logChronoGiftTaskFinish failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logChronoGiftTaskFinish(player, termId, taskId);
	}

	@Override
	public void logChronoGiftUnlock(String playerId, int termId, int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logChronoGiftUnlock failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logChronoGiftUnlock(player, termId, giftId);
		
	}

	@Override
	public void logChronoGiftFreeAwardAchieve(String playerId, int termId, int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logChronoGiftUnlock failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logChronoGiftFreeAwardAchieve(player, termId, giftId);
		
	}

	
	

	@Override
	public void logRechargeFundInvest(String playerId, int termId, int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logRechargeFundInvest failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logRechargeFundInvest(player, termId, giftId);
	}

	@Override
	public void logRechargeFundRecharge(String playerId, int termId, int rechargeGold, int rechargeBef, int rechargeAft, int unlockCnt) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logRechargeFundRecharge failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logRechargeFundRecharge(player, termId, rechargeGold, rechargeBef, rechargeAft, unlockCnt);
	}

	@Override
	public void logRechargeFundReward(String playerId, int termId, int giftId, int rewardId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logRechargeFundReward failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logRechargeFundReward(player, termId, giftId, rewardId);
	}
	
	
	
	@Override
	public boolean checkMergeServerTimeWithCrossTime(MergeServerTimeCfg timeCfg) {
		//有两种途径校验一种是xmlReload,一种起服之后.
		if (!GsApp.getInstance().isInitOK()) {
			return true;
		}
		return CrossActivityService.getInstance().checkMergeServerTimeWithCrossTime(timeCfg);			
	}
	
	@Override
	public BackFlowPlayer getBackFlowPlayer(String playerId) {
		return BackFlowService.getInstance().getBackFlowPlayer(playerId);
	}
	
	
	@Override
	public void sendChatRoomMessage(String sender,List<String> receivers,NoticeCfgId nid,Object... params){
		Player sendPlayer =  this.getPlayer(sender);
		if (sendPlayer == null) {
			HawkLog.errPrintln(" sendP2PChatRoomMessage failed, sender null, sender: {}", sender);
			return;
		}
		List<Player> members = receivers.stream().map(this::getPlayer).collect(Collectors.toList());
		members.add(sendPlayer);
		PersonalMailService.getInstance().createChatRoom(sendPlayer, members, nid, params);
	}
	
	/**
	 * 获取登录时间
	 */
	@Override
	public long getAccountLoginTime(String playerId){
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		if (accountInfo != null) {
			return accountInfo.getLoginTime();
		} else {
			return 0;
		}
	}
	
	
	@Override
	public void logBackGiftLottery(String playerId, int termId, int backCount, int isFree,int lotteryCount){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logBackGiftLottery failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logBackGiftLottery(player, termId, backCount, isFree, lotteryCount);
	}
	
	
	
	@Override
	public void logPowerSendMessageCount(String playerId, int termId, int backCount, int messageCount,int messageTotalCount){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logPowerSendMessageCount failed, player null, playerId: {}", playerId);
			return;
		}
		LogUtil.logPowerSendMessageCount(player, termId, backCount, messageCount, messageTotalCount);
	}

	@Override
	public void logExchangeDecorateLevel(String playerId, int termId, int addExp,int afterLevel,int afterExp){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logExchangeDecorateLevel failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logExchangeDecorateLevel(player, termId, addExp, afterLevel, afterExp);
	}	

	@Override
	public void logGhostSecretDrewResult(String playerId, int termId,int drewValue) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logGhostSecretDrewResult failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logGhostSecretDrewResult(player, termId, drewValue);
	}

	@Override
	public void logGhostSecretResetInfo(String playerId, int termId, int drewedTimes) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logGhostSecretResetInfo failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logGhostSecretResetInfo(player, termId, drewedTimes);
	}

	@Override
	public void logGhostSecretRewardInfo(String playerId,int termId, int rewardId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logGhostSecretRewardInfo failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logGhostSecretRewardInfo(player, termId, rewardId);
		
	}

	@Override
	public void logExchangeDecorateMission(String playerId, int termId, int missionId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logExchangeDecorateMission failed, sender null, sender: {}", playerId);
			return;
		}
		LogUtil.logExchangeDecorateMission(player, termId, missionId);
	}

	@Override
	public void logEnergiesSelfScore(String playerId, int termId, int addScore, int addType, int afterScore) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logEnergiesSelfScore failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logEnergiesSelfScore(player, termId, addScore, addType, afterScore);
	}

	@Override
	public void logEnergiesGuildScore(String guildId, int termId, int addScore, int addType, long afterScore) {
		LogUtil.logEnergiesGuildScore(guildId, termId, addScore, addType, afterScore);
	}

	@Override
	public void logEnergiesRank(int termId, int rankType, String rankerId, int rank, long score) {
		LogUtil.logEnergiesRank(termId, rankType, rankerId, rank, score);

	}


	@Override
	public void logVirtualLaboratoryOpenCard(String playerId, int termId, int cardIndex, int cardIndexTwo, int cardValue) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logVirtualLaboratoryOpenCard failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logVirtualLaboratoryOpenCard(player, termId, cardIndex, cardIndexTwo, cardValue);
	}	
	@Override
	public boolean checkCyborgWar(String playerId) {
		//TODO 配合装扮修复数据使用
		//指定12期检查赛博数据 
		CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(playerId, 12);
		if (cwPlayerData == null) {
			return false;
		}
		return cwPlayerData.getEnterTime() > 0?true:false;
	}

	@Override
	public PBDragonBoat.Builder getDragonBoatInfo(){
		WorldPoint point = WorldResTreasurePointService.getInstance().getDragonBoatPoint();
		if(point != null && point.getDragonBoatInfo()!= null){
			return point.getDragonBoatInfo().toBuilder();
		}
		return null;
	}

	@Override
	public HawkTuple2<Integer, Integer> getDragonBoatPos(){
		WorldPoint point = WorldResTreasurePointService.getInstance().getDragonBoatPoint();
		if(point == null){
			return null;
		}
		HawkTuple2<Integer, Integer> pos = new 
				HawkTuple2<Integer, Integer>(point.getX(),point.getY());
		return pos;
	}
	

	
	@Override
	public void logDragonBoatCelebrateionLevelReward(int termId, String guildId,int level,String players) {
		LogUtil.logDragonBoatCelebrateionLevelReward(termId, guildId, level, players);
	}
	
	@Override
	public void logDragonBoatCelebrationDonate(String playerId, int termId,String guildId, int donateType, int donateExp ,int totalExp) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logDragonBoatCelebrationDonate failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logDragonBoatCelebrationDonate(player, termId, guildId, donateType, donateExp,totalExp);
	}
	
	
	@Override
	public void logDragonBoatExchange(String playerId, int termId, int exchangeId, int exchangeCount) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logDragonBoatExchange failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logDragonBoatExchange(player, termId, exchangeId, exchangeCount);
	}
	
	@Override
	public void logDragonBoatGiftAchieve(String playerId, int termId, int type, long boatId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logDragonBoatGiftAchieve failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logDragonBoatGiftAchieve(player, termId, type, boatId);
	}
	
	@Override
	public void logDragonBoatLuckyBagOpen(String playerId, int termId, int openCount) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logDragonBoatLuckyBogOpen failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logDragonBoatLuckyBagOpen(player, termId, openCount);
	}
	
	
	@Override
	public void logDragonBoatRechargeDays(String playerId, int termId, int days) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln(" logDragonBoatRechargeDays failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logDragonBoatRechargeDays(player, termId, days);
	}

	@Override
	public void logMedalFundRewardScoreInfo(String playerId, int termId, int buyId, String scoreInfo, int type) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logMedalFundRewardInfo failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logMedalFundRewardScoreInfo(player, termId, buyId, scoreInfo, type);
	}

	@Override
	public int getPlayerTavernBoxScore(String playerId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("getPlayerTavernBoxScore failed, player is null, playerId: {}", playerId);
			return 0;
		}
		int score = TavernService.getInstance().getTavernBoxScore(playerId);
		return score;
	}

	@Override
	public StarWarsOfficerStruct getWorldKing() {
		StarWarsOfficerStruct worldKing = StarWarsOfficerService.getInstance().getWorldKing();
		return worldKing;
	}
	

	@Override
	public void logArmiesMassOpenSculpture(String playerId, int termId, int stage, int quality) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logArmiesMassOpenSculpture failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logArmiesMassOpenSculpture(player, termId, stage, quality);
		
	}
	

	@Override
	public void logSupersoldierInvestRewardScoreInfo(String playerId, int termId, int buyId, String scoreInfo) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logSupersoldierInvestRewardScoreInfo failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logSupersoldierInvestRewardScoreInfo(player, termId, buyId, scoreInfo);
		
	}
	@Override
	public void logEnergyInvestRewardScoreInfo(String playerId, int termId, int buyId, String scoreInfo) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logEnergyInvestRewardScoreInfo failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logEnergyInvestRewardScoreInfo(player, termId, buyId, scoreInfo);
		
	}

	@Override
	public void logOverlordBlessingInfo(String playerId, int termId, long blessRealNum, long blessWaterNum) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logOverlordBlessingInfo failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logOverlordBlessingInfo(player, termId, blessRealNum, blessWaterNum);
	}
	
	
	
	@Override
	public void logNewBuyOrderExp(String playerId, int termId, int expId, int exp) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logNewBuyOrderExp failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logNewBuyOrderExp(player, termId, expId, exp);
	}
	
	@Override
	public void logNewBuyOrderAuth(String playerId, int termId, int authId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logNewBuyOrderAuth failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logNewBuyOrderAuth(player, termId, authId);
	}
	
	@Override
	public void logNewOrderExpChange(String playerId, int termId, int expAdd, int totalExp,int exp, int level, int reason, int reasonId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logNewOrderExpChange failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logNewOrderExpChange(player, termId, expAdd, totalExp, exp, level, reason, reasonId);
	}
	
	@Override
	public void logNewOrderFinishId(String playerId, int termId, int orderId, int addTimes, int finishTimes){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logNewOrderFinishId failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logNewOrderFinishId(player, termId, orderId, addTimes, finishTimes);
	}

	@Override
	public void logSeasonBuyOrderAuth(String playerId, int termId, int authId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logSeasonBuyOrderAuth failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logSeasonBuyOrderAuth(player, termId, authId);
	}

	@Override
	public void logSeasonOrderExpChange(String playerId, int termId, int expAdd, int exp,int oldLevel, int level, int reason, int reasonId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logSeasonOrderExpChange failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logSeasonOrderExpChange(player, termId, expAdd, exp, oldLevel, level, reason, reasonId);
	}

	@Override
	public void logSeasonOrderFinishId(String playerId, int termId, int orderId, int addTimes, int finishTimes){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logSeasonOrderFinishId failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logSeasonOrderFinishId(player, termId, orderId, addTimes, finishTimes);
	}


	@Override
	public void logStarLightSignAward(String playerId, List<Reward.RewardItem.Builder> awardList, int reason) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logStarLightSignAward failed, player is null, playerId: {}", playerId);
			return;
		}
		List<ItemInfo> rewardList = new ArrayList<>();
		for(Reward.RewardItem.Builder rewardItem : awardList){
			rewardList.add(new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(), (int) rewardItem.getItemCount()));
		}
		LogUtil.logStarLightSignAward(player, ItemInfo.toString(rewardList), reason);
	}

	@Override
	public void logStarLightSignScore(String playerId, int before, int after, int add) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logStarLightSignScore failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logStarLightSignScore(player, before, after, add);
	}

	@Override
	public void logStarLightSignChoose(String playerId, int type, int rechargeType, int choose) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("logStarLightSignChoose failed, player is null, playerId: {}", playerId);
			return;
		}
		LogUtil.logStarLightSignChoose(player, type, rechargeType, choose);
	}

	@Override
	public int getCenterFlagCount(String guildId) {
		return FlagCollection.getInstance().getCenterFlagCount(guildId);
	}
	
	@Override
	public int getCenterFlagPlaceCount(String guildId) {
		return FlagCollection.getInstance().getCenterFlagPlaceCount(guildId);
	}



	@Override
	public void logDoubleGiftBuy(String playerId, int termId, int giftId, int rewardId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logDoubleGiftBuy(player, termId, giftId, rewardId);
		}
		
	}

	@Override
	public void logGroupBuy(String playerId, int termId, int giftId, int rewardId, long realTimes, long waterTimes) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logGroupBuy(player, termId, giftId, rewardId, realTimes, waterTimes);
		}
	}
	@Override
	public PBCakeShare.Builder getCakeShareInfo() {
		WorldPoint point = WorldResTreasurePointService.getInstance().getCakeSharePoint();
		if(point != null && point.getCakeShareInfo()!= null){
			return point.getCakeShareInfo().toBuilder();
		}
		return null;
	}

	@Override
	public HawkTuple2<Integer, Integer> getCakeSharePos() {
		WorldPoint point = WorldResTreasurePointService.getInstance().getCakeSharePoint();
		if(point == null){
			return null;
		}
		HawkTuple2<Integer, Integer> pos = new 
				HawkTuple2<Integer, Integer>(point.getX(),point.getY());
		return pos;
	}
		
	
	
	@Override
	public void logOrdnanceFortressOpen(String playerId, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logOrdnanceFortressOpen(player, termId, stage, openId, count, cost, rewardType, rewardId);
		}
	}
	
	
	@Override
	public void logOrdnanceFortressAdvance(String playerId,int termId,  int fromStage, int toStage) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logOrdnanceFortressAdvance(player, termId, fromStage, toStage);
		}
	}
	@Override
	public void updatePlayerFireWorks(String playerId, int type, long duration) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			WorldPointService.getInstance().updateFireWorks(playerId, type, duration);
		}
	}

	@Override
	public void logFireWorksForBuffActive(String playerId, int termId, int buffId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logFireWorksForBuffActive(player, termId, buffId) ;
		}
	}

	@Override
	public void logCelebrationFoodMake(String playerId, int termId, int level) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logCelebrationFoodMake(player, termId,level) ;
		}
	}
	
	/**
	 * 装备工匠词条变动
	 * @param playerId
	 * @param cfgId 装备词条配置id
	 * @param armourAddCfgId 装备属性配置id
	 * @param effectType 作用号类型
	 * @param effectValue 作用号值
	 * @param reason 变动原因 1 获取 2 放弃 3 被传承
	 * @param inheritArmourCfgId 传承的装备配置id
	 */
	@Override
	public void logEquipCarftsmanAttr(String playerId, int cfgId, int armourAddCfgId, int effectType, int effectValue, int reason, int inheritArmourCfgId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return;
		}
		LogUtil.logEquipCarftsmanAttr(player, cfgId, armourAddCfgId, effectType, effectValue, reason, inheritArmourCfgId);
	}

	@Override
	public void logRedPackageOpen(String playerId, int termId, int stage, int score) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logRedPackageOpen(player, termId, stage, score);
		}
	}

	@Override
	public void logActivityAchieve(String playerId, int activityId, int termId, int achieveId, int achieveState,
			String achieveData) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logActivityAchieve(player, activityId, termId, achieveId, achieveState, achieveData);
		}
	}

	@Override
	public void logBattleFieldBuyGift(String playerId, int termId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logBattleFieldBuyGift(player, termId);
		}
	}

	@Override
	public void logBattleFieldDice(String playerId, int termId, boolean add, int diceType, int count, int afterCount) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logBattleFieldDice(player, termId, add, diceType, count, afterCount);
		}
	}

	@Override
	public void logBattleFieldDiceReward(String playerId, int termId, int awardType, int awardId, int cellId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logBattleFieldDiceReward(player, termId, awardType, awardId, cellId);
		}
	}


	@Override
	public void logArmamentExchangeFirst(String playerId, int termId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logArmamentExchangeFirst(player, termId);
		}		
	}

	@Override
	public void logBuyOrderEquipExp(String playerId, int termId, int cycle, int expId, int exp) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logBuyOrderEquipExp(player, termId, cycle, expId, exp);
		}
		
	}
	@Override
	public void logBuyOrderEquipAuth(String playerId, int termId, int cycle, int authId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logBuyOrderEquipAuth(player, termId, cycle, authId);
		}
		
	}
	
	
	@Override
	public void logOrderEquipExpChange(String playerId, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logOrderEquipExpChange(player, termId, cycle, expAdd, exp, level, reason, reasonId);
		}
		
	}
	
	@Override
	public void logOrderEquipFinishId(String playerId, int termId, int cycle, int orderId, int addTimes, int finishTimes) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logOrderEquipFinishId(player, termId, cycle, orderId, addTimes, finishTimes);
		}
		
	}

	@Override
	public int checkMonthCardPriceCut(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			return player.checkMonthCardPriceCut();
		}
		
		return -1;
	}

	@Override
	public void logAllianceCelebrateScore(String playerId, String guildId, int termId, int addScore, int afterPlayerScore,int afterGuildScore) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceCelebrateScore(player, guildId, termId, addScore, afterPlayerScore, afterGuildScore);
		}
	}

	@Override
	public void logAllianceCelebrateReward(String playerId, int termId, int level, int index) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceCelebrateReward(player, termId, level, index);
		}
	}

	@Override
	public void logResourceDefenseSkillRefreshAndActive(String playerId, int termId, int type, String skillInfo) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logResourceDefenseSkillRefreshAndActive(player, termId, type, skillInfo);
		}
	}

	@Override
	public void logResourceDefenseAgentSkillEffect(String playerId, int termId, int skillId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logResourceDefenseAgentSkillEffect(player, termId, skillId);
		}
	}

	@Override
	public List<Integer> getVoucherItemUserParams(int id){
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, id);
		if(itemCfg == null){
			return null;
		}
		List<Integer> voucherList = SerializeHelper.cfgStr2List(itemCfg.getVoucherUse());
		return voucherList;
	}
	
	
	@Override
	public long getVoucherItemLimitPrice(int id,int type){
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, id);
		if(itemCfg == null){
			return 0;
		}
		return itemCfg.getVoucherLimitByType(type);
	}
	
	@Override
	public long getVoucherEndTime(int id){
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, id);
		if(itemCfg == null){
			return 0;
		}
		return HawkTime.parseTime(itemCfg.getVoucherTime());
	}
	
	@Override
	public long getVoucherValue(int id){
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, id);
		if(itemCfg == null){
			return 0;
		}
		return itemCfg.getNum();
	}

	@Override
	public String getDYZZBattleInfo(String playerId) {
		DYZZSeasonBattleInfo battleInfo = DYZZSeasonRedisData.getInstance().getDYZZSeasonBattle(playerId);
		if(battleInfo == null){
			return null;
		}
		return battleInfo.serializ();
	}

	@Override
	public void delDYZZBattleInfo(String playerId) {
		DYZZSeasonRedisData.getInstance().delDYZZSeasonBattle(playerId);
	}

	@Override
	public void logSuperDiscountDraw(String playerId, int termId, int refreshType, int cfgId,int poolId, String discount){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logSuperDiscountDraw(player,termId, refreshType, cfgId,poolId, discount) ;
		}		
	}
	

	
	@Override
	public void logSuperDiscountBuy(String playerId, int termId,String goods, String price, int num, String discount, int goodsId,int voucherId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logSuperDiscountBuy(player, termId,goods, price, num, discount, goodsId,voucherId) ;
		}		
	}
	
	@Override
	public void logPlayerGlobalSign(String playerId,int termId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(null != player){
			LogUtil.logPlayerGlobalSign(player, termId);
		}		
	}
	
	@Override
	public void logReturnPuzzleScore(String playerId, int termId,int score) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logReturnPuzzleScore(player, termId,score);
		}
	}

	@Override
	public void logFireReigniteReceiveBox(String playerId, int termId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logFireReigniteReceiveBox(player, termId);
		}
	}

	@Override
	public void logAccountInherit(String playerId, int termId, String oldPlayerId, String oldServerId, long sumGold,
			long rebetGold, long sumVipExp, long rebetExp) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logAccountInherit(player, termId, oldPlayerId, oldServerId, sumGold, rebetGold, sumVipExp, rebetExp);
		}
	}
	


	@Override
	public void logPlantFortressOpen(String playerId, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logPlantFortressOpen(player, termId, stage, openId, count, cost, rewardType, rewardId);
		}
	}
	
	
	@Override
	public void logPlantFortressAdvance(String playerId,int termId,  int fromStage, int toStage) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logPlantFortressAdvance(player, termId, fromStage, toStage);
		}
	}

	@Override
	public void logFireReigniteReceiveBoxTwo(String playerId, int termId, int boxId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logFireReigniteReceiveBoxTwo(player, termId, boxId);
		}
	}


	@Override
	public void logMilitaryPrepareAdvancedReward(String playerId, int termId, int rewardId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logMilitaryPrepareAdvancedReward(player, termId, rewardId);
		}
	}
	

	@Override
	public List<RewardItem> getShowReward(List<String> itemStrs) {
		List<ItemInfo> items = new ArrayList<>();
		for (String itemStr : itemStrs) {
			items.addAll(ItemInfo.valueListOf(itemStr));
		}
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(items);
		return awardItems.getShowItems();
	}

	@Override
	public void logPeakHonourScore(String playerId, String guildId, int getType, long addScore, long afterScore, int matchId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logPeakHonourScore(player, guildId, getType, addScore, afterScore, matchId);
		}
	}

	@Override
	public void logTimeLimitBuy(String playerId, int goodsId, int success) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logTimeLimitBuy(player, goodsId, success);
		}
	}

	@Override
	public void logTimeLimitBuyWater(int goodsId, int addCount) {
		LogUtil.logTimeLimitBuyWater(goodsId, addCount);
	}
	
	@Override
	public AccountRoleInfo getAccountRole(String serverId, String platform, String openId) {
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(openId);
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			if (!platform.equals(roleInfoObj.getPlatform())) {
				continue;
			}
			
			if (roleInfoObj.getServerId().equals(serverId)) {
				return roleInfoObj;
			}
			
			if (GlobalData.getInstance().getMainServerId(roleInfoObj.getServerId()).equals(serverId)) {
				return roleInfoObj;
			}
		}
		
		return null;
	}
	
	public List<AccountRoleInfo> getAccountRoleList(String openId) {
		List<AccountRoleInfo> list = new ArrayList<>();
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(openId);
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			list.add(roleInfoObj);
		}
		return list;
	}

	@Override
	public void logChristmasRechargeDiamond(String playerId, int termId, int rechargeDiamond, int totalRechargeDiamond) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logChristmasRechargeDiamond(player, termId, rechargeDiamond, totalRechargeDiamond);
		}
	}

	/**
	 * 当前是否为测试环境
	 * @return
	 */
	public boolean isServerDebug(){
		if (GsConfig.getInstance().isDebug() && HawkOSOperator.isWindowsOS()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isProprietaryServer() {
		String result = RedisProxy.getInstance().getServerProprietary(GsConfig.getInstance().getServerId());
		return !HawkOSOperator.isEmptyString(result) && result.equals("1");
	}
	
	/**
	 * 获取当前生效作用号值
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int effectTodayUsedTimes(String playerId,EffType effId){
		return RedisProxy.getInstance().effectTodayUsedTimes(playerId, effId);
	}

	/**
	 * 获取军事备战直购礼包名字信息
	 * @param giftId
	 * @return
	 */
	public String getMilitaryPrepareGiftName(String giftId){
		PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if(cfg != null){
			return cfg.getNameData();
		}
		return null;
	}


		
	/**
	 * 雄心壮志活动抽取宝箱
	 * @param playerId
	 * @param termId
	 * @param boxCount
	 */
	public void logCoreplateBox(String playerId, int termId, int boxCount) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logCoreplateBox(player, termId, boxCount);
		}
	}



	@Override
	public void buyLoginFundTwoRecord(String playerId, int termId,  int type){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("buy loginFund record failed, player null, playerId: {}", playerId);
			return;
		}

		LogUtil.logBuyLoginFundTwoFlow(player, termId, type);
	}


	@Override
	public void logHongFuGiftUnlock(String playerId, int termId, int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHongFuGiftUnlockFlow(player, termId, giftId);
		}
	}

	@Override
	public void logHongFuGiftRecReward(String playerId, int termId, int giftId, int dayCount, int chooseRewardId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHongFuGiftRecRewardFlow(player, termId, giftId, dayCount, chooseRewardId);
		}
	}

	@Override
	public void logRedbludTicketFlow(String playerId, int termId, int operType, int pool, int ticketId, int rewardId,
			int refreshTimes) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logRedbludTicketFlow(player, termId, operType, pool, ticketId, rewardId, refreshTimes);
		}
	}
	
	@Override
	public void logDressTreasureRandom(String playerId,int termId,int randomFirst,int randomSecond,
			int awardNumStart,int awardNumEnd,int randomId,int awardId,String cost){
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logDressTreasureRandom(player, termId, randomFirst, randomSecond, 
					awardNumStart, awardNumEnd, randomId, awardId,cost);
		}
	}
	
	@Override
	public void logDressTreasureRest(String playerId,int termId,int randomId, int awardNumStart, int awardNumEnd){
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logDressTreasureRest(player, termId, randomId, awardNumStart, awardNumEnd);
		}
	}


	
	@Override
	public boolean xzqOpen(){
		return XZQConstCfg.getInstance().isOpen();
	}
	
	@Override
	public boolean checkPrestressinLossActivityOpen(String playerId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			return false;
		}
		
		String redisVal = RedisProxy.getInstance().getRedisSession().getString(getPrestressingLossResultKey(playerId));
		if (!HawkOSOperator.isEmptyString(redisVal)) {
			return Integer.parseInt(redisVal) > 0;
		}
		
		HttpClient httpClient = HawkHttpUrlService.getInstance().getHttpClient();
		if (httpClient == null || !httpClient.isRunning()) {
			RedisProxy.getInstance().getRedisSession().setString(getPrestressingLossResultKey(playerId), "0", 120);
			return false;
		}
		
		boolean result = false;
		try {
			PrestressingLossKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
			String requestResult = cfg.getTestData();
			if (HawkOSOperator.isEmptyString(requestResult)) {
				requestResult = l5Req(player, cfg);
				if (requestResult == null) {
				    RedisProxy.getInstance().getRedisSession().setString(getPrestressingLossResultKey(playerId), "0", 120);
					return false;
				}
			}
			
			JSONObject json = JSONObject.parseObject(requestResult);
			if (json == null || json.getIntValue("error_code") != 0) {
				RedisProxy.getInstance().getRedisSession().setString(getPrestressingLossResultKey(playerId), "0", 120);
				HawkLog.debugPrintln("PrestressinLossActivity fetch data failed, playerId: {}, openid: {}, data: {}", player.getId(), player.getOpenId(), requestResult);
				return false;
			}
			
			/**
			 * 1. i1='0'对照组，i1='1'测试组(实验组)；
			 * 2. v3为流失概率，但是为了避免科学计数法，由原来0-1的小数改为0-10000的数字，使用的时候要除以10000
			 * 3. 只有是否实验组=true且流失概率 > 0.7 才会满足接口触发条件
			 */
			JSONObject dataObj = json.getJSONObject("result").getJSONObject("data");
			int i1Val = dataObj.containsKey("i1") ? dataObj.getIntValue("i1") : 0;
			float v3Val = dataObj.containsKey("v3") ? dataObj.getFloatValue("v3") : 0f;
			String roleid = dataObj.getString("v2");
			
			result = playerId.equals(roleid) && i1Val == 1 && v3Val >= 7000;
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		RedisProxy.getInstance().getRedisSession().setString(getPrestressingLossResultKey(playerId), result ? "1" : "0", 120);
		return result;
	}
	
	private String getPrestressingLossResultKey(String playerId) {
		return "prestressingLossResult:" + playerId; 
	}
	
	private String l5Req(Player player, PrestressingLossKVCfg cfg) {
		if (GameUtil.isWin32Platform(player)) {
			return null;
		}
		
		String time = HawkTime.formatTime(HawkTime.getMillisecond() - HawkTime.DAY_MILLI_SECONDS, "yyyyMMdd");
		if (!cfg.isL5()) {
			try {
				String url = cfg.getAddr().replace("{0}", time).replace("{1}", player.getOpenId());
				ContentResponse resp = HawkHttpUrlService.getInstance().doGet(url, 500);
				if (resp != null) {
					return resp.getContentAsString();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			HawkLog.errPrintln("PrestressinLossActivity fetch data failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			return null;
		}
		
		HawkTuple3<Integer, String, Object> retInfo = L5Helper.l5Task(cfg.getL5_modId(), cfg.getL5_cmdId(), 500, new L5Task() {
			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {
					String subAttr = cfg.getSubAttr().replace("{0}", time).replace("{1}", player.getOpenId());
					if (!host.endsWith("/")) {
						host += "/";
					}
					
					String url = String.format("http://%s%s", host, subAttr);
					ContentResponse response = HawkHttpUrlService.getInstance().doGet(url, 500);
					return new HawkTuple2<Integer, Object>(0, response);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return new HawkTuple2<Integer, Object>(-1, null);
			}
		});
		
		if (retInfo.third != null) {
			ContentResponse resp = (ContentResponse)retInfo.third;
			return resp.getContentAsString();
		}
		
		HawkLog.errPrintln("PrestressinLossActivity fetch data by l5 failed, playerId: {}, openid: {}, host: {}", player.getId(), player.getOpenId(), retInfo != null ? retInfo.second : "empty");
		return null;
	}


	/**
	 * 异步
	 * 登陆时触发
	 * 调用腾讯的接口判断玩家是否可以激活活动
	 * @param playerId
	 */
	@Override
	public void checkHeavenBlessingActivityOpen(String playerId) {
		HawkLog.errPrintln("HeavenBlessingActivity 1, playerId: {}", playerId);
		//获得玩家数据
		Player player =  this.getPlayer(playerId);
		//如果玩家数据不存在则不执行后续逻辑
		if (player == null) {
			HawkLog.errPrintln("HeavenBlessingActivity player is null, playerId: {}", playerId);
			return;
		}
		long now = HawkTime.getMillisecond();
		//活动数据基础配置
		HeavenBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeavenBlessingKVCfg.class);
		if(now - player.getCreateTime() < cfg.getRegisterDays()){
			HawkLog.errPrintln("HeavenBlessingActivity player is too new, playerId: {}", playerId);
			return;
		}
		if(player.getCityLevel() < cfg.getBuildingLevel()){
			HawkLog.errPrintln("HeavenBlessingActivity player is low, playerId: {}", playerId);
			return;
		}
		//防止阻塞异步请求腾讯l5
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				PayPuidCtrl payPuidCtrl = HawkConfigManager.getInstance().getConfigByKey(PayPuidCtrl.class, player.getOpenId());
				if(payPuidCtrl != null){
					ActivityManager.getInstance().postEvent(new HeavenBlessingActiveEvent(player.getId(), payPuidCtrl.getVip(), payPuidCtrl.getMoney()));
					return null;
				}
				//测试数据,测试环境下使用
				String requestResult = cfg.getTestData();
				//如果配有测试数据就不请求l5了
				if (HawkOSOperator.isEmptyString(requestResult)) {
					//请求l5获得返回结果
					requestResult = heavenBlessingL5Req(player, cfg);
					//返回结果为空就直接返回，不执行后续逻辑
					if (requestResult == null) {
						return null;
					}
				}else {
					String tmp = RedisProxy.getInstance().getRedisSession().hGet("HEAVEN_BLESSING_TEST", playerId);
					if(!HawkOSOperator.isEmptyString(tmp)){
						requestResult = tmp;
					}
				}
				//把返回结果转换成jsonObject
				JSONObject json = JSONObject.parseObject(requestResult);
				//json为空或者返回结果不为0直接返回，不执行后续逻辑
				if (json == null || json.getIntValue("error_code") != 0) {
					/**
					 * 输出错误日志
					 * 102：模版配置错误
					 * 103：模版匹配错误
					 * 104：找不到对应的reidis数据源信息
					 * 105：访问redis，返回结果为NULL
					 * 106：访问redis异常(超时等)
					 * 500：其他错误
					 */
					HawkLog.errPrintln("HeavenBlessingActivity fetch data failed, playerId: {}, openid: {}, data: {}", player.getId(), player.getOpenId(), requestResult);
					return null;
				}
				JSONObject dataObj = json.getJSONObject("result").getJSONObject("data");
				int ifEffect = dataObj.getIntValue("if_effect");
				if(ifEffect != 1){
					HawkLog.errPrintln("HeavenBlessingActivity fetch data effect failed, playerId: {}, openid: {}, data: {}", player.getId(), player.getOpenId(), requestResult);
					return null;
				}
				//获得vip等级
				int vip = dataObj.getIntValue("viplevel");
				//获得近期充值金额
				int money = dataObj.getIntValue("imoney");
				//活动激活事件
				ActivityManager.getInstance().postEvent(new HeavenBlessingActiveEvent(player.getId(), vip, money));
				return null;
			}
		});
	}


	/**
	 * 洪福天降腾讯l5相关请求
	 * @param player
	 * @param cfg
	 * @return
	 */
	public String heavenBlessingL5Req(Player player, HeavenBlessingKVCfg cfg){
		HawkLog.errPrintln("HeavenBlessingActivity 2, playerId: {}", player.getId());
		if (GameUtil.isWin32Platform(player)) {
			HawkLog.errPrintln("HeavenBlessingActivity player is win32, playerId: {}", player.getId());
			return null;
		}
		HawkLog.errPrintln("HeavenBlessingActivity 3, playerId: {}", player.getId());
		//直接请求外部链接的两种情况，1，没有配l5,2,内网本机windows环境
		if (!cfg.isL5Req()) {
			try {
				//替换配置中的参数
				String url = cfg.getAddr().replace("{0}", player.getId());
				//请求外部链接，500ms超时
				//外部链接目前只在品管服有效果，所以内部测试暂时使用策划配置测试数据的方式
				ContentResponse resp = HawkHttpUrlService.getInstance().doGet(url, 500);
				//返回请求结果
				if (resp != null) {
					return resp.getContentAsString();
				}
			} catch (Exception e) {
				//记录可能产生的网络请求报错
				HawkException.catchException(e);
			}
			//打印错误日志
			HawkLog.errPrintln("HeavenBlessingActivity fetch data failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			//返回请求失败的结果
			return null;
		}
		HawkLog.errPrintln("HeavenBlessingActivity 4, playerId: {}", player.getId());
		//请求l5，通过modid,cmdid获得内部链接请求超时500ms
		HawkTuple3<Integer, String, Object> retInfo = L5Helper.l5Task(cfg.getL5_modId(), cfg.getL5_cmdId(), 500, new L5Task() {
			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {
					HawkLog.errPrintln("HeavenBlessingActivity l5 1, playerId: {}", player.getId());
					//替换配置中的参数
					String subAttr = cfg.getSubAddr().replace("{0}", player.getId());
					//检查链接格式
					if (!host.endsWith("/")) {
						host += "/";
					}
					//获得完整链接
					String url = String.format("http://%s%s", host, subAttr);
					HawkLog.errPrintln("HeavenBlessingActivity l5 2, playerId: {}, url: {}", player.getId(), url);
					//请求l5链接，500ms超时
					ContentResponse response = HawkHttpUrlService.getInstance().doGet(url, 500);
					//返回请求结果
					return new HawkTuple2<Integer, Object>(0, response);
				} catch (Exception e) {
					//记录可能产生的网络请求报错
					HawkException.catchException(e);
				}
				//打印错误日志
				HawkLog.errPrintln("HeavenBlessingActivity l5 fetch data failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
				//返回请求失败的结果
				return new HawkTuple2<Integer, Object>(-1, null);
			}
		});

		//如果请求成功且有返回值，则返回请求结果
		if (retInfo.third != null) {
			ContentResponse resp = (ContentResponse)retInfo.third;
			return resp.getContentAsString();
		}
		//记录错误日志
		HawkLog.errPrintln("HeavenBlessingActivity fetch data by l5 failed, playerId: {}, openid: {}, host: {}", player.getId(), player.getOpenId(), retInfo != null ? retInfo.second : "empty");
		//返回请求失败的结果
		return null;
	}


	@Override
	public void logLuckyBoxRandom(String playerId,int termId,String group,int randomCount, int cellId, int rewardId,int canSelect,int finish){
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logLuckyBoxRandom(player, termId, group, randomCount, cellId, rewardId,canSelect,finish);
		}
	}

	@Override
	public void logPlantSecret(String playerId, int termId, int operType, int openBoxCount, int openCardTime, int buyItemCount,
			int openBoxTimes, boolean success, int serverNum, int clientNum) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logPlantSecret(player, termId, operType, openBoxCount, openCardTime, buyItemCount, openBoxTimes, success, serverNum, clientNum);
		}
	}

	@Override
	public void logPrestressingLoss(String playerId, int openTerm) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logPrestressingLoss(player, openTerm);
		}
	}

	@Override
	public boolean immgrationBackFlowCheck(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		// 是否触发回流
		PlayerImmgrationModule module = player.getModule(GsConst.ModuleType.IMMGRATION);
		if (!module.checkOpenBackFlow()) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 盟军祝福活动签到
	 * @param player
	 * @param termId 期数
	 * @param openPos 开放数字位置
	 * @param openNum 开放数字之
	 */
	public void logAllianceWishSign(String playerId,int termId,int signType, int openPos, int openNum) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceWishSign(player, termId, signType,openPos, openNum);
		}
	}
	
	
	/**
	 * 盟军祝福活动帮助
	 * @param player
	 * @param termId 期数
	 * @param guildMember  联盟玩家ID
	 */
	public void logAllianceWishHelp(String playerId,int termId, String guildMember) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceWishHelp(player, termId, guildMember);
		}
	}

	@Override
	public void logHeavenBlessingPay(String playerId, int groupId, int level, int payCount, int choose) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHeavenBlessingPay(player, groupId, level, payCount, choose);
		}
	}

	@Override
	public void logHeavenBlessingActive(String playerId, int groupId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHeavenBlessingActive(player, groupId);
		}
	}

	@Override
	public void logHeavenBlessingAward(String playerId, int groupId, int level, int payCount, int choose) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHeavenBlessingAward(player, groupId, level, payCount, choose);
		}
	}

	@Override
	public void logHeavenBlessingRandomAward(String playerId, Reward.RewardItem.Builder item) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			ItemInfo itemInfo = new ItemInfo(item.getItemType(),item.getItemId(),item.getItemCount());
			LogUtil.logHeavenBlessingRandomAward(player, itemInfo.toString());
		}
	}

	@Override
	public void logHeavenBlessingOpen(String playerId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHeavenBlessingOpen(player);
		}
	}

	/**
	 * 活动通用记录打点
	 * @param playerId 玩家
	 * @param param 参数
	 */
	@Override
	public void logActivityCommon(String playerId, LogInfoType logInfoType, Map<String, Object> param) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logActivityCommon(player, logInfoType, param);
		}
	}
	
	/**
	 * 活动通用记录打点
	 */
	public void logActivityCommon(LogInfoType logInfoType, Map<String, Object> param) {
		LogUtil.logActivityCommon(logInfoType, param);
	}

	@Override
	public void logGrateBenefitsAward(String playerId, int punchCount, int createDays, int help, int gold) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrateBenefitsAward(player, punchCount, createDays, help, gold);
		}
	}

	@Override
	public void logHonorRepayBuy(String playerId, int termId, int num) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHonorRepayBuy(player, termId, num);
		}

	}

	@Override
	public void logDYZZAchieveReach(String playerId, int achieveId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logDYZZAchieveReach(player, achieveId);
		}
	}

	@Override
	public void logDYZZAchieveTake(String playerId, int achieveId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		int score = 0;
		int count = 0;
		DYZZSeasonPlayerData seasonData = DYZZSeasonService.getInstance()
				.getDYZZSeasonPlayerData(playerId);
		if(seasonData!=null){
			score = seasonData.getScore();
			count = seasonData.getBattleCount();
		}
		if (player != null) {
			LogUtil.logDYZZAchieveTake(player, score, count, achieveId);
		}
	}

	@Override
	public void logHonorRepayReceiveReward(String playerId, int termId, int buyTimes, int type) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			LogUtil.logHonorRepayReceiveReward(player, termId, buyTimes, type);
		}
	}


	@Override
	public Set<Tuple> getRankList(Rank.RankType rankType, int maxCount) {
		return LocalRedis.getInstance().getRankList(rankType, maxCount);
	}

	@Override
	public List<RankInfo> getRankCache(Rank.RankType rankType, int maxCount) {
		return RankService.getInstance().getRankCache(rankType, maxCount);
	}

	@Override
	public int returnBuildingLvUp(String playerId, int level){
		try {
			BuildingCfg toCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, level);
			if(toCfg == null){
				return -3;
			}
			if(getConstructionFactoryCfgId(playerId) >= level){
				return -5;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				return -1;
			}
			if (player.getData().getQueueEntitiesByType(Const.QueueType.BUILDING_QUEUE_VALUE).size() > 0) {
				return -4;
			}
			unlockArea(player);
			for (int buildType : getBuildTypeList()) {
				// 这个时只在前端显示的假建筑，后端屏蔽不处理
				if (buildType == 2213) {
					continue;
				}
				//todo 先这么写看看效果，后面再和策划对
				if(buildType == 2233){
					continue;
				}
				if(buildType == 2234){
					continue;
				}
				if(buildType == 2235){
					continue;
				}
				if(buildType == 2236){
					continue;
				}
				if(buildType == 2237){
					continue;
				}
				BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
				if (buildingEntity == null && !BuildAreaCfg.isShareBlockBuildType(buildType)) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, Building.BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
					if (buildType == BuildingType.RADAR_VALUE) {
						try {
							PlayerAgencyModule module = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
							module.initData();
						} catch (Exception e) {
						}
					}
				}
				buildUpgrade(player, buildingEntity, toCfg.getLevel(), toCfg.getProgress());
			}
			for (BuildingBaseEntity entity : player.getData().getBuildingEntities()) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
				if (buildingCfg == null) {
					continue;
				}
				buildUpgrade(player, entity, toCfg.getLevel(), toCfg.getProgress());
			}
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			return 1;
		} catch (Exception e) {
			HawkException.catchException(e);
			return -2;
		}
	}

	/**
	 * 升级建筑
	 *
	 * @param player
	 * @param buildingEntity
	 */
	private void buildUpgrade(Player player, BuildingBaseEntity buildingEntity,int honorLevel, int progress) {
		buildUpgradeLevel(player, buildingEntity, honorLevel);
		buildUpgradeProgress(player, buildingEntity, honorLevel, progress);
	}

	private void buildUpgradeLevel(Player player, BuildingBaseEntity buildingEntity,int honorLevel) {
		// 建筑满级
		BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		while (buildingCfg != null && buildingCfg.getLevel() < honorLevel && buildingCfg.getBuildType() == buildingEntity.getType()) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, Building.BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		}
	}

	private void buildUpgradeProgress(Player player, BuildingBaseEntity buildingEntity,int honorLevel, int progress) {
		// 建筑满级
		BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		while (buildingCfg != null && buildingCfg.getLevel() <= honorLevel && buildingCfg.getProgress() <= progress && buildingCfg.getBuildType() == buildingEntity.getType()) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, Building.BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		}
	}

	/**
	 * 根据建筑cfgId获取建筑实体
	 * @param id
	 */
	public BuildingBaseEntity getBuildingBaseEntity(Player player, int buildingType) {
		Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
				.filter(e -> e.getStatus() != Const.BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getType() == buildingType)
				.findAny();
		if(op.isPresent()) {
			return op.get();
		}
		return null;
	}


	/**
	 * 获取需要升级至满级的建筑列表
	 * @return
	 */
	private List<Integer> getBuildTypeList() {
		List<Integer> retList = new ArrayList<>();

		ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildCfgIterator.hasNext()) {
			BuildingCfg buildCfg = buildCfgIterator.next();
			if (buildCfg.getLevel() > 1) {
				continue;
			}
			BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
			if (cfg == null || cfg.getLimit(30) > 1) {
				continue;
			}
			retList.add(buildCfg.getBuildType());
		}
		return retList;
	}

	/**
	 * 解锁地块
	 * @param player
	 */
	private void unlockArea(Player player) {
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);

			List<Integer> areaList = new ArrayList<Integer>();
			while (iterator.hasNext()) {
				BuildAreaCfg areaCfg = iterator.next();
				int areaId = areaCfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}

				areaList.add(areaId);
			}

			areaList.stream().forEach(e -> {
				player.unlockArea(e);
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(e));
				MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SuperSoldier.SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
				// 解锁地块任务
				BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, BehaviorLogger.Params.valueOf("buildAreaId", e));
			});

			player.getPush().synUnlockedArea();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public int returnTechUp(String playerId, int level){
		try {
			BuildingCfg toCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, level);
			if(toCfg == null){
				return -3;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				return -1;
			}
			if (player.getData().getQueueEntitiesByType(Const.QueueType.SCIENCE_QUEUE_VALUE).size() > 0) {
				return -4;
			}
			for(TechnologyCfg cfg : TechnologyCfg.getTechMap(toCfg.getLevel()).values()){
				techLevelUp(player, cfg);
			}
			return 1;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return -2;
	}
	/**
	 * 科技升级
	 * @param techId
	 * @return
	 */
	private boolean techLevelUp(Player player, TechnologyCfg cfg) {
		int techId = cfg.getTechId();
		TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
		if (entity == null) {
			entity = player.getData().createTechnologyEntity(cfg);
		}

		player.getData().getPlayerEffect().addEffectTech(player, entity);
		entity.setLevel(cfg.getLevel());
		entity.setResearching(false);
		player.getPush().syncTechnologyLevelUpFinish(entity.getCfgId());
		player.refreshPowerElectric(PowerChangeReason.TECH_LVUP);

		// 如果科技解锁技能,则推送科技技能信息
		if (cfg.getTechSkill() > 0) {
			player.getPush().syncTechSkillInfo();
		}

		return true;
	}

	@Override
	public int returnRoleUp(String playerId, int level) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return -1;
		}
		if(player.getLevel() >= level){
			return -4;
		}
		while (player.getLevel() < level){
			PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, player.getLevel() + 1);
			int exp = cfg.getExp() - player.getExp() + 1;
			HawkLog.logPrintln("returnRoleUp, cfg:{}, player:{}", cfg.getExp(), player.getExp());
			if(exp <= 0){
				return -2;
			}
			AwardItems awardItems = AwardItems.valueOf("10000_1004_"+exp);
			awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
		}
		return 0;
	}

	@Override
	public boolean returnUpgradeCheck(String playerId, Activity.ReturnUpgradeType type) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return false;
		}
		switch (type){
			case REUP_BASE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.BUILDING_QUEUE_VALUE).size() > 0) {
					return false;
				}
			}
			break;
			case REUP_TECH:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.SCIENCE_QUEUE_VALUE).size() > 0) {
					return false;
				}
			}
			break;
		}
		return true;
	}

	@Override
	public Map<Integer, Integer> getTechTypeMap(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return new ConcurrentHashMap<>();
		}
		Map<Integer, Integer> typeMap = new ConcurrentHashMap<>();
		for(TechnologyEntity entity : player.getData().getTechnologyEntities()){
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
			if(cfg == null){
				continue;
			}
			int cur = typeMap.getOrDefault(cfg.getTechType(), 0);
			cur += cfg.getLevel();
			typeMap.put(cfg.getTechType(), cur);
		}
		return typeMap;
	}

	@Override
	public Map<Integer, Integer> getTechLevelTypeMap(int level) {
		BuildingCfg toCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, level);
		if(toCfg == null){
			return new ConcurrentHashMap<>();
		}
		return TechnologyCfg.getTypeMap(toCfg.getLevel());
	}

	@Override
	public Map<Integer, Integer> getTechTypeMaxMap() {
		return TechnologyCfg.getTypeMaxMap();
	}

	@Override
	public long getTechLevelPower(int level) {
		BuildingCfg toCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, level);
		if(toCfg == null){
			return 0L;
		}
		return TechnologyCfg.getPower(toCfg.getLevel());
	}


	@Override
	public Activity.ChangeServerActivityCondition.Builder getChangeServerActivityCondition(String playerId, Map<String, String> toServerIdMap, String tarServerId, Activity.ChangeServerActivityConditionType type) {
		Activity.ChangeServerActivityCondition.Builder builder = Activity.ChangeServerActivityCondition.newBuilder();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			builder.setIsDone(false);
			return builder;
		}
		builder.setType(type);
		switch (type){
			case CHANGE_SVR_GUILD:{
				if (player.hasGuild()) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_COLLEGE:{
				if (player.hasCollege()) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_MARCH:{
				if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_BUILDING_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.BUILDING_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_SCIENCE_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.SCIENCE_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_SOILDER_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.SOILDER_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_CURE_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.CURE_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_EQUIP_RESEARCH:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.EQUIP_RESEARCH_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_GUARDER:{
				if (RelationService.getInstance().hasGuarder(player.getId())) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_CROSS:{
				if (player.isCsPlayer()) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_ACCOUNT:{
				Set<String> haveSet = new HashSet<>();
				Set<String> canSet = new HashSet<>();
				Set<String> result = new HashSet<>();
				Map<String, AccountRoleInfo> accountRoleInfos = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
				for (AccountRoleInfo accountRole : accountRoleInfos.values()) {
					haveSet.add(accountRole.getServerId());
				}
				for(String toServerId : toServerIdMap.keySet()){
					String toMainServerId = toServerIdMap.get(toServerId);
					if (toMainServerId.equals(tarServerId)) {
						canSet.add(toServerId);
					}
				}
				result.addAll(canSet);
				result.removeAll(haveSet);
				if(result.size()<=0){
					builder.setIsDone(false);
					return builder;
				}
				builder.setIsDone(true);
				return builder;
			}
			case CHANGE_SVR_CROSS_TECH_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.CROSS_TECH_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_PLANT_SCIENCE_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.PLANT_SCIENCE_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_PLANT_ADVANCE_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.PLANT_ADVANCE_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_CURE_PLANT_QUEUE:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.CURE_PLANT_QUEUE_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			case CHANGE_SVR_NATIONAL_HOSPITAL_RECOVER:{
				if (player.getData().getQueueEntitiesByType(Const.QueueType.NATIONAL_HOSPITAL_RECOVER_VALUE).size() > 0) {
					builder.setIsDone(false);
				}else {
					builder.setIsDone(true);
				}
				return builder;
			}
			default:{
				builder.setIsDone(false);
				return builder;
			}
		}
	}

	@Override
	public void onChangeServerSearch(String playerId, int protoType, String name, int type) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(HawkOSOperator.isEmptyString(name)){
			player.sendError(protoType, Status.SysError.PARAMS_INVALID, 0);
			return;
		}
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), GsConst.IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				Activity.ChangeServerActivitySearchResp.Builder response = Activity.ChangeServerActivitySearchResp.newBuilder();
				response.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_SEARCH_RESP, response));
				return;
			}
		}
		GameTssService.getInstance().wordUicChatFilter(player, name,
				com.hawk.game.protocol.Player.MsgCategory.CHANGE_SVR_SEARCH_MEMBER.getNumber(), GameMsgCategory.CHANGE_SVR_SEARCH_MEMBER,
				String.valueOf(type), null, protoType);
	}

	@Override
	public String getRealChangeServerId(String playerId, String tarServerId, Map<String, String> toServerIdMap){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		Set<String> haveSet = new HashSet<>();
		Set<String> canSet = new HashSet<>();
		Set<String> result = new HashSet<>();
		Map<String, AccountRoleInfo> accountRoleInfos = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		for (AccountRoleInfo accountRole : accountRoleInfos.values()) {
			haveSet.add(accountRole.getServerId());
		}
		for(String toServerId : toServerIdMap.keySet()){
			String toMainServerId = toServerIdMap.get(toServerId);
			if (toMainServerId.equals(tarServerId)) {
				canSet.add(toServerId);
			}
		}
		result.addAll(canSet);
		result.removeAll(haveSet);
		if(result.size()<=0){
			return "";
		}
		List<String> canIds = new ArrayList<String>(result);
		return canIds.get(0);
	}

	@Override
	public boolean onChangeServer(String playerId, String tarServerId, Map<String, String> toServerIdMap) {
		// 拦截下
		if (tarServerId.equals(GsConfig.getInstance().getServerId())) {
			HawkLog.logPrintln("on change server error, can not change to own server, playerId:{}, tarServerId:{}", playerId, tarServerId);
			return false;
		}
		
		HawkLog.logPrintln("player immgration, onImmgration begin, playerId:{}, tarServerId:{}", playerId, tarServerId);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String serverId = getRealChangeServerId(playerId, tarServerId, toServerIdMap);
		if(HawkOSOperator.isEmptyString(serverId)){
			return false;
		}
		Map<String, AccountRoleInfo> accountRoleInfos = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		for (AccountRoleInfo accountRole : accountRoleInfos.values()) {
			if (accountRole.getServerId().equals(serverId)){
				return false;
			}
		}
		HawkLog.logPrintln("player immgration, onImmgration begin, playerId:{}, serverId:{}", playerId, serverId);
		PlayerImmgrationModule module = player.getModule(GsConst.ModuleType.IMMGRATION);

//		// 迁服前检查
//		if (!module.checkBeforeImmgration(tarServerId)) {
//			return false;
//		}
		HawkLog.logPrintln("player immgration, onImmgration, flush to redis begin, playerId:{}", player.getId());

		try {
			int termId = ImmgrationService.getInstance().getImmgrationActivityTermId();
			JSONObject immgrationLog = new JSONObject();
			immgrationLog.put("playerId", player.getId());
			immgrationLog.put("fromServer", GsConfig.getInstance().getServerId());
			immgrationLog.put("tarServer", serverId);
			immgrationLog.put("time", HawkTime.formatNowTime());
			immgrationLog.put("puid", player.getPuid());
			RedisProxy.getInstance().updateImmgrationRecord(termId, player.getId(), immgrationLog.toJSONString());
			RedisProxy.getInstance().addPlayerImmgrationLog(immgrationLog);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		//把数据刷到redis里面
		try {
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToRedis) {
				return false;
			}
			// 序列化活动数据
			ConfigIterator<ImmgrationActivityCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityCfg.class);
			while (cfgIter.hasNext()) {
				ImmgrationActivityCfg cfg = cfgIter.next();
				if (module.flushActivityToRedis(player.getId(), cfg.getActivityId())) {
					HawkLog.logPrintln("player immgration, onImmgration, flush activity to redis, playerId:{}, activityId:{}", player.getId(), cfg.getActivityId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		HawkLog.logPrintln("player immgration, onImmgration, flush to redis end, playerId:{}", player.getId());

		// 通知目标服
		Immgration.ImmgrationServerReq.Builder builder = Immgration.ImmgrationServerReq.newBuilder();
		builder.setPlayerId(playerId);
		builder.setTarServerId(serverId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_REQ_VALUE, builder);
		CrossProxy.getInstance().sendNotify(protocol, tarServerId, player.getId(), null);
		HawkLog.logPrintln("player immgration, onImmgration, send notify, playerId:{}, tarServerId:{}", player.getId(), tarServerId);
		return true;
	}

	@Override
	public int getRechargeTimesAfter(String playerId, int rechargeType, String iosId, String androidId, long startTime) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String platform = player.getPlatform();
		String otherPlatorm = Platform.IOS.strLowerCase().equals(platform) ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
		int count = 0;
		for (RechargeEntity entity : player.getData().getPlayerRechargeEntities()){
			if (entity.getType() != rechargeType || entity.getCreateTime() <= startTime) {
				continue;
			}
			
			String goodsCfgId = entity.getGoodsId();
			PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsCfgId);
			if (cfg != null && !cfg.getChannelType().equals(platform)) {
				goodsCfgId = PayGiftCfg.getId(cfg.getSaleId(), otherPlatorm);
			}
			if (iosId.equals(goodsCfgId) || androidId.equals(goodsCfgId)){
				count++;
			}
		}
		return count;
	}

	@Override
	public int getShowDress(String playerId, int dressType) {
		DressItem ditem = WorldPointService.getInstance().getShowDress(playerId, dressType);
		if (Objects.isNull(ditem)) {
			return 0;
		}
		return ditem.getModelType();
	}
	
	
	/**
	 * 七夕相遇结局
	 * @param playerId
	 * @param termId
	 * @param endingId
	 */
	public void logLoverMeetEnding(String playerId,int termId, int endingId){
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logLoverMeetEnding(player, termId, endingId);
		}
	}

	@Override
	public int getAmourGachaCount(String playerId) {
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			return 0;
		}
		
		final int dayOfYear = HawkTime.getYearDay();
		PlayerGachaEntity gachaOnceEntity = player.getData().getGachaEntityByType(GachaType.ARMOUR_ONE);
		int count = gachaOnceEntity.getDayOfYear() != dayOfYear ? 0 : gachaOnceEntity.getDayCount();
		PlayerGachaEntity gachaTenEntity = player.getData().getGachaEntityByType(GachaType.ARMOUR_TEN);
		int gachaTenCount = gachaTenEntity.getDayOfYear() != dayOfYear ? 0 : gachaTenEntity.getDayCount();
		count += gachaTenCount * 10;
		HawkLog.logPrintln("init equip tech activity achieveItems fetch armour gacha count, playerId: {}, count: {}, dayOfYear: {}, once dayOfYear: {}, ten dayOfYear: {}", 
				playerId, count, dayOfYear, gachaOnceEntity.getDayOfYear(), gachaTenEntity.getDayOfYear());
		return count;
	}

	@Override
	public boolean monthCardFrontBuildCheck(String playerId, int cardType) {
		int frontBuildId = MonthCardActivityCfg.getFrontBuildId(cardType);
		if (frontBuildId > 0) {
			Player player = this.getPlayer(playerId);
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontBuildId);
			int maxCfgId = player.getData().getMaxLevelBuildingCfg(cfg != null ? cfg.getBuildType() : 0);
			if (maxCfgId < frontBuildId) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int monthCardGoldPrivilegeCheck(String playerId, int cardType, String payGiftId, boolean paySuccess) {
		if (cardType != ConstProperty.getInstance().getGoldPrivilegeType()) {
			return 0;
		}
		
		Player player = this.getPlayer(playerId);
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, payGiftId);
		int itemCount = player.getData().getItemNumByItemId(ConstProperty.getInstance().getGoldPrivilegeDiscountItem());
		String androidPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdAndroid();
		String iosPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdIos();
		if (itemCount > 0 && !giftCfg.getId().equals(androidPriceCutId) && !giftCfg.getId().equals(iosPriceCutId)) {
			String cutPriceGiftId = !player.getPlatform().equalsIgnoreCase("ios") ? androidPriceCutId : iosPriceCutId;
			PayGiftCfg tmpCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, cutPriceGiftId);
			giftCfg = tmpCfg == null ? giftCfg : tmpCfg;
		}
		
		Set<String> goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(player.getId());
		if (goodsIds.contains(giftCfg.getId())) {
			if (paySuccess) {
				HawkLog.errPrintln("buy monthCard goldPrivilegeCheck failed, callback unreached, playerId: {}, giftId: {}",  player.getId(), giftCfg.getId());
				return Status.Error.PAY_GIFT_LAST_UNFINISH_VALUE;
			}
			
			RedisProxy.getInstance().removeUnfinishedRechargeGoods(player.getId(), giftCfg.getId());
		}
		
		return 0;
	}

	@Override
	public boolean hasFinishStoryMission(String playerId, int missionId) {
		return StoryMissionService.getInstance().hasFinishStoryMission(playerId, missionId);
	}
	
	
	@Override
	public void logHeroWishChoose(String playerId,int termId, int choose){
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHeroWishChoose(player, termId, choose);
		}
	}
	
	
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  签到天数
	 */
	@Override
	public void logAllianceWishGiftBuy(String playerId,int termId,int giftId, long count,int signCount) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceWishGiftBuy(player, termId, giftId, count,signCount);
		}
	}
	
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  礼包ID
	 */
	@Override
	public void logAllianceWishAchieve(String playerId,int termId, long count,int giftId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logAllianceWishAchieve(player, termId, count,giftId);
		}
	}

	
	
	@Override
	public void logHonourHeroBefellLottery(String playerId,int termId, int lotteryTpye,String lotteryRlt) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logHonourHeroBefellLottery(player, termId, lotteryTpye,lotteryRlt);
		}
	}
		


	@Override
	public boolean isJoinCurrWar(String guildId) {
		return StarWarsActivityService.getInstance().isJoinCurrWar(guildId);
	}

	/**
	 * 荣耀同享玩家捐献
	 * @param playerId
	 * @param termId
	 * @param itemId
	 * @param count
	 * @param guildId
	 * @param time 时间
	 * @param beforeEnergy 捐献前经验值
	 * @param afterEnergy 捐献后经验值
	 * @param curEnergyLevel 捐献后的A能量等级
	 */
	@Override
	public void logShareGloryDonate(String playerId,int termId, int itemId,
									int count, String guildId, int beforeEnergy,
									int afterEnergy, int curEnergyLevel){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			return;
		}
		LogUtil.logShareGloryDonate(player, termId, itemId, count, guildId,
				beforeEnergy, afterEnergy, curEnergyLevel);
	}

	/**
	 *  荣耀同享能量柱升级
	 * @param playerId
	 * @param termId
	 * @param guildId
	 * @param time
	 * @param curLevel
	 */
	@Override
	public void logShareGloryEnergyLevelup(String playerId,int termId, String guildId,
										   int curLevel, int itemId){
		Player player =  this.getPlayer(playerId);
		if (player == null) {
			return;
		}
		LogUtil.logShareGloryEnergyLevelup(player, termId, guildId, curLevel, itemId);
	}

	@Override
	public void logLotteryGiftPay(String playerId, int lotteryType, int selectId, String randomReward) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logLotteryGiftPay(player, lotteryType, selectId, randomReward);
		}
	}

	@Override
	public void logLotteryTakeAchieveReward(String playerId, int lotteryType, int achieveId) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logLotteryTakeAchieveReward(player, lotteryType, achieveId);
		}
	}

	@Override
	public void logLotteryInfo(String playerId, int lotteryType, String reward) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logLotteryInfo(player, lotteryType, reward);
		}
	}

	@Override
	public SpaceMachineGuardActivityInfoPB.Builder getSpaceMechaInfo(String guildId) {
		return SpaceMechaService.getInstance().getSpaceMechaInfo(guildId);
	}

	@Override
	public List<GuardRecordPB.Builder> getGuildSpaceRecord(String guildId) {
		return SpaceMechaService.getInstance().getGuildSpaceRecord(guildId);
	}
	
	@Override
	public boolean spaceMechaGuildCall(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player == null) {
			return false;
		}
		return SpaceMechaService.getInstance().spaceMechaGuildCall(player);
	}

	
	
	@Override
	public void logMachineLabDrop(String playerId,int termId,int dropType,int dropCount) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logMachineLabDrop(player, termId, dropType, dropCount);
		}
	}
	
	@Override
	public void logMachineLabOrderReward(String playerId,int termId,int orderType,String levels) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logMachineLabOrderReward(player, termId, orderType, levels);
		}
	}
	
	@Override
	public void logMachineLabContribute(String playerId,int termId, int count, int giftMult,
			int donatMult, int serverExpAdd, int playerExpAdd, int stormingPointAdd, int stormingPointTotal) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logMachineLabContribute(player ,termId, count, giftMult, donatMult,
					serverExpAdd, playerExpAdd, stormingPointAdd, stormingPointTotal);
		}
	}
	
	@Override
	public void logMachineLabExchange(String playerId,int termId,int exchangeId,int exchangeCount) {
		Player player =  this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logMachineLabExchange(player, termId, exchangeId, exchangeCount);
		}
	}
	
	/**
	 * 获得星币打点
	 * @param player
	 * @param termId
	 * @param taskId
	 * @param pointCount
	 * @param guildPointCount
	 * @param taskLimitGap
	 */
	public void logSpaceMechaPointGet(String playerId, int termId, int taskId, int pointCount, long guildPointCount, int taskLimitGap) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			SpaceMechaService.getInstance().logSpaceMechaPointGet(player, termId, taskId, pointCount, guildPointCount, taskLimitGap);
		}
	}

	@Override
	public void addSpaceMechaPoint(String guildId, int addPoint) {
		SpaceMechaService.getInstance().addGuildPoint(guildId, addPoint);
	}

	@Override
	public long getSpaceMechaPoint(String guildId) {
		return SpaceMechaService.getInstance().getGuildPointCount(guildId);
	}

	@Override
	public boolean corssPostEvent(String playerId, ActivityEvent event) {
		if(event instanceof BattlePointChangeEvent){ // 战力变化不处理
			return true;
		}
		Player player = this.getPlayer(playerId);
		byte[] bytes = HawkSerializer.serialize(event);
		PBCrossPostActivityEvent.Builder builder = PBCrossPostActivityEvent.newBuilder();
		builder.setClassName(event.getClass().getName());
		builder.setEventSerialize(ByteString.copyFrom(bytes));
		HawkProtocol protocol = HawkProtocol.valueOf(HP.sys.CROSS_POST_ACTIVITY_EVENT, builder);
		CrossProxy.getInstance().sendNotify(protocol, player.getMainServerId(), playerId);
		HawkLog.logPrintln("post activity event cross server, playerId: {}, event: {}", playerId, event.getClass().getName());
		return true;
	}

	@Override
	public void dungeonRedisLog(String anyId, String messagePattern, Object... arguments) {
		DungeonRedisLog.log(anyId, messagePattern, arguments);
	}

	@Override
	public boolean isOverlordBlessingOpenServer() {
		ConfigIterator<StarWarsPartCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class);
		for (StarWarsPartCfg cfg : configIterator) {
			if (!cfg.getAreaId().equals(GsConfig.getInstance().getAreaId())) {
				continue;
			}
			
			if (cfg.getTeam() != 0 || cfg.getZone() != 0) {
				continue;
			}
			
			if (cfg.getServerList().contains(GsConfig.getInstance().getServerId())) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void logCelebrationGiftBuy(String playerId, int fundLevel) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logCelebrationFundGiftBuy(player, fundLevel);
		}
	}

	@Override
	public void logCelebrationScoreBuy(String playerId, int score, int cost) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logCelebrationFundScoreBuy(player, score, cost);
		}
	}

	@Override
	public int getDiamonds(String playerId) {
		Player player = this.getPlayer(playerId);
		if (player != null) {
			return player.getDiamonds();
		}
		return 0;
	}

	@Override
	public void logGoldBabyFindReward(String playerId, ActivityType activityType, int isLockTopGrade, int poolId, int costCount, int rewardCount, int magnification) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGoldBabyFindReward(player, String.valueOf(activityType.intValue()), isLockTopGrade, poolId, costCount, rewardCount, magnification);
		}
		
	}

	@Override
	public void logNewbieTrain(String playerId, int trainType, int times, int remainTimes, int gachaTimes, int gachaTimesTotal) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logNewbieTrain(player, trainType, times, remainTimes, gachaTimes, gachaTimesTotal);
		}
	}

	@Override
	public int getBuff(String playerId, EffType effType) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		return player.getEffect().getEffVal(effType);
	}
	
	@Override
	public long getBuffEndTime(String playerId, EffType effType) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		return player.getData().getBuffEndTime(effType.getNumber());
	}

	@Override
	public void syncEffect(String playerId, Collection<Integer> collection) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player != null){
			player.getEffect().syncEffect(player, collection);
		}
	}

	/**
	 * 判断对应装扮是否处于激活状态
	 */
	@Override
	public boolean dressInActiveState(String playerId, int dressId) {
		Player player = this.getPlayer(playerId);
		if (player == null) {
			return false;
		}
		
		DressCollectionKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionKVCfg.class);
		if (cfg.getDressIdSet().contains(dressId)) {//装扮ID
			DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
			DressEntity dressEntity = player.getData().getDressEntity();
			DressItem dressInfo = dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
			if (dressInfo != null && dressInfo.getStartTime() + dressInfo.getContinueTime() > HawkTime.getMillisecond()) {
				return true;
			}
		} else {//头像ID
			long endTime = player.getData().getBuffEndTime(dressId);
			if (endTime > HawkTime.getMillisecond()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 选举邀请合服活动队长
	 */
	@Override
	public String chooseInviteMergeLeader() {
		// 异常情况判断,联盟排行榜为空
		List<RankInfo> rankCache = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY, 1);
		if (rankCache == null || rankCache.isEmpty()) {
			return null;
		}
		
		// 如果是本服司令
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		if (president != null && president.getServerId().equals(GsConfig.getInstance().getServerId())) {
			return president.getPlayerId();
		} else {
			HawkLog.logPrintln("chooseInviteMergeLeader president null");
		}
		
		// 否则取一盟盟主
		String guildId = rankCache.get(0).getId();
		HawkLog.logPrintln("chooseInviteMergeLeader guildId:{}", guildId);
		return GuildService.getInstance().getGuildLeaderId(guildId);
	}

	@Override
	public int getOfficerId(String playerId) {
		return GameUtil.getOfficerId(playerId);
	}

	@Override
	public String getGmRechargeRedisKey() {
		return RedisKey.IDIP_GM_RECHARGE;
	}

	/**
	 * 主服列表
	 */
	@Override
	public List<ServerInfo> getServerList() {
		return RedisProxy.getInstance().getServerList().stream().filter(s-> GlobalData.getInstance().isMainServer(s.getId())).filter(s-> s.getServerType() == ServerType.NORMAL).collect(Collectors.toList());
	}

	/**
	 * 星能探索活动刷矿点
	 */
	@Override
	public List<Integer> planetExploreRefreshResPoint(int refreshCount) {
		try {
			return WorldPointService.getInstance().planetExploreRefreshPoint(refreshCount);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return new ArrayList<>();
	}

	@Override
	public long getPlanetPointResRemain(int posX, int posY) {
		int pointId = GameUtil.combineXAndY(posX, posY);
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point == null || point.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
			return 0;
		}
		
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, point.getResourceId());
		if (resTreCfg.getId() != cfg.getRefreshTargetId()) {
			return 0;
		}
		
		WorldPointPB pointPB = point.toBuilder(WorldPointPB.newBuilder(), "").build();
		List<PBTreaCollRec> caijiJilu = pointPB.getTreaCollrecList();
		// 采集总数
		long total = 0;
		for (PBTreaCollRec record : caijiJilu) {
			List<ItemInfo> rs = ItemInfo.valueListOf(record.getAward());
			if (!rs.isEmpty()) {
				total += rs.get(0).getCount();
			}
		}
		
		List<ItemInfo> rsAll = ItemInfo.valueListOf(resTreCfg.getTotalRes());
		return rsAll.get(0).getCount() - total;
	}

	@Override
	public long getPlanetPointLifeTime(int refreshTargetId) {
		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, refreshTargetId);
		return resTreCfg == null ? 0L : resTreCfg.getLifeTime() * 1000L;
	}

	@Override
	public boolean isUnlockEquipResearch(String playerId, int type) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			switch (type) {
			case 1:
				return player.getEntity().getUnlockEquipResearch() > 0;
			case 2:
				for (com.hawk.game.player.supersoldier.SuperSoldier superSoldier : player.getAllSuperSoldier()) {
					if (superSoldier.getSoldierEnergy().isUnlockEnergy()) {
						return true;
					}
				}
				return false;
			case 3:
				CommanderEntity entity = player.getData().getCommanderEntity();
				ArmourStarExplores starExplores = entity.getStarExplores();
				return starExplores.getIsActive() == 1;

			default:
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}


	@Override
	public void logGrowUpBoostAchieveScore(String playerId, int termId, int achieveId, int scoreAdd, int scoreBef,
		int scoreAft) {
	Player player= this.getPlayer(playerId);
	if (player != null) {
		LogUtil.logGrowUpBoostAchieveScore(player, termId, achieveId, scoreAdd, scoreBef, scoreAft);
	}
	}

	@Override
	public void logGrowUpBoostItemScore(String playerId, int termId, int itemId, int itemNum, int scoreAdd,
			int scoreBef, int scoreAft) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostItemScore(player, termId, itemId, itemNum, scoreAdd, scoreBef, scoreAft);
		}
	}

	@Override
	public void logGrowUpBoostScoreAchieveRewardTake(String playerId, int termId, int achieveId, int score) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostScoreAchieveRewardTake(player, termId, achieveId, score);
		}
	}

	@Override
	public void logGrowUpBoostScoreAchievePageChange(String playerId, int termId, int refreshCount, int curPage) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostScoreAchievePageChange(player, termId, refreshCount, curPage);
		}
	}

	@Override
	public void logGrowUpBoostExchangeGroup(String playerId, int termId, int exchangeId, int exchangeGroup,
			int unlockGroupMaxBef, int unlockGroupMaxAft) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostExchangeGroup(player, termId, exchangeId, exchangeGroup, unlockGroupMaxBef, unlockGroupMaxAft);
		}
	}

	@Override
	public void logGrowUpBoostBuyGift(String playerId, int termId, int buyId) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostBuyGift(player, termId, buyId);
		}
	}
	
	@Override
	public void logGrowUpBoostItemRecover(String playerId, int termId, int itemId,int itemCount) {
		Player player= this.getPlayer(playerId);
		if (player != null) {
			LogUtil.logGrowUpBoostItemRecover(player, termId,itemId,itemCount);
		}
	}
	
	@Override
	public boolean activity185Monster(int monsterId) {
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if(monsterCfg != null && monsterCfg.getRelateActivity() == 185){
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean activity184Monster(int monsterId) {
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if(monsterCfg != null && monsterCfg.getRelateActivity() == 184){
			return true;
		}
		return false;
	}
	

	
	/**
	 * 获取记录
	 * @param playerId
	 * @return
	 */
	public Map<Integer,Long> getGrowUpBoostItemRecord(String playerId){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return new HashMap<>();
		}
		CustomDataEntity customData = player.getData().getCustomDataEntity(GrowUpBoostActivity.CUSTOM_KEY);
		if (customData == null) {
			return new HashMap<>();
		}
		
		String str = customData.getArg();
		HawkLog.logPrintln("getGrowUpBoostItemRecord,playerId:{},data:{}", playerId,str);
		return SerializeHelper.stringToMap(str, Integer.class, Long.class);
	}
	
	/**
	 * 更新保持活动记录
	 * @param playerId
	 * @param rmap
	 */
	public void updateGrowUpBoostItemRecord(String playerId,Map<Integer,Long> rmap){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return;
		}
		String str = SerializeHelper.mapToString(rmap);
		CustomDataEntity customData = player.getData().getCustomDataEntity(GrowUpBoostActivity.CUSTOM_KEY);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GrowUpBoostActivity.CUSTOM_KEY, 0, str);
			return;
		} 
		customData.setArg(str);
		HawkLog.logPrintln("updateGrowUpBoostItemRecord,playerId:{},data:{}", playerId,str);
	}
	
	/**
	 * 记录数据
	 * @param playerId
	 * @param items
	 */
	public void growUpBoostEquipDecomposeItemRecord(String playerId,Map<Integer,Long> items){
		GrowUpBoostKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
		if (kvConfig == null) {
			return;
		}
		long curTime = HawkTime.getMillisecond();
		boolean update =false;
		Map<Integer,Long> rmap = this.getGrowUpBoostItemRecord(playerId);
		for(Map.Entry<Integer,Long> item :items.entrySet()){
			int itemId = item.getKey();
			long count = item.getValue();
			if(!kvConfig.getDecomposeItemMap().containsKey(itemId)){
				continue;
			}
			long workTime = kvConfig.getDecomposeItemMap().get(itemId);
			if(curTime < workTime){
				continue;
			}
			long recoredCount = rmap.getOrDefault(itemId, 0l);
			recoredCount += count;
			rmap.put(itemId, recoredCount);
			update = true;
		}
		if(update){
			this.updateGrowUpBoostItemRecord(playerId, rmap);
			Optional<ActivityBase> optional = ActivityManager.getInstance()
					.getActivity(Activity.ActivityType.GROW_UP_BOOST_VALUE);
			if(optional.isPresent()){
				GrowUpBoostActivity activity = (GrowUpBoostActivity) optional.get();
				activity.syncActivityDataInfo(playerId);
			}
		}
	}


	@Override
	public Map<Integer,Long> getGfitServerAwardItemsCount(String giftId){
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if (giftCfg == null) {
			return new HashMap<>();
		}
		ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(giftCfg.getServerAwardId());
		if (cfg == null) {
			return new HashMap<>();
		}
		AwardItems awardItems = cfg.getAwardItems();
		return awardItems.getAwardItemsCount();
	}

	@Override
	public List<String> getPlayerGameFriends(String playerId) {
		List<String> playerRelationIds = RelationService.getInstance().getPlayerRelationIdList(playerId, GsConst.RelationType.FRIEND);
		return playerRelationIds;
	}
	
	@Override
	public String getPlayerByName(String name) {
		String playerId = GlobalData.getInstance().getPlayerIdByName(name);
		if(HawkOSOperator.isEmptyString(playerId)){
			return null;
		}
		if(!this.isServerPlayer(playerId)){
			return null;
		}
		return playerId;
	}


	
	
	/**
	 * 获取记录
	 * @param playerId
	 * @return
	 */
	public String getBackImmgrationData(String playerId){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return "";
		}
		CustomDataEntity customData = player.getData().getCustomDataEntity(BackImmgrationActivity.CUSTOM_KEY);
		if (customData == null) {
			return "";
		}
		String str = customData.getArg();
		return str;
	}
	
	/**
	 * 更新保持活动记录
	 * @param playerId
	 * @param rmap
	 */
	public void updateBackImmgrationData(String playerId,String str){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return;
		}
		CustomDataEntity customData = player.getData().getCustomDataEntity(BackImmgrationActivity.CUSTOM_KEY);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(BackImmgrationActivity.CUSTOM_KEY, 0, str);
			return;
		} 
		customData.setArg(str);
	}
	
	
	public Map<String, HawkTuple2<Integer, Long>> getRankDataMapCache(RankType rankType){
		return RankService.getInstance().getRankDataMapCache(rankType);
	}

	@Override
	public long getPlayerNoArmyPower(String playerId) {
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return 0;
		}
		return player.getNoArmyPower();
	}
	
	
	public boolean inCrossActivityTime(){
		ConfigIterator<CrossTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (CrossTimeCfg timeCfg : its) {
			if (timeCfg.getShowTimeValue() <= now && now <= timeCfg.getEndTimeValue()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断超武养成线功能是否解锁
	 * @param playerId
	 * @return
	 */
	public boolean isManhattanFuncUnlocked(String playerId) {
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return false;
		}
		
		return player.checkManhattanFuncUnlock();
	}

	@Override
	public boolean isPlantWeaponUnlocked(String playerId, int swId) {
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return false;
		}
		
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		return manhattan != null;
	}

	@Override
	public boolean isPlantWeaponUnlockItemEnough(String playerId, int swId) {
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return false;
		}
		ManhattanSWCfg config = HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, swId);
		if (config == null) {
			return false;
		}
		ItemInfo item = ItemInfo.valueOf(config.getUnlockConsumption());
		int itemCount = player.getData().getItemNumByItemId(item.getItemId());
		return itemCount >= item.getCount();
	}

	@Override
	public void updateGuildEffect(int activityId, String guildId, Map<Integer, Integer> effMap) {
		ActivityManager.getInstance().updateGuildEffect(activityId, guildId, effMap);
	}

	@Override
	public void cleanGuildEffect(int activityId) {
		ActivityManager.getInstance().cleanGuildEffect(activityId);
	}

	@Override
	public int getServerPlayerCount() {
		return GlobalData.getInstance().getRegisterCount();
	}
	
	@Override
	public String getMainServer(String serverId){
		 return GlobalData.getInstance().getMainServerId(serverId);
	}
	
	@Override
	public GuildDragonTrapData getGuildDragonTrapData(String playerId){
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return null;
		}
		List<IGuildBuilding> list = GuildManorService.getInstance().getGuildBuildByType(guildId, TerritoryType.GUILD_DRAGON_TRAP);
		if(list.isEmpty()){
			return null;
		}
		GuildDragonTrap trap = (GuildDragonTrap) list.get(0);
		return trap.getGuildDragonTrapData(playerId);
	}
	
	@Override
	public void guildDragonTrapOp(String playerId,int action,String... params){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return;
		}
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
		int threadIndex = Math.abs(GuildManorService.getInstance().getXid().hashCode() % threadPool.getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
				if(HawkOSOperator.isEmptyString(guildId)){
					return null;
				}
				List<IGuildBuilding> list = GuildManorService.getInstance().getGuildBuildByType(guildId, TerritoryType.GUILD_DRAGON_TRAP);
				if(list.isEmpty()){
					return null;
				}
				GuildDragonTrap trap = (GuildDragonTrap) list.get(0);
				trap.guildDragonTrapOp(player, action, params);
				return null;
			}
		}, threadIndex);
	}
	
	
	@Override
	public List<PBDamageRank> guildDragonAttackRank(String playerId){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return null;
		}
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return null;
		}
		List<IGuildBuilding> list = GuildManorService.getInstance().getGuildBuildByType(guildId, TerritoryType.GUILD_DRAGON_TRAP);
		if(list.isEmpty()){
			return null;
		}
		GuildDragonTrap trap = (GuildDragonTrap) list.get(0);
		return trap.getRankData();
	}
	
	
	
	
	
	
	
	@Override
	public void xqhxTalentCheck(String playerId){
		Player player= this.getPlayer(playerId);
		if (Objects.isNull(player)) {
			return;
		}
		PlayerXQHXModule module = player.getModule(GsConst.ModuleType.XQHX_WAR_MOUDLE);
		module.checkTalent(player);
	}

	@Override
	public boolean isSeasonHonorDataNew() {
		return GameConstCfg.getInstance().isSeasonHonorNew();
	}
	
	@Override
	public HawkRedisSession getOldRedisSession() {
		return RedisProxy.getInstance().getOldRedisSession();
	}
	
	
	@Override
	public HawkTuple3<Integer, Integer, Integer> getSoldierConfigData(int soldierId) {
		BattleSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, soldierId);
		if (config == null) {
			return null;
		}
		return HawkTuples.tuple(config.getType(), config.getLevel(), config.getTime());
	}
	
	
	@Override
	public int getSpecialSoldierTime(int soldierLevel){
		int specialTime = NationConstCfg.getInstance().getSpecialSoldierTime(soldierLevel);
		return specialTime;
	}
	
	
	@Override
	public int getItemSpeedUpTime(int itemId){
		 ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		 if (itemCfg == null) {
			 return 0; 
		 }
		 return itemCfg.getSpeedUpTime();
	}
}
