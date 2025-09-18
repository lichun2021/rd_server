package com.hawk.robot.config;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.robot.util.WorldUtil;

/**
 * 地图阻挡信息
 * @author julia
 */
public class MapBlock {
	/**
	 * 阻挡点id列表
	 */
	private Set<Integer> stops = new TreeSet<Integer>();

	private static MapBlock instance = null;

	public static MapBlock getInstance() {
		if (instance == null) {
			instance = new MapBlock();
		}
		return instance;
	}
	
	public boolean init() {
		return loadMapData() && initMapRoundBlock();
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
		String filePath = HawkOSOperator.getWorkPath() + "xml/tmx_block.dat";
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
			strongholdIdArray = new short[len];
			for (int i = 0; i < len; i++) {
				colArray[i] = dis.readShort();// 列
				rowArray[i] = dis.readShort();// 行
				stopArray[i] = dis.readBoolean(); // 阻挡
				territoryIdArray[i] = dis.readShort(); // 联盟领地ID
				strongholdIdArray[i] = dis.readShort(); // 迷雾ID
			}
			for (int i = 0; i < rowArray.length; i++) {
				if (rowArray[i] % 2 == 0) {
					colArray[i] = (short) (colArray[i] * 2 + 1);
					rowArray[i] = (short) (rowArray[i] + 1);
				} else {
					colArray[i] = (short) ((colArray[i] + 1) * 2);
					rowArray[i] = (short) (rowArray[i] + 1);
				}
				int x = colArray[i];
				int y = rowArray[i];
				int pointId = WorldUtil.combineXAndY(x, y);
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
	 * 初始化, 排除周边一圈的点信息, 加入阻挡点
	 */
	private boolean initMapRoundBlock(){
		// 世界地图最大坐标
		int worldMaxX = 600;
		int worldMaxY = 1200;
		// 排除周边一圈的点信息, 加入阻挡点
		int boundary = 4;
		for (int y = 0; y <= worldMaxY; y++) {
			for (int x = 0; x <= worldMaxX; x++) {
				if (y <= boundary || y >= worldMaxY - boundary || x <= boundary || x >= worldMaxX - boundary) {
					MapBlock.getInstance().addStopPoint(WorldUtil.combineXAndY(x, y));
				}
			}
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

	/**
	 * 添加阻挡点id
	 * 
	 * @param pointId
	 */
	public void addStopPoint(int pointId) {
		stops.add(pointId);
	}
}
