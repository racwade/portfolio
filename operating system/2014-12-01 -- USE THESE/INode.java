/**
  * Implements the INode concept in Java.
  */
  
 import java.util.*;
class INode{
	private static short blockin[] = new short[64]; //Buffer for a single block to be read/written.
	
	public static void clear_blockmap(){
		for(int i = 0; i<8; i++){
			Arrays.fill(simdiskCore.file_blockno[i], (short)-1);
		}
		Arrays.fill(simdiskCore.file_pointer, (short)0);
		return;
	}
	
	public static int get_inode_table(){
		int i;
		for(i = 0; i<8; i++){
			blockio.get_block(i+2,simdiskCore.file_blockno[i]);
		}
		blockio.get_block(10,simdiskCore.file_pointer);
		return 0;
	}
	
	public static int put_inode_table(){
		// Puts the integer numbers from memory buffer "simdiskCore.file_blockno[8][64]" to the I_NODE_TABLE blocks (blocks #2 to #9) of the disk.
		int i;
		for(i = 0; i<8; i++){
			blockio.put_block(i+2,simdiskCore.file_blockno[i]);
		}
		blockio.put_block(10,simdiskCore.file_pointer);
		return 0;
	}
	public static short get_file_pointer(short i_number){
		// Gets the file pointer of the file whose i_number is sent as a parameter and returns it.
		// This effectively returns the size of the file in bytes.
		return simdiskCore.file_pointer[i_number];
	}
	public static void put_file_pointer(short i_number, short newsize){
		// Sets the file pointer of the i-number indicated to the size newsize (in bytes).
		simdiskCore.file_pointer[i_number] = newsize;
		put_inode_table();
		return;
	}
	
	public static short read_file_blocks(short i_number, short[] buffer, int firstblock, int lastblock){
		//Gets blocks from a file with i-number i_number. The blocks pulled are all those between firstblock and lastblock (inclusive).
		//The information is held in simdiskCore.buffer_cache as the blocks are being constructed.
		//Returns the amount of data read in via sneaky math.
		boolean atEnd = false;
		if(firstblock < 0 || firstblock > lastblock || lastblock > 7) return -1; //illegal blocks
		short fullfilesize = simdiskCore.file_pointer[i_number];
		if(lastblock == (fullfilesize/128)) atEnd = true; //notes that the last block pulled is the last block of the file
		int j;
		for(j=firstblock;j<=lastblock;j++){
			short blockid = simdiskCore.file_blockno[j][i_number];
			blockio.get_block(blockid,blockin);
			System.arraycopy(blockin,0,buffer,(j-firstblock)*64,64); //start at 0, increment by 64 each time
		}
		if(atEnd){
			return (short)(fullfilesize - (128*firstblock)); //we pulled a partly empty block, so the amount of relevant data is the size minus whatever we lost from the beginning
		}
		else{
			return (short)((firstblock-lastblock+1)*128); //we pulled full blocks, so the amount of relevant data is the full length
		}
	}
	public static short read_file(short i_number, short[] buffer){
		//Reads all of the file with i-number i_number.
		//Stores the file in buffer, and returns the file length (in shorts).
		int i = 0;
		while(simdiskCore.file_blockno[i][i_number] != -1){
			i++;
		}
		return (short)(read_file_blocks(i_number, buffer, 0, i-1)/2);
	}
	public static int write_file(short i_number, short[] buffer, int writelength){
		//Writes all of the file in buffer to the file specified by i-number i_number.
		//Writes a number of shorts equal to writelength.
		int isRemainder = 0;
		if(writelength%64 != 0) isRemainder = 1;
		return write_file_blocks(i_number, buffer, writelength, 0, (writelength/64)+isRemainder-1);
	}
	public static int write_file_blocks(short i_number, short[] buffer, int writelength, int firstblock, int lastblock){
		//Writes to the given file with i-number of i_number. The data to be written is in buffer.
		//writelength is the number of shorts to be written, firstblock-lastblock are the numbers of the blocks being written to.
		if(firstblock < 0 || firstblock > lastblock || lastblock > 7){
			return -1; //illegal blocks
		}
		if(writelength>((1+lastblock-firstblock)*64)){
			return -1; //error: not enough blocks to cover the transaction
		}
		int j;
		for(j=firstblock;j<=lastblock;j++){
			short blockid = simdiskCore.file_blockno[j][i_number];
			if(blockid == -1){
				blockid = alloc_block_tofile(i_number);
			}
			if(j==lastblock){
				System.arraycopy(buffer,(j-firstblock)*64,blockin,0,writelength%64); //write the odd-length tail of the file
				for(int i = writelength%64;i<64;i++){
					blockin[i] = 0; //fill remainder with zeroes
				}
			}
			else{
				System.arraycopy(buffer,(j-firstblock)*64,blockin,0,64); //start at 0, increment by 64 each time
			}
			blockio.put_block(blockid,blockin);
		}
		short filesize = (short)(writelength*2 + firstblock*128);
		put_file_pointer(i_number, filesize);
		return 0;
	}
	public static short alloc_block_tofile(short i_number){
		//Allocates 1 block to a file whose i_number is stored in "int i_number". The number of the allocated block is returned.
		// 1. Load the i_node table from disk to memory.
		short i;
		for(i = 0; i<8; i++){
			blockio.get_block(i+2,simdiskCore.file_blockno[i]);
		}
		// 2. Search to the list of direct blocks of the file to see how many blocks have already been allocated to the file.
		for(i = 0; i<8; i++){
			if(simdiskCore.file_blockno[i][i_number] == -1) break;
		}
		if(i == 8){
			return -1; // max blocks used
		}
		//i now holds the position where we can add a new block.
		// 3. Use get_empty_blk() to search the super block and find an empty block.
		short newblock = SuperBlock.get_empty_blk();
		// 4. Allocate the empty block to the file, and save the i_node table in the disk.
		simdiskCore.file_blockno[i][i_number] = newblock;
		put_inode_table();
		// 5. Return the allocated block
		return newblock;
	}
	public static short alloc_new_file(){
		//Creates a new file and returns its i_number.
		//This is done by finding the first unused entry in the 0 column of simdiskCore.file_blockno and allocating a block to it.
		int i = 0;
		while(simdiskCore.file_blockno[0][i] != -1 && i<64){
			i++;
		}
		if(i == 64) return -1; //completely full
		short newblock = alloc_block_tofile((short)i);
		return (short)i;
	}
	public static int parse_pathname(char[] path){
		// Parses the pathname passed into path[], and returns the number of components.
		// The parsed pathname will be constructed in "simdiskCore.pathname_parse[64][7]".
		// Valid pathname component has 5 chars and delimiter '\0' (ie. "xxxxx\0")
		// path[i]: 0<i<1023
		// simdiskCore.pathname_parse[j][k]: 0 < j < 63, 0 < k < 5
		// j counts individual pathname components
		// k counts the number of characters in each pathname component
		String fullpath = new String(path);
		if(fullpath.equals("/")){
			return 0;
		}
		String[] parsed = fullpath.split("/");
		if(parsed[0].length() != 0) return -17; //error: didn't start with '/'
		int i = 0;
		for(;i<parsed.length;i++){
			if(parsed[i].length() > 5) return -18; //error: filename too long
			simdiskCore.pathname_parse[i] = parsed[i].toCharArray();
		}
		return parsed.length-1; //number of non-root path qualifiers
	}
	
