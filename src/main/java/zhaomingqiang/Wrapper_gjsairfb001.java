package zhaomingqiang;

import java.util.ArrayList;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 保加利亚航空迁移	往返
 * create by chao.kang on 2014/7/2
 */
public class Wrapper_gjsairfb001 implements QunarCrawler {
	private static final String CODEBASE = "gjsairfb001";
	
	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();//搜索条件参数
		//设置参数
		searchParam.setDep("JFK");//出发地
		searchParam.setArr("SOF");//目的地
		searchParam.setDepDate("2014-09-07");//出发时间
		searchParam.setRetDate("2014-09-11");//返回时间
		searchParam.setWrapperid("gjdairfb001");
		searchParam.setTimeOut("60000");//
		searchParam.setToken("");//
		Wrapper_gjsairfb001 gjsairfb001=new Wrapper_gjsairfb001();
		String html = gjsairfb001.getHtml(searchParam);
	 
		ProcessResultInfo result = new ProcessResultInfo();
		result = gjsairfb001.process(html,searchParam);
		
		if(result.isRet() && result.getStatus().equals(Constants.SUCCESS))
		{
			List<RoundTripFlightInfo> flightList =  (List<RoundTripFlightInfo>) result.getData();
			for (RoundTripFlightInfo in : flightList){
				System.out.println("************" + in.getInfo().toString());
				System.out.println("************" + in.getRetinfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());
			}
		}
		else
		{
			System.out.println(result.getStatus());
		}
	}

	public String getHtml(FlightSearchParam param) {
		QFHttpClient httpClient = new QFHttpClient(param, false);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		
		String dep = param.getDep().toString();
		String arr = param.getArr().toString();
		
		String[] deptDate = param.getDepDate().split("-");
		String deptdate = deptDate[0] + deptDate[1] + deptDate[2] + "0000";
		String[] rettDate = param.getRetDate().split("-");
		String rettdate = rettDate[0] + rettDate[1] + rettDate[2] + "0000";
	
		String postUrl = "http://book.air.bg/plnext/bulgarian1/Override.action";
		QFPostMethod post = new QFPostMethod(postUrl);
		post.setRequestHeader("Referer", "http://www.air.bg/en");
		try {
			NameValuePair[] nameValuePairs = {
					new NameValuePair("B_DATE_1", deptdate),
					new NameValuePair("TRIP_TYPE", "R"),
					new NameValuePair("CABIN", "E"),
					new NameValuePair("ADTPAX", "1"),
					new NameValuePair("FIELD_YTH_NUMBER", "0"),
					new NameValuePair("CHDPAX", "0"),
					new NameValuePair("FIELD_STU_NUMBER", "0"),
					new NameValuePair("InfantPAX", "0"),
					new NameValuePair("B_ANY_TIME_1", "true"),
					new NameValuePair("B_DATE_2", rettdate),
					new NameValuePair("B_ANY_TIME_2", "true"),
					new NameValuePair("service_fee_h", "9.78"),
					new NameValuePair("So_GL",""),
					new NameValuePair("B_LOCATION_1", dep),
					new NameValuePair("Departure_date", ""),
					new NameValuePair("B_DAY", ""),
					new NameValuePair("B_MONTH", ""),
					new NameValuePair("tmp_B_DATA", ""),
					new NameValuePair("DATE_RANGE_VALUE_1", "0"),
					new NameValuePair("B_TIME", "0000"),
					new NameValuePair("E_LOCATION_1", arr),
					new NameValuePair("Return_date", ""),
					new NameValuePair("E_DAY", ""),
					new NameValuePair("E_MONTH", ""),
					new NameValuePair("tmp_E_DATA", ""),
					new NameValuePair("DATE_RANGE_VALUE_2", "0"),
					new NameValuePair("E_TIME", "0000"),
					new NameValuePair("TRAVELLER_TYPE_1", "ADT"),
					new NameValuePair("SO_SITE_USER_CURRENCY_CODE", "EUR"),
					new NameValuePair("EMBEDDED_TRANSACTION",
							"FlexPricerAvailability"),
					new NameValuePair("EXTERNAL_ID", "BOOKING"),
					new NameValuePair("LANGUAGE", "GB"),
					new NameValuePair("SEARCH_BY", "1"),
					new NameValuePair("SITE", "5ANS5ANS"),
					new NameValuePair("RESULT_LANGUAGE", "en"),
					new NameValuePair("SO_SITE_QUEUE_OFFICE_ID", "SOFFB08AB"),
					new NameValuePair("SO_QUEUE_NUMBER", "0"),
					new NameValuePair("SO_QUEUE_CATEGORY", "0"),
					new NameValuePair("SO_FAILURE_QUEUE_OFFICE_ID", "SOFFB08AB"),
					new NameValuePair("SO_FAILURE_QUEUE_NUMBER", "13"),
					new NameValuePair("SO_FAILURE_QUEUE_CATEGORY", "0"),
					new NameValuePair("TR_FLIGHTS", "TRUE"),
					new NameValuePair("SHUTTLE_FLIGHTS", "TRUE"),
					new NameValuePair("SCHEDULE", "TRUE"),
					new NameValuePair("SEVEN_DAY_SEARCH", "TRUE"),
					new NameValuePair("SO_SITE_SD_AIRL_FILTER_ACTIV", "TRUE"),
					new NameValuePair("SO_SITE_REST_AIRLINES_LST",
							"FB;VS;9W;US"),
					new NameValuePair("TRIP_FLOW", "YES"),
					new NameValuePair("BOOKING_FLOW", "REVENUE"),
					new NameValuePair("PRICING_TYPE", "O"),
					new NameValuePair("DISPLAY_TYPE", "1"),
					new NameValuePair("SO_SITE_FP_PRICING_TYPE", "O"),
					new NameValuePair("ARRANGE_BY", "D"),
					new NameValuePair("REFRESH", "0"),
					new NameValuePair("COMMERCIAL_FARE_FAMILY_1", "ECONOMY"),
					new NameValuePair("COMMERCIAL_FARE_FAMILY_2", "BUSINESS"),
					new NameValuePair("DATE_RANGE_QUALIFIER_1", "C"),
					new NameValuePair("DATE_RANGE_QUALIFIER_2", "C"),
					new NameValuePair("SO_SITE_ALLOW_SERVICE_FEE", "1"),
					new NameValuePair("ALLOW_PRIMARY_YTH", "Y"),
					new NameValuePair("ALLOW_PRIMARY_STU", "Y")
				
					
			};
			post.setRequestBody(nameValuePairs);
			httpClient.executeMethod(post);
			String body = post.getResponseBodyAsString();
			return body;
		} catch (Exception e) {
		
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
		return "Exception";
	}

	public ProcessResultInfo process(String html, FlightSearchParam param) {
		ProcessResultInfo result = new ProcessResultInfo();
		List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
try{
		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;	
		}
		
		if (html.contains("Please provide us with an arrival city and try again")) {
			result.setRet(true);
			result.setStatus(Constants.INVALID_AIRLINE);
			return result;	
		}

		if (html.contains("We are unable to find recommendations for your search. Please change your search criteria and resubmit the search.")) {
			result.setRet(true);
			result.setStatus(Constants.NO_RESULT);
			return result;	
		}

		String jsonStr = StringUtils.substringBetween(html, "var generatedJSon = new String('", "');");
		
		if (!html.contains("var generatedJSon = new String('")) {
			result.setRet(true);
			result.setStatus(Constants.PARSING_FAIL);
			return result;	
		}
	    JSONObject json =  JSONObject.parseObject(jsonStr);
			JSONObject outboundMap =json.getJSONObject("list_tab");
			//获取往返序号与价格列表
			JSONArray lowPriceList=getLowPriceList(outboundMap);
			// 获得航班信息
			JSONArray list_proposed_boundList = outboundMap.getJSONArray("list_proposed_bound");
			JSONObject out_flight_list = getFlightList(list_proposed_boundList,0);
			JSONObject in_flight_list = getFlightList(list_proposed_boundList,1);
			
			for(int i=0;i<lowPriceList.size();i++){
				RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
				FlightDetail flightDetail = new FlightDetail();
				String outflightId = lowPriceList.getJSONObject(i).getString("outflightid");
				String inflightId = lowPriceList.getJSONObject(i).getString("inflightid");
				List<FlightSegement> segsout = (List<FlightSegement>) out_flight_list.get(outflightId);
				List<FlightSegement> segsin = (List<FlightSegement>) in_flight_list.get(inflightId);
				
				flightDetail.setDepcity(param.getDep());
				flightDetail.setArrcity(param.getArr());
				flightDetail.setMonetaryunit(lowPriceList.getJSONObject(i).getString("curr"));
				flightDetail.setDepdate(Date.valueOf(param.getDepDate()));
				flightDetail.setPrice(lowPriceList.getJSONObject(i).getDoubleValue("price"));
			    flightDetail.setTax(lowPriceList.getJSONObject(i).getDoubleValue("tax"));
			    flightDetail.setWrapperid(CODEBASE);
			    List<String> flightnolist = Lists.newArrayList();
			    for(int j=0;j<segsout.size();j++){
			    	flightnolist.add(segsout.get(j).getFlightno());
			    }
			    flightDetail.setFlightno(flightnolist);
			    List<String> flightnolistret = Lists.newArrayList();
			    for(int j=0;j<segsin.size();j++){
			    	flightnolistret.add(segsin.get(j).getFlightno());
			    }
			    baseFlight.setRetflightno(flightnolistret);
			    baseFlight.setDetail(flightDetail);
			    baseFlight.setRetdepdate(Date.valueOf(param.getRetDate()));
			    
			    baseFlight.setInfo(segsout);
			    baseFlight.setRetinfo(segsin);
			    baseFlight.setOutboundPrice(lowPriceList.getJSONObject(i).getDoubleValue("depprice"));
			    baseFlight.setReturnedPrice(lowPriceList.getJSONObject(i).getDoubleValue("retprice"));
			    flightList.add(baseFlight);
			}

		result.setData(flightList);
		result.setStatus(Constants.SUCCESS);
		return result;	
}
catch (Exception e)
{
	result.setStatus(Constants.PARSING_FAIL);
    result.setData(flightList);
    return result;
}
	
	}
	
	public JSONObject getFlightList(JSONArray list_proposed_boundList, int index) {
		// TODO Auto-generated method stub
		JSONObject list = new JSONObject();
		JSONArray list_Flight = list_proposed_boundList.getJSONObject(index).getJSONArray("list_flight");
		for(int j=0; j<list_Flight.size();j++ ){
			List<FlightSegement> segs = new ArrayList<FlightSegement>();
			JSONObject list_segmentMap = list_Flight.getJSONObject(j);
			// 得到具体某航班信息
			JSONArray list_segment = list_segmentMap
					.getJSONArray("list_segment");
			// 获得航班序号
			String flight_id =  list_segmentMap.getString("flight_id");
			for (int k = 0; k < list_segment.size(); k++) {
				JSONObject flightInfoMap = list_segment.getJSONObject(k);
				FlightSegement seg = new FlightSegement();
				seg.setDepairport(flightInfoMap.getJSONObject("b_location")
						.getString("location_code"));
				seg.setArrairport(flightInfoMap.getJSONObject("e_location")
						.getString("location_code"));
				seg.setFlightno(flightInfoMap.getJSONObject("airline")
						.getString("code")
						+ flightInfoMap.getString("flight_number"));
				seg.setAircraft(flightInfoMap.getJSONObject("equipment")
						.getString("code"));
				seg.setDeptime(flightInfoMap
						.getString("b_date_formatted_time"));
				seg.setArrtime(flightInfoMap
						.getString("e_date_formatted_time"));
				seg.setDepDate(flightInfoMap.getString("b_date_date"));
				seg.setArrDate(flightInfoMap.getString("e_date_date"));
				segs.add(seg);
			}
			list.put(flight_id, segs);
		}
		return list;
	}

	public BookingResult getBookingInfo(FlightSearchParam param) {
		String bookingUrlPre = "http://book.air.bg/plnext/bulgarian1/Override.action";
		BookingResult bookingResult = new BookingResult();
		
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		
		
		String[] deptDate = param.getDepDate().split("-");
		String deptdate = deptDate[0] + deptDate[1] + deptDate[2] + "0000";
		String[] rettDate = param.getRetDate().split("-");
		String rettdate = rettDate[0] + rettDate[1] + rettDate[2] + "0000";
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("B_LOCATION_1", param.getDep());
		map.put("E_LOCATION_1", param.getArr());
		map.put("B_DATE_1", deptdate);
		map.put("B_ANY_TIME_1", "true");
		map.put("B_DATE_2", rettdate);
		map.put("B_ANY_TIME_2", "true");
		map.put("service_fee_h", "9.78");
		map.put("SO_GL", "");	
		
		map.put("Departure_date", "");
		map.put("B_DAY", "");
		map.put("B_MONTH", "");
		map.put("tmp_B_DATA", "");
		map.put("DATE_RANGE_VALUE_1", "0");
		map.put("B_TIME", "0000");
		
		map.put("Return_date", "");
		map.put("E_DAY", "");
		map.put("E_MONTH", "");
		map.put("tmp_E_DATA", "");
		map.put("DATE_RANGE_VALUE_2", "0");
		map.put("E_TIME", "0000");
		map.put("TRIP_TYPE", "R");
		map.put("CABIN", "E");
		map.put("ADTPAX", "1");
		map.put("FIELD_YTH_NUMBER", "0");
		map.put("CHDPAX", "0");
		map.put("FIELD_STU_NUMBER", "0");
		map.put("InfantPAX", "0");
		map.put("TRAVELLER_TYPE_1", "ADT");
		map.put("SO_SITE_USER_CURRENCY_CODE", "EUR");
		map.put("EMBEDDED_TRANSACTION",
				"FlexPricerAvailability");
		map.put("EXTERNAL_ID", "BOOKING");
		map.put("LANGUAGE", "GB");
		map.put("SEARCH_BY", "1");
		map.put("SITE", "5ANS5ANS");
		map.put("RESULT_LANGUAGE", "en");
		map.put("SO_SITE_QUEUE_OFFICE_ID", "SOFFB08AB");
		map.put("SO_QUEUE_NUMBER", "0");
		map.put("SO_QUEUE_CATEGORY", "0");
		map.put("SO_FAILURE_QUEUE_OFFICE_ID", "SOFFB08AB");
		map.put("SO_FAILURE_QUEUE_NUMBER", "13");
		map.put("SO_FAILURE_QUEUE_CATEGORY", "0");
		map.put("TR_FLIGHTS", "TRUE");
		map.put("SHUTTLE_FLIGHTS", "TRUE");
		map.put("SCHEDULE", "TRUE");
		map.put("SEVEN_DAY_SEARCH", "TRUE");
		map.put("SO_SITE_SD_AIRL_FILTER_ACTIV", "TRUE");
		map.put("SO_SITE_REST_AIRLINES_LST",
				"FB%3BVS%3B9W%3BUS");
		map.put("TRIP_FLOW", "YES");
		map.put("BOOKING_FLOW", "REVENUE");
		map.put("PRICING_TYPE", "O");
		map.put("DISPLAY_TYPE", "1");
		map.put("SO_SITE_FP_PRICING_TYPE", "O");
		map.put("ARRANGE_BY", "D");
		map.put("REFRESH", "0");
		map.put("COMMERCIAL_FARE_FAMILY_1", "ECONOMY");
		map.put("COMMERCIAL_FARE_FAMILY_2", "BUSINESS");
		map.put("DATE_RANGE_QUALIFIER_1", "C");
		map.put("DATE_RANGE_QUALIFIER_2", "C");
		map.put("SO_SITE_ALLOW_SERVICE_FEE", "1");
		map.put("ALLOW_PRIMARY_YTH", "Y");
		map.put("ALLOW_PRIMARY_STU", "Y");
		
		
		bookingInfo.setInputs(map);		
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}


	
	
	// 得到往返航班对应的lowPrice
	public JSONArray getLowPriceList(JSONObject outboundMap){
		
		JSONArray priceList=new JSONArray();
		JSONArray list_recommendationList =outboundMap.getJSONArray("list_recommendation");
		for (int h = 0; h < list_recommendationList.size(); h++) {
			
			double price = Double
					.valueOf((String)( list_recommendationList.getJSONObject(h).getJSONArray("list_trip_price"))
							.getJSONObject(0).getString("amount_without_tax"));
			double depprice = Double
					.valueOf((String)( list_recommendationList.getJSONObject(h).getJSONArray("list_trip_price"))
							.getJSONObject(0).getJSONArray("list_bound_price").getJSONObject(0).getString("amount_without_tax"));
			
			double retprice = Double
					.valueOf((String)( list_recommendationList.getJSONObject(h).getJSONArray("list_trip_price"))
							.getJSONObject(0).getJSONArray("list_bound_price").getJSONObject(1).getString("amount_without_tax"));
			
			double tax = Double
					.valueOf((String)( list_recommendationList.getJSONObject(h).getJSONArray("list_trip_price"))
							.getJSONObject(0).getString("tax"));
		
			String curr =  list_recommendationList.getJSONObject(h).getJSONArray("list_trip_price")
					.getJSONObject(0).getJSONObject("currency").getString("code");
			
			JSONArray outList_flight = list_recommendationList.getJSONObject(h).getJSONArray("list_bound").
					getJSONObject(0).getJSONArray("list_flight");
			
			JSONArray inList_flight = list_recommendationList.getJSONObject(h).getJSONArray("list_bound").
					getJSONObject(1).getJSONArray("list_flight");

			//得到航班号组合
			for(int j=0;j<outList_flight.size();j++){
								
				String outflightId = outList_flight.getJSONObject(j).getString("flight_id");
				
				for(int k=0;k<inList_flight.size();k++){			
					
					String inflightId = inList_flight.getJSONObject(k).getString("flight_id");
					JSONObject arrayList = new JSONObject();
					arrayList.put("outflightid", outflightId);
					arrayList.put("inflightid", inflightId);
					arrayList.put("curr", curr);
					arrayList.put("price",price);
					arrayList.put("tax", tax);
					arrayList.put("depprice",depprice);
					arrayList.put("retprice",retprice);
				    priceList.add(arrayList);
				}
			}
		}
		return priceList;
	}
	
	
}

	

