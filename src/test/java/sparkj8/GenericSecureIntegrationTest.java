package sparkj8;

import static spark.SparkJ8.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.util.SparkTestUtil;
import spark.util.SparkTestUtil.UrlResponse;

public class GenericSecureIntegrationTest {

    static SparkTestUtil testUtil;

    @AfterClass public static void tearDown () {
        stop ();
    }

    @BeforeClass public static void setup () {
        testUtil = new SparkTestUtil (4567);

        // note that the keystore stuff is retrieved from SparkTestUtil which
        // respects JVM params for keystore, password
        // but offers a default included store if not.
        String keyStoreLocation = SparkTestUtil.getKeyStoreLocation ();
        String keystorePassword = SparkTestUtil.getKeystorePassword ();
        setSecure (keyStoreLocation, keystorePassword, null, null);

        before ("/protected/*", (it, request, response) -> it.halt (401, "Go Away!"));

        get ("/hi", (it, request, response) -> "Hello World!");

        get ("/:param", (it, request, response) -> "echo: " + request.params (":param"));

        get ("/paramwithmaj/:paramWithMaj", (it, request, response) ->
                "echo: " + request.params (":paramWithMaj")
        );

        get ("/", (it, request, response) -> "Hello Root!");

        post ("/poster", (it, request, response) -> {
            String body = request.body ();
            response.status (201); // created
            return "Body was: " + body;
        });

        patch ("/patcher", (it, request, response) -> {
            String body = request.body ();
            response.status (200);
            return "Body was: " + body;
        });

        after ("/hi", (it, request, response) -> response.header ("after", "foobar"));

        try {
            Thread.sleep (500);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

    @Test public void testGetHi () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/hi", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("Hello World!", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testHiHead () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("HEAD", "/hi", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testGetHiAfterFilter () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/hi", null);
            Assert.assertTrue (response.headers.get ("after").contains ("foobar"));
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testGetRoot () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("Hello Root!", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testEchoParam1 () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/shizzy", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("echo: shizzy", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testEchoParam2 () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/gunit", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("echo: gunit", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testEchoParamWithMaj () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("GET", "/paramwithmaj/plop", null);
            Assert.assertEquals (200, response.status);
            Assert.assertEquals ("echo: plop", response.body);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testUnauthorized () throws Exception {
        try {
            UrlResponse urlResponse = testUtil.doMethodSecure ("GET", "/protected/resource", null);
            Assert.assertTrue (urlResponse.status == 401);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testNotFound () throws Exception {
        try {
            UrlResponse urlResponse = testUtil.doMethodSecure ("GET", "/no/resource", null);
            Assert.assertTrue (urlResponse.status == 404);
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testPost () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("POST", "/poster", "Fo shizzy");
            System.out.println (response.body);
            Assert.assertEquals (201, response.status);
            Assert.assertTrue (response.body.contains ("Fo shizzy"));
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    @Test public void testPatch () {
        try {
            UrlResponse response = testUtil.doMethodSecure ("PATCH", "/patcher", "Fo shizzy");
            System.out.println (response.body);
            Assert.assertEquals (200, response.status);
            Assert.assertTrue (response.body.contains ("Fo shizzy"));
        }
        catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }
}
