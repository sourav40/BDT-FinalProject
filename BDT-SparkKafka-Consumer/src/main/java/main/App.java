package main;

import java.util.ArrayList;
import java.util.List;

import sparkConfig.MySpark;
import sparkKafkaConsumer.MyKafkaConsumer;
import util.MyResources;

public class App {

	static String lastKey = "";
	static List<String> list = new ArrayList<String>();

	public static void main(String[] args) throws Exception {
		System.out.println("spark kafka is up and running...");
		MySpark spark = new MySpark();
		MyResources resources = new MyResources();
		MyKafkaConsumer consumer = new MyKafkaConsumer(resources);
		System.out.println("one here....");
		consumer.Wait(() -> {
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}, (String key, String val, boolean new_key) -> {
			System.out.println("inside the consumer function...");
			if (lastKey.isEmpty())
				lastKey = key;
			if (new_key) {
				try {
					if (!list.isEmpty())
						spark.Process(lastKey, list);
				} catch (Exception e) {
					e.printStackTrace();
				}

				lastKey = key;
				list.clear();
			}

			list.add(val);
		});
	}
}