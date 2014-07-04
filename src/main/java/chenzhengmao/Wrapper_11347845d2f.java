package chenzhengmao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.BaseFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * Created by zhengmao.chen on 2014/7/4.
 * 中国国际航空
 * 单程
 */
public class Wrapper_11347845d2f implements QunarCrawler {
    private static final String CODEBASE = "11347845d2f";
    private QFHttpClient httpClient = null;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {
        PostMethod post = new QFPostMethod("http://iec.airchina.com.cn/LowFareSearch/services/lowFareSearch");
        post.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        httpClient = new QFHttpClient(flightSearchParam, false);
        try {
            post.setRequestEntity(new ByteArrayRequestEntity(
                    ("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.lowfaresearch.ibm.com/\">    <soapenv:Header/>    <soapenv:Body>       <ws:search>          <departureAirport>"
                            + flightSearchParam.getDep()
                            + "</departureAirport>          <arrivalAirport>"
                            + flightSearchParam.getArr()
                            + "</arrivalAirport>          <departureDate>"
                            + flightSearchParam.getDepDate() + "</departureDate>          <flexible>false</flexible>       </ws:search>    </soapenv:Body> </soapenv:Envelope>")
                            .getBytes()));
            httpClient.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<BaseFlightInfo> data = new ArrayList<BaseFlightInfo>();
        processResultInfo.setData(data);
        try {
            if (StringUtils.equals(html, "Exception")) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                return processResultInfo;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("<table>");
            String[] directFlightList = StringUtils.substringsBetween(html, "<directFlightList>", "</directFlightList>");
            String[] mutiTripList = StringUtils.substringsBetween(html, "<mutiTripList>", "</mutiTripList>");
            List<BaseFlightInfo> goList = new ArrayList<BaseFlightInfo>();
            for (String flightlist : directFlightList) {
                if (StringUtils.isBlank(flightlist)) {
                    continue;
                }
                if (flightlist.indexOf("<departureDateTime>" + flightSearchParam.getDepDate()) < 0) {
                    continue;
                }
                OneWayFlightInfo owFlight = new OneWayFlightInfo();
                FlightDetail detail = new FlightDetail();
                List<String> flightNoList = new ArrayList<String>();
                detail.setFlightno(flightNoList);
                String flightno = StringUtils.substringBetween(flightlist, "<platingCarrier>", "</platingCarrier>") + StringUtils.substringBetween(flightlist, "<flightNumber>", "</flightNumber>");
                flightNoList.add(flightno);
                String dep = StringUtils.substringBetween(flightlist, "<departureAirport>", "</departureAirport>");
                detail.setDepcity(dep);
                String arr = StringUtils.substringBetween(flightlist, "<arrivalAirport>", "</arrivalAirport>");
                detail.setArrcity(arr);
                detail.setWrapperid(CODEBASE);
                detail.setMonetaryunit("CNY");
                detail.setDepdate(dateFormat.parse(flightSearchParam.getDepDate()));
                String price = StringUtils.substringBetween(flightlist, "<economy>", "</economy>");
                detail.setPrice(Double.parseDouble(price));

                owFlight.setDetail(detail);

                List<FlightSegement> segments = new ArrayList<FlightSegement>();
                owFlight.setInfo(segments);
                FlightSegement segment = new FlightSegement();
                segments.add(segment);

                segment.setFlightno(flightno);
                segment.setDepairport(dep);
                segment.setArrairport(arr);
                String depTime = StringUtils.substringBetween(flightlist, "<departureDateTime>", "</departureDateTime>");
                segment.setDepDate(depTime.substring(0, 10));
                segment.setDeptime(depTime.substring(11, 16));
                String arrTime = StringUtils.substringBetween(flightlist, "<arrivalDateTime>", "</arrivalDateTime>");
                segment.setArrDate(arrTime.substring(0, 10));
                segment.setArrtime(arrTime.substring(11, 16));
                String aircraft = StringUtils.substringBetween(flightlist, "<airEquipType>", "</airEquipType>");
                segment.setAircraft(aircraft);
                segment.setCompany(StringUtils.substringBetween(flightlist, "<platingCarrier>", "</platingCarrier>"));
//			String avcanbin = StringUtils.substringBetween(flightlist, "<resBookDesigCode>", "</resBookDesigCode>");
//			segment.setAvcanbin(avcanbin);
                segment.setDepDate(flightSearchParam.getDepDate());
                goList.add(owFlight);
            }
            for (String muti : mutiTripList) {
                if (StringUtils.isBlank(muti)) {
                    continue;
                }
                String[] flightlist = StringUtils.substringsBetween(muti, "<flightList>", "</flightList>");
                //中转超过一次则不处理
                if (flightlist.length > 2) {
                    continue;
                }
                if (flightlist[0].indexOf("<departureDateTime>" + flightSearchParam.getDepDate()) < 0) {
                    continue;
                }
                OneWayFlightInfo owFlight = new OneWayFlightInfo();
                FlightDetail detail = new FlightDetail();
                owFlight.setDetail(detail);
                detail.setDepcity(StringUtils.substringBetween(flightlist[0], "<departureAirport>", "</departureAirport>"));
                detail.setArrcity(StringUtils.substringBetween(flightlist[1], "<arrivalAirport>", "</arrivalAirport>"));
                detail.setMonetaryunit("CNY");
                String price = StringUtils.substringBetween(muti, "<economy>", "</economy>");
                detail.setPrice(Double.parseDouble(price));
                detail.setWrapperid(CODEBASE);
                detail.setDepdate(dateFormat.parse(flightSearchParam.getDepDate()));
                List<String> flightNoList = new ArrayList<String>();
                detail.setFlightno(flightNoList);
                List<FlightSegement> segmentList = new ArrayList<FlightSegement>();
                owFlight.setInfo(segmentList);
                for (String flight : flightlist) {
                    FlightSegement segment = new FlightSegement();
                    segment.setDepairport(StringUtils.substringBetween(flight, "<departureAirport>", "</departureAirport>"));
                    String deptime = StringUtils.substringBetween(flightlist[0], "<departureDateTime>", "</departureDateTime>");
                    segment.setDepDate(deptime.substring(0, 10));
                    segment.setDeptime(deptime.substring(11, 16));
                    segment.setArrairport(StringUtils.substringBetween(flight, "<arrivalAirport>", "</arrivalAirport>"));
                    String arrtime = StringUtils.substringBetween(flightlist[0], "<arrivalDateTime>", "</arrivalDateTime>");
                    segment.setArrDate(arrtime.substring(0, 10));
                    segment.setArrtime(arrtime.substring(11, 16));
                    segment.setAircraft(StringUtils.substringBetween(flight, "<airEquipType>", "</airEquipType>"));
                    String carrier = StringUtils.substringBetween(flight, "<platingCarrier>", "</platingCarrier>");
                    segment.setCompany(carrier);
                    String flightNumber = StringUtils.substringBetween(flightlist[0], "<flightNumber>", "</flightNumber>");
                    String flightNo = carrier + flightNumber;
                    segment.setFlightno(flightNo);
                    flightNoList.add(flightNo);
                    segmentList.add(segment);
                }
                goList.add(owFlight);
            }

            if (null != goList && goList.size() > 0) {
                data.addAll(goList);
            } else { //无去程
                processResultInfo.setStatus(Constants.NO_RESULT);
                return processResultInfo;
            }
            processResultInfo.setStatus(Constants.SUCCESS);
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult = new BookingResult();
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();

        BookingInfo bookingInfo = new BookingInfo();

        String url = "http://et.airchina.com.cn/InternetBooking/AirLowFareSearchExternal.do?"
                + "tripType=OW&searchType=FARE&flexibleSearch=false&directFlightsOnly=false"
                + "&fareOptions=1.FAR.X&outboundOption.originLocationCode=" + dep
                + "&outboundOption.destinationLocationCode=" + arr
                + "&outboundOption.departureDay=" + depDate.substring(8, 10)
                + "&outboundOption.departureMonth=" + depDate.substring(5, 7)
                + "&outboundOption.departureYear=" + depDate.substring(0, 4)
                + "&outboundOption.departureTime=NA"
                + "&guestTypes[0].type=ADT&guestTypes[0].amount=1&pos=AIRCHINA_CN&lang=zh_CN&cid=qunarint";

        bookingInfo.setAction(url);
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("post");
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    public static void main(String[] args) {
        FlightSearchParam param = new FlightSearchParam();
        param.setDep("PEK");
        param.setArr("BKK");
        param.setDepDate("2014-07-24");
        param.setWrapperid("11347845d2f");

        Wrapper_11347845d2f wrapper = new Wrapper_11347845d2f();
        String html = wrapper.getHtml(param);
        System.out.println(html);
        ProcessResultInfo info = wrapper.process(html, param);
        System.out.println(info);

//		Gson gson = new GsonBuilder().create();
//		System.out.println(gson.toJson(wrapper.getBookingInfo(param)));
    }

    public static String getValue(String source, String regEx) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
    }

    public static String[] getValues(String source, String regEx) {
        Vector<String> vec = new Vector<String>(5);
        Matcher mm = Pattern.compile(regEx).matcher(source);
        while (mm.find()) {
            vec.add(mm.group(mm.groupCount() > 0 ? 1 : 0));
        }
        return vec.toArray(new String[0]);
    }
}
