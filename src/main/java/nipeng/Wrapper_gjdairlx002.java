package nipeng;

import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFGetMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by peng.ni on 14-7-2.
 */
public class Wrapper_gjdairlx002 implements QunarCrawler {

//    private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
    private static final String CODEBASE = "gjdairlx002";
    private String bookingUrl;
    @Override
    public String getHtml(FlightSearchParam param) {

        QFHttpClient httpClient = new QFHttpClient(param, false);
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        QFGetMethod get = null;
        try {
            String temp[]=param.getDepDate().split("-");
            String outbound=temp[2]+"."+temp[1]+"."+temp[0];
            get=new QFGetMethod("http://tracking.mlsat03.de/swiss/api/index_phg.php?kid=995&dlid=1274&conid=8626&fwid=182053&pid=10lGaM&pwd=yQkPQT5K&origin="+param.getDep()+"&destination="+param.getArr()+"&outbound="+outbound+"&adults=1&children=0&infants=0&language=zh&country=cn");
            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();
        } catch (Exception e) {
//            logger.error("ErrorIn " + CODEBASE );
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return "Exception";
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam param) {
        {
            ProcessResultInfo processResultInfo = new ProcessResultInfo();
            List<OneWayFlightInfo> data = Lists.newArrayList();

            if ("Exception".equals(html)) {

//                logger.warn("{},{}", new Object[]{CODEBASE,
//                        "CONNECTION_FAIL"});
                processResultInfo.setData(data);
                processResultInfo.setRet(false);
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                return processResultInfo;

            }
            // 无结果，售完
            if (html.contains("Errors")) {
//                logger.warn("{},{}", new Object[] { CODEBASE, "NO_RESULT"});
                processResultInfo.setData(data);
                processResultInfo.setRet(false);
                processResultInfo.setStatus(Constants.NO_RESULT);
                return processResultInfo;
            }
            try {

                // 具体解析逻辑写在 try 里面
                String airlines[]= StringUtils.substringsBetween(html, "<Result>", "</Result>");
                String repeatcode="";
                for(int i=0;i<airlines.length;i++)
                {

                    OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
                    List<FlightSegement> info= Lists.newArrayList();
                    FlightDetail detail=new FlightDetail();

                    String flights[]= StringUtils.substringsBetween(airlines[i], "<FlightNumber CodeContext=\"IATA\">", "</FlightNumber>");
                    String flight=flights[0];
                    for(int j=1;j<flights.length;j++)
                        flight+="/"+flights[j];
                    if(flight.equals(repeatcode))
                        continue;//去掉重复的航班,取最低价
                    repeatcode=flight;

                    String[] flightSegement= StringUtils.substringsBetween(airlines[i], "<FlightSegment>", "</FlightSegment>");
                    for(String flightSegementPart:flightSegement)
                    {
                        FlightSegement fs=new FlightSegement();
                        String flightsSegement[]= StringUtils.substringsBetween(flightSegementPart, "<FlightNumber CodeContext=\"IATA\">", "</FlightNumber>");
                        String flightNoSegement=flightsSegement[0];//航班号


                        String depInfo= StringUtils.substringBetween(flightSegementPart, "<Departure>", "</Departure>");
                        String dep= StringUtils.substringBetween(depInfo, "<Airport CodeContext=\"IATA\">", "</Airport>");
                        String depDate= StringUtils.substringBetween(depInfo, "<DateTime>", "</DateTime>").split("T")[0];
                        String depTime= StringUtils.substringBetween(depInfo, "<DateTime>", "</DateTime>").split("T")[1];

                        String arrInfo= StringUtils.substringBetween(flightSegementPart, "<Arrival>", "</Arrival>");
                        String arr= StringUtils.substringBetween(arrInfo, "<Airport CodeContext=\"IATA\">", "</Airport>");
                        String arrDate= StringUtils.substringBetween(arrInfo, "<DateTime>", "</DateTime>").split("T")[0];
                        String arrTime= StringUtils.substringBetween(arrInfo, "<DateTime>", "</DateTime>").split("T")[1];
                        fs.setDepDate(depDate);
                        fs.setArrDate(arrDate);
                        fs.setDeptime(depTime.substring(0,5));
                        fs.setArrtime(arrTime.substring(0, 5));
                        fs.setDepairport(dep);
                        fs.setArrairport(arr);
                        fs.setFlightno(flightNoSegement);
                        fs.setCompany(flight.substring(0,2));
                        info.add(fs);

                    }

                    String currencyCode= StringUtils.substringBetween(airlines[i], "CurrencyCode=\"", "\">");
                    String retailPrice= StringUtils.substringBetween(airlines[i], "<BaseAmount>", "</BaseAmount>");
                    String tax= StringUtils.substringBetween(airlines[i], "<TaxAmount>", "</TaxAmount>");

                    detail.setDepcity(param.getDep());
                    detail.setArrcity(param.getArr());
                    detail.setMonetaryunit(currencyCode);
                    detail.setTax(Double.parseDouble(retailPrice));
                    detail.setPrice(Double.parseDouble(tax));
                    detail.setWrapperid(CODEBASE);
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                    Date date=sdf.parse(param.getDepDate());
                    detail.setDepdate(date);
                    List<String> flightNo= Lists.newArrayList();
                    for(FlightSegement fs:info)
                    {
                        flightNo.add(fs.getFlightno());
                    }
                    detail.setFlightno(flightNo);
                    oneWayFlightInfo.setDetail(detail);
                    oneWayFlightInfo.setInfo(info);
                    data.add(oneWayFlightInfo);
                    bookingUrl= StringUtils.substringBetween(airlines[i], "<Url>", "</Url>");
                }

                processResultInfo.setData(data);
                processResultInfo.setStatus(Constants.SUCCESS);
                processResultInfo.setRet(true);
                return processResultInfo;


            } catch (Exception e) {
//                logger.warn("{},{}", new Object[]{CODEBASE,
//                       "PARSING_FAIL"});
                processResultInfo.setData(data);
                processResultInfo.setRet(false);
                processResultInfo.setStatus(Constants.PARSING_FAIL);
                return processResultInfo;
            }

        }
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {
        BookingResult bookingResult=new BookingResult();
        BookingInfo bookingInfo=new BookingInfo();
        bookingInfo.setMethod("post");
        bookingInfo.setAction(bookingUrl);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;
    }

    public static void main(String[] args)
    {
        FlightSearchParam f = new FlightSearchParam();
        f.setDep("DUS");
        f.setArr("ZRH");
        f.setDepDate("2014-07-24");
        f.setRetDate("2014-07-24");
        Wrapper_gjdairlx002 wr = new Wrapper_gjdairlx002();
        String sw = wr.getHtml(f);
  //      System.out.println(sw);
        wr.process(sw,f);
//        ProcessResultInfo pri = wr.process(sw, f);
    }


}
