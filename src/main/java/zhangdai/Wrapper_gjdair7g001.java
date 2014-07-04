package zhangdai;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.developer.QFPostMethod;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.params.CookiePolicy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dai.zhang
 * Date: 14-7-1
 * Time: 下午12:02
 * 日本星锐航空
 * To change this template use File | Settings | File Templates.
 */
public class Wrapper_gjdair7g001 implements QunarCrawler {

    QFHttpClient httpClient = null;
    private static final String SEARCHURLBYBOOKING = "http://www.starflyer.jp/ap/SeatInquiry.aspx";


    @Override
    public String getHtml(FlightSearchParam flightSearchParam) {

        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String[] times = depDate.split("-");
        httpClient = new QFHttpClient(flightSearchParam, true);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        PostMethod post = null;
        try {

            post = new QFPostMethod("https://rsv.starflyer.jp/rsv_p/aln_web/BEVacant.do");
            NameValuePair[] names = {
                    new NameValuePair("CONNECTION_KIND", "TOP"),
                    new NameValuePair("btnSubmit:mapping=success", ""),
                    new NameValuePair("channel", "WEB"),
                    new NameValuePair("functionKind", "reserve"),
                    new NameValuePair("segConditionForm.seatKind", "Y"),
                    new NameValuePair("segConditionForm.nowSegIndex", "1"),
                    new NameValuePair("segConditionForm.selectedEmbMonth", times[1]),
                    new NameValuePair("segConditionForm.selectedEmbDay", times[2]),
                    new NameValuePair("segConditionForm.selectedDepApo", dep),
                    new NameValuePair("segConditionForm.selectedArrApo", arr),
                    new NameValuePair("paxCountConditionForm.selectedAdultCount", "1"),
                    new NameValuePair("paxCountConditionForm.selectedChildCount", "0"),
                    new NameValuePair("paxCountConditionForm.selectedInfantCount", "0")
            };
            post.setRequestBody(names);
            httpClient.executeMethod(post);
            if (post.getStatusCode() != 200) {
                return "StatusError" + post.getStatusCode();
            }
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
        List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
        try {

            if (html.equals("Exception")) {
                processResultInfo.setStatus(Constants.CONNECTION_FAIL);
                processResultInfo.setData(data);
                return processResultInfo;
            } else if (html.contains("空席照会条件入力")) {
                processResultInfo.setStatus(Constants.INVALID_AIRLINE);
                processResultInfo.setData(data);
                return processResultInfo;
            } else {
                String resultTable = StringUtils.substringBetween(html, "alt=\"株主優待割引運賃", "</table>");
                String lines[] = StringUtils.substringsBetween(resultTable, "<tr", "</tr>");

                for (int j = 0; j < lines.length; j++) {
                    if (!lines[j].contains("</a>"))
                        continue;
                    FlightDetail flightDetail = getFlightDetail(lines[j], flightSearchParam);
                    FlightSegement flightSegement = getFlightSegement(lines[j], flightSearchParam);
                    List<FlightSegement> flightSegementList = new ArrayList<FlightSegement>();
                    flightSegementList.add(flightSegement);
                    OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                    oneWayFlightInfo.setDetail(flightDetail);
                    oneWayFlightInfo.setInfo(flightSegementList);
                    data.add(oneWayFlightInfo);
                }
            }

            if (data.isEmpty()) {
                processResultInfo.setStatus(Constants.NO_RESULT);
                processResultInfo.setData(data);
            } else {
                processResultInfo.setStatus(Constants.SUCCESS);
                processResultInfo.setData(data);
            }
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(data);
            e.printStackTrace();
            return processResultInfo;
        }
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult = new BookingResult();
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String[] tempDate = depDate.split("-");
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction(SEARCHURLBYBOOKING);
        bookingInfo.setContentType("UTF-8");
        bookingInfo.setMethod("post");
        java.util.Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("depApo", dep);
        inputs.put("arrApo", arr);
        inputs.put("embMonth", tempDate[1]);
        inputs.put("embDay", tempDate[2]);
        inputs.put("adultPaxCount", "1");
        inputs.put("childPaxCount", "0");
        inputs.put("infantCount", "0");
        bookingInfo.setInputs(inputs);
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }


    /**
     * ******************getFlightDetail**************************
     */

    public FlightDetail getFlightDetail(String line, FlightSearchParam flightSearchParam) {

        //FlightDetail添加dep、arr、depdate属性
        FlightDetail flightDetail = new FlightDetail();
        flightDetail.setArrcity(flightSearchParam.getArr());
        flightDetail.setDepcity(flightSearchParam.getDep());
        Date Depdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Depdate = sdf.parse(flightSearchParam.getDepDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        flightDetail.setDepdate(Depdate);

        //设置flightno、monetaryunit、tax、 price属性
        Pattern flightnoPattern = Pattern.compile("[A-Z]{3} \\d+");
        Matcher matcher = flightnoPattern.matcher(line);
        List<String> lightno = new ArrayList<String>();
        if (matcher.find()) {
            lightno.add(matcher.group().replaceAll("\\s", "").replace("SFJ", "7G"));
        }
        flightDetail.setFlightno(lightno);
        flightDetail.setMonetaryunit("JPY");

        String prices[] = StringUtils.substringsBetween(line, "<br>\\", "</td>");
        String price;
        for (int i = prices.length - 1; i >= 0; i--) {
            String temp = StringUtils.substringBetween(line, ">", "</a><br>\\" + prices[i] + "</td>");
            if (!temp.isEmpty()) {
                price = prices[i];
                if (price.contains("black"))
                    price = StringUtils.substringBetween(price, "black>", "</font>");
                flightDetail.setPrice(Double.valueOf(price.replace(",", "")));
                break;
            }
        }
        return flightDetail;
    }

    /**
     * ********************getFlightSegement***************************
     */

    public FlightSegement getFlightSegement(String line, FlightSearchParam flightSearchParam) {

        //FlightSearchParam设置dep、arr、depdate属性
        FlightSegement flightSegement = new FlightSegement();
        flightSegement.setArrairport(flightSearchParam.getArr());
        flightSegement.setDepairport(flightSearchParam.getDep());
        flightSegement.setDepDate(flightSearchParam.getDepDate());
        flightSegement.setArrDate(flightSearchParam.getDepDate());

        //设置flightno、deptime、arrtime、arrDate;
        Pattern flightnoPattern = Pattern.compile("[A-Z]{3} \\d+");
        Matcher flightnomatcher = flightnoPattern.matcher(line);
        String lightno = "";
        if (flightnomatcher.find()) {
            lightno = flightnomatcher.group().replaceAll("\\s", "").replace("SFJ", "7G");
        }
        flightSegement.setFlightno(lightno);


        Pattern timePattern = Pattern.compile(">\\d+:\\d+");
        Matcher timematcher = timePattern.matcher(line);
        timematcher.matches();
        String deptime = "";
        String arrtime = "";
        if (timematcher.find()) {
            deptime = timematcher.group(0).replace(">", "");
        }
        if (timematcher.find()) {
            arrtime = timematcher.group(0).replace(">", "");
        }
        flightSegement.setFlightno(lightno);
        flightSegement.setDeptime(deptime);
        flightSegement.setArrtime(arrtime);

        return flightSegement;

    }


}

