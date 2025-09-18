package com.hawk.activity.type.impl.newyearlottery.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkRand;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryGiftCfg;

@Entity
@Table(name="activity_newyear_lottery")
public class NewyearLotteryEntity extends HawkDBEntity implements IActivityDataEntity {
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
	
    @IndexProp(id = 4)
	@Column(name = "dayTime", nullable = false)
	private String dayTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 礼包数据  */
    @IndexProp(id = 6)
	@Column(name = "payGiftInfo", nullable = false)
	private String payGiftInfo;
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<NewyearLotteryAchieveItem> itemList = new ArrayList<NewyearLotteryAchieveItem>();
	
	@Transient
	private List<PayGiftInfoItem> giftItemList = new ArrayList<PayGiftInfoItem>();
	
	public NewyearLotteryEntity(){}
	
	public NewyearLotteryEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.dayTime = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.payGiftInfo = SerializeHelper.collectionToString(this.giftItemList, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.SEMICOLON_ITEMS);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(NewyearLotteryAchieveItem.class, this.achieveItems, this.itemList);
		this.giftItemList.clear();
		SerializeHelper.stringToList(PayGiftInfoItem.class, this.payGiftInfo, SerializeHelper.ELEMENT_SPLIT, SerializeHelper.SEMICOLON_ITEMS, this.giftItemList);
	}
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
	
	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public List<NewyearLotteryAchieveItem> getItemList() {
		return itemList;
	}
	
	public List<NewyearLotteryAchieveItem> getItemList(int lotteryType) {
		return itemList.stream().filter(e -> e.getLotteryType() == lotteryType).collect(Collectors.toList());
	}
	
	public List<NewyearLotteryAchieveItem> getItemListByState(int state) {
		return itemList.stream().filter(e -> e.getState() == state).collect(Collectors.toList());
	}
	
	public void resetItemList(List<NewyearLotteryAchieveItem> list) {
		itemList.clear();
		itemList.addAll(list);
		notifyUpdate();
	}
	
	public NewyearLotteryAchieveItem getAchieveItem(int cfgId) {
		Optional<NewyearLotteryAchieveItem> op = itemList.stream().filter(e -> e.getAchieveId() == cfgId).findAny();
		return op.isPresent() ? op.get() : null;
	}

	public List<PayGiftInfoItem> getGiftItemList() {
		return giftItemList;
	}
	
	public PayGiftInfoItem getGiftItemByType(int lotteryType) {
		for (PayGiftInfoItem item : giftItemList) {
			if (item.getLotteryType() == lotteryType) {
				return item;
			}
		}
		
		NewyearLotteryGiftCfg cfg = NewyearLotteryGiftCfg.getDefaultRewardGiftCfg(lotteryType);
		String randomReward = HawkRand.randomWeightObject(cfg.getRandomRewardItems(), cfg.getRandomRewardWeight());
		PayGiftInfoItem item = PayGiftInfoItem.valueOf(lotteryType, cfg.getId(), randomReward);
		giftItemList.add(item);
		notifyUpdate();
		
		return item;
	}
	
	public String getDayTime() {
		return dayTime;
	}

	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}

}
