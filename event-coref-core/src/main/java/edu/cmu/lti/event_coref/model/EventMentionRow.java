package edu.cmu.lti.event_coref.model;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.ClusterUtils;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A row in the suduku table data structure. Save the Uima types in easy access Java format. Use the
 * SudokuTable util factory to create this is a better idea.
 * <p/>
 * Each row is associated with an event.
 *
 * @author Zhengzhong Liu, Hector
 */
public class EventMentionRow {
    // Sudoku row id
    private int id;

    // The following will be necessary for each row
    private Paragraph paragraph;

    private Sentence sentence; // the sentence associated with this event

    private Clause clause; // the clause associated with this event

    // private String text;
    private List<Word> words; // words associate with the mention

    // The following will need to be filled up when proceed
    private List<EntityBasedComponent> agents;

    private List<EntityBasedComponentLink> agentLinks;

    private List<EntityBasedComponent> patients;

    private List<EntityBasedComponentLink> patientLinks;

    private List<Location> locations;

    private List<EntityBasedComponentLink> locationLinks;

    private List<EntityBasedComponent> timeAnnotations;

    private List<EntityBasedComponentLink> timeLinks;

    private EventMention eventMention;

    private Joiner slashJoiner = Joiner.on(" / ").skipNulls();

    private Joiner semiColonJoiner = Joiner.on(" ; ").skipNulls();

    private Joiner hyphenJoiner = Joiner.on(" - ").skipNulls();

    private Joiner spaceJoiner = Joiner.on(" ").skipNulls();

    private static final Logger logger = LoggerFactory.getLogger(EventMentionRow.class);

    public EventMentionRow() {

    }

    public EventMentionRow(EventMention evm) {
        setEventMention(evm);

        if (evm.getAgentLinks() != null) {
            List<EntityBasedComponentLink> agentLinks = new ArrayList(FSCollectionFactory.create(evm
                    .getAgentLinks()));
            setAgents(agentLinks);
        } else {
            setAgents(new ArrayList<EntityBasedComponentLink>());
        }
        if (evm.getPatientLinks() != null) {
            List<EntityBasedComponentLink> patientLinks = new ArrayList(FSCollectionFactory.create(evm
                    .getPatientLinks()));
            setPatients(patientLinks);
        } else {
            setPatients(new ArrayList<EntityBasedComponentLink>());
        }
        if (evm.getLocationLinks() != null) {
            List<EntityBasedComponentLink> locationLinks = new ArrayList(FSCollectionFactory.create(evm
                    .getLocationLinks()));
            setLocations(locationLinks);
        } else {
            setLocations(new ArrayList<EntityBasedComponentLink>());
        }

        if (evm.getTimeLinks() != null) {
            List<EntityBasedComponentLink> timeLinks = new ArrayList(FSCollectionFactory.create(evm
                    .getTimeLinks()));
            setTimeLinks(timeLinks);
        }

    }

    public List<EntityBasedComponentLink> getAgentLinks() {
        return agentLinks;
    }

    private void setAgentLinks(List<EntityBasedComponentLink> agentLinks) {
        this.agentLinks = agentLinks;
    }

    public List<EntityBasedComponentLink> getPatientLinks() {
        return patientLinks;
    }

    private void setPatientLinks(List<EntityBasedComponentLink> patientLinks) {
        this.patientLinks = patientLinks;
    }

    public List<EntityBasedComponentLink> getLocationLinks() {
        return locationLinks;
    }

    private void setLocationLinks(List<EntityBasedComponentLink> locationLinks) {
        this.locationLinks = locationLinks;
    }

    private void setTimeLinks(List<EntityBasedComponentLink> timeLinks) {
        this.timeLinks = timeLinks;
    }

    public List<EntityBasedComponentLink> getTimelinks() {
        return timeLinks;
    }

