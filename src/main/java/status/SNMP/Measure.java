package status.SNMP;

import status.mongodb.MongoSuperClass;

public class Measure extends MongoSuperClass {

	public static String COLLECTION_NAME = "Measure";

	private static String NAME = "name";

	private static String TIMESTAMP = "timestamp";

	private static String TX = "tx";

	private static String RX = "rx";

	public String oid;

	public Measure(String name) {
		super(COLLECTION_NAME);
		setName(name);

	}

	public Measure(String name, String timestamp, Long tx, Long rx) {
		super(COLLECTION_NAME);
		setName(name);
		setTimestamp(timestamp);
		setTx(tx);
		setRx(rx);
	}

	public String getName() {
		return getField(NAME).toString();
	}

	public void setName(String name) {
		addField(NAME, name);
	}

	public String getTimestamp() {
		return getField(TIMESTAMP).toString();
	}

	public void setTimestamp(String timestamp) {
		addField(TIMESTAMP, timestamp);
	}

	public Long getTx() {
		return Long.valueOf(getField(TX).toString());
	}

	public void setTx(Long tx) {
		addField(TX, tx);
	}

	public Long getRx() {
		return Long.valueOf(getField(RX).toString());
	}

	public void setRx(Long rx) {
		addField(RX, rx);
	}

}
