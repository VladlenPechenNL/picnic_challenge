import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.eclipse.egit.github.core.Gist;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.util.Base64;
import java.util.List;

public class BaseTest {
    //I would store these values in .properties, but for speed I put them here
    protected final String BASE_URL = "";
    protected String token = "YzY4NDJlZTUyNjk0NWZiM2VmMTcyMjhkN2E0ZDAzOWFhOGQ4Y2NlOQ==";
    protected String thisGistId;
    protected RequestSpecification postClient;

    @BeforeTest
    public void setupRestAssured() {
        RestAssured.baseURI = "https://api.github.com";
    }

    @BeforeClass
    public void auth() {
        //not necessarily, but more transparent for supportability
        postClient = RestAssured.given();
        postClient.auth().oauth2(new String(Base64.getDecoder().decode(token.getBytes())));
    }

    @AfterClass
    public void removeAllGists() {
        List<Gist> mygists =
                postClient
                        .when()
                        .get("/users/VladlenPechenNL/gists")
                        .then()
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", Gist.class);
        mygists.forEach((Gist gist) ->{
            postClient
                    .when()
                    .delete(String.format("/gists/%s", gist.getId()))
                    .then()
                    .statusCode(204);
        });
    }
}
