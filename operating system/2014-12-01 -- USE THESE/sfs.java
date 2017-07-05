 import java.util.*;
public class sfs{
	private static int readdir_dir = -1; //the fd of the last usage of sfs_readdir
	private static short readdir_pos = 0; //where we left off reading from readdir
	private static boolean isInit = false;
	
	sfs(){
		//local memory buffer
	}
	
	public static int sfs_open(String pathname){
		//parse pathname
		//navigate to each directory/file
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		int num_components = INode.parse_pathname(pathname.toCharArray());
		if(num_components < 0) return num_components; //return error codes
		char[] component = new char[8];
		short file_i = INode.parse_dir_entry(num_components, component); //store file i-number
		if(file_i == -1) return -2; //error: file doesn't exist
		short new_fd;
		if(file_i == 0){
			new_fd = get_new_fd(file_i,(short)1);
		}
		else{
			new_fd = get_new_fd(file_i,(short)(sfs_gettype(pathname)));
		}
		return new_fd;
	}
	public static int sfs_read(int fd, int start, int length, char[] mem_pointer){
		//copy length number of bytes to mem_pointer
		//start is where to begin copy
		//zero-based copy
		//copy into memory
		//return -1 if error:
		//file not long enough
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		if(fd < 0 || fd>=64) return -15; //file pointer can't be less than 0;
		short i_number = simdiskCore.fd_table[fd];
		if(i_number == -1) return -15; //file pointer isn't in use
		if(simdiskCore.fd_type[fd] == 1) return -4; //can't use this on directories
		short filesize = INode.read_file(i_number,simdiskCore.buffer_cache);
		if(filesize < start+length) return -6; //not enough file to read
		char[] output = new char[length];
		for(int i = 0; i<length; i++){
			output[i] = (char)(simdiskCore.buffer_cache[start+i]);
		}
		//System.arraycopy(simdiskCore.buffer_cache.toArray(new char[0]),0,mem_pointer,start,length);
		System.arraycopy(output,0,mem_pointer,0,length);
		return 0;
	}
	
	public static int sfs_write(int fd, int start, int length, char[] mem_pointer){
		//copy length number of bytes from mem_pointer
		//start is where to begin overwrite
		//-1 start appends to file
		//if not -1 
		//copy into memory
		//return -1 if error:
		//file not long enough
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		if(fd < 0 || fd>=64) return -15; //file pointer can't be less than 0;
		short i_number = simdiskCore.fd_table[fd];
		if(i_number == -1) return -15; //file pointer isn't in use
		if(simdiskCore.fd_type[fd] == 1){
			return -4; //can't use this on directories
		}
		short filesize = INode.read_file(i_number,simdiskCore.buffer_cache);
		if(start == -1){ //append mode
			for(int i=0;i<length;i++){
				simdiskCore.buffer_cache[filesize+i] = (short)mem_pointer[i];
			}
			//System.arraycopy(mem_pointer,0,simdiskCore.buffer_cache,filesize,length);
			filesize+=length;
			INode.write_file(i_number,simdiskCore.buffer_cache,filesize);
		}
		else{ //overwrite mode
			for(int i = 0;i<length; i++){
				simdiskCore.buffer_cache[start+i] = (short)mem_pointer[i];
			}
			//System.arraycopy(mem_pointer.toArray(new short[0]),0,simdiskCore.buffer_cache,start,length);
			if(start+length > filesize){
				if(filesize/64 < ((start+length)/64)) return -7; //array has not gotten enough block allocated to it yet
				filesize = (short)(start+length);
			}
			INode.write_file(i_number,simdiskCore.buffer_cache,filesize);
		}
		return 0;
	}
	
	public static int sfs_readdir(int fd, char[] mem_pointer){
		//read file components from directory
		//first file put into memory at pointer
		//recursive?
		//each call name is put into buffer which maybe considered the mem_pointer
		//when all files are in buffer nothing should be placed in buffer
		//return 0 for successful scan
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		if(fd < 0 || fd>=64) return -15; //file pointer can't be less than 0;
		short i_number = simdiskCore.fd_table[fd];
		if(i_number == -1) return -15; //file pointer isn't in use
		if(simdiskCore.fd_type[fd] == 0) return -5; //can't use this on files
		short filesize = INode.read_file(i_number,simdiskCore.buffer_cache);
		if(readdir_dir != fd){
			readdir_dir = fd;
			readdir_pos = 0;
		}
		short numfiles = 0;
		short j = 0;
		short num_skipped = 0;
		while(j<filesize && num_skipped < readdir_pos+2){
			j+= simdiskCore.buffer_cache[j+2]+3; //increase the pointer based on the projected filename length
			num_skipped++; //increment the number of filenames we've passed
		}
		if(j>=filesize) return -8; //nothing left to read
		short filename_length = simdiskCore.buffer_cache[j+2];
		//char[] filename = new char[filename_length+1];
		int i = 0;
		for(i = 0; i<filename_length; i++){
			//filename[i] = (char)(simdiskCore.buffer_cache[j+3+i]);
			mem_pointer[i] = (char)(simdiskCore.buffer_cache[j+3+i]);
		}
		//filename[i] = '\0';
		mem_pointer[i] = '\0';
		readdir_pos++;
		return 0;
	}
	
