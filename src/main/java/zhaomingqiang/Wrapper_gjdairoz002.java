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
 * Date: 14-6-30
 * Time: 下午8:38
 * 韩亚航空
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjdairoz002 implements QunarCrawler {
    private static final String CODEBASE = "gjdairoz002";
    private QFHttpClient httpClient = null;
    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate().replaceAll("-", "");
        PostMethod post=null,post1=null;
        try {
            httpClient = new QFHttpClient(flightSearchParam,true);
            httpClient.getParams().setCookiePolicy(
                    CookiePolicy.BROWSER_COMPATIBILITY);
            NameValuePair[] names={  new NameValuePair("tripType","OW"),
                    new NameValuePair("domIntType","I"),
                    new NameValuePair("cabinClass","T"),
                    new NameValuePair("fareViewType","C"),
                    new NameValuePair("departureArea","CN"),
                    new NameValuePair("departureAirport",dep),
                    new NameValuePair("arrivalArea",""),
                    new NameValuePair("arrivalAirport",arr),
                    new NameValuePair("departureDate",depDate),
                    new NameValuePair("arrivalDate",""),
                    new NameValuePair("adultCount","1"),
                    new NameValuePair("childCount","0"),
                    new NameValuePair("infantCount","0"),
                    new NameValuePair("paymentCNType","AL"),
                    new NameValuePair("sessionUniqueKey","undefined")};
            post = new QFPostMethod("http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenCalendarSelect.do");
            post.setRequestBody(names);
            post.setRequestHeader("Host","cn.flyasiana.com");
            post.setRequestHeader("Referer", "http://cn.flyasiana.com/C/ch/main.do");
            post.getParams().setContentCharset("UTF-8");
            throwExceptionByResponseCode(httpClient.executeMethod(post),200);
            String result= post.getResponseBodyAsString();
            result= StringUtils.substringBetween(result, "tableType02 one-ticket", "</table>");
            String result1= StringUtils.substringBetween(result, "<td class=\"gline col1 row4\"", "</td>");
            if(result1.contains("class=\"userNoneInner\""))
            {
                return Constants.INVALID_DATE;
            }
            else
            {
                result=StringUtils.substringBetween(result1,"id=\"delegateFare3\">","</strong>").replaceAll(",","")+"$$";
            }
            post1 = new PostMethod("http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenFlightSelect.do");
            NameValuePair[] names1={ new NameValuePair("sessionUniqueKey","undefined"),
                    new NameValuePair("departureDate",depDate),
                    new NameValuePair("arrivalDate",""),
                    new NameValuePair("departureArea","CN"),
                    new NameValuePair("arrivalArea",""),
                    new NameValuePair("departureAirport",dep),
                    new NameValuePair("arrivalAirport",arr),
                    new NameValuePair("tripType","OW"),
                    new NameValuePair("domIntType","I"),
                    new NameValuePair("cabinClass","T"),
                    new NameValuePair("openDepartureArea1","CN"),
                    new NameValuePair("openDepartureAirport1",dep),
                    new NameValuePair("openArrivalArea1",""),
                    new NameValuePair("openArrivalAirport1",arr),
                    new NameValuePair("selDepartureDate",depDate),
                    new NameValuePair("selArrivalDate",""),
                    new NameValuePair("hidCallPage","CAL_OF_INT"),
                    new NameValuePair("hidPageType","S")};
            post1.setRequestBody(names1);
            post1.setRequestHeader("Host","cn.flyasiana.com");
            post1.setRequestHeader("Referer", "http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenCalendarSelect.do");
            post1.getParams().setContentCharset("UTF-8");
            throwExceptionByResponseCode(httpClient.executeMethod(post1), 200);
            result+= post1.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
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
        List<OneWayFlightInfo> data = (List<OneWayFlightInfo>) processResultInfo.getData();
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
                Date depDate=new SimpleDateFormat("yyyy/MM/dd").parse(StringUtils.substringBetween(result, "<span class='day'>", "("));
                String monetaryunit=priceAndUtil.substring(0,3);
                double price= Double.valueOf(priceAndUtil.substring(3));
                result= StringUtils.substringBetween(result, "tdFlightList0", "</table>");
                result=StringUtils.substringBetween(result, "<tbody>", "</tbody>");
                String trs[]=StringUtils.substringsBetween(result,"<tr","</tr>");
                Pattern pNo = Pattern.compile(">(\\w+\\d+)  </span>");
                Pattern pTime=Pattern.compile("(\\d{2}:\\d{2})( \\[\\+(\\d)\\])?");
                Pattern pCity=Pattern.compile(" \\(([A-Z]{3})\\)");

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
                    flightDetail.setDepdate(depDate);
                    FlightSegement flightSegement=null;
                    for(int i=0,len=noList.size();i<len;i++)
                    {
                        flightSegement=new FlightSegement();
                        if(len>1)
                        {
                            flightSegement.setDepairport(cityList.get(i));
                            flightSegement.setArrairport(cityList.get(i+2));
                            flightSegement.setFlightno(noList.get(i));
                            handleDepDate(flightSegement,depDate,timeList.get(i),true);
                            handleDepDate(flightSegement,depDate,timeList.get(i+2),false);
                        }
                        else
                        {
                            flightSegement.setDepairport(cityList.get(0));
                            flightSegement.setArrairport(cityList.get(1));
                            flightSegement.setFlightno(noList.get(0));
                            handleDepDate(flightSegement,depDate,timeList.get(0),true);
                            handleDepDate(flightSegement,depDate,timeList.get(1),false);
                        }
                        info.add(flightSegement);
                    }
                    oneWayFlightInfo.setInfo(info);
                    data.add(oneWayFlightInfo);
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
        String depDate=flightSearchParam.getDepDate();
        BookingInfo bookingInfo=new BookingInfo();
        bookingInfo.setAction("http://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenFlightSelect.do");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("post");
        java.util.Map<String,String> inputs =new HashMap<String, String>();
        inputs.put("tripType","OW");
        inputs.put("domIntType","I");
        inputs.put("cabinClass","T");
        inputs.put("fareViewType","C");
        inputs.put("departureArea","CN");
        inputs.put("departureAirport",dep);
        inputs.put("arrivalArea","");
        inputs.put("arrivalAirport",arr);
        inputs.put("departureDate",depDate);
        inputs.put("arrivalDate","");
        inputs.put("adultCount","1");
        inputs.put("childCount","0");
        inputs.put("infantCount","0");
        inputs.put("paymentCNType","AL");
        inputs.put("sessionUniqueKey","undefined");
        bookingInfo.setInputs(inputs);
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}
