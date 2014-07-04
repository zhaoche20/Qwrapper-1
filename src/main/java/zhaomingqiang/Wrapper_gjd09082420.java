package zhaomingqiang;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-4
 * Time: 下午1:46
 * 国泰航空
 */
public class Wrapper_gjd09082420 implements QunarCrawler {
    private static final String CODEBASE = "gjd09082420";
    private QFHttpClient httpClient = null;

    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        flightSearchParam.setToken("");
        QFGetMethod get = null;
        String url = "http://amadeus.corp.qunar.com/OneWay?dep=" + dep + "&arr=" + arr + "&depDate=" + depDate + "&CarrierId=CX";
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
        List<OneWayFlightInfo> data = (List<OneWayFlightInfo>) processResultInfo.getData();
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
                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                data.add(oneWayFlightInfo);
                String[] infoItems = StringUtils.substringsBetween(iter, "<td>", "</td>");

                String monetaryunit = infoItems[0];
                String tax = infoItems[1];
                String price = infoItems[2];
                String flightno = infoItems[3];
                String depairport = infoItems[5];
                String arrairport = infoItems[6];
                String deptime = infoItems[7];
                String arrtime = infoItems[8];

                String[] flightnos = StringUtils.split(flightno, "/");
                int fSegeMents = flightnos.length;
                int i = 0;
                for (String fNo : flightnos) {
                    ++i;
                    FlightSegement segement = new FlightSegement();
                    oneWayFlightInfo.getInfo().add(segement);
                    oneWayFlightInfo.getDetail().getFlightno().add(fNo);
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
                processResultInfo.setStatus(Constants.SUCCESS);
                oneWayFlightInfo.getDetail().setMonetaryunit(monetaryunit);
                oneWayFlightInfo.getDetail().setDepcity(searchParam.getDep());
                oneWayFlightInfo.getDetail().setArrcity(searchParam.getArr());
                oneWayFlightInfo.getDetail().setDepdate(Date.valueOf(searchParam.getDepDate()));
                oneWayFlightInfo.getDetail().setPrice(Double.valueOf(price));
                oneWayFlightInfo.getDetail().setTax(Double.valueOf(tax));
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
        String info = "MS-Qunar-_-Qunar-CN-_-"+dep+"-_-"+dep+"-"+arr;
        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        java.util.Map<String, String> inputs = new HashMap<String, String>();
        bookingInfo.setAction("http://www.cathaypacific.com/wdsibe/IBEFacade");
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("get");
        inputs.put("ACTION","SINGLECITY_SEARCH");
        inputs.put("FLEXIBLEDATE","true");
        inputs.put("BOOKING_FLOW","REVENUE");
        inputs.put("ENTRYLANGUAGE","sc");
        inputs.put("ENTRYPOINT","http%3A%2F%2Fwww.qunar.com");
        inputs.put("ENTRYCOUNTRY","CN");
        inputs.put("RETURNURL","http://www.cathaypacific.com:80/cx/sc_CN/_jcr_content.handler.html");
        inputs.put("ERRORURL","http://www.cathaypacific.com:80/cx/sc_CN/_jcr_content.handler.html");
        inputs.put("ORIGIN",dep);
        inputs.put("DESTINATION",arr);
        inputs.put("DEPARTUREDATE",depDate);
        inputs.put("ARRIVALDATE",depDate);
        inputs.put("TRIPTYPE","O");
        inputs.put("CABINCLASS","Y");
        inputs.put("ADULT","1");
        inputs.put("CHILD","0");
        inputs.put("INFANT","0");
        inputs.put("cm_mmc",info);
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }
}
