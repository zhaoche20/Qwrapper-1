package zhaomingqiang;

import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-6-30
 * Time: 下午8:38
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
            result= post1.getResponseBodyAsString();
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
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        try
        {
            String result=getHtml(param);
            if(result.equals(Constants.INVALID_DATE))
            {
                processResultInfo.setStatus(Constants.INVALID_DATE);
                processResultInfo.setData(data);
                return processResultInfo;
            }
            else if(html.equals("Exception"))
            {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                processResultInfo.setData(data);
                return processResultInfo;
            }
            else
            {
               result= StringUtils.substringBetween(result, "tdFlightList0", "</table>");





            }
        }catch (Exception e)
        {

        }
        return null;
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {
        return null;
    }

    public static void main(String...args)
    {
        Wrapper_gjdairoz002 wrapper_gjdairoz002=new Wrapper_gjdairoz002();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-07");
        flightSearchParam.setDep("PVG");
        flightSearchParam.setArr("ICN"); //SIN
        System.out.println("-------------------------------");
        System.out.println(wrapper_gjdairoz002.getHtml(flightSearchParam));
        System.out.println("--------------------------------");
    }
}
