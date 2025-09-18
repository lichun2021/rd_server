package com.hawk.game.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.game.aoi.HawkAOIObj;
import org.hawk.game.aoi.HawkAOIScene;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldObjType;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldSnowballService;

public class WorldScene extends HawkAOIScene {
	/**
	 * 世界场景实例单例
	 */
	private static WorldScene instance;

	/**
	 * 获取单例对象
	 * 
	 * @return
	 */
	public static WorldScene getInstance() {
		return instance;
	}

	/**
	 * 世界场景构造
	 * 
	 * @param width
	 * @param height
	 * @param maxRadius
	 */
	public WorldScene(int width, int height, int searchRadius) {
		super(width, height, searchRadius);
		instance = this;
	}

	/**
	 * 判断是否为世界点上的对象
	 * 
	 * @param aoiObj
	 * @return
	 */
	public boolean isWorldPointObjs(HawkAOIObj aoiObj) {
		if (aoiObj.getType() == GsConst.WorldObjType.MONSTER ||
				aoiObj.getType() == GsConst.WorldObjType.RESOURCE ||
				aoiObj.getType() == GsConst.WorldObjType.CITY ||
				aoiObj.getType() == GsConst.WorldObjType.ARMY ||
				aoiObj.getType() == GsConst.WorldObjType.GUILD_GUARD ||
				aoiObj.getType() == GsConst.WorldObjType.GUILD_TERRITORY ||
				aoiObj.getType() == GsConst.WorldObjType.MOVEABLE_BUILDING ||
				aoiObj.getType() == GsConst.WorldObjType.ROBOT ||
				aoiObj.getType() == GsConst.WorldObjType.BOX ||
				aoiObj.getType() == GsConst.WorldObjType.YURI ||
				aoiObj.getType() == GsConst.WorldObjType.PRESIDENT ||
				aoiObj.getType() == GsConst.WorldObjType.PRESIDENT_TOWER ||
				aoiObj.getType() == GsConst.WorldObjType.STRONGPOINT ||
				aoiObj.getType() == GsConst.WorldObjType.FOGGYFORTESS || 
				aoiObj.getType() == GsConst.WorldObjType.SUPER_WEAPON ||
				aoiObj.getType() == GsConst.WorldObjType.YURI_STRIKE ||
				aoiObj.getType() == GsConst.WorldObjType.GOUDA ||
				aoiObj.getType() == GsConst.WorldObjType.NIAN ||
				aoiObj.getType() == GsConst.WorldObjType.TREASURE_MON ||
				aoiObj.getType() == GsConst.WorldObjType.TREASURE_RES ||
				aoiObj.getType() == GsConst.WorldObjType.WAR_FLAG ||
				aoiObj.getType() == GsConst.WorldObjType.RESOURC_TRESURE ||
				aoiObj.getType() == GsConst.WorldObjType.CROSS_FORTRESS ||
				aoiObj.getType() == GsConst.WorldObjType.NIAN_BOX ||
				aoiObj.getType() == GsConst.WorldObjType.PYLON ||
				aoiObj.getType() == GsConst.WorldObjType.SNOWBALL ||
				aoiObj.getType() == GsConst.WorldObjType.CHRISTMAS_BOSS ||
				aoiObj.getType() == GsConst.WorldObjType.CHRISTMAS_BOX ||
				aoiObj.getType() == GsConst.WorldObjType.DRAGON_BOAT ||
				aoiObj.getType() == GsConst.WorldObjType.GHOST_TOWER_MONSTER||
				aoiObj.getType() == GsConst.WorldObjType.CAKE_SHARE||
				aoiObj.getType() == GsConst.WorldObjType.XQZ_BUILD || 
				aoiObj.getType() == GsConst.WorldObjType.NATION_BUILD ||
				aoiObj.getType() == GsConst.WorldObjType.RESOURCE_SPREE_BOX ||
				aoiObj.getType() == GsConst.WorldObjType.SPACE_MECHA_MAIN ||
				aoiObj.getType() == GsConst.WorldObjType.SPACE_MECHA_SLAVE ||
				aoiObj.getType() == GsConst.WorldObjType.SPACE_MECHA_MONSTER ||
				aoiObj.getType() == GsConst.WorldObjType.SPACE_MECHA_STRONG_HOLD ||
				aoiObj.getType() == GsConst.WorldObjType.SPACE_MECHA_BOX) {
			return true;
		}
		return false;
	}

	@Override
	protected void notifyObjEnter(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}
		
