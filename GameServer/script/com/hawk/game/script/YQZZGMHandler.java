package com.hawk.game.script;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZSeasonServer;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZSeasonStateData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import java.util.*;

public class YQZZGMHandler extends HawkScript {
    private String outputInfo = "";
    @Override
    public String action(Map<String, String> map, HawkScriptHttpInfo hawkScriptHttpInfo) {
        if (!GsConfig.getInstance().isDebug()) {
            return "不是测试环境";
        }
//        String host = hawkScriptHttpInfo.getRequest().getRemoteHost();
//        int port = GsConfig.getInstance().getGmPort();
//        String URL = "http://"+host + ":"+port+"/script/yqzzgm";
        try {
            String opt = map.getOrDefault("opt", "");
            switch (opt){
                case "start":{
                    String termId = map.get("termId");
                    activityStart(Integer.parseInt(termId));
                }
                break;
                case "next":{
                    activityNext();
                }
                break;
                case "clear":{
                    YQZZTimeCfg.setGmTime(0);
                }
                break;
                case "clean":{
                    String termId = map.get("termId");
                    RedisProxy.getInstance().getRedisSession().del("YQZZ_ACTIVITY_ROOM_DATA"+":"+termId);
                    RedisProxy.getInstance().getRedisSession().del("YQZZ_ACTIVITY_JOIN_SERVER"+":"+termId);
                    RedisProxy.getInstance().getRedisSession().del("YQZZ_ACTIVITY_JOIN_SERVER"+":"+termId);
                    RedisProxy.getInstance().getRedisSession().del("YQZZ_ACTIVITY_MATCH_LOCK"+":"+termId);
                }
                break;
                case "sendMail":{
                    int mailId = Integer.parseInt(map.get("mailId"));
                    String params = map.get("param");
                    MailParames.Builder mailParames = MailParames.newBuilder();
                    mailParames.setMailId(MailConst.MailId.valueOf(mailId));
                    if(params != null){
                        for(String param : params.split(",")){
                            mailParames.addContents(param);
                        }
                    }
                    long currTime = HawkTime.getMillisecond();
                    long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
                    SystemMailService.getInstance().addGlobalMail(mailParames.build(), currTime, currTime + experiTime);
                }
                break;
                case "kickout":{
                    YQZZSeasonServer server = YQZZSeasonServer.loadByServerId(Integer.parseInt(map.get("season")), map.get("serverId"));
                    server.setAdvance(false);
                    server.saveRedis();
                }
                break;
                case "testMatch":{
                    return testMatch(Integer.parseInt(map.get("count")),Integer.parseInt(map.get("perCount")),Integer.parseInt(map.get("poolSize")));
                }
                default:{

                }
            }
            return pageInfo();
        }catch (Exception e){
            outputLog(e.getMessage());
            return pageInfo();
        }finally {
            outputInfo = "";
        }

    }

    public void activityStart(int termId){
        YQZZConst.YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
        if(state != YQZZConst.YQZZActivityState.HIDDEN){
            outputLog("当前正在活动期间");
            return;
        }

        YQZZTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
        if(cfg == null) {
            outputLog("设置期数不存在，termId:"+termId);
        }
        long now = HawkTime.getMillisecond();
        long showTime = HawkTime.parseTime(cfg.getShowTime());
        long gmTime = now - showTime;
        YQZZTimeCfg.setGmTime(gmTime);
        outputLog("执行成功");
    }
    public void activityNext(){
        YQZZConst.YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
        if(state == YQZZConst.YQZZActivityState.HIDDEN){
            int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId() + 1;
            YQZZTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
            if(cfg == null) {
                outputLog("设置期数不存在，termId:"+termId);
            }
            long now = HawkTime.getMillisecond();
            long showTime = HawkTime.parseTime(cfg.getShowTime());
            long gmTime = now - showTime;
            YQZZTimeCfg.setGmTime(gmTime);
            outputLog("执行成功");
            return;
        }
        int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
        YQZZTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
        if(cfg == null) {
            outputLog("配置不存在，termId:"+termId);
            return;
        }
        YQZZMatchService.getInstance().getState().gmOp();
        long now = HawkTime.getMillisecond();
        long toTime = 0;
        switch (state){
            case START_SHOW:{
                toTime = HawkTime.parseTime(cfg.getMatchTime());
            }
            break;
            case MATCH:{
                toTime = HawkTime.parseTime(cfg.getBattleTime());
            }
            break;
            case BATTLE:{
                toTime = HawkTime.parseTime(cfg.getRewardTime());
            }
            break;
            case REWARD:{
                toTime = HawkTime.parseTime(cfg.getEndShowTime());
            }
            break;
            case END_SHOW:{
                toTime = HawkTime.parseTime(cfg.getHiddenTime());
            }
            break;
        }
        toTime -= 10000;
        long gmTime = now - toTime;
        YQZZTimeCfg.setGmTime(gmTime);
    }



