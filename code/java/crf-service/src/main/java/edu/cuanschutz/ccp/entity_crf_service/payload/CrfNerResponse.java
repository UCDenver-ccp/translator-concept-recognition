package edu.cuanschutz.ccp.entity_crf_service.payload;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import lombok.Data;

@Data
public class CrfNerResponse {

	private Map<String, String> docIdToBionlpEntityAnnotationsMap;

	/**
	 * @param entityDocuments text document with only entity annotations
	 */
	public CrfNerResponse(List<TextAnnotation> entityAnnots) {
		super();

		docIdToBionlpEntityAnnotationsMap = new HashMap<String, String>();

		Map<String, Collection<TextAnnotation>> docIdToAnnotMap = organizeByDocId(entityAnnots);

		for (Entry<String, Collection<TextAnnotation>> entry : docIdToAnnotMap.entrySet()) {
			int annotId = 0;
			StringBuilder sb = new StringBuilder();
			for (TextAnnotation annot : entry.getValue()) {
				sb.append(serializeAnnotation(annot, String.format("T%d", annotId++)) + "\n");
			}
			docIdToBionlpEntityAnnotationsMap.put(entry.getKey(), sb.toString());
		}
	}

	private Map<String, Collection<TextAnnotation>> organizeByDocId(List<TextAnnotation> entityAnnots) {
		Map<String, Collection<TextAnnotation>> docIdToAnnotMap = new HashMap<String, Collection<TextAnnotation>>();
		for (TextAnnotation annot : entityAnnots) {
			CollectionsUtil.addToOne2ManyMap(annot.getDocumentID(), annot, docIdToAnnotMap);
		}
		return docIdToAnnotMap;
	}

	private static String serializeAnnotation(TextAnnotation ta, String annotId) {
		StringBuffer sb = new StringBuffer();
		sb.append(annotId + "\t");
		String annotType = ta.getClassMention().getMentionName();
		/*
		 * BioNLP format does not support spaces in the annotation type, so replace all
		 * spaces in annotType with SPACE_PLACEHOLDER
		 */
		annotType = annotType.replaceAll(" ", "^");
		sb.append(annotType + " ");
		for (int i = 0; i < ta.getSpans().size(); i++) {
			Span span = ta.getSpans().get(i);
			sb.append(((i > 0) ? ";" : "") + span.getSpanStart() + " " + span.getSpanEnd());
		}
		sb.append("\t" + ta.getCoveredText().replaceAll("\\n", " "));

		return sb.toString();
	}

}
