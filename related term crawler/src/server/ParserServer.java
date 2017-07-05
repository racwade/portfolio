import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
class FreqWord implements Comparable<FreqWord>{
	// The FreqWord class.
	// Contains both a particular word and how frequently it appears.
	public String word;
	public int instances;
	public FreqWord(String word, int instances){
		// Initializes a new FreqWord.
		this.word = word;
		this.instances = instances;
	}
	public int compareTo(FreqWord f){
		// compareTo - Comparator for ordering.
		return Integer.compare(this.instances, f.instances);
	}
}
class Document {
	// The Document class.
	// Holds information about a parsed document.
	int total; //total number of words in the document
	String filename; //filename of the document
	List<FreqWord> frequencies = new ArrayList<FreqWord>(); //list of word frequencies
	public void save() throws IOException{
		BufferedWriter outfile = new BufferedWriter(new FileWriter("frequencies/" + filename + ".dat"));
		for(int i = 0; i<frequencies.size(); i++){
			FreqWord f = frequencies.get(i);
			outfile.write(f.word + "," + f.instances);
			if(i<frequencies.size() - 1) outfile.newLine();
		}
		outfile.close();
		return;
	}
	public void load() throws IOException{
		frequencies = new ArrayList<FreqWord>();
		int tot = 0;
		BufferedReader infile = new BufferedReader(new FileReader("frequencies/" + filename + ".dat"));
		for(String inline = infile.readLine(); inline != null; inline = infile.readLine()){
			String[] freq = inline.split(",");
			frequencies.add(new FreqWord(freq[0],Integer.parseInt(freq[1])));
			tot += Integer.parseInt(freq[1]);
		}
		this.total = tot;
		infile.close();
		return;
	}
	public boolean tryLoad() throws IOException{
		if(new File("frequencies/" + filename + ".dat").isFile()){
			load();
			return true;
		}
		else {
			return false;
		}
	}
	public Document(String name){
		filename = name;
	}
}
class WaitingThread {
	// The WaitingThread class.
	// Contains information about a thread waiting to be run.
	public int consecutive;
	public Thread thread;
	public WaitingThread(Thread action, int min_consecutive_threads){
		consecutive = min_consecutive_threads;
		thread = action;
	}
}
class ThreadManager {
	// The ThreadManager class.
	// Exists to manage queues of threads, making sure that they're allowed to run or pause in the right places to avoid deadlock.
	private static int max_threads = 10; //maximum threads
	private static int running_threads = 0; //how many threads are running currently. 
	private static Queue<WaitingThread> queue = new LinkedList<WaitingThread>(); //queue. also has an intrinsic lock used for synchronization
	public static void freeThread(Thread thread) throws InterruptedException {
		//freeThread - Runs a thread without checking for the number of threads currently run.
		//NOTE: Only run this after adding a thread with >1 minimum consecutive threads.
		//Thread thread - The thread to be run.
		thread.start();
		thread.join();
		releaseThread();
	}
	public static void addThread(Thread thread, int min_consecutive_threads) throws InterruptedException{
		// addThread - Attempts to add a new Thread. If the system is currently too full, adds the thread to the ThreadManager queues
		// Thread thread - The thread to be added.
		// int min_consecutive_threads - The bare minimum number of consecutive threads necessary to complete this thread's intended goal (including the thread itself).
		synchronized(queue) {
			if(running_threads + min_consecutive_threads > max_threads){
				// store the thread to wait.
				WaitingThread newThread = new WaitingThread(thread,min_consecutive_threads);
				queue.add(newThread);
			}
			else {
				// run the thread immediately
				running_threads += min_consecutive_threads;
				thread.start();
			}
		}
	}
	public static void releaseThread(){
		// releaseThread - Frees the space set aside for a thread, and checks if another thread can be run.
		// Generic version that releases 1 point.
		releaseThread(1);
	}
	public static void releaseThread(int return_amount){
		// releaseThread - Frees the space set aside for a thread, and checks if another thread can be run.
		try{
			WaitingThread next_thread = new WaitingThread(null,0);
			synchronized(queue){
				running_threads -= return_amount; //return the thread space
				if(queue.size() > 0){
					next_thread = queue.peek();
					if(running_threads + next_thread.consecutive <= max_threads){
						queue.remove();
						running_threads += next_thread.consecutive;
						next_thread.thread.start();
					}
				}
			}
		} catch(Exception e){
			System.out.println(e);
		}
	}
}
class DocumentThread extends Thread {
	// The DocumentThread class.
	// Exists to load a new document into memory.
	// This document may or may not have a pre-existing frequency table.
	String filename;
	public void run(){
		if(new File("sources/" + filename + ".txt").isFile()){
			try {
				Document newdoc = new Document(filename);
				if(newdoc.tryLoad()){
					System.out.println("Data loaded from file for " + filename + ".");
					ParserServer.documents.add(newdoc);
				}
				else{
					newdoc = ParserUtil.loadDocument("sources/" + filename + ".txt");
					ParserServer.documents.add(newdoc);
					newdoc.save();
					System.out.println("New file created for " + filename + ".");
				}
			} catch (Exception e){
				System.out.println(e);
				return;
			}
		}
		else {
			System.out.println("SYSTEM ERROR: Cannot find " + filename + ".txt! Check your sources folder.");
		}
		try {
			ThreadManager.releaseThread();
		} catch(Exception e){
			System.out.println(e);
			ThreadManager.releaseThread();
		}
		return;
	}
	public DocumentThread(String file){
		filename = file;
		return;
	}
}
class SearcherThread extends Thread {
	// The ReaderThread class.
	// Exists for searching documents, and extracting proximity-words from them.
	RequestThread parent;
	Document document;
	int max_distance = 3;
	public void run(){
		Map<String,Integer> strings = new HashMap<String,Integer>();
		try {
			synchronized(document){
				BufferedReader infile = new BufferedReader(new FileReader("sources/" + document.filename + ".txt"));
				BufferedReader badwords = new BufferedReader(new FileReader("exclude.txt"));
				Pattern[] proximities = new Pattern[max_distance]; //holds forward patterns
				Pattern[] reverse_proximities = new Pattern[max_distance]; //holds reverse patterns
				for(String badword = badwords.readLine(); badword != null; badword = badwords.readLine()){
					strings.put(badword, -1);
				}
				String reversephrase = new StringBuilder(parent.phrase).reverse().toString();
				for(int i = 0; i<max_distance; i++){
					proximities[i] = Pattern.compile(ParserUtil.produceRegex(parent.phrase,i+1));
					reverse_proximities[i] = Pattern.compile(ParserUtil.produceRegex(reversephrase,i+1));
				}
				for(String inline = infile.readLine(); inline != null; inline = infile.readLine()){
					for(int i = 0; i<max_distance; i++){
						List<String> words = new ArrayList<String>();
						Matcher forward = proximities[i].matcher(inline);
						while(forward.find()){
							String[] wordsplit = forward.group().split("\\s+");
							String word = wordsplit[wordsplit.length-1];
							words.add(ParserUtil.standardize(word));
						}
						String enilni = new StringBuilder(inline).reverse().toString(); //inline reversed, so we can check for the phrase first and then work backwards.
						Matcher reverse = reverse_proximities[i].matcher(enilni);
						while(reverse.find()){
							String word = new StringBuilder(reverse.group()).reverse().toString().split("\\s+")[0];
							words.add(ParserUtil.standardize(word));
						}
						for(int j = 0; j<words.size(); j++){
							String word = words.get(j);
							if(word.equals("")) continue;
							if(strings.containsKey(word)){
								int count = strings.get(word);
								if(count > 0){
									strings.put(word,count+ParserUtil.calculateQuality(i,max_distance));
								}
							}
							else {
								strings.put(word,ParserUtil.calculateQuality(i,max_distance));
							}
						}
					}
				}
				infile.close();
			}
			synchronized(parent.common_words){
				for(String word: strings.keySet()){
					int count = strings.get(word);
					if(count <= 0) continue;
					if(parent.common_words.containsKey(word)){
						int prev_count = parent.common_words.get(word);
						parent.common_words.put(word,prev_count + count);
					}
					else {
						//System.out.println("Adding word " + word);
						parent.common_words.put(word,count);
					}
				}
			}
			ThreadManager.releaseThread();
		} catch(Exception e){
			System.out.println(e);
			ThreadManager.releaseThread();
		}
		return;
	}
	public SearcherThread(RequestThread fromClient, Document fromDocument, int distance){
		parent = fromClient;
		document = fromDocument;
		max_distance = distance;
		return;
	}
}
abstract class RequestThread extends Thread {
	// The RequestThread class.
	// Standardizes requests, whether they come from clients or the server.
	String phrase = "";
	Map<String,Integer> common_words = new HashMap<String,Integer>(); //frequencies
	protected int return_amt = 2;
}
class ClientThread extends RequestThread {
	// The ClientThread class.
	// Exists to manage a client's request.
	private Socket user = null;
	int total = 0;
	public void run(){
		try {
			PrintWriter output = new PrintWriter(user.getOutputStream(), true);
			BufferedReader input = new BufferedReader(new InputStreamReader(user.getInputStream()));
			String text_in, text_out;
			text_in = input.readLine(); //Get the request from the client that connected. Just expect one line.
			String[] request = text_in.split(" ");
			// format is [topic] [separation] [sources] [simple]
			// separation and sources will either be positive integers, or -1, which means default
			// simple is 0 for complex output, or 1 for simple output
			if(request.length != 4){
				System.out.println("REQUEST ERROR: Only found request of length " + request.length);
				ThreadManager.releaseThread();
				return;
			}
			phrase = request[0];
			int separation = Integer.parseInt(request[1]); //maximum separation
			if(separation == -1) separation = ParserServer.default_distance;
			int num_sources = Integer.parseInt(request[2]); //number of sources
			if(num_sources == -1) num_sources = ParserServer.default_scanned;
			int simple_mode = Integer.parseInt(request[3]); //simple mode?
			Document infile = ParserUtil.loadDocument(input); //load file; last line is <<end>>
			
			
			
			ThreadManager.releaseThread();
			String topwords = "";
			for(int i = 0; i<3; i++){
				topwords = topwords + infile.frequencies.get(i).word + " ";
			}
			Document[] docs = ParserUtil.findBestSources(infile, 3, num_sources); //find best sources matching top 3 words
			List<SearcherThread> sources = new ArrayList<SearcherThread>();
			if(docs.length > 0){
				sources.add(new SearcherThread(this, docs[0], separation));
				ThreadManager.freeThread(sources.get(0));
			}
			for(int i = 1; i<docs.length; i++){
				sources.add(new SearcherThread(this, docs[i], separation));
				ThreadManager.addThread(sources.get(i),1);
			}
			for(int j = 0; j<sources.size(); j++){
				SearcherThread st = sources.get(j);
				while(st.getState() != Thread.State.TERMINATED){
					Thread.sleep(10);
				}
			}
			List<FreqWord> commons = new ArrayList<FreqWord>();
			for(String word: common_words.keySet()){
				int count = common_words.get(word);
				if(count <= 0) continue;
				total += count;
				FreqWord n = new FreqWord(word, count);
				commons.add(n);
			}
			Collections.sort(commons,Collections.reverseOrder());
			String nearwords = "";
			for(int i = 0; i<3; i++){
				nearwords = nearwords + commons.get(i).word + " (" + (((double) commons.get(i).instances)/total) + ") ";
			}
			if(simple_mode == 1){
				output.println(commons.get(0).word);
			}
			else{
				output.println("The supercategory words for " + phrase + " were: " + topwords);
				output.println("The words most commonly found near " + phrase + " were: " + nearwords);
			}
			System.out.println("Connection resolved successfully.");
			
			ThreadManager.releaseThread();
		} catch(Exception e){
			System.out.println(e);
			ThreadManager.releaseThread(return_amt);
		}
		return;
	}
	public ClientThread(Socket client){
		user = client;
		return;
	}
}
class ServerThread extends RequestThread {
	// The ServerThread class.
	// Like the ClientThread class, but prints the result to stdout instead of to a client.
	String filename;
	public void run(){
		try {
			int scanned = ParserServer.default_scanned; //number of documents to scan
			int distance = ParserServer.default_distance; //distance away to scan
			Document infile = ParserUtil.loadDocument(filename);
			String topwords = "";
			for(int i = 0; i<3; i++){
				topwords = topwords + infile.frequencies.get(i).word + " ";
			}
			Document[] docs = ParserUtil.findBestSources(infile, 3, scanned);
			List<SearcherThread> sources = new ArrayList<SearcherThread>();
			if(docs.length > 0){
				sources.add(new SearcherThread(this, docs[0], distance));
				ThreadManager.freeThread(sources.get(0));
			}
			for(int i = 1; i<docs.length; i++){
				sources.add(new SearcherThread(this, docs[i], distance));
				ThreadManager.addThread(sources.get(i),1);
			}
			for(int j = 0; j<sources.size(); j++){
				SearcherThread st = sources.get(j);
				while(st.getState() != Thread.State.TERMINATED){
					Thread.sleep(10);
				}
			}
			List<FreqWord> commons = new ArrayList<FreqWord>();
			for(String word: common_words.keySet()){
				int count = common_words.get(word);
				if(count <= 0) continue;
				FreqWord n = new FreqWord(word, count);
				commons.add(n);
			}
			Collections.sort(commons,Collections.reverseOrder());
			String nearwords = "";
			for(int i = 0; i<3; i++){
				nearwords = nearwords + commons.get(i).word + " ";
			}
			System.out.println("The supercategory words for " + phrase + " were: " + topwords);
			System.out.println("The words most commonly found near " + phrase + " were: " + nearwords);
			
			ThreadManager.releaseThread();
		} catch(Exception e){
			System.out.println(e);
			ThreadManager.releaseThread(return_amt);
		}
		return;
	}
	public ServerThread(String target, String file){
		phrase = target;
		filename = file;
		return;
	}
}
class ParserServer {
	static List<Document> documents = new ArrayList<Document>();
	static int port = 4444;
	static int default_distance = 3;
	static int default_scanned = 3;
	
