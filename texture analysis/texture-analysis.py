import numpy as np
import scipy as sp
from scipy import signal
import makeSfilters as S
import argparse
import cv2

def filter(image1,image2):
	verbose = True #if True, outputs texture calculations midway
	max_mean_diff = 200 #maximum distance between mean vectors
	max_std_diff = 100 #maximum distance between std dev vectors
	img1 = cv2.imread(image1, 0)
	img2 = cv2.imread(image2, 0)
	S_filt = S.makeSfilters() #make the filters
	mean1 = np.zeros(13) #vectors of means per filter
	mean2 = np.zeros(13)
	stddev1 = np.zeros(13) #vectors of std devs per filter
	stddev2 = np.zeros(13)
	for i in range(13):
		if(verbose):
			print("calculating texture",i+1,"of 13")
		filt = S_filt[i]
		im1conv = sp.signal.convolve(img1, filt) #run filters
		im2conv = sp.signal.convolve(img2, filt)
		mean1[i] = np.average(im1conv)
		mean2[i] = np.average(im2conv)
		stddev1[i] = np.std(im1conv)
		stddev2[i] = np.std(im2conv)
	dist_mean = np.linalg.norm(mean1-mean2) #calculate distance between vectors
	dist_std = np.linalg.norm(stddev1-stddev2)
	if(dist_mean < max_mean_diff and dist_std < max_std_diff):
		print("true")
	else:
		print("false")
	return

if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='CSCI 4220U Assignment 2.')
	parser.add_argument('im1', help='Image file 1')
	parser.add_argument('im2', help='Image file 2')
	args = parser.parse_args()
	filter(args.im1,args.im2)