package com.hawk.robot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.os.HawkTime;

import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.config.MapBlock;
import com.hawk.robot.util.GuildUtil;
import com.hawk.robot.util.WorldUtil;

public class WorldData {
	/**
	 * 机器人信息(上层数据)
	 */
	protected GameRobotData robotData;

	protected WorldInfoPush worldInfo;
	
	protected WorldPointSync worldPointSync;
	
	protected WorldSearchResp worldSearchResp;
	
	private Map<Integer, WorldPointPB> areaPoints = new HashMap<Integer, WorldPointPB>();
	/**
	 * 玩家行军信息
	 */
	protected List<String> marchIds = new CopyOnWriteArrayList<String>();
	
	public WorldData(GameRobotData gameRobotData) {
		robotData = gameRobotData;
	}
	
	public GameRobotData getRobotData() {
		return robotData;
	}

	public WorldInfoPush getWorldInfo() {
		return worldInfo;
	}

	public void setWorldInfo(WorldInfoPush worldInfo) {
		this.worldInfo = worldInfo;
	}

	public WorldPointSync getWorldPointSync() {
		return worldPointSync;
	}

	public void setWorldPointSync(WorldPointSync worldPointSync) {
		if(worldPointSync.getPointsList().size() > 1){
			this.worldPointSync = worldPointSync;
		}
		for (WorldPointPB point : worldPointSync.getPointsList()) {
			//需要将点的占用点也存入
			int radius = WorldUtil.getPointRadius(point.getPointType().getNumber(), point.getTerriId());
			List<Integer> arroundPonit = WorldUtil.getRadiusAllPoints(point.getPointX(), point.getPointY(), radius);
			for (Integer pointId : arroundPonit) {
				this.areaPoints.put(pointId, point);
			}
		}
//		WorldUtil.testDrawMap(areaPoints);
	}
	
	public boolean checkExsit(int x, int y){
		return this.areaPoints.containsKey(WorldUtil.combineXAndY(x, y)) || MapBlock.getInstance().isStopPoint(WorldUtil.combineXAndY(x, y));
	}
	
	public Map<Integer, WorldPointPB> getAreaPoints() {
		return areaPoints;
	}

	public WorldSearchResp getWorldSearchResp() {
		return worldSearchResp;
	}

	public void setWorldSearchResp(WorldSearchResp worldSearchResp) {
		this.worldSearchResp = worldSearchResp;
	}

	/**
	 * 获取玩家世界行军数据
	 * @return
	 */
	public List<String> getMarchIdList() {
		return marchIds;
	}
	
	public int getMarchCount() {
		return marchIds.size();
	}
	
	/**
	 * 刷新行军数据
	 * @param worldMarch
	 */
	public void refreshWorldMarch(GameRobotEntity robotEntity, WorldMarchPB... worldMarchPBs) {
		for(WorldMarchPB worldMarch : worldMarchPBs) {
			if(worldMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED) {
				worldMarch = worldMarch.toBuilder().setStartTime(HawkTime.getMillisecond()).build();
			}
			
			if (!marchIds.contains(worldMarch.getMarchId())) {
				marchIds.add(worldMarch.getMarchId());
			}
			
			WorldDataManager.getInstance().addMarch(robotEntity.getGuildId(), worldMarch);
			
			//增加领地行军的数量, 回程行军不计入
			if(GuildUtil.isManorMarch(worldMarch.getMarchType()) && worldMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK){
				WorldDataManager.getInstance().addPointMarch(WorldUtil.combineXAndY(worldMarch.getTerminalX(), worldMarch.getTerminalY()), worldMarch.getMarchId());
			}
		}
	}
	
	/**
	 * 删除行军
	 * @param marchId
	 */
	public void delWorldMarch(GameRobotEntity robotEntity, String marchId) {
		marchIds.remove(marchId);
		WorldMarchPB march = WorldDataManager.getInstance().removeMarch(robotEntity.getGuildId(), marchId);
		if(march != null && GuildUtil.isManorMarch(march.getMarchType())){
			WorldDataManager.getInstance().removePointMarch(WorldUtil.combineXAndY(march.getTerminalX(), march.getTerminalY()), marchId);
			WorldDataManager.getInstance().removePointMarch(WorldUtil.combineXAndY(march.getOrigionX(), march.getOrigionY()), marchId);
		}
	}
	
}
