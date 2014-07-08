package kangchao;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

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
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
/**
 * Initial Created by kangchao at 14-6-8
 * <p/>
 * Description:
 */
public class Wrapper_gjd10031910 implements QunarCrawler {
    private static final String CODEBASE = "gjd10031910";
    public static void main(String[] args) {
        Wrapper_gjd10031910 p = new Wrapper_gjd10031910();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("YYZ");
        flightSearchParam.setArr("PVG");
        flightSearchParam.setDepDate("2014-07-25");
        String html = p.getHtml(flightSearchParam);
        //System.out.println(".."+html);
        p.process(html, flightSearchParam);
    }


    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String date = depDate.replaceAll("-", "");

        QFHttpClient httpClient = new QFHttpClient(flightSearchParam, true);
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);

        String postUrl="http://book.aircanada.com/pl/AConline/en/OverrideServlet";
        QFPostMethod post = new QFPostMethod(postUrl);
        try {
            NameValuePair data[]={
                    new NameValuePair("EMBEDDED_TRANSACTION","FlexPricerAvailabilityServlet"),
                    new NameValuePair("SO_SITE_POINT_OF_SALE","PEK"),
                    new NameValuePair("SO_SITE_POINT_OF_TICKETING","PEK"),
                    new NameValuePair("SO_SITE_COUNTRY_OF_RESIDENCE","CN"),
                    new NameValuePair("LANGUAGE","ZH"),
                    new NameValuePair("COUNTRY","CN"),
                    new NameValuePair("SITE","SAADSAAD"),
                    new NameValuePair("PRIVATE_LABEL","ACO_AC_AIR"),
                    new NameValuePair("USERID","GUEST"),
                    new NameValuePair("EXTERNAL_ID","GUEST"),
                    new NameValuePair("TITLE",""),
                    new NameValuePair("FNAME",""),
                    new NameValuePair("MNAME",""),
                    new NameValuePair("LNAME",""),
                    new NameValuePair("FFMILES",""),
                    new NameValuePair("B_DATE_1",date+"0000"),
                    new NameValuePair("B_DATE_2",""),
                    new NameValuePair("B_ANY_TIME_1","TRUE"),
                    new NameValuePair("B_ANY_TIME_2","TRUE"),
                    new NameValuePair("B_LOCATION_1",dep),
                    new NameValuePair("E_LOCATION_1",arr),
                    new NameValuePair("B_LOCATION_2",""),
                    new NameValuePair("E_LOCATION_2",""),
                    new NameValuePair("DATE_RANGE_QUALIFIER_1","C"),
                    new NameValuePair("DATE_RANGE_QUALIFIER_2",""),
                    new NameValuePair("DATE_RANGE_VALUE_1","0"),
                    new NameValuePair("DATE_RANGE_VALUE_2","0"),
                    new NameValuePair("NTP_AUTHORIZATION","true"),
                    new NameValuePair("TRIP_FLOW","YES"),
                    new NameValuePair("BOOKING_FLOW","INTERNATIONAL"),
                    new NameValuePair("MARKET","CN"),
                    new NameValuePair("SO_SITE_FP_WITHHOLD_SURCHARG","TRUE"),
                    new NameValuePair("SO_SITE_EDITION",""),
                    new NameValuePair("TRAVELLER_TYPE_1","ADT"),
                    new NameValuePair("TRAVELLER_TYPE_2",""),
                    new NameValuePair("TRAVELLER_TYPE_3",""),
                    new NameValuePair("TRAVELLER_TYPE_4",""),
                    new NameValuePair("TRAVELLER_TYPE_5",""),
                    new NameValuePair("TRAVELLER_TYPE_6",""),
                    new NameValuePair("TRAVELLER_TYPE_7",""),
                    new NameValuePair("TRAVELLER_TYPE_8",""),
                    new NameValuePair("TRAVELLER_TYPE_9",""),
                    new NameValuePair("IS_YOUTH_1","FALSE"),
                    new NameValuePair("IS_YOUTH_2",""),
                    new NameValuePair("IS_YOUTH_3",""),
                    new NameValuePair("IS_YOUTH_4",""),
                    new NameValuePair("IS_YOUTH_5",""),
                    new NameValuePair("IS_YOUTH_6",""),
                    new NameValuePair("IS_YOUTH_7",""),
                    new NameValuePair("IS_YOUTH_8",""),
                    new NameValuePair("IS_YOUTH_9",""),
                    new NameValuePair("HAS_INFANT_1","FALSE"),
                    new NameValuePair("HAS_INFANT_2",""),
                    new NameValuePair("HAS_INFANT_3",""),
                    new NameValuePair("HAS_INFANT_4",""),
                    new NameValuePair("HAS_INFANT_5",""),
                    new NameValuePair("HAS_INFANT_6",""),
                    new NameValuePair("HAS_INFANT_7",""),
                    new NameValuePair("HAS_INFANT_8",""),
                    new NameValuePair("HAS_INFANT_9",""),
                    new NameValuePair("TRIP_TYPE","O"),
                    new NameValuePair("SEVEN_DAY_SEARCH","FALSE"),
                    new NameValuePair("DIRECT_NON_STOP","FALSE"),
                    new NameValuePair("PRICING_TYPE","I"),
                    new NameValuePair("DISPLAY_TYPE","2"),
                    new NameValuePair("COMMERCIAL_FARE_FAMILY_1","NBMOW"),
                    new NameValuePair("COMMERCIAL_FARE_FAMILY_2",""),
                    new NameValuePair("COMMERCIAL_FARE_FAMILY_3",""),
                    new NameValuePair("CORPORATE_NUMBER_1",""),
                    new NameValuePair("CORPORATE_NUMBER_2",""),
                    new NameValuePair("TYPE_OF_CORPORATE_FARE",""),
                    new NameValuePair("ARRANGE_BY","N"),
                    new NameValuePair("AUTHORIZATION_ID",""),
                    new NameValuePair("TRAVEL_AGENT_PNR",""),
                    new NameValuePair("IATA_AGENT_ID_NUMBER",""),
                    new NameValuePair("IATA_AGENT_LAST_NAME",""),
                    new NameValuePair("IATA_AGENT_FIRST_NAME",""),
                    new NameValuePair("SO_SITE_REST_AIRLINES_LST","AC;CA"),
                    new NameValuePair("CERTIFICATE_NUMBER",""),
                    new NameValuePair("SELECTED_TIER_STATUS",""),
                    new NameValuePair("CUSTOM_AUTH_ID",""),
                    new NameValuePair("searchType","")
            };
            post.setRequestBody(data);
            httpClient.executeMethod(post);
            String body = post.getResponseBodyAsString();
            return body;
        } catch (Exception e) {
            return "Exception";
        } finally{
            if(post!=null){
                post.releaseConnection();
            }
        }
    }


    public ProcessResultInfo process(String html, FlightSearchParam param) {
        ProcessResultInfo result = new ProcessResultInfo();
        List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
        if ("Exception".equals(html)) {
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        if (html.startsWith("StatusError")) {
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }

        try{
            String[] flightlist = html.split("<td class='borderTop OpBy'>");
            for(int i=1;i<flightlist.length;i++){

                if(i==flightlist.length-1){
                    flightlist[i]= StringUtils.substringBefore(flightlist[i], "</tbody>");
                }
                OneWayFlightInfo oneFlight = getOneFlight(flightlist[i],param);
                if(oneFlight.getInfo().size() > 0){
                    flightList.add(oneFlight);
                }

            }
            result.setData(flightList);
            result.setStatus(Constants.SUCCESS);
            return result;

        } catch (Exception e) {
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }

    }



    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        String bookingUrlPre = "http://book.aircanada.com/pl/AConline/en/OverrideServlet";
        BookingResult result = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction(bookingUrlPre);
        bookingInfo.setMethod("post");
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String date = depDate.replaceAll("-", "");
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("EMBEDDED_TRANSACTION","FlexPricerAvailabilityServlet");
        map.put("SO_SITE_POINT_OF_SALE","PEK");
        map.put("SO_SITE_POINT_OF_TICKETING","PEK");
        map.put("SO_SITE_COUNTRY_OF_RESIDENCE","CN");
        map.put("LANGUAGE","ZH");
        map.put("COUNTRY","CN");
        map.put("SITE","SAADSAAD");
        map.put("PRIVATE_LABEL","ACO_AC_AIR");
        map.put("USERID","GUEST");
        map.put("EXTERNAL_ID","GUEST");
        map.put("TITLE","");
        map.put("FNAME","");
        map.put("MNAME","");
        map.put("LNAME","");
        map.put("FFMILES","");
        map.put("B_DATE_1",date+"0000");
        map.put("B_DATE_2","");
        map.put("B_ANY_TIME_1","TRUE");
        map.put("B_ANY_TIME_2","TRUE");
        map.put("B_LOCATION_1",dep);
        map.put("E_LOCATION_1",arr);
        map.put("B_LOCATION_2","");
        map.put("E_LOCATION_2","");
        map.put("DATE_RANGE_QUALIFIER_1","C");
        map.put("DATE_RANGE_QUALIFIER_2","");
        map.put("DATE_RANGE_VALUE_1","0");
        map.put("DATE_RANGE_VALUE_2","0");
        map.put("NTP_AUTHORIZATION","true");
        map.put("TRIP_FLOW","YES");
        map.put("BOOKING_FLOW","INTERNATIONAL");
        map.put("MARKET","CN");
        map.put("SO_SITE_FP_WITHHOLD_SURCHARG","TRUE");
        map.put("SO_SITE_EDITION","");
        map.put("TRAVELLER_TYPE_1","ADT");
        map.put("TRAVELLER_TYPE_2","");
        map.put("TRAVELLER_TYPE_3","");
        map.put("TRAVELLER_TYPE_4","");
        map.put("TRAVELLER_TYPE_5","");
        map.put("TRAVELLER_TYPE_6","");
        map.put("TRAVELLER_TYPE_7","");
        map.put("TRAVELLER_TYPE_8","");
        map.put("TRAVELLER_TYPE_9","");
        map.put("IS_YOUTH_1","FALSE");
        map.put("IS_YOUTH_2","");
        map.put("IS_YOUTH_3","");
        map.put("IS_YOUTH_4","");
        map.put("IS_YOUTH_5","");
        map.put("IS_YOUTH_6","");
        new NameValuePair("IS_YOUTH_7","");
        map.put("IS_YOUTH_8","");
        map.put("IS_YOUTH_9","");
        map.put("HAS_INFANT_1","FALSE");
        map.put("HAS_INFANT_2","");
        map.put("HAS_INFANT_3","");
        map.put("HAS_INFANT_4","");
        map.put("HAS_INFANT_5","");
        map.put("HAS_INFANT_6","");
        map.put("HAS_INFANT_7","");
        map.put("HAS_INFANT_8","");
        map.put("HAS_INFANT_9","");
        map.put("TRIP_TYPE","O");
        map.put("SEVEN_DAY_SEARCH","FALSE");
        map.put("DIRECT_NON_STOP","FALSE");
        map.put("PRICING_TYPE","I");
        map.put("DISPLAY_TYPE","2");
        map.put("COMMERCIAL_FARE_FAMILY_1","NBMOW");
        map.put("COMMERCIAL_FARE_FAMILY_2","");
        map.put("COMMERCIAL_FARE_FAMILY_3","");
        map.put("CORPORATE_NUMBER_1","");
        map.put("CORPORATE_NUMBER_2","");
        map.put("TYPE_OF_CORPORATE_FARE","");
        map.put("ARRANGE_BY","N");
        map.put("AUTHORIZATION_ID","");
        map.put("TRAVEL_AGENT_PNR","");
        map.put("IATA_AGENT_ID_NUMBER","");
        map.put("IATA_AGENT_LAST_NAME","");
        map.put("IATA_AGENT_FIRST_NAME","");
        map.put("SO_SITE_REST_AIRLINES_LST","AC;CA");
        map.put("CERTIFICATE_NUMBER","");
        map.put("SELECTED_TIER_STATUS","");
        map.put("CUSTOM_AUTH_ID","");
        map.put("searchType","");
        bookingInfo.setInputs(map);
        result.setData(bookingInfo);
        result.setRet(true);
        return result;
    }



    private OneWayFlightInfo getOneFlight(String flightlist,FlightSearchParam param) {
        OneWayFlightInfo oneFlight=new OneWayFlightInfo();
        List<FlightSegement> segs = new ArrayList<FlightSegement>();
        FlightDetail flightDetail = new FlightDetail();
        try{
            String[] segList = flightlist.split("<td class=' OpBy'>");

            List<String> flightnolist = Lists.newArrayList();
            String[] airportList = new String[5];
            airportList[0] = param.getDep();
            airportList[segList.length] = param.getArr();
            for(int j=1;j<segList.length;j++){
                String segStr = segList[j-1];
                airportList[j] = StringUtils.substringBetween(StringUtils.substringBetween(segStr, "<span class=\"connx\"", "</td>"), "</br>", "</span>").substring(1, 4).trim();
            }
            System.out.println(segList.length);
            for(int i=0;i<segList.length;i++){
                FlightSegement seg =  new FlightSegement();
                String segStr = segList[i];
                String dep = airportList[i];
                String arr = airportList[i+1];
                String flightno = StringUtils.substringBetween(segStr, "flightNameCSS floatleft\">", "<!--//-->").trim();
                String deptime = getValue(segStr,"\\d\\d:\\d\\d",1);
                String arrtime = getValue(segStr,"\\d\\d:\\d\\d",2);
                String plane=null;
                if(segStr.contains("'700' ,'330')\">")){
                    plane=StringUtils.substringBetween(segStr, "'700' ,'330')\">","</a>").trim();
                }
                else{
                    plane=StringUtils.substringBetween(segStr, "tdXmAircraft'> <div>","</div>").trim();
                }

                String depdate = param.getDepDate();
                String updepStr = StringUtils.substringBetween(segStr, "<span style=\"white-space:nowrap;\">", "</span>");

                if(updepStr!=null){
                    String monthCH = StringUtils.substringBetween(updepStr, "title=", ">");
                    String dayCH = StringUtils.substringBetween(updepStr, "(", "-");
                    String month = monthMap.get(monthCH);
                    if(dayCH.length()==1){
                        dayCH = "0"+dayCH;
                    }
                    if("01".equals(month)&&"01".equals(dayCH)){
                        int year = Integer.parseInt(depdate.substring(0, 4))+1;
                        depdate = Integer.toString(year) + "-" +  month + "-" +dayCH;
                    }
                    else{
                        depdate = depdate.substring(0,4)+ "-" +  month + "-" +dayCH;
                    }

                }
                String arrdate = depdate;
                String uparrStr = StringUtils.substringBetween(segStr, "/>+&nbsp;", "&nbsp;");
                if("1".equals(uparrStr)){
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(fmt.parse(depdate));
                    calendar.add(Calendar.DAY_OF_YEAR,1);
                    arrdate = fmt.format(calendar.getTime());
                }

                seg.setDepDate(depdate);
                seg.setArrDate(arrdate);
                seg.setDepairport(dep);
                seg.setArrairport(arr);
                seg.setFlightno(flightno);
                seg.setDeptime(deptime);
                seg.setArrtime(arrtime);
                seg.setAircraft(plane);
                segs.add(seg);
                flightnolist.add(flightno);

            }
            double totalprice = 0 ;
            String price[]=StringUtils.substringBetween(flightlist, "PieChartObject.updatePieInfo('0',",")").replaceAll("'","").split(",");

            for(int i=0;i<3;i++){
                totalprice+=Double.parseDouble(price[i]);
            }

            flightDetail.setDepcity(param.getDep());
            flightDetail.setArrcity(param.getArr());
            flightDetail.setDepdate(Date.valueOf(param.getDepDate()));
            flightDetail.setMonetaryunit("CNY");
            flightDetail.setPrice(totalprice);
            flightDetail.setFlightno(flightnolist);
            flightDetail.setWrapperid(CODEBASE);

            oneFlight.setDetail(flightDetail);
            oneFlight.setInfo(segs);
            return oneFlight;
        }catch(Exception e){
            //e.printStackTrace();

        }
        return oneFlight;
    }

    private static String getValue(String source, String regEx,int index) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        for(int i=1;i<index;i++){
            mm.find();
        }
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
    }
    private static Map<String,String> monthMap = new HashMap<String,String>(){
        {
            put("一月","01");
            put("二月","02");
            put("三月","03");
            put("四月","04");
            put("五月","05");
            put("六月","06");
            put("七月","07");
            put("八月","08");
            put("九月","09");
            put("十月","10");
            put("十一月","11");
            put("十二月","12");
        }
    };

}

