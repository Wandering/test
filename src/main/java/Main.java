import com.alibaba.fastjson.JSON;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicHttpRequest;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        CloseableHttpClient closeableHttpClient = HttpClientBuilder
                .create().
                        setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
                .build();
        HttpClientContext clientContext = HttpClientContext.create();
        CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie2 cookie2 ;
        cookie2 = new BasicClientCookie2("JSESSIONID-L","d0f1f4ec-3df3-44cf-95f5-2583dd5adf74");
        cookieStore.addCookie(cookie2);
        cookie2 = new BasicClientCookie2("tmri_csfr_token","7A661BE07F1379FFFE8BF72AD6E8B3B7");
        cookieStore.addCookie(cookie2);
        HttpGet httpGet = new HttpGet("http://ln.122.gov.cn/captcha?nocache="+System.currentTimeMillis());

        HttpHost httpHost = new HttpHost("ln.122.gov.cn",80);
        HttpPost post = new HttpPost();


        post.setHeader("Access-Control-Allow-Origin","*");
        post.setHeader("Accept","application/json, text/javascript, */*; q=0.01");
        post.setHeader("Accept-Encoding","gzip, deflate");
        post.setHeader("Accept-Language","zh-CN,zh;q=0.9");
        post.setHeader("Origin","http://ln.122.gov.cn");
        post.setHeader("Proxy-Connection","keep-alive");
        post.setHeader("Host","ln.122.gov.cn");
        post.setHeader("Referer","http://ln.122.gov.cn/views/inquiry.html?q=j");
        post.setHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        try {
            CloseableHttpResponse response;
            response = closeableHttpClient.execute(httpGet);
            Tesseract tessreact = new Tesseract();
            tessreact.setDatapath("temp");
                String path = "F:"+ "\\" + "File" +  Math.random() + ".png";
                File file = new File(path);
                Files.asByteSink(file, FileWriteMode.APPEND).writeFrom(response.getEntity().getContent());
                String code = tessreact.doOCR(file);
                System.out.println(code);
            post.setURI(new URI("/m/publicquery/vio?hpzl=02&hphm1b=AG98T3&hphm=辽AG98T3&fdjh=279903&captcha="+code+"&qm=wf&page=1"));
                response = closeableHttpClient.execute(httpHost,post,clientContext);
                System.out.println(JSON.parseObject(response.getEntity().getContent(),String.class).toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
