package nipeng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.travelco.html.tools.JSONReader;

/**
 * Created by peng.ni on 14-6-30.
 */
public class Wrapper_gjdair7c001 implements QunarCrawler {


    private static final String CODEBASE = "gjdair7c001";
    //private static final String url="http://book.hongkongairlines.com";

    @Override
    public String getHtml(FlightSearchParam param) {
        // TODO Auto-generated method stub
        QFHttpClient httpClient = new QFHttpClient(param, false);
      //  String []args = new URLHelper(url).getValues();
        httpClient.getParams().setContentCharset("UTF-8");
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        QFPostMethod post = null;
        try {
            String postUrl = new String("http://en.jejuair.net/jejuair/com/jeju/ibe/international/searchAvailFare.do");
          //  String postUrl = new String("http://en.jejuair.net/jejuair/com/jeju/ibe/international/intAvailInit.do?lang=ko_CN");
            post = new QFPostMethod(postUrl);
            post.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            post.addRequestHeader("X-Requested-With","XMLHttpRequest");
            NameValuePair[] values = new NameValuePair[]{
                    new NameValuePair("DepStn",param.getDep()),
                    new NameValuePair("ArrStn", param.getArr()),
                    new NameValuePair("DepDate", param.getDepDate()),
                    new NameValuePair("AdultPaxCnt", "1"),
                    new NameValuePair("ChildPaxCnt", "0"),
                    new NameValuePair("InfantPaxCnt	", "0"),
                    new NameValuePair("RouteType", "I"),
                    new NameValuePair("Language", "EN"),
                    new NameValuePair("TripType", "OW"),
                    new NameValuePair("SignType","en.ibe.int"),
                    new NameValuePair("SegType","DEP"),
                    new NameValuePair("Index","4")
            };
            post.addParameters(values);
            httpClient.executeMethod(post);
            String res = post.getResponseBodyAsString();
            if(post.getStatusCode()!=200){
                return "StatusError"+post.getStatusCode();
            }
            return res;
        } catch (Exception e) {
//            logger.error("ErrorIn " + CODEBASE + " : " , e);
        } finally{
            if(post!=null){
                post.releaseConnection();
            }
        }
        return "Exception";
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam param) {

        // TODO Auto-generated method stub
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = Lists.newArrayList();

        if ("Exception".equals(html)) {
//            logger.warn("{},{}",new Object[]{CODEBASE, "CONNECTION_FAIL"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        if ("PARAMERROR".equals(html)) {
            //可选辅助 Key.TEMP，Key.TEMP1 等计数器细分抓取失败的类型

//            logger.warn("{},{}",new Object[]{CODEBASE,"PARAMERROR"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        if (html.startsWith("StatusError")) {
//            logger.warn("{},{}", new Object[]{CODEBASE, html});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        //无效航线||无效日期
        if (html.contains("\"message\":\"NO ROUTINGS\"")) {
//            logger.warn("{},{}",new Object[]{CODEBASE,"INVALID_AIRLINE"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        //无效日期
        if (html.contains("特征字符串  网站给出日期无效的相关描述性特征字符串")) {

//            logger.warn("{},{}",new Object[]{CODEBASE, "INVALID_DATE"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        //无结果，售完
        if (html.contains("特征字符串  网站给出无航班、售完等相关描述性特征字符串")) {
//            logger.warn("{},{}",new Object[]{CODEBASE, "NO_RESULT"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
        //可选特殊信息回传
        String specialMessage = "NULL";


        try {
            //TODO processor implementation
            List<Map<?, ?>> plainList = null;
            //   Map<?,?> plainMap = (Map<?, ?>)new JSONReader().read(html);
            Map<?,?> plainMap = JSON.parseObject(html, new TypeReference<Map<?,?>>() {
            });
            Map<?,?> plainResult = (Map<?,?>) plainMap.get("Result");
            Map<?,?> plainData = (Map<?, ?>) plainResult.get("data");
            plainList=(List<Map<?, ?>>) plainData.get("availData");
            int count=plainList.size();
            //具体解析逻辑写在 try 里面

            int num=0;
            for(int i=0;i<count;i++)
            {
                OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
                List<FlightSegement> info= Lists.newArrayList();
                FlightSegement seg=new FlightSegement();

                String planeType=plainList.get(i).get("fltType").toString();
                String price =plainList.get(i).get("specialEquivFare").toString();
                String RBD=plainList.get(i).get("specialRBD").toString();
                String FareBasis=plainList.get(i).get("specialEquivFareBasis").toString();
                if(price.equals(""))
                {
                    price =plainList.get(i).get("discountEquivFare").toString();
                    RBD=plainList.get(i).get("discountRBD").toString();
                    FareBasis=plainList.get(i).get("discountEquivFareBasis").toString();
                }
                if(price.equals(""))
                {
                    price =plainList.get(i).get("normalEquivFare").toString();
                    RBD=plainList.get(i).get("normalRBD").toString();
                    FareBasis=plainList.get(i).get("normalEquivFareBasis").toString();
                }
                if(price.equals(""))
                    continue;

                String departureTime=plainList.get(i).get("depTime").toString();
                departureTime=departureTime.substring(0, 2)+":"+departureTime.substring(2);

                String arrivalTime=plainList.get(i).get("arrTime").toString();
                arrivalTime=arrivalTime.substring(0, 2)+":"+arrivalTime.substring(2);

                String currencyCode=plainList.get(i).get("currency").toString();

                String code="7C"+plainList.get(i).get("fltNo").toString();

              //  String tax="0";

                //抓取税费
				String tax=getFare(code.substring(2),plainList.get(i).get("depDate").toString()+plainList.get(i).get("depTime").toString(), plainList.get(i).get("arrDate").toString()+plainList.get(i).get("arrTime").toString(),param.getDep(),param.getArr(),RBD,FareBasis,param);
                System.out.println(tax);
                seg.setDepDate(param.getDepDate());
                seg.setArrDate(param.getRetDate());
                seg.setDeptime(departureTime);
                seg.setArrtime(arrivalTime);
                seg.setFlightno(code);
                seg.setDepairport(param.getDep());
                seg.setArrairport(param.getArr());
                seg.setCompany("7C");
                seg.setAircraft(planeType);
                info.add(seg);

                FlightDetail detail=new FlightDetail();
                detail.setDepcity(param.getDep());
                detail.setArrcity(param.getArr());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date= sdf.parse(param.getDepDate());
                detail.setDepdate(date);
                detail.setMonetaryunit("KRW");
                if(price!=null)
                {
                    detail.setPrice(Double.parseDouble(price));
                }
                if(!(tax.contains("message")))
                {
                    detail.setTax(Double.parseDouble(tax));
                }
                detail.setWrapperid(CODEBASE);
                oneWayFlightInfo.setInfo(info);
                oneWayFlightInfo.setDetail(detail);
                data.add(oneWayFlightInfo);
                num++;
            }
            //已售完、无结果
            if(num==0)
            {
//                logger.warn("{},{}",new Object[]{CODEBASE, "NO_RESULT"});
                processResultInfo.setData(data);
                processResultInfo.setRet(false);
                return processResultInfo;
            }
        } catch (Exception e) {
            // TODO: handle exception

//            logger.warn("{},{}",new Object[]{CODEBASE, "PARSING_FAIL"});
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            return processResultInfo;
        }
          
        //return result.toString();
        processResultInfo.setData(data);
        processResultInfo.setRet(true);
        processResultInfo.setStatus(Constants.SUCCESS);
        return processResultInfo;
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {

        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setMethod("post");
        bookingInfo.setAction("http://www.jejuair.net/jejuair/com/jeju/ibe/international/intAvailInit.do");

        Map<String, String> input = new HashMap<String, String>();
        input.put("lang", "ko_EN");
        input.put("easyReservation", "true");
        input.put("tripType","0");
        input.put("cboAirport1", param.getDep());
        input.put("cboAirport3", param.getArr());
        input.put("DepartureDate_int", param.getDepDate());
        input.put("AdultPax_Cnt_int", "1");
        input.put("ChildPax_Cnt_int", "0");
        input.put("InfantPax_Cnt_int", "0");
//        input.put("cboDepStn",  param.getDep());
//        input.put("cboArrStn", param.getArr());
        input.put("AdultPax_Cnt_dom", "1");
        input.put("ChildPax_Cnt_dom", "0");
        input.put("InfantPax_Cnt_dom", "0");

        bookingResult.setRet(true);
        bookingInfo.setInputs(input);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    //请求获取税费 未调用
    private String getFare(String flightno, String depTime, String arrTime,String DepStn,String ArrStn,String RBD,String FareBasis,FlightSearchParam param)
    // throws Exception
    {
        QFHttpClient httpClient = new QFHttpClient(param, false);
        // TODO Auto-generated method stub
        QFPostMethod post = new QFPostMethod("http://www.jejuair.net/jejuair/com/jeju/ibe/international/searchFare.do");
        NameValuePair[] values = new NameValuePair[]{
                new NameValuePair("TripType", "OW"),
                new NameValuePair("ReqType", "Price"),
                new NameValuePair("Language", "EN"),
                new NameValuePair("DepStn", param.getDep()),
                new NameValuePair("ArrStn", param.getArr()),
                new NameValuePair("DepDate", depTime),
                new NameValuePair("ArrDate", arrTime),
                new NameValuePair("AdultPaxCnt", "1"),
                new NameValuePair("ChildPaxCnt", "0"),
                new NameValuePair("InfantPaxCnt", "0"),
                new NameValuePair("RouteType", "I"),
                new NameValuePair("FltNo", flightno),
                new NameValuePair("RBD", RBD),
                new NameValuePair("FareBasis", FareBasis)
        };
        post.addRequestHeader("Referer", "http://www.jejuair.net/jejuair/com/jeju/ibe/international/intAvailInit.do?lang=ko_EN");
        post.addRequestHeader("Host", "en.jejuair.net");
        post.addRequestHeader("Connection", "keep-alive");
        post.addRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        post.addParameters(values);
        String resp="";
        try{
            httpClient.executeMethod(post);
            resp = post.getResponseBodyAsString();
        }catch(Exception e){}
        String tax = StringUtils.substringBetween(resp, "taxAmount\":\"", "\",\"");
        return tax;
    }

    public static void main(String[] args)
    {
        FlightSearchParam f = new FlightSearchParam();
        f.setArr("KIX");
        f.setDep("ICN");
        f.setDepDate("2014-07-09");
        f.setRetDate("2014-08-28");
        Wrapper_gjdair7c001 wr = new Wrapper_gjdair7c001();
        String sw = wr.getHtml(f);
   //     System.out.println(sw);
        wr.process(sw,f);
//        ProcessResultInfo pri = wr.process(sw, f);
    }
}
