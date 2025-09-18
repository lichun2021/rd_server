package com.hawk.activity.type.impl.ghostSecret.entity;

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
import com.hawk.game.protocol.Activity.TreasureType;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_ghost_secret")
public class GhostSecretEntity extends HawkDBEntity implements IActivityDataEntity{
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
	
	/**翻牌数据 */
    @IndexProp(id = 4)
	@Column(name = "drewInfo", nullable = false)
	private String drewInfo;
	
	/**挖宝藏总次数 */
    @IndexProp(id = 5)
	@Column(name = "drewNum", nullable = false)
	private int drewNum;
	
	/**今日重置次数 */
    @IndexProp(id = 6)
	@Column(name = "resetNum", nullable = false)
	private int resetNum;
	
	/**当天已中最大奖 */
    @IndexProp(id = 7)
	@Column(name = "specAwardGot", nullable = false)
	private boolean specAwardGot;

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
	private List<String> drewInfoList = new ArrayList<>();

	public GhostSecretEntity() {
	}
	
	public GhostSecretEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public GhostSecretEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.drewInfo = initDrewInfo();
	}
	
	
	/**初始化九宫格,9个0
	 * @return
	 */
	public String initDrewInfo(){
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 1; i <= 9; i++) {
			sBuilder.append(TreasureType.TYPE_CLOSE_VALUE);
			if (i < 9) {
				sBuilder.append("_");
			}
		}
		return sBuilder.toString();
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
	
	@Override
	public void beforeWrite() {
		this.drewInfo = SerializeHelper.collectionToString(this.drewInfoList, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public void afterRead() {
		//SerializeHelper.stringToList(String.class, this.drewInfo, this.drewInfoList);
		this.drewInfoList = SerializeHelper.stringToList(String.class, drewInfo, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public boolean isSpecAwardGot() {
		return specAwardGot;
	}

	public void setSpecAwardGot(boolean specAwardGot) {
		this.specAwardGot = specAwardGot;
	}

	public String getDrewInfo() {
		return drewInfo;
	}

	public void setDrewInfo(String drewInfo) {
		this.drewInfo = drewInfo;
	}

	public int getDrewNum() {
		return drewNum;
	}

	public void setDrewNum(int drewNum) {
		this.drewNum = drewNum;
	}
	
	public int getResetNum() {
		return resetNum;
	}

	public void setResetNum(int resetNum) {
		this.resetNum = resetNum;
	}

	public List<String> getDrewInfoList() {
		if (drewInfoList.isEmpty()) {
			drewInfoList = initDrewInfoList();
			notifyUpdate();
		}
		return drewInfoList;
	}

	public void setDrewInfoList(List<String> drewInfoList) {
		this.drewInfoList = drewInfoList;
	}
	
	public void resetDrewInfoList(){
		this.drewInfoList = initDrewInfoList();
		notifyUpdate();
	}
	
	public List<String> initDrewInfoList(){
		List<String> list = new ArrayList<>();
		for (int i = 1; i <= 9; i++) {
			list.add(String.valueOf(TreasureType.TYPE_CLOSE_VALUE));
		}
		return list;
	}
	
}
