#!/usr/bin/env python

import tweepy
import json
import couchdb  # @UnresolvedImport
import time
from optparse import OptionParser
import sys

head="digraph T {\n"
tail="}"
def trace_tweet(option, opt, value, parser):
    # Assuming localhost:5984
    couch = couchdb.Server() 
    # get database
    try:
        db = couch[parser.rargs[0]]
    except:
        print "Database not exist"
        sys.exit(0)
    # open file, if doesnt exist then create it
    # the name of the file is the name of database
    #myfile=open(db.name+'.dot','w+')
    #myfile.write(head)
    print (head)
    for index in db:
        doc=db.get(index)
        # if it is a retweet
        if doc.has_key('retweeted_status'):
            #print "It is a rewteet"
            # get root tweet
            root=doc['retweeted_status']
            # get root id_str
            root_id_str=root['id_str']
            # get the time of root
            root_time=root['created_at']
            # get the screen name of root
            root_name=root['user']['screen_name']
            # get the text of root 
            root_text=repr(root['text'])
            
            # get retweet id_str
            retweet_id_str=doc['id_str']
            # get the time of retweet
            retweet_time=doc['created_at']
            # get the screen name of retweet
            retweet_name=doc['user']['screen_name']
            # get the text of retweet 
            retweet_text=repr(doc['text'])
            #print root_id_str,"->",retweet_id_str
            # write to dot file
            print (create_lable(retweet_id_str,retweet_time,retweet_name,retweet_text))
            print (create_lable(root_id_str,root_time,root_name,root_text))
            print (root_id_str+" -> "+retweet_id_str+"\n")
        
        # if it is a reply tweet
        elif doc.has_key('in_reply_to_status_id_str'):
            if doc['in_reply_to_status_id_str'] != None:
                #print "It is a reply tweet"
                # get the root id_str
                reply_root_id_str=doc['in_reply_to_status_id_str']
                # try to get the tweet from database
                try:
                    reply_root=db.get(reply_root_id_str)
                    # get the time
                    reply_root_time=reply_root['created_at']
                    # get the screen name
                    reply_root_name=reply_root['user']['screen_name']
                    # get the text
                    reply_root_text=repr(reply_root['text'])
                # if cannot be found
                except:
                    # here I give it some error msg
                    reply_root_time='created_time_unknown'
                    reply_root_name='screen_name_unknown'
                    reply_root_text='text_unknown'
                    
                # get the id_str of reply_tweet
                reply_id_str=doc['id_str']
                # get the time
                reply_time=doc['created_at']
                # get the screen name
                reply_name=doc['user']['screen_name']
                # get the text
                reply_text=repr(doc['text'])
                #print reply_root_id_str,"->",reply_id_str
                # write to dot file
                print (create_lable(reply_id_str,reply_time,reply_name,reply_text))
                print (create_lable(reply_root_id_str,reply_root_time,reply_root_name,reply_root_text))
                print (reply_root_id_str+" -> "+reply_id_str+"\n")
    '''myfile.write(tail)
    myfile.flush()
    myfile.close()'''
    print (tail)
# this function is used to create each label content in dot file
def create_lable(id_str,time,name,text):
    text_br='<br/>'.join(text[i:i+40] for i in range(0, len(text), 40))
    text_br = text_br.replace("\"","\\\"")
    out_str= id_str+" [label=\"<b>"+time+"</b><br/>"+name+": "+text_br+"\"]\n"
    return out_str

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-T","--trace", action='callback', callback=trace_tweet,
                      help="read file and store to couchdb")
    (options, args) = parser.parse_args()