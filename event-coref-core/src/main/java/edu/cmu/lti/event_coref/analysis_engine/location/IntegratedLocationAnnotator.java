package edu.cmu.lti.event_coref.analysis_engine.location;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.event_coref.utils.WorldGazetteer;
import edu.cmu.lti.event_coref.utils.WorldGazetteerRecord;
import edu.cmu.lti.utils.general.ListUtils;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Zhengzhong Liu, Hector
 */
public class IntegratedLocationAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_WORLD_GAZETEER_PATH = "WorldGazetteerPath";

    public static final String PARAM_GEO_NAME_SERVICE_USERNAME = "GeonameUserName";

    @ConfigurationParameter(name = PARAM_WORLD_GAZETEER_PATH)
    private String gazetteerPath;

//    @ConfigurationParameter(name = PARAM_GEO_NAME_SERVICE_USERNAME)
//    private String geonameServiceUserName;

    private WorldGazetteer wg;

    private List<EventMention> eventMentionList;

    private Collection<Sentence> sentenceList;

    private ArrayListMultimap<Span, Location> spanToLocationsMap;

    public static final String COMPONENT_ID_ANNOTATOR = "System-integrated-location";

    public static final String COMPONENT_ID_WORLD = "System-integrated-location-world-gazetteer";

//    public static final String COMPONENT_ID_DBEPDIA = "System-integrated-location-dbpedia";
//
//    public static final String COMPONENT_ID_GEONAMES = "System-itengrated-location-geonames";
//
//    private static final String DBPediaLocationType = "DBpedia:Place";

//	private GeonamesServices geonameService;

    private SimilarityCalculator sim;

    private static final Logger logger = LoggerFactory.getLogger(IntegratedLocationAnnotator.class);

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