	public static int sfs_close(int fd){
		//file descriptor no longer needed
		//set fd in table to -1 or 0 or null
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		return release_fd((short)fd);
	}
	
	public static int sfs_create(String pathname, int type){
		//if no file of this name created, create it
		//type indicates directory/file
		//total number of files increase by 1
		//error:
		//file name already in use
		//pathname doesn't exist
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		if(type != 1 && type != 0) return -16; //type must be either 0 or 1
		String[] parsed = pathname.split("/");
		if(parsed.length == 0) return -20;
		for(int i = 1; i < parsed.length; i++){
			if(parsed[i].length() == 0) return -20; //way too small
		}
		char[] filename = parsed[parsed.length-1].toCharArray(); //name of new thing to be written
		int num_components = INode.parse_pathname(pathname.toCharArray());
		if(num_components < 0) return num_components; //return error codes
		char[] newname = new char[8];
		short parent_i = INode.parse_dir_entry(num_components-1, newname); //check if parent directory exists
		if(parent_i == -1){
			return -10; //error: parent directory doesn't exist
		}
		short file_i = INode.parse_dir_entry(num_components, newname); //check if this directory already exists
		if(file_i != -1){
			return -11; //error: file to be created already exists
		}
		short dirlength = INode.read_file(parent_i,simdiskCore.buffer_cache); //prepare to append
		short new_i = INode.alloc_new_file();
		short[] newpath = new short[3+filename.length];
		newpath[0] = new_i; //new id
		newpath[1] = (short)type; //type (file is 0, directory is 1)
		newpath[2] = (short)filename.length; //filename length (initially empty)
		for(int j = 0; j<filename.length; j++){
			newpath[3+j] = (short)(filename[j]); //migrates filename to the directory info
		}
		System.arraycopy(newpath,0,simdiskCore.buffer_cache,dirlength,newpath.length);
		INode.write_file(parent_i,simdiskCore.buffer_cache,(dirlength + newpath.length));
		if(type == 1){
			short[] emptydir = new short[64];
			emptydir[0] = new_i; //i_number (of this directory)
			emptydir[1] = 1; //file type (directory)
			emptydir[2] = 1; //length of filename
			emptydir[3] = (short)'.'; //filename (in this case, ".", or "this directory")
			emptydir[4] = parent_i; //i_number (of parent)
			emptydir[5] = 1; //file type (directory)
			emptydir[6] = 2; //length of filename
			emptydir[7] = (short)'.'; //filename (in this case, "..", or "the parent directory")
			emptydir[8] = (short)'.';
			INode.write_file(new_i,emptydir,(short)9); //write this to the directory
		}
		
		return 0;
	}
	
	public static int sfs_delete(String pathname){
		//delete directory/file
		//directory must be empty return error
		//total number of files decreases by 1
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		if(pathname.equals("/")) return -19;
		int num_components = INode.parse_pathname(pathname.toCharArray());
		if(num_components < 0) return num_components; //return error codes
		char[] component = new char[8];
		short parent_i = INode.parse_dir_entry(num_components-1, component); //store parent i-number
		short file_i = INode.parse_dir_entry(num_components, component); //store file i-number
		if(file_i == -1) return -2; //error: file doesn't exist
		if(simdiskCore.file_refcount[file_i] != 0) return -12; //error: file/directory has not been closed
		if(component[1] == 1){
			if(sfs_getsize(pathname) != 0) return -13; //error: directory isn't empty
		}
		short dirsize = INode.read_file(parent_i,simdiskCore.buffer_cache); //read the parent directory into buffer_cache
		int j = 0;
		int found = 0;
		while(j<dirsize){
			short namesize = simdiskCore.buffer_cache[j+2];
			if(simdiskCore.buffer_cache[j] == file_i){
				found = 1;
				dirsize -= (3+namesize); //size of directory is decremented
				System.arraycopy(simdiskCore.buffer_cache,j+3+namesize,simdiskCore.buffer_cache,j,(dirsize)-j); //shunt everything in the array over to overwrite the file to be deleted
				INode.write_file(parent_i,simdiskCore.buffer_cache,dirsize);
				break;
			}
			else j+=(3+namesize);
		}
		if(found == 0) return -1; //something strange happened, and we couldn't find the file's entry in the directory
		INode.clear_file(file_i); //finally, free the blocks and remove them from the inode table
		return 0;
	}	
	
