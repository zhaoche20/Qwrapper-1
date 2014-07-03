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
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;

public class Wrapper_gjsairay003  implements QunarCrawler{
	
	
	private static final Logger logger = LoggerFactory.getLogger("CrawlerLog");
	private static final String CODEBASE = "gjsairay003";

	@Override
	public String getHtml(FlightSearchParam paramFlightSearchParam) {
		// TODO Auto-generated method stub
		
		String depCity =  paramFlightSearchParam.getDep();
		String arrCity =  paramFlightSearchParam.getArr();
		String depDate =  paramFlightSearchParam.getDepDate();
		String retDepdate =  paramFlightSearchParam.getRetDate();
		String getUrl = null;
		    
		try {
			paramFlightSearchParam.setToken(null);
			QFHttpClient httpClient = new QFHttpClient(paramFlightSearchParam, false);
			getUrl = "http://amadeus.corp.qunar.com/RoundTrip?dep=" + depCity + "&arr=" + arrCity + "&depDate=" + depDate +"&arrDate="+ retDepdate + "&CarrierId=AY";
			
			System.out.println(getUrl );
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
		// TODO Auto-generated method stub
		ProcessResultInfo processResultInfo=new ProcessResultInfo();
	    List<RoundTripFlightInfo> data = new ArrayList<RoundTripFlightInfo>();
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
			RoundTripFlightInfo roundTripFlightInfo =  new RoundTripFlightInfo();
			data.add(roundTripFlightInfo);
			String [] infoItems = StringUtils.substringsBetween(iter, "<td>", "</td>");
		 
			String monetaryunit = infoItems[0];
			String tax = infoItems[1];
			String price = infoItems[2];
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
			
			String [] flightnos  = StringUtils.split(flightno,"/");
			int fSegeMents = flightnos.length;
			int i = 0;
			for(String fNo : flightnos) {
				++i;
				FlightSegement  segement = new FlightSegement();
				roundTripFlightInfo.getInfo().add(segement);
				roundTripFlightInfo.getDetail().getFlightno().add(fNo);
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
			
			
			String [] retflightnos  = StringUtils.split(retflightno,"/");
			int retfSegeMents = retflightnos.length;
			int j = 0;
			for(String retfNo : retflightnos) {
				++j;
				FlightSegement  segement = new FlightSegement();
				roundTripFlightInfo.getRetinfo().add(segement);
				roundTripFlightInfo.getRetflightno().add(retfNo);
				segement.setFlightno(retfNo);
				segement.setDepDate(searchParam.getRetDate());
				segement.setArrDate(searchParam.getRetDate());
				if(1 == j) { // 如果是第一段
					segement.setDepairport(retdepairport);
					segement.setDeptime(retdeptime);
				}
				if(retfSegeMents == j) { //如果是最后一段
					segement.setArrairport(retarrairport);
					segement.setArrtime(retarrtime);
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
			roundTripFlightInfo.getDetail().setWrapperid("gjsairay003");
			roundTripFlightInfo.getDetail().setMonetaryunit(monetaryunit);
			roundTripFlightInfo.getDetail().setDepcity(searchParam.getDep());
			roundTripFlightInfo.getDetail().setArrcity(searchParam.getArr());
			roundTripFlightInfo.getDetail().setDepdate(Date.valueOf(searchParam.getDepDate()));
			roundTripFlightInfo.getDetail().setPrice(Double.valueOf(price));
			roundTripFlightInfo.getDetail().setTax(Double.valueOf(tax));
			roundTripFlightInfo.setRetdepdate(Date.valueOf(searchParam.getRetDate()));
		}
		
		return processResultInfo;
	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
		// TODO Auto-generated method stub
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
        String retDepDate=flightSearchParam.getRetDate();
        
        String bookingDepDate = StringUtils.replace(depDate, "-", "") + "0000";
        String bookingretDepDate = StringUtils.replace(retDepDate, "-", "") + "0000";
        		
        
        String bookingRes = String.format("TRIP_TYPE=return|B_DATES=%s|E_DATES=%s|B_LOCS=%s|E_LOCS=%s|MAIN_PAX=ADT|NB_MAIN_PAX=1|NB_CHD=0|NB_INF=0|CABIN=E|IS_FLEX=true|IS_AWARD=false", bookingDepDate,bookingretDepDate, dep ,arr);
      
      //  System.out.println( bookingRes );
        
        inputs.put("PREFILLED_INPUT", bookingRes);
        
		return bookingResult;
	}

}