		WorldPoint worldPoint = (WorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
		WorldPointPB.Builder pointBuilder = WorldPointPB.newBuilder();
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		builder.setServerId(GsConfig.getInstance().getServerId());
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == WorldObjType.PLAYER) {
				Player player = (Player) targetObj.getUserData();
				if (player == null) {
					continue;
				}
				builder.clearPoints();
				builder.addPoints(worldPoint.toBuilder(pointBuilder.clear(), player.getId()));
				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			}
		}
	}

	@Override
	protected void notifyObjUpdate(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}
		
		WorldPoint worldPoint = (WorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
	
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		builder.setServerId(GsConfig.getInstance().getServerId());
		WorldPointPB.Builder pointBuilder = WorldPointPB.newBuilder();
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				Player player = (Player) targetObj.getUserData();
				if (player == null) {
					continue;
				}
				builder.clearPoints();
				builder.addPoints(worldPoint.toBuilder(pointBuilder.clear(), player.getId()));
				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			}
		}
	}

	@Override
	protected void notifyObjMove(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		
	}

	@Override
	protected void notifyObjLeave(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}
		
		WorldPoint worldPoint = (WorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
		WorldPointPB.Builder pointBuilder = WorldPointPB.newBuilder();
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		builder.setServerId(GsConfig.getInstance().getServerId());
		builder.setIsRemove(true);
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				Player player = (Player) targetObj.getUserData();
				if (player == null) {
					continue;
				}
				builder.clearPoints();
				
				builder.addPoints(worldPoint.toBuilder(pointBuilder.clear(), player.getId()));
				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
				
				if (worldPoint.getPointType() == WorldPointType.SNOWBALL_VALUE) {
					WorldSnowballService.getInstance().notifySnowballMove(player, worldPoint);
				}
			}
		}
	}
	
	@Override
	protected void syncSceneObjs(HawkAOIObj aoiObj, Set<HawkAOIObj> oldInviewObjs, Set<HawkAOIObj> nowInviewObjs, float moveSpeed) {
		if (aoiObj.getType() == GsConst.WorldObjType.PLAYER && GsApp.getInstance().isRunning()) {
			Player player = (Player) aoiObj.getUserData();
			if (player == null) {
				return;
			}

			// 需要新同步给本对象的集合
			Set<HawkAOIObj> inviewObjs = new HashSet<HawkAOIObj>(nowInviewObjs.size());
			inviewObjs.addAll(nowInviewObjs);
			if (!HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor()) && !HawkOSOperator.isZero(moveSpeed)) {
				if (oldInviewObjs != null) {
					inviewObjs.removeAll(oldInviewObjs);
				}
			}
			
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setServerId(GsConfig.getInstance().getServerId());
			WorldPointPB.Builder pointBuilder = WorldPointPB.newBuilder();
			
			float randFactor = 1.0f;
			if (!HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor())) {
				randFactor = (float) Math.pow(GameConstCfg.getInstance().getMoveSyncFactor() - moveSpeed, 2);
			}

			for (HawkAOIObj inviewObj : inviewObjs) {
				try {
					if (!isWorldPointObjs(inviewObj)) {
						continue;
					}

					WorldPoint worldPoint = (WorldPoint) inviewObj.getUserData();
					if (worldPoint == null || !worldPoint.isVisible()) {
						continue;
					}
					
					if (randFactor >= 1.0f) {
						builder.addPoints(worldPoint.toBuilder(pointBuilder.clear(), player.getId()));
					} else {
						try {
							if (HawkRand.randFloat(0.0f, 1.0f) <= randFactor) {
								builder.addPoints(worldPoint.toBuilder(pointBuilder.clear(), player.getId()));
							}
						} catch (HawkException e) {
							HawkException.catchException(e);
						}
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			if ((WorldPointService.getInstance().getWorldSyncFlag() & GsConst.WorldSyncFlag.SYNC_WORLD) > 0) {
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
				player.sendProtocol(protocol);
			}
		}
	}

	/**
	 * 获取玩家视野中的对象
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public List<WorldPointPB.Builder> getPlayerViewObjs(String viewerId, int x, int y, float moveSpeed) {		
		int viewRadiusX = GameConstCfg.getInstance().getViewXRadius();
		int viewRadiusY = GameConstCfg.getInstance().getViewYRadius();
		Set<HawkAOIObj> inviewObjs = getRangeObjs(x, y, viewRadiusX, viewRadiusY);
		List<WorldPointPB.Builder> builderList = new ArrayList<WorldPointPB.Builder>(inviewObjs.size());
		fillWorldPoints(viewerId, inviewObjs, moveSpeed, builderList);
		return builderList;
	}

	/**
	 * 根据移动速度填充同步的世界点
	 * 
	 * @param inviewObjs
	 * @param moveSpeed
	 * @param builderList
	 * @return
	 */
	private int fillWorldPoints(String viewerId, Set<HawkAOIObj> inviewObjs, float moveSpeed, List<WorldPointPB.Builder> builderList) {
		float randFactor = 1.0f;
		if (!HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor())) {
			randFactor = (float) Math.pow(GameConstCfg.getInstance().getMoveSyncFactor() - moveSpeed, 2);
		}

		for (HawkAOIObj inviewObj : inviewObjs) {
			try {
				if (!isWorldPointObjs(inviewObj)) {
					continue;
				}

				WorldPoint worldPoint = (WorldPoint) inviewObj.getUserData();
				if (worldPoint == null || !worldPoint.isVisible()) {
					continue;
				}
				if (randFactor >= 1.0f) {
					builderList.add(worldPoint.toBuilder(WorldPointPB.newBuilder(), viewerId));
				} else {
					try {
						if (HawkRand.randFloat(0.0f, 1.0f) <= randFactor) {
							builderList.add(worldPoint.toBuilder(WorldPointPB.newBuilder(), viewerId));
						}
					} catch (HawkException e) {
						HawkException.catchException(e);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return builderList.size();
	}

	@Override
	protected void broadcastObjProtocol(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs, HawkProtocol protocol) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}
		
		WorldPoint worldPoint = (WorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
	
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				Player player = (Player) targetObj.getUserData();
				if (player == null) {
					continue;
				}
				player.sendProtocol(protocol);
			}
		}
	}
}
