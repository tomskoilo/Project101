import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;


class ServerThread implements Runnable {
	
	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			while(true) {
				Server.im.turn(socket);
				socket.close();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Image {
	private DataInputStream dis;
	private FileOutputStream fos;
	private DataOutputStream dos;
	private FileInputStream fis;
	private File f;
	private FileReader in;
	private Scanner sc;

	synchronized void turn(Socket socket) throws IOException{
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		saveFile();
		Process process = Runtime.getRuntime().exec("matlab -nodisplay -nosplash -nodesktop -noFigureWindows-r testing2");
		sendFile();
		close();
	}

	private void saveFile() throws IOException {
		byte[] buffer = new byte[4096];
		fos = new FileOutputStream("read.jpg");
		int filesize = (int) dis.readLong(); // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			fos.write(buffer, 0, read);
		}
	}

	private void sendFile() throws IOException {
		byte[] buffer = new byte[4096];
		f = new File("result.jpg");
		while(!f.exists()) {}
		fis = new FileInputStream(f);
		in = new FileReader("name.txt");
		sc = new Scanner(in);
		dos.writeUTF(sc.next()); //img name
		dos.writeDouble(sc.nextDouble()); //percentage of accuracy
		dos.writeLong(f.length()); //img size
		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}
	}

	private void close() {
		try {
			if(f.exists())
				f.delete();
			if(dis!=null)
				dis.close();
			if(dos!=null)
				dos.close();
			if(fis!=null)
				fis.close();
			if(fos!=null)
				fos.close();
			if(in!=null)
				in.close();
			if(sc!=null)
				sc.close();
			notifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// The server
public class Server
{
	public static Image im = new Image();

    public static void main(String[] args) throws IOException
	{
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(3532);
        }
		catch (IOException e) {
            System.err.println("Could not listen on port: 3536");
            System.exit(-1);
        }
		
        while (true)
		{
			new Thread(new ServerThread(ss.accept())).start();
		}		
	}
}