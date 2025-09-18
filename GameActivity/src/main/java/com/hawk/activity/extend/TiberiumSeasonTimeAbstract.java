package com.hawk.activity.extend;

import org.hawk.config.HawkConfigBase;

public abstract class TiberiumSeasonTimeAbstract extends HawkConfigBase{
	private final int id;
	
	private final int season;
	
	private final int termId;
	
	private final int type;

	public TiberiumSeasonTimeAbstract() {
		id = 0;
		season = 0;
		termId = 0;
		type = 0;
	}

	public int getId() {
		return id;
	}

	public int getSeason() {
		return season;
	}

	public int getTermId() {
		return termId;
	}

	public int getType() {
		return type;
	}

	private long seasonStartTimeValue;

	private long matchStartTimeValue;

	private long matchEndTimeValue;
	
	private long manageEndTimeValue;

	private long warStartTimeValue;

	private long warEndTimeValue;
	
	private long seasonEndTimeValue;

	public long getSeasonStartTimeValue() {
		return seasonStartTimeValue;
	}

	public long getMatchStartTimeValue() {
		return matchStartTimeValue;
	}

	public long getMatchEndTimeValue() {
		return matchEndTimeValue;
	}

	public long getManageEndTimeValue() {
		return manageEndTimeValue;
	}

	public long getWarStartTimeValue() {
		return warStartTimeValue;
	}

	public long getWarEndTimeValue() {
		return warEndTimeValue;
	}

	public long getSeasonEndTimeValue() {
		return seasonEndTimeValue;
	}


	
}