	public static short parse_dir_entry(int component_no, char[] component){
		// Returns the 'i number' and stores the file information in component.
		// Parses the directory entry and returns "pathway component" and "i_number" using "component_no".
		// It accesses the entry in "simdiskCore.pathname_parse[64][7]" and parses the entry, then returns the component (i-number, type and name) and i_number.
		// Returns an integer with 2 digits: XX
		if(component_no == 0){
			component = "".toCharArray(); //root directory cannot be fiddled with
			return 0; //root directory is always 0
		}
		else{
			short parent_i = parse_dir_entry(component_no -1, component);
			if(parent_i == -1) return -1;
			char[] itemname = simdiskCore.pathname_parse[component_no]; //return the name of the directory part
			String itemname_str = new String(itemname);
			short filesize = read_file(parent_i,simdiskCore.buffer_cache);
			
			short j = 0;
			while(j<filesize){
				short fileid = simdiskCore.buffer_cache[j];
				short filetype = simdiskCore.buffer_cache[j+1];
				short namelength = simdiskCore.buffer_cache[j+2];
				char[] filename = new char[8];
				filename[0] = (char)fileid;
				filename[1] = (char)filetype;
				filename[2] = (char)namelength;
				for(short k = 0; k<namelength+3; k++){
					filename[k] = (char)simdiskCore.buffer_cache[j+k];
				}
				String outfile = new String(Arrays.copyOfRange(filename,3,namelength+3));
				if(outfile.equals(itemname_str)){
					System.arraycopy(filename,0,component,0,filename.length); //component = filename;
					return fileid;
				}
				j += namelength + 3;
			}
			return -1;
			//The directory is what stores file names.
			//First entry in a directory is always ., or 'this directory'.
			//Second entry in a directory is always .., or 'this directory's parent'.
			
		}
		
		//recursively call itself. component no 0 is the root. from each other, pass the previous value up

	}
	public static int clear_file(short i_number){
		if(i_number == 0) return -1; //can't clear the root directory
		for(int i = 0; i<8; i++){
			if(simdiskCore.file_blockno[i][i_number] == -1) break; //we've cleared everything
			SuperBlock.release_block(simdiskCore.file_blockno[i][i_number]);
			simdiskCore.file_blockno[i][i_number] = -1;
		}
		simdiskCore.file_pointer[i_number] = 0; //size is now 0
		SuperBlock.put_super_blk();
		put_inode_table();
		return 0;
	}
	
}