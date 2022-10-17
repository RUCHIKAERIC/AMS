package com.egil.ams.dao;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.egil.ams.dao.entity.Empdetails_aes;
import com.egil.ams.dao.entity.Ndetails_aes;
import com.egil.ams.dao.impl.SpocAdminProfileDaoImpl;
import com.egil.ams.exception.ServiceLayerException;
import com.egil.ams.service.MailerService;
import com.egil.ams.service.UserService;
import com.egil.ams.service.impl.LadpServiceImpl;
import com.egil.ams.service.impl.LoginDelegate;
import com.egil.ams.service.impl.MachineUserServiceImpl;
import com.egil.ams.service.impl.ManagerServiceImpl;
import com.egil.ams.service.impl.SimpleSecurityServiceImpl;
import com.egil.ams.service.impl.SpocAdminProfileServiceImpl;
import com.egil.ams.service.impl.UserCreationServiceImpl;
import com.egil.ams.service.impl.UserServiceImpl;
import com.egil.ams.util.AmsConstants;
import com.egil.ams.web.beans.MachineUserBean;
import com.egil.ams.web.beans.ManagerBean;
import com.egil.ams.web.beans.userAddReqBean;
import com.egil.ams.web.beans.userCreationRequestBean;
import com.egil.ams.web.request.SpocAdmin;
import com.egil.ams.web.response.AreaDomain;
import com.egil.ams.web.response.DatatablePagingResponse;
import com.egil.ams.web.response.EmployeeDetailsWithSecurityInfo;
import com.egil.ams.web.response.GenericServiceResponse;
import com.egil.ams.web.response.ManagerList;

@Controller
public class SpocAdminControllerBackUp {
	Logger _logger = LoggerFactory.getLogger(SpocAdminControllerBackUp.class);

	@Value("${mail.serverIP}")
	private String serverIP;

	@Value("${maintenanceFlag}")
	private boolean maintenanceFlag;

	@Value("${mail.opcoName}")
	private String opcoName;

	@Autowired
	TaskExecutor taskExecutorService;
	@Autowired
	UserServiceImpl UserServiceImpl;
	@Autowired
	MachineUserServiceImpl MachineUserServiceImpl;

	@Autowired
	UserCreationServiceImpl userCreationServiceImpl;
	@Autowired
	SpocAdminProfileServiceImpl spocAdminProfileServiceImpl;

	@Autowired
	ManagerServiceImpl managerServiceImpl;

	@Autowired
	UserService UserService;
	@Autowired
	LadpServiceImpl ladpServiceImpl;
	@Autowired
	UserDao userDao;
	@Autowired
	private LoginDelegate loginDelegate;

	@Autowired
	MailerService mailerService;

	@Autowired
	SimpleSecurityServiceImpl simpleSecurityServiceImpl;

	@Autowired
	SpocAdminProfileDaoImpl spocAdminProfileDaoImpl;

	private static final int BUFFER_SIZE = 4096;

	String subject = null;

	@RequestMapping("signumList.htm")
	public @ResponseBody GenericServiceResponse SignumList(HttpServletRequest request) throws SQLException {
		// ModelAndView mav = null;
		GenericServiceResponse response = new GenericServiceResponse();

		List<String> list = UserService.totalSignum();
		response.setResults(list);
		return response;
	}

	@RequestMapping("signumList1.htm")
	public @ResponseBody GenericServiceResponse signumList1(HttpServletRequest request) throws SQLException {

		GenericServiceResponse response = new GenericServiceResponse();

		List<Empdetails_aes> list = UserService.totalSignum1();
		response.setResults(list);
		return response;
	}

	@RequestMapping("datetimepicker.htm")
	public ModelAndView datetimepicker() throws SQLException {
		ModelAndView mav = null;
		mav = new ModelAndView("datetimepicker");
		return mav;

	}

