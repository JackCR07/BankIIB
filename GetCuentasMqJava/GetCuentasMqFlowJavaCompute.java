

import java.io.PrintWriter;
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

public class GetCuentasMqFlowJavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		
		try {
			try {
				
				
				// create new message as a copy of the input
				MbMessage outMessage = new MbMessage(inMessage);
				outAssembly = new MbMessageAssembly(inAssembly, outMessage);
				// ----------------------------------------------------------
				// Add user code below
				MbElement rootElement = outMessage.getRootElement();
				
				
				//*****************************Obtener datos del mensaje******************************************
				MbElement idClienteMb = rootElement.getFirstElementByPath("/XMLNSC/datos/id_cliente");
				MbElement idTipoCuentaMb = rootElement.getFirstElementByPath("/XMLNSC/datos/id_tipo_cuenta");
				MbElement monedaMb = rootElement.getFirstElementByPath("/XMLNSC/datos/moneda");
				MbElement fechaMayorAMb = rootElement.getFirstElementByPath("/XMLNSC/datos/fecha_mayor_a");
				MbElement fechaMenorAMb = rootElement.getFirstElementByPath("/XMLNSC/datos/fecha_menor_a");
				MbElement saldoMayorAMb = rootElement.getFirstElementByPath("/XMLNSC/datos/saldo_mayor_a");
				MbElement saldoMenorAMb = rootElement.getFirstElementByPath("/XMLNSC/datos/saldo_menor_a");
				
				
				//****************************Convertir los datos obtenidos a string******************************
				String idClienteStr = idClienteMb.getValueAsString();
				String idTipoCuentaStr = idTipoCuentaMb.getValueAsString();
				String monedaStr = monedaMb.getValueAsString();
				String fechaMayorAStr = fechaMayorAMb.getValueAsString();
				String fechaMenorAStr = fechaMenorAMb.getValueAsString();
				String saldoMayorAStr = saldoMayorAMb.getValueAsString();
				String saldoMenorAStr = saldoMenorAMb.getValueAsString();
				
				//***************************Convertir los datos a sus tipos respectivos**************************
				String idCliente = !idClienteStr.equals("-1") ? idClienteStr: "-1";
				int idTipoCuenta = !idTipoCuentaStr.equals("-1") ? Integer.parseInt(idTipoCuentaStr): -1;
				String moneda = monedaStr.equals("null") ?  null : monedaStr;
				Date fechaMayorA = fechaMayorAStr.equals("null") ?  null : Date.valueOf(fechaMayorAStr);
				Date fechaMenorA = fechaMenorAStr.equals("null") ?  null : Date.valueOf(fechaMenorAStr);
				double saldoMayorA = !saldoMayorAStr.equals("-1") ? Double.parseDouble(saldoMayorAStr): -1;
				double saldoMenorA = !saldoMenorAStr.equals("-1") ? Double.parseDouble(saldoMenorAStr): -1;
				
				//****************************Borrar los datos del mensaje de entrada****************************
				rootElement.getFirstElementByPath("/XMLNSC/datos").delete();
				
				
				//**********************************Coneccion con la base de datos*******************************
				Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
				Connection connection = 
						DriverManager.getConnection("jdbc:db2://172.16.11.225:50000/IIBDB", "admin","Thisli07");
				PrintWriter writer1 = new PrintWriter("C:/log1sasa.txt", "UTF-8");
				writer1.write("conecte con base");
				writer1.close();
				//*****************************Preparar query a la base de datos********************************
				String query = "{call ADMIN.GET_CUENTAS_BY_ID_CLIENTE_OPTIONALS_6ARGS(?,?,?,?,?,?,?)}";
				CallableStatement cStmt = connection.prepareCall(query);
		        cStmt.setString("ID_CLIENTE", idCliente);
		        cStmt.setString("MONEDA", moneda);
		        cStmt.setInt("ID_TIPO_CUENTA", idTipoCuenta);
		        cStmt.setDate("FECHA_MAYOR_A",  fechaMayorA);
		        cStmt.setDate("FECHA_MENOR_A", fechaMenorA);
		        cStmt.setDouble("SALDO_MAYOR_A", saldoMayorA);
		        cStmt.setDouble("SALDO_MENOR_A", saldoMenorA);
		        
		        //*****************************Ejecutar query a la base de datos********************************
				cStmt.execute();
				
				
				//*****************************Obtener resulset*************************************************
				ResultSet rs = cStmt.getResultSet();
				
				//****************************Procesar resulset*************************************************
				if (rs != null) {
					MbElement DataElement = rootElement.getFirstElementByPath("/XMLNSC");
					DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"operation1Response", "");
					DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
					int i=0;
					while (rs.next()) {
						int resIdCuenta = rs.getInt("PK_ID_CUENTA");
						String resNumeroCuenta = rs.getString("NUMERO_CUENTA");
						double resSaldo = rs.getDouble("SALDO");
						Date resFechaCreacion = rs.getDate("FECHA_CREACION");
						String resTipoCuenta = rs.getString("TIPO_CUENTA");
						String resMoneda = rs.getString("MONEDA");
						DataElement = rootElement.getFirstElementByPath("/XMLNSC/operation1Response");
						DataElement.createElementAsFirstChild(MbElement.TYPE_NAME,"cuenta", "");
						DataElement =  rootElement.getFirstElementByPath("/XMLNSC/operation1Response/cuenta");
						DataElement.createElementAsFirstChild(MbElement.TYPE_NAME, "id_cuenta", resIdCuenta);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "numero_cuenta",resNumeroCuenta);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "moneda",resMoneda);
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "saldo", resSaldo+"");
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "fecha_creacion",resFechaCreacion.toString());
						DataElement.createElementAsLastChild(MbElement.TYPE_NAME, "tipo_cuenta",resTipoCuenta);
						i++;
					}
				}

				
			} catch (Throwable s) {
				PrintWriter writer2 = new PrintWriter("C:/log2.txt", "UTF-8");
				writer2.println(s);
				writer2.close();
			}
			// End of user code
			// ----------------------------------------------------------

		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be
			// handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
