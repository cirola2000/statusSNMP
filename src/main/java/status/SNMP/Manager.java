package status.SNMP;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import status.properties.PropertiesFile;

public class Manager {

	static ConcurrentHashMap<String, String> nameOIDs = new ConcurrentHashMap<String, String>();

	static ConcurrentHashMap<String, Measure> measures = new ConcurrentHashMap<String, Measure>();

	static String rxOID = ".1.3.6.1.2.1.2.2.1.10.";

	static String txOID = ".1.3.6.1.2.1.2.2.1.16.";

	public static void main(String[] args) {
		try {
			SNMPManager client = new SNMPManager(PropertiesFile.SNMP_SERVER);

			// get pppoe interfaces ids
			nameOIDs = client.getOIDNames();

			for (String s : nameOIDs.keySet()) {
				System.out.println("Adding: " + s + " " + nameOIDs.get(s));
				measures.put(nameOIDs.get(s), new Measure(s));
			}

			// schedule to update the client list every 10 mins
			Thread updateThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000 * 20);
							System.out.println("Updating client list...");
							SNMPManager client = new SNMPManager("udp:molettta.no-ip.biz/161");
							nameOIDs = client.getOIDNames();

							measures = new ConcurrentHashMap<String, Measure>();
							for (String s : nameOIDs.keySet()) {
//								if (measures.get(nameOIDs.get(s)) == null) {
									System.out.println("Added: " + s + " " + nameOIDs.get(s));
									measures.put(nameOIDs.get(s), new Measure(s));
//								}
							}

						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});

			 updateThread.start();

			// schedule to update the client list every 10 mins
			Thread measureThread = new Thread(new Runnable() {
				public void run() {
					SNMPManager client = new SNMPManager("udp:molettta.no-ip.biz/161");
					while (true) {
						try {

							System.out.println("Updating...");

							// getting TX data
							OID txOIDs[] = new OID[nameOIDs.size()];
							int i = 0;

							for (String s : nameOIDs.keySet()) {
								txOIDs[i] = new OID(txOID + nameOIDs.get(s));
								i++;
							}

							ResponseEvent txResponseEvent = client.get(txOIDs);
							Iterator<VariableBinding> txIterator = txResponseEvent.getResponse().getVariableBindings()
									.iterator();

							// getting RX data
							OID rxOIDs[] = new OID[nameOIDs.size()];
							i = 0;

							for (String s : nameOIDs.keySet()) {
								rxOIDs[i] = new OID(rxOID + nameOIDs.get(s));
								i++;
							}

							ResponseEvent rxResponseEvent = client.get(rxOIDs);
							Iterator<VariableBinding> rxIterator = rxResponseEvent.getResponse().getVariableBindings()
									.iterator();

							// name iterators
							// Iterator<String> nameIterator =
							// nameOIDs.keySet().iterator();

							// assigning values to measures
							while (txIterator.hasNext()) {
								try {
									VariableBinding txVariable = txIterator.next();

									Long tx = Long.parseLong(txVariable.getVariable().toString());
									Measure measure = measures.get(getSuffix(txVariable.getOid().toString()));
									measure.setTx(tx);

								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							// assigning values to measures
							while (rxIterator.hasNext()) {
								try {
									VariableBinding rxVariable = rxIterator.next();

									Long rx = Long.parseLong(rxVariable.getVariable().toString());
									Measure measure = measures.get(getSuffix(rxVariable.getOid().toString()));
									measure.setRx(rx);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							for (Measure m : measures.values()) {
								m.setTimestamp(String.valueOf(System.currentTimeMillis()));
								m.insert(false);
							}

							// make a measure every 10 seconds
							Thread.sleep(1000 * 10);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});

			measureThread.start();

			try {
				updateThread.join();
				measureThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getSuffix(String oid) {
		int length = oid.split("\\.").length;
		return oid.split("\\.")[length - 1];
	}

}
