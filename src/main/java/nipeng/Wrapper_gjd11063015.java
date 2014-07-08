package nipeng;

import com.google.common.collect.Lists;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFGetMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peng.ni on 14-7-8.
 */
public class Wrapper_gjd11063015 implements QunarCrawler {

//    private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
    private static final String CODEBASE = "gjd11063015";
    @Override
    public String getHtml(FlightSearchParam param) {

        QFHttpClient httpClient = new QFHttpClient(param, false);
        QFGetMethod get=null;
        QFGetMethod get2=null;
        String dates =param.getDepDate().replaceAll("(\\d{4})-(\\d{2})-(\\d{2})", "$3/$2/$1");
        QFPostMethod postMethod = new QFPostMethod("http://booking.alitalia.com/booking/CN_ZH/");
        String body = "Type=BrandSearch&Itinerary=OneWay&Destinations.DepartureAirport="+param.getDep()+"&Destinations.ArrivalAirport="+param.getArr()+"&SearchMod=on&Destinations.From=&Destinations.To=&Destinations.DepartureDate="+dates+"&Destinations.ReturnDate="+dates+"&FlexDate=false&OnlyDirect=false&Class=Economy&AdultNumb=1&ChildNumb=0&InfantNumb=0";
        try {
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            postMethod.setRequestEntity(new ByteArrayRequestEntity(body.getBytes()));
            httpClient.executeMethod(postMethod);
            Header locationHeader = postMethod.getResponseHeader("location");
            if (locationHeader != null) {
                String location = locationHeader.getValue();
                String tmpCookies = "";
                Header[] headers = postMethod.getResponseHeaders("Set-Cookie");
                for (Header header : headers) {
                    tmpCookies += header.getValue();
                }
                System.out.println("**************************************");
                System.out.println("http://booking.alitalia.com" + location);
                System.out.println(tmpCookies);
                get = new QFGetMethod("http://booking.alitalia.com" + location);
                get.setRequestHeader("cookie", tmpCookies);
                get.setRequestHeader("Referer","http://www.alitalia.com/CN_ZH/home/index.aspx");
                httpClient.executeMethod(get);
            }

            return get.getResponseBodyAsString();


        } catch (Exception e) {
//            if (!e.getMessage().equals("Connection refused: connect"))
//                ProxyService.delProxyHost(PROXYKEY, httpClient);
//            logger.error("ErrorIn " + CODEBASE + " : " + url, e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return "Exception";
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam param) {

        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = Lists.newArrayList();

        if ("Exception".equals(html)) {
//            Collector.count(CODEBASE, WrapperCounter.Key.CONNECTION_FAIL);
//            ecLogger.warn(CODEBASE + "," + WrapperCounter.Key.CONNECTION_FAIL + "," + url);
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            return processResultInfo;
        }
//        StringBuilder builder = new StringBuilder("<table>");
        String []flights = html.split("<tr class=\"routedetail-tr routedetail-tr-top");
//        if(flights.length<3){
//            return "";
//        }
        int invalidateCount = 0;
        int count = 0;
        for (int i = 1; i < flights.length; i++) {
            if(!flights[i].contains("OutboundSolution")){
               count++;
               continue;
            }

            OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
            FlightDetail detail=new FlightDetail();
            List<FlightSegement> flightSegements= Lists.newArrayList();

            String []codes = getValues(flights[i], "\"\" /><br />", "</td>");
            String []deps = getValues(flights[i], "<td class=\"routedetail_departure\">", "<br />");
            String []arrs = getValues(flights[i], "<td class=\"routedetail_arrival\">", "<br />");
            String []times = getValues(flights[i], "<b>", "</b>");
            String []radios = getValues(flights[i], "type=\"radio\" value=\"","\"");

            for(int j=0;j<codes.length;j++)
            {
                FlightSegement fs=new FlightSegement();
                if(j==0){
                    fs.setDepDate(param.getDepDate());
                    fs.setArrDate(param.getDepDate());
                }
                else
                {
                    fs.setDepDate("1970-01-01");
                    fs.setArrDate("1970-01-01");
                }

                fs.setDeptime(times[j*2].trim());
                fs.setArrtime(times[j*2+1].trim());
                fs.setFlightno(org.apache.commons.lang.StringUtils.deleteWhitespace(codes[j]));
                fs.setCompany(fs.getFlightno().substring(0,3));
                //fs.setDepairport(deps[j].trim());
                //fs.setArrairport(arrs[j].trim());
                if(j==0)
                {
                    fs.setDepairport(param.getDep());
                    fs.setArrairport("XXX");
                }
                else if(j==codes.length-1)
                {
                    fs.setDepairport("XXX");
                    fs.setArrairport(param.getArr());
                }
                else
                {
                    fs.setDepairport("XXX");
                    fs.setArrairport("XXX");
                }
                flightSegements.add(fs);
            }

              String code = org.apache.commons.lang.StringUtils.join(codes,"/").replaceAll("\\s", "");
//            String departure = deps[0].replaceAll("\\s", "").split(",")[0];
//            String arrival = arrs[arrs.length-1].replaceAll("\\s", "").split(",")[0];
//            String dept = times[0].trim();
//            String arrt = times[times.length-1].trim();
            String [] temp = radios[0].split("\\|");
            if(temp.length==1){
                count++;
                continue;
            }
            String currency = temp[3];
            float price = Float.parseFloat(temp[1]);
            float tax = Float.parseFloat(temp[2])-price;

            List<String> flightno= Lists.newArrayList();
            for(FlightSegement fss:flightSegements)
            {
                flightno.add(fss.getFlightno());
            }
            detail.setFlightno(flightno);
            detail.setWrapperid(CODEBASE);
            detail.setDepcity(param.getDep());
            detail.setArrcity(param.getArr());
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = sdf.parse(param.getDepDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            detail.setDepdate(date);
            detail.setMonetaryunit(currency);
            detail.setTax(tax);
            detail.setPrice(price);
            oneWayFlightInfo.setDetail(detail);
            oneWayFlightInfo.setInfo(flightSegements);
            data.add(oneWayFlightInfo);

            if(code.contains("<")){
//                Collector.count(CODEBASE, WrapperCounter.Key.TEMP);
                count++;
                continue;
            }
            if(!code.contains("AZ") && (code.contains("AF") || code.contains("KL"))){
                invalidateCount ++;
                continue;
            }


        }
        if ((invalidateCount+count) == flights.length-1){
            //System.out.println("hhh");
//            Collector.count(CODEBASE, WrapperCounter.Key.TEMP1);
//            logger.error("Codebase: " + CODEBASE + " invalidate airline ");
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.INVALID_AIRLINE);
            return processResultInfo;
        }
//        return builder.append("</table>").toString();

        processResultInfo.setData(data);
        processResultInfo.setRet(true);
        processResultInfo.setStatus(Constants.SUCCESS);
        return processResultInfo;
    }


    public String[] getValues(String source, String st, String end) {
        String target = "";
        int a, b;
        while (true) {
            a = source.indexOf(st);
            if (a == -1)
                break;
            b = source.indexOf(end, a + st.length());
            if (b == -1)
                break;
            target += source.substring(a + st.length(), b) + "##@@##";
            source = source.substring(b);
        }
        return target.split("##@@##");
    }

    public String getValue(String source, String regEx) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
    }

