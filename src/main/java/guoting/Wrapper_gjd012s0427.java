package guoting;

/**
 * Created with IntelliJ IDEA.
 * User: ting.guo
 * Date: 14-7-8
 * Time: 下午7:16
 * To change this template use File | Settings | File Templates.
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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

public class Wrapper_gjd012s0427 implements QunarCrawler {
    public static void main(String[] args) {
        Wrapper_gjd012s0427 wrapper_gjd012s0427 = new Wrapper_gjd012s0427();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("PVG");
        flightSearchParam.setArr("CPH");
        flightSearchParam.setDepDate("2014-07-09");
        flightSearchParam.setTimeOut("40000");
        String html = wrapper_gjd012s0427.getHtml(flightSearchParam);
        ProcessResultInfo processResultInfo = wrapper_gjd012s0427.process(html,
                flightSearchParam);
        BookingResult bookingInfo=wrapper_gjd012s0427.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(processResultInfo));

    }

    public String getHtml(FlightSearchParam param) {
        QFHttpClient httpClient = new QFHttpClient(param, false);
        QFGetMethod get = null;
        QFPostMethod post = null;
        String depAirport = param.getDep();
        String arrAirport = param.getArr();
        String depDate = param.getDepDate();
        String urlStr = "http://www.flysas.com/en/cn/?org=" + depAirport
                + "&dest=" + arrAirport + "&out=" + depDate
                + "&adt=1&intercont=false&directsubmit=true&vst=true";

        try {
            get = new QFGetMethod(urlStr);
            httpClient.executeMethod(get);
            String page = get.getResponseBodyAsString();
            String postURL = StringUtils.substringBetween(page,
                    "'btnSubmitAmadeus', '', false, '', '", "',");
            if (postURL == null) {
                return "Exception";
            }
            post = new QFPostMethod(postURL);
            String[] params = StringUtils.substringsBetween(page, "<input ",
                    "/>");
            for (int i = 0; i < params.length; i++) {
                if (params[i].contains("submit"))
                    continue;
                if (params[i].contains("__EVENTTARGET")) {
                    post.addParameter("__EVENTTARGET", "btnSubmitAmadeus");
                } else if (params[i].contains("SO_GL")) {
                    String name = StringUtils.substringBetween(params[i],
                            "name=\"", "\"");
                    String value = StringUtils.substringBetween(params[i],
                            "value=\"", "\"");
                    value = StringEscapeUtils.unescapeHtml(value);
                    post.addParameter(name, value);
                } else {
                    String name = StringUtils.substringBetween(params[i],
                            "name=\"", "\"");
                    String value = StringUtils.substringBetween(params[i],
                            "value=\"", "\"");
                    post.addParameter(name, value);
                }
            }
            httpClient.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (Exception e) {
            return "CONNECTION_FAIL";
        } finally {
            if (null != get) {
                get.releaseConnection();
            }
        }
    }

    public ProcessResultInfo process(String html, FlightSearchParam param) {

        // TODO Auto-generated method stub
        ProcessResultInfo processResultInfo=new ProcessResultInfo();
        List<OneWayFlightInfo> oneWayFlightInfos =Lists.newArrayList();
        try {
            if (html.equals("CONNECTION_FAIL")) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                return processResultInfo;
            }
            else if(html.contains("No flights or not enough available seats"))
            {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                return processResultInfo;
            }
            else if(html.indexOf("No destination city selected")>-1)
            {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                return processResultInfo;
            }

            //取出包含航班信息的字符串
            String str=StringUtils.substringBetween(html,"REV_AVD_COForm.prototype.init = function","REV_AVD_COForm.prototype.removeFlexInterstices = function");
            //System.out.println(str);
            //按价格区分航班
            String[] byPrice=str.split("var recommendation = new REV_AVDRecommendation");
            //分别获取每个航班的详细信息
            for(int i=1;i<byPrice.length;i++){
                //获取byPrice[i]里包含航段的信息
                String[] flights = StringUtils.substringsBetween(byPrice[i],"var segment = new Object();","recommendation.segments.push(segment);");
                List<String> flightnos=Lists.newArrayList();
                List<FlightSegement> flightSegement=Lists.newArrayList();
                for(int j=0;j<flights.length;j++){
                    String id=StringUtils.substringBetween(flights[j],"segment.flightId = \"","\"");
                    String bookingClass=StringUtils.substringBetween(flights[j],"segment.bookingClass = \"","\"");
                    String depAirport=StringUtils.substringBetween(flights[j], "segment.bLocation   = \"","\"");
                    String arrAirport=StringUtils.substringBetween(flights[j], "segment.eLocation   = \"","\"");
                    String depTime=StringUtils.substringBetween(flights[j], "segment.bDate = \"","\"").replaceAll(".*(\\d{2}:\\d{2}):.*", "$1");
                    String s=StringUtils.substringBetween(flights[j], "segment.eDate =	\"","\"");
                    String arrTime=s.replaceAll(".*(\\d{2}:\\d{2}):.*", "$1");
                    String[] s1=s.split(" ");


                    Map<String,String> map=formatDate();
                    String m=map.get(s1[1]);
                    String arrDate=s1[5]+"-"+m+"-"+s1[2];
                    String flightno=StringUtils.substringBetween(html,"<acronym class=\"flightNumber ico own\">", "</acronym>").trim();
                    FlightSegement segement=new FlightSegement();
                    segement.setDepairport(depAirport);
                    segement.setDepDate(param.getDepDate());
                    segement.setDeptime(depTime);
                    segement.setArrairport(arrAirport);
                    segement.setArrtime(arrTime);
                    segement.setArrDate(arrDate);
                    segement.setFlightno(flightno);
                    flightSegement.add(segement);
                    flightnos.add(flightno);

                }
                Double totalPrice=Double.parseDouble(StringUtils.substringBetween(byPrice[i],"'price':'","'"));
                Double tax=Double.parseDouble(StringUtils.substringBetween(byPrice[i],"'tax':'","'"));
                Double price=Double.parseDouble(StringUtils.substringBetween(byPrice[i],"'priceWithoutTax':'","'"));
                String currency=StringUtils.substringBetween(html, "Price is shown in "," per ");
                FlightDetail flightDetail=new FlightDetail();
                flightDetail.setDepcity(param.getDep());
                flightDetail.setArrcity(param.getArr());
                flightDetail.setDepdate(formatDate(param.getDepDate(),"yyyy-MM-dd"));
                flightDetail.setFlightno(flightnos);
                flightDetail.setMonetaryunit(currency);
                flightDetail.setPrice(price);
                flightDetail.setTax(tax);

                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                oneWayFlightInfo.setDetail(flightDetail);
                oneWayFlightInfo.setInfo(flightSegement);
                oneWayFlightInfos.add(oneWayFlightInfo);

            }
            processResultInfo.setData(oneWayFlightInfos);
            processResultInfo.setStatus(oneWayFlightInfos.isEmpty() ? Constants.PARSING_FAIL
                    : Constants.SUCCESS);
            return processResultInfo;
        }catch(Exception e){
            e.printStackTrace();
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
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
    private Map<String,String> formatDate(){
        Map<String,String> map=new HashMap<String,String>();
        map.put("Jan", "01");
        map.put("Feb", "02");
        map.put("Mar", "03");
        map.put("Apr", "04");
        map.put("May", "05");
        map.put("Jun", "06");
        map.put("Jul", "07");
        map.put("Aug", "08");
        map.put("Sep", "09");
        map.put("Oct", "10");
        map.put("Nov", "11");
        map.put("Dec", "12");
        return map;
    }
    public BookingResult getBookingInfo(FlightSearchParam param) {
        // TODO Auto-generated method stub
        BookingResult bookingResult=new BookingResult();
        BookingInfo bookingInfo=new BookingInfo();
        //bookingInfo.setAction("http://www.flysas.com/zh-cn/cn/?WT.mc_id=CNBA512&org="+param.getDep()+"&dest="+param.getArr()+"&out="+param.getDepDate()+"&adt=1&intercont=false&directsubmit=true");
        bookingInfo.setAction("http://www.flysas.com/zh-cn/cn/?");
        bookingInfo.setContentType("utf-8");
        bookingInfo.setMethod("post");
        Map<String,String> map=new HashMap<String,String>();
        map.put("WT.mc_id", "CNBA512");
        map.put("org",param.getDep());
        map.put("dest", param.getArr());
        map.put("out",param.getDepDate());
        map.put("adt", "1");
        map.put("intercont", "false");
        map.put("directsubmit", "true");
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;
    }


}
