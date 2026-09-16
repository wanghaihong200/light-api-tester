package com.wang.light.example;

import com.wang.light.api.client.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * multipart 文件上传演示。
 */
@SpringBootTest(classes = DemoApp.class)
public class MultipartUploadTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApiClient apiClient;

    @Test
    public void uploadFile_returnsOk() throws IOException {
        Path tmp = Files.createTempFile("light-demo", ".txt");
        Files.write(tmp, "hello light-api".getBytes("UTF-8"));
        try {
            apiClient.post("/api/upload").field("file", tmp.toFile()).execute().assertThat()
                    .statusCode(200)
                    .jsonPath("$.msg", is("ok"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
