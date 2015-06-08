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

public class Operation1_Request_Response_JavaCompute_Get_Transacciones extends
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
			// ***************************Inicio de codigo del usuario********************************************
			
			//*****************************Obtener datos del mensaje**********************************************
			MbElement rootElement = outMessage.getRootElement();
			MbElement idCuentaMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/id_cuenta");;
			MbElement idTipoTransaccionMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/id_tipo_transaccion");
			MbElement fechaMovMayorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/fecha_mov_mayor_a");
			MbElement fechaMovMenorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/fecha_mov_menor_a");
			MbElement monTransMayorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/mon_trans_mayor_a");
			MbElement monTransMenorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/mon_trans_menor_a");
			MbElement nTransMayorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/n_trans_mayor_a");
			MbElement nTransMenorAMb = rootElement.getFirstElementByPath("/XMLNSC/operation1/n_trans_menor_a");
			
			//****************************Convertir los datos obtenidos a string*************************************
			String idCuentaStr = idCuentaMb.getValueAsString();
			String idTipoTransaccionStr = idTipoTransaccionMb.getValueAsString();
			String fechaMovMayorAStr = fechaMovMayorAMb.getValueAsString();
			String fechaMovMenorAStr = fechaMovMenorAMb.getValueAsString();
			String monTransMayorAStr = monTransMayorAMb.getValueAsString();
			String monTransMenorAStr = monTransMenorAMb.getValueAsString();
			String nTransMayorAStr = nTransMayorAMb.getValueAsString();
			String nTransMenorAStr = nTransMenorAMb.getValueAsString();
			
			
			//***************************Convertir los datos a sus tipos respectivos**************************
			int idCuenta = Integer.parseInt(idCuentaStr);
			int idTipoTransaccion = Integer.parseInt(idTipoTransaccionStr);
			Date fechaMovMayorA = fechaMovMayorAStr.equals("null") ?  null : Date.valueOf(fechaMovMayorAStr);
			Date fechaMovMenorA = fechaMovMenorAStr.equals("null") ?  null : Date.valueOf(fechaMovMenorAStr);
			double monTransMayorA = Double.parseDouble(monTransMayorAStr);
			double monTransMenorA = Double.parseDouble(monTransMenorAStr);
			int nTransMayorA = Integer.parseInt(nTransMayorAStr);
			int nTransMenorA = Integer.parseInt(nTransMenorAStr);
			
			
			
		
			//****************************Borrar los datos del mensaje de entrada****************************
			rootElement.getFirstElementByPath("/XMLNSC/operation1").delete();
			
			
			//**********************************Coneccion con la base de datos*******************************
			Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
			Connection connection = 
					DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB", "admin","Thisli07");
			
			//*****************************Preparar query a la base de datos********************************
			String query = "{call ADMIN.GET_TRANSACCIONES_BY_ID_CUENTA_OPTIONALS(?,?,?,?,?,?,?,?)}";
			CallableStatement cStmt = connection.prepareCall(query);
	        cStmt.setInt("ID_CUENTA", idCuenta);
	        cStmt.setInt("ID_TIPO_TRANSACCION", idTipoTransaccion);
	        cStmt.setDate("FECHA_MOV_MAYOR_A",  fechaMovMayorA);
	        cStmt.setDate("FECHA_MOV_MENOR_A", fechaMovMenorA);
	        cStmt.setDouble("MONTO_TRANS_MAYOR_A", monTransMayorA);
	        cStmt.setDouble("MONTO_TRANS_MENOR_A", monTransMenorA);
	        cStmt.setInt("NUM_TRANS_MAYOR_A", nTransMayorA);
	        cStmt.setInt("NUM_TRANS_MENOR_A", nTransMenorA);
	        
	        //*****************************Ejecutar query a la base de datos********************************
			cStmt.execute();
			
			
			//*****************************Obtener resulset*************************************************
			ResultSet rs = cStmt.getResultSet();

			//****************************Procesar resulset*************************************************
			if (rs != null) {
				MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");
				DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response", "");
				DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
				DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"resultado", 0);
				DataElement.createElementAsLastChild(MbElement.TYPE_NAME,"Transacciones", "");
				int i=0;
				while (rs.next()) {
					
					int resIdTransaccion = rs.getInt("PK_ID_TRANSACCION");
					String resNumeroTransaccion = rs.getString("NUMERO_TRANSACCION");
					double resMonto = rs.getDouble("MONTO");
					Date resFechaTransaccion = rs.getDate("FECHA_TRANSACCION");
					String resTipoTransaccion = rs.getString("TIPO_TRANSACCION");
					int resIdCuentaOrigen = rs.getInt("ID_CUENTA_ORIGEN");
					int resnumCuentaOrigen = rs.getInt("NUM_CUENTA_ORIGEN");
					int resIdCuentaDestino = rs.getInt("ID_CUENTA_DESTINO");
					int resnumCuentaDestino = rs.getInt("NUM_CUENTA_DESTINO");
					String resMoneda = rs.getString("MONEDA");
					//Evaluar transaccion para saber si es solicitada y para averiguar el tipo
					boolean creditosSolicitados=false;
					boolean debitosSolicitados=false;
					boolean esCredito=false;
					boolean esDebito=false;
					boolean transaccionSolicitada=false;
					if(idTipoTransaccion ==1)
						creditosSolicitados=true;
					else if(idTipoTransaccion==2)
						debitosSolicitados=true;
					else if(idTipoTransaccion==-1){
						creditosSolicitados=true;
						debitosSolicitados=true;
					}
					if(idCuenta==resIdCuentaOrigen){
						esCredito=true;
						resTipoTransaccion="Credito";
					}
					else if(idCuenta==resIdCuentaDestino){
						esDebito=true;
						resTipoTransaccion="Debito";
					}
					if((esCredito && creditosSolicitados) || (esDebito && debitosSolicitados))
						transaccionSolicitada=true;
					if(transaccionSolicitada){
						DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
						DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"transaccion", "");
						DataElement =  rootElement.getFirstElementByPath("/XMLNSC/operation1Response/transaccion");
						DataElement.createElementAsFirstChild(MbElement.TYPE_NAME, "id_transaccion", resIdTransaccion);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "numero_transaccion",resNumeroTransaccion);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "moneda",resMoneda);
						
						if(esCredito && creditosSolicitados)//Credito
						{
							DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "credito", resMonto+"");
							DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "debito", "");
						}
						else if(esDebito && debitosSolicitados)//Debito
						{
							DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "credito", "");
							DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "debito", resMonto+"");
						}
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "fecha_transaccion",resFechaTransaccion.toString());
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "tipo_transaccion",resTipoTransaccion);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "id_cuenta_origen",resIdCuentaOrigen);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "num_cuenta_origen",resnumCuentaOrigen);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "id_cuenta_destino",resIdCuentaDestino);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "num_cuenta_destino",resnumCuentaDestino);
					}
					i++;
				}
			}

			
		} catch (Exception s) {
			int errorCode=-1;
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ***************************Inicio de codigo del usuario********************************************
			
			//*****************************Obtener datos del mensaje**********************************************
			MbElement rootElement = outMessage.getRootElement();
			MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response", "");
			DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"xmlns", "http://REINTRG");
			DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"resultado", errorCode);
			
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
