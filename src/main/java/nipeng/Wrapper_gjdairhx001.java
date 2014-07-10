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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by peng.ni on 14-6-30.
 */
public class Wrapper_gjdairhx001 implements QunarCrawler {

    private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
    private static final String CODEBASE = "gjdairhx001";
    private static final String url="http://book.hongkongairlines.com";
    private String cookies;
    private List<String> priceInfoMain=null;

    @Override
    public String getHtml(FlightSearchParam param) {

        QFHttpClient httpClient = new QFHttpClient(param, false);
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        String getUrl = new String("http://book.hongkongairlines.com/hxet/reservation/AVQuery.do");
        QFGetMethod get = new QFGetMethod(getUrl);
        String queryString = new String("language=CN" +
                "&orgcity=" + param.getDep() +
                "&dstcity=" + param.getArr() +
                "&takeoffDate=" + param.getDepDate() +
                "&cabinType=ECONOMY" +
                "&adultCount=1" +
                "&childCount=0" +
                "&tripType=OW");
        get.setQueryString(queryString);

        String postUrl = new String("http://book.hongkongairlines.com/hxet/reservation/AVQuery.do");
        QFPostMethod post = new QFPostMethod(postUrl);


        try {
            httpClient.executeMethod(get);

            Header[] headers = get.getResponseHeaders("Set-Cookie");
            for(int i=0;i<headers.length;i++)
            {
                cookies+=headers[i].getValue();
            }

            String tmp = get.getResponseBodyAsString();
//            System.out.println("****************************");
//            System.out.println(tmp);
//            System.out.println("****************************");
            String random = getValue(tmp, "<input type=\"hidden\"  name=\"random\" value='", "' />");
            String postBody = new String("orgcitycode=" + param.getDep() +
                    "&orgcity=" + param.getDep() +
                    "&dstcity=" + param.getArr() +
                    "&takeoffDate=" + param.getDepDate() +
                    "&returnDate=" +
                    "&tripType=OW" +
                    "&cabinType=ECONOMY" +
                    "&adultCount=1" +
                    "&childCount=0" +
                    "&infantCount=0" +
                    "&area=" + //MAINLAND_CHINA" +//
                    "&sureDate=1" +
                    "&currencyCode=" + //CNY" +
                    "&farefamilyname=" +
                    "&email=" +
                    "&language=CN" +
                    "&random=" + random);
            post.setQueryString(postBody);
            httpClient.executeMethod(post);



            tmp = post.getResponseBodyAsString();
//            System.out.println("****************************");
//            System.out.println(tmp);
//            System.out.println("****************************");

            return tmp;

        } catch (Exception e) {
            e.printStackTrace();
            if (!e.getMessage().equals("Connection refused: connect"))
                logger.error("ErrorIn " + CODEBASE + " : " + url, e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
            if (post != null) {
                post.releaseConnection();
            }
        }
        return "Exception";


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
    public ProcessResultInfo process(String html, FlightSearchParam param) {

        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = Lists.newArrayList();

        if (null == html){
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.NO_RESULT);
            return processResultInfo;
        }else if (html.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")){
//            return HxWebserviceDataProcess.processData(html, CODEBASE, url);
            //  System.out.println("**********************************");
            //    System.out.println("XML");
            //     System.out.println("**********************************");
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.NO_RESULT);
            return processResultInfo;


        }else {
            return processBodyFromAir(html, param);
        }
    }


