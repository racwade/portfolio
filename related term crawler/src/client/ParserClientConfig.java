import java.io.*;
class ParserClientConfig {
	public static void main(String args[]) throws Exception{
		System.out.println("ParserClientConfig v1.0");
		File f = new File("config.dat");
		String server = "127.0.0.1";
		int port = 4444;
		if(f.isFile()){
			BufferedReader infile = new BufferedReader(new FileReader(f));
			String inline = infile.readLine();
			String[] values = inline.split(",");
			server = values[0];
			port = Integer.parseInt(values[1]);
			System.out.println("Config file successfully read.");
			infile.close();
		}
		else {
			System.out.println("No config file found! Building a new one.");
		}
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("------------------");
		System.out.print("Enter host address (default: " + server + "): >");
		server = stdin.readLine();
		if(server.equals("")) server = "127.0.0.1"; //default
		System.out.print("Enter port number (default: " + port + "): >");
		String portval = stdin.readLine();
		if(portval.equals("")) portval = "4444"; //default
		port = Integer.parseInt(portval);
		BufferedWriter outfile = new BufferedWriter(new FileWriter(f));
		outfile.write(server + "," + port);
		outfile.close();
		System.out.println("Options saved successfully!");
		return;
	}
}