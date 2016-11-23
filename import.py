#!/usr/bin/python
# -*- coding: utf-8 -*-

import csv
import sqlite3
import os
import datetime
import string

DBFILE = "initial.db"

def unicode_csv_reader(utf8_data, dialect=csv.excel, **kwargs):
    csv_reader = csv.reader(utf8_data, dialect=dialect, **kwargs)
    for row in csv_reader:
        yield [unicode(cell, 'utf-8') for cell in row]

def setup_tables(c):
    c.execute('''CREATE TABLE users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT,
              nfcid TEXT,
              role TEXT
              )''')
    c.execute('''CREATE TABLE payments (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              datetime TEXT,
              userid INTEGER,
              amount DOUBLE,
              FOREIGN KEY(userid) REFERENCES users(id) ON DELETE CASCADE
              )''')
    c.execute("CREATE TABLE android_metadata (locale TEXT)")
    c.execute("INSERT INTO android_metadata (locale) VALUES ('en_US')")
    c.execute("PRAGMA schema_version = 1")
    c.execute("PRAGMA user_version = 1")

def insert_user(c, user, balance, timestamp):
    print user, balance
    c.execute("INSERT INTO users (name, role, nfcid) VALUES (?, ?, ?)", [user, u"user", u""])
    uid = c.lastrowid
    c.execute("INSERT INTO payments (datetime, userid, amount) VALUES (?, ?, ?)", [timestamp, uid, balance])

def format_data(user, balance):
    # split user name
    user = unicode(user, "utf8")
    names = user.split()
    user = names[1] + " " + names[0]
    user = string.replace(user, ",", "")
    # split balance, comma to dot
    balance = string.replace(balance.split()[0], ",", ".")
    balance = float(balance)
    return (user, balance)

with open("kaffeekasse.csv", "rb") as csvfile:
    reader = csv.reader(csvfile, delimiter=';', quotechar='"')
    timestamp = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    if (os.path.isfile(DBFILE)):
        os.remove(DBFILE)
    conn = sqlite3.connect(DBFILE)
    c = conn.cursor()
    setup_tables(c)
    for row in reader:
        (user, balance) = format_data(row[0], row[1])
        insert_user(c, user, balance, timestamp)
    conn.commit()
    conn.close()
