package com.splider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CheckedOutputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.tools.zip.ZipOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.Statement;

public class ParserHtml {

	public static void main(String[] args) {
		
		//step1--抓取数据存入数据库
//		String encoding = System.getProperty("file.encoding");//utf8
//		System.out.println("===encoding=="+encoding);
//		long starTime = System.currentTimeMillis();
//
//		ArrayList<String> bookUrls = new ArrayList<String>();
//		//分别抓取：互联网、编程、算法
//		bookUrls = downloadBookUrl("编程");
//		//根据书籍的url获取信息
//		getBookInfo(bookUrls);
//		//获取评分排名前40的数据并保存至xls
//        getTop(40); 
//		long endTime = System.currentTimeMillis();
//		long Time = endTime - starTime;
//		System.out.println("执行耗时 : " + Time + " 毫秒 ");
//		System.out.println("执行耗时 : " + Time / 1000f + " 秒 ");
		
		//step2--根据评分取出前40并保存至xls文件中
		getTop(40);
	}
   /**
    * top40
    */
	private static void getTop(int i) {
//		String sql = "insert into books values (DEFAULT,'" + title + "', '" + score + "', '"
//		+ rating_sum + "', '" + author + "', '" + press + "', '" + date + "', '" + price + "')";
		System.out.println("=======start========");
		String sql = "select *  from books  order by score desc limit 40";
		SaveBookInfo saveBookInfo  =  new SaveBookInfo();
		Statement smt = saveBookInfo.saveBookInfo();
		
		List<BookVO> list = new ArrayList<BookVO>();
		ExcelOpt excel  =new ExcelOpt();
        try {
			ResultSet rs = smt.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等   
	        int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
			while(rs.next()){
				BookVO book = new BookVO();
				String increment = rs.getString("increment");
				String title = rs.getString("title");
			    String score = rs.getString("score");
			    String rating_num = rs.getString("rating_sum");
			    String author = rs.getString("author");
			    String press = rs.getString("press");
			    String date = rs.getString("date");
			    String price = rs.getString("price");	
			    book.setIncrement(increment);
			    book.setTitle(title);
			    book.setScore(score);
			    book.setRating_sum(rating_num);
			    book.setAuthor(author);
			    book.setPress(press);
			    book.setDate(date);
			    book.setPrice(price);
			    list.add(book);
			}
			excel.writeExcelBo("f:\\testWrite.xls",list);  
			System.out.println("=====END======");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				saveBookInfo.conn.close();
				smt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        
	}

	/**
	 * 抓取每本书的info
	 * 
	 * @param ArrayList<String>
	 */
	public static void getBookInfo(ArrayList<String> bookUrls) {
		SaveBookInfo saveBookInfo = new SaveBookInfo();
		Map<String, String> cookies = new HashMap<String, String>();
		//book.douban.com
		cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");
		cookies.put("__utmb", "81379588.1.10.1478780180");
		cookies.put("__utmc", "81379588");
		cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
		cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
		cookies.put("_pk_ses.100001.3ac3", "*");
		//douban.com
		cookies.put("bid", "MvEsSVNL_Nc");
		//read.douban.com
		cookies.put("_ga", "GA1.3.117318709.1478747468");
		cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
		cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
				+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
				+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
		//www.douban.com
		cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
		cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
				+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
				+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");

		int count = 0;
		for (String url : bookUrls) {
			try {
				//connect(String url) 方法创建一个新的 Connection, 和 get() 取得和解析一个HTML文件。如果从该URL获取HTML时发生错误，便会抛出 IOException，应适当处理
				Document doc = Jsoup.connect(url)
						.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
						.timeout(3000).get();//获取doom数据
				Elements titleElement = doc.getElementsByClass("subject clearfix").select("a");//书名
				Elements scoreElement = doc.select("strong");//评分
				Elements ratingSum = doc.getElementsByClass("rating_sum").select("a").select("span");//评价人数
				Elements authorElement = doc.getElementById("info").select("span").first().select("a");//作者
				Element pressElement = doc.getElementById("info");//出版社

				// 书名
				String title = titleElement.attr("title");
				// 评分
				String score = scoreElement.html();
				// 评价人数
				String rating_sum = ratingSum.html();
				// 作者
				String author = authorElement.html();
				// 出版社
				String press = pressElement.text();
				if (press.indexOf("出版社:") > -1) {
					press = pressElement.text().split("出版社:")[1].split(" ")[1];
				} else {
					press = "";
				}
				// 出版日期
				String date = pressElement.text();
				if (date.indexOf("出版年:") > -1) {
					date = pressElement.text().split("出版年:")[1].split(" ")[1];
				} else {
					date = "";
				}
				// 价格
				String price = pressElement.text();
				if (price.indexOf("定价:") > -1) {
					price = pressElement.text().split("定价:")[1].split(" ")[1];
					if (price.equals("CNY")) {
						price = pressElement.text().split("定价:")[1].split(" ")[2];
					}
				} else {
					price = "";
				}

				System.out.println(title);
				//方式一
				// 评价人数大于1000插入数据到数据库
				if (!rating_sum.equals("") && Integer.parseInt(rating_sum) >= 1000) {
					String sql = "insert into books values (DEFAULT,'" + title + "', '" + score + "', '"
							+ rating_sum + "', '" + author + "', '" + press + "', '" + date + "', '" + price + "')";
					Statement smt = saveBookInfo.saveBookInfo();
					smt.execute(sql);
					System.out.println(++count);
				}
				//数组排序
				
				// 睡眠防止ip被封
				try {
					System.out.println("睡眠1秒");
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存书的url
	 * 
	 * @param keyWord
	 * @return
	 */
	public static ArrayList<String> downloadBookUrl(String keyWord) {
		ArrayList<String> bookUrls = new ArrayList<String>();
		int index = 0;
		try {
			Map<String, String> cookies = new HashMap<String, String>();
			//book.douban.com
			cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");//是来识别网站独立访客的访客ID
			cookies.put("__utmb", "81379588.1.10.1478780180");//utmb和utmc都是记录visit的cookie   30分钟是一个分界点
			cookies.put("__utmc", "81379588");
			cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");//用来记录网站访问者的来源
			cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
			cookies.put("_pk_ses.100001.3ac3", "*");
			//douban.com
			cookies.put("bid", "MvEsSVNL_Nc");
			//read.douban.com
			cookies.put("_ga", "GA1.3.117318709.1478747468");
			cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
			cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
					+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
					+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
			//www.douban.com
			cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
			cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
					+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
					+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");
			
			while (true) {
				// 获取cookies

				Document doc = Jsoup.connect("https://book.douban.com/tag/" + keyWord + "?start=" + index + "&type=T")
						.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
						.timeout(3000).get();
				Elements newsHeadlines = doc.select("ul").select("h2").select("a");
				System.out.println("本页：  " + newsHeadlines.size());
				for (Element e : newsHeadlines) {
					System.out.println(e.attr("href"));
					bookUrls.add(e.attr("href"));
				}
				index += newsHeadlines.size();
				System.out.println("共抓取url个数：" + index);
				if (newsHeadlines.size() == 0) {
					System.out.println("end");
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookUrls;
	}
  
}
