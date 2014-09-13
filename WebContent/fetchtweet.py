#!/usr/bin/env python
'''
Created on 13/11/2012

@author: aaron
'''
import tweepy
import sys
import os
import subprocess
import json
import couchdb #@UnresolvedImport
from optparse import OptionParser
import datetime
import time

class MyModelParser(tweepy.parsers.ModelParser):
    def parse(self, method, payload):
        result = super(MyModelParser, self).parse(method, payload)
        result._payload = json.loads(payload)
        return result

consumer_key = 'RG25udN0Rbe9QjE89yH7zySRV'
consumer_secret = '8Oxo7B5fqDHZXKmMpMM0YANdmMufvbbnihu1YtOnOu0wewERQo'
access_token = '166920597-peurMia6Fmy73rtuluutN6iGIo6H8AJllfeZOdCb'
access_token_secret = 'iaw8mDIj9i8SXq9k3CzEFJu0rW7lAWKCIqOpngX7tfh2A'

auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_token_secret)

api = tweepy.API(auth,parser=MyModelParser())

def parts(tweet_id):
    ids=str(tweet_id)
    part1=ids[0:4]
    part2=ids[4:8]
    #part3=ids[7:10] 
    return (part1, part2)

def reply_retweet_fetch(doc,db):
        print "Start tracing..."
        # if it is a retweet
        if doc.has_key('retweeted_status'):
            print "is a retweet"
            # get the root tweet
            retweeted= doc['retweeted_status']
            stored=False;
            # if root tweet can be found
            if retweeted['id_str'] in db:
                print "Source of retweet has already been stored in CouchDB database -- " ,retweeted
                stored=True
            # if cannot be found
            if not stored:
                retweeted['traced'] = True
                id_str=retweeted['id_str']
                # save it and index by id_str
                db[id_str]=dict(retweeted)
                print "New retweet is stored in CouchDB database -- " ,retweeted
                # repeat doing this function
                reply_retweet_fetch(retweeted,db)
        # if it is a reply tweet
        elif doc['in_reply_to_status_id_str'] != None:
            print "is a reply tweet"
            # get the root id_str
            reply_id_str=doc['in_reply_to_status_id_str']
            stored=False;
            # if the root tweet can be found
            if reply_id_str in db:
                print "Tweet that is replied has already been stored in CouchDB database -- " ,reply_id_str
                stored=True
            # if cannot be found
            if not stored:
                print "get tweet from Twitter"
                # get root tweet from Twitter
                result = api.get_status(id=reply_id_str)
                tweet = result._payload
                tweet['traced']=True
                print "Get From Twitter Server",tweet
                # sleep for 10sec
                time.sleep(5)
                # store it and index by id_str
                db[reply_id_str]=dict(tweet)
                print "New reply tweet is stored in CouchDB database -- " ,tweet
                # repeat doing this function
                reply_retweet_fetch(tweet,db)
        else:
            print "Skip as it is neither a retweet or reply tweet"

def fetch_test(option, opt, value, parser):
    # get the start time
    starttime=datetime.datetime.now()
    # Assuming localhost:5984
    couch = couchdb.Server()
    # get couchdb
    try:
        db = couch[parser.rargs[0]]
    except:
        print "database not exist"
        sys.exit(0)
    # an index that shows which document is currently processed
    currentpostion=1
    # the total number of documents
    dblen=len(db)
    for did in db:
        doc=db.get(did)
        print "Processing",currentpostion,"/",dblen
        try:
            # if a document is not traced
            if doc['traced'] == False:
                # start tracing
                doc['traced'] = True
                db.save(doc)
                reply_retweet_fetch(doc,db)
            # move to the next index
            currentpostion+=1
        except:
            # an exception can be found if nothing can be received from Twitter server
            currentpostion+=1
            continue
            
        
    endtime = datetime.datetime.now()
    timeperiod=endtime-starttime
    print "Done! The time taking is" ,timeperiod
    
