package zhengzhifang;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.lang.StringUtils;
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
import com.qunar.qfwrapper.util.QFPostMethod;
/**
 * zhifang.zheng
 */
public class Wrapper_gjdairjw001 implements QunarCrawler {
    private static final String CODEBASE = "gjdairjw001";
    private QFHttpClient httpClient = null;
    public static void main(String[] args) {
        Wrapper_gjdairjw001 p = new Wrapper_gjdairjw001();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("NRT");
        flightSearchParam.setArr("OKA");
        flightSearchParam.setDepDate("2014-07-09");
        flightSearchParam.setWrapperid("gjdairjw001");
        flightSearchParam.setQueryId("http://???");
        String html = p.getHtml(flightSearchParam);
        System.out.println("开始打印：" + html);
        System.out.println("结束打印");
		/*System.out.println("结果是"+JSON.toJSONString(p.process(html, flightSearchParam)));
		BookingResult bookingResult = p.getBookingInfo(flightSearchParam);
        System.out.println("booking结果是"+JSON.toJSONString(bookingResult));*/
    }



    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String travelDate;
        try {
            travelDate = dateFormat(flightSearchParam.getDepDate());
        } catch (ParseException e1) {
            return "DateParseException";
        }
        // 利用get取jsessionid
        String jsessionid = "";
        httpClient = new QFHttpClient(flightSearchParam, true);
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        String uri = "http://www.vanilla-air.com/reservation/ibe/ibe/booking?origin="
                + dep
                + "&destination="
                + arr
                + "&travelDate="
                + travelDate
                + "&tripType=OW&adults=1&children=0&infants=0&promoCode=&mode=searchResultInter&locale=tw&wvm=WVMD&channel=PB&cabinClass=ECONOMY&pointOfPurchase=OTHERS";
        QFGetMethod getMethod = new QFGetMethod(uri);
        try {
            httpClient.executeMethod(getMethod);
            String info = getMethod.getResponseBodyAsString();
            jsessionid += StringUtils
                    .substringBetween(
                            info,
                            "@import url(\"/reservation/tw/styles/booking.css;jsessionid=",
                            "\");");
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        // post抓取数据
        QFPostMethod postMethod = null;
        if (jsessionid.equals("")) {
            postMethod = new QFPostMethod(
                    "http://www.vanilla-air.com/reservation/ibe/ibe/booking?execution=e1s1&locale=tw&llt=&agentId=");
        } else {
            postMethod = new QFPostMethod(
                    "http://www.vanilla-air.com/reservation/ibe/ibe/booking;jsessionid="
                            + jsessionid
                            + "?execution=e1s1&locale=tw&llt=&agentId=");
        }
        String body = "_eventId=showWtLblResult&mode=searchResultInter&wvm=WVMD&tripType=OW&origin="
                + dep
                + "&destination="
                + arr
                + "&travelDate="
                + travelDate
                + "&adults=1&children=0&infants=0&cabinClass=ECONOMY&promoCode=&pointOfPurchase=OTHERS&flightNumberOW=&fareOW=&flightNumberRT=&fareRT=&channel=PB&flexTrvlDates=&ccType=&deviceType=";
        String refer = String
                .format("http://www.vanilla-air.com/reservation/ibe/ibe/booking?origin=$$&destination=$$&travelDate=$$&tripType=OW&adults=1&children=0&infants=0&promoCode=&mode=searchResultInter&locale=tw&wvm=WVMD&channel=PB&cabinClass=ECONOMY&pointOfPurchase=OTHERS",
                        dep, arr, travelDate);
        try {
            postMethod.setRequestEntity(new ByteArrayRequestEntity(body
                    .getBytes()));
            postMethod.addRequestHeader("Connection", "keep-alive");
            postMethod.addRequestHeader("Content-Type",
                    "application/x-www-form-urlencoded");
            postMethod.addRequestHeader("Host", "www.vanilla-air.com");
            postMethod.addRequestHeader("Referer", refer);
            postMethod.getParams().setContentCharset("UTF-8");
            httpClient.executeMethod(postMethod);
            String resp = postMethod.getResponseBodyAsString();
            if (resp.contains("No Flights <br> Available")) {
                return "INVALID_DATE";
            }
            return (resp);
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }


    public ProcessResultInfo process(String html, FlightSearchParam flightSearchParam) {
        ProcessResultInfo processResultInfo=new ProcessResultInfo();
        List<OneWayFlightInfo> oneWayFlightInfos = new ArrayList<OneWayFlightInfo>();

        if (StringUtils.equals(html, "Exception")) {
            processResultInfo.setStatus(Constants.CONNECTION_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        if (html.indexOf("对不起！没有找到符合条件的航班信息") > 0
                || html.indexOf("很抱歉，该航班已经售") > 0) {
            processResultInfo.setStatus(Constants.NO_RESULT);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        if(html.equals("INVALID_DATE")){
            processResultInfo.setStatus(Constants.INVALID_DATE);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
        try {
            processResultInfo.setKey(flightSearchParam.getQueryId());
            // 取机场三字码
            String city[] = StringUtils.substringsBetween(html,
                    "<label  class=\"text_bold\" />", "訂位結果");
            String citys[] = getValues(city[0], "[A-Z]{3}");
            // 取航班信息（时间*2，code，price，货币，机型）
            String month = flightSearchParam.getDepDate().substring(5, 7);//2014-03-12
            String day = flightSearchParam.getDepDate().substring(8, 10);
            String check[] = StringUtils.substringsBetween(html,
                    "<!-- >label class=\"lbl_bld\">" + day + "-" + month
                            + "</label-->", "<tr class=\"spacer_row\" >");           //  30-06
            int number = check.length;
            for (int i = 0; i < number; i++) {
                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                List<FlightSegement> segements = new ArrayList<FlightSegement>();
                FlightSegement flightSegement = new FlightSegement();
                FlightDetail detail = new FlightDetail();
                String taxcode = StringUtils.substringBetween(check[i],
                        "<input type=\"hidden\" id=\"selectedTax_",
                        "_0\" value=\"");
                String tax = StringUtils.substringBetween(check[i],
                        "<input type=\"hidden\" id=\"selectedTax_" + taxcode
                                + "_0\" value=\"", "\"/>");
                String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
                String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
                String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
                Pattern p_script = Pattern.compile(regEx_script,
                        Pattern.CASE_INSENSITIVE);
                Matcher m_script = p_script.matcher(check[i]);
                check[i] = m_script.replaceAll(""); // 过滤script标签
                Pattern p_style = Pattern.compile(regEx_style,
                        Pattern.CASE_INSENSITIVE);
                Matcher m_style = p_style.matcher(check[i]);
                check[i] = m_style.replaceAll(""); // 过滤style标签
                Pattern p_html = Pattern.compile(regEx_html,
                        Pattern.CASE_INSENSITIVE);
                Matcher m_html = p_html.matcher(check[i]);
                check[i] = m_html.replaceAll(""); // 过滤html标签
                check[i] = check[i].replaceAll("\\s+", " ");
                // 判断全部售罄则跳过
                String CurrentCode = getValue(check[i], "[A-Z]{3}");
                if (CurrentCode.equals("")) {
                    continue;
                }
                String times[] = getValues(check[i],
                        "\\d\\d:\\d\\d");
                String codeinfo = getValue(check[i], "JW&nbsp;\\d+");
                String codenumber = getValue(codeinfo, "\\d+");
                String code = "JW" + codenumber;
                String typeinfo = getValue(check[i], "機材 : \\d+");
                String type = getValue(typeinfo, "\\d+");
                String priceinfos[] = getValues(check[i],
                        CurrentCode + " \\d+[,]\\d+");
                int pricenum = priceinfos.length;
                String price = getValue(priceinfos[pricenum - 1],
                        "\\d+[,]\\d+");
                detail.setDepcity(citys[0]);
                detail.setArrcity(citys[1]);
                detail.setDepdate(new SimpleDateFormat("yyyy-MM-dd").parse(flightSearchParam.getDepDate()));
                List<String> flightNoList = Lists.newArrayList();
                flightNoList.add(code);
                detail.setFlightno(flightNoList);
                detail.setMonetaryunit(CurrentCode);
                Float taxFloat = Float.parseFloat(tax);
                detail.setTax(taxFloat);
                Double priceDouble = Double.parseDouble(price.replace(",",""));
                detail.setPrice(priceDouble);
                detail.setWrapperid("gjdairjw001");
                detail.setCreatetime(new Timestamp(System.currentTimeMillis()));
                detail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
                oneWayFlightInfo.setDetail(detail);
                flightSegement.setDepairport(citys[0]);
                flightSegement.setArrairport(citys[1]);
                flightSegement.setFlightno(code);
                flightSegement.setDeptime(times[0]);
                flightSegement.setArrtime(times[1]);
                flightSegement.setDepDate(flightSearchParam.getDepDate());
                flightSegement.setArrDate("");   //到达日期
                flightSegement.setCompany("JW");
                segements.add(flightSegement);
                oneWayFlightInfo.setInfo(segements);
                oneWayFlightInfos.add(oneWayFlightInfo);
            }
            processResultInfo.setData(oneWayFlightInfos);
            processResultInfo.setStatus(Constants.SUCCESS);
            processResultInfo.setRet(true);
            return processResultInfo;
        } catch (Exception e) {
            processResultInfo.setStatus(Constants.PARSING_FAIL);
            processResultInfo.setData(oneWayFlightInfos);
            return processResultInfo;
        }
    }


    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {

        BookingResult bookingResult = new BookingResult();
        bookingResult.setRet(true);
        BookingInfo bookingInfo = new BookingInfo();
//        bookingInfo.setAction("http://www.vanilla-air.com/reservation/ibe/ibe/booking");
        bookingInfo.setAction("http://www.vanilla-air.com/tw");
        bookingInfo.setMethod("post");
        Map<String, String> inputs = Maps.newLinkedHashMap();
        bookingInfo.setInputs(inputs);
        bookingResult.setData(bookingInfo);
        return bookingResult;
    }

    private String dateFormat(String orgDate) throws ParseException {
        String date = new SimpleDateFormat("dd-MM-yyyy")
                .format(new SimpleDateFormat("yyyy-MM-dd").parse(orgDate));
        String[] splitDate = date.split("-");
        String mon = splitDate[1];
        String ens[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec" };
        int month = Integer.parseInt(mon);
        String en = ens[month - 1];
        return splitDate[0] + "-" + en + "-" + splitDate[2];
    }

    public static String[] getValues(String source, String regEx) {
        Vector<String> vec = new Vector<String>(5);
        Matcher mm = Pattern.compile(regEx).matcher(source);
        while (mm.find()) {
            vec.add(mm.group(mm.groupCount() > 0 ? 1 : 0));
        }
        return vec.toArray(new String[0]);
    }

    public static String getValue(String source, String regEx) {
        Matcher mm = Pattern.compile(regEx).matcher(source);
        return mm.find() ? mm.group(mm.groupCount() > 0 ? 1 : 0) : "";
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



}
