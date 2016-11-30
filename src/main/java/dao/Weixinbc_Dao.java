package dao;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.PayRecord;
import entity.WeixinBindConnectPerson;

public class Weixinbc_Dao {
	private static Logger logger = LogManager.getLogger(Agent_Dao.class);
	private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Weixinbc_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
	/*	session = sessionFactory.openSession();
		transaction = session.beginTransaction();*/
		try {
				session = sessionFactory.openSession();
				transaction = session.beginTransaction();
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("beginTransaction failed:" + e);
		}

	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public boolean add(WeixinBindConnectPerson in_assistance){
		try {
			beginTransaction();
			session.save(in_assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("save failed");
			logger_error.error("��Ӷ�����ϵ��" +in_assistance.getUsername() + "ʧ��" + e);
			return false;
		}
	}
	
	public boolean delete(WeixinBindConnectPerson de_assistance){
		try {
			beginTransaction();
			session.delete(de_assistance);
			endTransaction();
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete failed");
			logger_error.error("ɾ��������ϵ��" + de_assistance.getUsername() + "ʧ��" + e);
			return false;
		}
	}
	
	public WeixinBindConnectPerson findById(Class<WeixinBindConnectPerson> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			WeixinBindConnectPerson find_assistance = (WeixinBindConnectPerson) session.get(cla, id);
			session.close();
			return find_assistance;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("����id" + "=" + id + "���Ҷ�����ϵ��ʧ��" + e);
			return null;
		}
	}
	
	public boolean update(WeixinBindConnectPerson connectp){
		try {
			beginTransaction();
			session.update(connectp);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("���¶�����ϵ��" + connectp.getUsername()+ "ʧ��" + e);
			return false;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<WeixinBindConnectPerson> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select connectp from WeixinBindConnectPerson connectp where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<WeixinBindConnectPerson> wxbcp = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			return wxbcp;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("����" + filed + "=" + value + "����΢����ϵ��ʧ��" + e);
			return null;
		}
	}
}
