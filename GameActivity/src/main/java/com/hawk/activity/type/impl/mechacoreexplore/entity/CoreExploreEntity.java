package com.hawk.activity.type.impl.mechacoreexplore.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import org.hawk.collection.ConcurrentHashSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreHelper;
import com.hawk.game.protocol.Activity.CEObstacleType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_core_explore")
public class CoreExploreEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity{
	
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false)
	private int termId;
    
    /** 当前行数  */
    @IndexProp(id = 4)
	@Column(name="currLine", nullable = false)
	private int currLine;
    
    /** 地图数据 */
    @IndexProp(id = 5)
   	@Column(name = "zoneArea", nullable = false)
   	private String zoneArea = "";
    
    /** 宝箱数据 */
    @IndexProp(id = 6)
   	@Column(name = "areaBox", nullable = false)
   	private String areaBox = "";  
    
    /** 石头数据 */
    @IndexProp(id = 7)
   	@Column(name = "areaStone", nullable = false)
   	private String areaStone = "";  
    
    /** 已完成的科技数据 */
    @IndexProp(id = 8)
   	@Column(name = "techInfo", nullable = false)
   	private String techInfo = "";
    
    /** 免费矿镐数据 */
    @IndexProp(id = 9)
   	@Column(name = "freePick", nullable = false)
   	private String freePick = "";  
    
    /** 矿镐购买次数  */
    @IndexProp(id = 10)
	@Column(name="pickBuyTimes", nullable = false)
	private int pickBuyTimes;
    
    /** 兑换商店数据  */
    @IndexProp(id = 11)
   	@Column(name = "shopItems", nullable = false)
   	private String shopItems = "";
    
    @IndexProp(id = 12)
   	@Column(name = "tips", nullable = false)
   	private String tips;

    @IndexProp(id = 13)
	@Column(name="createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name="invalid", nullable = false)
	private boolean invalid;
    
    @IndexProp(id = 16)
   	@Column(name="dayTime", nullable = false)
   	private long dayTime;
    
    /** 是否开启自动挖矿（1是0否）  */
    @IndexProp(id = 17)
	@Column(name="autoPick", nullable = false)
	private int autoPick;
    
	/** 自动挖矿获得的奖励  */
    @IndexProp(id = 18)
	@Column(name="autoPickRewards", nullable = false)
	private String autoPickRewards = "";
    
    @IndexProp(id = 19)
   	@Column(name="autoPickConsumes", nullable = false)
   	private String autoPickConsumes = "";
    
    /** 特殊道具 */
    @IndexProp(id = 20)
   	@Column(name = "specialItems", nullable = false)
   	private String specialItems = "";  
    
    /** 沙土附带矿石，或石头附带矿石的矿石道具 */
    @IndexProp(id = 21)
   	@Column(name = "oreItems", nullable = false)
   	private String oreItems = "";  
    
    
	/**
     * 自动挖矿获得的奖励
     */
    @Transient
    List<RewardItem.Builder> autoPickRewardList = new ArrayList<>();
    
    @Transient
    List<RewardItem.Builder> autoPickConsumeList = new ArrayList<>();
    
	/**
     * 地图数据
     */
    @Transient
	private List<List<Integer>> zoneLineList = new ArrayList<>();
    
    /**
     * 通路格子
     */
    @Transient
	private List<List<Integer>> connectedGrids = new ArrayList<>();
    
    /**
     * 不在通路上的空格子
     */
    @Transient
    Set<String> unconnectedEmptyGrids = new ConcurrentHashSet<>();
	
	/**
     * 宝箱数据
     */
	@Transient
	private List<BoxItem> boxList = new ArrayList<>();
	
	/**
     * 石头数据
     */
	@Transient
	private List<StoneItem> stoneList = new ArrayList<>();
	
	/**
	 * 免费矿镐数据
	 */
	@Transient
	private PickItem freePickData = new PickItem();
	
	/**
	 * 已完成的科技
	 */
	@Transient
	private Set<Integer> completeTechs = new HashSet<>();
    
    /** 
     * 兑换商店兑换数据  
     */
	@Transient
	private Map<Integer, Integer> shopItemMap = new HashMap<>();
	
    @Transient
	private Set<Integer> tipSet = new HashSet<>();
    
    /**
     * 特殊道具
     */
	@Transient
	private List<SpecialItem> specialItemList = new ArrayList<>();
	
	/**
     * 沙土附带矿石，或石头附带矿石的矿石道具
     */
	@Transient
	private List<SpecialItem> oreItemList = new ArrayList<>();
    
	
	public CoreExploreEntity(){}
	
	public CoreExploreEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		StringJoiner sj = new StringJoiner(";");
		for (List<Integer> zoneLine : zoneLineList) {
			String line = SerializeHelper.collectionToString(zoneLine, ",");
			sj.add(line);
		}
		this.zoneArea = sj.toString();
		this.areaBox = SerializeHelper.collectionToString(this.boxList, SerializeHelper.ELEMENT_DELIMITER);
		this.areaStone = SerializeHelper.collectionToString(this.stoneList, SerializeHelper.ELEMENT_DELIMITER);
		this.freePick = SerializeHelper.toJson(freePickData);
		this.techInfo = SerializeHelper.collectionToString(this.completeTechs, SerializeHelper.ATTRIBUTE_SPLIT);
		this.shopItems = SerializeHelper.mapToString(this.shopItemMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet, SerializeHelper.ATTRIBUTE_SPLIT);
		this.specialItems = SerializeHelper.collectionToString(this.specialItemList, SerializeHelper.ELEMENT_DELIMITER, ";");
		this.oreItems = SerializeHelper.collectionToString(this.oreItemList, SerializeHelper.ELEMENT_DELIMITER, ";");
		
		StringJoiner sj1 = new StringJoiner(",");
		for (RewardItem.Builder item : autoPickRewardList) {
			if (item != null) {
				sj1.add(RewardHelper.toItemString(item.build()));
			}
		}
		autoPickRewards = sj1.toString();
		
		StringJoiner sj2 = new StringJoiner(",");
		for (RewardItem.Builder item : autoPickConsumeList) {
			if (item != null) {
				sj2.add(RewardHelper.toItemString(item.build()));
			}
		}
		autoPickConsumes = sj2.toString();
	}
	
	@Override
	public void afterRead() {
		if (!zoneLineList.isEmpty()) {
			zoneLineList.clear();
		}
		String[] arr = this.zoneArea.split(";");
		for (String lineData : arr) {
			List<Integer> zoneLineVal = SerializeHelper.stringToList(Integer.class, lineData, ",");
			if (!zoneLineVal.isEmpty()) {
				zoneLineList.add(zoneLineVal);
			}
		}
		
		//检测连通性
		CoreExploreHelper.refreshConnectedData(this);
		
		this.boxList.clear();
		SerializeHelper.stringToList(BoxItem.class, this.areaBox, this.boxList);
		
		stoneList.clear();
		SerializeHelper.stringToList(StoneItem.class, this.areaStone, this.stoneList);
		
		this.freePickData = SerializeHelper.parseJsonStr(freePick, PickItem.class);
		SerializeHelper.stringToSet(Integer.class, this.techInfo, SerializeHelper.ATTRIBUTE_SPLIT, null, this.completeTechs);
		this.shopItemMap = SerializeHelper.stringToMap(this.shopItems, Integer.class, Integer.class);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT, null, this.tipSet);
		
		specialItemList.clear();
		SerializeHelper.stringToList(SpecialItem.class, this.specialItems, SerializeHelper.ELEMENT_SPLIT, ";", this.specialItemList);
		oreItemList.clear();
		SerializeHelper.stringToList(SpecialItem.class, this.oreItems, SerializeHelper.ELEMENT_SPLIT, ";", this.oreItemList);
		
		autoPickRewardList.clear();
		String[] arr1 = this.autoPickRewards.split(",");
		for (String rewardItem : arr1) {
			if (!HawkOSOperator.isEmptyString(rewardItem)) {
				autoPickRewardList.add(RewardHelper.toRewardItem(rewardItem));
			}
		}
		
		autoPickConsumeList.clear();
		String[] arr2 = this.autoPickConsumes.split(",");
		for (String consumeItem : arr2) {
			if (!HawkOSOperator.isEmptyString(consumeItem)) {
				autoPickConsumeList.add(RewardHelper.toRewardItem(consumeItem));
			}
		}
	}
	

	@Override
	public String getPrimaryKey() {
		return id;
	}
	
	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public Set<Integer> getTipSet() {
		return this.tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		this.tipSet = tips;
	}

	public int getCurrLine() {
		return currLine;
	}
	
	public void setCurrLine(int currLine) {
		this.currLine = currLine;
	}
	
	public void currLineInc() {
		setCurrLine(currLine + 1);
	}
	
	public int getFirstLine() {
		return currLine - 6;
	}
	
	public int getTotalLine() {
		return this.getCurrLine() + 2;
	}
	
	public boolean isCurrLine(int line) {
		return this.getCurrLine() == line;
	}

	public String getZoneArea() {
		return zoneArea;
	}

	public void setZoneArea(String zoneArea) {
		this.zoneArea = zoneArea;
	}

	public String getAreaBox() {
		return areaBox;
	}

	public void setAreaBox(String areaBox) {
		this.areaBox = areaBox;
	}

	public String getTechInfo() {
		return techInfo;
	}

	public void setTechInfo(String techInfo) {
		this.techInfo = techInfo;
	}

	public String getFreePick() {
		return freePick;
	}

	public void setFreePick(String freePick) {
		this.freePick = freePick;
	}

	public int getPickBuyTimes() {
		return pickBuyTimes;
	}

	public void setPickBuyTimes(int pickBuyTimes) {
		this.pickBuyTimes = pickBuyTimes;
	}

	public String getShopItems() {
		return shopItems;
	}

	public void setShopItems(String shopItems) {
		this.shopItems = shopItems;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public Map<Integer, Integer> getShopItemMap() {
		return shopItemMap;
	}
	
	public void setShopItemMap(Map<Integer, Integer> shopItemMap) {
		this.shopItemMap = shopItemMap;
	}

	public List<BoxItem> getBoxList() {
		return boxList;
	}

	public void setBoxList(List<BoxItem> boxList) {
		this.boxList = boxList;
	}
	
	public void removeBox(BoxItem box) {
		if (box != null) {
			boxList.remove(box);
			this.notifyUpdate();
		}
	}
	
	public void removeBox(int line, int column) {
		BoxItem box = this.getBox(line, column);
		this.removeBox(box);
	}
	
	public void addBox(BoxItem box) {
		boxList.add(box);
		this.notifyUpdate();
	}
	
	public BoxItem getBox(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return null;
		}
		for (BoxItem box : boxList) {
			if (box.getLine() == line && box.getColumn() == column) {
				return box;
			}
		} 
		return null;
	}
	
	public List<StoneItem> getStoneList() {
		return stoneList;
	}

	public void setStoneList(List<StoneItem> stoneList) {
		this.stoneList = stoneList;
	}
	
	public void removeStone(StoneItem stone) {
		if (stone != null) {
			stoneList.remove(stone);
			this.notifyUpdate();
		}
	}
	
	public void removeStone(int line, int column) {
		StoneItem stone = this.getStone(line, column);
		this.removeStone(stone);
	}
	
	public void addStone(StoneItem stone) {
		stoneList.add(stone);
		this.notifyUpdate();
	}
	
	public StoneItem getStone(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return null;
		}
		for (StoneItem stone : stoneList) {
			if (stone.getLine() == line && stone.getColumn() == column) {
				return stone;
			}
		} 
		return null;
	}

	public PickItem getFreePickData() {
		return freePickData;
	}

	public void setFreePickData(PickItem freePickData) {
		this.freePickData = freePickData;
	}

	public Set<Integer> getCompleteTechs() {
		return completeTechs;
	}

	public void setCompleteTechs(Set<Integer> completeTechs) {
		this.completeTechs = completeTechs;
	}
	
	public void addCompleteTech(int techId) {
		completeTechs.add(techId);
		this.notifyUpdate();
	}
	
	public List<List<Integer>> getLineList() {
		return zoneLineList;
	}
	
	public void addNewLine(List<Integer> columnList) {
		zoneLineList.add(columnList);
	}
	
	public List<Integer> removeFirstLine() {
		return zoneLineList.remove(0);
	}
	
	public Integer getGridObstacle(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return null;
		}
		
		int index = this.getLineIndex(line);
		return zoneLineList.get(index).get(column - 1);
	}
	
	public List<Integer> getColumnList(int line) {
		if (lineInvalid(line)) {
			return Collections.emptyList();
		}
		int index = this.getLineIndex(line);
		return zoneLineList.get(index);
	}
	
	public void setGridEmpty(int line, int column) {
		List<Integer> columnList = this.getColumnList(line);
		if (!columnList.isEmpty()) {
			columnList.set(column - 1, CEObstacleType.CE_EMPTY_VALUE);
		}
	}
	
	public int getLineIndex(int line) {
		return line - this.getFirstLine();
	}
	
	private boolean lineInvalid(int line) {
		return line < this.getFirstLine() || line > this.getTotalLine();
	}
	
	private boolean columnInvalid(int column) {
		return column < 1 || column > 6;
	}
	
	public List<List<Integer>> getConnectedGrids() {
		return connectedGrids;
	}
	
	public List<Integer> removeFirstLineConnect() {
		return connectedGrids.remove(0);
	}
	
	public void clearConnectedGrids() {
		connectedGrids.clear();
	}

	public boolean isConnectedGrids(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return false;
		}
		int index = this.getLineIndex(line);
		return connectedGrids.get(index).get(column - 1) > 0;
	}
	
	public boolean addConnectedGrid(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return false;
		}
		int index = this.getLineIndex(line);
		if (connectedGrids.get(index).get(column - 1) >= 1) {
			return false;
		}
		connectedGrids.get(index).set(column - 1, 1);
		return true;
	}
	
	public boolean isConnectedLine(int line) {
		if (lineInvalid(line)) {
			return false;
		}
		
		int index = this.getLineIndex(line);
		Optional<Integer> optional = connectedGrids.get(index).stream().filter(e -> e>0).findFirst();
		return optional.isPresent();
	}
	
	public Set<String> getUnconnectedEmptyGrids() {
		return unconnectedEmptyGrids;
	}

	public void updateUnconnectedEmptyGrids(Collection<String> unconnectedEmptyGrids) {
		this.unconnectedEmptyGrids.clear();
		this.unconnectedEmptyGrids.addAll(unconnectedEmptyGrids);
	}
	
	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}
	
	 public int getAutoPick() {
		return autoPick;
	 }

	 public void setAutoPick(int autoPick) {
		this.autoPick = autoPick;
	 }
	 
	 public String getAutoPickRewards() {
		return autoPickRewards;
	 }

	 public void setAutoPickRewards(String autoPickRewards) {
		this.autoPickRewards = autoPickRewards;
	 }

	public List<RewardItem.Builder> getAutoPickRewardList() {
		return autoPickRewardList;
	}
	
	public void addAutoPickRewards(List<RewardItem.Builder> rewardList) {
		if (this.getAutoPick() > 0) {
			autoPickRewardList.addAll(rewardList);
			autoPickRewardList = RewardHelper.mergeRewardItem(autoPickRewardList);
			this.notifyUpdate();
		}
	}
	
	public List<RewardItem.Builder> getAutoPickConsumeList() {
		return autoPickConsumeList;
	}
	
	public void addAutoPickConsumes(List<RewardItem.Builder> consumeList) {
		if (this.getAutoPick() > 0) {
			autoPickConsumeList.addAll(consumeList);
			autoPickConsumeList = RewardHelper.mergeRewardItem(autoPickConsumeList);
			this.notifyUpdate();
		}
	}

	public List<SpecialItem> getSpecialItemList() {
		return specialItemList;
	}
	
	public void removeSpecialItem(SpecialItem item) {
		if (item != null) {
			specialItemList.remove(item);
			this.notifyUpdate();
		}
	}
	
	public void removeSpecialItem(int line, int column) {
		SpecialItem item = this.getSpecialItem(line, column);
		this.removeSpecialItem(item);
	}
	
	public void addSpecialItem(SpecialItem item) {
		specialItemList.add(item);
		this.notifyUpdate();
	}
	
	public SpecialItem getSpecialItem(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return null;
		}
		for (SpecialItem item : specialItemList) {
			if (item.getLine() == line && item.getColumn() == column) {
				return item;
			}
		} 
		return null;
	}
	
	
	public List<SpecialItem> getOreItemList() {
		return oreItemList;
	}
	
	public void removeOreItem(SpecialItem item) {
		if (item != null) {
			oreItemList.remove(item);
			this.notifyUpdate();
		}
	}
	
	public void removeOreItem(int line, int column) {
		SpecialItem item = this.getOreItem(line, column);
		this.removeOreItem(item);
	}
	
	public void addOreItem(SpecialItem item) {
		oreItemList.add(item);
		this.notifyUpdate();
	}
	
	public SpecialItem getOreItem(int line, int column) {
		if (lineInvalid(line) || columnInvalid(column)) {
			return null;
		}
		for (SpecialItem item : oreItemList) {
			if (item.getLine() == line && item.getColumn() == column) {
				return item;
			}
		} 
		return null;
	}
	
}