	public static int sfs_getsize(String pathname){
		//return number of bytes in a file
		//return number of files in directory
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		int num_components = INode.parse_pathname(pathname.toCharArray());
		if(num_components < 0) return num_components; //return error codes
		char[] component = new char[8];
		short file_i = INode.parse_dir_entry(num_components, component);
		if(file_i == -1) return -2; //error: file doesn't exist
		if(component[1] == 0){ //file
			return INode.get_file_pointer(file_i);
		}
		else if(component[1] == 1){ //directory
			short filesize = INode.read_file(file_i, simdiskCore.buffer_cache);
			short numfiles = 0;
			short j = 2;
			while(j<filesize){
				if(simdiskCore.buffer_cache[j] < 1){
					break;
				}
				j+= simdiskCore.buffer_cache[j]+3; //increase the pointer based on the projected filename length
				numfiles++; //increment the number of filenames we've passed
			}
			return numfiles-2; //the . and .. directories don't count
		}
		else return -1; //something went wrong; component[1] isn't 1 or 2
	}
	
	public static int sfs_gettype(String pathname){
		//return int determining type of directory/file
		if(!isInit) return -14; //can't do anything unless the system's been initialized!
		int num_components = INode.parse_pathname(pathname.toCharArray());
		if(num_components < 0) return num_components; //return error codes
		char[] component = new char[8];
		short file_i = INode.parse_dir_entry(num_components, component);
		if(file_i == -1) return -2; //error: file doesn't exist
		return component[1];
	}
	
	public static int sfs_initialize(int erase){
		//first call
		//additional calls
		//erase = 0 is default
		//erase = 1 existing files destroyed create new system
		//new sys consists of root directory only
		Arrays.fill(simdiskCore.fd_table, (short)-1);
		Arrays.fill(simdiskCore.file_refcount, (short)0);
		readdir_dir = -1; //the fd of the last usage of sfs_readdir
		readdir_pos = 0; //where we left off reading from readdir
		if(erase == 1){
			blockio.init_disk(1);
			INode.clear_blockmap(); //set default blockmap (that is to say, nothing)
			SuperBlock.defaults(); //set default block usage markers
			SuperBlock.put_super_blk();
			INode.put_inode_table();
			short rootblock = INode.alloc_block_tofile((short)0); //allocate a block to the root directory
			short[] rootarray = new short[64];
			rootarray[0] = 0; //i_number
			rootarray[1] = 1; //file type (directory)
			rootarray[2] = 1; //length of filename
			rootarray[3] = (short)'.'; //filename (in this case, ".", or "this directory")
			rootarray[4] = 0; //i_number
			rootarray[5] = 1; //file type (directory)
			rootarray[6] = 2; //length of filename
			rootarray[7] = (short)'.'; //filename (in this case, "..", or "the parent directory")
			rootarray[8] = (short)'.';
			//rootarray[7] = -1; //terminator: a value that will never be an i_number
			blockio.put_block(rootblock,rootarray); //write the basic root directory
			INode.put_file_pointer((short)0,(short)18); //set the number of shorts in the directory to be 9.
			SuperBlock.put_super_blk();
			INode.put_inode_table();
		}
		else{
			blockio.init_disk(0);
			SuperBlock.get_super_blk();
			INode.get_inode_table();
		}
		isInit = true;
		return 0;
	}
	public static short get_new_fd(short i_number, short type){
		short i = 0;
		for(i = 0; i<64; i++){
			if(simdiskCore.fd_table[i] == -1) break;
		}
		if(i == 64) return -3; //all fd's used
		simdiskCore.fd_table[i] = i_number;
		simdiskCore.file_refcount[i_number] += 1;
		simdiskCore.fd_type[i] = type;
		return i;
	}
	public static int release_fd(short fd){
		short i_number = simdiskCore.fd_table[fd];
		if(i_number == -1) return -9; //tried to release fd that wasn't used
		simdiskCore.file_refcount[i_number] -= 1;
		simdiskCore.fd_table[fd] = -1;
		return 0;
	}
}