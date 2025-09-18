package com.hawk.game.entity.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 集结编队信息
 * @author Golden
 *
 */
public class GuildFormationCell {

	/**
	 * 序号
	 */
	private MassFormationIndex index;
	
	/**
	 * 编队名字
	 */
	private String name = "";
	
	/**
	 * 队长Id
	 */
	private String leaderId = "";
	
	/**
	 * 出战玩家Id
	 */
	private Set<String> fightPlayerIds = new ConcurrentHashSet<>();
	
	/**
	 * 使用这个编队的行军id
	 */
	protected Set<String> marchIds = new ConcurrentHashSet<>();
	
	/**
	 * 更新时间
	 */
	private long updateTime = 0L;
	
	/**
	 * 加载
	 * @param str
	 * @return
	 */
	public static GuildFormationCell load(String str) {
		GuildFormationCell cell = new GuildFormationCell();
		cell.unSerializ(str);
		return cell;
	}

	public void unSerializ(String str) {
		JSONObject obj = JSONObject.parseObject(str);
		index = MassFormationIndex.valueOf(obj.getIntValue("index"));
		name = obj.getString("name");
		leaderId = obj.getString("leaderId");
		fightPlayerIds = SerializeHelper.stringToSet(
				String.class, 
				obj.getString("idStr"), 
				SerializeHelper.SEMICOLON_ITEMS, 
				null, 
				new ConcurrentHashSet<>());
		marchIds = SerializeHelper.stringToSet(
				String.class, 
				obj.getString("marchIdStr"), 
				SerializeHelper.SEMICOLON_ITEMS, 
				null, 
				new ConcurrentHashSet<>());
		updateTime = obj.getLongValue("updateTime");
	}

	/**
	 * 序列化
	 * @return
	 */
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("index", index.getNumber());
		obj.put("name", name);
		obj.put("leaderId", leaderId);
		String idStr = SerializeHelper.collectionToString(fightPlayerIds, SerializeHelper.SEMICOLON_ITEMS);
		obj.put("idStr", idStr);
		String marchIdStr = SerializeHelper.collectionToString(marchIds, SerializeHelper.SEMICOLON_ITEMS);
		obj.put("marchIdStr", marchIdStr);
		obj.put("updateTime", updateTime);
		return obj.toJSONString();
	}
	
	/**
	 * 获取队列编号
	 * @return
	 */
	public MassFormationIndex getIndex() {
		return index;
	}

	public void setIndex(MassFormationIndex index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public boolean isLeader(String playerId) {
		return playerId.equals(leaderId);
	}
	
	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}
	
	public int getMemberCount() {
		return fightPlayerIds.size();
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public boolean fight(String playerId) {
		return fightPlayerIds.contains(playerId);
	}
	
	public Set<String> getFightIds() {
		return fightPlayerIds;
	}
	
	public void addFight(String playerId) {
		fightPlayerIds.add(playerId);
	}
	
	public void delFight(String playerId) {
		fightPlayerIds.remove(playerId);
	}
	
	public Set<String> getMarchIds() {
		return marchIds;
	}

	public void addMarchId(String marchId) {
		this.marchIds.add(marchId);
	}
	
	public boolean hasMarch(String marchId) {
		return this.marchIds.contains(marchId);
	}
	
	/**
	 * 需要提醒加入的集结队伍
	 * @param marchId
	 * @param playerId
	 * @return
	 */
	public Set<String> getNoticeJoinMarchIds(String playerId) {
		Set<String> ret = new HashSet<>();
		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				continue;
			}
			if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				continue;
			}
			ret.add(marchId);
		}
		return ret;
	}
	
	/**
	 * 检测行军id移除
	 */
	public void checkMarchIdRemove() {
		List<String> rmList = new ArrayList<>();
		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				rmList.add(marchId);
				continue;
			}
		}
		marchIds.removeAll(rmList);		
	}
}