    public String activityInfo(){
        int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
        YQZZConst.YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
        YQZZConst.YQZZActivityJoinState joinState = YQZZMatchService.getInstance().getDataManger().getStateData().getJoinGame();
        YQZZSeasonStateData seasonStateData = YQZZMatchService.getInstance().getDataManger().getSeasonStateData();
        return "<p>服务器id:"+GsConfig.getInstance().getServerId()+"</p>" +
                "<p>活动状态:"+state.name()+"</p>" +
                "<p>活动期数:"+termId+"</p>" +
                "<p>参与状态:"+joinState.name()+"</p>"+
                "<p>赛季期数:"+seasonStateData.getSeason()+"</p>"+
                "<p>赛季状态:"+seasonStateData.getState().name()+"</p>";
    }

    public void outputLog(String msg){
        this.outputInfo = (msg==null?"":msg);
    }

    public String outputInfo(){
        if(outputInfo.equals("")){
            return outputInfo;
        }else {
            return "<p>执行结果:</p>"+"<p>"+ outputInfo +"</p>";
        }
    }

    public String joinServerInfo(){
        YQZZConst.YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
        if(state == YQZZConst.YQZZActivityState.HIDDEN){
            return "<p>服务器状态:</p>";
        }
        int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
        YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
        Map<String, YQZZSeasonServer> seasonServerMap = YQZZSeasonServer.loadAll(timeCfg.getSeason());
        Map<String, YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(termId);
        Map<String,YQZZJoinServer> map = YQZZJoinServer.loadAll(termId);
        String info = "";
        if(dataMap != null && dataMap.size()>0){
            for(YQZZMatchRoomData roomData : dataMap.values()){
                for(String serverId : roomData.getServers()){
                    YQZZJoinServer joinServer = map.get(serverId);
                    YQZZSeasonServer seasonServer = seasonServerMap.get(serverId);
                    info = info + genServerInfo(joinServer, seasonServer, roomData);
                }
            }
        }else {
            for(YQZZJoinServer server : map.values()){
                YQZZSeasonServer seasonServer = seasonServerMap.get(server.getServerId());
                info = info + genServerInfo(server, seasonServer, null);
            }
        }


        return "<p>服务器状态:</p>" +
               "<table border=\"1\" width=\"1200px\">\n" +
               "<tr>\n" +
               "<td>id</td>\n" +
               "<td>战力</td>\n" +
                "<td>leaderId</td>\n" +
                "<td>leaderName</td>\n" +
                "<td>leaderGuild</td>\n" +
                "<td>leaderGuildName</td>\n" +
                "<td>联盟数</td>\n" +
                "<td>玩家数</td>\n" +
                "<td>战场服</td>\n" +
                "<td>分数</td>\n" +
                "<td>是否晋级</td>\n" +
               "</tr>\n" +
                info+
               "</table>";
    }

