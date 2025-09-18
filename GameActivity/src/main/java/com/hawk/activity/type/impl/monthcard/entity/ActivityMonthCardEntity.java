package com.hawk.activity.type.impl.monthcard.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.game.protocol.MonthCard.MonthCardState;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 月卡周卡活动数据存储
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_card")
public class ActivityMonthCardEntity extends HawkDBEntity implements IActivityDataEntity {
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

	/** 活动项数据 */
    @IndexProp(id = 4)
	@Column(name = "cardItems", nullable = false)
	private String cardItems;
	
	/**
	 * 月卡领取状态刷新的时间
	 */
    @IndexProp(id = 5)
	@Column(name = "lastRefreshTime", nullable = false)
	private long lastRefreshTime;

    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    //特权兑换商店刷新时间
    @IndexProp(id = 9)
   	@Column(name = "exchangeRefreshTime", nullable = false)
   	private long exchangeRefreshTime;

    //兑换商店已兑换次数信息
    @IndexProp(id = 10)
   	@Column(name = "exchangeItems", nullable = false)
   	private String exchangeItems = "";
    
    //定制月卡选择物品信息
    @IndexProp(id = 11)
   	@Column(name = "customItems", nullable = false)
   	private String customItems = "";
    
    /**
     * 关注的兑换id列表 用积分兑换物品的勾选状态
     **/
    @IndexProp(id = 12)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint = "";
    
    /**
     * 最近一次设置的定制奖励
     */
    @IndexProp(id = 13)
    @Column(name = "customLatest", nullable = false)
    private String customLatest = "";
    
    
	@Transient
	private BlockingDeque<MonthCardItem> cardList;
	
	@Transient
	private List<CustomItem> itemList = new CopyOnWriteArrayList<CustomItem>();
	
	@Transient
	private Map<Integer, CustomItem> customLatestMap = new HashMap<>();
	
	/** 兑换数量 */
	@Transient
	private Map<Integer, Integer> exchangeMap = new HashMap<>();
	
	@Transient
	private List<Integer> playerPoints = new CopyOnWriteArrayList<Integer>();
	

	public ActivityMonthCardEntity() {
		this.cardList = new LinkedBlockingDeque<>();
	}

	public ActivityMonthCardEntity(String playerId) {
		this(playerId, 0);
	}
	
	public ActivityMonthCardEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.cardItems = "";
		this.cardList = new LinkedBlockingDeque<>();
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

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	
	public void setCardItems(String cardItems) {
		this.cardItems = cardItems;
	}

	public String getCardItems() {
		return cardItems;
	}
	
	@Override
	public void beforeWrite() {
		this.customItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.exchangeItems = SerializeHelper.mapToString(this.exchangeMap);
		this.customLatest = SerializeHelper.collectionToString(this.customLatestMap.values(), SerializeHelper.ELEMENT_DELIMITER);
		this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
		if (cardList != null && !cardList.isEmpty()) {
			itemsToString();
		} else {
			this.cardItems = "";
		}
	}
	
