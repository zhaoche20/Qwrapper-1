package kangchao;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
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
 * Initial Created by kangchao at 14-6-8
 * <p/>
 * Description:
 */
public class Wrapper_gjdairba002 implements QunarCrawler {
    private static final String CODEBASE = "gjdairba002";
    public static void main(String[] args) {
        Wrapper_gjdairba002 p = new Wrapper_gjdairba002();
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("LHR");
        flightSearchParam.setArr("SFO");
        flightSearchParam.setDepDate("2014-07-18");
        String html = p.getHtml(flightSearchParam);
        //System.out.println(".."+html);
        System.out.println(p.process(html, flightSearchParam));
    }


    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        /*Protocol myhttps = new Protocol("https", new MineSSLSocketFactory(),443);
        Protocol.registerProtocol("https", myhttps);*/
        QFHttpClient httpClient = new QFHttpClient(flightSearchParam, true);

        QFGetMethod get = null;
        try {
            String getURL = "https://api.ba.com/rest-v1/v1/flightOfferMktAffiliates;" +
                    "departureDateTimeOutbound=" + depDate + "T00:00:00Z;locationCodeOriginOutbound=" + dep +
                    ";locationCodeDestinationOutbound=" + arr + ";cabin=Economy;ADT=1;CHD=0;INF=0;format=.json";
            get = new QFGetMethod(getURL);
            get.addRequestHeader("Client-Key","v25re37fnf6yns8vvnucmza5");
            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();
        } catch (Exception e) {
            return "Exception";
        } finally{
            if(get!=null){
                get.releaseConnection();
            }
        }
    }


    public ProcessResultInfo process(String html, FlightSearchParam param) {
        ProcessResultInfo result = new ProcessResultInfo();
        List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
        if ("Exception".equals(html)) {
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        if (html.startsWith("StatusError")) {
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }

        try {
            JSONObject htmlObject = JSON.parseObject(html);
            //如果只有一趟航班得到的对象为JSONObject对象；否则为JSONArray对象，所以分开处理
            //只有一趟航班
            if(htmlObject.isEmpty()){
                result.setStatus(Constants.NO_RESULT);
                return result;
            }
            if(!html.contains("SequenceNumber\":\"2"))
            {
                OneWayFlightInfo baseFlight = new OneWayFlightInfo();
                List<FlightSegement> segs = new ArrayList<FlightSegement>();
                FlightDetail flightDetail = new FlightDetail();
                FlightSegement seg = new FlightSegement();

                JSONObject jsonObject = htmlObject.getJSONObject("OTA_AirLowFareSearchRS").getJSONObject("PricedItineraries").getJSONObject("PricedItinerary");
                //containPriceObject包含价格、税费和货币符号
                JSONObject containPriceObject = jsonObject.getJSONObject("AirItineraryPricingInfo").getJSONObject("ItinTotalFare");
                //cotainCurrencyCodeAndPriceObject包含机票价格和货币符号
                JSONObject cotainCurrencyCodeAndPriceObject = containPriceObject.getJSONObject("BaseFare");
                Double retailPrice = cotainCurrencyCodeAndPriceObject.getDouble("@Amount");
                String currencyCode = cotainCurrencyCodeAndPriceObject.getString("@CurrencyCode");
                Double tax = containPriceObject.getJSONObject("Taxes").getJSONObject("Tax").getDouble("@Amount");
                //containFlightInfos包含单程航班的航班号，起飞降落机场、时间等信息
                JSONObject containFlightInfos = jsonObject.getJSONObject("AirItinerary").getJSONObject("OriginDestinationOptions").getJSONObject("OriginDestinationOption").getJSONObject("FlightSegment");
                //得到起降时间信息
                String arrivalTime = formatTimeString(containFlightInfos.getString("@ArrivalDateTime"));
                String departureTime = formatTimeString(containFlightInfos.getString("@DepartureDateTime"));

                //得到起降日期 信息
                String arrivalDate = formatDateString(containFlightInfos.getString("@ArrivalDateTime"));
                String departureDate = formatDateString(containFlightInfos.getString("@DepartureDateTime"));
                //得到航班号的数字部分
                String numPartOfFlightCode = containFlightInfos.getString("@FlightNumber");
                //得到起降机场信息
                String departureAirport = containFlightInfos.getJSONObject("DepartureAirport").getString("@LocationCode");
                String arrivalAirport = containFlightInfos.getJSONObject("ArrivalAirport").getString("@LocationCode");
                //得到机型
                String plantType = containFlightInfos.getJSONObject("Equipment").getString("@AirEquipType");
                //得到航班号的字母部分
                String letPartOfFlightCode = containFlightInfos.getJSONObject("MarketingAirline").getString("@Code");

                List<String> flightno = Lists.newArrayList();
                flightno.add(letPartOfFlightCode + numPartOfFlightCode);
                seg.setDepairport(departureAirport);
                seg.setArrairport(arrivalAirport);
                seg.setFlightno(letPartOfFlightCode + numPartOfFlightCode);
                seg.setDeptime(departureTime);
                seg.setArrtime(arrivalTime);
                seg.setDepDate(departureDate);
                seg.setArrDate(arrivalDate);
                seg.setAircraft(plantType);
                segs.add(seg);

                flightDetail.setDepcity(param.getDep());
                flightDetail.setArrcity(param.getArr());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                flightDetail.setDepdate(sdf.parse(param.getDepDate()));
                flightDetail.setFlightno(flightno);
                flightDetail.setTax(tax);
                flightDetail.setPrice(retailPrice);
                flightDetail.setMonetaryunit(currencyCode);
                flightDetail.setWrapperid(CODEBASE);

                baseFlight.setDetail(flightDetail);
                baseFlight.setInfo(segs);

                flightList.add(baseFlight);
                result.setData(flightList);
                result.setStatus(Constants.SUCCESS);
                return result;
            }
            //含有多个航班
            else
            {

                //jsonObject包含所需的全部航班信息
                JSONArray jsonArray = htmlObject.getJSONObject("OTA_AirLowFareSearchRS").getJSONObject("PricedItineraries").getJSONArray("PricedItinerary");

                for(int i=0 ; i<jsonArray.size() ; i++)
                {
                    OneWayFlightInfo baseFlight = new OneWayFlightInfo();
                    List<FlightSegement> segs = new ArrayList<FlightSegement>();
                    FlightDetail flightDetail = new FlightDetail();
                    FlightSegement seg = new FlightSegement();

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //containPriceObject包含价格、税费和货币符号
                    JSONObject containPriceObject = jsonObject.getJSONObject("AirItineraryPricingInfo").getJSONObject("ItinTotalFare");
                    //cotainCurrencyCodeAndPriceObject包含机票价格和货币符号
                    JSONObject cotainCurrencyCodeAndPriceObject = containPriceObject.getJSONObject("BaseFare");
                    Double retailPrice = cotainCurrencyCodeAndPriceObject.getDouble("@Amount");
                    String currencyCode = cotainCurrencyCodeAndPriceObject.getString("@CurrencyCode");
                    Double tax = containPriceObject.getJSONObject("Taxes").getJSONObject("Tax").getDouble("@Amount");
                    //containFlightInfos包含单程航班的航班号，起飞降落机场、时间等信息
                    JSONObject containFlightInfos = jsonObject.getJSONObject("AirItinerary").getJSONObject("OriginDestinationOptions").getJSONObject("OriginDestinationOption").getJSONObject("FlightSegment");
                    //得到起降时间信息
                    String arrivalTime = formatTimeString(containFlightInfos.getString("@ArrivalDateTime"));
                    String departureTime = formatTimeString(containFlightInfos.getString("@DepartureDateTime"));
                    //得到起降日期 信息
                    String arrivalDate = formatDateString(containFlightInfos.getString("@ArrivalDateTime"));
                    String departureDate = formatDateString(containFlightInfos.getString("@DepartureDateTime"));
                    //得到航班号的数字部分
                    String numPartOfFlightCode = containFlightInfos.getString("@FlightNumber");
                    //得到起降机场信息
                    String departureAirport = containFlightInfos.getJSONObject("DepartureAirport").getString("@LocationCode");
                    String arrivalAirport = containFlightInfos.getJSONObject("ArrivalAirport").getString("@LocationCode");
                    //得到机型
                    String plantType = containFlightInfos.getJSONObject("Equipment").getString("@AirEquipType");
                    //得到航班号的字母部分
                    String letPartOfFlightCode = containFlightInfos.getJSONObject("MarketingAirline").getString("@Code");
                    List<String> flightno = Lists.newArrayList();
                    flightno.add(letPartOfFlightCode + numPartOfFlightCode);
                    seg.setDepairport(departureAirport);
                    seg.setArrairport(arrivalAirport);
                    seg.setFlightno(letPartOfFlightCode + numPartOfFlightCode);
                    seg.setDeptime(departureTime);
                    seg.setArrtime(arrivalTime);
                    seg.setDepDate(departureDate);
                    seg.setArrDate(arrivalDate);
                    seg.setAircraft(plantType);
                    segs.add(seg);

                    flightDetail.setDepcity(param.getDep());
                    flightDetail.setArrcity(param.getArr());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    flightDetail.setDepdate(sdf.parse(param.getDepDate()));
                    flightDetail.setFlightno(flightno);
                    flightDetail.setTax(tax);
                    flightDetail.setPrice(retailPrice);
                    flightDetail.setMonetaryunit(currencyCode);
                    flightDetail.setWrapperid(CODEBASE);

                    baseFlight.setDetail(flightDetail);
                    baseFlight.setInfo(segs);

                    flightList.add(baseFlight);
                }
                result.setData(flightList);
                result.setStatus(Constants.SUCCESS);
                return result;
            }
        } catch (Exception e) {
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }

    }


    public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
        BookingResult result = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        String[] depdate = flightSearchParam.getDepDate().split("-");
        String depDate = depdate[2] + "/" + depdate[1] + "/" + depdate[0].substring(2, 4);
        String url = "http://prf.hn/click/camref:11lvzr/destination:http://ad.doubleclick.net/clk;279260675;106345031;c?http://ba.com/travel/fx/public/zh_cn?eId=111009&from="+flightSearchParam.getDep()+"&to="+flightSearchParam.getArr()+"&ad=1&ch=0&inf=0&cabin=M&depDate="+depDate+"&myDatesAreFixedSelected=true&restrictionType=LOWEST&DM1_Mkt=CN&DM1_Channel=AFF&DM1_Campaign=AO&DM1_Site=PHG&utm_source=PHG&utm_medium=Affiliate&utm_campaign=PHG";
        bookingInfo.setAction(url);
        bookingInfo.setMethod("get");
        result.setData(bookingInfo);
        result.setRet(true);
        return result;
    }

    private static String formatTimeString(String timeStr)
    {
        String time = timeStr.split("T")[1];
        String[] temp = time.split(":");
        return temp[0] + ":" + temp[1];
    }

    private static String formatDateString(String timeStr)
    {
        String date = timeStr.split("T")[0];
        return date;
    }



}

   
    
