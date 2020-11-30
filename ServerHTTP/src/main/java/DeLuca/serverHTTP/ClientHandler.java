package DeLuca.serverHTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
	        while (!(line = br.readLine()).isBlank()) {
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
	        	
		        else if(requestedPath.equals("/punti-vendita.xml")) {
		        	jsonToXml("punti-vendita");
		        	sendResponse("200 OK", "text/xml", getFileLength("punti-vendita.xml"), getFileContent("punti-vendita.xml"));
		        }
		        else if(!requestedPath.contains(".")) 
		        	redirect(requestedPath);
	        		
	        	else 
	        		sendResponse("404 Not found", "text/html", getFileLength("error404.html"), getFileContent("error404.html"));
				
	        }else {
	        	sendResponse("405 Method Not Allowed", "text/html", getFileLength("error405.html"), getFileContent("error405.html"));
	        }
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
	
	private void jsonToXml (String file) throws Exception {
		ObjectMapper o = new ObjectMapper();
		PuntiVendita pv = o.readValue(getFileContent(file+".json"), PuntiVendita.class);
		XmlMapper x = new XmlMapper();
		//File xmlFile = new File(pathToFiles+file);
		File xmlFile = new File(pathToFiles + file + ".xml");
		x.writeValue(xmlFile, pv);
	}
}