	public static void main( String args[] ) throws Exception{
		// Main loop for the parser server.
		// Unfinished
		System.out.println("ParserServer v1.0");
		System.out.println("-----------------");
		if(args.length == 1){
			port = Integer.parseInt(args[0]);
			System.out.println("Opened on port " + port);
		}
		else{
			System.out.println("Opened on default port (4444)");
		}
		File dir = new File("sources/");
		ArrayList<String> filenames = new ArrayList<String>(Arrays.asList(dir.list()));
		for(String name : filenames){
			String trimname = name.split("\\.")[0];
			loadDocument(trimname);
		}
		System.out.println("-----------------");
		System.out.println("Listening for connections...");
		listen();
	}
	private static void loadDocument(String document) throws InterruptedException{
		Thread t = new DocumentThread(document);
		ThreadManager.addThread(t,1);
		while(t.getState() != Thread.State.TERMINATED){
			Thread.sleep(10);
		}
		return;
	}
	private static void listen() throws Exception{
		ServerSocket server = new ServerSocket(port);
		while(true){
			Socket newsocket = server.accept();
			System.out.println("Accepting a new connection...");
			ThreadManager.addThread(new ClientThread(newsocket),2);
		}
	}
	private static void self_test() throws Exception{
		//function to self-test
		Console c = System.console();
		while(c != null){
			System.out.println("Press enter to spawn a new thread.");
			c.readLine();
			Thread thr = new ServerThread("Alice","example.txt");
			ThreadManager.addThread(thr,2);
			thr.join();
		}
	}
}
class ParserUtil {
	public static String standardize(String in){
		//standardize - Makes a string homogeneous. Used often throughout the code, so standardized here.
		return in.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase().replaceAll(",","");
	}
	public static Document loadDocument(String filename){
		//loadDocument - Create a brand new document from a given file.
		//filename - the full path to the file (not just the name)
		try {
			File f = new File(filename);
			Document newdoc = new Document(f.getName().split("\\.")[0]);
			BufferedReader infile = new BufferedReader(new FileReader(filename));
			Map<String,Integer> strings = new HashMap<String,Integer>();
			BufferedReader badwords = new BufferedReader(new FileReader("exclude.txt"));
			for(String badword = badwords.readLine(); badword != null; badword = badwords.readLine()){
				strings.put(badword, -1);
			}
			for(String inline = infile.readLine(); inline != null; inline = infile.readLine()){
				String[] words = inline.split("\\s+");
				for(int i = 0; i<words.length; i++){
					String word = ParserUtil.standardize(words[i]);
					if(word.equals("")) continue;
					if(strings.containsKey(word)){
						int count = strings.get(word);
						if(count > 0){
							strings.put(word,count+1);
						}
					}
					else {
						strings.put(word,1);
					}
				}
			}
			int total = 0;
			for(String word: strings.keySet()){
				int count = strings.get(word);
				if(count <= 0) continue;
				FreqWord n = new FreqWord(word, strings.get(word));
				newdoc.frequencies.add(n);
				total += count;
			}
			newdoc.total = total;
			Collections.sort(newdoc.frequencies,Collections.reverseOrder());
			infile.close();
			return newdoc;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	public static Document loadDocument(BufferedReader input){
		//loadDocument - Create a brand new document from an input stream.
		//input - the stream to pull from
		try {
			Document newdoc = new Document("input");
			Map<String,Integer> strings = new HashMap<String,Integer>();
			BufferedReader badwords = new BufferedReader(new FileReader("exclude.txt"));
			for(String badword = badwords.readLine(); badword != null; badword = badwords.readLine()){
				strings.put(badword, -1);
			}
			for(String inline = input.readLine();; inline = input.readLine()){
				if(inline.equals("<<end>>")) break; //end the input with <<end>>
				String[] words = inline.split("\\s+");
				for(int i = 0; i<words.length; i++){
					String word = ParserUtil.standardize(words[i]);
					if(word.equals("")) continue;
					if(strings.containsKey(word)){
						int count = strings.get(word);
						if(count > 0){
							strings.put(word,count+1);
						}
					}
					else {
						strings.put(word,1);
					}
				}
			}
			int total = 0;
			for(String word: strings.keySet()){
				int count = strings.get(word);
				if(count <= 0) continue;
				FreqWord n = new FreqWord(word, strings.get(word));
				newdoc.frequencies.add(n);
				total += count;
			}
			newdoc.total = total;
			Collections.sort(newdoc.frequencies,Collections.reverseOrder());
			return newdoc;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	public static Document[] findBestSources(Document target, int words, int num_documents){
		int num_words = words;
		if(target.frequencies.size() < num_words) num_words = target.frequencies.size();
		String[] major_words = new String[num_words];
		Double[] percentage = new Double[num_words];
		Arrays.fill(percentage,0.0);
		Document[] bestdocs = new Document[num_documents];
		Double[] bestdocquality = new Double[num_documents];
		Arrays.fill(bestdocquality,0.0);
		for(int i = 0; i<num_words; i++){
			FreqWord f = target.frequencies.get(i);
			major_words[i] = f.word;
			percentage[i] = ((double) f.instances) / target.total;
		}
		for(int i = 0; i<ParserServer.documents.size(); i++){
			Document read_doc = ParserServer.documents.get(i);
			synchronized(read_doc){
				double docval = 0;
				for(int j = 0; j<num_words; j++){
					//search for each top word, and improve the document's quality by the amount found
					double read_frequency = 0.0; //default of 0 change, if the word isn't found
					for(int k = 0; k<read_doc.frequencies.size(); k++){
						FreqWord f = read_doc.frequencies.get(k);
						if(major_words[j].equals(f.word)){
							read_frequency = ((double)f.instances) / read_doc.total;
							break;
						}
					}
					docval += percentage[j] * read_frequency;
				}
				double difference = 0;
				int position = 0;
				for(int k = 0; k<num_documents; k++){
					if(docval - bestdocquality[k] > difference){
						difference = docval - bestdocquality[k];
						position = k;
					}
				}
				if(difference > 0){
						bestdocs[position] = read_doc;
						bestdocquality[position] = docval;
						//System.out.println("Adding document " + read_doc.filename + "(" + docval + ")");
				}
				else {
					//System.out.println("Skipping " + read_doc.filename + "(" + docval + ")");
				}
			}
		}
		//System.out.println("Best documents:");
		//for(int i = 0; i<num_documents; i++){
		//	if(bestdocs[i].filename.equals(null)) System.out.println("SHAME!");
		//	System.out.println(bestdocs[i].filename + " " + bestdocquality[i]);
		//}
		return bestdocs;
	}
	public static String produceRegex(String phrase, int separation){
		//produceRegex - A more attractive shorthand for generating the regex operation we need.
		//String phrase - the key word or phrase that anchors the rest
		//int separation - how many words away we want the match to be
		
		//I'll try to explain the regex here.
		//First, match to the phrase.
		//Then, reluctantly match non-alphabetic characters until an alphabetic character is found.
		//Greedily match alphabetic characters. This is one "word".
		//Repeat this up to [separation] times. The last result is the nth word.
		return "(?:" + phrase + ")(?:(?:[^a-zA-Z]+?)([a-zA-Z']+)){" + separation + "}";
	}
	public static int calculateQuality(int proximity,int max_proximity){
		//calculateProximity - The function for determining how much weight to give a word with proximity n. If n is 1, the words are next to each other; 2 means there's 1 word in the middle, and so on.
		//Right now, extremely naive.
		return (int) Math.pow(2,max_proximity-proximity);
		//return 1;
	}
}