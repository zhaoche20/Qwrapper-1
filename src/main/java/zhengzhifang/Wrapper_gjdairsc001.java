package zhengzhifang;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.client.params.CookiePolicy;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.travelco.rdf.infocenter.InfoCenter;

/**
 * zhifang.zheng
 */
public class Wrapper_gjdairsc001 implements QunarCrawler {

    private static final String CODEBASE = "gjdairsc001";
    private QFHttpClient httpClient = null;

    public static void main(String[] args) {
        Wrapper_gjdairsc001 p = new Wrapper_gjdairsc001();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("TNA");
        flightSearchParam.setArr("TPE");
        flightSearchParam.setDepDate("2014-07-13");
        flightSearchParam.setWrapperid("gjdairsc001");
        flightSearchParam.setQueryId("http://???");
        String html = p.getHtml(flightSearchParam);
        System.out.println("开始打印：" + html);
        System.out.println("结束打印");
//		System.out.println(p.process(html, ""));
        System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
		/*BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(bookingResult));*/
    }




    public String getHtml(FlightSearchParam flightSearchParam) {
        try {
            httpClient = new QFHttpClient(flightSearchParam, true);
            httpClient.getParams().setParameter("Use-Https", "1");
            httpClient.getParams().setCookiePolicy(
                    CookiePolicy.BROWSER_COMPATIBILITY);
            String dep = flightSearchParam.getDep();
            String arr = flightSearchParam.getArr();
            String postUrl = "http://sc.travelsky.com/scet/queryAvInternational.do";// 跳转到

            QFPostMethod postMethod = new QFPostMethod(postUrl);
            String requestStr = "countrytype=1&travelType=0&cityNameOrg=&cityCodeOrg="
                    + dep
                    + "&cityNameDes=&cityCodeDes="
                    + arr
                    + "&takeoffDate="
                    + flightSearchParam.getDepDate()
                    + "&returnDate=";

            postMethod.setRequestEntity(new StringRequestEntity(requestStr,
                    "application/x-www-form-urlencoded", "UTF-8"));
            postMethod.addRequestHeader("Content-Type",
                    "application/x-www-form-urlencoded");
            postMethod.addRequestHeader("Referer",
                    "http://sc.travelsky.com/scet/index.jsp");

            int responseCode = httpClient.executeMethod(postMethod);
            String resp = postMethod.getResponseBodyAsString();
            return resp;
        } catch (Exception e) {
            return "EXCEPTION";
        }
    }

    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        ArrayList<OneWayFlightInfo> oneWayFlightInfos = new ArrayList<OneWayFlightInfo>();
        if ("Exception".equals(html)) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }

        if (html.contains("很抱歉，没有符合条件的查询结果")) {
            processResultInfo.setStatus(Constants.INVALID_DATE);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        if (html.contains("国际航班查询出错:输入参数错误")) {
            processResultInfo.setStatus(Constants.INVALID_DATE);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        StringBuilder builder = new StringBuilder("<table>");
        try {
            String invalidInfo = getValue(html,"<table width=\"718\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\" bgcolor=\"#9ac5e3\" class=\"mg-bottom14\">","<table width=\"723\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\" bgcolor=\"#9ac5e3\">").trim();
            String[] trStrs = getValues(invalidInfo, "<table width=\"718\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"flow0_chaxun_tab060\">", "</table></td>");
            for (int i = 0; i < trStrs.length; i++) {

                String flightStr = getValue(trStrs[i], "<tr>", "<table");
                String[] tds = getValues(flightStr, "<td", "/td>");
                String code = getValue(getValue(tds[0], "<span", "/span>"), ">", "<").trim();
                String depTime = getValue(tds[1], ">", "<br").trim();
                String arrTime = getValue(tds[1], "/>", "<").trim();
                String[] spans = getValues(flightStr, "<span", "/span>");
                String planType = getValue(spans[1], ">", "<").trim();
                String[] priceStr = getValues(trStrs[i], "<td width=\"114\" align=\"center\">", "</td>");
                String priceInfo = getValue(priceStr[0], "<span", "/span>");
                String str = getValue(priceInfo, ">", "<").trim().replaceAll("[^\\d]", "");
                int lowPrice = Integer.valueOf(str);
                for(int j = 1; j < priceStr.length; j++){
                    int price = Integer.valueOf(getValue(getValue(priceStr[j], "<span", "/span>"), ">", "<").trim().replaceAll("[^\\d]", ""));
                    if (price < lowPrice) {
                        lowPrice = price;
                    }
                }

                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                List<FlightSegement> segements = new ArrayList<FlightSegement>();
                FlightSegement flightSegement = new FlightSegement();
                FlightDetail flightDetail = new FlightDetail();
                List<String> flightNoList = Lists.newArrayList();

                //flightSegement
                flightSegement.setDepairport(dep);
                flightSegement.setArrairport(arr);
                flightSegement.setFlightno(code);
                flightSegement.setDeptime(depTime);
                flightSegement.setArrtime(arrTime);
                flightSegement.setDepDate(flightSearchParam.getDepDate());
                flightSegement.setArrDate("");//到达日期
                flightSegement.setCompany("SC");
                flightSegement.setAircraft(planType);
                segements.add(flightSegement);
                //flightDetail
                flightDetail.setDepcity(dep);
                flightDetail.setArrcity(arr);
                flightDetail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(flightSearchParam.getDepDate()));
                flightNoList.add(code);
                flightDetail.setFlightno(flightNoList);
                flightDetail.setMonetaryunit("CNY");
                flightDetail.setTax(0);
                flightDetail.setPrice(lowPrice);
                flightDetail.setWrapperid("gjdairsc001");
                flightDetail.setCreatetime(new Timestamp(System.currentTimeMillis()));
                flightDetail.setUpdatetime(new Timestamp(System.currentTimeMillis()));

                oneWayFlightInfo.setDetail(flightDetail);
                oneWayFlightInfo.setInfo(segements);
                oneWayFlightInfos.add(oneWayFlightInfo);
            }
            processResultInfo.setData(oneWayFlightInfos);
            processResultInfo.setStatus(Constants.SUCCESS);
            processResultInfo.setRet(true);
            processResultInfo.setKey(flightSearchParam.getQueryId());
            return processResultInfo;
        }
        catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
    }

    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult = new BookingResult();
        bookingResult.setRet(true);
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction("http://sc.travelsky.com/scet/queryAvInternational.do");
        bookingInfo.setMethod("post");
//      bookingInfo.setContentType("GBK");
        String depDate = flightSearchParam.getDepDate();
        Map<String, String> inputs = Maps.newLinkedHashMap();
        String depzh = InfoCenter.getCityFromAnyCode(flightSearchParam.getDep(), "ZH");
        String arrzh = InfoCenter.getCityFromAnyCode(flightSearchParam.getArr(), "ZH");
        inputs.put("countrytype", "1");
        inputs.put("travelType", "0");
        inputs.put("cityNameOrg", "");
        inputs.put("cityCodeOrg", flightSearchParam.getDep());
        inputs.put("cityNameDes", "");
        inputs.put("cityCodeDes", flightSearchParam.getArr());
        inputs.put("takeoffDate", depDate);
        inputs.put("step", "0");
        inputs.put("usefor", "1");
        inputs.put("x", "67");
        inputs.put("y", "18");
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

