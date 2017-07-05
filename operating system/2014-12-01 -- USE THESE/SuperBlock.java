/**
  * Implements the SuperBlock concept in Java.
  */
  
  import java.util.*;

class SuperBlock {
	
	public static void defaults(){
		//Automatically marks the first few blocks of the simdiskCore.disk_bitmap as used.
		for(int i = 0; i<14; i++){
			simdiskCore.disk_bitmap[i] = 1; //marks the simdiskCore.disk_bitmap block
		}
	}
	
	public static int put_super_blk(){
		//Moves the super block from the simdiskCore.disk_bitmap to the disk.
		for(int i = 0; i < 512; i+=4){
			short val = 0;
			if(simdiskCore.disk_bitmap[i] == 1) val += 1;
			if(simdiskCore.disk_bitmap[i+1] == 1) val += 2;
			if(simdiskCore.disk_bitmap[i+2] == 1) val += 4;
			if(simdiskCore.disk_bitmap[i+3] == 1) val += 8;
			simdiskCore.super_blk_buf[i/4] = val;
		}
		blockio.put_block(0,Arrays.copyOfRange(simdiskCore.super_blk_buf,0,63));
		blockio.put_block(1,Arrays.copyOfRange(simdiskCore.super_blk_buf,64,127));
		return 0;
	}
	
	public static int get_super_blk(){
		// 1. Gets super-block (blocks 0 and 1 of disk) as 'short integer bytes' and puts them in the "simdiskCore.super_blk_buf[128]"
		short[] inbuffer = new short[64];
		blockio.get_block(0,inbuffer);
		System.arraycopy(inbuffer,0,simdiskCore.super_blk_buf,0,64); //copy into first half of simdiskCore.super_blk_buf
		blockio.get_block(1,inbuffer);
		System.arraycopy(inbuffer,0,simdiskCore.super_blk_buf,64,64); //copy into second half of simdiskCore.super_blk_buf
		// 2. Decodes each integer(<15 & >0) in simdiskCore.super_blk_buf[128] into 4 bits and puts them in simdiskCore.disk_bitmap[512].
		int i;
		for(i = 0; i<128; i++){
			if((simdiskCore.super_blk_buf[i] & 1) > 0) simdiskCore.disk_bitmap[(4*i)] = 1;
			else simdiskCore.disk_bitmap[(4*i)] = 0;
			if((simdiskCore.super_blk_buf[i] & 2) > 0) simdiskCore.disk_bitmap[(4*i)+1] = 1;
			else simdiskCore.disk_bitmap[(4*i)+1] = 0;
			if((simdiskCore.super_blk_buf[i] & 4) > 0) simdiskCore.disk_bitmap[(4*i)+2] = 1;
			else simdiskCore.disk_bitmap[(4*i)+2] = 0;
			if((simdiskCore.super_blk_buf[i] & 8) > 0) simdiskCore.disk_bitmap[(4*i)+3] = 1;
			else simdiskCore.disk_bitmap[(4*i)+3] = 0;
		}
		return 0;
	}
	public static short get_empty_blk(){
		// Searches the super-block and, if it has an empty block, marks it as a busy block and returns its block number to the calling function.
		short i;
		for(i = 0; i<512; i++){
			if(simdiskCore.disk_bitmap[i] != 1){ //if the block is free...
				simdiskCore.disk_bitmap[i] = 1; //make the block busy
				return i;
			}
		}
		return -1; //couldn't find a free block
	}
	public static int release_block(int release_blk_no){
		// Releases the block as a free block to the system, this block was already allocated to a file; it also updates the simdiskCore.disk_bitmap[512].
		if(release_blk_no < 14) return -1; //can't free system-use blocks
		if(simdiskCore.disk_bitmap[release_blk_no] != 1) return -1; //block wasn't in use before...
		else{
			simdiskCore.disk_bitmap[release_blk_no] = 0;
			return 0;
		}
	}
}
