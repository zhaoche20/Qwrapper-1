package zhaomingqiang;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.qunar.qfwrapper.util.TimeUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mingqiang.zhao on 2014/5/14.
 * 宿务太平洋航空
 * 单向飞行
 */
public class Wrapper_gjdair5j001 implements QunarCrawler {
    /**
     * 航班查询对应的URL
     */
    private static final String SEARCHURL="https://book.cebupacificair.com/Search.aspx";
    private static final String SEARCHURLBYBOOKING="https://book.cebupacificair.com/Search.aspx";
    /**
     * 航班查询的结果对应的URL
     */
    private static final String SELECTURL="https://book.cebupacificair.com/Select.aspx";
    /**
     * 税费与总价对应的URL
     */
    private static final String TAXANDPRICE="https://book.cebupacificair.com/TaxAndFeeInclusiveDisplayAjax-resource.aspx?" +
            "numberOfMarkets=1&keyDelimeter=%2C&flightKeys=";
    private static final String CODEBASE = "gjdair5j001";
    private QFHttpClient httpClient = null;
    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate();
        String[] tempDate=depDate.split("-");
        PostMethod post=null;
        GetMethod get =null;
        try {
            httpClient = new QFHttpClient(flightSearchParam,true);
            httpClient.getParams().setParameter("Use-Https","1");
            NameValuePair[] nameValuePairs = {
                    new NameValuePair("__EVENTTARGET","ControlGroupSearchView$AvailabilitySearchInputSearchView$LinkButtonNewSearch"),
                    new NameValuePair("__EVENTARGUMENT",""),
                    new NameValuePair("__VIEWSTATE",""),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$RadioButtonMarketStructure","OneWay"),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketOrigin1",dep),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketDestination1",arr),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDay1",tempDate[2]),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketMonth1",tempDate[0]+"-"+tempDate[1]),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_ADT","1"),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_CHD","0"),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_INFANT","0"),
                    new NameValuePair("ControlGroupSearchView$AvailabilitySearchInputSearchView$promoCodeID","")
            };
            post = new QFPostMethod(SEARCHURL);
            post.setRequestBody(nameValuePairs);
            throwExceptionByResponseCode(httpClient.executeMethod(post),302);
            get = new QFGetMethod(SELECTURL);
            throwExceptionByResponseCode(httpClient.executeMethod(get),200);
            String result= get.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
            return "Exception";
        }finally{
            if(get!=null){
                get.releaseConnection();
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
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo=new ProcessResultInfo();
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        try
        {
            if(html.equals("Exception") || html.startsWith("QProxy"))
            {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                processResultInfo.setData(data);
                return processResultInfo;
            }

            int tableIndex=html.indexOf("<table id=\"availabilityTable\"");
            if(tableIndex==-1 )
            {
                processResultInfo.setStatus(Constants.NO_RESULT);
                processResultInfo.setData(data);
                return processResultInfo;
            }
            String table=html.substring(tableIndex, html.indexOf("</table>", tableIndex)).replaceAll("\r|\n", "");
            if(table.indexOf("tr")==-1)
            {
                processResultInfo.setStatus(Constants.NO_RESULT);
                processResultInfo.setData(data);
                return processResultInfo;
            }
            int trIndex=-1 , trOffset=0;
            String tr=null;
            while((trIndex=table.indexOf("<tr",trOffset))!=-1)
            {
                trOffset=table.indexOf("</tr>",trIndex);
                tr=table.substring(trIndex,trOffset);
                String redioFlag="name=\"ControlGroupSelectView$AvailabilityInputSelectView$market";
                int redioIndex=tr.indexOf(redioFlag);
                redioIndex=tr.indexOf("value=\"",redioIndex)+7;
                //航班片段信息
                String flightSegement=tr.substring(redioIndex, tr.indexOf("\">", redioIndex));
                // ~~PUS~05/16/2014 21:40~MNL~05/17/2014 00:15~^5J~5054~ ~~MNL~05/17/2014 05:25~NRT~05/17/2014 10:35~
                OneWayFlightInfo oneWayFlightInfo=getBaseFlightInfo(flightSegement);
                if(oneWayFlightInfo!=null)
                {
                    data.add(oneWayFlightInfo);
                }
            }
            processResultInfo.setData(data);
            if(data.isEmpty()) {
                processResultInfo.setStatus(Constants.NO_RESULT);
            }
            else
            {
                processResultInfo.setStatus(Constants.SUCCESS);
            }
            return processResultInfo;
        }
        catch (IOException e)
        {
            if(data.isEmpty())
            {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            }
            else
            {
                processResultInfo.setStatus(Constants.SUCCESS);
            }
            processResultInfo.setData(data);
            return processResultInfo;
        }
        catch (Exception e)
        {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(data);
            return processResultInfo;
        }
    }
    private String[] taxAndPrice(String s) throws IOException {
        String[] taxAndPrice=new String[3];
        String url= null;
        try {
            url = TAXANDPRICE+java.net.URLEncoder.encode(s,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        GetMethod get = new QFGetMethod(url);
        try {
            throwExceptionByResponseCode(httpClient.executeMethod(get),200);
            String html = get.getResponseBodyAsString();
            //logger.info(CODEBASE+".taxAndPrice"+"\n"+html);
            int monetaryunitIndex = html.indexOf("Price Details (") + 15;
            String monetaryunit = html.substring(monetaryunitIndex, html.indexOf(")", monetaryunitIndex));
            taxAndPrice[0] = monetaryunit;
            int priceIndex = html.indexOf(">", html.indexOf("<span id=\"faresSubTotal\"")) + 1;
            String price_s = html.substring(priceIndex, html.indexOf("</span", priceIndex)).replaceAll(",", "");
            taxAndPrice[2] = price_s;
            int taxIndex = html.indexOf(">", html.indexOf("id=\"taxesFaresTotal\"")) + 1;
            String tax_s = html.substring(taxIndex, html.indexOf("</span", taxIndex)).replaceAll(",", "");
            taxAndPrice[1] = tax_s;
        } catch (IOException e) {
            throw e ;
        }finally{
            if(get!=null){
                get.releaseConnection();
            }
        }
        return taxAndPrice;
    }
    public OneWayFlightInfo getBaseFlightInfo(String s) throws IOException {
        int j5=s.indexOf("5J");
        if(j5==-1)
        {
            return null;
        }
        String [] taxAndPrice= taxAndPrice(s);
        if(taxAndPrice==null)
        {
            return null;
        }
        OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
        s=s.substring(s.indexOf("5J"));
        Pattern pCity = Pattern.compile("~[A-Z]{3}~");
        Matcher mCity = pCity.matcher(s);
        int segement=0;
        for(;mCity.find();)
        {
            segement++;
        }
        String airp[]=new String[segement];
        mCity.reset();
        for(int j=0;mCity.find();j++)
        {
            String group=mCity.group();
            airp[j]=group.substring(1,group.length()-1);
        }
        String dates[]=new String[segement];
        Pattern pDates = Pattern.compile("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}");
        Matcher mDates = pDates.matcher(s);
        for(int j=0;mDates.find();j++)
        {
            dates[j]=mDates.group();
        }
        String[] no=new String[segement/2];
        Pattern pNo = Pattern.compile("\\w{2}~[0-9a-zA-Z ]+~ ");
        Matcher mNo = pNo.matcher(s);
        for(int j=0;mNo.find();j++)
        {
            String group=mNo.group();
            no[j]=group.replaceAll("~","").replaceAll(" ","");
        }
        List<FlightSegement> flightSegements=new ArrayList<FlightSegement>();
        FlightSegement flightSegement=null;
        int iTemp=0,dTemp=0;
        for(int i=0;i<segement/2;i++)
        {
            flightSegement=new FlightSegement();
            flightSegement.setDepairport(airp[iTemp++]);
            flightSegement.setArrairport(airp[iTemp++]);
            String[] dataAndTimeS=getDateAndTime(dates[dTemp++]);
            flightSegement.setDepDate(dataAndTimeS[0]);
            flightSegement.setDeptime(dataAndTimeS[1]);
            String[] dataAndTimeO=getDateAndTime(dates[dTemp++]);
            flightSegement.setArrDate(dataAndTimeO[0]);
            flightSegement.setArrtime(dataAndTimeO[1]);
            flightSegement.setFlightno(no[i]);
            flightSegements.add(flightSegement);
        }
        FlightDetail flightDetail=new FlightDetail();
        flightDetail.setArrcity(airp[airp.length-1]);
        flightDetail.setDepcity(airp[0]);
        flightDetail.setDepdate(TimeUtil.getDateByString(getDateAndTime(dates[0])[0]));
        flightDetail.setWrapperid(CODEBASE);
        flightDetail.setFlightno(Arrays.asList(no));
        flightDetail.setMonetaryunit(taxAndPrice[0]);
        double price=Double.valueOf(taxAndPrice[2]);
        double tax=Double.valueOf(taxAndPrice[1]);
        flightDetail.setPrice(price);
        flightDetail.setTax(tax);
        oneWayFlightInfo.setDetail(flightDetail);
        oneWayFlightInfo.setInfo(flightSegements);
        return oneWayFlightInfo;

    }
    private String[] getDateAndTime(String s)
    {
        String[] dateAndTime=s.split(" ");
        String mdy[]=dateAndTime[0].split("/");
        dateAndTime[0]=mdy[2]+"-"+mdy[0]+"-"+mdy[1];
        return dateAndTime;
    }
    @Override
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult=new BookingResult();
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate();
        String[] tempDate=depDate.split("-");
        BookingInfo bookingInfo=new BookingInfo();
        bookingInfo.setAction(SEARCHURLBYBOOKING);
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("post");
        java.util.Map<String,String> inputs =new HashMap<String, String>();
        inputs.put("__EVENTTARGET","ControlGroupSearchView$AvailabilitySearchInputSearchView$LinkButtonNewSearch");
        inputs.put("__EVENTARGUMENT","");
        inputs.put("__VIEWSTATE","");
        inputs.put("pageToken","R%2FVM2Ws3%2F8E%3D&");
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24RadioButtonMarketStructure","OneWay");
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24TextBoxMarketOrigin1",dep);
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24TextBoxMarketDestination1",arr);
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24DropDownListMarketDay1",tempDate[2]);
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24DropDownListMarketMonth1",tempDate[0]
                +"-"+tempDate[1]);
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24DropDownListPassengerType_ADT","1");
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24DropDownListPassengerType_CHD","0");
        inputs.put("ControlGroupSearchView%24AvailabilitySearchInputSearchView%24DropDownListPassengerType_INFANT","0");
        inputs.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$promoCodeID", "");
        bookingInfo.setInputs(inputs);
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}




