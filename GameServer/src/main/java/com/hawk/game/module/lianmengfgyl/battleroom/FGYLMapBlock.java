package com.hawk.game.module.lianmengfgyl.battleroom;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.util.GameUtil;

/**
 * 地图阻挡信息
 * @author julia
 */
public class FGYLMapBlock {
	/**
	 * 阻挡点id列表
	 */
	private Set<Integer> stops = new TreeSet<Integer>();

	private static FGYLMapBlock instance = new FGYLMapBlock();

	public static FGYLMapBlock getInstance() {
		return instance;
	}

	public boolean init() {
		return loadMapData();// && initMapRoundBlock();
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
		String filePath = HawkOSOperator.getWorkPath() + "xml/kvk_gve_tmx_block.dat";
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
	 * 是否是阻挡点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isStopPoint(int pointId) {
		return stops.contains(pointId);
	}

}