	@RequestMapping("uploadFile.htm")
	public ModelAndView uploadFile(HttpServletRequest request) throws SQLException {

		ModelAndView mav = null;
		_logger.info("Entering uploadFile()");

		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);

			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("uploadFile");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}

		} catch (Exception e) {

			_logger.error("error in uploadFile()" + e);
		}

		_logger.info("Exiting from  uploadFile()");
		return mav;

	}

	@RequestMapping("uploadFileViewForToolRightDeletion.htm")
	public ModelAndView uploadFileViewForToolRightDeletion(HttpServletRequest request) throws SQLException {

		ModelAndView mav = null;
		_logger.info("Entering  uploadFileViewForToolRightDeletion");

		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {

				mav = new ModelAndView("uploadFileViewForToolRightDeletion");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("error in uploadFileViewForToolRightDeletion" + e);
		}

		_logger.info("Exiting from  uploadFileViewForToolRightDeletion");
		return mav;

	}

	@RequestMapping("uploadFileViewForToolAddition.htm")
	public ModelAndView uploadFileViewForToolAddition(HttpServletRequest request) throws SQLException {

		_logger.info("Entering  in  uploadFileViewForToolAddition");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("uploadFileViewForToolAddition");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}

		} catch (Exception e) {
			_logger.error(" error in uploadFileViewForToolAddition");
		}
		_logger.info("Exiting from  uploadFileViewForToolAddition");
		return mav;

	}

	@RequestMapping("uploadFileViewForToolDeletion.htm")
	public ModelAndView uploadFileViewForTooltDeletion(HttpServletRequest request) throws SQLException {

		_logger.info("Entering  in  uploadFileViewForToolDeletion.htm");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("uploadFileViewForToolDeletion");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}

		} catch (Exception e) {
			_logger.error(" error in uploadFileViewForToolDeletion.htm");
		}
		_logger.info("Exiting from  uploadFileViewForToolDeletion.htm");
		return mav;

	}

	@RequestMapping("downloadFile1.htm")
	public ModelAndView downloadFile(HttpServletRequest request) throws SQLException {

		ModelAndView mav = null;
		_logger.info("entering in downloadFile1.htm");
		try {
			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("downloadFile");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("error in downloadFile1.htm" + e);
		}
		_logger.info("exiting from downloadFile1.htm");
		return mav;

	}

	@RequestMapping("downloadFileAccSignum.htm")
	public ModelAndView downloadFileAccSignum(HttpServletRequest request) throws SQLException {

		ModelAndView mav = null;
		_logger.info("entering in downloadFileAccSignum");
		try {
			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("downloadFileAccSignum");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("error in downloadFileAccSignum" + e);
		}
		_logger.info("exiting from downloadFileAccSignum");
		return mav;

	}

	@RequestMapping("downloadHistoricalFile.htm")
	public ModelAndView downloadHistoricalFile(HttpServletRequest request) throws SQLException {

		_logger.info("Entering in  downloadHistoricalFile.htm");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);

			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("downloadHistoricalFile");
				mav.addObject("userType", sessionUserType);

			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("Error in downloadHistoricalFile.htmt" + e);
		}
		_logger.info("Exiting from  downloadHistoricalFile.htm");
		return mav;

	}

	@RequestMapping("downloadHistoricalFileAccSignum.htm")
	public ModelAndView downloadHistoricalFileAccSignum(HttpServletRequest request) throws SQLException {

		_logger.info("Entering in  downloadHistoricalFileAccSignum.htm");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);

			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("downloadHistoricalFileAccSignum");
				mav.addObject("userType", sessionUserType);

			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("Error in  downloadHistoricalFileAccSignum" + e);
		}
		_logger.info("Exiting from  downloadHistoricalFileAccSignum");
		return mav;

	}

	@RequestMapping("bulkNodeAddition.htm")
	public ModelAndView bulkNodeAddition() throws SQLException {

		ModelAndView mav = null;
		mav = new ModelAndView("bulkNodeAddition");
		return mav;

	}

	@RequestMapping("deleteUserRequest.htm")
	public ModelAndView deleteUserRequest(HttpServletRequest request) throws SQLException {
		_logger.info("entering  deleteUserRequest method in SpocAdmin controller");
		ModelAndView mav = null;
		try {
			String validation = request.getParameter("validation");
			System.out.println("validation is" + validation);
			int valid = UserService.validation_key_user(validation);
			System.out.println("cid is::" + valid);

			String cid = Integer.toString(valid);
			Map<String, String> list = UserService.userDetails(cid);
			// System.out.println(list);
			List<Ndetails_aes> ndetailsList = UserService.totalIP();
			// mav = new ModelAndView("deleteUserRequest", "list", list);
			if (!list.isEmpty()) {
				if (valid > 0) {
					mav = new ModelAndView("deleteUserRequest", "list", list);
					// mav.addObject("cid",cid);
				} else {
					mav = new ModelAndView("errorPage");
				}
			} else {
				mav = new ModelAndView("errorPage", "message",
						"Manager has already approved/rejected your message <br> Your request has been deleted");
			}
		} catch (Exception e) {
			_logger.error("error in deleteUserRequest method in SpocAdmin controller", e);
		}

		_logger.info("exiting  deleteUserRequest method in SpocAdmin controller");

		return mav;

	}

	@RequestMapping("deleteUserRequestDB.htm")
	public @ResponseBody String deleteUserRequestDB(@RequestBody userAddReqBean useradd, HttpServletRequest request)
			throws ServiceLayerException, SQLException {
		_logger.info("entering  deleteUserRequestDB in SpocAdmin controller");
		String return_val = null;
		String status = request.getParameter("status");
		// System.out.println("staus is::" + status);
		_logger.info("staus is::" + status);
		String cid = useradd.getCid();
		String userid = useradd.getEmailid();
		String u_comment = useradd.getM_comment();
		int i = UserService.userRequestDeletionStatus(cid, u_comment, status);
		// int i=UserService.managerApprovalStatus("129","abc","0");
		// List<String> nodeownerDetails = UserService.nodeOwnerEmailId();
		if (i != 0) {
			return_val = "your request has been deleted";
		} else {
			return_val = "your request has not been deleted";
		}
		_logger.info("exiting  deleteUserRequestDB in SpocAdmin controller");
		return return_val;

	}

	@RequestMapping("userAdditionRequest.htm")
	public ModelAndView userAdditionRequest(HttpServletRequest request) throws SQLException {

		_logger.info("Entering in  userAdditionRequest");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("spocAdminProfile");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("Error in userAdditionRequest" + e);
		}
		_logger.info("Exiting from  userAdditionRequest");
		return mav;

	}

	@RequestMapping("consolidatedDbRequestsForSpocAdminProfile.htm")
	public ModelAndView consolidatedDbRequestsForSpocAdminProfile(HttpServletRequest request) throws SQLException {
		// String sessionVal = (String)
		// request.getSession(false).getAttribute("UserName");
		_logger.info("Entering in  consolidatedDbRequestsForSpocAdminProfile");
		ModelAndView mav = null;
		try {

			HttpSession sessionUser = request.getSession(false);
			System.out.println("sessionUser" + sessionUser);
			String sessionUserType = (String) sessionUser.getAttribute("UserType");
			System.out.println("sessionUserType" + sessionUserType);
			if (sessionUserType.equals("1")) {
				mav = new ModelAndView("consolidatedDbRequestsForSpocAdminProfile");
				mav.addObject("userType", sessionUserType);
			} else {
				mav = new ModelAndView("loginView");
			}
		} catch (Exception e) {
			_logger.error("Error in consolidatedDbRequestsForSpocAdminProfile" + e);
		}
		_logger.info("Exiting from  consolidatedDbRequestsForSpocAdminProfile");
		return mav;

	}

	@RequestMapping("userAdditionRequestList.htm")
	public @ResponseBody GenericServiceResponse userAdditionRequestList(HttpServletRequest request)
			throws SQLException {
		GenericServiceResponse response = new GenericServiceResponse();
		String sessionVal = (String) request.getSession(false).getAttribute("UserName");
		List<userCreationRequestBean> userAdditionRequestList = UserService.userAdditionRequestList(sessionVal);
		response.setResults(userAdditionRequestList);
		return response;
	}

	@RequestMapping("checkManagerStatus.htm")
	public @ResponseBody String checkManagerStatus(HttpServletRequest request) throws SQLException {
		_logger.info("entering checkManagerStatus method in SpocAdmin controller method");
		String cid = request.getParameter("cid");
		int valid = UserService.checkManagerStatus(cid);
		String valid1 = Integer.toString(valid);
		_logger.info("exiting checkManagerStatus method in SpocAdmin controller method");
		return valid1;

	}

	@RequestMapping("generateOTP.htm")
	public @ResponseBody String otpGeneration(HttpServletRequest request) {
		_logger.info("entering otpGeneration method in SpocAdmin controller");
		String signum = request.getParameter("signum");
		String otpMessage = null;
		String otp = null;
		subject = opcoName + "::Password Reset";
		try {
			otp = simpleSecurityServiceImpl.keyGeneration(4);
			System.out.println("OTP is::" + otp);
			int i = UserService.otpUpdationInDB(signum, otp);
			if (i > 0)
				otpMessage = "OTP generated and sent to your email id";
			else if (i == -5)
				otpMessage = "This user does not exist";
			else
				otpMessage = "Please generate OTP again";

			String userEmailId = UserService.getUserEmailId(signum);
			mailerService.sendMail(userEmailId, String.format(AmsConstants.MANAGER_TO_BE_APPROVED_SUBJECT, subject),
					String.format(AmsConstants.SENDING_OTP_MESSAGE_TO_USER, otp));

		} catch (Exception e) {
			_logger.error("error in otpGeneration::" + e);
		}
		_logger.info("exiting otpGeneration method in SpocAdmin Controller");
		return otpMessage;
	}

	@RequestMapping("spocAdminProfile.htm")
	public @ResponseBody DatatablePagingResponse spocAdminProfile(HttpServletRequest request)
			throws ServiceLayerException, SQLException, java.text.ParseException {

		Integer draw;
		Integer offset;
		Integer pageSize;
		String searchToken;
		int sortColumn;
		String sortColumnDirection;
		String sortColumnName = null;

		draw = Integer.parseInt(request.getParameter("draw"));
		offset = Integer.parseInt(request.getParameter("start"));
		pageSize = Integer.parseInt(request.getParameter("length"));
		sortColumn = Integer.parseInt(request.getParameter("order[0][column]"));
		sortColumnDirection = request.getParameter("order[0][dir]");
		System.out.println(" Before sortColumn is" + sortColumn);

		if (sortColumn == 0) {
			sortColumn = 1;
			sortColumnName = "rd.cid";
			sortColumnDirection = "desc";
		} else {

			if (sortColumn == 1) {
				sortColumn = 9;
				sortColumnName = "rd.cid";
				System.out.println("sort column name in " + sortColumnName);
			} else if (sortColumn == 2) {
				sortColumn = 2;
				sortColumnName = "rd.user_id";
			} else if (sortColumn == 3) {
				sortColumn = 14;
				sortColumnName = "u.email";
			} else if (sortColumn == 4) {
				sortColumn = 4;
				sortColumnName = "rd.start_date";
			} else if (sortColumn == 5) {
				sortColumn = 5;
				sortColumnName = "rd.end_date";
			} else if (sortColumn == 6) {
				sortColumn = 10;
				sortColumnName = "rd.sysname";
			} else if (sortColumn == 7) {
				sortColumn = 7;
				sortColumnName = "rd.tool_right";
			} else if (sortColumn == 8) {
				sortColumn = 13;
				sortColumnName = "s.sys_group";
			} else if (sortColumn == 9) {
				sortColumn = 12;
				sortColumnName = "s.sys_owner";
			} else if (sortColumn == 10) {
				sortColumn = 11;
				sortColumnName = " s.sys_type";
			} else if (sortColumn == 11) {
				sortColumn = 8;
				sortColumnName = "req.requestType";
			} else if (sortColumn == 12) {
				sortColumn = 6;
				sortColumnName = "st.status_name";
			} else if (sortColumn == 1) {
				sortColumn = 9;
				sortColumnName = "rd.cid";
			}

		}

		System.out.println("sortColumnDirection is " + sortColumnDirection);
		System.out.println("sortColumn is" + sortColumn);
		System.out.println("sort column name in " + sortColumnName);

		searchToken = request.getParameter("search[value]");
		String regex = "^([0-9]{2})-([0-9]{2})-([0-9]{4})$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(searchToken);
		boolean bool = matcher.matches();

		System.out.println("search token is boolena value " + bool);
		if (bool) {
			String dateStr = searchToken;

			DateFormat srcDf = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date = srcDf.parse(dateStr);

			DateFormat destDf = new SimpleDateFormat("yyyy-MM-dd");

			// format the date into another format
			dateStr = destDf.format(date);

			System.out.println("date in string format is" + dateStr);

			searchToken = dateStr;
			System.out.println(" search token in date format" + searchToken);
		}

		DatatablePagingResponse response = new DatatablePagingResponse();

		System.out.println(
				"draw is" + draw + "  offset" + offset + " pagesize" + pageSize + " searchToken " + searchToken);

		System.out.println("entering asdf");

		try {
			Enumeration<String> params = request.getParameterNames();
			System.out.println("params is" + params);
			while (params.hasMoreElements()) {
				String paramName = params.nextElement();
				System.out.println("Parameter Name - " + paramName + ", Value - " + request.getParameter(paramName));
			}

			System.out.println("request for tool list is" + request.toString());

			response = spocAdminProfileServiceImpl.FetchSpocAdminProfileView(offset, pageSize, searchToken, sortColumn,
					sortColumnDirection, sortColumnName);

		} catch (Exception e) {
			_logger.error("error in spocAdminProfile" + e);
		}

		return response;

	}

	@RequestMapping("consolidatedDbRequestsTableInfo.htm")
	public @ResponseBody DatatablePagingResponse consolidatedDbRequestsTableInfo(HttpServletRequest request)
			throws ServiceLayerException, SQLException, java.text.ParseException {

		Integer draw;
		Integer offset;
		Integer pageSize;
		String searchToken;
		int sortColumn;
		String sortColumnDirection;
		String sortColumnName = null;

		draw = Integer.parseInt(request.getParameter("draw"));
		offset = Integer.parseInt(request.getParameter("start"));
		pageSize = Integer.parseInt(request.getParameter("length"));
		sortColumn = Integer.parseInt(request.getParameter("order[0][column]"));
		sortColumnDirection = request.getParameter("order[0][dir]");
		System.out.println(" Before sortColumn is" + sortColumn);

		if (sortColumn == 0) {
			sortColumn = 1;
			sortColumnName = "rd.cid";
			sortColumnDirection = "desc";
		} else {

			if (sortColumn == 1) {
				sortColumn = 9;
				sortColumnName = "rd.cid";
				System.out.println("sort column name in " + sortColumnName);
			} else if (sortColumn == 2) {
				sortColumn = 2;
				sortColumnName = "rd.user_id";
			} else if (sortColumn == 3) {
				sortColumn = 4;
				sortColumnName = "rd.start_date";
			} else if (sortColumn == 4) {
				sortColumn = 5;
				sortColumnName = "rd.end_date";
			} else if (sortColumn == 5) {
				sortColumn = 10;
				sortColumnName = "rd.sysname";
			} else if (sortColumn == 6) {
				sortColumn = 7;
				sortColumnName = "rd.tool_right";
			} else if (sortColumn == 7) {
				sortColumn = 13;
				sortColumnName = "s.sys_group";
			} else if (sortColumn == 8) {
				sortColumn = 12;
				sortColumnName = "s.sys_owner";
			} else if (sortColumn == 9) {
				sortColumn = 11;
				sortColumnName = " s.sys_type";
			} else if (sortColumn == 10) {
				sortColumn = 8;
				sortColumnName = "req.requestType";
			} else if (sortColumn == 11) {
				sortColumn = 6;
				sortColumnName = "st.status_name";
			} else if (sortColumn == 12) {
				sortColumn = 14;
				sortColumnName = "req.managerName";
			} else if (sortColumn == 13) {
				sortColumn = 16;
				sortColumnName = "rd.spoc_admin_comment";
			} else if (sortColumn == 14) {
				sortColumn = 17;
				sortColumnName = "rd.tdcComment";
			} else if (sortColumn == 15) {
				sortColumn = 18;
				sortColumnName = "rd.systemAdminComment";
			}

		}

		System.out.println("sortColumnDirection is " + sortColumnDirection);
		System.out.println("sortColumn is" + sortColumn);
		System.out.println("sort column name in " + sortColumnName);

		searchToken = request.getParameter("search[value]");
		String regex = "^([0-9]{2})-([0-9]{2})-([0-9]{4})$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(searchToken);
		boolean bool = matcher.matches();

		System.out.println("search token is boolena value " + bool);
		if (bool) {
			String dateStr = searchToken;

			DateFormat srcDf = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date = srcDf.parse(dateStr);

			DateFormat destDf = new SimpleDateFormat("yyyy-MM-dd");

			// format the date into another format
			dateStr = destDf.format(date);

			System.out.println("date in string format is" + dateStr);

			searchToken = dateStr;
			System.out.println(" search token in date format" + searchToken);
		}

		DatatablePagingResponse response = new DatatablePagingResponse();

		System.out.println(
				"draw is" + draw + "  offset" + offset + " pagesize" + pageSize + " searchToken " + searchToken);

		System.out.println("entering asdf");

		try {
			Enumeration<String> params = request.getParameterNames();
			System.out.println("params is" + params);
			while (params.hasMoreElements()) {
				String paramName = params.nextElement();
				System.out.println("Parameter Name - " + paramName + ", Value - " + request.getParameter(paramName));
			}

			System.out.println("request for tool list is" + request.toString());
			// SpocAdminProfileServiceImpl SpocAdminProfileServiceImpl=new
			// SpocAdminProfileServiceImpl();

			response = spocAdminProfileServiceImpl.consolidatedDbRequestsTableInfoView(offset, pageSize, searchToken,
					sortColumn, sortColumnDirection, sortColumnName);

		} catch (Exception e) {
			_logger.error("error in spocAdminProfile" + e);
		}

		/* GenericServiceResponse response = new GenericServiceResponse(); */
		/*
		 * String sessionVal = (String)
		 * request.getSession(false).getAttribute("UserName"); List<toolDetails>
		 * reqUserDetailsList = UserService.toolList(sessionVal);
		 * response.setResults(reqUserDetailsList); System.out.println(
		 * "present in Node list part after getting nodelist");
		 */
		// return response;

		return response;

	}

	@RequestMapping("sendingMailToTdcAndUser.htm")
	public @ResponseBody String sendingMailToTdcAndUser(@RequestBody SpocAdmin spocadmin, HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering sendingMailToTdcAndUser in LoginController");

		System.out.println("Entering savingUserCreationRequestForm ");
		System.out.println("spoc admin values" + spocadmin.getCid());
		System.out.println("spoc admin comment in controller" + spocadmin.getspocAdminComment());
		String userSignum = null;
		String photoKeyFileName = null;
		int deleteCounter = -10;

		Enumeration<String> params = request.getParameterNames();
		System.out.println("params is" + params);
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			System.out.println("Parameter Name - " + paramName + ", Value - " + request.getParameter(paramName));
		}

		String return_val = null;
		try {

			String sessionVal = (String) request.getSession(false).getAttribute("UserName");

			// settong request status as approved by spoc admin
			List<String> mailInfo = spocAdminProfileServiceImpl.spocAdminApprovalUpdate(spocadmin, sessionVal);

			System.out.println("delete counter is" + deleteCounter);

			if (mailInfo.size() == 9) {
				String managerName = mailInfo.get(0);
				String userEmailId = mailInfo.get(3);
				userSignum = mailInfo.get(4);
				String TDCSignum = mailInfo.get(5);
				String toolRight = spocadmin.getToolRight();
				String companyName = mailInfo.get(6);
				photoKeyFileName = mailInfo.get(7);
				String toolFindFlagStringFormat = mailInfo.get(8);

				System.out.println("tool right is" + toolRight);
				System.out.println("TDC signum before m2m block" + TDCSignum);

				int cid = Integer.parseInt(spocadmin.getCid());
				System.out.println("is nodeAdd value is::" + cid);

				// Extracting request Type
				String requestType = spocadmin.getRequestType();

				// Extracting Machine Info
				MachineUserBean machineUserBean = MachineUserServiceImpl.getMachineUserInfo(spocadmin.getCid());

				/*
				 * Preparing user mil body depending on type of request(human
				 * user create /update machine user create/update
				 */
				List<String> userBodyArgs = new ArrayList<String>();
				String userName = mailInfo.get(1) + " " + mailInfo.get(2);
				String userMailSubject = "[UM]USER ACCESS REQUEST L2 Approval - APPROVED RequestId : %s Tool :%s";

				if (requestType.toLowerCase().indexOf("machine") > -1) {

					userBodyArgs.add(
							machineUserBean.getRequesterFirstName() + " " + machineUserBean.getRequesterLastName());
					String text = "<br> Machine Name :" + machineUserBean.getMachineName() + "<br>";
					userBodyArgs.add(text);
					userMailSubject += " M2M";
					TDCSignum = machineUserBean.getRequesterTDCId();
					System.out.println("TDC signum in m2m block" + TDCSignum);
				} else {

					userBodyArgs.add(mailInfo.get(1) + " " + mailInfo.get(2));
					userBodyArgs.add("<br>");
				}

				userBodyArgs.add(spocadmin.getCid());
				userBodyArgs.add(spocadmin.getTool());
				userBodyArgs.add(spocadmin.getToolRight());

				// user subject arguments
				List<String> userSubjectArgs = new ArrayList<String>();
				userSubjectArgs.add(spocadmin.getCid());
				String toolname = spocadmin.getTool();
				userSubjectArgs.add(toolname);

				// getting manager info
				ManagerBean managerDetails = managerServiceImpl.getManagerInfo(spocadmin.getCid());
				String managerEmailAddress = managerDetails.getManager_email_address();
				System.out.println("manager email address " + managerEmailAddress);

				// sending mail to user
				int count = 0;
				int maxTries = 3;
				while (true) {
					try {
						// Some Code
						// break out of loop, or return, on success

						mailerService.sendMail(userEmailId, String.format(userMailSubject, userSubjectArgs.toArray()),
								String.format(AmsConstants.ApprovedBySpocAdminMailSentToUser, userBodyArgs.toArray()),
								"user");
						break;
					} catch (Exception e) {
						// handle exception
						System.out.println("count is" + count);
						if (++count == maxTries)
							throw e;
					}
				}

				/*
				 * Preaparing TDC mail body according to type of request human
				 * user create/update or machine user create/update if human
				 * user update-whether it is on processing state or in valid TDC
				 * id format ex :M12345
				 */
				List<String> tdcMailBodyArgs = new ArrayList<String>();

				String requestTypeString = "";
				String userTdcSignumParameterForMailBody = "";

				if (requestType.equals("User Creation")) {
					requestTypeString = "Create";

					userTdcSignumParameterForMailBody = "";

					System.out.println("entring in if part for  user creation request type");

				} else {
					String TDCIdString = "TDC Id:";
					if (requestType.equals("User Updation")) {
						requestTypeString = "Update";
					} else if (requestType.equals("User Deletion")) {
						requestTypeString = "Delete";
					} else if (requestType.equals("Machine to Machine User Creation")) {
						requestTypeString = "Create M2M";
						TDCIdString = "Requester TDC Id:";
					} else if (requestType.equals("Machine to Machine User Updation")) {

						requestTypeString = "Update M2M";
						TDCIdString = "Requester TDC Id:";
					}
					// have to add more condition taking account tdc id;
					// have to check whethet tdc is in prcoess or not for update
					// case part
					if (TDCSignum.equals("Processing")) {

						userTdcSignumParameterForMailBody = "Note that TDC account is in process for this user.";
					}

					else {

						userTdcSignumParameterForMailBody = TDCIdString + TDCSignum;
						System.out.println("entring in else part for  user creation request type");
					}
				}

				String parameterForUserName = "User : " + mailInfo.get(1) + " " + mailInfo.get(2) + "<br>"
						+ userTdcSignumParameterForMailBody + "<br>";
				if (requestType.toLowerCase().indexOf("machine") > -1) {

					parameterForUserName = " Requester User : " + machineUserBean.getRequesterFirstName() + " "
							+ machineUserBean.getRequesterLastName() + "<br>" + userTdcSignumParameterForMailBody
							+ "<br>";
				}

				tdcMailBodyArgs.add(parameterForUserName);
				tdcMailBodyArgs.add(managerName);

				tdcMailBodyArgs.add(sessionVal);

				if (requestType.toLowerCase().indexOf("machine") > -1) {

					String machineTdcId = " ";
					if (machineUserBean.getMachineTDCID().equals("Processing")) {

						machineTdcId = " ";
					} else {
						machineTdcId = machineUserBean.getMachineTDCID();
					}

					String parameterForMachineInfo = "Machine Name:" + machineUserBean.getMachineName()
							+ "<br> Machine TDC ID:" + machineTdcId + "<br>";
					tdcMailBodyArgs.add(parameterForMachineInfo);
				} else {
					tdcMailBodyArgs.add("");
				}

				tdcMailBodyArgs.add(Integer.toString(cid));
				tdcMailBodyArgs.add(toolname);
				tdcMailBodyArgs.add(spocadmin.getToolRight());
				tdcMailBodyArgs.add(companyName);

				List<String> tdcSubjectArgs = new ArrayList<String>();
				tdcSubjectArgs.add(mailInfo.get(1) + " " + mailInfo.get(2));
				tdcSubjectArgs.add(toolname);

				String spocAdminMail = "team.security.dk@ericsson.com";

				count = 0;
				maxTries = 3;

				String userNameWithoutTrimmingSpaces = userName.trim();

				// sending mail to spoc admin ie security team
				while (true) {
					try {

						mailerService.sendMail(spocAdminMail,
								String.format("[UM]USER ACCESS REQUEST - " + requestTypeString + "  %s  Application %s",
										tdcSubjectArgs.toArray()),
								String.format(AmsConstants.ApprovedBySpocAdminMailSentToTdc, tdcMailBodyArgs.toArray()),
								4, userSignum, photoKeyFileName, userNameWithoutTrimmingSpaces);

						break;
					} catch (Exception e) {
						// handle exception
						System.out.println("count is" + count);
						if (++count == maxTries)
							throw e;
					}

					finally {

					}
				}

				if (toolFindFlagStringFormat.equals("false")) {
					return_val = "Tool Sheet is Empty\n"
							+ "Request has been raised  & it has been sent to TDC for approval";
				} else {
					return_val = "Request has been raised  & it has been sent to TDC for approval";
				}
			} else {
				return_val = "Request has not been  raised.Please try again";
			}
			_logger.info("exiting sendingMailToTdcAndUser in LoginController");

		} catch (Exception e) {
			return_val = "request has not been raised Mail issue chances";
			System.out.println("request has not been raised");
			e.printStackTrace();
			_logger.error("request has been not  raised  value of return value" + return_val);
			_logger.error("Error in sendingMailToTdcAndUser in LoginController", e);
		} finally {

			System.out.println("finally block");

			// deleting generated Excel file after mail has been sent
			if (userSignum != null) {
				String rootPath = System.getProperty("catalina.home");
				String userFolderPath = rootPath + File.separator + "tdcams" + File.separator + userSignum;

				System.out.println("path of user folder in SpocAdmin controller" + userFolderPath);
				File index = new File(userFolderPath);

				String[] entries = index.list();
				for (String s : entries) {
					File currentFile = new File(index.getPath(), s);
					currentFile.delete();
				}
				index.delete();

				if (photoKeyFileName != null && !photoKeyFileName.isEmpty() && deleteCounter == 0) {
					String photorPath = photoKeyFileName;
					System.out.println("path of user folder in SpocAdmin controller" + photorPath);
					File indexPhoto = new File(photorPath);

					boolean exists = indexPhoto.exists();
					if (exists) {
						indexPhoto.delete();
					}
				}

			}
		}
		return return_val;

	}

	@RequestMapping("rejectedByTDCButton.htm")
	public @ResponseBody String rejectedByTDCButton(@RequestBody SpocAdmin spocadmin, HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering rejectedByTDCButton.htm in rejectedByTDCButton function in SpocAdmin Controller");

		System.out.println("Entering spocAdminDeny ");
		System.out.println("spoc admin values" + spocadmin.getCid());

		String return_val = null;

		try {
			// Updating that request has been denied by TDC
			List<String> mailInfo = spocAdminProfileServiceImpl.tdcDenialUpdate(spocadmin);

			if (mailInfo.size() == 3) {

				String firstName = mailInfo.get(0);
				String lastName = mailInfo.get(1);
				String userEmailId = mailInfo.get(2);

				List<String> userBodyArgs = new ArrayList<String>();

				// Extracting request type
				String requestType = spocAdminProfileServiceImpl.getRequestType(spocadmin.getCid());

				System.out.println("spoc admin comment" + spocadmin.getspocAdminComment());

				String subject = "[UM]USER ACCESS REQUEST - TDC Approval - REJECTED  Request ID:%s Tool :%S";

				// Extracting machine information
				MachineUserBean machineUserBean = MachineUserServiceImpl.getMachineUserInfo(spocadmin.getCid());

				/*
				 * Preaparing user mail body depending on the type of
				 * request(human create /update or Machine user create or update
				 */
				if (requestType.toLowerCase().indexOf("machine") > -1) {
					subject += " M2M";

					userBodyArgs.add(
							machineUserBean.getRequesterFirstName() + " " + machineUserBean.getRequesterLastName());
				} else {
					userBodyArgs.add(firstName + " " + lastName);
				}
				userBodyArgs.add(spocadmin.getspocAdminComment());
				userBodyArgs.add(spocadmin.getCid());
				if (requestType.toLowerCase().indexOf("machine") > -1) {
					String text = "<br> Machine Name :" + machineUserBean.getMachineName() + "<br>";
					userBodyArgs.add(text);
				} else {
					userBodyArgs.add("<br>");
				}
				userBodyArgs.add(spocadmin.getTool());
				userBodyArgs.add(spocadmin.getToolRight());

				// user mail sbject parameter
				List<String> userSubjectArgs = new ArrayList<String>();
				userSubjectArgs.add(spocadmin.getCid());
				String toolname = spocadmin.getTool();

				userSubjectArgs.add(toolname);

				// getting manager Info
				ManagerBean managerDetails = managerServiceImpl.getManagerInfo(spocadmin.getCid());
				String managerEmailAddress = managerDetails.getManager_email_address();

				System.out.println("manager email address " + managerEmailAddress);

				// Sending mail to user
				int count = 0;
				int maxTries = 3;
				while (true) {
					try {
						// Some Code
						// break out of loop, or return, on success
						mailerService.sendMail(userEmailId, String.format(subject, userSubjectArgs.toArray()),
								String.format(AmsConstants.RejectedbyTDC, userBodyArgs.toArray()), "user",
								managerEmailAddress);
						break;
					} catch (Exception e) {
						// handle exception
						System.out.println("count is" + count);
						if (++count == maxTries)
							throw e;
					}
				}

				return_val = "REJECT action has been processed successfully(ie request has been REJECTED by TDC TEAM)";

			} else {
				return_val = "request has not been raised ";
			}
			_logger.info("exiting rejectedByTDCButton.htm in rejectedByTDCButton function in SpocAdmin Controller");

		} catch (Exception e) {

			e.printStackTrace();
			_logger.error("Error in tdcDeny", e);
		}

		return return_val;

	}

	@RequestMapping("spocAdminDeny.htm")
	public @ResponseBody String spocAdminDeny(@RequestBody SpocAdmin spocadmin, HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering spocAdminDeny.htm in spocAdminDeny function in SpocAdmin controller ");

		System.out.println("Entering spocAdminDeny ");
		System.out.println("spoc admin values" + spocadmin.getCid());

		String return_val = null;

		try {

			// updating spocAdmin denial in DB through service layer
			List<String> mailInfo = spocAdminProfileServiceImpl.spocAdminDenialUpdate(spocadmin);

			if (mailInfo.size() == 3) {
				String firstName = mailInfo.get(0);
				String lastName = mailInfo.get(1);
				String userEmailId = mailInfo.get(2);

				// Extracting request type
				String requestType = spocAdminProfileServiceImpl.getRequestType(spocadmin.getCid());

				List<String> userBodyArgs = new ArrayList<String>();

				// Extracting Machine Info
				MachineUserBean machineUserBean = MachineUserServiceImpl.getMachineUserInfo(spocadmin.getCid());

				String subject = "[UM]USER ACCESS REQUEST L2 Approval - REJECTED  Request ID:%s Tool :%S";

				// Manipulating user mail body depending on the type of
				// request(human user or machine)
				if (requestType.toLowerCase().indexOf("machine") > -1) {
					System.out.println(machineUserBean.getRequesterFirstName());
					userBodyArgs.add(
							machineUserBean.getRequesterFirstName() + " " + machineUserBean.getRequesterLastName());
					subject += " M2M";
				} else {
					userBodyArgs.add(firstName + " " + lastName);
				}
				userBodyArgs.add(spocadmin.getspocAdminComment());
				userBodyArgs.add(spocadmin.getCid());

				if (requestType.toLowerCase().indexOf("machine") > -1) {
					String text = "<br> Machine Name :" + machineUserBean.getMachineName() + "<br>";
					userBodyArgs.add(text);
				} else {
					userBodyArgs.add("<br>");
				}

				userBodyArgs.add(spocadmin.getTool());
				userBodyArgs.add(spocadmin.getToolRight());

				// user mail subject parameters
				List<String> userSubjectArgs = new ArrayList<String>();
				userSubjectArgs.add(spocadmin.getCid());
				String toolname = spocadmin.getTool();
				userSubjectArgs.add(toolname);

				ManagerBean managerDetails = managerServiceImpl.getManagerInfo(spocadmin.getCid());
				String managerEmailAddress = managerDetails.getManager_email_address();

				System.out.println("manager email address " + managerEmailAddress);

				// sending mail to user
				int count = 0;
				int maxTries = 3;
				while (true) {
					try {
						// Some Code
						// break out of loop, or return, on success
						mailerService.sendMail(userEmailId, String.format(subject, userSubjectArgs.toArray()),
								String.format(AmsConstants.RejectedBySpocAdmin, userBodyArgs.toArray()), "user",
								managerEmailAddress);
						break;
					} catch (Exception e) {
						// handle exception
						System.out.println("count is" + count);
						if (++count == maxTries)
							throw e;
					}
				}

				System.out.println("return value is" + return_val);
				return_val = " REJECT action has been processed successfully(ie request has been REJECTED by SPOC ADMIN)";
				System.out.println("return value is" + return_val);

			} else {
				return_val = "request has not been  raised ";
			}
			_logger.info("exiting  in try block spocAdminDeny from logincontroller");

		} catch (Exception e) {

			e.printStackTrace();
			_logger.error("Error spocAdminDeny.htm in spocAdminDeny function in SpocAdmin controller", e);
		}
		_logger.info("exiting spocAdminDeny.htm in spocAdminDeny function in SpocAdmin controller ");
		return return_val;

	}

	// @RequestMapping(value="savePhoto.htm",consumes = { "multipart/form-data"
	// })
	@RequestMapping(value = "savePhoto.htm")
	public @ResponseBody String uploadFile(@RequestParam("file") MultipartFile uploadfile, HttpServletRequest request) {

		_logger.info("entering in uploadFile");
		String returnValue = null;
		BufferedOutputStream stream = null;
		try {

			String filenameKey = request.getParameter("photoid");
			String regex = "^(\\+)$";
			filenameKey = filenameKey.replaceAll(regex, "");

			filenameKey = filenameKey.replaceAll(" ", "");
			filenameKey = filenameKey.replaceAll(":", "");
			filenameKey = filenameKey + ".jpg";
			System.out.println("photo id is" + filenameKey);

			String filename = uploadfile.getOriginalFilename();
			String rootPath = System.getProperty("catalina.home");
			String userFolderPath = rootPath + File.separator + "tdcams";

			System.out.println("path of user folder in SpocAdmin controller" + userFolderPath);
			// String directory = "C:\\Users\\ekuiani\\Desktop\\tdcams";
			String directory = userFolderPath;
			// String filepath = Paths.get(directory, filename).toString();
			String filepath = Paths.get(directory, filenameKey).toString();

			// Save the file locally
			stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());

			System.out.println("finally done");
			returnValue = "Uploaded Successfully";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			_logger.error("Error in uploadFile for image " + e);

			returnValue = "File not uploaded.Try again";

		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		_logger.info("Exiting from uploadFile for image ");
		return returnValue;
	} //

	@RequestMapping("getMnagerList.htm")
	public @ResponseBody GenericServiceResponse getMnagerList(HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering getMnagerList.htm in SpocAdmin Controller");
		GenericServiceResponse response = new GenericServiceResponse();
		try {
			List<ManagerList> list = userCreationServiceImpl.managerList();
			response.setResults(list);
		} catch (Exception e) {
			_logger.error("error in getMnagerList" + e);
		}
		_logger.info("exiting getMnagerList.htm in SpocAdmin Controller");
		/*
		 * System.out.println("present in Node list part after getting nodelist"
		 * );
		 */
		return response;

	}

	@RequestMapping("getAreaDomainList.htm")
	public @ResponseBody GenericServiceResponse getAreaDomainList(HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering getAreaDomainList method in SpocAdmin Controller");
		GenericServiceResponse response = new GenericServiceResponse();
		try {
			List<AreaDomain> list = userCreationServiceImpl.Arealist();
			response.setResults(list);
		} catch (Exception e) {
			_logger.error("Error in getAreaDomainList method" + e);
		}
		_logger.info("exiting getAreaDomainList method in SpocAdmin Controller");

		return response;
	}

	@RequestMapping("employeeDetailsWithSecurityInfoFunction.htm")
	public @ResponseBody GenericServiceResponse getEmployeeDetailsWithSecurityInfoFunction(HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info(" entering employeeDetailsWithSecurityInfoFunction");

		GenericServiceResponse response = new GenericServiceResponse();
		try {

			String cid = request.getParameter("cid");
			System.out.println("employee signum  is" + cid);

			List<EmployeeDetailsWithSecurityInfo> list = spocAdminProfileServiceImpl
					.getEmployeeDetailsWithSecurityInfoFunction(cid);
			response.setResults(list);

		} catch (Exception e) {

			// e.printStackTrace();
			_logger.error("Error in getEmployeeDetailsWithSecurityInfoFunction in LoginController", e);
		}
		_logger.info("exiting  employeeDetailsWithSecurityInfoFunction");
		return response;

	}

	@RequestMapping("photoKeyPathRetreival.htm")
	public @ResponseBody String photoKeyPathRetreival(@RequestBody SpocAdmin spocadmin, HttpServletRequest request,
			HttpServletResponse response) throws SQLException {

		_logger.info("entering in photoKeyPathRetreival.htm");
		String cidd = request.getParameter("cid");
		System.out.println("cid is " + cidd);
		String photoKeyPath = null;
		String encodedString = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {

			String toolName = spocadmin.getTool();
			System.out.println("toolName is" + toolName);
			photoKeyPath = userCreationServiceImpl.photoKeyPathRetreival(cidd, toolName);
			System.out.println("key path is:" + photoKeyPath);
			OutputStream os = response.getOutputStream();

			File file = new File(photoKeyPath);
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			int b;
			byte[] buffer = new byte[1024];
			while ((b = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, b);
			}
			byte[] fileBytes = bos.toByteArray();

			byte[] encoded = Base64.encodeBase64(fileBytes);
			encodedString = new String(encoded);
			System.out.println("encode string is" + encodedString);

			ModelMap map = new ModelMap();
			map.put("image", encodedString);

		} catch (Exception e) {
			_logger.error("error in entering in photoKeyPathRetreival.htm" + e);
		} finally {

			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		_logger.info("exiting entering in photoKeyPathRetreival.htm");

		return encodedString;

	}

	@RequestMapping("removedBySpocAdmin.htm")
	public @ResponseBody String removedBySpocAdmin(@RequestBody SpocAdmin spocadmin, HttpServletRequest request)
			throws ServiceLayerException, SQLException {

		_logger.info("entering into removedBySpocAdmin");

		System.out.println("Entering removedBySpocAdmin ");
		System.out.println("spoc admin values" + spocadmin.getCid());

		String return_val = null;
		String photoKeyFileName = null;

		int deleteCounter = -10;

		try {

			// setting status as Approved From Spoc Admin
			List<String> mailInfo = spocAdminProfileServiceImpl.removedBySpocAdmin(spocadmin);
			String sessionVal = (String) request.getSession(false).getAttribute("UserName");

			/*
			 * Upload photo in case of tDC media card needs to be deleted fo
			 * that it is checking how many request are not completed where
			 * photo is required
			 */
			deleteCounter = spocAdminProfileServiceImpl.uploadedImageDeleteCounter(spocadmin, sessionVal);

			if (mailInfo.size() == 4) {

				String userEmailId = mailInfo.get(2);
				photoKeyFileName = mailInfo.get(3);

				// user mail body parameters
				List<String> userBodyArgs = new ArrayList<String>();
				userBodyArgs.add(spocadmin.getCid());
				userBodyArgs.add(spocadmin.getTool());
				userBodyArgs.add(spocadmin.getToolRight());

				// user mail subject parameters
				List<String> userSubjectArgs = new ArrayList<String>();
				String toolname = spocadmin.getTool();
				userSubjectArgs.add(toolname);
				userSubjectArgs.add(spocadmin.getCid());

				// sending mail to team security team
				int count = 0;
				int maxTries = 3;
				while (true) {
					try {
						// Some Code
						// break out of loop, or return, on success
						String teamSecurityMailIdString = "team.security.dk@ericsson.com";
						mailerService.sendMail(teamSecurityMailIdString,
								String.format("[UM]USER ACCESS REQUEST- Removed Application:%s Request Id :%S",
										userSubjectArgs.toArray()),
								String.format(AmsConstants.removedBySystemAdmin, userBodyArgs.toArray()), "user");
						break;
					} catch (Exception e) {
						// handle exception
						System.out.println("count is" + count);
						if (++count == maxTries)
							throw e;
					}
				}

				return_val = "request has been removed  successfully from Approval/Denial Panel";

			} else {
				return_val = "request has not been removed ";
			}
			_logger.info("exiting from  removedBySpocAdmin  in LoginController");

		} catch (Exception e) {

			e.printStackTrace();
			_logger.error("Error in removedBySpocAdmin  in LoginController", e);
		}

		finally {

			if (photoKeyFileName != null && !photoKeyFileName.isEmpty() && deleteCounter == 0) {
				String photorPath = photoKeyFileName;
				System.out.println("path of user folder in SpocAdmin controller" + photorPath);
				File indexPhoto = new File(photorPath);

				boolean exists = indexPhoto.exists();
				if (exists) {
					indexPhoto.delete();
				}
			}
		}

		return return_val;

	}

	private ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
