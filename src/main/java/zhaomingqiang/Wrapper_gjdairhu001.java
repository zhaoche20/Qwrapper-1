package zhaomingqiang;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-7
 * Time: 下午4:07
 * 海南航空
 */
public class Wrapper_gjdairhu001 implements QunarCrawler{
    private static final String CODEBASE = "gjd09082420";
    private QFHttpClient httpClient = null;
    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate();
        httpClient=new QFHttpClient(flightSearchParam,true);
        String postUrl = "http://hnair.travelsky.com/huet/bc10_av.do";
        NameValuePair[] nameValuePairs = {
                new NameValuePair("queryPassengerType","0"),
                new NameValuePair("dstCity",arr),
                new NameValuePair("returnDate",depDate),
                new NameValuePair("date","ONEWAY"),
                new NameValuePair("tripType","ONEWAY"),
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
        List<OneWayFlightInfo> oneWayFlightInfos= (List<OneWayFlightInfo>) processResultInfo.getData();
        String table= StringUtils.substringBetween(html,"class=\"view_table\"","</tbody>");
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
        String[] trs=StringUtils.substringsBetween(table,"<tr class=\"tbody\">","<tr class=\"tbody price_all\">");
        for(String tr : trs)
        {
            OneWayFlightInfo oneWayFlightInfo=getOneWayFlightInfo(tr, flightSearchParam);
            if(oneWayFlightInfo!=null)
                oneWayFlightInfos.add(oneWayFlightInfo);
        }
        processResultInfo.setStatus(Constants.SUCCESS);
        return processResultInfo;
    }
    private OneWayFlightInfo getOneWayFlightInfo(String tr,FlightSearchParam flightSearchParam)
    {
        OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
        List<FlightSegement> info=oneWayFlightInfo.getInfo();
        FlightDetail flightDetail=oneWayFlightInfo.getDetail();
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
        else
        {
            List<String> prices=new ArrayList<String>();
            Pattern pattern1=Pattern.compile("<b>￥(\\d+)</b>");
            Matcher matcher1=pattern1.matcher(tr);
            while (matcher1.find())
            {
                prices.add(matcher1.group(1));
            }
            if(prices.size()==0)
            {
                return null;
            }
            FlightSegement flightSegement=new FlightSegement();
            flightSegement.setDepairport(flightSearchParam.getDep());
            flightSegement.setArrairport(flightSearchParam.getArr());
            flightSegement.setFlightno(StringUtils.substringAfter(tds[0], ">"));
            flightSegement.setDepDate(flightSearchParam.getDepDate());
            List<String> dateTimes=new ArrayList<String>();
            Pattern pattern=Pattern.compile("\\d{2}:\\d{2}");
            Matcher matcher=pattern.matcher(tds[1]);
            while (matcher.find())
            {
                dateTimes.add(matcher.group());
            }
            flightSegement.setDeptime(dateTimes.get(0));
            flightSegement.setArrtime(dateTimes.get(1));
            if(tds[1].indexOf("+")>-1)
            {
                flightSegement.setArrDate(getDate(flightSearchParam.getDepDate(),1));
            }
            else
            {
                flightSegement.setArrDate(flightSearchParam.getDepDate());
            }
            info.add(flightSegement);
            flightDetail.setArrcity(flightSearchParam.getArr());
            flightDetail.setDepcity(flightSearchParam.getDep());
            flightDetail.setDepdate(Date.valueOf(flightSearchParam.getDepDate()));
            flightDetail.setMonetaryunit("CNY");
            flightDetail.getFlightno().add(flightSegement.getFlightno());
            flightDetail.setPrice(Double.valueOf(prices.get(prices.size()-1)));
        }
        return oneWayFlightInfo;
    }
    private String getDate(String date,int day)
    {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
            calendar.set(Calendar.DAY_OF_YEAR,day);
            return dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public BookingResult getBookingInfo(FlightSearchParam param) {
        return null;
    }
}
