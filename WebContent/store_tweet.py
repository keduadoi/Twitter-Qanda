#!/usr/bin/env python
'''
Created on 12/04/2011

@author: aaron
'''

from multiprocessing import Process, Queue
from optparse import OptionParser
import sys
import json
import os
import fcntl
import couchdb  # @UnresolvedImport
import datetime

def parts(tweet_id):
    ids=str(tweet_id)
    part1=ids[0:4]
    part2=ids[4:8]
#     part3=ids[7:10] 
    return (part1, part2)
    
def handle_tweet(q):
    running=True
    while running:
        work=q.get()
        if work['action']=='quit':
            running=False #superfluous, but anyway
            break
        if work['action']=='init':
            silent=work['silent']
            continue
        line=work['tweet']
        tweet={}
        try:
            tweet=json.loads(line)
        except:
            if(not silent):
                print """ problem with json object """
                print line 
            continue    
        if tweet.has_key('id') and tweet.has_key('created_at'):
            tweet_id=tweet['id']
            dirname=ROOTDIR+"/"+"/IDS/%s/%s" % parts(tweet_id)
            try:
                os.makedirs(dirname)
            except os.error:
                """ dir may already exist """    
            filename=dirname+"/ids.dat"
            filename2=dirname+"/idtomin.dat"
            try:
                if not os.path.exists(filename):
                    fd=open(filename, 'w')
                    fd.close()
                    fd2=open(filename2, 'w')
                    fd2.close()
                fd=open(filename,"r")
                fcntl.lockf(fd, fcntl.LOCK_SH)
                idlist=fd.readlines()
                fcntl.lockf(fd,fcntl.LOCK_UN)
                fd.close()
                if str(tweet_id)+"\n" in idlist:
                    print "duplicate: ",tweet_id
                    continue
                fd=open(filename,"a")
                fd2=open(filename2,"a")
                fcntl.lockf(fd, fcntl.LOCK_EX)
                fcntl.lockf(fd2, fcntl.LOCK_EX)
                fd.write(str(tweet_id)+"\n")
                fd2.write(str(tweet_id)+"\t"+tweet['created_at']+'\n')
                fcntl.lockf(fd2,fcntl.LOCK_UN)
                fcntl.lockf(fd,fcntl.LOCK_UN)
                fd.close()
                fd2.close()
            except IOError:
                """ blocking lock failed? """
                if(not silent):
                    print "Failed to get lock: ",filename
                continue
            created_at=tweet['created_at']
            [DayName, Month, Day, Time, Milli, Year]=created_at.split()
            [Hour, Minute, Second]=Time.split(':')
            dirname=ROOTDIR+"/%s/%s/%s" % (Year,Month,Day)
            try:
                os.makedirs(dirname)
            except os.error:
                """ dir may already exist """
            filename=dirname+"/"+Hour+"_"+Minute+".dat"
            try:
                fd=open(filename,"a")
                fcntl.lockf(fd, fcntl.LOCK_EX)
                fd.write(line)
                fcntl.lockf(fd, fcntl.LOCK_UN)
                fd.close()
            except IOError:
                """ blocking lock failed? """
                if(not silent):
                    print "Failed to get lock: ",filename


def store_test(option, opt, value, parser):
    # get the start time
    starttime=datetime.datetime.now()
    # Assuming localhost:5984
    couch = couchdb.Server()
    # get database
    try:
        couch.create(parser.rargs[0])
        db = couch[parser.rargs[0]]
    except:
        print "couchdb database exists"
        db = couch[parser.rargs[0]]
    # open file
    try:
        file_object=open(parser.rargs[1])
    except:
        print "ERROR: File cannot be found in this location\nUsage -- Python store_tweet.py -C [DBName] [FilePath]"
        sys.exit(0)
    # a list of file contents
    list=file_object.readlines()  # @ReservedAssignment
    # total tweets stored
    num=0
    for i in list:
            try:
                # convert to json object
                data_string = json.loads(i)
                # get the id_str
                id_str = data_string['id_str']
                # if id_str does not exists
                if id_str not in db:
                    data_string['traced'] = False
                    # index by id_str
                    db[id_str] = dict(data_string)
                    # total number + 1
                    num+=1
                    print "Tweet saved -- " ,data_string
            except:
                continue   
    # close the file
    file_object.close()
    # get the end time
    endtime = datetime.datetime.now()
    # calculate the running time
    timeperiod=endtime-starttime
    
    # if no tweet is stored
    if num ==0 :
        print "No tweet is stored"
    #if 1 tweet is stored
    elif num==1:
        print "1 tweet is stored"
    # more than 1
    else:
        print num,"tweets are stored and the time taking is",timeperiod
#     sys.exit(0)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-p","--processes",dest="processes",type="int",action="store",
                    default=5,help="number of processes to use")
    parser.add_option("-B","--batch",dest="batch",type="int",action="store",
                    default=1000000,help="tweet batch size")
    parser.add_option("-s","--silent",dest="silent",action="store_true",default=False,
                      help="Suppress all output.")
    parser.add_option("-R","--root",dest="rootdir",type="string",action="store",
                      default="/tmp", help="the database root directory")
    # option added for couchdb storage
    parser.add_option("-C","--couch", action='callback', callback=store_test,
                      help="read file and store to couchdb")
    (options, args) = parser.parse_args()
    # set the ROOTDIR
    ROOTDIR=options.rootdir;
    
    # construct the queues for each process
    workQueue=options.processes*[None]
    for i in range(0,options.processes): 
        workQueue[i]=Queue(maxsize=options.batch*2)

    # spawn each process
    workProcess=options.processes*[None]
    for i in range(0,options.processes): 
        workProcess[i]=Process(target=handle_tweet,args=(workQueue[i],))
        workProcess[i].start()
    
    work={}
    # send initialization data
    for i in range(0,options.processes): 
        work['action']='init'
        work['silent']=options.silent
        workQueue[i].put(work.copy())
    
    work.clear()
    
    # start with the first process
    processCounter=0;
    ticker=options.batch
    
    for line in sys.stdin:
        work['action']='work'
        work['tweet']=line
        workQueue[processCounter].put(work.copy())
        ticker=ticker-1
        if ticker==0:
            processCounter=(processCounter+1) % options.processes 
            ticker=options.batch
            
    # terminate all processes and join
    for i in range(0,options.processes):
        work['action']='quit'
        workQueue[i].put(work)
        workProcess[i].join()
    
            
