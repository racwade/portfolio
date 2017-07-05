/** 
 * This program controls block input and output.
 *
 * Based on code provided for CSCI3020.
 */
 
 import java.io.*;
 import java.nio.*;
 import java.net.*;

class blockio{
	//file for storing simulated disk's data
	private static final String DISKFILE = "simdisk.data";
	// size of blocks on simulated disk
	private static final int BLKSIZE = 128;
	//number of blocks on simulated disk
	private static final int NUMBLKS = 512;
	private static File disk_id = null; //the file of the disk
	private static RandomAccessFile disk = null; //the interface for random access of the disk
	
	public static int init_disk(int fresh){
		//initialize the disk. if fresh=1, try writing to it, too.
		char garbage = 'a'; //for testing the write
		try{
			disk_id = new File(DISKFILE); //open the disk
			if(disk_id.length() < BLKSIZE*NUMBLKS){ //if file isn't large enough
				throw new Exception("File size error"); //file size error
			}
			disk = new RandomAccessFile(disk_id, "rw"); //opens in read-write mode
			if(fresh == 1) disk.write(garbage); //attempt to write a single character
		}
		catch (Exception e){
			System.out.println(e);
		}
		return 0;
	}
	
	public static int put_block(int block_num, byte[] buffer){
		try{
			if(block_num < 0 || block_num >= NUMBLKS){ //if trying to write past the file bounds
				throw new Exception("Invalid block! Can't write to block " + block_num);
			}
			if(disk == null){ //if the disk isn't initialized
				throw new Exception("Disk not initialized!");
			} //file not open error
			if(buffer.length > BLKSIZE){
				throw new Exception("Too large a write!");
			}
			disk.seek(block_num*BLKSIZE); //move the file pointer to the appropriate location
			disk.write(buffer); //writes to that location
		} catch (Exception e){
			System.out.println(e);
		}
		return 0;
	}
	public static int put_block(int block_num, short[] buffer){ //shorthand converter
		byte[] output = new byte[buffer.length * 2]; //buffer for byte conversion
		ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer); //little-endian convert short to byte
		return put_block(block_num, output);
	}
	public static int get_block(int block_num, byte[] buffer){
		try{
			if(block_num < 0 || block_num >= NUMBLKS){ //if trying to write past the file bounds
				throw new Exception("Invalid block! Can't read from block " + block_num);
			}
			if(disk == null){ //if the disk isn't initialized
				throw new Exception("Disk not initialized!");
			} //file not open error
			disk.seek(block_num*BLKSIZE); //moves the file pointer
			disk.read(buffer,0,BLKSIZE); //reads from that location (up to 128 bytes)
		} catch (Exception e){
			System.out.println(e);
		}
		return 0;
	}
	public static int get_block(int block_num, short[] buffer){ //shorthand converter
		byte[] input = new byte[buffer.length * 2]; //buffer for byte input
		int returnval = get_block(block_num, input); //get the bytes
		ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer); //little-endian convert byte to short
		return returnval;
	}
}