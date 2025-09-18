package com.hawk.activity.type.impl.questTreasure.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureBoxCfg;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureKVCfg;
import com.hawk.game.protocol.Activity.QuestTreasureGameBox;
import com.hawk.game.protocol.Activity.QuestTreasureGameData;
import com.hawk.game.protocol.Activity.QuestTreasureGamePos;
import com.hawk.game.protocol.Status;

public class QuestTreasureGame {
	
	public static final int PosParam = 1000;
	
	//当前位置
	private int rolePos;
	
	//初始化位置
	private int homePos;
	
	//选择路线
	private List<Integer> chooseList = new ArrayList<>();
	
	//宝箱
	private Map<Integer,Integer> boxMap = new HashMap<>();
	
	//领取过的宝箱
	private Map<Integer,Integer> boxAchieveMap = new HashMap<>();
	
	//掷骰子的结果
	private List<Integer> randomList = new ArrayList<>();
	
	
	public void init(){
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		HawkTuple2<Integer, Integer> homePos = cfg.getHomePos();
		int initPos = calPosVal(homePos.first, homePos.second);
		//设置玩家起点
		this.setRolePos(initPos);
		//记录 远点
		this.setHomePos(initPos);
		//随机箱子
		this.genBoxPos();
	}
	
	
	public void genBoxPos(){
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		HawkTuple2<Integer, Integer> range = cfg.getRange();
		List<Integer> plist = new ArrayList<>(); 
		for(int x = 0;x<range.first;x++){
			for(int y=0;y<range.second;y++){
				plist.add(calPosVal(x, y));
			}
		}
		//移出起点
		plist.remove(Integer.valueOf(this.rolePos));
		//宝箱列表
		Map<Integer,Integer> boxMap = new HashMap<>();
		if(true){
			//高级宝箱
			QuestTreasureBoxCfg boxcfg1 =  HawkConfigManager.getInstance().getConfigByKey(QuestTreasureBoxCfg.class, 1);
			HawkTuple2<Integer, Integer>  boxCount1 = boxcfg1.getBoxCount();
			List<HawkTuple2<Integer, Integer>> boxPosList1 = boxcfg1.getCoordinateList();
			//如果配置的高级宝箱最大数量小于配置的高级宝箱生成的坐标数量，则在配置的坐标中随机选取对应数量的坐标生成高级宝箱
			if(boxCount1.second < boxPosList1.size()){
				HawkRand.randomOrder(boxPosList1);
				int count = HawkRand.randInt(boxCount1.first, boxCount1.second);
				for(int b =0;b<count;b++){
					if(boxPosList1.size() <= 0){
						continue;
					}
					int index = HawkRand.randInt(0, boxPosList1.size()-1);
					HawkTuple2<Integer, Integer> tuple = boxPosList1.remove(index);
					int bpos = calPosVal(tuple.first, tuple.second);
					if(bpos == this.rolePos){
						continue;
					}
					boxMap.put(bpos, 1);
					plist.remove(Integer.valueOf(bpos));
				}
			}else{
			   //如果配置的高级宝箱最大数量大于配置的高级宝箱生成的坐标数量，则按照配置的坐标生成宝箱，此时生成宝箱的数量为配置的坐标数量（生成在配置的位置）
				for(HawkTuple2<Integer, Integer> tuple : boxPosList1){
					int bpos = calPosVal(tuple.first, tuple.second);
					if(bpos == this.rolePos){
						continue;
					}
					boxMap.put(bpos, 1);
					plist.remove(Integer.valueOf(bpos));
				}
			}
		}
		
		if(true){
			//中级宝箱
			QuestTreasureBoxCfg boxcfg2 =  HawkConfigManager.getInstance().getConfigByKey(QuestTreasureBoxCfg.class, 2);
			HawkTuple2<Integer, Integer>  boxCount2 = boxcfg2.getBoxCount();
			int count2 = HawkRand.randInt(boxCount2.first, boxCount2.second);
			for(int b=0;b<count2;b++){
				if(plist.size() <= 0){
					continue;
				}
				int index = HawkRand.randInt(0, plist.size()-1);
				int bpos = plist.remove(index);
				boxMap.put(bpos, 2);
				plist.remove(Integer.valueOf(bpos));
			}
		}
		
		if(true){
			//低级宝箱
			QuestTreasureBoxCfg boxcfg3 =  HawkConfigManager.getInstance().getConfigByKey(QuestTreasureBoxCfg.class, 3);
			HawkTuple2<Integer, Integer>  boxCount3 = boxcfg3.getBoxCount();
			int count3 = HawkRand.randInt(boxCount3.first, boxCount3.second);
			for(int b=0;b<count3;b++){
				if(plist.size() <= 0){
					continue;
				}
				int index = HawkRand.randInt(0, plist.size()-1);
				int bpos = plist.remove(index);
				boxMap.put(bpos, 3);
				plist.remove(Integer.valueOf(bpos));
			}
		}
		this.boxMap = boxMap;
	}
	
	
	
	
	
	
	public int addChoosePoints(List<QuestTreasureGamePos> plist){
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		HawkTuple2<Integer, Integer>  range = cfg.getRange();
		List<Integer> clist = new ArrayList<>();
		HawkTuple2<Integer, Integer> rp = getGamePos(this.rolePos);
		int frontX = rp.first;
		int frontY = rp.second;
		for(QuestTreasureGamePos pos : plist){
			int x = pos.getX();
			int y = pos.getY();
			if(x >= range.first || x < 0){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			if(y >= range.second || y < 0){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			int val = calPosVal(x, y);
			if(val == this.rolePos){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			if(clist.contains(val)){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			if(x-1 != frontX && x+1 != frontX && x != frontX ){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			if(y-1 != frontY && y+1 != frontY && y != frontY){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			clist.add(val);
			frontX = x;
			frontY = y;
		}
		if(clist.size() != cfg.getChooseCount()){
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		this.chooseList = clist;
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	
	
	public HawkTuple3<List<Integer>, Map<Integer,Integer>,Integer> randomWalk(){
		boolean checkRlt = this.checkRandom();
		if(!checkRlt){
			return null;
		}
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		int walkCount = HawkRand.randomWeightObject(cfg.getRandomWeightMap());
		List<Integer> wpoints = new ArrayList<>();
		Map<Integer,Integer> box = new HashMap<>();
		boolean rlt = this.walk(walkCount,wpoints,box);
		if(!rlt){
			return null;
		}
		return HawkTuples.tuple(wpoints, box,walkCount);
	}

	
	
	
	
	public boolean checkRandom(){
		if(this.chooseList.size() <= 0){
			return false;
		}
		if(this.checkGameOver()){
			return false;
		}
		return true;
	}
	
	
	
	

	

	public boolean walk(int count,List<Integer> wpoints,Map<Integer,Integer> box){
		int walkIndex = 0;
		int maxIndex = this.chooseList.size() -1;
		for(int i=0;i<= maxIndex;i++){
			if(this.rolePos == this.chooseList.get(i)){
				walkIndex = i+1;
			}
		}
		if(walkIndex > maxIndex){
			return false;
		}
		//往前走
		for(int i=0;i<count;i++){
			int curIndex = walkIndex + i;
			if(curIndex >  maxIndex){
				continue;
			}
			int curPos = this.chooseList.get(curIndex);
			wpoints.add(curPos);
			//赋值当前位置
			this.rolePos = curPos;
			//是否有宝箱
			int boxId = this.boxMap.getOrDefault(curPos, 0);
			if(boxId >= 0){
				boxAchieveMap.put(curPos, boxId);
				//给奖励
				box.put(curPos, boxId);
			}
		}
		this.randomList.add(count);
		return true;
	}
	
	/**
	 * 是否结束
	 * @return
	 */
	public boolean checkGameOver(){
		if(this.chooseList.size() <= 0){
			return false;
		}
		int len = this.chooseList.size();
		int endPoint = this.chooseList.get(len -1);
		if(this.rolePos != endPoint){
			return false;
		}
		return true;
	}
	
	
	
	
	
	public void setRolePos(int rolePos) {
		this.rolePos = rolePos;
	}
	
	public void setHomePos(int homePos) {
		this.homePos = homePos;
	}
	
	
	public QuestTreasureGameData.Builder genBuilder(){
		QuestTreasureGameData.Builder builder = QuestTreasureGameData.newBuilder();
		//角色位置
		QuestTreasureGamePos.Builder rb= genQuestTreasureGamePos(this.rolePos);
		builder.setRolePos(rb);
		//路线点
		if(this.chooseList.size() > 0){
			for(int point :this.chooseList){
				QuestTreasureGamePos.Builder cp= genQuestTreasureGamePos(point);
				builder.addChoosePos(cp);
			}
		}
		//宝箱
		if(this.boxMap.size() > 0){
			for(Map.Entry<Integer, Integer> boxEntry :this.boxMap.entrySet()){
				int bpos = boxEntry.getKey();
				int bId = boxEntry.getValue();
				QuestTreasureGameBox.Builder bb= QuestTreasureGameBox.newBuilder();
				QuestTreasureGamePos.Builder cp= genQuestTreasureGamePos(bpos);
				bb.setBoxId(bId);
				bb.setPos(cp);
				builder.addBoxs(bb);
			}
		}
		//上一次随机数
		builder.setRandomNum(0);
		int randomSize = this.randomList.size();
		if(randomSize > 0){
			int rnum = this.randomList.get(randomSize - 1);
			builder.setRandomNum(rnum);
		}
		return builder;
	}
	
	
	

	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("rolePos", this.rolePos);
		obj.put("homePos", this.homePos);
		if(chooseList.size() > 0){
			JSONArray arr = new JSONArray();
			for(int i : chooseList){
				arr.add(i);
			}
			obj.put("chooseList", arr.toJSONString());
		}
		
		if(boxMap.size() > 0){
			JSONArray arr = new JSONArray();
			for(Map.Entry<Integer,Integer> entry : boxMap.entrySet()){
				String str = entry.getKey()+"_"+entry.getValue();
				arr.add(str);
			}
			obj.put("boxMap", arr.toJSONString());
		}
		

		if(boxAchieveMap.size() > 0){
			JSONArray arr = new JSONArray();
			for(Map.Entry<Integer,Integer> entry : boxAchieveMap.entrySet()){
				String str = entry.getKey()+"_"+entry.getValue();
				arr.add(str);
			}
			obj.put("boxAchieveMap", arr.toJSONString());
		}
		
		
		if(randomList.size() > 0){
			JSONArray arr = new JSONArray();
			for(int i : randomList){
				arr.add(i);
			}
			obj.put("randomList", arr.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.rolePos = obj.getIntValue("rolePos");
		this.homePos = obj.getIntValue("homePos");
		List<Integer> chooseListTemp = new ArrayList<>();
		if(obj.containsKey("chooseList")){
			String str = obj.getString("chooseList");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				int val = jarr.getInteger(i);
				chooseListTemp.add(val);
			}
		}
		this.chooseList = chooseListTemp;
		
		Map<Integer,Integer> boxMapTemp = new HashMap<>();
		if(obj.containsKey("boxMap")){
			String str = obj.getString("boxMap");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				String arrStr = jarr.getString(i);
				String[] params = arrStr.split("_");
				boxMapTemp.put(Integer.parseInt(params[0]), Integer.parseInt(params[1]));
			}
		}
		this.boxMap = boxMapTemp;
		
		Map<Integer,Integer> boxAchieveMapTemp = new HashMap<>();
		if(obj.containsKey("boxAchieveMap")){
			String str = obj.getString("boxAchieveMap");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				String arrStr = jarr.getString(i);
				String[] params = arrStr.split("_");
				boxAchieveMapTemp.put(Integer.parseInt(params[0]), Integer.parseInt(params[1]));
			}
		}
		this.boxAchieveMap = boxAchieveMapTemp;
		
		List<Integer> randomListTemp = new ArrayList<>();
		if(obj.containsKey("randomList")){
			String str = obj.getString("randomList");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				int val = jarr.getInteger(i);
				randomListTemp.add(val);
			}
		}
		this.randomList = randomListTemp;
	}
	
	
	public int getRolePos() {
		return rolePos;
	}
	
	public List<Integer> getChooseList() {
		return chooseList;
	}
	
	public Map<Integer, Integer> getBoxMap() {
		return boxMap;
	}
	
	public static HawkTuple2<Integer, Integer> getGamePos(int param){
		int x = param / PosParam;
		int y = param % PosParam;
		return HawkTuples.tuple(x, y);
	}
	
	public static int calPosVal(int x,int y){
		return x * PosParam + y;
	}
	
	public static QuestTreasureGamePos.Builder genQuestTreasureGamePos(int posVal){
		HawkTuple2<Integer, Integer> tuple = getGamePos(posVal);
		QuestTreasureGamePos.Builder builder = QuestTreasureGamePos.newBuilder();
		builder.setX(tuple.first);
		builder.setY(tuple.second);
		return builder;
	}
}
