package deft.cmucs.hovy.eventcoref;


import adept.common.Document;
import adept.common.HltContentContainer;
import adept.module.AdeptModuleException;
import adept.utilities.CommandLineApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/25/15
 * Time: 4:23 PM
 *
 * @author Zhengzhong Liu
 */
public class EventCoreferenceRunner extends CommandLineApp {
    private static String theAlgorithmConfig = "../config/edu/stanford/nlp/StanfordCoreNlpProcessorConfig.xml";

    private HovyEventCoreferenceResolver theAlgorithmProcessor = new HovyEventCoreferenceResolver();

    public static void main(String[] args) {
        new EventCoreferenceRunner().Run(args, theAlgorithmConfig);
    }

    private static final Logger logger = LoggerFactory.getLogger(EventCoreferenceRunner.class);

//    public static void main(String[] args) throws IOException {
//        File configFile = new File(EventCoreferenceRunner.class.getClassLoader().
//                getResource("edu/cmucs/hovy/eventcoref/CmucsHovyEventcorefProcessorConfig.xml").getFile());
//        new EventCoreferenceRunner().Run(args, configFile.getCanonicalPath());
//    }

    @Override
    protected void doActivate(String algorithmConfig) {
        try {
            File f = new File(algorithmConfig);
            if (!f.exists()) {
                // Hack for Eclipse
                //System.out.println("Config file not found:  " + f.getAbsolutePath());
                algorithmConfig = "edu/stanford/nlp/StanfordCoreNlpProcessorConfig.xml";
                //System.out.println("Hack for Eclipse:  " + algorithmConfig);
                f = new File(algorithmConfig);
            }
            if (!f.exists()) {
                // Hack for command line
                //System.out.println("Config file not found:  " + f.getAbsolutePath());
                algorithmConfig = "./target/classes/edu/stanford/nlp/StanfordCoreNlpProcessorConfig.xml";
                f = new File(algorithmConfig);
            }
            if (!f.exists())
                throw new RuntimeException("Config file hovy event coreference not found: " + algorithmConfig);
            logger.info("Config file found:  " + f.getAbsolutePath());
            theAlgorithmProcessor.activate(algorithmConfig);
        } catch (AdeptModuleException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doDeactivate() {
        try {
            theAlgorithmProcessor.deactivate();
        } catch (AdeptModuleException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected HltContentContainer doProcess(Document document, HltContentContainer hltContentContainer) {
        try {
            // create the HltContentContainer object
            if (hltContentContainer == null) hltContentContainer = new HltContentContainer();
            hltContentContainer = theAlgorithmProcessor.process(document, hltContentContainer);
            //logger.info("hltContentContainer populated with sentences, POS tags and Entity mentions");
            return hltContentContainer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}