	/**
	 * 读取后处理(主要用来组装数据)
	 */
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(CustomItem.class, this.customItems, this.itemList);
		this.exchangeMap = SerializeHelper.stringToMap(this.exchangeItems, Integer.class, Integer.class);
		List<CustomItem> latestItemList = new ArrayList<>();
		SerializeHelper.stringToList(CustomItem.class, this.customLatest, latestItemList);
		for (CustomItem item : latestItemList) {
			MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, item.getCardId());
			customLatestMap.put(cfg.getType(), item);
		}
		
		this.playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
		if (!HawkOSOperator.isEmptyString(cardItems)) {
			stringToItems();
		}
	}
	
	public Map<Integer, CustomItem> getCustomLatestMap() {
		return customLatestMap;
	}
	
	public void updateCustomLatest(int cardType, CustomItem item) {
		customLatestMap.put(cardType, item);
		this.notifyUpdate();
	}
	
	public CustomItem getCustomLatest(int cardType) {
		return customLatestMap.get(cardType);
	}
	
	private void itemsToString() {
		String itemStr = SerializeHelper.collectionToString(cardList, SerializeHelper.BETWEEN_ITEMS);
		this.cardItems = itemStr;
	}

	private void stringToItems() {
		String[] array = SerializeHelper.split(cardItems, SerializeHelper.BETWEEN_ITEMS);
		List<MonthCardItem> itemList = new ArrayList<>();
		for (String data : array) {
			itemList.add(MonthCardItem.valueOf(data));
		}
		
		fillCardList(itemList);
	}

	private void fillCardList(List<MonthCardItem> cardList) {
		this.cardList.clear();
		this.cardList.addAll(cardList);
	}
	
	public BlockingDeque<MonthCardItem> getCardList() {
		return cardList;
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
	
	public MonthCardItem getCard(int cardId) {
		for (MonthCardItem card : cardList) {
			if (card.getCardId() == cardId) {
				return card;
			}
		}
		
		return null;
	}
	
	public void addCard(MonthCardItem card) {
		this.cardList.add(card);
	}
	
	/**
	 * 删除月卡（删除未购买状态的月卡，防止积累的月卡太多浪费空间）
	 * @param cards
	 */
	public void removeMultiCard(List<MonthCardItem> cards) {
		this.cardList.removeAll(cards);
	}
	
	/**
	 * 删除月卡（删除未购买状态的月卡，防止积累的月卡太多浪费空间）
	 * @param card
	 */
	public void removeCard(MonthCardItem card) {
		this.cardList.remove(card);
	}
	
	/**
	 * 通过类型获取状态为生效状态的月卡
	 * 
	 * @param type 月卡类别
	 * @return
	 */
	public List<MonthCardItem> getEfficientCardList(int type) {
		List<MonthCardItem> retList = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		for (MonthCardItem card : cardList) {
			// type<0时获取所有类型的生效月卡
			if (type >= 0 && MonthCardActivityCfg.getMonthCardType(card.getCardId()) != type) {
				continue;
			}
			
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
			if (cardCfg == null || (cardCfg.getValidEndTime(card.getPucharseTime()) > now && card.getState() != MonthCardState.TO_RENEW_VALUE)) {
				retList.add(card);
			}
		}
		
		return retList;
	}
	
	/**
	 * 获取单个状态为生效状态的月卡
	 * 
	 * @param type 月卡类别
	 * @return
	 */
	public MonthCardItem getEfficientCard(int type) {
		List<MonthCardItem> cardList = getEfficientCardList(type);
		if (cardList.isEmpty()) {
			return null;
		}
		
		return cardList.get(0);
	}
	
	/**
	 * 获取所有状态为生效状态的月卡
	 * 
	 * @return
	 */
	public List<MonthCardItem> getEfficientCardList() {
		return getEfficientCardList(-1);
	}
	
	/**
	 * 获取所有处于生效期或可续费期状态的月卡
	 * 
	 * @return
	 */
	public List<MonthCardItem> getUnfinishedCards(int type) {
		List<MonthCardItem> retList = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		for (MonthCardItem card : cardList) {
			// type<0时获取所有类型的月卡
			if (type >= 0 && MonthCardActivityCfg.getMonthCardType(card.getCardId()) != type) {
				continue;
			}
			
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
			if (cardCfg == null || cardCfg.getRenewEndTime(card.getPucharseTime()) > now) {
				retList.add(card);
			}
		}
		
		return retList;
	}
	
	public void addCustomItem(CustomItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<CustomItem> getCustomItemList() {
		return itemList;
	}
	
	public CustomItem getCustomItem(int cardId) {
		for (CustomItem item : itemList) {
			if (item.getCardId() == cardId) {
				return item;
			}
		}
		return null;
	}
	
	public void removeCustomItem(int cardId) {
		CustomItem item = getCustomItem(cardId);
		if (item != null) {
			itemList.remove(item);
			notifyUpdate();
		}
	}
	
	public Map<Integer, Integer> getExchangeMap() {
		return exchangeMap;
	}
	
	
	public long getExchangeRefreshTime() {
		return exchangeRefreshTime;
	}

	public void setExchangeRefreshTime(long exchangeRefreshTime) {
		this.exchangeRefreshTime = exchangeRefreshTime;
	}
	
	public void addTips(int id) {
        if (!playerPoints.contains(id)) {
            playerPoints.add(id);
        }
        this.notifyUpdate();
    }

    public void removeTips(int id) {
        playerPoints.remove(new Integer(id));
        this.notifyUpdate();
    }

    public List<Integer> getPlayerPoints() {
        return playerPoints;
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
