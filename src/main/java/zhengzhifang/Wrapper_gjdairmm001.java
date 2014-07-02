package zhengzhifang;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.moneyeye.wrapper.Collector;
import com.qunar.moneyeye.wrapper.WrapperCounter.Key;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;


public class Wrapper_gjdairmm001 implements QunarCrawler {

	private static final String CODEBASE = "gjdairmm001";
	private QFHttpClient httpClient = null;

	public static void main(String[] args) {
		Wrapper_gjdairmm001 p = new Wrapper_gjdairmm001();
		// TODO 根据自己需要修改url //OKA ISG
		FlightSearchParam flightSearchParam = new FlightSearchParam();
		flightSearchParam.setDep("HKG");
		flightSearchParam.setArr("KIX");
		flightSearchParam.setDepDate("2014-07-11");
		flightSearchParam.setWrapperid("gjdairmm001");
		flightSearchParam.setQueryId("http://???");
		String html = p.getHtml(flightSearchParam);
		System.out.println("开始打印：" + html);
		System.out.println("结束打印");
		System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
		BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(bookingResult));
	}


	private String[] findValidFlights(String[] str) {
		String[] flights = new String[str.length];
		int count = 0;
		for (String flight : str) {
			// 有航班,但航班信息部分无价格,不抓取
			if (flight.contains("Full")
					&& flight.indexOf("boarding_class_rcd") == -1) {
				continue;
			} else {
				flights[count] = flight;
				count++;
			}
		}
		String[] validFlights = new String[count];
		for (int i = 0; i < count; i++) {
			validFlights[i] = flights[i];
		}

		return validFlights;
	}

	private ArrayList<Flight> getFlights(String[] str, String html, String dep,
			String arr) throws ParseException {
		ArrayList<Flight> flights = new ArrayList<Flight>();
		String content = "";
		for (int i = 0; i < str.length; i++) {
			Flight flight = new Flight();
			content = getValue(str[i], "boarding_class_rcd:", "onclick");
			// 获取出发时间、到达时间
			flight.depTime = formatDepTime(content);
			flight.arrTime = formatArrTime(content);
			flight.arrDate = formatArrDate(content);
			// 获取出发机场、到达机场
			flight.dep = dep;
			flight.arr = arr;
			// 获取航班号
			flight.code = formatCode(content);
			// 获取机型
			flight.planeType = formatPlaneType(str[i]);
			// 获取货币符合
			flight.currencyCode = formatCurrencyCode(content);
			// 获取价格
			flight.retailPrice = formatPrice(content);
			// 抓取税费
			flight.tax = formatTax(html);
			flights.add(flight);
		}
		return flights;
	}


	private void getResult(ArrayList<OneWayFlightInfo> oneWayFlightInfos,ArrayList<Flight> str,String depdate) throws ParseException {
		ArrayList<Flight> flights = str;
		for (int i = 0; i < flights.size(); i++) {
			OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
			List<FlightSegement> segements = new ArrayList<FlightSegement>();
			FlightSegement flightSegement = new FlightSegement(); 
			FlightDetail detail = new FlightDetail();
			detail.setDepcity(flights.get(i).dep);
			detail.setArrcity(flights.get(i).arr);
			detail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(depdate));
			List<String> flightNoList = Lists.newArrayList();
			flightNoList.add(flights.get(i).code);
			detail.setFlightno(flightNoList);
			detail.setMonetaryunit(flights.get(i).currencyCode);
			detail.setTax(flights.get(i).tax);
			detail.setPrice(flights.get(i).retailPrice);
			detail.setWrapperid("gjdairmm001");
			detail.setCreatetime(new Timestamp(System.currentTimeMillis()));
	        detail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
	        oneWayFlightInfo.setDetail(detail);
	        flightSegement.setDepairport(flights.get(i).dep);
	        flightSegement.setArrairport(flights.get(i).arr);
	        flightSegement.setFlightno(flights.get(i).code);
	        flightSegement.setDeptime(flights.get(i).depTime);
	        flightSegement.setArrtime(flights.get(i).arrTime);
	        flightSegement.setDepDate(depdate);
	        flightSegement.setArrDate(flights.get(i).arrDate);   //到达日期
	        flightSegement.setCompany("MM");
	        flightSegement.setAircraft(flights.get(i).planeType);
	        segements.add(flightSegement);
	        oneWayFlightInfo.setInfo(segements);
	        oneWayFlightInfos.add(oneWayFlightInfo);
		}
	}

	private String formatCode(String str) {
		// code1获取航班号中的机场码，code2获取航班号中的数字
		String code1 = getValue(str, "airline_rcd:", "|flight_number");
		String code2 = getValue(str, "flight_number:", "|origin_rcd");
		String code = code1 + code2;
		return code;
	}

	private String formatDepTime(String str) {
		// 获取包含出发时间的字符串
		String depTime = getValue(str, "planned_departure_time:",
				"|planned_arrival_time").trim();
		// 将时间的5 这种格式转化为 00:05这种格式
		if (depTime.length() == 1) {
			depTime = "00:0" + depTime;
		}
		// 将时间的35这种格式转化为 00:35这种格式
		else if (depTime.length() == 2) {
			depTime = "00:" + depTime;
		}
		// 将时间的730 这种格式转化为 07:30这种格式
		else if (depTime.length() == 3) {
			depTime = "0" + depTime.substring(0, 1) + ":"
					+ depTime.substring(1);
		} else {
			depTime = depTime.substring(0, 2) + ":" + depTime.substring(2);
		}
		return depTime;
	}

	private String formatArrTime(String str) {
		String arrTime = getValue(str, "planned_arrival_time:",
				"|transit_airline_rcd").trim();
		// 将时间的5 这种格式转化为 00:05这种格式
		if (arrTime.length() == 1) {
			arrTime = "00:0" + arrTime;
		}
		// 将时间的35这种格式转化为 00:35这种格式
		else if (arrTime.length() == 2) {
			arrTime = "00:" + arrTime;
		}
		// 将时间的730 这种格式转化为 07:30这种格式
		else if (arrTime.length() == 3) {
			arrTime = "0" + arrTime.substring(0, 1) + ":"
					+ arrTime.substring(1);
		} else {
			arrTime = arrTime.substring(0, 2) + ":" + arrTime.substring(2);
		}
		return arrTime;
	}

	private String formatArrDate(String str) throws ParseException {
		String arrDate = getValue(str, "arrival_date:",
				"|number_of_adult").trim();
		// 将20140711这种格式转化为 2014-07-11
		return new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyyMMdd").parse(arrDate));
	}
	
	private float formatPrice(String str) {
		String price = getValue(str, "adult_fare:", "|child_fare").trim();
		price = price.replaceAll(",", "");
		float retailPrice = Float.parseFloat(price);
		return retailPrice;
	}

	private float formatTax(String str) {
		String taxStr1 = getValue(str, "TaxInfo", "class=\\\"clear-all\\\"");
		String taxStr2 = getValue(taxStr1,
				"class=\\\"charges_price_total\\\"\\u003e\\", "\\u003c/div");
		taxStr2 = taxStr2.replaceAll(",", "");
		String taxStr = getValue(taxStr2, "[0-9]+");
		float tax = 0;
		if (!taxStr.equals("")) {// TaxInfo //class=\"clear-all\" //
									// class=\"charges_price_total\"\u003e\ \\
									// \u003c/div
			tax = Float.parseFloat(taxStr);
		}
		return tax;
	}

	private String formatCurrencyCode(String str) {
		String currencyCode = getValue(str, "currency_rcd:",
				"|transit_arrival_date").trim();

		return currencyCode;
	}

	private String formatPlaneType(String str) {
		String planeType = getValue(str, "Aircraft", "/li").trim();
		planeType = getValue(planeType, "[A-Z|0-9]+");

		return planeType;
	}

	class Flight {
		String depTime;
		String arrTime;
		String dep;
		String arr;
		String code;
		String planeType;
		String currencyCode;
		float retailPrice;
		float tax;
		String arrDate;
	}

	public String getHtml(FlightSearchParam flightSearchParam) {
		// get all query parameters from the url set by wrapperSearchInterface

		// 将日期转变为网站所需的格式
		String[] temps = flightSearchParam.getDepDate().split("-");
		String date = temps[0] + temps[1] + temps[2];

		httpClient = new QFHttpClient(flightSearchParam, true);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);

		String refer = "http://book.flypeach.com/default.aspx?ao=B2CZHHK&ori="
				+ flightSearchParam.getDep() + "&des="
				+ flightSearchParam.getArr() + "&dep="
				+ flightSearchParam.getDepDate()
				+ "&adt=1&chd=0&inf=0&langculture=zh-HK&bLFF=false";
		// post 200 获取结果页面
		QFPostMethod post = new QFPostMethod(
				"http://book.flypeach.com/WebService/B2cService.asmx/GetAvailability");
		String body1 = "{\"origin\":\""
				+ flightSearchParam.getDep()
				+ "\",\"destination\":\""
				+ flightSearchParam.getArr()
				+ "\",\"dateFrom\":\""
				+ date
				+ "\",\"dateTo\":\""
				+ date
				+ "\",\"iOneWay\":\"true\",\"iFlightOnly\":\"0\",\"iAdult\":1,\"iChild\":0,\"iInfant\":0,\"BoardingClass\":\"\",\"CurrencyCode\":\"\",\"strPromoCode\":\"\",\"SearchType\":\"FARE\",\"iOther\":0,\"otherType\":\"\",\"strIpAddress\":\"\"}";
		QFPostMethod post2 = null;
		try {
			post.setRequestEntity(new ByteArrayRequestEntity(body1.getBytes()));
			post.setRequestHeader("Content-Type",
					"application/json; charset=utf-8");
			post.setRequestHeader("Referer", refer);

			httpClient.executeMethod(post);
			String firstRes = post.getResponseBodyAsString();

			/*
			 * if (org.apache.commons.lang.isEmpty(firstRes)) { return
			 * "PARAMERROR"; }
			 */
			if (org.apache.commons.lang.StringUtils.isEmpty(firstRes)) {
				return "PARAMERROR";
			}
			// 无效日期
			if (firstRes.indexOf("tr class=\\\"FlightInformation") == -1) {
				return "INVALID_DATE";
			}
			// 无结果，售完
			if (firstRes.contains("Full")
					&& firstRes.indexOf("boarding_class_rcd") == -1) {
				return "SOLD_OUT";
			}

			// 获取要抓取税费页面所要传的值
			String flight_id = getValue(firstRes, "flight_id:", "|fare_id:")
					.trim();
			String airline_rcd = getValue(firstRes, "airline_rcd:",
					"|flight_number:").trim();
			String flight_number = getValue(firstRes, "flight_number:",
					"|origin_rcd").trim();
			String fare_id = getValue(firstRes, "fare_id:",
					"|boarding_class_rcd").trim();
			String booking_class_rcd = getValue(firstRes, "booking_class_rcd:",
					"|airline_rcd").trim();
			String planned_departure_time = getValue(firstRes,
					"planned_departure_time:", "|planned_arrival_time").trim();
			String planned_arrival_time = getValue(firstRes,
					"planned_arrival_time:", "|transit_airline_rcd").trim();
			String currency_rcd = getValue(firstRes, "currency_rcd:",
					"|transit_arrival_date").trim();
			// post 200 获取含税费的页面
			if (flight_id != null && fare_id != null) {
				post2 = new QFPostMethod(
						"http://book.flypeach.com/WebService/B2cService.asmx/GetQuoteSummary");
				String body2 = "{\"strFlightXml\":\"<flights><flight><flight_id>"
						+ flight_id
						+ "</flight_id><airline_rcd>"
						+ airline_rcd
						+ "</airline_rcd><flight_number>"
						+ flight_number
						+ "</flight_number><origin_rcd>"
						+ flightSearchParam.getDep()
						+ "</origin_rcd><destination_rcd>"
						+ flightSearchParam.getArr()
						+ "</destination_rcd><fare_id>"
						+ fare_id
						+ "</fare_id><transit_airline_rcd></transit_airline_rcd><transit_flight_number></transit_flight_number><transit_flight_id></transit_flight_id><departure_date>"
						+ date
						+ "</departure_date><arrival_date>"
						+ date
						+ "</arrival_date><planned_departure_time>"
						+ planned_departure_time
						+ "</planned_departure_time><planned_arrival_time>"
						+ planned_arrival_time
						+ "</planned_arrival_time><transit_departure_date></transit_departure_date><transit_departure_day></transit_departure_day><transit_arrival_date></transit_arrival_date><transit_arrival_day></transit_arrival_day><transit_planned_departure_time></transit_planned_departure_time><transit_planned_arrival_time></transit_planned_arrival_time><transit_airport_rcd></transit_airport_rcd><transit_fare_id></transit_fare_id><booking_class_rcd>"
						+ booking_class_rcd
						+ "</booking_class_rcd><currency_rcd>"
						+ currency_rcd
						+ "</currency_rcd></flight></flights>\",\"strFlightType\":\"Outward\"}";

				post2.setRequestEntity(new ByteArrayRequestEntity(body2
						.getBytes()));
				post2.setRequestHeader("Content-Type",
						"application/json; charset=utf-8");
				post2.setRequestHeader("Accept-Encoding", "gzip, deflate");
				post2.setRequestHeader("Referer", refer);
				String cookie = org.apache.commons.lang.StringUtils.join(
						httpClient.getState().getCookies(), "; "); // 改动
				httpClient.getState().clearCookies();
				post2.setRequestHeader("Cookie", cookie);
				httpClient.executeMethod(post2);

				return firstRes + "\nHADTAX\n"
						+ post2.getResponseBodyAsString();
			}

		} catch (Exception e) {
			return "Exception";
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
			if (post2 != null) {
				post2.releaseConnection();
			}
		}
		return "Exception";
	}

	public ProcessResultInfo process(String html,
			FlightSearchParam flightSearchParam) {

		ProcessResultInfo processResultInfo = new ProcessResultInfo();
		ArrayList<OneWayFlightInfo> oneWayFlightInfos = new ArrayList<OneWayFlightInfo>();
		// 如果加Qproxy后测试出错时用到
		if (html.startsWith("QProxy")) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}

		if ("Exception".equals(html)) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
		if ("PARAMERROR".equals(html)) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
		// 无效日期
		if ("INVALID_DATE".equals(html)) {
			processResultInfo.setStatus(Constants.INVALID_DATE);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
		// 无结果，售完
		if ("SOLD_OUT".equals(html)) {
			processResultInfo.setStatus(Constants.NO_RESULT);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}

		try {
			processResultInfo.setKey(flightSearchParam.getQueryId());
			String contentStr = getValue(html, "table", "/table");
			String[] infos = getValues(contentStr,
					"tr class=\\\"FlightInformation", "/tr");
			// result.append(content);
			// 无结果，返回no_result
			if ("".equals(infos[0])) {
				Collector.count(CODEBASE, Key.NO_RESULT);
				processResultInfo.setStatus(Constants.NO_RESULT);
				processResultInfo.setData(oneWayFlightInfos);
				return processResultInfo;
			}
			// 搜索出来的航线中部分可选，部分已卖完，去掉已卖完的航线信息息
			String[] validInfos = findValidFlights(infos);
			ArrayList<Flight> flights = getFlights(validInfos, html,
					flightSearchParam.getDep(), flightSearchParam.getArr());
			getResult(oneWayFlightInfos,flights,flightSearchParam.getDepDate());
			processResultInfo.setData(oneWayFlightInfos);
			processResultInfo.setStatus(Constants.SUCCESS);
			processResultInfo.setRet(true);
			return processResultInfo; 

		} catch (Exception e) {
			processResultInfo.setStatus(Constants.PARSING_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
	}

	public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
		
		BookingResult bookingResult = new BookingResult();
        bookingResult.setRet(true);
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction("http://book.flypeach.com/default.aspx");
        bookingInfo.setMethod("post");
        String depDate = flightSearchParam.getDepDate();
        Map<String, String> inputs = Maps.newLinkedHashMap();
        inputs.put("ao", "B2CZHHK");
        inputs.put("ori", flightSearchParam.getDep());
        inputs.put("des", flightSearchParam.getArr());
        inputs.put("dep", depDate);
        inputs.put("adt", "1");
        inputs.put("chd", "0");
        inputs.put("inf", "0");
        inputs.put("langculture", "zh-HK");
        inputs.put("bLFF", "false");
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
	}

	public static String getValue(String source, String st, String end) {
		int a = source.indexOf(st);
		if (a == -1)
			return "";
		int b = source.indexOf(end, a + st.length());
		if (b == -1)
			return "";
		return source.substring(a + st.length(), b);
	}

	public static String getValue(String source, String regEx) {
		Matcher mm = Pattern.compile(regEx).matcher(source);
		return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
	}

	public static String[] getValues(String source, String st, String end) {
		String target = "";
		int a, b;
		while (true) {
			a = source.indexOf(st);
			if (a == -1)
				break;
			b = source.indexOf(end, a + st.length());
			if (b == -1)
				break;
			target += source.substring(a + st.length(), b) + "##@@##";
			source = source.substring(b);
		}
		return target.split("##@@##");
	}

	public static String[] getValues(String source, String regEx) {
		Vector<String> vec = new Vector<String>(5);
		Matcher mm = Pattern.compile(regEx).matcher(source);
		while (mm.find()) {
			vec.add(mm.group(mm.groupCount() > 0 ? 1 : 0));
		}
		return vec.toArray(new String[0]);
	}

}