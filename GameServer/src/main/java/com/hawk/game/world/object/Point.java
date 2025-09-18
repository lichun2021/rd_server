package com.hawk.game.world.object;

import java.util.List;

import com.hawk.game.module.spacemecha.MechaSpaceConst.SpaceMechaGrid;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldPointService;

/**
 * 一个坐标点,临时存储使用
 * @author julia
 */
public class Point{
	
	// 点的坐标id, 复合值
	private int id;
	// 坐标x
	private int x;
	// 坐标y
	private int y;
	// 程序划分刷新区块id
	private int areaId;
	// 所属资源带id
	private int zoneId;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		this.id = GameUtil.combineXAndY(x, y);
	}
	
	public Point(int pointId) {
		this.id = pointId;
		int[] xy = GameUtil.splitXAndY(pointId);
		this.x = xy[0];
		this.y = xy[1];
	}

	public Point(int x, int y, int areaId, int zoneId) {
		this.x = x;
		this.y = y;
		this.areaId = areaId;
		this.zoneId = zoneId;
		this.id = GameUtil.combineXAndY(x, y);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}

	/**
	 * 玩家城点是否可以落座
	 * 
	 * @return
	 */
	public boolean canPlayerSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 机甲是否可以落座
	 * 
	 * @return
	 */
	public boolean canGundamSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 年兽是否可以落座
	 * 
	 * @return
	 */
	public boolean canNianSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 == 0) {
			return false;
		}
		return true;
	}
	
	public boolean canChristmasWarSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 玩家城点是否可以落座
	 * 
	 * @return
	 */
	public boolean canQuarteredSeat() {
		if ((x + y) % 2 != 0) {
			return false;
		}
		// 战争区域不可以驻扎
		if(WorldPointService.getInstance().isInCapitalArea(this.id)){
			return false;
		}
		return true;
	}	
	
	/**
	 * 野怪点是否可以生成
	 * 
	 * @return
	 */
	public boolean canMonsterGen(MonsterType monsterType) {
		if(!checkPointFree()){
			return false;
		}
		if (monsterType == null || monsterType.equals(MonsterType.TYPE_1)
				|| monsterType.equals(MonsterType.TYPE_2)
				|| monsterType.equals(MonsterType.TYPE_7)) {
			if ((x + y) % 2 != 0) {
				return false;
			}
		} else {
			if ((x + y) % 2 == 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canSpaceMechaSeat(int pointType) {
		if(!checkPointFree()){
			return false;
		}
		
		if (pointType == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			return (x + y) % 2 != SpaceMechaGrid.SPACE_MAIN_GRID % 2;
		} 
		
		if (pointType == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			return (x + y) % 2 != SpaceMechaGrid.SPACE_SLAVE_GRID % 2;
		} 
		
		if (pointType == WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
			return (x + y) % 2 != SpaceMechaGrid.MONSTER_GRID % 2;
		} 
		
		if (pointType == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			return (x + y) % 2 != SpaceMechaGrid.STRONG_HOLD_GRID % 2;
		}
		
		return (x + y) % 2 != SpaceMechaGrid.MECHA_BOX % 2;
	}
	
	/**
	 * yuri是否可以落座
	 * 
	 * @return
	 */
	public boolean canYuriSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 == 0) {
			return false;
		}
		int distance = GsConst.PLAYER_POINT_RADIUS;
		// 城点距离限制范围内的所有点(1距离为4个点, 2距离为12个点)
		List<Point> freeAroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(x, y, distance);
		// 必须为4个, 否则就是有阻挡点存在
		if (freeAroundPoints.size() != 2 * distance * (distance - 1)) {
			return false;
		}
		// 首都区域
//		if(WorldPointService.getInstance().isInCapitalArea(this.id)){
//			return false;
//		}
		return true;
	}

	/**
	 * 是否可以刷新资源或者怪物
	 * 
	 * @return
	 */
	public boolean canRMSeat() {
		if(!checkPointFree()){
			return false;
		}
		if ((x + y) % 2 != 0) {
			return false;
		}
		return true;
	}
	
	public boolean canTerrSeat(TerritoryType territoryType){
		if(!checkPointFree()){
			return false;
		}
		if(territoryType == TerritoryType.GUILD_BARTIZAN){
			if ((x + y) % 2 != 0) {
				return false;
			}
		} else {
			if ((x + y) % 2 == 0) {
				return false;
			}
		}
		// 首都区域
		if(WorldPointService.getInstance().isInCapitalArea(this.id)){
			return false;
		}
		return true;
	}
	
	/**
	 * 检查点是否是空闲点
	 * <pre>
	 * 此处一定要注意，之前的奇偶算法，存在漏洞，当地图上存在半径为偶数的建筑时，
	 * 此时放置半径为1的建筑，会放到偶数建筑的边缘点，从而重叠，所以仅检查奇偶是不行的。必须同时检查这个点是否是空闲点
	 * </pre>
	 * @return
	 */
	public boolean checkPointFree(){
		AreaObject areaObj = WorldPointService.getInstance().getArea(x, y);
		if(!areaObj.isFreePoint(x, y)){
			return false;
		}
		return true;
	}
}
