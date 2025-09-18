package com.hawk.activity.type.impl.exchangeDecorate.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateLevelExpCfg;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 累计登录活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_exchange_decorate")
public class ActivityExchangeDecorateEntity extends HawkDBEntity implements IActivityDataEntity{
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	//等级
    @IndexProp(id = 4)
	@Column(name = "level", nullable = false)
	private int level;
	
	//经验
    @IndexProp(id = 5)
	@Column(name = "exp", nullable = false)
	private int exp;

	//等级奖励数据
    @IndexProp(id = 6)
	@Column(name = "levelReward", nullable = false)
	private String levelReward;
	
	//每日成就
    @IndexProp(id = 7)
	@Column(name = "achieveDayItems", nullable = false)
	private String achieveDayItems;
    @IndexProp(id = 8)
	@Column(name = "achieveDayRefreshTime", nullable = false)
	private long achieveDayRefreshTime;
	
	//每周成就
    @IndexProp(id = 9)
	@Column(name = "achieveWeekItems", nullable = false)
	private String achieveWeekItems;
    @IndexProp(id = 10)
	@Column(name = "achieveWeekRefreshTime", nullable = false)
	private int achieveWeekRefreshTime;	
	
	//等级开启兑换
    @IndexProp(id = 11)
	@Column(name = "levelOpenExchange", nullable = false)
	private String levelOpenExchange;

	//装扮兑换	
    @IndexProp(id = 12)
	@Column(name = "decorateExchange", nullable = false)
	private String decorateExchange;
	
    @IndexProp(id = 13)
	@Column(name = "loginRefreshTime", nullable = false)
	private long loginRefreshTime;

    @IndexProp(id = 14)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
    @IndexProp(id = 15)
	@Column(name = "weekNum", nullable = false)
	private int weekNum;
	
    @IndexProp(id = 16)
	@Column(name = "weekBuyExpNum", nullable = false)
	private int weekBuyExpNum;
	
