import java.io.*;
import java.net.*;
// Alexander Crawford, 100569102
// JAVA-BASED RELATED TERM CRAWLER CLIENT
// -------
// This is the client implementation of the related term crawler.
// Intended for use on the end of the advertiser,
// this class communicates with the server
class ParserClient {
	public static void main( String args[] ) throws Exception{
		if(args.length < 2){
			System.out.println("Usage: ParserClient [sourcefile] [topic] [flags]");
			System.out.println("FLAGS:");
			System.out.println("-n [num]");
			System.out.println("-d [num]");
			System.out.println("-simple");
			return;
		}
		String host = "127.0.0.1";
		int port = 4444;
		File options = new File("config.dat");
		if(options.isFile()){
			BufferedReader infile = new BufferedReader(new FileReader(options));
			String inline = infile.readLine();
			String[] values = inline.split(",");
			host = values[0];
			port = Integer.parseInt(values[1]);
			infile.close();
		}
		else {
			System.out.println("ERROR: No config.dat file found! Please run ParserClientConfig.");
			return;
		}
		int num_sources = -1;
		int num_separation = -1;
		int simple_mode = 0;
		for(int i = 2; i<args.length;){
			if(args[i].equals("-n")){
				num_sources = Integer.parseInt(args[i+1]);
				i+=2;
			}
			else if(args[i].equals("-d")){
				num_separation = Integer.parseInt(args[i+1]);
				i+=2;
			}
			else if(args[i].equals("-simple")){
				simple_mode = 1;
				i++;
			}
			else{
				System.out.println("ERROR: Unknown flag or value " + args[i]);
				return;
			}
		}
		File f = new File(args[0]);
		if(!f.isFile()){
			System.out.println("ERROR: Cannot read file " + args[0]);
			return;
		}
		Socket connection = new Socket(host,port);
		PrintWriter output = new PrintWriter(connection.getOutputStream(), true);
		BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String msg, out;
		output.println(args[1] + " " + num_separation + " " + num_sources + " " + simple_mode);
		BufferedReader infile = new BufferedReader(new FileReader(f));
		for(String inline = infile.readLine(); inline != null; inline = infile.readLine()){
			output.println(inline);
		}
		output.println("<<end>>");
		if(simple_mode != 1){
			System.out.println("Requesting data for keyword \"" + args[1] + "\" from file " + args[0] + "...");
			System.out.println("--------------------");
			System.out.println(input.readLine());
			System.out.println(input.readLine());
			System.out.println("--------------------");
		}
		else{
			System.out.println(input.readLine());
		}
		return;
	}
}