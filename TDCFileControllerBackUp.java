package com.egil.ams.web.controller;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.egil.ams.dao.impl.TDCFileDaoImpl;
import com.egil.ams.service.UserService;
import com.egil.ams.service.impl.TDCFileServiceImpl;

@Controller
public class TDCFileControllerBackUp {
	Logger _logger = LoggerFactory.getLogger(TDCFileControllerBackUp.class);

	@Value("${mail.serverIP}")
	private String serverIP;

	@Value("${mail.opcoName}")
	private String opcoName;

	@Autowired
	UserService UserService;
	@Autowired
	TDCFileServiceImpl tdcFileServiceImpl;

	@Autowired
	TDCFileDaoImpl tdcFileDaoImpl;

	@RequestMapping("fileDownloadFromServer.htm")
	public void downloadFileFromServer(HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttrs) throws Exception

	{

		String startDateRequestParam = request.getParameter("startDateId");
		String endDateRequestParam = request.getParameter("endDateId");

		_logger.info("entering into  fileDownloadFromServer.htm");
		PrintWriter out = null;

		try {
			String dateStr11 = startDateRequestParam;

			DateFormat srcDf11 = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date11 = srcDf11.parse(dateStr11);

			DateFormat destDf11 = new SimpleDateFormat("MM-dd-yyyy");

			// format the date into another format
			dateStr11 = destDf11.format(date11);

			Date startDate = destDf11.parse(dateStr11);

			String dateStr22 = endDateRequestParam;

			DateFormat srcDf22 = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date22 = srcDf22.parse(dateStr22);

			DateFormat destDf22 = new SimpleDateFormat("MM-dd-yyyy");

			// format the date into another format
			dateStr22 = destDf22.format(date22);

			Date endDate = destDf22.parse(dateStr22);

			System.out.println("start dtae is" + startDate);
			System.out.println("end date is" + endDate);

			response.setContentType("application/vnd.ms-excel;charset=UTF-8");

			String reportName = "AMS User Creation Updation Report_.xls";
			response.setHeader("Content-disposition", "attachment; filename=" + reportName);
			out = response.getWriter();
			System.out.println("buffer size is" + response.getBufferSize());

			out.flush();

			int batchSize = 5;
			BigInteger batchSizeCount = new BigInteger("400");

			BigInteger count = tdcFileDaoImpl.RetreivingExcelInformationCount(300, startDate, endDate);

			int offset = 0;
			int limit = 0;
			int batchSize1 = 400;
			// int limit=200;

			Map<String, Object[]> data = new HashMap<String, Object[]>();

			Set<String> keyset = data.keySet();
			data.put("4",
					new Object[] { "Request Type", "First Name", "Last Name", "user ID", "Mobile Phone", "Birth date",
							"Email", "Nationality", "Company", "TDC user ID (MID)", "Ericsson Group / Role",
							"Approver Manager", "1st Request form sent on", "Request Id", "Tool-Group", "Tool",
							"Role/Account Rights", "Reason for access request", "Approval manager",
							"L1_Time[TimeStamp]", "L2_Time[TimeStamp]", "TDC_Time[TimeStamp]", "Sent to Sys Admin",
							"Received from Sys Admin", "SpocAdminComment", "TDC Comment", " System Admin Comment",
							"Request Status", "Manager Comments", "Request Raised By",
							"Requester for M2M system user First name", "Requester for M2M system user Last Name",
							"Requester for M2M system TDCId" });

			for (String key : keyset) {

				Object[] objArr = data.get(key);

				System.out.println("key is " + key);
				for (Object obj : objArr) {

					System.out.println("object converted to string is  " + (String) obj);

					if (obj instanceof String) {

						String item = ((String) obj).replaceAll("\\n", " ");
						out.print(item);
						System.out.println(" String insatnce");
						out.print("\t");

					}

				}
				out.print("\n");

			}

			int comparecount = count.compareTo(batchSizeCount);
			System.out.println("compare count is" + comparecount);
			System.out.println("count is" + count);

			int j = 0;
			int previousCid = 0;

			int batchSizeCounter = 0;
			// while(count.compareTo(batchSizeCount)>=0 )
			while (true)

			{

				limit = batchSize1;
				batchSizeCounter = batchSizeCounter + 1;

				System.out.println("value of offset" + offset);
				System.out.println("value of limit" + limit);

				/* if(batchSizeCounter%batchSize1==0){ */
				List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl.RetreivingExcelInformation(offset, limit,
						startDate, endDate);
				int counterForDeleteCase = 0;

				for (Object[] empInfo : RetreivingExcelInformationList)

				{

					String requestType = (String) empInfo[0];
					int currentCid = (int) empInfo[13];
					int counter = 0;
					System.out.println("request type is" + requestType);

					if (requestType.equals("User Creation") && previousCid != currentCid) {

						System.out.println(
								"Entering into User Creation and previous cid and current cid comparison block");
						for (Object obj : empInfo) {

							// if (counter>=14 && counter <=15)
							if (counter >= 14 && counter <= 15) {

								System.out.println("value d counter is" + counter);
								out.print("TDC Account (MID) ");
								out.print("\t");

							} else if (counter == 16) {
								out.print("Yes");
								out.print("\t");

							} else {

								if (obj != null) {

									System.out.println("value of items  inside if condition is" + obj);
									if (obj instanceof Date) {

										Date date = (Date) obj;
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										String strDate = dateFormat.format(date);

										DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
										DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
										Date date1 = originalFormat.parse(strDate);
										String formattedDate = targetFormat.format(date1);

										out.print(formattedDate);

										// cell.setCellValue((Date) obj);
										System.out.println("date instance");
										out.print("\t");
									}

									else if (obj instanceof Boolean) {
										out.print((String) obj);

										out.print("\t");
									} else if (obj instanceof String) {

										try {

											String item = ((String) obj).replaceAll("\\n", " ");
											// item = ((String)

											out.print(item);
											System.out.println(" String insatnce");
											out.print("\t");
										} catch (Exception e) {
											e.printStackTrace();
											_logger.error("error in tdc controller" + e);
											out.print(" ");
											out.print("**special character is coming**");
											out.print("\t");

										}

									} else if (obj instanceof Double) {
										out.print((String) obj);

										System.out.println(" Double insatnce");
										out.print("\t");

									} else if (obj instanceof Integer) {
										System.out.println(
												"object is inside only else if  condition for integer instance" + obj);
										out.print(obj.toString());

										System.out.println(" integer insatnce");
										out.print("\t");

									} else {
										System.out.println("object is inside only else condition" + obj);
										out.print(obj.toString());

										System.out.println(" integer insatnce");
										out.print("\t");
									}

								} else {

									System.out.println("object" + obj);
									System.out.println("null instance insatnce");
									out.print(" ");
									out.print("\t");
								}

							}

							counter = counter + 1;

						}

						out.print("\n");

					}

					previousCid = currentCid;
					counterForDeleteCase = 0;

					for (Object obj : empInfo) {

						System.out.println("object is" + obj);

						if (obj != null) {

							System.out.println("value of items  inside if condition is" + obj);
							if (obj instanceof Date) {

								Date date = (Date) obj;
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String strDate = dateFormat.format(date);

								DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
								DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
								Date date1 = originalFormat.parse(strDate);
								String formattedDate = targetFormat.format(date1);

								out.print(formattedDate);

								// cell.setCellValue((Date) obj);
								System.out.println("date instance");
								out.print("\t");
							}

							else if (obj instanceof Boolean) {
								out.print((String) obj);

								out.print("\t");
							} else if (obj instanceof String) {

								try {

									System.out.println("string instance is" + (String) obj);

									if (obj.equals(" ") && counterForDeleteCase == 15
											&& requestType.equalsIgnoreCase("User Deletion")) {
										out.print("TDCID(Account)");
									} else if (obj.equals(" ") && counterForDeleteCase == 16
											&& requestType.equalsIgnoreCase("User Deletion")) {
										out.print("Delete");
									} else {
										String item = ((String) obj).replaceAll("\\n", " ");

										System.out.println("string instance after replacement  is" + (String) obj);
										out.print(item);
									}
									System.out.println(" String insatnce");
									out.print("\t");
								}

								catch (Exception e) {
									e.printStackTrace();
									_logger.error("error in tdc controller" + e);
									out.print(" ");
									out.print("**special character is coming**");
									out.print("\t");

								}

							} else if (obj instanceof Double) {
								out.print((String) obj);

								System.out.println(" Double insatnce");
								out.print("\t");

							} else if (obj instanceof Integer) {
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");

							} else {
								System.out.println("object is inside only else condition" + obj);
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");
							}

						} else {

							System.out.println("object" + obj);
							System.out.println("null instance insatnce");
							out.print(" ");
							out.print("\t");
						}
						counterForDeleteCase++;
					}

					out.print("\n");

				}
				System.out.println("count is" + count);
				System.out.println("batchSizeCount is" + batchSizeCount);

				count = count.subtract(batchSizeCount);
				System.out.println("limit is" + limit);
				// limit=limit+1;
				// offset=limit+1;
				// offset=limit;
				/*
				 * if(offset>=20) { break; }
				 */

				offset = offset + batchSize1;

				System.out.println("in  while loop");
				System.out.println("count is" + count);
				System.out.println("offset is" + offset);
				System.out.println("batchSize1 is" + batchSize1);
				System.out.println("limit is" + limit);
				if (count.compareTo(batchSizeCount) >= 0) {

				} else {
					break;
				}

				// }
				out.flush();
			}
			System.out.println("after while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/*************/

			List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl.RetreivingExcelInformation(offset, limit,
					startDate, endDate);

			System.out.println("list size is" + RetreivingExcelInformationList.size());
			int counterForDeleteCase = 0;
			for (Object[] empInfo : RetreivingExcelInformationList)

			{

				String requestType = (String) empInfo[0];
				int currentCid = (int) empInfo[13];
				int counter = 0;
				System.out.println("request type is" + requestType);

				if (requestType.equals("User Creation") && previousCid != currentCid) {

					System.out.println("Entering into User Creation and previous cid and current cid comparison block");
					for (Object obj : empInfo) {

						// if (counter>=14 && counter <=15)
						if (counter >= 14 && counter <= 15) {

							System.out.println("value d counter is" + counter);
							out.print("TDC Account (MID) ");
							out.print("\t");

						} else if (counter == 16) {
							out.print("Yes");
							out.print("\t");

						} else {

							if (obj != null) {

								System.out.println("value of items  inside if condition is" + obj);
								if (obj instanceof Date) {

									Date date = (Date) obj;
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									String strDate = dateFormat.format(date);

									DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
									DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
									Date date1 = originalFormat.parse(strDate);
									String formattedDate = targetFormat.format(date1);

									out.print(formattedDate);

									// cell.setCellValue((Date) obj);
									System.out.println("date instance");
									out.print("\t");
								}

								else if (obj instanceof Boolean) {
									out.print((String) obj);

									out.print("\t");
								} else if (obj instanceof String) {

									try {
										String item = ((String) obj).replaceAll("\\n", " ");

										out.print(item);
										System.out.println(" String insatnce");
										out.print("\t");
									} catch (Exception e) {
										e.printStackTrace();
										_logger.error("error in tdc controller" + e);
										out.print(" ");
										out.print("**special character is coming**");
										out.print("\t");

									}

								} else if (obj instanceof Double) {
									out.print((String) obj);

									System.out.println(" Double insatnce");
									out.print("\t");

								} else if (obj instanceof Integer) {
									System.out.println(
											"object is inside only else if  condition for integer instance" + obj);
									out.print(obj.toString());

									System.out.println(" integer insatnce");
									out.print("\t");

								} else {
									System.out.println("object is inside only else condition" + obj);
									out.print(obj.toString());

									System.out.println(" integer insatnce");
									out.print("\t");
								}

							} else {

								System.out.println("object" + obj);
								System.out.println("null instance insatnce");
								out.print(" ");
								out.print("\t");
							}

						}

						counter = counter + 1;

					}

					out.print("\n");

				}

				previousCid = currentCid;
				counterForDeleteCase = 0;

				for (Object obj : empInfo) {

					System.out.println("object is" + obj);

					if (obj != null) {

						System.out.println("value of items  inside if condition is" + obj);
						if (obj instanceof Date) {

							Date date = (Date) obj;
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							String strDate = dateFormat.format(date);

							DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
							DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
							Date date1 = originalFormat.parse(strDate);
							String formattedDate = targetFormat.format(date1);

							out.print(formattedDate);

							// cell.setCellValue((Date) obj);
							System.out.println("date instance");
							out.print("\t");
						}

						else if (obj instanceof Boolean) {
							out.print((String) obj);

							out.print("\t");
						} else if (obj instanceof String) {

							try {
								if (obj.equals(" ") && counterForDeleteCase == 15
										&& requestType.equalsIgnoreCase("User Deletion")) {
									out.print("TDCID(Account)");
								} else if (obj.equals(" ") && counterForDeleteCase == 16
										&& requestType.equalsIgnoreCase("User Deletion")) {
									out.print("Delete");
								} else {

									String item = ((String) obj).replaceAll("\\n", " ");

									out.print(item);
								}
								System.out.println(" String insatnce");
								out.print("\t");
							} catch (Exception e) {
								e.printStackTrace();
								_logger.error("error in tdc controller" + e);
								out.print(" ");
								out.print("**special character is coming**");
								out.print("\t");

							}

						} else if (obj instanceof Double) {
							out.print((String) obj);

							System.out.println(" Double insatnce");
							out.print("\t");

						} else if (obj instanceof Integer) {
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");

						} else {
							System.out.println("object is inside only else condition" + obj);
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");
						}

					} else {

						System.out.println("object" + obj);
						System.out.println("null instance insatnce");
						out.print(" ");
						out.print("\t");
					}
					counterForDeleteCase++;

				}

				out.print("\n");

			}
			System.out.println("count is" + count);
			System.out.println("batchSizeCount is" + batchSizeCount);

			count = count.subtract(batchSizeCount);
			System.out.println("limit is" + limit);
			// limit=limit+1;
			// offset=limit+1;
			// offset=limit;
			/*
			 * if(offset>=20) { break; }
			 */

			offset = offset + batchSize1;

			System.out.println("in  while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/****************/

			System.out.println("start dtae is" + startDate);
			System.out.println("end date is" + endDate);
			_logger.info("exiting from  fileDownloadFromServer.htm");
		} catch (Exception e) {
			_logger.error("error in  fileDownloadFromServer.htm" + e);
			e.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}

	}

	@RequestMapping("fileDownloadFromServerAccSignum.htm")
	public void fileDownloadFromServerAccSignum(HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttrs) throws Exception

	{

		String signumRequestParam = request.getParameter("signumId");

		System.out.println("signum id is" + signumRequestParam);

		_logger.info("entering into  fileDownloadFromServerAccSignum.htm");
		PrintWriter out = null;

		try {
			response.setContentType("application/vnd.ms-excel");
			String reportName = "AMS User Creation Updation Report_.xls";
			response.setHeader("Content-disposition", "attachment; filename=" + reportName);
			out = response.getWriter();
			/*
			 * Iterator<String> iter = rows.iterator(); while (iter.hasNext()) {
			 * String outputString = (String) iter.next();
			 * 
			 * }
			 */

			out.flush();

			int batchSize = 5;
			BigInteger batchSizeCount = new BigInteger("400");

			BigInteger count = tdcFileDaoImpl.RetreivingExcelInformationCountAccSignum(300, signumRequestParam);

			/*
			 * while( count.subtract(batchSizeCount)!=null) {
			 * 
			 * }
			 */
			int offset = 0;
			int limit = 0;
			int batchSize1 = 400;
			// int limit=200;

			Map<String, Object[]> data = new HashMap<String, Object[]>();

			Set<String> keyset = data.keySet();
			data.put("4",
					new Object[] { "Request Type", "First Name", "Last Name", "user ID", "Mobile Phone", "Birth date",
							"Email", "Nationality", "Company", "TDC user ID (MID)", "Ericsson Group / Role",
							"Approver Manager", "1st Request form sent on", "Request Id", "Tool-Group", "Tool",
							"Role/Account Rights", "Reason for access request", "Approval manager",
							"L1_Time[TimeStamp]", "L2_Time[TimeStamp]", "TDC_Time[TimeStamp]", "Sent to Sys Admin",
							"Received from Sys Admin", "SpocAdminComment", "TDC Comment", " System Admin Comment",
							"Request Status", "Manager Comments", "Request Raised By",
							"Requester for M2M system user First name", "Requester for M2M system user Last Name",
							"Requester for M2M system TDCId" });

			for (String key : keyset) {

				Object[] objArr = data.get(key);

				System.out.println("key is " + key);
				for (Object obj : objArr) {

					System.out.println("object converted to string is  " + (String) obj);

					if (obj instanceof String) {

						try {

							String item = ((String) obj).replaceAll("\\n", " ");
							out.print(item);
							System.out.println(" String insatnce");
							out.print("\t");
						} catch (Exception e) {
							e.printStackTrace();
							_logger.error("error in tdc controller" + e);
							out.print(" ");
							out.print("**special character is coming**");
							out.print("\t");

						}

					}

				}
				out.print("\n");

			}

			int comparecount = count.compareTo(batchSizeCount);
			System.out.println("compare count is" + comparecount);
			System.out.println("count is" + count);

			int j = 0;
			int previousCid = 0;

			int batchSizeCounter = 0;
			// while(count.compareTo(batchSizeCount)>=0 )
			while (true)

			{

				limit = batchSize1;
				batchSizeCounter = batchSizeCounter + 1;

				System.out.println("value of offset" + offset);
				System.out.println("value of limit" + limit);

				/* if(batchSizeCounter%batchSize1==0){ */
				List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl
						.RetreivingExcelInformationAccSignum(offset, limit, signumRequestParam);
				int counterForDeleteCase = 0;
				for (Object[] empInfo : RetreivingExcelInformationList)

				{

					String requestType = (String) empInfo[0];
					int currentCid = (int) empInfo[13];
					int counter = 0;
					System.out.println("request type is" + requestType);

					if (requestType.equals("User Creation") && previousCid != currentCid) {

						System.out.println(
								"Entering into User Creation and previous cid and current cid comparison block");
						for (Object obj : empInfo) {

							// if (counter>=14 && counter <=15)
							if (counter >= 14 && counter <= 15) {

								System.out.println("value d counter is" + counter);
								out.print("TDC Account (MID) ");
								out.print("\t");

							} else if (counter == 16) {
								out.print("Yes");
								out.print("\t");

							} else {

								if (obj != null) {

									System.out.println("value of items  inside if condition is" + obj);
									if (obj instanceof Date) {

										Date date = (Date) obj;
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										String strDate = dateFormat.format(date);

										DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
										DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
										Date date1 = originalFormat.parse(strDate);
										String formattedDate = targetFormat.format(date1);

										out.print(formattedDate);

										// cell.setCellValue((Date) obj);
										System.out.println("date instance");
										out.print("\t");
									}

									else if (obj instanceof Boolean) {
										out.print((String) obj);

										out.print("\t");
									} else if (obj instanceof String) {

										try {
											String item = ((String) obj).replaceAll("\\n", " ");

											out.print(item);
											System.out.println(" String insatnce");
											out.print("\t");
										} catch (Exception e) {
											e.printStackTrace();
											_logger.error("error in tdc controller" + e);
											out.print(" ");
											out.print("**special character is coming**");
											out.print("\t");

										}

									} else if (obj instanceof Double) {
										out.print((String) obj);

										System.out.println(" Double insatnce");
										out.print("\t");

									} else if (obj instanceof Integer) {
										System.out.println(
												"object is inside only else if  condition for integer instance" + obj);
										out.print(obj.toString());

										System.out.println(" integer insatnce");
										out.print("\t");

									} else {
										System.out.println("object is inside only else condition" + obj);
										out.print(obj.toString());

										System.out.println(" integer insatnce");
										out.print("\t");
									}

								} else {

									System.out.println("object" + obj);
									System.out.println("null instance insatnce");
									out.print(" ");
									out.print("\t");
								}

							}

							counter = counter + 1;

						}

						out.print("\n");

					}

					previousCid = currentCid;
					counterForDeleteCase = 0;
					for (Object obj : empInfo) {

						System.out.println("object is" + obj);

						if (obj != null) {

							System.out.println("value of items  inside if condition is" + obj);
							if (obj instanceof Date) {

								Date date = (Date) obj;
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String strDate = dateFormat.format(date);

								DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
								DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
								Date date1 = originalFormat.parse(strDate);
								String formattedDate = targetFormat.format(date1);

								out.print(formattedDate);

								// cell.setCellValue((Date) obj);
								System.out.println("date instance");
								out.print("\t");
							}

							else if (obj instanceof Boolean) {
								out.print((String) obj);

								out.print("\t");
							} else if (obj instanceof String) {

								try {
									if (obj.equals(" ") && counterForDeleteCase == 15
											&& requestType.equalsIgnoreCase("User Deletion")) {
										out.print("TDCID(Account)");
									} else if (obj.equals(" ") && counterForDeleteCase == 16
											&& requestType.equalsIgnoreCase("User Deletion")) {
										out.print("Delete");
									} else {
										String item = ((String) obj).replaceAll("\\n", " ");
										out.print(item);
									}
									System.out.println(" String insatnce");
									out.print("\t");
								} catch (Exception e) {
									e.printStackTrace();
									_logger.error("error in tdc controller" + e);
									out.print(" ");
									out.print("**special character is coming**");
									out.print("\t");

								}

							} else if (obj instanceof Double) {
								out.print((String) obj);

								System.out.println(" Double insatnce");
								out.print("\t");

							} else if (obj instanceof Integer) {
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");

							} else {
								System.out.println("object is inside only else condition" + obj);
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");
							}

						} else {

							System.out.println("object" + obj);
							System.out.println("null instance insatnce");
							out.print(" ");
							out.print("\t");
						}
						counterForDeleteCase++;
					}

					out.print("\n");

				}
				System.out.println("count is" + count);
				System.out.println("batchSizeCount is" + batchSizeCount);

				count = count.subtract(batchSizeCount);
				System.out.println("limit is" + limit);

				offset = offset + batchSize1;

				System.out.println("in  while loop");
				System.out.println("count is" + count);
				System.out.println("offset is" + offset);
				System.out.println("batchSize1 is" + batchSize1);
				System.out.println("limit is" + limit);
				if (count.compareTo(batchSizeCount) >= 0) {

				} else {
					break;
				}

				// }
				out.flush();
			}
			System.out.println("after while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/*************/

			List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl.RetreivingExcelInformationAccSignum(offset,
					limit, signumRequestParam);

			System.out.println("list size is" + RetreivingExcelInformationList.size());
			int counterForDeleteCase = 0;
			for (Object[] empInfo : RetreivingExcelInformationList)

			{

				String requestType = (String) empInfo[0];
				int currentCid = (int) empInfo[13];
				int counter = 0;
				System.out.println("request type is" + requestType);

				if (requestType.equals("User Creation") && previousCid != currentCid) {

					System.out.println("Entering into User Creation and previous cid and current cid comparison block");
					for (Object obj : empInfo) {

						// if (counter>=14 && counter <=15)
						if (counter >= 14 && counter <= 15) {

							System.out.println("value d counter is" + counter);
							out.print("TDC Account (MID) ");
							out.print("\t");

						} else if (counter == 16) {
							out.print("Yes");
							out.print("\t");

						} else {

							if (obj != null) {

								System.out.println("value of items  inside if condition is" + obj);
								if (obj instanceof Date) {

									Date date = (Date) obj;
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									String strDate = dateFormat.format(date);

									DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
									DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
									Date date1 = originalFormat.parse(strDate);
									String formattedDate = targetFormat.format(date1);

									out.print(formattedDate);

									// cell.setCellValue((Date) obj);
									System.out.println("date instance");
									out.print("\t");
								}

								else if (obj instanceof Boolean) {
									out.print((String) obj);

									out.print("\t");
								} else if (obj instanceof String) {

									try {

										String item = ((String) obj).replaceAll("\\n", " ");

										out.print(item);
										System.out.println(" String insatnce");
										out.print("\t");
									} catch (Exception e) {
										e.printStackTrace();
										_logger.error("error in tdc controller" + e);
										out.print(" ");
										out.print("**special character is coming**");
										out.print("\t");

									}

								} else if (obj instanceof Double) {
									out.print((String) obj);

									System.out.println(" Double insatnce");
									out.print("\t");

								} else if (obj instanceof Integer) {
									System.out.println(
											"object is inside only else if  condition for integer instance" + obj);
									out.print(obj.toString());

									System.out.println(" integer insatnce");
									out.print("\t");

								} else {
									System.out.println("object is inside only else condition" + obj);
									out.print(obj.toString());

									System.out.println(" integer insatnce");
									out.print("\t");
								}

							} else {

								System.out.println("object" + obj);
								System.out.println("null instance insatnce");
								out.print(" ");
								out.print("\t");
							}

						}

						counter = counter + 1;

					}

					out.print("\n");

				}

				previousCid = currentCid;
				counterForDeleteCase = 0;
				for (Object obj : empInfo) {

					System.out.println("object is" + obj);

					if (obj != null) {

						System.out.println("value of items  inside if condition is" + obj);
						if (obj instanceof Date) {

							Date date = (Date) obj;
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							String strDate = dateFormat.format(date);

							DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
							DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
							Date date1 = originalFormat.parse(strDate);
							String formattedDate = targetFormat.format(date1);

							out.print(formattedDate);

							// cell.setCellValue((Date) obj);
							System.out.println("date instance");
							out.print("\t");
						}

						else if (obj instanceof Boolean) {
							out.print((String) obj);

							out.print("\t");
						} else if (obj instanceof String) {

							try {
								if (obj.equals(" ") && counterForDeleteCase == 15
										&& requestType.equalsIgnoreCase("User Deletion")) {
									out.print("TDCID(Account)");
								} else if (obj.equals(" ") && counterForDeleteCase == 16
										&& requestType.equalsIgnoreCase("User Deletion")) {
									out.print("Delete");
								} else {
									String item = ((String) obj).replaceAll("\\n", " ");
									out.print(item);
								}

								System.out.println(" String insatnce");
								out.print("\t");
							} catch (Exception e) {
								e.printStackTrace();
								_logger.error("error in tdc controller" + e);
								out.print(" ");
								out.print("**special character is coming**");
								out.print("\t");

							}

						} else if (obj instanceof Double) {
							out.print((String) obj);

							System.out.println(" Double insatnce");
							out.print("\t");

						} else if (obj instanceof Integer) {
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");

						} else {
							System.out.println("object is inside only else condition" + obj);
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");
						}

					} else {

						System.out.println("object" + obj);
						System.out.println("null instance insatnce");
						out.print(" ");
						out.print("\t");
					}
					counterForDeleteCase++;
				}

				out.print("\n");

			}
			System.out.println("count is" + count);
			System.out.println("batchSizeCount is" + batchSizeCount);

			count = count.subtract(batchSizeCount);
			System.out.println("limit is" + limit);
			// limit=limit+1;
			// offset=limit+1;
			// offset=limit;
			/*
			 * if(offset>=20) { break; }
			 */

			offset = offset + batchSize1;

			System.out.println("in  while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/****************/

			_logger.info("exiting from  fileDownloadFromServerAccSignum.htm");
		} catch (Exception e) {
			_logger.error("error in  fileDownloadFromServeAccSignum.htm" + e);
		} finally {
			out.flush();
			out.close();
		}

	}

	@RequestMapping("historicalFileDownloadFromServer.htm")
	public void historicalFileDownloadFromServer(HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttrs) throws Exception

	{

		String startDateRequestParam = request.getParameter("startDateId");
		String endDateRequestParam = request.getParameter("endDateId");
		PrintWriter out = null;
		_logger.info("entering into historicalFileDownloadFromServer.htm");

		try {
			String dateStr11 = startDateRequestParam;

			DateFormat srcDf11 = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date11 = srcDf11.parse(dateStr11);

			DateFormat destDf11 = new SimpleDateFormat("MM-dd-yyyy");

			// format the date into another format
			dateStr11 = destDf11.format(date11);

			Date startDate = destDf11.parse(dateStr11);

			String dateStr22 = endDateRequestParam;

			DateFormat srcDf22 = new SimpleDateFormat("dd-MM-yyyy");

			// parse the date string into Date object
			Date date22 = srcDf22.parse(dateStr22);

			DateFormat destDf22 = new SimpleDateFormat("MM-dd-yyyy");

			// format the date into another format
			dateStr22 = destDf22.format(date22);

			Date endDate = destDf22.parse(dateStr22);

			System.out.println("start dtae is" + startDate);
			System.out.println("end date is" + endDate);
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");

			String reportName = "AMS User Creation Updation Report_.xls";
			response.setHeader("Content-disposition", "attachment; filename=" + reportName);
			response.setBufferSize(1024);
			out = response.getWriter();

			out.flush();

			BigInteger batchSizeCount = new BigInteger("400");

			BigInteger count = tdcFileDaoImpl.RetreivingHistoricalExcelInformationCount(300, startDate, endDate);

			int offset = 0;
			int limit = 0;
			int batchSize1 = 400;
			// int limit=200;

			Map<String, Object[]> data = new HashMap<String, Object[]>();

			Set<String> keyset = data.keySet();
			data.put("4",
					new Object[] { "user ID", "First Name", "Last Name", "Mobile Phone", "Birth date", "Email",
							"Nationality", "Company", "TDC user ID (MID)", "Ericsson Group / Role", "Approver Manager",
							"1st Request form sent on", "Tool-Group", "Tool", "Role/Account Rights",
							"Reason for access request", "Approval manager", "L1_Time[TimeStamp]", "L2_Time[TimeStamp]",
							"TDC_Time[TimeStamp]", "Sent to Sys Admin", "Received from Sys Admin" });

			for (String key : keyset) {

				Object[] objArr = data.get(key);

				System.out.println("key is " + key);
				for (Object obj : objArr) {

					System.out.println("object converted to string is  " + (String) obj);

					if (obj instanceof String) {

						try {
							String item = ((String) obj).replaceAll("\\n", " ");

							out.print(item);
							System.out.println(" String insatnce");
							out.print("\t");
						} catch (Exception e) {
							e.printStackTrace();
							_logger.error("error in tdc controller" + e);
							out.print(" ");
							out.print("**special character is coming**");
							out.print("\t");

						}

					}

				}
				out.print("\n");

			}

			int comparecount = count.compareTo(batchSizeCount);
			System.out.println("compare count is" + comparecount);
			System.out.println("count is" + count);

			int j = 0;
			int previousCid = 0;

			int batchSizeCounter = 0;
			// while(count.compareTo(batchSizeCount)>=0 )
			while (true)

			/*
			 * for(BigInteger bi = count; bi.compareTo(BigInteger.ZERO) > 0; bi
			 * = bi.subtract(BigInteger.ONE))
			 */
			{

				limit = batchSize1;
				batchSizeCounter = batchSizeCounter + 1;

				System.out.println("value of offset" + offset);
				System.out.println("value of limit" + limit);

				List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl
						.RetreivingHistoricalExcelInformation(offset, limit, startDate, endDate);

				for (Object[] empInfo : RetreivingExcelInformationList)

				{

					for (Object obj : empInfo) {

						System.out.println("object is" + obj);

						if (obj != null) {

							System.out.println("value of items  inside if condition is" + obj);
							if (obj instanceof Date) {

								Date date = (Date) obj;
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String strDate = dateFormat.format(date);

								DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
								DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
								Date date1 = originalFormat.parse(strDate);
								String formattedDate = targetFormat.format(date1);

								out.print(formattedDate);

								// cell.setCellValue((Date) obj);
								System.out.println("date instance");
								out.print("\t");
							}

							else if (obj instanceof Boolean) {
								out.print((String) obj);

								out.print("\t");
							} else if (obj instanceof String) {
								try {
									String item = ((String) obj).replaceAll("\\n", " ");

									out.print(item);
									System.out.println(" String insatnce");
									out.print("\t");
								} catch (Exception e) {
									e.printStackTrace();
									_logger.error("error in tdc controller" + e);
									out.print(" ");
									out.print("**special character is coming**");
									out.print("\t");

								}

							} else if (obj instanceof Double) {
								out.print((String) obj);

								System.out.println(" Double insatnce");
								out.print("\t");

							} else if (obj instanceof Integer) {
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");

							} else {
								System.out.println("object is inside only else condition" + obj);
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");
							}

						} else {

							System.out.println("object" + obj);
							System.out.println("null instance insatnce");
							out.print(" ");
							out.print("\t");
						}

					}

					out.print("\n");

				}
				System.out.println("count is" + count);
				System.out.println("batchSizeCount is" + batchSizeCount);

				count = count.subtract(batchSizeCount);
				System.out.println("limit is" + limit);
				// limit=limit+1;
				// offset=limit+1;
				// offset=limit;
				/*
				 * if(offset>=20) { break; }
				 */

				offset = offset + batchSize1;

				System.out.println("in  while loop");
				System.out.println("count is" + count);
				System.out.println("offset is" + offset);
				System.out.println("batchSize1 is" + batchSize1);
				System.out.println("limit is" + limit);
				if (count.compareTo(batchSizeCount) >= 0) {

				} else {
					break;
				}

				// }
				out.flush();
			}
			System.out.println("after while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/*************/

			List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl.RetreivingHistoricalExcelInformation(offset,
					limit, startDate, endDate);

			System.out.println("list size is" + RetreivingExcelInformationList.size());

			for (Object[] empInfo : RetreivingExcelInformationList)

			{

				for (Object obj : empInfo) {

					System.out.println("object is" + obj);

					if (obj != null) {

						System.out.println("value of items  inside if condition is" + obj);
						if (obj instanceof Date) {

							Date date = (Date) obj;
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							String strDate = dateFormat.format(date);

							DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
							DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
							Date date1 = originalFormat.parse(strDate);
							String formattedDate = targetFormat.format(date1);

							out.print(formattedDate);

							// cell.setCellValue((Date) obj);
							System.out.println("date instance");
							out.print("\t");
						}

						else if (obj instanceof Boolean) {
							out.print((String) obj);

							out.print("\t");
						} else if (obj instanceof String) {

							try {
								String item = ((String) obj).replaceAll("\\n", " ");
								out.print(item);
								System.out.println(" String insatnce");
								out.print("\t");
							} catch (Exception e) {
								e.printStackTrace();
								_logger.error("error in tdc controller" + e);
								out.print(" ");
								out.print("**special character is coming**");
								out.print("\t");

							}

						} else if (obj instanceof Double) {
							out.print((String) obj);

							System.out.println(" Double insatnce");
							out.print("\t");

						} else if (obj instanceof Integer) {
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");

						} else {
							System.out.println("object is inside only else condition" + obj);
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");
						}

					} else {

						System.out.println("object" + obj);
						System.out.println("null instance insatnce");
						out.print(" ");
						out.print("\t");
					}

				}

				out.print("\n");

			}
			System.out.println("count is" + count);
			System.out.println("batchSizeCount is" + batchSizeCount);

			count = count.subtract(batchSizeCount);
			System.out.println("limit is" + limit);
			// limit=limit+1;
			// offset=limit+1;
			// offset=limit;
			/*
			 * if(offset>=20) { break; }
			 */

			offset = offset + batchSize1;

			System.out.println("in  while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);
			/*
			 * if(count.compareTo(batchSizeCount)>=0 ||
			 * count.compareTo(batchSizeCount)<=-4 ) {
			 * 
			 * } else { break; }
			 */

			/****************/

			// List<Object[]>list=
			// tdcFileDaoImpl.RetreivingExcelInformation(300);

			// create some sample data

			// return a view which will be resolved by an excel view resolver
			// return new ModelAndView("excelView", "list", list);

			System.out.println("start dtae is" + startDate);
			System.out.println("end date is" + endDate);
			_logger.info("exiting from  fileDownloadFromServer.htm");
		} catch (Exception e) {
			_logger.error("error in  fileDownloadFromServer.htm" + e);
		} finally {
			out.flush();
			out.close();
		}

	}

	@RequestMapping("historicalFileDownloadFromServerAccSignum.htm")
	public void historicalFileDownloadFromServerAccSignum(HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttrs) throws Exception

	{

		_logger.info("entering into historicalFileDownloadFromServerAccSignum.htm");
		PrintWriter out = null;

		try {

			String signumRequestParam = request.getParameter("signumId");
			System.out.println("signum id is" + signumRequestParam);

			response.setContentType("application/vnd.ms-excel");
			String reportName = "AMS User Creation Updation Report_.xls";
			response.setHeader("Content-disposition", "attachment; filename=" + reportName);

			out = response.getWriter();
			out.flush();

			BigInteger batchSizeCount = new BigInteger("400");

			BigInteger count = tdcFileDaoImpl.RetreivingHistoricalExcelInformationCountAccSignum(300,
					signumRequestParam);

			int offset = 0;
			int limit = 0;
			int batchSize1 = 400;

			Map<String, Object[]> data = new HashMap<String, Object[]>();

			Set<String> keyset = data.keySet();
			data.put("4",
					new Object[] { "user ID", "First Name", "Last Name", "Mobile Phone", "Birth date", "Email",
							"Nationality", "Company", "TDC user ID (MID)", "Ericsson Group / Role", "Approver Manager",
							"1st Request form sent on", "Tool-Group", "Tool", "Role/Account Rights",
							"Reason for access request", "Approval manager", "L1_Time[TimeStamp]", "L2_Time[TimeStamp]",
							"TDC_Time[TimeStamp]", "Sent to Sys Admin", "Received from Sys Admin" });

			for (String key : keyset) {

				Object[] objArr = data.get(key);

				System.out.println("key is " + key);
				for (Object obj : objArr) {

					System.out.println("object converted to string is  " + (String) obj);

					if (obj instanceof String) {
						try {

							String item = ((String) obj).replaceAll("\\n", " ");

							out.print(item);
							System.out.println(" String insatnce");
							out.print("\t");
						} catch (Exception e) {
							e.printStackTrace();
							_logger.error("error in tdc controller" + e);
							out.print(" ");
							out.print("**special character is coming**");
							out.print("\t");

						}

					}

				}
				out.print("\n");

			}

			int comparecount = count.compareTo(batchSizeCount);
			System.out.println("compare count is" + comparecount);
			System.out.println("count is" + count);

			int j = 0;
			int previousCid = 0;

			int batchSizeCounter = 0;
			// while(count.compareTo(batchSizeCount)>=0 )
			while (true)

			{

				limit = batchSize1;
				batchSizeCounter = batchSizeCounter + 1;

				System.out.println("value of offset" + offset);
				System.out.println("value of limit" + limit);

				List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl
						.RetreivingHistoricalExcelInformationAccSignum(offset, limit, signumRequestParam);

				for (Object[] empInfo : RetreivingExcelInformationList)

				{

					for (Object obj : empInfo) {

						System.out.println("object is" + obj);

						if (obj != null) {

							System.out.println("value of items  inside if condition is" + obj);
							if (obj instanceof Date) {

								Date date = (Date) obj;
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String strDate = dateFormat.format(date);

								DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
								DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
								Date date1 = originalFormat.parse(strDate);
								String formattedDate = targetFormat.format(date1);

								out.print(formattedDate);

								// cell.setCellValue((Date) obj);
								System.out.println("date instance");
								out.print("\t");
							}

							else if (obj instanceof Boolean) {
								out.print((String) obj);

								out.print("\t");
							} else if (obj instanceof String) {

								try {
									String item = ((String) obj).replaceAll("\\n", " ");

									out.print(item);
									System.out.println(" String insatnce");
									out.print("\t");
								} catch (Exception e) {
									e.printStackTrace();
									_logger.error("error in tdc controller" + e);
									out.print(" ");
									out.print("**special character is coming**");
									out.print("\t");

								}

							} else if (obj instanceof Double) {
								out.print((String) obj);

								System.out.println(" Double insatnce");
								out.print("\t");

							} else if (obj instanceof Integer) {
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");

							} else {
								System.out.println("object is inside only else condition" + obj);
								out.print(obj.toString());

								System.out.println(" integer insatnce");
								out.print("\t");
							}

						} else {

							System.out.println("object" + obj);
							System.out.println("null instance insatnce");
							out.print(" ");
							out.print("\t");
						}

					}

					out.print("\n");

				}
				System.out.println("count is" + count);
				System.out.println("batchSizeCount is" + batchSizeCount);

				count = count.subtract(batchSizeCount);
				System.out.println("limit is" + limit);

				offset = offset + batchSize1;

				System.out.println("in  while loop");
				System.out.println("count is" + count);
				System.out.println("offset is" + offset);
				System.out.println("batchSize1 is" + batchSize1);
				System.out.println("limit is" + limit);
				if (count.compareTo(batchSizeCount) >= 0) {

				} else {
					break;
				}

				// }
				out.flush();
			}
			System.out.println("after while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/*************/

			List<Object[]> RetreivingExcelInformationList = tdcFileDaoImpl
					.RetreivingHistoricalExcelInformationAccSignum(offset, limit, signumRequestParam);

			System.out.println("list size is" + RetreivingExcelInformationList.size());

			for (Object[] empInfo : RetreivingExcelInformationList)

			{

				for (Object obj : empInfo) {

					System.out.println("object is" + obj);

					if (obj != null) {

						System.out.println("value of items  inside if condition is" + obj);
						if (obj instanceof Date) {

							Date date = (Date) obj;
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							String strDate = dateFormat.format(date);

							DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
							DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
							Date date1 = originalFormat.parse(strDate);
							String formattedDate = targetFormat.format(date1);

							out.print(formattedDate);

							// cell.setCellValue((Date) obj);
							System.out.println("date instance");
							out.print("\t");
						}

						else if (obj instanceof Boolean) {
							out.print((String) obj);

							out.print("\t");
						} else if (obj instanceof String) {
							try {
								String item = ((String) obj).replaceAll("\\n", " ");
								out.print(item);
								System.out.println(" String insatnce");
								out.print("\t");
							} catch (Exception e) {
								e.printStackTrace();
								_logger.error("error in tdc controller" + e);
								out.print(" ");
								out.print("**special character is coming**");
								out.print("\t");

							}

						} else if (obj instanceof Double) {
							out.print((String) obj);

							System.out.println(" Double insatnce");
							out.print("\t");

						} else if (obj instanceof Integer) {
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");

						} else {
							System.out.println("object is inside only else condition" + obj);
							out.print(obj.toString());

							System.out.println(" integer insatnce");
							out.print("\t");
						}

					} else {

						System.out.println("object" + obj);
						System.out.println("null instance insatnce");
						out.print(" ");
						out.print("\t");
					}

				}

				out.print("\n");

			}
			System.out.println("count is" + count);
			System.out.println("batchSizeCount is" + batchSizeCount);

			count = count.subtract(batchSizeCount);
			System.out.println("limit is" + limit);

			offset = offset + batchSize1;

			System.out.println("in  while loop");
			System.out.println("count is" + count);
			System.out.println("offset is" + offset);
			System.out.println("batchSize1 is" + batchSize1);
			System.out.println("limit is" + limit);

			/****************/

			_logger.info("exiting from   fileDownloadFromServer.htm");

		} catch (Exception e) {
			_logger.error("error in  fileDownloadFromServer.htm" + e);
		} finally {
			out.flush();
			out.close();
		}

	}

}
