package entity;
// Generated 2016-11-22 19:08:12 by Hibernate Tools 5.1.0.Beta1

/**
 * ConnectPerson generated by hbm2java
 */
public class ConnectPerson implements java.io.Serializable {

	private String username;
	private String phone;
	private String weixin;
	private String company;
	private String password;
	private String realName;
	private Character registerWay;
	private String agent;
	private String contractMes;
	private String companyid;
	private Integer flag;
	private String email;
	private String cardid;
	private Integer score;

	public ConnectPerson() {
	}

	public ConnectPerson(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public ConnectPerson(String username, String phone, String weixin, String company, String password, String realName,
			Character registerWay, String agent, String contractMes, String companyid, Integer flag, String email,
			String cardid, Integer score) {
		this.username = username;
		this.phone = phone;
		this.weixin = weixin;
		this.company = company;
		this.password = password;
		this.realName = realName;
		this.registerWay = registerWay;
		this.agent = agent;
		this.contractMes = contractMes;
		this.companyid = companyid;
		this.flag = flag;
		this.email = email;
		this.cardid = cardid;
		this.score = score;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getWeixin() {
		return this.weixin;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealName() {
		return this.realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Character getRegisterWay() {
		return this.registerWay;
	}

	public void setRegisterWay(Character registerWay) {
		this.registerWay = registerWay;
	}

	public String getAgent() {
		return this.agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getContractMes() {
		return this.contractMes;
	}

	public void setContractMes(String contractMes) {
		this.contractMes = contractMes;
	}

	public String getCompanyid() {
		return this.companyid;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public Integer getFlag() {
		return this.flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCardid() {
		return this.cardid;
	}

	public void setCardid(String cardid) {
		this.cardid = cardid;
	}

	public Integer getScore() {
		return this.score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

}
