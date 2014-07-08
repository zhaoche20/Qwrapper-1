package guoting;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFGetMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;

/**
 *  User: ting.guo
 */
public class Wrapper_gjsairbt002 implements QunarCrawler {

    public static void main(String[] args) {

        FlightSearchParam searchParam = new FlightSearchParam();
        searchParam.setDep("AAL");
        searchParam.setArr("RIX");
        searchParam.setDepDate("2014-07-12");
        searchParam.setRetDate("2014-07-30");
        String html = new Wrapper_gjsairbt002().getHtml(searchParam);
        ProcessResultInfo info = new Wrapper_gjsairbt002().process(html,
                searchParam);
        BookingResult bookingInfo=new Wrapper_gjsairbt002().getBookingInfo(searchParam);
        System.out.println(JSON.toJSONString(info));
    }

    public String getHtml(FlightSearchParam searchParam) {
        QFHttpClient httpClient = null;
        QFGetMethod get = null;
        try {
            httpClient = new QFHttpClient(searchParam, false);
            String depAirport = searchParam.getDep().toLowerCase();
            String arrAirport = searchParam.getArr().toLowerCase();
            String depDay = searchParam.getDepDate().split("-")[2];
            String depMonth = searchParam.getDepDate().split("-")[1] + "-"
                    + searchParam.getDepDate().split("-")[0];
            String retDay = searchParam.getRetDate().split("-")[2];
            String retMonth = searchParam.getRetDate().split("-")[1] + "-"
                    + searchParam.getRetDate().split("-")[0];
            String urlStr = "https://tickets1.airbaltic.com/app/fb.fly?sref=QNR&k=d41d8cd98f00b204e9800998ecf8427e&p=QNR&l=en&"
                    + "action=avail&legs=2&origin="
                    + depAirport
                    + "&destin="
                    + arrAirport
                    + "&day0="
                    + depDay
                    + "&day1="
                    + retDay
                    + "&month0="
                    + depMonth
                    + "&month1="
                    + retMonth
                    + "&numadt=1&numchd=0&numinf=0";
            get = new QFGetMethod(urlStr);
            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (null != get) {
                get.releaseConnection();
            }
        }
    }

