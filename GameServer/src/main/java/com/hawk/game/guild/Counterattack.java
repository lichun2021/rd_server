package com.hawk.game.guild;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.entity.GuildCounterattackEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Counterattack.PBCounterattackDetailResp;
import com.hawk.game.protocol.Counterattack.PBCounterattackInfo;
import com.hawk.game.protocol.Counterattack.PBDonor;
import com.hawk.game.protocol.Counterattack.PBDonor.Builder;
import com.hawk.game.protocol.Counterattack.PBKillPower;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventCounterAttack;
import com.hawk.game.util.GameUtil;

/**
 * 联盟反击
 * 
 * @author lwt
 * @date 2018年2月6日
 */
public class Counterattack {
	private AtomicBoolean hasSendReward;
	private GuildCounterattackEntity dbObj;
	// 玩家id: 赏金
	private Map<String, CounterattackBounty> playerBounty;
	// 玩家id: 战力
	private Map<String, Integer> playerWipout;

	private Counterattack(GuildCounterattackEntity dbObj) {
		this.hasSendReward = new AtomicBoolean(false);
		this.dbObj = dbObj;
	}

	public static Counterattack create(GuildCounterattackEntity guildCounterattackEntity) {
		Counterattack result = new Counterattack(guildCounterattackEntity);
		result.init();
		guildCounterattackEntity.recordObj(result);
		return result;
	}

	public long getCreateTime() {
		return this.dbObj.getCreateTime();
	}

	public PBCounterattackInfo toSummaryPbObj() {
		Player playersnapshot = GlobalData.getInstance().makesurePlayer(dbObj.getPlayerId());
		Player attackersnapshot = GlobalData.getInstance().makesurePlayer(dbObj.getAtkerId());
		PBCounterattackInfo.Builder builder = PBCounterattackInfo.newBuilder();
		builder.setUuid(dbObj.getId())
				.setCreateTime(dbObj.getCreateTime())
				.setPlayerId(playersnapshot.getId())
				.setEplayerId(attackersnapshot.getId())
				.setEpfIcon(attackersnapshot.getPfIcon())
				.setEicon(attackersnapshot.getIcon())
				.setEname(attackersnapshot.getName())
				.setEguildTag(attackersnapshot.getGuildTag())
				.setBounty(ItemInfo.toString(allBounty()))
				.setGoalWipeout(dbObj.getCounterPower())
				.setWipeout(wipeOutPower())
				.setBattlePoint(attackersnapshot.getPower());

		return builder.build();
	}

	/**
	 * 详情
	 */
	public PBCounterattackDetailResp.Builder toDetailPbobj(String playerId) {
		Player playersnapshot = GlobalData.getInstance().makesurePlayer(dbObj.getPlayerId());
		Player attackersnapshot = GlobalData.getInstance().makesurePlayer(dbObj.getAtkerId());
		PBCounterattackDetailResp.Builder builder = PBCounterattackDetailResp.newBuilder();
		builder.setUuid(dbObj.getId())
				.setCreateTime(dbObj.getCreateTime())
				.setPlayerId(playersnapshot.getId())
				.setPfIcon(playersnapshot.getPfIcon())
				.setIcon(playersnapshot.getIcon())
				.setName(playersnapshot.getName())
				.setEplayerId(attackersnapshot.getId())
				.setEpfIcon(attackersnapshot.getPfIcon())
				.setEicon(attackersnapshot.getIcon())
				.setEname(attackersnapshot.getName())
				.setEguildTag(attackersnapshot.getGuildTag())
				.setBounty(ItemInfo.toString(allBounty()))
				.setGoalWipeout(dbObj.getCounterPower())
				.setWipeout(wipeOutPower());
		playerBounty.forEach((pid, bounty) -> {
			if (Objects.equals(bounty.getPlayerId(), playerId)) {
				builder.setReqPlayerUpTimes(bounty.getUpTimes());
			}
			Player shot = GlobalData.getInstance().makesurePlayer(pid);
			Builder setBounty = PBDonor.newBuilder().setName(shot.getName())
					.setUpTimes(bounty.getUpTimes())
					.setBounty(ItemInfo.toString(bounty.getBounty().getAwardItems()))
					.setIcon(shot.getIcon())
					.setPfIcon(shot.getPfIcon());
			builder.addDonors(setBounty);
		});
		playerWipout.forEach((pid, power) -> {
			Player shot = GlobalData.getInstance().makesurePlayer(pid);
			PBKillPower.Builder killPower = PBKillPower.newBuilder().setName(shot.getName())
					.setWipeout(power)
					.setIcon(shot.getIcon())
					.setPfIcon(shot.getPfIcon());
			builder.addKillPowers(killPower);
		});
		return builder;
	}

	/**
	 * 消灭战力
	 */
	public void upWipout(Player player, int killpower, int battlePointId) {
		int addKillpower = Math.min(killpower, dbObj.getCounterPower() - wipeOutPower());
		playerWipout.merge(player.getId(), addKillpower, (v1, v2) -> v1 + v2);
		this.notifyChanged();
		//反击击杀
		GuildRankMgr.getInstance().onPlayerCounter(player.getId(), killpower);
		rewardCheck(player, battlePointId);
	}

