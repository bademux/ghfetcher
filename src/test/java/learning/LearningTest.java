package learning;

import com.github.bademux.ghfecher.Application;
import com.github.bademux.ghfecher.utils.PagingAwareDispatcher;
import com.github.bademux.ghfecher.utils.Utils;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@ContextConfiguration(initializers = LearningTest.Initializer.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8080", "spring.main.web-application-type=reactive"}
)
@RunWith(SpringRunner.class)
public class LearningTest {

    private static final MockWebServer MOCK_WEB_SERVER = new MockWebServer();

    @Rule
    public MockWebServer server = MOCK_WEB_SERVER;

    @Ignore("learning test, should be in Learning suit/package")
    @Test
    public void runSpringAndMockServer() throws Exception {
        //prepare
        while (server.takeRequest(10, TimeUnit.MINUTES) != null) ;
    }


    @Before
    public void setUp() throws Exception {
        server.setDispatcher(PagingAwareDispatcher.of(Utils.createItems(299)));
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "githabBaseUrl=" + MOCK_WEB_SERVER.url("").toString(),
                    "cache.maximumSize=10000",
                    "cache.expireAfterWriteMin=5"
            )
                    .applyTo(applicationContext.getEnvironment());
        }

    }
}