    @IndexProp(id = 17)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 18)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 19)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	//日，周列表
	@Transient
	private List<AchieveItem> achieveDayList = new CopyOnWriteArrayList<>();
	@Transient
	private List<AchieveItem> achieveWeekList = new CopyOnWriteArrayList<>();
	//兑换列表
	@Transient
	private List<ExchangeDecorateInfo> decorateList = new CopyOnWriteArrayList<>();
	//已开启等级列表
	@Transient
	private List<ExchangeDecorateInfo> levelRewardList = new CopyOnWriteArrayList<>();
	//由等级开启的购买列表
	@Transient
	private List<ExchangeDecorateLimitInfo> levelOpenExchangeList = new CopyOnWriteArrayList<>();	
	
	public ActivityExchangeDecorateEntity() {
	}
	
	public ActivityExchangeDecorateEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.level = 1;
		this.exp = 0;
		this.levelReward = "";
		this.achieveDayItems = "";
		this.achieveDayRefreshTime = 0;
		this.achieveWeekItems = "";
		this.achieveWeekRefreshTime = 0;
		this.levelOpenExchange = "";
		this.decorateExchange="";
		this.loginRefreshTime = HawkTime.getMillisecond();
		this.loginDays = 1;
		this.weekNum=1;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
	
	public void increaseExp(int exp){
		final int maxLevel = ExchangeDecorateLevelExpCfg.getMaxLevel();
		
		int newExp = getExp() + exp;
		if (getExp() > 0 && newExp < 0) {
			newExp = Integer.MAX_VALUE - 1;
		}
		setExp(newExp);
		ConfigIterator<ExchangeDecorateLevelExpCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecorateLevelExpCfg.class);
		int newLevel = (int) (configIterator.stream()
				.filter(cfg -> cfg.getLevelUpExp() <= getExp()).count() );
		
		if(newLevel!=level){
			this.level = Math.min(maxLevel, newLevel);
		}
	}

	public String getLevelReward() {
		return levelReward;
	}

	public void setLevelReward(String levelReward) {
		this.levelReward = levelReward;
	}

	public String getAchieveDayItems() {
		return achieveDayItems;
	}

	public void setAchieveDayItems(String achieveDayItems) {
		this.achieveDayItems = achieveDayItems;
	}

	public long getAchieveDayRefreshTime() {
		return achieveDayRefreshTime;
	}

	public void setAchieveDayRefreshTime(long achieveDayRefreshTime) {
		this.achieveDayRefreshTime = achieveDayRefreshTime;
	}

	public String getAchieveWeekItems() {
		return achieveWeekItems;
	}

	public void setAchieveWeekItems(String achieveWeekItems) {
		this.achieveWeekItems = achieveWeekItems;
	}

	public int getAchieveWeekRefreshTime() {
		return achieveWeekRefreshTime;
	}

	public void setAchieveWeekRefreshTime(int achieveWeekRefreshTime) {
		this.achieveWeekRefreshTime = achieveWeekRefreshTime;
	}

	public String getLevelOpenExchange() {
		return levelOpenExchange;
	}

	public void setLevelOpenExchange(String levelOpenExchange) {
		this.levelOpenExchange = levelOpenExchange;
	}

	public String getDecorateExchange() {
		return decorateExchange;
	}

	public void setDecorateExchange(String decorateExchange) {
		this.decorateExchange = decorateExchange;
	}

	public List<AchieveItem> getAchieveDayList() {
		return achieveDayList;
	}

	public void setAchieveDayList(List<AchieveItem> achieveDayList) {
		this.achieveDayList = achieveDayList;
	}

	public List<AchieveItem> getAchieveWeekList() {
		return achieveWeekList;
	}

	public void setAchieveWeekList(List<AchieveItem> achieveWeekList) {
		this.achieveWeekList = achieveWeekList;
	}

	public List<AchieveItem> getAchieveList(){
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(this.achieveDayList);
		list.addAll(this.achieveWeekList);
		return list;
	}
	
	public List<ExchangeDecorateInfo> getDecorateList() {
		return decorateList;
	}

	public void setDecorateList(List<ExchangeDecorateInfo> decorateList) {
		this.decorateList = decorateList;
	}

	public List<ExchangeDecorateInfo> getLevelRewardList() {
		return levelRewardList;
	}
	
	public void addLevelReward(ExchangeDecorateInfo info){
		levelRewardList.add(info);
		this.notifyUpdate();
	}

	public void setLevelRewardList(List<ExchangeDecorateInfo> levelRewardList) {
		this.levelRewardList = levelRewardList;
	}

	public List<ExchangeDecorateLimitInfo> getLevelOpenExchangeList() {
		return levelOpenExchangeList;
	}

	public void setLevelOpenExchangeList(List<ExchangeDecorateLimitInfo> levelOpenExchangeList) {
		this.levelOpenExchangeList = levelOpenExchangeList;
	}

	public int getWeekNum() {
		return weekNum;
	}

	public void setWeekNum(int weekNum) {
		this.weekNum = weekNum;
	}

	public int getWeekBuyExpNum() {
		return weekBuyExpNum;
	}

	public void setWeekBuyExpNum(int weekBuyExpNum) {
		this.weekBuyExpNum = weekBuyExpNum;
	}

	@Override
	public void beforeWrite() {
		this.achieveDayItems = SerializeHelper.collectionToString(this.achieveDayList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveWeekItems = SerializeHelper.collectionToString(this.achieveWeekList, SerializeHelper.ELEMENT_DELIMITER);
//		this.levelOpenExchange = SerializeHelper.collectionToString(this.levelOpenExchangeList, SerializeHelper.ELEMENT_DELIMITER);
		this.levelOpenExchange = toLimitInfoString(this.levelOpenExchangeList);
		this.levelReward = SerializeHelper.collectionToString(this.levelRewardList, SerializeHelper.ELEMENT_DELIMITER);
		this.decorateExchange = SerializeHelper.collectionToString(this.decorateList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.achieveDayList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveDayItems, this.achieveDayList);
		this.achieveWeekList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveWeekItems, this.achieveWeekList);
//		SerializeHelper.stringToList(ExchangeDecorateLimitInfo.class, this.levelOpenExchange, this.levelOpenExchangeList);
		this.levelRewardList.clear();
		SerializeHelper.stringToList(ExchangeDecorateInfo.class, this.levelReward, this.levelRewardList);
		this.decorateList.clear();
		SerializeHelper.stringToList(ExchangeDecorateInfo.class, this.decorateExchange, this.decorateList);
		this.levelOpenExchangeList = toLimitList(this.levelOpenExchange);
	}
	
	private List<ExchangeDecorateLimitInfo> toLimitList(String result){
		if(result.equals("[]")){
			return new CopyOnWriteArrayList<>();
		}
		String arr[] = SerializeHelper.split(result, SerializeHelper.SEMICOLON_ITEMS);
		List<ExchangeDecorateLimitInfo> list = new CopyOnWriteArrayList<ExchangeDecorateLimitInfo>();
		for (String subString: arr) {
			String subArr[] = SerializeHelper.split(subString, SerializeHelper.COLON_ITEMS);
			int level = Integer.valueOf(subArr[0]);
			String arrTwo[] = SerializeHelper.split(subArr[1], SerializeHelper.SEMICOLON_ITEMS);
			ExchangeDecorateLimitInfo limitInfo = new ExchangeDecorateLimitInfo();
			limitInfo.setLevelId(level);
			for (String string : arrTwo) {
				String infoArr[] = SerializeHelper.split(string, SerializeHelper.BETWEEN_ITEMS);
				
				for (String infoDesc : infoArr) {
					ExchangeDecorateInfo info = new ExchangeDecorateInfo();
					String descArr[] = SerializeHelper.split(infoDesc, SerializeHelper.ATTRIBUTE_SPLIT);
					info.setLevelId(Integer.parseInt(descArr[0]));
					info.setState(Integer.parseInt(descArr[1]));
					limitInfo.getInfos().add(info);
				}
			}
			list.add(limitInfo);
		}
		return list;
	}
	
	private String toLimitInfoString(List<ExchangeDecorateLimitInfo> list){
		String result="";
		if(list.isEmpty()){
			result="[]";
			return result;
		}
		for (ExchangeDecorateLimitInfo exchangeDecorateLimitInfo : list) {
			result += exchangeDecorateLimitInfo.getLevelId()+SerializeHelper.COLON_ITEMS;
			for (int i = 0; i < exchangeDecorateLimitInfo.getInfos().size(); i++) {
				ExchangeDecorateInfo info = exchangeDecorateLimitInfo.getInfos().get(i);
				result += info.getLevelId()+SerializeHelper.ATTRIBUTE_SPLIT+info.getState();
				if(i != exchangeDecorateLimitInfo.getInfos().size()-1){
					result+=SerializeHelper.BETWEEN_ITEMS;
				}
			}
			result +=SerializeHelper.SEMICOLON_ITEMS;
		}
		return result;
	}
	
	public long getLoginRefreshTime() {
		return loginRefreshTime;
	}

	public void setLoginRefreshTime(long loginRefreshTime) {
		this.loginRefreshTime = loginRefreshTime;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
