package com.hawk.game.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZFoggyFortress;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZResource;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.data.YQZZDataManager;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinGuild;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarHistroyResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarRecord;

/**
 * 打印玩家作用号属性 localhost:8080/script/yqmatch?serverCnt=50&poolSize=20&needSize=3
 * 玩家名字
 * 
 * @author Jesse
 *
 */
public class YQZZMatchTestHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			if(!HawkOSOperator.isEmptyString(params.get("opt"))){
				int opt =Integer.parseInt(params.get("opt"));
				if(opt == 1){
					StringBuilder sbuilder = new StringBuilder();
					this.getMatchInfo(sbuilder);
					return sbuilder.toString();
				}else if(opt == 2){
					String rlt = this.printXXX();
					return HawkScript.successResponse(rlt);
				}else if(opt == 3){
					String rlt = this.addDeclare();
					return HawkScript.successResponse(rlt);
				}
				return "no opt";
			}
			
			int serverCnt =Integer.parseInt(params.get("serverCnt"));
			int poolSize = Integer.parseInt(params.get("poolSize"));
			int needSize =Integer.parseInt(params.get("needSize"));
			StringBuilder sbuilder = new StringBuilder();
			Map<String,YQZZJoinServer>  serverMap = genServer(10000, serverCnt);
			this.doMatch(10000, serverMap, poolSize, needSize,sbuilder);
			
			String output = System.getProperty("user.dir") + "/logs/YQZZMatch.txt";
			File file = new File(output);
			if (file.exists()) {
				file.delete();
			}
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(output, true);
				fileWriter.write(sbuilder.toString());
			} catch (IOException e) {
				HawkException.catchException(e);
			} finally {
				if (fileWriter != null) {
					try {
						fileWriter.flush();
						fileWriter.close();
					} catch (IOException e1) {
						HawkException.catchException(e1);
					}
				}
			}
			return successResponse("succese");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	private String addDeclare(){
		YQZZBattleRoom room = YQZZRoomManager.getInstance().findAllRoom().get(0);
		for(YQZZGuildBaseInfo baseInfo :room.getBattleCamps()){
			baseInfo.declareWarPoint = room.getCfg().getDeclareWarOrderMax();
		}
		return "sucess";
	}
	
	
	private String printXXX() {
		String result;
		YQZZBattleRoom room = YQZZRoomManager.getInstance().findAllRoom().get(0);
		Collection<IYQZZWorldPoint> viewList = room.getViewPoints();
		StringBuilder builder = new StringBuilder();
		builder.append("Monster" + HawkScript.HTTP_NEW_LINE);
		for (IYQZZWorldPoint point : viewList) {
			if (point instanceof YQZZMonster) {
				builder.append("mcfgId:"+((YQZZMonster) point).getCfgId() + ",    " + point.getX() + "," + point.getY()).append(HawkScript.HTTP_NEW_LINE);
			}
		}
		builder.append("Foggy" + HawkScript.HTTP_NEW_LINE);
		for (IYQZZWorldPoint point : viewList) {
			if (point instanceof YQZZFoggyFortress) {
				builder.append("fcfgId:"+((YQZZFoggyFortress) point).getCfgId() + ",    " + point.getX() + "," + point.getY()).append(HawkScript.HTTP_NEW_LINE);
			}
		}
		builder.append("Res" + HawkScript.HTTP_NEW_LINE);
		for (IYQZZWorldPoint point : viewList) {
			if (point instanceof YQZZResource) {
				builder.append("rcfgId:"+((YQZZResource) point).getCfgId() + ",    " + point.getX() + "," + point.getY()).append(HawkScript.HTTP_NEW_LINE);
			}
		}
		result = builder.toString();
		return result;
	}
	
	private void getMatchInfo(StringBuilder builder){
		YQZZDataManager dataManager = YQZZMatchService.getInstance().getDataManger();
		YQZZActivityStateData stateData = dataManager.getStateData();
		String serverId = GsConfig.getInstance().getServerId();
		builder.append("服务器:"+serverId).append("\r\n</br>");
		builder.append("期数:"+stateData.getTermId()).append("\r\n</br>");
		builder.append("本服是否参与(0阶段错误 1 参与  2未参与):"+stateData.getJoinGame().getValue()).append("\r\n</br>");
		builder.append("本服信息写入(1 写入  0未写入):"+stateData.getSaveServerInfo()).append("\r\n</br>");
		builder.append("本服匹配信息加载(1 已经加载  0未加载):"+(dataManager.getRoomData()==null?0:1)).append("\r\n</br>");
		YQZZMatchRoomData roomData = dataManager.getRoomData();
		if(roomData != null){
			builder.append("战场房间信息:"+roomData.getRoomId()+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>").append("\r\n</br>");
			builder.append("战场服ID:"+roomData.getRoomServerId()).append("\r\n</br>");
			builder.append("***********************************************").append("\r\n</br>");
			builder.append("参与服务器:"+roomData.getServers().toString()).append("\r\n</br>");
			Map<String, YQZZJoinServer> servers = dataManager.getRoomServerDataMap();
			for(String joinServer : roomData.getServers()){
				YQZZJoinServer server = servers.get(joinServer);
				builder.append("参与服务器信息:"+server.getServerId()+",匹配战力:"+server.getPower()).append("\r\n</br>");
			}
			builder.append("***********************************************").append("\r\n</br>");
			Map<String, YQZZJoinGuild>  guilds = dataManager.getRoomGuilds();
			for(YQZZJoinGuild guild : guilds.values()){
				builder.append("参与联盟:"+guild.getServerId());
				builder.append("\t");
				builder.append(guild.getGuildId());
				builder.append("\t");
				builder.append(guild.getGuildName());
				builder.append("\t");
				builder.append(guild.getGuildRank());
				builder.append("\r\n</br>");
			}
		}
		addYQZZHistoryInfo(builder);
		
	}
	
	public void addYQZZHistoryInfo(StringBuilder Stringbuilder){
		Stringbuilder.append("********************历史战绩***************************").append("\r\n</br>");
		int maxRank = YQZZMatchService.getInstance().getDataManger().getStatisticsData().getMaxRank();
		String serverId = GsConfig.getInstance().getServerId();
		List<Integer> showTerms = this.getShowHistoryTerm();
		Map<Integer,YQZZRecordData> records = YQZZRecordData.loadAll(serverId, showTerms);
		Stringbuilder.append("最佳名次:"+maxRank).append("\r\n</br>");
		for(YQZZRecordData record:records.values()){
			int termId = record.getTermId();
			YQZZTimeCfg cfg = HawkConfigManager.getInstance()
					.getConfigByKey(YQZZTimeCfg.class, termId);
			if(cfg == null){
				continue;
			}
			Stringbuilder.append(">>>期数<<<:"+termId).append("\r\n</br>");
			Stringbuilder.append("战斗时间:"+HawkTime.formatTime(cfg.getBattleTimeValue())).append("\r\n</br>");
			Stringbuilder.append("名次:"+record.getRank()).append("\r\n</br>");
			Stringbuilder.append("国家积分:"+record.getRank()).append("\r\n</br>");
		}
	}
	
	private List<Integer> getShowHistoryTerm(){
		int curTerm = 0;
		ConfigIterator<YQZZTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (YQZZTimeCfg timeCfg : its) {
			if (now < timeCfg.getShowTimeValue()) {
				continue;
			}
			if(timeCfg.getTermId() > curTerm){
				curTerm = timeCfg.getTermId();
			}
		}
		List<Integer> showTerm = new ArrayList<>();
		for(int i=curTerm;i>0;i--){
			showTerm.add(i);
			if(showTerm.size() > 10){
				break;
			}
		}
		return showTerm;
	}
	
	private Map<String, YQZZJoinServer> genServer(int termId,int count){
		Map<String, YQZZJoinServer> rlt = new HashMap<>();
		for(int i=1;i<=count;i++){
			YQZZJoinServer server = new YQZZJoinServer();
			server.setServerId("server_"+i);
			server.setTermId(termId);
			server.setPower(HawkRand.randInt(2000000, 200000000));
			rlt.put(server.getServerId(), server);
		}
		return rlt;
	}
	//匹配
	private void doMatch(int termId,Map<String, YQZZJoinServer> serverMap,int pool,int need,StringBuilder sbuilder){
		List<YQZZJoinServer> serverList = new ArrayList<>();
		serverList.addAll(serverMap.values());
		//排序
		Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
			@Override
			public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
				if(o1.getPower() != o2.getPower()){
					return o1.getPower() > o2.getPower()?-1 :1;
				}
				return 0;
			}
		});
		sbuilder.append("服务器ID,服务器战力,战力排名\r\n");
		for(int i=1;i<=serverList.size();i++ ){
			YQZZJoinServer server = serverList.get(i-1);
			sbuilder.append(server.getServerId())
			.append("\t")
			.append(server.getPower())
			.append("\t")
			.append(i)
			.append("\r\n");
		}
		sbuilder.append("================================================================\r\n");
		Map<String,YQZZMatchRoomData> rooms = new HashMap<String,YQZZMatchRoomData>();
		int serverCount = serverList.size();
		int perCount = need;
		int poolSize = pool;
		int p1 = serverCount / perCount;
		int p2 = serverCount % perCount;
		int roomCount = p1;
		int lessIndex = serverCount + 1;
		if(p2 > 0){
			//最后一个房间元素不够，最后一个房间的元素个数是  perCount -1
			roomCount += 1;
			int p3 = perCount -1;
			//共有lessCount个房间少一个元素
			int lessCount = p3 - p2 + 1;
			//从lessIndex 开始每个房间少一个元素
			lessIndex = roomCount - lessCount + 1;
		}
		sbuilder.append("房间ID,服务器列表,战场服务器ID\r\n");
		for(int i = 1;i<=roomCount;i++){
			int getCount = perCount;
			if(i >= lessIndex){
				getCount = perCount -1;
			}
			//没有了 ,结束循环
			if(serverList.size() <= 0){
				break;
			}
			//开始拿去
			List<YQZZJoinServer> roomServers = this.getMatchRoomServers(serverList, poolSize, getCount);
			if(roomServers.isEmpty()){
				continue;
			}
			String roomServerId = roomServers.get(0).getServerId();
			YQZZMatchRoomData room = new YQZZMatchRoomData();
			room.setTermId(termId);
			room.setRoomId(HawkUUIDGenerator.genUUID());
			room.setRoomServerId(roomServerId);
			for(YQZZJoinServer roomServer : roomServers){
				room.addServer(roomServer.getServerId());
			}
			rooms.put(room.getRoomId(), room);
			
			sbuilder.append(room.getRoomId())
			.append("\t")
			.append(room.getServers().toString())
			.append("\t").append(room.getRoomServerId()).append("\r\n");
		}
		
	}
		
		
	private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> serverList,int poolSize,int needSize){
		List<YQZZJoinServer> rlt = new ArrayList<>();
		if(poolSize >= serverList.size()){
			poolSize = serverList.size();
		}
		//取X次
		for(int i=0;i<needSize;i++){
			if(poolSize <= 0){
				break;
			}
			//随机一个
			int index = HawkRand.randInt(0, poolSize -1);
			poolSize --;
			if(index <= serverList.size() -1){
				YQZZJoinServer server = serverList.remove(index);
				rlt.add(server);
			}
			
		}
		return rlt;
	}
}
