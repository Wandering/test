import com.alibaba.fastjson.JSON;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String simple_host = "ln.122.gov.cn";
    private static final String host = "http://" + simple_host;

    public static void main(String[] args) throws URISyntaxException {
        String hphm1b = "AG98T3";
        String type = "辽";
        Integer fdjh = 279903;
        Map<String, Object> rtnMap = post(hphm1b, type, fdjh);
        System.out.println(JSON.toJSONString(rtnMap));
    }

    private static Map<String, Object> post(String hphm1b, String type, Integer fdjh) throws URISyntaxException {
        Map<String, Object> rtnMap = null;
        String hphm = type + hphm1b;

        CloseableHttpClient closeableHttpClient = HttpClientBuilder
                .create()
                .build();
        HttpClientContext clientContext = HttpClientContext.create();
        HttpGet httpGet = new HttpGet(host + "/captcha?nocache=" + System.currentTimeMillis());

        HttpHost httpHost = new HttpHost(simple_host, 80);
        HttpPost post = new HttpPost();
        post.setHeader(HttpHeaders.REFERER, host + "/views/inquiry.html?q=j");
        post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        try {
            CloseableHttpResponse response;
            String code;

            do {

                code = getOcrCode(closeableHttpClient, httpGet);
                URI uri = new URI(String.format("/m/publicquery/vio?hpzl=02&hphm1b=%s&hphm=%s&fdjh=%s&captcha=%s&qm=wf&page=1", hphm1b, hphm, fdjh, code));
                post.setURI(uri);
                response = closeableHttpClient.execute(httpHost, post, clientContext);

                rtnMap = JSON.parseObject(response.getEntity().getContent(), Map.class);
            } while (rtnMap != null && (Integer) rtnMap.get("code") == 499);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return rtnMap;
    }

    private static String getOcrCode(CloseableHttpClient closeableHttpClient, HttpGet httpGet) throws IOException, TesseractException {
        CloseableHttpResponse response;
        String code;
        do {
            response = closeableHttpClient.execute(httpGet);
            BufferedImage img = ImageIO.read(response.getEntity().getContent());
            Tesseract tesseract = new Tesseract();
            //path ./tessdata/eng.traineddata
            tesseract.setLanguage("eng");
            code = tesseract.doOCR(img);

            if (code.lastIndexOf("\n") != -1) {
                code = code.trim().replace("\n", "");
            }

        } while (code.length() != 4 || isSpecialChar(code));
        return code;
    }

    /**
     * 判断是否含有特殊字符
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        boolean flag = false;
        System.out.println("字串："+str);
        int[] charInts = str.chars().toArray();
        for (int c : charInts) {
            System.out.println(c);

            if (!(((c > 47 && c < 58) || // 0-9
                    (c > 64 && c < 91) ||// A-Z
                    (c > 96 && c < 123)))) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
