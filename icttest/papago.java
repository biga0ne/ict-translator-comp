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

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;


public class papago {
	

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
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
			sql = "SELECT PBLINFO_NO, PLACE_NM FROM tf_pblinfo";
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next()){
				String PBLINFO_NO = rs.getString("PBLINFO_NO");
				String PLACE_NM = rs.getString("PLACE_NM");
				String PLACE_NM_EN = getPapago(PLACE_NM);  

				System.out.print("\n** PBLINFO_NO : " + PBLINFO_NO + ", " + " PLACE_NM: " + PLACE_NM + "=" + PLACE_NM_EN);
				
				sql2 = "INSERT INTO tf_pblinfo_lang (PBLINFO_NO, LANG_CODE_ID, PLACE_NM) values(" + PBLINFO_NO + ", 'en', '" + PLACE_NM_EN + "')";
				stmt2.execute(sql2);
				
				//break;
				
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
	
	private static String getPapago(String src) {
		
		String strResult = "";
		String t = "{\"message\":{\"@type\":\"response\",\"@service\":\"naverservice.labs.api\",\"@version\":\"1.0.0\",\"result\":{\"translatedText\":\"The chicken ribs too.\",\"srcLangType\":\"ko\"}}}";
        
        try {
            String text = URLEncoder.encode(src, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/language/translate";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
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
            
            //System.out.println(response.toString());
            
            JsonObject json = (JsonObject)new JsonParser().parse(response.toString());
            JsonObject message = json.getAsJsonObject("message");
            JsonObject result = message.getAsJsonObject("result");
            
            strResult = result.get("translatedText").toString().replaceAll("\"", "").replaceAll("\'", "\''");
            
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
		
		return strResult; 
		
  }

}
