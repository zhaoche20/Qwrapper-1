package kangchao;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
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

/**
 * 保加利亚航空迁移 单程 create by chao.kang on 2014/7/1
 */
public class Wrapper_gjdairfb001 implements QunarCrawler {
	private static final String CODEBASE = "gjdairfb001";

	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();// 搜索条件参数 //设置参数
		searchParam.setDep("BOS");// 出发地
		searchParam.setArr("SOF");// 目的地
		searchParam.setDepDate("2014-09-06");// 出发时间

		Wrapper_gjdairfb001 gjdairfb001 = new Wrapper_gjdairfb001();
		String html = gjdairfb001.getHtml(searchParam);
		ProcessResultInfo result = new ProcessResultInfo();
		result = gjdairfb001.process(html, searchParam);
		System.out.println("--------------------------------------------");
		System.out.println(JSON.toJSONString(result));
		System.out.println("--------------------------------------------");
		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
					.getData();
			for (OneWayFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());
			}
		} else {
			System.out.println(result.getStatus());
		}
	}

	public String getHtml(FlightSearchParam param) {
		QFHttpClient httpClient = new QFHttpClient(param, false);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);

		String dep = param.getDep();
		String arr = param.getArr();

		String[] deptDate = param.getDepDate().split("-");
		String deptdate = deptDate[0] + deptDate[1] + deptDate[2] + "0000";
		System.out.println(deptdate + "." + dep + "" + arr);
		String postUrl = "http://book.air.bg/plnext/bulgarian1/Override.action";
		QFPostMethod post = new QFPostMethod(postUrl);
		post.setRequestHeader("Referer", "http://www.air.bg/en");
		try {
			NameValuePair[] nameValuePairs = {
					new NameValuePair("B_DATE_1", deptdate),
					new NameValuePair("TRIP_TYPE", "O"),
					new NameValuePair("CABIN", "E"),
					new NameValuePair("ADTPAX", "1"),
					new NameValuePair("FIELD_YTH_NUMBER", "0"),
					new NameValuePair("CHDPAX", "0"),
					new NameValuePair("FIELD_STU_NUMBER", "0"),
					new NameValuePair("InfantPAX", "0"),
					new NameValuePair("B_ANY_TIME_1", "true"),
					new NameValuePair("B_DATE_2", deptdate),
					new NameValuePair("B_ANY_TIME_2", "true"),
					new NameValuePair("service_fee_h", "9.78"),
					new NameValuePair("So_GL", ""),
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
					new NameValuePair("DATE_RANGE_VALUE_2", ""),
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
			return "Exception";
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}

	}

	public ProcessResultInfo process(String html, FlightSearchParam param) {
		ProcessResultInfo result = new ProcessResultInfo();
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		try {
			if ("Exception".equals(html)) {
				result.setStatus(Constants.CONNECTION_FAIL);
				return result;
			}

			if (html.contains("Please provide us with an arrival city and try again")) {
				result.setStatus(Constants.INVALID_AIRLINE);
				return result;
			}

			if (html.contains("We are unable to find recommendations for your search. Please change your search criteria and resubmit the search.")) {
				result.setStatus(Constants.NO_RESULT);
				return result;
			}

			String jsonStr = StringUtils.substringBetween(html,
					"var generatedJSon = new String('", "');");

			if (!html.contains("var generatedJSon = new String('")) {
				result.setStatus(Constants.PARSING_FAIL);
				return result;
			}

			JSONObject json = JSONObject.parseObject(jsonStr);
			JSONObject outboundMap = json.getJSONObject("list_tab");
			JSONObject lowPriceMap = getLowPriceMap(outboundMap);
			JSONArray list_proposed_boundList = outboundMap
					.getJSONArray("list_proposed_bound");
			JSONObject flightMap = (JSONObject) list_proposed_boundList.get(0);
			JSONArray flightList1 = flightMap.getJSONArray("list_flight");
			for (int j = 0; j < flightList1.size(); j++) {
				OneWayFlightInfo baseFlight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				JSONObject list_segmentMap = flightList1.getJSONObject(j);
				// 得到具体某航班信息
				JSONArray list_segment = list_segmentMap
						.getJSONArray("list_segment");
				// 获得航班序号
				String flight_id = (String) list_segmentMap.get("flight_id");
				// 获得每个航班所对应的价格ht
				JSONObject priceList = lowPriceMap.getJSONObject(flight_id);
				flightDetail.setPrice(priceList.getDoubleValue("price"));
				flightDetail.setTax(priceList.getDoubleValue("tax"));
				flightDetail.setMonetaryunit(priceList.getString("curr"));
				flightDetail.setWrapperid(CODEBASE);
				// 考虑航班中有联程的情况
				List<String> flightnoList = Lists.newArrayList();
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
					seg.setDepDate(parseDate(flightInfoMap.getString("b_date_date")));
					seg.setArrDate(parseDate(flightInfoMap.getString("e_date_date")));
					segs.add(seg);
					flightnoList.add(seg.getFlightno());
				}
				flightDetail.setDepcity(segs.get(0).getDepairport());
				flightDetail.setArrcity(segs.get(list_segment.size() - 1)
						.getArrairport());
				
				flightDetail.setDepdate(Date.valueOf(param.getDepDate()));
				flightDetail.setFlightno(flightnoList);
				baseFlight.setDetail(flightDetail);
				baseFlight.setInfo(segs);
				flightList.add(baseFlight);
			}
			result.setData(flightList);
			result.setStatus(Constants.SUCCESS);
			return result;
		} catch (Exception e) {
			result.setStatus(Constants.PARSING_FAIL);
			result.setData(flightList);
			return result;
		}

	}

	public BookingResult getBookingInfo(FlightSearchParam param) {
		String bookingUrlPre = "http://book.air.bg/plnext/bulgarian1/Override.action";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		String[] deptDate = param.getDepDate().split("-");
		String deptdate = deptDate[0] + deptDate[1] + deptDate[2] + "0000";
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("B_LOCATION_1", param.getDep());
		map.put("E_LOCATION_1", param.getArr());
		map.put("B_DATE_1", deptdate);
		map.put("B_ANY_TIME_1", "true");
		map.put("B_DATE_2", deptdate);
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
		map.put("DATE_RANGE_VALUE_2", "");
		map.put("E_TIME", "0000");
		map.put("TRIP_TYPE", "O");
		map.put("CABIN", "E");
		map.put("ADTPAX", "1");
		map.put("FIELD_YTH_NUMBER", "0");
		map.put("CHDPAX", "0");
		map.put("FIELD_STU_NUMBER", "0");
		map.put("InfantPAX", "0");
		map.put("TRAVELLER_TYPE_1", "ADT");
		map.put("SO_SITE_USER_CURRENCY_CODE", "EUR");
		map.put("EMBEDDED_TRANSACTION", "FlexPricerAvailability");
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
		map.put("SO_SITE_REST_AIRLINES_LST", "FB%3BVS%3B9W%3BUS");
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

	// 得到所有航班对应的lowPrice
	public JSONObject getLowPriceMap(JSONObject outboundMap) {
		JSONObject priceMap = new JSONObject();
		JSONArray list_recommendationList = outboundMap
				.getJSONArray("list_recommendation");
		for (int h = 0; h < list_recommendationList.size(); h++) {

			JSONObject arrayList = new JSONObject();

			double total_price = Double
					.valueOf((String) (list_recommendationList.getJSONObject(h)
							.getJSONArray("list_trip_price")).getJSONObject(0)
							.getString("total_amount"));

			double price = Double.valueOf((String) (list_recommendationList
					.getJSONObject(h).getJSONArray("list_trip_price"))
					.getJSONObject(0).getString("amount_without_tax"));

			double tax = Double.valueOf((String) (list_recommendationList
					.getJSONObject(h).getJSONArray("list_trip_price"))
					.getJSONObject(0).getString("tax"));
			//
			String curr = list_recommendationList.getJSONObject(h)
					.getJSONArray("list_trip_price").getJSONObject(0)
					.getJSONObject("currency").getString("code");
			arrayList.put("curr", curr);
			arrayList.put("price", price);
			arrayList.put("tax", tax);
			arrayList.put("total_price", total_price);
			JSONArray list_fList = list_recommendationList.getJSONObject(h)
					.getJSONArray("list_bound").getJSONObject(0)
					.getJSONArray("list_flight");

			for (int i = 0; i < list_fList.size(); i++) {
				String flight_id = list_fList.getJSONObject(i).getString(
						"flight_id");
				if (!priceMap.containsKey(flight_id)) {
					priceMap.put(flight_id, arrayList);
				}
			}

		}
		return priceMap;
	}

	public String parseDate(String s){
		return s.substring(0, 4)+"-"+s.substring(4, 6)+"-"+s.substring(6, 8);
	}
}
