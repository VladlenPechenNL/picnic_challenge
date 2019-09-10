import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import static io.restassured.RestAssured.given;

public class BaseTest {
    protected final String BASE_URL = "";
    protected String token = "844a761535c5862c55a223b330f72e7d99ed098f";
    protected String thisGistId;

    @BeforeClass
    public void setupRestAssured() {
        RestAssured.baseURI = "https://api.github.com";
    }

    @BeforeTest
    public void auth() {
        given().auth().oauth2(token);
    }

    @AfterTest
    public void removeGist() {
        int statusCode = RestAssured.when().delete("/gists/" + thisGistId).statusCode();
        Assert.assertEquals(statusCode,204, String.format("Status code expected: %s; actual %s; while deleting %s gist","204", String.valueOf(statusCode), thisGistId));
    }
}
