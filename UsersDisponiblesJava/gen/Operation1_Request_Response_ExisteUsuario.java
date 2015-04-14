package gen;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class Operation1_Request_Response_ExisteUsuario extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			
			MbElement rootElement = outMessage.getRootElement();
			
			MbElement nombre = rootElement.getFirstElementByPath("/XMLNSC/operation1/cedulaCliente");
			String nombreCliente = nombre.getValueAsString();
			Boolean DatabaseError;
			int cantidad=0;
			try{		     
				String query = "call existClient('"+ nombreCliente +"',?)";
				//Coneccion con base de datos
				Class. forName ( "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver" ); 
		        Connection  connection = 
		                DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB","admin","Thisli07"); 
		        CallableStatement cStmt = connection.prepareCall(query);
		        cStmt.registerOutParameter("CANTIDAD", Types.INTEGER);
		        cStmt.execute();
		        cantidad = cStmt.getInt("CANTIDAD");
			}
			catch(Exception s){
				DatabaseError=true;
				PrintWriter writer = new PrintWriter("C:/log.txt", "UTF-8");
				writer.println(s);
				writer.close();
			}
			rootElement.getFirstElementByPath("/XMLNSC/operation1").delete();
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");					
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response","");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"xmlns","http://REINTRG");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"resultado",cantidad);
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegrationResponse/Resultado");
			
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
