# 📊 Analytics Service

The **Analytics Service** is an event-driven microservice designed to process and analyze data without impacting the performance of the core operational services. In our architecture, it demonstrates the implementation of **Apache Kafka** for asynchronous messaging and the **Observer Pattern**.

## 🏗️ Architecture & Event-Driven Design

While the Billing Service uses gRPC for synchronous operations, the Analytics Service uses **Kafka** for asynchronous, "Fire-and-Forget" operations.

### Why Event-Driven / Kafka?
- **Decoupling**: The Patient Service does not know or care if the Analytics Service exists. It simply broadcasts an event saying "A patient was created." The Analytics Service subscribes to these events.
- **Resilience**: If the Analytics Service goes down for maintenance or crashes, the Patient Service continues operating normally. When the Analytics Service comes back online, it will pick up exactly where it left off, reading the missed messages from the Kafka topic.
- **Scalability**: Kafka allows for massive throughput. You can spin up multiple instances of the Analytics Service in the same "Consumer Group," and Kafka will distribute the workload among them automatically.

## 📂 Core Components Deep Dive

### Protobuf Event Schema (`patient_event.proto`)
Just like gRPC, we use Protocol Buffers to serialize our Kafka messages.
- The schema defines `PatientEvent` with fields like `patient_id`, `name`, and `email`.
- Using Protobuf over JSON in Kafka ensures strong schema validation and smaller message sizes, which is critical for high-throughput event streaming.

### Kafka Consumer (`KafkaConsumer.java`)
- Uses Spring Kafka's `@KafkaListener` annotation.
- Subscribes to the `"patient"` topic and binds to the `"analytics-service"` consumer group.
- **Payload Handling**: The consumer receives a raw `byte[]` from Kafka. It then uses the generated Protobuf class (`PatientEvent.parseFrom(event)`) to securely deserialize the binary data back into a strongly-typed Java object.
- **Error Handling**: Wraps the deserialization in a try-catch for `InvalidProtocolBufferException`. In a robust production environment, un-parseable messages would be sent to a "Dead Letter Queue" (DLQ) for manual inspection rather than crashing the consumer.

## 🔄 Interaction Flow
1. The **Patient Service** successfully commits a new patient to its database.
2. The Patient Service serializes a `PatientEvent` into binary using Protobuf and publishes it to the Kafka broker on the `patient` topic.
3. The Kafka Broker stores the message persistently on disk.
4. The **Analytics Service** (listening on the `patient` topic) detects a new message.
5. The Analytics Service pulls the byte array, deserializes it, and processes it (e.g., updating a real-time dashboard or feeding a data warehouse).

## 🚀 Running the Service

- **Kafka Broker Required**: To run this service locally, you must have a local instance of Zookeeper and Kafka running (typically via Docker Compose).
- The service itself starts on a dynamic or configured port and constantly polls the Kafka broker in the background.

## 📚 Educational Takeaways
1. **Eventually Consistent**: Unlike gRPC where data is consistent immediately, event-driven systems are "eventually consistent." There might be a slight delay between a patient being created and the analytics dashboard updating.
2. **Consumer Groups**: By assigning a `groupId`, Kafka tracks the "offset" (which messages have been read). This prevents a message from being processed twice by the same logical application, even if scaled horizontally.