    public ProcessResultInfo process(String html,
                                     FlightSearchParam flightSearchParam) {

        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        try {
            if (html.equals("Exception")) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                return processResultInfo;
            } else if (html
                    .indexOf("we are unable to offer flights on these exact dates") > -1) {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                return processResultInfo;
            } else if (html
                    .indexOf("airBaltic does not operate flights on this route") > -1) {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                return processResultInfo;
            }

            String shtml = StringUtils.substringBetween(html, "<offers>",
                    "</offers>");
            List<RoundTripFlightInfo> roundTripFlightInfos = (List<RoundTripFlightInfo>) processResultInfo
                    .getData();
            String[] roundTripFlightInfosArr = StringUtils.substringsBetween(
                    shtml, "<offer ", "</offer>");
            for (String s_roundTripFlightInfo : roundTripFlightInfosArr) {

                RoundTripFlightInfo roundTripFlightInfo = getRoundTripFlightInfo(
                        s_roundTripFlightInfo, flightSearchParam);
                roundTripFlightInfos.add(roundTripFlightInfo);
            }
            processResultInfo.setData(roundTripFlightInfos);
            processResultInfo
                    .setStatus(roundTripFlightInfos.isEmpty() ? Constants.NO_RESULT
                            : Constants.SUCCESS);
            return processResultInfo;
        } catch (Exception e) {
            e.printStackTrace();
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }

    }

    private RoundTripFlightInfo getRoundTripFlightInfo(String str,
                                                       FlightSearchParam flightSearchParam) {
        // 返程的信息
        RoundTripFlightInfo roundTripFlightInfo = new RoundTripFlightInfo();
        roundTripFlightInfo.setRetdepdate(formatDate(
                flightSearchParam.getRetDate(), "yyyy-MM-dd"));
        String s_retFlightSegementInfo = StringUtils.substringBetween(str,
                "<journey id=\"1\">", "</journey>");
        roundTripFlightInfo
                .setRetinfo(getFlightSegements(s_retFlightSegementInfo));
        roundTripFlightInfo.setRetflightno(getFlightNos(roundTripFlightInfo
                .getRetinfo()));

        // 去程的信息
        String s_FlightSegementInfo = StringUtils.substringBetween(str,
                "<journey id=\"0\">", "</journey>");
        roundTripFlightInfo.setInfo(getFlightSegements(s_FlightSegementInfo));
        FlightDetail detail = roundTripFlightInfo.getDetail();
        detail.setArrcity(flightSearchParam.getArr());
        detail.setDepcity(flightSearchParam.getDep());
        detail.setDepdate(formatDate(flightSearchParam.getDepDate(),
                "yyyy-MM-dd"));
        detail.setFlightno(getFlightNos(roundTripFlightInfo.getInfo()));
        double tax = Double.parseDouble(StringUtils.substringBetween(str, "<taxtotal>", "</taxtotal>"));
        double price = Double.parseDouble(StringUtils.substringBetween(str, "<GROSS>", "</GROSS>")) - tax;
        String monetaryunit = StringUtils.substringBetween(str, "<currency>", "</currency>");
        detail.setTax(tax);
        detail.setPrice(price);
        detail.setMonetaryunit(monetaryunit);
        roundTripFlightInfo.setDetail(detail);

        return roundTripFlightInfo;
    }

    // 验证flightDetail
    private boolean validateDetail(FlightDetail flightDetail) {
        return flightDetail != null
                && StringUtils.isNotBlank(flightDetail.getDepcity())
                && StringUtils.isNotBlank(flightDetail.getArrcity())
                && flightDetail.getDepdate() != null
                && (flightDetail.getFlightno().size()) != 0
                && flightDetail.getPrice() != 0.0
                && (flightDetail.getTax() == 0.0 || flightDetail.getTax() > 0.0);
    }

    // 格式化日期
    private Date formatDate(String dateStr, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    // 获取每个航段中的航段号
    private List<String> getFlightNos(List<FlightSegement> flightSegements) {
        List<String> flightNos = Lists.newArrayList();
        for (FlightSegement flightSegement : flightSegements) {
            flightNos.add(flightSegement.getFlightno());
        }
        return flightNos;
    }

    // 添加每个航段到航段list里
    private List<FlightSegement> getFlightSegements(String s_flightInfo) {

        List<FlightSegement> flightInfo = Lists.newArrayList(); // 返程航段信息列表
        String[] flightInfos = StringUtils.substringsBetween(s_flightInfo,
                "<flight ", "</flight>");
        if (flightInfos == null || flightInfos.length == 0) {
            return flightInfo;
        }
        for (String Info : flightInfos) {
            flightInfo.add(getOneFlightSegement(Info));
        }
        return flightInfo;
    }

    // 验证航段信息
    private boolean validateFlightSegement(FlightSegement flightSegement) {
        return flightSegement != null
                && StringUtils.isNotBlank(flightSegement.getArrairport())
                && StringUtils.isNotBlank(flightSegement.getArrtime())
                && StringUtils.isNotBlank(flightSegement.getDepairport())
                && StringUtils.isNotBlank(flightSegement.getDeptime())
                && StringUtils.isNotBlank(flightSegement.getFlightno());
    }

    // 获取航段的信息
    private FlightSegement getOneFlightSegement(String str) {
        FlightSegement flightSegement = new FlightSegement();
        String flightno = StringUtils.substringBetween(str, "<carrCode>",
                "</carrCode>").trim()
                + StringUtils.substringBetween(str, "<fltNumber>",
                "</fltNumber>").trim();
        String depairport = StringUtils.substringBetween(str, "<origCode>",
                "</origCode>").trim();
        String depDate = StringUtils.substringBetween(str, "<depDate>",
                "</depDate>").trim();
        String deptime = StringUtils.substringBetween(str, "<depTime>",
                "</depTime>").trim();
        String arrairport = StringUtils.substringBetween(str, "<destCode>",
                "</destCode>").trim();
        String arrDate = StringUtils.substringBetween(str, "<arrDate>",
                "</arrDate>").trim();
        String arrtime = StringUtils.substringBetween(str, "<arrTime>",
                "</arrTime>").trim();
        flightSegement.setFlightno(flightno);
        flightSegement.setDepairport(depairport);
        flightSegement.setDepDate(depDate);
        flightSegement.setDeptime(deptime);
        flightSegement.setArrairport(arrairport);
        flightSegement.setArrDate(arrDate);
        flightSegement.setArrtime(arrtime);
        return flightSegement;

    }

    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult  bookingResult=new 	BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction("https://tickets.airbaltic.com/app/fb.fly");
        bookingInfo.setContentType("utf-8");
        bookingInfo.setMethod("post");
        Map<String,String> map=new HashMap<String,String>();
        map.put("action", "avail");
        map.put("p", "bti");
        map.put("pos", "LV");
        map.put("l","en");
        map.put("traveltype", "bti");
        map.put("origin", "");
        map.put("origin_type", "");
        map.put("destin", "");
        map.put("destin_type", "");
        map.put("flt_origin_text", flightSearchParam.getDep());
        map.put("flt_destin_text",flightSearchParam.getArr());
        map.put("flt_leaving_on", getDate(flightSearchParam.getDepDate()));
        map.put("flt_returning_on", getDate(flightSearchParam.getRetDate()));
        map.put("legs","2");
        map.put("numadt","1");
        map.put("numchd", "0");
        map.put("numinf", "0");
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return  bookingResult;
    }

    private String getDate(String date)
    {
        String[] ss=date.split("-");
        return ss[2]+"."+ss[1]+"."+ss[0];

    }
}
