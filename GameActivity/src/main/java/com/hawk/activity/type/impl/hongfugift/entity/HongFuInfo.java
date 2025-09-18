package com.hawk.activity.type.impl.hongfugift.entity;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

import java.util.ArrayList;
import java.util.List;

/**洪福礼包详细数据
 * @author hf
 */
public class HongFuInfo implements SplitEntity {
    /**id*/
    private int id;
    /**解锁*/
    private int unlock;
    /**自选奖励*/
    private int rewardId;
    /**领取的天数*/
    private List<Integer> recDayList;

    public HongFuInfo() {

    }

    public static HongFuInfo valueOf(int id) {
        HongFuInfo hongFuInfo = new HongFuInfo();
        hongFuInfo.id = id;
        hongFuInfo.recDayList = new ArrayList<>();
        return hongFuInfo;
    }

    @Override
    public SplitEntity newInstance() {
        return new HongFuInfo();
    }

    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(this.id);
        dataList.add(this.unlock);
        dataList.add(this.rewardId);
        dataList.add(SerializeHelper.collectionToString(this.recDayList, SerializeHelper.BETWEEN_ITEMS));
    }

    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(4);
        this.id = dataArray.getInt();
        this.unlock = dataArray.getInt();
        this.rewardId = dataArray.getInt();
        String recDayStr = dataArray.getString();
        this.recDayList = SerializeHelper.stringToList(Integer.class, recDayStr, SerializeHelper.BETWEEN_ITEMS, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "[id=" + id + ",unlock=" + unlock + ", rewardId=" + rewardId +  ", recDayList=" + SerializeHelper.collectionToString(this.recDayList, SerializeHelper.BETWEEN_ITEMS) +  "]";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUnlock() {
        return unlock;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }
    /**大于0 解锁 */
    public boolean isUnlock() {
       return unlock > 0;
    }

    public int getRewardId() {
        return rewardId;
    }

    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }

    public List<Integer> getRecDayList() {
        return recDayList;
    }

    /**
     * 添加领奖的天数数据
     * @param dayTh
     */
    public void addRecDayList(int dayTh) {
        if (!recDayList.contains(dayTh)){
            this.recDayList.add(dayTh);
        }
    }

}
