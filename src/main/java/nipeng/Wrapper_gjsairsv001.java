package nipeng;

import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFPostMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peng.ni on 14-7-3.
 */
public class Wrapper_gjsairsv001 implements QunarCrawler {

//    private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
    private static final String CODEBASE = "gjsairsv001";
    private String bookingUrl;

    @Override
    public String getHtml(FlightSearchParam param) {
        String dep = param.getDep();
        String arr = param.getArr();

        QFHttpClient httpClient = new QFHttpClient(param, false);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        QFPostMethod midpostMethod = new QFPostMethod(
                "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");//new一个跳转的post

//		PostMethod postMethod = new DefaultPostMethod("http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action;jsessionid=yFHrSHMJ0PG2PSvpHGGWys9YpjW5QHVgV06h8vTvTpmLQN1mJs1W!-1259699195!2031870957");//new一个最终的Post
        QFPostMethod postMethod = null;


        try {



            String Mid_B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";
            String Mid_E_DATE_1 = param.getRetDate().replaceAll("-", "") + "0000";//将取出的时间串格式化与Post中的格式一致。

            String[] Split_Date1=param.getDepDate().split("-");
            String  Field_Date1=Split_Date1[2]+"/"+Split_Date1[1]+"/"+Split_Date1[0];
            String[] Split_Date2=param.getRetDate().split("-");
            String  Field_Date2=Split_Date2[2]+"/"+Split_Date2[1]+"/"+Split_Date2[0];





            String midpostBody = String.format(
                    "&TRIP_TYPE=R" +
                            "&B_LOCATION_auto=" +
                            "&B_LOCATION=" +dep+
                            "&E_LOCATION_auto=" +
                            "&E_LOCATION=" +arr+
                            "&textfield2=" +Field_Date1+
                            "&DATE_1=" +Mid_B_DATE_1+
                            "&textfield2=" +Field_Date2+
                            "&DATE_2=" +Mid_E_DATE_1+
                            "&adults=1" +
                            "&TRAVELLER_TYPE_1=ADT" +
                            "&children=0" +
                            "&infants=0" +
                            "&CABIN=E" +
                            "&EMBEDDED_TRANSACTION=AirSearch" +
                            "&LANGUAGE=GB" +
                            "&SITE=BDXHBDXH" +
                            "&TRIP_FLOW=YES" +
                            "&BOOKING_FLOW=REVENUE" +
                            "&SHOW_MULTIDEST=FALSE" +
                            "&IS_FLEXIBLE=FALSE" +
                            "&WDS_SERVICING=TRUE" +
                            "&WDS_TIMETABLE=TRUE" +
                            "&WDS_FLIGHTSTATUS=TRUE" +
                            "&WDS_ETKT_RETRIEVE=TRUE" +
                            "&WDS_AAAS=TRUE" +
                            "&WDS_XBAG=TRUE" +
                            "&WDS_UPGR=FALSE" +
                            "&WDS_ATC_REBOOK=FALSE" +
                            "&WDS_XHTML_DIRECT_ACCESS=FALSE" +
                            "&WDS_ATC_REFUND=TRUE" +
                            "&SO_SITE_ALLOW_ATC_REFUND=TRUE" +
                            "&SO_SITE_ATC_ALLOW_OFFLINE=TRUE" +
                            "&SO_SITE_ATC_FARE_DRIVEN=TRUE" +
                            "&SO_SITE_ATC_SCHEDULE_DRIVEN=FALSE" +
                            "&WDS_PAY_NOW=TRUE" +
                            "&WDS_CANCEL_PNR=TRUE" +
                            "&SO_SITE_ALLOW_PNR_CANCEL=Y" +
                            "&SO_SITE_ALLOW_ON_HOLD_CANCEL=TRUE" +
                            "&SO_SITE_ALLOW_TKT_PNR_CANCEL=Y&WDS_JOIN_ALFURSAN=TRUE");

            // TODO compose your connection string

            midpostMethod.setRequestEntity(new StringRequestEntity(midpostBody, "application/x-www-form-urlencoded",
                    null));
            midpostMethod.addRequestHeader("Referer", "http://www.saudiairlines.com/portal/site/saudiairlines/menuitem.aeabf36fd343b335173ff63dc8f034a0/?vgnextoid=82aae1cb93e70110VgnVCM1000008c0f430aRCRD");
            httpClient.executeMethod(midpostMethod);
            String midResult = midpostMethod.getResponseBodyAsString();//将midPost的内容post给服务器取出Post需要的PostData


            String B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";
            String B_DATE_2 = param.getRetDate().replaceAll("-", "") + "0000";
            String B_LOCATION_1=param.getDep();
            String E_LOCATION_1=param.getArr();//取出Post中的变量，并且将其格式转化成需要Post需要的格式

            String WDS_B_LOCATION_1_DISPLAY="";//getFullName(B_LOCATION_1,midResult);
            String WDS_E_LOCATION_1_DISPLAY="";//getFullName(E_LOCATION_1,midResult);

            String action=getValue(midResult, "<form id=\"flightDetailsFrame\" name=\"SEARCH_FLIGHT_FORM\" target=\"_blank\" method=\"post\" action=\"", "\" onSubmit=\"return false;\" >");

			/*String[] SO_SITE_OFFICE_ID_Split=StringUtils.getValues(midResult, "type=\"hidden\" name=\"SO_SITE_OFFICE_ID\" value=\"","\" />");

			String SO_SITE_OFFICE_ID=SO_SITE_OFFICE_ID_Split[3];*/
            String officeId = "";
            String javascript = getValue(midResult, "LOCATIONS = {", "};").trim();
            javascript = getValue(javascript, "\"" + dep + "\" : {", "\"domestic\" :");
            officeId = getValue(javascript, "\"GLOBAL\" : \"", "\"},").trim();
            if ("".equals(officeId)){
                officeId = "WASSV08AA";
            }


            String postBody = "&TRIP_TYPE=R" +
                    "&LANGUAGE=GB" +
                    "&SITE=BDXHBDXH" +
                    "&EMBEDDED_TRANSACTION=FlexPricerAvailability" +
                    "&EXTERNAL_ID=dummy" +
                    "&B_DATE_1=" +B_DATE_1+
                    "&B_DATE_2=" +B_DATE_2+
                    "&PRICING_TYPE=O" +
                    "&DISPLAY_TYPE=1" +
                    "&COMMERCIAL_FARE_FAMILY_1=CFFGUEST" +
                    "&COMMERCIAL_FARE_FAMILY_2=CFFBUS" +
                    "&COMMERCIAL_FARE_FAMILY_3=CFFFIRST" +
                    "&B_LOCATION_1=" +B_LOCATION_1+
                    "&E_LOCATION_1=" +E_LOCATION_1+
                    "&B_LOCATION_2=" +E_LOCATION_1+
                    "&E_LOCATION_2=" +B_LOCATION_1+
                    "&WDS_TABS=online" +
                    "&SO_SITE_OFFICE_ID=" +officeId+
                    "&TRAVELLER_TYPE_1=ADT" +
                    "&REFRESH=0" +
                    "&WDS_B_LOCATION_1_DISPLAY=" +WDS_B_LOCATION_1_DISPLAY+
                    "&WDS_E_LOCATION_1_DISPLAY=" +WDS_E_LOCATION_1_DISPLAY+
                    "&CABIN=E" +
                    "&WDS_NB_ADT=1" +
                    "&WDS_NB_CHD=0" +
                    "&WDS_NB_INF=0";



            postMethod = new QFPostMethod("http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/"+action);//new一个最终的Post

            postMethod.setRequestEntity(new StringRequestEntity(postBody, "application/x-www-form-urlencoded",
                    null));

            postMethod.addRequestHeader("Referer", "http://www.bookonline.saudiairlines.com/pl/SaudiAirlines/wds/Override.action");
            httpClient.executeMethod(postMethod);

            String Result=postMethod.getResponseBodyAsString();			//类同与MidPost过程，将postBody通过PastMethod方法给浏览器，最后取回Result

            return Result;



        } catch (Exception e) {
//            if (!e.getMessage().equals("Connection refused: connect"))
//            logger.error("ErrorIn " + CODEBASE, e);
        } finally {
            if (postMethod != null) {
                ((HttpMethodBase) postMethod).releaseConnection();
            }
            if (midpostMethod != null) {
                midpostMethod.releaseConnection();
            }
        }
        return "Exception";
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam param) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<RoundTripFlightInfo> data = Lists.newArrayList();

