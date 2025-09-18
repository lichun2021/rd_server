package com.hawk.activity.type.impl.heavenBlessing.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.List;

/**
 * 天降洪福
 * @author Leon
 */
@HawkConfigManager.KVResource(file = "activity/heaven_blessing/heaven_blessing_cfg.xml")
public class HeavenBlessingKVCfg extends HawkConfigBase {
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;
    /**
     * 玩家注册天数, 玩家注册天数 > N天
     */
    private final int registerDays;
    /**
     * 主堡建筑等级, 玩家主堡等级 > N级
     */
    private final int buildingLevel;
    /**
     * 循环天数, N天内仅可触发一次
     */
    private final int loopDays;

    private final int continuouslyTime;

    private final int freeAward;

    /**
     * 请求tx接口的地址
     */
    private final String addr;
    /**
     * l5参数
     */
    private final String addrL5;
    /**
     * 测试数据
     */
    private final String testData;

    /**
     * l5的分解参数
     */
    private int l5_modId = 0;
    private int l5_cmdId = 0;

    /**
     * 是否使用l5
     */
    private boolean l5Req = false;
    /**
     * 请求子链接
     */
    private String subAddr;

    public HeavenBlessingKVCfg() {
        serverDelay = 0;
        registerDays = 0;
        buildingLevel = 0;
        loopDays = 0;
        continuouslyTime = 0;
        freeAward = 0;
        addr = "";
        addrL5 = "";
        testData = "";
    }

    @Override
    protected boolean assemble() {
        //如果配置了l5请求值
        if (!HawkOSOperator.isEmptyString(addrL5)) {
            //分割请求链接获得modid和cmdid
            String infos[] = addrL5.split(":");
            this.l5_modId = Integer.valueOf(infos[0]);
            this.l5_cmdId = Integer.valueOf(infos[1]);
            //使用l5
            l5Req = true;
            //获得子链接
            String originString = addr;
            if (originString.startsWith("https")) {
                originString = originString.replaceFirst("https://", "");
            } else {
                originString = originString.replaceFirst("http://", "");
            }
            int index = originString.indexOf("/");
            subAddr = originString.substring(index + 1, originString.length());
            //输出日志记录
            HawkLog.logPrintln("HeavenBlessingKVCfg config, l5_modId: {}, l5_cmdId: {}, subAttr: {}", l5_modId, l5_cmdId, subAddr);
        }
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public long getRegisterDays(){
        return registerDays * 24 * 60 * 60 * 1000l;
    }

    public long getContinuouslyTime() {
        return continuouslyTime * 1000L;
    }

    public long getLoopDays() {
        return loopDays * 1000L;
    }

    public int getBuildingLevel() {
        return buildingLevel;
    }

    public int getFreeAward() {
        return freeAward;
    }

    public String getAddr() {
        return addr;
    }

    public String getAddrL5() {
        return addrL5;
    }

    public int getL5_modId() {
        return l5_modId;
    }

    public int getL5_cmdId() {
        return l5_cmdId;
    }

    public boolean isL5Req() {
        return l5Req;
    }

    public String getSubAddr() {
        return subAddr;
    }

    public String getTestData() {
        return testData;
    }
}
