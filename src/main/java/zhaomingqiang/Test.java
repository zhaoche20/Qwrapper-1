package zhaomingqiang;

import com.alibaba.fastjson.JSON;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import junit.framework.TestCase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-1
 * Time: 上午9:36
 * To change this template use File | Settings | File Templates.
 */
public class Test extends TestCase {

    public void testWrapper_gjdair5j001()
    {
        long s=System.currentTimeMillis();
        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjdair5j001();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDep("MNL");
        flightSearchParam.setArr("NRT");
        flightSearchParam.setDepDate("2014-07-11");
        //flightSearchParam.setToken("4TU2XY");
        flightSearchParam.setTimeOut("40000");
        String html=qunarCrawler.getHtml(flightSearchParam);
        System.out.println(html);
        System.out.println("---------------------------------------------");
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));
    }
    public void testWrapper_gjdairoz002()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjdairoz002();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-12");
        flightSearchParam.setDep("PVG");
        flightSearchParam.setArr("NRT"); //SIN
        String html=qunarCrawler.getHtml(flightSearchParam);
        System.out.println(html);
        System.out.println("---------------------------------------------");
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));
    }
    public void testWrapper_gjsairoz001()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjsairoz001();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-08");
        flightSearchParam.setRetDate("2014-07-18");
        flightSearchParam.setDep("PVG");
        flightSearchParam.setArr("ICN"); //SIN
        String html=qunarCrawler.getHtml(flightSearchParam);
        System.out.println("---------------------------------------------");
        System.out.println(html);
        System.out.println("---------------------------------------------");
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));
    }
    public void testWrapper_11eab9de15d()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_11eab9de15d();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-04");
        flightSearchParam.setRetDate("2014-07-11");
        flightSearchParam.setDep("PEK");
        flightSearchParam.setArr("HKG"); //SIN
        String html=qunarCrawler.getHtml(flightSearchParam);
        /*System.out.println("---------------------------------------------");
        System.out.println(html);
        System.out.println("---------------------------------------------");*/
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));

        System.out.println("====================================");
        qunarCrawler.getBookingInfo(flightSearchParam);
    }

    public void testWrapper_gjd09082420()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjd09082420();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-08-30");
        flightSearchParam.setDep("CAN");
        flightSearchParam.setArr("SIN"); //SIN
        String html=qunarCrawler.getHtml(flightSearchParam);
        /*System.out.println("---------------------------------------------");
        System.out.println(html);
        System.out.println("---------------------------------------------");*/
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));

        System.out.println("====================================");
        qunarCrawler.getBookingInfo(flightSearchParam);
    }


}
