import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.eclipse.egit.github.core.Gist;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.time.LocalTime;
import java.util.Base64;
import java.util.List;

public class BaseTest {
    final String testId = LocalTime.now().toString(); //not the best practice but let's put test identifier as current machine time
    final String gistFilename = String.format("Test_%s.txt", testId);

    //I would store these values in .properties, but for speed I put them here
    protected final String BASE_URL = "https://api.github.com";
    protected String token = "YzY4NDJlZTUyNjk0NWZiM2VmMTcyMjhkN2E0ZDAzOWFhOGQ4Y2NlOQ==";
    protected RequestSpecification postClient;

    @BeforeTest
    public void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
    }

    @BeforeClass
    public void auth() {
        //not necessarily, but more transparent for supportability
        postClient = RestAssured.given();
        postClient.auth().oauth2(new String(Base64.getDecoder().decode(token.getBytes())));
    }

    @AfterClass
    public void removeAllGists() {
        Response response = postClient
                .when()
                .get("/users/VladlenPechenNL/gists");
        if (300 < response.statusCode() || response.statusCode() < 200) {
            return;
        } else {
            List<Gist> mygists =
                    response
                            .then()
                            .extract()
                            .body()
                            .jsonPath()
                            .getList(".", Gist.class);
            mygists.forEach((Gist gist) -> {
                postClient
                        .when()
                        .delete(String.format("/gists/%s", gist.getId()))
                        .then()
                        .statusCode(204);
            });
        }
    }
}
