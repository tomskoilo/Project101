import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.File;


public class FileClient {
	
	private Socket s;
	private DataOutputStream dos;
	private FileInputStream fis;
	private DataInputStream dis;
	private FileOutputStream fos;
	public String name;
	public double percent;
	
	public FileClient(String host, int port, String file) {
		try {
			s = new Socket(host, port);
			sendFile(file);
			saveFile();
		} catch (Exception e) {
			System.out.println("No Internet Connection");
		} finally {
			try {
				if(fis!=null)
					fis.close();
				if(dos!=null)
					dos.close();
				if(fos!=null)			
					fos.close();
				if(dis!=null)
					dis.close();
			} catch (Exception e) {
				System.out.println("An error has occured");
			}
		}
	}
	
	public void sendFile(String file) throws IOException {
		dos = new DataOutputStream(s.getOutputStream());
		File f = new File(file);
		fis = new FileInputStream(f);
		byte[] buffer = new byte[4096];

		dos.writeLong(f.length());

		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}	
	}

	private void saveFile() throws IOException {
		dis = new DataInputStream(s.getInputStream());
		byte[] buffer = new byte[4096];
		
		name = dis.readUTF(); //leaf name - DISPLAY
		System.out.println("name is " + name);
		if(!name.equals("No_result_found")) {
			fos = new FileOutputStream((name + ".jpg")); //saved image name - CHANGE IF YOU WANT, 
			//but DISPLAY leaf image

			percent = dis.readDouble(); //accuracy percent - DISPLAY
			System.out.println("Percentage is " + percent);
			int filesize = (int) dis.readLong();
			int read = 0;
			int totalRead = 0;
			int remaining = filesize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				remaining -= read;
				fos.write(buffer, 0, read);
			}
		}
	}
	
	public static void main(String[] args) {
		//change localhost, 3532, and sycamore.jpg
		FileClient fc = new FileClient("192.168.1.21", 3535, "img002.jpg");
	}
}