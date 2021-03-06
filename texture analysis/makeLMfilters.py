import numpy as np
def makeLMfilters():
# Returns the LML filter bank of size 49x49x48 in F. To convolve an
# image I with the filter bank you can either use the matlab function
# conv2, i.e. responses(:,:,i)=conv2(I,F(:,:,i),'valid'), or use the
# Fourier transform.

  SUP=49;                 # Support of the largest filter (must be odd)
  SCALEX=np.multiply(sqrt(2),range(1,4));  # Sigma_{x} for the oriented filters
  NORIENT=6;              # Number of orientations

  NROTINV=12;
  NBAR=length(SCALEX)*NORIENT;
  NEDGE=length(SCALEX)*NORIENT;
  NF=NBAR+NEDGE+NROTINV;
  F=zeros(SUP,SUP,NF);
  hsup=(SUP-1)/2;
  [x,y]=meshgrid(range(-hsup,hsup+1),range(hsup,-hsup-1,-1));
  orgpts=np.transpose([x[:], y[:]]);

  count=1;
  for scale in range(1,length(SCALEX)+1):
    for orient in range(0,NORIENT):
      angle=pi*orient/NORIENT;  # Not 2pi as filters have symmetry
      c=cos(angle);s=sin(angle);
      rotpts=[[c, -s],[s, c]]*orgpts;
      F[:,:,count]=makefilter(SCALEX(scale),0,1,rotpts,SUP);
      F[:,:,count+NEDGE]=makefilter(SCALEX(scale),0,2,rotpts,SUP);
      count=count+1;
    end;
  end;
  
  count=NBAR+NEDGE+1;
  SCALES=np.power(sqrt(2),range(1,5));
  for i in range(1,length(SCALES)+1):
    F[:,:,count]=normalise(fspecial('gaussian',SUP,SCALES(i)));
    F[:,:,count+1]=normalise(fspecial('log',SUP,SCALES(i)));
    F[:,:,count+2]=normalise(fspecial('log',SUP,3*SCALES(i)));
    count=count+3;
  end;
  return F

def makefilter(scale,phasex,phasey,pts,sup):
  gx=gauss1d(3*scale,0,pts[1,:],phasex);
  gy=gauss1d(scale,0,pts[2,:],phasey);
  f=normalise(reshape(np.multiply(gx,gy),sup,sup));
  return f

def gauss1d(sigma,mean,x,ord):
# Function to compute gaussian derivatives of order 0 <= ord < 3
# evaluated at x.

  x=x-mean;num=np.multiply(x,x);
  variance=sigma^2;
  denom=2*variance; 
  g=exp(-num/denom)/sqrt(pi*denom);
  if (ord == 1):
    g=np.multiply(-g,(x/variance));
  elif (ord == 2):
    g=(np.multiply(g,((num-variance)/(variance^2))));
  return g

def normalise(f):
  f=f-mean(f[:])
  f=f/sum(abs(f[:]))
  return f