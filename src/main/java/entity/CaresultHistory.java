package entity;
// Generated 2016-11-22 19:08:12 by Hibernate Tools 5.1.0.Beta1

/**
 * CaresultHistory generated by hbm2java
 */
public class CaresultHistory implements java.io.Serializable {

	private Integer id;
	private String caid;
	private String cayear;
	private String camonth;
	private String url;
	private String owner;
	private Character caresult;
	private Boolean lastcaid;

	public CaresultHistory() {
	}

	public CaresultHistory(String caid) {
		this.caid = caid;
	}

	public CaresultHistory(String caid, String cayear, String camonth, String url, String owner, Character caresult,
			Boolean lastcaid) {
		this.caid = caid;
		this.cayear = cayear;
		this.camonth = camonth;
		this.url = url;
		this.owner = owner;
		this.caresult = caresult;
		this.lastcaid = lastcaid;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCaid() {
		return this.caid;
	}

	public void setCaid(String caid) {
		this.caid = caid;
	}

	public String getCayear() {
		return this.cayear;
	}

	public void setCayear(String cayear) {
		this.cayear = cayear;
	}

	public String getCamonth() {
		return this.camonth;
	}

	public void setCamonth(String camonth) {
		this.camonth = camonth;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Character getCaresult() {
		return this.caresult;
	}

	public void setCaresult(Character caresult) {
		this.caresult = caresult;
	}

	public Boolean getLastcaid() {
		return this.lastcaid;
	}

	public void setLastcaid(Boolean lastcaid) {
		this.lastcaid = lastcaid;
	}

}