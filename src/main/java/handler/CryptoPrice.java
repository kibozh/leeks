package handler;

import bean.CoinBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.util.PropertiesComponent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class CryptoPrice {
    public static void main(String[] args) {
        try {
           getCoinData("bitcoin");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static CoinBean getCoinData(String coinId) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId;
        URL obj = new URL(url);
        HttpURLConnection con;

        String proxyStr = PropertiesComponent.getInstance().getValue("key_proxy");
        if (StringUtils.isNotBlank(proxyStr)) {
            String[] proxyHP = proxyStr.split(":");
            Proxy.Type type;
            String proxyHost;
            if (proxyHP[0].startsWith("http")) {
                type = Proxy.Type.HTTP;
                proxyHost = proxyHP[1].substring("//".length());
                int proxyPort = Integer.parseInt(proxyHP[2]);
                Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
                con = (HttpURLConnection) obj.openConnection(proxy);
            } else if (proxyHP[0].startsWith("socks")) {
                type = Proxy.Type.SOCKS;
                proxyHost = proxyHP[1].substring("//".length());
                int proxyPort = Integer.parseInt(proxyHP[2]);
                Proxy proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
                con = (HttpURLConnection) obj.openConnection(proxy);
            } else {
//                type = Proxy.Type.DIRECT;
                con = (HttpURLConnection) obj.openConnection();
            }
        } else {
            con = (HttpURLConnection) obj.openConnection();
        }
        // 设置请求头
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // 解析JSON响应
        JSONObject jsonObject = JSON.parseObject(response.toString());
        JSONObject marketData = jsonObject.getJSONObject("market_data");
        BigDecimal highPrice = new BigDecimal(marketData.getJSONObject("high_24h").getString("usd"));
        BigDecimal lowPrice = new BigDecimal(marketData.getJSONObject("low_24h").getString("usd"));
        BigDecimal currentPrice = new BigDecimal(marketData.getJSONObject("current_price").getString("usd"));
        BigDecimal priceChange = marketData.getBigDecimal("price_change_24h");
        BigDecimal priceChangePercentage = marketData.getBigDecimal("price_change_percentage_24h");

        System.out.println("Bitcoin Highest Price (24h): $" + highPrice);
        System.out.println("Bitcoin Lowest Price (24h): $" + lowPrice);
        System.out.println("Bitcoin Current Price: $" + currentPrice);
        System.out.println("Bitcoin Price Change (24h): $" + priceChange);
        System.out.println("Bitcoin Price Change Percentage (24h): " + priceChangePercentage + "%");

        CoinBean coinBean = new CoinBean(coinId);
        coinBean.setRegularMarketDayHigh(highPrice.doubleValue());
        coinBean.setRegularMarketDayLow(lowPrice.doubleValue());
        coinBean.setRegularMarketPrice(currentPrice.doubleValue());
        coinBean.setRegularMarketChange(priceChange.doubleValue());
        coinBean.setRegularMarketChangePercent(priceChangePercentage.doubleValue());
        return coinBean;
    }
}