    public ProcessResultInfo processBodyFromAir(String html, FlightSearchParam params) {
//        QFHttpClient httpClient = new QFHttpClient(params, false);
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<OneWayFlightInfo> data = Lists.newArrayList();

        if ("Exception".equals(html)) {
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            return processResultInfo;
        }
        if (html.contains("没有航班") || html.contains("售罄")) {
            processResultInfo.setData(data);
            processResultInfo.setRet(false);
            processResultInfo.setStatus(Constants.NO_RESULT);
            return processResultInfo;
        }

        String random = getValue(html, "<input type=\"hidden\"  name=\"random\" value='", "' />");
        String[] flightRawData=StringUtils.substringsBetween(html,"<tr id=\"trOW","<!-- end seg -->");
        for(String subData:flightRawData)
        {
            OneWayFlightInfo oneWayFlightInfo=new OneWayFlightInfo();
            List<FlightSegement> info=Lists.newArrayList();
            FlightDetail detail=new FlightDetail();

            String subFlightDataRaw=StringUtils.substringBetween(subData,"<input type=\"radio\" name=\"flightOW\" id=\"radio\"","/>");
            String subFlightDataJson=StringUtils.substringBetween(subFlightDataRaw," value=\"","\"");
            String[] flightNo=StringUtils.substringsBetween(subFlightDataJson,"flightNo:'","',");
            String[] depCity=StringUtils.substringsBetween(subFlightDataJson,"org_cityCode:'","',");
            String[] arrCity=StringUtils.substringsBetween(subFlightDataJson,"des_cityCode:'","',");
            String[] depDate=StringUtils.substringsBetween(subFlightDataJson,"departureDate:'","',");
            String[] arrDate=StringUtils.substringsBetween(subFlightDataJson,"arriveDate:'","',");
            String[] plane=StringUtils.substringsBetween(subFlightDataJson,"aircraft:'","',");
            String pek_departureDate=StringUtils.substringBetween(subData, "<input type=\"hidden\" name=\"pek_departureDate\" value=\"", "\"/>");
            String pek_arriveDate=StringUtils.substringBetween(subData, "<input type=\"hidden\" name=\"pek_arriveDate\" value=\"", "\"/>");
            //      System.out.println("=======================================================");
            // List<String> priceInfo=getPrice(html,params,random,subFlightDataJson,pek_departureDate,pek_arriveDate);
            if(priceInfoMain==null)
                priceInfoMain=getPrice(html,params,random,subFlightDataJson,pek_departureDate,pek_arriveDate);
            for(int i=0;i<flightNo.length;i++)
            {
                FlightSegement flightSegement=new FlightSegement();
                flightSegement.setDepairport(depCity[i]);
                flightSegement.setArrairport(arrCity[i]);
                flightSegement.setFlightno(flightNo[i]);
                flightSegement.setDepDate(depDate[i].split(" ")[0]);
                flightSegement.setDeptime(depDate[i].split(" ")[1].substring(0, 5));
                flightSegement.setArrDate(arrDate[i].split(" ")[0]);
                flightSegement.setArrtime(arrDate[i].split(" ")[1].substring(0,5));
                flightSegement.setCompany(flightNo[i].substring(0,3));
                flightSegement.setAircraft(plane[i]);
                info.add(flightSegement);
            }
            List<String> flightNoList=Lists.newArrayList();
            for(FlightSegement fs:info)
            {
                flightNoList.add(fs.getFlightno());
            }
            detail.setFlightno(flightNoList);

            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Date date=null;
            try {
                date=sdf.parse(params.getDepDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            detail.setDepdate(date);
            detail.setDepcity(params.getDep());
            detail.setArrcity(params.getArr());
            detail.setWrapperid(CODEBASE);

            if(priceInfoMain!=null)
            {

                detail.setMonetaryunit(priceInfoMain.get(0));
                if(priceInfoMain.get(2).contains(","))
                {
                    priceInfoMain.set(2,priceInfoMain.get(2).split(",")[0]+priceInfoMain.get(2).split(",")[1]);
                }else  if(priceInfoMain.get(1).contains(","))
                {
                    priceInfoMain.set(1, priceInfoMain.get(1).split(",")[0] + priceInfoMain.get(1).split(",")[1]);
                }
                detail.setPrice(Double.parseDouble(priceInfoMain.get(2)));
                detail.setTax(Double.parseDouble(priceInfoMain.get(1)));



            }
            oneWayFlightInfo.setDetail(detail);
            oneWayFlightInfo.setInfo(info);
            data.add(oneWayFlightInfo);
        }


        processResultInfo.setData(data);
        processResultInfo.setRet(true);
        processResultInfo.setStatus(Constants.SUCCESS);
        return processResultInfo;
    }

    public List<String> getPrice(String html,FlightSearchParam params,String random,String json,String pek_departureDate,String pek_arriveDate)
    {
        List<String> priceInfo=Lists.newArrayList();
        QFHttpClient httpClient=new QFHttpClient(params,false);
        QFPostMethod post=new QFPostMethod("https://book.hongkongairlines.com/hxet/reservation/forPassengerInput.do");
        NameValuePair data[]={  new NameValuePair("random",random),
                new NameValuePair("from","stopOver"),
                new NameValuePair("cabinType","ECONOMY"),
                new NameValuePair("adultCount","1"),
                new NameValuePair("childCount","0"),
                new NameValuePair("infantCount","0"),
                new NameValuePair("language","CN"),
                new NameValuePair("currencyCode",""),
                new NameValuePair("email",""),
                new NameValuePair("querySegSize","1"),
                new NameValuePair("org_cityCode",params.getDep()),
                new NameValuePair("des_cityCode",params.getArr()),
                new NameValuePair("tripType","OW"),
                new NameValuePair("departureDate",params.getDepDate()),
                new NameValuePair("returnDate",""),
                new NameValuePair("negoCode",""),
                new NameValuePair("negoCabin",""),
                new NameValuePair("negoAvailable","false"),
                new NameValuePair("promotioncode",""),
                new NameValuePair("flightOW",json),
                new NameValuePair("pek_departureDate",pek_departureDate),
                new NameValuePair("pek_arriveDate",pek_arriveDate)
        };
        post.setRequestBody(data);
        cookies=StringUtils.substringBetween(cookies,"GMT","Path");
        post.setRequestHeader("cookie", cookies);

        try {
            httpClient.executeMethod(post);
            String result=post.getResponseBodyAsString();
//            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//            System.out.println(result);
//            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

            String taxRaw=StringUtils.substringBetween(result,"<td colspan=\"2\">","</td>").trim();
            if(taxRaw==null||taxRaw.equals(""))
                return  null;
            String moneyUnit=taxRaw.split(" ")[0];
            priceInfo.add(moneyUnit);
            String tax=taxRaw.split(" ")[1];
            priceInfo.add(tax);
            String priceRaw=StringUtils.substringBetween(result,"<span class=\"fc2 fs4\">","</span>").trim();
            String price=priceRaw.split(" ")[1];
            priceInfo.add(price);
            return  priceInfo;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }



    @Override
    public BookingResult getBookingInfo(FlightSearchParam param) {
        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setMethod("post");
        bookingInfo.setAction("http://book.hongkongairlines.com/hxet/reservation/AVQuery.do");
        Map<String, String> input = new HashMap<String, String>();
        input.put("language", "CN");
        input.put("orgcity", param.getDep());
        input.put("dstcity", param.getArr());
        input.put("takeoffDate", param.getDepDate());
        input.put("cabinType", "ECONOMY");
        input.put("adultCount", "1");
        input.put("childCount","0");
        input.put("tripType", "OW");

        bookingResult.setRet(true);
        bookingInfo.setInputs(input);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    public static void main(String[] args)
    {
        FlightSearchParam f = new FlightSearchParam();
        f.setArr("HKG");
        f.setDep("TXL");
        f.setDepDate("2014-07-28");
        f.setRetDate("2014-07-28");
        Wrapper_gjdairhx001 wr = new Wrapper_gjdairhx001();
        String sw = wr.getHtml(f);
        System.out.println(sw);
        wr.process(sw,f);
//        ProcessResultInfo pri = wr.process(sw, f);
    }
}
