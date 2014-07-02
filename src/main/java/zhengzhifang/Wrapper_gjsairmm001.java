package zhengzhifang;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * Insert the type's description here.
 * Creation date: (2014-7-11)
 * @author: zzf
 */
public class Wrapper_gjsairmm001 implements QunarCrawler {

	private static final String CODEBASE = "gjsairmm001";
	private QFHttpClient httpClient = null;
	
	public static void main(String[] args) {
		Wrapper_gjsairmm001 p = new Wrapper_gjsairmm001();
		FlightSearchParam flightSearchParam = new FlightSearchParam();
		flightSearchParam.setDep("HKG");
		flightSearchParam.setArr("KIX");
		flightSearchParam.setDepDate("2014-07-11");
		flightSearchParam.setRetDate("2014-07-19");
		flightSearchParam.setWrapperid("gjdairmm001");
		flightSearchParam.setQueryId("http://???");
		String html = p.getHtml(flightSearchParam);
		System.out.println("开始打印：" + html);
		System.out.println("结束打印");
		System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
		/*BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(bookingResult));*/
	}	
	
	
	
	private String[] findValidFlights(String[] str) {
		String[] flights = new String[str.length];
		int count = 0;
		for(String flight:str){
			//有航班,但航班信息部分无价格,不抓取
			if(flight.contains("Full")&&flight.indexOf("boarding_class_rcd")== -1){
				continue;
			}
			else{
				flights[count] = flight;
				count++;
			}
		}
		String[] validFlights = new String[count];
		for(int i=0;i<count;i++){
			validFlights[i] = flights[i];
		}
		
		return validFlights;
	}
	
	private ArrayList<ArrayList> getFlights(String[] outInfos , String[] rtInfos , String html,String dep,String arr) throws ParseException
	{
		ArrayList<ArrayList> result = new ArrayList<ArrayList>();
		ArrayList<Flight> outboundFlights = new ArrayList<Flight >();
		ArrayList<Flight > returnedFlights = new ArrayList<Flight >();
		
		String content = "";
		for(int i=0;i<outInfos.length;i++){	
			Flight flight = new Flight();
			content = getValue(outInfos[i], "boarding_class_rcd:", "onclick");
			//获取去程出发时间、到达时间
			flight.depTime = formatDepTime(content);
			flight.arrTime = formatArrTime(content);
			flight.arrDate = formatArrDate(content);
			//获取去程出发机场、到达机场
			flight.dep = dep;
			flight.arr = arr;
			//获取去程航班号
			flight.code = formatCode(content);
			//获取去程机型
			flight.planeType = formatPlaneType(outInfos[i]);
			//获取货币号
			flight.currencyCode = formatCurrencyCode(content);
			//获取去程价格
			flight.retailPrice = formatPrice(content);
			//抓取去程税费
			flight.tax = formatOutTax(html);
			outboundFlights.add(flight);
		}
		
		for(int j=0;j<rtInfos.length;j++){	
			Flight flight = new Flight();
			content = getValue(rtInfos[j], "boarding_class_rcd:", "onclick");
			//获取返程出发时间、到达时间
			flight.depTime = formatDepTime(content);
			flight.arrTime = formatArrTime(content);
			flight.arrDate = formatArrDate(content);
			//获取返程出发机场、到达机场
			flight.dep = arr;
			flight.arr = dep;
			//获取返程航班号
			flight.code = formatCode(content);
			//获取返程机型
			flight.planeType = formatPlaneType(rtInfos[j]);
			//获取货币号
			flight.currencyCode = formatCurrencyCode(content);
			//获取返程价格
			flight.retailPrice = formatPrice(content);
			//抓取返程程税费
			flight.tax = formatReturnTax(html);
			returnedFlights.add(flight);
		}
		
		result.add(outboundFlights);
		result.add(returnedFlights);
		return result;
	}
	
