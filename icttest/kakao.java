package com.icttest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.http.client.methods.HttpGet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;


public class kakao {
	

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://{ip}:3306/{schema}?characterEncoding=utf8&amp;useUnicode=true";

	static final String USERNAME = {uid};
	static final String PASSWORD = {pwd};

	static final String clientId = "{clientid}";//애플리케이션 클라이언트 아이디값";
    	static final String clientSecret = "{secret}";//애플리케이션 클라이언트 시크릿값";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		
		try{
			Class.forName(JDBC_DRIVER);
			conn = (Connection) DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
			System.out.println("\n- MySQL Connection");
			stmt = (Statement) conn.createStatement();
			stmt2 = (Statement) conn.createStatement();
			
			String sql, sql2;
			sql = "SELECT PBLINFO_NO, PLACE_NM, NW_ADRES, ADRES FROM tf_pblinfo where PBLINFO_NO > 611";
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next()){
				String PBLINFO_NO = rs.getString("PBLINFO_NO");
				String PLACE_NM = rs.getString("PLACE_NM");
				String NW_ADRES = rs.getString("NW_ADRES");
				String ADRES = rs.getString("ADRES");
				String inADR = NW_ADRES;
				if ( "".equals(inADR) || inADR == null ) inADR = ADRES;
				
				String [] CRDNT = getKAKAO(inADR);  

				System.out.print("\n** PBLINFO_NO : " + PBLINFO_NO + ", " + " PLACE_NM: " + PLACE_NM + ", ADRES:" + ADRES + ", NW_ADRES:" + NW_ADRES + ",LAT:" + CRDNT[1] + "/LON:" + CRDNT[0] );
				
				sql2 = "UPDATE tf_pblinfo SET CRDNT_LA = '" + CRDNT[1].substring(0, 10) + "', CRDNT_LO = '" + CRDNT[0].substring(0, 11) + "' WHERE PBLINFO_NO = " + PBLINFO_NO;
				stmt2.execute(sql2);
				
				
				
			}
			rs.close();
			stmt.close();
			stmt2.close();
			conn.close();
		}catch(SQLException se1){
			se1.printStackTrace();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(stmt!=null) stmt.close();
				if(stmt2!=null) stmt2.close();
			}catch(SQLException se2){
			}
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		System.out.println("\n\n- MySQL Connection Close");		
		
		
		if (true) return;

    }
	
	private static String [] getKAKAO(String src) {
		
		String [] strResult = {"",""};
		String t = "{\"meta\":{\"is_end\":true,\"total_count\":1,\"pageable_count\":1},\"documents\":[{\"road_address\":{\"undergroun_yn\":\"N\",\"road_name\":\"공지로\",\"zone_no\":\"24244\",\"region_2depth_name\":\"춘천시\",\"sub_building_no\":\"\",\"region_3depth_name\":\"근화동\",\"main_building_no\":\"591\",\"address_name\":\"강원 춘천시 공지로 591\",\"y\":\"37.884669436626915\",\"x\":\"127.71700475606609\",\"region_1depth_name\":\"강원\",\"building_name\":\"춘천역\"},\"address_name\":\"강원 춘천시 공지로 591\",\"address\":{\"b_code\":\"4211011700\",\"region_3depth_h_name\":\"\",\"main_address_no\":\"190\",\"h_code\":\"\",\"region_2depth_name\":\"춘천시\",\"main_adderss_no\":\"190\",\"sub_address_no\":\"\",\"region_3depth_name\":\"근화동\",\"address_name\":\"강원 춘천시 근화동 190\",\"y\":\"37.88504101310166\",\"x\":\"127.71694015483496\",\"mountain_yn\":\"N\",\"zip_code\":\"200930\",\"region_1depth_name\":\"강원\",\"sub_adderss_no\":\"\"},\"y\":\"37.884669436626915\",\"x\":\"127.71700475606609\",\"address_type\":\"ROAD_ADDR\"}]}";
		
        try {
            String text = URLEncoder.encode(src, "UTF-8");
            String apiURL = "https://dapi.kakao.com/v2/local/search/address.json?query="+java.net.URLEncoder.encode(src, "UTF-8");
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "KakaoAK {kakao auth token}");

            // post request
            String postParams = "source=ko&target=en&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String encResponse = unicodeConvert(response.toString());
            System.out.println(encResponse); 
            
            //if (true) return new String[] {"",""}; 
            
            JsonObject json = (JsonObject)new JsonParser().parse(encResponse);
            JsonObject documents = json.getAsJsonArray("documents").get(0).getAsJsonObject();
            strResult[0] = documents.get("x").toString().replaceAll("\"", "").replaceAll("\'", "\''"); // Longitude
            strResult[1] = documents.get("y").toString().replaceAll("\"", "").replaceAll("\'", "\''"); // Latitude
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
		
		return strResult; 
		
	}
	
	public static String unicodeConvert(String str) {
        StringBuilder sb = new StringBuilder();
        char ch;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            if (ch == '\\' && str.charAt(i+1) == 'u') {
                sb.append((char) Integer.parseInt(str.substring(i+2, i+6), 16));
                i+=5;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }



}
