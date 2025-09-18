package com.hawk.game.util;

public class RandomContent<T> implements WeightAble{
	private int weight;
	private T obj;
	
	public static <T extends Object> RandomContent<T> create(T obj,int weight){
		RandomContent<T> reuslt = new RandomContent<T>();
		reuslt.obj = obj;
		reuslt.weight = weight;
		return reuslt;
	}
	
	@Override
	public int getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}
	public T getObj() {
		return obj;
	}
	public void setObj(T obj) {
		this.obj = obj;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	
}
