package com.epis.dao.cashbookDummy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.epis.bean.admin.UserBean;
import com.epis.bean.cashbook.HdfcBean;
import com.epis.bean.cashbookDummy.AccountingCodeInfo;
import com.epis.bean.cashbookDummy.BankBook;
import com.epis.bean.cashbookDummy.VoucherDetails;
import com.epis.bean.cashbookDummy.VoucherInfo;
import com.epis.common.exception.EPISException;
import com.epis.dao.admin.UserDAO;
import com.epis.service.cashbookDummy.EmployeeService;
import com.epis.utilities.Authentication;
import com.epis.utilities.DBUtility;
import com.epis.utilities.Log;
import com.epis.utilities.StringUtility;
import com.epis.utilities.UserTracking;

public class VoucherDAO {

	Log log = new Log(VoucherDAO.class);


	public String addVoucherRecord(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : addVoucherRecord() entering method");
		Connection conn = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String approval=null;
		try {
			conn = DBUtility.getConnection();
			conn.setAutoCommit(false);
			if("A".equals(info.getAppStatus())|| "".equals(StringUtility.checknull(info.getAppStatus())))
			{
				approval="N";
			}
			else
				approval=info.getAppStatus();
			
			
			pst = conn.prepareStatement(getQueries("keyNoGenQuery"));
			pst.setString(1, info.getPreparedDt());
			pst.setString(2, info.getPreparedDt());
			rs = pst.executeQuery();
			while (rs.next()) {
				info.setKeyNo(rs.getString(1));
			}
		} catch (Exception e) {
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			pst = conn.prepareStatement(getQueries("insertQuery"));
			pst.setString(1, info.getKeyNo());
			pst.setString(2, info.getAccountNo());
			pst.setString(3, info.getFinYear());
			pst.setString(4, info.getTrustType());
			pst.setString(5, info.getVoucherType());
			pst.setString(6, info.getPartyType());
			pst.setString(7, info.getEmpPartyCode());
			 pst.setString(8, info.getLoginUserId());
			pst.setString(9, info.getDetails());
			pst.setString(10, info.getPreparedDt());
			pst.setString(11, info.getEmpCpfacno());
			pst.setString(12, info.getEmpRegion());
			pst.setString(13, info.getPfidFlag());
			pst.setString(14, info.getEmployeeName());
			pst.setString(15, info.getTransactionType());
			pst.setString(16, info.getLoginUserId());
			pst.setString(17, info.getLoginUnitCode());
			pst.setString(18, info.getEmpUnitName());
			pst.setString(19, info.getOtherTransactionType());
			pst.setString(20, info.getTransactionId());
			pst.setString(21,info.getPurpose());
			pst.setString(22,approval);
			pst.setString(23,info.getISIN());
			pst.executeUpdate();

		} catch (Exception e) {
			conn.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		UserTracking
				.write(info.getLoginUserId(), info.getAccountNo() + "-"
						+ info.getEmpPartyCode() + "-" + info.getPartyType()
						+ "-" + info.getVoucherType() + "-"
						+ info.getTrustType() + "-" + info.getFinYear(), "S",
						"CB", info.getKeyNo(), "Voucher Info");
		try {
			if(!"".equals(StringUtility.checknull(info.getOtherTransactionType()))){
				pst = conn.prepareStatement(getQueries(("FS".equals(info.getOtherTransactionType()) || "FSA".equals(info.getOtherTransactionType()))?"fsQuery":"pfwcpfQuery"));
				pst.setString(1, info.getTransactionId());
				pst.executeUpdate();
			}
		} catch (Exception e) {
			conn.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;
		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			pst = conn.prepareStatement(getQueries("insertDtQuery"));
			List voucherDts = info.getVoucherDetails();
			int length = voucherDts.size();
			for (int i = 0; i < length; i++) {
				VoucherDetails voucherDt = (VoucherDetails) voucherDts.get(i);
				pst.setString(1, info.getKeyNo());
				pst.setString(2, voucherDt.getAccountHead());
				pst.setString(3, voucherDt.getMonthYear());
				pst.setDouble(4, voucherDt.getCredit());
				pst.setString(5, voucherDt.getDetails());
				pst.setDouble(6, voucherDt.getDebit());
				pst.setString(7, voucherDt.getChequeNo());
				pst.setString(8, info.getLoginUserId());
				pst.setString(9, info.getLoginUnitCode());
				pst.setString(10, voucherDt.getRegard());
				pst.executeUpdate();
				UserTracking.write(info.getEnteredBy(), voucherDt
						.getAccountHead()
						+ "-"
						+ voucherDt.getMonthYear()
						+ "-"
						+ voucherDt.getCredit()
						+ "-"
						+ voucherDt.getDetails()
						+ "-"
						+ voucherDt.getDebit()
						+ "-"
						+ voucherDt.getChequeNo(), "S", "CB", info.getKeyNo(),
						"Voucher Details");
			}
		} catch (Exception e) {
			conn.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(info.getAppStatus().equals("R")&& "CPF".equals(info.getOtherTransactionType()))
		{
			
			try{
			String voucherupdate="update employee_advances_form set FINALTRANSSTATUS='R' where advancetransid='"+info.getTransactionId()+"'";
			pst=conn.prepareStatement(voucherupdate);
			pst.executeUpdate();
			}
			catch(Exception e)
			{
				conn.rollback();
				log.info("VoucherDAO :Exception :"+e.toString());
			}
			finally{
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			
		}
		try {
			if (!"C".equals(info.getVoucherType())
					&& "E".equals(info.getPartyType())
					&& !"true".equals(info.getPfidFlag())) {
				EmployeeService empService = new EmployeeService();
				empService.updateEmpDesig(info.getDesignation(), info
						.getEmpPartyCode());
				UserTracking.write(info.getEnteredBy(), info.getDesignation(),
						"U", "CB", info.getEmpPartyCode(),
						"Update Employee Desig. Details");
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				pst.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		log.info("VoucherDAO : addVoucherRecord() leaving method");
		return info.getKeyNo();
	}

	public List searchRecords(VoucherInfo info, String type) throws Exception {
		log.info("VoucherDAO : searchRecords : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List voucherInfo = new ArrayList();
		String query = null;
		try {
			con = DBUtility.getConnection();

			if ("edit".equals(type)) {
				pst = con.prepareStatement(getQueries("editQuery"));
				pst.setString(1, (info.getKeyNo() != null ? info.getKeyNo()
						: ""));

			} else if ("A".equals(type)) {
				if (info.getPreparedDt() != null
						&& !"".equals(info.getPreparedDt().trim())) {
					query = getQueries("selectAppQuery") + " and preperation_dt = to_date('"
							+ info.getPreparedDt() + "','dd/mon/yyyy')";
				} else {
					query = getQueries("selectAppQuery");
				}
				query = query + " order by voucher.ENTEREDDT";
				pst = con.prepareStatement(query);
				pst.setString(1, info.getBankName() + "%");
				pst.setString(2, info.getFinYear() + "%");
				pst.setString(3, info.getVoucherType() + "%");
				pst.setString(4, info.getAccountNo() == null ? "%" : info
						.getAccountNo().trim()
						+ "%");
				pst.setString(5, info.getKeyNo() == null ? "%" : info
						.getKeyNo().trim()
						+ "%");
			} else {
				query = getQueries("selectQuery");
				if (info.getVoucherDt() != null
						&& !"".equals(info.getVoucherDt().trim())) {
					query = query + " and voucher_dt = to_date('"
							+ info.getVoucherDt() + "','dd/mon/yyyy')";
				}
				if (info.getPreparedDt() != null
						&& !"".equals(info.getPreparedDt().trim())) {
					query = query + " and PREPERATION_DT = to_date('"
							+ info.getPreparedDt() + "','dd/mon/yyyy')";
				}
				if (info.getVoucherNo() != null
						&& !"".equals(info.getVoucherNo().trim())) {
					query = query
							+ " and upper(voucher.voucherno) like upper('"
							+ info.getVoucherNo() + "%') ";
				}
				query=query+" order by voucher.ENTEREDDT desc";
				pst = con.prepareStatement(query);
				pst.setString(1, info.getBankName() + "%");
				pst.setString(2, info.getFinYear() + "%");
				pst.setString(3, info.getVoucherType() + "%");
				pst.setString(4, info.getAccountNo() == null ? "%" : info
						.getAccountNo().trim()
						+ "%");
				pst.setString(5, info.getEmpPartyCode() == null ? "%" : info
						.getEmpPartyCode().trim()
						+ "%");
			}
						
			rs = pst.executeQuery();
			while (rs.next()) {
				info = new VoucherInfo();
				info.setKeyNo(rs.getString("KEYNO"));
				info.setBankName(rs.getString("BANKNAME"));
				info.setFinYear(rs.getString("FYEAR"));
				info.setVoucherType(rs.getString("VOUCHERTYPE"));
				info.setPartyType(rs.getString("PARTYTYPE"));
				info.setVoucherNo(rs.getString("voucherNo"));
				info.setPreparedDt(rs.getString("preperation_dt"));
				info.setAmount(rs.getString("Amount"));
				info.setVoucherDt(rs.getString("voucher_dt"));
				info.setTransactionId(rs.getString("TRANSID"));
				info.setVtype(rs.getString("vtype"));
				info.setISIN(rs.getString("isin"));
				voucherInfo.add(info);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : searchRecords : leaving method");
		return voucherInfo;
	}

	public VoucherInfo getReport(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getReport : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			
			con = DBUtility.getConnection();
			log.info("VoucherDAO : getReport : Report Parent Table");
			pst = con.prepareStatement(getQueries("reportQuery"));
			pst.setString(1, info.getKeyNo());
			rs = pst.executeQuery();
			if (rs.next()) {
				info = new VoucherInfo();
				info.setKeyNo(rs.getString("KEYNO"));
				info.setBankName(rs.getString("BANKNAME"));
				info.setAccountNo(rs.getString("accountNo"));
				info.setFinYear(rs.getString("FYEAR"));
				info.setVoucherType(rs.getString("VOUCHERTYPE"));
				info.setPartyType(rs.getString("PARTYTYPE"));
				info.setTrustType(rs.getString("trusttype"));
				info.setTransactionType(rs.getString("TRANSACTIONTYPE"));
				info.setPfid(rs.getString("partyDt"));
				info.setVtype(rs.getString("vtype"));
				if (rs.getString("emp_party_code") != null) {
					info.setEmpPartyCode(rs.getString("emp_party_code"));
				} else {
					info.setEmpPartyCode("");
				}
				info.setPartyDetails(rs.getString("partyDetails"));
				info.setVoucherNo(rs.getString("voucherno"));
				info.setCheckedBy(rs.getString("checkedby"));
				info.setApprovedBy(rs.getString("approvedby"));
				info.setDetails(rs.getString("details"));
				info.setVoucherDt(rs.getString("VOUCHER_DT"));
				info.setStatus(rs.getString("APPROVAL"));
				info.setPreparedDt(rs.getString("preperation_dt"));
				info.setPreparedBy(rs.getString("preparedby"));
				info.setOtherTransactionType(rs.getString("OTHERMODULELINK"));
				info.setISIN(rs.getString("ISIN"));
				//UserDAO.getInstance().getImage(,);
				log.info("::::::::::::::::::::::::"+info.getISIN());
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		try {
			log.info("VoucherDAO : getReport : Report Child Table");
			pst = con.prepareStatement(getQueries("reportDtQuery"));
			pst.setString(1, info.getKeyNo());
			rs = pst.executeQuery();
			List voucherDts = new ArrayList();
			VoucherDetails voucherDt = null;
			while (rs.next()) {
				voucherDt = new VoucherDetails();
				voucherDt.setAccountHead(rs.getString("ACCOUNTHEAD"));
				voucherDt.setParticular(rs.getString("PARTICULAR"));
				voucherDt.setMonthYear(rs.getString("MONTH_YEAR"));
				voucherDt.setDetails(rs.getString("details"));
				voucherDt.setChequeNo(rs.getString("chequeno"));
				voucherDt.setDebit(Double.parseDouble(rs.getString("debit")));
				voucherDt.setCredit(Double.parseDouble(rs.getString("credit")));
				voucherDt.setRegard(rs.getString("Regard"));
				voucherDt.setRowid(rs.getString("rowid"));
				voucherDts.add(voucherDt);
			}
			info.setVoucherDetails(voucherDts);
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getReport : leaving method");
		return info;
	}
	public List getMultipleVoucherData(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getMultipleVoucherData : Entering method");
		Connection con = null;
		List multiplevouchers=new ArrayList();
		PreparedStatement pst = null;
		PreparedStatement pstdt=null;
		ResultSet rs = null;
		ResultSet rsdt=null;
		String path="";
		try {
			path=info.getPath();
			con = DBUtility.getConnection();
			log.info("VoucherDAO : getReport : Report Parent Table");
			pst = con.prepareStatement(getQueries("multipleVoucherQuery"));
			pst.setString(1, info.getFinYear());
			pst.setString(2,info.getVoucherType());
			pst.setString(3,info.getFromVoucherNo());
			pst.setString(4,info.getToVoucherNo());
			pst.setString(5, info.getAccountNo() == null ? "%" : info
					.getAccountNo().trim()
					+ "%");
			
			rs = pst.executeQuery();
			while(rs.next()) {
				
				info = new VoucherInfo();
				info.setKeyNo(rs.getString("KEYNO"));
				info.setBankName(rs.getString("BANKNAME"));
				info.setAccountNo(rs.getString("accountNo"));
				info.setFinYear(rs.getString("FYEAR"));
				info.setVoucherType(rs.getString("VOUCHERTYPE"));
				info.setPartyType(rs.getString("PARTYTYPE"));
				info.setTrustType(rs.getString("trusttype"));
				info.setTransactionType(rs.getString("TRANSACTIONTYPE"));
				info.setPfid(rs.getString("partyDt"));
				if (rs.getString("emp_party_code") != null) {
					info.setEmpPartyCode(rs.getString("emp_party_code"));
				} else {
					info.setEmpPartyCode("");
				}
				info.setPartyDetails(rs.getString("partyDetails"));
				info.setVoucherNo(rs.getString("voucherno"));
				info.setCheckedBy(rs.getString("checkedby"));
				info.setApprovedBy(rs.getString("approvedby"));
				info.setDetails(rs.getString("details"));
				info.setVoucherDt(rs.getString("VOUCHER_DT"));
				info.setStatus(rs.getString("APPROVAL"));
				info.setPreparedDt(rs.getString("preperation_dt"));
				info.setPreparedBy(rs.getString("preparedby"));
				//UserDAO.getInstance().getImage(,);
				
				log.info("VoucherDAO : getReport : Report Child Table");
				pstdt= con.prepareStatement(getQueries("reportDtQuery"));
				log.info("the keyno is..."+info.getKeyNo());
				pstdt.setString(1, info.getKeyNo());
				rsdt = pstdt.executeQuery();
				List voucherDts = new ArrayList();
				VoucherDetails voucherDt = null;
				while (rsdt.next()) {
					voucherDt = new VoucherDetails();
					voucherDt.setAccountHead(rsdt.getString("ACCOUNTHEAD"));
					voucherDt.setParticular(rsdt.getString("PARTICULAR"));
					voucherDt.setMonthYear(rsdt.getString("MONTH_YEAR"));
					voucherDt.setDetails(rsdt.getString("details"));
					voucherDt.setChequeNo(rsdt.getString("chequeno"));
					voucherDt.setDebit(Double.parseDouble(rsdt.getString("debit")));
					voucherDt.setCredit(Double.parseDouble(rsdt.getString("credit")));
					voucherDt.setRegard(rsdt.getString("Regard"));
					voucherDt.setRowid(rsdt.getString("rowid"));
					voucherDts.add(voucherDt);
				}
				info.setVoucherDetails(voucherDts);
				String basePath="";
				UserBean user = UserDAO.getInstance().getUser(info.getApprovedBy());
				basePath =path+"/uploads/SIGNATORY/"+user.getEsignatoryName();
				UserDAO.getInstance().getImage(basePath,user.getUserName());
				multiplevouchers.add(info);
			}
		} catch (SQLException e) {
			log.info(e.toString());
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.info(e.toString());
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		
		log.info("VoucherDAO : getMultipleVoucherData : leaving method");
		return multiplevouchers;
	}

	public void updateApprovalVoucher(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : updateApprovalVoucher : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = DBUtility.getConnection();
			con.setAutoCommit(false);
			pst = con.prepareStatement(getQueries("updateQuery"));
			pst.setString(1, info.getVoucherDt());
			pst.setString(2, info.getCheckedBy());
			pst.setString(3, info.getStatus());
			pst.setString(4, info.getLoginUserId());
			pst.setString(5, getVoucherNo(info.getKeyNo()));
			pst.setString(6, info.getLoginUserId());
			pst.setString(7, info.getKeyNo());
			pst.executeUpdate();

		} catch (SQLException e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();				
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		try {
			info = getReport(info);
			List voucherDts = info.getVoucherDetails();
			int length = voucherDts.size();
			Statement stmt = con.createStatement();
			String existsRec = null;
			ResultSet rs = null;
			
			if("Payment".equals(info.getVoucherType()) && "E".equals(info.getPartyType())){
				
				for (int i = 0; i < length; i++) {
					String  query = null;
					String updatequery=null;
					VoucherDetails voucherDt = (VoucherDetails) voucherDts.get(i);
					if("672.03".equals(voucherDt.getAccountHead().trim())){
						query = " insert into employee_pension_advances (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,AMOUNT,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,keyno,ADVTRANSDATE,ADVPURPOSE)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),debit-credit,details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno,VOUCHER_DT,PURPOSETYPE from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
						if(query != null)
							stmt.executeUpdate(query);
					} else if("672.04".equals(voucherDt.getAccountHead().trim()) || "672.05".equals(voucherDt.getAccountHead().trim())){
						existsRec = "select count(*) from employee_pension_loans where keyno = '"+info.getKeyNo()+"' ";
						rs = stmt.executeQuery(existsRec);
						if(rs.next() && rs.getInt(1)>0){
							if("672.04".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_pension_loans set SUB_AMT = nvl(SUB_AMT,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"') where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							} else {
								query = " update employee_pension_loans set CONT_AMT = nvl(CONT_AMT,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"') where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							}
						}else {
							if("672.04".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_pension_loans (CPFACCNO,LOANDATE,SUB_AMT,AIRPORTCODE,REGION,REMARKS,EMPLOYEENAME,EMPLOYEENO,PENSIONNO,FINYEAR,KEYNO,LOANTYPE,LOANPURPOSE)  (select ECPFACNO,VOUCHER_DT,debit-credit,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EREGION,details.DETAILS,trim(info.EMPLOYEENAME),einfo.employeeno,EMP_PARTY_CODE,FYEAR,info.keyno,'NRF',PURPOSETYPE from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else {
								query = " insert into employee_pension_loans (CPFACCNO,LOANDATE,CONT_AMT,AIRPORTCODE,REGION,REMARKS,EMPLOYEENAME,EMPLOYEENO,PENSIONNO,FINYEAR,KEYNO,LOANTYPE,LOANPURPOSE)  (select ECPFACNO,VOUCHER_DT,debit-credit,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EREGION,details.DETAILS,trim(info.EMPLOYEENAME),einfo.employeeno,EMP_PARTY_CODE,FYEAR,info.keyno,'NRF',PURPOSETYPE from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							}
						}
						if(query != null)
							stmt.executeUpdate(query);
					} else if("672.06".equals(voucherDt.getAccountHead().trim()) || "672.07".equals(voucherDt.getAccountHead().trim())){
						existsRec = "select count(*) from employee_pension_finsettlement where keyno = '"+info.getKeyNo()+"' ";
						rs = stmt.executeQuery(existsRec);
						if(rs.next() && rs.getInt(1)>0){
							if("672.06".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_pension_finsettlement set FINEMP = nvl(FINEMP,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  ),NETAMOUNT=nvl(FINEMP,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  )+nvl(FINAAI,0) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							}
							
							else {
								query = " update employee_pension_finsettlement set FINAAI = nvl(FINAAI,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  ),NETAMOUNT=nvl(FINEMP,0)+ nvl(FINAAI,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  ) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							}
						}else {
							if("672.06".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_pension_finsettlement (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,FINEMP,SETTLEMENTDATE,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,KEYNO)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),debit-credit,VOUCHER_DT,details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
								updatequery="update employee_pension_finsettlement set NETAMOUNT=nvl(FINEMP,0)+nvl(FINAAI,0) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')) ";
							} else {
								query = " insert into employee_pension_finsettlement (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,FINAAI,SETTLEMENTDATE,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,KEYNO)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),debit-credit,VOUCHER_DT,details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
								updatequery="update employee_pension_finsettlement set NETAMOUNT=nvl(FINEMP,0)+nvl(FINAAI,0) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')) ";
							}
						}
						log.info("the query is..."+query);
						if(query != null)
							stmt.executeUpdate(query);
						if(updatequery != null)
							stmt.executeUpdate(updatequery);
							
					} 
				/*	else if("201.01".equals(voucherDt.getAccountHead().trim()) || "201.02".equals(voucherDt.getAccountHead().trim()) ){
						existsRec = "select KEYNO from employee_adj_ob where (ADJOBYEAR,PENSIONNO) = (SELECT TO_DATE('01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy')),EMP_PARTY_CODE from cb_voucher_info where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')))  and KEYNO is not null";
						rs = stmt.executeQuery(existsRec);
						String accKeyno = "";
						if(rs.next()){
							accKeyno = rs.getString(1);
							if("201.01".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_adj_ob set ADJEMPSUB = nvl(ADJEMPSUB,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' ) where upper(trim(keyno))=upper(trim('"+accKeyno+"'))  ";
							} else {
								query = " update employee_adj_ob set ADJAAICONTRIB = nvl(ADJAAICONTRIB,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(keyno))=upper(trim('"+accKeyno+"'))  ";
							}
						}else {
							existsRec = "select CB_VOUCHER_VALIDATE_SEQ.nextval from dual  ";
							ResultSet rsSeq = stmt.executeQuery(existsRec);
							accKeyno="";
							if(rsSeq.next()){
								accKeyno = rs.getString(1);
							}
							if("201.01".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_adj_ob (PENSIONNO,ADJOBYEAR,REMARKS,REGION,ADJEMPSUB,KEYNO)  (select EMP_PARTY_CODE,'01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy'),details.DETAILS,EREGION,debit-credit,'"+accKeyno+"' from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							} else {
								query = " insert into employee_adj_ob (PENSIONNO,ADJOBYEAR,REMARKS,REGION,ADJAAICONTRIB,KEYNO)  (select EMP_PARTY_CODE,'01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy'),details.DETAILS,EREGION,debit-credit,'"+accKeyno+"' from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							}
						}
						if(query != null){
							stmt.executeUpdate(query);
							stmt.executeUpdate("update cb_voucher_details set ACC_KEYNO = '"+accKeyno+"' where rowid= '"+voucherDt.getRowid()+"'  ");
						}
					}else if("314.00".equals(voucherDt.getAccountHead().trim()) || "314.01".equals(voucherDt.getAccountHead().trim())){
						existsRec = "select KEYNO from employee_pension_ob where (OBYEAR,PENSIONNO) = (SELECT TO_DATE('01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy')),EMP_PARTY_CODE from cb_voucher_info where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')))  and KEYNO is not null";
						rs = stmt.executeQuery(existsRec);
						String accKeyno = "";
						if(rs.next()){
							accKeyno = rs.getString(1);
							if("314.00".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_pension_ob set EMPNETOB = nvl(EMPNETOB,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(keyno))=upper(trim('"+accKeyno+"'))  ";
							} else {
								query = " update employee_pension_ob set AAINETOB = nvl(AAINETOB,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(keyno))=upper(trim('"+accKeyno+"'))  ";
							}
						}else {
							existsRec = "select CB_VOUCHER_VALIDATE_SEQ.nextval from dual  ";
							ResultSet rsSeq = stmt.executeQuery(existsRec);
							accKeyno="";
							if(rsSeq.next()){
								accKeyno = rs.getString(1);
							}
							if("314.00".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_pension_ob (PENSIONNO,OBYEAR,REMARKS,REGION,EMPNETOB,KEYNO,CPFACCNO,AIRPORTCODE,EMPLOYEENO,FINYEAR)  (select EMP_PARTY_CODE,'01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy'),details.DETAILS,EREGION,debit-credit,'"+accKeyno+"',ECPFACNO,eunitcode,einfo.employeeno,FYEAR from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							} else {
								query = " insert into employee_pension_ob (PENSIONNO,OBYEAR,REMARKS,REGION,AAINETOB,KEYNO,CPFACCNO,AIRPORTCODE,EMPLOYEENO,FINYEAR)  (select EMP_PARTY_CODE,'01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy'),details.DETAILS,EREGION,debit-credit,'"+accKeyno+"',ECPFACNO,eunitcode,einfo.employeeno,FYEAR from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							}
						}
						if(query != null){
							stmt.executeUpdate(query);
							stmt.executeUpdate("update cb_voucher_details set ACC_KEYNO = '"+accKeyno+"' where rowid= '"+voucherDt.getRowid()+"'  ");
						}
					}else if("|313.00|313.01|313.02|315.00|315.01|316.00|318.00|318.01|".indexOf("|"+voucherDt.getAccountHead()+"|") > -1){
						existsRec = "select ACC_KEYNO from employee_pension_validate where (MONTHYEAR,PENSIONNO) = (SELECT TO_DATE('01/'||TO_CHAR(VOUCHER_DT,'mon/yyyy'),'dd/mon/yyyy'),EMP_PARTY_CODE from cb_voucher_info where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')))  and ACC_KEYNO is not null";
						rs = stmt.executeQuery(existsRec);
						String accKeyno = "";
						if(rs.next()){
							accKeyno = rs.getString(1);
							if("313.00".equals(voucherDt.getAccountHead().trim()) || "315.00".equals(voucherDt.getAccountHead().trim()) || ("315.01".equals(voucherDt.getAccountHead().trim()) && "A".equals(voucherDt.getRegard()))){
								query = " update employee_pension_validate set EMOLUMENTS= to_char((to_number(to_number(nvl(EMPPFSTATUARY,0))+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' ))*100/12),'99999999999.99'), EMPPFSTATUARY = nvl(EMPPFSTATUARY,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							} else  if("313.01".equals(voucherDt.getAccountHead().trim()) || ("315.01".equals(voucherDt.getAccountHead().trim()) && "V".equals(voucherDt.getRegard()))){
								query = " update employee_pension_validate set EMPVPF = nvl(EMPVPF,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							} else  if("318.00".equals(voucherDt.getAccountHead().trim()) || ("313.02".equals(voucherDt.getAccountHead().trim()) && "P".equals(voucherDt.getRegard()))){
								query = " update employee_pension_validate set EMPADVRECPRINCIPAL = nvl(EMPADVRECPRINCIPAL,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							} else  if("318.01".equals(voucherDt.getAccountHead().trim())|| ("313.02".equals(voucherDt.getAccountHead().trim()) && "I".equals(voucherDt.getRegard()))){
								query = " update employee_pension_validate set EMPADVRECINTEREST = nvl(EMPADVRECINTEREST,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							} else  if("316.00".equals(voucherDt.getAccountHead().trim())||("313.02".equals(voucherDt.getAccountHead().trim()) && "E".equals(voucherDt.getRegard()))){
								query = " update employee_pension_validate set PF = nvl(PF,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							} else  if("313.02".equals(voucherDt.getAccountHead().trim()) && "C".equals(voucherDt.getRegard())){
								query = " update employee_pension_validate set PENSIONCONTRI = nvl(PENSIONCONTRI,0)+ (select debit-credit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"' )  where upper(trim(ACC_KEYNO))=upper(trim('"+accKeyno+"'))  ";
							}
							
						}else {
							existsRec = "select CB_VOUCHER_VALIDATE_SEQ.nextval from dual  ";
							ResultSet rsSeq = stmt.executeQuery(existsRec);
							accKeyno="";
							if(rsSeq.next()){
								accKeyno = rs.getString(1);
							}
							if("313.00".equals(voucherDt.getAccountHead().trim()) || "315.00".equals(voucherDt.getAccountHead().trim()) || ("315.01".equals(voucherDt.getAccountHead().trim()) && "A".equals(voucherDt.getRegard()))){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,EMPPFSTATUARY,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION,EMOLUMENTS)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION,((debit-credit)*100/12) from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else  if("313.01".equals(voucherDt.getAccountHead().trim()) || ("315.01".equals(voucherDt.getAccountHead().trim()) && "V".equals(voucherDt.getRegard()))){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,EMPVPF,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else  if("318.00".equals(voucherDt.getAccountHead().trim()) || ("313.02".equals(voucherDt.getAccountHead().trim()) && "P".equals(voucherDt.getRegard()))){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,EMPADVRECPRINCIPAL,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else  if("318.01".equals(voucherDt.getAccountHead().trim())|| ("313.02".equals(voucherDt.getAccountHead().trim()) && "I".equals(voucherDt.getRegard()))){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,EMPADVRECINTEREST,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else  if("316.00".equals(voucherDt.getAccountHead().trim())||("313.02".equals(voucherDt.getAccountHead().trim()) && "E".equals(voucherDt.getRegard()))){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,PF,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else  if("313.02".equals(voucherDt.getAccountHead().trim()) && "C".equals(voucherDt.getRegard())){
								query = " insert into employee_pension_validate (CPFACCNO,AIRPORTCODE,MONTHYEAR,PENSIONCONTRI,EMPLOYEENAME,EMPLOYEENO,REMARKS,REGION,PENSIONNO,ACC_KEYNO,DESEGNATION)  (select ECPFACNO,eunitcode,'01/'||to_char(VOUCHER_DT,'Mon/YYYY'),debit-credit,einfo.EMPLOYEENAME,einfo.employeeno,details.DETAILS,EREGION,EMP_PARTY_CODE,'"+accKeyno+"',einfo.DESEGNATION from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							}
						}
						if(query != null){
							stmt.executeUpdate(query);
							stmt.executeUpdate("update cb_voucher_details set ACC_KEYNO = '"+accKeyno+"' where rowid= '"+voucherDt.getRowid()+"'  ");
						}
					}*/
				}	
			}
			if("Receipt".equals(info.getVoucherType()) && "E".equals(info.getPartyType())){
				for (int i = 0; i < length; i++) {
					String  query = null;
					String updatequery=null;
					VoucherDetails voucherDt = (VoucherDetails) voucherDts.get(i);
					if("672.03".equals(voucherDt.getAccountHead().trim())){
						query = " insert into employee_pension_advances (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,AMOUNT,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,keyno,ADVTRANSDATE)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),-(credit-DEBIT),details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno,VOUCHER_DT from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
						if(query != null)
							stmt.executeUpdate(query);
					} else if("672.04".equals(voucherDt.getAccountHead().trim()) || "672.05".equals(voucherDt.getAccountHead().trim())){
						existsRec = "select count(*) from employee_pension_loans where keyno = '"+info.getKeyNo()+"' ";
						rs = stmt.executeQuery(existsRec);
						if(rs.next() && rs.getInt(1)>0){
							if("672.04".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_pension_loans set SUB_AMT = '-'||(nvl(SUB_AMT,0)+ (select credit-DEBIT from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"')) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							} else {
								query = " update employee_pension_loans set CONT_AMT = '-'||(nvl(CONT_AMT,0)+ (select credit-DEBIT from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"')) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							}
						}else {
							if("672.04".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_pension_loans (CPFACCNO,LOANDATE,SUB_AMT,AIRPORTCODE,REGION,REMARKS,EMPLOYEENAME,EMPLOYEENO,PENSIONNO,FINYEAR,KEYNO,LOANTYPE)  (select ECPFACNO,VOUCHER_DT,-(credit-debit),get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EREGION,details.DETAILS,trim(info.EMPLOYEENAME),einfo.employeeno,EMP_PARTY_CODE,FYEAR,info.keyno,'NRF' from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"')) and details.rowid='"+voucherDt.getRowid()+"') ";
							} else {
								query = " insert into employee_pension_loans (CPFACCNO,LOANDATE,CONT_AMT,AIRPORTCODE,REGION,REMARKS,EMPLOYEENAME,EMPLOYEENO,PENSIONNO,FINYEAR,KEYNO,LOANTYPE)  (select ECPFACNO,VOUCHER_DT,-(credit-debit),get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EREGION,details.DETAILS,trim(info.EMPLOYEENAME),einfo.employeeno,EMP_PARTY_CODE,FYEAR,info.keyno,'NRF' from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
							}
						}
						if(query != null)
							stmt.executeUpdate(query);
					} else if("672.06".equals(voucherDt.getAccountHead().trim()) || "672.07".equals(voucherDt.getAccountHead().trim())){
						existsRec = "select count(*) from employee_pension_finsettlement where keyno = '"+info.getKeyNo()+"' ";
						rs = stmt.executeQuery(existsRec);
						if(rs.next() && rs.getInt(1)>0){
							if("672.06".equals(voucherDt.getAccountHead().trim())){
								query = " update employee_pension_finsettlement set FINEMP = '-'||(nvl(FINEMP,0)+ (select credit-debit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  )),NETAMOUNT=nvl(FINAAI,0)-(nvl(FINEMP,0)+ (select credit-debit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  )) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							} else {
								query = " update employee_pension_finsettlement set FINAAI = '-'||(nvl(FINAAI,0)+ (select credit-debit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  )),NETAMOUNT=nvl(FINEMP,0)-(nvl(FINAAI,0)+ (select credit-debit from cb_voucher_details details where rowid= '"+voucherDt.getRowid()+"'  )) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"'))  ";
							}
						}else {
							if("672.06".equals(voucherDt.getAccountHead().trim())){
								query = " insert into employee_pension_finsettlement (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,FINEMP,SETTLEMENTDATE,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,KEYNO)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),-(credit-debit),VOUCHER_DT,details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
								updatequery="update employee_pension_finsettlement set NETAMOUNT=nvl(FINEMP,0)+nvl(FINAAI,0) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')) ";
							} else {
								query = " insert into employee_pension_finsettlement (CPFACCNO,EMPLOYEENO,EMPLOYEENAME,FINAAI,SETTLEMENTDATE,REMARKS,REGION,AIRPORTCODE,PENSIONNO,FINYEAR,KEYNO)  (select ECPFACNO,einfo.employeeno,trim(info.EMPLOYEENAME),-(credit-debit),VOUCHER_DT,details.DETAILS,EREGION,get_description('UNITNAME', 'employee_unit_master', 'UNITCODE=''' || eunitcode || '''')eunitcode,EMP_PARTY_CODE,FYEAR,info.keyno from cb_voucher_info info, cb_voucher_details details,employee_personal_info einfo where to_char(einfo.pensionno)=info.emp_party_code and info.keyno = details.keyno and info.keyno='"+info.getKeyNo()+"' and upper(trim(ACCOUNTHEAD))=upper(trim('"+voucherDt.getAccountHead()+"'))and details.rowid='"+voucherDt.getRowid()+"') ";
								updatequery="update employee_pension_finsettlement set NETAMOUNT=nvl(FINEMP,0)+nvl(FINAAI,0) where upper(trim(keyno))=upper(trim('"+info.getKeyNo()+"')) ";
							}
						}
						if(query != null)
							stmt.executeUpdate(query);
						if(updatequery != null)
							stmt.executeUpdate(updatequery);
					} 
				}
			}

			con.commit();
		} catch (Exception e) {
			con.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				con.close();
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		UserTracking.write(info.getEnteredBy(), info.getVoucherDt() + "-"
				+ info.getCheckedBy() + "-" + info.getApprovedBy() + "-"
				+ getVoucherNo(info.getKeyNo()) + "-"
				+ info.getVoucherType() + "-" + info.getTrustType(), "S", "CB",
				info.getKeyNo(), "Voucher Info");
		log.info("VoucherDAO : updateApprovalVoucher : Leaving method");
	}
	public String getappVoucherDate()throws Exception{
		String voucherdate="";
		Connection con = null;
		Statement st=null;
		ResultSet rs=null;
		try{
			con = DBUtility.getConnection();
			st=con.createStatement();
			String query="select to_char(sysdate,'dd/Mon/yyyy') voucherdate from dual";
			rs=st.executeQuery(query);
			if(rs.next())
				voucherdate=StringUtility.checknull(rs.getString("voucherdate"));
			
		}
		catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				st.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		return voucherdate;
	}

	public BankBook getOpenBalance(String accountNo, String fromdate)
			throws Exception {
		log.info("VoucherDAO : getOpenBalance : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		BankBook book = new BankBook();
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("bankOpenBalQuery"));			
			pst.setString(1, accountNo);
			pst.setString(2, fromdate);
			pst.setString(3, accountNo);
			pst.setString(4, fromdate);
			pst.setString(5, accountNo);
			pst.setString(6, fromdate);
			pst.setString(7, accountNo);
			pst.setString(8, accountNo);
			pst.setString(9, fromdate);
			pst.setString(10, accountNo);
			pst.setString(11, accountNo);
			pst.setString(12, accountNo);
			pst.setString(13, accountNo);
			pst.setString(14, fromdate);

			rs = pst.executeQuery();

			if (rs.next()) {
				book.setOpeningBalAmt(rs.getDouble(1));
				book.setBankName(rs.getString("bankname"));
				book.setAmountType(rs.getString("amountType"));
				book.setAccountNo(accountNo);
			}

		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getOpenBalance : Leaving method");
		return book;
	}

	public List getBankBook(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getBankBook : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List bookList = new ArrayList();
		List accountNos = getAccountNos(info.getAccountHead());
		con = DBUtility.getConnection();
		int size = accountNos.size();
		for (int cnt = 0; cnt < size; cnt++) {
			BankBook book = getOpenBalance((String) accountNos.get(cnt), info
					.getFromDate());
			List bankBookList = new ArrayList();
			BankBook bankBook = null;
			try {
				con = DBUtility.getConnection();
				pst = con.prepareStatement(getQueries("bankBookQuery"));
				pst.setString(1, (String) accountNos.get(cnt));
				pst.setString(2, (String) accountNos.get(cnt));
				pst.setString(3, (String) accountNos.get(cnt));
				pst.setString(4, (String) accountNos.get(cnt));
				pst.setString(5, (String) accountNos.get(cnt));
				pst.setString(6, (String) accountNos.get(cnt));
				pst.setString(7, info.getFromDate());
				pst.setString(8, info.getToDate());

				rs = pst.executeQuery();

				while (rs.next()) {
					bankBook = new BankBook();
					bankBook.setAccountHead(rs.getString("accounthead"));
					bankBook.setVoucherno(rs.getString("voucherno"));
					bankBook.setParticular(rs.getString("particular"));
					bankBook.setPartyName(rs.getString("partyname"));
					bankBook.setDescription(rs.getString("details"));
					bankBook.setPayments(rs.getDouble("debit"));
					bankBook.setReceipts(rs.getDouble("credit"));
					bankBook.setChequeNo(rs.getString("chequeno"));
					bankBook.setVoucherDate(rs.getString("voucher_dt"));
					bankBookList.add(bankBook);
				}
				book.setBankBookList(bankBookList);
			} catch (SQLException e) {
				log.printStackTrace(e);
				throw e;
			} catch (Exception e) {
				log.printStackTrace(e);
				throw e;
			} finally {
				try {
					pst.close();
				} catch (SQLException e) {
					log.printStackTrace(e);
				}
			}
			bookList.add(book);
		}
		con.close();
		log.info("VoucherDAO : getBankBook : leaving method");
		return bookList;
	}

	private List getAccountNos(String accountHead) throws Exception {
		log.info("VoucherDAO : getAccountNos : Entering method");
		List list = new ArrayList();
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("accNosQuery"));
			pst.setString(1, accountHead);
			rs = pst.executeQuery();
			while (rs.next()) {
				list.add(rs.getString(1));
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getAccountNos : leaving method");
		return list;
	}

	public void updateVoucherRecord(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : updateVoucherRecord : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = DBUtility.getConnection();
			con.setAutoCommit(false);
			pst = con.prepareStatement(getQueries("updateVoucherQry"));
			pst.setString(1, info.getAccountNo());
			pst.setString(2, info.getFinYear());
			pst.setString(3, info.getTrustType());
			pst.setString(4, info.getPartyType());
			pst.setString(5, info.getEmpPartyCode());
			pst.setString(6, info.getLoginUserId());
			pst.setString(7, info.getDetails());
			pst.setString(8, info.getTransactionType());
			pst.setString(9, info.getLoginUserId());
			pst.setString(10, info.getPreparedDt());
			pst.setString(11, info.getISIN());
			pst.setString(12, info.getKeyNo());
			
			log.info(">>>>>>>>>"+info.getISIN()+"<<<<<<<<<<<"+info.getEmpPartyCode());
			pst.executeUpdate();
		} catch (SQLException e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();

			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		UserTracking
				.write(info.getEnteredBy(), info.getAccountNo() + "-"
						+ info.getEmpPartyCode() + "-" + info.getPartyType()
						+ "-" + info.getVoucherType() + "-"
						+ info.getTrustType() + "-" + info.getFinYear(), "U",
						"CB", info.getKeyNo(), "Voucher Info");
		try {
			pst = con.prepareStatement(getQueries("deleteDtQuery"));
			pst.setString(1, info.getKeyNo());
			pst.executeUpdate();
		} catch (SQLException e) {
			con.rollback();
			throw e;
		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			pst = con.prepareStatement(getQueries("insertDtQuery"));
			List voucherDts = info.getVoucherDetails();
			int length = voucherDts.size();
			for (int i = 0; i < length; i++) {
				VoucherDetails voucherDt = (VoucherDetails) voucherDts.get(i);				
				pst.setString(1, info.getKeyNo());
				pst.setString(2, voucherDt.getAccountHead());
				pst.setString(3, voucherDt.getMonthYear());
				pst.setDouble(4, voucherDt.getCredit());
				pst.setString(5, voucherDt.getDetails());
				pst.setDouble(6, voucherDt.getDebit());
				pst.setString(7, voucherDt.getChequeNo());
				pst.setString(8, info.getLoginUserId());
				pst.setString(9, info.getLoginUnitCode());
				pst.setString(10, voucherDt.getRegard());
				pst.executeUpdate();
				UserTracking.write(info.getEnteredBy(), voucherDt
						.getAccountHead()
						+ "-"
						+ voucherDt.getMonthYear()
						+ "-"
						+ voucherDt.getCredit()
						+ "-"
						+ voucherDt.getDetails()
						+ "-"
						+ voucherDt.getDebit()
						+ "-"
						+ voucherDt.getChequeNo(), "U", "CB", info.getKeyNo(),
						"Voucher Details");
			}
			con.commit();
		} catch (Exception e) {
			con.rollback();
			log.info("VoucherDAO : Exception : " + e.toString());
			throw e;

		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		log.info("VoucherDAO : updateVoucherRecord : leaving method");

	}

	public String getVoucherNo(String keyno)
			throws Exception {
		log.info("VoucherDAO : getVoucherNo : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String voucherNo = null;
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("voucherNoQuery"));
			pst.setString(1, keyno);
			rs = pst.executeQuery();
			if (rs.next()) {
				voucherNo = rs.getString(1);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getVoucherNo : leaving method");
		return voucherNo;
	}

	public void deleteVoucher(String codes) throws Exception {
		log.info("VoucherDAO : deleteVoucher : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			con = DBUtility.getConnection();
			con.setAutoCommit(false);
			pst = con.prepareStatement(getQueries("otherModuleUpdate"));
			pst.setString(1, codes);
			pst.setString(2, codes);
			rs = pst.executeQuery();
			stmt = con.createStatement();
			while(rs.next()){				
				stmt.executeUpdate(rs.getString(1));
			}
		} catch (SQLException e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				rs.close();
				stmt.close();
				pst.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		try {
			pst = con.prepareStatement(getQueries("deleteVDTQuery"));
			pst.setString(1, codes);
			pst.executeUpdate();
		} catch (SQLException e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		try {
			pst = con.prepareStatement(getQueries("deleteVQuery"));
			pst.setString(1, codes);
			pst.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			con.rollback();
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : deleteVoucher : leaving method");
	}

	public List getLedgerAccount(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getLedgerAccount : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List dataList = new ArrayList();
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("ledgerQuery"));
			pst.setString(1, info.getFromDate());
			pst.setString(2, info.getFromDate());
			pst.setString(3, info.getToDate());
			pst.setString(4, info.getToDate());
			pst.setString(5, info.getVoucherType() + "%");
			pst.setString(6, info.getBankCode() + "%");
			pst.setString(7, info.getTrustType() + "%");
			rs = pst.executeQuery();
			while (rs.next()) {
				BankBook book = new BankBook();
				book.setVoucherDate(rs.getString("vdate"));
				book.setAccountNo(rs.getString("accno"));
				book.setBankName(rs.getString("bank"));
				book.setParticular(rs.getString("particulars"));
				book.setVoucherType(rs.getString("vtype"));
				book.setVoucherno(rs.getString("NO"));
				book.setPayments(rs.getDouble("dt"));
				book.setReceipts(rs.getDouble("cr"));
				dataList.add(book);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getLedgerAccount : leaving method");
		return dataList;
	}

	public Map getGenLedger(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getGenLedger : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		Map ledger = new LinkedHashMap();
		List ledgers = null;
		Map trustTypeBased = null;
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("genLedgerQuery"));
			pst.setString(1, info.getFromDate());
			pst.setString(2, info.getTrustType() + "%");
			pst.setString(3, info.getAccountHead());
			pst.setString(4, info.getAccountHead());
			pst.setString(5,info.getToAccountHead());
			pst.setString(6,info.getToAccountHead());
			pst.setString(7, info.getVoucherType() + "%");
			pst.setString(8, info.getFromDate());
			pst.setString(9, info.getFromDate());
			pst.setString(10, info.getToDate());
			pst.setString(11, info.getToDate());
			pst.setString(12, info.getFromDate());
			pst.setString(13, info.getTrustType() + "%");
			pst.setString(14, info.getAccountHead());
			pst.setString(15, info.getAccountHead());
			pst.setString(16, info.getToAccountHead());
			pst.setString(17, info.getToAccountHead());
			pst.setString(18, info.getVoucherType() + "%");
			pst.setString(19, info.getFromDate());
			pst.setString(20, info.getFromDate());
			pst.setString(21, info.getToDate());
			pst.setString(22, info.getToDate());
			rs = pst.executeQuery();
			while (rs.next()) {
				if (ledger.containsKey(rs.getString("trusttype"))) {
					trustTypeBased = (LinkedHashMap) ledger.get(rs
							.getString("trusttype"));
					if (trustTypeBased.containsKey(rs.getString("Accountcode"))) {
						ledgers = (List) trustTypeBased.get(rs
								.getString("Accountcode"));
					} else {
						ledgers = new ArrayList();
					}
				} else {
					trustTypeBased = new LinkedHashMap();
					ledgers = new ArrayList();
				}
				BankBook ledgerAcc = new BankBook();
				ledgerAcc.setOpeningBalAmt(rs.getDouble("openingBal"));
				ledgerAcc.setTrustType(rs.getString("trusttype"));
				ledgerAcc.setFinYear(rs.getString("FYEAR"));
				ledgerAcc.setAccountHead(rs.getString("Accountcode"));
				ledgerAcc.setVoucherDate(rs.getString("vdate"));
				ledgerAcc.setBankName(rs.getString("bank"));
				ledgerAcc.setDescription(rs.getString("details"));
				ledgerAcc.setVoucherType(rs.getString("vtype"));
				ledgerAcc.setVoucherno(rs.getString("voucherno"));
				if(StringUtility.checknull(rs.getString("debittype")).equals("DR"))
					ledgerAcc.setPayments(rs.getDouble("debit"));
					if(StringUtility.checknull(rs.getString("debittype")).equals("DC"))
						ledgerAcc.setReceipts(rs.getDouble("debit"));
					
					if(StringUtility.checknull(rs.getString("credittype")).equals("CR"))
						ledgerAcc.setReceipts(rs.getDouble("credit"));
						if(StringUtility.checknull(rs.getString("credittype")).equals("CD"))
							ledgerAcc.setPayments(rs.getDouble("credit"));
				ledgerAcc.setPartyName(rs.getString("partyname"));
				ledgerAcc.setChequeNo(rs.getString("chequeNo"));
				ledgers.add(ledgerAcc);
				trustTypeBased.put(rs.getString("Accountcode"), ledgers);
				ledger.put(rs.getString("trusttype"), trustTypeBased);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getGenLedger : leaving method");
		return ledger;
	}

	public Map getTrialBalance(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : getTrialBalance : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		Map trialBal = new HashMap();
		List accountHeads = null;
		Map trustTypeBased = null;
		Statement st=null;
		String query="";
		
		query="select type,Accountcode,trusttype,openBalcredit,openBaldebit,sum(credit)+Nvl((select case when sum(credit) - sum(debit)<0 then  sum(debit)-sum(credit) else 0 end  from cb_voucher_info info,cb_voucher_details details,cb_bank_info binfo where details.keyno = info.keyno and binfo.accountno = info.accountno and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"') and decode('"+info.getToDate()+"', '', '01/jan/3000','"+info.getToDate()+"') and upper(trim(binfo.trusttype)) = upper(trim(vouinfo.trusttype)) and   binfo.accountcode = vouinfo.accounthead group by info.accountno, binfo.accountcode, binfo.trusttype),0)-Nvl((select sum(credit) from cb_voucher_info info, cb_voucher_details  det,cb_bank_info bank where vouchertype = 'C' and det.keyno = info.keyno and bank.accountno = info.emp_party_code and " +
				"upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"') and decode('"+info.getToDate()+"', '', '01/jan/3000','"+info.getToDate()+"') and  bank.accountcode = vouinfo.accounthead),0) credit,sum(debit)+Nvl((select case when sum(credit) - sum(debit)>0 then  sum(credit) - sum(debit)  else 0 end from cb_voucher_info info,cb_voucher_details details,cb_bank_info binfo where details.keyno = info.keyno and binfo.accountno = info.accountno and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"') and decode('"+info.getToDate()+"', '', '01/jan/3000', '"+info.getToDate()+"') and upper(trim(binfo.trusttype)) = upper(trim(vouinfo.trusttype)) and   binfo.accountcode = vouinfo.accounthead " +
						"group by info.accountno, binfo.accountcode, binfo.trusttype),0)-Nvl((select sum(credit) from cb_voucher_info info, cb_voucher_details  det,cb_bank_info bank where vouchertype = 'C' and det.keyno = info.keyno and bank.accountno = info.accountno and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"') and decode('"+info.getToDate()+"', '', '01/jan/3000','"+info.getToDate()+"') and det.accounthead = vouinfo.accounthead),0) debit from (select ACCOUNTCODETYPE type,upper(dt.accounthead || ' ' || ainfo.particular) Accountcode,dt.accounthead accounthead,info.trusttype trusttype,(case when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=dt.accounthead) " +
								"when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 " +
								"where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 " +
								"where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00','658.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','444.00','403.00','658.00','112','673.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2018-2019' then (select AMOUNT from cb_temp_accountcode_details7 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=dt.accounthead)  else getAccHeadOpenBal(dt.accounthead, info.trusttype, '"+info.getFromDate()+"', 'C') end) openBalcredit,(case when dt.accounthead in('667.02') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2018-2019' then (select AMOUNT from cb_temp_accountcode_details7 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=dt.accounthead) else getAccHeadOpenBal(dt.accounthead, info.trusttype, '"+info.getFromDate()+"', 'D') end) openBaldebit, sum(credit) credit, sum(debit) debit from Cb_VOUCHER_INFO info,Cb_voucher_details dt,cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accounthead and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"') and decode('"+info.getToDate()+"', '', '01/jan/3000', '"+info.getToDate()+"') " +
										"group by dt.accounthead, info.trusttype,  ainfo.particular,ACCOUNTCODETYPE union all select ACCOUNTCODETYPE type, upper(dt.accountcode || ' ' || ainfo.particular) Accountcode,dt.accountcode accounthead, info.trusttype trusttype,(case when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00','313.00') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00','313.00') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','444.00','403.00','658.00','112','673.00') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2018-2019' then (select AMOUNT from cb_temp_accountcode_details7 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and " +
												"GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=dt.accountcode)  else  getAccHeadOpenBal(dt.accountcode,info.trusttype, '"+info.getFromDate()+"', 'C')end) openBalcredit, (case when dt.accountcode in('667.02') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=dt.accountcode)when dt.accountcode in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=dt.accountcode) else  getAccHeadOpenBal(dt.accountcode, info.trusttype, '"+info.getFromDate()+"', 'D') end) openBaldebit,sum(credit) credit,sum(debit) debit from Cb_Journalvoucher info,Cb_Journalvoucher_Details dt, cb_accountcode_info ainfo, cb_accountcodetype_info acctype where acctype.code = ainfo.type  and dt.keyno = info.keyno and ainfo.accounthead = dt.accountcode and upper(trim(info.trusttype))  like upper(trim('"+info.getTrustType()+"')) and info.voucher_dt " +
														"between decode('"+info.getFromDate()+"', '', '01/jan/1000', '"+info.getFromDate()+"')  and decode('"+info.getToDate()+"', '', '01/jan/3000', '"+info.getToDate()+"') group by dt.accountcode, info.trusttype, ainfo.particular,ACCOUNTCODETYPE union all select ACCOUNTCODETYPE type,upper(det.accounthead||' '||info.particular) Accountcode,det.accounthead accounthead,det.trusttype trusttype,(case when det.accounthead in('402.00','403.00','444.00')  and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00')  and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' " +
																"then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 where ACCOUNTHEAD=det.accounthead)" +
																		"when det.accounthead in('403.00','444.00','658.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','444.00','403.00','658.00','112','673.00') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2018-2019' then (select AMOUNT from cb_temp_accountcode_details7 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('107.01','402.00','444.00','403.00','658.00','112','673.00','658.00','201.01') and GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=det.accounthead) else   getAccHeadOpenBal(det.accounthead,det.trusttype,'"+info.getFromDate()+"','C') end) openBalcredit, (case when det.accounthead in('667.02') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2016-2017' then (select AMOUNT from cb_temp_accountcode_details5 where ACCOUNTHEAD=det.accounthead)when det.accounthead in('658.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2017-2018' then (select AMOUNT from cb_temp_accountcode_details6 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2018-2019' then (select AMOUNT from cb_temp_accountcode_details7 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2019-2020' then (select AMOUNT from cb_temp_accountcode_details8 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2020-2021' then (select AMOUNT from cb_temp_accountcode_details9 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('201.01','203.01','673.00') and  GET_FINYEAR_VAL('"+info.getFromDate()+"','"+info.getFromDate()+"')='2021-2022' then (select AMOUNT from cb_temp_accountcode_details10 where ACCOUNTHEAD=det.accounthead) else     getAccHeadOpenBal(det.accounthead,det.trusttype,'"+info.getFromDate()+"','D') end) openBaldebit,0 credit,0 debit from " +
																		"cb_accountcode_details det,cb_accountcode_info info,cb_accountcodetype_info acctype where info.accounthead = det.accounthead and upper(trim(det.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and acctype.code = info.type and det.accounthead || ' ' || det.trusttype not in ( select dt.accountcode || ' ' || info.trusttype from Cb_Journalvoucher info,Cb_Journalvoucher_Details dt,cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accountcode and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"','','01/jan/1000','"+info.getFromDate()+"') and decode('"+info.getToDate()+"','','01/jan/3000','"+info.getToDate()+"') group by dt.accountcode,info.trusttype union select dt.accounthead || ' ' || info.trusttype from Cb_VOUCHER_INFO info,Cb_voucher_details dt,cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accounthead and upper(trim(info.trusttype)) like upper(trim('"+info.getTrustType()+"%"+"')) and info.voucher_dt between decode('"+info.getFromDate()+"','','01/jan/1000','"+info.getFromDate()+"') and decode('"+info.getToDate()+"','','01/jan/3000','"+info.getToDate()+"') group by dt.accounthead,info.trusttype)) vouinfo  group by type, Accountcode, trusttype, openBalcredit, openBaldebit,vouinfo.accounthead order by trusttype, type, Accountcode";
		
		
		System.out.println("VoucherDAO : getTrialBalance : Query:"+query);
		
		try {
			//FileWriter fw=new FileWriter("D://Prasad.txt");
			//fw.write(query);
			//fw.flush();
			
			con = DBUtility.getConnection();
			
			st=con.createStatement();
			
			//System.out.println(getQueries("trailBalQuery")+info.getTrustType());
			
			/*pst = con.prepareStatement(getQueries("trailBalQuery"));
			
			pst.setString(1, info.getTrustType() + "%");
			pst.setString(2, info.getFromDate());
			pst.setString(3, info.getFromDate());
			pst.setString(4, info.getToDate());
			pst.setString(5, info.getToDate());
			pst.setString(6, info.getTrustType() + "%");
			pst.setString(7, info.getFromDate());
			pst.setString(8, info.getFromDate());
			pst.setString(9, info.getToDate());
			pst.setString(10, info.getToDate());			
			pst.setString(11, info.getTrustType() + "%");
			pst.setString(12, info.getFromDate());
			pst.setString(13, info.getFromDate());
			pst.setString(14, info.getToDate());
			pst.setString(15, info.getToDate());
			pst.setString(16, info.getTrustType() + "%");
			pst.setString(17, info.getFromDate());
			pst.setString(18, info.getFromDate());
			pst.setString(19, info.getToDate());
			pst.setString(20, info.getToDate());
			
			pst.setString(21,info.getFromDate());
			pst.setString(22,info.getFromDate());
			
			pst.setString(23,info.getFromDate());
			pst.setString(24,info.getFromDate());
			
			pst.setString(25,info.getFromDate());
			pst.setString(26,info.getFromDate());
			
			pst.setString(27,info.getFromDate());
			pst.setString(28,info.getFromDate());
			
			pst.setString(29,info.getFromDate());
			pst.setString(30,info.getFromDate());
			
			pst.setString(31, info.getFromDate());
			pst.setString(32, info.getFromDate());
			pst.setString(33, info.getTrustType() + "%");
			pst.setString(34, info.getFromDate());
			pst.setString(35, info.getFromDate());
			pst.setString(36, info.getToDate());
			pst.setString(37, info.getToDate());
			
			pst.setString(38,info.getFromDate());
			pst.setString(39,info.getFromDate());
			
			pst.setString(40,info.getFromDate());
			pst.setString(41,info.getFromDate());
			
			pst.setString(42,info.getFromDate());
			pst.setString(43,info.getFromDate());
			
			pst.setString(44,info.getFromDate());
			pst.setString(45,info.getFromDate());
			
			pst.setString(46,info.getFromDate());
			pst.setString(47,info.getFromDate());
			
			pst.setString(48, info.getFromDate());
			pst.setString(49, info.getFromDate());
			pst.setString(50, info.getTrustType() + "%");
			pst.setString(51, info.getFromDate());
			pst.setString(52, info.getFromDate());
			pst.setString(53, info.getToDate());
			pst.setString(54, info.getToDate());
			
			pst.setString(55,info.getFromDate());
			pst.setString(56,info.getFromDate());
			
			pst.setString(57,info.getFromDate());
			pst.setString(58,info.getFromDate());
			
			pst.setString(59,info.getFromDate());
			pst.setString(60,info.getFromDate());
			
			pst.setString(61,info.getFromDate());
			pst.setString(62,info.getFromDate());
			
			pst.setString(63,info.getFromDate());
			pst.setString(64,info.getFromDate());
			
			pst.setString(65, info.getFromDate());
			pst.setString(66, info.getFromDate());
			pst.setString(67, info.getTrustType() + "%");
			pst.setString(68, info.getTrustType() + "%");
			pst.setString(69, info.getFromDate());
			pst.setString(70, info.getFromDate());
			pst.setString(71, info.getToDate());
			pst.setString(72, info.getToDate());
			pst.setString(73, info.getTrustType() + "%");
			pst.setString(74, info.getFromDate());
			pst.setString(75, info.getFromDate());
			pst.setString(76, info.getToDate());
			pst.setString(77, info.getToDate());
			log.info(pst.toString());*/
			rs = st.executeQuery(query);
			while (rs.next()) {
				if (trialBal.containsKey(rs.getString("trusttype"))) {
					trustTypeBased = (HashMap) trialBal.get(rs
							.getString("trusttype"));
					if (trustTypeBased.containsKey(rs.getString("type"))) {
						accountHeads = (List) trustTypeBased.get(rs
								.getString("type"));
					} else {
						accountHeads = new ArrayList();
					}
				} else {
					trustTypeBased = new HashMap();
					accountHeads = new ArrayList();
				}
				BankBook trialBalance = new BankBook();
				trialBalance.setTrustType(rs.getString("trusttype"));
				trialBalance.setAccountHeadType(rs.getString("type"));
				trialBalance.setAccountHead(rs.getString("Accountcode"));
				trialBalance.setOpeningBalAmtDebit(rs.getDouble("openBaldebit"));
				trialBalance.setOpeningBalAmt(rs.getDouble("openBalcredit"));
				
				if(rs.getDouble("debit") >= 0 && rs.getDouble("credit") >= 0){
					trialBalance.setPayments(rs.getDouble("debit"));
					trialBalance.setReceipts(rs.getDouble("credit"));
				} else if(rs.getDouble("debit") < 0 && rs.getDouble("credit") >= 0){
					trialBalance.setPayments(0.00);
					trialBalance.setReceipts(rs.getDouble("credit")+(-1.00 * rs.getDouble("debit")));
				} else if(rs.getDouble("debit") >= 0 && rs.getDouble("credit") < 0){
					trialBalance.setPayments(rs.getDouble("debit")+(-1.00 * rs.getDouble("credit")));
					trialBalance.setReceipts(0.00);
				} else if(rs.getDouble("debit") < 0 && rs.getDouble("credit") < 0){
					trialBalance.setPayments((-1.00 * rs.getDouble("credit")));
					trialBalance.setReceipts((-1.00 * rs.getDouble("debit")));
				}
					
				
				accountHeads.add(trialBalance);
				trustTypeBased.put(rs.getString("type"), accountHeads);
				trialBal.put(rs.getString("trusttype"), trustTypeBased);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				st.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getTrialBalance : leaving method");
		return trialBal;
	}

	public List ePaymentSearch(VoucherInfo info) throws Exception {
		log.info("VoucherDAO : ePaymentSearch : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List voucherList = new ArrayList();
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("ePaymentsSearchQuery"));
			pst.setString(1, info.getFromDate());
			pst.setString(2, info.getFromDate());
			pst.setString(3, info.getToDate());
			pst.setString(4, info.getToDate());
			rs = pst.executeQuery();
			while (rs.next()) {
				info = new VoucherInfo();
				info.setBankName(rs.getString("bankname"));
				info.setFinYear(rs.getString("FYEAR"));
				info.setPartyType(rs.getString("PARTYTYPE"));
				info.setKeyNo(rs.getString("keyno"));
				info.setTrustType(rs.getString("TRUSTTYPE"));
				info.setVoucherDt(rs.getString("voucher_dt"));
				info.setVoucherNo(rs.getString("VOUCHERNO"));
				voucherList.add(info);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : ePaymentSearch : leaving method");
		return voucherList;
	}

	public void ePaymentTransfers(String keynos) throws Exception {
		log.info("VoucherDAO : ePaymentTransfers : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("ePaymentsQuery"));
			pst.setString(1, keynos);
			rs = pst.executeQuery();
			while (rs.next()) {
				
			}
			String path = createTextFile(new ArrayList());
			sendMail(path);
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : ePaymentTransfers : leaving method");
	}

	private void sendMail(String path) throws Exception {
		ResourceBundle bundle = ResourceBundle.getBundle("com.epis.resource.Mail");
		String host = bundle.getString("host");
		String authid = bundle.getString("authid");		
		String authpwd = bundle.getString("authpwd");
		
		//		 Create properties for the Session
		Properties prop=new Properties();

		// If using static Transport.send(),need to specify the mail server here
		prop.put("mail.smtp.host", host);

		prop.put("mail.smtp.auth", "true");

        //Setting Authentication Mail Server UserID and Password 
		Authenticator auth = new Authentication(authid,authpwd);

		// Get a session
		Session session = Session.getInstance(prop,auth);
		MimeMessage message = new MimeMessage(session);
		InternetAddress add=new InternetAddress("rajani.nannuri@navayuga.com","AAI ");
		message.setFrom(add);
		message.setRecipients(Message.RecipientType.TO, "jayasree.kalapala@navayuga.com");
		message.setSubject("Bill");
		message.setSentDate(new java.util.Date());
		
		StringBuffer htmlText = new StringBuffer("<table><tr><td><font face='arial'>Dear Sir/Madam,<br>");
		htmlText.append("<br>Please find the Attached E-Payments.<BR>");
		htmlText.append("<BR><BR>Best Regards<BR>AAI<BR></font></td></tr>");
		
		BodyPart bodyPart1 = new MimeBodyPart();
		bodyPart1.setContent(htmlText.toString(), "text/html");
		
		//Adds Attechment:
		BodyPart bodyPart2 = new MimeBodyPart();
		
		//first attachment
		DataSource source = new FileDataSource(path);
		bodyPart2.setDataHandler(new DataHandler(source));
		bodyPart2.setFileName("E-Payments.txt"); 

		Multipart multipart = new MimeMultipart();

		//Create a related multi-part to combine the parts
		multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

		message.setContent(multipart);
		Transport.send(message);
	}

	public String createTextFile(List ePayments) {
		Writer writer = null;
		File file = null;
		try {
			String text = "This is a text file";
			file = new File("write.txt");			
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(text);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file.getAbsolutePath();
	}
	
	public Map getAccountHeadsOpeningBalance(VoucherInfo vinfo) throws Exception {
		log.info("VoucherDAO : getAccountHeadsOpeningBalance : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		Map accounts = new LinkedHashMap();
		Map trustTypes = null;
		try {
			con = DBUtility.getConnection();
			pst = con.prepareStatement(getQueries("accountOpenBalQuery"));
			//System.out.println(getQueries("accountOpenBalQuery"));
			pst.setString(1, vinfo.getFromDate());
			pst.setString(2, vinfo.getFromDate());
			pst.setString(3, vinfo.getTrustType()+"%");
			pst.setString(4, vinfo.getAccountHead());
			pst.setString(5, vinfo.getAccountHead());
			pst.setString(6,vinfo.getToAccountHead());
			pst.setString(7,vinfo.getToAccountHead());
			rs = pst.executeQuery();
			while (rs.next()) {
				if(accounts.containsKey(rs.getString("ACCOUNTHEAD"))){
					trustTypes = (LinkedHashMap)accounts.get(rs.getString("ACCOUNTHEAD"));
				}else{
					trustTypes = new LinkedHashMap();
				}
				if("AIN".equals(rs.getString("TRUSTTYPE"))){
					if("".equals(vinfo.getTrustType()) || "A".equals(vinfo.getTrustType())){
						AccountingCodeInfo info = new AccountingCodeInfo();					
						info.setAccountHead(rs.getString("ACCOUNTHEAD"));
						info.setTrustType("AAI EPF");
						info.setAmount(rs.getString("openbal"));
						trustTypes.put("AAI EPF",info);
					}
					if("".equals(vinfo.getTrustType()) || "I".equals(vinfo.getTrustType())){
						AccountingCodeInfo info = new AccountingCodeInfo();
						info.setAccountHead(rs.getString("ACCOUNTHEAD"));
						info.setTrustType("IAAI ECPF");
						info.setAmount(rs.getString("openbal"));
						trustTypes.put("IAAI ECPF",info);
					}
					if("".equals(vinfo.getTrustType()) || "N".equals(vinfo.getTrustType())){
						AccountingCodeInfo info = new AccountingCodeInfo();
						info.setAccountHead(rs.getString("ACCOUNTHEAD"));
						info.setTrustType("NAA ECPF");
						info.setAmount(rs.getString("openbal"));
						trustTypes.put("NAA ECPF",info);
					}
				}else {
					AccountingCodeInfo info = new AccountingCodeInfo();
					info.setAccountHead(rs.getString("ACCOUNTHEAD"));
					info.setTrustType(rs.getString("TRUSTTYPE"));
					info.setAmount(rs.getString("openbal"));
					trustTypes.put(rs.getString("TRUSTTYPE"),info);
				}
				accounts.put(rs.getString("ACCOUNTHEAD"),trustTypes);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getAccountHeadsOpeningBalance : leaving method");
		return accounts;
	}
	
	public void updateNotiVoucher(String transid,String keyno) throws EPISException {
		String otherModuleLink = transid.substring(transid.indexOf("|")+1);
		transid = transid.substring(0,transid.indexOf("|"));
		Connection con = null;
		PreparedStatement pst = null;
		try{
			con = DBUtility.getConnection();
			String query = "update cb_voucher_info set TRANSID = ? ,OTHERMODULELINK= ? where keyno=? ";
			pst = con.prepareStatement(query);
			pst.setString(1, transid);
			pst.setString(2, otherModuleLink);
			pst.setString(3, keyno);
			pst.executeUpdate();			
		}catch (Exception e) {
			log.printStackTrace(e);
			throw new EPISException(e.toString());
		}finally{
			try {
				pst.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			con = DBUtility.getConnection();
			if("FS".equals(otherModuleLink) || "FSA".equals(otherModuleLink)){
				String query = "update employee_advance_noteparam  set RAISEPAYMENT = 'Y',COUNTVOUCHER=Nvl(COUNTVOUCHER,0)+1  where nssanctionno=?";
				pst = con.prepareStatement(query);
			}else {
				String query = "update employee_advances_form set RAISEPAYMENT = 'Y',COUNTVOUCHER=Nvl(COUNTVOUCHER,0)+1 where ADVANCETRANSID=?";
				pst = con.prepareStatement(query);
			}
			pst.setString(1, transid);
			pst.executeUpdate();			
		}catch (Exception e) {
			log.printStackTrace(e);
			throw new EPISException(e.toString());
		}finally{
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}	
	private String getQueries(String queryName){
		
		HashMap queries = new HashMap();
		
		StringBuffer insertQuery = new StringBuffer("insert into Cb_VOUCHER_INFO(KEYNO,ACCOUNTNO,FYEAR,TRUSTTYPE");
		insertQuery.append(" ,VOUCHERTYPE,PARTYTYPE,EMP_PARTY_CODE,PREPAREDBY,details,preperation_dt,ecpfacno,");
		insertQuery.append(" eregion,pfidFlag,EMPLOYEENAME,TRANSACTIONTYPE,ENTEREDBY,UNITCODE,EUNITCODE,");
		insertQuery.append(" otherModuleLink,TRANSID,PURPOSETYPE,APPROVAL,ISIN) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		queries.put("insertQuery",insertQuery);

		StringBuffer insertDtQuery = new StringBuffer("insert into Cb_voucher_details(KEYNO,ACCOUNTHEAD,MONTH_YEAR,");
		insertDtQuery.append(" credit,Details,debit,chequeno,ENTEREDBY,UNITCODE,Regard) values (?,?,?,?,?,?,?,?,?,?)");
		queries.put("insertDtQuery",insertDtQuery);

		StringBuffer selectQuery = new StringBuffer("select nvl(voucher.isin,' ') isin,VOUCHER_DT voucher_dt,to_char(preperation_dt,");
		selectQuery.append(" 'dd/Mon/YYYY') preperation_dt,voucherNo,KEYNO,BANKNAME,FYEAR,VOUCHERTYPE vtype,Decode(VOUCHERTYPE");
		selectQuery.append(" ,'P','Payment','R','Receipt','C','Contra','') VOUCHERTYPE,Decode(PARTYTYPE,'E',");
		selectQuery.append(" 'Employee','P','Party','C','Cash','I','Invest Party','B','Bank','') PARTYTYPE,( select case when ");
		selectQuery.append(" voucher.vouchertype='P' then sum(dt.debit-dt.credit)||' Dr.' else ");
		selectQuery.append(" sum(dt.credit-dt.debit)||' Cr.' end from Cb_voucher_details dt where dt.keyno = ");
		selectQuery.append(" voucher.keyno)Amount,(case when VOUCHER_DT > '08/Jun/2010' and partytype='E' then TRANSID else 1 end) TRANSID from  Cb_VOUCHER_INFO voucher,CB_BANK_INFO bank where APPROVAL <>'R' and bank.ACCOUNTNO");
		selectQuery.append(" = voucher.ACCOUNTNO and upper(BANKNAME) like upper(?) and FYEAR like ? and");
		selectQuery.append(" VOUCHERTYPE like ? and upper(voucher.ACCOUNTNO) like upper(?) and upper(voucher.EMP_PARTY_CODE) like upper(?)  ");
		queries.put("selectQuery",selectQuery);

		StringBuffer selectAppQuery = new StringBuffer("select to_char(VOUCHER_DT,'dd/Mon/YYYY') voucher_dt,voucher.isin as isin,");
		selectAppQuery.append(" to_char(preperation_dt,'dd/Mon/YYYY') preperation_dt,voucherNo,KEYNO,BANKNAME,");
		selectAppQuery.append(" FYEAR,VOUCHERTYPE vtype,Decode(VOUCHERTYPE,'P','Payment','R','Receipt','C','Contra','') VOUCHERTYPE");
		selectAppQuery.append(" ,Decode(PARTYTYPE,'E','Employee','P','Party','C','Cash','I','Invest Party','B','Bank','') PARTYTYPE ,( select ");
		selectAppQuery.append(" case when voucher.vouchertype='P' then sum(dt.debit-dt.credit)||' Dr.' else ");
		selectAppQuery.append(" sum(dt.credit-dt.debit)||' Cr.' end  from Cb_voucher_details dt where dt.keyno=");
		selectAppQuery.append(" voucher.keyno)Amount,(case when VOUCHER_DT > '08/Jun/2010' and partytype = 'E' then TRANSID else 1 end) TRANSID from  Cb_VOUCHER_INFO voucher,CB_BANK_INFO bank where"); 
		selectAppQuery.append("  bank.ACCOUNTNO = voucher.ACCOUNTNO and upper(BANKNAME) like upper(trim(?))and");
		selectAppQuery.append(" FYEAR like ? and upper(VOUCHERTYPE) like upper(?) and voucherno is null and APPROVAL <>'R'  ");
		selectAppQuery.append(" and upper(voucher.ACCOUNTNO) like upper(?) and upper(voucher.keyno) like upper(?) ");
		//selectAppQuery.append(" order by voucher.ENTEREDDT ");
		queries.put("selectAppQuery",selectAppQuery);

		StringBuffer reportQuery = new StringBuffer(" SELECT Nvl(TRANSACTIONTYPE,'C') TRANSACTIONTYPE,nvl(voucher.isin,' ') isin,");
		reportQuery.append(" Nvl(voucher.voucherNo,' ') voucherNo,voucher.accountNo accountNo,keyno, bankname,nvl(OTHERMODULELINK,'N')OTHERMODULELINK,");
		reportQuery.append(" fyear,VOUCHERTYPE vtype,DECODE (vouchertype,'P', 'Payment','R', 'Receipt','C', 'Contra',' ') ");
		reportQuery.append(" vouchertype,Nvl(partytype,' ') partytype,voucher.trusttype trusttype,(CASE WHEN partytype =");
		reportQuery.append(" 'E' THEN ecpfacno  WHEN partytype = 'B' THEN  (SELECT ");
		reportQuery.append(" bankname FROM CB_BANK_INFO WHERE accountno = emp_party_code) else emp_party_code  END )");
		reportQuery.append(" emp_party_code,emp_party_code partyDt,(CASE  WHEN partytype = 'E' and voucher.PFIDFLAG='true' THEN  ");
		reportQuery.append(" employeename WHEN partytype = 'E'   THEN (SELECT employeename FROM ");
		reportQuery.append(" employee_personal_info WHERE pensionno = emp_party_code) ELSE   emp_party_code ");
		reportQuery.append(" END ) partyDetails,   Nvl(USERNAME,' ') preparedby, Nvl(voucherno,' ') voucherno,");
		reportQuery.append(" Nvl(checkedby,' ') checkedby, Nvl(approvedby,' ') approvedby,Nvl(details,' ') details");
		reportQuery.append(" ,Nvl(to_char(VOUCHER_DT,'dd/Mon/YYYY'),' ') VOUCHER_DT,APPROVAL,to_char(preperation_dt");
		reportQuery.append(" ,'dd/Mon/YYYY') preperation_dt FROM Cb_VOUCHER_INFO voucher, CB_BANK_INFO bank,epis_user WHERE USERID(+) = preparedby and ");
		reportQuery.append(" bank.accountno = voucher.accountno AND upper(trim(keyno)) = upper(trim(?)) ");
		queries.put("reportQuery",reportQuery);
		
		StringBuffer multipleVoucherQuery=new StringBuffer("SELECT Nvl(TRANSACTIONTYPE,'C') TRANSACTIONTYPE, Nvl(voucher.voucherNo,' ') voucherNo,");
		multipleVoucherQuery.append("voucher.accountNo accountNo,keyno, bankname, fyear,DECODE (vouchertype,'P', 'Payment','R', 'Receipt','C', 'Contra',' ')  vouchertype,Nvl(partytype,' ') partytype,voucher.trusttype trusttype,(CASE WHEN partytype = 'E' THEN ecpfacno  WHEN partytype = 'B' THEN  (SELECT  bankname FROM CB_BANK_INFO WHERE accountno = emp_party_code) else emp_party_code  END ) emp_party_code,emp_party_code partyDt,(CASE  WHEN partytype = 'E' and voucher.PFIDFLAG='true' THEN   employeename WHEN partytype = 'E'   THEN (SELECT employeename FROM  employee_personal_info WHERE pensionno = emp_party_code) ELSE   emp_party_code  END ) partyDetails,   Nvl(USERNAME,' ') preparedby, Nvl(voucherno,' ') voucherno, Nvl(checkedby,' ') checkedby, Nvl(approvedby,' ') approvedby,Nvl(details,' ') details ,Nvl(to_char(VOUCHER_DT,'dd/Mon/YYYY'),' ') VOUCHER_DT,APPROVAL,to_char(preperation_dt ,'dd/Mon/YYYY') preperation_dt FROM Cb_VOUCHER_INFO voucher, CB_BANK_INFO bank,epis_user WHERE USERID(+) = preparedby and bank.accountno = voucher.accountno ");
		multipleVoucherQuery.append("AND voucher.FYEAR=? and  voucher.VOUCHERTYPE=?");
		multipleVoucherQuery.append(" and  substr(voucher.voucherno,instr(voucher.voucherno,'/',1,2)+1,length(voucher.voucherno))  between ?  and ? and upper(voucher.ACCOUNTNO) like upper(?)  order by substr(voucher.voucherno,instr(voucher.voucherno,'/',1,2)+1,length(voucher.voucherno))");
		
		queries.put("multipleVoucherQuery",multipleVoucherQuery);

		StringBuffer reportDtQuery = new StringBuffer("SELECT voucher.rowid,voucher.ACCOUNTHEAD ACCOUNTHEAD,acc.PARTICULAR");
		reportDtQuery.append(" PARTICULAR,MONTH_YEAR,Nvl(details,' ') details,Nvl(credit,0.0) credit,Nvl(debit,0.0)");
		reportDtQuery.append(" debit,Nvl(chequeno,'0') chequeno,Nvl(Regard,' ') Regard FROM Cb_voucher_details voucher, CB_ACCOUNTCODE_INFO acc");
		reportDtQuery.append(" WHERE voucher.ACCOUNTHEAD = acc.ACCOUNTHEAD AND upper(trim(keyno)) = upper(trim(?)) ");
		queries.put("reportDtQuery",reportDtQuery);

		StringBuffer updateQuery = new StringBuffer("update Cb_VOUCHER_INFO set VOUCHER_DT=?,CHECKEDBY=?,APPROVAL=?");
		updateQuery.append(" ,APPROVEDBY=?,voucherno=?,ApprovalBY=?,ApprovedDDT=sysdate where KEYNO=?");
		queries.put("updateQuery",updateQuery);

		StringBuffer bankOpenBalQuery = new StringBuffer("select (case when Nvl((select amountType from ");
		bankOpenBalQuery.append(" cb_bankopeningbal_info  where accountno = ? and openeddate <= ?),'CR') = 'DR' then");
		bankOpenBalQuery.append(" (NVl((select amount from cb_bankopeningbal_info where accountno = ? and openeddate ");
		bankOpenBalQuery.append(" <= ?),  0) + NVL(ncredit, 0) - Nvl(ndebit, 0)) else (NVl((select amount from ");
		bankOpenBalQuery.append(" cb_bankopeningbal_info where accountno = ? and openeddate <= ?),  0) - NVL(ncredit,");
		bankOpenBalQuery.append(" 0) + Nvl(ndebit, 0))  end) OpeningBanace, (select bankname from CB_BANK_INFO where");
		bankOpenBalQuery.append(" accountno = ?) bankname,  decode((select amountType from cb_bankopeningbal_info");
		bankOpenBalQuery.append(" where accountno = ? and openeddate <= ?), 'DR', 'Dr.', 'Cr.') amountType from");
		bankOpenBalQuery.append(" (select sum(decode(?||'C',info.accountno||info.vouchertype,nvl(credit, 0), ");
		bankOpenBalQuery.append(" nvl(debit, 0))) nDebit,sum(decode(?||'C',info.accountno||info.vouchertype,");
		bankOpenBalQuery.append(" nvl(debit, 0), nvl(credit, 0))) nCredit from Cb_voucher_details dt,(select keyno,");
		bankOpenBalQuery.append(" info.accountno,   vouchertype, voucher_dt,emp_party_code from Cb_VOUCHER_INFO");
		bankOpenBalQuery.append(" info,cb_bankopeningbal_info openbal where (info.accountno = ? or ");
		bankOpenBalQuery.append(" (info.emp_party_code = ? and info.vouchertype = 'C')) and voucher_dt is not");
		bankOpenBalQuery.append(" null and  info.voucher_dt < ? and info.accountno = openbal.accountno(+) and ");
		bankOpenBalQuery.append("info.voucher_dt >= openbal.openeddate) info where dt.keyno = info.keyno)");
		queries.put("bankOpenBalQuery",bankOpenBalQuery);

		StringBuffer bankBookQuery = new StringBuffer("SELECT info.keyno, dt.accounthead, InitCap(acc.particular)");
		bankBookQuery.append(" particular, decode(?,emp_party_code,debit,credit) credit,decode(?,emp_party_code,");
		bankBookQuery.append(" credit,debit) debit, InitCap(Nvl(info.DETAILS,' ')) details, vouchertype,");
		bankBookQuery.append(" InitCap(partyname) partyname,info.voucherno,to_char(info.voucher_dt,'dd/Mon/YYYY')");
		bankBookQuery.append(" voucher_dt,Nvl(chequeno,'0') chequeno FROM Cb_voucher_details dt,(SELECT DETAILS,");
		bankBookQuery.append(" keyno, vouchertype,(CASE WHEN partytype = 'E' THEN (SELECT employeename FROM ");
		bankBookQuery.append(" employee_personal_info WHERE pensionno = emp_party_code) WHEN vouchertype = 'C'");
		bankBookQuery.append(" AND emp_party_code != ? THEN (SELECT bankname FROM CB_BANK_INFO WHERE accountno =");
		bankBookQuery.append(" emp_party_code) WHEN partytype = 'B' THEN (SELECT bankname FROM CB_BANK_INFO WHERE ");
		bankBookQuery.append(" accountno = info.accountno) ELSE  emp_party_code END ) partyname, ? accountno,");
		bankBookQuery.append(" voucherno,voucher_dt,emp_party_code,partytype FROM Cb_VOUCHER_INFO info WHERE ");
		bankBookQuery.append(" APPROVAL='Y' and (accountno = ? OR (vouchertype = 'C' AND emp_party_code = ?))");
		bankBookQuery.append(" and voucherno is not null and voucher_dt between ? and ?) info,cb_accountcode_info");
		bankBookQuery.append(" acc WHERE info.keyno = dt.keyno and acc.ACCOUNTHEAD = dt.accounthead order by ");
		bankBookQuery.append(" info.voucher_dt,info.voucherno");
		queries.put("bankBookQuery",bankBookQuery);

		StringBuffer editQuery = new StringBuffer("select *,( select case when voucher.vouchertype='P' then ");
		editQuery.append(" sum(dt.debit-dt.credit)||' Dr.' else sum(dt.credit-dt.debit)||' Cr.' end from ");
		editQuery.append(" Cb_voucher_details dt where dt.keyno=voucher.keyno)Amount from  Cb_VOUCHER_INFO voucher,");
		editQuery.append(" CB_BANK_INFO bank where bank.ACCOUNTNO = voucher.ACCOUNTNO and  KEYNO=?");
		queries.put("editQuery",editQuery);

		StringBuffer updateVoucherQry = new StringBuffer("update  Cb_VOUCHER_INFO set ACCOUNTNO=?,FYEAR=?,TRUSTTYPE=?");
		updateVoucherQry.append(" ,PARTYTYPE=?,EMP_PARTY_CODE=?,PREPAREDBY=?,details=?,TRANSACTIONTYPE=? ");
		updateVoucherQry.append(" ,EDITEDBY=?,EDITEDDT=sysdate,preperation_dt=to_date(?,'dd/MM/YYYY'),isin=? where KEYNO=?");
		queries.put("updateVoucherQry",updateVoucherQry);

		StringBuffer deleteDtQuery = new StringBuffer(" delete from  Cb_voucher_details  where KEYNO=?");
		queries.put("deleteDtQuery",deleteDtQuery);

		StringBuffer deleteVQuery = new StringBuffer("delete from  Cb_VOUCHER_INFO where INSTR(upper(?),upper(KEYNO)) > 0");
		queries.put("deleteVQuery",deleteVQuery);
		
		StringBuffer deleteVDTQuery = new StringBuffer(" delete from  Cb_voucher_details  where INSTR(upper(?),upper(KEYNO)) > 0");
		queries.put("deleteVDTQuery",deleteVDTQuery);

		StringBuffer voucherNoQuery = new StringBuffer("SELECT vouchertype || 'V/' ||(select SUBSTR(BANKNAME, 0, 3)");
		voucherNoQuery.append(" BANKNAME from cb_bank_info bank where bank.accountno = info.accountno) || '/' ");
		voucherNoQuery.append(" || LPAD((SELECT MAX(TO_NUMBER(NVL(SUBSTR(voucherno,INSTR(voucherno, '/', -1) + ");
		voucherNoQuery.append(" 1),0))) + 1 FROM Cb_VOUCHER_INFO vinfo where vinfo.vouchertype= info.vouchertype and info.fyear=vinfo.fyear ");
		voucherNoQuery.append(" and info.accountno = vinfo.accountno),4,0) FROM Cb_VOUCHER_INFO info WHERE  upper(keyno)= upper(?)");
		queries.put("voucherNoQuery",voucherNoQuery);

		StringBuffer keyNoGenQuery = new StringBuffer("select to_char(to_date(?,'dd/mon/yyyy'),'ddmmyy')||");
		keyNoGenQuery.append(" Lpad(Nvl(max(substr(KEYNO,7)),0)+1,6,'0') voucherNo from Cb_VOUCHER_INFO where ");
		keyNoGenQuery.append(" to_char(to_date(?,'dd/mon/yyyy'),'ddmmyy')=substr(KEYNO,0,6)");
		queries.put("keyNoGenQuery",keyNoGenQuery);

		StringBuffer ledgerQuery = new StringBuffer("SELECT   TO_CHAR (voucher_dt, 'dd/Mon/YYYY') vdate, ");
		ledgerQuery.append(" info.accountno accno,bank.bankcode || ' ' || bank.bankname bank, info.details ");
		ledgerQuery.append(" particulars,decode(vouchertype,'P','Payment','R','Receipt','Contra') vtype, ");
		ledgerQuery.append(" voucherno NO, case when vouchertype='P' then SUM (NVL (dt.debit, 0))-SUM (NVL");
		ledgerQuery.append(" (dt.credit, 0)) else 0 end dt,case when vouchertype='R' then SUM (NVL (dt.credit,");
		ledgerQuery.append(" 0))-SUM (NVL (dt.debit, 0)) when vouchertype='C' then SUM (NVL (dt.credit, 0)) else");
		ledgerQuery.append(" 0 end cr FROM Cb_VOUCHER_INFO info, Cb_voucher_details dt, CB_BANK_INFO bank WHERE voucherno");
		ledgerQuery.append(" IS NOT NULL AND voucherno != 'REJECTED' AND bank.accountno = info.accountno AND");
		ledgerQuery.append(" info.keyno = dt.keyno and voucher_dt between decode(?,'','01-JAN-1000',?) and ");
		ledgerQuery.append(" decode(?,'','31-JAN-9999',?) and voucherType like ? AND UPPER (bank.bankcode)");
		ledgerQuery.append(" LIKE UPPER (?) AND UPPER (bank.trusttype) LIKE UPPER (?) GROUP BY voucher_dt,");
		ledgerQuery.append(" info.accountno,bank.bankcode,bank.bankname,info.details,vouchertype,voucherno ");
		ledgerQuery.append(" ORDER BY voucher_dt, voucherno");
		queries.put("ledgerQuery",ledgerQuery);

		StringBuffer accNosQuery = new StringBuffer("SELECT accountno from CB_BANK_INFO where accountcode = ? ");
		queries.put("accNosQuery",accNosQuery);

		StringBuffer genLedgerQuery = new StringBuffer("select * from ( Select vinfo.TRUSTTYPE trusttype,vdt.accounthead || ' ' || ainfo.particular");
		genLedgerQuery.append(" Accountcode,to_char(vinfo.voucher_dt, 'dd/Mon/YYYY') vdate,(binfo.accountcode ");
		genLedgerQuery.append(" || ' ' || binfo.bankname) bank, trim(vinfo.details) details, decode(VOUCHERTYPE,");
		genLedgerQuery.append(" 'R', 'Receipt', 'P', 'Payment', 'C', 'Contra') vtype, vinfo.voucherno,");
		genLedgerQuery.append(" (case when (vinfo.vouchertype='P' and sum(vdt.debit)>sum(vdt.credit)) then sum(vdt.debit)-sum(vdt.credit) when(vinfo.vouchertype='P' and sum(vdt.debit)<sum(vdt.credit)) then sum(vdt.credit)-sum(vdt.debit) end)  debit,(case when (vinfo.vouchertype='P' and sum(vdt.debit)>sum(vdt.credit)) then 'DR' when(vinfo.vouchertype='P' and sum(vdt.debit)<sum(vdt.credit)) then 'DC' end)  debittype, (case when ((vinfo.vouchertype='R' or vinfo.vouchertype='C')and sum(vdt.credit)>sum(vdt.debit)) then sum(vdt.credit)-sum(vdt.debit) when((vinfo.vouchertype='R' or vinfo.vouchertype='C') and sum(vdt.credit)<sum(vdt.debit)) then sum(vdt.debit)-sum(vdt.credit) end)  credit,(case when ((vinfo.vouchertype='R'or vinfo.vouchertype='C') and sum(vdt.credit)>sum(vdt.debit)) then 'CR' when((vinfo.vouchertype='R'or vinfo.vouchertype='C') and sum(vdt.credit)<sum(vdt.debit)) then 'CD' end)  credittype,");
		genLedgerQuery.append(" getAccheadopenbal( vdt.accounthead,vinfo.TRUSTTYPE,?,'') openingBal,(CASE WHEN ");
		genLedgerQuery.append(" partytype = 'E' and vinfo.PFIDFLAG='true' THEN EMPLOYEENAME WHEN partytype = ");
		genLedgerQuery.append(" 'E' THEN (SELECT EMPLOYEENAME FROM employee_personal_info WHERE PENSIONNO = ");
		genLedgerQuery.append(" emp_party_code) WHEN partytype = 'B' THEN  (SELECT bankname FROM CB_BANK_INFO ");
		genLedgerQuery.append(" WHERE accountno = emp_party_code) else emp_party_code  END ) partyname,Nvl((select");
		genLedgerQuery.append(" chequeno from Cb_voucher_details where keyno=''||vinfo.keyno||'' and rownum<2),' ') chequeNo,vinfo.FYEAR FYEAR  from ");
		genLedgerQuery.append(" Cb_VOUCHER_INFO  vinfo, Cb_voucher_details  vdt, CB_BANK_INFO binfo, cb_accountcode_info ");
		genLedgerQuery.append(" ainfo where ainfo.accounthead = vdt.accounthead and vdt.accounthead not in('661.00','662.00','663.00','664.00') and binfo.accountno = ");
		genLedgerQuery.append(" vinfo.ACCOUNTNO and vdt.keyno = vinfo.keyno and vinfo.keyno in (select keyno");
		genLedgerQuery.append(" from Cb_VOUCHER_INFO info where info.voucherno is not null) and upper(trim(");
		genLedgerQuery.append(" vinfo.trustType)) like upper(trim(?)) and upper(vdt.accounthead) between decode(upper(trim(?)), '', '100.00', upper(trim(?))) and decode(upper(trim(?)), '', '999.07', upper(trim(?)))");
		genLedgerQuery.append(" and upper(trim(vinfo.vouchertype)) like upper(trim(?)) and ");
		genLedgerQuery.append(" vinfo.voucher_dt between decode(?, '', '01/Jan/1000', ?) and decode(?, '', ");
		genLedgerQuery.append(" '01/Jan/3000', ?) group by vinfo.voucherno, vinfo.voucher_dt, binfo.accountcode,");
		genLedgerQuery.append(" binfo.bankname, vinfo.details, vinfo.TRUSTTYPE, VOUCHERTYPE, vdt.accounthead,");
		genLedgerQuery.append(" ainfo.particular,partytype,vinfo.PFIDFLAG,EMPLOYEENAME,emp_party_code,vinfo.keyno,vinfo.FYEAR");
		genLedgerQuery.append(" union all select trusttype,jvdt.accountcode || ' ' || accinfo.particular Accountcode,");
		genLedgerQuery.append(" to_char(jv.voucher_dt, 'dd/Mon/YYYY') vdate,jvdt.accountcode || ' ' || accinfo.particular bank,");
		genLedgerQuery.append(" jv.details details,'Journal' vtype,voucherno,debit,'DR'debittype,credit,'CR'credittype,Nvl(getAccheadopenbal( jvdt.accountcode,jv.TRUSTTYPE,?,''),0) openingBal,");
		genLedgerQuery.append(" jv.emp_pfid || jv.party_code || jv.other partyname,' ' chequeNo,FINYEAR FYEAR from cb_journalvoucher jv,");
		genLedgerQuery.append(" cb_journalvoucher_details jvdt,cb_accountcode_info accinfo where jvdt.keyno = jv.keyno and accinfo.accounthead = jvdt.accountcode and jvdt.accountcode not in('661.00','662.00','663.00','664.00')  and ");
		genLedgerQuery.append(" approval = 'Y' and upper(trim(trustType)) like upper(trim(?)) and  upper(jvdt.ACCOUNTCODE) between decode(upper(trim(?)), '', '100.00', upper(trim(?))) and decode(upper(trim(?)), '', '999.07', upper(trim(?))) ");
		genLedgerQuery.append("  and 'J' like ? and jv.voucher_dt between decode(?, '', '01/Jan/1000', ?) and decode(?, '', ");
		genLedgerQuery.append(" '01/Jan/3000', ?))  order by Accountcode, to_date(vdate),vtype desc,to_number(substr(VOUCHERNO, instr(VOUCHERNO, '/', -1) + 1)) ");
		queries.put("genLedgerQuery",genLedgerQuery);

		StringBuffer trailBalQuery = new StringBuffer("select type,Accountcode,trusttype,openBalcredit, ");
		trailBalQuery.append(" openBaldebit,sum(credit)+Nvl((select case when sum(credit) - sum(debit)<0 then  sum(debit)-sum(credit) else 0 end  from cb_voucher_info info,cb_voucher_details details,cb_bank_info binfo where details.keyno = info.keyno and binfo.accountno = info.accountno and upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?, '', '01/jan/1000', ?) and decode(?, '', '01/jan/3000', ?) and upper(trim(binfo.trusttype)) = upper(trim(vouinfo.trusttype)) and   binfo.accountcode = vouinfo.accounthead group by info.accountno, binfo.accountcode, binfo.trusttype),0)-Nvl((select sum(credit) from cb_voucher_info info, cb_voucher_details  det,");
		trailBalQuery.append(" cb_bank_info bank where vouchertype = 'C' and det.keyno = info.keyno and bank.accountno = info.emp_party_code and ");
		trailBalQuery.append(" upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?, '', '01/jan/1000', ?) and ");
		trailBalQuery.append(" decode(?, '', '01/jan/3000',?) and  bank.accountcode = vouinfo.accounthead),0) credit,sum(debit)+Nvl((select case when sum(credit) - sum(debit)>0 then  sum(credit) - sum(debit)  else 0 end from cb_voucher_info info,cb_voucher_details details,cb_bank_info binfo where details.keyno = info.keyno and binfo.accountno = info.accountno and upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?, '', '01/jan/1000', ?) and decode(?, '', '01/jan/3000', ?) and upper(trim(binfo.trusttype)) = upper(trim(vouinfo.trusttype)) and   binfo.accountcode = vouinfo.accounthead group by info.accountno, binfo.accountcode, binfo.trusttype),0)-Nvl((select sum(credit) from cb_voucher_info info, cb_voucher_details  det,");
		trailBalQuery.append(" cb_bank_info bank where vouchertype = 'C' and det.keyno = info.keyno and bank.accountno = info.accountno and ");
		trailBalQuery.append(" upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?, '', '01/jan/1000', ?) and ");
		trailBalQuery.append(" decode(?, '', '01/jan/3000',?) and det.accounthead = vouinfo.accounthead),0) debit from (select ACCOUNTCODETYPE ");
		trailBalQuery.append(" type,upper(dt.accounthead || ' ' || ainfo.particular) Accountcode,dt.accounthead accounthead,info.trusttype ");
		
		trailBalQuery.append(" trusttype,(case when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accounthead) when dt.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL(?,?)='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 where ACCOUNTHEAD=dt.accounthead) else getAccHeadOpenBal(dt.accounthead, info.trusttype, ?, 'C') end) openBalcredit,");
		trailBalQuery.append(" (case when dt.accounthead in('667.02') and  GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accounthead)  else getAccHeadOpenBal(dt.accounthead, info.trusttype, ?, 'D') end) openBaldebit, ");
		trailBalQuery.append(" sum(credit) credit, sum(debit) debit from Cb_VOUCHER_INFO info,Cb_voucher_details dt,");
		trailBalQuery.append(" cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ");
		trailBalQuery.append(" ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accounthead and ");
		trailBalQuery.append(" upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between ");
		trailBalQuery.append(" decode(?, '', '01/jan/1000', ?) and decode(?, '', '01/jan/3000', ?) group by ");
		trailBalQuery.append(" dt.accounthead, info.trusttype,  ainfo.particular,ACCOUNTCODETYPE union all select");
		trailBalQuery.append(" ACCOUNTCODETYPE type, upper(dt.accountcode || ' ' || ainfo.particular) ");
		
		trailBalQuery.append(" Accountcode,dt.accountcode accounthead, info.trusttype trusttype,(case when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accountcode) when dt.accountcode in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL(?,?)='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 where ACCOUNTHEAD=dt.accountcode) else  getAccHeadOpenBal(dt.accountcode,info.trusttype, ?, 'C')end) ");
		trailBalQuery.append("  openBalcredit, (case when dt.accountcode in('667.02') and  GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=dt.accountcode) else  getAccHeadOpenBal(dt.accountcode,");
		trailBalQuery.append(" info.trusttype, ?, 'D') end) openBaldebit,sum(credit) credit,sum(debit) debit from ");
		trailBalQuery.append(" Cb_Journalvoucher info,Cb_Journalvoucher_Details dt, cb_accountcode_info ainfo, ");
		trailBalQuery.append(" cb_accountcodetype_info acctype where acctype.code = ainfo.type  and dt.keyno = ");
		trailBalQuery.append(" info.keyno and ainfo.accounthead = dt.accountcode and upper(trim(info.trusttype)) ");
		trailBalQuery.append(" like upper(trim(?)) and info.voucher_dt between decode(?, '', '01/jan/1000', ?) ");
		trailBalQuery.append(" and decode(?, '', '01/jan/3000', ?) group by dt.accountcode, info.trusttype, ");
		
		trailBalQuery.append(" ainfo.particular,ACCOUNTCODETYPE union all select ACCOUNTCODETYPE type,upper(det.accounthead||' '||info.particular) Accountcode,det.accounthead accounthead,det.trusttype trusttype,(case when det.accounthead in('402.00','403.00','444.00')  and GET_FINYEAR_VAL(?,?)='2012-2013' then (select AMOUNT from cb_temp_accountcode_details where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00')  and GET_FINYEAR_VAL(?,?)='2013-2014' then (select AMOUNT from cb_temp_accountcode_details2 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00','313.00') and GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=det.accounthead) when det.accounthead in('402.00','403.00','444.00') and GET_FINYEAR_VAL(?,?)='2015-2016' then (select AMOUNT from cb_temp_accountcode_details4 where ACCOUNTHEAD=det.accounthead) else   getAccHeadOpenBal(det.accounthead,det.trusttype,?,'C') end) openBalcredit, (case when det.accounthead in('667.02') and  GET_FINYEAR_VAL(?,?)='2014-2015' then (select AMOUNT from cb_temp_accountcode_details3 where ACCOUNTHEAD=det.accounthead) else     getAccHeadOpenBal(det.accounthead,det.trusttype,?,'D') end) openBaldebit,0 credit,0 debit from cb_accountcode_details det,cb_accountcode_info info,cb_accountcodetype_info acctype where info.accounthead = det.accounthead and upper(trim(det.trusttype)) like upper(trim(?)) and acctype.code = info.type and det.accounthead || ' ' || det.trusttype not in ( select dt.accountcode || ' ' || info.trusttype from Cb_Journalvoucher info,Cb_Journalvoucher_Details dt,cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accountcode and upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?,'','01/jan/1000',?) and decode(?,'','01/jan/3000',?) group by dt.accountcode,info.trusttype union select dt.accounthead || ' ' || info.trusttype from Cb_VOUCHER_INFO info,Cb_voucher_details dt,cb_accountcode_info ainfo,cb_accountcodetype_info acctype where acctype.code = ainfo.type and dt.keyno = info.keyno and ainfo.accounthead = dt.accounthead and upper(trim(info.trusttype)) like upper(trim(?)) and info.voucher_dt between decode(?,'','01/jan/1000',?) and decode(?,'','01/jan/3000',?) group by dt.accounthead,info.trusttype)) vouinfo  group by type, Accountcode, trusttype, ");
		trailBalQuery.append(" openBalcredit, openBaldebit,vouinfo.accounthead order by trusttype, type, Accountcode");
		queries.put("trailBalQuery",trailBalQuery);

		StringBuffer ePaymentsSearchQuery = new StringBuffer("select keyno,bankname,FYEAR,vinfo.TRUSTTYPE TRUSTTYPE,decode(PARTYTYPE,'P','Party'");
		ePaymentsSearchQuery.append(" ,'E','Employee','B','Bank') PARTYTYPE,DETAILS,VOUCHERTYPE,VOUCHERNO,");
		ePaymentsSearchQuery.append(" to_char(voucher_dt,'dd/Mon/YYYY') voucher_dt  from Cb_VOUCHER_INFO vinfo,CB_BANK_INFO");
		ePaymentsSearchQuery.append(" binfo where binfo.accountno = vinfo.accountno and  VOUCHER_DT between decode(?,''");
		ePaymentsSearchQuery.append(" ,'01/Jan/1000',?) and decode(?,'','01/Jan/3000',?)  and VOUCHERTYPE = 'P' and APPROVAL='Y'");
		queries.put("ePaymentsSearchQuery",ePaymentsSearchQuery);

		StringBuffer ePaymentsQuery = new StringBuffer(" select * from  Cb_VOUCHER_INFO  where INSTR(upper(?),");
		ePaymentsQuery.append(" upper(KEYNO)) > 0");
		queries.put("ePaymentsQuery",ePaymentsQuery);
		
		StringBuffer accountOpenBalQuery = new StringBuffer("select ainfo.ACCOUNTHEAD|| ' ' || ainfo.particular ACCOUNTHEAD ,");
		accountOpenBalQuery.append(" TRUSTTYPE, Nvl((case when OPENDATE <= ? then decode(AMOUNTTYPE, 'CR', AMOUNT, -AMOUNT) ");
		accountOpenBalQuery.append(" else 0 end) + Nvl((select sum(det.credit) - sum(det.debit)  from Cb_voucher_details ");
		accountOpenBalQuery.append(" det,Cb_VOUCHER_INFO info where info.keyno =det.keyno and info.voucher_dt < ? ");
		accountOpenBalQuery.append(" and ainfo.accounthead = det.accounthead and details.trusttype = ");
		accountOpenBalQuery.append(" info.trusttype),0),0) openbal,particular from cb_accountcode_details details, ");
		accountOpenBalQuery.append(" cb_accountcode_info ainfo where ainfo.accounthead = details.accounthead(+) and ainfo.accounthead not in('661.00','662.00','663.00','664.00') and  ");
		accountOpenBalQuery.append(" Nvl(trusttype,' ') like ? and ainfo.ACCOUNTHEAD between decode(upper(trim(?)), '', '100.00', upper(trim(?))) and decode(upper(trim(?)), '', '999.07', upper(trim(?))) order by ainfo.accounthead, trusttype ");
		queries.put("accountOpenBalQuery",accountOpenBalQuery);
		
		StringBuffer pfwcpfQuery = new StringBuffer("update EMPLOYEE_ADVANCES_FORM set RAISEPAYMENT = 'Y', countVoucher=Nvl(countVoucher,0)+1 where ADVANCETRANSID = ?");
		queries.put("pfwcpfQuery",pfwcpfQuery);
		
		StringBuffer fsQuery = new StringBuffer("update EMPLOYEE_ADVANCE_NOTEPARAM set RAISEPAYMENT = 'Y', countVoucher=Nvl(countVoucher,0)+1 where NSSANCTIONNO = ?");
		queries.put("fsQuery",fsQuery);
		
		StringBuffer otherModuleUpdate = new StringBuffer(" select distinct case when OTHERMODULELINK='FS' then ");
		otherModuleUpdate.append(" 'update employee_advance_noteparam  set RAISEPAYMENT = decode(Nvl(countVoucher,1)-1,0,''N'',''Y'') , countVoucher=Nvl(countVoucher,1)-1  where NSSANCTIONNO");
		otherModuleUpdate.append(" in (''' else 'update employee_advances_form set RAISEPAYMENT = decode(Nvl(countVoucher,1)-1,0,''N'',''Y'') , countVoucher=Nvl(countVoucher,1)-1 where ");
		otherModuleUpdate.append(" ADVANCETRANSID  in (''' end  || join(cursor(select TRANSID from  ");
		otherModuleUpdate.append(" Cb_VOUCHER_INFO vou where TRANSID is not null and vou.OTHERMODULELINK = ");
		otherModuleUpdate.append(" info.othermodulelink and INSTR(upper(?),upper(KEYNO)) > 0 ),''',''')||''')' ");
		otherModuleUpdate.append(" from Cb_VOUCHER_INFO info where TRANSID is not null and OTHERMODULELINK  ");
		otherModuleUpdate.append(" is not null and INSTR(upper(?),upper(KEYNO)) > 0");
		queries.put("otherModuleUpdate",otherModuleUpdate);
		
		return queries.get(queryName).toString();
	}
	public List hdfcUploadReport(String  fDate,String tDate) throws Exception {
		log.info("VoucherDAO : showReport : Entering method");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List dataList = new ArrayList();
		try {
			con = DBUtility.getConnection();
			String query  ="select a.*,b.*,to_char(a.voucher_dt,'dd-Mon-yyyy') vdate   from (select Decode(i.transactiontype, 'N', 'N', 'R', 'R') as TType,i.voucherno,i.vouchertype,i.voucher_dt,transid from cb_voucher_info i) a," +
					"(select f.approvedamnt,o.name,f.purposetype,advancetransid,o.savingbnkaccno,f.advancetype,o.neftrtgscode,o.bankname,nvl(o.branchaddress,'') as branchaddress from employee_advances_form f," +
					" employee_bank_info o where f.advancetransid = o.cpfpfwtransid and f.deleteflag='N') b  where a.transid = b.advancetransid and a.voucher_dt between '"+fDate+"' and '"+tDate+"'"+
					"        union all                select a.*,b.*,to_char(a.voucher_dt,'dd-Mon-yyyy') vdate   from (select Decode(i.transactiontype, 'N', 'N', 'R', 'R') as TType,i.voucherno, " +
							"i.vouchertype,i.voucher_dt,transid from cb_voucher_info i) a,(select n.netcontribution,o.name,'' as type,n.nssanctionno,o.savingbnkaccno,n.nstype, " +
							" o.neftrtgscode,o.bankname,nvl(o.branchaddress,'') as branchaddress from employee_advance_noteparam n, employee_bank_info o  where n.nssanctionno= o.cpfpfwtransid and deleteflag='N') b  where a.transid = b.nssanctionno and a.voucher_dt  between '"+fDate+"' and '"+tDate+"'";
			
			
			
			pst = con.prepareStatement(query);
			log.info("account no in dao is "+query);
			
			rs = pst.executeQuery();
			while (rs.next()) {
				HdfcBean hbean=new HdfcBean();
				hbean.setTransactionType(rs.getString("TType"));
				hbean.setBenecode("");
				hbean.setBeneAcno(rs.getString("savingbnkaccno"));
				hbean.setInstrumentAmt(rs.getString("approvedamnt"));
				hbean.setName(rs.getString("name"));
				hbean.setInsrtuctionRefNo(rs.getString("voucherno"));
				hbean.setCustRefNo(rs.getString("voucherno"));
				hbean.setPaymentDetails(rs.getString("advancetype"));
				hbean.setChqNo("");
				hbean.setChqTrDate(rs.getString("vdate"));
				hbean.setMicrNO("");
				hbean.setIfscCode(rs.getString("neftrtgscode"));
				hbean.setBankName(rs.getString("bankname"));
				if(rs.getString("branchaddress")!=null){
				hbean.setBranchName(rs.getString("branchaddress"));
				}else{
					hbean.setBranchName("-NA- ");	
				}
				hbean.setBmailID("aaiepf@gmail.com");
				dataList.add(hbean);
			}
		} catch (SQLException e) {
			log.printStackTrace(e);
			throw e;
		} catch (Exception e) {
			log.printStackTrace(e);
			throw e;
		} finally {
			try {
				pst.close();
				con.close();
			} catch (SQLException e) {
				log.printStackTrace(e);
			}
		}
		log.info("VoucherDAO : getLedgerAccount : leaving method");
		return dataList;
	}

	
}
