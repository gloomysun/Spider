package com.ly.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.BlockingQueue;

import static com.ly.utils.GetFromUrl.getFromUrl;

public class QueueUtils {

    //传入用户url，获取他所关注人的url
    public static void addUserFollowingUrl(BlockingQueue urlQueue,String userUrl){
        int i = 1;
        String userFollowingUrl = "";
        userFollowingUrl = userUrl+"/following?page=" + i;
        Element userFollowingContent = null;

        userFollowingContent = Jsoup.parse(getFromUrl(userFollowingUrl));
        Elements followingElements = userFollowingContent.select(".List-item");
        //判断当前页关注人数是否为0，是的话就跳出循环
        if (followingElements.size() != 0) {
            for (Element e : followingElements) {
                String newUserUrl = e.select("a[href]").get(0).attr("href");
                //把获取到的地址加入阻塞队列
                try {
                    if (!newUserUrl.contains("org")) {
                        urlQueue.put("https:" + newUserUrl);
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
