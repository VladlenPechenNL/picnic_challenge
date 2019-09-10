import com.google.common.collect.ImmutableMap;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EndToEndGists extends BaseTest{
    
    Gist modelGist;
    final String testId = LocalTime.now().toString(); //not the best practice but let's put test identifier as current machine time
    final String gistFilename = String.format("Test_%s.txt", testId);    

    String gistFilecontent;
    String gistId;

    @Test (priority = 2)
    public void createGist(){
        //generating some random content here
        byte[] byteContent = new byte[100];
        new Random().nextBytes(byteContent);
        gistFilecontent = new String(byteContent, Charset.defaultCharset());

        //creating gist mock using Gist class provided in org.eclipse.egit.github.core package
        modelGist = new Gist().setDescription(String.format("picnic test %s: gist creation", testId));

        //creating gist file
        GistFile fileMk = new GistFile().setFilename(gistFilename).setContent(gistFilecontent);

        //assigning file to Gist object
        modelGist.setFiles(ImmutableMap.of(fileMk.getFilename(), fileMk));

        //forming request for gist creations
        modelGist =
                postClient
                .contentType("application/json")
                .body(modelGist) //auto-serializing happens
        .when()
                .post("/gists") //at this point we perform creation itself
        .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(Gist.class); //de-serializing at this point
        Assert.assertNotNull(modelGist); //simple assert that it's been assigned
        
        gistId = modelGist.getId();
    }

    @Test (priority = 4)
    public void updateGist(){
        //preparing files to put
        Map<String,GistFile> filesMap = modelGist.getFiles();
        String newContent = gistFilecontent.substring(50);
        GistFile newFileMk = new GistFile().setFilename(gistFilename.replace(".txt", ".py")).setContent(newContent);
        filesMap.put(newFileMk.getFilename(), newFileMk);
        modelGist.setFiles(filesMap);

        Gist actualGist =
        postClient
                .contentType("application/json")
                .body(modelGist)
        .when()
                .patch(String.format("/gists/%s", gistId))
        .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(Gist.class);

        Assert.assertNotNull(actualGist);

        Assert.assertEquals(actualGist.getId(), modelGist.getId(),
                String.format("Created and actual gist.id mismatch; expected: %s, actual: %s"
                        , modelGist.getId(), actualGist.getId()));

        Assert.assertNotNull(actualGist.getFiles().get(newFileMk.getFilename()),
                String.format("actual Gist has no filename expected; expected: %s"
                        , newFileMk.getFilename()));

        Assert.assertEquals(actualGist.getFiles().get(newFileMk.getFilename()).getContent(), newContent,
                String.format("Created and actual gist.content mismatch; expected: %s, actual: %s",
                        newContent, actualGist.getFiles().get(gistFilename).getContent()));
    }

    @Test(priority = 3)
    public void getThisGist(){
        Gist actualGist =
         postClient
         .when()
                .get(String.format("/gists/%s", gistId))
        .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(Gist.class);

        Assert.assertEquals(modelGist.getId(), actualGist.getId(),
                String.format("Created and actual gist.id mismatch; expected: %s, actual: %s",
                        modelGist.getId(), actualGist.getId()));

        Assert.assertNotNull(actualGist.getFiles().get(gistFilename),
                String.format("actual Gist has no filename expected; expected: %s",
                        gistFilename));

        Assert.assertEquals(actualGist.getFiles().get(gistFilename).getContent(), gistFilecontent,
                String.format("Created and actual gist.content mismatch; expected: %s, actual: %s",
                        gistFilecontent, actualGist.getFiles().get(gistFilename).getContent()));
    }

    @Test(priority = 1)
    public void getMyGists(){

        //here it's quite obvious, no comments
        List mygists =
                postClient
                        .when().get("/users/VladlenPechenNL/gists")
                        .then()
                        .extract()
                        .as(List.class);
        Assert.assertTrue(mygists.isEmpty());

    }

    @AfterClass
    public void deleteGist(){
        postClient
                .when()
                .delete(String.format("/gists/%s", gistId))
        .then()
                .statusCode(204);

        //negative check
        postClient
                .when().get(String.format("/gists/%s", gistId))
                .then()
                .statusCode(404);
    }
}
