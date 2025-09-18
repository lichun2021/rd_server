package com.hawk.game.guild.manor.building;

import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.world.WorldPoint;

/**
 * 联盟建筑
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public interface IGuildBuilding {
	
	/**
	 * 获取实体
	 * @return
	 */
	public GuildBuildingEntity getEntity();
	
	/**
	 * 获取建筑状态
	 * @return
	 */
	public GuildBuildingStat getBuildStat();
	
	/**
	 * 改变建筑状态
	 * @param stat
	 * @return
	 */
	public boolean tryChangeBuildStat(int stat);
	
	/**
	 * 获取建筑类型
	 * @return
	 */
	public TerritoryType getBuildType();
	
	
	/**
	 * 组装协议
	 * @param builder
	 */
	public void addProtocol2Builder(GuildManorList.Builder builder);
	
	/**
	 * 获取世界点
	 * @return
	 */
	public WorldPoint getPoint();
	
	/**
	 * 解析建筑相关参数
	 */
	public void parseBuildingParam(String buildParam);
	
	/**
	 * 生成buildingParam字符串
	 * @return
	 */
	public String genBuildingParamStr();
	
	/**
	 * 获取建筑最大上限
	 * @return
	 */
	public int getBuildingUpLimit();
	
	/**
	 * 建筑是否可以被移除
	 * @return
	 */
	public boolean canRemove();
	
	/**
	 * 建筑被建成
	 * @return
	 */
	public boolean onBuildComplete();
	
	/**
	 * 建筑被收回
	 * @return
	 */
	public boolean onBuildRemove();
	
	/**
	 * 建筑被收回
	 * @return
	 */
	public boolean onBuildDelete();
	
	/**
	 * Tick
	 */
	public void tick();
	
	/**
	 * 当玩家退出联盟
	 */
	default void doQuitGuild(String playerId){};
	
	/**
	 * 获取坐标点ID
	 * @return
	 */
	public abstract int getPositionId();
	/**
	 * 是否没有放置在世界上
	 * @return
	 */
	public boolean isPlaceGround();
	
	/**
	 * 游戏服关闭时执行
	 */
	public void onCloseServer();
	
}
