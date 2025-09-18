package com.hawk.activity.type.impl.mechacoreexplore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.type.impl.mechacoreexplore.entity.CoreExploreEntity;
import com.hawk.game.protocol.Activity.CEObstacleType;

public class CoreExploreHelper {

	/**
	 * 连通性数据初始化
	 */
	public static void refreshConnectedData(CoreExploreEntity entity) {
		try {
			entity.clearConnectedGrids();
			List<String> unconnectedEmptyGrids = new ArrayList<>();
			for (int i = 0; i < entity.getLineList().size(); i++) {
				int line = entity.getFirstLine() + i;
				checkConnected(entity, line, unconnectedEmptyGrids);
			}
			
			checkConnected(entity, unconnectedEmptyGrids);
			
		} catch (Exception e) {
			HawkException.catchException(e, entity.getPlayerId());
		}
	}
	
	/**
	 * 通路检测
	 * @param entity
	 * @param unconnectedEmptyGrids
	 */
	public static void checkConnected(CoreExploreEntity entity, Collection<String> unconnectedEmptyGrids) {
		List<List<Integer>> connectedGrids = entity.getConnectedGrids();
		int size = unconnectedEmptyGrids.size();
		while (size > 0) {
			size--;
			boolean change = false;
			Iterator<String> iterator = unconnectedEmptyGrids.iterator();
			while (iterator.hasNext()) {
				try {
					String[] gridVal = iterator.next().split(",");
					int lineIndex = Integer.parseInt(gridVal[0]), colIndex = Integer.parseInt(gridVal[1]);
					//上下左右
					int top = lineIndex == 0 ? 0 : connectedGrids.get(lineIndex-1).get(colIndex);
					int bottom = lineIndex == connectedGrids.size() - 1 ? 0 : connectedGrids.get(lineIndex+1).get(colIndex);
					int left =  colIndex == 0 ? 0 : connectedGrids.get(lineIndex).get(colIndex-1);
					int right = colIndex == 5 ? 0 : connectedGrids.get(lineIndex).get(colIndex+1);
					//判断当前格子的上、下、左、右格子是否在通路中
					if (top > 0 || bottom > 0 || left > 0 || right > 0) {
						change = true;
						iterator.remove(); //从非联通空格列表中移除
						connectedGrids.get(lineIndex).set(colIndex, 1); //将当前格子加进通路中
						entity.getLineList().get(lineIndex).set(colIndex, CEObstacleType.CE_EMPTY_VALUE); //将状态为8的格子改成1
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			//没有可以加进通路的空格了
			if (!change) {
				break;
			}
		}
		
		entity.updateUnconnectedEmptyGrids(unconnectedEmptyGrids);
	}
	
	/**
	 * 检测指定行上各个格子的连通性
	 * @param entity
	 * @param lineNum
	 */
	public static void checkConnected(CoreExploreEntity entity, int lineNum) {
		checkConnected(entity, lineNum, new ArrayList<>());
	}
	
	/**
	 * 检测指定行上各个格子的连通性
	 * @param entity
	 * @param lineNum
	 * @param emptyGrids
	 */
	public static void checkConnected(CoreExploreEntity entity, int lineNum, List<String> emptyGrids) {
		List<List<Integer>> connectedGrids = entity.getConnectedGrids();
		List<Integer> columnList = entity.getColumnList(lineNum);
		int lineIndex = entity.getLineIndex(lineNum), colIndex = 0;
		List<Integer> connectedCols = new ArrayList<>();
		try {
			for (colIndex = 0; colIndex < columnList.size(); colIndex++) {
				int columnVal = columnList.get(colIndex);
				if (columnVal == CEObstacleType.CE_SPECIAL_EMPTY_VALUE) {
					connectedCols.add(0);
					emptyGrids.add(lineIndex + "," + colIndex);
					continue;
				}
				if (columnVal != CEObstacleType.CE_EMPTY_VALUE) {
					connectedCols.add(0);
					continue;
				}
				//第一行的空格直接加进通路
				if (lineIndex == 0) {
					connectedCols.add(1);
					continue;
				}
				
				//先判断上边和左边，如果这两个格子在通路中，那当前格子也进入通路
				if (colIndex > 0 && connectedCols.get(colIndex - 1) > 0) { //左边
					connectedCols.add(1);
					continue;
				}
				if (lineIndex > 0 && connectedGrids.get(lineIndex - 1).get(colIndex) > 0) { //上面
					connectedCols.add(1);
					continue;
				}
				
				boolean rightConnected = false;
				//剩下判断右边的格子，是否有在通路里面的
				for (int i = colIndex + 1; i < columnList.size(); i++) {
					if (columnList.get(i) != CEObstacleType.CE_EMPTY_VALUE) {
						break;
					}
					if (lineIndex > 0 && connectedGrids.get(lineIndex - 1).get(i) > 0) { //上面
						rightConnected = true;
						break;
					}
				}
				
				if (rightConnected) {
					connectedCols.add(1);
					continue;
				}
				connectedCols.add(0);
				emptyGrids.add(lineIndex + "," + colIndex);
			}
			
			connectedGrids.add(connectedCols);
			
		} catch (Exception e) {
			HawkLog.errPrintln("CoreExploreActivity checkConnected exception, playerId: {}, lineNum: {}, line: {}, column: {}", entity.getPlayerId(), lineNum, lineIndex, colIndex);
			HawkException.catchException(e);
		}
	}
	
}
