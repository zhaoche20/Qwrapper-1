
import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * Insert the type's description here. Creation date: (2010-6-2 14:21:55)
 * Modification date: (2010-6-2 14:21:55)
 * 
 * @author:
 */
public class Wrapper_gjdairkl001 implements QunarCrawler {

	private QFHttpClient httpClient = null;
	
	private String getRequest(Vector<String> requiredKeywords) {
		StringBuilder builder = new StringBuilder();
		builder.append("<soap:Envelope ");
		builder
				.append("\r\nxmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" ");
		builder
				.append("\r\nxmlns:ns=\"http://www.opentravel.org/OTA/2003/05\"");
		builder
				.append("\r\nxmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">");
		builder.append("\r\n<soap:Header>");
		builder.append("\r\n            <wsse:Security>");
		builder.append("\r\n                        <wsse:UsernameToken>");
		builder
				.append("\r\n                                    <wsse:Username>w06705835</wsse:Username>");
		builder
				.append("\r\n                                    <wsse:Password>pe7DOgoX4PfqcuN</wsse:Password>");
		builder.append("\r\n                        </wsse:UsernameToken>");
		builder.append("\r\n            </wsse:Security>");
		builder.append("\r\n   </soap:Header>        ");
		builder.append("\r\n   <soap:Body>");
		builder.append("\r\n      <ns:OTA_AirLowFareSearchRQ>");
		builder
				.append("\r\n         <ns:KLM_MetaSearchParty>QU</ns:KLM_MetaSearchParty>");
		builder.append("\r\n         <!--Optional:-->");
		builder.append("\r\n         <ns:KLM_Lang>cn</ns:KLM_Lang>");
		builder.append("\r\n         <ns:OriginDestinationInformation>");
		builder
				.append("\r\n            <ns:DepartureDateTime WindowBefore=\"P0D\" WindowAfter=\"P0D\">"
						+ requiredKeywords.get(2) + "</ns:DepartureDateTime>");
		builder
				.append("\r\n            <ns:OriginLocation MultiAirportCityInd=\"false\">"
						+ requiredKeywords.get(0) + "</ns:OriginLocation>");
		builder
				.append("\r\n            <ns:DestinationLocation MultiAirportCityInd=\"false\">"
						+ requiredKeywords.get(1) + "</ns:DestinationLocation>");
		builder.append("\r\n         </ns:OriginDestinationInformation>");

		builder.append("\r\n         <ns:TravelerInfoSummary>");
		builder.append("\r\n            <ns:AirTravelerAvail>");
		builder.append("\r\n               <ns:AirTraveler Quantity=\"1\"/>");
		builder.append("\r\n            </ns:AirTravelerAvail>");
		builder
				.append("\r\n            <ns:PriceRequestInformation CabinType=\"Economy\"/>");
		builder.append("\r\n         </ns:TravelerInfoSummary>");
		builder.append("\r\n         <!--Optional:-->");
		builder.append("\r\n         <ns:TravelPreferences>");
		builder
				.append("\r\n            <ns:FlightTypePref FlightType=\"Connection\" MaxConnections=\"2\"/>");
		builder.append("\r\n         </ns:TravelPreferences>");
		builder.append("\r\n      </ns:OTA_AirLowFareSearchRQ>");
		builder.append("\r\n   </soap:Body>");
		builder.append("\r\n</soap:Envelope>");
		return builder.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Wrapper_gjdairkl001 p = new Wrapper_gjdairkl001();
		FlightSearchParam flightSearchParam = new FlightSearchParam();
//		flightSearchParam.setDep("PVG");
//		flightSearchParam.setArr("AMS");
		flightSearchParam.setDep("YYZ");
		flightSearchParam.setArr("PEK");
//		flightSearchParam.setDepDate("2014-07-12");
		flightSearchParam.setDepDate("2015-06-26");
		flightSearchParam.setWrapperid("gjdairkl001");
		flightSearchParam.setQueryId("http://???");
		String html = p.getHtml(flightSearchParam);
		System.out.println("开始打印：" + html);
		System.out.println("结束打印");
		System.out.println(JSON.toJSONString(p.process(html, flightSearchParam)));
	}

	public String getHtml(FlightSearchParam flightSearchParam) {
		httpClient = new QFHttpClient(flightSearchParam, true);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		QFPostMethod post = new QFPostMethod(
				"https://services.klm.com/MetaSearchService/MetaSearchService_v1.asmx");
		Vector<String> requiredKeywords = new Vector<String>();
		requiredKeywords.add(flightSearchParam.getDep());
		requiredKeywords.add(flightSearchParam.getArr());
		requiredKeywords.add(flightSearchParam.getDepDate());
		try {
			String request = getRequest(requiredKeywords);
			post.addRequestHeader("Content-Type", "text/xml;charset=UTF-8");
			post.addRequestHeader("Accept-Encoding", "gzip,deflate");
			post.addRequestHeader("Host", "services.klm.com");
			post.addRequestHeader("User-Agent",
					"Jakarta Commons-HttpClient/3.1");

			post.addRequestHeader("SOAPAction",
							"\"http://www.klm.com/schema/services/qo/metasearch:oTA_AirLowFareSearchRQIn\"");

			post.setRequestEntity(new StringRequestEntity(request,
						"application/x-www-form-urlencoded", "UTF-8"));
			httpClient.executeMethod(post);
			String result = post.getResponseBodyAsString().replaceAll("^\\s+",
						"");
			return result;
			}catch (Exception e) {
				return "Exception";
			} finally {
				if (post != null) {
					post.releaseConnection();
				}
			}
	}

	public ProcessResultInfo process(String html,
			FlightSearchParam flightSearchParam) {
		ProcessResultInfo processResultInfo = new ProcessResultInfo();
		ArrayList<OneWayFlightInfo> oneWayFlightInfos = new ArrayList<OneWayFlightInfo>();
		if ("Exception".equals(html)) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
		// see if it is an xml file
		
		if (!html.contains("<?xml version")) {
			processResultInfo.setStatus(Constants.PARSING_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(html)));
			Element root = doc.getDocumentElement();
			NodeList airItinerarys = root.getElementsByTagName("AirItinerary");
			NodeList priceInfos = root
					.getElementsByTagName("AirItineraryPricingInfo");
			NodeList deeplinkURLs = root.getElementsByTagName("deeplinkURL");
			int routeLNum = airItinerarys.getLength();
			if (routeLNum == 0) {
				// no fare node, no route
				processResultInfo.setStatus(Constants.INVALID_DATE);
				processResultInfo.setData(oneWayFlightInfos);
				return processResultInfo;
			}
			for (int i = 0; i < routeLNum; i++) {
				OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
				List<FlightSegement> segements = new ArrayList<FlightSegement>();
				FlightDetail detail = new FlightDetail();
				List<String> flightNoList = Lists.newArrayList();

				Element airItinerary = (Element) airItinerarys.item(i);
				Element priceInfo = (Element) priceInfos.item(i);
				NodeList airChildren = airItinerary.getChildNodes();
				NodeList flights = airChildren.item(0).getChildNodes().item(0)
						.getChildNodes();
				String price = priceInfo.getElementsByTagName("BaseFare")
						.item(0).getAttributes().getNamedItem("Amount")
						.getNodeValue(); // 价格
				System.out.println("价格"+price);
				String monetaryunit = priceInfo.getFirstChild().getFirstChild()
						.getAttributes().getNamedItem("CurrencyCode")
						.getNodeValue(); // 货币单位
				Float tax = Float.parseFloat(priceInfo
						.getElementsByTagName("Tax").item(0).getAttributes()
						.getNamedItem("Amount").getNodeValue())
						+ Float.parseFloat(priceInfo
								.getElementsByTagName("Fee").item(0)
								.getAttributes().getNamedItem("Amount")
								.getNodeValue()); // 税
				System.out.println("税"+tax);
				String type = flights.item(0).getLastChild().getAttributes()
						.getNamedItem("AirEquipType").getNodeValue(); // type是驶出机型
				String code = "";
				
				for (int j = 1; j < flights.getLength(); j++) {
					if (!type.equals(flights.item(j).getLastChild()
							.getAttributes().getNamedItem("AirEquipType")
							.getNodeValue())) {
						type = "0";
						break;
					}
				}

				for (int j = 0; j < flights.getLength(); j++) {
					code += flights.item(j).getAttributes()
							.getNamedItem("FlightNumber").getNodeValue()
							+ "/";
				}
				code = code.substring(0, code.length() - 1); // code是驶出航班号

				
//				NodeList segmentNodes = airItinerary
//						.getElementsByTagName("FlightSegment");
				NodeList segmentNodes = flights;
				String param = "&c[0].os="+flightSearchParam.getDep()+"&c[0].dd="+flightSearchParam.getDepDate();
				for (int j = 0; j < segmentNodes.getLength(); j++) {
					Element seg = (Element) segmentNodes.item(j);
					String depAirport = seg.getChildNodes().item(0)
							.getAttributes().getNamedItem("LocationCode")
							.getNodeValue(); // 出发机场
					String arrAirport = seg.getChildNodes().item(1)
							.getAttributes().getNamedItem("LocationCode")
							.getNodeValue();// 到达机场
					String flightCode = seg.getAttribute("FlightNumber");
					flightNoList.add(flightCode);

					String deptime = seg.getAttribute("DepartureDateTime")
							.substring(11, 16);// 驶出出发时间
					String arrTime = seg.getAttribute("ArrivalDateTime")
							.substring(11, 16);// 驶出到达时间
					String depDate = seg.getAttribute("DepartureDateTime")
							.substring(0, 10);// 驶出出发日期
					String arrDate = seg.getAttribute("ArrivalDateTime")
							.substring(0, 10);// 驶出到达日期
					String aircraft = seg.getChildNodes().item(3)
							.getNodeValue();

					FlightSegement flightSegement = new FlightSegement();
					flightSegement.setDepairport(depAirport);
					flightSegement.setArrairport(arrAirport);
					flightSegement.setFlightno(flightCode);
					flightSegement.setDeptime(deptime);
					flightSegement.setArrtime(arrTime);
					flightSegement.setDepDate(depDate);
					flightSegement.setArrDate(arrDate);
					flightSegement.setCompany("KL");
					flightSegement.setAircraft(aircraft);

					segements.add(flightSegement);
					String mc = flightCode.substring(0, 2);
					String formatFcode = flightCode.substring(2);
					if(formatFcode.length()<4){
						for(int k=0;k<4-formatFcode.length();k++){
							formatFcode = "0"+formatFcode;
						}
						
					}
					param+="&c[0].s["+j+"].os="+depAirport+"&c[0].s["+j+"].ds="+arrAirport+"&c[0].s["+j+"].dd="+depDate
							+"&c[0].s["+j+"].dt="+deptime.replace(":", "").replace("：", "")+"&c[0].s["+j+"].mc="+mc+"&c[0].s["+j+"].fn="+formatFcode;
				}
				param+="&c[0].ds="+flightSearchParam.getArr();
				detail.setDepcity(flightSearchParam.getDep());
				detail.setArrcity(flightSearchParam.getArr());
				detail.setFlightno(flightNoList);
				detail.setMonetaryunit(monetaryunit);
				detail.setTax(tax);
				detail.setPrice(Double.parseDouble(price));
				detail.setWrapperid("gjdairkl001");
				detail.setCreatetime(new Timestamp(System.currentTimeMillis()));
				detail.setUpdatetime(new Timestamp(System.currentTimeMillis()));
				
				String bookingUrl = "https://www.klm.com/travel/cn_cn/apps/ebt/ebt_home.htm?goToPage=customize&frame=b2c&pos=CN&lang=CN&mp=QU&msp=Qunar&cffcc=ECONOMY&adtQty=1&chdQty=0&infQty=0&dev=5&cur="+monetaryunit+"&WT.mc_id=C_CN_metasearch_Qunar_organic_website_Null_Null"+param;
//				System.out.println("booking链接"+bookingUrl);
				detail.setBookingUrl(bookingUrl);
				try {
					detail.setDepdate(new SimpleDateFormat("yyyy-MM-dd")
							.parse(flightSearchParam.getDepDate()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				oneWayFlightInfo.setInfo(segements);
				oneWayFlightInfo.setDetail(detail);
				oneWayFlightInfos.add(oneWayFlightInfo);
			}
			processResultInfo.setData(oneWayFlightInfos);
			processResultInfo.setStatus(Constants.SUCCESS);
			processResultInfo.setRet(true);
			processResultInfo.setKey(flightSearchParam.getQueryId());
			return processResultInfo;
		} catch (IOException e) {
			processResultInfo.setStatus(Constants.CONNECTION_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		} catch (Exception e) {
			processResultInfo.setStatus(Constants.PARSING_FAIL);
			processResultInfo.setData(oneWayFlightInfos);
			return processResultInfo;
		}
	}

	
	public BookingResult getBookingInfo(FlightSearchParam flightSearchParam) {
		/*BookingResult bookingResult = new BookingResult();
		bookingResult.setRet(true);
		BookingInfo bookingInfo = new BookingInfo();
		String url = "https://www.klm.com/travel/cn_cn/apps/ebt/ebt_home.htm";
		Map<String, String> inputs = Maps.newLinkedHashMap();
			
        inputs.put("goToPage", "customize");
        inputs.put("frame", "b2");
        inputs.put("pos", "CN");
        inputs.put("lang", "CN");
        inputs.put("mp", "QU");
        inputs.put("msp", "Qunar");
        inputs.put("cffcc", "ECONOMY");
        inputs.put("adtQty", "1");
        inputs.put("chdQty", "0");
        inputs.put("infQty", "0");
        inputs.put("dev", "5");
        inputs.put("cur", "CNY");
//        inputs.put("tp", "13506.00");
//        inputs.put("tf", "12080.00");
        inputs.put("c[0].os", flightSearchParam.getDep());
        inputs.put("c[0].dd", flightSearchParam.getDepDate());
        inputs.put("c[0].s[0].os", flightSearchParam.getDep());
        inputs.put("c[0].s[0].ds", flightSearchParam.getArr());
        inputs.put("c[0].s[0].dd", flightSearchParam.getDepDate());
        inputs.put("c[0].s[0].dt", "2320");
        inputs.put("c[0].s[0].mc", "KL");
        inputs.put("c[0].s[0].fn", "0894");
        inputs.put("c[0].ds", flightSearchParam.getArr());
        inputs.put("ref", "MS,QU,fb=MFFWCN,fee=0,oper=KL,srv=DDG");
        inputs.put("WT.tsrc", "metasearch");
        inputs.put("WT.mc_id", "C_CN_metasearch_Qunar_organic_website_Null_Null");
        
        bookingInfo.setInputs(inputs);
		System.out.println(url);
		bookingInfo.setMethod("post");
		bookingInfo.setAction(url);
		bookingResult.setData(bookingInfo);*/
		return null;
	}

}
