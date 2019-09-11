import com.google.common.collect.ImmutableMap;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;

public class AuthGistsTests extends BaseTest{

    private String gistFilecontent;
    private String testUser = "VmxhZGxlblBlY2hlbk5M";
    private String testPass = "UGljTmljX2xla2tlcl9lZXQ=";


    //just another way of gist file content generation in this case
    public AuthGistsTests(){
        byte[] byteContent = new byte[100];
        new Random().nextBytes(byteContent);
        gistFilecontent = new String(byteContent, Charset.defaultCharset());
    }

    @Test
    public void testBasic(){
        postClient.auth().basic(new String(Base64.getDecoder().decode(testUser.getBytes())),
                new String(Base64.getDecoder().decode(testPass.getBytes())))
        .when()
                .get("/users/VladlenPechenNL/gists")
        .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testNegativeOauth2(){
        postClient.auth().oauth2(token)
                .when()
        .get("/users/VladlenPechenNL/gists")
                .then()
                .assertThat()
                .statusCode(401);
    }

    //all the tests will be negative cause we already have positive test in EndToEnd therefore we need to check that
    //it's not possible to create gist while being logged in with wrong credentials
    @AfterTest
    public void testNegativeCreateGist(){
        Gist modelGist = new Gist().setDescription(String.format("picnic test %s: gist creation", testId));
        GistFile fileMk = new GistFile().setFilename(gistFilename).setContent(gistFilecontent);
        modelGist.setFiles(ImmutableMap.of(fileMk.getFilename(), fileMk));

        postClient
                .contentType("application/json")
                .body(modelGist)
        .when()
                        .post("/gists")
        .then()
                        .assertThat()
                        .statusCode(401);
    }
}