	private void getResult(@SuppressWarnings("rawtypes") ArrayList<ArrayList> resultList,ArrayList<RoundTripFlightInfo> roundTripFlightInfos,FlightSearchParam flightSearchParam) throws ParseException
	{
		@SuppressWarnings("unchecked")
		ArrayList<Flight> outboundFlights = resultList.get(0);
		@SuppressWarnings("unchecked")
		ArrayList<Flight> returnedFlights = resultList.get(1);
		StringBuilder result = new StringBuilder();
		result.append("<table>\n");
		for(int i=0 ; i < outboundFlights.size(); i++)
		{		
			for(int j=0 ; j < returnedFlights.size() ; j++)
			{
				RoundTripFlightInfo roundTripFlightInfo = new RoundTripFlightInfo();
				List<FlightSegement> segementList = Lists.newArrayList();
				List<String> flightNoList = Lists.newArrayList();
				FlightDetail flightDetail = new FlightDetail();
				List<FlightSegement> reSegementList = Lists.newArrayList();
				List<String> reFlightNoList = Lists.newArrayList();
				FlightSegement flightSegement = new FlightSegement();
				FlightSegement RetflightSegement = new FlightSegement();
				flightSegement.setDepairport(flightSearchParam.getDep());
				flightSegement.setArrairport(flightSearchParam.getArr());
				flightSegement.setFlightno(outboundFlights.get(i).code);
				flightSegement.setDeptime(outboundFlights.get(i).depTime);
				flightSegement.setArrtime(outboundFlights.get(i).arrTime);
				flightSegement.setDepDate(flightSearchParam.getDepDate());
				flightSegement.setArrDate(outboundFlights.get(i).arrDate);//到达日期
				flightSegement.setCompany("MM");
				flightSegement.setAircraft(outboundFlights.get(i).planeType);
				segementList.add(flightSegement);
				
				RetflightSegement.setDepairport(flightSearchParam.getArr());
				RetflightSegement.setArrairport(flightSearchParam.getDep());
				RetflightSegement.setFlightno(returnedFlights.get(j).code);
				RetflightSegement.setDeptime(returnedFlights.get(j).depTime);
				RetflightSegement.setArrtime(returnedFlights.get(j).arrTime);
				RetflightSegement.setDepDate(flightSearchParam.getRetDate());
				RetflightSegement.setArrDate(returnedFlights.get(j).arrDate);//返程到达日期
				RetflightSegement.setCompany("MM");
				RetflightSegement.setAircraft(returnedFlights.get(j).planeType);
				reSegementList.add(RetflightSegement);
				
				flightDetail.setDepcity(flightSearchParam.getDep());
				flightDetail.setArrcity(flightSearchParam.getArr());
				flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(flightSearchParam.getDepDate()));
				flightNoList.add(outboundFlights.get(i).code);
				flightDetail.setFlightno(flightNoList);
				flightDetail.setMonetaryunit(outboundFlights.get(i).currencyCode);
				flightDetail.setTax(outboundFlights.get(i).tax + returnedFlights.get(j).tax);
				flightDetail.setPrice(outboundFlights.get(i).retailPrice + returnedFlights.get(j).retailPrice);
				flightDetail.setWrapperid("gjsairmm001");
				flightDetail.setCreatetime(new Timestamp(System.currentTimeMillis()));
				flightDetail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
				
				roundTripFlightInfo.setInfo(segementList);
				roundTripFlightInfo.setDetail(flightDetail);
				roundTripFlightInfo.setRetdepdate(new SimpleDateFormat("yyyy-MM-dd").parse(flightSearchParam.getRetDate()));
				reFlightNoList.add(returnedFlights.get(j).code);
				roundTripFlightInfo.setRetflightno(reFlightNoList);
				roundTripFlightInfo.setRetinfo(reSegementList);
				roundTripFlightInfo.setOutboundPrice(outboundFlights.get(i).retailPrice);
				roundTripFlightInfo.setReturnedPrice(returnedFlights.get(j).retailPrice);
				roundTripFlightInfos.add(roundTripFlightInfo);
			}
		}
	}
	
	private String formatCode(String str) {
		//code1获取航班号中的机场码，code2获取航班号中的数字
		String code1 = getValue(str, "airline_rcd:", "|flight_number");			
		String code2 = getValue(str, "flight_number:", "|origin_rcd");
		String code = code1 + code2;
		return code;
	}
	
	private String formatDepTime(String str) {
		//获取包含出发时间的字符串
		String depTime = getValue(str, "planned_departure_time:", "|planned_arrival_time").trim();
		//将时间的5 这种格式转化为 00:05这种格式
		if(depTime.length()==1){
			depTime = "00:0" + depTime;
		}
		//将时间的35这种格式转化为 00:35这种格式
		else if(depTime.length()==2){
			depTime = "00:" + depTime;
		}
		//将时间的730 这种格式转化为 07:30这种格式
		else if(depTime.length()==3){
			depTime = "0" + depTime.substring(0, 1) + ":" + depTime.substring(1);
		}
		else{
			depTime = depTime.substring(0, 2) + ":" + depTime.substring(2);
		}		
		return depTime;
	}
	
	private String formatArrTime(String str) {
		String arrTime = getValue(str, "planned_arrival_time:", "|transit_airline_rcd").trim();
		//将时间的5 这种格式转化为 00:05这种格式
		if(arrTime.length()==1){
			arrTime = "00:0" + arrTime;
		}
		//将时间的35这种格式转化为 00:35这种格式
		else if(arrTime.length()==2){
			arrTime = "00:" + arrTime;
		}		
		//将时间的730 这种格式转化为 07:30这种格式
		else if(arrTime.length()==3){
			arrTime = "0" + arrTime.substring(0, 1) + ":" + arrTime.substring(1);
		}
		else{
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
	
	private float formatOutTax(String str) {
		int beginIndex = str.indexOf("OUTWARDTAX");
		int endIndex = str.indexOf("RETURNTAX");
		String taxStr = str.substring(beginIndex, endIndex);
		String taxStr1 = getValue(taxStr, "TaxInfo", "class=\\\"clear-all\\\"");
		String taxStr2 = getValue(taxStr1, "class=\\\"charges_price_total\\\"\\u003e\\", "\\u003c/div");
		taxStr2 = taxStr2.replaceAll(",", "");
		String taxStr3 = getValue(taxStr2, "[0-9]+");		
		float tax = 0;
		if(!taxStr3.equals("")){//TaxInfo   //class=\"clear-all\"  // class=\"charges_price_total\"\u003e\    \\   \u003c/div
			tax = Float.parseFloat(taxStr3);
		}	
		return tax;
	}
	
	private float formatReturnTax(String str) {
		int beginIndex = str.indexOf("RETURNTAX");
		String taxStr = str.substring(beginIndex);
		String taxStr1 = getValue(taxStr, "TaxInfo", "class=\\\"clear-all\\\"");
		String taxStr2 = getValue(taxStr1, "class=\\\"charges_price_total\\\"\\u003e\\", "\\u003c/div");
		taxStr2 = taxStr2.replaceAll(",", "");
		String taxStr3 = getValue(taxStr2, "[0-9]+");		
		float tax = 0;
		if(!taxStr3.equals("")){//TaxInfo   //class=\"clear-all\"  // class=\"charges_price_total\"\u003e\    \\   \u003c/div
			tax = Float.parseFloat(taxStr3);
		}	
		return tax;
	}
	
	private String formatCurrencyCode(String str) {
		String currencyCode = getValue(str, "currency_rcd:", "|transit_arrival_date").trim();		
		
		return currencyCode;
	}
	
	private String formatPlaneType(String str) {
		String planeType = getValue(str, "Aircraft", "/li").trim();
		planeType = getValue(planeType, "[A-Z|0-9]+");		
		
		return planeType;
	}
	
	class Flight
	{
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
		String depDate = temps[0] + temps[1] + temps[2];
		String[] temps1 = flightSearchParam.getRetDate().split("-");
		String arrDate = temps1[0] + temps1[1] + temps1[2];

		httpClient = new QFHttpClient(flightSearchParam, true);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);

		String refer = "http://book.flypeach.com/default.aspx?ao=B2CZHHK&ori="
				+ flightSearchParam.getDep() + "&des=" + flightSearchParam.getArr() + "&dep=" + flightSearchParam.getDepDate() + "&ret="
				+ flightSearchParam.getRetDate() + "&adt=1&chd=0&inf=0&langculture=zh-HK&bLFF=false";
		// post 200 获取结果页面
		QFPostMethod post = new QFPostMethod(
				"http://book.flypeach.com/WebService/B2cService.asmx/GetAvailability");
		String body1 = "{\"origin\":\""
				+ flightSearchParam.getDep()
				+ "\",\"destination\":\""
				+ flightSearchParam.getArr()
				+ "\",\"dateFrom\":\""
				+ depDate
				+ "\",\"dateTo\":\""
				+ arrDate
				+ "\",\"iOneWay\":\"false\",\"iFlightOnly\":\"0\",\"iAdult\":1,\"iChild\":0,\"iInfant\":0,\"BoardingClass\":\"\",\"CurrencyCode\":\"\",\"strPromoCode\":\"\",\"SearchType\":\"FARE\",\"iOther\":0,\"otherType\":\"\",\"strIpAddress\":\"\"}";
		QFPostMethod post2 = null, post3 = null;
		try {
			post.setRequestEntity(new ByteArrayRequestEntity(body1.getBytes()));
			post.setRequestHeader("Content-Type",
					"application/json; charset=utf-8");
			post.setRequestHeader("Referer", refer);
			httpClient.executeMethod(post);
			String firstRes = post.getResponseBodyAsString();
			// 分别获取去程和返程的航班信息
			String outwardString = getValues(firstRes, "table",
					"/table")[0];
			String returnString = getValues(firstRes, "table",
					"/table")[1];

			if (org.apache.commons.lang.StringUtils.isEmpty(outwardString)
					|| org.apache.commons.lang.StringUtils
							.isEmpty(returnString)) {
				return "PARAMERROR";
			}
			// 无效日期
			if (outwardString.indexOf("tr class=\\\"FlightInformation") == -1
					|| returnString.indexOf("tr class=\\\"FlightInformation") == -1) {
				return "INVALID_DATE";
			}
			// 无结果，售完
			if ((outwardString.contains("Full") && outwardString
					.indexOf("boarding_class_rcd") == -1)
					|| (returnString.contains("Full") && returnString
							.indexOf("boarding_class_rcd") == -1)) {
				return "SOLD_OUT";
			}
			// 获取去程要抓取税费页面所要传的值
			String flight_id = getValue(outwardString,
					"flight_id:", "|fare_id:").trim();
			String airline_rcd = getValue(outwardString,
					"airline_rcd:", "|flight_number:").trim();
			String flight_number = getValue(outwardString,
					"flight_number:", "|origin_rcd").trim();
			String fare_id = getValue(outwardString, "fare_id:",
					"|boarding_class_rcd").trim();
			String booking_class_rcd = getValue(outwardString,
					"booking_class_rcd:", "|airline_rcd").trim();
			String planned_departure_time = getValue(outwardString,
					"planned_departure_time:", "|planned_arrival_time").trim();
			String planned_arrival_time = getValue(outwardString,
					"planned_arrival_time:", "|transit_airline_rcd").trim();
			String currency_rcd = getValue(outwardString,
					"currency_rcd:", "|transit_arrival_date").trim();
			String cookie = "";
			String outTaxStr = "";
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
						+ depDate
						+ "</departure_date><arrival_date>"
						+ depDate
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
				cookie = org.apache.commons.lang.StringUtils.join(httpClient
						.getState().getCookies(), "; ");
				httpClient.getState().clearCookies();
				post2.setRequestHeader("Cookie", cookie);
				httpClient.executeMethod(post2);
				outTaxStr = post2.getResponseBodyAsString();

			}
			// 获取返程要抓取税费页面所要传的值
			String flight_id1 = getValue(returnString,
					"flight_id:", "|fare_id:").trim();
			String airline_rcd1 = getValue(returnString,
					"airline_rcd:", "|flight_number:").trim();
			String flight_number1 = getValue(returnString,
					"flight_number:", "|origin_rcd").trim();
			String fare_id1 = getValue(returnString, "fare_id:",
					"|boarding_class_rcd").trim();
			String booking_class_rcd1 = getValue(returnString,
					"booking_class_rcd:", "|airline_rcd").trim();
			String planned_departure_time1 = getValue(returnString,
					"planned_departure_time:", "|planned_arrival_time").trim();
			String planned_arrival_time1 = getValue(returnString,
					"planned_arrival_time:", "|transit_airline_rcd").trim();
			String currency_rcd1 = getValue(returnString,
					"currency_rcd:", "|transit_arrival_date").trim();

			if (flight_id1 != null && fare_id1 != null) {
				post3 = new QFPostMethod(
						"http://book.flypeach.com/WebService/B2cService.asmx/GetQuoteSummary");
				String body3 = "{\"strFlightXml\":\"<flights><flight><flight_id>"
						+ flight_id1
						+ "</flight_id><airline_rcd>"
						+ airline_rcd1
						+ "</airline_rcd><flight_number>"
						+ flight_number1
						+ "</flight_number><origin_rcd>"
						+ flightSearchParam.getArr()
						+ "</origin_rcd><destination_rcd>"
						+ flightSearchParam.getDep()
						+ "</destination_rcd><fare_id>"
						+ fare_id1
						+ "</fare_id><transit_airline_rcd></transit_airline_rcd><transit_flight_number></transit_flight_number><transit_flight_id></transit_flight_id><departure_date>"
						+ arrDate
						+ "</departure_date><arrival_date>"
						+ arrDate
						+ "</arrival_date><planned_departure_time>"
						+ planned_departure_time1
						+ "</planned_departure_time><planned_arrival_time>"
						+ planned_arrival_time1
						+ "</planned_arrival_time><transit_departure_date></transit_departure_date><transit_departure_day></transit_departure_day><transit_arrival_date></transit_arrival_date><transit_arrival_day></transit_arrival_day><transit_planned_departure_time></transit_planned_departure_time><transit_planned_arrival_time></transit_planned_arrival_time><transit_airport_rcd></transit_airport_rcd><transit_fare_id></transit_fare_id><booking_class_rcd>"
						+ booking_class_rcd1
						+ "</booking_class_rcd><currency_rcd>"
						+ currency_rcd1
						+ "</currency_rcd></flight></flights>\",\"strFlightType\":\"Return\"}";

				post3.setRequestEntity(new ByteArrayRequestEntity(body3
						.getBytes()));
				post3.setRequestHeader("Content-Type",
						"application/json; charset=utf-8");
				post3.setRequestHeader("Accept-Encoding", "gzip, deflate");
				post3.setRequestHeader("Referer", refer);

				post3.setRequestHeader("Cookie", cookie);
				httpClient.executeMethod(post3);

				return firstRes + "\nOUTWARDTAX\n" + outTaxStr
						+ "\nRETURNTAX\n" + post3.getResponseBodyAsString();
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
			if (post3 != null) {
				post3.releaseConnection();
			}
		}
		return "Exception";
	}
	public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
		ProcessResultInfo processResultInfo = new ProcessResultInfo();
		ArrayList<RoundTripFlightInfo> roundTripFlightInfos = new ArrayList<RoundTripFlightInfo>();
		StringBuilder result = new StringBuilder();
		if (html.startsWith("QProxy")) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(roundTripFlightInfos);
			return processResultInfo;
		}

		if ("Exception".equals(html)) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(roundTripFlightInfos);
			return processResultInfo;
		}
		if ("PARAMERROR".equals(html)) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(roundTripFlightInfos);
			return processResultInfo;
		}
		// 无效日期
		if ("INVALID_DATE".equals(html)) {
			processResultInfo.setStatus(Constants.INVALID_DATE);
			processResultInfo.setData(roundTripFlightInfos);
			return processResultInfo;
		}
		// 无结果，售完
		if ("SOLD_OUT".equals(html)) {
			processResultInfo.setStatus(Constants.NO_RESULT);
			processResultInfo.setData(roundTripFlightInfos);
			return processResultInfo;
		}

		try {
			String outStr = getValues(html, "table", "/table")[0];
			String rtStr = getValues(html, "table", "/table")[1];
			String[] outInfos = getValues(outStr, "tr class=\\\"FlightInformation","/tr");
			String[] rtInfos = getValues(rtStr, "tr class=\\\"FlightInformation","/tr");
			//去程无结果，返回no_result
			if ("".equals(outInfos[0])) {
				Collector.count(CODEBASE, Key.NO_RESULT);
				processResultInfo.setStatus(Constants.NO_RESULT);
				processResultInfo.setData(roundTripFlightInfos);
				return processResultInfo;
			}
			//返程无结果，返回no_result
			if ("".equals(rtInfos[0])) {
				Collector.count(CODEBASE, Key.NO_RESULT);
				processResultInfo.setStatus(Constants.NO_RESULT);
				processResultInfo.setData(roundTripFlightInfos);
				return processResultInfo;
			}	
			//搜索出来的航线中部分可选，部分已卖完，去掉已卖完的航线信息息		
			String[] outValidInfos = findValidFlights(outInfos);
			String[] rtValidInfos = findValidFlights(rtInfos);
			//获取去程、返程各自的航班信息
			ArrayList<ArrayList> resultList = getFlights(outValidInfos , rtValidInfos ,html, flightSearchParam.getDep(),flightSearchParam.getArr());
			getResult(resultList,roundTripFlightInfos,flightSearchParam);
			processResultInfo.setData(roundTripFlightInfos);
	        processResultInfo.setStatus(Constants.SUCCESS);
	        processResultInfo.setRet(true);
	        processResultInfo.setKey(flightSearchParam.getQueryId());
	        return processResultInfo;
		} catch (Exception e) {
			processResultInfo.setStatus(Constants.PARSING_FAIL);
			processResultInfo.setData(roundTripFlightInfos);
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
        inputs.put("ret", flightSearchParam.getRetDate());
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
