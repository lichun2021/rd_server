package com.hawk.game.entity.item;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.entity.GuildInfoEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;

/**
 * 集结编队信息
 * 
 * @author Golden
 *
 */
public class GuildFormationObj {

	/**
	 * 数据库实体
	 */
	private GuildInfoEntity entity;

	/**
	 * 编队信息
	 */
	protected Map<MassFormationIndex, GuildFormationCell> formations;

	/**
	 * 加载
	 */
	public static GuildFormationObj load(GuildInfoEntity entity, String str) {
		GuildFormationObj obj = new GuildFormationObj();
		obj.entity = entity;
		obj.unSerializ(str);
		return obj;
	}

	/**
	 * 序列化
	 */
	public String serializ() {
		JSONArray arr = new JSONArray();
		formations.values().stream().map(GuildFormationCell::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	/**
	 * 反序列化
	 */
	public void unSerializ(String str) {
		formations = new ConcurrentHashMap<>();
		if (HawkOSOperator.isEmptyString(str)) {
			return;
		}

		JSONArray arr = JSONArray.parseArray(str);
		arr.forEach(s -> {
			GuildFormationCell cell = GuildFormationCell.load(s.toString());
			formations.put(cell.getIndex(), cell);
		});
	}

	/**
	 * 通知更新
	 */
	public void notifyUpdate() {
		if (entity != null) {
			entity.notifyUpdate();
			
			// 用于跨服,这里频率不高
			RedisProxy.getInstance().updateCsGuildFormation(entity.getId(), this.serializ());
		}
	}

	/**
	 * 获取编队信息
	 */
	public Collection<GuildFormationCell> getFormations() {
		return formations.values();
	}

	/**
	 * 获取编队信息
	 */
	public GuildFormationCell getFormation(MassFormationIndex index) {
		if (index == null) {
			return null;
		}
		return formations.get(index);
	}

	/**
	 * 初始化检测
	 */
	public void initCheck() {
		MassFormationIndex[] values = MassFormationIndex.values();
		if (values.length == formations.size()) {
			return;
		}
		
		for (int i = 0; i < values.length; i++) {
			MassFormationIndex index = values[i];
			GuildFormationCell cell = formations.get(index);
			if (cell != null) {
				continue;
			}
			cell = new GuildFormationCell();
			cell.setIndex(index);
			cell.setName("");
			formations.put(index, cell);
		}
		notifyUpdate();
	}

	/**
	 * 添加使用编队行军
	 */
	public void addFormationMarch(int indexInt, String marchId) {
		try {
			MassFormationIndex index = MassFormationIndex.valueOf(indexInt);
			if (index == null) {
				return;
			}
			formations.get(index).addMarchId(marchId);
			notifyUpdate();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取当前使用的集结编队
	 * @param marchId
	 */
	public GuildFormationCell getGuildFormation(String marchId) {
		for (GuildFormationCell formation: formations.values()) {
			if (formation.hasMarch(marchId)) {
				return formation;
			}
		}
		return null;
	}
	
	/**
	 * 玩家退出联盟
	 * @param playerId
	 */
	public void quitGuild(String playerId) {
		for (GuildFormationCell formation: formations.values()) {
			
			if (formation.getLeaderId().equals(playerId)) {
				formation.setLeaderId("");
			}
			if (formation.fight(playerId)) {
				formation.delFight(playerId);
			}
		}
	}
	
	/**
	 * 检测行军id移除
	 */
	public void checkMarchIdRemove() {
		for (GuildFormationCell formation : formations.values()) {
			try {
				formation.checkMarchIdRemove();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 是否在编队里面
	 * @param index
	 * @param playerId
	 * @return
	 */
	public boolean isInFormation(int index, String playerId) {
		GuildFormationCell guildFormationCell = formations.get(MassFormationIndex.valueOf(index));
		if (guildFormationCell == null) {
			return false;
		}
		return guildFormationCell.fight(playerId);
	}
}
