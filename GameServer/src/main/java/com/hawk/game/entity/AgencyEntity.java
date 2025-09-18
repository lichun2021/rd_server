package com.hawk.game.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 情报中心
 * 
 * @author Golden
 *
 */
/**
 * @author LENOVO
 *
 */
@Entity
@Table(name = "agency")
public class AgencyEntity extends HawkDBEntity {

	/**
	 * 玩家id
	 */
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId;
	
	/**
	 * 情报中心事件
	 */
	@Column(name = "agencyEventStr", nullable = false)
    @IndexProp(id = 2)
	private String agencyEventStr;
	
	/**
	 * 情报中心事件池
	 */
	@Column(name = "agencyEventPoolStr", nullable = false)
    @IndexProp(id = 3)
	private String agencyEventPoolStr;

	/**
	 * 已经生成的道具事件
	 */
	@Column(name = "itemEventGen", nullable = false)
    @IndexProp(id = 4)
	private String itemEventGen;
	
	/**
	 * 经验
	 */
	@Column(name = "exp", nullable = false)
    @IndexProp(id = 5)
	protected int exp;

	/**
	 * 当前等级
	 */
	@Column(name = "currLevel")
    @IndexProp(id = 6)
	protected int currLevel;

	/**
	 * 箱子
	 */
	@Column(name = "box")
    @IndexProp(id = 7)
	protected String box;
	
	/**
	 * 是否击杀过野怪
	 */
	@Column(name = "hasKilled")
    @IndexProp(id = 8)
	protected int hasKilled;

	/**
	 * 击杀野怪数量
	 */
	@Column(name = "killCount")
    @IndexProp(id = 9)
	protected int killCount;
	
	/**
	 * 下次刷新时间
	 */
	@Column(name = "nextRefreshTime")
    @IndexProp(id = 10)
	protected long nextRefreshTime;
	
	/**
	 * 记录下玩家的位置,发生变化的时候要改变事件的位置
	 */
	@Column(name = "playerPos")
    @IndexProp(id = 11)
	protected int playerPos;
	
	/**完成总数*/
	@Column(name = "finishCount")
    @IndexProp(id = 12)
	protected int finishCount;
	
	
	/**
	 * 额外升级的箱子
	 */
	@Column(name = "boxExtLevel")
    @IndexProp(id = 13)
	protected int boxExtLevel;
	
	
	@Column(name = "specialId")
    @IndexProp(id = 14)
	protected int specialId;
	
