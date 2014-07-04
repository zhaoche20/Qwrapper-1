package zhengzhifang;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
public class Wrapper_gjsairek001 implements QunarCrawler {
    private QFHttpClient httpClient = null;
    public static void main(String[] args) {
        Wrapper_gjsairek001 p = new Wrapper_gjsairek001();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("BAH");
        flightSearchParam.setArr("BKK");
        flightSearchParam.setDepDate("2014-07-12");
        flightSearchParam.setRetDate("2014-07-20");
        flightSearchParam.setWrapperid("gjsairek001");
        flightSearchParam.setQueryId("http://???");
        String html = p.getHtml(flightSearchParam);
        System.out.println("开始打印：" + html);
        System.out.println("结束打印");
        System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
        BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(bookingResult));
    }


    private String removeCDATA(String origin) {
        String output = getValue(origin, "CDATA[", "]");
        return output;
    }

    public String getHtml(FlightSearchParam flightSearchParam) {
        httpClient = new QFHttpClient(flightSearchParam, true);
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        String getUrl = new String();
        getUrl = "http://emiratesapi.performancehorizon.com/service/?ticket=18NawMQPNXuOKFgm&affid=19901504C756129050T&minprice=&maxprice=&mindate="
                + flightSearchParam.getDepDate()
                + "&maxdate="
                + flightSearchParam.getRetDate()
                + "&departcode="
                + flightSearchParam.getDep()
                + "&arrivecode="
                + flightSearchParam.getArr()
                + "&sort=price&onewayreturn=1&cabinclass=0&adults=1&children=0&infants=0&pub=/cn/chinese";
        System.out.println(getUrl);
        QFGetMethod get = new QFGetMethod(getUrl);
        try {
            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }

    public ProcessResultInfo process(String html,
                                     FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        ArrayList<RoundTripFlightInfo> roundTripFlightInfos = new ArrayList<RoundTripFlightInfo>();
        if ("Exception".equals(html)) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            processResultInfo.setData(roundTripFlightInfos);
            return processResultInfo;
        }
        if (html.indexOf("<noResults>1</noResults>") > 0) {
            processResultInfo.setStatus(Constants.NO_RESULT);
            processResultInfo.setData(roundTripFlightInfos);
            return processResultInfo;
        }
        try {
            String[] Flightlist = getValues(html, "<Flight no=", "</Flight>");
            for (String aa : Flightlist) {
                RoundTripFlightInfo roundTripFlightInfo = new RoundTripFlightInfo();
                List<FlightSegement> segements = new ArrayList<FlightSegement>();
                FlightDetail detail = new FlightDetail();
                List<String> flightNoList = Lists.newArrayList();
                List<FlightSegement> reSegements = Lists.newArrayList();
                List<String> reFlightNoList = Lists.newArrayList();

                String currenycode = getValue(aa, "<Currency>", "</Currency>");
                currenycode = removeCDATA(currenycode);

                String retailprice = getValue(aa, "<Price>", "</Price>");
                retailprice = removeCDATA(retailprice);

                String flightdetail_outbound = getValue(aa,
                        "<Flightdetails_Outbound>", "</Flightdetails_Outbound>");
                String flightdetail_inbound = getValue(aa,
                        "<Flightdetails_Inbound>", "</Flightdetails_Inbound>");

                String[] out_depTimes = getValues(flightdetail_outbound,
                        "<DepartTime>", "</DepartTime>");
                String[] out_arrTimes = getValues(flightdetail_outbound,
                        "<ArriveTime>", "</ArriveTime>");
                String[] in_depTimes = getValues(flightdetail_inbound,
                        "<DepartTime>", "</DepartTime>");
                String[] in_arrTimes = getValues(flightdetail_inbound,
                        "<ArriveTime>", "</ArriveTime>");

                String outdepartureTime = removeCDATA(out_depTimes[0])
                        .substring(0, 5);
                String outarrtime = removeCDATA(
                        out_arrTimes[out_arrTimes.length - 1]).substring(0, 5);

                String returndepartureTime = removeCDATA(in_depTimes[0])
                        .substring(0, 5);
                String returnarrtime = removeCDATA(
                        in_arrTimes[in_arrTimes.length - 1]).substring(0, 5);

                String outstr = getValue(aa, "<Flightdetails_Outbound>",
                        "</Flightdetails_Outbound>");
                String returnstr = getValue(aa, "<Flightdetails_Inbound>",
                        "</Flightdetails_Inbound>");
                String[] outflightdetails = getValues(outstr, "<Detail no",
                        "</Detail>");
                String[] returnflightdetails = getValues(returnstr,
                        "<Detail no", "</Detail>");
                String outcode = "";
                String incode = "";
                String outairport = "";
                String inairport = "";
                for (int i = 0; i < outflightdetails.length; i++) // 去程多个航段信息
                {
                    if (i > 0) {
                        outcode += "/";
                    }
                    outcode += getValue(outflightdetails[i],
                            "<FlightCode><![CDATA[", "]]></FlightCode>");
                    if (2 == outflightdetails.length) {
                        String[] deps = getValues(outstr,
                                "<DepartCode><![CDATA[", "]]></DepartCode>");
                        String[] arrs = getValues(outstr,
                                "<ArriveCode><![CDATA[", "]]></ArriveCode>");
                        outairport = arrs[0] + "," + deps[1];
                    }

                    // 解析去程
                    String segementInfo = outflightdetails[i];
                    String dep = removeCDATA(getValue(segementInfo,
                            "<DepartCode>", "</DepartCode>"));
                    String arr = removeCDATA(getValue(segementInfo,
                            "<ArriveCode>", "</ArriveCode>"));
                    String flightCode = removeCDATA(getValue(segementInfo,
                            "<FlightCode>", "</FlightCode>"));
                    String depTime = removeCDATA(
                            getValue(segementInfo, "<DepartTime>",
                                    "</DepartTime>")).substring(0, 5);
                    String arrTime = removeCDATA(
                            getValue(segementInfo, "<ArriveTime>",
                                    "</ArriveTime>")).substring(0, 5);
                    String dDate = removeCDATA(getValue(segementInfo,
                            "<DepartDate>", "</DepartDate>"));
                    String aDate = removeCDATA(getValue(segementInfo,
                            "<ArriveDate>", "</ArriveDate>"));
                    String avcanbin = removeCDATA(getValue(segementInfo,
                            "<Cabinclass>", "</Cabinclass>"));

                    flightNoList.add(flightCode);
                    FlightSegement flightSegement = new FlightSegement();
                    flightSegement.setDepairport(dep);
                    flightSegement.setArrairport(arr);
                    flightSegement.setFlightno(flightCode);
                    flightSegement.setDeptime(depTime);
                    flightSegement.setArrtime(arrTime);
                    flightSegement.setDepDate(dDate);
                    flightSegement.setArrDate(aDate);
                    flightSegement.setAvcanbin(avcanbin);
                    flightSegement.setCompany("EK");
                    segements.add(flightSegement);
                }
                for (int i = 0; i < returnflightdetails.length; i++) // 返程多个航段信息
                {
                    if (i > 0) {
                        incode += "/";
                    }
                    incode += getValue(returnflightdetails[i],
                            "<FlightCode><![CDATA[", "]]></FlightCode>");
                    if (2 == returnflightdetails.length) {
                        String[] deps = getValues(returnstr,
                                "<DepartCode><![CDATA[", "]]></DepartCode>");
                        String[] arrs = getValues(returnstr,
                                "<ArriveCode><![CDATA[", "]]></ArriveCode>");
                        inairport = arrs[0] + "," + deps[1];
                    }

                    // 解析返程
                    String segementInfo = returnflightdetails[i];
                    String dep = removeCDATA(getValue(segementInfo,
                            "<DepartCode>", "</DepartCode>"));
                    String arr = removeCDATA(getValue(segementInfo,
                            "<ArriveCode>", "</ArriveCode>"));
                    String flightCode = removeCDATA(getValue(segementInfo,
                            "<FlightCode>", "</FlightCode>"));
                    String depTime = removeCDATA(
                            getValue(segementInfo, "<DepartTime>",
                                    "</DepartTime>")).substring(0, 5);
                    String arrTime = removeCDATA(
                            getValue(segementInfo, "<ArriveTime>",
                                    "</ArriveTime>")).substring(0, 5);
                    String dDate = removeCDATA(getValue(segementInfo,
                            "<DepartDate>", "</DepartDate>"));
                    String aDate = removeCDATA(getValue(segementInfo,
                            "<ArriveDate>", "</ArriveDate>"));
                    String avcanbin = removeCDATA(getValue(segementInfo,
                            "<Cabinclass>", "</Cabinclass>"));

                    reFlightNoList.add(flightCode);
                    FlightSegement flightSegement = new FlightSegement();
                    flightSegement.setDepairport(dep);
                    flightSegement.setArrairport(arr);
                    flightSegement.setFlightno(flightCode);
                    flightSegement.setDeptime(depTime);
                    flightSegement.setArrtime(arrTime);
                    flightSegement.setDepDate(dDate);
                    flightSegement.setArrDate(aDate);
                    flightSegement.setAvcanbin(avcanbin);
                    flightSegement.setCompany("EK");
                    reSegements.add(flightSegement);
                }
                detail.setDepcity(flightSearchParam.getDep());
                detail.setArrcity(flightSearchParam.getArr());
                detail.setFlightno(flightNoList);
                detail.setMonetaryunit(currenycode);
                detail.setTax(0);
                detail.setPrice(Double.parseDouble(retailprice));
                detail.setWrapperid("gjsairek001");
                detail.setCreatetime(new Timestamp(System.currentTimeMillis()));
                detail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
                detail.setDepdate(new SimpleDateFormat("yyyy-MM-dd")
                        .parse(flightSearchParam.getDepDate()));
                roundTripFlightInfo.setDetail(detail);
                roundTripFlightInfo.setInfo(segements);
                roundTripFlightInfo.setRetdepdate(new SimpleDateFormat(
                        "yyyy-MM-dd").parse(flightSearchParam.getRetDate()));
                roundTripFlightInfo.setRetflightno(reFlightNoList);
                roundTripFlightInfo.setRetinfo(reSegements);
                roundTripFlightInfos.add(roundTripFlightInfo);
                // 去程价格和回程价格抓不到，没有赋值
            }
            processResultInfo.setData(roundTripFlightInfos);
            processResultInfo.setStatus(Constants.SUCCESS);
            processResultInfo.setRet(true);
            processResultInfo.setKey(flightSearchParam.getQueryId());
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(roundTripFlightInfos);
            return processResultInfo;
        }
    }

    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult = new BookingResult();
        bookingResult.setRet(true);
        BookingInfo bookingInfo = new BookingInfo();
        String depDate = flightSearchParam.getDepDate();
        depDate = dateFormat(depDate);
        String RetDate = dateFormat(flightSearchParam.getRetDate());
        String url = "http://www.emirates.com/SessionHandler.aspx";
        Map<String, String> inputs = Maps.newLinkedHashMap();
        inputs.put("Tab", "1");
        inputs.put("publisher", "19901504C756129050T");
        inputs.put("seldcity1", flightSearchParam.getDep());
        inputs.put("selacity1", flightSearchParam.getArr());
        inputs.put("selddate1", depDate);
        inputs.put("seladate1", RetDate);
        inputs.put("seladults", "1");
        inputs.put("selchildren", "0");
        inputs.put("selinfants", "0");
        inputs.put("resultby", "0");
        inputs.put("selcabinclass", "0");
        inputs.put("pageurl", "/IBE.aspx");
        inputs.put("section", "IBE");
        inputs.put("Bsp", "Home");
        inputs.put("showpage", "1");
        inputs.put("J", "t");
        inputs.put("pub", "/cn/chinese");
        inputs.put("s", "");
        inputs.put("h", "");
        inputs.put("flyOption", "0");
        inputs.put("TID", "SB");
        inputs.put("cid", "PHG-zanox_publisher");

        bookingInfo.setInputs(inputs);
        System.out.println(url);
        bookingInfo.setMethod("get");
        bookingInfo.setAction(url);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    public static String getValue(String source, String st, String end) {
        int a = source.indexOf(st);
        if (a == -1)
            return "";
        int b = source.indexOf(end, a + st.length());
        if (b == -1)
            return "";
        return source.substring(a + st.length(), b);
    }

    public static String getValue(String source, String regEx) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
    }

    public static String[] getValues(String source, String st, String end) {
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

    public static String[] getValues(String source, String regEx) {
        Vector<String> vec = new Vector<String>(5);
        Matcher mm = Pattern.compile(regEx).matcher(source);
        while (mm.find()) {
            vec.add(mm.group(mm.groupCount() > 0 ? 1 : 0));
        }
        return vec.toArray(new String[0]);
    }

    String dateFormat(String depDate){
        String dates[] = depDate.split("-");
        String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
                "AUG", "SEPT", "OCT", "NOV", "DEC" };
        String month = months[Integer.parseInt(dates[1]) - 1];
        depDate = dates[2] +"-"+ month +"-"+ dates[0];
        return depDate;
    }
}
