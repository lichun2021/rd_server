package com.hawk.activity.type.impl.militaryprepare.entity;
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
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareAchieveCfg;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareScoreCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_military_prepare")
public class MilitaryPrepareEntity extends HawkDBEntity implements IActivityDataEntity{

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
	@Column(name = "advanced", nullable = false)
	private int advanced;
	
    @IndexProp(id = 5)
	@Column(name = "advancedBox", nullable = false)
	private String advancedBox;
	
	
    @IndexProp(id = 6)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	/**
	 * 跨天天数更新时间
	 */
    @IndexProp(id = 7)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	@Transient
	private List<Integer> advancedBoxList = new CopyOnWriteArrayList<>();

	public MilitaryPrepareEntity() {
	}
	
	public MilitaryPrepareEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.advanced = 0;
		this.loginDays = 1;
		this.refreshTime = HawkTime.getMillisecond();
		this.achieveItems = "";
		this.advancedBox = ""; 
		
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
	
	public int getAdvanced() {
		return advanced;
	}

	public void setAdvanced(int advanced) {
		this.advanced = advanced;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
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
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		List<AchieveItem> copy = new ArrayList<>();
		copy.addAll(itemList);
		int size1 = HawkConfigManager.getInstance().getConfigSize(MilitaryPrepareScoreCfg.class);
		int size2 = HawkConfigManager.getInstance().getConfigSize(MilitaryPrepareAchieveCfg.class);
		int limitSize = size1 + size2;  //62个
		if (copy.size() > limitSize) {
			for (int i = copy.size() - 1; i >= limitSize; i--) {
				itemList.remove(i);
			}
		}
		return itemList;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.advancedBox = SerializeHelper.collectionToString(this.advancedBoxList);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.advancedBoxList = SerializeHelper.stringToList(Integer.class, this.advancedBox);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public boolean addAdvancedBoxAchived(int id){
		if(this.advancedBoxList.contains(id)){
			return false;
		}
		this.advancedBoxList.add(id);
		this.notifyUpdate();
		return true;
	}

	public List<Integer> getAdvancedBoxList() {
		return advancedBoxList;
	}

}
