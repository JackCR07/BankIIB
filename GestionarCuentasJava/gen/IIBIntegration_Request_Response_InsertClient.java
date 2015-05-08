package gen;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;

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
			MbElement cedula = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/cedula");
			MbElement nombre = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/nombre");
			MbElement apellido = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/apellido");
			MbElement direccion = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/direccion");
			MbElement fechaNacimiento = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/fechaNacimiento");
			MbElement password = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegration/Cliente/password");
			
			String cedulaCliente = cedula.getValueAsString();
			String nombreCliente = nombre.getValueAsString();
			String apellidoCliente = apellido.getValueAsString();
			String direccionCliente = direccion.getValueAsString();
			String fechaNacimientoCliente = fechaNacimiento.getValueAsString();
			String passwordCliente = password.getValueAsString();
			
			if(nombreCliente.length() >20 || direccionCliente.length()> 20){
				lengthError=true;
			}
			
			if(!lengthError){
				try{
					String query = "insert into \"Cliente\" (\"cedula\",\"nombre\", \"apellido\",\"fechaNacimiento\", \"direccion\", \"password\") " +
							"values('" + cedulaCliente + "','" + nombreCliente + "','" + apellidoCliente + "','" + fechaNacimientoCliente +"','" + direccionCliente + "',encrypt('"+password+"','12345678','12345678' ))";
					//Coneccion con base de datos
					Class. forName ( "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver" ); 
			        Connection  connection = 
			                DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB","admin","Thisli07"); 
			        
			        connection.prepareStatement(query).executeUpdate();
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
			String command = "cmd.exe /C c:\\IBM\\WebSphere\\wp_profile1\\bin\\wsadmin -lang jython -user admin -password admin -c \"AdminTask.createUser ('[-uid "+nombreCliente+" -password "+passwordCliente+" -confirmPassword "+passwordCliente+" -cn na -sn "+apellidoCliente+" -mail na]')\"";
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
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/IIBIntegrationResponse/Resultado/Cliente");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"nombre",nombreCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"apellido",apellidoCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"cedula",cedulaCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"direccion",direccionCliente);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"fechaNacimiento",fechaNacimientoCliente);
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
