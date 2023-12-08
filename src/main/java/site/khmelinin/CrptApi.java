package site.khmelinin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApi {

    private static Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        long requestInterval = timeUnit.toMillis(1) / requestLimit;
        semaphore = new Semaphore(requestLimit);

        Thread timerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(requestInterval);
                    semaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    public void createDocument(Document document, String signature) {
        try {
            if (semaphore.tryAcquire(1, 1, TimeUnit.SECONDS)) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonDocument = objectMapper.writeValueAsString(document);

                    String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";

                    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                        HttpPost httpPost = new HttpPost(apiUrl);
                        httpPost.setEntity(new StringEntity(jsonDocument));

                        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                            System.out.println("Status code: " + response.getStatusLine().getStatusCode());
                        } catch (IOException e) {
                            System.err.println("Error executing HTTP request: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        System.err.println("Error creating or closing HttpClient: " + e.getMessage());
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Error processing JSON: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while acquiring semaphore: " + e.getMessage());
        }
    }

    @Data
    private static class Document {
        @JsonProperty("description")
        private Description description = new Description();
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        @JsonProperty("importRequest")
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("production_type")
        private String productionType;
        @JsonProperty("products")
        private List<Product> products = new Product().addList();
        @JsonProperty("reg_date")
        private String regDate;
        @JsonProperty("reg_number")
        private String regNumber;

        @Data
        public static class Description {
            @JsonProperty("participantInn")
            private String participantInn;
        }

        @Data
        public static class Product {
            @JsonProperty("certificate_document")
            private String certificateDocument;
            @JsonProperty("certificate_document_date")
            private String certificateDocumentDate;
            @JsonProperty("certificate_document_number")
            private String certificateDocumentNumber;
            @JsonProperty("owner_inn")
            private String ownerInn;
            @JsonProperty("producer_inn")
            private String producerInn;
            @JsonProperty("production_date")
            private String productionDate;
            @JsonProperty("tnved_code")
            private String tnvedCode;
            @JsonProperty("uit_code")
            private String uitCode;
            @JsonProperty("uitu_code")
            private String uituCode;

            public List<Product> addList() {
                List<Product> products = new ArrayList<>();
                products.add(this);
                return products;
            }
        }
    }
}