        if ("Exception".equals(html)) {
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            return processResultInfo;
        }

        if(!html.contains("html")){
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.NO_RESULT);
            return processResultInfo;
        }


        String currencystr = getValue(html, "<p class=\"note\">", "</p>");
        String currency =getValue(currencystr, "<strong>", "</strong>").trim();
        String outInfo = getValue(html, "<tbody id=\"b0FlightsTBodyE", "</tbody>");
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//        System.out.println(outInfo);
        List<Flight> outFlight = getFlightList(outInfo, param);
        setPriceToFlight(outFlight, html);
        String inInfo = getValue(html, "<tbody id=\"b1FlightsTBodyE", "</tbody>");
        List<Flight> inFlight = getFlightList(inInfo, param);
        setPriceToFlight(inFlight, html);

        for(Flight out : outFlight){  //去程
            for(Flight in : inFlight){  //返程
                RoundTripFlightInfo roundTripFlightInfo=new RoundTripFlightInfo();
                roundTripFlightInfo.setOutboundPrice(out.price);
                roundTripFlightInfo.setReturnedPrice(in.price);
                List<FlightSegement> info=getSegement(out,param,0);
                List<FlightSegement> retinfo=getSegement(in,param,1);
                List<String> retflightno= Lists.newArrayList();
                List<String> flightNo= Lists.newArrayList();
                for(FlightSegement fs:retinfo)
                {
                    retflightno.add(fs.getFlightno());
                }
                for(FlightSegement fs:info)
                {
                    flightNo.add(fs.getFlightno());
                }
               SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
               Date retdepdate=null;
                Date depDate=null;
                try {
                    retdepdate=sdf.parse(param.getRetDate());
                    depDate=sdf.parse(param.getDepDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                FlightDetail detail=new FlightDetail();
                detail.setDepcity(param.getDep());
                detail.setArrcity(param.getArr());
                detail.setWrapperid(CODEBASE);
                detail.setDepdate(depDate);
                detail.setPrice(roundTripFlightInfo.getOutboundPrice() + roundTripFlightInfo.getReturnedPrice());
                detail.setTax(0.0);
                detail.setFlightno(flightNo);
                detail.setMonetaryunit(currency);
                roundTripFlightInfo.setDetail(detail);

                roundTripFlightInfo.setRetdepdate(retdepdate);
                roundTripFlightInfo.setRetinfo(retinfo);
                roundTripFlightInfo.setRetflightno(retflightno);
                roundTripFlightInfo.setInfo(info);
                data.add(roundTripFlightInfo);

            }
        }
//        return builder.append("</table>").toString();

        processResultInfo.setData(data);
        processResultInfo.setStatus(Constants.SUCCESS);
        processResultInfo.setRet(true);
        return processResultInfo;
    }



    private List<FlightSegement> getSegement(Flight flight,FlightSearchParam param,int tag)
    {
        List<FlightSegement> segements= Lists.newArrayList();
        for(int i=0;i<flight.dept.size();i++)
        {
            FlightSegement flightSegement=new FlightSegement();
            flightSegement.setDepairport(flight.dep.get(i));
            flightSegement.setArrairport(flight.arr.get(i));
            flightSegement.setFlightno(flight.code.get(i));
            flightSegement.setDeptime(flight.dept.get(i));
            flightSegement.setArrtime(flight.arrt.get(i));
            if(i==0&&tag==0)
            {
                flightSegement.setDepDate(param.getDepDate());
            }
            if(i==0&&tag==1)
            {
                flightSegement.setDepDate(param.getRetDate());
            }
            else{
                flightSegement.setDepDate("1970-01-01");
                flightSegement.setArrDate("1970-01-01");
            }
            flightSegement.setCompany(flight.code.get(i).substring(0,3));
            segements.add(flightSegement);
        }
        return segements;
    }



    private List<Flight> getFlightList(String info, FlightSearchParam param){

        List<Flight> list = new ArrayList<Flight>();
        String[] flights = info.split("class=\"flight flight\\d+(.*) first");
        for(int i = 1; i < flights.length; i++){
            List<String> code= Lists.newArrayList();
            List<String> dept= Lists.newArrayList();
            List<String> dep= Lists.newArrayList();
            List<String> arr= Lists.newArrayList();
            List<String> arrt= Lists.newArrayList();

            String str = flights[i];
            String[] firstime = getValues(str, "<th class=\"first time\">", "</th>");
            String[] lasttimes =getValues(str, "<th class=\"time\">", "</th>");
            String[] codes = getValues(str, "onclick=\"theOcupForm.popFLIF(", ");");
            String[] priceInfos = getValues(str, "theOcupForm.selectFlightOption(", ")\">");
            List<String> prices = Arrays.asList(priceInfos);
            for(int j=0;j<firstime.length;j++)
            {
                String deptime = getValue(firstime[j], "\\d{2}:\\d{2}");
                String arrtime = getValue(lasttimes[j], "\\d{2}:\\d{2}");
                dept.add(deptime);
                arrt.add(arrtime);
                String[] codestr = codes[j].split("','");
                dep.add(codestr[1]);
                arr.add(codestr[2]);
                code.add(codestr[3]+codestr[4]);

            }

//     //

//
//            String flightNumber = "";
//            for(String code : codes){
//                String[] codestr = code.split("','");
//                if (org.apache.commons.lang.StringUtils.isEmpty(flightNumber)){
//                    flightNumber = codestr[3] + codestr[4];
//                }else {
//                    flightNumber += "/" + codestr[3] + codestr[4];
//                }
//            }
            Flight flight = new Flight();
            flight.dept = dept;
            flight.dep =dep;
            flight.arrt = arrt;
            flight.arr = arr;
            flight.code = code;
            flight.planeType = "0";
            flight.priceInfos = prices;
            list.add(flight);
        }
        return list;
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

    public String getValue(String source, String st, String end) {
        int a = source.indexOf(st);
        if (a == -1)
            return "";
        int b = source.indexOf(end, a + st.length());
        if (b == -1)
            return "";
        return source.substring(a + st.length(), b);
    }

    private void setPriceToFlight(List<Flight> flights, String html){
        String fareInfo = getValue(html, "OcupForm.prototype.initFlights", "OcupForm.prototype.initFFDescriptions");
        String[] fareInfos = getValues(fareInfo, "this.createOwdFlightOption", ");");
        Map<String, Double> priceMap = new HashMap<String, Double>();
        for (String str : fareInfos){
            //System.out.println(str.replaceAll("\\\"", ""));
            String[] strs = str.replaceAll("\\\"", "").split(",");
            String key = strs[0].replaceAll("\\(", "").trim() + "_" + strs[1].trim() + "_" + strs[3].trim();
            double price = Double.valueOf(strs[5].trim());
            if (priceMap.containsKey(key)){
                double oldPrice = priceMap.get(key);
                if (price < oldPrice){
                    priceMap.put(key, price);
                }
            }else {
                priceMap.put(key, price);
            }
        }

        for(Flight flight : flights){
            double lowPrice = 0;
            for (String info : flight.priceInfos){
                String[] infos = info.replaceAll("'", "").split(",");
                String priceKey = infos[0].trim() + "_" + infos[1].trim() + "_" + infos[2].trim();
                if (priceMap.containsKey(priceKey)){
                    double price = priceMap.get(priceKey);
                    if (0 == lowPrice || price < lowPrice){
                        lowPrice = price;
                    }
                }
            }
            flight.price = lowPrice;
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

        String Mid_B_DATE_1 = param.getDepDate().replaceAll("-", "") + "0000";
        String Mid_E_DATE_1 = param.getRetDate().replaceAll("-", "") + "0000";//将取出的时间串格式化与Post中的格式一致。

        String[] Split_Date1=param.getDepDate().split("-");
        String  Field_Date1=Split_Date1[2]+"/"+Split_Date1[1]+"/"+Split_Date1[0];
        String[] Split_Date2=param.getRetDate().split("-");
        String  Field_Date2=Split_Date2[2]+"/"+Split_Date2[1]+"/"+Split_Date2[0];

        java.util.Map<java.lang.String, java.lang.String> inputs = new HashMap<String, String>();
        inputs.put("TRIP_TYPE", "R");
        inputs.put("B_LOCATION_auto", "Riyadh");
        inputs.put("B_LOCATION", param.getDep());
        inputs.put("E_LOCATION_auto", "Beijing");
        inputs.put("E_LOCATION", param.getArr());
        inputs.put("textfield2", Field_Date1);
        inputs.put("DATE_1", Mid_B_DATE_1);
        inputs.put("textfield2", Field_Date2);
        inputs.put("DATE_2", Mid_E_DATE_1);
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
    public static void main(String[] args)
    {
        FlightSearchParam f = new FlightSearchParam();
        f.setDep("BJS");
        f.setArr("RUH");
        f.setDepDate("2014-08-22");
        f.setRetDate("2014-10-30");
        Wrapper_gjsairsv001 wr = new Wrapper_gjsairsv001();
        String sw = wr.getHtml(f);
//        System.out.println(sw);
        wr.process(sw,f);
//        ProcessResultInfo pri = wr.process(sw, f);
    }




class Flight{
    List<String> code;
    List<String> dept;
    List<String> dep;
    List<String> arr;
    List<String> arrt;
    String planeType;
    double price;
    double tax;
    List<String> priceInfos;
}

}
