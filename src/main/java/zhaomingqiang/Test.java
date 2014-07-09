package zhaomingqiang;

import com.alibaba.fastjson.JSON;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: mingqiang.zhao
 * Date: 14-7-1
 * Time: 上午9:36
 * To change this template use File | Settings | File Templates.
 */
public class Test extends TestCase {

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
        QunarCrawler qunarCrawler=new Wrapper_gjsairka001();
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
    public void testWrapper_gjd09082510()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjdairka001();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-04");
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
        QunarCrawler qunarCrawler=new Wrapper_gjdaircx001();
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

    public void testWrapper_gjdairhu001()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjdairhu001();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-17");
        flightSearchParam.setDep("PEK");
        //flightSearchParam.setArr("BER");
        flightSearchParam.setArr("KWE");
        String html=qunarCrawler.getHtml(flightSearchParam);
        System.out.println("---------------------------------------------");
        System.out.println(html);
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));
    }

    public void testWrapper_gjsairhu001()
    {
        long s=System.currentTimeMillis();

        //String url = "dep=PUS&arr=NRT&date=2014-05-14&from=Qunar";
        QunarCrawler qunarCrawler=new Wrapper_gjsairhu001();
        FlightSearchParam flightSearchParam=new FlightSearchParam();
        flightSearchParam.setDepDate("2014-07-17");
        flightSearchParam.setRetDate("2014-07-20");
        flightSearchParam.setDep("PEK");
        //flightSearchParam.setArr("BER");
        flightSearchParam.setArr("KWE");
        String html=qunarCrawler.getHtml(flightSearchParam);
        System.out.println("---------------------------------------------");
        System.out.println(html);
        ProcessResultInfo info= qunarCrawler.process(html, flightSearchParam);
        System.out.println(System.currentTimeMillis()-s);
        System.out.println(JSON.toJSONString(info));
    }


}
