import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
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

public class Wrapper_gjdairay003  implements QunarCrawler  {
	
	private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
	private static final String CODEBASE = "gjdairay003";

	/*
	 * 
	 
	 http://www.finnair.com/FI/GB/deeplink?PREFILLED_INPUT=TRIP_TYPE=return|B_DATES=201208210000|E_DATES=201208210000|B_LOCS=HEL|E_LOCS=VAA|MAIN_PAX=ADT|NB_MAIN_PAX=2|NB_CHD=1|NB_INF=0|CABIN=E|IS_FLEX=true|IS_AWARD=false
	 
	 
	 */
	
	//单程url
	//  http://www.finnair.com/FI/GB/deeplink?PREFILLED_INPUT=TRIP_TYPE=oneWay|B_DATES=201408210000|E_DATES=|B_LOCS=HEL|E_LOCS=VAA|MAIN_PAX=ADT|NB_MAIN_PAX=2|NB_CHD=1|NB_INF=0|CABIN=E|IS_FLEX=true|IS_AWARD=false 
	@Override
	public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
		// TODO Auto-generated method stub
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		
	    bookingInfo.setAction("http://www.finnair.com/FI/GB/deeplink");
	    bookingInfo.setContentType("UTF-8");
	    bookingInfo.setMethod("get");
	        
		Map<String, String> inputs = Maps.newHashMap();
        bookingInfo.setInputs(inputs);
        bookingResult.setRet(true);
        bookingResult.setData(bookingInfo);
        
        String dep=flightSearchParam.getDep();
        String arr=flightSearchParam.getArr();
        String depDate=flightSearchParam.getDepDate();
        
        String bookingDepDate = StringUtils.replace(depDate, "-", "") + "0000";
        		
        
        String bookingRes = String.format("TRIP_TYPE=oneWay|B_DATES=%s|E_DATES=|B_LOCS=%s|E_LOCS=%s|MAIN_PAX=ADT|NB_MAIN_PAX=1|NB_CHD=0|NB_INF=0|CABIN=E|IS_FLEX=true|IS_AWARD=false", bookingDepDate, dep ,arr);
      
      //  System.out.println( bookingRes );
        
        inputs.put("PREFILLED_INPUT", bookingRes);
        
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam paramFlightSearchParam) {
		// TODO Auto-generated method stub
		
		String depCity =  paramFlightSearchParam.getDep();
		String arrCity =  paramFlightSearchParam.getArr();
		String depDate =  paramFlightSearchParam.getDepDate();
		String getUrl = null;
		    
		try {
			paramFlightSearchParam.setToken(null);
			QFHttpClient httpClient = new QFHttpClient(paramFlightSearchParam, false);
			getUrl = "http://amadeus.corp.qunar.com/OneWay?dep=" + depCity + "&arr=" + arrCity + "&depDate=" + depDate + "&CarrierId=AY";
			QFGetMethod get = new QFGetMethod(getUrl);
			httpClient.executeMethod(get);
			String result =  get.getResponseBodyAsString();
			return result;
		} catch (Exception e) {
			logger.warn("amadeus." + CODEBASE + " fetch fail:" + getUrl, e);
		}
		return "Exception";
	}

	@Override
	public ProcessResultInfo process(String html, FlightSearchParam searchParam) {
		// TODO Auto-generated method stub
		ProcessResultInfo processResultInfo=new ProcessResultInfo();
	    List<OneWayFlightInfo> data = new ArrayList<OneWayFlightInfo>();
	    processResultInfo.setData(data);
		String[] res = StringUtils.substringsBetween(html, "<tr>", "</tr>");
		
		// CNY	29220	1823	AY56/AY2645/AY5931	333	CKG	LHR	09:50	19:50
		if(StringUtils.equals(html,  "Exception")) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setRet(false);
			return processResultInfo;
		}
		
		if(res == null ||  res.length == 0) {
			processResultInfo.setStatus(Constants.NO_RESULT);
			processResultInfo.setRet(false);
			return processResultInfo;
		}
		
		for(String iter : res) { // 每个是一个航班
			OneWayFlightInfo oneWayFlightInfo =  new OneWayFlightInfo();
			data.add(oneWayFlightInfo);
			String [] infoItems = StringUtils.substringsBetween(iter, "<td>", "</td>");
		 
			String monetaryunit = infoItems[0];
			String tax = infoItems[1];
			String price = infoItems[2];
			String flightno = infoItems[3];
			String depairport = infoItems[5]; 
			String arrairport = infoItems[6];
			String deptime = infoItems[7];
			String arrtime = infoItems[8];
			
			String [] flightnos  = StringUtils.split(flightno,"/");
			int fSegeMents = flightnos.length;
			int i = 0;
			for(String fNo : flightnos) {
				++i;
				FlightSegement  segement = new FlightSegement();
				oneWayFlightInfo.getInfo().add(segement);
				oneWayFlightInfo.getDetail().getFlightno().add(fNo);
				segement.setFlightno(fNo);
				segement.setDepDate(searchParam.getDepDate());
				segement.setArrDate(searchParam.getDepDate());
				if(1 == i) { // 如果是第一段
					segement.setDepairport(depairport);
					segement.setDeptime(deptime);
				}
				if(fSegeMents == i) { //如果是最后一段
					segement.setArrairport(arrairport);
					segement.setArrtime(arrtime);
				}
				if(StringUtils.isEmpty(segement.getDepairport())) {
					segement.setDepairport("XXX");
				}
                if(StringUtils.isEmpty(segement.getArrairport())) {
                	segement.setArrairport("XXX");
				}
	            if(StringUtils.isEmpty(segement.getDeptime())) {
	            	segement.setDeptime("00:00");
				}
                if(StringUtils.isEmpty(segement.getArrtime())) {
                	segement.setArrtime("00:00");
				}
			}
			processResultInfo.setStatus(Constants.SUCCESS);
			processResultInfo.setRet(true);
			oneWayFlightInfo.getDetail().setWrapperid("gjdairay003");
			oneWayFlightInfo.getDetail().setMonetaryunit(monetaryunit);
			oneWayFlightInfo.getDetail().setDepcity(searchParam.getDep());
			oneWayFlightInfo.getDetail().setArrcity(searchParam.getArr());
			oneWayFlightInfo.getDetail().setDepdate(Date.valueOf(searchParam.getDepDate()));
			oneWayFlightInfo.getDetail().setPrice(Double.valueOf(price));
			oneWayFlightInfo.getDetail().setTax(Double.valueOf(tax));
		}
		
		return processResultInfo;
	}
	// 格式  货币单位   加个 税费   航班号  机型 起降机场  起降时间

	public static void main(String[] args) {
		FlightSearchParam  test  = new FlightSearchParam();
		test.setDep("CKG");
		test.setArr("LHR");
		test.setDepDate("2014-06-29");
		Wrapper_gjdairay003 gjdairay003 = new Wrapper_gjdairay003();
		gjdairay003.getBookingInfo(test);
	}
}
