package org.cloudfoundry.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.cloudfoundry.example.Controller.FORWARDED_URL;
import static org.cloudfoundry.example.Controller.PROXY_METADATA;
import static org.cloudfoundry.example.Controller.PROXY_SIGNATURE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ControllerTest {

    private static final String BODY_VALUE = "test-body";

    private static final String PROXY_METADATA_VALUE = "test-proxy-metadata";

    private static final String PROXY_SIGNATURE_VALUE = "test-proxy-signature";

    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;

    @Test
    public void deleteRequest() throws Exception {
        this.mockServer
            .expect(method(DELETE))
            .andExpect(requestTo("http://localhost/original/delete"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andRespond(withSuccess());

        this.mockMvc
            .perform(delete("http://localhost/route-service/delete")
                .header(FORWARDED_URL, "http://localhost/original/delete")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(status().isOk());

        this.mockServer.verify();
    }

    @Test
    public void getRequest() throws Exception {
        this.mockServer
            .expect(method(GET))
            .andExpect(requestTo("http://localhost/original/get"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andRespond(withSuccess(BODY_VALUE, TEXT_PLAIN));

        this.mockMvc
            .perform(get("http://localhost/route-service/get")
                .header(FORWARDED_URL, "http://localhost/original/get")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TEXT_PLAIN))
            .andExpect(content().string(BODY_VALUE));

        this.mockServer.verify();
    }

    @Test
    public void headRequest() throws Exception {
        this.mockServer
            .expect(method(HEAD))
            .andExpect(requestTo("http://localhost/original/head"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andRespond(withSuccess());

        this.mockMvc
            .perform(head("http://localhost/route-service/head")
                .header(FORWARDED_URL, "http://localhost/original/head")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(status().isOk());

        this.mockServer.verify();
    }

    @Test
    public void incompleteRequest() throws Exception {
        this.mockMvc
            .perform(get("http://localhost/route-service/incomplete"))
            .andExpect(status().isNotFound());

        this.mockServer.verify();
    }

    @Test
    public void patchRequest() throws Exception {
        this.mockServer
            .expect(method(PATCH))
            .andExpect(requestTo("http://localhost/original/patch"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(MockRestRequestMatchers.content().contentType(TEXT_PLAIN))
            .andExpect(MockRestRequestMatchers.content().string(BODY_VALUE))
            .andRespond(withSuccess(BODY_VALUE, TEXT_PLAIN));

        this.mockMvc
            .perform(patch("http://localhost/route-service/patch")
                .header(FORWARDED_URL, "http://localhost/original/patch")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE)
                .contentType(TEXT_PLAIN)
                .content(BODY_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TEXT_PLAIN))
            .andExpect(content().string(BODY_VALUE));

        this.mockServer.verify();
    }

    @Test
    public void postRequest() throws Exception {
        this.mockServer
            .expect(method(POST))
            .andExpect(requestTo("http://localhost/original/post"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(MockRestRequestMatchers.content().contentType(TEXT_PLAIN))
            .andExpect(MockRestRequestMatchers.content().string(BODY_VALUE))
            .andRespond(withSuccess(BODY_VALUE, TEXT_PLAIN));

        this.mockMvc
            .perform(post("http://localhost/route-service/post")
                .header(FORWARDED_URL, "http://localhost/original/post")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE)
                .contentType(TEXT_PLAIN)
                .content(BODY_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TEXT_PLAIN))
            .andExpect(content().string(BODY_VALUE));

        this.mockServer.verify();
    }

    @Test
    public void putRequest() throws Exception {
        this.mockServer
            .expect(method(PUT))
            .andExpect(requestTo("http://localhost/original/put"))
            .andExpect(header(PROXY_METADATA, PROXY_METADATA_VALUE))
            .andExpect(header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE))
            .andExpect(MockRestRequestMatchers.content().contentType(TEXT_PLAIN))
            .andExpect(MockRestRequestMatchers.content().string(BODY_VALUE))
            .andRespond(withSuccess(BODY_VALUE, TEXT_PLAIN));

        this.mockMvc
            .perform(put("http://localhost/route-service/put")
                .header(FORWARDED_URL, "http://localhost/original/put")
                .header(PROXY_METADATA, PROXY_METADATA_VALUE)
                .header(PROXY_SIGNATURE, PROXY_SIGNATURE_VALUE)
                .contentType(TEXT_PLAIN)
                .content(BODY_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TEXT_PLAIN))
            .andExpect(content().string(BODY_VALUE));

        this.mockServer.verify();
    }

    @Autowired
    void setRestTemplate(RestTemplate restTemplate) {
        this.mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Autowired
    void setWebApplicationContext(WebApplicationContext wac) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

}
