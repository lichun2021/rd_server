package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.List;

import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.TiberiumConstCfg;

public class TWFightState {
	@JSONField(serialize = false)
	public boolean hasInit;
	
	public int termId = 0;
	

	public List<TWFightUnit> unitList = new ArrayList<>();

	
	
	public TWFightState() {
		List<TWFightUnit> list = new ArrayList<>();
		List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			TWFightUnit unit = new TWFightUnit();
			unit.setTimeIndex(i);
			list.add(unit);
		}
		unitList = list;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public List<TWFightUnit> getUnitList() {
		return unitList;
	}

	public void setUnitList(List<TWFightUnit> unitList) {
		this.unitList = unitList;
	}

	@JSONField(serialize = false)
	public boolean isHasInit() {
		return hasInit;
	}
	
	@JSONField(serialize = false)
	public void setHasInit(boolean hasInit) {
		this.hasInit = hasInit;
	}
	
}
