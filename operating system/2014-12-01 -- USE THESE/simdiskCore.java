class simdiskCore{
	public static short[] disk_bitmap = new short[512]; //Marks which blocks are used. Write to this.
	public static short[] super_blk_buf = new short[128]; //Buffer for transferring between disk_bitmap and the disk. DON'T CALL THIS; put/get_super_blk use this alone.
	public static short[][] file_blockno = new short[8][64]; //Contains block location information. The [64] component is the i_number, and the [8] in order are the 8 possible blocks a file can control. Read from blocks 2-9.
	public static short file_pointer[] = new short[64]; //Shows number of elements in the files (i.e. their size in bytes). Index is the i_number. Read from block 10.
	public static short buffer_cache[] = new short[1024]; //buffer for movement between memory and disk
	public static char pathname_parse[][] = new char[64][7]; //buffer for storing parsed path info. 64 directories, 7 characters each
	public static short file_refcount[] = new short[64]; //Number of 'references' to each file (i-number).
	public static short fd_table[] = new short[64]; //converts from the file descriptor to the i_number. index is FD, value is i_number.
	public static short fd_type[] = new short[64]; //stores the type of the given fd's
}