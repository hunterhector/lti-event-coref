package edu.cmu.lti.event_coref.model;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.HashMultimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.EventMentionUtils;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.uimafit.util.JCasUtil;

import java.util.*;
import java.util.Map.Entry;

public class EventMentionTable {
	private Map<EventMention, EventMentionRow> sudokuTable;

	private Map<EventMention, EventMentionRow> domainOnlySudokuTable;

	private int numOfNonEpistemicdDomainEvents = 0;

	public EventMentionTable(JCas aJCas) {
		this(aJCas, "event"); // use a default value here, but hard code is very
								// bad
	}

	public EventMentionTable(JCas aJCas, String domainEventType) {
		AnnotationCondition basicCondition = new AnnotationCondition() {
			@Override
			public Boolean check(TOP aAnnotation) {
				ComponentAnnotation anno = (ComponentAnnotation) aAnnotation;
				return anno.getBegin() > 0 && anno.getEnd() > 0;
			}
		};

		List<EventMention> allEvents = UimaConvenience
				.getAnnotationListWithFilter(aJCas, EventMention.class,
						basicCondition);

		sudokuTable = new LinkedHashMap<EventMention, EventMentionRow>();
		domainOnlySudokuTable = new LinkedHashMap<EventMention, EventMentionRow>();

		int count = 0;
		for (EventMention eevm : allEvents) {
			// System.out.println("Creating row for " + eevm.getCoveredText());
			EventMentionRow skdr = new EventMentionRow(eevm);
			sudokuTable.put(eevm, skdr);

			if (EventMentionUtils.isTargetEventType(eevm, domainEventType)) {
				if (eevm.getBegin() >= 0 && eevm.getEnd() > 0) {
					domainOnlySudokuTable.put(eevm, skdr);
					numOfNonEpistemicdDomainEvents++;
				}
			}

			List<Word> words = JCasUtil.selectCovered(aJCas, Word.class, eevm);
			skdr.setWords(words);
			skdr.setId(count);
			count++;
		}

		List<Paragraph> paraList = UimaConvenience.getAnnotationList(aJCas,
				Paragraph.class);

		if (paraList.size() == 0) {
			for (Sentence sent : JCasUtil.select(aJCas, Sentence.class)) {
				Paragraph para = new Paragraph(aJCas, sent.getBegin(),
						sent.getEnd());
				para.addToIndexes();
				para.setComponentId("System_sentence_as_paragraph");
			}

			paraList = UimaConvenience
					.getAnnotationList(aJCas, Paragraph.class);
		}

		// link discourse structures to sudoku row
		for (Paragraph para : paraList) {
			for (EventMention eleEvmInPara : JCasUtil.selectCovered(aJCas,
					EventMention.class, para)) {
				if (sudokuTable.containsKey(eleEvmInPara)) {
					sudokuTable.get(eleEvmInPara).setParagraph(para);
				}
			}
			List<Sentence> sentencesCoveredByPara = JCasUtil.selectCovered(
					aJCas, Sentence.class, para);
			for (Sentence sent : sentencesCoveredByPara) {
				for (EventMention eleEvmInSent : JCasUtil.selectCovered(aJCas,
						EventMention.class, sent)) {
					if (sudokuTable.containsKey(eleEvmInSent)) {
						sudokuTable.get(eleEvmInSent).setSentence(sent);
					}
				}
				// note clause currently does not give full coverage
				List<Clause> clausesCoveredBySent = JCasUtil.selectCovered(
						aJCas, Clause.class, sent);
				for (Clause cl : clausesCoveredBySent) {
					for (EventMention eleEvm : JCasUtil.selectCovered(aJCas,
							EventMention.class, cl)) {
						if (sudokuTable.containsKey(eleEvm)) {
							sudokuTable.get(eleEvm).setClause(cl);
						}
					}
				}
			}
		}
	}

	public Map<EventMention, EventMentionRow> getTableView() {
		return sudokuTable;
	}

	public Map<EventMention, EventMentionRow> getDomainOnlyTableView() {
		return domainOnlySudokuTable;
	}

	public List<EventMention> getDomainEventView() {
		return new ArrayList<EventMention>(domainOnlySudokuTable.keySet());
	}

	public EventMentionRow[] getRowView() {
		int numOfEvents = sudokuTable.size();
		EventMentionRow[] allRows = sudokuTable.values().toArray(
				new EventMentionRow[numOfEvents]);
		return allRows;
	}

	public EventMentionRow[] getDomainOnlyRowView() {
		int numOfEvents = domainOnlySudokuTable.size();
		EventMentionRow[] allRows = domainOnlySudokuTable.values().toArray(
				new EventMentionRow[numOfEvents]);
		return allRows;
	}

	public int getNumOfNonEpistemicdDomainEvents() {
		return numOfNonEpistemicdDomainEvents;
	}

	public int getNumOfEvents() {
		return sudokuTable.size();
	}

	public void writeSudokuTableAsCSV(CSVWriter bw, String articleTitle,
			boolean withSentence) {
		String[] header;
		if (withSentence)
			header = new String[] { "Event", "Type", "Sentence", "Agents",
					"Patients", "Locations", "Time" };
		else
			header = new String[] { "Event", "Type", "Agents", "Patients",
					"Locations", "Time" };

		bw.writeNext(new String[] { articleTitle });
		bw.writeNext(header);

		for (EventMentionRow row : sudokuTable.values()) {
			bw.writeNext(row.toStringArray(withSentence));
		}
	}

	public void writeDomainSudokuTableAsSemanticDatabaseRequiredCSV(
			CSVWriter bw, int docId, String articleTitle, boolean withSentence) {
		for (EventMentionRow row : domainOnlySudokuTable.values()) {
			bw.writeNext(row.toSemanticDatabaseRequiredStringArray(
					withSentence, articleTitle, docId));
		}
	}

	public static HashMultimap<Word, EntityMention> getWord2Entities(JCas aJCas) {
		// map from words to the list of surface form of its entity mentions
		HashMultimap<Word, EntityMention> word2EntityMentions = HashMultimap
				.create();
		for (EntityMention em : UimaConvenience.getAnnotationList(aJCas,
				EntityMention.class)) {
			List<Word> coveredWords = JCasUtil.selectCovered(Word.class, em);
			for (Word coveredWord : coveredWords) {
				word2EntityMentions.put(coveredWord, em);
			}
		}
		return word2EntityMentions;
	}

	public static Comparator<Entry<EventMention, EventMentionRow>> getCompletenessComparator() {
		return new Comparator<Entry<EventMention, EventMentionRow>>() {
			public int compare(Entry<EventMention, EventMentionRow> entry1,
					Entry<EventMention, EventMentionRow> entry2) {
				return EventMentionRow.completenessComparator().compare(
						entry1.getValue(), entry2.getValue());
			}
		};
	}

}
