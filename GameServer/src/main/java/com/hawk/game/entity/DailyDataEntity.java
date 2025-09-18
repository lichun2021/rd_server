package com.hawk.game.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 每日数据实体
 * @author golden
 *
 */
@Entity
@Table(name = "daily_data")
public class DailyDataEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = "";
	
	/**
	 *黑市商人的刷新次数
	 *最开始是每日重置，后面是根据系统刷新重置，怕策划需求变动，暂时先放这.
	 */
	@Column(name ="travelShopRefreshTimes", nullable = false)
    @IndexProp(id = 2)
	private int travelShopRefreshTimes;
	
	/** 领取好友宝箱次数*/
	@Column(name = "dailyFriendBoxTimes", nullable = false)
    @IndexProp(id = 3)
	private String dailyFriendBoxTimes;
	
	/** 每日接受联盟邀请函推送次数*/
	@Column(name = "guildPushTimes", nullable = false)
    @IndexProp(id = 4)
	private int guildPushTimes;
	
	/** 死盟玩家拒绝联盟推荐次数(死盟玩家换盟需要清理此值为0) **/
	@Column(name = "deadGuildRefuseRecommendCnt", nullable = false)
    @IndexProp(id = 5)
	private int deadGuildRefuseRecommendCnt;
	
	/** 邀请函上次推送时间*/
	@Column(name = "lastPushTime", nullable = false)
    @IndexProp(id = 6)
	private long lastPushTime;

	/**重置时间*/
	@Column(name = "resetTime", nullable = false)
    @IndexProp(id = 7)
	protected long resetTime = 0;
	
	@Column(name ="travelGiftBuyTimes")
    @IndexProp(id = 8)
	protected int travelGiftBuyTimes;
	/**
	 * vip的黑市礼包购买次数
	 */
	@Column(name="vipTravelGiftBuyTimes")
    @IndexProp(id = 9)
	protected int vipTravelGiftBuyTimes;

	/**
	 * 军衔津贴是否已领取
	 */
	@Column(name ="isMilitaryRankRecieve")
    @IndexProp(id = 10)
	protected boolean isMilitaryRankRecieve;
	
	/**
	 * 攻击迷雾要塞胜利次数（发起集结行军）
	 */
	@Column(name ="attackFoggyWinTimes")
    @IndexProp(id = 11)
	protected int attackFoggyWinTimes;
	
	/**
	 * 玩家今日参与英雄试练领奖次数
	 */
	@Column(name ="crRewardTimes")
    @IndexProp(id = 12)
	private int crRewardTimes;
	
	/**
	 * 今日英雄试练最高积分
	 */
	@Column(name ="crHighestScore")
    @IndexProp(id = 13)
	private int crHighestScore;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 14)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 15)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 16)
	protected boolean invalid;
	/**
	 * 守护的礼物信息
	 */
	@Column(name = "guardGift")
	@IndexProp(id = 17)
	private String guardGift;

	@Column(name = "ghostBox")
    @IndexProp(id = 18)
	private int ghostBox;		
	
	@Column(name = "resCollDropTimes")
    @IndexProp(id = 21)
	private int resCollDropTimes;
	/**
	 * 记录每天的特惠商人购买物品
	 */
	@Column(name = "travelShopInfo")
	@IndexProp(id = 22)
	private String travelShopInfo;
	
	/**
	 * 装备星级属性随机次数
	 */
	@Column(name = "armourStarAttrTimes")
    @IndexProp(id = 23)
	private int armourStarAttrTimes;
	
	/**
	 * 国家任务每日购买次数
	 */
	@Column(name = "nationMissionDayBuyTimes")
    @IndexProp(id = 24)
	private int nationMissionDayBuyTimes;
	
	/**
	 * 国家飞船制造厂助力
	 */
	@Column(name = "nationShipAssist")
    @IndexProp(id = 25)
	private int nationShipAssist;
	
	/**
	 * 国家科技是否帮助过
	 */
	@Column(name = "nationTechHelp")
    @IndexProp(id = 26)
	private int nationTechHelp;
	
	/**
	 * 国家科技是否提醒通知过
	 */
	@Column(name = "nationTechNotice")
    @IndexProp(id = 27)
	private int nationTechNotice;
	
	/**
	 * 国家科技技能掉落次数
	 */
	@Column(name = "nationSkillDropTimes")
    @IndexProp(id = 28)
	private int nationSkillDropTimes;
	
	/**
	 * 攻击迷雾要塞胜利次数（参与集结行军）
	 */
	@Column(name ="joinAtkFoggyWinTimes")
    @IndexProp(id = 29)
	protected int joinAtkFoggyWinTimes;
	

	/** 领取好友宝箱次数*/
	@Transient
	private Map<Integer, Integer> friendBox = new HashMap<Integer, Integer>();
	
	/**
	 * 特惠商人的购买信息.
	 */
	@Transient
	private Map<Integer, Integer> travelShopInfoMap = new HashMap<>();	

	/**
	 * {@link #guardGift}
	 */
	@Transient
	private Map<Integer, Integer> guardGiftMap = new HashMap<>();
	
	public DailyDataEntity(String playerId) {
		this.playerId = playerId;
		this.dailyFriendBoxTimes = "";
		this.resetTime = HawkTime.getMillisecond();
		this.isMilitaryRankRecieve = false;
	}
	
	public DailyDataEntity() {
		
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getDailyFriendBoxTimes() {
		return dailyFriendBoxTimes;
	}

	public void setDailyFriendBoxTimes(String dailyFriendBoxTimes) {
		this.dailyFriendBoxTimes = dailyFriendBoxTimes;
	}
	
	public int getGuildPushTimes() {
		return guildPushTimes;
	}

	public void setGuildPushTimes(int guildPushTimes) {
		this.guildPushTimes = guildPushTimes;
	}

	public long getLastPushTime() {
		return lastPushTime;
	}

	public void setLastPushTime(long lastPushTime) {
		this.lastPushTime = lastPushTime;
	}

	public long getResetTime() {
		return resetTime;
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	/**
	 * 获取领取好友礼包次数
	 * @param id
	 * @return
	 */
	public int getFriendBoxTimes(int id) {
		Integer num = friendBox.get(id);
		return num == null ? 0 : num;
	}
	
	public Map<Integer, Integer> getFriendBoxMap() {
		return friendBox;
	}
	
	/**
	 * 增加领取好友礼包次数
	 * @param id
	 * @return
	 */
	public void addFriendBoxTimes(int id, int addTimes) {
		if (!friendBox.containsKey(id)) {
			friendBox.put(id, addTimes);
		} else {
			int curTimes = friendBox.get(id);
			friendBox.put(id, curTimes + addTimes);
		}
		notifyUpdate();
	}
	
	public int getTravelShopRefreshTimes() {
		return travelShopRefreshTimes;
	}

	public void setTravelShopRefreshTimes(int travelShopRefreshTimes) {
		this.travelShopRefreshTimes = travelShopRefreshTimes;
	}

	
	public int getMassAtkFoggyWinTimes() {
		return attackFoggyWinTimes;
	}

	public void setMassAtkFoggyWinTimes(int attackFoggyWinTimes) {
		this.attackFoggyWinTimes = attackFoggyWinTimes;
	}
	
	public void addMassAtkFoggyWinTimes(int add) {
		setMassAtkFoggyWinTimes(attackFoggyWinTimes+add);
	}
	
	public int getJoinAtkFoggyWinTimes() {
		return joinAtkFoggyWinTimes;
	}
	
	public void setJoinAtkFoggyWinTimes(int joinAtkFoggyWinTimes) {
		this.joinAtkFoggyWinTimes = joinAtkFoggyWinTimes;
	}
	
	public void addJoinAtkFoggyWinTimes(int add) {
		setJoinAtkFoggyWinTimes(joinAtkFoggyWinTimes+add);
	}

	
	public int getNationShipAssist() {
		return nationShipAssist;
	}

	public void setNationShipAssist(int nationShipAssist) {
		this.nationShipAssist = nationShipAssist;
	}

	/**
	 * 清除
	 */
	public void clear() {
		this.friendBox = new HashMap<Integer, Integer>();
		setResetTime(HawkTime.getMillisecond());
		this.travelGiftBuyTimes = 0;
		this.guildPushTimes = 0;
		this.isMilitaryRankRecieve = false;
		this.vipTravelGiftBuyTimes = 0;
		this.attackFoggyWinTimes = 0;
		this.joinAtkFoggyWinTimes = 0;
		this.crRewardTimes = 0;
		this.crHighestScore = 0;
		this.guardGiftMap = new HashMap<>();
		this.ghostBox = 0;		
		this.resCollDropTimes = 0;
		this.travelShopInfoMap = new HashMap<>();
		this.armourStarAttrTimes = 0;
		this.nationMissionDayBuyTimes = 0;
		this.nationShipAssist = 0;
		this.nationTechHelp = 0;
		this.nationTechNotice = 0;
		this.nationSkillDropTimes = 0;
	}
	
	@Override
	public void afterRead() {
		this.friendBox = SerializeHelper.stringToMap(this.dailyFriendBoxTimes, Integer.class, Integer.class);
		this.guardGiftMap = SerializeHelper.stringToMap(this.guardGift, Integer.class, Integer.class);
		this.travelShopInfoMap = SerializeHelper.stringToMap(this.travelShopInfo, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {
		this.dailyFriendBoxTimes = SerializeHelper.mapToString(this.friendBox);
		this.guardGift = SerializeHelper.mapToString(this.guardGiftMap);
		this.travelShopInfo = SerializeHelper.mapToString(this.travelShopInfoMap); 
	}

	public int getTravelGiftBuyTimes() {
		return travelGiftBuyTimes;
	}

	public void setTravelGiftBuyTimes(int travelGiftBuyTimes) {
		this.travelGiftBuyTimes = travelGiftBuyTimes;
	}

	public boolean isMilitaryRankRecieve() {
		return isMilitaryRankRecieve;
	}

	public void setMilitaryRankRecieve(boolean isMilitaryRankRecieve) {
		this.isMilitaryRankRecieve = isMilitaryRankRecieve;
	}

	public int getVipTravelGiftBuyTimes() {
		return vipTravelGiftBuyTimes;
	}

	public void setVipTravelGiftBuyTimes(int vipTravelGiftBuyTimes) {
		this.vipTravelGiftBuyTimes = vipTravelGiftBuyTimes;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		playerId = primaryKey;
	}

	public int getDeadGuildRefuseRecommendCnt() {
		return deadGuildRefuseRecommendCnt;
	}

	public void setDeadGuildRefuseRecommendCnt(int deadGuildRefuseRecommendCnt) {
		this.deadGuildRefuseRecommendCnt = deadGuildRefuseRecommendCnt;
	}

	public int getCrRewardTimes() {
		return crRewardTimes;
	}

	public void setCrRewardTimes(int crRewardTimes) {
		this.crRewardTimes = crRewardTimes;
	}

	public int getCrHighestScore() {
		return crHighestScore;
	}

	public void setCrHighestScore(int crHighestScore) {
		this.crHighestScore = crHighestScore;
	}
	
	public int getGhostBox() {
		return ghostBox;
	}

	public void setGhostBox(int ghostBox) {
		this.ghostBox = ghostBox;
	}

	public String getOwnerKey() {
		return playerId;
	}
	
	public Map<Integer, Integer> getGuardGiftMap() {
		return this.guardGiftMap;
	}
	
	public int getGuardGiftNum(int giftId) {
		return this.guardGiftMap.getOrDefault(giftId, 0);
	}
	
	public void addGuardGiftNum(int giftId, int num) {
		int oldNum = this.getGuardGiftNum(giftId);
		this.guardGiftMap.put(giftId, oldNum + num);
		
		this.notifyUpdate();
	}
	public int getResCollDropTimes() {
		return resCollDropTimes;
	}

	public void setResCollDropTimes(int resCollDropTimes) {
		this.resCollDropTimes = resCollDropTimes;
	}
	
	public Map<Integer, Integer> getTravelShopInfoMap() {
		return travelShopInfoMap;
	}
	
	public void addTravelShopBoughtInfo(int giftId) {
		int oldNum = this.travelShopInfoMap.getOrDefault(giftId, 0);
		travelShopInfoMap.put(giftId, oldNum + 1);
		this.notifyUpdate();
	}

	public int getArmourStarAttrTimes() {
		return armourStarAttrTimes;
	}

	public void addArmourStarAttrTimes() {
		this.armourStarAttrTimes++;
		notifyUpdate();
	}

	public int getNationMissionDayBuyTimes() {
		return nationMissionDayBuyTimes;
	}

	public void addNationMissionDayBuyTimes() {
		this.nationMissionDayBuyTimes++;
		notifyUpdate();
	}

	public int getNationTechHelp() {
		return nationTechHelp;
	}

	public void setNationTechHelp(int nationTechHelp) {
		this.nationTechHelp = nationTechHelp;
	}

	public int getNationTechNotice() {
		return nationTechNotice;
	}

	public void setNationTechNotice(int nationTechNotice) {
		this.nationTechNotice = nationTechNotice;
	}

	public int getNationSkillDropTimes() {
		return nationSkillDropTimes;
	}

	public void setNationSkillDropTimes(int nationSkillDropTimes) {
		this.nationSkillDropTimes = nationSkillDropTimes;
	}
}