//		geonameService = new GeonamesServices(geonameServiceUserName);
        sim = new SimilarityCalculator(true);

        // Get config. parameter values
        wg = new WorldGazetteer(gazetteerPath);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        try {
            Thread.sleep(500); // stop a while because we need to call dbpedia
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UimaConvenience.printProcessLog(aJCas, logger);

        // logger.info("Processing " + JCasUtil.selectSingle(aJCas,
        // Article.class).getArticleName());

        // initialize an empty location map
        spanToLocationsMap = ArrayListMultimap.create();

        AnnotationCondition basicCondition = new AnnotationCondition() {
            // simply prevent out of the document
            @Override
            public Boolean check(TOP aAnnotation) {
                Annotation anno = (Annotation) aAnnotation;
                if (anno.getBegin() < 0 || anno.getEnd() < 0) {
                    return false;
                }
                return true;
            }
        };


        Map<EventMention, Collection<Sentence>> sentenceCoveringByEvm = JCasUtil.indexCovering(aJCas, EventMention.class, Sentence.class);

        eventMentionList = UimaConvenience.getAnnotationListWithFilter(aJCas,
                EventMention.class, basicCondition);

        sentenceList = JCasUtil.select(aJCas, Sentence.class);

        for (EventMention evm : eventMentionList) {
            Sentence sentence = null;
            if (sentenceCoveringByEvm.containsKey(evm)) {
                Collection<Sentence> sentences = sentenceCoveringByEvm.get(evm);
                if (sentences.size() > 0) {
                    sentence = Iterators.getLast(sentences.iterator());
                }
            }

            List<ComponentAnnotation> locationCandidates = new ArrayList<ComponentAnnotation>();

            if (sentence == null) {
                continue;
            }

            // Option 1, use the Stanford annotations within the same sentence.
            List<StanfordEntityMention> scnesInSent = JCasUtil.selectCovered(
                    StanfordEntityMention.class, sentence);

            for (StanfordEntityMention scne : scnesInSent) {
                String entityType = scne.getEntityType();
                if (!(entityType == null) && entityType.equals("LOCATION")) {
                    locationCandidates.add(scne);
                }
            }

            if (ListUtils.isNullOrEmptyList(locationCandidates)) {
                // TODO In the case that there is no location candidate found in
                // the
                // sentence, maybe use some other way to deal with it later
            } else {
                List<Location> locationsToAttach;
                if (locationCandidates.size() == 1) {
                    locationsToAttach = createLocationFromAnnotation(aJCas,
                            locationCandidates.get(0));
                } else {
                    // choose based on distance proximity now
                    ComponentAnnotation closestCand = locationCandidates.get(0);
                    int min = Integer.MAX_VALUE;
                    for (ComponentAnnotation cand : locationCandidates) {
                        int dist = Math.min(
                                Math.abs(evm.getBegin() - cand.getBegin()),
                                Math.abs(evm.getEnd() - cand.getEnd()));
                        if (dist < min) {
                            min = dist;
                            closestCand = cand;
                        }
                    }
                    locationsToAttach = createLocationFromAnnotation(aJCas,
                            closestCand);
                }

                Set<Location> locList = new HashSet<Location>();
                locList.addAll(locationsToAttach);

                List<EntityBasedComponentLink> loclinks = new ArrayList<EntityBasedComponentLink>();
                for (Location loc : locList) {
                    EntityBasedComponentLink llink = new EntityBasedComponentLink(
                            aJCas);
                    llink.setEventMention(evm);
                    llink.setComponent(loc);
                    llink.addToIndexes(aJCas);
                    llink.setComponentId(COMPONENT_ID_ANNOTATOR);
                    llink.setLinkType(APLUtils.LOCATION_LINK_TYPE);
                    loc.setComponentLinks(UimaConvenience.appendFSList(aJCas,
                            loc.getComponentLinks(), llink,
                            EntityBasedComponentLink.class));
                    loclinks.add(llink);
                }
                evm.setLocationLinks(FSCollectionFactory.createFSList(aJCas,
                        loclinks));
            }
        }
    }

    /**
     * Analyze a certain annotation and transform it to the possible location
     *
     * @param anno
     * @param aJCas
     * @return
     */
    private List<Location> createLocationFromAnnotation(JCas aJCas,
                                                        ComponentAnnotation anno) {
        List<Location> resultLocations = new ArrayList<Location>();

        int begin = anno.getBegin();
        int end = anno.getEnd();

        Span locSpan = new Span(begin, end);

        // 0. check if we can reuse existing location annotation
        if (spanToLocationsMap.containsKey(locSpan)) {
            return spanToLocationsMap.get(locSpan);
        }

        // this simpleSurfaceForm could be the most descriptive term for the
        // location
        String simpleSurfaceForm = anno.getCoveredText();

//        // 1. Find dbpedia annotations contained
//        List<DbpediaAnnotation> allDbpediaAnnotations = JCasUtil.selectCovered(
//                DbpediaAnnotation.class, anno);
//
//        List<DbpediaAnnotation> dbpediaLocationAnnotations = new ArrayList<DbpediaAnnotation>();
//        // filter and only use those location annotations.
//        for (DbpediaAnnotation dbpediaAnnotation : allDbpediaAnnotations) {
//            StringList annotationTypesSL = dbpediaAnnotation.getResourceType();
//            Collection<String> annotationTypes = FSCollectionFactory
//                    .create(annotationTypesSL);
//
//            boolean isALocation = false;
//            for (String annotationType : annotationTypes) {
//                if (annotationType.equals(DBPediaLocationType)) {
//                    isALocation = true;
//                }
//            }
//            if (isALocation) {
//                dbpediaLocationAnnotations.add(dbpediaAnnotation);
//            }
//        }
//
//        if (!dbpediaLocationAnnotations.isEmpty()) {
//            logger.info("Using DBpedia information.");
//
//            Location loc = createPlainLocatoin(aJCas, begin, end,
//                    COMPONENT_ID_DBEPDIA);
//
//            String dbpediaPrefix = "http://dbpedia.org/resource/";
//            // naive solution, assume only one resource inside this span
//            DbpediaAnnotation dbpediaLocationAnnotation = dbpediaLocationAnnotations
//                    .get(0);
//            String plainResourceUri = dbpediaLocationAnnotation.getUri();
//            String resourceUri = plainResourceUri.replaceAll("^"
//                    + dbpediaPrefix, "");
//            logger.info(resourceUri);
//
//            try {
//                ArrayListMultimap<String, String> dbpediaInfo = DbpediaService
//                        .queryDBpediaSparsql(resourceUri);
//
//                if (dbpediaInfo.containsKey("country")) {
//                    String country = dbpediaInfo.get("country").get(0); // one
//                    // is
//                    // enougth
//                    country = country.substring(dbpediaPrefix.length(),
//                            country.length());
//                    loc.setCountry(country);
//                }
//
//                List<String> entitiesIn = new ArrayList<String>();
//                for (String entity : dbpediaInfo.get("partOf")) {
//                    entitiesIn.add(entity);
//                }
//
//                for (String entity : dbpediaInfo.get("country_of")) {
//                    entitiesIn.add(entity);
//                }
//                loc.setEntitiesLocactedIn(FSCollectionFactory.createStringList(
//                        aJCas, entitiesIn));
//
//                List<String> locatedIn = new ArrayList<String>();
//
//                for (String locate : dbpediaInfo.get("location")) {
//                    locatedIn.add(locate);
//                }
//
//                for (String locate : dbpediaInfo.get("parOf_of")) {
//                    locatedIn.add(locate);
//                }
//
//                loc.setLocatedIn(FSCollectionFactory.createStringList(aJCas,
//                        locatedIn));
//
//                List<String> historicalEvents = dbpediaInfo.get("place_of");
//                if (historicalEvents != null) {
//                    loc.setHistoricalEvents(FSCollectionFactory
//                            .createStringList(aJCas, historicalEvents));
//                }
//
//                if (dbpediaInfo.containsKey("long")) {
//                    String longtitudeStr = dbpediaInfo.get("long").get(0);
//                    float longtitude = Float.parseFloat(longtitudeStr
//                            .split("\\^\\^")[0]);
//                    loc.setLongitude(longtitude);
//                }
//
//                if (dbpediaInfo.containsKey("lat")) {
//                    String latitudeStr = dbpediaInfo.get("lat").get(0);
//                    float latitude = Float.parseFloat(latitudeStr
//                            .split("\\^\\^")[0]);
//                    loc.setLatitude(latitude);
//                }
//
//                List<String> alternativeNames = new ArrayList<String>();
//
//                for (String name : dbpediaInfo.get("name")) {
//                    String stripName = name.split("@")[0];
//                    alternativeNames.add(stripName);
//                }
//
//                for (String name : dbpediaInfo.get("dbName")) {
//                    String stripName = name.split("@")[0];
//                    alternativeNames.add(stripName);
//                }
//
//                loc.setNames(FSCollectionFactory.createStringList(aJCas,
//                        alternativeNames));
//
//                resultLocations.add(loc);
//                return resultLocations;
//            } catch (Exception e) {
//                // dbpedia query probably failed
//                // e.printStackTrace();
//                System.out.println("Query Fail! Roll back to other methods");
//            }
//        }
//
//        // 2 use geoname
//        List<GeonamesQueryResult> geoNameResults = geonameService
//                .getGeonameResults(simpleSurfaceForm, 10);
//        if (geoNameResults.size() != 0) {
//            List<GeonamesQueryResult> bestMatchResults = new ArrayList<GeonamesQueryResult>();
//
//            boolean stringMatch = false;
//
//            for (GeonamesQueryResult result : geoNameResults) {
//                String name = result.getName();
//
//                if (sim.getDiceCoefficient(name, simpleSurfaceForm) > 0.8) {
//                    bestMatchResults.add(result);
//                    stringMatch = true;
//                }
//
//                if (!stringMatch && sim.subStringTest(name, simpleSurfaceForm)) {
//                    bestMatchResults.add(result);
//                }
//            }
//
//            for (GeonamesQueryResult result : bestMatchResults) {
//                Location loc = createPlainLocatoin(aJCas, begin, end,
//                        COMPONENT_ID_GEONAMES);
//                Set<String> names = new HashSet<String>();
//                names.add(simpleSurfaceForm);
//                names.addAll(result.getAlternativeNames());
//                names.add(result.getName());
//
//                loc.setNames(FSCollectionFactory.createStringList(aJCas, names));
//                loc.setLatitude((float) result.getLatitude());
//                loc.setLongitude((float) result.getLongtitde());
//                loc.setCountry(result.getCountry());
//
//                logger.info("Using geoname for " + result.getName());
//                resultLocations.add(loc);
//            }
//            return resultLocations;
//        }

        // 2 backup plan, use world gazetteer
        logger.info("Using world gazetteer information");

        List<WorldGazetteerRecord> records = wg
                .getNaiveRecord(simpleSurfaceForm);

        if (records.size() == 0) {
            Location loc = createPlainLocatoin(aJCas, begin, end,
                    COMPONENT_ID_ANNOTATOR);
            List<String> names = new ArrayList<String>();
            names.add(simpleSurfaceForm);
            loc.setNames(FSCollectionFactory.createStringList(aJCas, names)); // if
            // no
            // match
            // in
            // gazeteer,
            // using
            // the
            // simple
            // name
            // from
            // text
            // instead
            resultLocations.add(loc);
        } else {
            for (WorldGazetteerRecord record : records) {
                Location loc = createPlainLocatoin(aJCas, begin, end,
                        COMPONENT_ID_WORLD);
                String englishName = record.getName();
                loc.setLatitude(record.getLatitude());
                loc.setLongitude(record.getLongitude());

                String locationType = record.getGeograficalType();
                loc.setLocationType(locationType);

                if (null != locationType && locationType.equals("country")) {
                    loc.setCountry(englishName);
                } else {
                    loc.setCountry(record.getParentCountry());
                }

                Set<String> names = new HashSet<String>();
                names.add(englishName);
                names.addAll(record.getAlternativeNames());
                names.addAll(record.getOriginNames());
                StringList namesSl = FSCollectionFactory.createStringList(
                        aJCas, names);
                loc.setNames(namesSl);

                resultLocations.add(loc);
                spanToLocationsMap.put(locSpan, loc);
            }
        }
        return resultLocations;

    }

    private Location createPlainLocatoin(JCas aJCas, int begin, int end,
                                         String componentId) {
        Location loc = new Location(aJCas);
        loc.setBegin(begin);
        loc.setEnd(end);
        loc.addToIndexes(aJCas);
        loc.setComponentId(componentId);
        Span locSpan = new Span(begin, end);
        spanToLocationsMap.put(locSpan, loc); // add to this map to avoid
        // duplications
        return loc;
    }

}
