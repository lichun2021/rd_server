package com.hawk.game.module.lianmengyqzz.battleroom;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBattleCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZAreaCfg;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

/**
 * 地图阻挡信息
 * @author julia
 */
public class YQZZMapBlock {
	/**
	 * 阻挡点id列表
	 */
	private Set<Integer> stops = new TreeSet<Integer>();
	private Map<Integer, Integer> pointNumber = new HashMap<>();
	private Map<Integer, Integer> subareaCircle = new HashMap<>();
	private Map<Integer, YQZZRandPointSeed> pointSeedmap = new HashMap<>();

	private static YQZZMapBlock instance = new YQZZMapBlock();

	public static YQZZMapBlock getInstance() {
		return instance;
	}

	public boolean init() {
		return loadMapData() && loadSubareaData() && loadSubareaCircle();// && initMapRoundBlock();
	}

	/**
	 * 加载预置数据
	 * @return
	 */
	private boolean loadMapData() {
		short[] rowArray;
		short[] colArray;
		boolean[] stopArray;
		short[] territoryIdArray;
		short[] strongholdIdArray;
		String filePath = HawkOSOperator.getWorkPath() + "xml/moon_war_tmx_block.dat";
		try {
			// 读取配置文件内容
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			fis.close();
			// 解析内容
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			int len = dis.readInt();
			rowArray = new short[len];
			colArray = new short[len];
			stopArray = new boolean[len];
			territoryIdArray = new short[len];
//			strongholdIdArray = new short[len];
			for (int i = 0; i < len; i++) {
//				System.out.println("i = "+i);
				colArray[i] = dis.readShort();// 列
				rowArray[i] = dis.readShort();// 行
				stopArray[i] = dis.readBoolean(); // 阻挡
				territoryIdArray[i] = dis.readShort(); // 联盟领地ID
//				strongholdIdArray[i] = dis.readShort(); // 迷雾ID
			}
			for (int i = 0; i < rowArray.length; i++) {
				int x = colArray[i];
				int y = rowArray[i];
				int pointId = GameUtil.combineXAndY(x, y);
				// 阻挡
				if (stopArray[i]) {
					stops.add(pointId);
				}
			}
			dis.close();
			return true;
		} catch (IOException e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 加载预置数据
	 * @return
	 */
	private boolean loadSubareaData() {
		short[] rowArray;
		short[] colArray;
		boolean[] stopArray;
		short[] subareaArray;
		String filePath = HawkOSOperator.getWorkPath() + "xml/moon_war_tmx_grass.dat";
		try {
			// 读取配置文件内容
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			fis.close();
			// 解析内容
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			int len = dis.readInt();
			rowArray = new short[len];
			colArray = new short[len];
			stopArray = new boolean[len];
			subareaArray = new short[len];
			for (int i = 0; i < len; i++) {
				colArray[i] = dis.readShort();// 列
				rowArray[i] = dis.readShort();// 行
				stopArray[i] = dis.readBoolean(); // 阻挡
				subareaArray[i] = dis.readShort(); // 联盟领地ID
			}
			for (int i = 0; i < rowArray.length; i++) {
				int x = colArray[i];
				int y = rowArray[i];
				int pointId = GameUtil.combineXAndY(x, y);
				int subareaNum = (int) subareaArray[i];
				pointNumber.put(pointId, subareaNum);
				if (!pointSeedmap.containsKey(subareaNum)) {
					pointSeedmap.put(subareaNum, new YQZZRandPointSeed());
				}
				pointSeedmap.get(subareaNum).add(pointId);
				// System.out.println(len+" " +x+", "+y+ " "+ pointNumber.get(pointId));
			}
			dis.close();
			pointSeedmap.values().forEach(YQZZRandPointSeed::shuffle);
			return true;
		} catch (IOException e) {
			HawkException.catchException(e);
		}
		return false;
	}

	public boolean loadSubareaCircle() {
		ConfigIterator<YQZZAreaCfg> bit = HawkConfigManager.getInstance().getConfigIterator(YQZZAreaCfg.class);
		for (YQZZAreaCfg cfg : bit) {
			subareaCircle.put(cfg.getAreaId(), cfg.getCircle());
		}
		return true;
	}

	/**
	 * 是否是阻挡点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isStopPoint(int pointId) {
		return stops.contains(pointId);
	}

	public int subareaNum(int pointId) {
		return pointNumber.getOrDefault(pointId, 0);
	}

	public int circleNumber(int pointId) {
		return subareaCircle.getOrDefault(subareaNum(pointId), 1);
	}

	public YQZZRandPointSeed getRandPointSeedBySubareaNum(int subareaNum) {
		return pointSeedmap.get(subareaNum);
	}

	/**
	 * 添加阻挡点id
	 * 
	 * @param pointId
	 */
	private void addStopPoint(int pointId) {
		stops.add(pointId);
	}

	/**
	 * 初始化, 排除周边一圈的点信息, 加入阻挡点
	 */
	private boolean initMapRoundBlock() {
		DYZZBattleCfg roomCfg = HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
		// 世界地图最大坐标
		int worldMaxX = roomCfg.getMapX();
		int worldMaxY = roomCfg.getMapY();
		// 排除周边一圈的点信息, 加入阻挡点
		int boundary = GsConst.WORLD_BOUNDARY_SIZE;
		for (int y = 0; y <= worldMaxY; y++) {
			for (int x = 0; x <= worldMaxX; x++) {
				if (y <= boundary || y >= worldMaxY - boundary || x <= boundary || x >= worldMaxX - boundary) {
					addStopPoint(GameUtil.combineXAndY(x, y));
				}
			}
		}
		return true;
	}

}