if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-b","--base",dest="base",type="string",action="store",
                      default="./",help="the base of the tweet index")
    parser.add_option("-i","--id_str",dest="id_str",type="string",action="store",
                    default="1", help="the id of the tweet")
    parser.add_option("-y","--year",dest="year",type="string",action="store",
                      default=None,help="the year of the tweet")
    parser.add_option("-m","--month",dest="month",type="string",action="store",
                      default=None,help="the month of the tweet")
    parser.add_option("-d","--day",dest="day",type="string",action="store",
                      default=None,help="the day of the tweet")
    parser.add_option("-r","--hour",dest="hour",type="string",action="store",
                      default=None,help="the hour of the tweet")
    parser.add_option("-n","--minute",dest="minute",type="string",action="store",
                      default=None,help="the minute of the tweet")
    parser.add_option("-s","--skip",dest="skip",action="store_true",default=False,
                      help="skip the search step")
    parser.add_option("-f","--files",dest="files",type="string",action="store",
                      default=None,help="search these files only")
    parser.add_option("-l","--id_file",dest="id_file",type="string",action="store",
                      default=None,help="file of ids")
    # option added for fetching
    parser.add_option("-C","--couch", action='callback', callback=fetch_test,
                      help="fetch doc from couchdb")
    (options, args) = parser.parse_args()
    if options.id_file==None:
        id_str_list=options.id_str.split(',')
    else:
        try:
            fd=open(options.id_file,"r")
        except:
            sys.exit(0)
        id_str_list=fd.readlines()
        for i in range(0,len(id_str_list)):
            id_str_list[i]=id_str_list[i].rstrip()
        fd.close()
    filelist=[]
    
    '''
    Have the files to search been given?
    '''
    if(options.files!=None):
        filelist=options.files.split(',');
    else:
        '''
        Check for exact hits in the idstomin.dat
        '''
        hits=0
        for tid in id_str_list:
            dirname=options.base+"/IDS/%s/%s" % parts(tid)
            filename=dirname+"/idtomin.dat"
            try:
                fd=open(filename,"r")
            except:
                continue
            idlist=fd.readlines()
            for line in idlist:
                (atid,created_at)=line.split('\t')
                if(atid==tid):
                    hits+=1
                    [DayName, Month, Day, Time, Milli, Year]=created_at.split()
                    [Hour, Minute, Second]=Time.split(':')
                    hdirname=options.base+"/%s/%s/%s" % (Year,Month,Day)
                    hfilename=hdirname+"/"+Hour+"_"+Minute+".dat"
                    filelist.append(hfilename)
            fd.close()
        
        '''
        Add generic search paths if not every id was hit (this is very slow)
        '''
        
        if(hits<len(id_str_list) and (not options.skip)):
            '''
            Form the file expression for searching.
            '''
            fileexp=options.base
            usefind=True # should we use find command, otherwise use ls
            if(not options.year == None):
                fileexp+="/"+options.year+"/"
                if(not options.month == None):
                    fileexp+=options.month+"/"
                    if(not options.day == None):
                        fileexp+=options.day+"/"
                        usefind=False
                        if(not options.hour == None):
                            fileexp+=options.hour+"_"
                            if(not options.minute == None):
                                fileexp+=options.minute
            
            '''
            Find or list the set of files to search.
            '''
            #output=subprocess.Popen([ROOTDIR+"/bin/grep_tweets.sh","sample",keywords],
            #                        stdout=subprocess.PIPE).communicate()[0]
            if(usefind):
                output=subprocess.Popen(["find",fileexp,"-name", "*_*.dat"],
                                    stdout=subprocess.PIPE).communicate()[0]
            else:
                output=subprocess.Popen(["ls -1 "+fileexp+"*.dat"],
                                        stdout=subprocess.PIPE,shell=True).communicate()[0]               
            filelist.extend(output.split('\n')) 
    #print >> sys.stderr, filelist    
    
    '''
    Read through the files and find the tweet that matches the ID
    '''             
    for filename in filelist:
        #print >> sys.stderr, filename
        if not os.path.exists(filename):
            continue
        fd=open(filename,"r")
        tweetlist=fd.readlines()
        fd.close()
        for line in tweetlist:
            try:
                tweet=json.loads(line)
            except:
                continue
            #print >> sys.stderr, tweet
            if(not tweet.has_key("id_str")):
                continue
            if(tweet["id_str"] in id_str_list):
                print >> sys.stdout, line.rstrip()
                del id_str_list[id_str_list.index(tweet["id_str"])]
                if(len(id_str_list)==0): sys.exit(0)
            