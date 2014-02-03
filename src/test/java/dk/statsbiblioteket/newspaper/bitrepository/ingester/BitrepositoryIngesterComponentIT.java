package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.io.FileInputStream;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.bitrepository.releaser.BitrepositoryReleaserComponent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BitrepositoryIngesterComponentIT {
    private final static String TEST_BATCH_ID = "400022028241";
    private String pathToConfig;
    private String pathToTestBatch;
    private final Properties properties = new Properties();

    /**
     * Tests that the releaser have the expected good case behavior.
     */
    @Test(groups = "integrationTest")
    public void releaseOnlineSuccess() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "small-test-batch");
        properties.setProperty(BitrepositoryReleaserComponent.RELEASE_ONLINE_COMMAND, "true");
        
        BitrepositoryReleaserComponent bitrepositoryReleaserComponent =
                new BitrepositoryReleaserComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Bitrepository releaser", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryReleaserComponent.doWorkOnBatch(batch, resultCollector);
        
        assertTrue(resultCollector.isSuccess());
    }
    
    /**
     * Tests that the releaser have the expected bad case behavior.
     */
    @Test(groups = "integrationTest")
    public void releaseOnlineFailure() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "small-test-batch");
        properties.setProperty(BitrepositoryReleaserComponent.RELEASE_ONLINE_COMMAND, "false");
        
        BitrepositoryReleaserComponent bitrepositoryReleaserComponent =
                new BitrepositoryReleaserComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Bitrepository releaser", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryReleaserComponent.doWorkOnBatch(batch, resultCollector);
        
        assertFalse(resultCollector.isSuccess());
        resultCollector.toReport().contains("Failed to release batch from online status.");
    }
    
    /**
     * Tests that the ingester can parse a (small) production like batch.
     */
    @Test(groups = "integrationTest")
    public void badBatchSurvivabilityCheck() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "bad-bad-batch");

        BitrepositoryReleaserComponent bitrepositoryReleaserComponent =
                new BitrepositoryReleaserComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Mocked bitrepository releaser", "test version");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryReleaserComponent.doWorkOnBatch(batch, resultCollector);
    }

    @BeforeMethod(alwaysRun = true)
    private void loadConfiguration() throws Exception {
        String generalPropertiesPath = System.getProperty("integration.test.newspaper.properties");
        String propertiesDir = generalPropertiesPath.substring(0, generalPropertiesPath.lastIndexOf("/"));
        pathToConfig = propertiesDir + "/newspaper-bitrepository-ingester-config";
        pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        properties.load(new FileInputStream(pathToConfig + "/config.properties"));
    }
}
