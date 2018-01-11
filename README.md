# Kaffeekasse

Android Cashier app for coffee automata using NFC tags for user identification.

Features:
- User management
- SQL Database
- NFC user identification

##User import:
1. cleanup user excel sheet (leave only name and balance columns, no headers)
2. export as *kaffeekasse.csv* and store it next to *import.py*
3. reformat to UTF-8 without BOM (e.g. with NotePad++)
4. run *import.py*
5. adb push or copy *initial.db* to DB backup folder on device
6. in settings view import database *initial.db* 