    public String[] toSemanticDatabaseRequiredStringArray(boolean withSentence, String articleTitle,
                                                          int docId) {
        List<String> contentList = new LinkedList<String>();

        List<String> wordsAsString = Lists.transform(words, new Function<Word, String>() {
            @Override
            public String apply(Word from) {
                return from.getCoveredText();
            }
        });

        contentList.add(spaceJoiner.join(wordsAsString));

        if (withSentence) {
            contentList.add(sentence.getCoveredText().replaceAll("\n", " "));
        }

        String agentCell = "";
        if (hasAgents()) {
            List<String> agentSurfaceList = Lists.transform(agents,
                    new Function<EntityBasedComponent, String>() {
                        @Override
                        public String apply(EntityBasedComponent from) {
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));
                            return from.getCoveredText() + " : "
                                    + semiColonJoiner.join(getShortenAnnotationSurfaces(annotations));
                        }
                    });
            agentCell = slashJoiner.join(agentSurfaceList);
        }
        contentList.add(agentCell);

        String patientCell = "";
        if (hasPatients()) {
            List<String> patientSurfaceList = Lists.transform(patients,
                    new Function<EntityBasedComponent, String>() {
                        @Override
                        public String apply(EntityBasedComponent from) {
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));
                            return from.getCoveredText() + " : "
                                    + semiColonJoiner.join(getShortenAnnotationSurfaces(annotations));
                        }
                    });
            patientCell = slashJoiner.join(patientSurfaceList);
        }
        contentList.add(patientCell);

        String locationCell = "";
        if (hasLocations()) {
            List<String> locationSurfaceList = Lists.transform(locations,
                    new Function<Location, String>() {
                        @Override
                        public String apply(Location from) {
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));
                            return from.getCoveredText() + " : "
                                    + semiColonJoiner.join(getShortenAnnotationSurfaces(annotations));
                        }
                    });
            locationCell = slashJoiner.join(locationSurfaceList);
        }
        contentList.add(locationCell);

        contentList.add(docId + "");
        contentList.add(articleTitle);

        return contentList.toArray(new String[contentList.size()]);
    }

    public String[] toStringArray(boolean withSentence) {
        // StringBuilder sb = new StringBuilder();
        List<String> contentList = new LinkedList<String>();

        List<String> wordsAsString = Lists.transform(words, new Function<Word, String>() {
            @Override
            public String apply(Word from) {
                return from.getCoveredText();
            }
        });

        contentList.add(id + "." + spaceJoiner.join(wordsAsString));

        contentList.add(eventMention.getEventType());

        if (withSentence) {
            contentList.add(sentence.getCoveredText().replaceAll("\n", " "));
        }

        String agentCell = getAllAgentStr(false);

        contentList.add(agentCell);

        String patientCell = getAllPatientStr(false);
        contentList.add(patientCell);

        String locationCell = getAllLocationStr(false);
        contentList.add(locationCell);

        return contentList.toArray(new String[contentList.size()]);
    }

    public String getAllAgentStr(final boolean withEntities) {
        String agentCell = "";
        if (hasAgents()) {
            List<String> agentSurfaceList = Lists.transform(agents,
                    new Function<EntityBasedComponent, String>() {
                        @Override
                        public String apply(EntityBasedComponent from) {
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));

                            String agentStr = withEntities ? (from.getCoveredText() + " : " + semiColonJoiner
                                    .join(getShortenAnnotationSurfaces(annotations))) : from.getCoveredText();

                            return agentStr;
                        }
                    });
            agentCell = slashJoiner.join(agentSurfaceList);
        }
        return agentCell.replace("\n", " ");
    }

    public String getAllPatientStr(final boolean withEntities) {
        String patientCell = "";
        if (hasPatients()) {

            List<String> patientSurfaceList = Lists.transform(patients,
                    new Function<EntityBasedComponent, String>() {
                        @Override
                        public String apply(EntityBasedComponent from) {
                            // System.out.println("from : " + from.getCoveredText());
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));

                            String str = withEntities ? (from.getCoveredText() + " : " + semiColonJoiner
                                    .join(getShortenAnnotationSurfaces(annotations))) : from.getCoveredText();

                            return str;
                        }
                    });

            // for (EntityBasedComponent p : patients) {
            // try {
            // System.out.println("Patient " + p.getCoveredText());
            // } catch (Exception e) {
            // System.out.println(event.getCoveredText());
            // System.out.println(p.getBegin() + " " + p.getEnd());
            // System.exit(1);
            // }
            // }
            // System.out.println(patientSurfaceList.size());
            // for (String p : patientSurfaceList) {
            // System.out.println("String: " + p);
            // }

            patientCell = slashJoiner.join(patientSurfaceList);
        }
        return patientCell.replace("\n", " ");
    }

    public String getAllLocationStr(final boolean withEntities) {
        String locationCell = "";
        if (hasLocations()) {
            List<String> locationSurfaceList = Lists.transform(locations,
                    new Function<Location, String>() {
                        @Override
                        public String apply(Location from) {
                            List<EntityMention> annotations = new ArrayList<EntityMention>(
                                    FSCollectionFactory.create(from.getContainingEntityMentions(),
                                            EntityMention.class));
                            String str = withEntities ? (from.getCoveredText() + " : " + semiColonJoiner
                                    .join(getShortenAnnotationSurfaces(annotations))) : from.getCoveredText();

                            return str;
                        }
                    });
            locationCell = slashJoiner.join(locationSurfaceList);
        }
        return locationCell.replace("\n", " ");
    }

    public String getAllTimeStr() {
        String timeCell = "";

        if (hasTimeAnnotations()) {
            List<String> timeSurfaceList = Lists.transform(timeAnnotations,
                    new Function<EntityBasedComponent, String>() {
                        @Override
                        public String apply(EntityBasedComponent from) {
                            return from.getCoveredText();
                        }
                    });

            timeCell = slashJoiner.join(timeSurfaceList);
        }
        return timeCell.replace("\n", " ");
    }

    private Set<String> getShortenAnnotationSurfaces(List<EntityMention> annos) {
        Set<String> surfaces = new HashSet<String>();
        for (EntityMention anno : annos) {
            EntityCoreferenceCluster cluster = ClusterUtils.getEntityFullClusterSystem(anno);
            if (cluster == null) {
                surfaces.add(anno.getCoveredText());
            } else {
                surfaces.add(getMentionTextInCluster(cluster));
            }
        }
        return surfaces;
    }

    private String getMentionTextInCluster(EntityCoreferenceCluster cluster) {
        List<String> mentionSurfaces = new ArrayList<String>();
        for (EntityMention em : FSCollectionFactory.create(cluster.getEntityMentions(),
                EntityMention.class)) {
            mentionSurfaces.add(em.getCoveredText());
        }
        return hyphenJoiner.join(mentionSurfaces);
    }

    public void setParagraph(Paragraph aParagraph) {
        paragraph = aParagraph;
    }

    public void setClause(Clause aClause) {
        clause = aClause;
    }

    public void setSentence(Sentence aSent) {
        sentence = aSent;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public void setAgents(List<EntityBasedComponentLink> agentLinks) {
        List<EntityBasedComponent> eevmAgents = new ArrayList<EntityBasedComponent>(agentLinks.size());
        for (EntityBasedComponentLink alink : agentLinks) {
            eevmAgents.add(alink.getComponent());
        }
        setAgentLinks(agentLinks);
        this.agents = eevmAgents;
    }

    public void setPatients(List<EntityBasedComponentLink> patientLinks) {
        List<EntityBasedComponent> eevmPatients = new ArrayList<EntityBasedComponent>(
                patientLinks.size());
        for (EntityBasedComponentLink plink : patientLinks) {
            eevmPatients.add(plink.getComponent());
        }
        setPatientLinks(patientLinks);
        this.patients = eevmPatients;
    }

    public void setLocations(List<EntityBasedComponentLink> locationLinks) {
        List<Location> eevmLocations = new ArrayList<Location>(locationLinks.size());
        for (EntityBasedComponentLink llink : locationLinks) {
            eevmLocations.add((Location) llink.getComponent());
        }
        setLocationLinks(locationLinks);
        this.locations = eevmLocations;
    }

    public void setTimeAnnotations(List<EntityBasedComponentLink> timeLinks) {
        List<EntityBasedComponent> eevmTimeAnnos = new ArrayList<EntityBasedComponent>(timeLinks.size());
        for (EntityBasedComponentLink tlink : timeLinks) {
            eevmTimeAnnos.add(tlink.getComponent());
        }
        setTimeAnnotations(timeLinks);
        this.timeAnnotations = eevmTimeAnnos;
    }

    public List<Word> getWords() {
        return words;
    }

    public List<EntityBasedComponent> getAgents() {
        return agents;
    }

    public List<EntityBasedComponent> getPatients() {
        return patients;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<EntityBasedComponent> getTimeAnnotations() {
        return timeAnnotations;
    }

    public int getSentenceId() {
        if (sentence != null)
            return Integer.parseInt(sentence.getSentenceId());
        else {
            logger.error("Encounter null sentence");
            return 0; // this should not happen ..
        }
    }

    public boolean isTitle() {
        return getSentenceId() == 1;
    }

    /**
     * Check how complete the information on this row is. This seems doesn't make much sense in
     * automatic process because system always try to find information for each
     *
     * @return
     */
    public float getCompleteness() {
        float score = (float) 0.0;
        if (agents != null) {
            score += 1.0;
            // System.out.println("Agent found");
        }
        if (patients != null) {
            score += 1.0;
            // System.out.println("Patient found");
        }
        // time and location can have more fine grained completeness
        if (locations != null) {
            score += 1.0;
            // System.out.println("Location found");
        }

        return score / 4;
    }

    public boolean hasLocations() {
        return locations != null && !locations.isEmpty();
    }

    public boolean hasAgents() {
        return agents != null && !agents.isEmpty();
    }

    public boolean hasPatients() {
        return patients != null && !patients.isEmpty();
    }

    public boolean hasTimeAnnotations() {
        return timeAnnotations != null && !timeAnnotations.isEmpty();
    }

    public static Comparator<EventMentionRow> completenessComparator() {
        return new Comparator<EventMentionRow>() {

            @Override
            public int compare(EventMentionRow row1, EventMentionRow row2) {
                float comp1 = row1.getCompleteness();
                float comp2 = row2.getCompleteness();
                if (comp1 > comp2)
                    return 1;
                else if (comp2 > comp1)
                    return -1;
                return 0;
            }
        };
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public Clause getClause() {
        return clause;
    }

    public EventMention getEventMention() {
        return eventMention;
    }

    public void setEventMention(EventMention eventMention) {
        this.eventMention = eventMention;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
