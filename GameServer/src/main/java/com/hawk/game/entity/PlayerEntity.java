package com.hawk.game.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.item.SpyMarkItem;
import com.hawk.game.util.GameUtil;

/** 玩家基础数据
 *
 * @author hawk */
@Entity
@Table(name = "player")
public class PlayerEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "puid", unique = true, nullable = false)
    @IndexProp(id = 2)
	private String puid = "";

	@Column(name = "openid", unique = true, nullable = false)
    @IndexProp(id = 3)
	private String openid = "";

	@Column(name = "serverId", nullable = false)
    @IndexProp(id = 4)
	private String serverId = "";

	@Column(name = "name", unique = true, nullable = false)
    @IndexProp(id = 5)
	private String name;

	// 体力
	@Column(name = "vit")
    @IndexProp(id = 6)
	private int vit = 0;

	// 体力恢复时间
	@Column(name = "vitTime")
    @IndexProp(id = 7)
	private long vitTime = 0;

	// 头像id
	@Column(name = "icon")
    @IndexProp(id = 8)
	private int icon = 0;

	@Column(name = "iconBuy")
    @IndexProp(id = 9)
	private String iconBuy;

	@Column(name = "battlePoint")
    @IndexProp(id = 10)
	private long battlePoint = 0;

	// 今日获得免费vip点数
	@Column(name = "vipFreePoint")
    @IndexProp(id = 11)
	private int vipFreePoint = 0;

	// vip经验
	@Column(name = "vipExp")
    @IndexProp(id = 12)
	private int vipExp = 0;

	// vip等级
	@Column(name = "vipLevel")
    @IndexProp(id = 13)
	private int vipLevel = 0;

	// vip等级
	@Column(name = "vipFlag")
    @IndexProp(id = 14)
	private int vipFlag = 0;

	// 军衔经验
	@Column(name = "militaryExp")
    @IndexProp(id = 15)
	private int militaryExp;

	// 天赋方案类型
	@Column(name = "talentType")
    @IndexProp(id = 16)
	private int talentType = 0;

	@Column(name = "platform", nullable = false)
    @IndexProp(id = 17)
	private String platform = "";

	@Column(name = "channel", nullable = false)
    @IndexProp(id = 18)
	private String channel = "";

	@Column(name = "channelId", nullable = false)
    @IndexProp(id = 19)
	private String channelId = "";

	@Column(name = "country", nullable = false)
    @IndexProp(id = 20)
	private String country = "";

	@Column(name = "deviceId", nullable = false)
    @IndexProp(id = 21)
	private String deviceId = "";

	// 登录方式，高16位表示时间日期，低16位表示登录方式
	@Column(name = "loginWay")
    @IndexProp(id = 22)
	private int loginWay;

	@Column(name = "lang")
    @IndexProp(id = 23)
	private String lang = "";

	@Column(name = "version")
    @IndexProp(id = 24)
	private String version = "";

	// 禁止登陆到期时间
	@Column(name = "forbidenTime")
    @IndexProp(id = 25)
	private long forbidenTime = 0;

	// 禁言结束时间
	@Column(name = "silentTime")
    @IndexProp(id = 26)
	private long silentTime = 0;

	// 零收益时间
	@Column(name = "zeroEarningTime")
    @IndexProp(id = 27)
	private long zeroEarningTime = 0;

	// 每日重置时间
	@Column(name = "resetTime")
    @IndexProp(id = 28)
	private long resetTime = 0;

	// 登陆时间
	@Column(name = "loginTime")
    @IndexProp(id = 29)
	private long loginTime = 0;

	// 登出时间
	@Column(name = "logoutTime")
    @IndexProp(id = 30)
	private long logoutTime = 0;

	// 登录标记
	@Column(name = "loginMask")
    @IndexProp(id = 31)
	private long loginMask = 0;

	// 活跃标记
	@Column(name = "livelyMask")
    @IndexProp(id = 32)
	private int livelyMask = 0;

	// 最新的全服邮件创建时间
	@Column(name = "lastGmailCtime")
    @IndexProp(id = 33)
	private long lastGmailCtime = 0;

	// 建筑工厂等级提升升级时间 Map<Level, time>
	@Column(name = "factoryUpTime")
    @IndexProp(id = 34)
	private String factoryUpTime;

	// 军队消耗石油的时间
	@Column(name = "oilConsumeTime")
    @IndexProp(id = 35)
	private long oilConsumeTime = 0;

	// 历史累计在线时长（单位秒）
	@Column(name = "onlineTimeHistory")
    @IndexProp(id = 36)
	private int onlineTimeHistory;

	// 当日在线时长，分两段，高15位表示dayOfYear, 低17位表示当天在线总时长单位秒
	@Column(name = "onlineTimeCurDay")
    @IndexProp(id = 37)
	private int onlineTimeCurDay;

	// 侦查标记
	@Column(name = "spyMark")
    @IndexProp(id = 38)
	private String spyMark;
	// 玩家坐标
	@Column(name = "pos")
    @IndexProp(id = 39)
	private String pos;

	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 40)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 41)
	private long updateTime;

	// 记录是否有效
	@Column(name = "invalid")
    @IndexProp(id = 42)
	private boolean invalid;

	// 记录是否是活跃玩家
	@Column(name = "isActive")
    @IndexProp(id = 43)
	private boolean isActive = true;
	
	// 激活超级xx
	@Column(name = "superLab")
	@IndexProp(id = 44)
	private int superLab;
	
	// 活动每日重置时间
	@Column(name = "actResetTime")
    @IndexProp(id = 45)
	private long actResetTime = 0;
	
	// 记录是否是被邀请进来的玩家
	@Column(name = "beInvited")
    @IndexProp(id = 46)
	private boolean beInvited;
	
	@Column(name = "lastLoginTime")
    @IndexProp(id = 47)
	private long lastLoginTime = 0;

	@Column(name = "armourSuit")
    @IndexProp(id = 48)
	private int armourSuit = 1;
	
	@Column(name = "armourSuitCount")
    @IndexProp(id = 49)
	private int armourSuitCount = 1;
	
	// 激活超级xx
	@Column(name = "laboratory")
	@IndexProp(id = 53)
	private int laboratory = 1;
	
	// 解锁装备科技
	@Column(name="unlockEquipResearch")
	@IndexProp(id = 54)
	private int unlockEquipResearch;

	// 历史最大战力
	@Column(name = "maxBattlePoint")
	@IndexProp(id = 55)
	private long maxBattlePoint = 0;

	@Transient
	private String nameEncoded;

	@Transient
	private Map<Integer, Long> factoryLevelUpTimes = new ConcurrentHashMap<>();

	@Transient
	private List<SpyMarkItem> spyMarkInfo = new ArrayList<>();

	@Transient
	private int[] posXY = new int[2];

	public PlayerEntity() {
		this.loginTime = 0;
		this.resetTime = this.loginTime;
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		nameEncoded = GameUtil.getStrEncoded(name);
	}

	public String getNameEncoded() {
		if (nameEncoded == null) {
			nameEncoded = GameUtil.getStrEncoded(name);
		}
		return nameEncoded;
	}

	public void setNameEncoded(String nameEncoded) {
		this.nameEncoded = nameEncoded;
	}

	public long getBattlePoint() {
		return battlePoint;
	}

	public void setBattlePoint(long battlePoint) {
		this.battlePoint = battlePoint;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public int getSuperLab() {
		return superLab;
	}

	public void setSuperLab(int superLab) {
		this.superLab = superLab;
	}

	public int getVipExp() {
		return vipExp;
	}

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}

	public int getVipFreePoint() {
		return vipFreePoint;
	}

	public void setVipFreePoint(int vipFreePoint) {
		this.vipFreePoint = vipFreePoint;
	}

	public int getTalentType() {
		return talentType;
	}

	public void setTalentType(int talentType) {
		this.talentType = talentType;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getLoginWayAndDay() {
		return loginWay;
	}

	public void setLoginWayAndDay(int loginWay) {
		this.loginWay = loginWay;
	}

	public int getLoginWay() {
		int day = (0xffff0000 & loginWay) >> 16;
		int todayOfYear = HawkTime.getYearDay();
		if (todayOfYear != day) {
			return 0;
		}

		return 0x0000ffff & loginWay;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getForbidenTime() {
		return forbidenTime;
	}

	public void setForbidenTime(long forbidenTime) {
		this.forbidenTime = forbidenTime;
	}

	public long getSilentTime() {
		return silentTime;
	}

	public void setSilentTime(long silentTime) {
		this.silentTime = silentTime;
	}

	public long getZeroEarningTime() {
		return zeroEarningTime;
	}

	public void setZeroEarningTime(long zeroEarningTime) {
		this.zeroEarningTime = zeroEarningTime;
	}

	public long getResetTime() {
		return resetTime;
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public long getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(long logoutTime) {
		this.logoutTime = logoutTime;
	}

	public long getLoginMask() {
		return loginMask;
	}

	public void setLoginMask(long loginMask) {
		this.loginMask = loginMask;
	}

	public int getVit() {
		return vit;
	}

	public void setVit(int vit) {
		this.vit = vit;
	}

	public long getVitTime() {
		return vitTime;
	}

	public void setVitTime(long vitTime) {
		this.vitTime = vitTime;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getIconBuy() {
		return iconBuy;
	}

	public void setIconBuy(String iconBuy) {
		this.iconBuy = iconBuy;
	}

	public long getLastGmailCtime() {
		return lastGmailCtime;
	}

	public void setLastGmailCtime(long lastGmailCtime) {
		this.lastGmailCtime = lastGmailCtime;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public Map<Integer, Long> getFactoryLevelUpTimes() {
		return factoryLevelUpTimes;
	}

	public void addFactoryLevelUpTime(int level, long time) {
		factoryLevelUpTimes.put(level, time);
		notifyUpdate();
	}

	public int getOnlineTimeHistory() {
		return onlineTimeHistory;
	}

	public void setOnlineTimeHistory(int onlineTimeHistory) {
		this.onlineTimeHistory = onlineTimeHistory;
	}

	public int getOnlineTimeCurDay() {
		return onlineTimeCurDay;
	}

	public void setOnlineTimeCurDay(int onlineTimeCurDay) {
		this.onlineTimeCurDay = onlineTimeCurDay;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public long getOilConsumeTime() {
		if (oilConsumeTime == 0) {
			oilConsumeTime = HawkTime.getMillisecond();
		}
		return oilConsumeTime;
	}

	public void setOilConsumeTime(long oilConsumeTime) {
		this.oilConsumeTime = oilConsumeTime;
	}

	@Override
	public void beforeWrite() {
		this.factoryUpTime = JSON.toJSONString(factoryLevelUpTimes);
		this.pos = posXY[0] + "," + posXY[1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterRead() {
		factoryLevelUpTimes = new ConcurrentHashMap<>();
		if (HawkOSOperator.isEmptyString(this.factoryUpTime) == false) {
			@SuppressWarnings("rawtypes")
			Map timeMap = JSON.parseObject(this.factoryUpTime, Map.class);
			this.factoryLevelUpTimes.putAll(timeMap);
		}

		if (!HawkOSOperator.isEmptyString(pos)) {
			posXY = new int[2];
			String[] posSplit = pos.split(",");
			posXY[0] = Integer.parseInt(posSplit[0]);
			posXY[1] = Integer.parseInt(posSplit[1]);
		}
		super.afterRead();
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getLivelyMask() {
		return livelyMask;
	}

	public void setLivelyMask(int livelyMask) {
		this.livelyMask = livelyMask;
	}

	public int getMilitaryExp() {
		return militaryExp;
	}

	public void setMilitaryExp(int militaryExp) {
		this.militaryExp = militaryExp;
	}

	public int getVipFlag() {
		return vipFlag;
	}

	public void setVipFlag(int vipFlag) {
		this.vipFlag = vipFlag;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	public long getActResetTime() {
		return actResetTime;
	}

	public void setActResetTime(long actResetTime) {
		this.actResetTime = actResetTime;
	}
	
	public boolean isBeInvited() {
		return beInvited;
	}

	public void setBeInvited(boolean beInvited) {
		this.beInvited = beInvited;
	}

	public int getArmourSuit() {
		if (armourSuit == 0) {
			armourSuit = 1;
		}
		return armourSuit;
	}

	public void setArmourSuit(int armourSuit) {
		this.armourSuit = armourSuit;
	}

	public int getArmourSuitCount() {
		if (armourSuitCount == 0) {
			armourSuitCount = 1;
		}
		return armourSuitCount;
	}

	public void setArmourSuitCount(int armourSuitCount) {
		this.armourSuitCount = armourSuitCount;
	}

	/** 更新玩家坐标
	 * 
	 * @param x
	 * @param y */
	public void updatePos(int x, int y) {
		posXY[0] = x;
		posXY[1] = y;
		notifyUpdate();
	}

	/** 获取侦查标记
	 * 
	 * @return */
	public List<SpyMarkItem> getSpyMarkInfo() {
		return spyMarkInfo;
	}

	/** 移除过期的侦查标记
	 * 
	 * @param current
	 * @param overTime */
	public void removeOverTimeSpyMark(int current, int overTime) {
		Iterator<SpyMarkItem> iterator = spyMarkInfo.iterator();
		while (iterator.hasNext()) {
			SpyMarkItem next = iterator.next();
			if (current - next.getStartTime() < overTime) {
				continue;
			}
			iterator.remove();
		}
	}

	/** 添加侦查标记
	 * 
	 * @param spyMark */
	public void addSpyMark(SpyMarkItem spyMark) {
		for (SpyMarkItem info : spyMarkInfo) {
			if (info.getPointId() == spyMark.getPointId()) {
				info.setMailId(spyMark.getMailId());
				info.setMarchId(spyMark.getMarchId());
				info.setStartTime(spyMark.getStartTime());
				notifyUpdate();
				return;
			}
		}
		spyMarkInfo.add(spyMark);
	}
	
	/**注册的时候AccountInfo还没有写入,所以需要特殊处理一下. 
	 * {@link GsApp#doLoginProcess(org.hawk.net.session.HawkSession, org.hawk.net.protocol.HawkProtocol, long)}
	 * */
	public String getOwnerKey() {
		//相等的时候即为创建的时候.而PlayerEntity是不会在跨服创建的.
		if (createTime == updateTime) {
			return null;
		} else {
			return id;
		}
	}

	public int getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(int laboratory) {
		this.laboratory = laboratory;
	}

	public int getUnlockEquipResearch() {
		return unlockEquipResearch;
	}

	public void setUnlockEquipResearch(int unlockEquipResearch) {
		this.unlockEquipResearch = unlockEquipResearch;
	}

	public long getMaxBattlePoint() {
		return maxBattlePoint;
	}

	public void setMaxbattlePoint(long maxBattlePoint) {
		this.maxBattlePoint = maxBattlePoint;
	}
}