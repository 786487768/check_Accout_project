package entity;
// Generated 2016-11-22 19:08:12 by Hibernate Tools 5.1.0.Beta1

/**
 * BankInputBackup generated by hbm2java
 */
public class BankInputBackup implements java.io.Serializable {

	private Integer id;
	private String payer;
	private Double money;
	private String payWay;
	private String payerAccount;
	private Integer payid;
	private String contractNum;
	private Boolean status;
	private String inputTime;
	private String payee;
	private String payeeAccount;
	private Boolean isConnect;
	private String manyContract;
	private Integer connectNum;
	private String connectClient;
	private String owner;

	public BankInputBackup() {
	}

	public BankInputBackup(String payer, Double money, String payWay, String payerAccount, Integer payid,
			String contractNum, Boolean status, String inputTime, String payee, String payeeAccount, Boolean isConnect,
			String manyContract, Integer connectNum, String connectClient, String owner) {
		this.payer = payer;
		this.money = money;
		this.payWay = payWay;
		this.payerAccount = payerAccount;
		this.payid = payid;
		this.contractNum = contractNum;
		this.status = status;
		this.inputTime = inputTime;
		this.payee = payee;
		this.payeeAccount = payeeAccount;
		this.isConnect = isConnect;
		this.manyContract = manyContract;
		this.connectNum = connectNum;
		this.connectClient = connectClient;
		this.owner = owner;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPayer() {
		return this.payer;
	}

	public void setPayer(String payer) {
		this.payer = payer;
	}

	public Double getMoney() {
		return this.money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public String getPayWay() {
		return this.payWay;
	}

	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}

	public String getPayerAccount() {
		return this.payerAccount;
	}

	public void setPayerAccount(String payerAccount) {
		this.payerAccount = payerAccount;
	}

	public Integer getPayid() {
		return this.payid;
	}

	public void setPayid(Integer payid) {
		this.payid = payid;
	}

	public String getContractNum() {
		return this.contractNum;
	}

	public void setContractNum(String contractNum) {
		this.contractNum = contractNum;
	}

	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getInputTime() {
		return this.inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public String getPayee() {
		return this.payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public String getPayeeAccount() {
		return this.payeeAccount;
	}

	public void setPayeeAccount(String payeeAccount) {
		this.payeeAccount = payeeAccount;
	}

	public Boolean getIsConnect() {
		return this.isConnect;
	}

	public void setIsConnect(Boolean isConnect) {
		this.isConnect = isConnect;
	}

	public String getManyContract() {
		return this.manyContract;
	}

	public void setManyContract(String manyContract) {
		this.manyContract = manyContract;
	}

	public Integer getConnectNum() {
		return this.connectNum;
	}

	public void setConnectNum(Integer connectNum) {
		this.connectNum = connectNum;
	}

	public String getConnectClient() {
		return this.connectClient;
	}

	public void setConnectClient(String connectClient) {
		this.connectClient = connectClient;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

}
