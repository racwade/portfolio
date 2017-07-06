
# coding: utf-8

# In[1]:

import scipy as sp
import numpy as np
import skfuzzy as fuzz


# In[2]:

min_classification = 0.8 #Probability for how much fuzzy membership an item needs in order to be classified as that class
min_truth = 0.97 #Probability for how much truth a node needs to be deemed a true predictor.
def truth_function(value):
    #truth function; varies per example
    #in this example, we're looking for good wine (quality >= 7)
    if(value >= 7):
        return 1
    else:
        return 0


# In[3]:

def best_means(data,minmeans,maxmeans):
    #Calculates the best mean centers for the data clusters, based on fpc (fuzzy partition coefficient)
    final_fpc = 0
    for i in range(minmeans,maxmeans):
        cntr, u, u0, d, jm, p, fpc = fuzz.cluster.cmeans(data, i, 2, error=0.005, maxiter=1000)
        if(fpc > final_fpc):
            final_cntr = cntr
            final_u = u
            final_fpc = fpc
    return(final_cntr,final_u,final_fpc)


# In[4]:

#Load in our test data, and split it arbitrarily into training and test data.
file = np.genfromtxt("wq.csv", delimiter=',')
parameters_train = file[:999,0:10]
parameters_test = file[1000:,0:10]
target_train = file[:999,11]
target_test = file[1000:,11]


# In[5]:

#Fuzzify both our data sets.
centers = []
groups = []
fuzzydata = []
fpcs = []
fuzzydata_test = []
for j in range(0,10):
    cntr, u, fpc = best_means(np.array(parameters_train[:,j],ndmin=2).astype(float),2,10)
    centers.append(cntr)
    fuzzydata.append(u)
    groups.append(len(u))
    fpcs.append(fpc)
for k in range(0,10):
    u,u0,d,jm,p,fpc = fuzz.cluster.cmeans_predict(np.array(parameters_test[:,k],ndmin=2).astype(float),centers[k],2, error=0.005, maxiter=1000)
    fuzzydata_test.append(u)


# In[6]:


def generate_tree(elements, attribute, rem_attributes, last_prediction, cursor):
    #Generates a tree containing text elements.
    #elements: list of element indexes
    #attribute: index of the current attribute we're looking at
    #rem_attributes: list of all attribute indexes that haven't been used
    #last_prediction: stored value of what we last predicted
    #cursor: where we currently are in the array
    next_attribute = -1
    nodes = []
    if(len(rem_attributes) > 0):
        #as long as we have more attributes to test on, grab the best one
        rem_fpcs = []
        for index in rem_attributes:
            rem_fpcs.append(fpcs[index])
        best_new_attribute_value = max(rem_fpcs)
        next_attribute = fpcs.index(best_new_attribute_value)
        rem_attributes.remove(next_attribute)
        
    num_groups = groups[attribute] #number of groups we need to create things for
    for i in range(0,num_groups):
        group_elems = []
        for e in elements:
            if(fuzzydata[attribute][i][e] > min_classification):
                group_elems.append(e)
        #appended all relevant elements. now...
        if(len(group_elems) == 0):
            #this group was empty. use the last prediction.
            cmd_tree.insert(cursor+i,last_prediction)
            continue
        truth = 0
        for e in elements:
            truth = truth + truth_function(target_train[e])
        truth = truth / len(elements)
        output = "0"
        if(truth > 0.5):
            output = "1"
        if(truth > min_truth or 1 - truth > min_truth or next_attribute == -1):
            #Category was statistically significant.
            #OR
            #Category was not significant, but no more attributes.
            cmd_tree.insert(cursor+i,output)
            continue
        else:
            #Push these values back. We'll deal with them in time.
            #We have to solve the regular nodes first.
            cmd_tree.insert(cursor+i,"PLACEHOLDER")
            nodes.append((i,list(group_elems),next_attribute,list(rem_attributes),output,cursor))
    for n in nodes:
        i,group_elems,next_attribute,rem_attributes,output,curs = n
        cmd_tree[curs+i] = (str(len(cmd_tree)) + "," + str(next_attribute))
        generate_tree(group_elems,next_attribute,rem_attributes,output,len(cmd_tree))
    return


# In[7]:

cmd_tree = []
attrs = list(range(0,10))
elems = list(range(0,len(target_train)))
rem_fpcs = []
for index in attrs:
    rem_fpcs.append(fpcs[index])
best_new_attribute_value = max(rem_fpcs)
start_attribute = rem_fpcs.index(best_new_attribute_value)
attrs.remove(start_attribute)
generate_tree(elems, start_attribute, attrs, "ERROR",0)


# In[8]:

def predict(e_index):
    cursor = 0
    attr = start_attribute
    while(True):
        grouping = []
        for i in range(0,groups[attr]):
            grouping.append(fuzzydata_test[attr][i][e_index])
        best_group = max(grouping)
        b_g_index = list(grouping).index(best_group)
        command = cmd_tree[cursor+b_g_index]
        if ',' in command:
            commands = command.split(",")
            cursor = int(commands[0])
            attr = int(commands[1])
            continue
        else:
            return int(command)

def actual(e_index):
    return truth_function(target_test[e_index])


# In[9]:

correct = 0
for i in range(0,len(target_test)):
    if(predict(i) == actual(i)):
        correct = correct + 1
print("Prediction accuracy: " + str(correct/len(target_test)))


# In[ ]:



