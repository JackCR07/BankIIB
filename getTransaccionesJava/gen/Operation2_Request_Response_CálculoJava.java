package gen;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.sql.Date;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;


public class Operation2_Request_Response_CálculoJava extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ***************************Inicio de codigo del usuario********************************************
			
			//*****************************Obtener datos del mensaje**********************************************
			MbElement rootElement = outMessage.getRootElement();
			MbElement idCuentaMb = rootElement.getFirstElementByPath("/XMLNSC/operation2InputParameter1/idCliente");;
			MbElement idTipoTransaccionMb = rootElement.getFirstElementByPath("/XMLNSC/operation2InputParameter1/idTipoTransaccion");
			
			//****************************Convertir los datos obtenidos a string*************************************
			String idCuentaStr = idCuentaMb.getValueAsString();
			String idTipoTransaccionStr = idTipoTransaccionMb.getValueAsString();
			
			
			//***************************Convertir los datos a sus tipos respectivos**************************
			int idCuenta = Integer.parseInt(idCuentaStr);
			int idTipoTransaccion = Integer.parseInt(idTipoTransaccionStr);
			
			
			
		
			//****************************Borrar los datos del mensaje de entrada****************************
			rootElement.getFirstElementByPath("/XMLNSC/operation2InputParameter1").delete();
			
			
			//**********************************Coneccion con la base de datos*******************************
			Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
			Connection connection = 
					DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB", "admin","Thisli07");
			
			//*****************************Preparar query a la base de datos********************************
			String query = "{call ADMIN.SumaTransacciones(?,?)}";
			CallableStatement cStmt = connection.prepareCall(query);
	        cStmt.setInt("ID_CUENTA", idCuenta);
	        cStmt.setInt("ID_TIPO_TRANSACCION", idTipoTransaccion);
	        
	        //*****************************Ejecutar query a la base de datos********************************
			cStmt.execute();
			
			
			//*****************************Obtener resulset*************************************************
			ResultSet rs = cStmt.getResultSet();

			//****************************Procesar resulset*************************************************
			if (rs != null) {
				MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");
				DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation2OutputParameter1", "");
				DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation2OutputParameter1");
				rs.next();
				DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"Debito", rs.getInt("debito"));
				DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Credito", rs.getInt("credito"));
				DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Resultado", 0);
				
			}

			
		} catch (Exception s) {
			int errorCode=-1;
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ***************************Inicio de codigo del usuario********************************************
			
			//*****************************Obtener datos del mensaje**********************************************
			MbElement rootElement = outMessage.getRootElement();
			rootElement.getFirstElementByPath("/XMLNSC/operation2InputParameter1").delete();
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation2OutputParameter1", "");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation2OutputParameter1");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"Debito", 0);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Credito", 0);
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Resultado", -1);
			
			//Creacion de log
			try {
				PrintWriter writer2;
				writer2 = new PrintWriter("C:/logGetTransacciones.txt", "UTF-8");
				writer2.println(s);
				writer2.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			// End of user code
		
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
