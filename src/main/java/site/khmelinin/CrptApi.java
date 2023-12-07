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

public class CrptApi {

    public CrptApi() {
        // не забыть въебать ограничение запросов
    }


    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi();
        CrptApi.Document document = new CrptApi.Document();
        crptApi.createDocument(document);

    }

    public void createDocument(Document document) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(document);
            System.out.println(jsonDocument);
            String apiUrl = "http://localhost:8081/receive";
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(apiUrl);
                StringEntity requestEntity = new StringEntity(jsonDocument);
                httpPost.setEntity(requestEntity);

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("Status code: " + statusCode);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
            public List<Product> addList() {
                List<Product> products = new ArrayList<>();
                products.add(this);
                return products;
            }

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
        }
    }
}
