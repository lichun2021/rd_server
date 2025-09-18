package com.hawk.game.data;


/***
 * 福袋分享，手Q数据返回解析类
 * @author yang.rao
 *
 */
public class BlessBagQQResponse {
	
	private int ret;
	
	private InnerDate data;
	
	private String msg;

	public int getRet() {
		return ret;
	}

	public void setRet(int ret) {
		this.ret = ret;
	}

	public InnerDate getData() {
		return data;
	}

	public void setData(InnerDate data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public static class InnerDate{
		private Act act;
		private Op op;
		private String rule;
		public Act getAct() {
			return act;
		}
		public void setAct(Act act) {
			this.act = act;
		}
		public Op getOp() {
			return op;
		}
		public void setOp(Op op) {
			this.op = op;
		}
		public String getRule() {
			return rule;
		}
		public void setRule(String rule) {
			this.rule = rule;
		}
	}
	
	public static class Act{		
	}
	
	public static class Op{
		private String token;
		private String owner; //qq号
		private int total;
		private int num;
		private int index;
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		public int getTotal() {
			return total;
		}
		public void setTotal(int total) {
			this.total = total;
		}
		public int getNum() {
			return num;
		}
		public void setNum(int num) {
			this.num = num;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
	}
}
