package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.*;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import kafka.KafkaPublisher;

public class TweetPuller {

	public static void main(String[] args) throws Exception {
		System.out.println("trying to send message...");

		try {
			String apiURL = "https://raw.githubusercontent.com/sourav40/Tweets/main/Tweets.json";
			URL url = new URL(apiURL);
 
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();

				String line;

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}

				reader.close();

				JSONObject jsonResponse = new JSONObject(response.toString());
				JSONArray dataArray = jsonResponse.getJSONArray("data");

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject dataObject = dataArray.getJSONObject(i);
					String id = dataObject.getString("id");
					String text = dataObject.getString("text");

					// Twitter twitter = new Twitter(id, text);

					KafkaPublisher.publishTweetsToConsumer(id, text);
				}

			} else {
				System.out.println("HTTP GET failed with response code: "
						+ responseCode);
			}

			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
