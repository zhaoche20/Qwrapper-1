package zhangdai;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFHttpClient;
import com.qunar.qfwrapper.developer.QFPostMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dai.zhang
 * Date: 14-7-3
 * Time: 下午5:35
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjd11121210 implements QunarCrawler {


    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {
        String query = flightSearchParam.getDep() + "," + flightSearchParam.getArr() + "," + flightSearchParam.getDepDate();
        if (!query.contains("SHA")) {
            return getDetailHtml(query, flightSearchParam);
        } else {
            String query2 = query.replace("SHA", "PVG");
            return getDetailHtml(query, flightSearchParam) + "=========" + getDetailHtml(query2, flightSearchParam);
        }
    }


    public String getDetailHtml(String query, FlightSearchParam flightSearchParam) {
        String[] args = query.split(",");
        String dep = args[0];
        String arr = args[1];
        String date = args[2].replaceAll("-", "");
        HttpClient client = new QFHttpClient(flightSearchParam, true);

        String url = "";

        url = "http://b2c.csair.com/B2C40/detail-" + dep + arr + "-" + date + "-1-0-0-0-1-0-1-0-1-0.g2c";

        QFPostMethod post = new QFPostMethod(url);
        try {
            post.setRequestHeader("Referer", "http://b2c.csair.com/B2C40/modules/booking/international/flightSelectDirect_inter.jsp");
            post.getParams().setContentCharset("UTF-8");
            client.executeMethod(post);
            String resp = post.getResponseBodyAsString();//403 Forbidden
            if (resp.contains("403 Forbidden") || resp.contains("<NEEDVERIFY>True</NEEDVERIFY>")) {
                return "Exception";
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        } finally {
            try {
                if (post != null) {
                    post.releaseConnection();
                }
            } catch (Exception e) {
                return "Exception";
            }
        }
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        try {
            if ("Exception".equals(html)) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                processResultInfo.setData(data);
                return processResultInfo;
            } else if (html.contains("OVERSEAURL")) {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                processResultInfo.setData(data);
                return processResultInfo;
            } else {

                String[] flights = StringUtils.substringsBetween(html, "<DATEFLIGHT>", "</DATEFLIGHT>");
                if ("".equals(flights[0])) {
                    processResultInfo.setStatus(Constants.NO_RESULT);
                    processResultInfo.setData(data);
                    return processResultInfo;
                }

                for (int j = 0; j < flights.length; j++) {

                    String[] times = StringUtils.substringsBetween(flights[j], "TIME>", "</");
                    if (!times[0].contains(flightSearchParam.getDepDate())) {
                        processResultInfo.setStatus(Constants.INVALID_DATE);
                        processResultInfo.setData(data);
                        return processResultInfo;
                    }

                    FlightDetail flightDetail = getFlightDetail(flights[j], flightSearchParam);

                    List<FlightSegement> flightSegementList = new ArrayList<FlightSegement>();
                    String[] oneFlight = StringUtils.substringsBetween(html, "<FLIGHT>", "</FLIGHT>");
                    for (int i = 0; i < oneFlight.length; i++) {
                        FlightSegement flightSegement = getFlightSegement(oneFlight[i], flightSearchParam);
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
            }
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(data);
            e.printStackTrace();
            return processResultInfo;
        }
    }


    /**
     * ******************getFlightDetail**************************
     */
    public FlightDetail getFlightDetail(String flights, FlightSearchParam flightSearchParam) {
        FlightDetail flightDetail = new FlightDetail();
        flightDetail.setArrcity(flightSearchParam.getArr());
        flightDetail.setDepcity(flightSearchParam.getDep());
        Date Depdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Depdate = sdf.parse(flightSearchParam.getDepDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        flightDetail.setDepdate(Depdate);

        //设置flightDetail的flightno、monetaryunit、tax、 price属性
        String[] flightno = StringUtils.substringsBetween(flights, "<FLIGHTNO>", "</FLIGHTNO>");
        flightDetail.setFlightno(Arrays.asList(flightno));

        String monetaryunit = StringUtils.substringBetween(flights, "<ADULTCURRENCY>", "</ADULTCURRENCY>");
        flightDetail.setMonetaryunit(monetaryunit);

        //三种税相加
        String cnString = StringUtils.substringBetween(flights, "<ADULTCN>", "</ADULTCN>").replace(",", "");
        String yqString = StringUtils.substringBetween(flights, "<ADULTYQ>", "</ADULTYQ>").replace(",", "");
        String xtString = StringUtils.substringBetween(flights, "<ADULTXT>", "</ADULTXT>").replace(",", "");
        double tax = Double.valueOf(cnString) + Double.valueOf(yqString) + Double.valueOf(xtString);
        flightDetail.setTax(tax);

        String price = StringUtils.substringBetween(flights, "<ADULTPRICE>", "</ADULTPRICE>");
        flightDetail.setPrice(Double.valueOf(price));
        return flightDetail;
    }


    /**
     * ********************getFlightSegement***************************
     */

    public FlightSegement getFlightSegement(String oneFlight, FlightSearchParam flightSearchParam) {

        FlightSegement flightSegement = new FlightSegement();
        flightSegement.setDepairport(StringUtils.substringBetween(oneFlight, "<DEPPORT>", "</DEPPORT>"));
        flightSegement.setArrairport(StringUtils.substringBetween(oneFlight, "<ARRPORT>", "</ARRPORT>"));
        flightSegement.setFlightno(StringUtils.substringBetween(oneFlight, "<FLIGHTNO>", "</FLIGHTNO>"));
        flightSegement.setDeptime(StringUtils.substringBetween(oneFlight, "<DEPTIME>", "</DEPTIME>").substring(11));
        flightSegement.setArrtime(StringUtils.substringBetween(oneFlight, "<ARRTIME>", "</ARRTIME>").substring(11));
        flightSegement.setDepDate(StringUtils.substringBetween(oneFlight, "<DEPTIME>", "</DEPTIME>").substring(0, 10));
        flightSegement.setArrDate(StringUtils.substringBetween(oneFlight, "<ARRTIME>", "</ARRTIME>").substring(0, 10));

        return flightSegement;
    }


    /**
     * *******booking方法由于南航公司国内使用机场，国外使用城市名进行检索，而且无法抓取到bookinginfo，暂无法完成。********
     */
    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {
        BookingResult bookingResult = new BookingResult();
        String dep = param.getDep();
        String arr = param.getArr();
        String date = param.getDepDate();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction("http://b2c.csair.com/B2C40/modules/booking/international/flightSelectDirect_inter.jsp?WT.mc_id=vse-qunar-international");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("post");
        Map<String, String> inputMap = new HashMap<String, String>();


//        String bookingInfo = parameters.get("bookinginfo");
//
//        if (bookingInfo != null) {
//            String[] book = bookingInfo.split("\\|\\|");
//            String[] book1 = book[1].replaceAll("bookingurl:&", "").split("&");
//            for (int i = 0; i < book1.length; i++) {
//                String[] nv = book1[i].split("=");
//                parameters.put(nv[0], nv.length == 1 ? "" : nv[1]);
//            }
//            if (parameters.get("dep").equals("SHA")) {
//                dep = "SHA";
//            } else if (parameters.get("dep").equals("PVG")) {
//                dep = "PVG";
//            }
//            if (parameters.get("arr").equals("SHA")) {
//                arr = "SHA";
//            } else if (parameters.get("arr").equals("PVG")) {
//                arr = "PVG";
//            }
//        }
//
//        String depzh = InfoCenter.getCityFromAirportCode(dep, "zh");
//        if (dep.equals("PVG")) {
//            depzh += "浦东";
//        } else if (dep.equals("SHA")) {
//            depzh += "虹桥";
//        }
//
//        String arrzh = InfoCenter.getCityFromAirportCode(arr, "zh");
//        if (arr.equals("PVG")) {
//            arrzh += "浦东";
//        } else if (arr.equals("SHA")) {
//            arrzh += "虹桥";
//        }
//        String date = parameters.get("required2");
//        inputMap.put("segtype_1", "1");
//        inputMap.put("segtype", "1");
//        inputMap.put("fromcity", depzh);
//        inputMap.put("city1_code", dep);
//        inputMap.put("tocity", arrzh);
//        inputMap.put("city2_code", arr);
//        inputMap.put("DepartureDate", date);
//        inputMap.put("ReturnDate", date);
//        inputMap.put("adult", "1");
//        inputMap.put("child", "0");
//        inputMap.put("infant", "0");
//        inputMap.put("cabinSeat", "经济舱");
//        inputMap.put("preUrl", "PINGAN");
//
//        bookingResult.setData(bookingInfo);
//        return bookingResult;
//    }
        return null;

    }
}
