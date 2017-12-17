package com.trade.bigone.web.exchange.detail;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;

import com.trade.bigone.dao.mapper.ActionLogMapper;
import com.trade.bigone.dao.model.ActionLog;
import com.trade.bigone.dao.model.ActionLogExample;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.asynchttpclient.Dsl.*;
import org.asynchttpclient.*;


@RestController
public class HelloController {

    private static final AsyncHttpClient c = asyncHttpClient(config());
    private static final AsyncHttpClient asyncHttpClient = asyncHttpClient();

    @Autowired
    private ActionLogMapper actionLogMapper;


    @RequestMapping("/")
    public String index() throws Exception {
        ActionLogExample example = new ActionLogExample();
        example.createCriteria().andIdEqualTo(1L);
        List<ActionLog> actionLogList = actionLogMapper.selectByExample(example);
        System.out.println(actionLogList);

        List<String> result = new ArrayList<>(10000);
        while (true) {
            ZbTicker usdtTicker = getHttpResponse("eos", "usdt");
            ZbTicker qcTicker = getHttpResponse("eos", "qc");

            String usdt = "" + getDoubleValue(usdtTicker.getTicker().getLast(), 6.7d) + " " +
                getDoubleValue(usdtTicker.getTicker().getBuy(), 6.7d) + " "
                + getDoubleValue(usdtTicker.getTicker().getSell(),  6.7d);
            String qc = "" + getDoubleValue(qcTicker.getTicker().getLast(), 1d)+ " " +
                getDoubleValue(qcTicker.getTicker().getSell(), 1d) + " "
                + getDoubleValue(qcTicker.getTicker().getBuy(), 1d);

            Thread.sleep(1000);
            result.add(usdt + " " + qc);
            System.err.println(usdt);
            System.err.println(qc);
            System.err.println();
        }
    }

    public String getWebSocketResponse() throws ExecutionException, InterruptedException {
        WebSocket websocket = c.prepareGet("wss://api.zb.com:9999/websocket")
            .execute(new Builder().addWebSocketListener(
                new WebSocketListener() {

                    @Override
                    public void onOpen(WebSocket websocket) {
                        System.out.println("open");
                        websocket.sendMessage("{'event':'addChannel','channel':'ltcbtc_ticker'}");
                    }

                    @Override
                    public void onClose(WebSocket websocket) {
                        System.out.println("close");
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("error");
                    }
                }).build()).get();
        return websocket.toString();
    }



    public ZbTicker getHttpResponse(String basic, String middle) throws Exception {
        String market = basic+"_"+middle;
        Future<Response> whenResponse = asyncHttpClient.prepareGet("http://api.zb.com/data/v1/ticker?market=" + market).execute();
        String json =  whenResponse.get().getResponseBody();
        try{
            ZbTicker zbTicker = JSON.parseObject(json, ZbTicker.class);
            return zbTicker;
        }catch (Exception e) {
            System.out.println("parseError:" + json);
        }
        return new ZbTicker();
    }

    public static String getDoubleValue(String value, double x) {
        double result = Double.valueOf(value) * x;
        BigDecimal b = new BigDecimal(result).setScale(4, RoundingMode.HALF_EVEN);
        return String.format("%.4f", b.doubleValue());
    }

    public static void main(String[] args) {
        String y = getDoubleValue("12.554", 1d);
        System.out.println(y);
    }

    public static class ZbTicker {
        private ZbTickerDetail ticker;
        private Date date;

        public ZbTicker(ZbTickerDetail ticker, Date date) {
            this.ticker = ticker;
            this.date = date;
        }

        public ZbTicker() {
        }

        public ZbTickerDetail getTicker() {
            return ticker;
        }

        public void setTicker(ZbTickerDetail ticker) {
            this.ticker = ticker;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    public static class ZbTickerDetail {

        /**
         * 24小时成交量
         */
        private String vol;

        /**
         * 最近成交价
         */
        private String last;

        /**
         * 卖一价
         */
        private String sell;

        /**
         * 买一价
         */
        private String buy;

        /**
         * 最高价
         */
        private String high;

        /**
         * 最低价
         */
        private String low;

        public String getVol() {
            return vol;
        }

        public void setVol(String vol) {
            this.vol = vol;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getSell() {
            return sell;
        }

        public void setSell(String sell) {
            this.sell = sell;
        }

        public String getBuy() {
            return buy;
        }

        public void setBuy(String buy) {
            this.buy = buy;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }
    }
}
