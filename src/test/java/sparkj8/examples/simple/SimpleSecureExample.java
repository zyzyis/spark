/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sparkj8.examples.simple;

import static spark.SparkJ8.get;
import static spark.SparkJ8.post;
import static spark.SparkJ8.setSecure;

/**
 * A simple example just showing some basic functionality You'll need to provide
 * a JKS keystore as arg 0 and its password as arg 1.
 *
 * @author Peter Nicholls, based on (practically identical to in fact)
 *         {@link sparkj8.spark.examples.simple.SimpleExample} by Per Wendel
 */
public class SimpleSecureExample {

    public static void main(String[] args) {

        // setPort(5678); <- Uncomment if you want spark to listen on a port different than 4567.

        setSecure(args[0], args[1], null, null);

        get("/hello", (it, request, response) -> "Hello Secure World!");

        post("/hello", (it, request, response) -> "Hello Secure World: " + request.body());

        get("/private", (it, request, response) -> {
            response.status(401);
            return "Go Away!!!";
        });

        get("/users/:name", (it, request, response) -> "Selected user: " + request.params(":name"));

        get("/news/:section", (it, request, response) -> {
            response.type("text/xml");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>"
                + request.params("section") + "</news>";
        });

        get("/protected", (it, request, response) -> {
            it.halt(403, "I don't think so!!!");
            return null;
        });

        get("/redirect", (it, request, response) -> {
            response.redirect("/news/world");
            return null;
        });

        get("/", (it, request, response) -> "root");
    }
}
