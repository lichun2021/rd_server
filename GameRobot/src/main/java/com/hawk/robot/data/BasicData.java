package com.hawk.robot.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Consume.ConsumeItem;
import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.protocol.Equip.EquipInfo;
import com.hawk.game.protocol.Equip.PBEquipSlot;
import com.hawk.game.protocol.Friend.FriendApplyMsg;
import com.hawk.game.protocol.Friend.FriendMsg;
import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGift;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Item.HPSyncGiftInfoResp;
import com.hawk.game.protocol.Item.HotSalesInfo;
import com.hawk.game.protocol.Item.ItemInfo;
import com.hawk.game.protocol.Item.VipExclusiveBox;
import com.hawk.game.protocol.Item.VipShopItem;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.President.GiftInfo;
import com.hawk.game.protocol.President.MemeberInfo;
import com.hawk.game.protocol.President.OfficerInfo;
import com.hawk.game.protocol.PushGift.PushGiftMsg;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Reward.HPPlayerReward;
import com.hawk.game.protocol.Talent.HPTalentInfoSync;
import com.hawk.game.protocol.Talent.HPTalentUpgradeResp;
import com.hawk.game.protocol.Talent.TalentInfo;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.config.TalentCfg;
import com.hawk.robot.config.TalentLevelCfg;

public class BasicData {
	/**
	 * 机器人信息(上层数据)
	 */
	protected GameRobotData robotData;
	/**
	 * 玩家属性数据
	 */
	protected PlayerAttrInfo playerInfo = new PlayerAttrInfo();
	/**
	 * 商品热卖信息
	 */
	private HotSalesInfo hotSaleInfo;
	/**
	 * 付费队列可用状态结束时间
	 */
	private long paidQueueEnableEndTime;
	/**
	 * 队列对象列表
	 */
	protected Map<String, QueuePB> queueObjects = new ConcurrentHashMap<String, QueuePB>();
	/**
	 * 物品道具信息
	 */
	protected Map<String, ItemInfo> itemObjects = new ConcurrentHashMap<String, ItemInfo>();
	/**
	 * vip商城商品数据<shopId, VipShopItem>
	 */
	protected Map<Integer, VipShopItem> vipShopItems = new ConcurrentHashMap<Integer, VipShopItem>();
	/**
	 * vip专属礼包信息<vip等级，是否已购买过>
	 */
	protected Map<Integer, Boolean> vipExclusiveBoxStates = new ConcurrentHashMap<Integer, Boolean>();
	/**
	 * 未领取过的vip福利礼包数据
	 */
	protected List<Integer> unreceivedBenefitBoxes = new CopyOnWriteArrayList<Integer>();
	/**
	 * 当前等级的vip福利礼包是否已领取过
	 */
	protected boolean curLevelBenefitBoxTaken;
	
	/**
	 * 礼包ID
	 */
	protected List<Integer> onSellGifts = new CopyOnWriteArrayList<>();
	/**
	 * 天赋信息
	 */
	protected Map<Integer, TalentInfo> talentObjects = new ConcurrentHashMap<Integer, TalentInfo>();
	/**
	 * 邮件数据
	 */
	protected Map<String, MailLiteInfo> mailObjects = new ConcurrentHashMap<String, MailLiteInfo>();
	
	/**英雄数据*/
	private Map<Integer,PBHeroInfo> heroInfo= new HashMap<>();
	
	/**联盟大礼包数据*/
	private Map<String,PBPlayerGuildGift> guildGiftInfo= new HashMap<>();
	
	/**
	 * 指挥官装备位信息
	 */
	private Map<Integer, PBEquipSlot> commanderEquipSlots = new HashMap<>();
	/**
	 * 超值礼包
	 */
	private HPSyncGiftInfoResp.Builder giftBuilder;
	/**
	 * 指挥官装备位信息
	 */
	private Map<String, EquipInfo> equipInfos = new HashMap<>();
	
	private int talentType = 0;
	
	private List<Integer> unlockedTalents = new CopyOnWriteArrayList<>();
	
	private List<FriendApplyMsg> friendApplyMsg = new CopyOnWriteArrayList<FriendApplyMsg>();
	