	/**
	 * 创建时间
	 */
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 22)
	protected long createTime;
	
	/**
	 * 更新时间
	 */
	@Column(name = "updateTime")
    @IndexProp(id = 23)
	protected long updateTime;

	/**
	 * 是否有效
	 */
	@Column(name = "invalid")
    @IndexProp(id = 24)
	protected boolean invalid;

	@Column(name = "finishSpecialCount")
    @IndexProp(id = 25)
	protected int finishSpecialCount;
	

	@Column(name = "finishSpecialDay")
    @IndexProp(id = 26)
	protected int finishSpecialDay;
	
	/**
	 * 情报中心事件
	 */
	@Transient
	private Map<String, AgencyEventEntity> agencyEvents = new ConcurrentHashMap<>();
	
	/**
	 * 情报中心事件池
	 */
	@Transient
	private List<AgencyEventEntity> agencyEventsPool = new ArrayList<>();
	
	/**
	 * 已经生成的道具事件
	 */
	@Transient
	private Set<Integer> itemEventGenSet = new ConcurrentHashSet<>();
	
	/**
	 * 领取的宝箱
	 */
	@Transient
	private Set<Integer> boxSet = new ConcurrentHashSet<>();
	
	
	/**
	 * 默认构造
	 */
	public AgencyEntity() {
		
	}
	
	@Override
	public void beforeWrite() {
		agencyEventStr = SerializeHelper.mapToString(agencyEvents);
		agencyEventPoolStr = SerializeHelper.collectionToString(agencyEventsPool, SerializeHelper.COLON_ITEMS);
		box = SerializeHelper.collectionToString(boxSet, SerializeHelper.COLON_ITEMS);
		itemEventGen = SerializeHelper.collectionToString(itemEventGenSet, SerializeHelper.COLON_ITEMS);
	}

	@Override
	public void afterRead() {
		
		agencyEvents = SerializeHelper.stringToMap(agencyEventStr,
				String.class,
				AgencyEventEntity.class,
				SerializeHelper.COLON_ITEMS,
				SerializeHelper.ELEMENT_SPLIT,
				SerializeHelper.BETWEEN_ITEMS,
				new ConcurrentHashMap<>());
		
		try {
			agencyEventsPool = SerializeHelper.stringToList(AgencyEventEntity.class,
					agencyEventPoolStr,
					SerializeHelper.COLON_ITEMS,
					SerializeHelper.ATTRIBUTE_SPLIT,
					new ArrayList<>());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		boxSet = SerializeHelper.stringToSet(Integer.class,
				box,
				SerializeHelper.COLON_ITEMS,
				null,
				new ConcurrentHashSet<>());
		
		itemEventGenSet = SerializeHelper.stringToSet(Integer.class,
				itemEventGen,
				SerializeHelper.COLON_ITEMS,
				null,
				new ConcurrentHashSet<>());
	}

	
	
	
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getExp() {
		return exp;
	}

	public void addExp(int addExp) {
		this.exp += addExp;
		notifyUpdate();
	}

	public void setExp(int exp) {
		this.exp = exp;
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

	public Map<String, AgencyEventEntity> getAgencyEvents() {
		return agencyEvents;
	}

	public AgencyEventEntity getAgencyEvent(String uuid) {
		return agencyEvents.get(uuid);
	}
	
	public void addAgencyEvent(AgencyEventEntity event) {
		agencyEvents.put(event.getUuid(), event);
		notifyUpdate();
	}
	
	public void clearAgencys() {
		agencyEvents.clear();
		notifyUpdate();
	}
	
	public int getAgencyEventCount() {
		return agencyEvents.size();
	}
	
	public void removeAgencyEvent(String uuid) {
		agencyEvents.remove(uuid);
		notifyUpdate();
	}
	
	public List<AgencyEventEntity> getAgencyEventsPool() {
		return agencyEventsPool;
	}

	public void addAgencyPoolEvent(AgencyEventEntity event) {
		agencyEventsPool.add(event);
		notifyUpdate();
	}
	
	public int getAgencyPoolEventCount(){
		return agencyEventsPool.size();
	}
	
	
	public void sortAgencyPoolEvent(){
		Collections.sort(this.agencyEventsPool, new Comparator<AgencyEventEntity>() {
			@Override
			public int compare(AgencyEventEntity arg0, AgencyEventEntity arg1) {
				return arg0.getEventEndTime() > arg1.getEventEndTime()?-1:1;
			}
		});
		notifyUpdate();
	}
	
	public Set<Integer> getBoxSet() {
		return boxSet;
	}

	public void addBox(int boxId) {
		boxSet.add(boxId);
		notifyUpdate();
	}
	
	public int getCurrLevel() {
		return currLevel;
	}

	public void setCurrLevel(int currLevel) {
		this.currLevel = currLevel;
	}

	public int getHasKilled() {
		return hasKilled;
	}

	public void setHasKilled(int hasKilled) {
		this.hasKilled = hasKilled;
	}

	public long getNextRefreshTime() {
		return nextRefreshTime;
	}

	public void setNextRefreshTime(long nextRefreshTime) {
		this.nextRefreshTime = nextRefreshTime;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public void addKillCount(int count) {
		this.killCount += count;
		notifyUpdate();
	}
	
	public void addFinishCount() {
		this.finishCount++;
		notifyUpdate();
	}
	
	public Set<Integer> getItemEventGenSet() {
		return itemEventGenSet;
	}

	public void addItemEventGen(int eventId) {
		this.itemEventGenSet.add(eventId);
		notifyUpdate();
	}

	public int getPlayerPos() {
		return playerPos;
	}

	public void setPlayerPos(int playerPos) {
		this.playerPos = playerPos;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		throw new UnsupportedOperationException();
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public int getFinishCount() {
		return finishCount;
	}

	public void setFinishCount(int finishCount) {
		this.finishCount = finishCount;
	}

	public String getAgencyEventStr() {
		return agencyEventStr;
	}

	public void setAgencyEventStr(String agencyEventStr) {
		this.agencyEventStr = agencyEventStr;
	}

	public String getAgencyEventPoolStr() {
		return agencyEventPoolStr;
	}

	public void setAgencyEventPoolStr(String agencyEventPoolStr) {
		this.agencyEventPoolStr = agencyEventPoolStr;
	}

	public String getItemEventGen() {
		return itemEventGen;
	}

	public void setItemEventGen(String itemEventGen) {
		this.itemEventGen = itemEventGen;
	}

	public String getBox() {
		return box;
	}

	public void setBox(String box) {
		this.box = box;
	}

	public int getBoxExtLevel() {
		return boxExtLevel;
	}

	public void setBoxExtLevel(int boxExtLevel) {
		this.boxExtLevel = boxExtLevel;
	}

	public int getSpecialId() {
		return specialId;
	}

	public void setSpecialId(int specialId) {
		this.specialId = specialId;
	}


	public int getFinishSpecialCount() {
		return finishSpecialCount;
	}
	
	public void setFinishSpecialCount(int finishSpecialCount) {
		this.finishSpecialCount = finishSpecialCount;
	}
	
	
	public int getFinishSpecialDay() {
		return finishSpecialDay;
	}
	
	public void setFinishSpecialDay(int finishSpecialDay) {
		this.finishSpecialDay = finishSpecialDay;
	}
	
	
	public void addFinishSpecialCount(int finishCount) {
		int day = HawkTime.getYearDay();
		if(this.finishSpecialDay!= day){
			this.finishSpecialDay = day;
			this.finishSpecialCount = 0;
		}
		this.finishSpecialCount += finishCount;
	}
	
	
	
}
