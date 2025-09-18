package com.hawk.game.module.lianmengfgyl.battleroom;

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
import com.hawk.game.module.lianmengfgyl.battleroom.player.FGYLPlayerEye;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldObjType;
import com.hawk.game.world.service.WorldPointService;

/**
 * 滑动的时候 是客户端主动删除远处的点的
 * @author lwt
 * @date 2020年9月14日
 */
public class FGYLWorldScene extends HawkAOIScene {

	/**
	 * 世界场景构造
	 * 
	 * @param width
	 * @param height
	 * @param maxRadius
	 */
	public FGYLWorldScene(int width, int height, int searchRadius) {
		super(width, height, searchRadius);
	}

	/**
	 * 判断是否为世界点上的对象
	 * 
	 * @param aoiObj
	 * @return
	 */
	public boolean isWorldPointObjs(HawkAOIObj aoiObj) {
		return aoiObj.getType() != GsConst.WorldObjType.PLAYER;
	}

	@Override
	protected void notifyObjEnter(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}

		IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		builder.setServerId(GsConfig.getInstance().getServerId());
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == WorldObjType.PLAYER) {
				FGYLPlayerEye playerEye = (FGYLPlayerEye) targetObj.getUserData();
				if (playerEye == null) {
					continue;
				}
				builder.clearPoints();
				builder.addPoints(worldPoint.toBuilder(playerEye.getParent()));
				playerEye.getParent().sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			}
		}
	}

	@Override
	protected void notifyObjUpdate(HawkAOIObj aoiObj, Set<HawkAOIObj> targetObjs) {
		if (!isWorldPointObjs(aoiObj) || !GsApp.getInstance().isRunning()) {
			return;
		}

		IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}

		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		builder.setServerId(GsConfig.getInstance().getServerId());
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				FGYLPlayerEye playerEye = (FGYLPlayerEye) targetObj.getUserData();
				if (playerEye == null) {
					continue;
				}
				builder.clearPoints();
				builder.addPoints(worldPoint.toBuilder(playerEye.getParent()));
				playerEye.getParent().sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
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

		IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}
		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				FGYLPlayerEye playerEye = (FGYLPlayerEye) targetObj.getUserData();
				if (playerEye == null) {
					continue;
				}
				WorldPointSync.Builder builder = WorldPointSync.newBuilder();
				builder.setServerId(GsConfig.getInstance().getServerId());
				builder.setIsRemove(true);
				builder.addPoints(worldPoint.toBuilder(playerEye.getParent()));
				playerEye.getParent().sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			}
		}
	}

	@Override
	protected void syncSceneObjs(HawkAOIObj aoiObj, Set<HawkAOIObj> oldInviewObjs, Set<HawkAOIObj> nowInviewObjs, float moveSpeed) {
		if (aoiObj.getType() == GsConst.WorldObjType.PLAYER && GsApp.getInstance().isRunning()) {
			FGYLPlayerEye playerEye = (FGYLPlayerEye) aoiObj.getUserData();
			if (playerEye == null) {
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

			float randFactor = 1.0f;
			if (!HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor())) {
				randFactor = (float) Math.pow(GameConstCfg.getInstance().getMoveSyncFactor() - moveSpeed, 2);
			}

			for (HawkAOIObj inviewObj : inviewObjs) {
				try {
					if (!isWorldPointObjs(inviewObj)) {
						continue;
					}

					IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) inviewObj.getUserData();
					if (worldPoint == null) {
						continue;
					}

					if (randFactor >= 1.0f) {
						builder.addPoints(worldPoint.toBuilder(playerEye.getParent()));
					} else {
						try {
							if (HawkRand.randFloat(0.0f, 1.0f) <= randFactor) {
								builder.addPoints(worldPoint.toBuilder(playerEye.getParent()));
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
				playerEye.getParent().sendProtocol(protocol);
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
	public List<WorldPointPB.Builder> getPlayerViewObjs(FGYLPlayerEye viewer, int x, int y, float moveSpeed) {
		int viewRadiusX = GameConstCfg.getInstance().getViewXRadius();
		int viewRadiusY = GameConstCfg.getInstance().getViewYRadius();
		Set<HawkAOIObj> inviewObjs = getRangeObjs(x, y, viewRadiusX, viewRadiusY);
		List<WorldPointPB.Builder> builderList = new ArrayList<WorldPointPB.Builder>(inviewObjs.size());
		fillWorldPoints(viewer, inviewObjs, moveSpeed, builderList);
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
	private int fillWorldPoints(FGYLPlayerEye viewer, Set<HawkAOIObj> inviewObjs, float moveSpeed, List<WorldPointPB.Builder> builderList) {
		float randFactor = 1.0f;
		if (!HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor())) {
			randFactor = (float) Math.pow(GameConstCfg.getInstance().getMoveSyncFactor() - moveSpeed, 2);
		}

		for (HawkAOIObj inviewObj : inviewObjs) {
			try {
				if (!isWorldPointObjs(inviewObj)) {
					continue;
				}

				IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) inviewObj.getUserData();
				if (worldPoint == null) {
					continue;
				}
				if (randFactor >= 1.0f) {
					builderList.add(worldPoint.toBuilder(viewer.getParent()));
				} else {
					try {
						if (HawkRand.randFloat(0.0f, 1.0f) <= randFactor) {
							builderList.add(worldPoint.toBuilder(viewer.getParent()));
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

		IFGYLWorldPoint worldPoint = (IFGYLWorldPoint) aoiObj.getUserData();
		if (worldPoint == null) {
			return;
		}

		for (HawkAOIObj targetObj : targetObjs) {
			if (targetObj.getType() == GsConst.WorldObjType.PLAYER) {
				FGYLPlayerEye playerEye = (FGYLPlayerEye) targetObj.getUserData();
				if (playerEye == null) {
					continue;
				}
				playerEye.getParent().sendProtocol(protocol);
			}
		}
	}
}
