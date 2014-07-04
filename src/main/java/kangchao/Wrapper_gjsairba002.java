package kangchao;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.protocol.Protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
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
/**
 * Initial Created by kangchao at 14-6-8
 * <p/>
 * Description:
 */
public class Wrapper_gjsairba002 implements QunarCrawler {
    private static final String CODEBASE = "gjsairba002";
    public static void main(String[] args) {
        Wrapper_gjsairba002 p = new Wrapper_gjsairba002();
        //dep=LHR&to=SFO&depDate=18/07/14&retDate=25/07/14
        FlightSearchParam flightSearchParam = new FlightSearchParam();
        flightSearchParam.setDep("LHR");
        flightSearchParam.setArr("SFO");
        flightSearchParam.setDepDate("2014-07-18");
        flightSearchParam.setRetDate("2014-07-25");
        String html = p.getHtml(flightSearchParam);
        //System.out.println(".."+html);
        System.out.println(p.process(html, flightSearchParam));
    }


    public String getHtml(FlightSearchParam flightSearchParam) {
        String dep = flightSearchParam.getDep();
        String arr = flightSearchParam.getArr();
        String depDate = flightSearchParam.getDepDate();
        String retDate = flightSearchParam.getRetDate();
        Protocol myhttps = new Protocol("https", new MineSSLSocketFactory(),443);
        Protocol.registerProtocol("https", myhttps);
        QFHttpClient httpClient = new QFHttpClient(flightSearchParam, true);

        QFGetMethod get = null;
        try {
            String getURL =" https://api.ba.com/rest-v1/v1/flightOfferMktAffiliates;" +
					"departureDateTimeOutbound=" + depDate + "T00:00:00Z;locationCodeOriginOutbound=" + dep + ";" +
					"locationCodeDestinationOutbound=" + arr + ";departureDateTimeInbound=" + retDate + "T00:00:00Z;" +
					"locationCodeOriginInbound=" + arr + ";locationCodeDestinationInbound=" + dep + ";cabin=Economy;ADT=1;" +
					"CHD=0;INF=0;format=.json";
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
        List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
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
                JSONObject jsonObject = htmlObject.getJSONObject("OTA_AirLowFareSearchRS").getJSONObject("PricedItineraries").getJSONObject("PricedItinerary");
                
                RoundTripFlightInfo baseFlight = getOneRoundTripFlight(jsonObject,param);
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

                for(int i=0 ; i<jsonArray.size();i++)
                {
                	JSONObject jsonObject = jsonArray.getJSONObject(i);
                	RoundTripFlightInfo baseFlight = getOneRoundTripFlight(jsonObject,param);
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
        String[] retdate = flightSearchParam.getRetDate().split("-");
        String retDate = retdate[2] + "/" + retdate[1] + "/" + retdate[0].substring(2, 4);
        String url = "http://prf.hn/click/camref:11lvzr/destination:http://ad.doubleclick.net/clk;279260675;106345031;c?http://ba.com/travel/fx/public/zh_cn?"
        		+ "eId=111009&from="+ flightSearchParam.getDep() +"&to="+ flightSearchParam.getArr()+"&ad=1&cabin=M&depDate="+depDate+"&retDate="+retDate+"&myDatesAreFixedSelected=true&restrictionType=LOWEST&"
        		+ "DM1_Mkt=CN&DM1_Channel=AFF&DM1_Campaign=AO&DM1_Site=PHG&utm_source=PHG&utm_medium=Affiliate&utm_campaign=PHG";
        bookingInfo.setAction(url);
        bookingInfo.setMethod("get");
        result.setData(bookingInfo);
        result.setRet(true);
        return result;
    }
    
    private  RoundTripFlightInfo getOneRoundTripFlight(JSONObject jsonObject,FlightSearchParam param){
    	RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
        List<FlightSegement> segouts = new ArrayList<FlightSegement>();
        List<FlightSegement> segins = new ArrayList<FlightSegement>();
        FlightDetail flightDetail = new FlightDetail();
        FlightSegement segout = new FlightSegement();
        FlightSegement segin = new FlightSegement();

    	 //containPriceObject包含价格、税费和货币符号
        JSONObject containPriceObject = jsonObject.getJSONObject("AirItineraryPricingInfo").getJSONObject("ItinTotalFare");
       
      //公共信息：cotainCurrencyCodeAndPriceObject包含机票价格和货币符号
        JSONObject cotainCurrencyCodeAndPriceObject = containPriceObject.getJSONObject("BaseFare");
        Double retailPrice = cotainCurrencyCodeAndPriceObject.getDouble("@Amount");
        String currencyCode = cotainCurrencyCodeAndPriceObject.getString("@CurrencyCode");
        Double tax = containPriceObject.getJSONObject("Taxes").getJSONObject("Tax").getDouble("@Amount");
		
		//containFlightInfos包含去程和单程航班的航班号，起飞降落机场、时间等信息，
		//由于本接口只支持直飞，所以containFlightInfos包含两个航班的信息（一个去程一个返程）
		JSONArray containFlightInfos = jsonObject.getJSONObject("AirItinerary").getJSONObject("OriginDestinationOptions").getJSONArray("OriginDestinationOption");
		
		//去程信息：containFlightInfos的第一个元素是去程
		JSONObject outboundFlightObject = containFlightInfos.getJSONObject(0).getJSONObject("FlightSegment");
		String numPartOfOutboundFlightCode = outboundFlightObject.getString("@FlightNumber");
		String outboundDepTime = formatTimeString(outboundFlightObject.getString("@DepartureDateTime"));
		String outboundArrTime = formatTimeString(outboundFlightObject.getString("@ArrivalDateTime"));
		String outboundarrivalDate = formatDateString(outboundFlightObject.getString("@ArrivalDateTime"));
        String outbounddepartureDate = formatDateString(outboundFlightObject.getString("@DepartureDateTime"));
		String outboundDepAirport = outboundFlightObject.getJSONObject("DepartureAirport").getString("@LocationCode");
		String outboundArrAirport = outboundFlightObject.getJSONObject("ArrivalAirport").getString("@LocationCode");
		String outboundPlaneType = outboundFlightObject.getJSONObject("Equipment").getString("@AirEquipType");
		String letPartOfOutboundFlightCode = outboundFlightObject.getJSONObject("MarketingAirline").getString("@Code");
		
		//返程信息：containFlightInfos的第二个元素是返程
		JSONObject returnedFlightObject = containFlightInfos.getJSONObject(1).getJSONObject("FlightSegment");
		String numPartOfReturnedFlightCode = returnedFlightObject.getString("@FlightNumber");
		String returnedDepTime = formatTimeString(returnedFlightObject.getString("@DepartureDateTime"));
		String returnedArrTime = formatTimeString(returnedFlightObject.getString("@ArrivalDateTime"));
	    String returnedarrivalDate = formatDateString(returnedFlightObject.getString("@ArrivalDateTime"));
        String returneddepartureDate = formatDateString(returnedFlightObject.getString("@DepartureDateTime"));
		String returnedDepAirport = returnedFlightObject.getJSONObject("DepartureAirport").getString("@LocationCode");
		String returnedArrAirport = returnedFlightObject.getJSONObject("ArrivalAirport").getString("@LocationCode");
		String returnedPlaneType = returnedFlightObject.getJSONObject("Equipment").getString("@AirEquipType");
		String letPartOfReturnedFlightCode = returnedFlightObject.getJSONObject("MarketingAirline").getString("@Code");
		
        List<String> flightnoout = Lists.newArrayList();
        List<String> flightnoret = Lists.newArrayList();
        flightnoout.add(letPartOfOutboundFlightCode + numPartOfOutboundFlightCode);
        flightnoret.add(letPartOfReturnedFlightCode + numPartOfReturnedFlightCode);
        
        segout.setDepairport(outboundDepAirport);
        segout.setArrairport(outboundArrAirport);
        segout.setFlightno(letPartOfOutboundFlightCode + numPartOfOutboundFlightCode);
        segout.setDeptime(outboundDepTime);
        segout.setArrtime(outboundArrTime);
        segout.setDepDate(outbounddepartureDate);
        segout.setArrDate(outboundarrivalDate);
        segout.setAircraft(outboundPlaneType);
        segouts.add(segout);
        
        segin.setDepairport(returnedDepAirport);
        segin.setArrairport(returnedArrAirport);
        segin.setFlightno(letPartOfReturnedFlightCode + numPartOfReturnedFlightCode);
        segin.setDeptime(returnedDepTime);
        segin.setArrtime(returnedArrTime);
        segin.setDepDate(returneddepartureDate);
        segin.setArrDate(returnedarrivalDate);
        segin.setAircraft(returnedPlaneType);
        segins.add(segin);

        flightDetail.setDepcity(param.getDep());
        flightDetail.setArrcity(param.getArr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
			flightDetail.setDepdate(sdf.parse(param.getDepDate()));
		} catch (ParseException e) {
			
		}
        flightDetail.setFlightno(flightnoout);
        flightDetail.setTax(tax);
        flightDetail.setPrice(retailPrice);
        flightDetail.setMonetaryunit(currencyCode);
        flightDetail.setWrapperid(CODEBASE);

        baseFlight.setDetail(flightDetail);
        baseFlight.setInfo(segouts);
        baseFlight.setRetinfo(segins);
        try {
			baseFlight.setRetdepdate(sdf.parse(param.getRetDate()));
		} catch (ParseException e) {
			
		}
        baseFlight.setRetflightno(flightnoret);

        return baseFlight;
    	
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

   
    