	public void rewardCheck(Player brother, int battlePointId) {
		if (hasSendReward.get()) {
			return;
		}
		int killCount = wipeOutPower();
		

		if (killCount < dbObj.getCounterPower()) {
			return;
		}
		GuildService.getInstance().counterattackRemove(this);
		
		List<ItemInfo> allItems = allBounty();
		Player guildMember = GlobalData.getInstance().makesurePlayer(dbObj.getPlayerId());
		Player attacker = GlobalData.getInstance().makesurePlayer(dbObj.getAtkerId());
		// 按伤害值分奖励
		List<Entry<String, Integer>> sortedKill = playerWipout.entrySet().stream()
				.sorted(Comparator.comparingInt(Entry::getValue))
				.collect(Collectors.toCollection(ArrayList::new));
		AwardItems hasSend = AwardItems.valueOf();
		for (int i = 0; i < sortedKill.size(); i++) {
			Entry<String, Integer> ent = sortedKill.get(i);
			String pid = ent.getKey();
			double kill = ent.getValue();
			double per = kill / killCount;
			List<ItemInfo> reward;
			if (i < sortedKill.size() - 1) {// 不是击杀最高者
				reward = allItems.stream()
						.map(ItemInfo::clone)
						.peek(re -> re.setCount((int) (re.getCount() * per)))
						.filter(re -> re.getCount() > 0)
						.collect(Collectors.toList());
				hasSend.addItemInfos(reward);// 记录发给第二名有以后的奖励
			} else { // 把余额全发放
				List<ItemInfo> sendReward = hasSend.getAwardItems();
				reward = allItems.stream()
						.map(ItemInfo::clone)
						.peek(item -> {
							for (ItemInfo tmpItem : sendReward) {
								if (item.getItemId() == tmpItem.getItemId()) {
									item.setCount(item.getCount() - tmpItem.getCount());
								}
							}
						}).filter(re -> re.getCount() > 0)
						.collect(Collectors.toList());
			}

			// R57914246 调优037 联盟悬赏--赏金限制
			List<ItemInfo>	sendReward = checkAndAmend(pid, reward);
			
			if (sendReward.isEmpty()) {
				continue;
			}
			
			int todayAchieveBounty = LocalRedis.getInstance().getDayGuildBountyAwd(pid);
			int configTodayMaxAchieve = GuildConstProperty.getInstance().getBeatbackMaxDailyBouns();
			int lastCount = configTodayMaxAchieve - todayAchieveBounty;
			lastCount = Math.max(lastCount, 0);
			MailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(pid)
					.setMailId(MailId.COUNTERATTACK_REWARD)
					.addRewards(sendReward)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(guildMember.getName())
					.addContents(attacker.getNameWithGuildTag())
					.addContents(lastCount)
					.build());
			//联盟排行榜 领赏榜单
			GuildRankMgr.getInstance().onPlayerAwdBounty( brother.getId() , sendReward);
	
		}

	
		MailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(dbObj.getPlayerId())
				.setMailId(MailId.BEATBACK_SUC)
				.addRewards(ItemInfo.valueListOf(dbObj.getBitBackRewards()))
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(brother.getName())
				.addContents(attacker.getNameWithGuildTag())
				.addSubTitles(brother.getName())
				.addSubTitles(attacker.getNameWithGuildTag())
				.build());
		

		// 广播
		int[] xy = GameUtil.splitXAndY(battlePointId);
		ChatParames parames = ChatParames.newBuilder()
				.setChatType(Const.ChatType.CHAT_ALLIANCE)
				.setKey(Const.NoticeCfgId.GUILD_COUNTERATTACK_WIN)
				.setPlayer(guildMember)
				.addParms(attacker.getName(), xy[0], xy[1], brother.getName())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(parames);

		hasSendReward.set(true);

		// 任务
		MissionManager.getInstance().postMsg(brother, new EventCounterAttack());
	}

	private List<ItemInfo> checkAndAmend(String pid, List<ItemInfo> reward) {
		try {

			List<ItemInfo> tmpReward = new ArrayList<>();
			int todayAchieveBounty = LocalRedis.getInstance().getDayGuildBountyAwd(pid);
			int configTodayMaxAchieve = GuildConstProperty.getInstance().getBeatbackMaxDailyBouns();
			if (todayAchieveBounty < configTodayMaxAchieve) {
				int goldenAmount = 0;
				for (ItemInfo info : reward) {
					if ((PlayerAttr.GOLD_VALUE == info.getItemId())) {
						if (goldenAmount + todayAchieveBounty < configTodayMaxAchieve) {
							if (goldenAmount + todayAchieveBounty + info.getCount() < configTodayMaxAchieve) {
								goldenAmount += info.getCount();
								tmpReward.add(info.clone());
							} else if (goldenAmount + todayAchieveBounty + info.getCount() == configTodayMaxAchieve) {
								goldenAmount += info.getCount();
								tmpReward.add(info.clone());
							} else {
								int addVal = configTodayMaxAchieve - (goldenAmount + todayAchieveBounty);
								goldenAmount += addVal;
								tmpReward.add(new ItemInfo(info.getType(), info.getItemId(), addVal));
							}
						}
					} else {
						tmpReward.add(info.clone());
					}
				}
				if (goldenAmount > 0) {
					LocalRedis.getInstance().dayGuildBountyAwdInc(pid, goldenAmount);
				}
			}
			return tmpReward;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return reward;
	}

	/** 已消灭战力 */
	private int wipeOutPower() {
		return playerWipout.values().stream().mapToInt(Integer::intValue).sum();
	}

	private List<ItemInfo> allBounty() {
		AwardItems allAward = AwardItems.valueOf(dbObj.getRewards());
		playerBounty.values().stream().map(CounterattackBounty::getBounty).forEach(bounty -> allAward.addItemInfos(bounty.getAwardItems()));

		List<ItemInfo> allItems = allAward.getAwardItems();
		return allItems;
	}

	public boolean hasSendReward() {
		return hasSendReward.get();
	}

	/** 提升赏金历史次数 */
	public int upBountyTimes(String playerId) {
		if (!playerBounty.containsKey(playerId)) {
			return 0;
		}
		return playerBounty.get(playerId).getUpTimes();
	}

	/**
	 * 提升赏金
	 */
	public void upBounty(Player player, List<ItemInfo> arg0) {
		HawkAssert.notNull(arg0);
		CounterattackBounty bounty = playerBounty.getOrDefault(player.getId(), new CounterattackBounty(player.getId()));
		bounty.addBounty(arg0);
		bounty.setUpTimes(bounty.getUpTimes() + 1);
		playerBounty.put(player.getId(), bounty);
		notifyChanged();
	}

	private void sendBackBounty(String playerId, MailId mailId) {
		CounterattackBounty bounty = playerBounty.remove(playerId);
		if (Objects.isNull(bounty)) {
			return;
		}

		switch (mailId) {
		case COUNTERATTACK_REWARD_QUITGUILD_BACK:
			Player playersnapshot = GlobalData.getInstance().makesurePlayer(dbObj.getPlayerId());
			MailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(mailId)
					.addRewards(bounty.getBounty().getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(playersnapshot.getName())
					.build());

			break;
		case COUNTERATTACK_REWARD_CHEXIAO_BACK:
			Player attacker = GlobalData.getInstance().makesurePlayer(dbObj.getAtkerId());
			String nameWithGuildTag = attacker.getNameWithGuildTag();
			MailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(mailId)
					.addRewards(bounty.getBounty().getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(nameWithGuildTag)
					.addTitles(nameWithGuildTag)
					.addSubTitles(nameWithGuildTag)
					.build());
			break;
		default:
			break;
		}

	}

	/**
	 * 奖励返还
	 */
	public void sendBackOneBounty(String playerId, MailId mailId) {
		this.sendBackBounty(playerId, mailId);
		this.notifyChanged();
	}

	/**
	 * 返还全部奖励
	 */
	public void sendBackAllBounty(MailId mailId) {
		List<String> playerIdList = new ArrayList<>(playerBounty.keySet());
		playerIdList.forEach(pid -> sendBackBounty(pid, mailId));
		this.notifyChanged();
	}

	private void notifyChanged() {
		dbObj.setChanged(true);
	}

	private void init() {
		loadPlayerBounty();
		loadWipeout();
	}

	private void loadWipeout() {
		playerWipout = new HashMap<>();
		if (StringUtils.isNotEmpty(dbObj.getWipeoutSer())) {
			JSONObject obj = JSON.parseObject(dbObj.getWipeoutSer());
			obj.forEach((k, v) -> {
				playerWipout.put(k, (Integer) v);
			});
		}

	}

	/** 加载玩家悬赏 */
	private void loadPlayerBounty() {
		playerBounty = new HashMap<>();
		if (StringUtils.isNotEmpty(dbObj.getPlayerBountySer())) {
			JSONArray arr = JSONArray.parseArray(dbObj.getPlayerBountySer());
			for (Object str : arr) {
				CounterattackBounty bounty = new CounterattackBounty(null);
				bounty.mergeFrom(str.toString());
				playerBounty.put(bounty.getPlayerId(), bounty);
			}
		}
	}

	public String playerBountySer() {
		JSONArray result = new JSONArray();
		playerBounty.values().stream()
				.map(CounterattackBounty::serializ)
				.forEach(result::add);

		return result.toJSONString();
	}

	public String wipeoutSer() {
		JSONObject obj = new JSONObject();
		playerWipout.forEach((k, v) -> {
			obj.put(k, v);
		});
		return obj.toJSONString();
	}

	public GuildCounterattackEntity getDbObj() {
		return dbObj;
	}

	public String getAtkerId() {
		return dbObj.getAtkerId();
	}

	public long getOverTime() {
		return dbObj.getOverTime();
	}

}
