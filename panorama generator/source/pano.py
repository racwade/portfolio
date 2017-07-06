import cv2
import numpy as np
from scipy.spatial import distance as d
from numpy import linalg as la
import argparse

def img_match(image1, image2):
    sift = cv2.xfeatures2d.SIFT_create()
    src1 = cv2.cvtColor(image1, cv2.COLOR_RGB2GRAY)
    src1 = np.uint8(src1)
    src2 = cv2.cvtColor(image2, cv2.COLOR_RGB2GRAY)
    src2 = np.uint8(src2)
    kp1 = sift.detect(src1, None)
    kp2 = sift.detect(src2, None)
    kp1, des1 = sift.compute(src1, kp1)
    kp2, des2 = sift.compute(src2, kp2)
    bf = cv2.BFMatcher()
    matches = bf.knnMatch(des1,des2, k=2)
    good = []
    for m,n in matches:
        if m.distance < 0.90*n.distance:
            good.append((m.queryIdx, m.trainIdx))
            
    if(len(kp1) < len(kp2)):
        num_points = len(kp1)
    else:
        num_points = len(kp2)
    trans = np.zeros((3,3))
    if(len(good) >= 4):
        img1_pts = []
        img2_pts = []
        for point in range(0,len(good)):
            img1_pts.append(kp1[good[point][0]].pt)
            img2_pts.append(kp2[good[point][1]].pt)
        trans,status = cv2.findHomography(np.float32(img1_pts),np.float32(img2_pts), cv2.RANSAC, 4.0)
    return (len(good)/num_points,trans)

	
def imgmerge(img_base,img_add,trans):
    warpheight = img_base.shape[0]
    warpwidth = img_base.shape[1]+img_add.shape[1]
    added_width = trans[len(trans)-1][0][2]
    warpimg = np.zeros((warpheight,warpwidth,3), np.uint8)
    if(added_width > 0):
        warpimg[0:img_add.shape[0],0:img_add.shape[1]] = img_add
        for k in range(0,len(trans)):
            warpimg = cv2.warpPerspective(warpimg,trans[k], (warpwidth,warpheight))
        warpimg[0:img_base.shape[0], 0:img_base.shape[1]] = img_base
        result = warpimg[:,0:(int(img_base.shape[1]+abs(added_width)))]
    else:
        warpimg[warpheight-img_add.shape[0]:warpheight,warpwidth-img_add.shape[1]:warpwidth] = img_add
        for k in range(0,len(trans)):
            warpimg = cv2.warpPerspective(warpimg,trans[k], (warpwidth,warpheight))
        warpimg[warpheight-img_base.shape[0]:warpheight,warpwidth-img_base.shape[1]:warpwidth] = img_base
        result = warpimg[:,warpimg.shape[1]-(int(img_base.shape[1]+abs(added_width))):warpimg.shape[1]]
    return result

def panorama(imgarr):
    weights = np.zeros((len(imgarr), len(imgarr)))
    print("matching images...")
    total = len(imgarr) * len(imgarr)
    percent = 0.1
    for i in range(0,len(imgarr)):
        for j in range(0,len(imgarr)):
            if(i == j):
                weights[i][j] = -1
                continue
            value,func = img_match(imgarr[i], imgarr[j])
            weights[i][j] = value
            if(func[0][2] < 0):
                weights[i][j] = -1
                continue
                #print("error: not a complete panorama set")
                #quit()
            if((i*len(imgarr) + j)/total > percent):
                print(str(int(percent * 100)) + "%")
                percent = percent + 0.1
    height, width, channels = imgarr[0].shape
    bigimage = np.zeros((height,width*(len(imgarr)+2),channels), np.uint8)
    transformations = []
    done_images = []
    result = imgarr[0].copy()
    i = 0 #pointer to image of interest
    done_images.append(i)
    print("stitching...")
    while(len(done_images) < len(imgarr)):
        bestmatch = -1
        bestmatch_val = 0
        for j in range(0,len(imgarr)):
            if(j in done_images):
                continue
            weight = weights[j][i]
            if weight > bestmatch_val:
                bestmatch = j
                bestmatch_val = weight
        #print("Best match for image " + str(i) + " is " + str(bestmatch) + " (" + str(bestmatch_val) + ")")
        if(bestmatch != -1):
            value,func = img_match(imgarr[bestmatch], imgarr[i])
            #print(func)
            transformations.append(func)
            result = imgmerge(result,imgarr[bestmatch],transformations)
        else:
            print("error: not a complete panorama")
        done_images.append(i)
        i = bestmatch
    cv2.imwrite('out.jpg',cv2.cvtColor(result, cv2.COLOR_RGB2BGR))
    print("written to out.jpg")


if __name__ == '__main__':
    print("reading images...")
    parser = argparse.ArgumentParser(description='Panorama generator.')
    parser.add_argument('input', nargs='+', help='One or more input files.')
    args = parser.parse_args()
    imgarr = []
    for i in args.input:
        img = cv2.imread(i);
        imgarr.append(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    panorama(imgarr)