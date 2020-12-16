package DeLuca.serverHTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ClientHandler implements Runnable{
	private final String pathToFiles = Paths.get(".").toAbsolutePath()+"/src/main/java/DeLuca/serverHTTP/files/";
	private Socket socket;
	private String requestedMethod;
	private String requestedPath;
	private OutputStream clientOutput;
	
	public ClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
	        
			StringBuilder requestBuilder = new StringBuilder();
	        String line;
	        while (!(line = br.readLine()).isEmpty()) {
	            requestBuilder.append(line + "\r\n");
	        }

	        String request = requestBuilder.toString();
	        
	        String[] requestsLines = request.split("\r\n");
	        String[] requestLine = requestsLines[0].split(" ");
	        requestedMethod = requestLine[0];
	        requestedPath = requestLine[1];
	        
	        if(requestedMethod.equals("GET")) {
	        	if(requestedPath.equals("/") || requestedPath.equals("/index.html")) 
					sendResponse("200 OK", "text/html", getFileLength("index.html"),getFileContent("index.html"));
	        	
		        else if(requestedPath.equals("/es/i.html") || requestedPath.equals("/es/"))
		        	sendResponse("200 OK", "text/html", getFileLength("es/i.html"), getFileContent("es/i.html"));
		        
		        else if(!requestedPath.contains(".")) 
		        	redirect(requestedPath);
	        	
		        else if(requestedPath.equals("/punti-vendita.xml")) {
		        	jsonToXml("punti-vendita");
		        	sendResponse("200 OK", "text/xml", getFileLength("punti-vendita.xml"), getFileContent("punti-vendita.xml"));
		        }
	        	
		        else if(requestedPath.equals("/db/persone.xml")) {
		        	toXml(getPeopleListFromDatabase());
		        	sendResponse("200 OK", "text/xml", getFileLength("/db/persone.xml"), getFileContent("/db/persone.xml"));
		        }
	        	
		        else if(requestedPath.equals("/db/persone.json")) {
		        	toJson(getPeopleListFromDatabase());
		        	sendResponse("200 OK", "application/json", getFileLength("/db/persone.json"), getFileContent("/db/persone.json"));
		        }
	        		
	        	else 
	        		sendResponse("404 Not found", "text/html", getFileLength("error404.html"), getFileContent("error404.html"));
				
	        }else
	        	sendResponse("405 Method Not Allowed", "text/html", getFileLength("error405.html"), getFileContent("error405.html"));
	       
		} catch (Exception e) {
			return;
		}
	}
	
	private String getFileLength(String path) {
		try {
			long size = Files.size(Paths.get(pathToFiles+path));
			return ""+size;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] getFileContent(String file) {
		try {
			return Files.readAllBytes(Paths.get(pathToFiles+file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void sendResponse(String status, String contentType, String contentLength, byte[] content) throws IOException {
    	clientOutput = socket.getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Server: Java HTTP Server from DeLuca : 1.0 \r\n").getBytes());
        clientOutput.write(("Date: " + new Date()+"\r\n").getBytes());
        clientOutput.write(("Content-Type: " + contentType+"\r\n").getBytes());
        clientOutput.write(("Content-length: " + contentLength + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientOutput.close();
    }
	
	private void redirect(String location) throws IOException {
    	clientOutput = socket.getOutputStream();
        clientOutput.write(("HTTP/1.1 301 Moved Permanently \r\n").getBytes());
        clientOutput.write(("Server: Java HTTP Server from DeLuca : 1.0 \r\n").getBytes());
        clientOutput.write(("Date: " + new Date()+"\r\n").getBytes());
        location = location.replace("/", "");
        clientOutput.write(("Location: http://localhost:9090/"+location+"/\r\n").getBytes());
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientOutput.close();
    }
	
	private ListaPersone getPeopleListFromDatabase() throws Exception {
		ListaPersone listaPersone = new ListaPersone();
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/esTPIT", "root", "password");
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select * from persone");

		while (rs.next()) {
			listaPersone.addPersona(new Persona(rs.getInt(1), rs.getString(2), rs.getString(3)));
		}
		con.close();
		return listaPersone;
	}
	
	private void toXml(ListaPersone listaPersone) throws Exception {
		XmlMapper x = new XmlMapper();
		File xmlFile = new File(pathToFiles + "/db/persone.xml");
		x.writeValue(xmlFile, listaPersone);
	}

	private void toJson(ListaPersone listaPersone) throws Exception {
		File jsonFile = new File(pathToFiles + "/db/persone.json");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(jsonFile, listaPersone);
	}
	
	private void jsonToXml (String file) throws Exception {
		ObjectMapper o = new ObjectMapper();
		PuntiVendita pv = o.readValue(getFileContent(file+".json"), PuntiVendita.class);
		XmlMapper x = new XmlMapper();
		File xmlFile = new File(pathToFiles + file + ".xml");
		x.writeValue(xmlFile, pv);
	}
}
