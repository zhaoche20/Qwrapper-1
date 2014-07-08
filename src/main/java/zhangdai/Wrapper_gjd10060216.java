package zhangdai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dai.zhang
 * Date: 14-7-7
 * Time: 上午10:28
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjd10060216 implements QunarCrawler {

    QFHttpClient httpClient = null;


    public String getHtml(FlightSearchParam flightSearchParam) {

        if (flightSearchParam.getDep() == "DXB")
            flightSearchParam.setDep("XNB");
        if (flightSearchParam.getArr() == "DXB")
            flightSearchParam.setArr("XNB");


        httpClient = new QFHttpClient(flightSearchParam, true);
        httpClient.getHostConfiguration().setHost("booking.etihad.com");
        httpClient.getParams().setCookiePolicy(org.apache.commons.httpclient.cookie.CookiePolicy.DEFAULT);
        String getUrl = "/SSW2010/EYEY/webqtrip.html?journeySpan=OW&promoCode=&referrerCode=affqunar&searchType=NORMAL&departureDate=" + flightSearchParam.getDepDate() + "&numChildren=0&numAdults=1&numInfants=0&origin=" + flightSearchParam.getDep() + "&destination=" + flightSearchParam.getArr() + "&alternativeLandingPage=true&lang=CN&cabinClass=ECONOMY&CID=affqunar&utm_campaign=qunar&utm_medium=affiliates&utm_source=qunar";
        GetMethod get = new QFGetMethod(getUrl);
        get.setFollowRedirects(false);
        GetMethod get1 = null;
        try {
            httpClient.executeMethod(get);
            Header locationHeader = get.getResponseHeader("location");
            getUrl = locationHeader.getValue();
            //服务器返回的cookies信息
            Cookie[] cookies = httpClient.getState().getCookies();
            String tmpcookies = "";
            for (Cookie c : cookies) {
                tmpcookies = tmpcookies + c.toString() + ";";
            }
            if (getUrl.contains("booking.etihad.com")) {
                getUrl = getUrl.split("booking.etihad.com", 2)[1];
            }
            get1 = new QFGetMethod(getUrl);
            //get1.setFollowRedirects(false);
            get1.setRequestHeader("Cookie", tmpcookies);
            httpClient.executeMethod(get1);
            return get1.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        } finally {
            if (get != null)
                get.releaseConnection();
            if (get1 != null)
                get1.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();

        try {
            if (html.equals("Exception")) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                processResultInfo.setData(data);
                return processResultInfo;
            } else if (html.contains("没有航班提供。")) {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                processResultInfo.setData(data);
                return processResultInfo;
            }

            String jsonstr = StringUtils.substringBetween(html, "var templateData =", "};");
            StringBuffer jsonstrbuffer = new StringBuffer(jsonstr);
            jsonstrbuffer.append("}");
            jsonstr = jsonstrbuffer.toString();
            String flightsinfojsonstr = StringUtils.substringBetween(jsonstr, "\"outbounds\":", ",\"inbounds\":[],");

            JSONArray flightsinfo = JSONArray.parseArray(flightsinfojsonstr);
            List list = flightsinfo;

            for (Object flightinfo : list) {

                FlightDetail flightDetail = getFlightDetail(flightinfo, flightSearchParam);
                List<FlightSegement> flightSegementList = new ArrayList<FlightSegement>();

                JSONObject flightinfojson = (JSONObject) flightinfo;
                List segments = flightinfojson.getJSONArray("segments");
                for (Object segment : segments) {
                    JSONObject segmentjson = (JSONObject) segment;
                    FlightSegement flightSegement = new FlightSegement();
                    flightSegement.setDepairport(segmentjson.getString("departureCode"));
                    flightSegement.setArrairport(segmentjson.getString("arrivalCode"));
                    flightSegement.setFlightno(segmentjson.getJSONArray("airlineCodes").get(0) + String.valueOf(segmentjson.getJSONArray("flightNumber").get(0)));
                    flightSegement.setDeptime(segmentjson.getString("departureDate").substring(11, 15));
                    flightSegement.setArrtime(segmentjson.getString("arrivalDate").substring(11, 15));
                    flightSegement.setDepDate(segmentjson.getString("departureDate").substring(0, 10).replace("/", "-"));
                    flightSegement.setArrDate(segmentjson.getString("arrivalDate").substring(0, 10).replace("/", "-"));

                    flightSegementList.add(flightSegement);
                }

                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                oneWayFlightInfo.setDetail(flightDetail);
                oneWayFlightInfo.setInfo(flightSegementList);
                data.add(oneWayFlightInfo);
            }

            if (data.isEmpty()) {
                processResultInfo.setStatus(Constants.NO_RESULT);
                processResultInfo.setData(data);
            } else {
                processResultInfo.setStatus(Constants.SUCCESS);
                processResultInfo.setData(data);
            }

            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(data);
            e.printStackTrace();
            return processResultInfo;
        }
    }


    @Override
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {

        BookingResult bookingResult = new BookingResult();
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();

        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction("https://booking.etihad.com/SSW2010/EYEY/webqtrip.html");
        Map<String, String> inputs = new HashMap<String, String>();
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("get");
        inputs.put("journeySpan", "OW");
        inputs.put("promoCode", "");
        inputs.put("referrerCode", "affqunar");
        inputs.put("searchType", "NORMAL");
        inputs.put("departureDate", depDate);
        inputs.put("numChildren", "0");
        inputs.put("numAdults", "1");
        inputs.put("numInfants", "0");
        inputs.put("origin", dep);
        inputs.put("destination", arr);
        inputs.put("referrerCode", "");
        inputs.put("alternativeLandingPage", "true");
        inputs.put("lang", "CN");
        inputs.put("cabinClass", "ECONOMY");
        inputs.put("CID", "affqunar");
        inputs.put("utm_campaign", "qunar");
        inputs.put("utm_medium", "affiliates");
        inputs.put("utm_source", "qunar");

        bookingInfo.setInputs(inputs);
        System.out.println(bookingInfo);
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    /**
     * ******************getFlightDetail**************************
     */
    public FlightDetail getFlightDetail(Object flightinfo, FlightSearchParam flightSearchParam) {


        FlightDetail flightDetail = new FlightDetail();
        flightDetail = setprice(flightinfo, flightDetail);
        flightDetail.setDepcity(flightSearchParam.getDep());
        flightDetail.setArrcity(flightSearchParam.getArr());
        Date depdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            depdate = sdf.parse(flightSearchParam.getDepDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        flightDetail.setDepdate(depdate);


        JSONObject flightinfojson = (JSONObject) flightinfo;
        JSONObject itineraryPartData = flightinfojson.getJSONObject("itineraryPartData");

        List<String> flightno = new ArrayList<String>();
        List list = itineraryPartData.getJSONArray("flightNumber");
        List list2 = itineraryPartData.getJSONArray("operatingCarrier");
        for (int i = 0; i < list.size(); i++) {
            flightno.add((String) list2.get(i) + String.valueOf(list.get(i)));
        }
        flightDetail.setFlightno(flightno);


        return flightDetail;
    }

    /**
     * ******************setprice**************************
     */

    public FlightDetail setprice(Object s, FlightDetail flightDetail) {
        JSONObject jsonstr2 = (JSONObject) s;
        JSONObject basketsRef = jsonstr2.getJSONObject("basketsRef");


        JSONObject es = basketsRef.getJSONObject("ES");
        JSONObject ev = basketsRef.getJSONObject("EV");
        JSONObject ef = basketsRef.getJSONObject("EF");
        JSONObject bs = basketsRef.getJSONObject("BS");
        JSONObject bv = basketsRef.getJSONObject("BV");
        JSONObject bf = basketsRef.getJSONObject("BF");
        JSONObject df = basketsRef.getJSONObject("DF");

        if (es != null) {
            flightDetail = setflightDetailByBasketsRef(es, flightDetail);
        } else if (ev != null) {
            flightDetail = setflightDetailByBasketsRef(ev, flightDetail);
        } else if (ef != null) {
            flightDetail = setflightDetailByBasketsRef(ef, flightDetail);
        } else if (bs != null) {
            flightDetail = setflightDetailByBasketsRef(bs, flightDetail);
        } else if (bv != null) {
            flightDetail = setflightDetailByBasketsRef(bv, flightDetail);
        } else if (bf != null) {
            flightDetail = setflightDetailByBasketsRef(bf, flightDetail);
        } else if (df != null) {
            flightDetail = setflightDetailByBasketsRef(df, flightDetail);
        }
        return flightDetail;
    }


    public FlightDetail setflightDetailByBasketsRef(JSONObject basketsRef, FlightDetail flightDetail) {
        JSONObject price = basketsRef.getJSONObject("prices");
        Double tax = 0.0;
        List taxs = price.getJSONArray("moneyElements");
        for (int i = 0; i < 3; i++) {
            JSONObject moneyElement = (JSONObject) taxs.get(i);
            JSONObject moneyTO = moneyElement.getJSONObject("moneyTO");
            tax += moneyTO.getDouble("amount");
        }

        Double price1 = ((JSONObject) taxs.get(3)).getJSONObject("moneyTO").getDouble("amount");
        String monetaryunit = ((JSONObject) taxs.get(3)).getJSONObject("moneyTO").getJSONObject("currency").getString("code");

        flightDetail.setMonetaryunit(monetaryunit);
        flightDetail.setPrice(price1);
        flightDetail.setTax(tax);
        return flightDetail;
    }

}
