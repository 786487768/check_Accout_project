package check_Asys;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.hibernate.SessionFactory;
import org.springframework.web.multipart.MultipartFile;

import com.sun.org.apache.bcel.internal.generic.GOTO;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import check_Asys.CheckAcManage.Watch_Object;
import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.BInput_Backup_Dao;
import dao.BankInput_Dao;
import dao.CaresultHistory_Dao;
import dao.ConnectPerson_Dao;
import dao.CusSdStore_Backup_Dao;
import dao.OpLog_Dao;
import dao.Ori_BackUp_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import en_de_code.ED_Code;
import entity.Agent;
import entity.BankInput;
import entity.CaresultHistory;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.OpLog;
import entity.OriOrder;
import entity.PayRecord;
import entity.PayRecordHistory;
import file_op.AnyFile_Op;
import file_op.Excel_RW;
import file_op.AnyFile_Op.AnyFileElement;
import file_op.Excel_RW.Excel_Row;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 对账系统的业务服务（对账服务）实现类，这个类实现了对账系统的业务功能
 * @author zhangxinming
 *
 */
public class CheckAcManage {
	private static Logger logger = LogManager.getLogger(CheckAcManage.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public AutoCheckAuccount auccount;
	public CheckARseult cARseult;//对账结果
	public Dao_List dao_List;
	public FormProduce formProduce;
	public byte m_op;
	public static final String IMPORT = "import";//导入(最好采用十六进制表示)
	public static final String WATCH = "watch";//查看
	public static final String CHECK = "check";//审阅
	public static final String MAP = "map";//寻找匹配的出纳记录
	public static final String START_CHECKWORK="start_work";//开始对账
	public static final String RES_PAYR = "payrecord";//付款信息
	public static final String RES_ORIA = "ori_account";//原始账单
	public static final String RES_BANKTs = "bankTs";//未关联出纳记录
	public static final String RES_CAHISTORY = "caresult_history";//对账结果
	public static final String RES_PAYCACHE = "pay_cache";//新上传的付款信息
	public static final String WATCH_CARes = "watch_cares";//查看对账结果
	public static final String EXPORT_CARes = "export_cares";//导出对账结果
	public static final String ENTRER_CaModel = "enter_camodel";//进入对账模式
	public static final String CANCEL_CaAgain = "cancel_calagin";//取消并重新对账
	public static final String FREEBACK = "freeback";//返回积分
	
	public static final String SaveDirName_Orider = "OrderForms";
	public static final String SaveDirName_BankInput = "BankinputForms";
	public static final String FileName_Orider = "_orider.xlsx";
	public static final String FileName_BankInput = "_bankinput.xlsx";
	public String checkresult_url;
	
	public CheckAcManage(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		  dao_List = new Dao_List(wFactory);
		  auccount = new AutoCheckAuccount(dao_List.bDao,dao_List.pDao,dao_List.tDao,dao_List.sDao,dao_List.pHDao,dao_List.cDao,dao_List.pCDao,dao_List.cPerson_Dao,dao_List.oUp_Dao,dao_List.bInput_Backup_Dao,dao_List.agent_Dao,dao_List.cBackup_Dao);
		  cARseult = new CheckARseult(dao_List);
		  formProduce = new FormProduce(wFactory, dao_List,cARseult);
	}
	  
	public void Close_All_Dao(){
		dao_List.tDao.Close_Connect(); 
		dao_List.pDao.Close_Connect();
		dao_List.bDao.Close_Connect();
		dao_List.sDao.Close_Connect();
	}
	  
	public Object OpSelect(String operation,Object object,Owner owner){
		Object re_object = null;
		if (operation.equals(IMPORT)) {//上传货款表及出纳表
			Import_Object select = (Import_Object) object;
			return Import(select.import_type,select.file,select.operator,select.savepath,select.filename,select.caid);
		}
		else if(operation.equals(MAP)){
			Map_Object map_Object = (Map_Object) object;
			List<BankInput> fBankInputs = Map(map_Object,owner.work_id);
			re_object = fBankInputs;
		}
		else if(operation.equals(CHECK)){
			
		}
		else if(operation.equals(START_CHECKWORK)){
			String caid = (String) object;

			re_object = StartCheckWork(owner.work_id);
		}
		else if (operation.equals(WATCH_CARes)) {
			Watch_CAResObject watch_select = (Watch_CAResObject) object;
			re_object = Watch_CheckAResult(watch_select,watch_select.operator);
		}
		else if (operation.equals(EXPORT_CARes)) {
			Export_CAResObject export_CAResObject = (Export_CAResObject) object;
			Export_CheckA_Result(export_CAResObject,owner);
		}
		else if (operation.equals(ENTRER_CaModel)) {
			//Enter_CaModel(owner.work_id);
			String who = (String) object;
			String caid = auccount.Enter_CaModel(owner.work_id);
			
			/*查找上次上传的时间和上传的结果*/
			List<OpLog> fLogs = dao_List.opLog_Dao.FindBySpeElement_S_ByOwner("content", OpLog_Service.IMPORT, who);
			
			JSONObject jsonObject = new JSONObject();
			if (fLogs.size() == 0) {
				jsonObject.element("caid", caid);
				jsonObject.element("flag", 0);
				jsonObject.element("lastUploadResult", "没有上传记录");
				jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
			}
			else {
				String time = fLogs.get(fLogs.size() - 1).getTime();
				String result = fLogs.get(fLogs.size() - 1).getResult();
				String curym = new SimpleDateFormat("yyyy/MM").format(new Date());
				if (time.contains(curym)) {
					jsonObject.element("caid", caid);
					jsonObject.element("flag", 0);
					jsonObject.element("lastUploadResult", result);
					jsonObject.element("lastUploadTime", time.replace('/', '-'));
				}
				else {
					jsonObject.element("caid", caid);
					jsonObject.element("flag", 0);
					jsonObject.element("lastUploadResult", "本月没有上传记录");
					jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
				}
			}	
			/*查找上次上传的时间和上传的结果*/
			
			re_object = jsonObject;
		}
		else if (operation.equals(CANCEL_CaAgain)) {
			String caid = (String)object;
			CancelAndCaAgain(owner.work_id,caid);
		}
		else if (operation.equals(FREEBACK)) {
			FreeBackToCustom(owner.work_id);
		}
		else{
			System.out.println("unknow operation");
		}
		
		return re_object;
	}
	
	/*统一返利客户*/
	public void FreeBackToCustom(String owner){
		List<PayRecord> pList = dao_List.pDao.GetPrecordTbByElement("owner", owner);
		
		for (int i = 0; i < pList.size(); i++) {
			PayRecord pRecord = pList.get(i);
			auccount.isFreeBack(pRecord,owner);
		}
	}
	
	/*进入对账模式*/
	public void Enter_CaModel(String owner){
		auccount.Enter_CaModel(owner);
	}
	
	/**
	 * Import_Initial 导入的初始化工作
	 * @param import_type 导入类型
	 * @param owner 拥有者
	 */
	public void Import_Initial(String caid,char import_type,String owner){
		if (auccount.CreateCaid(owner).equals(caid)) {//本月对账
			if (import_type == 'A') {
				auccount.TransferCus_WareaToBackup(owner);
				auccount.TransferOri_WareaToBackup(owner);//转移货款表到备份区
			}
			else if (import_type == 'B') {
				auccount.TransferBinput_WareaToBackup(owner);//转移出纳到备份区
			}
			else {
				logger_error.error("导入类型未知");
			}			
		}
		else {//历史对账
			if (import_type == 'A') {
				dao_List.tDao.DeleteOoriderByElement("owner", owner);
				dao_List.sDao.DeleteTbByElement("owner", owner);
			}
			else if (import_type == 'B') {
				dao_List.bDao.DeleteBinputTbByElement("owner", owner);
			}
			else {
				logger_error.error("导入类型未知");
			}	
		}

	}
	
	/**
	 * Import_AfterWork 根据导入操作的结果，进行不同的后续操作
	 * @param flag
	 */
	public void Import_AfterWork(int flaga,int flagb,String owner,MultipartFile rfilea,MultipartFile rfileb,String filenamea,String filenameb,String savedira,String savedirb,String caid){
		if (auccount.CreateCaid(owner).equals(caid)) {//本月对账

			if (flaga == 0 && flagb == 0) {//两个表上传成功
				logger.info("本月对账,删除备份区的货款记录和出纳记录");
				dao_List.cBackup_Dao.DeleteTbByElement("owner", owner);
				dao_List.oUp_Dao.DeleteOBackupByElement("owner", owner);
				dao_List.bInput_Backup_Dao.DeleteBInputBupByElement("owner", owner);
			}
		}
		
		if (flaga == 0 && flagb == 0) {//两个表上传成功
			logger.info("货款表和出纳表上传成功");
		
			AnyFile_Op aOp = new AnyFile_Op();
			long filesizea = rfilea.getSize();
			long filesizeb = rfileb.getSize();
			//	String fileName = eCode.ISO_To_UTF8(rfile.getOriginalFilename());//将文件名字符串转成utf-8格式
			AnyFileElement aElementa = aOp.new AnyFileElement(filenamea, savedira, (int)filesizea);
			AnyFileElement aElementb = aOp.new AnyFileElement(filenameb, savedirb, (int)filesizeb);
			
			File dira = aOp.CreateDir(savedira);//创建保存目录
			File dirb = aOp.CreateDir(savedirb);//创建保存目录
			
			File wFilea = aOp.CreateFile(aElementa.dirname, aElementa.filename);//创建保存的文件	
			byte read_ba[] = aOp.ReadFile(rfilea);//读取上传的文件的内容
			aOp.WriteFile(aElementa,read_ba,wFilea);//将文件保存到服务器指定目录中
			
			File wFileb = aOp.CreateFile(aElementb.dirname, aElementb.filename);//创建保存的文件
			byte read_bb[] = aOp.ReadFile(rfileb);//读取上传的文件的内容
			aOp.WriteFile(aElementb,read_bb,wFileb);//将文件保存到服务器指定目录中
		}
		else {
			logger_error.error("导入类型未知");
		}
	}
	
	/**
	 * Import 
	 * @param import_type
	 * @param rfile
	 * @param owner
	 * @param savedir
	 * @param filename
	 */
	public JSONObject Import(char import_type,MultipartFile rfile,String owner,String savedir,String filename,String caid){
		JSONObject re_js = new JSONObject();
		
		Import_Initial(caid,import_type,owner);
		
		if (import_type == 'A') {//导入原始账单
			logger.info("import Account");
			
			/*解析excel表内容*/
			Excel_RW excel_RW = new Excel_RW();//解析excel内容
			ArrayList<Excel_Row> totalA_table = null;
			try {
				totalA_table = excel_RW.ReadExcel_Table(rfile.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Agent fagent = dao_List.agent_Dao.findById(Agent.class, owner);
			JSONObject re_TT = excel_RW.Table_To_Ob_OriOrders(totalA_table,fagent);//将excel表转换成对象
			/*解析excel表内容*/	
			if (re_TT.getInt("flag") == -1) {
				re_js.element("flag", -1);
				re_js.element("errmsg", re_TT.getString("errmsg"));
				return re_js;
			}
			JSONArray jsonArray = (JSONArray) re_TT.get("orders");
			OriOrder[]orders = (OriOrder[]) JSONArray.toArray(jsonArray, OriOrder.class);
			
			/*写入数据库*/
			for (OriOrder order:orders) {
				
				/*填补对账联系人信息*/
				List<ConnectPerson> fPersons = dao_List.cPerson_Dao.FindBySpeElement("companyid", order.getId().getCuscompanyid(), owner);
				if (fPersons.isEmpty() !=  true) {
					order.setCustomname(fPersons.get(0).getRealName());
					order.setCustomphone(fPersons.get(0).getPhone());
					order.setCustomweixin(fPersons.get(0).getWeixin());
				}
				/*填补对账联系人信息*/
				
				/*填补代理商财务信息*/
				Agent agent = dao_List.agent_Dao.findById(Agent.class, owner);
				order.setAsname(agent.getAgentConnectpname());
				order.setAsphone(agent.getAgentCpphone());
				order.setAsemail(agent.getAgentCpemail());
				/*填补代理商财务信息*/
				
				order.setConnectBank(null);
				dao_List.tDao.add(order);
				auccount.ConnectAccountWithCustom(order);  //添加或者刷新客户合同信息
			}
			/*写入数据库*/
			re_js.element("flag", 0);
			re_js.element("errmsg", "导入货款表成功");
			return re_js;
		}
		else if (import_type == 'B') {//导入银行记录
			logger.info("import bank");
			auccount.ResetCustomAccoutMsg(owner);
			/*解析excel文件*/
			Excel_RW excel_RW = new Excel_RW();//解析excel内容
			ArrayList<Excel_Row> totalA_table = null;
			try {
				totalA_table = excel_RW.ReadExcel_Table(rfile.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<BankInput> bankInputs = excel_RW.Table_To_Ob_BankIn(totalA_table,owner);
			/*解析excel文件*/
			
			/*写入数据库*/
			int offsetid = dao_List.bDao.GetMaxID();
			for (int i = 0; i < bankInputs.size(); i++) {
				BankInput bankInput = bankInputs.get(i);
				offsetid = offsetid + 1;
				bankInput.setId(offsetid);
				dao_List.bDao.add(bankInput);
				auccount.ConnectBankinputWithCustom(bankInput);//获取客户的合同信息
			}
			/*写入数据库*/
			re_js.element("flag", 0);
			re_js.element("errmsg", "导入出纳表成功");
			return re_js;
		}
		else {
			logger.info("[Import]:unknow import");
			re_js.element("flag", -1);
			re_js.element("errmsg", "未知导入类型");
			return re_js;
		}
	}
	
	/*导出对账结果*/
	public void Export_CheckA_Result(Export_CAResObject eResObject,Owner owner){
		//formProduce.CreateForm(eResObject.exportlist,owner.work_id, owner.user_type, eResObject.dir,eResObject.filename);
	}
	
	/*查看功能*/
	public JSONObject Watch(Watch_Object location,Owner owner,int offset,int pagesize){
		JSONObject re_json = new JSONObject();
		java.util.List list = null;
		int num = 0;
		switch (location.watch_type) {
		case 'T'://查看整张表
			if (location.table_name.equals(CheckAcManage.RES_PAYR)) {//查看付款记录
				logger.info("watch payrecord table");
				/*根据不同的用户类型获取不同的订单数据*/
					if (owner.user_type.equals("bu")) {//对账人员
						//list = dao_List.pDao.FindBySpeElement_S("owner", owner.work_id);
						list = dao_List.pDao.FindBySpeElement_S_Page("owner", owner.work_id, offset, pagesize);
						num = dao_List.pDao.GetPayTb_Num_ByElement("owner", owner.work_id);
					}
					else if(owner.user_type.equals("bm")){//总监
						
					}
					else {
						System.out.println("unknow usetype");
					}
				//	list = pDao.GetTolAccount();
					//list = dao_List.pDao.FindBySpeElement_S_N("pass", true);
					/*根据不同的用户类型获取不同的订单数据*/
			}
			else if (location.table_name.equals(CheckAcManage.RES_ORIA)) {//查看原始账单
				System.out.println("watch ori_account table");
				/*根据不同的用户类型获取不同的订单数据*/
				if (owner.user_type.equals("bu")) {
					list = dao_List.tDao.FindBySpeElement_S("owner", owner.work_id);	
				}
				else if(owner.user_type.equals("bm")){
					list = dao_List.tDao.GetTolAccount();
				}
				else {
					System.out.println("unknow usetype");
				}
				/*根据不同的用户类型获取不同的订单数据*/
			}
			else if(location.table_name.equals(CheckAcManage.RES_BANKTs)){
				System.out.println("watch bankInputs table");
			//	list = bDao.GetTolBankIns();
				list = dao_List.bDao.FindBySpeElement("status", false,owner.work_id);
				
			}
			else if (location.table_name.equals(CheckAcManage.RES_CAHISTORY)) {
				logger.info("查看对账记录历史");
				list = dao_List.cDao.FindBySpeElement_ByPage("cayear", location.cayear, owner.work_id,offset,pagesize);
				logger.info(list.size() + location.cayear + owner.work_id);
				
				JSONArray orders = new JSONArray();//json类中的数组
				
				for(int i = 0;i<list.size();i++){
					CaresultHistory cahistory = (CaresultHistory) list.get(i);
					if (cahistory != null) {
						//System.out.println(object.getPayer());
						String months = cahistory.getCamonth();
						int month = Integer.parseInt(months);
						if (month < 10) {
							months = String.valueOf(month);
						}
						JSONObject caresult = new JSONObject();
						caresult.element("month", months);
						caresult.element("url", cahistory.getUrl());
						orders.add(i,caresult);
					}
					else{
						logger.error("not find the record");
					}
				}
				re_json.element("data", orders);//将数组添加到对象中
				return re_json;
			}
			else if(location.table_name.equals(CheckAcManage.RES_PAYCACHE)){
				logger.info("查看预付款记录历史" + "owner=" + owner.work_id);
				list = dao_List.pCDao.GetPayRecordsTb(owner.work_id);
				logger.info(list.size());
			}
			else {
				logger.error("未知查看类型");
			}
			break;
		case 'S'://查看单条记录
			break;
		default:
			System.out.println("unknow type" + location.watch_type);
			break;
		}
		
		re_json.element("data", list);
		if (num % 10 > 0) {//计算总页数
			re_json.element("totalpage", num/10 + 1);
		}
		else {
			re_json.element("totalpage", num/10);
		}
		return re_json;
		//return list;
	}

	/*寻找匹配及确定唯一匹配功能*/
	public List<BankInput> Map(Map_Object map_Object,String owner){
		List<BankInput> resultMapp = null;
		
		if (map_Object.map_opString.equals("find_map")) {//查找和付款记录相关的出纳记录
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, map_Object.pay_id);//获取被操作付款记录的id
			resultMapp = auccount.MappPayToBank(pRecord.getId(),owner);
		}
		else if (map_Object.map_opString.equals("cer_map")) {//获取唯一满足条件的出纳记录
			BankInput bInput = dao_List.bDao.findById(BankInput.class, map_Object.bank_id);
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, map_Object.pay_id);
			
			if (pRecord.getIsconnect() == true) {//如果之前已经绑定出纳
				auccount.CancelConnecttBWithP(pRecord.getId(), pRecord.getBankinputId());//取消之前的绑定信息，包括付款记录及出纳记录	
			}
			auccount.ConnectBankWithPay(map_Object.pay_id, map_Object.bank_id,pRecord.getContractNum(),pRecord.getManyPay());
			
			resultMapp = new ArrayList<BankInput>();
			resultMapp.add(0, bInput);	
		}
		else if (map_Object.map_opString.equals("cancel_map")) {
			auccount.CancelConnectBkAndPay(map_Object.pay_id, map_Object.bank_id);
		}
		else {
			System.out.println("[Map]:unknow map_op:" + map_Object.map_opString);
			
		}
		
		return resultMapp;
	}
	
	/*审阅功能*/
	public void Check(int id,String check_op){
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, id);//获取被操作付款记录的id
			
			if (check_op.equals("yes")) {
				
			}
			else if(check_op.equals("no")){
				
			}
	}
	
	/*开始对账功能*/
	public JSONObject StartCheckWork(String owner){
		JSONObject jmesg = new JSONObject();
		jmesg.element("flag", -1);
		int flag = -1;
		if(auccount.IsStartCheckWork(owner).getInt("flag") == -1){
			jmesg.element("flag", -1);
			jmesg.element("errmsg", auccount.IsStartCheckWork(owner).getString("errmsg"));
			return jmesg;
			//return -1;
		}
		else if (auccount.IsStartCheckWork(owner).getInt("flag") ==0){//开启对账，出纳记录逐条关联到货款表
			List lbank = dao_List.bDao.GetTolBankInsByElement("owner",owner);
			
			System.out.println(lbank.size());
			for (int i = 0; i < lbank.size(); i++) {
				BankInput bInput = (BankInput) lbank.get(i);
				logger.info("***开启" + bInput.getId() + "出纳记录的关联***");
				if (bInput.getIsConnect() == true) {//使用合同号去关联货款表
					logger.info("使用合同号去关联出纳" + bInput.getPayer());
					jmesg = auccount.ConnectBankWithAccount(bInput,owner);
					if ((int)jmesg.get("flag") == -1) {
						logger.info("使用合同号去关联出纳" + bInput.getPayer() + "失败");
					}
				}
				
				if ((int)jmesg.get("flag") == -1) {//使用客户名称去关联货款表
					CusSecondstore fcustom = dao_List.sDao.findById(CusSecondstore.class, bInput.getPayer());//改成用出纳记录中的绑定的手机用户的单位id或者身份证去找，同时客户表中的主键改成货款表中客户的id
					if (fcustom == null) {
						logger.warn("找不到付款人为：" + bInput.getPayer() + "的客户");
					}
					
					else {
						if (fcustom.getOwner().equals(owner)) {//将代理商个人信息也作为判断条件
							if(fcustom.getContractMes() == null){//没有合同信息
								logger.info(fcustom.getClient() + "无合同信息");
								auccount.ConnectBankWithCustom(bInput,bInput.getPayer(),bInput.getMoney());
							}
							else {
								if (fcustom.getContractNum() <= 1) {
									logger.info(fcustom.getClient() + "只有一个货款性质,直接关联到货款表");
									String paymentNature = JSONArray.fromObject(fcustom.getContractMes()).getString(0);
									auccount.ConnectBankWithAccount_Only(bInput,paymentNature);//如果客户只有一个合同或者只有一个合同欠款，用该合同号去关联
								}
								else {//否则出纳记录关联到客户名下
									logger.info("关联" + bInput.getId() + ":" + bInput.getPayer() + "出纳记录到客户名下");
									auccount.ConnectBankWithCustom(bInput,bInput.getPayer(),bInput.getMoney());
								}
							}
	
						}
						else{
							logger.warn("出纳记录的代理商和客户名下的代理商信息不一致：");
						}						
					}
				}
				logger.info("***结束" + bInput.getId() + "付款记录的关联***");
			}
			
			jmesg.element("flag", 0);
			jmesg.element("errmsg", "对账成功");
			return jmesg;
		}
		else {
			jmesg.element("flag", -5);
			jmesg.element("errormsg", auccount.IsStartCheckWork(owner).getString("errormsg"));
			return jmesg;
		}
	}

	
	/*对账后查看对账结果*/
	public Object Watch_CheckAResult(Watch_CAResObject wCaResObject,String owner){
		List<BankInput> fBankInputs = null;
		List<PayRecord> fPayRecords = null;
		List<OriOrder> fOrders = null;
		if (wCaResObject.watch_restype.equals("bfailconnect")) {//结果1：无法关联的合同号或者客户名下的出纳记录
			fBankInputs = cARseult.Produce_BInputFailConnect(owner);
			cARseult.Test_ResultB("FailConnect",fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("btocontract")) {//结果2：关联到合同号的出纳记录
			fBankInputs = cARseult.Produce_BInputToContract(owner);
			cARseult.Test_ResultB("btocontract", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("btoclient")) {//结果3：关联到客户名下的出纳记录
			fBankInputs = cARseult.Produce_BInputToClient(owner);
			cARseult.Test_ResultB("btoclient", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("bnopay")) {//结果4：没有关联到付款信息的出纳记录(客户没有上传付款信息)
			fBankInputs = cARseult.Produce_BInputNoPayRecord(owner);
			cARseult.Test_ResultB("bnopay", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("bhaspay")) {//结果5：关联到付款信息的出纳记录
			fBankInputs = cARseult.Produce_BInputHasPayRecord(owner);
			cARseult.Test_ResultB("bhaspay", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("phasbinput")) {//结果6：关联到出纳信息的手机付款记录
			fPayRecords = cARseult.Produce_PayHasBInput(owner);
			cARseult.Test_ResultP("phasbinput", fPayRecords);
			return fPayRecords;
		}
		else if (wCaResObject.watch_restype.equals("truepnobinput")) {//结果7：没有关联到出纳信息的真实手机付款记录(催款)
			fPayRecords = cARseult.Produce_TruePayNoBInput(owner);
			cARseult.Test_ResultP("truepnobinput", fPayRecords);
			return fPayRecords;
		}
		else if (wCaResObject.watch_restype.equals("falsepnobinput")) {//结果8：没有关联到出纳信息的真实手机付款记录(垃圾信息)
			fPayRecords = cARseult.Produce_FalsePayNoBInput(owner);
			cARseult.Test_ResultP("falsepnobinput", fPayRecords);
			return fPayRecords;
		}
		else if(wCaResObject.watch_restype.equals("onobinput")){//结果9：本月没有收到付款的货款记录
			fOrders = cARseult.Produce_OriorderNoBInput(owner);
			cARseult.Test_ResultO("onobinput",fOrders);
			return fOrders;
		}
		else if(wCaResObject.watch_restype.equals("ohasbinput")){//结果10：本月有收到付款的货款记录
			fOrders = cARseult.Produce_OriorderHasBInput(owner);
			cARseult.Test_ResultO("onobinput",fOrders);
			return fOrders;
		}
		//添加更多的对账结果
		else{
			System.out.println("unknow request of watch result_type");
			return null;
		}
	}
	
	/*取消并重新对账*/
	public Object  CancelAndCaAgain(String owner,String caid){
		int flag = -1;
		auccount.CancelAndCaAgain(owner, caid);
		
		flag = 0;
		return flag;
	}
	
	public class Map_Object{
		public String map_opString;
		public int pay_id;
		public int bank_id;
		
		public Map_Object(String map_opString,int pay_id){
			this.map_opString = map_opString;
			this.pay_id = pay_id;
		}
		public Map_Object(String map_opString,int pay_id,int bank_id) {
			// TODO Auto-generated constructor stub
			this.map_opString = map_opString;
			this.pay_id = pay_id;
			this.bank_id = bank_id;
		}
	}
	
	public class Import_Object{
		public 	String operator;//操作者
		public char import_type;
		public MultipartFile file;
		public String savepath;//保存目录
		public String filename;
		public String caid;
		
		public Import_Object(char import_type,MultipartFile file,String operator,String savepath,String filename,String caid) {
			this.import_type = import_type;
			this.file = file;
			this.operator = operator;
			this.savepath = savepath;
			this.filename = filename;
			this.caid = caid;
		}
	}
	
	public class Watch_Object{//order_num和id改成只能存在某一个的结构
		public char watch_type;
		public String table_name;
		public String order_num;//原始账单主键
		public int id;//待办，有误及未关联出纳主键
		public String cayear;//对账历史结果的年份
		public Watch_Object(){
		
		}
	}
	
	public Watch_Object Create_Watch_Object(JSONObject jstr){
		Watch_Object wObject = new Watch_Object();//设置查看的参数
		wObject.watch_type = (char) jstr.getString("watch_type").charAt(0);//设置查看的类型,前台传参
		wObject.table_name = jstr.getString("table_name");//查看资源名称,前台传参
		wObject.order_num = null;//前台传参
		wObject.id = -1;//前台传参
		if (jstr.has("year")) {
			wObject.cayear = (String) jstr.getString("year");	
		}
		return wObject;
	}
	
	public class Dao_List{
		public Total_Account_Dao tDao;//创建总账单业务
		public PayRecord_Dao pDao;
		public BankInput_Dao bDao;
		public SendStore_Dao sDao;
		public Assistance_Dao aDao;
		public PayRecordHistory_Dao pHDao;
		public CaresultHistory_Dao cDao;
		public PayRecordCache_Dao pCDao;
		public Agent_Dao agent_Dao;
		public ConnectPerson_Dao cPerson_Dao;
		public Ori_BackUp_Dao oUp_Dao;
		public BInput_Backup_Dao bInput_Backup_Dao;
		public CusSdStore_Backup_Dao cBackup_Dao;
		public OpLog_Dao opLog_Dao;
		
		public Dao_List(SessionFactory wFactory){
			  tDao = new Total_Account_Dao(wFactory); 
			  pDao = new PayRecord_Dao(wFactory);
			  bDao = new BankInput_Dao(wFactory);
			  sDao = new SendStore_Dao(wFactory);
			  aDao = new Assistance_Dao(wFactory);
			  pHDao = new PayRecordHistory_Dao(wFactory);
			  cDao = new CaresultHistory_Dao(wFactory);
			  pCDao = new PayRecordCache_Dao(wFactory);
			  agent_Dao = new Agent_Dao(wFactory);
			  cPerson_Dao = new ConnectPerson_Dao(wFactory);
			  oUp_Dao = new Ori_BackUp_Dao(wFactory);
			  bInput_Backup_Dao = new BInput_Backup_Dao(wFactory);
			  cBackup_Dao = new CusSdStore_Backup_Dao(wFactory);
			  opLog_Dao = new OpLog_Dao(wFactory);
		}
	}
	
	public class Watch_CAResObject{
		public String watch_restype;
		public String operator;
		public  Watch_CAResObject(String watch_restype,String operator){
			this.watch_restype = watch_restype;
			this.operator = operator;
		}
	}
	
	public class Export_CAResObject{
		public String dir;
		public String filename;
		public List<String> exportlist;
		
		public Export_CAResObject(String dir,List<String> exportlist,String filename){
			this.dir = dir;
			this.exportlist = exportlist;
			this.filename = filename;
		}
	}
	
	public class Owner{
		public String work_id;
		public String user_type;
		public Owner() {
	}
		// TODO Auto-generated constructor stub
	}
}
	

