package sparkj8;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static spark.SparkJ8.post;

/**
 * System tests for the Cookies support.
 * @author dreambrother
 */
public class CookiesIntegrationTest {

    private static final String DEFAULT_HOST_URL = "http://localhost:4567";
    private HttpClient httpClient = new DefaultHttpClient();

    @BeforeClass
    public static void initRoutes() throws InterruptedException {
        post("/assertNoCookies", (r, request, response) -> {
			if (!request.cookies().isEmpty()) {
				r.halt(500);
			}
			return "";
        });

        post("/setCookie", (r, request, response) -> {
			response.cookie(request.queryParams("cookieName"), request.queryParams("cookieValue"));
			return "";
        });

        post("/assertHasCookie", (r, request, response) -> {
			String cookieValue = request.cookie(request.queryParams("cookieName"));
			if (!request.queryParams("cookieValue").equals(cookieValue)) {
				r.halt(500);
			}
			return "";
        });

        post("/removeCookie", (r, request, response) -> {
			String cookieName = request.queryParams("cookieName");
			String cookieValue = request.cookie(cookieName);
			if (!request.queryParams("cookieValue").equals(cookieValue)) {
				r.halt(500);
			}
			response.removeCookie(cookieName);
			return "";
        });
    }

    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Test
    public void testEmptyCookies() {
        httpPost("/assertNoCookies");
    }

    @Test
    public void testCreateCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertHasCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
    }

    @Test
    public void testRemoveCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/removeCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertNoCookies");
    }

    private void httpPost(String relativePath) {
        HttpPost request = new HttpPost(DEFAULT_HOST_URL + relativePath);
        try {
            HttpResponse response = httpClient.execute(request);
            assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            request.releaseConnection();
        }
    }
}
