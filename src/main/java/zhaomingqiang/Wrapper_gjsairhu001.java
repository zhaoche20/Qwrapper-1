package zhaomingqiang;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFPostMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-8
 * Time: 上午9:57
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjsairhu001 implements QunarCrawler{

    private static final String CODEBASE = "gjsairhu001";
    private QFHttpClient httpClient = null;
    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate();
        String retDate=flightSearchParam.getRetDate();
        httpClient=new QFHttpClient(flightSearchParam,true);
        String postUrl = "http://hnair.travelsky.com/huet/bc10_av.do";
        NameValuePair[] nameValuePairs = {
                new NameValuePair("queryPassengerType","0"),
                new NameValuePair("dstCity",arr),
                new NameValuePair("returnDate",retDate),
                new NameValuePair("date","ROUNDTRIP"),
                new NameValuePair("tripType","ROUNDTRIP"),
                new NameValuePair("adultNum","1"),
                new NameValuePair("bookSeatClass","E"),
                new NameValuePair("city_name1",""),
                new NameValuePair("childNum","0"),
                new NameValuePair("city_name",""),
                new NameValuePair("takeoffDate",depDate),
                new NameValuePair("orgCity",dep)
        };
        PostMethod postMethod = new QFPostMethod(postUrl);
        try {
            postMethod.setRequestBody(nameValuePairs);
            httpClient.executeMethod(postMethod);
            throwExceptionByResponseCode(httpClient.executeMethod(postMethod),200);
            return postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }
    /**
     *
     * @param responseCode 请求反馈的状态吗
     * @param compareCode  目标状态码
     * @throws java.io.IOException
     */
    private void throwExceptionByResponseCode(int responseCode,int compareCode) throws IOException {
        if(responseCode!=compareCode)
            throw new IOException("CONNECTION_FAIL");
    }
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo=new ProcessResultInfo();
        List<RoundTripFlightInfo> roundTripFlightInfoList= (List<RoundTripFlightInfo>) processResultInfo.getData();
        try
        {
            String table= StringUtils.substringBetween(html,"class=\"view_table\"","</table>");
            if(table.indexOf("很抱歉，您所查询"+flightSearchParam.getDepDate()+"的航班座位已售完")>0)
            {
                processResultInfo.setStatus(Constants.NO_RESULT);
                return processResultInfo;
            }
            else if(table.indexOf("很抱歉，")>-1)
            {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                return processResultInfo;
            }
            if(table.indexOf("<tr class=\"tbody price_all\">")==-1)
            {
                String[] trs=StringUtils.substringsBetween(table,"<tr class=\"tbody\">","</tr>");
                for(String tr : trs)
                {
                    RoundTripFlightInfo roundTripFlightInfo=getRoundTripFlightInfo(tr, flightSearchParam);
                    if(roundTripFlightInfo!=null)
                        roundTripFlightInfoList.add(roundTripFlightInfo);
                }
            }
            else
            {
                if(!table.contains("id=\"sort_table\""))
                {
                    String deparrs[]=StringUtils.substringBetween(StringUtils.substringBetween(html,"<div class=\"calendar_title\">","</div>"),"<h1>","</h1>").replaceAll("\\s","").split("-");
                    System.out.println(deparrs);
                    table= StringUtils.substringBetween(html,"class=\"view_table\"","</tbody>");
                    String[] trs=StringUtils.substringsBetween(table,"<tr class=\"tbody\">","<tr class=\"tbody price_all\">");
                    for(String tr : trs)
                    {
                        RoundTripFlightInfo roundTripFlightInfo=getRoundTripFlightInfo(tr, flightSearchParam);
                        if(roundTripFlightInfo!=null)
                            roundTripFlightInfoList.add(roundTripFlightInfo);
                    }
                }
            }
            processResultInfo.setStatus(Constants.SUCCESS);
            return processResultInfo;
        }catch (Exception e)
        {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
    }
    private RoundTripFlightInfo getRoundTripFlightInfo(String tr,FlightSearchParam flightSearchParam) throws ParseException {
        RoundTripFlightInfo roundTripFlightInfo=new RoundTripFlightInfo();
        List<FlightSegement> info=roundTripFlightInfo.getInfo();
        FlightDetail flightDetail=roundTripFlightInfo.getDetail();
        roundTripFlightInfo.setRetdepdate(Date.valueOf(flightSearchParam.getRetDate()));
        List<String> retFlightNos= roundTripFlightInfo.getRetflightno();
        List<FlightSegement> retFlightSegements=roundTripFlightInfo.getRetinfo();
        String[] tds=StringUtils.substringsBetween(tr,"<td","</td>");
        if(tds[0].indexOf("lower_price hidden")>-1)
        {
            String[] as=StringUtils.substringsBetween(tds[1],"<a","</a>");
            List<String> nos=new ArrayList<String>();
            List<String> dateTimes=new ArrayList<String>();
            for(String a : as)
            {
                nos.add(StringUtils.substringAfter(a, "class=\"popup_text\">"));
            }
            Pattern pattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
            Matcher matcher=pattern.matcher(tds[3]);
            while (matcher.find())
            {
                dateTimes.add(matcher.group());
            }
            int i=0,j=0;
            for(String no : nos)
            {
                FlightSegement flightSegement=new FlightSegement();
                flightSegement.setDepairport(flightSearchParam.getDep());
                if(j==nos.size()-1)
                {
                    flightSegement.setArrairport(flightSearchParam.getArr());
                }
                else
                {
                    flightSegement.setArrairport("XXX");
                }
                flightSegement.setFlightno(no);
                String[] depDateTime=dateTimes.get(i).split(" ");
                flightSegement.setDepDate(depDateTime[0]);
                flightSegement.setDeptime(depDateTime[1]);
                i++;
                String[] retDateTime=dateTimes.get(i).split(" ");
                flightSegement.setArrDate(retDateTime[0]);
                flightSegement.setArrtime(retDateTime[1]);
                j++;
                info.add(flightSegement);
            }
            flightDetail.setArrcity(flightSearchParam.getArr());
            flightDetail.setDepcity(flightSearchParam.getDep());
            flightDetail.setDepdate(Date.valueOf(flightSearchParam.getDepDate()));
            flightDetail.setMonetaryunit("CNY");
            flightDetail.getFlightno().addAll(nos);
            flightDetail.setPrice(Double.valueOf(StringUtils.substringBetween(tds[0],"class=\"lower_price hidden\">","</span>")));
        }
        /*else
        {
            String price=StringUtils.substringAfter(tds[8],"￥");
            if(price==null)
                return null;
            List<String> dateTimes=new ArrayList<String>();
            Pattern pattern=Pattern.compile("\\d{2}:\\d{2}");
            Matcher matcher=pattern.matcher(tds[3]);
            while (matcher.find())
            {
                dateTimes.add(matcher.group());
            }
            String [] times=tds[3].split("<br />");
            FlightSegement goflightSegement=new FlightSegement();
            goflightSegement.setDepairport(flightSearchParam.getDep());
            goflightSegement.setArrairport(flightSearchParam.getArr());
            goflightSegement.setFlightno(StringUtils.substringBetween(tds[1], ">","<br />"));
            goflightSegement.setDepDate(flightSearchParam.getDepDate());
            if(times[0].indexOf("+")>-1)
            {
                goflightSegement.setArrDate(getDate(flightSearchParam.getDepDate(),1));
            }
            else
            {
                goflightSegement.setArrDate(flightSearchParam.getDepDate());
            }
            goflightSegement.setDeptime(dateTimes.get(0));
            goflightSegement.setArrtime(dateTimes.get(1));

            FlightSegement returnflightSegement=new FlightSegement();
            returnflightSegement.setDepairport(flightSearchParam.getArr());
            returnflightSegement.setArrairport(flightSearchParam.getDep());
            returnflightSegement.setFlightno(StringUtils.substringAfter(tds[1], "<br />"));
            returnflightSegement.setDepDate(flightSearchParam.getRetDate());
            if(times[1].indexOf("+")>-1)
            {
                returnflightSegement.setArrDate(getDate(flightSearchParam.getDepDate(),1));
            }
            else
            {
                returnflightSegement.setArrDate(flightSearchParam.getDepDate());
            }
            returnflightSegement.setDeptime(dateTimes.get(2));
            returnflightSegement.setArrtime(dateTimes.get(3));
            info.add(goflightSegement);
            retFlightSegements.add(returnflightSegement);
            retFlightNos.add(returnflightSegement.getFlightno());
            flightDetail.setArrcity(flightSearchParam.getArr());
            flightDetail.setDepcity(flightSearchParam.getDep());
            flightDetail.setDepdate(Date.valueOf(flightSearchParam.getDepDate()));
            flightDetail.setMonetaryunit("CNY");
            flightDetail.getFlightno().add(goflightSegement.getFlightno());
            flightDetail.setPrice(Double.valueOf(price));
        }*/
        return roundTripFlightInfo;
    }
    private String getDate(String date,int day) throws ParseException {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(dateFormat.parse(date));
        calendar.add(Calendar.DAY_OF_YEAR, day);
        return dateFormat.format(calendar.getTime());
    }
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult=new BookingResult();
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        BookingInfo bookingInfo = new BookingInfo();
        java.util.Map<String, String> inputs = new HashMap<String, String>();
        bookingInfo.setAction("http://hnair.travelsky.com/huet/bc10_av.do");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("get");
        inputs.put("WT.mc_id", "ad_from_qunar");
        inputs.put("queryPassengerType", "0");
        inputs.put("dstCity", dep);
        inputs.put("returnDate", depDate);
        inputs.put("date", "ONEWAY");
        inputs.put("takeoffDate", depDate);
        inputs.put("tripType", "ONEWAY");
        inputs.put("CABINCLASS", "Y");
        inputs.put("adultNum", "1");
        inputs.put("bookSeatClass", "E");
        inputs.put("city_name1", "");
        inputs.put("childNum", "0");
        inputs.put("city_name", "");
        inputs.put("orgCity", arr);
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}
