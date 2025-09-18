package com.hawk.game.guild;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.AllianceBigGiftCfg;
import com.hawk.game.config.AllianceBigGiftLevelCfg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.GuildBigGiftEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.GuildSmailGiftAddMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildBigGift.PBGuildBigGift;
import com.hawk.game.protocol.GuildBigGift.PBSyncGuildBigGift;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.GuildService;

public class GuildBigGift {
	private GuildBigGiftEntity entity;

	/** 保留配置时间内的小礼包 */
	private List<GuildSmailGift> smailGiftList;

	public static GuildBigGift create(GuildBigGiftEntity entity) {
		GuildBigGift obj = new GuildBigGift();
		obj.entity = entity;
		obj.init();
		return obj;
	}

	private void init() {
		smailGiftList = loadSmailGiftList();
	}

	public AllianceBigGiftCfg getCfg() {
		AllianceBigGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceBigGiftCfg.class, entity.getBigGiftId());
		HawkAssert.notNull(cfg);
		return cfg;
	}

	/** 未获得的小礼包 */
	public List<GuildSmailGift> unResiveGifts(long lastCreateTime) {
		return smailGiftList.stream().filter(gift -> gift.getCreateTime() > lastCreateTime).collect(Collectors.toList());
	}

	public int getLevel() {
		ConfigIterator<AllianceBigGiftLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AllianceBigGiftLevelCfg.class);
		long level = configIterator.stream()
				.filter(cfg -> cfg.getGiftLevelExp() <= entity.getBigGiftLevelExp()).count() + 1;
		return (int) Math.min(configIterator.size(), level);
	}

	/**
	 * 添加小礼包,
	 * 
	 * @param itemId
	 *            领取时打开reward字段
	 * @param open
	 *            是否直接开启, 显示可领取物品
	 */
	public void addSmailGift(int itemId, boolean open) {
		GuildSmailGift gift = new GuildSmailGift();
		if (open) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			AwardItems awardItems = awardCfg.getRandomAward();
			String reward = ItemInfo.toString(awardItems.getAwardItems());
			gift.setReward(reward);
		}
		long now = HawkTime.getMillisecond();
		long allianceGiftTime = GuildConstProperty.getInstance().getAllianceGiftDisappearTime() * 1000;
		gift.setItemId(itemId);
		gift.setCreateTime(now);
		gift.setOverTime(now + allianceGiftTime);
		smailGiftList.add(gift);

		while (smailGiftList.size() > GuildConstProperty.getInstance().getAllianceGiftUpLimit()) {
			smailGiftList.remove(0);
		}
		entity.notifyUpdate();

		// 通知在线成员刷新小礼包 走message
		Collection<String> mesbers = GuildService.getInstance().getGuildMembers(entity.getGuildId());
		for (String playerId : mesbers) {
			GuildSmailGiftAddMsg msg = GuildSmailGiftAddMsg.valueOf(gift);
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null && player.isActiveOnline()) {
				HawkApp.getInstance().postMsg(player.getXid(), msg);
			}
		}
	}

	private List<GuildSmailGift> loadSmailGiftList() {
		if (StringUtils.isEmpty(entity.getGiftSerialized())) {// 新盟
			return new LinkedList<>();
		}

		try {
			long now = HawkTime.getMillisecond();
			List<GuildSmailGift> list = new LinkedList<>();
			JSONArray arr = JSONArray.parseArray(entity.getGiftSerialized());
			for (Object str : arr) {
				GuildSmailGift gift = new GuildSmailGift();
				gift.mergeFrom(str.toString());
				if (gift.getOverTime() > now) {
					list.add(gift);
				}
			}
			Collections.sort(list, Comparator.comparingLong(GuildSmailGift::getOverTime));
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new LinkedList<>();
	}

	public String smailGiftListSerialize() {
		JSONArray arr = new JSONArray();
		smailGiftList.stream().map(GuildSmailGift::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public void notifyChanged() {
		entity.notifyUpdate();
		// 全联盟广播刷新上半部分
		HawkProtocol valueOf = bigGfitSyncInfoProtocol();
		GuildService.getInstance().broadcastProtocol(entity.getGuildId(), valueOf);
	}

	private HawkProtocol bigGfitSyncInfoProtocol() {
		PBSyncGuildBigGift.Builder resp = PBSyncGuildBigGift.newBuilder();
		resp.setGift(toPbObj());
		HawkProtocol valueOf = HawkProtocol.valueOf(HP.code.GUILD_BIG_GIFT_SYNC, resp);
		return valueOf;
	}

	public void syncInfo(Player player) {
		player.sendProtocol(bigGfitSyncInfoProtocol());
	}

	/** 增加等级经验 */
	public synchronized void incBigGiftLevelExp(int exp) {
		entity.setBigGiftLevelExp(entity.getBigGiftLevelExp() + exp);
	}

	/**
	 * 随机一个大礼包
	 */
	public synchronized void randomBigGift() {
		AllianceBigGiftLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceBigGiftLevelCfg.class, getLevel());
		int giftId = cfg.rundomBigGift();
		entity.setBigGiftId(giftId);
	}

	/**
	 * 成员领取小礼包 增加大礼包经验(经验满,增加一个特殊的GuildSmailGift)
	 */
	public synchronized void incBigGiftExp(int add) {
		int bigGiftExp = entity.getBigGiftExp() + add;
		AllianceBigGiftCfg bigGiftCfg = getCfg();
		while (bigGiftExp >= bigGiftCfg.getBigGiftExp()) {// 已集满
			randomBigGift(); // 随即新的大礼包
			addSmailGift(bigGiftCfg.getItem(), true); // 给出特殊小礼包
			bigGiftExp = bigGiftExp - bigGiftCfg.getBigGiftExp();
		}
		entity.setBigGiftExp(bigGiftExp);
	}

	public PBGuildBigGift toPbObj() {
		int level = getLevel();
		int levelExp;
		int levelTotalExp;
		AllianceBigGiftLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceBigGiftLevelCfg.class, level);
		if (level == 1) {
			levelExp = entity.getBigGiftLevelExp();
			levelTotalExp = levelCfg.getGiftLevelExp();
		} else {
			int preLevelExp = HawkConfigManager.getInstance().getConfigByKey(AllianceBigGiftLevelCfg.class, level - 1).getGiftLevelExp();
			levelExp = entity.getBigGiftLevelExp() - preLevelExp;
			levelTotalExp = levelCfg.getGiftLevelExp() - preLevelExp;
			levelExp = Math.min(levelExp, levelTotalExp);
		}
		PBGuildBigGift.Builder builder = PBGuildBigGift.newBuilder();
		builder.setLevel(level)
				.setBigGiftLevelExp(levelExp)
				.setBigGiftLevelTotalExp(levelTotalExp)
				.setBigGiftId(entity.getBigGiftId())
				.setBigGiftExp(entity.getBigGiftExp());

		return builder.build();

	}

	public GuildBigGiftEntity getEntity() {
		return entity;
	}

	public void setEntity(GuildBigGiftEntity entity) {
		this.entity = entity;
	}

}