    public  String genServerInfo(YQZZJoinServer joinServer, YQZZSeasonServer seasonServer, YQZZMatchRoomData roomData){
        return "<tr>\n" +
                "<td>"+joinServer.getServerId()+"</td>\n" +
                "<td>"+joinServer.getPower()+"</td>\n" +
                "<td>"+joinServer.getLeaderId()+"</td>\n" +
                "<td>"+joinServer.getLeaderName()+"</td>\n" +
                "<td>"+joinServer.getLeaderGuild()+"</td>\n" +
                "<td>"+joinServer.getLeaderGuildName()+"</td>\n" +
                "<td>"+joinServer.getJoinGuilds().size()+"</td>\n" +
                "<td>"+joinServer.getFreePlayers().size()+"</td>\n" +
                "<td>"+(roomData == null ? "0":roomData.getRoomServerId())+"</td>\n" +
                "<td>"+(seasonServer == null ? "0":seasonServer.getScore())+"</td>\n" +
                "<td>"+(seasonServer == null ? "非赛季":(seasonServer.isAdvance()?"已晋级":"未晋级"))+"</td>\n" +
               "</tr>\n";
    }
    public String pageInfo(){
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head> \n" +
                "    <meta charset=\"utf-8\"> \n" +
                "    <title>月球之战GM</title> \n" +
                "    <style>\n" +
                "        #main{ margin:0 auto; width:320px; height:100px;}\n" +
                "    </style>\n" +
                "</head> \n" +
                "<body>\n" +
                "    <div>\n" +
                "        <h1>月球之战GM</h1>\n" +
                "        <label>设置期数:</label >\n" +
                "        <input type=\"text\" id=\"termId\" onkeyup=\"value=value.replace(/[^\\d]/g,'')\">\n" +
                activityInfo()+
                "        <button onclick=\"flush();\">刷新</button>\n" +
                "        <button onclick=\"changeState('clean');\">清除当期数据</button>\n" +
                "        <button onclick=\"changeState('start');\">开始活动</button>\n" +
                "        <button onclick=\"changeState('next');\">下一阶段</button>\n" +
                "        <button onclick=\"changeState('clear');\">解除GM控制</button>\n" +
                outputInfo()+
                joinServerInfo()+
                "    </div>\n" +
                "</body>\n" +
                "<script type=\"text/javascript\">\n" +
                "    function getQueryVariable(variable)\n" +
                "    {\n" +
                "        var query = window.location.search.substring(1);\n" +
                "        var vars = query.split(\"&\");\n" +
                "        for (var i=0;i<vars.length;i++) {\n" +
                "            var pair = vars[i].split(\"=\");\n" +
                "            if(pair[0] == variable){\n" +
                "                return pair[1];\n" +
                "            }\n" +
                "        }\n" +
                "        return(false);\n" +
                "    }\n" +
                "    var tmp = getQueryVariable(\"termId\");\n" +
                "    const term = document.getElementById(\"termId\");\n" +
                "    console.log(tmp);\n" +
                "    if(tmp == false){\n" +
                "        term.value = 0;\n" +
                "    }else{\n" +
                "        term.value = tmp;\n" +
                "    }   \n" +
                "    function changeState(opt) {\n" +
                "        const term = document.getElementById(\"termId\");\n" +
                "        var id=term.value;\n" +
                "        var origin = window.location.origin\n" +
                "        window.location.href = origin + \"/script/yqzzgm?opt=\"+opt+\"&termId=\"+id;\n" +
                "    }\n" +
                "   function flush() {\n" +
                "        var origin = window.location.origin\n" +
                "        window.location.href = origin + \"/script/yqzzgm\";\n" +
                "    }\n" +
                "</script>\n" +
                "</html>";
    }

    public String testMatch(int count, int perCount, int poolSize){
        List<YQZZMatchRoomData> rooms = new ArrayList<>();
        List<YQZZJoinServer> serverList = new ArrayList<>();
        Map<String, YQZZJoinServer> serverMap = new HashMap<>();
        for(int i=1;i<=count;i++){
            YQZZJoinServer server = new YQZZJoinServer();
            server.setServerId(""+(10000+i));
            server.setPower(HawkRand.randInt(2000000, 200000000));
            serverList.add(server);
            serverMap.put(server.getServerId(), server);
        }
        int serverCount = serverList.size();
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
        Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
            @Override
            public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
                if(o1.getPower() != o2.getPower()){
                    return o1.getPower() > o2.getPower()?-1 :1;
                }
                return 0;
            }
        });
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
            List<YQZZJoinServer> roomServers = this.getMatchRoomServers(serverList, poolSize, getCount);
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
            room.setTermId(0);
            room.setRoomId(HawkUUIDGenerator.genUUID());
            room.setRoomServerId(roomServerId);
            Set<String> joinServerSet = new HashSet<>();
            for (YQZZJoinServer roomServer : roomServers) {
                room.addServer(roomServer.getServerId());
                joinServerSet.add(roomServer.getServerId());
            }
            rooms.add(room);
        }
        String rlt = "";
        for(YQZZMatchRoomData roomData : rooms){
            rlt+="[";
            for(String serverId : roomData.getServers()){
                YQZZJoinServer server = serverMap.get(serverId);
                rlt += server.getServerId()+"("+server.getPower()+"),";
                //rlt += roomData.getServers();
            }

            rlt += "]<br>";
            rlt += "<br>";
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

}
