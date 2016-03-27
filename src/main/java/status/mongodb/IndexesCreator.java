package status.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class IndexesCreator {
	public void createIndexes(){
		
//		// indexes for datasets
//		addIndex(WordDB.COLLECTION_NAME, WordDB.WORD, 1);
//
//		addIndex(SentenceDB.COLLECTION_NAME, SentenceDB.SENTENCE, 1);
//		
//		addIndex(VideoDB.COLLECTION_NAME, VideoDB.VIDEO_EXTERNAL_ID, 1);
//		
//		addIndex(WordSentenceRelationDB.COLLECTION_NAME, WordSentenceRelationDB.SENTENCE_ID, 1);
//		addIndex(WordSentenceRelationDB.COLLECTION_NAME, WordSentenceRelationDB.WORD_ID, 1);
//		addIndex(WordSentenceRelationDB.COLLECTION_NAME, WordSentenceRelationDB.POS_ID, 1);
//
//		addIndex(LemmaDB.COLLECTION_NAME, LemmaDB.LEMMA, 1);
//		
//		addIndex(POSDB.COLLECTION_NAME, POSDB.POS, 1);
		
	}
	
	public void addIndex(String collection, String field, int value){
		DBObject indexOptions = new BasicDBObject();
		indexOptions.put(field, value);
		MongoSuperClass.getCollection(collection).createIndex(indexOptions ); 			
		
	}
}
