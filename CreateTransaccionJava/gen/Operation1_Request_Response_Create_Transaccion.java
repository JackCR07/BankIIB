package gen;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Types;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class Operation1_Request_Response_Create_Transaccion extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			if(inMessage==null){
				PrintWriter writer23 = new PrintWriter("C:/logInMesaageNull.txt", "UTF-8");
				writer23.write("entre");
				writer23.close();
			}
			else{
			
				PrintWriter writer4 = new PrintWriter("C:/logInMesaageNoNull.txt", "UTF-8");
				writer4.write("entre");
				writer4.close();
			}
		
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			PrintWriter writer1 = new PrintWriter("C:/log1sasa.txt", "UTF-8");
			writer1.write("entre");
			writer1.close();
			//*****************************Obtener datos del mensaje**********************************************
			MbElement rootElement = outMessage.getRootElement();
			PrintWriter writer3 = new PrintWriter("C:/logObtuveRootElement.txt", "UTF-8");
			writer3.write("entre");
			writer3.close();
			MbElement idCuentaOrigenMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/id_cuenta_origen");;
			MbElement numCuentaDestinoMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/numero_cuenta_destino");
			MbElement idTipoTransaccionMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/id_tipo_transaccion");
			MbElement montoTransferidoMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/monto_transferido");
			
			
			PrintWriter writer2 = new PrintWriter("C:/logObtuveDatos.txt", "UTF-8");
			writer2.write("entre");
			writer2.close();
			//****************************Convertir los datos obtenidos a string*************************************
			String idCuentaOrigenStr = idCuentaOrigenMb.getValueAsString();
			String CuentaDestinoStr = numCuentaDestinoMb.getValueAsString();
			String idTipoTransaccionStr = idTipoTransaccionMb.getValueAsString();
			String montoTransferidoStr = montoTransferidoMb.getValueAsString();
			 
			int idCuentaOrigen = Integer.parseInt(idCuentaOrigenStr);
			int idTipoTransaccion = Integer.parseInt(idTipoTransaccionStr);
			double montoTransferido = Double.parseDouble(montoTransferidoStr);
			

			//****************************Borrar los datos del mensaje de entrada****************************
			rootElement.getFirstElementByPath("/XMLNSC/operation1").delete();
			
			
			//**********************************Coneccion con la base de datos*******************************
			Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
			Connection connection = 
					DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB", "admin","Thisli07");
			
			//*****************************Preparar query a la base de datos********************************
			String query = "{call ADMIN.CREATE_TRANSACCION(?,?,?,?,?)}";
			CallableStatement cStmt = connection.prepareCall(query);
	        cStmt.setInt("ID_CUENTA_ORIGEN", idCuentaOrigen);
	        cStmt.setString("NUMERO_CUENTA_DESTINO", CuentaDestinoStr);
	        cStmt.setInt("ID_TIPO_TRANSACCION", idTipoTransaccion);
	        cStmt.setDouble("MONTO", montoTransferido);
	        
	        //*******************************Registrar parametros de output********************************
	        cStmt.registerOutParameter("ERROR_CODE", Types.INTEGER);
	        
	        //*****************************Ejecutar query a la base de datos********************************
			cStmt.execute();
			//*****************************Obtener resultado***********************************************
			int resultado = cStmt.getInt("ERROR_CODE");
			
			//*****************************Formar mensaje de retorno**************************************************
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");					
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response","");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"resultado",resultado);
			
			
			// End of user code
			// ----------------------------------------------------------
		} catch (Throwable e) {
			// Re-throw to allow Broker handling of MbException
			int errorCode = -4;//Error en coneccion con base de datos
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			MbElement rootElement = outMessage.getRootElement();
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");					
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response","");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
			DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"resultado",errorCode);
		}
		
		
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
