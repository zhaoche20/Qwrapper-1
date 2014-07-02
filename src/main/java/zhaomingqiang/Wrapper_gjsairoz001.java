package zhaomingqiang;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-1
 * Time: 下午8:03
 * 韩亚航空 往返
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjsairoz001 implements QunarCrawler {
    private static final String CODEBASE = "gjsairoz001";
    private QFHttpClient httpClient = null;
    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate().replaceAll("-", "");
        String retDate=flightSearchParam.getRetDate().replaceAll("-","");
        PostMethod post=null,post1=null;
        try {
            httpClient = new QFHttpClient(flightSearchParam,true);
            httpClient.getParams().setCookiePolicy(
                    CookiePolicy.BROWSER_COMPATIBILITY);
            NameValuePair[] names={
                    new NameValuePair("tripType","RT"),
                    new NameValuePair("domIntType","I"),
                    new NameValuePair("cabinClass","T"),
                    new NameValuePair("fareViewType","C"),
                    new NameValuePair("departureArea","CN"),
                    new NameValuePair("departureAirport",dep),
                    new NameValuePair("arrivalArea",""),
                    new NameValuePair("arrivalAirport",arr),
                    new NameValuePair("departureDate",depDate),
                    new NameValuePair("arrivalDate",retDate),
                    new NameValuePair("adultCount","1"),
                    new NameValuePair("childCount","0"),
                    new NameValuePair("infantCount","0"),
                    new NameValuePair("paymentCNType","AL"),
                    new NameValuePair("sessionUniqueKey","undefined")
            };
            post = new QFPostMethod("http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenCalendarSelect.do");
            post.setRequestBody(names);
            post.setRequestHeader("Host","cn.flyasiana.com");
            post.setRequestHeader("Referer", "http://cn.flyasiana.com/C/ch/main.do");
            post.getParams().setContentCharset("UTF-8");
            throwExceptionByResponseCode(httpClient.executeMethod(post),200);
            String result= post.getResponseBodyAsString();
            result= StringUtils.substringBetween(result, "class=\"tableType02\"", "</table>");
            String result1= StringUtils.substringBetween(result, "<td class=\"gline col4 row4\"", "</td>");
            if(result1.contains("class=\"userNoneInner\""))
            {
                return Constants.INVALID_DATE;
            }
            else
            {
                result1=result1.replaceAll("\\s","");
                result=StringUtils.substringBetween(result1,"<ahref=\"#none\">","<strong")+StringUtils.substringBetween(result1,"id=\"delegateFare24\">","</strong>").replaceAll(",","")+"$$";
            }
            post1 = new PostMethod("http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenFlightSelect.do");
            NameValuePair[] names1={
                    new NameValuePair("sessionUniqueKey","undefined"),
                    new NameValuePair("departureDate",depDate),
                    new NameValuePair("arrivalDate",retDate),
                    new NameValuePair("departureArea","CN"),
                    new NameValuePair("arrivalArea",""),
                    new NameValuePair("departureAirport",dep),
                    new NameValuePair("arrivalAirport",arr),
                    new NameValuePair("tripType","RT"),
                    new NameValuePair("domIntType","I"),
                    new NameValuePair("cabinClass","T"),
                    new NameValuePair("openDepartureArea1","CN"),
                    new NameValuePair("openDepartureAirport1",dep),
                    new NameValuePair("openArrivalArea1",""),
                    new NameValuePair("openArrivalAirport1",arr),
                    new NameValuePair("openDepartureArea2",""),
                    new NameValuePair("openDepartureAirport2",dep),
                    new NameValuePair("openArrivalArea2","CN"),
                    new NameValuePair("openArrivalAirport2",arr),
                    new NameValuePair("selDepartureDate",depDate),
                    new NameValuePair("selArrivalDate",retDate),
                    new NameValuePair("hidCallPage","CAL_OF_INT"),
                    new NameValuePair("hidPageType","S")
            };
            post1.setRequestBody(names1);
            post1.setRequestHeader("Host","cn.flyasiana.com");
            post1.setRequestHeader("Referer", "http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenCalendarSelect.do");
            post1.getParams().setContentCharset("UTF-8");
            throwExceptionByResponseCode(httpClient.executeMethod(post1), 200);
            result+= post1.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }finally{
            if(post1!=null){
                post1.releaseConnection();
            }
            if(post!=null){
                post.releaseConnection();
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
    @Override
    public ProcessResultInfo process(String html, FlightSearchParam param) {
        ProcessResultInfo processResultInfo=new ProcessResultInfo();
        List<RoundTripFlightInfo> data = (List<RoundTripFlightInfo>) processResultInfo.getData();
        try
        {
            String result=getHtml(param);
            if(result.equals(Constants.INVALID_DATE) || result.indexOf("tdFlightList0")==-1)
            {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                return processResultInfo;
            }
            else if(html.equals("Exception"))
            {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                return processResultInfo;
            }
            else
            {
                String priceAndUtil=result.substring(0,result.indexOf("$$"));
                String monetaryunit=priceAndUtil.substring(0,3);
                double price= Double.valueOf(priceAndUtil.substring(3));
                String[] airlineDate=StringUtils.substringsBetween(result, "<span class='day'>", "(");
                Date depDate=new SimpleDateFormat("yyyy/MM/dd").parse(airlineDate[0]);
                Date retDate=new SimpleDateFormat("yyyy/MM/dd").parse(airlineDate[1]);
                String tbody0=StringUtils.substringBetween(StringUtils.substringBetween(result, "tdFlightList0", "</table>"), "<tbody>", "</tbody>");
                String tbody1=StringUtils.substringBetween(StringUtils.substringBetween(result, "tdFlightList1", "</table>"), "<tbody>", "</tbody>");
                Pattern pNo = Pattern.compile(">(\\w+\\d+)  </span>");
                Pattern pTime=Pattern.compile("(\\d{2}:\\d{2})( \\[\\+(\\d)\\])?");
                Pattern pCity=Pattern.compile(" \\(([A-Z]{3})\\)");
                List<OneWayFlightInfo> depList=getOneWayFlightInfos(tbody0,monetaryunit,price,depDate,pNo,pTime,pCity);
                List<OneWayFlightInfo> retList=getOneWayFlightInfos(tbody1,monetaryunit,price,retDate,pNo,pTime,pCity);
                Iterator<OneWayFlightInfo> goIterator=depList.iterator();
                RoundTripFlightInfo roundTripFlightInfo=null;
                OneWayFlightInfo baseFlightInfo=null;
                OneWayFlightInfo backBaseFlightInfo=null;
                FlightDetail flightDetail=null,backFlightDetail=null;
                while(goIterator.hasNext())
                {
                    baseFlightInfo=goIterator.next();
                    flightDetail=baseFlightInfo.getDetail();
                    Iterator<OneWayFlightInfo> backIterator=retList.iterator();
                    while(backIterator.hasNext())
                    {
                        backBaseFlightInfo=backIterator.next();
                        roundTripFlightInfo=new RoundTripFlightInfo();
                        backFlightDetail=backBaseFlightInfo.getDetail();
                        roundTripFlightInfo.setRetinfo(backBaseFlightInfo.getInfo());
                        roundTripFlightInfo.setDetail(flightDetail);
                        roundTripFlightInfo.setInfo(baseFlightInfo.getInfo());
                        roundTripFlightInfo.setRetflightno(backFlightDetail.getFlightno());
                        roundTripFlightInfo.setRetdepdate(backBaseFlightInfo.getDetail().getDepdate());
                        data.add(roundTripFlightInfo);
                    }
                }
                if(data.isEmpty())
                {
                    processResultInfo.setStatus(Constants.INVALID_DATE);
                }
                else
                {
                    processResultInfo.setStatus(Constants.SUCCESS);
                }
                return processResultInfo;
            }
        }catch (Exception e)
        {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
    }

    public List<OneWayFlightInfo> getOneWayFlightInfos(String tbody,String monetaryunit,double price,Date date,Pattern pNo,Pattern pTime,Pattern pCity)
    {
        List<OneWayFlightInfo> list=new ArrayList<OneWayFlightInfo>();
        String trs[]=StringUtils.substringsBetween(tbody,"<tr","</tr>");
        for(int j=0,lens=trs.length;j<lens;j++)
        {
            String tr=trs[j];
                    /*if(tr.contains("class=\"goNone\""))
                    {
                        continue;
                    }*/
            List<String> noList=new ArrayList<String>();
            List<String> timeList=new ArrayList<String>();
            List<String> cityList=new ArrayList<String>();
            Matcher matcher=pNo.matcher(tr);
            while(matcher.find())
            {
                noList.add(matcher.group(1));
            }
            Matcher matcher1=pTime.matcher(tr);
            while(matcher1.find())
            {
                timeList.add(matcher1.group());
            }
            Matcher matcher2=pCity.matcher(tr);
            while (matcher2.find())
            {
                cityList.add(matcher2.group(1));
            }
            OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
            List<FlightSegement> info=new ArrayList<FlightSegement>();
            FlightDetail flightDetail=oneWayFlightInfo.getDetail();
            flightDetail.setMonetaryunit(monetaryunit);
            flightDetail.setPrice(price);
            flightDetail.setArrcity(cityList.get(cityList.size()-1));
            flightDetail.setDepcity(cityList.get(0));
            flightDetail.setFlightno(noList);
            flightDetail.setDepdate(date);
            FlightSegement flightSegement=null;
            for(int i=0,len=noList.size();i<len;i++)
            {
                flightSegement=new FlightSegement();
                if(len>1)
                {
                    flightSegement.setDepairport(cityList.get(i));
                    flightSegement.setArrairport(cityList.get(i+2));
                    flightSegement.setFlightno(noList.get(i));
                    handleDepDate(flightSegement,date,timeList.get(i),true);
                    handleDepDate(flightSegement,date,timeList.get(i+2),false);
                }
                else
                {
                    flightSegement.setDepairport(cityList.get(0));
                    flightSegement.setArrairport(cityList.get(1));
                    flightSegement.setFlightno(noList.get(0));
                    handleDepDate(flightSegement,date,timeList.get(0),true);
                    handleDepDate(flightSegement,date,timeList.get(1),false);
                }
                info.add(flightSegement);
            }
            oneWayFlightInfo.setInfo(info);
            list.add(oneWayFlightInfo);
        }
        return  list;
    }
    private void handleDepDate(FlightSegement flightSegement,Date date,String s,boolean isDepDate)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        if(s.indexOf("[+")!=-1)
        {
            int day=Integer.valueOf(StringUtils.substringBetween(s,"[+","]"));
            calendar.add(Calendar.DAY_OF_YEAR,day);
        }
        if(isDepDate)
        {
            flightSegement.setDeptime(s.substring(0,5));
            flightSegement.setDepDate(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
        else
        {
            flightSegement.setArrtime(s.substring(0,5));
            flightSegement.setArrDate(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    }
    @Override
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult=new BookingResult();
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate().replaceAll("-", "");
        String retDate=flightSearchParam.getRetDate().replaceAll("-","");
        BookingInfo bookingInfo=new BookingInfo();
        bookingInfo.setAction("http://cn.flyasiana.com");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("get");
        /*
        java.util.Map<String,String> inputs =new HashMap<String, String>();
        inputs.put("sessionUniqueKey","undefined");
        inputs.put("departureDate",depDate);
        inputs.put("arrivalDate",retDate);
        inputs.put("departureArea","CN");
        inputs.put("arrivalArea","");
        inputs.put("departureAirport",dep);
        inputs.put("arrivalAirport",arr);
        inputs.put("tripType","RT");
        inputs.put("domIntType","I");
        inputs.put("cabinClass","T");
        inputs.put("openDepartureArea1","CN");
        inputs.put("openDepartureAirport1",dep);
        inputs.put("openArrivalArea1","");
        inputs.put("openArrivalAirport1",arr);
        inputs.put("openDepartureArea2","");
        inputs.put("openDepartureAirport2",dep);
        inputs.put("openArrivalArea2","CN");
        inputs.put("openArrivalAirport2",arr);
        inputs.put("selDepartureDate",depDate);
        inputs.put("selArrivalDate",retDate);
        inputs.put("hidCallPage","CAL_OF_INT");
        inputs.put("hidPageType","S");
        bookingInfo.setInputs(inputs);
        */
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}
