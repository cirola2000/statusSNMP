package status.SNMP;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public class SNMPManager {

	Snmp snmp = null;
	String address = null;

	/**
	 * Constructor
	 * 
	 * @param add
	 */
	public SNMPManager(String add) {
		address = add;
		try {
			start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		/**
		 * Port 161 is used for Read and Other operations Port 162 is used for
		 * the trap generation
		 */
		SNMPManager client = new SNMPManager("udp:molettta.no-ip.biz/161");
		client.start();
		// Address targetAddress =
		// GenericAddress.parse("udp:molettta.no-ip.biz/161");
		// TransportMapping transport = new DefaultUdpTransportMapping();
		// Snmp snmp = new Snmp(transport);
		// transport.listen();

		// // setting up target
		// CommunityTarget target = new CommunityTarget();
		// target.setCommunity(new OctetString("public"));
		// target.setAddress(targetAddress);
		// target.setRetries(3);
		// target.setTimeout(1000 * 3);
		// target.setVersion(SnmpConstants.version1);

		String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.2.2.1.10.8505001"));
		System.out.println(sysDescr);
		// OID oi = new OID(".1.3.6.1.2.1.2.2.1.2.");
		// PDU p = new PDU();

		// ArrayList<String> results = new ArrayList<String>();
		// OID oid = new OID(".1.3.6.1.2.1.2.2.1.2.");
		//
		// TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		// List<TreeEvent> events = treeUtils.getSubtree(target, oid);
		// if (events == null || events.size() == 0) {
		// System.out.println("No result returned.");
		// System.exit(1);
		// }
		// // Get snmpwalk result.
		// for (TreeEvent event : events) {
		// if (event != null) {
		// if (event.isError()) {
		// System.err.println("oid [" + oid + "] " + event.getErrorMessage());
		// }
		//
		// VariableBinding[] varBindings = event.getVariableBindings();
		// if (varBindings == null || varBindings.length == 0) {
		// System.out.println("No result returned.");
		// }
		// for (VariableBinding varBinding : varBindings) {
		// System.out.println(varBinding.getOid().toString());
		// System.out.println(varBinding.getVariable().toString());
		// // results.add(varBinding.getVariable().toString());
		// }
		// }
		// }
		//
		// // print results
		// for (String value : results) {
		// System.out.println(value);
		// }
		// snmp.close();
	}

	/**
	 * Start the Snmp session. If you forget the listen() method you will not
	 * get any answers because the communication is asynchronous and the
	 * listen() method listens for answers.
	 * 
	 * @throws IOException
	 */
	private void start() throws IOException {
		TransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		// Do not forget this line!
		transport.listen();
	}

	public ConcurrentHashMap<String, String> getOIDNames() throws IOException {

		ConcurrentHashMap<String, String> result = new ConcurrentHashMap<String, String>();

		Address targetAddress = GenericAddress.parse(address);

		// setting up target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(1000 * 3);
		target.setVersion(SnmpConstants.version1);

		OID oid = new OID(".1.3.6.1.2.1.2.2.1.2.");

		TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(target, oid);
		if (events == null || events.size() == 0) {
			System.out.println("No result returned.");
			System.exit(1);
		}
		// Get snmpwalk result.
		for (TreeEvent event : events) {
			if (event != null) {
				if (event.isError()) {
					System.err.println("oid [" + oid + "] " + event.getErrorMessage());
				}

				VariableBinding[] varBindings = event.getVariableBindings();
//				if (varBindings == null || varBindings.length == 0) {
//					System.out.println("No result returned.");
//					System.out.println(varBindings.length); 
//				}
				for (VariableBinding varBinding : varBindings) {
					if (varBinding.getVariable().toString().contains("pppoe"))
						result.put(varBinding.getVariable().toString(), varBinding.getOid().toString().replace("1.3.6.1.2.1.2.2.1.2.", ""));
				}
			}
		}

		snmp.close();

		return result;

	}

	/**
	 * Method which takes a single OID and returns the response from the agent
	 * as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws IOException
	 */
	public String getAsString(OID oid) throws IOException {
		ResponseEvent event = get(new OID[] { oid });
		return event.getResponse().get(0).getVariable().toString();
	}

	/**
	 * This method is capable of handling multiple OIDs
	 * 
	 * @param oids
	 * @return
	 * @throws IOException
	 */
	public ResponseEvent get(OID oids[]) throws IOException {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);
		ResponseEvent event = snmp.send(pdu, getTarget(), null);
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}

	/**
	 * This method returns a Target, which contains information about where the
	 * data should be fetched and how.
	 * 
	 * @return
	 */
	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

}