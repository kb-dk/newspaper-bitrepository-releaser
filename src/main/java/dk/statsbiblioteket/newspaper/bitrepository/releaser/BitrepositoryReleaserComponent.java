package dk.statsbiblioteket.newspaper.bitrepository.releaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

/**
 * Releases a batch from it's forced online status in the bitrepository
 */
public class BitrepositoryReleaserComponent extends AbstractRunnableComponent<Batch> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RELEASE_ONLINE_COMMAND = "bitrepository.ingester.releaseOnlineCommand";
    
    public BitrepositoryReleaserComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getEventID() {
        return "Data_Released";
    }

    /**
     * Release the batch from its force online status
     */
    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws Exception {
        String releaseOnlineCommand = getProperties().getProperty(RELEASE_ONLINE_COMMAND);
        log.info("Attempting to release batch: '" + batch.getFullID() + "'");
        
        if(!releaseOnline(batch, releaseOnlineCommand)) {
            resultCollector.addFailure(batch.getFullID(), "releaseonline", getClass().getSimpleName(), 
                    "Failed to release batch from online status.");
            return;
        }

        log.info("Finished release of batch '" + batch.getFullID() + "'");
    }

    /**
     * Method to handle the task of releasing files from their online status when work on them has been completed. 
     * The method calls a command that's present on PATH. 
     * @param batch The batch from which to keep files online 
     * @param releaseCommand the command to call
     */
    private boolean releaseOnline(Batch batch, String releaseCommand) throws IOException {
        boolean success = false;
        List<String> command = new ArrayList<String>();
        command.add(releaseCommand);
        command.add(batch.getFullID());
        
        int exitCode = -1;
        try {
            Process forceOnlineProcess = new ProcessBuilder(command).start();
            exitCode = forceOnlineProcess.waitFor();
            if(exitCode == 0) {
                success = true;
            } else {
                log.warn("Call to releaseOnline command was not a success. Command was: '" + command.toString() + "'");
                success = false;
            } 
        } catch (InterruptedException e) {
            log.error("Was interrupted while calling releaseOnline command. Command was: '" + command.toString() + "'.");
        }
        
        return success;
    }

}
