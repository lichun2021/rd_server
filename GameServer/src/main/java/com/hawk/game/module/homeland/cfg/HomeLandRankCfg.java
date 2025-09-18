package com.hawk.game.module.homeland.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.*;

/**
 * 家园排行榜配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_rank.xml")
public class HomeLandRankCfg extends HawkConfigBase {
    @Id
    protected final int id;
    //荣誉度||点赞
    protected final int type;
    //本服|联盟|全服
    protected final int range;
    //展示人数
    protected final int maxNum;
    //排行榜刷新间隔
    protected final int delay;
    //服务器分组
    protected final String servers;
    //服务器分组
    protected List<String> serverList = new ArrayList<>();

    private static Map<Integer, Map<Integer, HomeLandRankCfg>> rankMap = new HashMap<>();

    public HomeLandRankCfg() {
        id = 0;
        type = 0;
        range = 0;
        maxNum = 100;
        delay = 10000;
        servers = "";
    }

    /**
     * 该建筑是否为主建筑
     *
     * @return
     */

    @Override
    protected boolean assemble() {
        Map<Integer, HomeLandRankCfg> serverMap = rankMap.computeIfAbsent(this.getType(), k -> new HashMap<>());
        serverMap.putIfAbsent(this.range, this);
        if (!HawkOSOperator.isEmptyString(servers)) {
            String[] serverStr = servers.split(SerializeHelper.BETWEEN_ITEMS);
            Collections.addAll(serverList, serverStr);
        }
        return true;
    }

    public static HomeLandRankCfg getByType(int type, int range) {
        if (rankMap.containsKey(type)) {
            if (rankMap.get(type).containsKey(range)) {
                return rankMap.get(type).get(range);
            }
        }
        return null;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getRange() {
        return range;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int getDelay() {
        return delay * 1000;
    }

    public List<String> getServerList() {
        return serverList;
    }
}
