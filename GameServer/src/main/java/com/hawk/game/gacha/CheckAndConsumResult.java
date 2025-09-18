package com.hawk.game.gacha;

import java.util.Collections;
import java.util.List;

import com.hawk.game.item.ItemInfo;

public class CheckAndConsumResult {
	private boolean success;
	private List<ItemInfo> cost;

	private CheckAndConsumResult() {
	}

	public static CheckAndConsumResult create(boolean success, List<ItemInfo> cost) {
		CheckAndConsumResult result = new CheckAndConsumResult();
		result.success = success;
		result.cost = cost;
		return result;
	}

	public static CheckAndConsumResult create(boolean success) {
		return create(success, Collections.emptyList());
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		throw new UnsupportedOperationException();
	}

	public List<ItemInfo> getCost() {
		return cost;
	}

	public void setCost(String cost) {
		throw new UnsupportedOperationException();
	}

}
