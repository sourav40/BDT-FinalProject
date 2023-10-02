package kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaPublisher {

	private final static String BOOTSTRAP_SERVER = "localhost:9092";
	private final static String TOPIC_NAME = "kafka-tweet";

	public static Producer<String, String> getProducer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", BOOTSTRAP_SERVER);
		props.put("key.serializer", StringSerializer.class.getName());
		props.put("value.serializer", StringSerializer.class.getName());
		return new KafkaProducer<String, String>(props);
	}

	public static void publishTweetsToConsumer(String id, String tweet)
			throws Exception {
		final Producer<String, String> producer = getProducer();
		final ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(
				TOPIC_NAME, id, tweet);
		System.out.println("pushing messages to kafka topic..."+tweet);
		producer.send(producerRecord).get();
	}

}