	private List<FriendMsg> friendMsg = new CopyOnWriteArrayList<FriendMsg>();
	/**
	 * 国王战部分查询玩家
	 */
	private List<MemeberInfo> memberInfoList = new CopyOnWriteArrayList<>();
	/**
	 * 官职信息
	 */
	private List<OfficerInfo.Builder> officerList = new CopyOnWriteArrayList<>();
	/**
	 * 国王战的礼包信息
	 */
	private List<GiftInfo> giftList = new CopyOnWriteArrayList<>();
	/**
	 * 上一次搜索玩家的时间
	 */
	private long lastSeTime;
	/**
	 * 黑市商人.
	 */
	private TravelShopInfoSync.Builder travelShop;
	/**
	 * 上一次购买黑市商人的配置ID
	 */
	private int lastTravelShopBuyId;
	/**
	 * 推送礼包
	 */
	private List<PushGiftMsg> pushGiftList; 
	
	/***
	 * 头像列表
	 */
	private List<Integer> imageList;
	
	/***
	 * 头像框列表
	 */
	private List<Integer> circleList;
	
	///////////////////////////////////////////////////////////////////////////////////////
	//
	///////////////////////////////////////////////////////////////////////////////////////
	
	public List<GiftInfo> getGiftList() {
		return giftList;
	}

	public void setGiftList(List<GiftInfo> giftList) {
		this.giftList = giftList;
	}

	public List<OfficerInfo.Builder> getOfficerList() {
		return officerList;
	}

	public void setOfficerList(List<OfficerInfo.Builder> officerList) {
		this.officerList = officerList;
	}
	
	public void setMemberInfoList(List<MemeberInfo> memberList) {
		this.memberInfoList = memberList;
	}
	public List<MemeberInfo> getMemberInfoList() {
		return memberInfoList;
	}

	public int getTalentType() {
		return talentType;
	}

	public void setTalentType(int talentType) {
		this.talentType = talentType;
	}

	public BasicData(GameRobotData gameRobotData) {
		robotData = gameRobotData;
	}
	
	public GameRobotData getRobotData() {
		return robotData;
	}


	public PlayerAttrInfo getPlayerInfo() {
		return playerInfo;
	}

	public void updatePlayerInfo(PlayerInfo playerInfo) {
		this.playerInfo.reset(playerInfo);
	}
	
	public void updatePlayerInfo(HPPlayerReward rewardInfo) {
		this.playerInfo.reset(rewardInfo);
	}
	
	public void updatePlayerInfo(SyncAttrInfo consumeAfterInfo) {
		this.playerInfo.reset(consumeAfterInfo);
	}
	
	/**
	 * 设置热卖商品信息
	 * @param hotSaleInfo
	 */
	public void setHotSaleInfo(HotSalesInfo hotSaleInfo) {
		this.hotSaleInfo = hotSaleInfo;
	}

	public HotSalesInfo getHotSaleInfo() {
		return hotSaleInfo;
	}
	
	public Map<String, QueuePB> getQueueObjects() {
		return queueObjects;
	}

	public Map<String, ItemInfo> getItemObjects() {
		return itemObjects;
	}
	
	public Optional<ItemInfo> getItemInfo(int itemId){
		return itemObjects.values().stream().filter(e->e.getItemId() == itemId).findAny();
	}
	
	public Map<Integer, TalentInfo> getTalentObjects() {
		return talentObjects;
	}

	public void resetPaidQueueEndTime(long endTime) {
		paidQueueEnableEndTime = endTime;
	}
	
	public long getPaidQueueEnableEndTime() {
		return paidQueueEnableEndTime;
	}

	/**
	 * 刷新队列数据
	 * @param queueList
	 */
	public void refreshQueueData(QueuePB... queueList) {
		if(queueList != null && queueList.length > 0) {
			for(QueuePB queuePB : queueList) {
				queueObjects.put(queuePB.getId(), queuePB);
			}
		}
	}
	
	/**
	 * 删除队列
	 * @param deleteQueueId
	 */
	public void deleteQueue(String deleteQueueId) {
		queueObjects.remove(deleteQueueId);
	}
	
	public boolean hasFreeBuildingQueue(BuildingCfg cfg) {
		int count = 0;
		for(QueuePB queue : queueObjects.values()) {
			if(queue.getQueueType() == QueueType.BUILDING_QUEUE) {
				count++;
			}
		}
		
		if (count > 1 || (count == 1 && getPaidQueueEnableEndTime() - HawkTime.getMillisecond() < cfg.getBuildTime() * 1000)) {
			return false;
		}
		
		return true;
	}

