package com.hawk.game.crossactivity.resourcespree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple4;

import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 资源狂欢宝箱刷出
 * @author chechangda
 *
 */
public class ResourceSpreeBoxRefersh extends WorldDelayTask{
	//需要刷新的宝箱 ID-数量
	private Map<Integer,Integer> boxMap = new HashMap<>();
	//每批刷多少个宝箱
	private int perNum;
	//没批间隔时间
	private long perDelayTime;
	//跨服期数
	private int termId;
	//可以领取宝箱的服务器ID
	private List<String> winner;
	
	public ResourceSpreeBoxRefersh(int termId,List<String> winner, long delayTime, Map<Integer,Integer> boxMap, int perNum, long perDelayTime) {
		super(GsConst.WorldTaskType.RESOURCE_SPREE_BOX_GEN, delayTime, delayTime, 1);
		this.boxMap.putAll(boxMap);
		this.perNum = perNum;
		this.perDelayTime = perDelayTime;
		this.winner = winner;
		this.termId = termId;
	}


	@Override
	public boolean onInvoke() {
		HawkTuple4<Integer, Integer, Integer, Integer> genScope = CrossConstCfg.getInstance().getResBoxGenScope();
		List<Point> freeList = this.getFreePoints(genScope.first, genScope.second, genScope.third, genScope.fourth);
		HawkLog.logPrintln("CrossActivityService genResourceSpreeBox point free size:{}", freeList.size());
		HawkRand.randomOrder(freeList);
		Map<Integer,Integer> refreshMap = getRefreshMap();
		for(Entry<Integer, Integer> entry : refreshMap.entrySet()){
			int cfgId = entry.getKey();
			int num = entry.getValue();
			for(int r = 0; r < num; r++){
				if(freeList.size() <= 0){
					return false;
				}
				//从最后拿
				int index = freeList.size() -1;
				Point point = freeList.remove(index);
				if(!point.canRMSeat()){
					continue;
				}
				ResourceSpreeBoxWorldPoint boxPoint = new ResourceSpreeBoxWorldPoint(
						point.getX(), point.getY(), point.getAreaId(), point.getZoneId(),WorldPointType.RESOURCE_SPREE_BOX_VALUE);
				boxPoint.setResourceSpreeBoxId(cfgId);
				boxPoint.setCrossTermId(termId);
				boxPoint.addWinner(winner);
				WorldPointService.getInstance().addPoint(boxPoint);
				HawkLog.logPrintln("CrossActivityService genResourceSpreeBox boxId:{},pointx:{},pointy:{}", cfgId, point.getX(),point.getY());
			}
		}
		
		//如果没刷新完,继续刷新
		if(this.boxMap.size() > 0){
			ResourceSpreeBoxRefersh refersh = new ResourceSpreeBoxRefersh(termId,winner,
					perDelayTime, boxMap, perNum, perDelayTime);
			WorldThreadScheduler.getInstance().postDelayWorldTask(refersh);
		}
		return true;
	}
	
	
	/**
	 * 找到这一批刷新多少个宝箱
	 * @return
	 */
	private Map<Integer,Integer> getRefreshMap(){
		Map<Integer,Integer> refres = new HashMap<>();
		int refreshNum = 0;
		for(Entry<Integer, Integer> entry : this.boxMap.entrySet()){
			int cfg = entry.getKey();
			int num = entry.getValue();
			int need = this.perNum - refreshNum;
			if(need <= 0){
				break;
			}
			if(num <= need){
				refreshNum += num;
				refres.put(cfg, num);
			}else{
				refreshNum += need;
				refres.put(cfg, need);
			}
		}
		//删除
		for(Entry<Integer, Integer> entry : refres.entrySet()){
			int cfg = entry.getKey();
			int num = entry.getValue();
			int val = this.boxMap.get(cfg);
			int last = val - num;
			if(last <= 0){
				this.boxMap.remove(cfg);
			}else{
				this.boxMap.put(cfg, last);
			}
		}
		return refres;
	}
	
	
	
	/**
	 * 找到可以生成宝箱得点
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return
	 */
	private List<Point> getFreePoints(int startX,int startY,int endX,int endY){
		ArrayList<Point> aroundPoints = new ArrayList<>();
		for(int x= startX;x<= endX;x++){
			for(int y =startY;y<=endY;y++ ){
				Point point =WorldPointService.getInstance().getAreaPoint(x, y, true);
				if (point == null) {
					continue;
				}
				if(!point.canRMSeat()){
					continue;
				}
				if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
					continue;
				}
				aroundPoints.add(point);
			}
		}
		return aroundPoints;
	}
	

	
	
}
