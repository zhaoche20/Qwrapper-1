package guoting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 *  User: ting.guo
 */
public class Wrapper_gjdairbt002 implements QunarCrawler {

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        Wrapper_gjdairbt002 wrapper_gjdairbt002 = new Wrapper_gjdairbt002();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("AAL");
        flightSearchParam.setArr("RIX");
        flightSearchParam.setDepDate("2014-07-12");
        flightSearchParam.setTimeOut("40000");
        String html = wrapper_gjdairbt002.getHtml(flightSearchParam);
        ProcessResultInfo info = wrapper_gjdairbt002.process(html, flightSearchParam);
        BookingResult bookingInfo=wrapper_gjdairbt002.getBookingInfo(flightSearchParam);
        //System.out.println(System.currentTimeMillis()-s);
        System.out.println(info);
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
        map.put("flt_leaving_on", flightSearchParam.getDepDate());
        map.put("legs","1");
        map.put("numadt","1");
        map.put("numchd", "0");
        map.put("numinf", "0");
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return  bookingResult;
    }

    public String getHtml(FlightSearchParam arg0) {

        // TODO Auto-generated method stub
        QFHttpClient httpClient = new QFHttpClient(arg0, false);
        QFGetMethod get = null;
        String depAirport = arg0.getDep();
        String arrAirport = arg0.getArr();
        String[] arg1 = arg0.getDepDate().split("-");
        String depDay = arg1[2];
        String depMonth = arg1[1] + "-" + arg1[0];
        String urlStr = "https://tickets1.airbaltic.com/app/fb.fly?sref=QNR&k=d41d8cd98f00b204e9800998ecf8427e&p=QNR&l=en&"
                + "action=avail&legs=1&origin="
                + depAirport
                + "&destin="
                + arrAirport
                + "&day0="
                + depDay
                + "&month0="
                + depMonth
                + "&numadt=1&numchd=0&numinf=0";
        get = new QFGetMethod(urlStr);
        try {
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
            }
            else if(html.indexOf("we are unable to offer flights on these exact dates")>-1)
            {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                return processResultInfo;
            }
            else if(html.indexOf("airBaltic does not operate flights on this route")>-1)
            {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                return processResultInfo;
            }

            // 将获取到的ＸＭＬ页面，分割，每段存放到arr数组里面
            List<OneWayFlightInfo> oneWayFlightInfos = getOneWayFlightInfos(html, flightSearchParam);
            processResultInfo.setData(oneWayFlightInfos);
            processResultInfo
                    .setStatus(oneWayFlightInfos.isEmpty() ? Constants.NO_RESULT
                            : Constants.SUCCESS);
            return processResultInfo;
        } catch (Exception e) {
            e.printStackTrace();
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }

    }

    private List<OneWayFlightInfo> getOneWayFlightInfos(String html,
                                                        FlightSearchParam flightSearchParam) {
        List<OneWayFlightInfo> oneWayFlightInfos = Lists.newArrayList();
        String[] oneWayFlightInfosArr =StringUtils.substringsBetween(html, "<offer ", "</offer>");
        //System.out.println(oneWayFlightInfosArr[0]);
        if (oneWayFlightInfosArr == null || oneWayFlightInfosArr.length == 0) {
            return oneWayFlightInfos;
        }
        for (String oneWayFlightInfoStr : oneWayFlightInfosArr) {
            List<FlightSegement> flightSegements = getFlightSegements(oneWayFlightInfoStr);
            if (flightSegements.isEmpty()) {
                continue;
            }
            List<String> flightNos = getFlightNos(flightSegements);
            if (flightNos.isEmpty()) {
                continue;
            }
            FlightDetail flightDetail = new FlightDetail();

            double tax = Double.parseDouble(StringUtils.substringBetween(
                    oneWayFlightInfoStr, "<taxtotal>", "</taxtotal>"));
            double price = Double.parseDouble(StringUtils.substringBetween(
                    oneWayFlightInfoStr, "<GROSS>", "</GROSS>")) - tax;
            flightDetail.setDepdate(formatDate(flightSearchParam.getDepDate(),
                    "yyyy-MM-dd"));
            flightDetail.setDepcity(flightSearchParam.getDep());
            flightDetail.setArrcity(flightSearchParam.getArr());
            flightDetail.setFlightno(flightNos);
            flightDetail.setPrice(price);
            flightDetail.setTax(tax);
            flightDetail.setMonetaryunit(StringUtils.substringBetween(
                    oneWayFlightInfoStr, "<currency>", "</currency>"));
            if(!validateDetail(flightDetail)){
                continue;
            }
            OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
            oneWayFlightInfo.setDetail(flightDetail);
            oneWayFlightInfo.setInfo(flightSegements);
            oneWayFlightInfos.add(oneWayFlightInfo);
        }
        return oneWayFlightInfos;
    }
    //验证flightDetail
    private boolean validateDetail(FlightDetail flightDetail) {
        return  flightDetail!=null
                &&StringUtils.isNotBlank(flightDetail.getDepcity())
                &&StringUtils.isNotBlank(flightDetail.getArrcity())
                &&flightDetail.getDepdate()!=null
                &&(flightDetail.getFlightno().size())!=0
                &&flightDetail.getPrice()!=0.0
                &&(flightDetail.getTax()==0.0||flightDetail.getTax()>0.0);
    }
    //格式化日期
    private Date formatDate(String dateStr, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
    //获取每个航段中的航段号
    private List<String> getFlightNos(List<FlightSegement> flightSegements) {
        List<String> flightNos = Lists.newArrayList();
        for (FlightSegement flightSegement : flightSegements) {
            flightNos.add(flightSegement.getFlightno());
        }
        return flightNos;
    }
    //添加每个航段到航段list里
    private List<FlightSegement> getFlightSegements(String oneFlightSegments) {
        List<FlightSegement> flightSegements = Lists.newArrayList();
        String[] flightInfos =StringUtils.substringsBetween(oneFlightSegments, "<flight ", "</flight>");
        if (flightInfos == null || flightInfos.length == 0) {
            return flightSegements;
        }
        for (String flightInfo : flightInfos) {
            FlightSegement flightSegement = getOneFlightSegement(flightInfo);
            if (!validateFlightSegement(flightSegement)) {
                continue;
            }
            flightSegements.add(flightSegement);
        }
        return flightSegements;
    }
    //验证航段信息
    private boolean validateFlightSegement(FlightSegement flightSegement) {
        return  flightSegement != null
                && StringUtils.isNotBlank(flightSegement.getArrairport())
                && StringUtils.isNotBlank(flightSegement.getArrtime())
                && StringUtils.isNotBlank(flightSegement.getDepairport())
                && StringUtils.isNotBlank(flightSegement.getDeptime())
                && StringUtils.isNotBlank(flightSegement.getFlightno());
    }
    //解析XML，给每个航段赋值
    private FlightSegement getOneFlightSegement(String str) {

        FlightSegement flightSegement = new FlightSegement();
        String flightno =StringUtils.substringBetween(str, "<carrCode>",
                "</carrCode>").trim()+StringUtils.substringBetween(str, "<fltNumber>",
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

}
