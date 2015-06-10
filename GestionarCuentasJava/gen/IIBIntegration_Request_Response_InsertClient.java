package gen;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;


public class IIBIntegration_Request_Response_InsertClient extends
		MbJavaComputeNode {
 
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		
		String nombreCliente="";
		String usuarioCliente="";
		
		try { 
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			MbElement rootElement = outMessage.getRootElement();
			// ----------------------------------------------------------
			// Add user code below
			
			boolean lengthError=false;
			boolean DatabaseError=false;
			boolean huboError=false;
			int estado= 0; // Indica si el resultado fue satisfactorio
			//Obtener datos para el query a la base de datos
			MbElement password = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/password");
			MbElement cedula = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/cedula");
			
			String passwordCliente = password.getValueAsString();
			String cedulaCliente = cedula.getValueAsString();
			
			
			if(!lengthError){
				try{
					String query ="update \"Cliente\" set \"password\" = encrypt('"+password+"','12345678','12345678' )"+
							"where \"cedula\" ="+cedulaCliente;
					//Coneccion con base de datos
					Class. forName ( "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver" ); 
			        Connection  connection = 
			                DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB","admin","Thisli07"); 
			        
			        connection.prepareStatement(query).executeUpdate();
			        query="select C.\"pk_id_cliente\", \"nombre\" from \"Cliente\" C WHERE C.\"cedula\"="+cedulaCliente;
			        CallableStatement cStmt = connection.prepareCall(query);
			        cStmt.execute();
			        ResultSet rs = cStmt.getResultSet();
			        if(rs.next()){
				        nombreCliente = rs.getString("nombre");
				        usuarioCliente = rs.getString("pk_id_cliente");
			        	
			        }
			        /*query = "insert into \"Cuenta\" (\"saldo\", \"fecha_creacion\",\"pk_id_tipo_cuenta\", \"pk_id_cliente\") " +
							"values('" + 240000.0 + "','" + "2015-05-28" + "','" + 1 + "','" + cedulaCliente+"))";
			        connection.prepareStatement(query).executeUpdate();*/
					
				}
				catch(Exception s){
					DatabaseError=true;
					PrintWriter writer = new PrintWriter("C:/log.txt", "UTF-8");
					writer.println(s);
					writer.close();
				}
				
			}
			
			//Evaluación de errores
			huboError = lengthError || DatabaseError;
			if(!huboError)
				estado=1;
			
			try
			{
			String command = "cmd.exe /C c:\\IBM\\WebSphere\\wp_profile1\\bin\\wsadmin -lang jython -user admin -password admin -c \"AdminTask.createUser ('[-uid "+usuarioCliente+" -password "+passwordCliente+" -confirmPassword "+passwordCliente+" -cn "+nombreCliente+" -sn na -mail na]')\"";
			Process p=Runtime.getRuntime().exec(command);
			}catch(IOException e){}
			rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration").delete();
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");					
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"IIBIntegrationResponse","");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegrationResponse");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"xmlns","http://REINTRG");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Resultado","");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegrationResponse/Resultado");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Cliente","");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"estado",estado);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"nombreCliente",nombreCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"usuarioCliente",usuarioCliente);
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegrationResponse/Resultado/Cliente");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"cedula",cedulaCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"password","********");
			
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
