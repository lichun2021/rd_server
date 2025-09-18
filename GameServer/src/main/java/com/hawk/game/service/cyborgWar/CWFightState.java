package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.List;

import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.CyborgConstCfg;

public class CWFightState {
	@JSONField(serialize = false)
	public boolean hasInit;
	
	public int termId = 0;
	

	public List<CWFightUnit> unitList = new ArrayList<>();

	
	
	public CWFightState() {
		List<CWFightUnit> list = new ArrayList<>();
		List<HawkTuple2<Integer, Integer>> timeList = CyborgConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			CWFightUnit unit = new CWFightUnit();
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

	public List<CWFightUnit> getUnitList() {
		return unitList;
	}

	public void setUnitList(List<CWFightUnit> unitList) {
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
