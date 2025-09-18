package com.hawk.game.util;

import org.hawk.xid.HawkXID;

public class FixThreadXID extends HawkXID {

	private int hashThread;

	private FixThreadXID() {
	}
	
	/**
	 * 实例化接口
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	static public FixThreadXID valueOf(int type, String uuid,int hashThread) {
		FixThreadXID hawkXID = new FixThreadXID();
		hawkXID.setType(type);
		hawkXID.setUUID(uuid);
		hawkXID.setHashThread(hashThread);
		return hawkXID;
	}

	@Override
	public int getHashThread(int threadNum) {
		return Math.abs(hashThread % threadNum);
	}

	public int getHashThread() {
		return hashThread;
	}

	public void setHashThread(int hashThread) {
		this.hashThread = hashThread;
	}

}
