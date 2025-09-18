package com.hawk.game.player.hero;

import java.util.Arrays;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.HeroTalentCfg;
import com.hawk.game.protocol.Hero.PBTalentSlot;

/**
 * 
 * @author lwt
 * @date 2017年10月23日
 */
public class TalentSlot implements SerializJsonStrAble {
	private PlayerHero parent;
	private boolean unlock;
	private int index;
	private HeroTalent talent;
	private int preSetTalent;// 随机待选定的天赋
	public TalentSlot(PlayerHero parent) {
		this.parent = parent;
	}

	/**
	 * 技能已解锁
	 * 
	 * @return
	 */
	public final boolean isUnLock() {
		return unlock;
	}

	public double power() {
		if (Objects.isNull(this.talent)) {
			return 0;
		}
		if (!isUnLock()) {
			return 0;
		}

		HeroTalentCfg skillCfg = this.getTalent().getCfg();
		return skillCfg.getPowerCoe().first + skillCfg.getPowerCoe().second * this.getTalent().getExp();
	}

	@Override
	public String serializ() {
		Object[] arr = new Object[5];
		arr[0] = unlock;
		arr[1] = index;

		if (Objects.isNull(talent)) {
			arr[2] = false;
		} else {
			arr[2] = true;
			arr[3] = talent.skillID();
			arr[4] = talent.serializ();
		}

		JSONArray array = new JSONArray(Arrays.asList(arr));
		return array.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONArray array = JSONArray.parseArray(serialiedStr);
		unlock = array.getBooleanValue(0);
		index = array.getIntValue(1);
		if (array.getBooleanValue(2)) {
			talent = new HeroTalent(array.getIntValue(3));
			talent.setParent(this);
			talent.mergeFrom(array.getString(4));
		}
	}

	public PBTalentSlot toPBObj() {
		PBTalentSlot.Builder builder = PBTalentSlot.newBuilder();
		builder.setIndex(index).setUnlock(unlock);
		if (Objects.nonNull(talent)) {
			builder.setTalent(talent.toPBobj());
		}
		return builder.build();
	}

	public PlayerHero getParent() {
		return parent;
	}

	public void setParent(PlayerHero parent) {
		this.parent = parent;
	}

	public boolean isUnlock() {
		return unlock;
	}

	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

	public HeroTalent getTalent() {
		return talent;
	}

	public void setTalent(HeroTalent talent) {
		this.talent = talent;
		if (Objects.nonNull(talent)) {
			talent.setParent(this);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setPreSetTalent(int preSetTalent) {
		this.preSetTalent = preSetTalent;
	}

	public int getPreSetTalent() {
		return preSetTalent;
	}
	
	
}
