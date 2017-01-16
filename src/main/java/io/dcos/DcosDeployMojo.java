package io.dcos;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.util.Map;

/**
 * Mojo to handle `mvn dcos:deploy`
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DcosDeployMojo extends AbstractDcosMojo {

  public void execute() throws MojoExecutionException {
    CloseableHttpClient client = null;
    try {
      client = DcosPluginHelper.buildClient(ignoreSSL);
      Map<String, String> marathonConfigurationJson = DcosPluginHelper.readJsonFileToMap(appDefinitionFile);
      HttpPut put = new HttpPut(dcosUrl + "/service/marathon/v2/apps/" + marathonConfigurationJson.get("id"));
      put.setHeader("Authorization", "token=" + DcosPluginHelper.readToken(dcosTokenFile));
      put.setEntity(new FileEntity(appDefinitionFile));
      client.execute(put);
    } catch (Exception e) {
      getLog().error("Unable to perform deployment", e);
      throw new RuntimeException(e);
    } finally { // no try-with-resources in this language level -> compatibility
      if (client != null) {
        try {
          client.close();
        } catch (IOException e) {
          getLog().warn("Unable to close the http client", e);
        }
      }
    }
  }
}
