package qurunjing;

import com.qunar.qfwrapper.interfaces.QunarCrawler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.qunar.qfwrapper.developer.QFPostMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.travelco.rdf.infocenter.InfoCenter;
/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-1
 * Time: 上午10:55
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjdairsv001 implements QunarCrawler {

    private static final String CODEBASE = "gjdairsv001";
    private QFHttpClient httpClient = null;
    private FlightSearchParam param = new FlightSearchParam();

    @Override
    public String getHtml(FlightSearchParam param) {
        httpClient = new QFHttpClient(param, true);
        this.param = param;
        httpClient = new QFHttpClient(param, true);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        PostMethod postMethod = null;
        PostMethod midpostMethod = new QFPostMethod(
                "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");// new一个跳转的post
        String dep = param.getDep().toString();
        String arr = param.getArr().toString();

        try {
            String Mid_B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";// 将取出的时间串格式化与Post中的格式一致。

            String[] Split_Date1 = param.getDepDate().split("-");

            String Field_Date1 = Split_Date1[2] + "/" + Split_Date1[1] + "/" + Split_Date1[0];

            String B_LOCATION_auto = InfoCenter.getCityFromAirportCode(dep);
            B_LOCATION_auto = InfoCenter.getCityFromAnyCode(dep, "en");
            String E_LOCATION_auto = InfoCenter.getCityFromAirportCode(arr);
            E_LOCATION_auto = InfoCenter.getCityFromAnyCode(arr, "en");

            String midpostBody = "&TRIP_TYPE=O" + "&B_LOCATION_auto=" + B_LOCATION_auto + "&B_LOCATION=" + dep
                    + "&E_LOCATION_auto=" + E_LOCATION_auto + "&E_LOCATION=" + arr + "&textfield2=" + Field_Date1
                    + "&DATE_1=" + Mid_B_DATE_1 + "&textfield2=" + "&DATE_2=" + "&adults=1" + "&TRAVELLER_TYPE_1=ADT"
                    + "&children=0" + "&infants=0" + "&CABIN=E" + "&EMBEDDED_TRANSACTION=AirSearch" + "&LANGUAGE=GB"
                    + "&SITE=BDXHBDXH" + "&TRIP_FLOW=YES" + "&BOOKING_FLOW=REVENUE" + "&SHOW_MULTIDEST=FALSE"
                    + "&IS_FLEXIBLE=FALSE" + "&WDS_SERVICING=TRUE" + "&WDS_TIMETABLE=TRUE" + "&WDS_FLIGHTSTATUS=TRUE"
                    + "&WDS_ETKT_RETRIEVE=TRUE" + "&WDS_AAAS=TRUE" + "&WDS_XBAG=TRUE" + "&WDS_UPGR=FALSE"
                    + "&WDS_ATC_REBOOK=FALSE" + "&WDS_XHTML_DIRECT_ACCESS=FALSE" + "&WDS_ATC_REFUND=TRUE"
                    + "&SO_SITE_ALLOW_ATC_REFUND=TRUE" + "&SO_SITE_ATC_ALLOW_OFFLINE=TRUE"
                    + "&SO_SITE_ATC_FARE_DRIVEN=TRUE" + "&SO_SITE_ATC_SCHEDULE_DRIVEN=FALSE" + "&WDS_PAY_NOW=TRUE"
                    + "&WDS_CANCEL_PNR=TRUE" + "&SO_SITE_ALLOW_PNR_CANCEL=Y" + "&SO_SITE_ALLOW_ON_HOLD_CANCEL=TRUE"
                    + "&SO_SITE_ALLOW_TKT_PNR_CANCEL=Y&WDS_JOIN_ALFURSAN=TRUE";

            midpostMethod.setRequestEntity(new StringRequestEntity(midpostBody, "application/x-www-form-urlencoded",
                    null));
            midpostMethod
                    .addRequestHeader(
                            "Referer",
                            "http://www.saudiairlines.com/portal/site/saudiairlines/menuitem.aeabf36fd343b335173ff63dc8f034a0/?vgnextoid=82aae1cb93e70110VgnVCM1000008c0f430aRCRD");
            httpClient.executeMethod(midpostMethod);

            String midResult = midpostMethod.getResponseBodyAsString();// 将midPost的内容post给服务器取出Post需要的PostData

			/* System.out.println("+=++=+++midResult+++=++=+="+midResult); */

            String B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";
            String B_DATE_2 = param.getDepDate().replaceAll("-", "") + "0000";
            String B_LOCATION_1 = param.getDep();
            String E_LOCATION_1 = param.getArr();// 取出Post中的变量，并且将其格式转化成需要Post需要的格式
            String officeId = "";

            String javascript = this.getValue(midResult, "LOCATIONS = {", "};").trim();
            javascript = this.getValue(javascript, "\"" + dep + "\" : {", "\"domestic\" :");
            officeId = this.getValue(javascript, "\"GLOBAL\" : \"", "\"},").trim();
            if ("".equals(officeId)) {
                officeId = "WASSV08AA";
            }

            String postBody = "&TRIP_TYPE=O" + "&LANGUAGE=GB" + "&SITE=BDXHBDXH"
                    + "&EMBEDDED_TRANSACTION=FlexPricerAvailability" + "&EXTERNAL_ID=dummy" + "&B_DATE_1="
                    + B_DATE_1
                    + "&B_DATE_2="
                    + B_DATE_2
                    + "&PRICING_TYPE=C"
                    + "&DISPLAY_TYPE=2"
                    + "&COMMERCIAL_FARE_FAMILY_1=CFFGUEST"
                    + "&COMMERCIAL_FARE_FAMILY_2=CFFBUS"
                    + "&COMMERCIAL_FARE_FAMILY_3=CFFFIRST"
                    + "&B_LOCATION_1="
                    + B_LOCATION_1
                    + "&E_LOCATION_1="
                    + E_LOCATION_1 + "&WDS_TABS=online" + "&SO_SITE_OFFICE_ID=" + officeId + // WASSV08AA,JEDSV08AA
                    "&TRAVELLER_TYPE_1=ADT" + "&REFRESH=0" + "&WDS_B_LOCATION_1_DISPLAY=" + B_LOCATION_auto + // "&WDS_B_LOCATION_1_DISPLAY="
                    // +WDS_B_LOCATION_1_DISPLAY+
                    "&WDS_E_LOCATION_1_DISPLAY=" + E_LOCATION_auto + // "&WDS_E_LOCATION_1_DISPLAY="
                    // +WDS_E_LOCATION_1_DISPLAY+
                    "&CABIN=E" + "&WDS_NB_ADT=1" + "&WDS_NB_CHD=0" + "&WDS_NB_INF=0";

            String action = this.getValue(midResult, "name=\"SEARCH_COMPLEX_FORM\" method=\"post\" action=\"",
                    "\" onSubmit=\"return false;\"");

            postMethod = new QFPostMethod("http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/" + action);

            postMethod.setRequestEntity(new StringRequestEntity(postBody, "application/x-www-form-urlencoded", null));
            postMethod.addRequestHeader("Referer",
                    "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");
            httpClient.executeMethod(postMethod);
            String Result = postMethod.getResponseBodyAsString(); // 类同与MidPost过程，将postBody通过PastMethod方法给浏览器，最后取回Result
            return Result;
        } catch (Exception e) {
            return "Exception";
        } finally {

            if (midpostMethod != null) {
                midpostMethod.releaseConnection();
            }

            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }

    }

    public String getValue(String source, String st, String end) {
        int a = source.indexOf(st);
        if (a == -1)
            return "";
        int b = source.indexOf(end, a + st.length());
        if (b == -1)
            return "";
        return source.substring(a + st.length(), b);
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        if (html.equals("Exception") || html.startsWith("QProxy")) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            processResultInfo.setData(data);
            return processResultInfo;
        }
        try {
            Map<String, SaudFlightD> flights = new HashMap<String, SaudFlightD>(); // 存放航班，key为航班号，值为航班
            processResultInfo = this.getFlightsInfo(html, flights);
        } catch (Exception e) {
            Collector.count(CODEBASE, Key.PARSING_FAIL);
            processResultInfo = new ProcessResultInfo();
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
        if (processResultInfo == null) {
            processResultInfo = new ProcessResultInfo();
            processResultInfo.setStatus(Constants.NO_RESULT);
        } else
            processResultInfo.setStatus(Constants.SUCCESS);
        return processResultInfo;
    }

    public String[] getValues(String source, String st, String end) {
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

    public String getValue(String source, String regEx) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
    }

    @SuppressWarnings("deprecation")
    private ProcessResultInfo getFlightsInfo(String html, Map<String, SaudFlightD> flights) {
        ProcessResultInfo result = new ProcessResultInfo();
        if(html.contains("We are unable to find departing flights for the requested outbound. (9200 [-1])")){
            result.setStatus(Constants.INVALID_AIRLINE);
            return result;
        }
        if(html.contains("Flights out are not available for the date / time specified. However, you can see flights for alternate days by selecting other dates below. (7124 [-1])")){
            result.setStatus(Constants.INVALID_DATE);
            return result;
        }
        if ("Exception".equals(html)) {
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        // String[] args = new URLHelper(url).getValues();
        String info = this.getValue(html, "Modify Search", "<span>Back</span>");
        String tbodyInfo = this.getValue(info, "<tbody id=\"b0FlightsTBodyEl\">", "</tbody>");
        String[] flightInfos = tbodyInfo.split("class=\"flight flight\\d+(.*) first");
        String currencystr = this.getValue(info, "<p class=\"note\">", "</p>");
        String currency = this.getValue(currencystr, "<strong>", "</strong>").trim();
        for (int i = 1; i < flightInfos.length; i++) {
            String arrDate = param.getDepDate();
            String depDate = param.getDepDate();
            String str = flightInfos[i];

            String[] codes = this.getValues(str, "onclick=\"theOcupForm.popFLIF(", ");");
            double retailPrice = 0;
            List<FlightSegement> seges = new ArrayList<FlightSegement>();
            FlightSegement sege = new FlightSegement();
            List<String> flightno = new ArrayList<String>();
            int timesNum = 0;
            for (String code : codes) {
                String[] codestr = code.split("','");
                flightno.add(codestr[3] + codestr[4]);
                sege = new FlightSegement();
                String[] firstime = this.getValues(str, "<th class=\"first time\">", "</th>");
                String deptime = this.getValue(firstime[timesNum], "\\d{2}:\\d{2}");
                String[] lasttimes = this.getValues(str, "<th class=\"time\">", "</th>");
                String arrtime = this.getValue(lasttimes[timesNum], "\\d{2}:\\d{2}");
                sege.setAircraft(codestr[5]);
                sege.setArrairport(codestr[2]);
                sege.setDepairport(codestr[1]);
                sege.setArrtime(arrtime);
                sege.setDeptime(deptime);
                sege.setArrDate(arrDate);
                sege.setDepDate(depDate);
                if (firstime[timesNum].contains("+1")) {
                    Date date = new Date(depDate.replaceAll("-", "/"));
                    date.setDate(date.getDate() + 1);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    depDate = sdf.format(date);
                    sege.setDepDate(depDate);
                }
                if (lasttimes[timesNum].contains("+1")) {
                    Date date = new Date(arrDate.replaceAll("-", "/"));
                    date.setDate(date.getDate() + 1);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    arrDate = sdf.format(date);
                    sege.setArrDate(arrDate);
                }
                timesNum++;
                // }
                sege.setFlightno(codestr[3] + codestr[4]);
                seges.add(sege);
            }

            String[] pricestrs = this.getValues(str, "<span class=\"price\">", "</span>");
            for (String pricestr : pricestrs) {
                pricestr = pricestr.replaceAll("<.*>", "").trim().replaceAll("[^\\d\\.]", "");
                if (org.apache.commons.lang.StringUtils.isNotEmpty(pricestr)) {
                    double price = Double.valueOf(pricestr);
                    if (0 == retailPrice || price < retailPrice) {
                        retailPrice = price;
                    }
                }
            }
            OneWayFlightInfo one = new OneWayFlightInfo();
            FlightDetail detail = new FlightDetail();
            detail.setArrcity(param.getArr());
            detail.setDepcity(param.getDep());
            detail.setDepdate(new Date(param.getDepDate().replaceAll("-", "/")));
            detail.setMonetaryunit(currency);
            detail.setPrice(retailPrice);
            detail.setTax(0);
            detail.setFlightno(flightno);
            detail.setWrapperid("gjdairsv001");
            one.setDetail(detail);
            one.setInfo(seges);
            data.add(one);


        }
        result.setData(data);
        return result;
    }

    private double MinPrice(String SplitFlightinfo) {

        String[] Price = this.getValues(SplitFlightinfo, "<span class=\"price\"> <span class=\"number\">",
                "</span></span>");

        double MinPrice = Double.parseDouble(Price[0].replaceAll("\\D", ""));

        try {
            for (int n = 0; n < Price.length - 1;) {
                ++n;
                if ((Double.parseDouble(Price[n - 1].replaceAll("\\D", ""))) < (Double.parseDouble(Price[0].replaceAll(
                        "\\D", "")))) {
                    MinPrice = Double.parseDouble(Price[n].replaceAll("\\D", ""));

                } else if ((Double.parseDouble(Price[n - 1].replaceAll("\\D", ""))) > (Double.parseDouble(Price[0]
                        .replaceAll("\\D", "")))) {
                    MinPrice = Double.parseDouble(Price[0].replaceAll("\\D", ""));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return MinPrice;
    }

    private String getOtherCode(String TbodyInfo, String url) {// 获得本地航空公司航班号码

        String[] Split_OtherCode = this.getValues(TbodyInfo, "'ico ico_other');\">", "</a>");
        String OtherCode = "";
        for (int i = 0; i < Split_OtherCode.length; i++) {
            OtherCode = OtherCode + Split_OtherCode[i] + "/";
        }

        return OtherCode;
    }

    private String getOwnCode(String TbodyInfo, String url) {// 获得其他航空公司航班号

        String[] Split_OwnCode = this.getValues(TbodyInfo, "'ico ico_own');\">", "</a>");
        String OwnCode = "";
        for (int i = 0; i < Split_OwnCode.length; i++) {
            OwnCode = OwnCode + Split_OwnCode[i] + "/";
        }

        return OwnCode;
    }

    private String getBorder(String url) {// 定位出发机场全名称，以便获得全部的航班号码

        String dep = param.getDep();
        String arr = param.getArr();
        String dep_Mid_B_date = param.getDepDate();

        QFHttpClient httpClient = new QFHttpClient(param, true);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        PostMethod midpostMethod = new QFPostMethod(
                "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");// new一个跳转的post
        PostMethod postMethod = new QFPostMethod(
                "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action;jsessionid=G6GtSDMJbqPt3SfZkSGHDxrZnLCh7r2h4mrJydQfFzhc3RTm4nfT!-2135301356!432618502");// new一个最终的Post


        try {

            String Mid_B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";// 将取出的时间串格式化与Post中的格式一致。

            String[] Split_Date1 = param.getDepDate().split("-");
            String Field_Date1 = Split_Date1[2] + "/" + Split_Date1[1] + "/" + Split_Date1[0];

            String B_LOCATION_auto = InfoCenter.getCityFromAirportCode(dep);

            String E_LOCATION_auto = InfoCenter.getCityFromAirportCode(arr);

            String midpostBody = "&TRIP_TYPE=O" + "&B_LOCATION_auto=" + B_LOCATION_auto + "&B_LOCATION=" + dep
                    + "&E_LOCATION_auto=" + E_LOCATION_auto + "&E_LOCATION=" + arr + "&textfield2=" + Field_Date1
                    + "&DATE_1=" + Mid_B_DATE_1 + "&textfield2=" + "&DATE_2=" + "&adults=1" + "&TRAVELLER_TYPE_1=ADT"
                    + "&children=0" + "&infants=0" + "&CABIN=E" + "&EMBEDDED_TRANSACTION=AirSearch" + "&LANGUAGE=GB"
                    + "&SITE=BDXHBDXH" + "&TRIP_FLOW=YES" + "&BOOKING_FLOW=REVENUE" + "&SHOW_MULTIDEST=FALSE"
                    + "&IS_FLEXIBLE=FALSE" + "&WDS_SERVICING=TRUE" + "&WDS_TIMETABLE=TRUE" + "&WDS_FLIGHTSTATUS=TRUE"
                    + "&WDS_ETKT_RETRIEVE=TRUE" + "&WDS_AAAS=TRUE" + "&WDS_XBAG=TRUE" + "&WDS_UPGR=FALSE"
                    + "&WDS_ATC_REBOOK=FALSE" + "&WDS_XHTML_DIRECT_ACCESS=FALSE" + "&WDS_ATC_REFUND=TRUE"
                    + "&SO_SITE_ALLOW_ATC_REFUND=TRUE" + "&SO_SITE_ATC_ALLOW_OFFLINE=TRUE"
                    + "&SO_SITE_ATC_FARE_DRIVEN=TRUE" + "&SO_SITE_ATC_SCHEDULE_DRIVEN=FALSE" + "&WDS_PAY_NOW=TRUE"
                    + "&WDS_CANCEL_PNR=TRUE" + "&SO_SITE_ALLOW_PNR_CANCEL=Y" + "&SO_SITE_ALLOW_ON_HOLD_CANCEL=TRUE"
                    + "&SO_SITE_ALLOW_TKT_PNR_CANCEL=Y";

            midpostMethod.setRequestEntity(new StringRequestEntity(midpostBody, "application/x-www-form-urlencoded",
                    null));
            midpostMethod
                    .addRequestHeader(
                            "Referer",
                            "http://www.saudiairlines.com/portal/site/saudiairlines/menuitem.aeabf36fd343b335173ff63dc8f034a0/?vgnextoid=82aae1cb93e70110VgnVCM1000008c0f430aRCRD");
            httpClient.executeMethod(midpostMethod);
            String midResult = midpostMethod.getResponseBodyAsString();// 将midPost的内容post给服务器取出Post需要的PostData

            String E_LOCATION_1 = param.getDep();// 取出Post中的变量，并且将其格式转化成需要Post需要的格式

            String DepAirport = this.get(E_LOCATION_1, midResult);

            return DepAirport;

        } catch (Exception e) {
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
            if (midpostMethod != null) {
                midpostMethod.releaseConnection();
            }
        }


    }

    private String get(String LOCATION, String midResult) {

        String Location = this.getValue(midResult, "LOCATIONS = {", "B_LOCATIONS = E_LOCATIONS ");

        String[] Split = Location.split("\"domestic\" : false},");

        String LOCATION_Finnal = null;

        for (int i = 0; i < Split.length; i++) {

            String Name_All = this.getValue(Split[i], "\"name\" : \"", "\",");

            String[] Args = Split[i].split(":");
            String Location_temp = Args[0].replaceAll("[^A-z]", "");

            if (LOCATION.equals(Location_temp)) {
                LOCATION_Finnal = Name_All;
            }

        }

        // TODO Auto-generated method stub
        return LOCATION_Finnal;
    }

    public String getLocation(String url) {// 通过Url获得完整的机场名称

        String dep = param.getDep();
        String arr = param.getArr();
        String dep_Mid_B_date = param.getDepDate();// url传入的四个参数

        QFHttpClient httpClient = new QFHttpClient(param, true);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        PostMethod midpostMethod = new QFPostMethod(
                "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");// new一个跳转的post

        try {

            String Mid_B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";// 将取出的时间串格式化与Post中的格式一致。

            String[] Split_Date1 = param.getDepDate().split("-");
            String Field_Date1 = Split_Date1[2] + "/" + Split_Date1[1] + "/" + Split_Date1[0];

            String B_LOCATION_auto = InfoCenter.getCityFromAirportCode(dep);

            String E_LOCATION_auto = InfoCenter.getCityFromAirportCode(arr);

            String midpostBody = "&TRIP_TYPE=O" + "&B_LOCATION_auto=" + B_LOCATION_auto + "&B_LOCATION=" + dep
                    + "&E_LOCATION_auto=" + E_LOCATION_auto + "&E_LOCATION=" + arr + "&textfield2=" + Field_Date1
                    + "&DATE_1=" + Mid_B_DATE_1 + "&textfield2=" + "&DATE_2=" + "&adults=1" + "&TRAVELLER_TYPE_1=ADT"
                    + "&children=0" + "&infants=0" + "&CABIN=E" + "&EMBEDDED_TRANSACTION=AirSearch" + "&LANGUAGE=GB"
                    + "&SITE=BDXHBDXH" + "&TRIP_FLOW=YES" + "&BOOKING_FLOW=REVENUE" + "&SHOW_MULTIDEST=FALSE"
                    + "&IS_FLEXIBLE=FALSE" + "&WDS_SERVICING=TRUE" + "&WDS_TIMETABLE=TRUE" + "&WDS_FLIGHTSTATUS=TRUE"
                    + "&WDS_ETKT_RETRIEVE=TRUE" + "&WDS_AAAS=TRUE" + "&WDS_XBAG=TRUE" + "&WDS_UPGR=FALSE"
                    + "&WDS_ATC_REBOOK=FALSE" + "&WDS_XHTML_DIRECT_ACCESS=FALSE" + "&WDS_ATC_REFUND=TRUE"
                    + "&SO_SITE_ALLOW_ATC_REFUND=TRUE" + "&SO_SITE_ATC_ALLOW_OFFLINE=TRUE"
                    + "&SO_SITE_ATC_FARE_DRIVEN=TRUE" + "&SO_SITE_ATC_SCHEDULE_DRIVEN=FALSE" + "&WDS_PAY_NOW=TRUE"
                    + "&WDS_CANCEL_PNR=TRUE" + "&SO_SITE_ALLOW_PNR_CANCEL=Y" + "&SO_SITE_ALLOW_ON_HOLD_CANCEL=TRUE"
                    + "&SO_SITE_ALLOW_TKT_PNR_CANCEL=Y";

            midpostMethod.setRequestEntity(new StringRequestEntity(midpostBody, "application/x-www-form-urlencoded",
                    null));
            midpostMethod
                    .addRequestHeader(
                            "Referer",
                            "http://www.saudiairlines.com/portal/site/saudiairlines/menuitem.aeabf36fd343b335173ff63dc8f034a0/?vgnextoid=82aae1cb93e70110VgnVCM1000008c0f430aRCRD");
            httpClient.executeMethod(midpostMethod);
            String midResult = midpostMethod.getResponseBodyAsString();// 将midPost的内容post给服务器取出Post需要的PostData

            String E_LOCATION_1 = param.getArr();// 取出Post中的变量，并且将其格式转化成需要Post需要的格式

            String ArrivalAirport = get(E_LOCATION_1, midResult);

            return ArrivalAirport;

        } catch (Exception e) {
            return "Exception";
        } finally {

            if (midpostMethod != null) {
                midpostMethod.releaseConnection();
            }
        }

    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {
        BookingResult booking = new BookingResult();
        BookingInfo info = new BookingInfo();
        booking.setRet(true);
        info.setMethod("get");
        //info.setAction("http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");
        info.setAction("http://www.bookonline.saudiairlines.com");
        String dep = param.getDep();
        String arr = param.getArr();
        String date = param.getDepDate();

        String Mid_B_DATE_1 = date.replaceAll("-", "") + "0000";// 将取出的时间串格式化与Post中的格式一致。

        String[] Split_Date1 = date.split("-");
        String Field_Date1 = Split_Date1[2] + "/" + Split_Date1[1] + "/" + Split_Date1[0];

        java.util.Map<java.lang.String, java.lang.String> inputs = new HashMap<String, String>();
        inputs.put("TRIP_TYPE", "0");
        inputs.put("B_LOCATION_auto", "Riyadh");
        inputs.put("B_LOCATION", param.getDep());
        inputs.put("E_LOCATION_auto", "Beijing");
        inputs.put("E_LOCATION", param.getArr());
        inputs.put("textfield2", Field_Date1);
        inputs.put("DATE_1", Mid_B_DATE_1);
        inputs.put("DATE_2", "");
        inputs.put("adults", "1");
        inputs.put("TRAVELLER_TYPE_1", "ADT");
        inputs.put("children", "0");
        inputs.put("infants", "0");
        inputs.put("CABIN", "E");
        inputs.put("EMBEDDED_TRANSACTION", "AirSearch");
        inputs.put("LANGUAGE", "GB");
        inputs.put("SITE", "BDXHBDXH");
        inputs.put("TRIP_FLOW", "YES");
        inputs.put("BOOKING_FLOW", "REVENUE");
        inputs.put("SHOW_MULTIDEST", "FALSE");
        inputs.put("IS_FLEXIBLE", "FALSE");
        inputs.put("WDS_SERVICING", "TRUE");
        inputs.put("WDS_FLIGHTSTATUS", "TRUE");
        inputs.put("WDS_ETKT_RETRIEVE", "TRUE");
        inputs.put("WDS_AAAS", "TRUE");
        inputs.put("WDS_XBAG", "TRUE");
        inputs.put("WDS_UPGR", "FALSE");
        inputs.put("WDS_ATC_REBOOK", "FALSE");
        inputs.put("WDS_XHTML_DIRECT_ACCESS", "FALSE");
        inputs.put("WDS_ATC_REFUND", "TRUE");
        inputs.put("SO_SITE_ALLOW_ATC_REFUND", "TRUE");
        inputs.put("SO_SITE_ATC_ALLOW_OFFLINE", "TRUE");
        inputs.put("SO_SITE_ATC_FARE_DRIVEN", "TRUE");
        inputs.put("SO_SITE_ATC_SCHEDULE_DRIVEN", "FALSE");
        inputs.put("WDS_PAY_NOW", "TRUE");
        inputs.put("WDS_CANCEL_PNR", "TRUE");
        inputs.put("SO_SITE_ALLOW_PNR_CANCEL", "Y");
        inputs.put("SO_SITE_ALLOW_ON_HOLD_CANCEL", "TRUE");
        inputs.put("SO_SITE_ALLOW_TKT_PNR_CANCEL", "Y");
        //info.setInputs(inputs);
        booking.setData(info);
        return booking;
    }

    class SaudFlightD {
        private String flightId;
        private double price;
        private double retailPrice;
        private String planeType;
        private String departureTime;
        private String arrivalTime;
        private String CurrencyCode;
        private String code;
        private double tax;
        private String departureAirport;
        private String arrivalAirport;
        private String transferAirport;

        public SaudFlightD() {
        }

        public SaudFlightD(String flightId, double retailPrice, String planeType, String departureTime,
                           String arrivalTime, String currencyCode, String code, double tax, String departureAirport,
                           String arrivalAirport, double price) {
            super();
            this.flightId = flightId;
            this.retailPrice = retailPrice;
            this.planeType = planeType;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.CurrencyCode = currencyCode;
            this.code = code;
            this.tax = tax;
            this.departureAirport = departureAirport;
            this.arrivalAirport = arrivalAirport;
            this.price = price;
        }

        public String getTransferAirport() {
            return transferAirport;
        }

        public void setTransferAirport(String transferAirport) {
            this.transferAirport = transferAirport;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getFlightId() {
            return flightId;
        }

        public void setFlightId(String flightId) {
            this.flightId = flightId;
        }

        public double getRetailPrice() {
            return retailPrice;
        }

        public void setRetailPrice(double retailPrice) {
            this.retailPrice = retailPrice;
        }

        public String getPlaneType() {
            return planeType;
        }

        public void setPlaneType(String planeType) {
            this.planeType = planeType;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public String getCurrencyCode() {
            return CurrencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            CurrencyCode = currencyCode;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public double getTax() {
            return tax;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public String getDepartureAirport() {
            return departureAirport;
        }

        public void setDepartureAirport(String departureAirport) {
            this.departureAirport = departureAirport;
        }

        public String getArrivalAirport() {
            return arrivalAirport;
        }

        public void setArrivalAirport(String arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
        }

        public int hashCode() {
            return flightId.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof SaudFlightD) {
                SaudFlightD f = (SaudFlightD) o;
                if (f.getFlightId().equals(this.flightId))
                    return true;
            }
            return false;
        }
    }

}