    public String getValue(String source, String st, String end) {
        int a = source.indexOf(st);
        if (a == -1)
            return "";
        int b = source.indexOf(end, a + st.length());
        if (b == -1)
            return "";
        return source.substring(a + st.length(), b);
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {

        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setMethod("post");
        bookingInfo.setAction("http://booking.alitalia.com/booking/CN_ZH/");
        String dates[] = param.getDepDate().split("-");
        String date2 = dates[2]+"/"+dates[1]+"/"+dates[0];
        java.util.Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("Type", "BrandSearch");
        inputs.put("Itinerary", "OneWay");
        inputs.put("Destinations.DepartureAirport", param.getDep());
        inputs.put("Destinations.ArrivalAirport", param.getArr());
        inputs.put("SearchMod", "on");
        inputs.put("Destinations.From","");
        inputs.put("Destinations.To","");
        inputs.put("Destinations.DepartureDate",date2);
        inputs.put("Destinations.ReturnDate",date2);
        inputs.put("FlexDate", "false");
        inputs.put("OnlyDirect", "false");
        inputs.put("Class", "Economy");
        inputs.put("AdultNumb", "1");
        inputs.put("ChildNumb", "0");
        inputs.put("InfantNumb", "0");

        bookingResult.setRet(true);
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    public static void main(String[] args)
    {
        FlightSearchParam f = new FlightSearchParam();
        f.setArr("ROM");
        f.setDep("PAR");
        f.setDepDate("2014-07-25");
        f.setRetDate("2014-07-25");
        Wrapper_gjd11063015 wr = new Wrapper_gjd11063015();
        String sw = wr.getHtml(f);
        System.out.println(sw);
        wr.process(sw,f);
//        ProcessResultInfo pri = wr.process(sw, f);
    }
}
