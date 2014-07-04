package zhaomingqiang;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * 港龙航空
 * Date: 14-7-4
 * Time: 上午10:25
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_11eab9de15d implements QunarCrawler {
    private static final String CODEBASE = "11eab9de15d";
    private QFHttpClient httpClient = null;

    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String retDate = flightSearchParam.getRetDate();
        flightSearchParam.setToken("");
        QFGetMethod get = null;
        String url = "http://amadeus.corp.qunar.com/RoundTrip?dep=" + dep + "&arr=" + arr + "&depDate=" + depDate + "&arrDate=" + retDate + "&CarrierId=KA";
        System.out.println(url);
        try {
            httpClient = new QFHttpClient(flightSearchParam, true);
            get = new QFGetMethod(url);
            throwExceptionByResponseCode(httpClient.executeMethod(get), 200);
            String result = get.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }

    /**
     * @param responseCode 请求反馈的状态吗
     * @param compareCode  目标状态码
     * @throws java.io.IOException
     */
    private void throwExceptionByResponseCode(int responseCode, int compareCode) throws IOException {
        if (responseCode != compareCode)
            throw new IOException("CONNECTION_FAIL");
    }

    public ProcessResultInfo process(String html, FlightSearchParam searchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        List<RoundTripFlightInfo> data = (List<RoundTripFlightInfo>) processResultInfo.getData();
        String[] res = StringUtils.substringsBetween(html, "<tr>", "</tr>");
        // CNY	29220	1823	AY56/AY2645/AY5931	333	CKG	LHR	09:50	19:50
        if (StringUtils.equals(html, "Exception")) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            return processResultInfo;
        }
        if (res == null || res.length == 0) {
            processResultInfo.setStatus(Constants.NO_RESULT);
            return processResultInfo;
        }
        try {
            for (String iter : res) { // 每个是一个航班
                RoundTripFlightInfo roundTripFlightInfo = new RoundTripFlightInfo();
                data.add(roundTripFlightInfo);
                String[] infoItems = StringUtils.substringsBetween(iter, "<td>", "</td>");
                String monetaryunit = infoItems[0];
                String price= infoItems[1];
                String tax= infoItems[2];
                String flightno = infoItems[3];
                String depairport = infoItems[5];
                String arrairport = infoItems[6];
                String deptime = infoItems[7];
                String arrtime = infoItems[8];
                String retflightno = infoItems[9];
                String retdepairport = infoItems[11];
                String retarrairport = infoItems[12];
                String retdeptime = infoItems[13];
                String retarrtime = infoItems[14];
                String[] flightnos = StringUtils.split(flightno, "/");
                int fSegeMents = flightnos.length;
                int i = 0;
                for (String fNo : flightnos) {
                    ++i;
                    FlightSegement segement = new FlightSegement();
                    roundTripFlightInfo.getInfo().add(segement);
                    roundTripFlightInfo.getDetail().getFlightno().add(fNo);
                    segement.setFlightno(fNo);
                    segement.setDepDate(searchParam.getDepDate());
                    segement.setArrDate(searchParam.getDepDate());
                    if (1 == i) { // 如果是第一段
                        segement.setDepairport(depairport);
                        segement.setDeptime(deptime);
                    }
                    if (fSegeMents == i) { //如果是最后一段
                        segement.setArrairport(arrairport);
                        segement.setArrtime(arrtime);
                    }
                    if (StringUtils.isEmpty(segement.getDepairport())) {
                        segement.setDepairport("XXX");
                    }
                    if (StringUtils.isEmpty(segement.getArrairport())) {
                        segement.setArrairport("XXX");
                    }
                    if (StringUtils.isEmpty(segement.getDeptime())) {
                        segement.setDeptime("00:00");
                    }
                    if (StringUtils.isEmpty(segement.getArrtime())) {
                        segement.setArrtime("00:00");
                    }
                }
                String[] retflightnos = StringUtils.split(retflightno, "/");
                int retfSegeMents = retflightnos.length;
                int j = 0;
                for (String retfNo : retflightnos) {
                    ++j;
                    FlightSegement segement = new FlightSegement();
                    roundTripFlightInfo.getRetinfo().add(segement);
                    roundTripFlightInfo.getRetflightno().add(retfNo);
                    segement.setFlightno(retfNo);
                    segement.setDepDate(searchParam.getRetDate());
                    segement.setArrDate(searchParam.getRetDate());
                    if (1 == j) { // 如果是第一段
                        segement.setDepairport(retdepairport);
                        segement.setDeptime(retdeptime);
                    }
                    if (retfSegeMents == j) { //如果是最后一段
                        segement.setArrairport(retarrairport);
                        segement.setArrtime(retarrtime);
                    }
                    if (StringUtils.isEmpty(segement.getDepairport())) {
                        segement.setDepairport("XXX");
                    }
                    if (StringUtils.isEmpty(segement.getArrairport())) {
                        segement.setArrairport("XXX");
                    }
                    if (StringUtils.isEmpty(segement.getDeptime())) {
                        segement.setDeptime("00:00");
                    }
                    if (StringUtils.isEmpty(segement.getArrtime())) {
                        segement.setArrtime("00:00");
                    }
                }
                processResultInfo.setStatus(Constants.SUCCESS);
                roundTripFlightInfo.getDetail().setMonetaryunit(monetaryunit);
                roundTripFlightInfo.getDetail().setDepcity(searchParam.getDep());
                roundTripFlightInfo.getDetail().setArrcity(searchParam.getArr());
                roundTripFlightInfo.getDetail().setDepdate(Date.valueOf(searchParam.getDepDate()));
                roundTripFlightInfo.getDetail().setPrice(Double.valueOf(price));
                roundTripFlightInfo.getDetail().setTax(Double.valueOf(tax));
                roundTripFlightInfo.setRetdepdate(Date.valueOf(searchParam.getRetDate()));
            }
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            return processResultInfo;
        }
    }

    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate().replaceAll("-", "");
        String retDate = flightSearchParam.getRetDate().replaceAll("-", "");
        String info = "MS-Qunar-_-Qunar-CN-_-" + dep + "-_-" + dep + "-" + arr + "-" + dep;
        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        java.util.Map<String, String> inputs = new HashMap<String, String>();
        bookingInfo.setAction("http://www.dragonair.com/kawdsibe/IBEFacade");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("get");
        inputs.put("ACTION", "SINGLECITY_SEARCH");
        inputs.put("FLEXIBLEDATE", "true");
        inputs.put("BOOKING_FLOW", "REVENUE");
        inputs.put("ENTRYLANGUAGE", "sc");
        inputs.put("ENTRYPOINT", "http%3A%2F%2Fwww.qunar.com");
        inputs.put("ENTRYCOUNTRY", "CN");
        inputs.put("RETURNURL", "http://www.dragonair.com:80/ka/sc_CN/_jcr_content.handler.html");
        inputs.put("ERRORURL", "http://www.dragonair.com:80/ka/sc_CN/_jcr_content.handler.html");
        inputs.put("ORIGIN", dep);
        inputs.put("DESTINATION", arr);
        inputs.put("DEPARTUREDATE", depDate);
        inputs.put("ARRIVALDATE", retDate);
        inputs.put("TRIPTYPE", "R");
        inputs.put("CABINCLASS", "Y");
        inputs.put("ADULT", "1");
        inputs.put("CHILD", "0");
        inputs.put("INFANT", "0");
        inputs.put("cm_mmc", info);
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}
