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
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;

/**
 * zhifang.zheng
 */
public class Wrapper_gjdairek001 implements QunarCrawler {

    private QFHttpClient httpClient = null;

    public static void main(String[] args) {
        Wrapper_gjdairek001 p = new Wrapper_gjdairek001();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("BAH");
        flightSearchParam.setArr("BKK");
        flightSearchParam.setDepDate("2014-07-12");
        flightSearchParam.setWrapperid("gjdairek001");
        flightSearchParam.setQueryId("http://???");
        String html = p.getHtml(flightSearchParam);
        System.out.println("开始打印：" + html);
        System.out.println("结束打印");
        System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
        BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println(JSON.toJSONString(bookingResult));

    }


    private String removeCDATA(String origin) {
        String output = getValue(origin, "CDATA[", "]").trim();
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
                + flightSearchParam.getDepDate()
                + "&departcode="
                + flightSearchParam.getDep()
                + "&arrivecode="
                + flightSearchParam.getArr()
                + "&sort=price&onewayreturn=2&cabinclass=0&adults=1&children=0&infants=0&pub=/cn/chinese";
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

    public ProcessResultInfo process(String html,FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo = new ProcessResultInfo();
        ArrayList<OneWayFlightInfo> oneWayFlightInfos = new ArrayList<OneWayFlightInfo>();
        if ("Exception".equals(html)) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        if (html.indexOf("<noResults>1</noResults>") > 0) {
            processResultInfo.setStatus(Constants.NO_RESULT);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        try {
            String[] Flightlist = getValues(html, "<Flight no=", "</Flight>");
            for (String aa : Flightlist) {
                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                List<FlightSegement> segements = new ArrayList<FlightSegement>();
                FlightDetail detail = new FlightDetail();
                List<String> flightNoList = Lists.newArrayList();

                String currenycode = getValue(aa, "<Currency>", "</Currency>");
                currenycode = removeCDATA(currenycode);
                String retailprice = getValue(aa, "<Price>", "</Price>");
                retailprice = removeCDATA(retailprice);

                String flightdetail_outbound = getValue(aa,
                        "<Flightdetails_Outbound>", "</Flightdetails_Outbound>");


                String[] flightdetails = getValues(aa, "<Detail no",
                        "</Detail>");
                String code = "";
                String transferAirport = "";
                String str = getValue(aa, "<Flightdetails_Outbound>",
                        "</Flightdetails_Outbound>");
                String[] deps = getValues(str, "<DepartCode><![CDATA[",
                        "]]></DepartCode>");
                String[] arrs = getValues(str, "<ArriveCode><![CDATA[",
                        "]]></ArriveCode>");
                for (int i = 0; i < flightdetails.length; i++) { // 多个航段信息
                    if (i > 0) {
                        code += "/";
                    }
                    code += getValue(flightdetails[i], "<FlightCode><![CDATA[",
                            "]]></FlightCode>");
                    if (2 == flightdetails.length) {
                        transferAirport = arrs[0] + "," + deps[1];
                    }

                    // 自己解析
                    String segementInfo = flightdetails[i];
                    String dep = removeCDATA(getValue(segementInfo,
                            "<DepartCode>", "</DepartCode>"));
                    String arr = removeCDATA(getValue(segementInfo,
                            "<ArriveCode>", "</ArriveCode>"));
                    String flightCode = removeCDATA(getValue(segementInfo,
                            "<FlightCode>", "</FlightCode>"));
                    String depTime = removeCDATA(getValue(segementInfo,
                            "<DepartTime>", "</DepartTime>")).substring(0, 5);
                    String arrTime = removeCDATA(getValue(segementInfo,
                            "<ArriveTime>", "</ArriveTime>")).substring(0, 5);
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
                detail.setDepcity(flightSearchParam.getDep());
                detail.setArrcity(flightSearchParam.getArr());
                detail.setFlightno(flightNoList);
                detail.setMonetaryunit(currenycode);
                detail.setTax(0);
                detail.setPrice(Double.parseDouble(retailprice));
                detail.setWrapperid("gjdairek001");
                detail.setCreatetime(new Timestamp(System.currentTimeMillis()));
                detail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
                detail.setDepdate(new SimpleDateFormat("yyyy-MM-dd")
                        .parse(flightSearchParam.getDepDate()));
                oneWayFlightInfo.setInfo(segements);
                oneWayFlightInfo.setDetail(detail);
                oneWayFlightInfos.add(oneWayFlightInfo);
            }
            processResultInfo.setData(oneWayFlightInfos);
            processResultInfo.setStatus(Constants.SUCCESS);
            processResultInfo.setRet(true);
            processResultInfo.setKey(flightSearchParam.getQueryId());
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
    }

	/*public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
		BookingResult bookingResult = new BookingResult();
		bookingResult.setRet(true);
		BookingInfo bookingInfo = new BookingInfo();
		String depDate = flightSearchParam.getDepDate();
		String dates[] = depDate.split("-");
		String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
				"AUG", "SEPT", "OCT", "NOV", "DEC" };
		String month = months[Integer.parseInt(dates[1]) - 1];
		depDate = dates[2] + "-"+month+"-" + dates[0];
		String url = "http://ad.zanox.com/ppc/?19901504C756129050T&zpar0=18NawMQPNXuOKFgm&ULP=destination:http%3A%2F%2Fwww.emirates.com%2FSessionHandler.aspx%3FTab%3D2%26publisher%3D19901504C756129050T%26seldcity1%3D"
				+ flightSearchParam.getDep()
				+ "%26selacity1%3D"
				+ flightSearchParam.getArr()
				+ "%26selddate1%3D"
				+ depDate
				+ "%26seladate1%3D"
				+ depDate
				+ "%26seladults%3D1%26selchildren%3D0%26selinfants%3D0%26resultby%3D0%26selcabinclass%3D0%26pageurl%3D%252FIBE.aspx%26section%3DIBE%26Bsp%3DHome%26showpage%3D1%26J%3Dt%26pub%3D%252Fcn%252Fchinese%26s%3D%26h%3D%26flyOption%3D0%26TID%3DOW";

		System.out.println(url);
		bookingInfo.setMethod("get");
		bookingInfo.setAction(url);
		bookingResult.setData(bookingInfo);
		return bookingResult;

	}*/

    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult bookingResult = new BookingResult();
        bookingResult.setRet(true);
        BookingInfo bookingInfo = new BookingInfo();
        String depDate = flightSearchParam.getDepDate();
        String dates[] = depDate.split("-");
        String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
                "AUG", "SEPT", "OCT", "NOV", "DEC" };
        String month = months[Integer.parseInt(dates[1]) - 1];
        depDate = dates[2] + month + dates[0];
        String url = "http://www.emirates.com/SessionHandler.aspx?Tab=2&publisher=19901504C756129050T&seldcity1=BAH&selacity1=BKK&selddate1=12JUL2014&seladate1=12JUL2014&seladults=1&selchildren=0&selinfants=0&resultby=0&selcabinclass=0&pageurl=/IBE.aspx" +
                "&section=IBE&Bsp=Home&showpage=1&J=t&pub=/cn/chinese&s=&h=&flyOption=0&TID=OW&cid=PHG-zanox_publisher";
        Map<String, String> inputs = Maps.newLinkedHashMap();
        inputs.put("Tab", "2");
        inputs.put("publisher", "19901504C756129050T");
        inputs.put("seldcity1", flightSearchParam.getDep());
        inputs.put("selacity1", flightSearchParam.getArr());
        inputs.put("selddate1", depDate);
        inputs.put("seladate1", depDate);
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
        inputs.put("TID", "OW");
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
}
