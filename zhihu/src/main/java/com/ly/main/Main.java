package com.ly.main;

import com.ly.entity.ZhiHuUser;
import com.ly.utils.DBConnectionPool;
import com.ly.utils.QueueUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.*;

import static com.ly.utils.GetFromUrl.getFromUrl;

public class Main {

    //用户url阻塞队列
    static BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();//未爬过的网页url
    static ExecutorService executor = Executors.newFixedThreadPool(20);

    public static void main(String[] args) {
        String url = "https://www.zhihu.com/people/rednaxelafx";
        try {
            urlQueue.put(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // crawler(url);

        for (int i = 0; i <10; i++) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        System.out.println("当前活动线程数目" + ((ThreadPoolExecutor) executor).getActiveCount());
                        try {
                            crawler(urlQueue.take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("队列大小：" + urlQueue.size());
                    }
                }
            });
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (((ThreadPoolExecutor) executor).getActiveCount() < 10) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        crawler(urlQueue.take());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("队列大小：" + urlQueue.size());
                                }
                            }
                        });
                        if (urlQueue.size() == 0) {
                            System.out.println("队列为0了！！！！！！！！！！1");
                        }
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }

    private static void crawler(String url) {
        System.out.println("爬取" + url);
        // Document doc = JsoupUtils.getDocument(url);

        Document doc = Jsoup.parse(getFromUrl(url));
        ZhiHuUser user = new ZhiHuUser();
        //用户名
        user.setUserName(url.substring(url.lastIndexOf("/") + 1));
        //昵称
        user.setNickName(doc.select(".ProfileHeader-name").first().text());
        //个人简介
        user.setProfile(doc.select(".ProfileHeader-headline").first().text());
        //性别
        String sexStr = doc.select(".ProfileHeader-buttons button").first().text();
        if (sexStr.contains("他")) {
            user.setSex(1);
        } else if (sexStr.contains("她")) {
            user.setSex(2);
        } else {
            user.setSex(0);
        }

        Elements elements = doc.select(".ProfileHeader-infoItem");
        if (elements.size() > 0) {
            for (Element ele : elements) {
                //行业 公司 职位
                if (ele.select("svg").attr("class").contains("company")) {
                    String str = ele.text();
                    System.out.println(str);
                    String strs[] = str.split(" ");
                    if (strs.length > 0) {
                        user.setBusiness(strs[0]);
                    }
                    if (strs.length > 1) {
                        user.setCompany(strs[1]);
                    }
                    if (strs.length > 2) {
                        user.setPosition(strs[2]);
                    }
                }
                //学校 专业
                if (ele.select("svg").attr("class").contains("education")) {
                    String str = ele.text();
                    String strs[] = str.split(" ");
                    if (strs.length > 0) {
                        user.setEducation(strs[0]);
                    }
                    if (strs.length > 1) {
                        user.setMajor(strs[1]);
                    }
                }

            }
        }

        //回答数
        user.setAnswersNum(Integer.parseInt(doc.select(".ProfileMain-tabs").select("li").get(1).select(".Tabs-meta").text().replace(",", "")));

        //问题数
        user.setQuestionsNum(Integer.parseInt(doc.select(".ProfileMain-tabs").select("li").get(2).select(".Tabs-meta").text().replace(",", "")));

        //赞 感谢
        String str = doc.select(".Profile-sideColumn").text();
        if (str.contains("赞同")) {
            user.setStarsNum(Integer.parseInt(str.substring(str.indexOf("获得") + 3, str.indexOf("次赞同") - 1).replace(",", "")));
        }
        if (str.contains("感谢")) {
            user.setThxNum(Integer.parseInt(str.substring(str.lastIndexOf("获得") + 3, str.indexOf("次感谢") - 1).replace(",", "")));
        }
        //关注的人
        String followingNum = doc.select(".NumberBoard-itemValue").first().text();
        user.setFollowingNum(Integer.parseInt(followingNum.replace(",", "")));
        //关注者数量
        String followersNum = doc.select(".NumberBoard-itemValue").get(1).text();
        user.setFollowersNum(Integer.parseInt(followersNum.replace(",", "")));
        System.out.println("爬取用户成功:" + user);
        System.out.println("当前活动线程数目" + ((ThreadPoolExecutor) executor).getActiveCount());
        //插入数据库
        String sql = "INSERT IGNORE INTO zhihuuser(username,nickname,sex,profile,business,company,position,education,major," +
                "answersNum,questionsNum,starsNum,thxNum,followingNum,followersNum) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        Connection connection = null;
        try {
            connection = DBConnectionPool.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getNickName());
            ps.setInt(3, user.getSex());
            ps.setString(4, user.getProfile());
            ps.setString(5, user.getBusiness());
            ps.setString(6, user.getCompany());
            ps.setString(7, user.getPosition());
            ps.setString(8, user.getEducation());
            ps.setString(9, user.getMajor());
            ps.setInt(10, user.getAnswersNum());
            ps.setInt(11, user.getQuestionsNum());
            ps.setInt(12, user.getStarsNum());
            ps.setInt(13, user.getThxNum());
            ps.setInt(14, user.getFollowingNum());
            ps.setInt(15, user.getFollowersNum());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        //获得关注的人列表插入queue;
        QueueUtils.addUserFollowingUrl(urlQueue, url);
    }
}
