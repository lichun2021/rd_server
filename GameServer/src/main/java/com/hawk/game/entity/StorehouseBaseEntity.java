package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * 玩家宝藏基础数据
 * 
 * @author lwt
 *
 */
@Entity
@Table(name = "story_house_base")
public class StorehouseBaseEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = null;

	// 今日剩余挖掘数
	@Column(name = "exc")
    @IndexProp(id = 2)
	private int exc;
	// 历史挖掘数
	@Column(name = "excCount")
    @IndexProp(id = 3)
	private int excCount;
	// 下次免费挖掘
	@Column(name = "nextFreeExc")
    @IndexProp(id = 4)
	private long nextFreeExc;

	// 上次刷新挖掘数
	@Column(name = "lastExcRecover")
    @IndexProp(id = 5)
	private long lastExcRecover;
	// 帮助数
	@Column(name = "help")
    @IndexProp(id = 6)
	private int help;
	// 上次刷新帮助数
	@Column(name = "lastHelpRecover")
    @IndexProp(id = 7)
	private long lastHelpRecover;
	// 上次刷新宝藏
	@Column(name = "lastRefrash")
    @IndexProp(id = 8)
	private long lastRefrash;
	// 下次跨天重置
	@Column(name = "overDay")
    @IndexProp(id = 9)
	private long overDay;
	// 宝藏未挖
	@Column(name = "store")
    @IndexProp(id = 10)
	private String store = "";

	// 今日手动刷新次
	@Column(name = "refrashCount")
    @IndexProp(id = 11)
	private int refrashCount;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 12)
	protected long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 13)
	protected long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 14)
	protected boolean invalid;

	@Transient
	private List<Integer> storeList = Collections.emptyList();

	@Override
	public void beforeWrite() {
		this.store = Joiner.on(",").skipNulls().join(Optional.ofNullable(storeList).orElse(Collections.emptyList()));
		super.beforeWrite();
	}

	public int getRefrashCount() {
		return refrashCount;
	}

	public void setRefrashCount(int refrashCount) {
		this.refrashCount = refrashCount;
	}

	@Override
	public void afterRead() {
		this.storeList = Splitter.on(",")
				.trimResults()
				.omitEmptyStrings()
				.splitToList(this.store)
				.stream()
				.map(Integer::valueOf)
				.collect(Collectors.toCollection(LinkedList::new));
		
		super.afterRead();
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public List<Integer> getStoreList() {
		return storeList;
	}

	public void setStoreList(List<Integer> storeList) {
		this.storeList = storeList;
	}

	public String getPlayerId() {
		return playerId;
	}

	public int getExc() {
		return exc;
	}

	public long getLastExcRecover() {
		return lastExcRecover;
	}

	public int getHelp() {
		return help;
	}

	public long getLastHelpRecover() {
		return lastHelpRecover;
	}

	public long getLastRefrash() {
		return lastRefrash;
	}

	public long getOverDay() {
		return overDay;
	}

	public long getCreateTime() {
		return createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public long getNextFreeExc() {
		return nextFreeExc;
	}

	public void setNextFreeExc(long nextFreeExc) {
		this.nextFreeExc = nextFreeExc;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setExc(int exc) {
		this.exc = exc;
	}

	public void setLastExcRecover(long lastExcRecover) {
		this.lastExcRecover = lastExcRecover;
	}

	public void setHelp(int help) {
		this.help = help;
	}

	public void setLastHelpRecover(long lastHelpRecover) {
		this.lastHelpRecover = lastHelpRecover;
	}

	public void setLastRefrash(long lastRefrash) {
		this.lastRefrash = lastRefrash;
	}

	public void setOverDay(long overDay) {
		this.overDay = overDay;
	}

	public int getExcCount() {
		return excCount;
	}

	public void setExcCount(int excCount) {
		this.excCount = excCount;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	
	public String getOwnerKey() {
		return playerId;
	}
}
