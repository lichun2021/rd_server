package com.hawk.activity.type.impl.virtualLaboratory.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_virtual_laboratory")
public class VirtualLaboratoryEntity extends HawkDBEntity implements IActivityDataEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**卡牌数据 */
    @IndexProp(id = 4)
	@Column(name = "cardInfo", nullable = false)
	private String cardInfo;
	
	/**已开卡牌数据 */
    @IndexProp(id = 5)
	@Column(name = "openCardInfo", nullable = false)
	private String openCardInfo;
	
	/**今日重置次数 */
    @IndexProp(id = 6)
	@Column(name = "resetNum", nullable = false)
	private int resetNum;

	/** 活动成就项数据 */
    @IndexProp(id = 7)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> cardInfoList = new ArrayList<>();
	@Transient
	private List<Integer> openCardInfoList = new ArrayList<>();
	@Transient
	private List<AchieveItem> itemList = new ArrayList<>();

	public VirtualLaboratoryEntity() {
	}
	
	public VirtualLaboratoryEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public VirtualLaboratoryEntity(String playerId, int termId) {
		this.playerId = playerId;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	

	public String getCardInfo() {
		return cardInfo;
	}

	public void setCardInfo(String cardInfo) {
		this.cardInfo = cardInfo;
	}

	public String getOpenCardInfo() {
		return openCardInfo;
	}

	public void setOpenCardInfo(String openCardInfo) {
		this.openCardInfo = openCardInfo;
	}

	public List<Integer> getCardInfoList() {
		return cardInfoList;
	}

	public void setCardInfoList(List<Integer> cardInfoList) {
		this.cardInfoList = cardInfoList;
	}

	public List<Integer> getOpenCardInfoList() {
		return openCardInfoList;
	}

	public void setOpenCardInfoList(List<Integer> openCardInfoList) {
		this.openCardInfoList = openCardInfoList;
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
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.cardInfo = SerializeHelper.collectionToString(this.cardInfoList, SerializeHelper.ATTRIBUTE_SPLIT);
		this.openCardInfo = SerializeHelper.collectionToString(this.openCardInfoList, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.cardInfoList = SerializeHelper.stringToList(Integer.class, cardInfo, SerializeHelper.ATTRIBUTE_SPLIT);
		this.openCardInfoList = SerializeHelper.stringToList(Integer.class, openCardInfo, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public int getResetNum() {
		return resetNum;
	}

	public void setResetNum(int resetNum) {
		this.resetNum = resetNum;
	}

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.resetNum = 0;
		this.notifyUpdate();
	}
	
	/**重置卡牌信息,和 开牌的信息
	 * @param cardInfoList
	 */
	public void resetCardInfoList(List<Integer> cardInfoList) {
		this.cardInfoList = cardInfoList;
		this.openCardInfoList = new ArrayList<>();
		this.notifyUpdate();
	}

}
