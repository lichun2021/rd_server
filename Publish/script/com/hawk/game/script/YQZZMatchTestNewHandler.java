package com.hawk.game.script;

import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.game.util.LogUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import java.util.*;

public class YQZZMatchTestNewHandler extends HawkScript {
    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
        int termId = Integer.parseInt(params.get("termId"));
        Map<String,String> groupRankMap = new HashMap<>();
        Map<String, YQZZRecordData> recordDataMap = new HashMap<>();
        List<YQZZJoinServer> serverList = new ArrayList<>();
        // 读文件
        List<String> infos = new ArrayList<>();
        try {
            HawkOSOperator.readTextFileLines("tmp/yqzz_match.txt", infos);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        Set<String> tmp = new HashSet<>();
        if(infos.size() > 0){
            for(int i = 1; i < infos.size(); i++){
                String info = infos.get(i);
                String [] arr = info.split(",");
                String serverId = arr[0];
                if(tmp.contains(serverId)){
                    continue;
                }
                tmp.add(serverId);
                YQZZJoinServer joinServer = new YQZZJoinServer();
                joinServer.setServerId(serverId);
                joinServer.setPower(Long.parseLong(arr[1]));
                serverList.add(joinServer);
                groupRankMap.put(serverId, arr[2]);
                YQZZRecordData recordData = new YQZZRecordData();
                recordData.setServerId(serverId);
                recordData.setRank(Integer.parseInt(arr[3]));
                recordData.setSeasonScore(Long.parseLong(arr[4]));
                recordData.setScore(Long.parseLong(arr[5]));
                recordDataMap.put(serverId, recordData);
            }
        }
        YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
        Map<String,YQZZMatchRoomData> roomDataMap = creatSeasonRoom(serverList, YQZZWar.PBYQZZWarType.valueOf(timeCfg.getType()), termId, groupRankMap, recordDataMap);
        StringBuilder builder = new StringBuilder();
        for(YQZZMatchRoomData roomData : roomDataMap.values()){
            builder.append("期数:"+roomData.getTermId()).append(HawkScript.HTTP_NEW_LINE);
            builder.append("房间id:"+roomData.getRoomId()).append(HawkScript.HTTP_NEW_LINE);
            builder.append("战场服:"+roomData.getRoomServerId()).append(HawkScript.HTTP_NEW_LINE);
            builder.append("房间成员:"+roomData.getServers()).append(HawkScript.HTTP_NEW_LINE);
        }
        return builder.toString();
    }