	/**
	 * 刷新物品道具数据
	 * @param itemInfoList
	 */
	public void refreshItemData(List<ItemInfo> itemInfoList) {
		for(ItemInfo itemInfo : itemInfoList) {
			itemObjects.put(itemInfo.getUuid(), itemInfo);
		}
	}
	
	/**
	 * 更新vip商城商品数据
	 * 
	 * @param vipShopItemList
	 */
	public void refreshVipShopItemData(List<VipShopItem> vipShopItemList) {
		for (VipShopItem vipShopItem : vipShopItemList) {
			vipShopItems.put(vipShopItem.getShopId(), vipShopItem);
		}
	}
	
	/**
	 * 获取vip商城商品数据
	 * 
	 * @return
	 */
	public Collection<VipShopItem> getVipShopItems() {
		return vipShopItems.values();
	}
	
	/**
	 * 刷新vip专属礼包状态数据
	 * 
	 * @param exclusiveBoxList
	 */
	public void refreshVipExclusiveBox(List<VipExclusiveBox> exclusiveBoxList) {
		for (VipExclusiveBox box : exclusiveBoxList) {
			vipExclusiveBoxStates.put(box.getVipLevel(), box.getBought());
		}
	}
	
	public Map<Integer, Boolean> getVipExclusiveBoxStates() {
		return vipExclusiveBoxStates;
	}
	
	/**
	 * 刷新vip福利礼包数据
	 * 
	 * @param unreceivedBenefitBoxes
	 * @param curLevelBenefitBoxState
	 */
	public void refreshVipBenefitBox(List<Integer> unreceivedBoxes, int curLevelBenefitBoxState) {
		if (!unreceivedBoxes.isEmpty()) {
			unreceivedBenefitBoxes.clear();
			unreceivedBenefitBoxes.addAll(unreceivedBoxes);
		}
		
		if (curLevelBenefitBoxState >= 0) {
			curLevelBenefitBoxTaken = (curLevelBenefitBoxState == 1);
		}
	}
	
	public List<Integer> getUnreceivedBenefitBoxes() {
		return unreceivedBenefitBoxes;
	}
	
	public boolean getCurLevelBenefitBoxState() {
		return curLevelBenefitBoxTaken;
	}
	
	
	/**
	 * 物品消耗
	 * @param itemList
	 */
	public void consumeItems(List<ConsumeItem> itemList) {
		if (itemList == null || itemList.size() == 0) {
			return;
		}
		
		for(ConsumeItem consumeItem : itemList) {
			ItemInfo item = itemObjects.get(consumeItem.getId());
			if (item != null) {
				int itemCount = item.getCount() - consumeItem.getCount();
				item = item.toBuilder().setCount(itemCount).build();
			}
			itemObjects.put(consumeItem.getId(), item);
		}
	}

	/**
	 * 刷新天赋数据
	 * @param talentInfoList
	 */
	public void refreshTalentData(HPTalentInfoSync talentInfo) {
		List<TalentInfo> talentInfoList = talentInfo.getTalentInfosList();
		if (talentType != talentInfo.getType()) {
			talentObjects.clear();
			setTalentType(talentInfo.getType());
		}
		
		if(talentInfoList != null && talentInfoList.size() > 0) {
			for(TalentInfo talent : talentInfoList) {
				talentObjects.put(talent.getTalentId(), talent);
				unlockedTalents.remove(Integer.valueOf(talent.getTalentId()));
			}
		}
	}
	
	/**
	 * 天赋升级后更新天赋数据
	 * @param resp
	 */
	public void updateTalentData(HPTalentUpgradeResp resp) {
		TalentInfo talentInfo = talentObjects.get(resp.getTalentId());
		if (talentInfo == null) {
			talentInfo = TalentInfo.newBuilder()
					.setTalentId(resp.getTalentId())
					.setType(resp.getType())
					.setLevel(resp.getLevel())
					.setId(HawkOSOperator.randomUUID())
					.build();
		} else {
			talentInfo = talentInfo.toBuilder().setLevel(resp.getLevel()).build();
		}
		
		talentObjects.put(resp.getTalentId(), talentInfo);
		unlockedTalents.remove(Integer.valueOf(resp.getTalentId()));

		int maxLevel = TalentLevelCfg.getMaxLevelByTalentId(resp.getTalentId());
		// 解锁新的天赋
		if (resp.getLevel() >= maxLevel) {
			Integer newTalentId = TalentCfg.getTalentIdByFront(resp.getTalentId());
			if (newTalentId != null) {
				unlockedTalents.add(newTalentId);
			}
		}
	}
	
