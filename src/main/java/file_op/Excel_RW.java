package file_op;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.SessionFactory;

import com.mysql.fabric.xmlrpc.base.Data;
import com.sun.org.apache.bcel.internal.generic.ReturnaddressType;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import antlr.collections.List;
import check_Asys.FormProduce.Export_Binput;
import check_Asys.FormProduce.Export_PayRecord;
import check_Asys.FormProduce.Export_TotalOriForm;
import controller.FormManagerController.ExcelFileAtr;
import controller.FormManagerController.OwerAtr;
import dao.Total_Account_Dao;
import entity.Agent;
import entity.BankInput;
import entity.OriOrder;
import entity.OriOrderId;
import entity.PayRecord;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Excel_RW 读写excel的接口
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class Excel_RW {
	private static Logger logger = LogManager.getLogger(Excel_RW.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public Sheet wsheet;
	public Sheet rsheet;
	
	public XSSFWorkbook wXssfWorkbook;
	public XSSFWorkbook rXssfWorkbook;
	
	public  Excel_RW() {
		// TODO Auto-generated constructor stub
//		table = new ArrayList<Excel_Row>();
	//	wXssfWorkbook = new XSSFWorkbook();
	}	
	
	public  Sheet CreateWSheet(String sheetname){
		Sheet sheet = wXssfWorkbook.createSheet(sheetname);
		return sheet;
	}
	
	public static java.util.List<String> WriteExcel(ExcelFileAtr eAtr,OwerAtr oAtr,SessionFactory wFactory){
		
		String dirname = eAtr.dirname;
		String filename = eAtr.filename;
		String sheetname = eAtr.sheetname;
		
		String work_id = oAtr.work_id;
		String userType = oAtr.userType;
		
		/*创建保存目录*/
		File dir = CreateDir(dirname);
		/*创建保存目录*/

		File file = CreateFile(dirname,filename);
		
		/*创建sheet*/
		FileOutputStream out = null;
		Sheet sheet1 = null;
		XSSFWorkbook workbook = null;
		try {	
			 workbook = new XSSFWorkbook();	
			 sheet1 = workbook.createSheet(sheetname);
			/*设置表格内容*/
			/*设置表头*/
			SetTotalAHead(workbook,sheet1);
			/*设置表头*/
			
			/*设置表格体内容*/
			java.util.List list_at = ChooseAccout(userType,work_id,wFactory);//根据用户类型，选择相应的报表
			
			for(int i = 0;i<list_at.size();i++){
				Row row_body = sheet1.createRow(i+1);
				OriOrder order = (OriOrder) list_at.get(i);
				
				/*设置对应项的内容*/
				SetTotalABody(row_body,order,workbook);
				/*设置对应项的内容*/
			}
			/*设置表格体内容*/
			/*设置表格内容*/
			
			
			out = new FileOutputStream(file);
			workbook.write(out);

		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*创建sheet*/
		
		java.util.List<String> result = new ArrayList<String>();
		result.add("总账单");
		result.add(CreateLink("报表中心", filename));
		return result;
	}
	
	public static File CreateDir(String dirname) {
		/*判断文件夹是否存在*/
		File dir = new File(dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			System.out.println(dirname + "不存在");
			dir.mkdir();
		}
		/*判断文件夹是否存在*/
		
		return dir;
	}
	
	public static File CreateFile(String dirname,String filename) {
		/*判断文件是否存在*/
		boolean exitflag = true;
		File file = new File(dirname + "/" + filename);
		if (!file.exists()) {
			exitflag = false;
			System.out.println(filename + "don't exit"); 
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{//重新创建,在linux下是否存在权限问题
			System.out.println(filename + "delet and create again");
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*判断文件是否存在*/
		return file;
	}
	
	public static void SetCellStyle(XSSFWorkbook workbook,Cell cell){
		/*设置单元格式*/
		CellStyle tStyle = workbook.createCellStyle();
		tStyle.setAlignment(CellStyle.ALIGN_CENTER);
		tStyle.setBorderBottom(CellStyle.BORDER_THIN);
		tStyle.setBorderLeft(CellStyle.BORDER_THIN);
		tStyle.setBorderRight(CellStyle.BORDER_THIN);
		tStyle.setBorderTop(CellStyle.BORDER_THIN);
		cell.setCellStyle(tStyle);
		/*设置单元格式*/
	}
	
	public static void SetTotalABody(Row row_body,OriOrder order,XSSFWorkbook workbook){
		/*设置对应项的内容*/
		Cell cell_body = null;
		cell_body = row_body.createCell(0);
		cell_body.setCellValue(order.getOrderNum());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(1);
		cell_body.setCellValue(order.getInput());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(2);
		cell_body.setCellValue(order.getDebt());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(3);
		cell_body.setCellValue(order.getTotal());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(4);
		cell_body.setCellValue(order.getClient());
		SetCellStyle(workbook,cell_body);
		/*设置对应项的内容*/
	}
	
	/*关于货款的导出都用这个函数*/
	public  void SetTotalAFormBody(java.util.List<OriOrder> list_at,Sheet sheet1){
		
		for(int i = 0;i<list_at.size();i++){
			Row row_body = sheet1.createRow(i+1);
			OriOrder order = list_at.get(i);
			
			/*设置对应项的内容*/
			Cell cell_body = null;
			cell_body = row_body.createCell(0);//分期销售设备编号/按揭、融资合同号
			cell_body.setCellValue(order.getNum());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("contract"));//分期销售设备编号/按揭、融资合同号
			cell_body.setCellValue(order.getOrderNum());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("inputMonth"));
			cell_body.setCellValue(order.getInput());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("debetMoney"));
			cell_body.setCellValue(order.getDebt());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("totalMoney"));
			cell_body.setCellValue(order.getTotal());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("clientName"));//设置客户名称
			cell_body.setCellValue(order.getClient());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("cusCompanyid"));//身份证号码/组织机构代码证
			cell_body.setCellValue(order.getId().getCuscompanyid());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("productTime"));//发货时间
			cell_body.setCellValue(order.getProductTime());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("paymentOwner"));//货款主体
			cell_body.setCellValue(order.getOwnerProduct());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("province"));//财务人员电话
			cell_body.setCellValue(order.getProvince());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("paymentNature"));//财务人员电话
			cell_body.setCellValue(order.getId().getPaymentNature());
			SetCellStyle(wXssfWorkbook,cell_body);
