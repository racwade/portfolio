import java.util.*;

public class sfs_test{
	public static void main(String[]args){
		sfs fileSys = new sfs();
		Scanner in = new Scanner(System.in);
		String input;
		String[] command;
		char[] buffer = new char[512];
		//int fd;
		System.out.println("Welcome to the file system test program!");
		System.out.println("Enter one of the following commands below:");
		System.out.println("i [erase] -- initialize the file system (load if 0, start fresh if 1)");
		System.out.println("m [pathname] [type] -- makes a new file (basic file if type=0, directory if type=1)");
		System.out.println("d [pathname] -- delete a file/directory");
		System.out.println("s [pathname] -- gets the size of a file, or the number of files in a directory");
		System.out.println("t [pathname] -- returns 0 if the path is a file, 1 if the path is a directory");
		System.out.println("o [pathname] -- opens a file; returns a file descriptor to read-write to the file");
		System.out.println("c [fd] -- closes a file and returns the file descriptor");
		System.out.println("r [fd] [start] [length] -- reads [length] characters starting from the [start] position of a file");
		System.out.println("R [fd] -- reads the files present in a directory, one by one; call repeatedly with the same fd to pull futher directories");
		System.out.println("w [fd] [start] [length] -- writes [length] characters from the buffer to the file at the position [start]");
		System.out.println("wS [fd] [string] -- shorthand version of w that treats everything after the fd as a string to be copied into the buffer; writes all the found characters with length=-1");
		System.out.println("h -- display this help again");
		System.out.println("q -- quit");
		while(true){
			System.out.print("> ");
			input = in.nextLine();
			command = input.split(" ");
			if(command[0].equals("o")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int fd = sfs.sfs_open(command[1]);
				if(parse_error_code(fd) == -1) continue;
				System.out.println("File " + command[1] + " opened with FD=" + fd);
			}else if(command[0].equals("r")){
				if(command.length < 4){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				Arrays.fill(buffer, '\0');
				int fd = Integer.parseInt(command[1]);
				int start = Integer.parseInt(command[2]);
				int length = Integer.parseInt(command[3]);
				if(parse_error_code(sfs.sfs_read(fd,start,length,buffer)) == -1) continue;
				String buffout = new String(buffer);
				System.out.println("Buffer contents: " + buffout);
			}else if(command[0].equals("w")){
				if(command.length < 4){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int fd = Integer.parseInt(command[1]);
				int start = Integer.parseInt(command[2]);
				int length = Integer.parseInt(command[3]);
				if(parse_error_code(sfs.sfs_write(fd,start,length,buffer)) == -1) continue;
				System.out.println("Successfully wrote " + length + " characters.");
			}else if(command[0].equals("wS")){
				if(command.length < 3){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int fd = Integer.parseInt(command[1]);
				String instring = new String();
				for(int i = 2; i<command.length; i++){
					instring += command[i];
					if(i+1 != command.length) instring += " ";
				}
				System.arraycopy(instring.toCharArray(),0,buffer,0,instring.length());
				if(parse_error_code(sfs.sfs_write(fd,-1,instring.length(),buffer)) == -1) continue;
				System.out.println("Successfully wrote " + instring.length() + " characters.");
			}else if(command[0].equals("R")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int fd = Integer.parseInt(command[1]);
				Arrays.fill(buffer, '\0');
				if(parse_error_code(sfs.sfs_readdir(fd,buffer)) == -1) continue;
				String buffout = new String(buffer);
				System.out.println("Buffer contents: " + buffout);
			}else if(command[0].equals("c")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int requestedFD = Integer.parseInt(command[1]);
				if(parse_error_code(sfs.sfs_close(requestedFD)) == -1) continue;
			}else if(command[0].equals("m")){
				if(command.length < 3){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int type = Integer.parseInt(command[2]);
				if(parse_error_code(sfs.sfs_create(command[1],type)) == -1) continue;
				System.out.println("File created.");
			}else if(command[0].equals("d")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				if(parse_error_code(sfs.sfs_delete(command[1])) == -1) continue;
				System.out.println("Successfully deleted.");
			}else if(command[0].equals("s")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int size = sfs.sfs_getsize(command[1]);
				if(parse_error_code(size) == -1) continue;
				System.out.println("The size of the file is: " + size);
			}else if(command[0].equals("t")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int type = sfs.sfs_gettype(command[1]);
				if(parse_error_code(type) == -1) continue;
				System.out.println("The type of the file is: " + type);
			}else if(command[0].equals("i")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				int erase = Integer.parseInt(command[1]);
				if(parse_error_code(sfs.sfs_initialize(erase)) == -1) continue;
			}else if(command[0].equals("h")){
				System.out.println("Enter one of the following commands below:");
				System.out.println("i [erase] -- initialize the file system (load if 0, start fresh if 1)");
				System.out.println("m [pathname] [type] -- makes a new file (basic file if type=0, directory if type=1)");
				System.out.println("d [pathname] -- delete a file/directory");
				System.out.println("s [pathname] -- gets the size of a file, or the number of files in a directory");
				System.out.println("t [pathname] -- returns 0 if the path is a file, 1 if the path is a directory");
				System.out.println("o [pathname] -- opens a file; returns a file descriptor to read-write to the file");
				System.out.println("c [fd] -- closes a file and returns the file descriptor");
				System.out.println("r [fd] [start] [length] -- reads [length] characters starting from the [start] position of a file");
				System.out.println("R [fd] -- reads the files present in a directory, one by one; call repeatedly with the same fd to pull futher directories");
				System.out.println("w [fd] [start] [length] -- reads [length] characters starting from the [start] position of a file");
				System.out.println("wS [fd] [start] [length] -- reads [length] characters starting from the [start] position of a file");
				System.out.println("b [string] -- fills the read/write buffer with the specified string");
				System.out.println("h -- display this help again");
				System.out.println("q -- quit");
			}else if(command[0].equals("b")){
				if(command.length < 2){
					System.out.println("I didn't find enough parameters. Please type 'h' if you want to see the parameters you need to enter.");
					continue;
				}
				Arrays.fill(buffer, '\0');
				String instring = new String();
				for(int i = 1; i<command.length; i++){
					instring += command[i];
					if(i+1 != command.length) instring += " ";
					else instring += "\0";
				}
				System.arraycopy(instring.toCharArray(),0,buffer,0,instring.length());
				String buffout = new String(buffer);
				System.out.println("Buffer contents: " + buffout);
			}else if(command[0].equals("q")){
				System.out.println("Have a good day!");
				break;
			}
		}
	}
	public static int parse_error_code(int code){
		//Converts error codes into human-readable error messages. Returns -1 if an error code was read, 0 if the code was normal.
		if(code >= 0) return 0; //skip if the code's normal
		if(code == -1) System.out.println("ERROR -1: Generic file system error");
		if(code == -2) System.out.println("ERROR -2: File does not exist");
		if(code == -3) System.out.println("ERROR -3: fd could not be generated (are you remembering to close files?)");
		if(code == -4) System.out.println("ERROR -4: This operation cannot be performed on directories");
		if(code == -5) System.out.println("ERROR -5: This operation cannot be performed on files");
		if(code == -6) System.out.println("ERROR -6: Attempting to read past file end");
		if(code == -7) System.out.println("ERROR -7: Insufficient space for overwrite; use start=-1 to append and increase file size");
		if(code == -8) System.out.println("ERROR -8: All directories read");
		if(code == -9) System.out.println("ERROR -9: Can't release a fd that's not in use");
		if(code == -10) System.out.println("ERROR -10: Parent directory does not exist");
		if(code == -11) System.out.println("ERROR -11: File/directory already exists");
		if(code == -12) System.out.println("ERROR -12: File/directory is still open by at least one process");
		if(code == -13) System.out.println("ERROR -13: Directory can't be deleted unless it's empty");
		if(code == -14) System.out.println("ERROR -14: Disk is not initialized!");
		if(code == -15) System.out.println("ERROR -15: Illegal/unused fd");
		if(code == -16) System.out.println("ERROR -16: Illegal file type; only 0 (for files) and 1 (for directories) is supported");
		if(code == -17) System.out.println("ERROR -17: Pathname didn't start with /");
		if(code == -18) System.out.println("ERROR -18: A component of the pathname was too long");
		if(code == -19) System.out.println("ERROR -19: Cannot delete root directory");
		if(code == -20) System.out.println("ERROR -20: Can't have a 0-length directory name");
		return -1;
	}
}