	/**
	 * 天赋洗点后清除天赋数据
	 */
	public void clearTalentData() {
		talentObjects.clear();
		unlockedTalents.clear();
	}
	
	public Map<String, MailLiteInfo> getMailObjects() {
		return mailObjects;
	}
	
	/**
	 * 刷新邮件数据
	 * @param liteInfos
	 */
	public void refreshMailData(List<MailLiteInfo> liteInfos) {
		
		if(mailObjects != null) {
			for(MailLiteInfo liteInfo : liteInfos) {
				mailObjects.put(liteInfo.getId(), liteInfo);
			}
		}
	}

	public Map<Integer, PBHeroInfo> getHeroInfo() {
		return heroInfo;
	}

	public void addHeroInfo(PBHeroInfo heroInfo) {
		this.heroInfo.put(heroInfo.getHeroId(), heroInfo);
	}
	
	public Map<String, PBPlayerGuildGift> getGuildGiftInfo() {
		return guildGiftInfo;
	}

	public void setGuildGiftInfo(Map<String, PBPlayerGuildGift> guildGiftInfo) {
		this.guildGiftInfo = guildGiftInfo;
	}

	public List<Integer> getUnlockedTalents() {
		return unlockedTalents;
	}

	public void refreshGiftInfo(HPSyncGiftInfoResp giftInfo) {
		onSellGifts.clear();
		onSellGifts.addAll(giftInfo.getGiftsOnSellList());
	}
	
	public List<Integer> getOnSellGifts() {
		return onSellGifts;
	}

	public List<FriendApplyMsg> getFriendApplyMsg() {
		return friendApplyMsg;
	}

	public void setFriendApplyMsg(List<FriendApplyMsg> friendApplyMsg) {
		this.friendApplyMsg = friendApplyMsg;
	}

	public List<FriendMsg> getFriendMsg() {
		return friendMsg;
	}

	public void setFriendMsg(List<FriendMsg> friendMsg) {
		this.friendMsg = friendMsg;
	}

	public long getLastSeTime() {
		return lastSeTime;
	}

	public void setLastSeTime(long lastSeTime) {
		this.lastSeTime = lastSeTime;
	}

	public List<EquipInfo> getEquipList() {
		List<EquipInfo> list = new ArrayList<>();
		list.addAll(equipInfos.values());
		return list;
	}

	public void updateEquipInfo(List<EquipInfo> equipList) {
		for(EquipInfo info : equipList){
			equipInfos.put(info.getId(), info);
		}
	}
	
	public void removeEquip(List<String> removeIds){
		for(String id : removeIds){
			equipInfos.remove(id);
		}
	}

	public List<PBEquipSlot> getCommanderEquipSlotList() {
		List<PBEquipSlot> list = new ArrayList<>();
		list.addAll(commanderEquipSlots.values());
		return list;
	}

	public void updateCommanderEquipInfo(List<PBEquipSlot> equipSlotList) {
		for(PBEquipSlot slot : equipSlotList){
			commanderEquipSlots.put(slot.getPos(), slot);
		}
	}

	public TravelShopInfoSync.Builder getTravelShop() {
		return travelShop;
	}

	public void setTravelShop(TravelShopInfoSync.Builder travelShop) {
		this.travelShop = travelShop;
	}

	public int getLastTravelShopBuyId() {
		return lastTravelShopBuyId;
	}

	public void setLastTravelShopBuyId(int lastTravelShopBuyId) {
		this.lastTravelShopBuyId = lastTravelShopBuyId;
	}

	public List<PushGiftMsg> getPushGiftList() {
		return pushGiftList;
	}

	public void setPushGiftList(List<PushGiftMsg> pushGiftList) {
		if (this.pushGiftList == null) {
			this.pushGiftList = new ArrayList<>();
		} else {
			this.pushGiftList.clear();
		}
		
		this.pushGiftList.addAll(pushGiftList);
	}

	public HPSyncGiftInfoResp.Builder getGiftBuilder() {
		return giftBuilder;
	}

	public void setGiftBuilder(HPSyncGiftInfoResp.Builder giftBuilder) {
		this.giftBuilder = giftBuilder;
	}

	public List<Integer> getImageList() {
		return imageList;
	}

	public void setImageList(List<Integer> imageList) {
		this.imageList = imageList;
	}

	public List<Integer> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<Integer> circleList) {
		this.circleList = circleList;
	}
}
