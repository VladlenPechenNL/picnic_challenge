import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.util.Base64;

import static io.restassured.RestAssured.given;

public class BaseTest {
    //I would store these values in .properties, but for speed I put them here
    protected final String BASE_URL = "";
    protected String token = "YzY4NDJlZTUyNjk0NWZiM2VmMTcyMjhkN2E0ZDAzOWFhOGQ4Y2NlOQ==";
    protected String thisGistId;

    @BeforeClass
    public void setupRestAssured() {
        RestAssured.baseURI = "https://api.github.com";
    }

    @BeforeTest
    public void auth() {
        given().auth().oauth2(new String(Base64.getDecoder().decode(token.getBytes())));
    }

    @AfterTest
    public void removeGist() {
        int statusCode = RestAssured.when().delete("/gists/" + thisGistId).statusCode();
        Assert.assertEquals(statusCode,204, String.format("Status code expected: %s; actual %s; while deleting %s gist","204", String.valueOf(statusCode), thisGistId));
    }
}
