package com.hawk.activity.type.impl.seasonpuzzle.entity;

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
@Table(name="activity_season_puzzle")
public class SeasonPuzzleEntity extends HawkDBEntity implements IActivityDataEntity{

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
    
    /** 当日首次登录时间 */
    @IndexProp(id = 4)
  	@Column(name = "dayTime", nullable = false)
  	private long dayTime;
    
    /** 当日赠送碎片数量 */
    @IndexProp(id = 5)
  	@Column(name = "itemSendCount", nullable = false)
  	private int itemSendCount;
    
    /** 当日获得碎片数量 */
    @IndexProp(id = 6)
  	@Column(name = "itemGetCount", nullable = false)
  	private int itemGetCount;
    
    /** 活动成就项数据 */
    @IndexProp(id = 7)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems = "";
	
    /** 发起求助的时间信息 */
    @IndexProp(id = 8)
  	@Column(name = "callHelpInfo", nullable = false)
  	private String callHelpInfo = "";
    
    /** 已放入的拼图碎片信息 */
    @IndexProp(id = 9)
	@Column(name = "itemSetInfo", nullable = false)
	private String itemSetInfo = "";
    
	@IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    
    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	@Transient
	private List<CallHelperInfo> callHelpInfoList = new ArrayList<>();
	
	@Transient
	private List<Integer> itemSetIndexList = new ArrayList<>();
	
	public SeasonPuzzleEntity() {
	}
	
	public SeasonPuzzleEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public SeasonPuzzleEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.callHelpInfo = SerializeHelper.collectionToString(callHelpInfoList, SerializeHelper.ELEMENT_DELIMITER);
		this.itemSetInfo = SerializeHelper.collectionToString(this.itemSetIndexList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
		this.callHelpInfoList = SerializeHelper.stringToList(CallHelperInfo.class, this.callHelpInfo);
		this.itemSetIndexList.clear();
		SerializeHelper.stringToList(Integer.class, this.itemSetInfo, this.itemSetIndexList);
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
	
	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	public int getItemSendCount() {
		return itemSendCount;
	}

	public void setItemSendCount(int itemSendCount) {
		this.itemSendCount = itemSendCount;
	}
	
	public void addItemSendCount(int add) {
		setItemSendCount(itemSendCount + add);
	}

	public int getItemGetCount() {
		return itemGetCount;
	}

	public void setItemGetCount(int itemGetCount) {
		this.itemGetCount = itemGetCount;
	}
	
	public void addItemGetCount(int add) {
		setItemGetCount(itemGetCount + add);
	}

	public String getItemSetInfo() {
		return itemSetInfo;
	}

	public void setItemSetInfo(String itemSetInfo) {
		this.itemSetInfo = itemSetInfo;
	}

	public String getCallHelpInfo() {
		return callHelpInfo;
	}

	public void setCallHelpInfo(String callHelpInfo) {
		this.callHelpInfo = callHelpInfo;
	}

	public List<Integer> getItemSetIndexList() {
		return itemSetIndexList;
	}

	public void setItemSetIndexList(List<Integer> itemSetIndexList) {
		this.itemSetIndexList = itemSetIndexList;
	}
	
	public void addItemSetIndexList(List<Integer> addList) {
		this.itemSetIndexList.addAll(addList);
		this.notifyUpdate();
	}

	public List<CallHelperInfo> getCallHelpInfoList() {
		return callHelpInfoList;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
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
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