    private Map<String, YQZZMatchRoomData> creatSeasonRoom(List<YQZZJoinServer> serverList, YQZZWar.PBYQZZWarType warType, int termId, Map<String,String> groupRankMap, Map<String, YQZZRecordData> recordDataMap){
        YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
        Map<String,YQZZMatchRoomData> rooms = new HashMap<String,YQZZMatchRoomData>();
        int serverCount = serverList.size();
        int perCount = timeCfg.getMatchNeedCount();
        int poolSize = timeCfg.getMatchListCount();
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
        List<YQZZJoinServer> firstServerList = new ArrayList<>();
        switch (warType){
            case YQZZ_NOT_SEASON:{
                Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
                    @Override
                    public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                        if(o1.getPower() != o2.getPower()){
                            return o1.getPower() > o2.getPower()?-1 :1;
                        }
                        return 0;
                    }
                });
            }
            break;
            case YQZZ_GROUP:{
                Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
                    @Override
                    public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                        if(o1.getPower() != o2.getPower()){
                            return o1.getPower() > o2.getPower()?-1 :1;
                        }
                        return 0;
                    }
                });
                for(int i = 0; i < roomCount; i++ ){
                    if(serverList.isEmpty()){
                        break;
                    }
                    YQZZJoinServer server = serverList.remove(0);
                    firstServerList.add(server);
                }
                Collections.shuffle(serverList);
            }
            break;
            case YQZZ_KICKOUT:{
                if(timeCfg.getTurn() == 1){
                    Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
                        @Override
                        public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                            int rank1 = Integer.parseInt(groupRankMap.getOrDefault(o1.getServerId(), "100"));
                            int rank2 = Integer.parseInt(groupRankMap.getOrDefault(o2.getServerId(), "100"));
                            if(rank1 != rank2){
                                return rank1 < rank2 ? -1 : 1;
                            }
                            return 0;
                        }
                    });
                }else {
                    Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
                        @Override
                        public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                            YQZZRecordData data1= recordDataMap.get(o1.getServerId());
                            YQZZRecordData data2= recordDataMap.get(o2.getServerId());
                            int rank1 = 10;
                            int rank2 = 10;
                            long seasonScore1 = 0;
                            long seasonScore2 = 0;
                            long score1 = 0;
                            long score2 = 0;
                            if(data1 != null){
                                rank1 = data1.getRank();
                                seasonScore1 = data1.getSeasonScore();
                                score1 = data1.getSeasonScore();
                            }
                            if(data2 != null){
                                rank2 = data2.getRank();
                                seasonScore2 = data2.getSeasonScore();
                                score2 = data2.getSeasonScore();
                            }
                            if(rank1 != rank2){
                                return rank1 < rank2 ? -1 : 1;
                            }
                            if(seasonScore1 != seasonScore2){
                                return seasonScore1 > seasonScore2 ? -1 : 1;
                            }
                            if(score1 != score2){
                                return score1 > score2 ? -1 : 1;
                            }
                            return 0;
                        }
                    });
                }
                for(int i = 0; i < roomCount; i++ ){
                    if(serverList.isEmpty()){
                        break;
                    }
                    YQZZJoinServer server = serverList.remove(0);
                    firstServerList.add(server);
                }
                Collections.shuffle(serverList);
            }
            break;
        }
        for(int i = 1;i<=roomCount;i++) {
            int getCount = perCount;
            if (i >= lessIndex) {
                getCount = perCount - 1;
            }
            //没有了 ,结束循环
            if (serverList.size() <= 0) {
                break;
            }
            //开始拿去
            List<YQZZJoinServer> roomServers = new ArrayList<>();

            switch (warType) {
                case YQZZ_NOT_SEASON: {
                    roomServers = this.getMatchRoomServers(serverList, poolSize, getCount);
                }
                break;
                case YQZZ_GROUP:{
                    roomServers = this.getMatchRoomServers(firstServerList, serverList, getCount);
                }
                break;
                case YQZZ_KICKOUT:{
                    roomServers = this.getMatchRoomServers(firstServerList, serverList, getCount);
                }
                break;
            }
            if (roomServers.isEmpty()) {
                continue;
            }
            //排序
            Collections.sort(roomServers, new Comparator<YQZZJoinServer>() {
                @Override
                public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                    if (o1.getPower() != o2.getPower()) {
                        return o1.getPower() > o2.getPower() ? -1 : 1;
                    }
                    return 0;
                }
            });
            String roomServerId = this.getRoomServer(roomServers);
            YQZZMatchRoomData room = new YQZZMatchRoomData();
            room.setTermId(termId);
            room.setRoomId(HawkUUIDGenerator.genUUID());
            room.setRoomServerId(roomServerId);
            Set<String> joinServerSet = new HashSet<>();
            for (YQZZJoinServer roomServer : roomServers) {
                room.addServer(roomServer.getServerId());
                joinServerSet.add(roomServer.getServerId());
            }
            if(warType == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT){
                room.setAdvance(true);
            }
            rooms.put(room.getRoomId(), room);
            try {
                String joinServerStr = SerializeHelper.collectionToString(joinServerSet, SerializeHelper.ATTRIBUTE_SPLIT);
                for (YQZZJoinServer roomServer : roomServers) {
                    LogUtil.logYQZZMatch(termId, roomServer.getServerId(), roomServer.getPower(), joinServerStr, roomServerId);
                }
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        return rooms;
    }

    private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> serverList,int poolSize,int needSize){
        List<YQZZJoinServer> rlt = new ArrayList<>();
        if(serverList.isEmpty()){
            return rlt;
        }
        YQZZJoinServer first = serverList.remove(0);
        rlt.add(first);
        poolSize = poolSize - 1;
        needSize = needSize - 1;
        if(poolSize >= serverList.size()){
            poolSize = serverList.size();
        }
        List<YQZZJoinServer> tmp = new ArrayList<>();
        for(int i = 0; i < poolSize; i++){
            YQZZJoinServer server = serverList.remove(0);
            tmp.add(server);
        }
        Collections.shuffle(tmp);
        for(int i=0; i<needSize; i++){
            if(tmp.size() <= 0){
                continue;
            }
            YQZZJoinServer server = tmp.remove(0);
            rlt.add(server);
        }
        Collections.sort(tmp, new Comparator<YQZZJoinServer>() {
            @Override
            public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                if(o1.getPower() != o2.getPower()){
                    return o1.getPower() < o2.getPower()?-1 :1;
                }
                return 0;
            }
        });
        while (tmp.size() > 0){
            YQZZJoinServer server = tmp.remove(0);
            serverList.add(0, server);
        }
        return rlt;
    }

    private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> serverList, int needSize){
        List<YQZZJoinServer> rlt = new ArrayList<>();
        for(int i=0;i<needSize;i++){
            if(serverList.isEmpty()){
                break;
            }
            YQZZJoinServer server = serverList.remove(0);
            rlt.add(server);
        }
        return rlt;
    }

    private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> firstServerList, List<YQZZJoinServer> serverList, int needSize){
        List<YQZZJoinServer> rlt = new ArrayList<>();
        YQZZJoinServer firstSeaver = firstServerList.remove(0);
        rlt.add(firstSeaver);
        for(int i=0;i<needSize-1;i++){
            if(serverList.isEmpty()){
                break;
            }
            YQZZJoinServer server = serverList.remove(0);
            rlt.add(server);
        }
        return rlt;
    }

    private String getRoomServer(List<YQZZJoinServer> joinList) {
        try {
            int serverChoose = 0;
            for(YQZZJoinServer join : joinList){
                String sId = join.getServerId();
                int serverId = Integer.parseInt(sId);
                if(serverChoose == 0){
                    serverChoose = serverId;
                }
                if(serverId > serverChoose){
                    serverChoose = serverId;
                }
                if(GsConfig.getInstance().getGoodServerList().contains(serverId)){
                    serverChoose = serverId;
                    break;
                }
            }
            return String.valueOf(serverChoose);
        } catch (Exception e) {
            HawkException.catchException(e);
            return joinList.get(0).getServerId();
        }
    }
}