/*			cell_body = row_body.createCell(Ori_Excel_Format.cp_weixin);//对账联系人微信
			cell_body.setCellValue(order.getCustomweixin());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_name);//对账联系人姓名
			cell_body.setCellValue(order.getCustomname());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_phone);//对账联系人电话
			cell_body.setCellValue(order.getCustomphone());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_cardid);//对账联系人身份证
			cell_body.setCellValue(order.getCuscompanyid());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_name);//财务人员姓名
			cell_body.setCellValue(order.getAsname());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_email);//财务人员邮箱
			cell_body.setCellValue(order.getAsemail());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_phone);//财务人员电话
			cell_body.setCellValue(order.getAsphone());
			SetCellStyle(wXssfWorkbook,cell_body);
			
*/
			
			/*设置对应项的内容*/
		}
	}
	
	/*关于付款信息的导出都用这个函数*/
	public void SetPayPecordFormBody(java.util.List<PayRecord> list_nbp,Sheet sheet1){
		for (int i = 0; i < list_nbp.size(); i++) {
			Row row_body = sheet1.createRow(i+1);
			
			PayRecord payRecord = list_nbp.get(i);
			
			Cell cell_body = null;
			cell_body = row_body.createCell(Export_PayRecord.payer);
			cell_body.setCellValue(payRecord.getPayer());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.paymoney);
			cell_body.setCellValue(payRecord.getPayMoney());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.payway);
			cell_body.setCellValue(payRecord.getPayWay());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.receiver);
			cell_body.setCellValue(payRecord.getReceiver());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.connectperson);
			cell_body.setCellValue(payRecord.getConnPerson());
			SetCellStyle(wXssfWorkbook,cell_body);
		}
	}
	
	public void SetBinputFormBody(java.util.List<BankInput> list_binput,Sheet sheet1){
		for (int i = 0; i < list_binput.size(); i++) {
			Row row_body = sheet1.createRow(i+1);
			
			BankInput bInput = list_binput.get(i);
			
			Cell cell_body = null;
			cell_body = row_body.createCell(Export_Binput.payer);
			cell_body.setCellValue(bInput.getPayer());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.paymoney);
			cell_body.setCellValue(bInput.getMoney());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.payway);
			cell_body.setCellValue(bInput.getPayWay());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.payee);
			cell_body.setCellValue(bInput.getPayee());
			SetCellStyle(wXssfWorkbook,cell_body);
			
		}
	}
	
	public static void SetTotalAHead(XSSFWorkbook workbook,Sheet sheet1){
		Row row = sheet1.createRow(0);
		ArrayList <String> list = new ArrayList<String>();
		list.add("订单号");
		list.add("已收入");
		list.add("在外金额");
		list.add("销售总额");
		list.add("客户信息");
		for(int i = 0;i<list.size();i++){
			Cell cell = row.createCell(i);
			cell.setCellValue(list.get(i));
			SetCellStyle(workbook,cell);
		}
	}
	
	public void SetFormHead(Sheet sheet,java.util.List<String> formhead){
		Row row = sheet.createRow(0);

		for(int i = 0;i<formhead.size();i++){
			Cell cell = row.createCell(i);
			cell.setCellValue(formhead.get(i));
			SetCellStyle(wXssfWorkbook,cell);
		}
	}
	
	public static String CreateLink(String dirname,String filename){
		String links = null;
		InetAddress iAddress = null;
		try {
			iAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ip = iAddress.getHostAddress();
		links = "http://" + ip + ":8080" + "/check_Accout/" + dirname + "/" + filename;
		return links;
	}
	
	public static java.util.List<OriOrder> ChooseAccout(String userType,String work_id,SessionFactory wSeFactory) {
		Total_Account_Dao tDao = new Total_Account_Dao(wSeFactory);
		java.util.List list_at = null;
		if (userType.equals("bm")) {
			list_at = tDao.GetTolAccount();
		}
		else if (userType.equals("bu")) {
			list_at = tDao.FindBySpeElement_S("owner", work_id);
		}
		
		return list_at;
	}
	
	public static void ReadExcel(String filename) {
		try {
			FileInputStream file = new FileInputStream(new File(filename));
			BufferedInputStream in = new BufferedInputStream(file);
		//	POIFSFileSystem fs = new POIFSFileSystem(in);
			
			XSSFWorkbook wb = new XSSFWorkbook(file);
			XSSFSheet st = wb.getSheetAt(0);
			
			Iterator<Row> rowIterator = st.iterator();
			while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                
                while (cellIterator.hasNext()) 
                {
                    Cell cell = cellIterator.next();
                    //Check the cell type and format accordingly
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                        	if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
                       
                        		Date date =	cell.getDateCellValue();
                        		String value = new SimpleDateFormat("yyyy-MM-dd").format(date);          
                        		System.out.print(value);
                        	}
                        	else
                        		System.out.print(cell.getNumericCellValue() + "t");
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "t");
                            break;
                        default:
                        	System.out.println("unknow format");
                        	break;
                    }
                }
                System.out.println("");
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Excel_Row> ReadExcel_Table(InputStream inputStream) {
		try {
			
			ArrayList<Excel_Row> excel_table = new ArrayList<>();
			
			XSSFWorkbook wb = new XSSFWorkbook(inputStream);
			XSSFSheet st = wb.getSheetAt(0);
			
			Iterator<Row> rowIterator = st.iterator();
			while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                
                String cell_svalue;//保存从excel中读出的一个单元的值
                Excel_Row eRow = new Excel_Row(); //保存从excel中读出的一行的值
                
                while (cellIterator.hasNext())//循环读取excel中的一行 
                {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                        	if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        		Date date =	cell.getDateCellValue();
                        		cell_svalue = new SimpleDateFormat("yyyy/MM/dd").format(date); 
                        		logger.info("time:" + cell.getDateCellValue());
                        	}
                        	else{
                        		logger.info("num:" + cell.getNumericCellValue());
                        		cell_svalue = Double.toString(cell.getNumericCellValue());
                        	}                        		
                            break;
                        case Cell.CELL_TYPE_STRING:
                        	cell_svalue = cell.getStringCellValue();
                            logger.info("string" + cell.getStringCellValue());
                            break;
                        default:
                        	cell_svalue = null;
                        	logger.error("unknow format");
                        	break;
                    }
                    
            //        if (cell_svalue != null) {
						eRow.list.add(cell_svalue);
			//		}  
                }
         //       table.add(eRow);
                excel_table.add(eRow);
            }

			return excel_table;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
	}

	//将excel表中的一行解析为OriOrder中的各个成员
	public JSONObject Row_To_Ob_OriOrder(int row_id,ArrayList<Excel_Row> table,Agent agent){
		logger.info("Row_To_Ob at : " + (row_id+1) + "行");
		JSONObject jsonObject = new JSONObject();
		String errmsg = "";
		OriOrder order = new OriOrder();
		jsonObject.element("order", order);
		Excel_Row re_row = table.get(row_id);
		
		String num_s = re_row.list.get(0);
		if (num_s == null) { 
			errmsg = "序号为主键,不能为空";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		
		String paymentNature = re_row.list.get(Ori_Excel_Format.paymentNature.compareTo(Ori_Excel_Format.num));
		String cuscompanyid = re_row.list.get(Ori_Excel_Format.cusCompanyid.compareTo(Ori_Excel_Format.num));
		String clientName = re_row.list.get(Ori_Excel_Format.clientName.compareTo(Ori_Excel_Format.num));
		if (clientName == null ) {
			errmsg = "导入货款表格式有误：" + (row_id+1) + "行用户名为空";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		if (paymentNature == null) {
			errmsg = "导入货款表格式有误：" + (row_id+1) + "行货款性质为空";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		if (cuscompanyid == null) {
			errmsg = "导入货款表格式有误：" + (row_id+1) + "行客户公司id为空";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		
		OriOrderId orderId = new OriOrderId();
		orderId.setCuscompanyid(cuscompanyid);
		orderId.setPaymentNature(paymentNature);
		//order.setPaymentNature(paymentNature);//设置货款主体
		order.setId(orderId);
		order.setClient(clientName);//设置客户名称
		//order.setCuscompanyid(re_row.list.get(Ori_Excel_Format.cusCompanyid.compareTo(Ori_Excel_Format.num)));//设置客户身份证或者组织机构代码证
		order.setOrderNum(re_row.list.get(Ori_Excel_Format.contract.compareTo(Ori_Excel_Format.num)));//设置合同号
		order.setProductTime(re_row.list.get(Ori_Excel_Format.productTime.compareTo(Ori_Excel_Format.num)));//发货时间
		order.setOwnerProduct(re_row.list.get(Ori_Excel_Format.paymentOwner.compareTo(Ori_Excel_Format.num)));//货款主体
		
		String totalMoney = re_row.list.get(Ori_Excel_Format.totalMoney.compareTo(Ori_Excel_Format.num));
		String debetMoney = re_row.list.get(Ori_Excel_Format.debetMoney.compareTo(Ori_Excel_Format.num));
		if (totalMoney == null || debetMoney == null) {
			errmsg = "导入货款表格式有误:第" + (row_id+1) + "行 总额或者在外金额为非数字类型";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		order.setNum(Double.valueOf(num_s).intValue());
		order.setTotal(Double.valueOf(totalMoney).doubleValue());//设置总额
		order.setDebt(Double.valueOf(debetMoney).doubleValue());//设置在外金额
		order.setOwner(agent.getAgentId());//设置货款记录所属者
		order.setAsname(agent.getAgentConnectpname());
		order.setAsphone(agent.getAgentCpphone());
		order.setAsemail(agent.getAgentCpemail());
		order.setProvince(re_row.list.get(Ori_Excel_Format.province.compareTo(Ori_Excel_Format.num)));
		order.setInput(0d);
		
		errmsg = "导入货款表成功";
		jsonObject.element("errmsg", errmsg);
		jsonObject.element("flag", 0);
		jsonObject.element("OriOrder", order);
		return jsonObject;
	}
	
	//将整个货款excel表解析为OriOrders
	public JSONObject Table_To_Ob_OriOrders(ArrayList<Excel_Row> excel_table,Agent agent) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		//ArrayList<OriOrder> orders = new ArrayList<OriOrder>();
		
		for (int i = 1; i < excel_table.size(); i++) {
			JSONObject re_js = Row_To_Ob_OriOrder(i,excel_table,agent);

			if (re_js.getInt("flag") == 0) {
				//OriOrder in_ori = (OriOrder) re_js.get("OriOrder");
				OriOrder in_ori = (OriOrder) JSONObject.toBean(re_js.getJSONObject("OriOrder"), OriOrder.class);
				//orders.add(in_ori);
				jsonArray.add(in_ori);
			}
			else {
				logger_error.error("获取货款表的第" + (i+1) + "行数据失败");
				jsonObject = re_js;
				return jsonObject;
			}
		}
		
		jsonObject.element("flag", 0);
		jsonObject.element("orders", jsonArray);
		return jsonObject;
	}
	
	//将excel表中的一行解析为BankInput中的各个成员
	public BankInput Row_To_Ob_BankIn(int row_id,ArrayList<Excel_Row> table,String ownerid) {
		logger.info("Row_To_Ob at:" + (row_id+1) + "行");
		
		BankInput bInput = new BankInput();
		Excel_Row eRow = table.get(row_id);
		
		String num_s = eRow.list.get(0);
		bInput.setNum(Double.valueOf(num_s).intValue());//设置序号
		bInput.setPayee(eRow.list.get(NameToValue_BankInput("inAccountName")));//设置收款人名称
		bInput.setPayWay(eRow.list.get(NameToValue_BankInput("inWay")));//设置收款方式
		bInput.setInputTime(eRow.list.get(NameToValue_BankInput("inTime")));//设置到帐日期
		bInput.setActualPayer(eRow.list.get(NameToValue_BankInput("actualPayer")));//设置实际付款人
		
		Double inMoney = Double.valueOf(eRow.list.get(NameToValue_BankInput("inMoney"))).doubleValue();
		bInput.setMoney(inMoney);//设置收款金额
		
		bInput.setPayer(eRow.list.get(NameToValue_BankInput("client")));//设置付款人名称
		bInput.setPayerAccount(eRow.list.get(NameToValue_BankInput("outAccount")));//设置付款人帐号
		bInput.setPaymentNature(eRow.list.get(NameToValue_BankInput("paymentNature")));
		bInput.setStatus(false);
		bInput.setIsConnect(false);
		
		bInput.setConnectClient("");
		bInput.setConnectNum(0);
		bInput.setOwner(ownerid);
		return bInput;
	}

	//将整个付款excel表解析为BankInputs
	public ArrayList<BankInput> Table_To_Ob_BankIn(ArrayList<Excel_Row> excel_table,String owner_id) {
		ArrayList<BankInput> bankInputs = new ArrayList<BankInput>();
		
		for (int i = 1; i < excel_table.size(); i++) {
			bankInputs.add(Row_To_Ob_BankIn(i, excel_table,owner_id));
		}
		
		return bankInputs;
	}

	public void Test_Table(ArrayList<Excel_Row> excel_table){
		for (int i = 0; i < excel_table.size(); i++) {
			Excel_Row excel_Row = excel_table.get(i);
			excel_Row.Test_list();
		}
	}
	
	public class Excel_Row{
		public ArrayList<String> list;
		public Excel_Row() {
			list = new ArrayList<String>();
	}
		
	public void Test_list(){
		for(int i = 0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}
		// TODO Auto-generated constructor stub
	}
	
	//货款excel表的格式
/*	public class Ori_Excel_Format{
		public static final int province = 0;//省份
		public static final int client_name = 1;//客户名称
		public static final int client_id = 2;//客户身份证或组织机构代码证
		public static final int contract = 3;//设备销售编号/合同号
		public static final int product_time = 4;//发货时间
		public static final int owner_product = 5;//货款主体
		public static final int total_money = 6;//合同总额
		public static final int debet_money = 7;//在外货款金额
		public static final int as_name = 8;//代理商管理员
		public static final int as_phone = 9;//代理商电话
		public static final int as_email = 10;//代理商邮箱
		public static final int cp_name = 11;//对账联系人
		public static final int cp_cardid = 12;//对账联系人身份证
		public static final int cp_phone = 13;//对账联系人移动电话
		public static final int cp_weixin = 14;//对账联系人微信
		public static final int input = 15;//本月回款
	}*/
	
	public enum Ori_Excel_Format{
		num,//序号
		province,//省份
		clientName,//客户名称
		cusCompanyid,//客户id
		dingdanNum,//订单号
		productName,//产品名称
		facilityName,//设备名称
		contract,//合同号
		productTime,//发货时间
		paymentOwner,//货款主体
		paymentNature,//货款性质
		inputNature,//回款性质
		totalMoney,//合同总额
		debetMoney,//在外金额
		actualPayer,//实际付款人
		payway,//付款方式
		inputMonth,//本月回款
		inputClient//客户回款
	}
	
	public static int NameToValue_Ori(String name){
		return Ori_Excel_Format.valueOf(name).compareTo(Ori_Excel_Format.num);
	}
	
	//收款excel表的格式
/*	public class BankInput_Excel_Format{
		public static final int in_account_name = 1;//收款帐号名称
		public static final int in_account_num = 2;//收款帐号
		public static final int in_way = 3;//收款方式
		public static final int in_linkcel_num = 4;//收款交易号
		public static final int in_Time = 5;//到帐日期
		public static final int in_money= 6;//到帐金额
		public static final int out_account_name = 7;//付款帐号名称
		public static final int out_account_num = 8;//付款帐号
	}*/
	
	public enum BankInput_Excel_Format{
		num,//序号
		province,//省份
		inAccountName,//收款账户
		outAccount,//付款账户
		actualPayer,//实际付款人
		inTime,//到帐日期
		inWay,//付款方式
		piaohao,//票号
		inMoney,//收款金额
		client,//法定客户名称
		paymentNature,//货款性质
		partMoney,//拆分金额
		remark//收款摘要
	}
	
	public static int NameToValue_BankInput(String name){
		return BankInput_Excel_Format.valueOf(name).compareTo(BankInput_Excel_Format.num);
	}
	
	public static class My_Date{
		public My_Date() {
	}
		// TODO Auto-generated constructor stub	
		public  static Date StringToDate(String date_s){
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sFormat.parse(date_s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static String DateToString(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date_s = sdf.format(date);
		return date_s;
	}
	}
}
